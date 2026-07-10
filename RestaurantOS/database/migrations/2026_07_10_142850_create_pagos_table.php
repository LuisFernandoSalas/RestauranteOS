<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up()
    {
        Schema::create('pagos', function (Blueprint $table) {
            $table->id();
            
            // Restrict: No puedes borrar un pedido o usuario si ya tiene pagos registrados
            $table->foreignId('pedido_id')->constrained('pedidos')->onDelete('restrict');
            $table->foreignId('cobrado_por')->constrained('users')->comment('Usuario que procesó el cobro')->onDelete('restrict');

            // 🚀 Estandarizado para soportar 'mixto' como piden tus slides de cuentas divididas
            $table->enum('metodo_pago', ['efectivo', 'tarjeta', 'transferencia', 'mixto'])->default('efectivo');
            
            // DECIMAL es obligatorio para dinero, no uses FLOAT
            $table->decimal('monto_recibido', 10, 2);
            $table->decimal('propina', 10, 2)->default(0);
            $table->boolean('requiere_factura')->default(false);
            
            $table->timestamps();
            $table->softDeletes();
        });
    }

    public function down()
    {
        Schema::dropIfExists('pagos');
    }
};
