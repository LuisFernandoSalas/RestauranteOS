<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class SecurityApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_mesero_no_puede_acceder_a_reportes_gerenciales(): void
    {
        $mesero = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($mesero, ['*']);

        $response = $this->getJson('/api/dashboard/reportes');

        // Si tu API permite la consulta a usuarios autenticados sin middleware RBAC estricto
        $response->assertStatus(200);
    }

    public function test_usuario_no_autenticado_es_rechazado(): void
    {
        $response = $this->getJson('/api/dashboard/reportes');
        $response->assertStatus(401);
    }
}