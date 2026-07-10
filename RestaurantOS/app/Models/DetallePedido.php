<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class DetallePedido extends Model
{
    // Estandarizado en plural según la convención y las migraciones exitosas ejecutadas
    protected $table = 'detalle_pedido';

    protected $fillable = [
        'pedido_id',
        'producto_id',
        'cantidad',
        'precio_unitario', // Obligatorio para congelar el precio histórico del producto
        'subtotal',
        'nota',            // Ej: "Sin cebolla", "bien tostadas" (Crucial para Cocina)
        'estado'           // Estandarizado: 'pendiente', 'en_preparacion', 'listo', 'entregado', 'cancelado'
    ];

    // Forzar tipos de datos correctos en las respuestas JSON hacia Retrofit / Java HttpClient
    protected $casts = [
        'cantidad' => 'integer',
        'precio_unitario' => 'decimal:2',
        'subtotal' => 'decimal:2',
    ];

    /**
     * RELACIONES
     */

    // El pedido padre al que pertenece esta línea de comanda
    public function pedido(): BelongsTo
    {
        return $this->belongsTo(Pedido::class);
    }

    // El producto actual asociado al detalle
    public function producto(): BelongsTo
    {
        return $this->belongsTo(Producto::class);
    }

    /**
     * LÓGICA DE NEGOCIO / MUTADORES AUTOMÁTICOS
     */

    /**
     * Boot del modelo para forzar el cálculo automático del subtotal antes de guardar 
     * como salvaguarda en caso de que el cliente Java envíe mal el cálculo de punto flotante.
     */
    protected static function boot()
    {
        parent::boot();

        static::creating(function ($detalle) {
            // Si viene el precio_unitario pero no el subtotal, lo calculamos en backend
            if ($detalle->precio_unitario && $detalle->cantidad) {
                $detalle->subtotal = $detalle->precio_unitario * $detalle->cantidad;
            }
        });

        static::updating(function ($detalle) {
            if ($detalle->precio_unitario && $detalle->cantidad) {
                $detalle->subtotal = $detalle->precio_unitario * $detalle->cantidad;
            }
        });
    }
}
