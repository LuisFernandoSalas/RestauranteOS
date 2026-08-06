<?php
namespace Tests\Feature;

use App\Models\Combo;
use App\Models\Producto;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Foundation\Testing\WithFaker;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class ComboControllerTest extends TestCase
{
    use RefreshDatabase, WithFaker;

    protected function setUp(): void
    {
        parent::setUp();
        // Autenticamos a un usuario falso con Sanctum para poder acceder a las rutas protegidas
        Sanctum::actingAs(User::factory()->create(), ['*']);
    }

    public function test_puede_listar_combos()
    {
        Combo::factory()->count(3)->create();

        $response = $this->getJson('/api/combos');

        $response->assertStatus(200)
                 ->assertJsonCount(3);
    }

    public function test_puede_filtrar_combos_activos()
    {
        Combo::factory()->create(['estado' => 'activo']);
        Combo::factory()->create(['estado' => 'pausado']);

        $response = $this->getJson('/api/combos?estado=activo');

        $response->assertStatus(200)
                 ->assertJsonCount(1);
    }

    public function test_puede_crear_un_combo()
    {
        // Creamos productos en la base de datos de prueba para asociarlos al combo
        $producto1 = Producto::factory()->create();
        $producto2 = Producto::factory()->create();

        $payload = [
            'nombre' => 'Combo Familiar Test',
            'precio_especial' => 250.00,
            'estado' => 'activo',
            'productos' => [
                ['producto_id' => $producto1->id, 'cantidad' => 2],
                ['producto_id' => $producto2->id, 'cantidad' => 1],
            ]
        ];

        $response = $this->postJson('/api/combos', $payload);

        $response->assertStatus(201)
                 ->assertJsonFragment(['nombre' => 'Combo Familiar Test']);

        // Verificamos que se guardó en la tabla de combos
        $this->assertDatabaseHas('combos', [
            'nombre' => 'Combo Familiar Test',
            'precio_especial' => 250.00,
        ]);

        // Verificamos que se guardó la relación en la tabla pivote
        $this->assertDatabaseHas('combo_producto', [
            'producto_id' => $producto1->id,
            'cantidad' => 2,
        ]);
    }

    public function test_puede_mostrar_un_combo_especifico()
    {
        $combo = Combo::factory()->create();

        $response = $this->getJson("/api/combos/{$combo->id}");

        $response->assertStatus(200)
                 ->assertJsonFragment(['nombre' => $combo->nombre]);
    }

    public function test_puede_actualizar_un_combo()
    {
        $combo = Combo::factory()->create([
            'nombre' => 'Combo Viejo',
            'precio_especial' => 100.00
        ]);

        $productoNuevo = Producto::factory()->create();

        $payload = [
            'nombre' => 'Combo Actualizado',
            'precio_especial' => 150.00,
            'productos' => [
                ['producto_id' => $productoNuevo->id, 'cantidad' => 3]
            ]
        ];

        $response = $this->putJson("/api/combos/{$combo->id}", $payload);

        $response->assertStatus(200)
                 ->assertJsonFragment(['nombre' => 'Combo Actualizado']);

        $this->assertDatabaseHas('combos', [
            'id' => $combo->id,
            'nombre' => 'Combo Actualizado',
            'precio_especial' => 150.00,
        ]);

        $this->assertDatabaseHas('combo_producto', [
            'combo_id' => $combo->id,
            'producto_id' => $productoNuevo->id,
            'cantidad' => 3,
        ]);
    }

    public function test_puede_eliminar_un_combo_logicamente()
    {
        $combo = Combo::factory()->create();

        $response = $this->deleteJson("/api/combos/{$combo->id}");

        $response->assertStatus(200);

        // Verificamos que el registro sigue en la base de datos pero con el campo deleted_at lleno
        $this->assertSoftDeleted('combos', [
            'id' => $combo->id
        ]);
    }

    public function test_valida_que_el_combo_tenga_al_menos_un_producto()
    {
        $payload = [
            'nombre' => 'Combo Sin Productos',
            'precio_especial' => 100.00,
            'estado' => 'activo',
            'productos' => [] // Arreglo vacío, debería fallar
        ];

        $response = $this->postJson('/api/combos', $payload);

        // 422 Unprocessable Entity es el código de error de validación de Laravel
        $response->assertStatus(422)
                 ->assertJsonValidationErrors(['productos']);
    }
}