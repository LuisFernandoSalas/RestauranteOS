<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\PedidoController;
use App\Http\Controllers\Api\MesaController;
use App\Http\Controllers\Api\ProductoController;
use App\Http\Controllers\Api\MenuController;
use App\Http\Controllers\Api\ComboController;
use App\Http\Controllers\Api\ReportController;
use App\Http\Controllers\Api\CajaController; 
use App\Http\Controllers\Api\V1\CocinaController; 
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\FacturacionController;

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
    // 🍽️ 2. COMBOS (Promociones y Paquetes)
    // ==========================================
    Route::apiResource('combos', ComboController::class);

    // ==========================================
    // 📲 3. MÓDULO DE MESEROS (Gestión de Comandas)
    // ==========================================
    Route::get('/pedidos', [PedidoController::class, 'index']);
    Route::post('/pedidos', [PedidoController::class, 'store']); // Envío masivo Android
    Route::get('/pedidos/{id}', [PedidoController::class, 'show']);
    Route::put('/pedidos/{id}', [PedidoController::class, 'update']);
    Route::get('/pedidos/mesa/{mesa_id}/activo', [PedidoController::class, 'porMesa']);

    // ==========================================
    // 🍳 4. MÓDULO DE COCINA (Kitchen Display System - KDS)
    // ==========================================
    Route::get('/cocina/pedidos', [CocinaController::class, 'index']);
    Route::patch('/cocina/detalles/{id}/estado', [CocinaController::class, 'updatePlatilloEstado']);
    Route::post('/cocina/pedidos/{id}/cancelar', [CocinaController::class, 'cancelarPedido']);
    Route::post('/cocina/productos/{id}/pausar', [CocinaController::class, 'pausarProducto']);

    // ==========================================
    // 💰 5. MÓDULO DE CAJA (Cobro y Pagos)
    // ==========================================
    Route::get('/pagos', [CajaController::class, 'index']); // 👈 Historial de pagos y Cierre de caja
    Route::get('/pedidos/{id}/detalle-cobro', [CajaController::class, 'obtenerDetalleCobro']);
    Route::post('/pedidos/{id}/cobrar', [CajaController::class, 'cobrar']);
    Route::post('/pedidos/{id}/dividir-partes', [CajaController::class, 'calcularDivisionPartes']);

    // ==========================================
    // 📈 6. CONTROL DE ADMINISTRACIÓN Y REPORTES
    // ==========================================
    Route::delete('/pedidos/{id}', [PedidoController::class, 'destroy']);
    Route::get('/dashboard/reportes', [ReportController::class, 'getDashboardData']);
    
    // 📊 Nuevos Endpoints de Reportes Avanzados
    Route::get('/reportes/ventas', [ReportController::class, 'ventas']);
    Route::get('/reportes/productos-populares', [ReportController::class, 'productosPopulares']);
    Route::get('/reportes/rendimiento-meseros', [ReportController::class, 'rendimientoMeseros']);

    // ==========================================
    // 🔔 7. RUTAS DE NOTIFICACIONES
    // ==========================================
    Route::get('/notificaciones', [NotificationController::class, 'index']);
    Route::put('/notificaciones/read-all', [NotificationController::class, 'markAllAsRead']);
    Route::put('/notificaciones/{id}/read', [NotificationController::class, 'markAsRead']);

    // ==========================================
    // 🧾 8. MÓDULO DE FACTURACIÓN ELECTRÓNICA
    // ==========================================
    Route::post('/facturacion/generar', [FacturacionController::class, 'generar']);
    
});