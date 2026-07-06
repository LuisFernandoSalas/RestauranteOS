<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Mesa extends Model
{
    use HasFactory;
    protected $table = 'mesas';
    protected $fillable = 
        [
            'client_uuid', 
            'mesa_id', 
            'user_id', 
            'status', 
            'total'
        ];
        public function pedidos(): HasMany
        {
            return $this->hasMany(Pedido::class);
        }
        public function scopeDisponible($query)
        {
            return $query->where('status', 'disponible');
        }
        // Define la relación: Un pedido pertenece a un usuario (mesero)
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    // Define la relación: Un pedido pertenece a una mesa
    public function mesa()
    {
        return $this->belongsTo(Mesa::class);
    }
    //
}
