package com.example.usuariomesero.network;

import com.example.usuariomesero.models.GuardarPedidoRequest;
import com.example.usuariomesero.models.LoginRequest;
import com.example.usuariomesero.models.LoginResponse;
import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.models.Producto;
import com.example.usuariomesero.models.ProductoResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Body;

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

    @Headers("ngrok-skip-browser-warning: true")
    @GET("productos")
    Call<ProductoResponse> getProductos(@Header("Authorization") String token);
}