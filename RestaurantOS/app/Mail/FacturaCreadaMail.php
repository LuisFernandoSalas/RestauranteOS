<?php

namespace App\Mail;

use App\Models\Factura;
use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Attachment;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Storage;

class FacturaCreadaMail extends Mailable
{
    use Queueable, SerializesModels;

    public Factura $factura;

    public function __construct(Factura $factura)
    {
        $this->factura = $factura;
    }

    public function envelope(): Envelope
    {
        return new Envelope(
            subject: "Su Factura Electrónica - Comanda #{$this->factura->pedido_id}",
        );
    }

    public function content(): Content
    {
        return new Content(
            markdown: 'emails.facturas.creada',
        );
    }

    /**
     * Adjuntar automáticamente el PDF y XML desde Storage
     */
    public function attachments(): array
    {
        $attachments = [];
        $pdfRelativePath = "facturas/{$this->factura->uuid_fiscal}.pdf";
        $xmlRelativePath = "facturas/{$this->factura->uuid_fiscal}.xml";

        if (Storage::disk('public')->exists($pdfRelativePath)) {
            $attachments[] = Attachment::fromPath(Storage::disk('public')->path($pdfRelativePath))
                ->as("Factura_{$this->factura->uuid_fiscal}.pdf")
                ->withMime('application/pdf');
        }

        if (Storage::disk('public')->exists($xmlRelativePath)) {
            $attachments[] = Attachment::fromPath(Storage::disk('public')->path($xmlRelativePath))
                ->as("Factura_{$this->factura->uuid_fiscal}.xml")
                ->withMime('application/xml');
        }

        return $attachments;
    }
}