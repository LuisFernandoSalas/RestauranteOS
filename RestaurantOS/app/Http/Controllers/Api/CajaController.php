<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pedido;
use Illuminate\Http\Request;

class CajaController extends Controller
{
    public function cobrar(Request $request, $id)
    {
        // 1. Validar los datos que vienen del prototipo
        $request->validate([
            'metodo_pago'    => 'required|in:efectivo,tarjeta,mixto',
            'pago_efectivo'  => 'required|numeric|min:0',
            'pago_tarjeta'   => 'required|numeric|min:0',
            'propina'        => 'nullable|numeric|min:0',
            'requiere_factura' => 'boolean'
        ]);

        $pedido = Pedido::findOrFail($id);

        //
        if (in_array($pedido->status, ['pagado', 'cancelado']))
            {
                return response()->json([
                    'error' => 'Este pedido ya ha sido cobrado o se encuentra cancelado.'
                ], 400);
            }
        // 2. Calcular total real y validar que los pagos cubran el total
        $total_a_pagar = $pedido->total; // Asumiendo que ya tienes este campo
        $total_recibido = $request->pago_efectivo + $request->pago_tarjeta;

        if ($total_recibido < $total_a_pagar) {
            return response()->json(['error' => 'El pago recibido es insuficiente.'], 400);
        }

        // 3. Guardar la información del cobro
        $pedido->update([
            'status'           => 'pagado',
            'metodo_pago'      => $request->metodo_pago,
            'pago_efectivo'    => $request->pago_efectivo,
            'pago_tarjeta'     => $request->pago_tarjeta,
            'propina'          => $request->propina ?? 0,
            'requiere_factura' => $request->requiere_factura ?? false
        ]);

        return response()->json([
            'message' => 'Cobro realizado con éxito',
            'cambio'  => $total_recibido - $total_a_pagar,
            'pedido'  => $pedido
        ]);
    }

    public function obtenerDetalleCobro($id)
    {
        // Traemos el pedido con sus productos y sus notas (cocina)
        $pedido = Pedido::with('detalles.producto')->findOrFail($id);
        
        return response()->json([
            'pedido' => $pedido,
            'total'  => $pedido->total // O el cálculo que tengas definido
        ]);
    }
}