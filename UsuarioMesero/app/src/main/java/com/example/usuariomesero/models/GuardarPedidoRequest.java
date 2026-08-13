package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GuardarPedidoRequest {
    @SerializedName("client_uuid")
    private String clientUuid;

    @SerializedName("mesa_id")
    private int mesaId;

    @SerializedName("productos")
    private List<ProductoItem> productos;

    public GuardarPedidoRequest(String clientUuid, int mesaId, List<ProductoItem> productos) {
        this.clientUuid = clientUuid;
        this.mesaId = mesaId;
        this.productos = productos;
    }

    public static class ProductoItem {
        @SerializedName("id")
        private int id;

        @SerializedName("cantidad")
        private int cantidad;

        @SerializedName("nota")
        private String nota;

        @SerializedName("combo_id")
        private int comboId;

        public ProductoItem(int id, int cantidad, String nota, int comboId) {
            this.id = id;
            this.cantidad = cantidad;
            this.nota = nota;
            this.comboId = comboId; // Asignamos el valor
        }
    }
}