package com.example.usuariococina.models;

public class Usuario {
    private int id;
    private String name;
    private String username;
    private String role; // Aquí vendrá "mesero", "cocina", "caja" o "admin"

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}