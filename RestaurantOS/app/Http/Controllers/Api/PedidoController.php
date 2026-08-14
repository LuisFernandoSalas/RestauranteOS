<?php 

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Requests\GuardarPedidoRequest;
use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\Producto;
use App\Models\Combo;
use App\Models\DetallePedido;
use App\Models\Receta;
use App\Models\Insumo;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Http\JsonResponse;

class PedidoController extends Controller
{
    /**
     * GET /api/pedidos
     * Lista general de pedidos activos para los Meseros y el panel general.
     */
    public function index(Request $request): JsonResponse
    {
        $pedidos = Pedido::with([
            'mesero:id,name', 
            'mesa:id,numero', 
            'detalles.producto', 
            'detalles.combo.productos'
        ])
            ->whereIn('estado', ['pendiente', 'en_preparacion', 'listo', 'entregado'])
            ->orderBy('created_at', 'asc')
            ->get()
            ->map(function ($pedido) {
                // Inyectamos el total calculado al JSON dinámicamente sin causar N+1
                $pedido->total_dinamico = $pedido->total_calculado;
                return $pedido;
            });

        return response()->json($pedidos, 200);
    }

    /**
     * POST /api/pedidos
     * Motor de Recepción de Comandas Masivas (App Android del Mesero)
     */
    public function store(GuardarPedidoRequest $request): JsonResponse
    {
        // 1. Candado de Idempotencia
        $pedidoExistente = Pedido::where('client_uuid', $request->client_uuid)->first();
        if ($pedidoExistente) {
            return response()->json([
                'status' => 'success',
                'mensaje' => 'La comanda ya había sido registrada previamente.',
                'data' => [
                    'pedido_id' => $pedidoExistente->id,
                    'total' => $pedidoExistente->total_calculado
                ]
            ], 200);
        }

        // 2. Transacción Atómica con control de Stock
        try {
            return DB::transaction(function () use ($request) {
                $mesa = Mesa::findOrFail($request->mesa_id);
                if ($mesa) {
                    $mesa->estado = 'ocupada';
                    $mesa->save();
                }

                $pedido = Pedido::create([
                    'client_uuid' => $request->client_uuid,
                    'mesa_id'     => $mesa->id,
                    'user_id'     => $request->user()?->id,
                    'estado'      => 'pendiente',
                ]);

                $totalPedido = 0.00;

                // Soporte híbrido: Acepta tanto la llave 'items' como la legacy 'productos'
                $items = $request->items ?? $request->productos ?? [];

                // ---------------------------------------------------------
                // 🚀 FUNCIÓN INTERNA REUTILIZABLE PARA DESCUENTO DE STOCK
                // ---------------------------------------------------------
                $descontarStockProducto = function ($productoId, $cantidadUnidades, $nombreContexto) {
                    $recetas = Receta::where('producto_id', $productoId)->get();

                    foreach ($recetas as $receta) {
                        $insumo = Insumo::where('id', $receta->insumo_id)
                            ->lockForUpdate()
                            ->first();

                        if ($insumo) {
                            $cantidadADescontar = $receta->cantidad_por_porcion * $cantidadUnidades;

                            // 🛑 Detener la comanda si no alcanza el inventario en almacén
                            if ($insumo->stock_actual < $cantidadADescontar) {
                                throw new \Exception("Stock insuficiente de '{$insumo->nombre}' para preparar '{$nombreContexto}'. Stock disponible: {$insumo->stock_actual} {$insumo->unidad_medida}.");
                            }

                            $insumo->decrement('stock_actual', $cantidadADescontar);
                        }
                    }
                };

                // ---------------------------------------------------------
                // 🛒 PROCESAMIENTO DE ÍTEMS (PRODUCTOS O COMBOS)
                // ---------------------------------------------------------
                foreach ($items as $item) {
                    $productoId = $item['producto_id'] ?? $item['id'] ?? null;
                    $comboId    = $item['combo_id'] ?? null;

                    if ($productoId) {
                        // --- PROCESAR PRODUCTO INDIVIDUAL ---
                        $producto = Producto::findOrFail($productoId);
                        
                        // Descontar inventario según la receta del producto
                        $descontarStockProducto($producto->id, $item['cantidad'], $producto->nombre);

                        $subtotal = $producto->precio * $item['cantidad'];
                        $totalPedido += $subtotal;

                        DetallePedido::create([
                            'pedido_id'       => $pedido->id,
                            'producto_id'     => $producto->id,
                            'combo_id'        => null,
                            'cantidad'        => $item['cantidad'],
                            'precio_unitario' => $producto->precio,
                            'nota'            => $item['nota'] ?? null,
                            'estado'          => 'pendiente',
                            'subtotal'        => $subtotal
                        ]);

                    } elseif ($comboId) {
                        // --- PROCESAR COMBO ---
                        $combo = Combo::with('productos')->findOrFail($comboId);

                        if ($combo->estado !== 'activo') {
                            throw new \Exception("El combo '{$combo->nombre}' no está disponible actualmente.");
                        }

                        // Descontar inventario de CADA producto incluido en el combo
                        foreach ($combo->productos as $prodEnCombo) {
                            $cantidadTotalReceta = $prodEnCombo->pivot->cantidad * $item['cantidad'];
                            $descontarStockProducto($prodEnCombo->id, $cantidadTotalReceta, "{$prodEnCombo->nombre} (incluido en Combo '{$combo->nombre}')");
                        }

                        $subtotal = $combo->precio_especial * $item['cantidad'];
                        $totalPedido += $subtotal;

                        DetallePedido::create([
                            'pedido_id'       => $pedido->id,
                            'producto_id'     => null,
                            'combo_id'        => $combo->id,
                            'cantidad'        => $item['cantidad'],
                            'precio_unitario' => $combo->precio_especial,
                            'nota'            => $item['nota'] ?? null,
                            'estado'          => 'pendiente',
                            'subtotal'        => $subtotal
                        ]);
                    }
                }

                $mesa->update(['estado' => 'ocupada']);

                // Broadcasting con las relaciones de productos y combos cargadas
                broadcast(new \App\Events\PedidoCreado($pedido->load(['mesero', 'mesa', 'detalles.producto', 'detalles.combo'])))->toOthers();

                return response()->json([
                    'status'  => 'success',
                    'mensaje' => '¡Orden enviada e inventario actualizado!',
                    'data'    => [
                        'pedido_id' => $pedido->id,
                        'total'     => $totalPedido
                    ]
                ], 201);
            });
        } catch (\Exception $e) {
            // Si falta stock de algún insumo, el rollback automático cancela todo el pedido de forma segura
            return response()->json([
                'status'  => 'error',
                'mensaje' => $e->getMessage()
            ], 422);
        }
    }

