package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CocinaResponse {
    private String status;

    // Aquí le decimos que la lista viene dentro de la etiqueta "comandas"
    @SerializedName("comandas")
    private List<Order> pedidos;

    public String getStatus() { return status; }
    public List<Order> getPedidos() { return pedidos; }
}