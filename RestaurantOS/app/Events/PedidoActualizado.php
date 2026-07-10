<?php

namespace App\Events;

use App\Models\Pedido;
use Illuminate\Broadcasting\Channel;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Contracts\Broadcasting\ShouldBroadcast;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class PedidoActualizado implements ShouldBroadcast
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    // El JSON completo y fresco que viajará por el WebSocket
    public $pedido;

    /**
     * El constructor recibe el pedido modificado (por caja, cocina o mesero)
     */
    public function __construct(Pedido $pedido)
    {
        // 🚀 Cargamos los datos limpios y frescos junto con sus relaciones 
        // para que la interfaz que reciba el evento dibuje los cambios al instante
        $this->pedido = $pedido->load([
            'mesero:id,name', 
            'mesa:id,numero', 
            'detalles.producto:id,nombre'
        ]);
    }

    /**
     * Canal unificado de transmisión
     */
    public function broadcastOn(): array
    {
        // Sintonizado en la misma frecuencia general de todo RestaurantOS
        return [
            new Channel('pedidos'),
        ];
    }

    /**
     * Nombre del evento estándar que leerá tu código en Java / Android
     */
    public function broadcastAs(): string
    {
        return 'pedido.actualizado';
    }
}