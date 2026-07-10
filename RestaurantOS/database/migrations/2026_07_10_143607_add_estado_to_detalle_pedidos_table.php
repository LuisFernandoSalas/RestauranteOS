<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{

    protected $tabla = 'detalle_pedido'; 

    public function up()
    {
        // Solo intenta alterar si la tabla realmente existe
        if (Schema::hasTable($this->tabla)) {
            if (!Schema::hasColumn($this->tabla, 'estado')) {
                Schema::table($this->tabla, function (Blueprint $table) {
                    // Estado independiente por cada platillo para el KDS de Cocina
                    $table->enum('estado', ['pendiente', 'en_preparacion', 'listo', 'entregado', 'cancelado'])->default('pendiente')->after('cantidad');
                });
            }
        } else {
            // Falla intencionalmente con un mensaje claro si te equivocas de nombre
            throw new \Exception("Arquitectura: La tabla '{$this->tabla}' no existe en tu BD. Verifica el nombre.");
        }
    }

    public function down()
    {
        if (Schema::hasTable($this->tabla) && Schema::hasColumn($this->tabla, 'estado')) {
            Schema::table($this->tabla, function (Blueprint $table) {
                $table->dropColumn('estado');
            });
        }
    }
};
