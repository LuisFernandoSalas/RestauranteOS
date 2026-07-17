package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemOrdenRequest {
   @ SerializedName("id")
    private int producto_id;
   @SerializedName("cantidad")
    private int cantidad;
@SerializedName("nota")
    private String nota;

    public ItemOrdenRequest(int producto_id, int cantidad, String nota) {
        this.producto_id = producto_id;
        this.cantidad = cantidad;
        this.nota = nota;
    }
}