<?php


namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Insumo;
use App\Models\MovimientoInventario;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class MovimientoInventarioController extends Controller
{
    /**
     * Consultar historial de movimientos con filtros y paginación.
     * GET /api/movimientos-inventario
     */
    public function index(Request $request)
    {
        $query = MovimientoInventario::with(['insumo', 'usuario:id,name,email']);

        // Filtro por insumo específico
        if ($request->filled('insumo_id')) {
            $query->where('insumo_id', $request->insumo_id);
        }

        // Filtro por tipo de movimiento (ENTRADA, MERMA, SALIDA_VENTA, AJUSTE)
        if ($request->filled('tipo')) {
            $query->where('tipo', $request->tipo);
        }

        // Filtro por rango de fechas
        if ($request->filled('fecha_inicio') && $request->filled('fecha_fin')) {
            $query->whereBetween('created_at', [
                $request->fecha_inicio . ' 00:00:00',
                $request->fecha_fin . ' 23:59:59'
            ]);
        }

        $movimientos = $query->orderBy('created_at', 'desc')->paginate(20);

        return response()->json([
            'status' => 'success',
            'data'   => $movimientos
        ], 200);
    }

    /**
     * Registrar manualmente una Entrada, Merma o Ajuste de inventario.
     * POST /api/movimientos-inventario
     */
    public function store(Request $request)
    {
        $request->validate([
            'insumo_id' => 'required|exists:insumos,id',
            'tipo'      => 'required|in:ENTRADA,MERMA,AJUSTE',
            'cantidad'  => 'required|numeric|gt:0',
            'motivo'    => 'nullable|string|max:255',
        ], [
            'insumo_id.required' => 'El insumo es obligatorio.',
            'tipo.in'            => 'El tipo debe ser ENTRADA, MERMA o AJUSTE.',
            'cantidad.gt'        => 'La cantidad debe ser mayor a cero.',
        ]);

        return DB::transaction(function () use ($request) {
            $insumo = Insumo::lockForUpdate()->findOrFail($request->insumo_id);
            $cantidad = (float) $request->cantidad;

            if ($request->tipo === 'ENTRADA') {
                $insumo->increment('stock_actual', $cantidad);
                $insumo->refresh();
            } else {
                // Utiliza la lógica nativa del modelo Insumo (descuenta + notifica si stock <= mínimo)
                $insumo->descontarStock($cantidad);
            }

            // Registrar la auditoría
            $movimiento = MovimientoInventario::create([
                'insumo_id'        => $insumo->id,
                'user_id'          => $request->user()?->id,
                'tipo'             => $request->tipo,
                'cantidad'         => $cantidad,
                'stock_resultante' => $insumo->stock_actual,
                'motivo'           => $request->motivo ?? 'Registro manual de inventario',
            ]);

            return response()->json([
                'status'       => 'success',
                'message'      => 'Movimiento de inventario registrado correctamente.',
                'nuevo_stock'  => $insumo->stock_actual,
                'stock_bajo'   => $insumo->stock_actual <= $insumo->stock_minimo,
                'data'         => $movimiento->load(['insumo', 'usuario:id,name'])
            ], 201);
        });
    }

    /**
     * Consultar detalle de un movimiento específico.
     * GET /api/movimientos-inventario/{id}
     */
    public function show($id)
    {
        $movimiento = MovimientoInventario::with(['insumo', 'usuario:id,name,email'])->findOrFail($id);

        return response()->json([
            'status' => 'success',
            'data'   => $movimiento
        ], 200);
    }
}