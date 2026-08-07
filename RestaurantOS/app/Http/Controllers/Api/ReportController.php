<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\DetallePedido;
use App\Models\Insumo;
use App\Models\Pago;
use App\Models\Pedido;
use App\Models\User;
use App\Models\Mesa;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class ReportController extends Controller
{
    /**
     * GET /api/dashboard/reportes
     * Sincroniza las maquetas de gráficas y tablas con los datos reales del ecosistema.
     */
    public function getDashboardData()
    {
        $hoy = now()->today();
        $inicioSemana = now()->startOfWeek();

        // 1. Resumen
        $totalVentasHoy = Pago::whereDate('created_at', $hoy)->sum('monto_recibido');
        $pedidosActivos = Pedido::whereIn('estado', ['pendiente', 'en_proceso', 'listo'])->count();
        $mesasOcupadas = Mesa::where('estado', 'ocupada')->count();
        $alertasStock = Insumo::whereColumn('stock_actual', '<=', 'stock_minimo')->count();

        // 2. Datos diarios (Compatible con SQLite y MySQL)
        $pagosSemana = Pago::where('created_at', '>=', $inicioSemana)->get();
        
        $diasEspaniol = [
            1 => 'Lunes', 2 => 'Martes', 3 => 'Miércoles',
            4 => 'Jueves', 5 => 'Viernes', 6 => 'Sábado', 7 => 'Domingo'
        ];

        $datosDiarios = $pagosSemana->groupBy(function ($pago) {
            return $pago->created_at->format('N'); // 1 (Lunes) a 7 (Domingo)
        })->map(function ($grupo, $diaNum) use ($diasEspaniol) {
            return [
                'dia' => $diasEspaniol[$diaNum],
                'ventas' => (float) $grupo->sum('monto_recibido'),
                'propina' => (float) $grupo->sum('propina'),
                'mesas' => $grupo->pluck('pedido_id')->unique()->count(),
            ];
        })->values();

        // 3. Métodos de pago
        $metodosPago = Pago::selectRaw('metodo_pago, count(*) as cantidad, sum(monto_recibido) as total')
            ->groupBy('metodo_pago')
            ->get();

        // 4. Top Productos
        $topProductos = DetallePedido::selectRaw('producto_id, sum(cantidad) as total_vendido, sum(subtotal) as total_ingresos')
            ->with('producto:id,nombre')
            ->groupBy('producto_id')
            ->orderByDesc('total_vendido')
            ->take(5)
            ->get();

        // 5. Inventario Crítico
        $inventarioCritico = Insumo::whereColumn('stock_actual', '<=', 'stock_minimo')
            ->take(5)
            ->get();

        return response()->json([
            'status' => 'success',
            'resumen' => [
                'ventas_hoy' => (float) $totalVentasHoy,
                'pedidos_activos' => $pedidosActivos,
                'mesas_ocupadas' => $mesasOcupadas,
                'alertas_stock' => $alertasStock,
            ],
            'datos_diarios' => $datosDiarios,
            'metodos_pago' => $metodosPago,
            'top_productos' => $topProductos,
            'inventario_critico' => $inventarioCritico,
            'auditoria' => []
        ]);
    }

    /**
     * GET /api/reportes/ventas?fecha_inicio=X&fecha_fin=Y
     * Total de ingresos recaudados en un periodo determinado.
     */
    public function ventas(Request $request): JsonResponse
    {
        $request->validate([
            'fecha_inicio' => 'required|date',
            'fecha_fin'    => 'required|date|after_or_equal:fecha_inicio',
        ]);

        $fechaInicio = $request->fecha_inicio . ' 00:00:00';
        $fechaFin    = $request->fecha_fin . ' 23:59:59';

        $totalIngresos  = (float) Pago::whereBetween('created_at', [$fechaInicio, $fechaFin])->sum('monto_recibido');
        $totalPropinas  = (float) Pago::whereBetween('created_at', [$fechaInicio, $fechaFin])->sum('propina');
        $totalComandas  = Pedido::where('estado', 'pagado')->whereBetween('created_at', [$fechaInicio, $fechaFin])->count();
        $promedioComanda = $totalComandas > 0 ? round($totalIngresos / $totalComandas, 2) : 0;

        return response()->json([
            'status' => 'success',
            'data'   => [
                'fecha_inicio'     => $request->fecha_inicio,
                'fecha_fin'        => $request->fecha_fin,
                'total_ingresos'   => $totalIngresos,
                'total_propinas'   => $totalPropinas,
                'total_comandas'   => $totalComandas,
                'promedio_comanda' => $promedioComanda,
            ]
        ], 200);
    }

    /**
     * GET /api/reportes/productos-populares
     * Top 5 de platillos y combos más vendidos.
     */
    public function productosPopulares(): JsonResponse
    {
        $topProductos = DetallePedido::select(
                'producto_id',
                DB::raw('SUM(cantidad) as total_vendido'),
                DB::raw('SUM(subtotal) as total_recaudado')
            )
            ->whereNotNull('producto_id')
            ->whereHas('pedido', fn($q) => $q->where('estado', 'pagado'))
            ->with('producto:id,nombre,precio')
            ->groupBy('producto_id')
            ->orderByDesc('total_vendido')
            ->limit(5)
            ->get();

        $topCombos = DetallePedido::select(
                'combo_id',
                DB::raw('SUM(cantidad) as total_vendido'),
                DB::raw('SUM(subtotal) as total_recaudado')
            )
            ->whereNotNull('combo_id')
            ->whereHas('pedido', fn($q) => $q->where('estado', 'pagado'))
            ->with('combo:id,nombre,precio')
            ->groupBy('combo_id')
            ->orderByDesc('total_vendido')
            ->limit(5)
            ->get();

        return response()->json([
            'status' => 'success',
            'data'   => [
                'productos' => $topProductos,
                'combos'    => $topCombos,
            ]
        ], 200);
    }

    /**
     * GET /api/reportes/rendimiento-meseros
     * Cantidad de comandas atendidas y total facturado por cada mesero.
     */
    public function rendimientoMeseros(): JsonResponse
    {
        $rendimiento = User::where('role', 'mesero')
            ->withCount(['pedidos as comandas_atendidas' => function ($q) {
                $q->where('estado', 'pagado');
            }])
            ->withSum(['pedidos as total_facturado' => function ($q) {
                $q->where('estado', 'pagado');
            }], 'total')
            ->get()
            ->map(function ($mesero) {
                return [
                    'mesero_id'          => $mesero->id,
                    'nombre'             => $mesero->name,
                    'comandas_atendidas' => $mesero->comandas_atendidas ?? 0,
                    'total_facturado'    => (float) ($mesero->total_facturado ?? 0),
                ];
            });

        return response()->json([
            'status' => 'success',
            'data'   => $rendimiento
        ], 200);
    }
}