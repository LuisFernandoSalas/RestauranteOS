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
        // Obtiene todas las mesas de la base de datos
        $mesas = \App\Models\Mesa::all();

        // Retorna la respuesta en formato JSON
        return response()->json($mesas, 200);
    }
}
