package com.example.usuariomesero.activities;

import com.example.usuariomesero.R;


import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

/**
 * Pantalla de Resumen de Cobro.
 * Muestra el desglose final del pago (subtotal, propina, total, método de pago y cambio).
 * Se utiliza como comprobante visual antes de liberar la mesa.
 */
public class ResumenCobroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resumen_cobro);

        // Get data from Intent
        double total = getIntent().getDoubleExtra("total", 0.0);
        double subtotal = getIntent().getDoubleExtra("subtotal", 0.0);
        double propinaMonto = getIntent().getDoubleExtra("propina_monto", 0.0);
        String propinaLabel = getIntent().getStringExtra("propina_label");
        String metodo = getIntent().getStringExtra("metodo");
        double recibido = getIntent().getStringExtra("recibido") != null ? Double.parseDouble(getIntent().getStringExtra("recibido")) : 0.0;
        double cambio = recibido - total;
        if (cambio < 0) cambio = 0;

        // Populate Views
        TextView tvTotal = findViewById(R.id.tv_resumen_total);
        TextView tvSubtotal = findViewById(R.id.tv_resumen_subtotal);
        TextView tvPropinaLabel = findViewById(R.id.tv_resumen_propina_label);
        TextView tvPropinaMonto = findViewById(R.id.tv_resumen_propina_monto);
        TextView tvMetodo = findViewById(R.id.tv_resumen_metodo);
        TextView tvCambio = findViewById(R.id.tv_resumen_cambio);

        if (tvTotal != null) tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
        if (tvSubtotal != null) tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        if (tvPropinaLabel != null && propinaLabel != null) tvPropinaLabel.setText("Propina (" + propinaLabel + ")");
        if (tvPropinaMonto != null) tvPropinaMonto.setText(String.format(Locale.getDefault(), "$%.2f", propinaMonto));
        if (tvMetodo != null) tvMetodo.setText(metodo);
        if (tvCambio != null) tvCambio.setText(String.format(Locale.getDefault(), "$%.2f", cambio));

        boolean factura = getIntent().getBooleanExtra("factura", false);
        TextView tvCfdi = findViewById(R.id.tv_resumen_cfdi);
        if (tvCfdi != null) {
            tvCfdi.setText(factura ? "Solicitado" : "No solicitado");
        }

        // Tablet specific views
        TextView tvRecibi = findViewById(R.id.tv_resumen_recibi);
        if (tvRecibi != null) {
            tvRecibi.setText(String.format(Locale.getDefault(), "$%.2f", recibido));
        }

        findViewById(R.id.btn_aceptar_resumen).setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        View btnBack = findViewById(R.id.btn_back_resumen);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}
