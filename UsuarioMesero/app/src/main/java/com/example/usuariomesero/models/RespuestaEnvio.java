package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class RespuestaEnvio {
    @SerializedName("status")
    private String status;

    @SerializedName("mensaje")
    private String mensaje;

    // Aquí le decimos que "data" es un objeto, no una lista
    @SerializedName("data")
    private DatosPedido data;

    public class DatosPedido {
        @SerializedName("pedido_id")
        public int pedidoId;

        @SerializedName("total")
        public double total;
    }

    public String getStatus() { return status; }
    public String getMensaje() { return mensaje; }
    public DatosPedido getData() { return data; }
}