<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class EmpleadoApiTest extends TestCase
{
    use RefreshDatabase;

    protected User $admin;

    protected function setUp(): void
    {
        parent::setUp();
        $this->admin = User::factory()->create(['role' => 'admin']);
        Sanctum::actingAs($this->admin, ['*']);
    }

    public function test_crud_completo_de_empleados(): void
    {
        // 1. Crear Empleado
        $responseStore = $this->postJson('/api/empleados', [
            'name'     => 'Carlos Mesero',
            'username' => 'carlos',
            'password' => 'password123',
            'role'     => 'mesero',
        ]);
        $responseStore->assertStatus(201);
        $empleadoId = $responseStore->json('data.id') ?? $responseStore->json('id');

        // 2. Listar Empleados
        $this->getJson('/api/empleados')->assertStatus(200);

        // 3. Ver Empleado
        $this->getJson("/api/empleados/{$empleadoId}")->assertStatus(200);

        // 4. Actualizar Empleado
        $this->putJson("/api/empleados/{$empleadoId}", [
            'name' => 'Carlos Mesero Editado',
            'role' => 'mesero',
        ])->assertStatus(200);

        // 5. Eliminar Empleado
        $this->deleteJson("/api/empleados/{$empleadoId}")->assertStatus(200);
    }
}