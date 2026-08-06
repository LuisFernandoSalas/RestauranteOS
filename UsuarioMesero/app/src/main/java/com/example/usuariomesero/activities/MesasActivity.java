package com.example.usuariomesero.activities;

import com.example.usuariomesero.R;
import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.adapters.MesaAdapter;
import com.example.usuariomesero.models.ItemOrden;
import com.example.usuariomesero.network.ApiService;
import com.example.usuariomesero.network.RetrofitClient;
import com.example.usuariomesero.utils.SesionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MesasActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvMesas;
    private MesaAdapter mesaAdapter;
    private List<Mesa> mesaList;
    private SesionManager sesionManager;

    private final ActivityResultLauncher<Intent> genericLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int mesaNumero = result.getData().getIntExtra("mesa_numero", -1);
                    if (mesaNumero != -1) {
                        String total = result.getData().getStringExtra("total_orden");
                        String itemsJson = result.getData().getStringExtra("items_orden");
                        String info = result.getData().getStringExtra("nombre_informacion");

                        if (total != null) {
                            List<ItemOrden> items = null;
                            if (itemsJson != null) {
                                Type listType = new TypeToken<List<ItemOrden>>() {}.getType();
                                items = new Gson().fromJson(itemsJson, listType);
                            }
                            actualizarMesaAOcupada(mesaNumero, total, items, info);
                        } else {
                            liberarMesa(mesaNumero);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);

        sesionManager = new SesionManager(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        rvMesas = findViewById(R.id.rv_mesas);

        setupRecyclerView();
        setupNavigationDrawer();
        setupCloseButton();
        setupDynamicUserData();

        // ¡Llamamos a la API real en lugar de datos falsos!
        cargarMesasDesdeAPI();
    }

    private void cargarMesasDesdeAPI() {
        String token = sesionManager.getAuthToken();
        if (token == null) {
            cerrarSesion();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(token).create(ApiService.class);
        apiService.getMesas().enqueue(new Callback<List<Mesa>>() {
            @Override
            public void onResponse(Call<List<Mesa>> call, Response<List<Mesa>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mesaList.clear(); // Limpiamos lista actual
                    mesaList.addAll(response.body()); // Agregamos datos de Laravel
                    mesaAdapter.notifyDataSetChanged(); // Refrescamos UI
                } else if (response.code() == 401) {
                    Toast.makeText(MesasActivity.this, "Sesión caducada", Toast.LENGTH_SHORT).show();
                    cerrarSesion();
                }
            }

            @Override
            public void onFailure(Call<List<Mesa>> call, Throwable t) {
                Toast.makeText(MesasActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cerrarSesion() {
        sesionManager.clearSession();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerView() {
        mesaList = new ArrayList<>();
        mesaAdapter = new MesaAdapter(mesaList, mesa -> {
            if (mesa.getEstado() == Mesa.Estado.LIBRE) {
                Intent intent = new Intent(MesasActivity.this, PedidoDetalleActivity.class);
                intent.putExtra("mesa_numero", mesa.getNumero());
                genericLauncher.launch(intent);
            } else if (mesa.getEstado() == Mesa.Estado.OCUPADA) {
                mostrarDialogoConfirmarCobro(mesa);
            } else if (mesa.getEstado() == Mesa.Estado.COBRO) {
                Intent intent = new Intent(MesasActivity.this, CobroActivity.class);
                intent.putExtra("mesa_numero", mesa.getNumero());
                intent.putExtra("total_pedido", mesa.getPrecio());
                genericLauncher.launch(intent);
            }
        });
        rvMesas.setLayoutManager(new GridLayoutManager(this, 3));
        rvMesas.setAdapter(mesaAdapter);
    }

    private void mostrarDialogoConfirmarCobro(Mesa mesa) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_charge);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        android.widget.TextView tvMsg = dialog.findViewById(R.id.tv_confirm_charge_message);
        tvMsg.setText(String.format(java.util.Locale.getDefault(), "Se solicitará el cobro para la Mesa %d.", mesa.getNumero()));

        android.widget.Button btnConfirmar = dialog.findViewById(R.id.btn_confirm_charge);
        android.widget.Button btnCancelar = dialog.findViewById(R.id.btn_cancel_charge);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            mesa.setEstado(Mesa.Estado.COBRO);
            mesaAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Estado de Mesa " + mesa.getNumero() + " actualizado a Cobro Pendiente", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void actualizarMesaAOcupada(int numeroMesa, String total, List<ItemOrden> items, String info) {
        for (Mesa mesa : mesaList) {
            if (mesa.getNumero() == numeroMesa) {
                mesa.setEstado(Mesa.Estado.OCUPADA);
                mesa.setPrecio(total);
                mesa.setItemsPedido(items);
                mesa.setNombreInformacion(info);
                mesaAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void liberarMesa(int numeroMesa) {
        for (Mesa mesa : mesaList) {
            if (mesa.getNumero() == numeroMesa) {
                mesa.setEstado(Mesa.Estado.LIBRE);
                mesa.setPrecio(null);
                mesa.setItemsPedido(null);
                mesa.setNombreInformacion(null);
                mesaAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void setupDynamicUserData() {
        String nombreUsuario = getIntent().getStringExtra("usuario_nombre");
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            TextView tvWelcomeToolbar = findViewById(R.id.tv_welcome_user_toolbar);
            if (tvWelcomeToolbar != null) {
                tvWelcomeToolbar.setText("Hola " + nombreUsuario + "!");
            }

            View navFooter = findViewById(R.id.nav_container_mesas);
            if (navFooter != null) {
                TextView tvNombreFooter = navFooter.findViewById(R.id.tv_user_name_footer);
                TextView tvAvatar = navFooter.findViewById(R.id.tv_avatar);

                if (tvNombreFooter != null) {
                    tvNombreFooter.setText(nombreUsuario);
                }

                if (tvAvatar != null) {
                    String initials = "";
                    String[] parts = nombreUsuario.split(" ");
                    if (parts.length > 0 && !parts[0].isEmpty()) {
                        initials += parts[0].substring(0, 1).toUpperCase();
                        if (parts.length > 1 && !parts[1].isEmpty()) {
                            initials += parts[1].substring(0, 1).toUpperCase();
                        }
                    }
                    tvAvatar.setText(initials);
                }
            }
        }
    }

    private void setupCloseButton() {
        android.view.View closeButton = findViewById(R.id.btn_close_drawer);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }
            });
        }
    }

    private void setupNavigationDrawer() {
        navigationView.setCheckedItem(R.id.nav_mesas);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    public void openDrawerClick(android.view.View view) {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}