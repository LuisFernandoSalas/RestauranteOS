<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\PedidoController;
use App\Http\Controllers\Api\MesaController;
use App\Http\Controllers\Api\ProductoController;
use App\Http\Controllers\Api\MenuController;
use App\Http\Controllers\Api\CajaController;
use App\Http\Controllers\Api\ReportController;

Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    
    //--- AUTENTICACIÓN ACTIVA Y PERFIL ---
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', function (Request $request) {
        return response()->json($request->user(), 200);
    });
    
    //--- CATÁLOGOS BASE ---
    Route::get('/menu', [MenuController::class, 'index']);
    Route::get('/mesas', [MesaController::class, 'index']);
    Route::get('/productos', [ProductoController::class, 'index']); 
    Route::get('/productos/{id}', [ProductoController::class, 'show']);

    //--- ECOSISTEMA DE PEDIDOS Y COMANDAS ---
    Route::get('/pedidos', [PedidoController::class, 'index']);
    Route::post('/pedidos', [PedidoController::class, 'store']);
    
    // Ruta añadida para monitor de cocina (Prioritaria)
    Route::get('/pedidos/cocina', [PedidoController::class, 'getPedidosCocina']);
    
    Route::get('/pedidos/{id}', [PedidoController::class, 'show']);
    Route::put('/pedidos/{id}', [PedidoController::class, 'update']);

    //--- OPERACIONES DE CAJA ---
    Route::get('/pedidos/{id}/detalle-cobro', [CajaController::class, 'obtenerDetalleCobro']);
    Route::post('/pedidos/{id}/cobrar', [CajaController::class, 'cobrar']);

    //--- CONTROL DE ADMINISTRACIÓN ---
    Route::delete('/pedidos/{id}', [PedidoController::class, 'destroy']);
    Route::get('/dashboard/reportes', [ReportController::class, 'getDashboardData']);
});