<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Receta extends Model
{
    protected $fillable = [
        'producto_id',
        'nombre',               // <-- 1. Agregado para poder guardar el nombre de la receta
        'insumo_id',
        'cantidad_por_porcion'
    ];

    public function producto()
    {
        // <-- 2. Le agregamos withDefault()
        return $this->belongsTo(Producto::class)->withDefault([
            'nombre' => 'Sin producto asignado'
        ]);
    }

    public function insumo()
    {
        return $this->belongsTo(Insumo::class);
    }
}