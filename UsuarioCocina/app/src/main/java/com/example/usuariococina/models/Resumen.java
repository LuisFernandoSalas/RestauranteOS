package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

public class Resumen {

    @SerializedName("total_comandas")
    private int totalComandas;

    @SerializedName("en_preparacion")
    private int enPreparacion;

    @SerializedName("pausados")
    private int pausados;

    @SerializedName("pendientes")
    private int pendientes;

    // --- GETTERS ---
    public int getTotalComandas() { return totalComandas; }
    public int getEnPreparacion() { return enPreparacion; }
    public int getPausados() { return pausados; }
    public int getPendientes() { return pendientes; }
}