<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Insumo extends Model
{
    use HasFactory;

    protected $fillable = [
        'nombre',
        'stock_actual',
        'stock_minimo',
        'unidad_medida' // kg, gr, pzas, lt
    ];

    // Relación: Un insumo puede estar en muchas recetas
    public function recetas()
    {
        return $this->hasMany(Receta::class);
    }
}