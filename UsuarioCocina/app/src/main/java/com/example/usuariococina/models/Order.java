package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;
<<<<<<< HEAD

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {

    @SerializedName("pedido_id")
    private int id;

    // Laravel manda un número directo ("mesa": 1)
    @SerializedName("mesa")
    private int mesa;

    // Laravel manda un texto directo ("mesero": "Hasiel")
    @SerializedName("mesero")
    private String mesero;

    @SerializedName("estado_general")
    private String status;

    @SerializedName("platillos")
    private List<OrderItem> items;


    // --- Getters ---

    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return mesa;
    }

    public String getWaiterName() {
        return mesero != null ? mesero : "Desconocido";
=======
import java.util.List;

public class Order {

    // Laravel manda un objeto "mesa"
    @SerializedName("mesa")
    private Mesa mesa;

    // Laravel manda un objeto "mesero"
    @SerializedName("mesero")
    private Mesero mesero;

    // Laravel manda un arreglo "detalles"
    @SerializedName("detalles")
    private List<OrderItem> items;

    // Laravel manda "estado"
    @SerializedName("estado")
    private String status;

    // Clases anidadas para leer los objetos de Laravel
    public static class Mesa {
        @SerializedName("numero")
        public int numero;
    }

    public static class Mesero {
        @SerializedName("name")
        public String name;
    }

    // --- Mantenemos tus Getters originales para que la UI no se entere del cambio ---

    public int getTableNumber() {
        return (mesa != null) ? mesa.numero : 0;
    }

    public String getWaiterName() {
        return (mesero != null && mesero.name != null) ? mesero.name : "Desconocido";
>>>>>>> d9dcc2e (feat: implement full API audit and security testing (RBAC, WebSockets, cash-out, and inventory))
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getStatus() {
        return status != null ? status.toUpperCase() : "DESCONOCIDO";
    }
}