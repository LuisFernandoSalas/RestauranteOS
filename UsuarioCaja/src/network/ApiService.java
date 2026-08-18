package network;

import com.google.gson.JsonObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import com.google.gson.JsonArray;

import java.util.Map;

public interface ApiService {
    // Ruta para que el cajero inicie sesión
    @POST("login")
    Call<JsonObject> login(@Body JsonObject body);

    // Ruta para abrir el turno de caja (ajusta "caja/abrir-turno" si José le puso otro nombre)
    @POST("caja/abrir-turno")
    Call<JsonObject> abrirTurno(@Header("Authorization") String token, @Body JsonObject body);

    @GET("mesas") // La misma ruta que usas en Android
    Call<JsonArray> getMesas(@Header("Authorization") String token);

    @GET("pedidos/{id}/detalle-cobro")
    Call<ResponseBody> obtenerDetalleCobro(@Path("id") int pedidoId);

    // 2. Procesar el cobro final
    @POST("pedidos/{id}/cobrar")
    Call<ResponseBody> cobrarPedido(@Path("id") int pedidoId, @Body Map<String, Object> body);

    // 1. Obtener un pedido específico por su ID (sin v1)
    @retrofit2.http.GET("pedidos/{id}")
    retrofit2.Call<okhttp3.ResponseBody> obtenerPedidoPorId(@retrofit2.http.Path("id") int pedidoId);

    // 2. Procesar el cobro financiero del pedido (sin v1)
    @retrofit2.http.POST("pagos/cobrar")
    retrofit2.Call<okhttp3.ResponseBody> procesarPago(@retrofit2.http.Body java.util.Map<String, Object> payload);
}