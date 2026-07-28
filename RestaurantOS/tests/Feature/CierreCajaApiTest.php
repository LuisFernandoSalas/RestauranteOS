<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Mesa;
use App\Models\Pedido;
use App\Models\Pago;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class CierreCajaApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test: Un usuario de caja puede auditar el historial de transacciones para el cierre.
     */
    public function test_cajero_puede_consultar_historial_de_pagos_estructurado(): void
    {
        // 1. Crear y autenticar al cajero
        $cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($cajero, ['*']);

        // 2. Preparar el escenario con transacciones registradas
        $mesa = Mesa::factory()->create();
        $pedido = Pedido::factory()->create(['mesa_id' => $mesa->id, 'estado' => 'pagado']);

        // Registramos un pago en efectivo
        Pago::create([
            'pedido_id' => $pedido->id,
            'cobrado_por' => $cajero->id,
            'metodo_pago' => 'efectivo',
            'monto_recibido' => 250.00,
            'propina' => 30.00,
            'requiere_factura' => false,
        ]);

        // Registramos un pago con tarjeta
        Pago::create([
            'pedido_id' => $pedido->id,
            'cobrado_por' => $cajero->id,
            'metodo_pago' => 'tarjeta',
            'monto_recibido' => 150.00,
            'propina' => 20.00,
            'requiere_factura' => true,
        ]);

        // 3. Consultar el endpoint del historial de pagos
        $response = $this->getJson('/api/pagos');

        // 4. Aseverar estructura correcta y paginación
        $response->assertStatus(200);
        $response->assertJsonStructure([
            'status',
            'data' => [
                'current_page',
                'data' => [
                    '*' => [
                        'id',
                        'pedido_id',
                        'cobrado_por',
                        'metodo_pago',
                        'monto_recibido',
                        'propina',
                        'requiere_factura',
                        'created_at'
                    ]
                ],
                'total'
            ]
        ]);

        // 5. Verificar que los montos clave vivan intactos en la respuesta (como string decimal)
        $response->assertJsonFragment(['metodo_pago' => 'efectivo', 'monto_recibido' => '250.00']);
        $response->assertJsonFragment(['metodo_pago' => 'tarjeta', 'monto_recibido' => '150.00']);
    }
}