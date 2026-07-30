package com.example.usuariococina.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.models.OrderItem;
import com.example.usuariococina.ui.PreparationDetailActivity;
import com.example.usuariococina.R;
import com.example.usuariococina.models.Order;

import java.util.List;

/**
 * Adaptador que gestiona la visualización de las tarjetas de pedidos en el tablero principal (Grid).
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> { // 👈 Singular

    private List<Order> orders;
    private OnOrderClickListener listener;
    private Context context;

    public interface OnOrderClickListener {
        void onMoreClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) { // 👈 Singular
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.tvTableName.setText("Mesa " + order.getTableNumber());
        holder.tvWaiterName.setText(order.getWaiterName());

        holder.btnStatus.setText("VER DETALLE");

        holder.containerItems.removeAllViews();

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                View itemView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_order_row, holder.containerItems, false);
                TextView tvQty = itemView.findViewById(R.id.tvItemQty);
                TextView tvName = itemView.findViewById(R.id.tvItemName);
                TextView tvNote = itemView.findViewById(R.id.tvItemNote);

                tvQty.setText(String.valueOf(item.getQuantity()));
                tvName.setText(item.getName());

                if (item.getNote() != null && !item.getNote().isEmpty()) {
                    tvNote.setVisibility(View.VISIBLE);
                    tvNote.setText(item.getNote());
                } else {
                    tvNote.setVisibility(View.GONE);
                }

                holder.containerItems.addView(itemView);
            }
        }

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(order);
        });

        holder.btnStatus.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreparationDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            intent.putExtra("TABLE_NUMBER", order.getTableNumber());
            intent.putExtra("WAITER_NAME", order.getWaiterName());

            // AGREGA ESTA LÍNEA: Pasamos el objeto completo (ya lo hicimos Serializable)
            intent.putExtra("PEDIDO_COMPLETO", order);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableName, tvWaiterName;
        LinearLayout containerItems;
        Button btnStatus;
        ImageView btnMore;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableName = itemView.findViewById(R.id.tvTableName);
            tvWaiterName = itemView.findViewById(R.id.tvWaiterName);
            containerItems = itemView.findViewById(R.id.containerItems);
            btnStatus = itemView.findViewById(R.id.btnStatus);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}