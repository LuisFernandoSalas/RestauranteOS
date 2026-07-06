package com.example.usuariococina.models;

/**
 * Modelo de datos que representa un producto individual dentro de una comanda.
 * Contiene la información necesaria para que cocina sepa qué preparar y bajo qué condiciones.
 */
public class OrderItem {
    // Nombre del plato o bebida (ej: "Pozole rojo")
    private String name;
    
    // Cantidad solicitada por el cliente
    private int quantity;
    
    // Instrucciones especiales (ej: "sin cebolla", "bien cocido")
    private String note;

    public OrderItem(String name, int quantity, String note) {
        this.name = name;
        this.quantity = quantity;
        this.note = note;
    }

    // Getters estándar para acceder a la información desde los adaptadores o actividades
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }
}