package com.example.usuariococina;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla principal del KDS (Kitchen Display System).
 * Muestra un tablero de todas las comandas activas en la cocina organizadas en un grid.
 */
public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_orders);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_orders), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvOrders = findViewById(R.id.rvOrders);
        
        // Determinar el número de columnas basado en la orientación: 4 para Landscape, 2 para Portrait
        int spanCount = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        rvOrders.setLayoutManager(new GridLayoutManager(this, spanCount));
        
        // Configurar botón de cerrar sesión (deshabilitado temporalmente por petición)
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Toast.makeText(this, "Botón de salida presionado (acción deshabilitada)", Toast.LENGTH_SHORT).show();
        });

        // Carga inicial de datos ficticios para demostración del flujo
        setupAdapter();
    }

    /**
     * Muestra un diálogo de confirmación antes de cerrar la sesión.
     */
    private void showLogoutConfirmation() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout_confirm_title)
            .setMessage(R.string.logout_confirm_msg)
            .setPositiveButton(R.string.logout_btn_confirm, (dialog, which) -> {
                performLogout();
            })
            .setNegativeButton(R.string.button_cancel, null)
            .show();
    }

    /**
     * Ejecuta la lógica para salir del sistema y volver al login.
     */
    private void performLogout() {
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Configura el adaptador con datos de ejemplo que simulan pedidos reales de distintas mesas.
     */
    private void setupAdapter() {
        List<Order> dummyOrders = new ArrayList<>();

        // Simulación: Pedido Mesa 2
        List<OrderItem> itemsMesa2 = new ArrayList<>();
        itemsMesa2.add(new OrderItem("Enchiladas verdes", 2, "sin picante"));
        itemsMesa2.add(new OrderItem("Pozole rojo", 1, null));
        itemsMesa2.add(new OrderItem("Agua Jamaica", 2, "sin azúcar"));
        
        dummyOrders.add(new Order(2, "Ana M.", itemsMesa2, "PENDIENTE"));

        // Simulación: Pedido Mesa 5
        List<OrderItem> itemsMesa5 = new ArrayList<>();
        itemsMesa5.add(new OrderItem("Carne asada", 1, null));
        itemsMesa5.add(new OrderItem("Arroz blanco", 1, null));
        itemsMesa5.add(new OrderItem("Agua Horchata", 2, null));
        
        dummyOrders.add(new Order(5, "Luis G.", itemsMesa5, "EN PREPARACIÓN"));

        // Simulación: Pedido extenso para validar el scroll vertical dentro de la tarjeta
        List<OrderItem> itemsLargo = new ArrayList<>();
        itemsLargo.add(new OrderItem("Tacos al pastor", 5, "Con mucha piña"));
        itemsLargo.add(new OrderItem("Gringas", 2, null));
        itemsLargo.add(new OrderItem("Consomé", 1, null));
        itemsLargo.add(new OrderItem("Refresco", 4, "Fríos"));
        itemsLargo.add(new OrderItem("Flan napolitano", 2, null));
        itemsLargo.add(new OrderItem("Café americano", 1, "Sin azúcar"));
        itemsLargo.add(new OrderItem("Guacamole extra", 1, null));
        
        dummyOrders.add(new Order(8, "Carlos P.", itemsLargo, "PENDIENTE"));

        // Inicialización del adaptador con un listener para capturar clics en las tarjetas
        OrdersAdapter adapter = new OrdersAdapter(this, dummyOrders, order -> {
            showOrderDetailDialog(order);
        });
        rvOrders.setAdapter(adapter);
    }

    /**
     * Muestra un panel lateral (Dialog) con los detalles específicos de la comanda seleccionada.
     * @param order El objeto de la orden que se desea inspeccionar.
     */
    public void showOrderDetailDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_order_details, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Vinculación de elementos del encabezado del diálogo
        TextView tvTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvWaiter = dialogView.findViewById(R.id.tvDetailWaiter);
        Button btnStatus = dialogView.findViewById(R.id.btnDetailTotalStatus);
        LinearLayout container = dialogView.findViewById(R.id.containerDetailItems);

        if (tvTitle != null) tvTitle.setText(getString(R.string.order_detail_title, order.getTableNumber()));
        if (tvWaiter != null) tvWaiter.setText(getString(R.string.waiter_label, order.getWaiterName()));
        if (btnStatus != null) btnStatus.setText(order.getStatus());

        // Inserción dinámica de los productos de la orden en el contenedor del diálogo
        if (container != null) {
            container.removeAllViews();
            for (OrderItem item : order.getItems()) {
                View itemView = inflater.inflate(R.layout.item_detail_product, container, false);
                ((TextView) itemView.findViewById(R.id.tvDetailProductQty)).setText(String.valueOf(item.getQuantity()));
                ((TextView) itemView.findViewById(R.id.tvDetailProductName)).setText(item.getName());
                ((Button) itemView.findViewById(R.id.btnDetailProductStatus)).setText(order.getStatus());
                
                TextView tvNote = itemView.findViewById(R.id.tvDetailProductNote);
                if (item.getNote() != null && !item.getNote().isEmpty()) {
                    tvNote.setText(item.getNote());
                } else {
                    // Si no existen notas especiales, se oculta el área de texto para optimizar espacio
                    View noteContainer = (View) tvNote.getParent();
                    if (noteContainer != null) noteContainer.setVisibility(View.GONE);
                }
                container.addView(itemView);
            }
        }

        // Configuración del botón de cierre
        if (dialogView.findViewById(R.id.btnCloseDetail) != null) {
            dialogView.findViewById(R.id.btnCloseDetail).setOnClickListener(v -> dialog.dismiss());
        }

        // Personalización del estilo del diálogo: Fondo transparente y anclado al lado derecho (estilo Sidebar)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(android.view.Gravity.END);
        }

        dialog.show();
    }

    /**
     * Lanza una notificación visual de tipo Pop-up para alertar a cocina sobre la entrada de un nuevo pedido.
     */
    public void showNewOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_order, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        View btnAccept = dialogView.findViewById(R.id.btnAcceptOrder);
        View btnLater = dialogView.findViewById(R.id.btnLaterOrder);

        if (btnAccept != null) btnAccept.setOnClickListener(v -> dialog.dismiss());
        if (btnLater != null) btnLater.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}