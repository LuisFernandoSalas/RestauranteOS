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
            'client_uuid'          => 'required|uuid',
            'mesa_id'              => 'required|exists:mesas,id',
            'productos'            => 'required|array|min:1',
            'productos.*.id'       => 'required|exists:productos,id',
            'productos.*.cantidad' => 'required|integer|min:1',
            'productos.*.nota'     => 'nullable|string|max:255',
        ];
    }
}