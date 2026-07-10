package com.example.usuariomesero.models;

public class ItemOrdenRequest {
    private int producto_id;
    private int cantidad;
    private String nota;

    public ItemOrdenRequest(int producto_id, int cantidad, String nota) {
        this.producto_id = producto_id;
        this.cantidad = cantidad;
        this.nota = nota;
    }
}