<?php

namespace App\Notifications;

use App\Models\Insumo;
use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Notification;

class StockBajoNotification extends Notification
{
    use Queueable;

    public Insumo $insumo;

    public function __construct(Insumo $insumo)
    {
        $this->insumo = $insumo;
    }

    public function via(object $notifiable): array
    {
        return ['database']; // Se guarda en la base de datos para consumirlo por API
    }

    public function toArray(object $notifiable): array
    {
        return [
            'tipo'         => 'stock_bajo',
            'insumo_id'    => $this->insumo->id,
            'insumo_nombre'=> $this->insumo->nombre,
            'stock_actual' => $this->insumo->stock_actual,
            'stock_minimo' => $this->insumo->stock_minimo,
            'unidad_medida'=> $this->insumo->unidad_medida ?? 'unidades',
            'mensaje'      => "El insumo '{$this->insumo->nombre}' ha alcanzado el nivel crítico de stock ({$this->insumo->stock_actual} restantes).",
        ];
    }
}