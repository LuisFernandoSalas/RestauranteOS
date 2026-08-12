package com.example.usuariococina.ui;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.adapters.PrepItemsAdapter;
import com.example.usuariococina.api.apiClient;
import com.example.usuariococina.api.apiService;
import com.example.usuariococina.models.DetallePedido;
import com.example.usuariococina.R;
import com.example.usuariococina.models.PedidoResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Activity que gestiona el detalle de preparación de una orden específica.
 * Permite a cocina marcar platos como listos, cancelar órdenes o pausar productos del menú.
 */
public class PreparationDetailActivity extends AppCompatActivity {

    private RecyclerView rvPrepItems;
    private TextView tvPrepTitle;
    private TextView tvPrepMesaWaiter;

    // Almacenan la referencia al botón seleccionado en los diálogos para gestionar su estilo visual
    private com.google.android.material.button.MaterialButton selectedReasonBtn = null;
    private com.google.android.material.button.MaterialButton selectedDurationBtn = null;

    // Lista local de los productos que componen la orden actual usando el nuevo modelo de la API
    private List<DetallePedido> currentOrderItems = new ArrayList<>();

    private PedidoResponse pedidoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparation_detail);

        // Inicialización de componentes de la UI
        rvPrepItems = findViewById(R.id.rvPrepItems);
        tvPrepTitle = findViewById(R.id.tvPrepTitle);
        tvPrepMesaWaiter = findViewById(R.id.tvPrepMesaWaiter);

        // Configuración de listeners para los botones de acción
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCompleteOrder).setOnClickListener(v -> showOrderCompletedDialog());
        findViewById(R.id.btnCancelOrder).setOnClickListener(v -> showCancelOrderDialog());
        findViewById(R.id.btnPauseOrder).setOnClickListener(v -> showPauseProductDialog());

        // --- AQUÍ EMPIEZA LA MAGIA REAL ---
        String pedidoJson = getIntent().getStringExtra("PEDIDO_DATA");

        if (pedidoJson != null) {
            // 1. Transformamos el texto de vuelta a nuestro objeto
            pedidoActual = new com.google.gson.Gson().fromJson(pedidoJson, PedidoResponse.class);

            // 2. Actualizamos los textos de arriba con la info real
            if (tvPrepTitle != null) {
                tvPrepTitle.setText("Mesa " + pedidoActual.getMesa());
            }
            if (tvPrepMesaWaiter != null) {
                String meseroNombre = pedidoActual.getMesero() != null ? pedidoActual.getMesero() : "Sin asignar";
                tvPrepMesaWaiter.setText("Mesero: " + meseroNombre);
            }

            // 3. Pasamos los platillos reales al adaptador e inyectamos la petición Retrofit
            if (pedidoActual.getPlatillos() != null) {
                currentOrderItems = pedidoActual.getPlatillos();
                PrepItemsAdapter adapter = new PrepItemsAdapter(pedidoActual.getPlatillos(), new PrepItemsAdapter.OnItemStatusChangeListener() {
                    @Override
                    public void onStatusChange(int detalleId, String nuevoEstado) {

                        // CORRECCIÓN 1: Credenciales correctas
                        android.content.SharedPreferences sharedPreferences = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE);
                        String token = sharedPreferences.getString("AUTH_TOKEN", "");
                        String authToken = "Bearer " + token;

                        apiService api = apiClient.getClient(token).create(apiService.class);
                        Call<ResponseBody> call = api.cambiarEstadoPlatillo(authToken, detalleId, nuevoEstado);

                        call.enqueue(new retrofit2.Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    android.util.Log.d("API_COCINA", "¡Platillo " + detalleId + " actualizado a " + nuevoEstado + "!");
                                } else {
                                    android.util.Log.e("API_COCINA", "Error de servidor: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                android.util.Log.e("API_COCINA", "Falla de red: " + t.getMessage());
                            }
                        });
                    }
                });

                rvPrepItems.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
                rvPrepItems.setAdapter(adapter);
            }
        } else {
            android.util.Log.e("Cocina_Detail", "No llegó ningún dato de la orden.");
        }
    }

    private void showOrderCompletedDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_order_completed, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnReturnToPanel).setOnClickListener(v -> {

            // CORRECCIÓN 2: Credenciales correctas
            android.content.SharedPreferences prefs = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE);
            String token = prefs.getString("AUTH_TOKEN", "");
            String authToken = "Bearer " + token;

            apiService api = apiClient.getClient(token).create(apiService.class);

            Call<ResponseBody> call = api.cambiarEstadoPedidoCompleto(authToken, pedidoActual.getPedidoId(), "entregado");
            call.enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(PreparationDetailActivity.this, "¡Pedido entregado!", android.widget.Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    } else {
                        android.widget.Toast.makeText(PreparationDetailActivity.this, "Error al completar: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    android.widget.Toast.makeText(PreparationDetailActivity.this, "Error de red", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showCancelOrderDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_cancel_order, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        com.google.android.material.button.MaterialButton btnOut = dialogView.findViewById(R.id.btnReasonOut);
        com.google.android.material.button.MaterialButton btnWaiter = dialogView.findViewById(R.id.btnReasonWaiter);
        com.google.android.material.button.MaterialButton btnCustomer = dialogView.findViewById(R.id.btnReasonCustomer);
        com.google.android.material.button.MaterialButton btnOther = dialogView.findViewById(R.id.btnReasonOther);
        android.widget.EditText etReason = dialogView.findViewById(R.id.etCancelReason);

        android.view.View.OnClickListener reasonClickListener = v -> {
            com.google.android.material.button.MaterialButton clickedBtn = (com.google.android.material.button.MaterialButton) v;

            if (selectedReasonBtn != null) {
                selectedReasonBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
                selectedReasonBtn.setTextColor(android.graphics.Color.parseColor("#5D4037"));
            }

            clickedBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C1440E")));
            clickedBtn.setTextColor(android.graphics.Color.parseColor("#C1440E"));
            selectedReasonBtn = clickedBtn;

            etReason.setText(clickedBtn.getText());
        };

        btnOut.setOnClickListener(reasonClickListener);
        btnWaiter.setOnClickListener(reasonClickListener);
        btnCustomer.setOnClickListener(reasonClickListener);
        btnOther.setOnClickListener(reasonClickListener);

        dialogView.findViewById(R.id.btnDismissCancel).setOnClickListener(v -> {
            selectedReasonBtn = null;
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> {
            String motivo = etReason.getText().toString();

            if (motivo.isEmpty()) {
                android.widget.Toast.makeText(this, "Selecciona o escribe un motivo", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // CORRECCIÓN 3: Credenciales correctas
            android.content.SharedPreferences prefs = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE);
            String token = prefs.getString("AUTH_TOKEN", "");
            String authToken = "Bearer " + token;

            apiService api = apiClient.getClient(token).create(apiService.class);

            Call<ResponseBody> call = api.cancelarPedido(authToken, pedidoActual.getPedidoId(), motivo);
            call.enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(PreparationDetailActivity.this, "Pedido cancelado", android.widget.Toast.LENGTH_SHORT).show();
                        selectedReasonBtn = null;
                        dialog.dismiss();
                        finish();
                    } else {
                        android.widget.Toast.makeText(PreparationDetailActivity.this, "Error al cancelar: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    android.widget.Toast.makeText(PreparationDetailActivity.this, "Error de red", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showPauseProductDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_pause_product, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        com.google.android.material.chip.ChipGroup cgProducts = dialogView.findViewById(R.id.cgProductsToPause);
        com.google.android.material.button.MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmPause);
        android.widget.TextView tvSummary = dialogView.findViewById(R.id.tvPauseSummary);

        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.5f);
        selectedDurationBtn = null;

        Runnable checkValidation = () -> {
            int checkedId = cgProducts.getCheckedChipId();
            if (checkedId != -1 && selectedDurationBtn != null) {
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                com.google.android.material.chip.Chip selectedChip = dialogView.findViewById(checkedId);
                tvSummary.setText("Listo para pausar: " + selectedChip.getText() + " (" + selectedDurationBtn.getText() + ")");
                tvSummary.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                tvSummary.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            } else {
                btnConfirm.setEnabled(false);
                btnConfirm.setAlpha(0.5f);
                tvSummary.setText("⚠️ Selecciona producto y duración para continuar");
                tvSummary.setBackgroundColor(android.graphics.Color.parseColor("#FFF8E1"));
                tvSummary.setTextColor(android.graphics.Color.parseColor("#C1440E"));
            }
        };

        // Generación dinámica de Chips
        for (DetallePedido item : currentOrderItems) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);

            String nombreItem = (item.getProducto() != null) ? item.getProducto() : "Desconocido";

            chip.setText(nombreItem);
            chip.setTag(item.getDetalleId());

            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(android.graphics.Color.parseColor("#5D4037"));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E08A2B")));
                    chip.setTextColor(android.graphics.Color.parseColor("#E08A2B"));
                } else {
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
                    chip.setTextColor(android.graphics.Color.parseColor("#5D4037"));
                }
                checkValidation.run();
            });

            cgProducts.addView(chip);
        }

        int[] durationIds = {R.id.btnTime30, R.id.btnTime1h, R.id.btnTimeReact};
        for (int id : durationIds) {
            com.google.android.material.button.MaterialButton btn = dialogView.findViewById(id);
            btn.setOnClickListener(v -> {
                if (selectedDurationBtn != null) {
                    selectedDurationBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
                    selectedDurationBtn.setTextColor(android.graphics.Color.parseColor("#5D4037"));
                }
                btn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E08A2B")));
                btn.setTextColor(android.graphics.Color.parseColor("#E08A2B"));
                selectedDurationBtn = btn;
                checkValidation.run();
            });
        }

        dialogView.findViewById(R.id.btnCancelPause).setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            int checkedChipId = cgProducts.getCheckedChipId();
            com.google.android.material.chip.Chip selectedChip = dialogView.findViewById(checkedChipId);
            String productName = selectedChip.getText().toString();

            int productId = (int) selectedChip.getTag();
            String duration = selectedDurationBtn.getText().toString();

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("⚠️ Confirmar Pausa Crítica")
                    .setMessage("¿Estás seguro de pausar '" + productName + "'?\n\nEsta acción eliminará el producto del menú digital inmediatamente por " + duration + ".")
                    .setPositiveButton("Confirmar Pausa", (d, which) -> {

                        // CORRECCIÓN 4: Credenciales correctas
                        android.content.SharedPreferences prefs = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE);
                        String token = prefs.getString("AUTH_TOKEN", "");
                        String authToken = "Bearer " + token;

                        apiService api = apiClient.getClient(token).create(apiService.class);

                        Call<ResponseBody> call = api.pausarProducto(authToken, productId, duration);
                        call.enqueue(new retrofit2.Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    android.widget.Toast.makeText(PreparationDetailActivity.this, "Producto pausado: " + productName, android.widget.Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    android.widget.Toast.makeText(PreparationDetailActivity.this, "Error al pausar: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                android.widget.Toast.makeText(PreparationDetailActivity.this, "Falla de red", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });

                    })
                    .setNegativeButton("Regresar", null)
                    .show();
        });

        dialog.show();
    }

    private void setupDummyData() {
        // ... (Este método lo mantenemos intacto por si en algún momento deseas referenciarlo, aunque ya no se llama en el onCreate)
    }
}