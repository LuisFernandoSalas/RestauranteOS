<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pedido;
use App\Models\Pago;
use App\Models\Insumo;
use App\Models\DetallePedido;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\DB;

class ReportController extends Controller
{
    /**
     * GET /api/dashboard/reportes
     * Sincroniza las maquetas de gráficas y tablas con los datos reales del ecosistema.
     */
    public function getDashboardData(): JsonResponse
    {
        $hoy = Carbon::today();

        // 1. RESUMEN GENERAL (Utilizando las tablas reales de Restaurant OS)
        $ventasTotales   = (float) Pago::sum('monto_recibido');
        $propinasTotales = (float) Pago::sum('propina');
        $ordenesTotales  = Pedido::count();
        // Contamos las solicitudes de factura electrónica (CFDI) marcadas en caja
        $cfdisEmitidos   = Pago::where('requiere_factura', true)->count();

        $resumen = [
            'ventas_totales'   => $ventasTotales,
            'ordenes_totales'  => $ordenesTotales,
            'propinas_totales' => $propinasTotales,
            'cfdis_emitidos'   => $cfdisEmitidos,
        ];

        // 2. DATOS DIARIOS (Para gráficas de barra y de área histórica)
        $datosDiarios = Pago::select(
            DB::raw('DAYNAME(created_at) as dia'),
            DB::raw('SUM(monto_recibido) as ventas'),
            DB::raw('SUM(propina) as propina'),
            DB::raw('COUNT(DISTINCT pedido_id) as mesas')
        )
            ->groupBy('dia')
            ->orderBy(DB::raw('FIELD(dia, "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")'))
            ->get();

        // 3. MÉTODOS DE PAGO DEL DÍA (Corte de caja preciso)
        $totalDia = Pago::whereDate('created_at', $hoy)->sum('monto_recibido') ?: 1; // Evitar división por cero
        
        $metodosPago = Pago::whereDate('created_at', $hoy)
            ->select('metodo_pago as tipo', DB::raw('SUM(monto_recibido) as monto'))
            ->groupBy('metodo_pago')
            ->get()
            ->map(function ($item) use ($totalDia) {
                $item->monto = (float) $item->monto;
                $item->porcentaje = round(($item->monto / $totalDia) * 100, 1);
                return $item;
            });

        // 4. 🚀 TOP 5 PRODUCTOS MÁS VENDIDOS (Como pide tu slide de "Efficiency Metrics")
        $topProductos = DetallePedido::select('producto_id', DB::raw('SUM(cantidad) as total_unidades'))
            ->whereHas('pedido', function ($query) {
                $query->where('estado', 'entregado'); // Solo sumamos ventas completadas con éxito
            })
            ->groupBy('producto_id')
            ->with('producto:id,nombre,precio')
            ->orderBy('total_unidades', 'desc')
            ->take(5)
            ->get();

        // 5. 📦 INVENTARIO CRÍTICO (Alerta de desabasto / Función 86 preventiva de administración)
        // Muestra insumos cuyo stock_actual esté en un nivel igual o inferior a su stock_minimo
        $inventarioCritico = Insumo::select('id', 'nombre', 'stock_actual', 'unidad_medida')
            ->whereRaw('stock_actual <= 5.00') // Asumiendo un stock mínimo estándar de alerta
            ->orderBy('stock_actual', 'asc')
            ->get();

        // 6. 📉 AUDITORÍA DE PÉRDIDAS POR CANCELACIONES (Mermas del día)
        $mermasHoy = (float) Pedido::onlyTrashed() // Captura registros eliminados por SoftDelete
            ->whereDate('deleted_at', $hoy)
            ->get()
            ->sum('total_calculado');

        // 7. RESPUESTA JSON TOTALIZADA
        return response()->json([
            'status'             => 'success',
            'resumen'            => $resumen,
            'datos_diarios'      => $datosDiarios,
            'metodos_pago'       => $metodosPago,
            'top_productos'      => $topProductos,
            'inventario_critico' => $inventarioCritico,
            'auditoria'          => [
                'mermas_cancelaciones_hoy' => $mermasHoy
            ]
        ], 200);
    }
}