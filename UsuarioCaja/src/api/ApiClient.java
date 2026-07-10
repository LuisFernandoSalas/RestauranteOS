package api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final String BASE_URL = "http://127.0.0.1:8000/api/";
    private static final HttpClient client = HttpClient.newHttpClient();

    // Ejemplo de tu método GET modificado
    public static String get(String endpoint) throws Exception {

        // 1. Empezamos a armar la petición
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json");

        // 🚀 2. AQUÍ ESTÁ LA MAGIA: Si hay token, se lo inyectamos a la cabecera
        if (SessionManager.hasToken()) {
            requestBuilder.header("Authorization", "Bearer " + SessionManager.getToken());
        }

        HttpRequest request = requestBuilder.GET().build();

        // 3. Enviamos la petición
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new Exception("Error de API (" + response.statusCode() + "): " + response.body());
        }
    }

    // 2️⃣ NUESTRO POST: El motor HTTP nivel Senior
    public static String post(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        // 🚀 AJUSTE 1: Si ya estamos logueados, mandamos el token en TODO POST
        if (SessionManager.hasToken()) {
            requestBuilder.header("Authorization", "Bearer " + SessionManager.getToken());
        }

        // Construimos finalmente la petición POST
        HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 🚀 AJUSTE 2: Aceptamos cualquier código de la familia 200 (200 OK, 201 Created, etc.)
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            // 🚀 AJUSTE 3: Mostramos el mensaje real de Laravel para depurar más rápido
            throw new RuntimeException("Error HTTP " + response.statusCode() + " - Detalle: " + response.body());
        }
    }
}