<?php

use Illuminate\Support\Facades\Broadcast;

// Canal público o privado para notificar cambios en los pedidos
Broadcast::channel('pedidos', function ($user) {
    // Retorna true si el usuario tiene un rol autorizado (Cajero, Mesero, Cocinero, Admin)
    return $user !== null; 
});