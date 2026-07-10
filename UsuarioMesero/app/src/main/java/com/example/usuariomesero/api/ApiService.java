package com.example.usuariomesero.api;

import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.models.OrdenRequest;
import com.example.usuariomesero.models.Producto;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // 🚀 Aquí definiremos todas las rutas de Laravel

    @Headers("Accept: application/json")
    @POST("login")
    Call<JsonObject> login(@Body JsonObject credenciales);

    @Headers("Accept: application/json")
    @GET("mesas")
    Call<List<Mesa>> obtenerMesas();

    // Mañana agregaremos aquí la ruta para enviar pedidos:
    @Headers("Accept: application/json")
    @POST("api/mesas/{id}/pedidos")
    Call<JsonObject> enviarPedido(@Path("id") int mesaId, @Body JsonObject datosPedido);

    // 🚀 Ruta para traer la comida real del servidor
    @Headers("Accept: application/json")
    @GET("productos") // Si José cambió esta ruta en su api.php, cámbiala aquí
    Call<ApiResponse> obtenerProductos();

    @Headers("Accept: application/json")
    @POST("pedidos")
    Call<ApiResponse> enviarComandaACocina(@Body OrdenRequest ordenRequest);
}