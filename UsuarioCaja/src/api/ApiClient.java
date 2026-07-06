package api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final String BASE_URL = "http://127.0.0.1:8000/api/";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // 1️⃣ NUESTRO TOKEN: Aquí guardamos la llave para las mesas
    public static String token = "";

    public static String get(String endpoint) throws Exception {
        System.out.println("DEBUG: Pidiendo endpoint -> " + endpoint);
        System.out.println("DEBUG: Token actual en ApiClient -> [" + token + "]");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        // Dentro del método GET de tu ApiClient.java:
        if (api.SessionManager.hasToken()) {
            builder.header("Authorization", "Bearer " + api.SessionManager.getToken());
        }

        HttpRequest request = builder.GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            // Si da 401, imprimimos qué nos responde el servidor para entenderlo mejor
            System.out.println("❌ Error 401 en: " + endpoint + " | Respuesta: " + response.body());
            throw new RuntimeException("Error en servidor: " + response.statusCode());
        }
    }

    // 2️⃣ NUESTRO POST: El motor para el login que nos faltaba
    public static String post(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body(); // Nos regresa el JSON con el token de José
        } else {
            throw new RuntimeException("Error en servidor: " + response.statusCode());
        }
    }
}