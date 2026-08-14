<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\UserController;
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
use App\Http\Controllers\Api\V1\InsumoController;
use App\Http\Controllers\Api\V1\RecetaController;
use App\Http\Controllers\Api\V1\MovimientoInventarioController;

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
    //  1. AUTENTICACIÓN ACTIVA Y PERFIL
    // ==========================================
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/me', function (Request $request) {
        return response()->json($request->user(), 200);
    });

    // ==========================================
    //  2. MÓDULO DE GESTIÓN DE EMPLEADOS (ADMIN)
    // ==========================================
    Route::get('/empleados', [UserController::class, 'index']);
    Route::post('/empleados', [UserController::class, 'store']);
    Route::get('/empleados/{id}', [UserController::class, 'show']);
    Route::put('/empleados/{id}', [UserController::class, 'update']);
    Route::delete('/empleados/{id}', [UserController::class, 'destroy']);
    
    // ==========================================
    //  3. CATÁLOGOS BASE (Menú QR y Dispositivos)
    // ==========================================
    Route::get('/menu', [MenuController::class, 'index']);
    Route::get('/mesas', [MesaController::class, 'index']);
    Route::apiResource('productos', ProductoController::class);

    // ==========================================
    //  4. COMBOS (Promociones y Paquetes)
    // ==========================================
    Route::apiResource('combos', ComboController::class);

    // ==========================================
    //  5. MÓDULO DE MESEROS (Gestión de Comandas)
    // ==========================================
    Route::get('/pedidos', [PedidoController::class, 'index']);
    Route::post('/pedidos', [PedidoController::class, 'store']); // Envío masivo Android
    Route::get('/pedidos/{id}', [PedidoController::class, 'show']);
    Route::put('/pedidos/{id}', [PedidoController::class, 'update']);
    Route::get('/pedidos/mesa/{mesa_id}/activo', [PedidoController::class, 'porMesa']);

    // ==========================================
    //  6. MÓDULO DE COCINA (Kitchen Display System - KDS)
    // ==========================================
    Route::get('/cocina/pedidos', [CocinaController::class, 'index']);
    
    // --- ACCIONES SOBRE PLATILLO INDIVIDUAL ---
    // Acepta body: { "estado": "en_preparacion" | "pausado" | "listo" | "cancelado" }
    Route::patch('/cocina/detalles/{id}/estado', [CocinaController::class, 'updatePlatilloEstado']);

    // --- ACCIONES SOBRE EL PEDIDO COMPLETO ---
    // Acepta body: { "estado": "en_preparacion" | "pausado" | "listo" }
    Route::patch('/cocina/pedidos/{id}/estado', [CocinaController::class, 'updatePedidoEstado']);
    Route::post('/cocina/pedidos/{id}/cancelar', [CocinaController::class, 'cancelarPedido']);

    // --- PAUSAR PRODUCTO DEL MENÚ GENERAL (Regla 86) ---
    Route::post('/cocina/productos/{id}/pausar', [CocinaController::class, 'pausarProducto']);

    // ==========================================
    //  7. MÓDULO DE CAJA (Cobro, Pagos y Turnos)
    // ==========================================
    Route::get('/pagos', [CajaController::class, 'index']); // Historial de pagos
    Route::get('/pedidos/{id}/detalle-cobro', [CajaController::class, 'obtenerDetalleCobro']);
    Route::post('/pedidos/{id}/cobrar', [CajaController::class, 'cobrar']);
    Route::post('/pedidos/{id}/dividir-partes', [CajaController::class, 'calcularDivisionPartes']);

    //  8. Control de Turnos y Arqueo de Caja
    Route::get('/caja/turno-actual', [CajaController::class, 'obtenerTurnoActual']);
    Route::post('/caja/abrir-turno', [CajaController::class, 'abrirTurno']);
    Route::post('/caja/cerrar-turno', [CajaController::class, 'cerrarTurno']);
    Route::get('/caja/historial-turnos', [CajaController::class, 'historialTurnos']);

    // ==========================================
    //  9. CONTROL DE ADMINISTRACIÓN Y REPORTES
    // ==========================================
    Route::delete('/pedidos/{id}', [PedidoController::class, 'destroy']);
    Route::get('/dashboard/reportes', [ReportController::class, 'getDashboardData']);
    
    //  Nuevos Endpoints de Reportes Avanzados
    Route::get('/reportes/ventas', [ReportController::class, 'ventas']);
    Route::get('/reportes/productos-populares', [ReportController::class, 'productosPopulares']);
    Route::get('/reportes/rendimiento-meseros', [ReportController::class, 'rendimientoMeseros']);

    // ==========================================
    //  10. RUTAS DE NOTIFICACIONES
    // ==========================================
    Route::get('/notificaciones', [NotificationController::class, 'index']);
    Route::put('/notificaciones/read-all', [NotificationController::class, 'markAllAsRead']);
    Route::put('/notificaciones/{id}/read', [NotificationController::class, 'markAsRead']);

    // ==========================================
    //  11. MÓDULO DE FACTURACIÓN ELECTRÓNICA
    // ==========================================
    Route::post('/facturacion/generar', [FacturacionController::class, 'generar']);

    // ==========================================
    //  12. MÓDULO DE INVENTARIO, INSUMOS Y RECETAS
    // ==========================================
    // Catálogo e Insumos
    Route::get('/insumos', [InsumoController::class, 'index']);
    Route::post('/insumos', [InsumoController::class, 'store']);
    Route::put('/insumos/{id}', [InsumoController::class, 'update']);
    Route::delete('/insumos/{id}', [InsumoController::class, 'destroy']);
    Route::post('/insumos/movimiento', [InsumoController::class, 'registrarMovimiento']);

    // Recetas de Productos
    Route::get('/recetas', [RecetaController::class, 'index']);
    Route::get('/recetas/producto/{productoId}', [RecetaController::class, 'show']);
    Route::post('/recetas', [RecetaController::class, 'store']);
    Route::delete('/recetas/{id}', [RecetaController::class, 'destroy']);
    Route::delete('/recetas/producto/{productoId}', [RecetaController::class, 'destroyByProducto']);

    // Historial y Auditoría de Movimientos
    Route::get('/movimientos-inventario', [MovimientoInventarioController::class, 'index']);
    Route::post('/movimientos-inventario', [MovimientoInventarioController::class, 'store']);
    Route::get('/movimientos-inventario/{id}', [MovimientoInventarioController::class, 'show']);
});