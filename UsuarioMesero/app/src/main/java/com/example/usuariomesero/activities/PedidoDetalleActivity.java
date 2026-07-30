package com.example.usuariomesero.activities;

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

import com.example.usuariomesero.R;
import com.example.usuariomesero.adapters.OrdenAdapter;
import com.example.usuariomesero.adapters.ProductoAdapter;
import com.example.usuariomesero.api.ApiClient;
import com.example.usuariomesero.api.ApiResponse;
import com.example.usuariomesero.api.ApiService;
import com.example.usuariomesero.models.ItemOrden;
import com.example.usuariomesero.models.Producto;
import com.example.usuariomesero.models.ItemOrdenRequest; // 🚀 Importado
import com.example.usuariomesero.models.OrdenRequest;     // 🚀 Importado
import com.example.usuariomesero.models.RespuestaEnvio;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoDetalleActivity extends AppCompatActivity {

    private RecyclerView rvProductos, rvOrden;
    private ProductoAdapter productoAdapter;
    private OrdenAdapter ordenAdapter;
    private List<Producto> listaProductosCompleta;
    private List<Producto> listaProductosFiltrada;
    private List<ItemOrden> listaOrden;
    private TextView tvTotal, tvMesaTitulo;
    private String categoriaSeleccionada = "Platos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_detalle);

        int mesaNumero = getIntent().getIntExtra("mesa_numero", 0);

        tvMesaTitulo = findViewById(R.id.tv_mesa_titulo);
        tvMesaTitulo.setText(String.format(Locale.getDefault(), "Mesa %d - Pedido", mesaNumero));

        rvProductos = findViewById(R.id.rv_productos_seleccion);
        rvOrden = findViewById(R.id.rv_orden_actual);
        tvTotal = findViewById(R.id.tv_total_orden);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_mandar_orden).setOnClickListener(v -> {
            if (listaOrden.isEmpty()) {
                Toast.makeText(this, "Agrega productos a la orden primero", Toast.LENGTH_SHORT).show();
            } else {
                // 🚀 En lugar de solo mostrar el diálogo, lanzamos la orden a Laravel
                enviarOrdenALaravel();
            }
        });

        setupRecyclerViews();
        setupCategoryButtons();
        cargarMenuDesdeAPI();
    }

    // 🚀 NUEVO MÉTODO: Convierte la orden a JSON y la manda por POST
    private void enviarOrdenALaravel() {
        int mesaNumero = getIntent().getIntExtra("mesa_numero", 0);
        List<ItemOrdenRequest> itemsParaEnviar = new ArrayList<>();

        // 1. Traducimos tu lista de Android a la lista limpia para Laravel
        for (ItemOrden item : listaOrden) {
            itemsParaEnviar.add(new ItemOrdenRequest(
                    item.getProducto().getId(), // Tu clase Producto debe tener el método getId()
                    item.getCantidad(),
                    item.getNota() != null ? item.getNota() : ""
            ));
        }

        // 2. Metemos todo al "sobre" principal
        OrdenRequest ordenRequest = new OrdenRequest(mesaNumero, itemsParaEnviar);

        // 3. Disparamos a Laravel
        ApiService api = ApiClient.getService(this);
        api.enviarComandaACocina(ordenRequest).enqueue(new retrofit2.Callback<RespuestaEnvio>() { // 🚀 CAMBIO AQUÍ
            @Override
            public void onResponse(retrofit2.Call<RespuestaEnvio> call, retrofit2.Response<RespuestaEnvio> response) { // 🚀 CAMBIO AQUÍ
                if (response.isSuccessful()) {
                    mostrarDialogoOrdenEnviada();
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorDeLaravel = response.errorBody().string();
                            android.util.Log.e("ERROR_422_DETALLE", errorDeLaravel);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(PedidoDetalleActivity.this, "Error de servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RespuestaEnvio> call, Throwable t) { // 🚀 CAMBIO AQUÍ
                Toast.makeText(PedidoDetalleActivity.this, "Fallo de red al enviar el pedido", Toast.LENGTH_SHORT).show();
                android.util.Log.e("ERROR_ENVIO", t.getMessage());
            }
        });
    }

    private void cargarMenuDesdeAPI() {
        ApiService api = ApiClient.getService(this);

        api.obtenerProductos().enqueue(new retrofit2.Callback<ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse> call, retrofit2.Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Producto> productosServidor = response.body().getData();

                    if (productosServidor != null) {
                        listaProductosCompleta.clear();

                        for (Producto p : productosServidor) {
                            if ("activo".equalsIgnoreCase(p.getStatus())) {
                                p.setImagenResId(com.example.usuariomesero.R.drawable.ic_launcher_background);
                                listaProductosCompleta.add(p);
                            }
                        }

                        filtrarProductos(categoriaSeleccionada);
                    }
                } else {
                    Toast.makeText(PedidoDetalleActivity.this, "Error en el servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse> call, Throwable t) {
                Toast.makeText(PedidoDetalleActivity.this, "Fallo de red al conectar el menú", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViews() {
        listaProductosCompleta = new ArrayList<>();
        listaProductosFiltrada = new ArrayList<>();
        productoAdapter = new ProductoAdapter(listaProductosFiltrada, producto -> {
            agregarAOrden(producto);
        });

        int spanCount = 2;
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

        if (btnEntradas != null) btnEntradas.setOnClickListener(v -> updateCategorySelection("Entradas", btnEntradas, btnPlatos, btnBebidas, btnPostres));

        // ⚠️ OJO AQUÍ: Tienes "Platos fuertes" en el click, pero abajo lo inicializas como "Platos".
        // Si tu base de datos dice "Platos", cambia esto a "Platos" para que no se blanquee la pantalla.
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
            if (p.getCategoria() != null && p.getCategoria().getNombre().equalsIgnoreCase(categoria)) {
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
        int mesaNumero = getIntent().getIntExtra("mesa_numero", 0);
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
}