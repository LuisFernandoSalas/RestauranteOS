<?php

namespace App\Http\Controllers\Api;

use App\Models\Mesa;
use App\Models\Pedido; // Asegúrate de importar el modelo Pedido
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class MesaController extends Controller
{
    /**
     * Actualiza el recurso especificado.
     */
    public function update(Request $request, Mesa $mesa)
    {
        // 1. Validamos que el estado sea uno de los permitidos
        $request->validate([
            'status' => 'required|in:libre,ocupado,pendiente_pago',
        ]);

        // 2. Actualizamos (Laravel ya buscó la Mesa automáticamente gracias al Route Model Binding)
        $mesa->status = $request->status;
        $mesa->save();

        return response()->json([
            'message' => 'Mesa actualizada correctamente',
            'mesa' => $mesa
        ], 200);
    }

    /**
     * Muestra una lista de los recursos.
     */
    public function index()
    {
        $mesas = Mesa::all();

        // Obtenemos TODOS los pedidos activos de una sola vez (Solución al problema N+1)
        $pedidosActivos = Pedido::whereIn('mesa_id', $mesas->pluck('id'))
            ->whereNotIn('estado', ['pagado', 'cancelado'])
            ->get()
            ->keyBy('mesa_id'); // Agrupamos por el ID de la mesa para búsqueda rápida

        // Mapeamos las mesas para inyectar "la magia de José" en memoria
        $mesas = $mesas->map(function ($mesa) use ($pedidosActivos) {
            // Buscamos si existe un pedido activo para esta mesa en la colección
            $pedidoActivo = $pedidosActivos->get($mesa->id);

            // Inyectamos el total actual
            $mesa->total_actual = $pedidoActivo ? $pedidoActivo->total_calculado : 0.00;
            
            // 👇 ESTAS SON LAS DOS LÍNEAS QUE FALTABAN 👇
            $mesa->pedido_id = $pedidoActivo ? $pedidoActivo->id : null;
            $mesa->estado_pedido = $pedidoActivo ? $pedidoActivo->estado : null;
            
            return $mesa;
        });

        return response()->json($mesas, 200);
    }
}