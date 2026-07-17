package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class Producto {
    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("precio")
    private double precio;

    @SerializedName("categoria_id")
    private int categoriaId;

    @SerializedName("estado")
    private String status;

    // Guardamos una imagen por defecto temporal ya que no viene de la base de datos
    private int imagenResId;
    private Categoria categoria; // 🚀 El nuevo objeto que manda Laravel

    public Producto(int id, String nombre, double precio, int categoriaId, String status) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoriaId = categoriaId;
        this.status = status;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCategoriaId() { return categoriaId; }
    public String getStatus() { return status; }

    // Le asignamos un icono de comida genérico de tus drawables para que no salga vacío
    public int getImagenResId() {
        // Aquí puedes poner un drawable genérico tuyo (ej. R.drawable.ic_food o un switch por categoriaId)
        return imagenResId;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setImagenResId(int imagenResId) {
        this.imagenResId = imagenResId;
    }
}