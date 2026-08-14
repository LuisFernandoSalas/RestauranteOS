<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\Insumo;
use App\Models\Producto;
use App\Models\Receta;
use App\Models\Categoria;

class InventarioTestSeeder extends Seeder
{
    public function run(): void
    {
        
        $categoria = Categoria::firstOrCreate([
            'nombre' => 'Platillos Principales'
        ]);

        // 1. Crear Insumos
    $pollo = Insumo::create([
        'nombre'         => 'Pechuga de Pollo',
        'categoria'      => 'Carnes',
        'stock_actual'   => 5.00,
        'stock_minimo'   => 1.00,
        'stock_maximo'   => 20.00,  // <-- Límite para la barra de progreso
        'costo_unitario' => 120.00, // <-- Para calcular costos en recetas ($/kg)
        'unidad_medida'  => 'kg'
    ]);

    $tortilla = Insumo::create([
        'nombre'         => 'Tortilla de Maiz',
        'categoria'      => 'Abarrotes',
        'stock_actual'   => 100.00,
        'stock_minimo'   => 20.00,
        'stock_maximo'   => 200.00, // <-- Límite para la barra de progreso
        'costo_unitario' => 0.25,   // <-- Para calcular costos en recetas ($/pza)
        'unidad_medida'  => 'pza'
    ]);


        // 2. Producto con ID de categoría dinámico
        $platillo = Producto::first() ?? Producto::create([
            'nombre' => 'Enchiladas de Pollo',
            'precio' => 120.00,
            'categoria_id' => $categoria->id 
        ]);

        // 3. Crear Receta
        Receta::create([
            'producto_id' => $platillo->id,
            'insumo_id' => $pollo->id,
            'cantidad_por_porcion' => 0.150
        ]);

        Receta::create([
            'producto_id' => $platillo->id,
            'insumo_id' => $tortilla->id,
            'cantidad_por_porcion' => 3.000
        ]);
    }
}