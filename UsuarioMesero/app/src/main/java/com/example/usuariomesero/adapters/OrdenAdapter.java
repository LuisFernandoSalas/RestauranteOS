package com.example.usuariomesero.adapters;

import com.example.usuariomesero.R;
import com.example.usuariomesero.models.ItemOrden;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para gestionar los productos agregados a una orden específica.
 * Permite modificar cantidades, eliminar ítems y agregar notas especiales.
 */
public class OrdenAdapter extends RecyclerView.Adapter<OrdenAdapter.OrdenViewHolder> {

    private List<ItemOrden> items;
    private OnOrdenActionListener listener;

    /**
     * Interfaz para comunicar cambios en la orden a la Activity.
     */
    public interface OnOrdenActionListener {
        /** Actualiza el monto total en la interfaz. */
        void onUpdateTotal();
        /** Abre el diálogo para editar o añadir notas al ítem. */
        void onItemClick(ItemOrden item);
    }

    public OrdenAdapter(List<ItemOrden> items, OnOrdenActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrdenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orden, parent, false);
        return new OrdenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdenViewHolder holder, int position) {
        ItemOrden item = items.get(position);
        holder.tvCantidad.setText(String.valueOf(item.getCantidad()));
        holder.tvNombre.setText(item.getProducto().getNombre());
        
        if (item.getNota() != null && !item.getNota().isEmpty()) {
            holder.tvNota.setText(item.getNota());
            holder.layoutNota.setVisibility(View.VISIBLE);
        } else {
            holder.layoutNota.setVisibility(View.GONE);
        }

        double subtotal = item.getProducto().getPrecio() * item.getCantidad();
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));

        // Hide action buttons if no listener is provided (e.g., read-only mode in CobroActivity)
        if (listener == null) {
            holder.btnMinus.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.GONE);
            holder.itemView.setClickable(false);
        } else {
            holder.btnMinus.setVisibility(View.VISIBLE);
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.itemView.setClickable(true);

            holder.btnMinus.setOnClickListener(v -> {
                if (item.getCantidad() > 1) {
                    item.setCantidad(item.getCantidad() - 1);
                    notifyItemChanged(holder.getAdapterPosition());
                } else {
                    int currentPos = holder.getAdapterPosition();
                    items.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, items.size());
                }
                if (listener != null) listener.onUpdateTotal();
            });

            holder.btnRemove.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                items.remove(currentPos);
                notifyItemRemoved(currentPos);
                notifyItemRangeChanged(currentPos, items.size());
                if (listener != null) listener.onUpdateTotal();
            });

            holder.itemView.setOnClickListener(v -> {
                listener.onItemClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrdenViewHolder extends RecyclerView.ViewHolder {
        TextView tvCantidad, tvNombre, tvPrecio, tvNota;
        View layoutNota;
        ImageButton btnMinus, btnRemove;

        public OrdenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCantidad = itemView.findViewById(R.id.tv_cantidad);
            tvNombre = itemView.findViewById(R.id.tv_nombre_orden);
            tvPrecio = itemView.findViewById(R.id.tv_precio_orden);
            tvNota = itemView.findViewById(R.id.tv_nota_item);
            layoutNota = itemView.findViewById(R.id.layout_nota_indicador);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
