package com.example.usuariococina.api;

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
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface LaravelApiService {

    // Login (Público)
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Monitor de Cocina
    @GET("cocina/pedidos")
    Call<List<Order>> getPedidosCocina();

    // Actualizar el estado desde la cocina
    @FormUrlEncoded
    @PUT("pedidos/{id}")
    Call<ResponseBody> updateOrderStatus(
            @Path("id") int orderId,
            @Field("status") String status
    );
}