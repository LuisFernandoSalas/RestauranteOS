<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        // 1. Estado
        if (!Schema::hasColumn('pedidos', 'estado')) {
            Schema::table('pedidos', function (Blueprint $table) {
                $table->enum('estado', ['pendiente', 'en_preparacion', 'listo', 'entregado', 'pagado', 'cancelado'])->default('pendiente')->after('id');
            });
        }

        // 2. Motivo de cancelación
        if (!Schema::hasColumn('pedidos', 'motivo_cancelacion')) {
            Schema::table('pedidos', function (Blueprint $table) {
                $table->string('motivo_cancelacion')->nullable()->after('estado');
            });
        }

        // 3. Cancelado por (Llave foránea segura)
        if (!Schema::hasColumn('pedidos', 'cancelado_por')) {
            Schema::table('pedidos', function (Blueprint $table) {
                // NOTA: Si tu tabla 'users' es muy vieja y su 'id' es INT normal (no BigInt), 
                // cambia 'unsignedBigInteger' por 'unsignedInteger' en la siguiente línea.
                $table->unsignedBigInteger('cancelado_por')->nullable()->after('motivo_cancelacion');
                $table->foreign('cancelado_por')->references('id')->on('users')->onDelete('restrict');
            });
        }

        // 4. Soft Deletes (deleted_at)
        if (!Schema::hasColumn('pedidos', 'deleted_at')) {
            Schema::table('pedidos', function (Blueprint $table) {
                $table->softDeletes();
            });
        }
    }

    public function down()
    {
        Schema::table('pedidos', function (Blueprint $table) {
            if (Schema::hasColumn('pedidos', 'cancelado_por')) {
                $table->dropForeign(['cancelado_por']);
                $table->dropColumn('cancelado_por');
            }
            if (Schema::hasColumn('pedidos', 'estado')) $table->dropColumn('estado');
            if (Schema::hasColumn('pedidos', 'motivo_cancelacion')) $table->dropColumn('motivo_cancelacion');
            if (Schema::hasColumn('pedidos', 'deleted_at')) $table->dropSoftDeletes();
        });
    }
};