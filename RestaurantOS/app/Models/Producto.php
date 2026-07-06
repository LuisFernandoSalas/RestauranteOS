<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Carbon\Carbon;

class Producto extends Model

{
    use HasFactory;
    protected $table = 'productos';
    protected $fillable = 
    [
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
    // Relación: Un producto pertenece a una categoría
    public function categoria()
    {
        return $this->belongsTo(Categoria::class);
    }

    // Scope para filtrar productos disponibles (activos y no pausados)
    public function scopeDisponibles($query)
    {
        return $query->where(function($q) {
            $q->whereNull('pausado_hasta')
              ->orWhere('pausado_hasta', '<=', Carbon::now());
        });
    }

    public function detalles()
    {
        return $this->hasMany(DetallePedido::class, 'producto_id');
    }
}