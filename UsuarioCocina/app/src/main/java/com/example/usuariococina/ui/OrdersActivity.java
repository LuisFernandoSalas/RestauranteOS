package com.example.usuariococina.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.example.usuariococina.models.OrderItem;
import com.example.usuariococina.R;
import com.example.usuariococina.adapters.OrdersAdapter;
import com.example.usuariococina.models.Order;

import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private final Handler pollingHandler = new Handler();
    private Runnable pollingRunnable;
    private static final int TIEMPO_REFRESCO = 5000;

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

        int spanCount = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        rvOrders.setLayoutManager(new GridLayoutManager(this, spanCount));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            showLogoutConfirmation();
        });

        iniciarMonitoreoCocina();
    }

    private void iniciarMonitoreoCocina() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                setupAdapter();
                pollingHandler.postDelayed(this, TIEMPO_REFRESCO);
            }
        };
        pollingHandler.post(pollingRunnable);
    }

    private void setupAdapter() {
        com.example.usuariococina.api.LaravelApiService apiService = com.example.usuariococina.api.ApiClient.getApiService(OrdersActivity.this);

        apiService.getPedidosCocina().enqueue(new retrofit2.Callback<List<Order>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Order>> call, retrofit2.Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> realOrders = response.body();

                    OrdersAdapter adapter = new OrdersAdapter(OrdersActivity.this, realOrders, new OrdersAdapter.OnOrderClickListener() {
                        @Override
                        public void onMoreClick(Order order) {
                            showOrderDetailDialog(order);
                        }
                    });
                    rvOrders.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Order>> call, Throwable t) {
                // Falla silenciosa o logs mínimos de sistema para producción
            }
        });
    }

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

    private void performLogout() {
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void showOrderDetailDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_order_details, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvWaiter = dialogView.findViewById(R.id.tvDetailWaiter);
        Button btnStatus = dialogView.findViewById(R.id.btnDetailTotalStatus);
        LinearLayout container = dialogView.findViewById(R.id.containerDetailItems);

        if (tvTitle != null) tvTitle.setText(getString(R.string.order_detail_title, order.getTableNumber()));
        if (tvWaiter != null) tvWaiter.setText(getString(R.string.waiter_label, order.getWaiterName()));
        if (btnStatus != null) btnStatus.setText(order.getStatus());

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
                    View noteContainer = (View) tvNote.getParent();
                    if (noteContainer != null) noteContainer.setVisibility(View.GONE);
                }
                container.addView(itemView);
            }
        }

        if (dialogView.findViewById(R.id.btnCloseDetail) != null) {
            dialogView.findViewById(R.id.btnCloseDetail).setOnClickListener(v -> dialog.dismiss());
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(android.view.Gravity.END);
        }

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }
}