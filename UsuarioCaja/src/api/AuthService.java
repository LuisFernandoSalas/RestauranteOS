package api;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class AuthService {
    // Instancia de Gson que ya agregaste al proyecto
    private static final Gson gson = new Gson();

    public static LoginResponse login(String username, String password) throws Exception {
        // 1. Convertimos los datos de Java a JSON automáticamente

        LoginRequest request = new LoginRequest(username, password);
        String jsonBody = gson.toJson(request);

        // 2. Usamos tu ApiClient para hacer el POST a Laravel
        String jsonResponse = ApiClient.post("login", jsonBody);

        System.out.println("JSON crudo de Laravel: " + jsonResponse);
        // 3. Transformamos la respuesta (JSON) de vuelta a Objetos Java mágicamente
        return gson.fromJson(jsonResponse, LoginResponse.class);
    }

    // ─────────────────────────────────────────────
    // CLASES INTERNAS PARA GSON (Data Transfer Objects)
    // ─────────────────────────────────────────────

    // Lo que enviamos a Laravel
    public static class LoginRequest {
        String username;
        String password;
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    // Lo que Laravel nos responde (Token + Datos del usuario)
    public static class LoginResponse {
        public String status;
        public String message;

        // 🚀 AQUÍ ESTÁ LA MAGIA: Le decimos a Gson cómo se llama realmente en el JSON
        @SerializedName("access_token")
        public String token;

        public Usuario user;
    }

    // El perfil del usuario que viene de la BD de Laravel
    public static class Usuario {
        public int id;
        public String name;
        public String username;
        public String role;
    }
}