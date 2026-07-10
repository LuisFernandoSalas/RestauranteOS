<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CobrarPedidoRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true; 
    }

    public function rules(): array
    {
        return [
            'client_uuid'           => 'required|uuid',
            'propina'               => 'required|numeric|min:0',
            'requiere_factura'      => 'required|boolean',
            'pagos'                 => 'required|array|min:1',
            'pagos.*.metodo_pago'   => 'required|in:efectivo,tarjeta,transferencia',
            'pagos.*.monto_recibido'=> 'required|numeric|gt:0',
        ];
    }
}