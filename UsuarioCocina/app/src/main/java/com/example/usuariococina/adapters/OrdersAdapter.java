package com.example.usuariococina.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.models.PedidoResponse;
import com.example.usuariococina.models.DetallePedido;
import com.example.usuariococina.R;
import com.example.usuariococina.ui.PreparationDetailActivity;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private static final String TAG = "Cocina_OrdersAdapter";
    private List<PedidoResponse> orders;
    private OnOrderClickListener listener;
    private Context context;

    public interface OnOrderClickListener {
        void onMoreClick(PedidoResponse order);
    }

    public OrdersAdapter(Context context, List<PedidoResponse> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    public void setOrders(List<PedidoResponse> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        PedidoResponse order = orders.get(position);

        String numeroMesa = String.valueOf(order.getMesa());
        holder.tvTableName.setText("Mesa " + numeroMesa);

        // ¡Ahora sí leemos el mesero directo de la API!
        holder.tvWaiterName.setText(order.getMesero() != null ? order.getMesero() : "Sin mesero");

        // Asumiendo que cambiaste getEstado() a getEstadoGeneral() en tu modelo
        holder.btnStatus.setText(order.getEstadoGeneral() != null ? order.getEstadoGeneral() : "pendiente");

        holder.containerItems.removeAllViews();

        // Validación clave: Verificamos que platillos no sea nulo antes de iterar
        if (order.getPlatillos() != null) {
            Log.d(TAG, "onBindViewHolder: Pintando " + order.getPlatillos().size() + " items para la Mesa " + numeroMesa);

            for (DetallePedido item : order.getPlatillos()) {
                View itemView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_order_row, holder.containerItems, false);
                TextView tvQty = itemView.findViewById(R.id.tvItemQty);
                TextView tvName = itemView.findViewById(R.id.tvItemName);
                TextView tvNote = itemView.findViewById(R.id.tvItemNote);

                tvQty.setText(String.valueOf(item.getCantidad()));

                // El producto ahora es un texto directo en el JSON, ya no un objeto
                String nombreItem = (item.getProducto() != null) ? item.getProducto() : "Desconocido";
                tvName.setText(nombreItem);

                if (item.getNota() != null && !item.getNota().isEmpty()) {
                    tvNote.setVisibility(View.VISIBLE);
                    tvNote.setText(item.getNota());
                } else {
                    tvNote.setVisibility(View.GONE);
                }

                holder.containerItems.addView(itemView);
            }
        } else {
            Log.d(TAG, "onBindViewHolder: La lista de platillos viene nula para la Mesa " + numeroMesa);
        }

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(order);
        });

        holder.btnStatus.setOnClickListener(v -> {
            Log.d(TAG, "Navegando al detalle de la Mesa " + numeroMesa);
            Intent intent = new Intent(context, PreparationDetailActivity.class);

            // Usamos Gson para empaquetar TODO el pedido (mesa, mesero, platillos) en un String
            String pedidoJson = new com.google.gson.Gson().toJson(order);
            intent.putExtra("PEDIDO_DATA", pedidoJson);

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