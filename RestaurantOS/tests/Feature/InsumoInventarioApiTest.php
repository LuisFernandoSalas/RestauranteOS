<?php

namespace Tests\Feature;

use App\Models\Insumo;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class InsumoInventarioApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_gestion_de_insumos_y_movimientos_de_inventario(): void
    {
        $admin = User::factory()->create(['role' => 'admin']);
        Sanctum::actingAs($admin, ['*']);

        $insumo = Insumo::factory()->create();

        $resMov = $this->postJson('/api/movimientos-inventario', [
            'insumo_id' => $insumo->id,
            'tipo'      => 'ENTRADA',
            'cantidad'  => 5.00,
            'motivo'    => 'Surtido de proveedor',
        ]);

        // La creación de movimiento retorna 201 Created
        $resMov->assertStatus(201);

        $resListMov = $this->getJson('/api/movimientos-inventario');
        $resListMov->assertStatus(200);
    }
}