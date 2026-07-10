<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Carbon\Carbon;

class Producto extends Model
{
    use HasFactory;

    protected $table = 'productos';

    protected $fillable = [
        'categoria_id',
        'nombre',
        'descripcion',
        'precio',
        'pausado_hasta'
    ];

    protected $casts = [
        'precio' => 'decimal:2',
        'pausado_hasta' => 'datetime',
    ];

    // Envía automáticamente el estado de disponibilidad calculado al JSON de la API
    protected $appends = ['is_disponible'];

    /**
     * RELACIONES
     */

    public function categoria(): BelongsTo
    {
        return $this->belongsTo(Categoria::class, 'categoria_id');
    }

    public function detalles(): HasMany
    {
        return $this->hasMany(DetallePedido::class, 'producto_id');
    }

    /**
     * SCOPES (Filtros eficientes para Query Builder)
     */

    public function scopeDisponibles($query)
    {
        return $query->where(function ($q) {
            $q->whereNull('pausado_hasta')
                ->orWhere('pausado_hasta', '<=', Carbon::now());
        });
    }

    /**
     * ACCESORS (Para consumo inmediato en las Apps Java/Retrofit)
     */

    /**
     * Devuelve true o false si el producto se puede ordenar en este milisegundo exacto.
     */
    public function getIsDisponibleAttribute(): bool
    {
        if (is_null($this->pausado_hasta)) {
            return true;
        }

        return $this->pausado_hasta->isPast();
    }
    // Un producto puede tener muchos ingredientes en su receta
    public function recetas()
    {
        return $this->hasMany(Receta::class);
    }

    // Un producto puede estar en varios combos
    public function combos()
    {
        return $this->belongsToMany(Combo::class, 'combo_producto');
    }
}
