<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\CobrarPedidoRequest;
use App\Models\Pedido;
use App\Models\Pago;
use Illuminate\Support\Facades\DB;
use Illuminate\Http\JsonResponse;


class PagoController extends Controller
{
    /**
     * GET /api/v1/pagos
     * Historial de transacciones (Requerido por la pantalla de Caja -> Historial)
     */
    public function index(): JsonResponse
    {
        $pagos = Pago::with(['pedido:id,mesa_id', 'cobrador:id,name'])
            ->orderBy('created_at', 'desc')
            ->paginate(15);

        return response()->json([
            'status' => 'success',
            'data' => $pagos
        ], 200);
    }

    /**
     * POST /api/v1/pedidos/{id}/cobrar
     * Motor financiero: Soporta pago simple, mixto y propinas.
     */
    public function cobrar(CobrarPedidoRequest $request, $id): JsonResponse
    {
        // Bloquear el registro del pedido concurrentemente para evitar doble cobro
        $pedido = Pedido::where('id', $id)->lockForUpdate()->firstOrFail();

        if (in_array($pedido->estado, ['pagado', 'cancelado'])) {
            return response()->json([
                'error' => 'Transacción inválida',
                'mensaje' => "El pedido ya se encuentra en estado: {$pedido->estado}."
            ], 422);
        }

        $totalPedido = $pedido->total_calculado;
        $propina = (float) $request->propina;
        $totalACobrar = $totalPedido + $propina;

        $datosPagos = $request->input('pagos');
        $sumaMontoRecibido = array_sum(array_column($datosPagos, 'monto_recibido'));

        if ($sumaMontoRecibido < $totalACobrar) {
            return response()->json([
                'error' => 'Fondos insuficientes',
                'mensaje' => "El total con propina es \${$totalACobrar}, pero se recibió \${$sumaMontoRecibido}."
            ], 422);
        }

        $cambio = $sumaMontoRecibido - $totalACobrar;

        // Operación Atómica
        DB::transaction(function () use ($pedido, $datosPagos, $propina, $request) {
            
            foreach ($datosPagos as $item) {
                Pago::create([
                    'pedido_id'        => $pedido->id,
                    'cobrado_por' => $request->user()?->id ?? $pedido->user_id,
                    'metodo_pago'      => $item['metodo_pago'],
                    'monto_recibido'   => $item['monto_recibido'],
                    'propina'          => $propina, 
                    'requiere_factura' => $request->requiere_factura,
                ]);
            }

            // Actualizar pedido a pagado
            $pedido->update(['estado' => 'pagado']);

            // Liberar mesa vinculada
            if ($pedido->mesa) {
                $pedido->mesa->update(['estado' => 'libre']); 
            }
        });

        return response()->json([
            'status' => 'success',
            'mensaje' => 'Pedido cobrado con éxito.',
            'data' => [
                'pedido_id'        => $pedido->id,
                'total_platillos'  => $totalPedido,
                'propina_aplicada' => $propina,
                'total_cobrado'    => $totalACobrar,
                'monto_recibido'   => $sumaMontoRecibido,
                'cambio'           => round($cambio, 2),
                'requiere_factura' => $request->requiere_factura
            ]
        ], 200);
    }
}