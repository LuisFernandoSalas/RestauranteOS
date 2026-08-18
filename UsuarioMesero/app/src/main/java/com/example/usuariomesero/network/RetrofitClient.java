package com.example.usuariomesero.network;

import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Asegúrate de que la IP sea la de tu servidor Laravel

=======
    private static final String BASE_URL = " https://satiable-parameter-synthesis.ngrok-free.dev/api/";
>>>>>>> 4b37fe92 (actualizacion 1)
    private static Retrofit retrofit = null;

    // 👇 ESTE ES EL MÉTODO QUE JAVA ESTABA BUSCANDO 👇
    public static Retrofit getClient(final String authToken) {

        // 1. Configuramos el Logger para ver las peticiones en la consola
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 2. Interceptor para inyectar Headers
        Interceptor headerInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/json"); // Obligatorio para Laravel

            // Si le pasamos un token (cuando ya hizo login), lo inyecta aquí
            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            requestBuilder.method(original.method(), original.body());
            return chain.proceed(requestBuilder.build());
        };

        // 3. Ensamblamos OkHttpClient con timeouts para la red local
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        // 4. Construimos Retrofit (Recreamos la instancia para asegurar que tome el token correcto)
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}