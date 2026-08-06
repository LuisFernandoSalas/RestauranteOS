<?php
namespace Database\Factories;

use App\Models\Combo;
use Illuminate\Database\Eloquent\Factories\Factory;

class ComboFactory extends Factory
{
    protected $model = Combo::class;

    public function definition(): array
    {
        $fechaInicio = $this->faker->dateTimeBetween('-1 month', '+1 month');
        
        return [
            'nombre' => $this->faker->words(3, true) . ' Combo',
            'precio_especial' => $this->faker->randomFloat(2, 50, 500), // Precio entre 50 y 500
            'fecha_inicio' => $fechaInicio->format('Y-m-d'),
            'fecha_fin' => $this->faker->dateTimeBetween($fechaInicio, '+3 months')->format('Y-m-d'),
            'estado' => $this->faker->randomElement(['activo', 'pausado']),
        ];
    }
}