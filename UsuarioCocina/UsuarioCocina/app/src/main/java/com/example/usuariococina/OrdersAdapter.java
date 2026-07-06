package com.example.usuariococina;

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
import java.util.List;

/**
 * Adaptador que gestiona la visualización de las tarjetas de pedidos en el tablero principal (Grid).
 * Cada tarjeta muestra un resumen rápido de los productos de una mesa.
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;
    private Context context;

    /**
     * Interfaz para gestionar eventos de interacción, como ver más detalles.
     */
    public interface OnOrderClickListener {
        void onMoreClick(Order order);
    }

    public OrdersAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el diseño de la tarjeta de pedido (CardView)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        
        // Configura la cabecera de la tarjeta
        holder.tvTableName.setText("Mesa " + order.getTableNumber());
        holder.tvWaiterName.setText(order.getWaiterName());
        holder.btnStatus.setText(order.getStatus());
        
        // Limpia los productos previos para evitar duplicados al reciclar la vista
        holder.containerItems.removeAllViews();
        
        // Renderiza cada producto de la orden dentro de la tarjeta
        for (OrderItem item : order.getItems()) {
            View itemView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.item_order_row, holder.containerItems, false);
            TextView tvQty = itemView.findViewById(R.id.tvItemQty);
            TextView tvName = itemView.findViewById(R.id.tvItemName);
            TextView tvNote = itemView.findViewById(R.id.tvItemNote);

            tvQty.setText(String.valueOf(item.getQuantity()));
            tvName.setText(item.getName());
            
            // Muestra notas de cocina solo si existen
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText(item.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }
            
            holder.containerItems.addView(itemView);
        }

        // Listener para el icono de "Más opciones" (Abre el diálogo de detalles)
        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(order);
        });

        // Al hacer clic en el botón de estado (ej: "PENDIENTE"), se navega a la pantalla de preparación detallada
        holder.btnStatus.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreparationDetailActivity.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    /**
     * ViewHolder que mantiene las referencias a los componentes de la tarjeta.
     */
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