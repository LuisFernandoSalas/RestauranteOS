package com.example.usuariococina.api;

import android.content.Context;
import com.example.usuariococina.utils.SessionManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8000/api/";xx
    private static Retrofit retrofit = null;

    public static LaravelApiService getApiService(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context);

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    String token = sessionManager.obtenerToken();

                    // 1. SIEMPRE preparamos el encabezado para exigir JSON
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .header("Accept", "application/json");

                    // 2. Si el token existe y NO está vacío, lo inyectamos
                    if (token != null && !token.trim().isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + token);
                    }

                    // 3. Continuamos con la petición ya armada
                    return chain.proceed(requestBuilder.build());
                }
            };

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit.create(LaravelApiService.class);
    }
}