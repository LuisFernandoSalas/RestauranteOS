package com.example.usuariococina.models;

public class LoginResponse {
    private String token;
    private Usuario user;

    // Getters y Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
}