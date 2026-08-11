<?php

namespace Tests\Feature;

use App\Models\Insumo;
use App\Models\Producto;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class RecetaMenuProductoApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_gestion_de_recetas(): void
    {
        $admin = User::factory()->create(['role' => 'admin']);
        Sanctum::actingAs($admin, ['*']);

        $producto = Producto::factory()->create();
        $insumo = Insumo::factory()->create();

        // Se añade la clave 'cantidad_por_porcion' exigida por la validación del controlador
        $resReceta = $this->postJson('/api/recetas', [
            'producto_id' => $producto->id,
            'insumos'     => [
                [
                    'insumo_id'            => $insumo->id,
                    'cantidad_por_porcion' => 1.5,
                ],
            ],
        ]);

        $resReceta->assertStatus(201);

        $this->getJson("/api/recetas/producto/{$producto->id}")->assertStatus(200);
    }
}