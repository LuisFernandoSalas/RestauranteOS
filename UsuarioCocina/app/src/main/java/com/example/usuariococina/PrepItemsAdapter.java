package com.example.usuariococina;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adaptador para el RecyclerView que muestra la lista de platos en preparación dentro de una orden.
 * Gestiona el estado visual de cada producto (En preparación vs Listo).
 */
public class PrepItemsAdapter extends RecyclerView.Adapter<PrepItemsAdapter.ViewHolder> {

    private List<OrderItem> items;

    public PrepItemsAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el diseño de la fila individual para cada producto
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prep_detail_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        
        // Asignación de datos básicos del producto
        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        
        // Manejo de la visibilidad de las notas: solo se muestran si el producto tiene instrucciones especiales
        if (item.getNote() != null && !item.getNote().isEmpty()) {
            holder.tvNote.setText(item.getNote());
            holder.llNotes.setVisibility(View.VISIBLE);
        } else {
            holder.llNotes.setVisibility(View.GONE);
        }

        // Configuración de listeners para los estados de preparación
        holder.btnReady.setOnClickListener(v -> {
            markAsReady(holder); // Cambia el plato a estado completado
        });

        holder.btnPrep.setOnClickListener(v -> {
            markAsInPrep(holder); // Regresa el plato a estado en preparación
        });
        
        // Estado inicial por defecto para nuevos items cargados
        markAsInPrep(holder);
    }

    /**
     * Aplica cambios visuales para indicar que un plato está terminado.
     * Reduce la opacidad y cambia colores para dar feedback de tarea completada.
     */
    private void markAsReady(ViewHolder holder) {
        holder.btnReady.setText("✓ LISTO");
        holder.btnReady.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7A3520"))); // Terracota oscuro
        holder.btnPrep.setVisibility(View.GONE); // Oculta el botón de "En prep"
        holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFF5F2")); // Fondo tenue
        holder.tvName.setAlpha(0.5f); // Atenuación del texto
        holder.tvQuantity.setAlpha(0.5f);
    }

    /**
     * Restablece el aspecto visual del item al estado activo (En cocina).
     */
    private void markAsInPrep(ViewHolder holder) {
        holder.btnReady.setText("✓ Listo");
        holder.btnReady.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7A3520")));
        holder.btnPrep.setVisibility(View.VISIBLE);
        holder.btnPrep.setText("En prep.");
        holder.btnPrep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C1440E"))); // Terracota principal
        holder.cardContainer.setCardBackgroundColor(Color.WHITE);
        holder.tvName.setAlpha(1.0f);
        holder.tvQuantity.setAlpha(1.0f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Contenedor de vistas para optimizar el rendimiento del RecyclerView.
     */
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

