<?php

namespace Database\Factories;

use App\Models\Insumo;
use Illuminate\Database\Eloquent\Factories\Factory;

class InsumoFactory extends Factory
{
    protected $model = Insumo::class;

    public function definition(): array
    {
        return [
            'nombre'        => $this->faker->word(),
            'stock_actual'  => $this->faker->numberBetween(10, 100),
            'stock_minimo'  => 10,
            'unidad_medida' => $this->faker->randomElement(['kg', 'gr', 'pzas', 'lt']),
        ];
    }
}