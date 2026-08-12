<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\DetallePedido;
use App\Models\Producto;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class CocinaApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Helper para autenticar rápido como cocinero en las pruebas
     */
    private function autenticarCocinero(): User
    {
        $user = User::factory()->create(['role' => 'cocinero']);
        Sanctum::actingAs($user, ['*']);
        return $user;
    }

    public function test_cocinero_puede_ver_pedidos_activos_en_cocina(): void
    {
        $user = $this->autenticarCocinero();

        $mesa = Mesa::factory()->create(['numero' => 5]);
        $producto = Producto::factory()->create(['nombre' => 'Tacos al Pastor', 'precio' => 100]);
        
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado' => 'pendiente'
        ]);

        DetallePedido::create([
            'pedido_id' => $pedido->id,
            'producto_id' => $producto->id,
            'cantidad' => 3,
            'precio_unitario' => $producto->precio,
            'subtotal' => $producto->precio * 3,
            'estado' => 'pendiente',
            'nota' => 'Sin cebolla'
        ]);

        $response = $this->getJson('/api/cocina/pedidos');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'status',
                'resumen' => ['total_comandas', 'en_preparacion', 'pausados', 'pendientes'],
                'comandas'
            ]);
    }

    public function test_cocinero_puede_actualizar_estado_de_un_platillo(): void
    {
        $user = $this->autenticarCocinero();

        $mesa = Mesa::factory()->create();
        $producto = Producto::factory()->create(['nombre' => 'Hamburguesa con Queso']);
        $pedido = Pedido::factory()->create(['mesa_id' => $mesa->id, 'user_id' => $user->id]);
        
        $detalle = DetallePedido::create([
            'pedido_id' => $pedido->id,
            'producto_id' => $producto->id,
            'cantidad' => 1,
            'precio_unitario' => $producto->precio,
            'subtotal' => $producto->precio,
            'estado' => 'pendiente',
            'nota' => 'Término medio'
        ]);

        $response = $this->patchJson("/api/cocina/detalles/{$detalle->id}/estado", [
            'estado' => 'en_preparacion'
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas((new DetallePedido)->getTable(), [
            'id' => $detalle->id,
            'estado' => 'en_preparacion'
        ]);
    }

    public function test_cocinero_puede_pausar_un_platillo_individual(): void
    {
        $user = $this->autenticarCocinero();

        $pedido = Pedido::factory()->create(['user_id' => $user->id]);
        $producto = Producto::factory()->create();
        
        $detalle = DetallePedido::create([
            'pedido_id' => $pedido->id,
            'producto_id' => $producto->id,
            'cantidad' => 1,
            'precio_unitario' => $producto->precio,
            'subtotal' => $producto->precio,
            'estado' => 'en_preparacion',
        ]);

        $response = $this->patchJson("/api/cocina/detalles/{$detalle->id}/estado", [
            'estado' => 'pausado'
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas((new DetallePedido)->getTable(), [
            'id' => $detalle->id,
            'estado' => 'pausado'
        ]);
    }

    public function test_cocinero_puede_cambiar_estado_del_pedido_completo(): void
    {
        $user = $this->autenticarCocinero();

        $pedido = Pedido::factory()->create(['user_id' => $user->id, 'estado' => 'pendiente']);

        // Cambiar estado a 'pausado' en el pedido general
        $response = $this->patchJson("/api/cocina/pedidos/{$pedido->id}/estado", [
            'estado' => 'pausado'
        ]);

        $response->assertStatus(200);

        $this->assertDatabaseHas('pedidos', [
            'id' => $pedido->id,
            'estado' => 'pausado'
        ]);
    }

    public function test_cocinero_puede_cancelar_pedido_completo_con_motivo(): void
    {
        $user = $this->autenticarCocinero();

        $pedido = Pedido::factory()->create(['user_id' => $user->id, 'estado' => 'pendiente']);

        $response = $this->postJson("/api/cocina/pedidos/{$pedido->id}/cancelar", [
            'motivo_cancelacion' => 'Sin insumos en cocina'
        ]);

        $response->assertStatus(200);

        $this->assertSoftDeleted('pedidos', [
            'id' => $pedido->id,
            'estado' => 'cancelado'
        ]);
    }
}