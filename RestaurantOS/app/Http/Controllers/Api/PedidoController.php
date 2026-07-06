<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\Producto;
use App\Models\DetallePedido;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class PedidoController extends Controller
{
    public function index(Request $request)
    {
        $pedidos = Pedido::with(['user:id,name', 'mesa:id,numero', 'detalles.producto'])
            ->whereIn('status', ['Activo', 'pendiente', 'en_preparacion', 'listo'])
            ->orderBy('created_at', 'asc')
            ->get();
            
        return response()->json($pedidos, 200);
    }

    /**
     * Monitor de Cocina: Lista de platillos pendientes
     * Ruta: GET /api/pedidos/cocina
     */
    public function getPedidosCocina()
    {
        return DetallePedido::with(['producto:id,nombre', 'pedido.mesa:id,numero'])
            ->whereIn('status', ['solicitado', 'en_preparacion'])
            ->orderBy('created_at', 'asc')
            ->get();
    }

    public function store(Request $request)
    {
        if (!$request->user()->isMesero() && !$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso no autorizado'], 403);
        }

        $request->validate([
            'client_uuid' => 'required|string|size:36',
            'mesa_id' => 'required|exists:mesas,id',
            'productos' => 'required|array|min:1',
            'productos.*.producto_id' => 'required|exists:productos,id',
            'productos.*.cantidad' => 'required|integer|min:1',
            'productos.*.nota' => 'nullable|string|max:255'
        ]);

        $pedidoExistente = Pedido::where('client_uuid', $request->client_uuid)->first();
        if ($pedidoExistente) {
            return response()->json(['status' => 'idempotente_success', 'data' => $pedidoExistente], 200);
        }

        return DB::transaction(function () use ($request) {
            $pedido = Pedido::create([
                'client_uuid' => $request->client_uuid,
                'mesa_id'     => $request->mesa_id,
                'user_id'     => $request->user()->id,
                'status'      => 'Activo',
                'total'       => 0.00
            ]);

            $totalPedido = 0.00;

            foreach ($request->productos as $item) {
                $producto = Producto::findOrFail($item['producto_id']);
                $subtotal = $producto->precio * $item['cantidad'];
                $totalPedido += $subtotal;

                DetallePedido::create([
                    'pedido_id'        => $pedido->id,
                    'producto_id'      => $item['producto_id'],
                    'cantidad'         => $item['cantidad'],
                    'nota'             => $item['nota'] ?? null,
                    'status'           => 'solicitado',
                    'subtotal'         => $subtotal
                ]);
            }

            $pedido->update(['total' => $totalPedido]);
            Mesa::find($request->mesa_id)->update(['status' => 'ocupado']);

            broadcast(new \App\Events\PedidoCreado($pedido->load(['user', 'mesa', 'detalles'])))->toOthers(); 

            return response()->json(['status' => 'success', 'pedido_id' => $pedido->id, 'total' => $totalPedido], 201);
        });
    }

    public function show($id)
    {
        $pedido = Pedido::with(['detalles.producto', 'user:id,name', 'mesa:id,numero'])->find($id);
        return $pedido ? response()->json($pedido, 200) : response()->json(['message' => 'No encontrado'], 404);
    }

    public function update(Request $request, $id)
    {
        $pedido = Pedido::findOrFail($id);
        $request->validate(['status' => 'required|in:pendiente,en_preparacion,listo,entregado,cancelado']);

        if ($request->user()->isCocinero() && in_array($request->status, ['entregado', 'cancelado'])) {
            return response()->json(['Error' => 'Cocina no puede marcar pedidos como entregados/cancelados'], 403);
        }

        $pedido->update(['status' => $request->status]);
        broadcast(new \App\Events\PedidoActualizado($pedido))->toOthers();

        return response()->json(['status' => 'success', 'message' => 'Actualizado a: ' . $request->status]);
    }

    public function cobrar(Request $request, $id)
    {
        if (!$request->user()->isCajero() && !$request->user()->isAdmin()) {
            return response()->json(['message' => 'Acceso no autorizado'], 403);
        }

        $request->validate([
            'metodo_pago'   => 'required|in:Efectivo,Tarjeta,Mixto',
            'pago_efectivo' => 'required|numeric|min:0',
            'pago_tarjeta'  => 'required|numeric|min:0',
            'propina'       => 'nullable|numeric|min:0',
        ]);

        $pedido = Pedido::findOrFail($id);
        
        // Validación Financiera
        $totalRecibido = $request->pago_efectivo + $request->pago_tarjeta;
        if ($totalRecibido < $pedido->total) {
            return response()->json(['error' => 'El pago recibido es insuficiente. Total: ' . $pedido->total], 400);
        }

        DB::beginTransaction();
        try {
            $pedido->update([
                'status'        => 'pagado',
                'metodo_pago'   => $request->metodo_pago,
                'pago_efectivo' => $request->pago_efectivo,
                'pago_tarjeta'  => $request->pago_tarjeta,
                'propina'       => $request->propina ?? 0,
            ]);

            Mesa::find($pedido->mesa_id)->update(['status' => 'libre']);
            
            DB::commit();
            broadcast(new \App\Events\PedidoActualizado($pedido))->toOthers();
            
            return response()->json(['status' => 'success', 'message' => 'Cobro exitoso', 'cambio' => $totalRecibido - $pedido->total]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['error' => 'Error al cobrar'], 500);
        }
    }

    public function destroy($id, Request $request)
    {
        if (!$request->user()->isAdmin()) return response()->json(['Error' => 'No autorizado'], 403);
        
        $pedido = Pedido::findOrFail($id);
        Mesa::find($pedido->mesa_id)->update(['status' => 'libre']);
        $pedido->delete();
        
        return response()->json(['status' => 'success', 'message' => 'Pedido eliminado']);
    }
}
