package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

public class DetallePedido {

    @SerializedName("detalle_id")
    private int id;

    @SerializedName("cantidad")
    private int cantidad;

    @SerializedName("nota")
    private String nota;

    @SerializedName("estado_platillo")
    private String estado;

    @SerializedName("producto")
    private String producto;

    private Combo combo;
    private boolean isListo;

    // --- GETTERS ---
    public int getId() { return id; }

    // Agregamos este alias para resolver el error de compilación
    public int getDetalleId() { return id; }

    public int getCantidad() { return cantidad; }
    public String getNota() { return nota; }
    public String getEstado() { return estado; }
    public String getProducto() { return producto; }
    public Combo getCombo() { return combo; }

    public boolean isListo() {
        return isListo;
    }

    public void setListo(boolean listo) {
        this.isListo = listo;
    }
}