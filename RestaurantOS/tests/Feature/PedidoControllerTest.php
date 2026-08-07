<?php

namespace Tests\Feature;

use App\Models\Combo;
use App\Models\Insumo;
use App\Models\Mesa;
use App\Models\Pedido;
use App\Models\Producto;
use App\Models\Receta;
use App\Models\User;
use App\Models\DetallePedido;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Str;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class PedidoControllerTest extends TestCase
{
    use RefreshDatabase;

    protected User $user;
    protected Mesa $mesa;

    protected function setUp(): void
    {
        parent::setUp();

        // Evitar que el broadcasting intente conectarse al servidor de websockets durante los tests
        Event::fake();

        // Crear usuario autenticado
        $this->user = User::factory()->create();
        Sanctum::actingAs($this->user, ['*']);

        // Crear mesa de prueba en estado 'libre'
        $this->mesa = Mesa::create([
            'numero' => 1,
            'estado' => 'libre',
        ]);
    }

    /**
     * Auxiliar para crear un insumo, un producto y su receta correspondiente.
     */
    private function crearProductoConInsumo(string $nombreProd, float $precio, float $stockInsumo, float $recetaPorcion): array
    {
        $insumo = Insumo::create([
            'nombre'         => 'Insumo ' . $nombreProd,
            'stock_actual'   => $stockInsumo,
            'stock_minimo'   => 5,
            'unidad_medida'  => 'gramos',
        ]);

        $producto = Producto::factory()->create([
            'nombre' => $nombreProd,
            'precio' => $precio,
        ]);

        Receta::create([
            'producto_id'          => $producto->id,
            'insumo_id'            => $insumo->id,
            'cantidad_por_porcion' => $recetaPorcion,
        ]);

        return [$producto, $insumo];
    }

    public function test_puede_crear_un_pedido_con_producto_y_descuenta_inventario()
    {
        [$producto, $insumo] = $this->crearProductoConInsumo('Hamburguesa Simple', 80.00, 1000, 150);

        $payload = [
            'client_uuid' => Str::uuid()->toString(),
            'mesa_id'     => $this->mesa->id,
            'items'       => [
                [
                    'producto_id' => $producto->id,
                    'cantidad'    => 2, // Consumirá 2 * 150 = 300g de insumo
                    'nota'        => 'Sin cebolla',
                ]
            ]
        ];

        $response = $this->postJson('/api/pedidos', $payload);

        $response->assertStatus(201)
                 ->assertJsonFragment(['status' => 'success']);

        // 1. Verificamos que se creó la comanda
        $this->assertDatabaseHas('pedidos', [
            'client_uuid' => $payload['client_uuid'],
            'mesa_id'     => $this->mesa->id,
            'estado'      => 'pendiente',
        ]);

        // 2. Verificamos la tabla detalle_pedido
        $this->assertDatabaseHas('detalle_pedido', [
            'producto_id'     => $producto->id,
            'cantidad'        => 2,
            'precio_unitario' => 80.00,
            'subtotal'        => 160.00,
            'nota'            => 'Sin cebolla',
        ]);

        // 3. Verificamos el descuento de stock: 1000 - 300 = 700
        $this->assertDatabaseHas('insumos', [
            'id'           => $insumo->id,
            'stock_actual' => 700,
        ]);

        // 4. La mesa debe pasar a 'ocupada'
        $this->assertDatabaseHas('mesas', [
            'id'     => $this->mesa->id,
            'estado' => 'ocupada',
        ]);
    }

    public function test_puede_crear_un_pedido_con_combo_y_descuenta_stock_de_todos_sus_productos()
    {
        [$prod1, $insumo1] = $this->crearProductoConInsumo('Burger Combo', 50.00, 500, 100);
        [$prod2, $insumo2] = $this->crearProductoConInsumo('Papas Combo', 30.00, 400, 50);

        // Crear combo con los dos productos
        $combo = Combo::factory()->create([
            'nombre'          => 'Combo Pareja',
            'precio_especial' => 70.00,
            'estado'          => 'activo',
        ]);

        // Relacionar productos al combo (Combo incluye: 2 burgers y 1 papas)
        $combo->productos()->attach([
            $prod1->id => ['cantidad' => 2],
            $prod2->id => ['cantidad' => 1],
        ]);

        $payload = [
            'client_uuid' => Str::uuid()->toString(),
            'mesa_id'     => $this->mesa->id,
            'items'       => [
                [
                    'combo_id' => $combo->id,
                    'cantidad' => 1, // 1 Combo: Descontará 2 Burgers (200g insumo1) y 1 Papas (50g insumo2)
                    'nota'     => 'Para llevar',
                ]
            ]
        ];

        $response = $this->postJson('/api/pedidos', $payload);

        $response->assertStatus(201);

        // Verificamos el detalle del pedido
        $this->assertDatabaseHas('detalle_pedido', [
            'combo_id'        => $combo->id,
            'precio_unitario' => 70.00,
            'subtotal'        => 70.00,
        ]);

        // Verificamos los insumos descontados
        $this->assertDatabaseHas('insumos', ['id' => $insumo1->id, 'stock_actual' => 300]); // 500 - (2 * 100)
        $this->assertDatabaseHas('insumos', ['id' => $insumo2->id, 'stock_actual' => 350]); // 400 - (1 * 50)
    }

    public function test_respeta_el_candado_de_idempotencia_client_uuid()
    {
        [$producto] = $this->crearProductoConInsumo('Refresco', 25.00, 100, 1);
        $uuid = Str::uuid()->toString();

        $payload = [
            'client_uuid' => $uuid,
            'mesa_id'     => $this->mesa->id,
            'items'       => [
                ['producto_id' => $producto->id, 'cantidad' => 1]
            ]
        ];

        // Primera petición (Creación original)
        $res1 = $this->postJson('/api/pedidos', $payload);
        $res1->assertStatus(201);

        // Segunda petición idéntica (Reintento de red por cliente Android)
        $res2 = $this->postJson('/api/pedidos', $payload);
        $res2->assertStatus(200)
             ->assertJsonFragment(['mensaje' => 'La comanda ya había sido registrada previamente.']);
    }

    public function test_cancela_pedido_si_el_stock_es_insuficiente()
    {
        // Solo 50g en almacén pero se requieren 100g
        [$producto, $insumo] = $this->crearProductoConInsumo('Corte de Carne', 250.00, 50, 100);

        $payload = [
            'client_uuid' => Str::uuid()->toString(),
            'mesa_id'     => $this->mesa->id,
            'items'       => [
                ['producto_id' => $producto->id, 'cantidad' => 1]
            ]
        ];

        $response = $this->postJson('/api/pedidos', $payload);

        // Debe retornar error 422 por rollback de transacción
        $response->assertStatus(422)
                 ->assertJsonFragment(['status' => 'error']);

        // No se debe haber creado el pedido
        $this->assertDatabaseMissing('pedidos', [
            'client_uuid' => $payload['client_uuid']
        ]);

        // El stock no debió alterarse
        $this->assertDatabaseHas('insumos', [
            'id'           => $insumo->id,
            'stock_actual' => 50,
        ]);
    }

    public function test_rechaza_crear_pedido_con_combo_pausado()
    {
        $combo = Combo::factory()->create([
            'estado' => 'pausado',
        ]);

        $payload = [
            'client_uuid' => Str::uuid()->toString(),
            'mesa_id'     => $this->mesa->id,
            'items'       => [
                ['combo_id' => $combo->id, 'cantidad' => 1]
            ]
        ];

        $response = $this->postJson('/api/pedidos', $payload);

        $response->assertStatus(422);
    }

    public function test_puede_recuperar_el_pedido_activo_de_una_mesa()
    {
        // 1. Crear producto e insumo
        [$producto] = $this->crearProductoConInsumo('Taco al Pastor', 20.00, 500, 50);

        // 2. Crear pedido activo en estado 'en_preparacion'
        $pedido = Pedido::create([
            'client_uuid' => Str::uuid()->toString(),
            'mesa_id'     => $this->mesa->id,
            'user_id'     => $this->user->id,
            'estado'      => 'en_preparacion',
        ]);

        DetallePedido::create([
            'pedido_id'       => $pedido->id,
            'producto_id'     => $producto->id,
            'cantidad'        => 2,
            'precio_unitario' => 20.00,
            'subtotal'        => 40.00,
        ]);

        // 3. Consultar endpoint
        $response = $this->getJson("/api/pedidos/mesa/{$this->mesa->id}/activo");

        // 4. Aseveraciones
        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'data'   => [
                         'id'      => $pedido->id,
                         'mesa_id' => $this->mesa->id,
                         'estado'  => 'en_preparacion',
                         'total'   => 40.00,
                     ]
                 ]);
    }

    public function test_retorna_empty_si_la_mesa_no_tiene_pedido_activo_o_esta_pagado()
    {
        // Crear un pedido finalizado/pagado para la mesa
        Pedido::create([
            'client_uuid' => Str::uuid()->toString(),
            'mesa_id'     => $this->mesa->id,
            'user_id'     => $this->user->id,
            'estado'      => 'pagado',
        ]);

        // Consultar el endpoint de mesa activa
        $response = $this->getJson("/api/pedidos/mesa/{$this->mesa->id}/activo");

        // Debe indicar que la mesa está libre
        $response->assertStatus(200)
                 ->assertJson([
                     'status'  => 'empty',
                     'message' => 'Mesa libre / Sin pedido activo',
                     'data'    => null
                 ]);
    }
}