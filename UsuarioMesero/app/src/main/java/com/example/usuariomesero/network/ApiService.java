package com.example.usuariomesero.network;

import com.example.usuariomesero.models.GuardarPedidoRequest;
import com.example.usuariomesero.models.LoginRequest;
import com.example.usuariomesero.models.LoginResponse;
import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.models.ProductoResponse;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("mesas")
    Call<List<Mesa>> getMesas();

    @POST("pedidos")
    Call<ResponseBody> enviarPedido(
            @Header("Authorization") String token,
            @Body GuardarPedidoRequest request
    );

    @PUT("pedidos/{id}")
    Call<ResponseBody> actualizarEstadoPedido(
            @Path("id") int pedidoId,
            @Body Map<String, String> body
    );

    @POST("pedidos/{id}/cobrar")
    Call<ResponseBody> cobrarPedido(
            @Path("id") int pedidoId,
            @Body Map<String, Object> body
    );

    @Headers("ngrok-skip-browser-warning: true")
    @GET("productos")
    Call<ProductoResponse> getProductos(@Header("Authorization") String token);

    // 🟢 1. Obtener los detalles del pedido para mostrarlos en la pantalla de cobro
    // Usamos la ruta específica que creó José: /pedidos/{id}/detalle-cobro
    @GET("pedidos/{id}/detalle-cobro")
    Call<okhttp3.ResponseBody> obtenerDetallePedidoParaCobro(
            @Header("Authorization") String token,
            @Path("id") int pedidoId
    );

    // 🟢 2. Enviar el pago al servidor
    @POST("pedidos/{id}/cobrar")
    Call<okhttp3.ResponseBody> procesarCobro(
            @Header("Authorization") String token,
            @Path("id") int pedidoId,
            @Body java.util.Map<String, Object> datosCobro
    );

    @POST("pedidos/{id}/cobrar") // Ajusta esta ruta a la que hayas definido en Laravel
    Call<ResponseBody> procesarCobro(
            @Header("Authorization") String token,
            @Path("id") int pedidoId,
            @Body JsonObject datosCobro
    );

    // Ruta para que el mesero actualice el pedido con los datos del cobro
    @PUT("pedidos/{id}")
    Call<okhttp3.ResponseBody> solicitarCobroPedido(
            @Header("Authorization") String token,
            @Path("id") int pedidoId,
            @Body com.google.gson.JsonObject body
    );
}