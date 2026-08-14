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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariomesero.models.ItemOrden;
import com.example.usuariomesero.adapters.OrdenAdapter;
import com.example.usuariomesero.models.Mesa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pantalla de Cobro: (Cascarón visual - Lógica de API pendiente)
 */
public class CobroActivity extends AppCompatActivity {

    private int mesaNumero;
    private int pedidoId;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobro);

        mesaNumero = getIntent().getIntExtra("mesa_numero", 0);
        pedidoId = getIntent().getIntExtra("pedido_id", 1);

        // Inicializar vistas del frontend
        initViews();

        // Configurar valores iniciales
        updateTotals();
        selectMetodo("Efectivo");

        // Listeners
        setupListeners();
    }

    private void initViews() {
        TextView tvTitulo = findViewById(R.id.tv_cobro_titulo);
        if (tvTitulo != null) {
            tvTitulo.setText(String.format(Locale.getDefault(), "Cobro – Mesa %d", mesaNumero));
        }

        Mesa mesa = getIntent().getParcelableExtra("mesa");

        TextView tvMesaInfo = findViewById(R.id.tv_mesa_info_header);

        if (tvMesaInfo != null && mesa != null) {
            int numeroMesa = mesa.getNumero();
            String info = (mesa.getNombreInformacion() != null && !mesa.getNombreInformacion().isEmpty())
                    ? mesa.getNombreInformacion()
                    : "Sin información";

            tvMesaInfo.setText(String.format(Locale.getDefault(), "Mesa %d — %s", numeroMesa, info));
        }

        tvSubtotal = findViewById(R.id.tv_subtotal_cobro);
        if (tvSubtotal != null) {
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", totalPedido));
        }

        rvDetallePedido = findViewById(R.id.rv_detalle_pedido_cobro);
        if (rvDetallePedido != null) {
            ordenAdapter = new OrdenAdapter(itemsPedido, null);
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
                Toast.makeText(this, "Funcionalidad de cobro pendiente de conectar a API", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void selectMetodo(String metodo) {
        metodoSeleccionado = metodo;

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
                .setNegativeButton("Cancelar", null).show();
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
}