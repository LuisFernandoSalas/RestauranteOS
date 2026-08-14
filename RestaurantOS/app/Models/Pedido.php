<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;
use Illuminate\Database\Eloquent\Relations\HasOne; // <-- 1. Importación agregada
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Pedido extends Model
{
    use SoftDeletes, HasFactory;

    protected $fillable = [
        'client_uuid',
        'mesa_id',
        'user_id',       // ID del Mesero que tomó la orden
        'estado',        // 'pendiente','en_preparacion','listo','entregado','pagado','cancelado'
        'motivo_cancelacion',
        'cancelado_por'  // ID del usuario que tumba el pedido
    ];

    /**
     * RELACIONES DEL SISTEMA
     */

    public function productos(): BelongsToMany
    {
        return $this->belongsToMany(Producto::class, 'pedido_producto')
                    ->withPivot('cantidad', 'precio_unitario', 'subtotal', 'estado')
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

    public function cancelador(): BelongsTo
    {
        return $this->belongsTo(User::class, 'cancelado_por');
    }

    public function pagos(): HasMany
    {
        return $this->hasMany(Pago::class);
    }

    // Factura vinculada al pedido
    public function factura(): HasOne // <-- 2. Tipo de retorno especificado
    {
        return $this->hasOne(Factura::class);
    }

    /**
     * ACCESORS SEGUROS
     */

    public function getTotalCalculadoAttribute(): float
    {
        if ($this->relationLoaded('detalles')) {
            return (float) $this->detalles->sum('subtotal');
        }
        return (float) $this->detalles()->sum('subtotal');
    }

    public function getTotalPagadoAttribute(): float
    {
        return (float) $this->pagos()->sum('monto_recibido');
    }
}