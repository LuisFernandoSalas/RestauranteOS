<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class Combo extends Model
{
    use HasFactory, SoftDeletes;

    protected $fillable = [
        'nombre',
        'precio_especial',
        'fecha_inicio',
        'fecha_fin',
        'estado'
    ];

    // <- Casts agregados para facilitar el uso de fechas en PHP
    protected $casts = [
        'fecha_inicio' => 'date',
        'fecha_fin' => 'date',
        'precio_especial' => 'decimal:2',
    ];

    public function productos()
    {
        return $this->belongsToMany(Producto::class, 'combo_producto')
                    ->withPivot('cantidad')
                    ->withTimestamps();
    }
}