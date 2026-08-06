<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Mesa extends Model
{
    use HasFactory;

    protected $table = 'mesas';

    protected $fillable = [
        'numero',
        'estado', // 'libre', 'ocupada', 'mantenimiento'
        'capacidad',
    ];

    /**
     * RELACIONES
     */

    // Una mesa puede tener muchos pedidos
    public function pedidos(): HasMany
    {
        return $this->hasMany(Pedido::class);
    }

    /**
     * SCOPES
     */

    // Scope para obtener mesas desocupadas
    public function scopeDisponible($query)
    {
        return $query->where('estado', 'libre');
    }
}