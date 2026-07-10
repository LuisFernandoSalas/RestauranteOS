package api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import modelos.Mesa;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MesaService {
    private static final Gson gson = new Gson();

    public static List<Mesa> obtenerMesas() throws Exception {
        // 1. Hacemos la petición (Tu ApiClient ya le pondrá el Token automáticamente)
        String jsonResponse = ApiClient.get("mesas");

        // (Opcional) Imprimir para ver qué nos mandó José
        System.out.println("JSON crudo de Mesas: " + jsonResponse);

        // 2. Le decimos a Gson que convierta el texto en una Lista de 'MesaLaravel'
        Type listType = new TypeToken<List<MesaLaravel>>(){}.getType();
        List<MesaLaravel> mesasLaravel = gson.fromJson(jsonResponse, listType);

        // 3. Convertimos las mesas de Laravel a las mesas que la ventana de Kevyn entiende
        List<Mesa> mesasVista = new ArrayList<>();

        for (MesaLaravel ml : mesasLaravel) {
            // Traducimos el texto de Laravel al Enum de Java
            Mesa.EstadoMesa estadoEnum = Mesa.EstadoMesa.LIBRE;
            if (ml.status != null) {
                if (ml.status.equalsIgnoreCase("ocupado")) estadoEnum = Mesa.EstadoMesa.OCUPADO;
                else if (ml.status.equalsIgnoreCase("cobro")) estadoEnum = Mesa.EstadoMesa.COBRO;
            }

            // Creamos el objeto Mesa final
            Mesa nuevaMesa = new Mesa(ml.id, ml.numero, estadoEnum, 0.0, 0);
            mesasVista.add(nuevaMesa);
        }

        return mesasVista;
    }

    // ─────────────────────────────────────────────
    // CLASE INTERNA: El molde exacto de la base de datos de José
    // ─────────────────────────────────────────────
    public static class MesaLaravel {
        public int id;
        public int numero;
        public String status;
    }
}