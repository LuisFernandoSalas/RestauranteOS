<?php

namespace Tests\Feature;

use App\Models\Mesa;
use App\Models\Pago;
use App\Models\Pedido;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class SplitPaymentTest extends TestCase
{
    use RefreshDatabase;

    protected User $cajero;

    protected function setUp(): void
    {
        parent::setUp();
        $this->cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($this->cajero);
    }

    public function test_puede_obtener_detalle_de_cobro()
    {
        $mesa = Mesa::factory()->create(['estado' => 'ocupada']);
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->cajero->id,
            'total'   => 150.00,
        ]);

        $response = $this->getJson("/api/pedidos/{$pedido->id}/detalle-cobro");

        $response->assertStatus(200)
                 ->assertJson([
                     'status'          => 'success',
                     'total_pedido'    => 150.00,
                     'total_pagado'    => 0.00,
                     'saldo_pendiente' => 150.00,
                 ]);
    }

    public function test_puede_calcular_division_por_partes_iguales()
    {
        $mesa = Mesa::factory()->create();
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->cajero->id,
            'total'   => 300.00,
        ]);

        $response = $this->postJson("/api/pedidos/{$pedido->id}/dividir-partes", [
            'personas'           => 3,
            'porcentaje_propina' => 10,
        ]);

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'data'   => [
                         'total_comanda'     => 300.00,
                         'numero_personas'   => 3,
                         'subtotal_persona'  => 100.00,
                         'propina_persona'   => 10.00,
                         'total_por_persona' => 110.00,
                     ]
                 ]);
    }

    public function test_puede_realizar_pago_parcial_y_mantener_pedido_activo()
    {
        $mesa = Mesa::factory()->create(['estado' => 'ocupada']);
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->cajero->id,
            'estado'  => 'en_proceso',
            'total'   => 200.00,
        ]);

        // Primer abonador paga $100 de $200
        $response = $this->postJson("/api/pedidos/{$pedido->id}/cobrar", [
            'monto_recibido' => 100.00,
            'propina'        => 10.00,
            'metodo_pago'    => 'efectivo',
        ]);

        $response->assertStatus(200);
        
        // Asertar numéricamente sin conflicto de Int vs Float
        $this->assertEquals(100.00, $response->json('saldo_pendiente'));
        $this->assertEquals(100.00, $response->json('total_pagado'));

        // Se creó el registro de pago asociado al cajero
        $this->assertDatabaseHas('pagos', [
            'pedido_id'      => $pedido->id,
            'monto_recibido' => 100.00,
            'cobrado_por'    => $this->cajero->id,
        ]);

        // La mesa y el pedido continúan activos
        $this->assertDatabaseHas('pedidos', ['id' => $pedido->id, 'estado' => 'en_proceso']);
        $this->assertDatabaseHas('mesas', ['id' => $mesa->id, 'estado' => 'ocupada']);
    }

    public function test_completa_pago_total_y_libera_mesa()
    {
        $mesa = Mesa::factory()->create(['estado' => 'ocupada']);
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->cajero->id,
            'estado'  => 'en_proceso',
            'total'   => 100.00,
        ]);

        // Cobro completo
        $response = $this->postJson("/api/pedidos/{$pedido->id}/cobrar", [
            'monto_recibido' => 100.00,
            'propina'        => 15.00,
            'metodo_pago'    => 'tarjeta',
        ]);

        $response->assertStatus(200)
                ->assertJsonPath('saldo_pendiente', 0)
                ->assertJsonPath('mensaje', 'Pedido cobrado con éxito.'); // 👈 Actualizado

        // El pedido cambia a pagado y libera la mesa
        $this->assertDatabaseHas('pedidos', ['id' => $pedido->id, 'estado' => 'pagado']);
        $this->assertDatabaseHas('mesas', ['id' => $mesa->id, 'estado' => 'disponible']);
    }

    public function test_rechaza_cobro_en_pedido_ya_pagado()
    {
        $mesa = Mesa::factory()->create(['estado' => 'disponible']);
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->cajero->id,
            'estado'  => 'pagado',
            'total'   => 100.00,
        ]);

        $response = $this->postJson("/api/pedidos/{$pedido->id}/cobrar", [
            'monto_recibido' => 100.00,
            'metodo_pago'    => 'efectivo',
        ]);

        $response->assertStatus(422)
                 ->assertJson([
                     'error' => 'Este pedido ya ha sido cobrado o se encuentra cancelado.'
                 ]);
    }
}