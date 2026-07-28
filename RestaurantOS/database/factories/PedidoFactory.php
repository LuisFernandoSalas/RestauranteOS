<?php

namespace Database\Factories;

use App\Models\Pedido;
use App\Models\Mesa;
use App\Models\User;
use Illuminate\Database\Eloquent\Factories\Factory;

class PedidoFactory extends Factory
{
    protected $model = Pedido::class;

    public function definition(): array
    {
        return [
            'client_uuid' => $this->faker->uuid(),
            'mesa_id' => Mesa::factory(),
            'user_id' => User::factory(),
            'estado' => 'pendiente',
        ];
    }
}
