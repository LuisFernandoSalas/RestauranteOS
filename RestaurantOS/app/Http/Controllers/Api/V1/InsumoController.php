<?php


namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Insumo;
use App\Models\MovimientoInventario;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class InsumoController extends Controller
{
    /**
     * Listar insumos con opción de filtrar por categoría
     */
    public function index(Request $request)
    {
        $query = Insumo::query();

        // Filtro opcional por categoría (?categoria=Verdura)
        if ($request->has('categoria')) {
            $query->where('categoria', $request->categoria);
        }

        $insumos = $query->orderBy('categoria')
            ->orderBy('nombre')
            ->get()
            ->map(function ($insumo) {
                $insumo->stock_bajo = $insumo->stock_actual <= $insumo->stock_minimo;
                return $insumo;
            });

        return response()->json([
            'status' => 'success',
            'data'   => $insumos
        ], 200);
    }

    /**
     * Registrar un nuevo insumo
     */
    public function store(Request $request)
    {
        $request->validate([
            'nombre'        => 'required|string|max:150',
            'categoria'     => 'nullable|string|max:100', // Ej: Verdura, Lácteo, Carne
            'unidad_medida' => 'required|string|max:20',  // kg, g, pza, lt
            'stock_actual'  => 'required|numeric|min:0',
            'stock_minimo'  => 'nullable|numeric|min:0',
        ]);

        $insumo = Insumo::create([
            'nombre'        => $request->nombre,
            'categoria'     => $request->categoria,
            'unidad_medida' => $request->unidad_medida,
            'stock_actual'  => $request->stock_actual,
            'stock_minimo'  => $request->stock_minimo ?? 5, // Toma el default de tu migración
        ]);

        // Registro de auditoría inicial
        if ($insumo->stock_actual > 0) {
            MovimientoInventario::create([
                'insumo_id'        => $insumo->id,
                'user_id'          => $request->user()?->id,
                'tipo'             => 'ENTRADA',
                'cantidad'         => $insumo->stock_actual,
                'stock_resultante' => $insumo->stock_actual,
                'motivo'           => 'Inventario inicial',
            ]);
        }

        // Evaluar stock mínimo de inmediato
        if ($insumo->stock_actual <= $insumo->stock_minimo) {
            $insumo->notificarStockBajo();
        }

        return response()->json([
            'status'  => 'success',
            'message' => 'Insumo registrado con éxito.',
            'data'    => $insumo
        ], 201);
    }

    /**
     * Actualizar insumo
     */
    public function update(Request $request, $id)
    {
        $insumo = Insumo::findOrFail($id);

        $request->validate([
            'nombre'        => 'required|string|max:150',
            'categoria'     => 'nullable|string|max:100',
            'unidad_medida' => 'required|string|max:20',
            'stock_minimo'  => 'required|numeric|min:0',
        ]);

        $insumo->update($request->only(['nombre', 'categoria', 'unidad_medida', 'stock_minimo']));

        return response()->json([
            'status'  => 'success',
            'message' => 'Insumo actualizado con éxito.',
            'data'    => $insumo
        ], 200);
    }

    /**
     * Eliminar insumo
     */
    public function destroy($id)
    {
        $insumo = Insumo::findOrFail($id);
        $insumo->delete();

        return response()->json([
            'status'  => 'success',
            'message' => 'Insumo eliminado del inventario.'
        ], 200);
    }

    /**
     * Movimientos manuales (Reabasto, Merma, Ajuste)
     */
    public function registrarMovimiento(Request $request)
    {
        $request->validate([
            'insumo_id' => 'required|exists:insumos,id',
            'tipo'      => 'required|in:ENTRADA,MERMA,AJUSTE',
            'cantidad'  => 'required|numeric|gt:0',
            'motivo'    => 'nullable|string|max:255',
        ]);

        return DB::transaction(function () use ($request) {
            $insumo = Insumo::lockForUpdate()->findOrFail($request->insumo_id);
            $cantidad = (float) $request->cantidad;

            if ($request->tipo === 'ENTRADA') {
                $insumo->increment('stock_actual', $cantidad);
                $insumo->refresh();
            } else {
                // Utiliza tu método con auto-notificación si llega al mínimo
                $insumo->descontarStock($cantidad);
            }

            $movimiento = MovimientoInventario::create([
                'insumo_id'        => $insumo->id,
                'user_id'          => $request->user()?->id,
                'tipo'             => $request->tipo,
                'cantidad'         => $cantidad,
                'stock_resultante' => $insumo->stock_actual,
                'motivo'           => $request->motivo ?? 'Movimiento manual',
            ]);

            return response()->json([
                'status'      => 'success',
                'message'     => 'Movimiento registrado correctamente.',
                'nuevo_stock' => $insumo->stock_actual,
                'movimiento'  => $movimiento
            ], 200);
        });
    }
}