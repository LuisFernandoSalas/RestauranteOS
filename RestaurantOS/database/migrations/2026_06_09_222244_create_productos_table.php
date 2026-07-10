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
        Schema::create('productos', function (Blueprint $table) {
            $table->id();
            
            // Relación con la tabla categorías
            $table->foreignId('categoria_id')->constrained('categorias')->onDelete('cascade');
            
            $table->string('nombre', 100);
            $table->text('descripcion')->nullable();
            $table->decimal('precio', 10, 2);
            
            // 🚀 Estandarizado a 'estado' en español para hacer juego con tus controladores
            $table->enum('estado', [
                'activo', 
                'pausado_desabasto', 
                'inactivo'
            ])->default('activo');

            // ⏱️ COLUMNA CRUCIAL NUEVA: Soporta el bloqueo temporal de insumos en la Cocina
            $table->timestamp('pausado_hasta')->nullable();
            
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('productos');
    }
};