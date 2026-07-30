package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

public class OrderItem {

    // Laravel manda un objeto "producto" adentro del detalle
    @SerializedName("producto")
    private Producto producto;

    @SerializedName("cantidad")
    private int quantity;

    @SerializedName("nota")
    private String note;

    // Clase anidada para leer el producto
    public static class Producto {
        @SerializedName("nombre")
        public String nombre;
    }

    // --- Tus Getters originales ---

    public String getName() {
        return (producto != null && producto.nombre != null) ? producto.nombre : "Producto sin nombre";
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }
}