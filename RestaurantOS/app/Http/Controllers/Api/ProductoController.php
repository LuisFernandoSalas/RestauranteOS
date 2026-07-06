<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Producto;
use Illuminate\Http\Request;

class ProductoController extends Controller
{   
    /**
     * Muestra la lista de productos para el menú (Solo activos).
     */
    public function index()
    {
        // Usamos tu scopeDisponibles() para filtrar automáticamente los status = 'activo'
        $productos = Producto::disponibles()->get();

        return response()->json([
            'success' => true,
            'data' => $productos
        ], 200);
    }

    /**
     * Muestra un producto en específico por su ID.
     */
    public function show(string $id)
    {
        $producto = Producto::find($id);

        if (!$producto) {
            return response()->json([
                'success' => false,
                'message' => 'Producto no encontrado'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $producto
        ], 200);
    }
}
