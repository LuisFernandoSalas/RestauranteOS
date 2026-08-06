package com.example.usuariomesero.activities;

import com.example.usuariomesero.R;
import com.example.usuariomesero.models.Producto;
import com.example.usuariomesero.models.ItemOrden;
import com.example.usuariomesero.models.GuardarPedidoRequest;
import com.example.usuariomesero.adapters.ProductoAdapter;
import com.example.usuariomesero.adapters.OrdenAdapter;
import com.example.usuariomesero.models.ProductoResponse;
import com.example.usuariomesero.network.ApiService;
import com.example.usuariomesero.network.RetrofitClient;
import com.example.usuariomesero.utils.SesionManager;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidoDetalleActivity extends AppCompatActivity {

    private RecyclerView rvProductos, rvOrden;
    private ProductoAdapter productoAdapter;
    private OrdenAdapter ordenAdapter;
    private List<Producto> listaProductosCompleta;
    private List<Producto> listaProductosFiltrada;
    private List<ItemOrden> listaOrden;
    private TextView tvTotal, tvMesaTitulo;
    private String categoriaSeleccionada = "Platos fuertes";

    private int mesaId;
    private int mesaNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cargarProductosDesdeServidor();
        setContentView(R.layout.activity_pedido_detalle);

        // Obtenemos los datos pasados desde la pantalla de Mesas
        mesaNumero = getIntent().getIntExtra("mesa_numero", 0);
        mesaId = getIntent().getIntExtra("mesa_id", mesaNumero); // ID para la BD de Laravel

        tvMesaTitulo = findViewById(R.id.tv_mesa_titulo);
        tvMesaTitulo.setText(String.format(Locale.getDefault(), "Mesa %d - Pedido", mesaNumero));

        rvProductos = findViewById(R.id.rv_productos_seleccion);
        rvOrden = findViewById(R.id.rv_orden_actual);
        tvTotal = findViewById(R.id.tv_total_orden);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 👇 AHORA EJECUTA EL ENVÍO REAL A LARAVEL EN LUGAR DE SOLO MOSTRAR EL DIÁLOGO 👇
        findViewById(R.id.btn_mandar_orden).setOnClickListener(v -> {
            if (listaOrden.isEmpty()) {
                Toast.makeText(this, "Agrega productos a la orden primero", Toast.LENGTH_SHORT).show();
            } else {
                enviarPedidoServidor();
            }
        });

        setupRecyclerViews();
        filtrarProductos(categoriaSeleccionada);
        setupCategoryButtons();
    }

    private void enviarPedidoServidor() {
        // 1. Generar UUID único para la idempotencia de tu backend
        String clientUuid = UUID.randomUUID().toString();

        // 2. Mapear los productos de la lista actual de Android al formato de Laravel
        List<GuardarPedidoRequest.ProductoItem> productosItem = new ArrayList<>();
        for (ItemOrden item : listaOrden) {
            productosItem.add(new GuardarPedidoRequest.ProductoItem(
                    item.getProducto().getId(), // ID real del producto en MySQL
                    item.getCantidad(),
                    item.getNota()
            ));
        }

        GuardarPedidoRequest request = new GuardarPedidoRequest(clientUuid, mesaId, productosItem);

        // 3. Preparar Token de Autenticación Sanctum
        SesionManager sesionManager = new SesionManager(this);
        String token = "Bearer " + sesionManager.getAuthToken();

        // 4. Petición HTTP mediante Retrofit
        ApiService apiService = RetrofitClient.getApiService();
        apiService.enviarPedido(token, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // ¡Éxito en Laravel! mostramos el diálogo y notificamos a la pantalla de Mesas
                    mostrarDialogoOrdenEnviada();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Error " + response.code();
                        Toast.makeText(PedidoDetalleActivity.this, "Error de Laravel: " + error, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(PedidoDetalleActivity.this, "Error al procesar la comanda", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PedidoDetalleActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViews() {
        listaProductosCompleta = new ArrayList<>();
        listaProductosFiltrada = new ArrayList<>();
        productoAdapter = new ProductoAdapter(listaProductosFiltrada, producto -> {
            agregarAOrden(producto);
        });

        int spanCount = 2; // Fixed for Tablet
        rvProductos.setLayoutManager(new GridLayoutManager(this, spanCount));
        rvProductos.setAdapter(productoAdapter);

        listaOrden = new ArrayList<>();
        ordenAdapter = new OrdenAdapter(listaOrden, new OrdenAdapter.OnOrdenActionListener() {
            @Override
            public void onUpdateTotal() {
                actualizarTotal();
            }

            @Override
            public void onItemClick(ItemOrden item) {
                mostrarDialogoNota(item);
            }
        });
        rvOrden.setLayoutManager(new LinearLayoutManager(this));
        rvOrden.setAdapter(ordenAdapter);
    }

    private void setupCategoryButtons() {
        Button btnEntradas = findViewById(R.id.btn_categoria_entradas);
        Button btnPlatos = findViewById(R.id.btn_categoria_platos);
        Button btnBebidas = findViewById(R.id.btn_categoria_bebidas);
        Button btnPostres = findViewById(R.id.btn_categoria_postres);

        // 👇 Usamos "Platos fuertes" para coincidir con la BD
        if (btnEntradas != null) btnEntradas.setOnClickListener(v -> updateCategorySelection("Entradas", btnEntradas, btnPlatos, btnBebidas, btnPostres));
        if (btnPlatos != null) btnPlatos.setOnClickListener(v -> updateCategorySelection("Platos fuertes", btnEntradas, btnPlatos, btnBebidas, btnPostres));
        if (btnBebidas != null) btnBebidas.setOnClickListener(v -> updateCategorySelection("Bebidas", btnEntradas, btnPlatos, btnBebidas, btnPostres));
        if (btnPostres != null) btnPostres.setOnClickListener(v -> updateCategorySelection("Postres", btnEntradas, btnPlatos, btnBebidas, btnPostres));

        if (btnPlatos != null) {
            updateCategorySelection("Platos fuertes", btnEntradas, btnPlatos, btnBebidas, btnPostres);
        }
    }

    private void updateCategorySelection(String categoria, Button... buttons) {
        categoriaSeleccionada = categoria;
        filtrarProductos(categoria);

        int colorTerracota = getResources().getColor(R.color.terracota_medio);
        int colorBlanco = getResources().getColor(R.color.white);
        int colorTextoOscuro = getResources().getColor(R.color.terracota_oscuro);

        for (Button btn : buttons) {
            if (btn == null) continue;
            if (btn.getText().toString().equalsIgnoreCase(categoria)) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorTerracota));
                btn.setTextColor(colorBlanco);
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
                btn.setTextColor(colorTextoOscuro);
            }
        }
    }

    private void filtrarProductos(String categoria) {
        categoriaSeleccionada = categoria;
        listaProductosFiltrada.clear();
        for (Producto p : listaProductosCompleta) {
            String catProd = p.getCategoria();
            if (catProd != null && (catProd.equalsIgnoreCase(categoria) || catProd.toLowerCase().contains(categoria.toLowerCase()))) {
                listaProductosFiltrada.add(p);
            }
        }
        productoAdapter.notifyDataSetChanged();
    }

    private void agregarAOrden(Producto producto) {
        boolean encontrado = false;
        for (ItemOrden item : listaOrden) {
            if (item.getProducto().getNombre().equals(producto.getNombre())) {
                item.setCantidad(item.getCantidad() + 1);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            listaOrden.add(new ItemOrden(producto, 1));
        }
        ordenAdapter.notifyDataSetChanged();
        actualizarTotal();
        Toast.makeText(this, producto.getNombre() + " agregado", Toast.LENGTH_SHORT).show();
    }

    private void actualizarTotal() {
        double total = 0;
        for (ItemOrden item : listaOrden) {
            total += item.getProducto().getPrecio() * item.getCantidad();
        }
        tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }

    private void mostrarDialogoNota(ItemOrden item) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText etNota = dialog.findViewById(R.id.et_nota_instrucciones);
        Button btnGuardar = dialog.findViewById(R.id.btn_save_note);
        Button btnCancelar = dialog.findViewById(R.id.btn_cancel_note);

        if (item.getNota() != null) {
            etNota.setText(item.getNota());
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnGuardar.setOnClickListener(v -> {
            String nota = etNota.getText().toString();
            item.setNota(nota);
            ordenAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoOrdenEnviada() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_order_sent);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvMsg = dialog.findViewById(R.id.tv_order_sent_message);
        tvMsg.setText(String.format(Locale.getDefault(), "El pedido de Mesa %d fue\nrecibido por cocina.", mesaNumero));

        Button btnVolver = dialog.findViewById(R.id.btn_back_to_mesas);
        btnVolver.setOnClickListener(v -> {
            dialog.dismiss();

            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra("mesa_numero", mesaNumero);
            resultIntent.putExtra("total_orden", tvTotal.getText().toString());

            String itemsJson = new Gson().toJson(listaOrden);
            resultIntent.putExtra("items_orden", itemsJson);
            resultIntent.putExtra("nombre_informacion", "Hasiel");

            setResult(RESULT_OK, resultIntent);
            finish();
        });

        dialog.show();
    }

    private void cargarProductosDesdeServidor() {
        SesionManager sesionManager = new SesionManager(this);
        String token = "Bearer " + sesionManager.getAuthToken();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProductos(token).enqueue(new Callback<ProductoResponse>() {
            @Override
            public void onResponse(Call<ProductoResponse> call, Response<ProductoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extraemos la lista dentro de "data"
                    List<Producto> productos = response.body().getData();

                    if (productos != null) {
                        listaProductosCompleta.clear();
                        listaProductosCompleta.addAll(productos);
                        filtrarProductos(categoriaSeleccionada);
                    }
                } else {
                    Toast.makeText(PedidoDetalleActivity.this, "Error al cargar el menú", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductoResponse> call, Throwable t) {
                Toast.makeText(PedidoDetalleActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}