<?php

namespace Database\Factories;

use App\Models\Factura;
use App\Models\Pedido;
use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\Factura>
 */
class FacturaFactory extends Factory
{
    protected $model = Factura::class;

    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition(): array
    {
        $uuid = $this->faker->uuid();

        return [
            'pedido_id'      => Pedido::factory(),
            'rfc'            => 'XAXX010101000', // RFC genérico
            'razon_social'   => $this->faker->company(),
            'uso_cfdi'       => 'G03',
            'regimen_fiscal' => '601',
            'email'          => $this->faker->safeEmail(),
            'uuid_fiscal'    => $uuid,
            'pdf_url'        => "/storage/facturas/{$uuid}.pdf",
            'xml_url'        => "/storage/facturas/{$uuid}.xml",
            'monto_total'    => $this->faker->randomFloat(2, 50, 1500),
            'estado'         => 'generada',
        ];
    }
}