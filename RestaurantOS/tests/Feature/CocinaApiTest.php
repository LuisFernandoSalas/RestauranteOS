<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\DetallePedido;
use App\Models\Producto;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum; // <-- Asegúrate de importar esta clase
use Tests\TestCase;

class CocinaApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_cocinero_puede_ver_pedidos_activos_en_cocina(): void
    {
        // 1. Crear el usuario usando la factory con el rol correcto
        $user = User::factory()->create(['role' => 'cocinero']);

        // 2. Autenticar de forma segura para Sanctum usando su propio método estático
        Sanctum::actingAs($user, ['*']); 

        // 3. El resto de tu código de prueba...
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

        $response->assertStatus(200);
    }

        public function test_cocinero_puede_actualizar_estado_de_un_platillo(): void
    {
        // 1. Autenticar al cocinero
        $user = User::factory()->create(['role' => 'cocinero']);
        \Laravel\Sanctum\Sanctum::actingAs($user, ['*']);

        // 2. Crear el escenario: Mesa -> Producto -> Pedido -> Detalle
        $mesa = Mesa::factory()->create();
        $producto = Producto::factory()->create(['nombre' => 'Hamburguesa con Queso']);
        $pedido = Pedido::factory()->create(['mesa_id' => $mesa->id, 'user_id' => $user->id]);
        
        $detalle = DetallePedido::create([
            'pedido_id' => $pedido->id,
            'producto_id' => $producto->id,
            'cantidad' => 1,
            'precio_unitario' => $producto->precio,
            'subtotal' => $producto->precio,
            'estado' => 'pendiente', // Estado inicial
            'nota' => 'Término medio'
        ]);

        // 3. Consumir el endpoint PATCH enviando el nuevo estado en el Body
        $response = $this->patchJson("/api/cocina/detalles/{$detalle->id}/estado", [
            'estado' => 'en_preparacion'
        ]);

        // 4. Aseveraciones
        $response->assertStatus(200); // O 200/204 según lo que devuelva tu controlador

        // Verificar en la base de datos en memoria que el cambio se guardó con éxito
        $this->assertDatabaseHas('detalle_pedido', [
            'id' => $detalle->id,
            'estado' => 'en_preparacion'
        ]);
    }
}