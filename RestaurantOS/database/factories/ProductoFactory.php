<?php

namespace Database\Factories;

use App\Models\Producto;
use App\Models\Categoria;
use Illuminate\Database\Eloquent\Factories\Factory;

class ProductoFactory extends Factory
{
    protected $model = Producto::class;

    public function definition(): array
    {
        return [
            'categoria_id'  => Categoria::factory(),
            'nombre'        => $this->faker->word(),
            'descripcion'   => $this->faker->sentence(),
            'precio'        => $this->faker->randomFloat(2, 50, 500),
            'estado'        => 'activo',
            'pausado_hasta' => null,
        ];
    }
}
