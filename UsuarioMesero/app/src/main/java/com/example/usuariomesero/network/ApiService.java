package com.example.usuariomesero.network;

import com.example.usuariomesero.models.GuardarPedidoRequest;
import com.example.usuariomesero.models.LoginRequest;
import com.example.usuariomesero.models.LoginResponse;
import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.models.ProductoResponse;

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

}