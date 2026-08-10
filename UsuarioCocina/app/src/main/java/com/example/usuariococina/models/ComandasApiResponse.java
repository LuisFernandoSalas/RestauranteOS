package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ComandasApiResponse {

    @SerializedName("status")
    private String status;

    // Aquí le decimos a Gson que busque el arreglo llamado "comandas"
    // y lo meta en tu modelo de lista
    @SerializedName("comandas")
    private List<PedidoResponse> comandas;

    public String getStatus() {
        return status;
    }

    public List<PedidoResponse> getComandas() {
        return comandas;
    }
}