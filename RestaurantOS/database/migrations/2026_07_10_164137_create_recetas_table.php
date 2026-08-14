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
        Schema::create('recetas', function (Blueprint $table) {
            $table->id();
            
            // 1. Permitir que producto_id sea opcional (nullable)
            $table->foreignId('producto_id')
                ->nullable()
                ->constrained()
                ->nullOnDelete(); // Si se borra el producto, la receta no se elimina, solo queda desvinculada

            // 2. Agregar el campo 'nombre' para identificar la receta
            $table->string('nombre')->nullable();

            $table->foreignId('insumo_id')->constrained()->onDelete('cascade');
            $table->decimal('cantidad_por_porcion', 10, 3); // Ej: 0.150 kg o g
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('recetas');
    }
};