    /**
     * GET /api/pedidos/{id}
     */
    public function show($id): JsonResponse
    {
        $pedido = Pedido::with([
            'detalles.producto', 
            'detalles.combo.productos', 
            'mesero:id,name', 
            'mesa:id,numero'
        ])->find($id);

        if (!$pedido) {
            return response()->json(['message' => 'No encontrado'], 404);
        }

        $pedidoArray = $pedido->toArray();
        $pedidoArray['total'] = $pedido->total_calculado;

        return response()->json($pedidoArray, 200);
    }

    /**
     * PUT /api/pedidos/{id}
     */
    public function update(Request $request, $id): JsonResponse
    {
        $pedido = Pedido::findOrFail($id);
        $request->validate(['estado' => 'required|in:pendiente,en_preparacion,listo,entregado,cobro']);

        $pedido->update(['estado' => $request->estado]);

        broadcast(new \App\Events\PedidoActualizado($pedido))->toOthers();

        return response()->json([
            'status'  => 'success',
            'message' => 'Actualizado a: ' . $request->estado
        ]);
    }

    /**
     * DELETE /api/pedidos/{id}
     */
    public function destroy($id, Request $request): JsonResponse
    {
        if (!$request->user()?->isAdmin()) {
            return response()->json(['Error' => 'No autorizado'], 403);
        }

        $pedido = Pedido::findOrFail($id);

        if ($pedido->mesa) {
            $pedido->mesa->update(['estado' => 'libre']);
        }

        $pedido->delete();

        return response()->json(['status' => 'success', 'message' => 'Pedido eliminado de forma lógica (auditable)']);
    }

    /**
     * GET /api/pedidos/mesa/{mesa_id}/activo
     * Recupera el pedido activo de una mesa (para restaurar estado en Android)
     */
    public function porMesa($mesa_id): JsonResponse
    {
        $pedido = Pedido::with(['detalles.producto', 'mesero:id,name', 'mesa:id,numero'])
            ->where('mesa_id', $mesa_id)
            ->whereNotIn('estado', ['pagado', 'cancelado'])
            ->latest()
            ->first();

        if (!$pedido) {
            return response()->json([
                'status'  => 'empty',
                'message' => 'Mesa libre / Sin pedido activo',
                'data'    => null
            ], 200);
        }

        $pedidoArray = $pedido->toArray();
        $pedidoArray['total'] = $pedido->total_calculado;

        return response()->json([
            'status' => 'success',
            'data'   => $pedidoArray
        ], 200);
    }
}