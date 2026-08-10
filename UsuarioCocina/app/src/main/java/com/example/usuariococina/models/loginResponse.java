package com.example.usuariococina.models;

import com.google.gson.annotations.SerializedName;

public class loginResponse {
    private String status;
    private String message;

    @SerializedName("access_token")
    private String accessToken;

    // 👈 Capturamos el usuario
    @SerializedName("user")
    private User user;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getAccessToken() { return accessToken; }
    public User getUser() { return user; }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    // 👈 Subclase para leer el rol
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