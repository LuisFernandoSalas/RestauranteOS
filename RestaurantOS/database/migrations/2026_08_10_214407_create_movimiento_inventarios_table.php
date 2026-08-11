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
        Schema::create('movimientos_inventario', function (Blueprint $table) {
            $table->id();
            $table->foreignId('insumo_id')->constrained('insumos')->cascadeOnDelete();
            $table->foreignId('user_id')->nullable()->constrained('users')->nullOnDelete();
            
            // Tipos: ENTRADA (reabasto), MERMA (desperdicio), SALIDA_VENTA (descuento automático), AJUSTE (corrección)
            $table->enum('tipo', ['ENTRADA', 'MERMA', 'SALIDA_VENTA', 'AJUSTE']);
            
            $table->decimal('cantidad', 10, 3);
            $table->decimal('stock_resultante', 10, 3);
            $table->string('motivo')->nullable(); // Ej: "Tomates echados a perder", "Caja chica proveedor"
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('movimientos_inventario');
    }
};