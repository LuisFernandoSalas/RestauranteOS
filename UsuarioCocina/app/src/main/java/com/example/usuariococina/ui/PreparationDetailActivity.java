package com.example.usuariococina.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.models.OrderItem;
import com.example.usuariococina.R;
import com.example.usuariococina.adapters.PrepItemsAdapter;

import java.util.ArrayList;
import java.util.List;

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
    
    // Lista local de los productos que componen la orden actual
    private List<OrderItem> currentOrderItems = new ArrayList<>();

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

    }

    /**
     * Muestra el diálogo de éxito cuando se completa una comanda.
     */
    private void showOrderCompletedDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_order_completed, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Hacer el fondo transparente para respetar los bordes redondeados del layout XML
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnReturnToPanel).setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Finaliza la actividad y regresa al panel principal de cocina
        });

        dialog.show();
    }

    /**
     * Gestiona el diálogo de cancelación de orden, permitiendo seleccionar motivos predefinidos.
     */
    private void showCancelOrderDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_cancel_order, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Obtener referencias de botones de motivos
        com.google.android.material.button.MaterialButton btnOut = dialogView.findViewById(R.id.btnReasonOut);
        com.google.android.material.button.MaterialButton btnWaiter = dialogView.findViewById(R.id.btnReasonWaiter);
        com.google.android.material.button.MaterialButton btnCustomer = dialogView.findViewById(R.id.btnReasonCustomer);
        com.google.android.material.button.MaterialButton btnOther = dialogView.findViewById(R.id.btnReasonOther);
        android.widget.EditText etReason = dialogView.findViewById(R.id.etCancelReason);

        // Listener compartido para gestionar la selección visual exclusiva de motivos
        android.view.View.OnClickListener reasonClickListener = v -> {
            com.google.android.material.button.MaterialButton clickedBtn = (com.google.android.material.button.MaterialButton) v;
            
            // Desmarcar el botón previamente seleccionado
            if (selectedReasonBtn != null) {
                selectedReasonBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
                selectedReasonBtn.setTextColor(android.graphics.Color.parseColor("#5D4037"));
            }

            // Marcar el nuevo botón con el color corporativo Terracota
            clickedBtn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#C1440E")));
            clickedBtn.setTextColor(android.graphics.Color.parseColor("#C1440E"));
            selectedReasonBtn = clickedBtn;

            // Sincronizar el texto del botón con el campo de texto editable
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
            selectedReasonBtn = null;
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    /**
     * Muestra el diálogo dinámico para pausar productos.
     * Genera chips de forma dinámica basados en la orden actual y valida selecciones obligatorias.
     */
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
        
        // Estado inicial del diálogo: Botón deshabilitado hasta que se complete la selección
        btnConfirm.setEnabled(false);
        btnConfirm.setAlpha(0.5f);
        selectedDurationBtn = null;

        // Lógica de validación dinámica (Feedback visual)
        Runnable checkValidation = () -> {
            int checkedId = cgProducts.getCheckedChipId();
            if (checkedId != -1 && selectedDurationBtn != null) {
                // Selección completa: Habilitar acción
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                com.google.android.material.chip.Chip selectedChip = dialogView.findViewById(checkedId);
                tvSummary.setText("Listo para pausar: " + selectedChip.getText() + " (" + selectedDurationBtn.getText() + ")");
                tvSummary.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9")); // Feedback Verde
                tvSummary.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            } else {
                // Selección incompleta: Bloquear acción
                btnConfirm.setEnabled(false);
                btnConfirm.setAlpha(0.5f);
                tvSummary.setText("⚠️ Selecciona producto y duración para continuar");
                tvSummary.setBackgroundColor(android.graphics.Color.parseColor("#FFF8E1")); // Feedback Alerta
                tvSummary.setTextColor(android.graphics.Color.parseColor("#C1440E"));
            }
        };

        // Generación dinámica de Chips para cada producto de la orden
        for (OrderItem item : currentOrderItems) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(item.getName());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(android.graphics.Color.parseColor("#5D4037"));
            
            // Listener para cambios de estado en el Chip
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E08A2B"))); // Ocre
                    chip.setTextColor(android.graphics.Color.parseColor("#E08A2B"));
                } else {
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D3D3D3")));
                    chip.setTextColor(android.graphics.Color.parseColor("#5D4037"));
                }
                checkValidation.run();
            });
            
            cgProducts.addView(chip);
        }

        // Gestión de botones de duración (30 min, 1h, Reactivar)
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

        // Acción final con Doble Confirmación para operaciones críticas
        btnConfirm.setOnClickListener(v -> {
            int checkedChipId = cgProducts.getCheckedChipId();
            com.google.android.material.chip.Chip selectedChip = dialogView.findViewById(checkedChipId);
            String productName = selectedChip.getText().toString();
            String duration = selectedDurationBtn.getText().toString();

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("⚠️ Confirmar Pausa Crítica")
                .setMessage("¿Estás seguro de pausar '" + productName + "'?\n\nEsta acción eliminará el producto del menú digital inmediatamente por " + duration + ".")
                .setPositiveButton("Confirmar Pausa", (d, which) -> {
                    android.widget.Toast.makeText(this, "Producto pausado: " + productName, android.widget.Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Regresar", null)
                .show();
        });

        dialog.show();
    }
}

