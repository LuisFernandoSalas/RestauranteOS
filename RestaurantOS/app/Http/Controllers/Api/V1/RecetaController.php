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
     * Obtener el listado global de todas las recetas registradas.
     * GET /api/recetas
     */
    public function index()
    {
        // Carga los insumos y los productos vinculados (si existen)
        $recetas = Receta::with(['insumo', 'producto'])->get();

        return response()->json([
            'status' => 'success',
            'data'   => $recetas
        ], 200);
    }

    /**
     * Obtener la receta (lista de ingredientes) de un producto específico.
     * GET /api/recetas/producto/{productoId}
     */
    public function show($productoId)
    {
        $producto = Producto::findOrFail($productoId);

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
     * Crear o reemplazar una receta (con o sin producto asignado).
     * POST /api/recetas
     */
    public function store(Request $request)
    {
        $request->validate([
            'producto_id'                    => 'nullable|integer|exists:productos,id',
            'nombre'                         => 'required|string|max:255',
            'insumos'                        => 'required|array|min:1',
            'insumos.*.insumo_id'            => 'required|exists:insumos,id',
            'insumos.*.cantidad_por_porcion' => 'required|numeric|gt:0',
        ], [
            'producto_id.exists'                => 'El producto seleccionado no existe en el menú.',
            'nombre.required'                   => 'El nombre de la receta es obligatorio.',
            'insumos.required'                  => 'Debes incluir al menos un ingrediente.',
            'insumos.*.insumo_id.exists'        => 'Uno de los insumos seleccionados no existe.',
            'insumos.*.cantidad_por_porcion.gt' => 'La cantidad por porción debe ser mayor a 0.',
        ]);

        return DB::transaction(function () use ($request) {
            $productoId = $request->producto_id;
            $nombre     = $request->nombre;

            // 1. Manejo según si está vinculada a un Producto o es independiente
            if ($productoId) {
                $producto = Producto::findOrFail($productoId);

                // Opcional: actualizamos el nombre del producto si fue editado desde la receta
                if ($request->filled('nombre')) {
                    $producto->update(['nombre' => $nombre]);
                }

                // Limpiar la receta previa de este producto
                Receta::where('producto_id', $productoId)->delete();
            } else {
                // Si producto_id es NULL, limpiamos recetas anteriores sin producto que coincidan en el nombre
                Receta::whereNull('producto_id')->where('nombre', $nombre)->delete();
            }

            // 2. Registrar los nuevos insumos de la receta
            foreach ($request->insumos as $item) {
                Receta::create([
                    'producto_id'          => $productoId, // Guarda NULL o el ID del producto
                    'nombre'               => $nombre,
                    'insumo_id'            => $item['insumo_id'],
                    'cantidad_por_porcion' => $item['cantidad_por_porcion'],
                ]);
            }

            // 3. Obtener la receta guardada para responder a Swing
            $query = Receta::with(['insumo', 'producto']);
            
            if ($productoId) {
                $recetaCargada = $query->where('producto_id', $productoId)->get();
            } else {
                $recetaCargada = $query->whereNull('producto_id')->where('nombre', $nombre)->get();
            }

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