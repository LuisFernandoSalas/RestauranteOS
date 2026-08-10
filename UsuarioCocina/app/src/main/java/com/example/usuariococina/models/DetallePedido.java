package com.example.usuariococina.models;

public class DetallePedido {
    private int id;
    private int cantidad;
    private String nota;
    private String estado;
    private String producto;
    private Combo combo;
    private boolean isListo;

    // Getters originales
    public int getId() { return id; }
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