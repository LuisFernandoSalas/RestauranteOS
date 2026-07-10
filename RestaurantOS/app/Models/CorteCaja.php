<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class CorteCaja extends Model
{
    protected $fillable = [
        'user_id',
        'fondo_apertura',
        'fondo_siguiente_turno',
        'ventas_sistema_efectivo',
        'efectivo_real',
        'diferencia',
        'total_entregado',
        'observaciones'
    ];

    public function cajero()
    {
        return $this->belongsTo(User::class, 'user_id');
    }
}