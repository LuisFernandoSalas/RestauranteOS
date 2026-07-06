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
    public $pedido;
    /**
     * Create a new event instance.
     */
    public function __construct(Pedido $pedido)
    {
        $this->pedido = $pedido;
    }

    /**
     * en que canal transmitimos?
     */
    public function broadcastOn(): array
    {
        //los meseros y la caja estaran sintonizados en este canal
        return [
            new Channel('meseros-canal'),
        ];
    }
    /**
     * nombre del evento que leera java
     */
    public function broadcastAs(): string
    {
        return 'pedido-actualizado';
    }
}
