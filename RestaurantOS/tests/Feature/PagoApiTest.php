<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\Producto;
use App\Models\DetallePedido;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class PagoApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test: Un usuario (cajero/administrador) puede procesar el cobro de un pedido.
     */
    public function test_cajero_puede_cobrar_un_pedido_y_liberar_mesa(): void
    {
        // 1. Autenticar al usuario
        $user = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($user, ['*']);

        // 2. Armar el escenario previo
        $mesa = Mesa::factory()->create(['estado' => 'ocupada']);
        $producto = Producto::factory()->create(['precio' => 200.00]);
        
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado' => 'listo',
            'total' => 400.00
        ]);

        // Registrar el consumo
        DetallePedido::create([
            'pedido_id' => $pedido->id,
            'producto_id' => $producto->id,
            'cantidad' => 2,
            'precio_unitario' => 200.00,
            'subtotal' => 400.00,
            'estado' => 'listo'
        ]);

        // Forzar la carga de la relación para que el controlador la detecte
        $pedido->load('mesa');

        // 3. Payload con campos en la raíz (no anidados)
        $payload = [
            'client_uuid' => (string) \Illuminate\Support\Str::uuid(),
            'metodo_pago' => 'efectivo',
            'monto_recibido' => 440.00, // Total del pedido (400) + Propina (40)
            'propina' => 40.00,
            'requiere_factura' => false,
        ];

        // 4. Consumir el endpoint POST
        $response = $this->postJson("/api/pedidos/{$pedido->id}/cobrar", $payload);

        // 5. Aseveraciones de la respuesta
        $response->assertStatus(200);
        $response->assertJsonFragment([
            'status' => 'success',
            'mensaje' => 'Pedido cobrado con éxito.'
        ]);

        // 6. Verificar efectos colaterales en la base de datos
        // Comprobamos que el pedido cambió a 'pagado'
        $this->assertDatabaseHas('pedidos', [
            'id' => $pedido->id,
            'estado' => 'pagado'
        ]);

        // Comprobamos directamente en la tabla que la mesa se liberó a 'disponible'
        $this->assertDatabaseHas('mesas', [
            'id' => $mesa->id,
            'estado' => 'disponible'
        ]);
    }

    /**
     * Test: Un usuario autenticado puede listar los pagos registrados.
     */
    public function test_usuario_puede_listar_pagos(): void
    {
        $user = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($user, ['*']);

        $response = $this->getJson('/api/pagos');

        $response->assertStatus(200);
        $response->assertJsonFragment(['status' => 'success']);
    }
}