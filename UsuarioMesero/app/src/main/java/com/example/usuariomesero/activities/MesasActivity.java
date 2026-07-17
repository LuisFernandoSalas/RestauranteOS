package com.example.usuariomesero.activities;

import com.example.usuariomesero.R;
import com.example.usuariomesero.api.ApiClient;
import com.example.usuariomesero.api.ApiService;
import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.adapters.MesaAdapter;
import com.example.usuariomesero.models.ItemOrden;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Type;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * MesasActivity: Pantalla principal del mesero donde se visualiza el estado de las mesas.
 * 
 * Esta actividad implementa un DrawerLayout para el menú lateral y una cuadrícula 
 * (que será un RecyclerView en el futuro) para mostrar las mesas.
 */
public class MesasActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView rvMesas;
    private MesaAdapter mesaAdapter;
    private List<Mesa> mesaList;

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

        // Inicialización de componentes
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        rvMesas = findViewById(R.id.rv_mesas);

        // Configuración del RecyclerView
        setupRecyclerView();

        // Configuración del menú lateral
        setupNavigationDrawer();
        setupCloseButton();
        setupDynamicUserData();
    }

    private void setupDynamicUserData() {
        String nombreUsuario = getIntent().getStringExtra("usuario_nombre");
        if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
            // Actualizar Toolbar
            TextView tvWelcomeToolbar = findViewById(R.id.tv_welcome_user_toolbar);
            if (tvWelcomeToolbar != null) {
                tvWelcomeToolbar.setText("Hola " + nombreUsuario + "!");
            }

            // Actualizar Footer del Drawer
            // El footer está en un include, así que lo buscamos dentro del contenedor
            View navFooter = findViewById(R.id.nav_container_mesas);
            if (navFooter != null) {
                TextView tvNombreFooter = navFooter.findViewById(R.id.tv_user_name_footer);
                TextView tvAvatar = navFooter.findViewById(R.id.tv_avatar);

                if (tvNombreFooter != null) {
                    tvNombreFooter.setText(nombreUsuario);
                }

                if (tvAvatar != null) {
                    // Generar iniciales (ej. "Hasiel G." -> "HG" o "Admin" -> "A")
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
        // Buscamos el botón de cierre en cualquier parte de la jerarquía
        android.view.View closeButton = findViewById(R.id.btn_close_drawer);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }
            });
        }
    }

    /**
     * Configura el RecyclerView con un GridLayoutManager y datos de la base de datos o de prueba.
     */
    // ...
    // private AppDatabase db; <-- ¡Borra esto de la parte de arriba de tu clase!
    // ...

    private void setupRecyclerView() {
        mesaList = new ArrayList<>();

        // Configuración de los clics en las mesas
        mesaAdapter = new MesaAdapter(mesaList, mesa -> {
            if (mesa.getEstado() == Mesa.Estado.LIBRE) {
                Intent intent = new Intent(MesasActivity.this, PedidoDetalleActivity.class);
                intent.putExtra("mesa_numero", mesa.getNumero());
                genericLauncher.launch(intent);

            } else if (mesa.getEstado() == Mesa.Estado.OCUPADA) {
                if (mesa.esCobroPermitido()) {
                    mostrarDialogoConfirmarCobro(mesa);
                } else {
                    Toast.makeText(MesasActivity.this,
                            "⏳ Los platillos de la Mesa " + mesa.getNumero() + " siguen en preparación en cocina. No se puede cobrar aún.",
                            Toast.LENGTH_LONG).show();
                }

            } else if (mesa.getEstado() == Mesa.Estado.COBRO) {
                Intent intent = new Intent(MesasActivity.this, CobroActivity.class);
                intent.putExtra("mesa_numero", mesa.getNumero());
                intent.putExtra("total_pedido", mesa.getPrecioFormateado());
                genericLauncher.launch(intent);
            }
        });

        // Configuración de la cuadrícula
        int columnCount = 3;
        rvMesas.setLayoutManager(new GridLayoutManager(this, columnCount));
        rvMesas.setAdapter(mesaAdapter);

        // Traemos las mesas reales desde el servidor de Laravel
        cargarMesasDesdeAPI();
    }

    private void cargarMesasDesdeAPI() {
        ApiService api = ApiClient.getService(this);
        api.obtenerMesas().enqueue(new retrofit2.Callback<List<Mesa>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Mesa>> call, retrofit2.Response<List<Mesa>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Limpiamos la lista vieja
                    mesaList.clear();
                    // Agregamos todas las mesas que nos mandó Laravel
                    mesaList.addAll(response.body());
                    // Le decimos al Adapter que redibuje la pantalla
                    mesaAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(MesasActivity.this, "Error al cargar mesas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Mesa>> call, Throwable t) {
                Toast.makeText(MesasActivity.this, "Error de red al cargar mesas", Toast.LENGTH_LONG).show();
                System.err.println("Fallo API Mesas: " + t.getMessage());
            }
        });
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
        // Ya NO mandamos nada a Laravel aquí porque PedidoDetalleActivity ya lo hizo con éxito.
        // Solo cambiamos el estado visual localmente a OCUPADA.

        for (Mesa mesa : mesaList) {
            if (mesa.getNumero() == numeroMesa) {
                mesa.setEstado(Mesa.Estado.OCUPADA);
                mesa.setPrecio(total);

                // Le decimos al adaptador que repinte esta mesa en la pantalla
                mesaAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Mesa " + numeroMesa + " ocupada", Toast.LENGTH_SHORT).show();
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

   /**
     * Configura los eventos de clic del menú lateral y la lógica de apertura/cierre.
     */
    private void setupNavigationDrawer() {
        navigationView.setCheckedItem(R.id.nav_mesas);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_mesas) {
                    // Ya estamos en mesas
                }

                // Cerrar el menú después de seleccionar
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    /**
     * Permite abrir el menú desde el icono de hamburguesa en la UI.
     */
    public void openDrawerClick(android.view.View view) {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        // Si el menú está abierto, lo cerramos en vez de salir de la app
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
