<?php

namespace App\Http\Controllers\Api;

use App\Models\Order;
use App\Models\Invoice;
use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;


class ReportController extends Controller
{
    public function getDashboardData()
    {
        // 1. Resumen: Ventas, Órdenes, Propinas y CFDI
        $resumen = [
            'ventas_totales' => Order::sum('total_amount'),
            'ordenes_totales' => Order::count(),
            'propinas_totales' => Order::sum('tip_amount'),
            'cfdis_emitidos' => Invoice::count(),
        ];

        // 2. Datos para tablas y gráficas: agrupados por día de la semana
        // Usamos DAYNAME para obtener el nombre del día y agrupamos por ese campo
        $datosDiarios = Order::select(
            DB::raw('DAYNAME(created_at) as dia'),
            DB::raw('SUM(total_amount) as ventas'),
            DB::raw('SUM(tip_amount) as propina'),
            DB::raw('COUNT(id) as mesas')
        )
            ->groupBy('dia')
            ->orderBy(DB::raw('FIELD(dia, "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")'))
            ->get();

        // 3. Métodos de pago (Efectivo, Tarjeta, Mixto)
        $totalGeneral = Order::sum('total_amount') ?: 1; // Evitar división por cero
        $metodosPago = Order::select('payment_method as tipo', DB::raw('SUM(total_amount) as monto'))
            ->groupBy('payment_method')
            ->get()
            ->map(function ($item) use ($totalGeneral) {
                $item->porcentaje = round(($item->monto / $totalGeneral) * 100, 1);
                return $item;
            });

        return response()->json([
            'resumen' => $resumen,
            'datos_diarios' => $datosDiarios,
            'metodos_pago' => $metodosPago
        ]);
    }
}
