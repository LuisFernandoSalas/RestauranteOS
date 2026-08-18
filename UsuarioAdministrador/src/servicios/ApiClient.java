package servicios;

import modelos.Empleado;
import modelos.Insumo;
import modelos.Producto;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente HTTP unificado para consumir la API backend del sistema.
 * Maneja llamadas síncronas HTTP/1.1 y HTTP/2, autenticación Bearer Token
 * y endpoints de Insumos, Empleados, Productos, Combos y Reportes.
 */
public class ApiClient {

    private static final String BASE_URL = "https://satiable-parameter-synthesis.ngrok-free.dev/api";
    private final HttpClient httpClient;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String getActiveToken() {
        return SesionManager.getToken();
    }

    // ─────────────────────────────────────────────
    // 🔑 AUTENTICACIÓN
    // ─────────────────────────────────────────────

    public boolean login(String username, String password) throws Exception {
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("username", username);
        jsonPayload.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonRes = new JSONObject(response.body());
            if (jsonRes.has("access_token")) {
                SesionManager.setToken(jsonRes.getString("access_token"));
                return true;
            }
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // 🛠️ MÉTODOS HTTP GENÉRICOS
    // ─────────────────────────────────────────────

    public String get(String endpoint) throws Exception {
        String token = getActiveToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(prepararUrl(endpoint)))
                .header("Accept", "application/json")
                .GET();

        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new Exception("Error HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public String post(String endpoint, String jsonBody) throws Exception {
        return enviarRequestPOST(endpoint, jsonBody);
    }

    public String put(String endpoint, String jsonBody) throws Exception {
        String token = getActiveToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(prepararUrl(endpoint)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.PUT(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new Exception("Error HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public String delete(String endpoint) throws Exception {
        String token = getActiveToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(prepararUrl(endpoint)))
                .header("Accept", "application/json")
                .DELETE();

        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new Exception("Error HTTP " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }

    public String enviarRequestPOST(String endpoint, String jsonBody) throws Exception {
        String token = getActiveToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(prepararUrl(endpoint)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (token != null && !token.trim().isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new Exception("Error HTTP " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    // ─────────────────────────────────────────────
    // 📦 MÓDULO INSUMOS
    // ─────────────────────────────────────────────

    public String obtenerInsumos() throws Exception {
        return get("/insumos");
    }

    public String crearInsumo(Insumo insumo) throws Exception {
        JSONObject json = new JSONObject();
        json.put("nombre", insumo.getNombre());
        json.put("categoria", insumo.getCategoria() != null ? insumo.getCategoria() : "General");
        json.put("unidad_medida", insumo.getUnidadMedida());
        json.put("stock_actual", insumo.getStockActual());
        json.put("stock_minimo", insumo.getStockMinimo() > 0 ? insumo.getStockMinimo() : 5.0);

        return enviarRequestPOST("/insumos", json.toString());
    }

    public String registrarMovimiento(int insumoId, String tipo, double cantidad, String motivo) throws Exception {
        JSONObject json = new JSONObject();
        json.put("insumo_id", insumoId);
        json.put("tipo", tipo);
        json.put("cantidad", cantidad);
        json.put("motivo", motivo);

        return enviarRequestPOST("/insumos/movimiento", json.toString());
    }

    public boolean eliminarInsumo(int id) throws Exception {
        delete("/insumos/" + id);
        return true;
    }

    public String actualizarInsumo(Insumo insumo) throws Exception {
        JSONObject json = new JSONObject();
        json.put("nombre", insumo.getNombre());
        json.put("categoria", insumo.getCategoria() != null ? insumo.getCategoria() : "General");
        json.put("unidad_medida", insumo.getUnidadMedida());
        json.put("stock_minimo", insumo.getStockMinimo());
        json.put("stock_maximo", insumo.getStockMaximo());

        return put("/insumos/" + insumo.getId(), json.toString());
    }

    // ─────────────────────────────────────────────
    // 👥 MÓDULO EMPLEADOS (PanelEmpleados)
    // ─────────────────────────────────────────────

    public String obtenerEmpleados() throws Exception {
        return get("/empleados");
    }

    public String crearEmpleado(Empleado empleado) throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", empleado.getNombre());
        json.put("username", empleado.getUsername());
        json.put("role", empleado.getRol()); // "mesero", "cocinero", "admin", etc.
        json.put("password", empleado.getPassword());

        return post("/empleados", json.toString());
    }

    public String actualizarEmpleado(Empleado empleado) throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", empleado.getNombre());
        json.put("username", empleado.getUsername());
        json.put("role", empleado.getRol());

        return put("/empleados/" + empleado.getId(), json.toString());
    }

    public boolean eliminarEmpleado(Integer id) throws Exception {
        delete("/empleados/" + id);
        return true;
    }

    // ─────────────────────────────────────────────
    // 🍲 MÓDULO PRODUCTOS Y RECETAS (PanelMenuProductos)
    // ─────────────────────────────────────────────

    public String obtenerProductos() throws Exception {
        return get("/productos");
    }

    public String obtenerRecetas() throws Exception {
        return get("/recetas");
    }

    /**
     * Resuelve el ID numérico de la categoría a partir de su nombre.
     */
    private int resolverCategoriaId(String nombreCat) {
        if (nombreCat == null) return 1;
        String cat = nombreCat.trim().toLowerCase();
        if (cat.contains("plato")) return 1;    // Platos fuertes
        if (cat.contains("entrada")) return 2;  // Entradas
        if (cat.contains("bebida")) return 3;   // Bebidas
        if (cat.contains("postre")) return 4;   // Postres
        if (cat.contains("combo")) return 5;    // Combos
        return 1;
    }

    public String crearProducto(Producto producto) throws Exception {
        JSONObject json = new JSONObject();
        json.put("nombre", producto.getNombre());
        json.put("precio", producto.getPrecio());
        json.put("descripcion", producto.getDescripcion() != null ? producto.getDescripcion() : "");

        String catName = producto.getCategoria();
        int catId = resolverCategoriaId(catName);

        // Envío de claves numéricas y de texto para compatibilidad total con Laravel
        json.put("categoria_id", catId);
        json.put("category_id", catId);
        json.put("categoria", catName);
        json.put("category", catName);
        json.put("estado", producto.getEstado() != null ? producto.getEstado() : "Activo");

        return post("/productos", json.toString());
    }

    public void actualizarProducto(Producto prod) throws Exception {
        JSONObject json = new JSONObject();
        json.put("nombre", prod.getNombre());
        json.put("name", prod.getNombre()); // Soporte por si Laravel valida 'name'
        json.put("precio", prod.getPrecio());
        json.put("price", prod.getPrecio());

        // Resolvemos el ID numérico
        int categoriaId = obtenerCategoriaId(prod.getCategoria());

        // Enviamos TODAS las variantes posibles para garantizar compatibilidad con Laravel
        json.put("categoria_id", categoriaId);
        json.put("category_id", categoriaId);
        json.put("categoria", prod.getCategoria());
        json.put("category", prod.getCategoria());

        // Estado / Disponibilidad
        boolean disponible = prod.getEstado().equalsIgnoreCase("Activo");
        json.put("is_disponible", disponible);
        json.put("disponible", disponible);
        json.put("estado", prod.getEstado());
        json.put("status", prod.getEstado());

        // Endpoint
        String endpoint = "/productos/" + prod.getId();
        put(endpoint, json.toString());
    }

    // Mapeador auxiliar con búsqueda flexible (usando 'contains')
    private int obtenerCategoriaId(String nombreCategoria) {
        if (nombreCategoria == null) return 1;
        String cat = nombreCategoria.trim().toLowerCase();

        if (cat.contains("plato")) return 1;    // Platos fuertes
        if (cat.contains("entrada")) return 2;  // Entradas
        if (cat.contains("bebida")) return 3;   // Bebidas
        if (cat.contains("postre")) return 4;   // Postres
        if (cat.contains("combo")) return 5;    // Combos
        return 1;
    }

    public boolean eliminarProducto(Integer id) throws Exception {
        delete("/productos/" + id);
        return true;
    }


    // ─────────────────────────────────────────────
    // 🏷️ MÓDULO COMBOS Y PROMOS (PanelCombosPromos)
    // ─────────────────────────────────────────────

    public String obtenerCombos() throws Exception {
        return get("/combos");
    }

    public String crearCombo(String jsonBody) throws Exception {
        return post("/combos", jsonBody);
    }

    public String actualizarCombo(int id, String jsonBody) throws Exception {
        return put("/combos/" + id, jsonBody);
    }

    public boolean eliminarCombo(int id) throws Exception {
        delete("/combos/" + id);
        return true;
    }

    // ─────────────────────────────────────────────
    // 📊 MÓDULO REPORTES Y DASHBOARD (PanelGeneral / PanelReportes)
    // ─────────────────────────────────────────────

    public String obtenerDashboardReportes() throws Exception {
        return get("/dashboard/reportes");
    }

    // ─────────────────────────────────────────────
    // ⚙️ MÉTODOS AUXILIARES
    // ─────────────────────────────────────────────

    private String prepararUrl(String endpoint) {
        if (endpoint.startsWith("http")) return endpoint;
        if (BASE_URL.endsWith("/api") && endpoint.startsWith("/api/")) {
            return BASE_URL.substring(0, BASE_URL.length() - 4) + endpoint;
        }
        return BASE_URL + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
    }
}