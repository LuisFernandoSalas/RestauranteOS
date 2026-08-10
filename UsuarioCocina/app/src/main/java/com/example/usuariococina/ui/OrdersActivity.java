package com.example.usuariococina.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.models.ComandasApiResponse;
import com.example.usuariococina.models.PedidoResponse;
import com.example.usuariococina.adapters.OrdersAdapter;
import com.example.usuariococina.api.apiClient;
import com.example.usuariococina.api.apiService;
import com.example.usuariococina.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "Cocina_OrdersActivity";
    private RecyclerView rvOrders;
    private OrdersAdapter adapter; // Lo declaramos global para poder actualizarlo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);

        Log.d(TAG, "onCreate: Abriendo el tablero de pedidos (KDS)");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_orders), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvOrders = findViewById(R.id.rvOrders);

        int spanCount = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        rvOrders.setLayoutManager(new GridLayoutManager(this, spanCount));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Log.d(TAG, "btnLogout: Intento de cerrar sesión");
            Toast.makeText(this, "Botón de salida presionado", Toast.LENGTH_SHORT).show();
        });

        setupAdapter();
        cargarPedidosDeApi();
    }

    private void setupAdapter() {
        // Inicializamos el adaptador con una lista vacía mientras carga la API
        adapter = new OrdersAdapter(this, new ArrayList<>(), order -> {
            String numMesa = String.valueOf(order.getMesa());
            Log.d(TAG, "onOrderClick: Solicitando detalles para la Mesa " + numMesa);
            showOrderDetailDialog(order);
        });
        rvOrders.setAdapter(adapter);
    }

    private void cargarPedidosDeApi() {
        Log.d(TAG, "cargarPedidosDeApi: Solicitando comandas al servidor...");

        // 1. Recuperamos el token guardado durante el Login
        android.content.SharedPreferences prefs = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("AUTH_TOKEN", null);

        if (token == null) {
            Log.e(TAG, "cargarPedidosDeApi: No se encontró el token de autenticación.");
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Usamos TU método que ya inyecta el token automáticamente en el Interceptor
        apiService service = apiClient.getApiService(token);

        // 3. Hacemos la llamada limpia sin pasar parámetros extra
        // 3. Hacemos la llamada con el nuevo modelo
        Call<ComandasApiResponse> call = service.obtenerPedidosCocina();

        call.enqueue(new Callback<ComandasApiResponse>() {
            @Override
            public void onResponse(Call<ComandasApiResponse> call, Response<ComandasApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 👇 AQUI ESTA LA MAGIA: Extraemos la lista del envoltorio
                    List<PedidoResponse> pedidosReales = response.body().getComandas();

                    if (pedidosReales != null) {
                        Log.d(TAG, "Éxito: Se descargaron " + pedidosReales.size() + " pedidos");
                        adapter.setOrders(pedidosReales);
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                    Toast.makeText(OrdersActivity.this, "Error al cargar pedidos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ComandasApiResponse> call, Throwable t) {
                Log.e(TAG, "Error de red al intentar conectar con la API", t);
                Toast.makeText(OrdersActivity.this, "Verifica tu conexión a internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showOrderDetailDialog(PedidoResponse order) {
        // Úsalo solo si quieres mantener el texto "Sin Mesa" cuando no haya número
        String numMesa = (order.getMesa() > 0) ? String.valueOf(order.getMesa()) : "Sin Mesa";
        Log.d(TAG, "showOrderDetailDialog: Construyendo diálogo para mesa " + numMesa);

        // ... (Tu código de AlertDialog original, pero adaptado al nuevo PedidoResponse) ...
    }
}