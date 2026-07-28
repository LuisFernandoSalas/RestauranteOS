<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Pedido;
use App\Models\DetallePedido;
use App\Models\Mesa;
use App\Models\Producto;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;
use Illuminate\Support\Str;

class PedidoApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test: Un mesero puede registrar un nuevo pedido con sus productos.
     */
        public function test_mesero_puede_crear_un_pedido_con_detalles(): void
    {
        // 1. Autenticar al usuario como mesero
        $user = User::factory()->create(['role' => 'mesero']);
        \Laravel\Sanctum\Sanctum::actingAs($user, ['*']);

        // 2. Preparar el entorno (Mesa y Producto que se van a ordenar)
        $mesa = Mesa::factory()->create(['estado' => 'disponible']);
        $producto = Producto::factory()->create(['precio' => 150.00]);

        // 3. Estructura de datos exacta para GuardarPedidoRequest
        $payload = [
            'client_uuid' => (string) \Illuminate\Support\Str::uuid(),
            'mesa_id' => $mesa->id,
            'productos' => [
                [
                    'id' => $producto->id,
                    'cantidad' => 2,
                    'nota' => 'Bien cocido'
                ]
            ]
        ];

        // 4. Realizar la petición POST
        $response = $this->postJson('/api/pedidos', $payload);

        // 5. Aseveraciones adaptadas a tu estructura real de respuesta
        $response->assertStatus(201);
        $response->assertJsonFragment([
            'status' => 'success',
            'mensaje' => '¡Orden enviada e inventario actualizado!'
        ]);

        // Verificar que el pedido se guardó en la base de datos
        $this->assertDatabaseHas('pedidos', [
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado' => 'pendiente'
        ]);

        // Verificar que el detalle se registró correctamente
        $this->assertDatabaseHas('detalle_pedido', [
            'producto_id' => $producto->id,
            'cantidad' => 2,
            'precio_unitario' => 150.00,
            'nota' => 'Bien cocido'
        ]);
    }

    /**
     * Test: Un usuario autenticado puede ver el listado de pedidos.
     */
    public function test_usuario_puede_listar_pedidos(): void
    {
        $user = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($user, ['*']);

        $mesa = Mesa::factory()->create();
        
        // Crear 3 pedidos de prueba en la base de datos en memoria
        Pedido::factory()->count(3)->create([
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado' => 'pendiente'
        ]);

        $response = $this->getJson('/api/pedidos');

        $response->assertStatus(200);
        
        // Verifica que la respuesta JSON contenga una estructura con los 3 elementos
        $response->assertJsonCount(3); 
        // Nota: Si usas paginación en tu controlador (API Resources), 
        // cambiar por: $response->assertJsonStructure(['data', 'links', 'meta']);
    }

    /**
     * Test: Un usuario puede ver el detalle individual de un pedido específico.
     */
    public function test_usuario_puede_ver_un_pedido_especifico(): void
    {
        // 1. Autenticar mesero
        $user = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($user, ['*']);

        // 2. Crear el escenario
        $mesa = Mesa::factory()->create(['numero' => 12]);
        $producto = Producto::factory()->create(['nombre' => 'Refresco de Cola', 'precio' => 45.00]);
        
        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $user->id,
            'estado' => 'en_preparacion'
        ]);

        $detalle = DetallePedido::create([
            'pedido_id' => $pedido->id,
            'producto_id' => $producto->id,
            'cantidad' => 2,
            'precio_unitario' => $producto->precio,
            'subtotal' => $producto->precio * 2,
            'estado' => 'pendiente'
        ]);

        // 3. Consumir el endpoint GET /api/pedidos/{id}
        $response = $this->getJson("/api/pedidos/{$pedido->id}");

        // 4. Aseveraciones
        $response->assertStatus(200);

        // Validamos fragmentos clave que tu API debería retornar al consultar un pedido
        $response->assertJsonFragment([
            'id' => $pedido->id,
            'estado' => 'en_preparacion',
            'mesa_id' => $mesa->id
        ]);
    }
}