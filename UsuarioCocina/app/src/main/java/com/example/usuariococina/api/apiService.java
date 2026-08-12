package com.example.usuariococina.api;

import com.example.usuariococina.models.ComandasApiResponse;
import com.example.usuariococina.models.PedidoResponse;
import com.example.usuariococina.models.loginRequest;
import com.example.usuariococina.models.loginResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface apiService {

    @POST("login")
    Call<loginResponse> login(@Body loginRequest request);

    @GET("cocina/pedidos")
    Call<ComandasApiResponse> obtenerPedidosCocina();

    // 1. Completar / Cambiar estado de pedido (VERSIÓN ÚNICA Y CORRECTA)
    @FormUrlEncoded
    @PATCH("cocina/detalles/{id}/estado")
    Call<ResponseBody> cambiarEstadoPlatillo(
            @Header("Authorization") String token,
            @Path("id") int detalleId,
            @Field("estado") String nuevoEstado
    );

    // 2. Cancelar el pedido
    @FormUrlEncoded
    @POST("cocina/pedidos/{id}/cancelar")
    Call<ResponseBody> cancelarPedido(
            @Header("Authorization") String token,
            @Path("id") int pedidoId,
            @Field("motivo") String motivo
    );

    // 3. Pausar un producto del menú
    @FormUrlEncoded
    @POST("cocina/productos/{id}/pausar")
    Call<ResponseBody> pausarProducto(
            @Header("Authorization") String token,
            @Path("id") int productoId,
            @Field("duracion") String duracion
    );

    @FormUrlEncoded
    @PATCH("cocina/pedidos/{id}/estado") // O la ruta que manejes para el pedido en tu Laravel
    Call<ResponseBody> cambiarEstadoPedidoCompleto(
            @Header("Authorization") String token,
            @Path("id") int pedidoId,
            @Field("estado") String nuevoEstado
    );
}