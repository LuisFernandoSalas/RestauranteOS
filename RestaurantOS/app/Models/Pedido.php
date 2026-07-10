<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\SoftDeletes;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

class Pedido extends Model
{
    use SoftDeletes;

    // 🚨 Eliminamos $appends = ['total'] para evitar el problema de rendimiento N+1.
    // Usaremos ->withSum('detalles', 'subtotal') en las consultas del controlador.

    protected $fillable = [
        'client_uuid',
        'mesa_id',
        'user_id',       // ID del Mesero que tomó la orden
        'estado',        // Estandarizado: 'pendiente','en_preparacion','listo','entregado','pagado','cancelado'
        'motivo_cancelacion',
        'cancelado_por'  // ID del usuario (Admin/Cocinero) que tumba el pedido
    ];

    /**
     * RELACIONES DEL SISTEMA
     */

    // Relación directa a través de la tabla pivote (si se requiere consultar rápido)
    public function productos(): BelongsToMany
    {
        return $this->belongsToMany(Producto::class, 'pedido_producto')
                    ->withPivot('cantidad', 'precio_unitario', 'subtotal', 'estado')
                    ->withTimestamps();
    }

    // Relación uno a muchos con el modelo intermedio (Recomendado para el KDS de Cocina)
    public function detalles(): HasMany 
    {
        return $this->hasMany(DetallePedido::class);
    }

    // Mesa asignada al pedido
    public function mesa(): BelongsTo
    {
        return $this->belongsTo(Mesa::class);
    }

    // Mesero asignado (Mantenemos user_id en BD por tu estructura original)
    public function mesero(): BelongsTo
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    // Relación con el empleado de auditoría que ejecutó la cancelación
    public function cancelador(): BelongsTo
    {
        return $this->belongsTo(User::class, 'cancelado_por');
    }

    // Historial transaccional del pedido (Soporta pago Efectivo, Tarjeta, Mixto)
    public function pagos(): HasMany
    {
        return $this->hasMany(Pago::class);
    }

    /**
     * ACCESORS SEGUROS (Sin consultas N+1 embebidas en JSON automáticamente)
     */
    
    /**
     * Obtiene el total calculado dinámicamente si los detalles ya están cargados en memoria,
     * si no, ejecuta la suma de forma aislada.
     */
    public function getTotalCalculadoAttribute(): float
    {
        if ($this->relationLoaded('detalles')) {
            return (float) $this->detalles->sum('subtotal');
        }
        return (float) $this->detalles()->sum('subtotal');
    }

    /**
     * Obtiene cuánto se ha pagado formalmente en la tabla transaccional.
     */
    public function getTotalPagadoAttribute(): float
    {
        return (float) $this->pagos()->sum('monto_recibido');
    }
}