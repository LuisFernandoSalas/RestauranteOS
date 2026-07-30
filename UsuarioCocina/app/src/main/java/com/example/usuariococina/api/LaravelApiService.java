package com.example.usuariococina.api;

import com.example.usuariococina.models.CocinaResponse;
import com.example.usuariococina.models.LoginRequest;
import com.example.usuariococina.models.LoginResponse;
import com.example.usuariococina.models.Order;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LaravelApiService {

    // Login (Público)
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

<<<<<<< HEAD
    // ==========================================
    // 🍳 MÓDULO DE COCINA (Kitchen Display System)
    // ==========================================
=======
    // Monitor de Cocina
    @GET("cocina/pedidos")
    Call<List<Order>> getPedidosCocina();
>>>>>>> d9dcc2e (feat: implement full API audit and security testing (RBAC, WebSockets, cash-out, and inventory))

    // 1. Obtiene el feed horizontal de comandas activas
    @GET("cocina/pedidos") // Asegúrate de que esta ruta sea la que tienes
    Call<CocinaResponse> getPedidosCocina();

    // 🚀 NUEVO: La "Paquetería" - Obtiene los detalles frescos de UNA SOLA orden
    @GET("cocina/pedidos/{id}")
    Call<Order> getPedidoCocinaId(@Path("id") int id);

    // 2. Avanza o cambia el estado por platillo individual (KDS interactivo)
    @FormUrlEncoded
    @PATCH("cocina/detalles/{id}/estado")
    Call<ResponseBody> updatePlatilloEstado(
            @Path("id") int detalleId,
            @Field("estado") String estado
    );

    // 3. Cancela una orden completa inyectando motivo (Auditoría/Mermas)
    @FormUrlEncoded
    @POST("cocina/pedidos/{id}/cancelar")
    Call<ResponseBody> cancelarPedido(
            @Path("id") int pedidoId,
            @Field("motivo") String motivo
    );

    // 4. Función 86: Desactiva un producto temporalmente del catálogo
    @FormUrlEncoded
    @POST("cocina/productos/{id}/pausar")
    Call<ResponseBody> pausarProducto(
            @Path("id") int productoId,
            @Field("duracion") String duracion
    );
}