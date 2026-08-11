<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class TurnoCaja extends Model
{
    use HasFactory;

    protected $table = 'turnos_caja';

    protected $fillable = [
        'user_id',
        'monto_apertura',
        'monto_cierre_efectivo',
        'monto_cierre_tarjeta',
        'monto_esperado_efectivo',
        'diferencia',
        'monto_siguiente_turno',
        'estado',
        'notas',
        'opened_at',
        'closed_at',
    ];

    protected $casts = [
        'monto_apertura' => 'decimal:2',
        'monto_cierre_efectivo' => 'decimal:2',
        'monto_cierre_tarjeta' => 'decimal:2',
        'monto_esperado_efectivo' => 'decimal:2',
        'diferencia' => 'decimal:2',
        'monto_siguiente_turno' => 'decimal:2',
        'opened_at' => 'datetime',
        'closed_at' => 'datetime',
    ];

    /**
     * Usuario/Cajero dueño del turno
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }
}
