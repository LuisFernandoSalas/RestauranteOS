<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Combo extends Model
{
    protected $fillable = [
        'nombre',
        'precio_especial',
        'fecha_inicio',
        'fecha_fin',
        'estado' // activo, pausado
    ];

    // Relación Muchos a Muchos con Productos
    public function productos()
    {
        return $this->belongsToMany(Producto::class, 'combo_producto')
                    ->withPivot('cantidad')
                    ->withTimestamps();
    }
}