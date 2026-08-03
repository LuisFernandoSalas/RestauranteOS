<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Categoria;
use App\Models\Producto;
use Carbon\Carbon;

class MenuController extends Controller
{
    public function index()
    {
        // Traemos todas las categorías y sus productos disponibles
        $menu = Categoria::with(['productos' => function ($query) {
            $query->where(function($q) {
                $q->whereNull('pausado_hasta')
                  ->orWhere('pausado_hasta', '<=', Carbon::now());
            })->where('status', 'activo');
        }])->get();

        return response()->json($menu);
    }
}