<?php

namespace App\Models;
use App\Models\User;
use App\Notifications\StockBajoNotification;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Insumo extends Model
{
    use HasFactory;

    protected $fillable = [
        'nombre',
        'categoría',
        'stock_actual',
        'stock_minimo',
        'unidad_medida', // kg, gr, pzas, lt
    ];

    /**
     * RELACIONES
     */

    // Relación: Un insumo puede estar en muchas recetas
    public function recetas(): HasMany
    {
        return $this->hasMany(Receta::class);
    }

    /**
     * MÉTODOS DE NEGOCIO Y STOCK
     */

    /**
     * Descuenta stock actual y verifica si es necesario notificar alerta de stock mínimo.
     */
    public function descontarStock(float $cantidad): void
    {
        $this->decrement('stock_actual', $cantidad);
        $this->refresh(); // Recarga el valor actualizado desde la DB

        if ($this->stock_actual <= $this->stock_minimo) {
            $this->notificarStockBajo();
        }
    }

    /**
     * Notifica a los usuarios pertinentes sin generar notificaciones duplicadas.
     */
    public function notificarStockBajo(): void
    {
        // 📌 Filtra los usuarios a notificar.
        // Si tienes una columna 'role' en User, puedes filtrar por ej: User::whereIn('role', ['admin', 'encargado'])->get();
        $usuariosANotificar = User::all();

        foreach ($usuariosANotificar as $user) {
            // Evita duplicar alertas en la bandeja sin leer para el mismo insumo
            $yaNotificado = $user->unreadNotifications()
                ->where('data->insumo_id', $this->id)
                ->exists();

            if (!$yaNotificado) {
                $user->notify(new StockBajoNotification($this));
            }
        }
    }
}