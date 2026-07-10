<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Insumo;
use App\Models\Producto;
use App\Models\Receta;

class InventarioTestSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Crear Insumos en el almacén
        $pollo = Insumo::create([
            'nombre' => 'Pechuga de Pollo',
            'categoria' => 'Carnes',
            'stock_actual' => 5.00, // 5 kg
            'stock_minimo' => 1.00,
            'unidad_medida' => 'kg'
        ]);

        $tortilla = Insumo::create([
            'nombre' => 'Tortilla de Maíz',
            'categoria' => 'Abarrotes',
            'stock_actual' => 100.00, // 100 piezas
            'stock_minimo' => 20.00,
            'unidad_medida' => 'pza'
        ]);

        // 2. Buscar o crear un producto existente (Asegúrate de que el ID coincida o crea uno)
        $platillo = Producto::first() ?? Producto::create([
            'nombre' => 'Enchiladas de Pollo',
            'precio' => 120.00,
            'categoria_id' => 1 // Asumiendo que tienes categorías
        ]);

        // 3. Crear la receta: 1 orden de Enchiladas gasta 150g de pollo y 3 tortillas
        Receta::create([
            'producto_id' => $platillo->id,
            'insumo_id' => $pollo->id,
            'cantidad_por_porcion' => 0.150 // 150 gramos
        ]);

        Receta::create([
            'producto_id' => $platillo->id,
            'insumo_id' => $tortilla->id,
            'cantidad_por_porcion' => 3.000 // 3 piezas
        ]);
    }
}