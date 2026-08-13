package com.example.usuariococina.ui;

import android.content.Intent; // <-- NUEVO IMPORT
import android.content.SharedPreferences;
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
import com.example.usuariococina.models.Resumen;
import com.example.usuariococina.adapters.OrdersAdapter;
import com.example.usuariococina.api.apiClient;
import com.example.usuariococina.api.apiService;
import com.example.usuariococina.R;
import com.google.gson.Gson; // <-- Asegúrate de tener esto para enviar el pedido como JSON

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private static final String TAG = "Cocina_OrdersActivity";
    private RecyclerView rvOrders;
    private OrdersAdapter adapter;
    private String resumenJsonGlobal = "";

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

        // (ELIMINAMOS LOS FINDVIEWBYID DE LOS TEXTVIEWS PORQUE ESTÁN EN LA OTRA PANTALLA)

        int spanCount = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        rvOrders.setLayoutManager(new GridLayoutManager(this, spanCount));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Log.d(TAG, "btnLogout: Intento de cerrar sesión");
            Toast.makeText(this, "Botón de salida presionado", Toast.LENGTH_SHORT).show();
        });

        setupAdapter();
        cargarPedidosDeApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: La pantalla volvió al frente, recargando pedidos...");
        cargarPedidosDeApi();
    }

    private void setupAdapter() {
        adapter = new OrdersAdapter(this, new ArrayList<>(), order -> {
            String numMesa = String.valueOf(order.getMesa());
            Log.d(TAG, "onOrderClick: Solicitando detalles para la Mesa " + numMesa);
            showOrderDetailDialog(order); // Aquí se activa el envío a la otra pantalla
        });
        rvOrders.setAdapter(adapter);
    }

    private void cargarPedidosDeApi() {
        Log.d(TAG, "cargarPedidosDeApi: Solicitando comandas al servidor...");

        SharedPreferences prefs = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("AUTH_TOKEN", null);

        if (token == null) {
            Log.e(TAG, "cargarPedidosDeApi: No se encontró el token de autenticación.");
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService service = apiClient.getApiService(token);
        Call<ComandasApiResponse> call = service.obtenerPedidosCocina();

        call.enqueue(new Callback<ComandasApiResponse>() {
            @Override
            public void onResponse(Call<ComandasApiResponse> call, Response<ComandasApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // NUEVO 2: Convertimos el resumen a texto INMEDIATAMENTE
                    Resumen resumen = response.body().getResumen();
                    if (resumen != null) {
                        resumenJsonGlobal = new Gson().toJson(resumen);
                        Log.d("DEBUG_COCINA", "API -> Resumen guardado como texto: " + resumenJsonGlobal);
                    }

                    List<PedidoResponse> pedidosReales = response.body().getComandas();

                    if (pedidosReales != null) {
                        List<PedidoResponse> pedidosPendientes = new ArrayList<>();

                        for (PedidoResponse p : pedidosReales) {
                            if (p.getEstadoGeneral() != null && !"listo".equalsIgnoreCase(p.getEstadoGeneral())) {
                                pedidosPendientes.add(p);
                            }
                        }
                        adapter.setOrders(pedidosPendientes);
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
        String numMesa = (order.getMesa() > 0) ? String.valueOf(order.getMesa()) : "Sin Mesa";
        Log.d(TAG, "showOrderDetailDialog: Construyendo diálogo para mesa " + numMesa);

        // --- NUEVO: ENVIAR DATOS A LA PANTALLA DE DETALLE ---
        Intent intent = new Intent(this, PreparationDetailActivity.class);

        // Enviamos el pedido completo convertido a String
        intent.putExtra("PEDIDO_DATA", new Gson().toJson(order));

        // Enviamos el resumen empacado como texto (si no está vacío)
        if (resumenJsonGlobal != null && !resumenJsonGlobal.isEmpty()) {
            Log.d("DEBUG_COCINA", "INTENT -> Enviando resumen empacado en JSON");
            intent.putExtra("RESUMEN_DATA", resumenJsonGlobal);
        } else {
            Log.e("DEBUG_COCINA", "INTENT -> ¡ALERTA! El resumen de texto estaba vacío justo antes de abrir el pedido");
        }

        startActivity(intent);
    }
}