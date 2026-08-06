<?php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Combo;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ComboController extends Controller
{
    // GET /api/combos
    public function index(Request $request)
    {
        $query = Combo::with('productos');

        // Permitir filtrar por estado (ej. /api/combos?estado=activo)
        if ($request->has('estado')) {
            $query->where('estado', $request->estado);
        }

        return response()->json($query->get());
    }

    // POST /api/combos
    public function store(Request $request)
    {
        $validated = $request->validate([
            'nombre'          => 'required|string|max:255',
            'precio_especial' => 'required|numeric|min:0',
            'fecha_inicio'    => 'nullable|date',
            'fecha_fin'       => 'nullable|date|after_or_equal:fecha_inicio',
            'estado'          => 'in:activo,pausado',
            'productos'       => 'required|array|min:1', // Exige al menos 1 producto
            'productos.*.producto_id' => 'required|exists:productos,id',
            'productos.*.cantidad'    => 'required|integer|min:1',
        ]);

        DB::beginTransaction();
        try {
            // 1. Crear el registro principal del Combo
            $combo = Combo::create([
                'nombre'          => $validated['nombre'],
                'precio_especial' => $validated['precio_especial'],
                'fecha_inicio'    => $validated['fecha_inicio'] ?? null,
                'fecha_fin'       => $validated['fecha_fin'] ?? null,
                'estado'          => $validated['estado'] ?? 'activo',
            ]);

            // 2. Preparar el arreglo para la tabla pivote (combo_producto)
            $productosPivot = [];
            foreach ($validated['productos'] as $prod) {
                // Estructura: [producto_id => ['cantidad' => valor]]
                $productosPivot[$prod['producto_id']] = ['cantidad' => $prod['cantidad']];
            }
            
            // 3. Adjuntar los productos al combo
            $combo->productos()->attach($productosPivot);

            DB::commit();
            return response()->json($combo->load('productos'), 201);
            
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['error' => 'Error al crear el combo', 'detalle' => $e->getMessage()], 500);
        }
    }

    // GET /api/combos/{id}
    public function show(Combo $combo)
    {
        return response()->json($combo->load('productos'));
    }

    // PUT /api/combos/{id}
    public function update(Request $request, Combo $combo)
    {
        $validated = $request->validate([
            'nombre'          => 'sometimes|required|string|max:255',
            'precio_especial' => 'sometimes|required|numeric|min:0',
            'fecha_inicio'    => 'nullable|date',
            'fecha_fin'       => 'nullable|date|after_or_equal:fecha_inicio',
            'estado'          => 'sometimes|in:activo,pausado',
            'productos'       => 'sometimes|array|min:1',
            'productos.*.producto_id' => 'required_with:productos|exists:productos,id',
            'productos.*.cantidad'    => 'required_with:productos|integer|min:1',
        ]);

        DB::beginTransaction();
        try {
            // 1. Actualizar datos del combo
            $combo->update($validated);

            // 2. Si enviaron arreglo de productos, sincronizamos la tabla pivote
            if ($request->has('productos')) {
                $productosPivot = [];
                foreach ($validated['productos'] as $prod) {
                    $productosPivot[$prod['producto_id']] = ['cantidad' => $prod['cantidad']];
                }
                // sync() elimina los que ya no están en el arreglo y actualiza los existentes
                $combo->productos()->sync($productosPivot);
            }

            DB::commit();
            return response()->json($combo->load('productos'));
            
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['error' => 'Error al actualizar el combo', 'detalle' => $e->getMessage()], 500);
        }
    }

    // DELETE /api/combos/{id}
    public function destroy(Combo $combo)
    {
        $combo->delete(); // Gracias al trait SoftDeletes, esto es una eliminación lógica
        return response()->json(['message' => 'Combo eliminado/desactivado correctamente']);
    }
}