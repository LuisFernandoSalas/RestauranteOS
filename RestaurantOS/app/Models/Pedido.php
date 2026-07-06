<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Pedido extends Model
{
    //para filtrar pedidos por monto
    protected $appends = ['total'];
    
    protected $fillable = [
        'status',
        'client_uuid',
        'mesa_id',
        'user_id',
        'total',
        'metodo_pago',
        // Campos añadidos para la Caja
        'pago_efectivo',
        'pago_tarjeta',
        'propina',
        'requiere_factura'
    ];

    // usamos withPivot para cargar los datos de la tabla intermedia
    public function productos()
    {
        return $this->belongsToMany(Producto::class, 'pedido_producto')
                    ->withPivot('cantidad', 'precio_unitario', 'subtotal')
                    ->withTimestamps();
    }

    public function detalles(): HasMany 
    {
        return $this->hasMany(DetallePedido::class);
    }

    public function mesa(): BelongsTo
    {
        return $this->belongsTo(Mesa::class);
    }

    public function mesero(): BelongsTo
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    // Alias para mayor claridad
    public function user()
    {
        return $this->mesero();
    }
    /**
     * Accesor para obtener el total del pedido sumando sus detalles
     */
    public function getTotalAttribute()
    {
        // Suma el subtotal de todos los detalles relacionados
        return $this->detalles()->sum('subtotal');
    }
}
