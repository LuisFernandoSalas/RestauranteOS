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
        if (!Schema::hasTable('insumos')) {
            Schema::create('insumos', function (Blueprint $table) {
                $table->id();
                $table->string('nombre');
                $table->string('categoria')->nullable(); // Verdura, Lácteo, Carne, etc.
                $table->decimal('stock_actual', 10, 2)->default(0);
                $table->decimal('stock_minimo', 10, 2)->default(5); // Alerta roja en dashboard
                $table->string('unidad_medida'); // kg, g, pza, lt
                $table->timestamps();
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('insumos');
    }
};
