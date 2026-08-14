package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Mesa {

    public enum Estado {
        @SerializedName("libre") LIBRE,
        @SerializedName("ocupada") OCUPADA,
        @SerializedName("cobro") COBRO
    }

    @SerializedName("numero")
    private int numero;

    @SerializedName("estado")
    private Estado estado;

    @SerializedName("total_actual")
    private String subtotal;

    @SerializedName("items_pedido")
    private List<ItemOrden> itemsPedido;

    @SerializedName("nombre_informacion")
    private String nombreInformacion;

    // 🆕 NUEVOS CAMPOS DESDE LARAVEL
    @SerializedName("pedido_id")
    private int pedidoId;

    @SerializedName("estado_pedido")
    private String estadoPedido; // "pendiente", "en_preparacion", "listo", "entregado"

    public Mesa(int numero, Estado estado, String precio) {
        this(numero, estado, precio, null, null);
    }

    public Mesa(int numero, Estado estado, String precio, List<ItemOrden> itemsPedido, String nombreInformacion) {
        this.numero = numero;
        this.estado = estado;
        this.subtotal = precio;
        this.itemsPedido = itemsPedido;
        this.nombreInformacion = nombreInformacion;
    }

    // --- GETTERS Y SETTERS ---
    public int getNumero() { return numero; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Double getTotal() {
        if (subtotal != null && !subtotal.isEmpty()) {
            try {
                return Double.parseDouble(subtotal);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public void setPrecio(String precio) { this.subtotal = precio; }
    public List<ItemOrden> getItemsPedido() { return itemsPedido; }
    public void setItemsPedido(List<ItemOrden> itemsPedido) { this.itemsPedido = itemsPedido; }
    public String getNombreInformacion() { return nombreInformacion; }
    public void setNombreInformacion(String nombreInformacion) { this.nombreInformacion = nombreInformacion; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }
}