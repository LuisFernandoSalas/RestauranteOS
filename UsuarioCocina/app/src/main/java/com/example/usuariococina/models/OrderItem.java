package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

<<<<<<< HEAD
import java.io.Serializable;

public class OrderItem implements Serializable {

    @SerializedName("detalle_id")
    private int id;

    // Laravel manda un texto directo ("producto": "Enchiladas verdes")
    @SerializedName("producto")
    private String name;
=======
public class OrderItem {

    // Laravel manda un objeto "producto" adentro del detalle
    @SerializedName("producto")
    private Producto producto;
>>>>>>> d9dcc2e (feat: implement full API audit and security testing (RBAC, WebSockets, cash-out, and inventory))

    @SerializedName("cantidad")
    private int quantity;

    @SerializedName("nota")
    private String note;

<<<<<<< HEAD
    @SerializedName("estado_platillo")
    private String estado;

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // --- Getters ---

    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado != null ? estado : "pendiente";
    }

    public String getName() {
        return name != null ? name : "Producto sin nombre";
=======
    // Clase anidada para leer el producto
    public static class Producto {
        @SerializedName("nombre")
        public String nombre;
    }

    // --- Tus Getters originales ---

    public String getName() {
        return (producto != null && producto.nombre != null) ? producto.nombre : "Producto sin nombre";
>>>>>>> d9dcc2e (feat: implement full API audit and security testing (RBAC, WebSockets, cash-out, and inventory))
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }
}