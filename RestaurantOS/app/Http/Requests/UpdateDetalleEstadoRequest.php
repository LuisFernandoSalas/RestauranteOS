<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateDetalleEstadoRequest extends FormRequest
{
    public function authorize(): bool { return true; }

    public function rules(): array
    {
        return [
            'estado' => [
                'required',
                'string',
                Rule::in(['pendiente', 'en_preparacion', 'pausado', 'listo', 'entregado', 'cancelado'])
            ]
        ];
    }

    /**
     * Mensajes de error personalizados.
     */
    public function messages(): array
    {
        return [
            'estado.required' => 'El nuevo estado del platillo es obligatorio.',
            'estado.in' => 'El estado proporcionado no es válido para el flujo de cocina.',
        ];
    }
}