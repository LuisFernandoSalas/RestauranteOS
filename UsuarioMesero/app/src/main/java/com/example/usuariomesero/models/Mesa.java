package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Clase de modelo que representa una Mesa conectada a la API.
 */
public class Mesa {

    /**
     * Estados posibles de una mesa mapeados al JSON del servidor.
     */
    public enum Estado {
        @SerializedName("libre")
        LIBRE,

        @SerializedName("ocupada")
        OCUPADA,

        @SerializedName("cobro")
        COBRO
    }

    // 🚨 Asegúrate de que los strings de @SerializedName coincidan EXACTAMENTE
    // con los nombres de las columnas o keys JSON que devuelve tu API en Laravel.

    @SerializedName("numero") // O "id", dependiendo de tu base de datos
    private int numero;

    @SerializedName("estado")
    private Estado estado;

    @SerializedName("total_actual") // Si tu API devuelve el total aquí
    private String subtotal;

    @SerializedName("items_pedido") // Ajustar si Laravel lo manda como "detalles" o "productos"
    private List<ItemOrden> itemsPedido;

    @SerializedName("nombre_informacion")
    private String nombreInformacion;

    // --- CONSTRUCTORES ---
    public Mesa(int numero, Estado estado, String precio) {
        this(numero, estado, precio, null, null);
    }

    public Mesa(int numero, Estado estado, String precio, List<ItemOrden> itemsPedido, String nombreInformacion) {
        this.numero = numero;
        this.estado = estado;
        this.subtotal = subtotal;
        this.itemsPedido = itemsPedido;
        this.nombreInformacion = nombreInformacion;
    }

    // --- GETTERS Y SETTERS ---
    public int getNumero() {
        return numero;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

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

    public void setPrecio(String precio) {
        this.subtotal = precio;
    }

    public List<ItemOrden> getItemsPedido() {
        return itemsPedido;
    }

    public void setItemsPedido(List<ItemOrden> itemsPedido) {
        this.itemsPedido = itemsPedido;
    }

    public String getNombreInformacion() {
        return nombreInformacion;
    }

    public void setNombreInformacion(String nombreInformacion) {
        this.nombreInformacion = nombreInformacion;
    }
}