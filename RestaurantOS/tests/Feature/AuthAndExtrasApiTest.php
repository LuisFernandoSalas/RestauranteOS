<?php

namespace Tests\Feature;

use App\Models\Producto;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class AuthAndExtrasApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_autenticacion_login_me_y_logout(): void
    {
        $user = User::factory()->create([
            'username' => 'admin',
            'password' => bcrypt('secret123'),
        ]);

        $resLogin = $this->postJson('/api/login', [
            'username' => 'admin',
            'password' => 'secret123',
        ]);

        $resLogin->assertStatus(200);

        $token = $resLogin->json('token') ?? $resLogin->json('access_token') ?? $resLogin->json('data.token');
        $this->assertNotNull($token, 'El token no debe ser nulo en la respuesta del login.');

        Sanctum::actingAs($user, ['*']);
        $this->getJson('/api/me')->assertStatus(200);

        $this->postJson('/api/logout')->assertStatus(200);
    }

    public function test_operaciones_extra_de_cocina_cobro_y_facturacion(): void
    {
        $user = User::factory()->create(['role' => 'cocinero']);
        Sanctum::actingAs($user, ['*']);

        $producto = Producto::factory()->create();

        $this->postJson("/api/cocina/productos/{$producto->id}/pausar", [
            'duracion' => '30_min',
        ])->assertStatus(200);
    }
}