<?php

namespace App\Events;

use App\Models\Pedido;
use Illuminate\Broadcasting\Channel;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Contracts\Broadcasting\ShouldBroadcast;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class PedidoCreado implements ShouldBroadcast
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    // Esta variable pública es el JSON completo que viaja por el WebSocket
    public $pedido;

    /**
     * El constructor recibe el pedido recién guardado en MySQL
     */
    public function __construct(Pedido $pedido)
    {
        // 🚀 CRUCIAL: Cargamos las relaciones para que el JSON lleve los platillos, notas y datos de mesa
        $this->pedido = $pedido->load([
            'mesero:id,name', 
            'mesa:id,numero', 
            'detalles.producto:id,nombre'
        ]);
    }

    /**
     * ¿En qué canal (frecuencia) vamos a transmitir este aviso?
     */
    public function broadcastOn(): array
    {
        // Canal unificado para todo el ecosistema de pedidos de RestaurantOS
        return [
            new Channel('pedidos'),
        ];
    }

    /**
     * El alias exacto que tus aplicaciones Java / Android escucharán
     */
    public function broadcastAs(): string
    {
        return 'pedido.creado';
    }
}