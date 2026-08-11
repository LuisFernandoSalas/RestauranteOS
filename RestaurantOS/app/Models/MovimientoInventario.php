<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class MovimientoInventario extends Model
{
    use HasFactory;

    protected $table = 'movimientos_inventario';

    protected $fillable = [
        'insumo_id',
        'user_id',
        'tipo',
        'cantidad',
        'stock_resultante',
        'motivo',
    ];

    protected $casts = [
        'cantidad'         => 'float',
        'stock_resultante' => 'float',
    ];

    /**
     * Insumo afectado por el movimiento
     */
    public function insumo(): BelongsTo
    {
        return $this->belongsTo(Insumo::class);
    }

    /**
     * Usuario/Empleado que autorizó o realizó la acción
     */
    public function usuario(): BelongsTo
    {
        return $this->belongsTo(User::class, 'user_id');
    }
}