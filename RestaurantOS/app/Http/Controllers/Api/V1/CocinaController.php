<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\UpdateDetalleEstadoRequest;
use App\Http\Requests\CancelarPedidoRequest;
use App\Http\Requests\PausarProductoRequest;
use App\Models\Pedido;
use App\Models\DetallePedido;
use App\Models\Producto;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class CocinaController extends Controller
{
    /**
     * GET /api/v1/cocina/pedidos
     * Obtiene todas las comandas activas en la cocina.
     */
    public function index(): JsonResponse
    {
        $pedidos = Pedido::whereIn('estado', ['pendiente', 'en_preparacion', 'pausado', 'listo'])
            ->with(['mesa', 'mesero:id,name', 'detalles.producto:id,nombre'])
            ->orderBy('created_at', 'asc')
            ->get();

        $comandas = $pedidos->map(function ($pedido) {
            return [
                'pedido_id' => $pedido->id,
                'mesa' => $pedido->mesa ? $pedido->mesa->numero : 'Barra',
                'mesero' => $pedido->mesero ? $pedido->mesero->name : 'N/A',
                'estado_general' => $pedido->estado,
                'tiempo_activo_minutos' => Carbon::parse($pedido->created_at)->diffInMinutes(Carbon::now()),
                'platillos' => $pedido->detalles->map(function ($detalle) {
                    return [
                        'detalle_id' => $detalle->id,
                        'producto_id' => $detalle->producto->id,
                        'producto' => $detalle->producto->nombre,
                        'cantidad' => $detalle->cantidad,
                        'nota' => $detalle->nota,
                        'estado_platillo' => $detalle->estado
                    ];
                })
            ];
        });

        return response()->json([
            'status' => 'success',
            'resumen' => [
                'total_comandas' => $comandas->count(),
                'en_preparacion' => $pedidos->where('estado', 'en_preparacion')->count(),
                'pausados' => $pedidos->where('estado', 'pausado')->count(),
                'pendientes' => $pedidos->where('estado', 'pendiente')->count(),
            ],
            'comandas' => $comandas
        ], 200);
    }

    /**
     * PATCH /api/v1/cocina/detalles/{id}/estado
     * Cambia el estado de un PLATILLO INDIVIDUAL (en_preparacion, pausado, listo, cancelado)
     */
    public function updatePlatilloEstado(UpdateDetalleEstadoRequest $request, $id): JsonResponse
    {
        // Cargar el detalle junto con su producto base
        $detalle = DetallePedido::with('producto')->findOrFail($id);
        
        // 1. Verificamos si el producto base se encuentra pausado temporalmente
        $productoEstaPausado = $detalle->producto 
            && $detalle->producto->pausado_hasta 
            && Carbon::parse($detalle->producto->pausado_hasta)->isFuture();

        // 2. REGLA DE NEGOCIO: Bloquear 'listo' si el platillo o el producto base están pausados
        if (($detalle->estado === 'pausado' || $productoEstaPausado) && $request->estado === 'listo') {
            return response()->json([
                'status' => 'error',
                'mensaje' => 'No puedes marcar este platillo como listo porque el producto se encuentra pausado.'
            ], 422);
        }

        DB::transaction(function () use ($detalle, $request) {
            $detalle->update([
                'estado' => $request->estado
            ]);

            $pedido = $detalle->pedido;

            // Si el chef pone un platillo en preparación y el pedido estaba pendiente/pausado
            if ($request->estado === 'en_preparacion' && in_array($pedido->estado, ['pendiente', 'pausado'])) {
                $pedido->update(['estado' => 'en_preparacion']);
            }

            // Si todos los platillos activos están 'listos' o 'cancelados', el pedido completo pasa a 'listo'
            $platillosPendientes = $pedido->detalles()
                ->whereNotIn('estado', ['listo', 'cancelado'])
                ->exists();

            if (!$platillosPendientes) {
                $pedido->update(['estado' => 'listo']);
            }
        });

        return response()->json([
            'status' => 'success',
            'mensaje' => 'Estado del platillo actualizado correctamente.',
            'data' => [
                'detalle_id' => $detalle->id,
                'nuevo_estado' => $detalle->estado,
                'pedido_estado_general' => $detalle->pedido->fresh()->estado
            ]
        ], 200);
    }

    /**
     * PATCH /api/v1/cocina/pedidos/{id}/estado
     * Cambia el estado del PEDIDO COMPLETO (en_preparacion, pausado, listo, cancelado)
     */
    public function updatePedidoEstado(Request $request, $id): JsonResponse
    {
        $request->validate([
            'estado' => ['required', Rule::in(['pendiente', 'en_preparacion', 'pausado', 'listo', 'cancelado'])]
        ]);

        $pedido = Pedido::findOrFail($id);

        DB::transaction(function () use ($pedido, $request) {
            $pedido->update(['estado' => $request->estado]);

            // Cascada opcional: Al cambiar estado del pedido, sincronizar sus platillos internos
            if (in_array($request->estado, ['en_preparacion', 'pausado', 'listo'])) {
                $pedido->detalles()
                    ->where('estado', '!=', 'cancelado')
                    ->update(['estado' => $request->estado]);
            }
        });

        return response()->json([
            'status' => 'success',
            'mensaje' => "El pedido #{$pedido->id} ha cambiado a estado '{$pedido->estado}'.",
            'data' => [
                'pedido_id' => $pedido->id,
                'nuevo_estado' => $pedido->estado
            ]
        ], 200);
    }

    /**
     * POST /api/v1/cocina/pedidos/{id}/cancelar
     */
    public function cancelarPedido(CancelarPedidoRequest $request, $id): JsonResponse
    {
        $pedido = Pedido::findOrFail($id);

        if ($pedido->estado === 'pagado') {
            return response()->json(['error' => 'No se puede cancelar una orden ya cobrada y facturada.'], 422);
        }

        DB::transaction(function () use ($pedido, $request) {
            $pedido->update([
                'estado' => 'cancelado',
                'motivo_cancelacion' => $request->motivo_cancelacion,
                'cobrado_por' => $request->user()?->id ?? $pedido->user_id,
            ]);

            $pedido->detalles()->update(['estado' => 'cancelado']);

            if ($pedido->mesa) {
                $pedido->mesa->update(['estado' => 'libre']);
            }

            $pedido->delete();
        });

        return response()->json([
            'status' => 'success',
            'mensaje' => 'Pedido cancelado con éxito. Se ha registrado la merma en auditoría.'
        ], 200);
    }

    /**
     * POST /api/v1/cocina/productos/{id}/pausar
     * Función 86: Desactiva un producto temporalmente del menú digital.
     */
    public function pausarProducto(PausarProductoRequest $request, $id): JsonResponse
    {
        $producto = Producto::findOrFail($id);
        $duracion = $request->duracion;

        $pausadoHasta = match ($duracion) {
            '30_min' => Carbon::now()->addMinutes(30),
            '1_hora' => Carbon::now()->addHour(),
            'indefinido' => Carbon::now()->addYears(10),
        };

        DB::transaction(function () use ($producto, $pausadoHasta) {
            // 1. Desactivamos el producto base del menú digital
            $producto->update([
                'pausado_hasta' => $pausadoHasta
            ]);

            // 2. Sincronizamos las comandas activas: cambia los detalles a 'pausado'
            DetallePedido::where('producto_id', $producto->id)
                ->whereIn('estado', ['pendiente', 'en_preparacion'])
                ->update(['estado' => 'pausado']);
        });

        return response()->json([
            'status' => 'success',
            'mensaje' => "El producto '{$producto->nombre}' ha sido pausado.",
            'pausado_hasta' => $producto->pausado_hasta->toDateTimeString(),
            'is_disponible' => false
        ], 200);
    }
}