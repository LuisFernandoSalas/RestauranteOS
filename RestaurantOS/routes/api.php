<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\PedidoController;
use App\Http\Controllers\Api\MesaController;
use App\Http\Controllers\Api\ProductoController;
use App\Http\Controllers\Api\MenuController;
use App\Http\Controllers\Api\ReportController;
use App\Http\Controllers\Api\V1\PagoController;   // Nuevo motor financiero
use App\Http\Controllers\Api\V1\CocinaController; // Nuevo motor KDS de Cocina

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
*/

// --- ACCESO PÚBLICO / LOGIN ---
Route::post('/login', [AuthController::class, 'login']);

// --- ZONA PROTEGIDA (SESIÓN ACTIVA SANCTUM) ---
Route::middleware('auth:sanctum')->group(function () {
    
    // ==========================================
    // 🔐 1. AUTENTICACIÓN ACTIVA Y PERFIL
    // ==========================================
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', function (Request $request) {
        return response()->json($request->user(), 200);
    });
    
    // ==========================================
    // 📋 2. CATÁLOGOS BASE (Menú QR y Dispositivos)
    // ==========================================
    Route::get('/menu', [MenuController::class, 'index']);
    Route::get('/mesas', [MesaController::class, 'index']);
    Route::get('/productos', [ProductoController::class, 'index']); 
    Route::get('/productos/{id}', [ProductoController::class, 'show']);

    // ==========================================
    // 📲 3. MÓDULO DE MESEROS (Gestión de Comandas)
    // ==========================================
    Route::get('/pedidos', [PedidoController::class, 'index']);
    Route::post('/pedidos', [PedidoController::class, 'store']); // Envío masivo Android
    Route::get('/pedidos/{id}', [PedidoController::class, 'show']);
    Route::put('/pedidos/{id}', [PedidoController::class, 'update']);

    // ==========================================
    // 🍳 4. MÓDULO DE COCINA (Kitchen Display System - KDS)
    // ==========================================
    // Obtiene el feed horizontal de comandos activos
    Route::get('/cocina/pedidos', [CocinaController::class, 'index']);
    // Avanza o cambia el estado por platillo individual (KDS interactivo)
    Route::patch('/cocina/detalles/{id}/estado', [CocinaController::class, 'updatePlatilloEstado']);
    // Cancela una orden completa inyectando motivo (Auditoría/Mermas)
    Route::post('/cocina/pedidos/{id}/cancelar', [CocinaController::class, 'cancelarPedido']);
    // Función 86: Desactiva un producto temporalmente del catálogo
    Route::post('/cocina/productos/{id}/pausar', [CocinaController::class, 'pausarProducto']);

    // ==========================================
    // 💵 5. MÓDULO DE CAJA (Cierre de Cuentas y POS)
    // ==========================================
    Route::get('/pagos', [PagoController::class, 'index']);//Historial de caja
    // Endpoint Unificado: Soporta Pago Simple, Mixto (Efectivo/Tarjeta) y Propinas Abiertas/Cerradas
    Route::post('/pedidos/{id}/cobrar', [PagoController::class, 'cobrar']);

    // ==========================================
    // 📈 6. CONTROL DE ADMINISTRACIÓN Y REPORTES
    // ==========================================
    // Cambiado de delete absoluto a deshabilitación controlada o softdelete si aplica
    Route::delete('/pedidos/{id}', [PedidoController::class, 'destroy']);
    Route::get('/dashboard/reportes', [ReportController::class, 'getDashboardData']);
});