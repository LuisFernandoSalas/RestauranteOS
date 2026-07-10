<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Pago;
use Carbon\Carbon;

class CorteCajaController extends Controller
{
    //
    // app/Http/Controllers/Api/CorteCajaController.php
    public function cerrarTurno(Request $request)
    {
        $hoy = Carbon::today();

        // Sumamos lo que el sistema dice que entró en efectivo
        $ventasEfectivoSistema = Pago::whereDate('created_at', $hoy)
            ->where('metodo_pago', 'efectivo')
            ->sum('monto_recibido');

        $efectivoFisico = $request->efectivo_en_caja;
        $diferencia = $efectivoFisico - $ventasEfectivoSistema;

        return response()->json([
            'ventas_sistema' => $ventasEfectivoSistema,
            'efectivo_real' => $efectivoFisico,
            'diferencia' => $diferencia,
            'mensaje' => $diferencia == 0 ? 'Caja cuadrada' : 'Hay un descuadre'
        ]);
    }
}
