package com.example.usuariococina.api;

import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class apiClient {

    private static final String BASE_URL = "  https://idealism-anchor-skipper.ngrok-free.dev/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(final String authToken) {

        // 1. Interceptor para ver todo el tráfico HTTP en Logcat
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 2. Interceptor para inyectar Headers obligatorios
        Interceptor headerInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("ngrok-skip-browser-warning", "true"); // 👈 Evita la pantalla de advertencia de Ngrok

            // Inyectamos el Token Bearer si existe
            if (authToken != null && !authToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            requestBuilder.method(original.method(), original.body());
            return chain.proceed(requestBuilder.build());
        };

        // 3. Cliente OkHttp con Tiempos de Espera
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        // 4. Instancia de Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    // 🟢 Método para llamadas SIN token (como Login)
    public static apiService getApiService() {
        return getClient(null).create(apiService.class);
    }

    // 🟢 Método para llamadas CON token (como Pedidos o Cambiar Estado)
    public static apiService getApiService(String token) {
        return getClient(token).create(apiService.class);
    }
}