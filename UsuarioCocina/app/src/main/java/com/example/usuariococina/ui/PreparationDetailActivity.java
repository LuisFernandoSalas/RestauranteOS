package com.example.usuariococina.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.adapters.PreparationAdapter;
import com.example.usuariococina.api.ApiClient;
import com.example.usuariococina.api.LaravelApiService;
import com.example.usuariococina.models.Order;
import com.example.usuariococina.models.OrderItem;
import com.example.usuariococina.R;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationDetailActivity extends AppCompatActivity {

    private RecyclerView rvPrepItems;
    private TextView tvPrepTitle;
    private TextView tvPrepMesaWaiter;

    private com.google.android.material.button.MaterialButton selectedReasonBtn = null;
    private com.google.android.material.button.MaterialButton selectedDurationBtn = null;

    private List<OrderItem> currentOrderItems = new ArrayList<>();

    // 🚀 NUEVO: Declaramos el adaptador
    private PreparationAdapter adapter;
    private int currentOrderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparation_detail);

        rvPrepItems = findViewById(R.id.rvPrepItems);
        tvPrepTitle = findViewById(R.id.tvPrepTitle);
        tvPrepMesaWaiter = findViewById(R.id.tvPrepMesaWaiter);

        currentOrderId = getIntent().getIntExtra("ORDER_ID", -1);
        int tableNumber = getIntent().getIntExtra("TABLE_NUMBER", 0);
        String waiterName = getIntent().getStringExtra("WAITER_NAME");

        tvPrepTitle.setText("Mesa " + tableNumber);
        tvPrepMesaWaiter.setText("Mesero: " + waiterName);

        // 🚀 1. PRIMERO creamos el adaptador y lo pegamos al RecyclerView
        rvPrepItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreparationAdapter(this, currentOrderItems);
        rvPrepItems.setAdapter(adapter);

        // 🚀 2. DESPUÉS recibimos los datos y le avisamos al adaptador
        Order pedidoSeleccionado = (Order) getIntent().getSerializableExtra("PEDIDO_COMPLETO");

        if (pedidoSeleccionado != null && pedidoSeleccionado.getItems() != null) {
            currentOrderItems.clear();
            currentOrderItems.addAll(pedidoSeleccionado.getItems());
            adapter.notifyDataSetChanged(); // ¡Ahora sí el adaptador existe y no explota!
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCompleteOrder).setOnClickListener(v -> showOrderCompletedDialog());
        findViewById(R.id.btnCancelOrder).setOnClickListener(v -> showCancelOrderDialog());
        findViewById(R.id.btnPauseOrder).setOnClickListener(v -> showPauseProductDialog());
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
            dialog.dismiss();
            finish();
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
            if (selectedReasonBtn == null) {
                Toast.makeText(this, "Por favor selecciona un motivo", Toast.LENGTH_SHORT).show();
                return;
            }

            String motivo = etReason.getText().toString();

            LaravelApiService apiService = ApiClient.getApiService(PreparationDetailActivity.this);
            apiService.cancelarPedido(currentOrderId, motivo).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PreparationDetailActivity.this, "Orden cancelada exitosamente", Toast.LENGTH_SHORT).show();
                        selectedReasonBtn = null;
                        dialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(PreparationDetailActivity.this, "Error al cancelar en el servidor", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(PreparationDetailActivity.this, "Fallo de conexión", Toast.LENGTH_SHORT).show();
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

        for (OrderItem item : currentOrderItems) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(item.getName());
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
            String duration = selectedDurationBtn.getText().toString();

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("⚠️ Confirmar Pausa Crítica")
                    .setMessage("¿Estás seguro de pausar '" + productName + "'?\n\nEsta acción eliminará el producto del menú digital inmediatamente por " + duration + ".")
                    .setPositiveButton("Confirmar Pausa", (d, which) -> {
                        Toast.makeText(this, "Producto pausado: " + productName, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Regresar", null)
                    .show();
        });

        dialog.show();
    }
}