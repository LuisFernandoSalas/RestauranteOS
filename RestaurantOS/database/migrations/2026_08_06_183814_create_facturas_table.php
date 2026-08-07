<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('facturas', function (Blueprint $table) {
            $table->id();
            $table->foreignId('pedido_id')->constrained('pedidos')->onDelete('cascade');
            $table->string('rfc', 15);
            $table->string('razon_social');
            $table->string('uso_cfdi')->default('G03');
            $table->string('regimen_fiscal')->nullable();
            $table->string('email');
            $table->string('uuid_fiscal')->nullable()->unique(); // Folio Fiscal / UUID del PAC
            $table->string('pdf_url')->nullable();
            $table->string('xml_url')->nullable();
            $table->decimal('monto_total', 10, 2);
            $table->string('estado')->default('generada'); // generada, cancelada
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('facturas');
    }
};
