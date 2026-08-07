<x-mail::message>
# ¡Gracias por su compra!

Estimado(a) **{{ $factura->razon_social }}**,

Adjunto a este correo encontrará los comprobantes fiscales (PDF y XML) correspondientes a su consumo de la comanda **#{{ $factura->pedido_id }}**.

### Detalle de la Factura:
* **RFC:** {{ $factura->rfc }}
* **Uso CFDI:** {{ $factura->uso_cfdi }}
* **Monto Total:** ${{ number_format($factura->monto_total, 2) }}
* **Folio Fiscal (UUID):** {{ $factura->uuid_fiscal }}

Si tiene cualquier duda sobre su facturación, no dude en responder a este correo.

Atentamente,<br>
{{ config('app.name') }}
</x-mail::message>