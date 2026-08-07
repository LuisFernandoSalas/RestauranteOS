<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Pedido;
use App\Models\Factura;
use App\Mail\FacturaCreadaMail;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Mail;

class FacturacionController extends Controller
{
    /**
     * Generar documento fiscal y enviarlo por correo
     */
    public function generar(Request $request)
    {
        $validated = $request->validate([
            'pedido_id'      => 'required|exists:pedidos,id',
            'rfc'            => 'required|string|min:12|max:13',
            'razon_social'   => 'required|string|max:255',
            'uso_cfdi'       => 'nullable|string',
            'regimen_fiscal' => 'nullable|string',
            'email'          => 'required|email',
        ]);

        $pedido = Pedido::with('detalles.producto')->findOrFail($validated['pedido_id']);

        if ($pedido->estado !== 'pagado') {
            return response()->json([
                'status'  => 'error',
                'message' => 'Solo se pueden facturar comandas que se encuentren en estado pagado.'
            ], 422);
        }

        if (Factura::where('pedido_id', $pedido->id)->exists()) {
            return response()->json([
                'status'  => 'error',
                'message' => 'Esta comanda ya ha sido facturada previamente.'
            ], 400);
        }

        // 1. Simulación de UUID Fiscal de PAC
        $uuidFiscal = (string) Str::uuid();

        // 2. Generar archivos simulados PDF y XML en storage/app/public/facturas
        $pdfContent = "%PDF-1.4 Mock PDF Invoice for Pedido #{$pedido->id} | RFC: {$validated['rfc']}";
        $xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cfdi:Comprobante UUID=\"{$uuidFiscal}\" Total=\"{$pedido->total}\"/>";

        Storage::disk('public')->put("facturas/{$uuidFiscal}.pdf", $pdfContent);
        Storage::disk('public')->put("facturas/{$uuidFiscal}.xml", $xmlContent);

        // 3. Registrar Factura
        $factura = Factura::create([
            'pedido_id'      => $pedido->id,
            'rfc'            => strtoupper($validated['rfc']),
            'razon_social'   => $validated['razon_social'],
            'uso_cfdi'       => $validated['uso_cfdi'] ?? 'G03',
            'regimen_fiscal' => $validated['regimen_fiscal'] ?? '601',
            'email'          => $validated['email'],
            'uuid_fiscal'    => $uuidFiscal,
            'pdf_url' => asset("storage/facturas/{$uuidFiscal}.pdf"),
            'xml_url' => asset("storage/facturas/{$uuidFiscal}.xml"),
            'monto_total'    => $pedido->total,
            'estado'         => 'generada'
        ]);

        // 4. Envío de correo automático
        Mail::to($factura->email)->send(new FacturaCreadaMail($factura));

        return response()->json([
            'status'  => 'success',
            'mensaje' => 'Factura generada y enviada por correo exitosamente.',
            'data'    => $factura
        ], 201);
    }
}