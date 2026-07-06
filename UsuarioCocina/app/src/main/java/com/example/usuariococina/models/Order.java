package com.example.usuariococina.models;

import java.util.List;

/**
 * Representa una comanda o pedido completo asignado a una mesa.
 * Agrupa varios productos (OrderItem) y mantiene el estado general de la atención.
 */
public class Order {
    // Número identificador de la mesa física
    private int tableNumber;

    // Nombre del mesero encargado de la orden
    private String waiterName;

    // Lista de platos y bebidas incluidos en este pedido
    private List<OrderItem> items;

    // Estado actual de la orden (ej: "PENDIENTE", "EN PREPARACIÓN", "LISTO")
    private String status;

    public Order(int tableNumber, String waiterName, List<OrderItem> items, String status) {
        this.tableNumber = tableNumber;
        this.waiterName = waiterName;
        this.items = items;
        this.status = status;
    }

    // Getters para consulta de datos desde la UI y adaptadores
    public int getTableNumber() { return tableNumber; }
    public String getWaiterName() { return waiterName; }
    public List<OrderItem> getItems() { return items; }
    public String getStatus() { return status; }
}