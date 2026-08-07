<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pago;
use App\Models\Pedido;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class CajaController extends Controller
{
        /**
     * Listar historial de pagos (para Cierre de Caja y Consultas)
     */
    public function index(Request $request)
    {
        $pagos = Pago::with(['pedido', 'cobrador'])
            ->orderBy('created_at', 'desc')
            ->paginate(15);

        return response()->json([
            'status' => 'success',
            'data'   => $pagos
        ], 200);
    }
        /**
     * Obtener el detalle completo del pedido para el módulo de cobro
     */
    public function obtenerDetalleCobro($id)
    {
       
        $pedido = Pedido::with(['detalles.producto', 'pagos', 'mesa'])->findOrFail($id);
        
        $totalPagado = (float) $pedido->pagos->sum('monto_recibido');
        $saldoPendiente = (float) max(0, $pedido->total - $totalPagado);

        return response()->json([
            'status'          => 'success',
            'pedido'          => $pedido,
            'total_pedido'    => (float) $pedido->total,
            'total_pagado'    => $totalPagado,
            'saldo_pendiente' => $saldoPendiente,
        ]);
    }

    /**
     * Procesar cobro (Soporta pagos parciales, mixtos y registro en la tabla 'pagos')
     */
    public function cobrar(Request $request, $id)
    {
        // 1. Validar la petición
        $request->validate([
            'metodo_pago'      => 'required|in:efectivo,tarjeta,transferencia,mixto',
            'monto_recibido'   => 'nullable|numeric|min:0.01',
            'pago_efectivo'    => 'nullable|numeric|min:0',
            'pago_tarjeta'     => 'nullable|numeric|min:0',
            'propina'          => 'nullable|numeric|min:0',
            'requiere_factura' => 'boolean'
        ]);

        $pedido = Pedido::with('pagos', 'mesa')->findOrFail($id);

        // Validar si el pedido ya fue pagado o cancelado
        $estadoActual = $pedido->estado ?? $pedido->status;
        if (in_array($estadoActual, ['pagado', 'cancelado'])) {
            return response()->json([
                'error' => 'Este pedido ya ha sido cobrado o se encuentra cancelado.'
            ], 422);
        }

        // 2. Determinar el monto que se está pagando en este movimiento
        if ($request->has('monto_recibido') && $request->monto_recibido > 0) {
            $montoAbonado = (float) $request->monto_recibido;
        } else {
            $montoAbonado = (float) ($request->pago_efectivo ?? 0) + (float) ($request->pago_tarjeta ?? 0);
        }

        if ($montoAbonado <= 0) {
            return response()->json([
                'error' => 'Debes ingresar un monto válido a cobrar.'
            ], 422);
        }

        // Obtener de forma segura el ID del usuario autenticado (compatible con Sanctum / Tests)
        $usuarioCobradorId = $request->user()?->id 
                          ?? auth('sanctum')->id() 
                          ?? $pedido->user_id;

        if (!$usuarioCobradorId) {
            return response()->json([
                'error' => 'No se especificó un usuario válido para registrar el cobro.'
            ], 422);
        }

        DB::beginTransaction();
        try {
            // 3. Crear el registro oficial en la tabla 'pagos'
            $pago = Pago::create([
                'pedido_id'        => $pedido->id,
                'cobrado_por'      => $usuarioCobradorId,
                'metodo_pago'      => $request->metodo_pago,
                'monto_recibido'   => $montoAbonado,
                'propina'          => $request->propina ?? 0,
                'requiere_factura' => $request->requiere_factura ?? false,
            ]);

            // 4. Calcular el saldo acumulado
            $totalPagadoHastaAhora = $pedido->pagos()->sum('monto_recibido');
            $saldoPendiente = max(0, $pedido->total - $totalPagadoHastaAhora);
            $cambio = max(0, $totalPagadoHastaAhora - $pedido->total);

            // 5. Si el saldo está cubierto, marcar el pedido como PAGADO y liberar la mesa
            if ($totalPagadoHastaAhora >= $pedido->total) {
                
                $updateData = [];
                if (isset($pedido->estado)) { $updateData['estado'] = 'pagado'; }
                if (isset($pedido->status)) { $updateData['status'] = 'pagado'; }

                $pedido->update($updateData);

                // Liberar la mesa asociada
                if ($pedido->mesa) {
                    $pedido->mesa->update(['estado' => 'disponible']);
                }
            }

            DB::commit();

            return response()->json([
                'status'          => 'success', // 💡 Agregamos status success para compatibilidad con PagoApiTest
                'message'         => $saldoPendiente <= 0 ? 'Pedido cobrado con éxito.' : 'Pago parcial registrado',
                'mensaje'         => $saldoPendiente <= 0 ? 'Pedido cobrado con éxito.' : 'Pago parcial registrado',
                'pago'            => $pago,
                'total_pedido'    => (float) $pedido->total,
                'total_pagado'    => (float) $totalPagadoHastaAhora,
                'saldo_pendiente' => (float) $saldoPendiente,
                'cambio'          => (float) $cambio,
                'pedido'          => $pedido->fresh(['pagos', 'mesa'])
            ], 200);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'error' => 'Error al procesar el pago: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Calculadora para División de Cuentas (Split Bill) por partes iguales
     */
    public function calcularDivisionPartes(Request $request, $id)
    {
        $request->validate([
            'personas'           => 'required|integer|min:2|max:50',
            'porcentaje_propina' => 'nullable|numeric|min:0|max:100'
        ]);

        $pedido = Pedido::findOrFail($id);
        $personas = (int) $request->personas;
        $porcentajePropina = (float) ($request->porcentaje_propina ?? 0);

        $subtotalPorPersona = round($pedido->total / $personas, 2);
        $montoPropinaTotal = round(($pedido->total * $porcentajePropina) / 100, 2);
        $propinaPorPersona = round($montoPropinaTotal / $personas, 2);
        $totalPorPersona = $subtotalPorPersona + $propinaPorPersona;

        return response()->json([
            'status' => 'success',
            'data'   => [
                'total_comanda'     => (float) $pedido->total,
                'numero_personas'   => $personas,
                'subtotal_persona'  => $subtotalPorPersona,
                'propina_persona'   => $propinaPorPersona,
                'total_por_persona' => $totalPorPersona,
            ]
        ]);
    }
}