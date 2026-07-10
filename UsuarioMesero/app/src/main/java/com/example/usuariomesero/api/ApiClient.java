package com.example.usuariomesero.api;

import android.content.Context;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.usuariomesero.utils.TokenManager;
import java.io.IOException;

public class ApiClient {

    // 🚨 IMPORTANTE: Cambia esta IP por la de tu computadora donde corre Laravel
    // Si usas el Emulador de Android Studio, pon: "http://10.0.2.2:8000/"
    // Si usas una Tablet física, pon la IPv4 de tu compu, ej: "http://192.168.1.75:8000/"
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";
    private static Retrofit retrofit = null;

    public static ApiService getService(Context context) {
        if (retrofit == null) {

            // 1. Interceptor para ver los JSON en la consola (Logcat)
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Interceptor para inyectar el Token a todas las peticiones mágicamente
            Interceptor tokenInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    TokenManager tokenManager = new TokenManager(context);
                    String token = tokenManager.obtenerToken();

                    if (token != null) {
                        Request requestBuilder = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(requestBuilder);
                    }
                    return chain.proceed(original);
                }
            };

            // 3. Armamos el cliente HTTP
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .addInterceptor(tokenInterceptor)
                    .build();

            // 4. Construimos Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}