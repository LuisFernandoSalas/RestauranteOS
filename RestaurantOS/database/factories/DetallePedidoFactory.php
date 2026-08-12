<?php

namespace Database\Factories;

use App\Models\DetallePedido;
use App\Models\Pedido;
use App\Models\Producto;
use Illuminate\Database\Eloquent\Factories\Factory;

class DetallePedidoFactory extends Factory
{
    protected $model = DetallePedido::class;

    public function definition(): array
    {
        return [
            'pedido_id' => Pedido::factory(),
            'producto_id' => Producto::factory(),
            'cantidad' => $this->faker->numberBetween(1, 4),
            'nota' => $this->faker->optional(0.3)->sentence(), // 30% de probabilidad de tener nota
            'estado' => 'pendiente',
        ];
    }

    /**
     * Estado: En Preparación
     */
    public function enPreparacion(): static
    {
        return $this->state(fn (array $attributes) => [
            'estado' => 'en_preparacion',
        ]);
    }

    /**
     * Estado: Pausado
     */
    public function pausado(): static
    {
        return $this->state(fn (array $attributes) => [
            'estado' => 'pausado',
        ]);
    }

    /**
     * Estado: Listo
     */
    public function listo(): static
    {
        return $this->state(fn (array $attributes) => [
            'estado' => 'listo',
        ]);
    }

    /**
     * Estado: Cancelado
     */
    public function cancelado(): static
    {
        return $this->state(fn (array $attributes) => [
            'estado' => 'cancelado',
        ]);
    }
}