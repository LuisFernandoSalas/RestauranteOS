package com.example.usuariococina.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.R;
import com.example.usuariococina.adapters.OrderAdapter;
import com.example.usuariococina.api.ApiClient;
import com.example.usuariococina.api.LaravelApiService;
import com.example.usuariococina.models.CocinaResponse;
import com.example.usuariococina.models.Order;
import com.example.usuariococina.models.OrderItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<Order> activeOrdersList = new ArrayList<>();

    private final Handler pollingHandler = new Handler();
    private Runnable pollingRunnable;
    private static final int TIEMPO_REFRESCO = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        rvOrders = findViewById(R.id.rvOrders);

        int spanCount = getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 4 : 2;
        rvOrders.setLayoutManager(new GridLayoutManager(this, spanCount));

        adapter = new OrderAdapter(this, activeOrdersList, this::showOrderDetailDialog);
        rvOrders.setAdapter(adapter);

        findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutConfirmation());

        iniciarMonitoreoCocina();
    }

    private void iniciarMonitoreoCocina() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                fetchPedidosDesdeLaravel();
                pollingHandler.postDelayed(this, TIEMPO_REFRESCO);
            }
        };
        pollingHandler.post(pollingRunnable);
    }

    private void fetchPedidosDesdeLaravel() {
        Log.d("COCINA_DEBUG", "🔄 Consultando pedidos en Laravel... [IP: 10.0.2.2:8000]");

        LaravelApiService apiService = ApiClient.getApiService(this);

        apiService.getPedidosCocina().enqueue(new Callback<CocinaResponse>() { // Cambia List<Order> por CocinaResponse
            @Override
            public void onResponse(Call<CocinaResponse> call, Response<CocinaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Extraemos la lista de pedidos del envoltorio!
                    List<Order> listaReal = response.body().getPedidos();

                    activeOrdersList.clear();
                    if (listaReal != null) {
                        activeOrdersList.addAll(listaReal);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CocinaResponse> call, Throwable t) {
                // ... tu código de error ...
            }
        });
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_msg)
                .setPositiveButton(R.string.logout_btn_confirm, (dialog, which) -> performLogout())
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

        if (btnStatus != null) {
            btnStatus.setText(order.getStatus() != null ? order.getStatus() : "VER");
        }

        if (container != null && order.getItems() != null) {
            container.removeAllViews();
            for (OrderItem item : order.getItems()) {
                View itemView = inflater.inflate(R.layout.item_detail_product, container, false);
                ((TextView) itemView.findViewById(R.id.tvDetailProductQty)).setText(String.valueOf(item.getQuantity()));
                ((TextView) itemView.findViewById(R.id.tvDetailProductName)).setText(item.getName());

                Button btnProductStatus = itemView.findViewById(R.id.btnDetailProductStatus);
                btnProductStatus.setText(item.getEstado() != null ? item.getEstado().toUpperCase() : "PENDIENTE");

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