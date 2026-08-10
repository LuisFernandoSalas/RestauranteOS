package com.example.usuariococina.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.models.DetallePedido;
import com.example.usuariococina.R;

import java.util.List;

public class PrepItemsAdapter extends RecyclerView.Adapter<PrepItemsAdapter.ViewHolder> {

    private List<DetallePedido> items;
    private OnItemStatusChangeListener listener;

    public interface OnItemStatusChangeListener {
        void onStatusChange(int detalleId, String nuevoEstado);
    }
    public PrepItemsAdapter(List<DetallePedido> items, OnItemStatusChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prep_detail_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetallePedido item = items.get(position);

        String nombreItem = (item.getProducto() != null) ? item.getProducto() : "Desconocido";

        holder.tvName.setText(nombreItem);
        holder.tvQuantity.setText(String.valueOf(item.getCantidad()));

        if (item.getNota() != null && !item.getNota().trim().isEmpty()) {
            holder.tvNote.setText(item.getNota());
            holder.llNotes.setVisibility(View.VISIBLE);
        } else {
            holder.llNotes.setVisibility(View.GONE);
        }

        // 1. Leemos el estado REAL del modelo al pintar la vista
        if (item.isListo()) { // Asegúrate de tener este getter en DetallePedido
            markAsReady(holder);
        } else {
            markAsInPrep(holder);
        }

        // 2. Actualizamos el modelo y HACEMOS LA PETICIÓN
        holder.btnReady.setOnClickListener(v -> {
            item.setListo(true);
            notifyItemChanged(position);
            // Avisamos a la Activity que mande "entregado" a Laravel
            if(listener != null) listener.onStatusChange(item.getId(), "entregado");
        });

        holder.btnPrep.setOnClickListener(v -> {
            item.setListo(false);
            notifyItemChanged(position);
            // Avisamos a la Activity que mande "en_preparacion" a Laravel
            if(listener != null) listener.onStatusChange(item.getId(), "en_preparacion");
        });
    }

    private void markAsReady(ViewHolder holder) {
        holder.btnReady.setText("✓ LISTO");
        holder.btnReady.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7A3520")));
        holder.btnPrep.setVisibility(View.GONE);
        holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFF5F2"));
        holder.tvName.setAlpha(0.5f);
        holder.tvQuantity.setAlpha(0.5f);
    }

    private void markAsInPrep(ViewHolder holder) {
        holder.btnReady.setText("✓ Listo");
        holder.btnReady.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7A3520")));
        holder.btnPrep.setVisibility(View.VISIBLE);
        holder.btnPrep.setText("En prep.");
        holder.btnPrep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C1440E")));
        holder.cardContainer.setCardBackgroundColor(Color.WHITE);
        holder.tvName.setAlpha(1.0f);
        holder.tvQuantity.setAlpha(1.0f);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvNote;
        View llNotes;
        Button btnPrep, btnReady;
        CardView cardContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPrepProductName);
            tvQuantity = itemView.findViewById(R.id.tvPrepQuantity);
            tvNote = itemView.findViewById(R.id.tvPrepNotes);
            llNotes = itemView.findViewById(R.id.llPrepNotes);
            btnPrep = itemView.findViewById(R.id.btnItemPrep);
            btnReady = itemView.findViewById(R.id.btnItemReady);
            cardContainer = itemView.findViewById(R.id.cardItemContainer);
        }
    }
}