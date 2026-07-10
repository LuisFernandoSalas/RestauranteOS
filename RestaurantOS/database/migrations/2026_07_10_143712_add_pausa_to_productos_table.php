<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        // Solo agrega la columna si NO existe previamente
        if (!Schema::hasColumn('productos', 'pausado_hasta')) {
            Schema::table('productos', function (Blueprint $table) {
                // Soporta la función de "Pausar por 30 min" o "Pausar indefinidamente"
                $table->timestamp('pausado_hasta')->nullable()->after('precio');
            });
        }
    }

    public function down()
    {
        if (Schema::hasColumn('productos', 'pausado_hasta')) {
            Schema::table('productos', function (Blueprint $table) {
                $table->dropColumn('pausado_hasta');
            });
        }
    }
};