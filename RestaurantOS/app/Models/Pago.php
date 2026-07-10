<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;

class Pago extends Model
{
    use SoftDeletes;

    protected $table = 'pagos';

    protected $fillable = [
        'pedido_id',
        'cobrado_por',
        'metodo_pago',
        'monto_recibido',
        'propina',
        'requiere_factura'
    ];

    // Forzar tipos de datos correctos para la API
    protected $casts = [
        'monto_recibido' => 'decimal:2',
        'propina' => 'decimal:2',
        'requiere_factura' => 'boolean',
    ];

    public function pedido()
    {
        return $this->belongsTo(Pedido::class);
    }

    public function cobrador()
    {
        return $this->belongsTo(User::class, 'cobrado_por');
    }
}
