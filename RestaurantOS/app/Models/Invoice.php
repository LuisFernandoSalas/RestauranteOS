<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Invoice extends Model
{
    //permite asisgnacion masiva de datos
    protected $fillable = ['order_id']; 
}
