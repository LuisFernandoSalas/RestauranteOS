package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OrderItem implements Serializable {

    @SerializedName("detalle_id")
    private int id;

    // Laravel manda un texto directo ("producto": "Enchiladas verdes")
    @SerializedName("producto")
    private String name;

    @SerializedName("cantidad")
    private int quantity;

    @SerializedName("nota")
    private String note;

    @SerializedName("estado_platillo")
    private String estado;

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado != null ? estado : "pendiente";
    }

    public String getName() {
        return name != null ? name : "Producto sin nombre";
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }
}