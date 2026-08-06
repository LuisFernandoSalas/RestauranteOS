package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("user")
    private User user;

    @SerializedName("access_token")
    private String token; // El Bearer token que Sanctum genera

    // 👇 ESTOS SON LOS MÉTODOS QUE JAVA ESTABA BUSCANDO 👇

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}