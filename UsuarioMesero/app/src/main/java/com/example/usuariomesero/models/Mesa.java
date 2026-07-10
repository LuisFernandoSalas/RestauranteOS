package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Mesa {

    // --- VARIABLES DE COMPATIBILIDAD (Para no romper tu Activity) ---
    private List<ItemOrden> itemsPedido;
    private String nombreInformacion;

    // 🚀 El traductor @SerializedName conecta el JSON de Laravel con nuestras variables

    @SerializedName("id_mesa")
    private int id; // ID de la base de datos

    @SerializedName("numero")
    private int numero;

    @SerializedName("estado")
    private String estadoString; // Laravel manda "libre", "ocupado", "cobro"

    @SerializedName("total")
    private double total; // Cambiamos el String de precio por el número real

    @SerializedName("tiempo_min")
    private int tiempoMin;

    // TODO: Después añadiremos la lista de productos real
    // private List<ItemOrden> itemsPedido;

    // --- GETTERS ---

    public int getId() { return id; }

    public int getNumero() { return numero; }

    // Mantenemos tu Enum para que tu Adapter siga funcionando igual
    public enum Estado { LIBRE, OCUPADA, COBRO }

    public Estado getEstado() {
        if (estadoString == null) return Estado.LIBRE;
        switch (estadoString.toLowerCase()) {
            case "ocupado": return Estado.OCUPADA;
            case "cobro": return Estado.COBRO;
            default: return Estado.LIBRE;
        }
    }

    public String getPrecioFormateado() {
        if (total <= 0) return null;
        return String.format("$ %.2f", total);
    }


    // --- SETTERS ---

    public void setEstado(Estado estado) {
        if (estado == Estado.LIBRE) this.estadoString = "libre";
        else if (estado == Estado.OCUPADA) this.estadoString = "ocupado";
        else if (estado == Estado.COBRO) this.estadoString = "cobro";
    }

    public void setPrecio(String precio) {
        try {
            if (precio != null && !precio.isEmpty()) {
                // Le quitamos el "$" y las comas para que Java lo guarde como número (double)
                String valorPuro = precio.replace("$", "").replace(",", "").trim();
                this.total = Double.parseDouble(valorPuro);
            } else {
                this.total = 0.0;
            }
        } catch (Exception e) {
            this.total = 0.0;
        }
    }

    public void setItemsPedido(List<ItemOrden> itemsPedido) {
        this.itemsPedido = itemsPedido;
    }

    public List<ItemOrden> getItemsPedido() {
        return itemsPedido;
    }

    public void setNombreInformacion(String nombreInformacion) {
        this.nombreInformacion = nombreInformacion;
    }

    // El estado del pedido que viene desde el controlador de José
    @SerializedName("pedido_status")
    private String pedidoStatus; // "Activo", "en_preparacion", "listo", "entregado"

    public boolean esCobroPermitido() {
        if (pedidoStatus == null) return false;

        // 🚀 Regla de negocio: Solo permitimos cobrar si la cocina ya lo marcó como 'listo' o si ya fue 'entregado'
        return pedidoStatus.equalsIgnoreCase("listo");
    }

    public String getNombreInformacion() {
        return nombreInformacion;
    }
}