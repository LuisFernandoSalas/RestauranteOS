<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\Factura;
use App\Mail\FacturaCreadaMail;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Storage;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class FacturacionApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_permite_generar_factura_y_envia_correo_con_adjuntos(): void
    {
        // 1. Iniciar fakes ANTES de procesar la solicitud
        Mail::fake();
        Storage::fake('public');

        $user = User::factory()->create();
        Sanctum::actingAs($user, ['*']);

        $mesa = Mesa::factory()->create();
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado'  => 'pagado',
            'total'   => 350.00
        ]);

        $payload = [
            'pedido_id'    => $pedido->id,
            'rfc'          => 'XAXX010101000',
            'razon_social' => 'PUBLICO EN GENERAL',
            'email'        => 'cliente@example.com',
            'uso_cfdi'     => 'CP01'
        ];

        // 2. Ejecutar petición POST
        $response = $this->postJson('/api/facturacion/generar', $payload);

        $response->assertStatus(201)
                 ->assertJsonFragment([
                     'status'  => 'success',
                     'mensaje' => 'Factura generada y enviada por correo exitosamente.'
                 ]);

        // 3. Validar registro en base de datos
        $this->assertDatabaseHas('facturas', [
            'pedido_id' => $pedido->id,
            'rfc'       => 'XAXX010101000'
        ]);

        $factura = Factura::where('pedido_id', $pedido->id)->first();

        // 4. Validar existencia de archivos en el disco faked 'public'
        
        // 4. Validar existencia de archivos en el disco faked 'public'
        $this->assertFileExists(Storage::disk('public')->path("facturas/{$factura->uuid_fiscal}.pdf"));
        $this->assertFileExists(Storage::disk('public')->path("facturas/{$factura->uuid_fiscal}.xml"));

        // 5. Validar envío de correo
        Mail::assertSent(FacturaCreadaMail::class, function ($mail) use ($payload) {
            return $mail->hasTo($payload['email']);
        });
    }

    public function test_rechaza_facturar_pedido_no_pagado(): void
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user, ['*']);

        $pedido = Pedido::factory()->create(['estado' => 'en_proceso']);

        $payload = [
            'pedido_id'    => $pedido->id,
            'rfc'          => 'XAXX010101000',
            'razon_social' => 'PUBLICO EN GENERAL',
            'email'        => 'cliente@example.com'
        ];

        $response = $this->postJson('/api/facturacion/generar', $payload);

        $response->assertStatus(422)
                 ->assertJsonFragment(['status' => 'error']);
    }

    public function test_rechaza_duplicar_factura(): void
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user, ['*']);

        $pedido = Pedido::factory()->create(['estado' => 'pagado', 'total' => 100]);
        Factura::factory()->create(['pedido_id' => $pedido->id, 'monto_total' => 100]);

        $payload = [
            'pedido_id'    => $pedido->id,
            'rfc'          => 'XAXX010101000',
            'razon_social' => 'PUBLICO EN GENERAL',
            'email'        => 'cliente@example.com'
        ];

        $response = $this->postJson('/api/facturacion/generar', $payload);

        $response->assertStatus(400)
                 ->assertJsonFragment(['status' => 'error']);
    }
}