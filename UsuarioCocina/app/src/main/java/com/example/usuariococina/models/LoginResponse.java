package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    // Aquí le decimos a Gson el nombre exacto que manda Laravel
    @SerializedName("access_token")
    private String token;

    private Usuario user;

    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
}