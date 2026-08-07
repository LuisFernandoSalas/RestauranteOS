<?php

namespace Tests\Feature;

use App\Models\DetallePedido;
use App\Models\Mesa;
use App\Models\Pago;
use App\Models\Pedido;
use App\Models\Producto;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class ReportTest extends TestCase
{
    use RefreshDatabase;

    protected User $admin;

    protected function setUp(): void
    {
        parent::setUp();
        $this->admin = User::factory()->create(['role' => 'admin']);
        Sanctum::actingAs($this->admin);
    }

    public function test_puede_obtener_datos_del_dashboard()
    {
        $response = $this->getJson('/api/dashboard/reportes');

        $response->assertStatus(200)
                 ->assertJsonStructure([
                     'status',
                     'resumen',
                     'datos_diarios',
                     'metodos_pago',
                     'top_productos',
                     'inventario_critico',
                     'auditoria'
                 ]);
    }

    public function test_puede_obtener_reporte_de_ventas_por_periodo()
    {
        $fechaHoy = now()->toDateString();

        $mesa = Mesa::factory()->create();

        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->admin->id,
            'estado'  => 'pagado',
            'total'   => 100.00,
        ]);

        Pago::create([
            'pedido_id'        => $pedido->id,
            'monto_recibido'   => 100.00,
            'monto_total'      => 100.00,
            'propina'          => 10.00,
            'metodo_pago'      => 'efectivo',
            'requiere_factura' => false,
            'cobrado_por'      => $this->admin->id, // <- Agregado aquí
        ]);

        $response = $this->getJson("/api/reportes/ventas?fecha_inicio={$fechaHoy}&fecha_fin={$fechaHoy}");

        $response->assertStatus(200)
                 ->assertJson([
                     'status' => 'success',
                     'data'   => [
                         'total_ingresos' => 100.00,
                         'total_propinas' => 10.00,
                         'total_comandas' => 1,
                     ]
                 ]);
    }

    public function test_puede_obtener_top_productos_populares()
    {
        $mesa = Mesa::factory()->create();
        $producto = Producto::factory()->create(['nombre' => 'Tacos al Pastor', 'precio' => 25.00]);

        $pedido = Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $this->admin->id,
            'estado'  => 'pagado',
            'total'   => 50.00,
        ]);

        DetallePedido::create([
            'pedido_id'       => $pedido->id,
            'producto_id'     => $producto->id,
            'cantidad'        => 2,
            'precio_unitario' => 25.00,
            'subtotal'        => 50.00,
        ]);

        $response = $this->getJson('/api/reportes/productos-populares');

        $response->assertStatus(200)
                 ->assertJsonPath('data.productos.0.producto.nombre', 'Tacos al Pastor');
    }

    public function test_puede_obtener_rendimiento_de_meseros()
    {
        $mesero = User::factory()->create(['role' => 'mesero', 'name' => 'Carlos']);
        $mesa = Mesa::factory()->create();

        Pedido::factory()->create([
            'mesa_id' => $mesa->id,
            'user_id' => $mesero->id,
            'estado'  => 'pagado',
            'total'   => 150.00,
        ]);

        $response = $this->getJson('/api/reportes/rendimiento-meseros');

        $response->assertStatus(200)
                 ->assertJsonFragment([
                     'nombre'             => 'Carlos',
                     'comandas_atendidas' => 1,
                     'total_facturado'    => 150.00,
                 ]);
    }
}