<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pago;
use App\Models\Pedido;
use App\Models\TurnoCaja;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

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
    // 1. Buscamos el pedido con sus detalles y pagos
    $pedido = Pedido::with(['detalles.producto', 'pagos', 'mesa'])->findOrFail($id);

    // 2. Calculamos los totales reales sumando las relaciones
    $total_pedido = $pedido->detalles->sum('subtotal');
    
    // Asumiendo que la tabla de pagos tiene una columna 'monto' o 'cantidad'
    $total_pagado = $pedido->pagos->sum('monto'); 
    
    $saldo_pendiente = $total_pedido - $total_pagado;

    // 3. (OPCIONAL PERO RECOMENDADO) Si el total en la BD estaba en 0.00, lo actualizamos para que ya quede guardado
    if ($pedido->total != $total_pedido) {
        $pedido->total = $total_pedido;
        $pedido->save();
    }

    // 4. Retornamos la respuesta al Android con los datos correctos
    return response()->json([
        'status' => 'success',
        'pedido' => $pedido,
        'total_pedido' => $total_pedido,
        'total_pagado' => $total_pagado,
        'saldo_pendiente' => $saldo_pendiente
    ]);
}

    /**
     * Procesar cobro (Soporta pagos parciales, mixtos y registro en la tabla 'pagos')
     */
    public function cobrar(Request $request, $id)
    {
        // Opcional: Validar que exista un turno de caja abierto antes de cobrar
        $turnoActivo = TurnoCaja::where('estado', 'ABIERTO')->first();
        if (!$turnoActivo) {
            return response()->json([
                'status' => 'error',
                'error'  => 'No se puede procesar el cobro porque no hay un turno de caja abierto.'
            ], 422);
        }

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

        // Obtener de forma segura el ID del usuario autenticado
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
                'status'          => 'success',
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

    // ==========================================
    // 🚀 NUEVOS MÉTODOS DE CONTROL DE TURNO Y ARQUEO
    // ==========================================

    /**
     * Obtener el estado del turno actual y ventas acumuladas en tiempo real
     */
    public function obtenerTurnoActual()
    {
        $turno = TurnoCaja::with('user:id,name,email')
            ->where('estado', 'ABIERTO')
            ->latest('opened_at')
            ->first();

        if (!$turno) {
            return response()->json([
                'status'  => 'success',
                'activo'  => false,
                'message' => 'No hay ningún turno de caja abierto actualmente.'
            ], 200);
        }

        // Consultar ventas reales directamente desde la tabla 'pagos' desde que se abrió el turno
        $ventasEfectivo = Pago::where('created_at', '>=', $turno->opened_at)
            ->whereIn('metodo_pago', ['efectivo', 'mixto'])
            ->sum('monto_recibido');

        $ventasTarjeta = Pago::where('created_at', '>=', $turno->opened_at)
            ->whereIn('metodo_pago', ['tarjeta', 'transferencia'])
            ->sum('monto_recibido');

        $propinasAcumuladas = Pago::where('created_at', '>=', $turno->opened_at)
            ->sum('propina');

        $efectivoEsperado = $turno->monto_apertura + $ventasEfectivo;

        return response()->json([
            'status' => 'success',
            'activo' => true,
            'turno'  => $turno,
            'resumen_tiempo_real' => [
                'monto_apertura'            => (float) $turno->monto_apertura,
                'ventas_efectivo'           => (float) $ventasEfectivo,
                'ventas_tarjeta'            => (float) $ventasTarjeta,
                'propinas_totales'          => (float) $propinasAcumuladas,
                'total_ventas_turno'        => (float) ($ventasEfectivo + $ventasTarjeta),
                'efectivo_esperado_en_caja' => (float) $efectivoEsperado,
            ]
        ], 200);
    }

    /**
     * Abrir turno de caja
     */
    public function abrirTurno(Request $request)
    {
        $request->validate([
            'monto_apertura' => 'required|numeric|min:0',
        ]);

        $turnoActivo = TurnoCaja::where('estado', 'ABIERTO')->first();
        if ($turnoActivo) {
            return response()->json([
                'status' => 'error',
                'error'  => 'Ya existe un turno abierto en caja.'
            ], 422);
        }

        $usuarioId = $request->user()?->id ?? auth('sanctum')->id();

        $turno = TurnoCaja::create([
            'user_id'        => $usuarioId,
            'monto_apertura' => $request->monto_apertura,
            'estado'         => 'ABIERTO',
            'opened_at'      => Carbon::now(),
        ]);

        return response()->json([
            'status'  => 'success',
            'message' => 'Turno de caja abierto correctamente.',
            'turno'   => $turno
        ], 201);
    }

    /**
     * Arqueo y cierre de turno de caja
     */
    public function cerrarTurno(Request $request)
    {
        $request->validate([
            'turno_id'              => 'required|exists:turnos_caja,id',
            'monto_cierre_efectivo' => 'required|numeric|min:0',
            'monto_cierre_tarjeta'  => 'required|numeric|min:0',
            'monto_siguiente_turno' => 'nullable|numeric|min:0',
            'notas'                 => 'nullable|string|max:500',
        ]);

        $turno = TurnoCaja::findOrFail($request->turno_id);

        if ($turno->estado === 'CERRADO') {
            return response()->json([
                'status' => 'error',
                'error'  => 'Este turno ya ha sido cerrado previamente.'
            ], 422);
        }

        // Calcular efectivo esperado en base a la tabla 'pagos'
        $ventasEfectivo = Pago::where('created_at', '>=', $turno->opened_at)
            ->whereIn('metodo_pago', ['efectivo', 'mixto'])
            ->sum('monto_recibido');

        $efectivoEsperado = $turno->monto_apertura + $ventasEfectivo;
        $diferencia = $request->monto_cierre_efectivo - $efectivoEsperado;

        $turno->update([
            'monto_cierre_efectivo'   => $request->monto_cierre_efectivo,
            'monto_cierre_tarjeta'    => $request->monto_cierre_tarjeta,
            'monto_esperado_efectivo' => $efectivoEsperado,
            'diferencia'              => $diferencia,
            'monto_siguiente_turno'   => $request->monto_siguiente_turno ?? 0.00,
            'estado'                  => 'CERRADO',
            'notas'                   => $request->notas,
            'closed_at'               => Carbon::now(),
        ]);

        return response()->json([
            'status'         => 'success',
            'message'        => 'Cierre de turno realizado con éxito.',
            'resumen_cierre' => [
                'turno_id'              => $turno->id,
                'fondo_inicial'         => (float) $turno->monto_apertura,
                'efectivo_esperado'     => (float) $efectivoEsperado,
                'efectivo_contado'      => (float) $turno->monto_cierre_efectivo,
                'diferencia'            => (float) $diferencia, // $0.00 es perfecto
                'monto_siguiente_turno' => (float) $turno->monto_siguiente_turno,
                'closed_at'             => $turno->closed_at->toDateTimeString()
            ]
        ], 200);
    }

    /**
     * Historial de turnos de caja (Para reportes/administración)
     */
    public function historialTurnos()
    {
        $turnos = TurnoCaja::with('user:id,name,email')
            ->orderBy('opened_at', 'desc')
            ->paginate(15);

        return response()->json([
            'status' => 'success',
            'data'   => $turnos
        ], 200);
    }
}