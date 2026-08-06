<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class DetallePedido extends Model
{
    // Estandarizado en singular según tu base de datos
    protected $table = 'detalle_pedido';

    protected $fillable = [
        'pedido_id',
        'producto_id',
        'combo_id',        // <- Novedad: Permite asociar un combo a esta línea
        'cantidad',
        'precio_unitario', // Congela el precio del producto o combo_especial
        'subtotal',
        'nota',            // Ej: "Sin cebolla", "bien tostadas" (Crucial para Cocina)
        'estado'           // 'pendiente', 'en_preparacion', 'listo', 'entregado', 'cancelado'
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

    // El producto actual asociado al detalle (null si es un combo)
    public function producto(): BelongsTo
    {
        return $this->belongsTo(Producto::class);
    }

    // El combo asociado al detalle (null si es un producto individual)
    public function combo(): BelongsTo
    {
        return $this->belongsTo(Combo::class);
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