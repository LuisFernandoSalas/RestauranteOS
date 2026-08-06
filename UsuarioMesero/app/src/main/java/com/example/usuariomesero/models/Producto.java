package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class Producto {

    @SerializedName("id")
    private int id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("precio")
    private double precio;

    // 👇 Mapea el objeto JSON de la categoría de Laravel
    @SerializedName("categoria")
    private CategoriaObjeto categoria;

    // Clase interna para leer la estructura {"id": 2, "nombre": "..."}
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

    // Retorna el nombre directo de la categoría para que la Activity no falle
    public String getCategoria() {
        if (categoria != null && categoria.getNombre() != null) {
            return categoria.getNombre();
        }
        return "";
    }
}