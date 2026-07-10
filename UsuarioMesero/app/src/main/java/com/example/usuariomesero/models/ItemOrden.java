package com.example.usuariomesero.models;

/**
 * Representa una línea dentro de un pedido.
 * Vincula un producto con la cantidad solicitada y notas especiales.
 */
public class ItemOrden {
    private Producto producto;
    private int cantidad;
    private String nota = "";

    /**
     * @param producto El producto a agregar.
     * @param cantidad Cantidad inicial.
     */
    public ItemOrden(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
