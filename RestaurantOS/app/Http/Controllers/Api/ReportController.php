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
        $ordenesHoy     = Pedido::whereDate('created_at', $hoy)->count(); // Total de órdenes creadas hoy
        $mesasOcupadas  = Mesa::where('estado', 'ocupada')->count();
        $alertasStock   = Insumo::whereColumn('stock_actual', '<=', 'stock_minimo')->count();

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

        // 6. Resumen de Transacciones (Últimos pagos realizados)
        $transacciones = Pago::with(['pedido.detalles', 'pedido.factura'])
            ->latest()
            ->take(10) // Muestra las últimas 10 transacciones
            ->get()
            ->map(function ($pago) {
                // Cantidad total de productos en este pedido
                $cantProductos = $pago->pedido ? $pago->pedido->detalles->sum('cantidad') : 0;

                // Verifica si existe un registro en la tabla 'facturas' para este pedido
                $tieneCfdi = $pago->pedido && $pago->pedido->factura !== null;

                return [
                    'id'      => '#' . str_pad($pago->id, 4, '0', STR_PAD_LEFT),
                    'hora'    => $pago->created_at->format('H:i'),
                    'detalle' => $cantProductos . ($cantProductos === 1 ? ' producto' : ' productos'),
                    'metodo'  => ucfirst($pago->metodo_pago),
                    'monto'   => (float) $pago->monto_recibido,
                    'cfdi'    => $tieneCfdi, // Booleano: true (muestra palomita) / false (vacio)
                ];
            });

        return response()->json([
            'status' => 'success',
            'resumen' => [
                'ventas_hoy'      => (float) $totalVentasHoy,
                'pedidos_activos' => $pedidosActivos,
                'ordenes_hoy'     => $ordenesHoy,
                'mesas_ocupadas'  => $mesasOcupadas,
                'alertas_stock'   => $alertasStock,
            ],
            'datos_diarios'      => $datosDiarios,
            'metodos_pago'       => $metodosPago,
            'top_productos'      => $topProductos,
            'inventario_critico' => $inventarioCritico,
            'transacciones'      => $transacciones, // <-- Clave agregada
            'auditoria'          => []
        ]);
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