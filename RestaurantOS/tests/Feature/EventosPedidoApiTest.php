<?php

namespace Tests\Feature;

use App\Models\User;
use App\Models\Mesa;
use App\Models\Producto;
use App\Events\PedidoCreado;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Event;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class EventosPedidoApiTest extends TestCase
{
    use RefreshDatabase;

    /**
     * Test: Al crear un pedido exitosamente, se debe disparar el evento de WebSocket.
     */
    public function test_crear_pedido_dispara_evento_broadcast(): void
    {
        // 1. Encender el interceptor de eventos de Laravel
        Event::fake([PedidoCreado::class]);

        // 2. Autenticar mesero y preparar datos limpios
        $mesero = User::factory()->create(['role' => 'mesero']);
        Sanctum::actingAs($mesero, ['*']);

        $mesa = Mesa::factory()->create(['estado' => 'libre']);
        $producto = Producto::factory()->create(['precio' => 150.00]);

        // Payload directo usando solo lo que tu controlador de pedidos pide
        $payload = [
            'client_uuid' => (string) \Illuminate\Support\Str::uuid(),
            'mesa_id' => $mesa->id,
            'productos' => [
                [
                    'id' => $producto->id,
                    'cantidad' => 2
                ]
            ]
        ];

        // 3. Ejecutar la petición
        $response = $this->postJson('/api/pedidos', $payload);

        // Asegurar que el controlador respondió bien (200 o 201 según tu API)
        $response->assertSuccessful(); 

        // 4. Verificar que el evento se despachó al canal de WebSockets con el pedido
        Event::assertDispatched(PedidoCreado::class, function ($event) use ($mesa) {
            return $event->pedido->mesa_id === $mesa->id;
        });
    }
}