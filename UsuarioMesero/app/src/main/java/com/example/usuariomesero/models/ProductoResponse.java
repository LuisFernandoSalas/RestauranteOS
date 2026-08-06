package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductoResponse {

    @SerializedName("success")
    private String success;

    @SerializedName("data")
    private List<Producto> data;

    public String getSuccess() {
        return success;
    }

    public List<Producto> getData() {
        return data;
    }
}