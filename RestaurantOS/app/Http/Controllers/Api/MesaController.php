<?php

namespace App\Http\Controllers\Api;

use App\Models\Mesa;
use Illuminate\Http\Request;
use App\Http\Controllers\Controller;

class MesaController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function update(Request $request, string $id)
    {
        // 1. Validamos que el estado sea uno de los permitidos
        // Esto evita que lleguen caracteres extraños a MySQL
        $request->validate([
            'status' => 'required|in:libre,ocupado,pendiente_pago',
        ]);

        // 2. Buscamos la mesa
        $mesa = Mesa::find($id);

        if (!$mesa) {
            return response()->json(['message' => 'Mesa no encontrada'], 404);
        }

        // 3. Actualizamos
        $mesa->status = $request->status;
        $mesa->save();

        return response()->json([
            'message' => 'Mesa actualizada correctamente',
            'mesa' => $mesa
        ], 200);
    }


    public function index()
    {
        $mesas = \App\Models\Mesa::all()->map(function ($mesa) {
            
            $pedidoActivo = \App\Models\Pedido::where('mesa_id', $mesa->id)
                ->whereNotIn('estado', ['pagado', 'cancelado']) // Volvemos a estado porque en tu modelo dice "estado" en la línea 24
                ->first();

            // 🚨 AQUÍ ESTÁ LA MAGIA: 
            // Usamos la función de José en lugar de la columna de la BD.
            $mesa->total_actual = $pedidoActivo ? $pedidoActivo->total_calculado : 0.00;
            
            return $mesa;
        });

        return response()->json($mesas, 200);
    }
}
