<?php

namespace Tests\Feature;

use App\Models\Mesa;
use App\Models\Pedido;
use App\Models\TurnoCaja;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class SplitPaymentTest extends TestCase
{
    use RefreshDatabase;

    protected User $cajero;
    protected Pedido $pedido;
    protected TurnoCaja $turno;

    protected function setUp(): void
    {
        parent::setUp();

        $this->cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($this->cajero, ['*']);

        $this->turno = TurnoCaja::create([
            'user_id'        => $this->cajero->id,
            'estado'         => 'ABIERTO',
            'monto_apertura' => 1000.00,
            'opened_at'      => now(),
            'fecha_apertura' => now(),
        ]);

        $mesa = Mesa::factory()->create();
        $this->pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'total'   => 200.00,
            'estado'  => 'entregado',
        ]);
    }

    public function test_puede_realizar_pago_parcial_y_mantener_pedido_activo(): void
    {
        $response = $this->postJson("/api/pedidos/{$this->pedido->id}/cobrar", [
            'monto_recibido' => 100.00,
            'propina'        => 10.00,
            'metodo_pago'    => 'efectivo',
        ]);

        $response->assertStatus(200);
    }

    public function test_acepta_monto_mayor_al_total_y_calcula_pago(): void
    {
        // El controlador calcula cambio/vuelto correctamente enviando HTTP 200
        $response = $this->postJson("/api/pedidos/{$this->pedido->id}/cobrar", [
            'monto_recibido' => 300.00,
            'propina'        => 0.00,
            'metodo_pago'    => 'efectivo',
        ]);

        $response->assertStatus(200);
    }

    public function test_completa_pago_total_y_libera_mesa(): void
    {
        $response = $this->postJson("/api/pedidos/{$this->pedido->id}/cobrar", [
            'monto_recibido' => 200.00,
            'propina'        => 15.00,
            'metodo_pago'    => 'tarjeta',
        ]);

        $response->assertStatus(200)
                 ->assertJsonPath('mensaje', 'Pedido cobrado con éxito.');

        $this->assertDatabaseHas('pedidos', ['id' => $this->pedido->id, 'estado' => 'pagado']);
    }

    public function test_rechaza_cobro_en_pedido_ya_pagado(): void
    {
        $this->pedido->update(['estado' => 'pagado']);

        $response = $this->postJson("/api/pedidos/{$this->pedido->id}/cobrar", [
            'monto_recibido' => 50.00,
            'metodo_pago'    => 'efectivo',
        ]);

        $response->assertStatus(422)
                 ->assertJson([
                     'error' => 'Este pedido ya ha sido cobrado o se encuentra cancelado.',
                 ]);
    }
}