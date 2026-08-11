<?php

namespace Tests\Feature;

use App\Models\DetallePedido;
use App\Models\Mesa;
use App\Models\Pedido;
use App\Models\Producto;
use App\Models\TurnoCaja;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class PagoApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_cajero_puede_cobrar_un_pedido_y_liberar_mesa(): void
    {
        $user = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($user, ['*']);

        // Se usa 'ABIERTO' en mayúsculas para cumplir con el CHECK constraint
        TurnoCaja::create([
            'user_id'        => $user->id,
            'estado'         => 'ABIERTO',
            'monto_apertura' => 1000.00,
            'opened_at'      => now(),
            'fecha_apertura' => now(),
        ]);

        $mesa = Mesa::factory()->create(['estado' => 'ocupada']);
        $producto = Producto::factory()->create(['precio' => 200.00]);
        
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado'  => 'listo',
            'total'   => 400.00
        ]);

        DetallePedido::create([
            'pedido_id'       => $pedido->id,
            'producto_id'     => $producto->id,
            'cantidad'        => 2,
            'precio_unitario' => 200.00,
            'subtotal'        => 400.00,
            'estado'          => 'listo'
        ]);

        $pedido->load('mesa');

        $payload = [
            'client_uuid'      => (string) \Illuminate\Support\Str::uuid(),
            'metodo_pago'      => 'efectivo',
            'monto_recibido'   => 440.00,
            'propina'          => 40.00,
            'requiere_factura' => false,
        ];

        $response = $this->postJson("/api/pedidos/{$pedido->id}/cobrar", $payload);

        $response->assertStatus(200);
        $response->assertJsonFragment([
            'status'  => 'success',
            'mensaje' => 'Pedido cobrado con éxito.'
        ]);

        $this->assertDatabaseHas('pedidos', [
            'id'     => $pedido->id,
            'estado' => 'pagado'
        ]);

        $this->assertDatabaseHas('mesas', [
            'id'     => $mesa->id,
            'estado' => 'disponible'
        ]);
    }

    public function test_usuario_puede_listar_pagos(): void
    {
        $user = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($user, ['*']);

        $response = $this->getJson('/api/pagos');

        $response->assertStatus(200);
        $response->assertJsonFragment(['status' => 'success']);
    }
}