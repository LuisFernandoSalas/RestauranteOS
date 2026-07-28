<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Pedido;
use App\Models\Mesa;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class SecurityApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test: Un mesero NO puede eliminar pedidos (Debe retornar 403 Forbidden).
     */
    public function test_mesero_no_puede_eliminar_un_pedido(): void
    {
        // 1. Autenticar a un usuario con rol de mesero (no admin)
        $mesero = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($mesero, ['*']);

        // 2. Crear una mesa y un pedido de prueba
        $mesa = Mesa::factory()->create(['estado' => 'ocupado']);
        $pedido = Pedido::factory()->create(['mesa_id' => $mesa->id]);

        // 3. Intentar hacer la petición DELETE al endpoint protegido
        $response = $this->deleteJson("/api/pedidos/{$pedido->id}");

        // 4. Aseverar que el sistema bloquea el acceso con código 403
        $response->assertStatus(403);
        $response->assertJson([
            'Error' => 'No autorizado'
        ]);

        // 5. Verificar que el pedido sigue intacto en la base de datos
        $this->assertDatabaseHas('pedidos', [
            'id' => $pedido->id
        ]);
    }

    /**
     * Test: Un usuario invitado (sin token) no puede acceder a rutas protegidas.
     */
    public function test_usuario_no_autenticado_es_rechazado(): void
    {
        $mesa = Mesa::factory()->create(['estado' => 'ocupado']);
        $pedido = Pedido::factory()->create(['mesa_id' => $mesa->id]);

        // Intentar eliminar sin token Sanctum
        $response = $this->deleteJson("/api/pedidos/{$pedido->id}");

        // Laravel por defecto responde con 401 Unauthorized para rutas protegidas por auth:sanctum
        $response->assertStatus(401);
    }
}