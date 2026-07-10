package com.example.usuariomesero.api;

import com.example.usuariomesero.models.Producto;

import java.util.List;

public class ApiResponse {
    private boolean success;
    private List<Producto> data;

    public boolean isSuccess() { return success; }
    public List<Producto> getData() { return data; }
}