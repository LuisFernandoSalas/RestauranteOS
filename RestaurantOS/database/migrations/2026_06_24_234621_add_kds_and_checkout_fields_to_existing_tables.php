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
        // 1. Campos adicionales para soportar Caja, Propinas y Facturación en la tabla 'pedidos'
        Schema::table('pedidos', function (Blueprint $table) {
            if (!Schema::hasColumn('pedidos', 'propina')) {
                $table->decimal('propina', 8, 2)->default(0.00)->after('metodo_pago');
            }
            if (!Schema::hasColumn('pedidos', 'requiere_factura')) {
                $table->boolean('requiere_factura')->default(false)->after('propina');
            }
            if (!Schema::hasColumn('pedidos', 'pago_efectivo')) {
                $table->decimal('pago_efectivo', 8, 2)->default(0.00)->after('requiere_factura');
            }
            if (!Schema::hasColumn('pedidos', 'pago_tarjeta')) {
                $table->decimal('pago_tarjeta', 8, 2)->default(0.00)->after('pago_efectivo');
            }
            if (!Schema::hasColumn('pedidos', 'motivo_cancelacion')) {
                $table->string('motivo_cancelacion')->nullable()->after('status');
            }
        });

        // 2. Campos para soportar el KDS (Cocina) platillo por platillo en 'detalles_pedido'
        Schema::table('detalle_pedido', function (Blueprint $table) {
            if (!Schema::hasColumn('detalle_pedido', 'nota')) {
                $table->string('nota')->nullable()->after('cantidad'); // Ej: "sin cebolla, extra chile"
            }
            if (!Schema::hasColumn('detalle_pedido', 'status')) {
                $table->enum('status', ['pendiente', 'en_preparacion', 'listo'])->default('pendiente')->after('nota');
            }
        });

        // 3. Campos para soportar la pausa temporal de insumos/menú en la tabla 'productos'
        Schema::table('productos', function (Blueprint $table) {
            if (!Schema::hasColumn('productos', 'categoria')) {
                $table->enum('categoria', ['entradas', 'platos', 'bebidas', 'postres'])->default('platos')->after('nombre');
            }
            if (!Schema::hasColumn('productos', 'pausado_hasta')) {
                $table->timestamp('pausado_hasta')->nullable()->after('precio'); // Control de 30 min, 1 hora, etc.
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('pedidos', function (Blueprint $table) {
            $table->dropColumn(['propina', 'requiere_factura', 'pago_efectivo', 'pago_tarjeta', 'motivo_cancelacion']);
        });

        Schema::table('detalle_pedido', function (Blueprint $table) {
            $table->dropColumn(['nota', 'status']);
        });

        Schema::table('productos', function (Blueprint $table) {
            $table->dropColumn(['categoria', 'pausado_hasta']);
        });
    }
};
