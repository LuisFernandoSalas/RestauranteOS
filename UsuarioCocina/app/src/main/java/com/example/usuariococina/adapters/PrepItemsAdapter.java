package com.example.usuariococina.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.R;
import com.example.usuariococina.models.DetallePedido;

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

        // Manejo de notas opcionales
        if (item.getNota() != null && !item.getNota().trim().isEmpty()) {
            holder.tvNote.setText(item.getNota());
            holder.llNotes.setVisibility(View.VISIBLE);
        } else {
            holder.llNotes.setVisibility(View.GONE);
        }

        // EVALUACIÓN DE ESTADOS
        boolean isPausado = "pausado".equalsIgnoreCase(item.getEstadoPlatillo());
        boolean isListo = item.isListo() || "listo".equalsIgnoreCase(item.getEstadoPlatillo());

        if (isPausado) {
            markAsPaused(holder);
        } else if (isListo) {
            markAsReady(holder);
        } else {
            markAsInPrep(holder);
        }

        int idPlatillo = (item.getDetalleId() > 0) ? item.getDetalleId() : item.getId();

        // ACCIÓN: Botón "✓ Listo"
        holder.btnReady.setOnClickListener(v -> {
            if (isPausado) {
                Toast.makeText(v.getContext(), "⚠️ Este platillo está pausado en el menú.", Toast.LENGTH_SHORT).show();
                return;
            }

            item.setListo(true);
            item.setEstadoPlatillo("listo");
            notifyItemChanged(position);

            if (listener != null) {
                listener.onStatusChange(idPlatillo, "listo");
            }
        });

        // ACCIÓN: Botón "En prep."
        holder.btnPrep.setOnClickListener(v -> {
            if (isPausado) {
                Toast.makeText(v.getContext(), "⚠️ Este platillo está pausado en el menú.", Toast.LENGTH_SHORT).show();
                return;
            }

            item.setListo(false);
            item.setEstadoPlatillo("en_preparacion");
            notifyItemChanged(position);

            if (listener != null) {
                listener.onStatusChange(idPlatillo, "en_preparacion");
            }
        });
    }

    // --- ESTADOS VISUALES DEL RECYCLERVIEW ---

    private void markAsPaused(ViewHolder holder) {
        holder.btnReady.setText("⏸ PAUSADO");
        holder.btnReady.setEnabled(false);
        holder.btnReady.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E"))); // Gris
        holder.btnPrep.setVisibility(View.GONE);
        holder.cardContainer.setCardBackgroundColor(Color.parseColor("#F5F5F5")); // Tarjeta tenue
        holder.tvName.setAlpha(0.5f);
        holder.tvQuantity.setAlpha(0.5f);
    }

    private void markAsReady(ViewHolder holder) {
        holder.btnReady.setText("✓ LISTO");
        holder.btnReady.setEnabled(true);
        holder.btnReady.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7A3520")));
        holder.btnPrep.setVisibility(View.GONE);
        holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFF5F2"));
        holder.tvName.setAlpha(0.5f);
        holder.tvQuantity.setAlpha(0.5f);
    }

    private void markAsInPrep(ViewHolder holder) {
        holder.btnReady.setText("✓ Listo");
        holder.btnReady.setEnabled(true);
        holder.btnReady.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7A3520")));
        holder.btnPrep.setVisibility(View.VISIBLE);
        holder.btnPrep.setText("En prep.");
        holder.btnPrep.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C1440E")));
        holder.cardContainer.setCardBackgroundColor(Color.WHITE);
        holder.tvName.setAlpha(1.0f);
        holder.tvQuantity.setAlpha(1.0f);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void setItems(List<DetallePedido> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
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