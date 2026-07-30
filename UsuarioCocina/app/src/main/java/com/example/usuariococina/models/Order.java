package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

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
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getStatus() {
        return status != null ? status.toUpperCase() : "DESCONOCIDO";
    }
}