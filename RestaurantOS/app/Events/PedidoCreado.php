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
    //esta variable publica es el json que va a viajar por el wbesocket
    public $pedido;
    /**
     * el constructor recibe el pedido recien guardado en mysql
     */
    public function __construct(Pedido $pedido)
    {
        $this->pedido = $pedido;
    }

    /**
     * en que canal (frecuencia) vamos a transmitir este aviso?
     */
    public function broadcastOn(): array
    {
        //creamos un canal publico exclusivo para que las tablets de la cocina escuchen 
        return [
            new Channel('cocina-canal'),
        ];
    }
    //opcional: con que nombre identificara java a este evento?
    public function broadcastAs(): string
    {
        return 'nuevo-pedido';
    }
}
