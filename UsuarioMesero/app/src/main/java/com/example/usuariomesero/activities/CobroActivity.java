package com.example.usuariomesero.activities;

import com.example.usuariomesero.R;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.usuariomesero.database.AppDatabase;
import com.example.usuariomesero.database.MesaEntity;
import com.example.usuariomesero.database.MesaDao;
import com.example.usuariomesero.models.ItemOrden;
import com.example.usuariomesero.adapters.OrdenAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla de Cobro: Gestiona el proceso final de pago de una mesa.
 * Permite seleccionar el método de pago (Efectivo, Tarjeta, Mixto), 
 * calcular propinas y determinar el cambio a entregar.
 */
public class CobroActivity extends AppCompatActivity {

    private int mesaNumero;
    private double totalPedido = 0.0;
    private double propinaMonto = 0.0;
    private double totalACobrar = 0.0;
    private String metodoSeleccionado = "Efectivo";

    private TextView tvPropinaPorcentaje, tvPropinaMonto, tvTotalACobrarResumen, tvPagoCambio, btnCobrarFinal, tvSubtotal;
    private EditText etPagoRecibido;
    private View btnEfectivo, btnTarjeta, btnMixto;
    private RecyclerView rvDetallePedido;
    private OrdenAdapter ordenAdapter;
    private List<ItemOrden> itemsPedido = new ArrayList<>();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro);

        mesaNumero = getIntent().getIntExtra("mesa_numero", 0);
        db = AppDatabase.getInstance(this);
        
        cargarDatosMesa();

        // Inicializar vistas
        initViews();
        
        // Configurar valores iniciales
        updateTotals();
        selectMetodo("Efectivo");

        // Listeners
        setupListeners();
    }

    private void cargarDatosMesa() {
        MesaEntity entity = db.mesaDao().getMesaByNumero(mesaNumero);
        if (entity != null) {
            totalPedido = parsePrecio(entity.getPrecio());
            if (entity.getItemsPedido() != null) {
                itemsPedido = entity.getItemsPedido();
            }
        }
    }

    private void initViews() {
        TextView tvTitulo = findViewById(R.id.tv_cobro_titulo);
        if (tvTitulo != null) {
            tvTitulo.setText(String.format(Locale.getDefault(), "Cobro – Mesa %d", mesaNumero));
        }

        TextView tvMesaInfo = findViewById(R.id.tv_mesa_info_header);
        if (tvMesaInfo != null) {
            MesaEntity entity = db.mesaDao().getMesaByNumero(mesaNumero);
            String info = (entity != null && entity.getNombreInformacion() != null) ? entity.getNombreInformacion() : "Hasiel";
            tvMesaInfo.setText(String.format(Locale.getDefault(), "Mesa %d — %s", mesaNumero, info));
        }

        tvSubtotal = findViewById(R.id.tv_subtotal_cobro);
        if (tvSubtotal != null) {
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", totalPedido));
        }

        rvDetallePedido = findViewById(R.id.rv_detalle_pedido_cobro);
        if (rvDetallePedido != null) {
            ordenAdapter = new OrdenAdapter(itemsPedido, null); // Sin listeners porque es solo vista
            rvDetallePedido.setLayoutManager(new LinearLayoutManager(this));
            rvDetallePedido.setAdapter(ordenAdapter);
        }

        btnEfectivo = findViewById(R.id.btn_efectivo);
        btnTarjeta = findViewById(R.id.btn_tarjeta);
        btnMixto = findViewById(R.id.btn_mixto);

        tvPropinaPorcentaje = findViewById(R.id.tv_propina_porcentaje);
        tvPropinaMonto = findViewById(R.id.tv_propina_monto);
        tvTotalACobrarResumen = findViewById(R.id.tv_total_a_cobrar_resumen);
        tvPagoCambio = findViewById(R.id.tv_pago_cambio);
        btnCobrarFinal = findViewById(R.id.btn_cobrar_final);
        etPagoRecibido = findViewById(R.id.et_pago_recibido);

        findViewById(R.id.btn_back_cobro).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        if (btnEfectivo != null) btnEfectivo.setOnClickListener(v -> selectMetodo("Efectivo"));
        if (btnTarjeta != null) btnTarjeta.setOnClickListener(v -> selectMetodo("Tarjeta"));
        if (btnMixto != null) btnMixto.setOnClickListener(v -> selectMetodo("Mixto"));

        View propinaSelector = findViewById(R.id.layout_propina_selector);
        if (propinaSelector != null) propinaSelector.setOnClickListener(this::showPropinaMenu);

        View btnClearPropina = findViewById(R.id.btn_clear_propina);
        if (btnClearPropina != null) {
            btnClearPropina.setOnClickListener(v -> {
                propinaMonto = 0;
                if (tvPropinaPorcentaje != null) tvPropinaPorcentaje.setText("0%");
                updateTotals();
            });
        }

        if (etPagoRecibido != null) {
            etPagoRecibido.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateChange();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnCobrarFinal != null) {
            btnCobrarFinal.setOnClickListener(v -> {
                if ("Efectivo".equals(metodoSeleccionado)) {
                    double recibido = getPagoRecibido();
                    if (recibido < totalACobrar) {
                        Toast.makeText(this, "El pago recibido es insuficiente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                mostrarDialogoConfirmarCobro();
            });
        }
    }

    private void mostrarDialogoConfirmarCobro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar cobro");
        builder.setMessage("¿Desea realizar el cobro para la Mesa " + mesaNumero + "?");
        builder.setPositiveButton("Sí, cobrar", (dialog, which) -> {
            lanzarResumen();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private final ActivityResultLauncher<android.content.Intent> resumenLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Si se aceptó el resumen, finalizamos esta actividad y notificamos éxito
                    android.content.Intent resultIntent = new android.content.Intent();
                    resultIntent.putExtra("mesa_numero", mesaNumero);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
    );

    private void lanzarResumen() {
        android.content.Intent intent = new android.content.Intent(this, ResumenCobroActivity.class);
        intent.putExtra("total", totalACobrar);
        intent.putExtra("subtotal", totalPedido);
        intent.putExtra("propina_monto", propinaMonto);
        if (tvPropinaPorcentaje != null) {
            intent.putExtra("propina_label", tvPropinaPorcentaje.getText().toString());
        }
        intent.putExtra("metodo", metodoSeleccionado);
        intent.putExtra("recibido", etPagoRecibido.getText().toString());
        
        android.widget.CheckBox cbFactura = findViewById(R.id.cb_solicitar_factura);
        if (cbFactura != null) {
            intent.putExtra("factura", cbFactura.isChecked());
        }

        resumenLauncher.launch(intent);
    }

    private void selectMetodo(String metodo) {
        metodoSeleccionado = metodo;
        
        // Reset backgrounds
        if (btnEfectivo != null) {
            btnEfectivo.setBackgroundResource(R.drawable.bg_button_payment_outline);
            ((TextView)btnEfectivo).setTextColor(0xFF888888);
        }
        if (btnTarjeta != null) {
            btnTarjeta.setBackgroundResource(R.drawable.bg_button_payment_outline);
            ((TextView)btnTarjeta).setTextColor(0xFF888888);
        }
        if (btnMixto != null) {
            btnMixto.setBackgroundResource(R.drawable.bg_button_payment_outline);
            ((TextView)btnMixto).setTextColor(0xFF888888);
        }

        // Set selected
        View selected = null;
        if (metodo.equals("Efectivo")) selected = btnEfectivo;
        else if (metodo.equals("Tarjeta")) selected = btnTarjeta;
        else if (metodo.equals("Mixto")) selected = btnMixto;

        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_button_efectivo);
            ((TextView)selected).setTextColor(ContextCompat.getColor(this, R.color.terracota_oscuro));
        }
    }

    private void showPropinaMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("5%");
        popup.getMenu().add("10%");
        popup.getMenu().add("15%");
        popup.getMenu().add("20%");
        popup.getMenu().add("Otra cantidad");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Otra cantidad")) {
                mostrarDialogoOtraPropina();
            } else {
                int percent = Integer.parseInt(title.replace("%", ""));
                setPropina(percent, title);
            }
            return true;
        });
        popup.show();
    }

    private void mostrarDialogoOtraPropina() {
        EditText etMonto = new EditText(this);
        etMonto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etMonto.setHint("Ej. 12");

        // Contenedor para dar padding al EditText en el diálogo
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        int paddingPx = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, (int) (8 * getResources().getDisplayMetrics().density), paddingPx, 0);
        container.addView(etMonto);

        new AlertDialog.Builder(this)
            .setTitle("Porcentaje de Propina")
            .setMessage("Ingresa el porcentaje deseado:")
            .setView(container)
            .setPositiveButton("Aplicar", (d, w) -> {
                String val = etMonto.getText().toString();
                if (!val.isEmpty()) {
                    try {
                        int percent = Integer.parseInt(val);
                        setPropina(percent, percent + "%");
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Número inválido", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void setPropina(int porcentaje, String label) {
        propinaMonto = totalPedido * (porcentaje / 100.0);
        if (tvPropinaPorcentaje != null) tvPropinaPorcentaje.setText(label);
        updateTotals();
    }

    private void updateTotals() {
        totalACobrar = totalPedido + propinaMonto;
        if (tvPropinaMonto != null) {
            tvPropinaMonto.setText(String.format(Locale.getDefault(), "$%.2f", propinaMonto));
        }
        
        String totalStr = String.format(Locale.getDefault(), "$%.2f", totalACobrar);
        if (tvTotalACobrarResumen != null) {
            tvTotalACobrarResumen.setText("Total a cobrar " + totalStr);
        }
        if (btnCobrarFinal != null) {
            btnCobrarFinal.setText("Cobrar " + totalStr);
        }
        calculateChange();
    }

    private void calculateChange() {
        double recibido = getPagoRecibido();
        double cambio = recibido - totalACobrar;
        if (cambio < 0) cambio = 0;
        if (tvPagoCambio != null) {
            tvPagoCambio.setText(String.format(Locale.getDefault(), "$%.2f", cambio));
        }
    }

    private double getPagoRecibido() {
        String s = etPagoRecibido.getText().toString();
        if (s.isEmpty()) return 0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parsePrecio(String precio) {
        if (precio == null || precio.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(precio.replace("$", "").replace(" ", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
