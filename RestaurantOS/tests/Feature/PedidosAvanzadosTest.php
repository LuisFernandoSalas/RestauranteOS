<?php

namespace Tests\Feature;

use App\Models\Mesa;
use App\Models\Pedido;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class PedidosAvanzadosTest extends TestCase
{
    use RefreshDatabase;

    protected function setUp(): void
    {
        parent::setUp();
        $user = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($user, ['*']);
    }

    public function test_actualizar_pedido(): void
    {
        $pedido = Pedido::factory()->create(['estado' => 'pendiente']);

        $response = $this->putJson("/api/pedidos/{$pedido->id}", [
            'estado' => 'en_preparacion',
        ]);

        $response->assertStatus(200);
    }

    public function test_obtener_pedido_activo_por_mesa(): void
    {
        $mesa = Mesa::factory()->create();
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'estado'  => 'pendiente',
        ]);

        $response = $this->getJson("/api/pedidos/mesa/{$mesa->id}/activo");

        $response->assertStatus(200);
    }

    public function test_consultar_lista_de_mesas_y_menu(): void
    {
        $resMesas = $this->getJson('/api/mesas');
        $resMesas->assertStatus(200);

        $resMenu = $this->getJson('/api/menu');
        $resMenu->assertStatus(200);
    }

    public function test_cancelar_pedido_desde_cocina(): void
    {
        $cocinero = User::factory()->create(['role' => 'cocinero']);
        Sanctum::actingAs($cocinero, ['*']);

        $pedido = Pedido::factory()->create(['estado' => 'pendiente']);

        $response = $this->postJson("/api/cocina/pedidos/{$pedido->id}/cancelar", [
            'motivo_cancelacion' => 'Sin insumos suficientes',
        ]);

        $response->assertStatus(200);
    }
}