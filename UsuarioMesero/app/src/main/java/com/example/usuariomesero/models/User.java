package com.example.usuariomesero.models;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("username")
    private String username;

    @SerializedName("role")
    private String role;

    // 👇 ESTOS SON LOS GETTERS QUE HACÍAN FALTA 👇

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}