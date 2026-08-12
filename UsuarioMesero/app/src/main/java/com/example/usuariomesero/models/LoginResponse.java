package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("user")
    private User user;

    @SerializedName("access_token")
    private String token; // El Bearer token que Sanctum genera

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    // 👈 Agregamos la subclase aquí mismo para leer el rol
    public static class User {
        private int id;
        private String name;
        private String role;
        private String username;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getUsername() { return username; }
    }
}