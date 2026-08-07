<?php

namespace Tests\Feature;

use App\Models\Insumo;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum; // 👈 Importar Sanctum
use Tests\TestCase;

class NotificationTest extends TestCase
{
    use RefreshDatabase;

    public function test_genera_notificacion_cuando_el_stock_cae_al_minimo()
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user); // 👈 Autenticar con Sanctum

        $insumo = Insumo::create([
            'nombre'        => 'Queso Gouda',
            'stock_actual'  => 10,
            'stock_minimo'  => 5,
            'unidad_medida' => 'kg',
        ]);

        // Descontamos 6kg (queda en 4kg <= 5kg stock mínimo)
        $insumo->descontarStock(6);

        // Verifica guardado en DB
        $this->assertDatabaseHas('notifications', [
            'notifiable_id'   => $user->id,
            'notifiable_type' => User::class,
        ]);

        // Consulta el endpoint de notificaciones
        $response = $this->getJson('/api/notificaciones');

        $response->assertStatus(200)
                 ->assertJsonCount(1, 'data')
                 ->assertJsonPath('data.0.data.insumo_nombre', 'Queso Gouda');
    }

    public function test_no_genera_notificacion_si_el_stock_sigue_por_encima_del_minimo()
    {
        $user = User::factory()->create();
        Sanctum::actingAs($user);

        $insumo = Insumo::create([
            'nombre'        => 'Harina',
            'stock_actual'  => 20,
            'stock_minimo'  => 5,
            'unidad_medida' => 'kg',
        ]);

        $insumo->descontarStock(5); // Quedan 15kg > 5kg

        $this->assertDatabaseCount('notifications', 0);
    }
}