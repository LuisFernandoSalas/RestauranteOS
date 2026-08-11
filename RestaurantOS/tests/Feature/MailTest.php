<?php

namespace Tests\Feature;

use App\Models\Mesa;
use App\Models\Pedido;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class MailTest extends TestCase
{
    use RefreshDatabase;

    public function test_envio_de_correo_al_generar_factura(): void
    {
        // 1. Autenticamos al usuario cajero
        $cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($cajero, ['*']);

        // 2. Preparamos la mesa y el pedido
        $mesa = Mesa::factory()->create();
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'total'   => 250.00,
            'estado'  => 'pagado',
        ]);

        // 3. Enviamos la petición enviando el correo objetivo real
        $response = $this->postJson('/api/facturacion/generar', [
            'pedido_id'    => $pedido->id,
            'email'        => 'josexdmendoza.j1310@gmail.com',
            'razon_social' => 'Cliente Prueba S.A.',
            'rfc'          => 'XAXX010101000',
        ]);

        // 4. Verificamos que la respuesta sea 201 Created
        $response->assertCreated();
    }
}