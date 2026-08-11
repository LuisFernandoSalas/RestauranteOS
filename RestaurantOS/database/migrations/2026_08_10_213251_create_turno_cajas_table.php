<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('turnos_caja', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            
            // Montos de apertura y cierre
            $table->decimal('monto_apertura', 10, 2);
            $table->decimal('monto_cierre_efectivo', 10, 2)->nullable();
            $table->decimal('monto_cierre_tarjeta', 10, 2)->nullable();
            
            // Lógica de Arqueo (Cálculos automáticos del sistema)
            $table->decimal('monto_esperado_efectivo', 10, 2)->default(0.00);
            $table->decimal('diferencia', 10, 2)->default(0.00);
            $table->decimal('monto_siguiente_turno', 10, 2)->nullable();
            
            // Estado y Tiempos
            $table->enum('estado', ['ABIERTO', 'CERRADO'])->default('ABIERTO');
            $table->text('notas')->nullable();
            $table->timestamp('opened_at');
            $table->timestamp('closed_at')->nullable();
            
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('turnos_caja');
    }
};