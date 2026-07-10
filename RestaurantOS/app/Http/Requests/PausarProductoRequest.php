<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class PausarProductoRequest extends FormRequest
{
    public function authorize(): bool { return true; }

    public function rules(): array
    {
        return [
            'duracion' => 'required|in:30_min,1_hora,indefinido'
        ];
    }
}