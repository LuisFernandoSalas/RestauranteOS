package network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RetrofitClient {

    private static final String BASE_URL = "https://idealism-anchor-skipper.ngrok-free.dev/api/";
    private static Retrofit retrofit = null;

    // --- NUEVO: Método sin parámetros para las partes de la app que aún no tienen token (como el Login) ---
    public static Retrofit getClient() {
        return getClient("");
    }

    // --- Método con parámetro para enviar el token (el que ya tenías) ---
    public static Retrofit getClient(String token) {

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Accept", "application/json")
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}