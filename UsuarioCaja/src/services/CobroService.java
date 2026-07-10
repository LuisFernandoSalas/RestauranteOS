package services;

import api.ApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import vistas.PanelCobro.ItemPedido; // Importamos la clase que Kevyn dejó en la vista
import java.util.List;

public class CobroService {

    private static final Gson gson = new Gson();

    // Este método es el puente real
    public static DetalleCobro obtenerDatosCobro(int idMesa) throws Exception {
        // Usamos tu ApiClient existente para llamar a la API de José
        // La ruta asume que José creó algo como /api/pedidos/{idMesa}/detalle-cobro
        String jsonResponse = ApiClient.get("pedidos/" + idMesa + "/detalle-cobro");

        // Gson convierte automáticamente el JSON que manda Laravel a nuestra clase Java
        return gson.fromJson(jsonResponse, DetalleCobro.class);
    }

    // --- Clase interna para mapear la respuesta de Laravel ---
    public static class DetalleCobro {
        public String nombreMesero;
        public int tiempoMinutos;
        public double totalGeneral;
        public List<ItemPedido> items; // Esta lista coincide con la que necesita Kevyn
    }
}