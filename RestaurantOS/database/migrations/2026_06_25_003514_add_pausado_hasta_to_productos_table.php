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
        // Solo agrega la columna SI NO existe ya
        if (!Schema::hasColumn('productos', 'pausado_hasta')) {
            Schema::table('productos', function (Blueprint $table) {
                $table->timestamp('pausado_hasta')->nullable()->after('status');
            });
        }
    }

    public function down(): void
    {
        Schema::table('productos', function (Blueprint $table) {
            $table->dropColumn('pausado_hasta');
        });
    }
};
