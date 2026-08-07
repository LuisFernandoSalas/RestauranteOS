<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Factura extends Model
{
    use HasFactory;

    protected $fillable = [
        'pedido_id',
        'rfc',
        'razon_social',
        'uso_cfdi',
        'regimen_fiscal',
        'email',
        'uuid_fiscal',
        'pdf_url',
        'xml_url',
        'monto_total',
        'estado',
    ];

    public function pedido()
    {
        return $this->belongsTo(Pedido::class);
    }
}