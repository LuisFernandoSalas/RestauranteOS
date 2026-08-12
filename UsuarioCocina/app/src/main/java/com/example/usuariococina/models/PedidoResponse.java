package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PedidoResponse {

    @SerializedName("pedido_id")
    private int pedidoId;

    @SerializedName("mesa")
    private int mesa;

    // --- EL QUE FALTABA ---
    @SerializedName("mesero")
    private String mesero;

    @SerializedName("estado_general")
    private String estadoGeneral;

    @SerializedName("platillos")
    private List<DetallePedido> platillos;


    // --- Y SUS GETTERS ---

    public int getPedidoId() {
        return pedidoId;
    }

    public int getMesa() { return mesa; }

    public String getMesero() { return mesero; }

    public String getEstadoGeneral() { return estadoGeneral; }

    public List<DetallePedido> getPlatillos() { return platillos; }

    // (Puedes dejar los setters también si los tienes)
}