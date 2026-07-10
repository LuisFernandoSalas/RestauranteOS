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
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Auth;

class CocinaController extends Controller
{
    /**
     * GET /api/v1/cocina/pedidos
     * Obtiene todas las comandas activas en la cocina (No pagadas, No canceladas).
     * Excluye datos financieros por motivos de seguridad y roles.
     */
    public function index(): JsonResponse
    {
        $pedidos = Pedido::whereIn('estado', ['pendiente', 'en_preparacion', 'listo'])
            ->with(['mesa', 'mesero:id,name', 'detalles.producto:id,nombre'])
            ->orderBy('created_at', 'asc')
            ->get();

        // Estructurar respuesta limpia mapeada para el Grid Horizontal de la Tablet
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
                'pendientes' => $pedidos->where('estado', 'pendiente')->count(),
            ],
            'comandas' => $comandas
        ], 200);
    }

    /**
     * PATCH /api/v1/cocina/detalles/{id}/estado
     * Cambia el estado de un platillo individual (KDS Interactivo).
     */
    public function updatePlatilloEstado(UpdateDetalleEstadoRequest $request, $id): JsonResponse
    {
        $detalle = DetallePedido::findOrFail($id);
        
        DB::transaction(function () use ($detalle, $request) {
            $detalle->update([
                'estado' => $request->estado
            ]);

            // Lógica inteligente: Si el chef cambia un platillo a "en_preparacion",
            // el pedido padre pasa automáticamente a "en_preparacion".
            $pedido = $detalle->pedido;
            if ($request->estado === 'en_preparacion' && $pedido->estado === 'pendiente') {
                $pedido->update(['estado' => 'en_preparacion']);
            }

            // Si todos los platillos individuales están 'listos', el pedido se marca como listo
            $todosListos = !$pedido->detalles()->where('estado', '!=', 'listo')->exists();
            if ($todosListos) {
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
     * POST /api/v1/cocina/pedidos/{id}/cancelar
     * Cancela una orden completa guardando auditoría fiscal sin romper las llaves foráneas.
     */
    public function cancelarPedido(CancelarPedidoRequest $request, $id): JsonResponse
    {
        $pedido = Pedido::findOrFail($id);

        if ($pedido->estado === 'pagado') {
            return response()->json(['error' => 'No se puede cancelar una orden ya cobrada y facturada.'], 422);
        }

        DB::transaction(function () use ($pedido, $request) {
            // Actualizar estado y auditoría antes del soft delete
            $pedido->update([
                'estado' => 'cancelado',
                'motivo_cancelacion' => $request->motivo_cancelacion,
                'cobrado_por' => $request->user()?->id ?? $pedido->user_id,
            ]);

            // Cancelar todos sus detalles internos
            $pedido->detalles()->update(['estado' => 'cancelado']);

            // Liberar la mesa
            if ($pedido->mesa) {
                $pedido->mesa->update(['estado' => 'libre']);
            }

            // Ejecuta el borrado lógico seguro
            $pedido->delete();
        });

        return response()->json([
            'status' => 'success',
            'mensaje' => 'Pedido cancelado con éxito. Se ha registrado la merma en auditoría.'
        ], 200);
    }

    /**
     * POST /api/v1/cocina/productos/{id}/pausar
     * Función 86: Desactiva un producto temporalmente del menú digital (App Meseros / QR)
     */
    public function pausarProducto(PausarProductoRequest $request, $id): JsonResponse
    {
        $producto = Producto::findOrFail($id);
        $duracion = $request->duracion;

        $pausadoHasta = match ($duracion) {
            '30_min' => Carbon::now()->addMinutes(30),
            '1_hora' => Carbon::now()->addHour(),
            'indefinido' => Carbon::now()->addYears(10), // Representación de "Hasta reactivar"
        };

        $producto->update([
            'pausado_hasta' => $pausadoHasta
        ]);

        return response()->json([
            'status' => 'success',
            'mensaje' => "El producto '{$producto->nombre}' ha sido pausado.",
            'pausado_hasta' => $producto->pausado_hasta->toDateTimeString(),
            'is_disponible' => false
        ], 200);
    }
}