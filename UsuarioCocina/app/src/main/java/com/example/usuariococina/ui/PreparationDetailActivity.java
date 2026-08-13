package com.example.usuariococina.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.R;
import com.example.usuariococina.adapters.PrepItemsAdapter;
import com.example.usuariococina.api.apiClient;
import com.example.usuariococina.api.apiService;
import com.example.usuariococina.models.ComandasApiResponse;
import com.example.usuariococina.models.DetallePedido;
import com.example.usuariococina.models.PedidoResponse;
import com.example.usuariococina.models.Resumen;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity que gestiona el detalle de preparación de una orden específica.
 * Permite a cocina marcar platos como listos, cancelar órdenes o pausar productos del menú.
 */
public class PreparationDetailActivity extends AppCompatActivity {

    private RecyclerView rvPrepItems;
    private TextView tvPrepTitle;
    private TextView tvPrepMesaWaiter;

    // Almacenan la referencia al botón seleccionado en los diálogos para gestionar su estilo visual
    private MaterialButton selectedReasonBtn = null;
    private MaterialButton selectedDurationBtn = null;

    // Lista local de los productos que componen la orden actual
    private List<DetallePedido> currentOrderItems = new ArrayList<>();
    private PedidoResponse pedidoActual;

    // Declaramos los Textos de forma global para poder actualizarlos
    private TextView tvComandasActivas, tvPendientes, tvEnPreparacion, tvListas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparation_detail);

        // Inicialización de componentes de la UI
        rvPrepItems = findViewById(R.id.rvPrepItems);
        tvPrepTitle = findViewById(R.id.tvPrepTitle);
        tvPrepMesaWaiter = findViewById(R.id.tvPrepMesaWaiter);

        // Enlazamos los números del panel derecho
        tvComandasActivas = findViewById(R.id.tvComandasActivas);
        tvPendientes = findViewById(R.id.tvPendientes);
        tvEnPreparacion = findViewById(R.id.tvEnPreparacion);
        tvListas = findViewById(R.id.tvListas);

        if (tvComandasActivas != null) {
            tvComandasActivas.setText("-");
            tvPendientes.setText("-");
            tvEnPreparacion.setText("-");
            tvListas.setText("-");
        }

        // Configuración de listeners para los botones de acción
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCompleteOrder).setOnClickListener(v -> showOrderCompletedDialog());
        findViewById(R.id.btnCancelOrder).setOnClickListener(v -> showCancelOrderDialog());
        findViewById(R.id.btnPauseOrder).setOnClickListener(v -> showPauseProductDialog());

        // Cargar métricas del panel
        cargarResumenFresco();

        // Recepción y renderizado de la orden recibida por Intent
        String pedidoJson = getIntent().getStringExtra("PEDIDO_DATA");

        if (pedidoJson != null) {
            pedidoActual = new Gson().fromJson(pedidoJson, PedidoResponse.class);

            if (tvPrepTitle != null) {
                tvPrepTitle.setText("Mesa " + pedidoActual.getMesa());
            }
            if (tvPrepMesaWaiter != null) {
                String meseroNombre = pedidoActual.getMesero() != null ? pedidoActual.getMesero() : "Sin asignar";
                tvPrepMesaWaiter.setText("Mesero: " + meseroNombre);
            }

            if (pedidoActual.getPlatillos() != null) {
                currentOrderItems = pedidoActual.getPlatillos();
                PrepItemsAdapter adapter = new PrepItemsAdapter(currentOrderItems, new PrepItemsAdapter.OnItemStatusChangeListener() {
                    @Override
                    public void onStatusChange(int detalleId, String nuevoEstado) {

                        String token = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE).getString("AUTH_TOKEN", "");
                        String authToken = "Bearer " + token;

                        apiService api = apiClient.getClient(token).create(apiService.class);
                        Call<ResponseBody> call = api.cambiarEstadoPlatillo(authToken, detalleId, nuevoEstado);

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    Log.d("API_COCINA", "¡Platillo " + detalleId + " actualizado a " + nuevoEstado + "!");
                                    cargarResumenFresco();
                                } else {
                                    Toast.makeText(PreparationDetailActivity.this, "No se puede actualizar el platillo", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.e("API_COCINA", "Falla de red: " + t.getMessage());
                            }
                        });
                    }
                });

                rvPrepItems.setLayoutManager(new LinearLayoutManager(this));
                rvPrepItems.setAdapter(adapter);
            }
        }
    }

    private void cargarResumenFresco() {
        String token = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE).getString("AUTH_TOKEN", "");
        if (token.isEmpty()) return;

        apiService api = apiClient.getClient(token).create(apiService.class);
        Call<ComandasApiResponse> call = api.obtenerPedidosCocina();

        call.enqueue(new Callback<ComandasApiResponse>() {
            @Override
            public void onResponse(Call<ComandasApiResponse> call, Response<ComandasApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Resumen resumen = response.body().getResumen();
                    if (resumen != null && tvComandasActivas != null) {
                        tvComandasActivas.setText(String.valueOf(resumen.getTotalComandas()));
                        tvPendientes.setText(String.valueOf(resumen.getPendientes()));
                        tvEnPreparacion.setText(String.valueOf(resumen.getEnPreparacion()));
                        tvListas.setText(String.valueOf(resumen.getPausados()));
                    }
                }
            }

            @Override
            public void onFailure(Call<ComandasApiResponse> call, Throwable t) {
                Log.e("Cocina_Detail", "Falla al descargar resumen: " + t.getMessage());
            }
        });
    }

    private void showOrderCompletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_order_completed, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnReturnToPanel).setOnClickListener(v -> {
            String token = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE).getString("AUTH_TOKEN", "");
            String authToken = "Bearer " + token;

            apiService api = apiClient.getClient(token).create(apiService.class);
            Call<ResponseBody> call = api.cambiarEstadoPedidoCompleto(authToken, pedidoActual.getPedidoId(), "listo");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PreparationDetailActivity.this, "¡Mesa completa marcada como lista!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(PreparationDetailActivity.this, "Error de Laravel: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PreparationDetailActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showCancelOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        MaterialButton btnOut = dialogView.findViewById(R.id.btnReasonOut);
        MaterialButton btnWaiter = dialogView.findViewById(R.id.btnReasonWaiter);
        MaterialButton btnCustomer = dialogView.findViewById(R.id.btnReasonCustomer);
        MaterialButton btnOther = dialogView.findViewById(R.id.btnReasonOther);
        EditText etReason = dialogView.findViewById(R.id.etCancelReason);

        View.OnClickListener reasonClickListener = v -> {
            MaterialButton clickedBtn = (MaterialButton) v;

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
            String motivo = etReason.getText().toString().trim();

            if (motivo.isEmpty()) {
                Toast.makeText(this, "Selecciona o escribe un motivo", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE).getString("AUTH_TOKEN", "");
            String authToken = "Bearer " + token;

            apiService api = apiClient.getClient(token).create(apiService.class);
            Call<ResponseBody> call = api.cancelarPedido(authToken, pedidoActual.getPedidoId(), motivo);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PreparationDetailActivity.this, "Pedido cancelado con éxito", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish(); // Cerramos la pantalla al cancelar la orden completa
                    } else {
                        Toast.makeText(PreparationDetailActivity.this, "Error al cancelar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PreparationDetailActivity.this, "Falla de red", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showPauseProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pause_product, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ChipGroup cgProducts = dialogView.findViewById(R.id.cgProductsToPause);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmPause);
        TextView tvSummary = dialogView.findViewById(R.id.tvPauseSummary);

        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.5f);
        selectedDurationBtn = null;

        Runnable checkValidation = () -> {
            int checkedId = cgProducts.getCheckedChipId();
            if (checkedId != -1 && selectedDurationBtn != null) {
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                Chip selectedChip = dialogView.findViewById(checkedId);
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
            Chip chip = new Chip(this);
            String nombreItem = (item.getProducto() != null) ? item.getProducto() : "Desconocido";

            chip.setText(nombreItem);
            chip.setTag(item.getProductoId());

            boolean yaPausado = "pausado".equalsIgnoreCase(item.getEstadoPlatillo());

            if (yaPausado) {
                chip.setText(nombreItem + " (Pausado)");
                chip.setEnabled(false);
                chip.setAlpha(0.5f);
                chip.setChipBackgroundColorResource(android.R.color.darker_gray);
            } else {
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
            }

            cgProducts.addView(chip);
        }

        int[] durationIds = {R.id.btnTime30, R.id.btnTime1h, R.id.btnTimeReact};
        for (int id : durationIds) {
            MaterialButton btn = dialogView.findViewById(id);
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
            Chip selectedChip = dialogView.findViewById(checkedChipId);

            final String productName = selectedChip.getText().toString();
            final int productId = (int) selectedChip.getTag();
            String uiDuration = selectedDurationBtn.getText().toString();

            final String durationParaLaravel;
            if ("30 min".equals(uiDuration)) {
                durationParaLaravel = "30_min";
            } else if ("1 hora".equals(uiDuration)) {
                durationParaLaravel = "1_hora";
            } else {
                durationParaLaravel = "indefinido";
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("⚠️ Confirmar Pausa Crítica")
                    .setMessage("¿Estás seguro de pausar '" + productName + "'?\n\nEsta acción eliminará el producto del menú digital inmediatamente por " + uiDuration + ".")
                    .setPositiveButton("Confirmar Pausa", (d, which) -> {

                        String token = getSharedPreferences("CocinaAppPrefs", MODE_PRIVATE).getString("AUTH_TOKEN", "");
                        String authToken = "Bearer " + token;

                        apiService api = apiClient.getClient(token).create(apiService.class);
                        Call<ResponseBody> call = api.pausarProducto(authToken, productId, durationParaLaravel);

                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(PreparationDetailActivity.this, "Producto pausado: " + productName, Toast.LENGTH_SHORT).show();

                                    // ACTUALIZAMOS LA LISTA LOCAL EN PANTALLA
                                    for (DetallePedido item : currentOrderItems) {
                                        if (item.getProductoId() == productId) {
                                            item.setEstadoPlatillo("pausado");
                                        }
                                    }

                                    // NOTIFICAMOS AL RECYCLERVIEW
                                    if (rvPrepItems.getAdapter() != null) {
                                        rvPrepItems.getAdapter().notifyDataSetChanged();
                                    }

                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(PreparationDetailActivity.this, "Error al pausar: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(PreparationDetailActivity.this, "Falla de red", Toast.LENGTH_SHORT).show();
                            }
                        });

                    })
                    .setNegativeButton("Regresar", null)
                    .show();
        });

        dialog.show();
    }
}