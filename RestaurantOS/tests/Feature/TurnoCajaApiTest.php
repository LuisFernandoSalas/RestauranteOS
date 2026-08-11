<?php

namespace Tests\Feature;

use App\Models\TurnoCaja;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class TurnoCajaApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_cajero_puede_abrir_y_consultar_turno_actual(): void
    {
        $cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($cajero, ['*']);

        $responseAbrir = $this->postJson('/api/caja/abrir-turno', [
            'monto_apertura' => 1000.00,
        ]);
        
        $responseAbrir->assertStatus(201);

        $responseActual = $this->getJson('/api/caja/turno-actual');
        $responseActual->assertStatus(200);
    }

    public function test_cajero_puede_cerrar_turno_y_ver_historial(): void
    {
        $cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($cajero, ['*']);

        // Se usa 'ABIERTO' para evitar la violación del CHECK constraint
        $turno = TurnoCaja::create([
            'user_id'        => $cajero->id,
            'estado'         => 'ABIERTO',
            'monto_apertura' => 1000.00,
            'opened_at'      => now(),
            'fecha_apertura' => now(),
        ]);

        $responseCerrar = $this->postJson('/api/caja/cerrar-turno', [
            'turno_id'              => $turno->id,
            'monto_cierre_efectivo' => 850.00,
            'monto_cierre_tarjeta'  => 0.00,
            'observaciones'         => 'Cierre sin diferencias',
        ]);
        $responseCerrar->assertStatus(200);

        $responseHistorial = $this->getJson('/api/caja/historial-turnos');
        $responseHistorial->assertStatus(200);
    }
}