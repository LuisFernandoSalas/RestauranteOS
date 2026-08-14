package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class Producto {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("precio")
    private double precio;

    @SerializedName("categoria")
    private CategoriaObjeto categoria;

    public Producto() {
    }

    public static class CategoriaObjeto {
        @SerializedName("id")
        private int id;

        @SerializedName("nombre")
        private String nombre;

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public String getCategoria() {
        if (categoria != null && categoria.getNombre() != null) {
            return categoria.getNombre();
        }
        return "";
    }
}