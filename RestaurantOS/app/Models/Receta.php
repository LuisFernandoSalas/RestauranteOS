<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Receta extends Model
{
    protected $fillable = [
        'producto_id',
        'insumo_id',
        'cantidad_por_porcion'
    ];

    public function producto()
    {
        return $this->belongsTo(Producto::class);
    }

    public function insumo()
    {
        return $this->belongsTo(Insumo::class);
    }
}