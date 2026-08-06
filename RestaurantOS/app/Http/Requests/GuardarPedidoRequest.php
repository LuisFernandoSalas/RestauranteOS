<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class GuardarPedidoRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; // El middleware de Sanctum ya controla la sesión activa
    }

   public function rules(): array
    {
        return [
            'client_uuid'            => 'required|string',
            'mesa_id'                => 'required|exists:mesas,id',
            
            // Permite validar arreglo como 'items' o 'productos'
            'items'                  => 'required_without:productos|array',
            'productos'              => 'required_without:items|array',
            
            'items.*.producto_id'    => 'nullable|exists:productos,id',
            'items.*.combo_id'       => 'nullable|exists:combos,id',
            'items.*.cantidad'       => 'required|integer|min:1',
            'items.*.nota'           => 'nullable|string',
        ];
    }
}