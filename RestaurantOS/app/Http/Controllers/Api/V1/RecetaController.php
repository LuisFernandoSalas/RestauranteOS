<?php


namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Receta;
use App\Models\Producto;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class RecetaController extends Controller
{
    /**
     * Obtener la receta (lista de ingredientes) de un producto específico.
     * GET /api/recetas/producto/{productoId}
     */
    public function show($productoId)
    {
        // Verificar que el producto exista
        $producto = Producto::findOrFail($productoId);

        // Cargar los ingredientes vinculados con su información del insumo
        $receta = Receta::with('insumo')
            ->where('producto_id', $productoId)
            ->get();

        return response()->json([
            'status'   => 'success',
            'producto' => $producto->nombre,
            'data'     => $receta
        ], 200);
    }

    /**
     * Crear o reemplazar la receta completa de un producto.
     * POST /api/recetas
     */
    public function store(Request $request)
    {
        $request->validate([
            'producto_id'                    => 'required|exists:productos,id',
            'insumos'                        => 'required|array|min:1',
            'insumos.*.insumo_id'             => 'required|exists:insumos,id',
            'insumos.*.cantidad_por_porcion' => 'required|numeric|gt:0',
        ], [
            'producto_id.required'                    => 'El producto es obligatorio.',
            'insumos.required'                        => 'Debes incluir al menos un ingrediente.',
            'insumos.*.insumo_id.exists'              => 'Uno de los insumos seleccionados no existe.',
            'insumos.*.cantidad_por_porcion.gt'       => 'La cantidad por porción debe ser mayor a 0.',
        ]);

        return DB::transaction(function () use ($request) {
            // 1. Limpiar la receta previa del producto para sincronizar
            Receta::where('producto_id', $request->producto_id)->delete();

            // 2. Registrar los nuevos insumos de la receta
            $nuevosIngredientes = [];
            foreach ($request->insumos as $item) {
                $nuevosIngredientes[] = Receta::create([
                    'producto_id'          => $request->producto_id,
                    'insumo_id'            => $item['insumo_id'],
                    'cantidad_por_porcion' => $item['cantidad_por_porcion'],
                ]);
            }

            // 3. Cargar las relaciones de insumo para la respuesta
            $recetaCargada = Receta::with('insumo')
                ->where('producto_id', $request->producto_id)
                ->get();

            return response()->json([
                'status'  => 'success',
                'message' => 'Receta guardada exitosamente.',
                'data'    => $recetaCargada
            ], 201);
        });
    }

    /**
     * Eliminar un ingrediente individual de la receta.
     * DELETE /api/recetas/{id}
     */
    public function destroy($id)
    {
        $recetaItem = Receta::findOrFail($id);
        $recetaItem->delete();

        return response()->json([
            'status'  => 'success',
            'message' => 'Ingrediente eliminado de la receta.'
        ], 200);
    }

    /**
     * Eliminar toda la receta de un producto.
     * DELETE /api/recetas/producto/{productoId}
     */
    public function destroyByProducto($productoId)
    {
        Receta::where('producto_id', $productoId)->delete();

        return response()->json([
            'status'  => 'success',
            'message' => 'Toda la receta del producto fue eliminada.'
        ], 200);
    }
}