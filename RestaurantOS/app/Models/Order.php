<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Order extends Model
{
    //para permitir asignacion masiva de datos
    protected $fillable = [
        'total_amount', 
        'tip_amount', 
        'payment_method'];
}
