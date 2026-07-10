<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\User;
use App\Models\Mesa;
use App\Models\Producto;
use App\Models\Categoria;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     */
    public function run(): void
    {
        // --- 1. USUARIOS Y ROLES ---
        User::create([
            'name' => 'José Eduardo', 
            'username' => 'admin', 
            'password' => Hash::make('admin123'), 
            'role' => 'admin'
        ]);
        User::create([
            'name' => 'Hasiel', 
            'username' => 'hasielMesero01', 
            'password' => Hash::make('mesero123'), 
            'role' => 'mesero'
        ]);
        User::create([
            'name' => 'Kevyn Bote', 
            'username' => 'kevynCaja', 
            'password' => Hash::make('cajero123'), 
            'role' => 'cajero'
        ]);
        User::create([
            'name' => 'Fernando', 
            'username' => 'ferCocina', 
            'password' => Hash::make('cocina123'), 
            'role' => 'cocinero'
        ]);
        User::create([
            'name' => 'Luis',
            'username' => 'luis', 
            'password' => Hash::make('123456')
        ]);

        // --- 2. CATÁLOGO DE MESAS (1 al 9 como en tu diseño) ---
        for ($i = 1; $i <= 9; $i++) {
            Mesa::create([
                'numero' => $i, 
                'status' => 'libre'
            ]);
        }

        // --- 3. CATÁLOGO DE CATEGORÍAS ---
        $categoriaEntrada     = Categoria::create(['nombre' => 'Entradas']);
        $categoriaPlatoFuerte = Categoria::create(['nombre' => 'Platos fuertes']);
        $categoriaBebida      = Categoria::create(['nombre' => 'Bebidas']);
        $categoriaPostre      = Categoria::create(['nombre' => 'Postres']);

        // --- 4. PRODUCTOS ---
        Producto::create([
            'nombre'       => 'Enchiladas verdes',
            'precio'       => 85.00,
            'categoria_id' => $categoriaPlatoFuerte->id,
            'status'       => 'activo' // <-- Añadido
        ]);

        Producto::create([
            'nombre'       => 'Pozole rojo',
            'precio'       => 95.00,
            'categoria_id' => $categoriaPlatoFuerte->id,
            'status'       => 'activo' // <-- Añadido
        ]);

        Producto::create([
            'nombre'       => 'Tostadas de pata',
            'precio'       => 45.00,
            'categoria_id' => $categoriaEntrada->id,
            'status'       => 'activo' // <-- Añadido
        ]);

        Producto::create([
            'nombre'       => 'Agua de Jamaica',
            'precio'       => 20.00,
            'categoria_id' => $categoriaBebida->id,
            'status'       => 'activo' // <-- Añadido
        ]);

        Producto::create([
            'nombre'       => 'Pastel de Chocolate',
            'precio'       => 35.00,
            'categoria_id' => $categoriaPostre->id,
            'status'       => 'activo' // <-- Añadido
        ]);

    }
}
