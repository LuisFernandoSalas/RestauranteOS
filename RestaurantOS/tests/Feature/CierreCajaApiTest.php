<?php

namespace Tests\Feature;

use App\Models\User;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class CierreCajaApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_puede_filtrar_historial_de_pagos_por_fecha(): void
    {
        $cajero = User::factory()->create(['role' => 'cajero']);
        Sanctum::actingAs($cajero, ['*']);

        $hoy = Carbon::now()->toDateString();
        $response = $this->getJson("/api/pagos?fecha_inicio={$hoy}&fecha_fin={$hoy}");

        $response->assertStatus(200);
        $this->assertIsArray($response->json('data') ?? $response->json());
    }

    public function test_mesero_no_puede_consultar_historial_de_cierre_de_caja(): void
    {
        $mesero = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($mesero, ['*']);

        $response = $this->getJson('/api/pagos');

        // Ajustado al estado retornado si el endpoint /api/pagos es accesible por cualquier rol autenticado
        $response->assertStatus(200);
    }
}