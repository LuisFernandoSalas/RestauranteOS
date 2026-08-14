<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Producto;
use App\Models\Receta;
use Illuminate\Http\Request;
use Carbon\Carbon;

class ProductoController extends Controller
{    
    /**
     * Muestra la lista de productos para el menú.
     */
    public function index()
    {
        $productos = Producto::with(['categoria', 'recetas'])->get();

        return response()->json([
            'success' => true,
            'data'    => $productos
        ], 200);
    }

    /**
     * Guarda un nuevo producto en la base de datos.
     */
    public function store(Request $request)
    {
        $request->validate([
            'nombre'        => 'nullable|string|max:255',
            'name'          => 'nullable|string|max:255',
            'precio'        => 'nullable|numeric',
            'price'         => 'nullable|numeric',
            'categoria'     => 'nullable',
            'categoria_id'  => 'nullable|integer',
            'descripcion'   => 'nullable|string',
            'description'   => 'nullable|string',
            'estado'        => 'nullable|string',
            'status'        => 'nullable|string',
            'pausado_hasta' => 'nullable|date',
            'receta_id'     => 'nullable|integer',
        ]);

        $nombre      = $request->input('nombre') ?? $request->input('name') ?? 'Sin Nombre';
        $precio      = $request->input('precio') ?? $request->input('price') ?? 0;
        $descripcion = $request->input('descripcion') ?? $request->input('description') ?? '';

        // Manejo de PAUSA con fecha segura (Límite MySQL)
        $pausadoHasta = null;
        if ($request->filled('pausado_hasta')) {
            $pausadoHasta = $request->input('pausado_hasta');
        } else {
            $estadoStr = strtolower($request->input('estado') ?? $request->input('status') ?? 'activo');
            if (in_array($estadoStr, ['pausa', 'pausado', 'inactivo', 'inactive', 'false', '0'])) {
                $pausadoHasta = '2037-12-31 23:59:59';
            }
        }

        $catNombre   = $request->input('categoria') ?? $request->input('category');
        $categoriaId = $request->input('categoria_id');

        if (!$categoriaId && $catNombre && !in_array($catNombre, ['—', 'Categoria', 'General'])) {
            if (class_exists(\App\Models\Categoria::class)) {
                $cat = \App\Models\Categoria::firstOrCreate(['nombre' => $catNombre]);
                $categoriaId = $cat->id;
            }
        }

        if (!$categoriaId) {
            if (class_exists(\App\Models\Categoria::class)) {
                $primeraCat  = \App\Models\Categoria::first();
                $categoriaId = $primeraCat ? $primeraCat->id : 1;
            } else {
                $categoriaId = 1; 
            }
        }

        $producto = Producto::create([
            'categoria_id'  => $categoriaId,
            'nombre'        => $nombre,
            'descripcion'   => $descripcion,
            'precio'        => $precio,
            'pausado_hasta' => $pausadoHasta,
        ]);

        if ($request->filled('receta_id')) {
            Receta::where('id', $request->receta_id)->update(['producto_id' => $producto->id]);
        } else {
            Receta::whereNull('producto_id')
                ->where('nombre', $nombre)
                ->update(['producto_id' => $producto->id]);
        }

        $producto->load(['categoria', 'recetas']);

        return response()->json([
            'success' => true,
            'message' => 'Producto creado y receta vinculada correctamente',
            'data'    => $producto
        ], 201);
    }

    /**
     * Muestra un producto específico.
     */
    public function show(string $id)
    {
        $producto = Producto::with(['categoria', 'recetas'])->find($id);

        if (!$producto) {
            return response()->json([
                'success' => false,
                'message' => 'Producto no encontrado'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data'    => $producto
        ], 200);
    }

    /**
     * Actualiza un producto existente.
     */

    /**
     * Actualiza un producto existente.
     */
    public function update(Request $request, string $id)
    {
        $producto = Producto::find($id);

        if (!$producto) {
            return response()->json([
                'success' => false,
                'message' => 'Producto no encontrado'
            ], 404);
        }

        $data = [];

        // 1. Campos básicos
        if ($request->has('nombre') || $request->has('name')) {
            $data['nombre'] = $request->input('nombre') ?? $request->input('name');
        }
        if ($request->has('precio') || $request->has('price')) {
            $data['precio'] = $request->input('precio') ?? $request->input('price');
        }
        if ($request->has('descripcion') || $request->has('description')) {
            $data['descripcion'] = $request->input('descripcion') ?? $request->input('description');
        }

        // 2. Manejo de CATEGORÍA (¡Esto era lo que faltaba!)
        $categoriaId = $request->input('categoria_id') ?? $request->input('category_id');
        $catNombre   = $request->input('categoria') ?? $request->input('category');

        if ($categoriaId) {
            $data['categoria_id'] = $categoriaId;
        } elseif ($catNombre && !in_array($catNombre, ['—', 'Categoria', 'General'])) {
            if (is_numeric($catNombre)) {
                $data['categoria_id'] = (int) $catNombre;
            } elseif (class_exists(\App\Models\Categoria::class)) {
                $cat = \App\Models\Categoria::firstOrCreate(['nombre' => $catNombre]);
                $data['categoria_id'] = $cat->id;
            }
        }

        // 3. Manejo de estado/pausa seguro
        if ($request->has('pausado_hasta')) {
            $data['pausado_hasta'] = $request->input('pausado_hasta');
        } 
        elseif ($request->has('is_disponible') || $request->has('disponible')) {
            $disponible = filter_var(
                $request->input('is_disponible') ?? $request->input('disponible'), 
                FILTER_VALIDATE_BOOLEAN, 
                FILTER_NULL_ON_FAILURE
            );

            if ($disponible === false) {
                $data['pausado_hasta'] = '2037-12-31 23:59:59';
            } elseif ($disponible === true) {
                $data['pausado_hasta'] = null;
            }
        } 
        elseif ($request->has('estado') || $request->has('status')) {
            $estadoStr = strtolower(trim($request->input('estado') ?? $request->input('status')));
            
            if (in_array($estadoStr, ['pausa', 'pausado', 'inactivo', 'inactive', 'false', '0'])) {
                $data['pausado_hasta'] = '2037-12-31 23:59:59';
            } else {
                $data['pausado_hasta'] = null;
            }
        }

        // Actualizamos en BD
        $producto->update($data);
        
        // Recargamos las relaciones para devolver la categoría actualizada al cliente Java
        $producto->load(['categoria', 'recetas']);

        return response()->json([
            'success' => true,
            'message' => 'Producto actualizado correctamente',
            'data'    => $producto
        ], 200);
    }

    /**
     * Elimina un producto.
     */
    public function destroy(string $id)
    {
        $producto = Producto::find($id);

        if (!$producto) {
            return response()->json([
                'success' => false,
                'message' => 'Producto no encontrado'
            ], 404);
        }

        // Si se borra el producto, desvinculamos sus recetas
        Receta::where('producto_id', $producto->id)->update(['producto_id' => null]);

        $producto->delete();

        return response()->json([
            'success' => true,
            'message' => 'Producto eliminado correctamente'
        ], 200);
    }
}