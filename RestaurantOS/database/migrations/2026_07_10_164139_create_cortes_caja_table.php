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
        Schema::create('cortes_caja', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained(); // El cajero que cierra
            $table->decimal('fondo_apertura', 10, 2);
            $table->decimal('fondo_siguiente_turno', 10, 2);
            $table->decimal('ventas_sistema_efectivo', 10, 2);
            $table->decimal('efectivo_real', 10, 2); // Lo que el cajero contó
            $table->decimal('diferencia', 10, 2)->default(0); // Automático: Real - Sistema

            // Guardamos un JSON con el desglose (Efectivo: X, Tarjeta: Y, Mixto: Z)
            $table->json('desglose_metodos_pago')->nullable();

            $table->decimal('total_entregado', 10, 2);
            $table->text('observaciones')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('cortes_caja');
    }
};
