<?php

namespace Database\Factories;

use App\Models\Pago;
use App\Models\Pedido;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

class PagoFactory extends Factory
{
    protected $model = Pago::class;

    public function definition(): array
    {
        return [
            'pedido_id'        => Pedido::factory(),
            'cobrado_por'      => User::factory(),
            'metodo_pago'      => $this->faker->randomElement(['efectivo', 'tarjeta', 'transferencia', 'mixto']),
            'monto_recibido'   => $this->faker->randomFloat(2, 50, 500),
            'propina'          => $this->faker->randomFloat(2, 0, 50),
            'requiere_factura' => $this->faker->boolean(20),
        ];
    }
}