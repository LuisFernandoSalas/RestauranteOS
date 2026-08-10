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
import retrofit2.http.Path;

public interface apiService {

    // Asegúrate de que esta ruta coincida con tu api.php en Laravel
    @POST("login")
    Call<loginResponse> login(@Body loginRequest request);

    @GET("cocina/pedidos")
    Call<ComandasApiResponse> obtenerPedidosCocina();

    @FormUrlEncoded
    @PATCH("cocina/detalles/{id}/estado")
    Call<ResponseBody> cambiarEstadoPlatillo(
            @Path("id") int detalleId,
            @Field("estado") String nuevoEstado
    );
}