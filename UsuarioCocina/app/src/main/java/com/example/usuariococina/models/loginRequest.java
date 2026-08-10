package com.example.usuariococina.models;

public class loginRequest {
    private String username; // O 'username', dependiendo de cómo lo tengas en tu BD
    private String password;

    public loginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}