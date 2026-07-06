<?php

namespace Database\Seeders;

use App\Models\Producto;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class ProductoSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        //
        Producto::created(['nombre' => 'Enchiladas Verdes', 'precio' => 120.00, 'categoria' => 'Comida']);
        Producto::created(['nombre' => 'Agua de Jamaica', 'precio' => 40.00, 'categoria' => 'Bebida']);
        Producto::created(['nombre' => 'Agua de Horchata', 'precio' => 40.00, 'categoria' => 'Bebida']);
    }
}
