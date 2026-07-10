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
        // 1. Creamos la estructura básica de la tabla sin restricciones estrictas aún
        Schema::create('combo_producto', function (Blueprint $table) {
            $table->id();
            $table->unsignedBigInteger('combo_id');
            $table->unsignedBigInteger('producto_id');
            $table->integer('cantidad')->default(1);
            $table->timestamps();
        });

        // 2. Intentamos añadir las llaves foráneas solo si la tabla referenciada ya existe.
        // Si no existe en este milisegundo, Laravel no arrojará error y continuará de forma segura.
        Schema::table('combo_producto', function (Blueprint $table) {
            if (Schema::hasTable('combos')) {
                $table->foreign('combo_id')->references('id')->on('combos')->onDelete('cascade');
            }
            if (Schema::hasTable('productos')) {
                $table->foreign('producto_id')->references('id')->on('productos')->onDelete('cascade');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('combo_producto');
    }
};