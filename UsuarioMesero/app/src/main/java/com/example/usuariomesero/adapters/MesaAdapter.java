package com.example.usuariomesero.adapters;

import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.R;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adaptador para mostrar la lista de mesas en un RecyclerView.
 * Maneja la visualización de diferentes estados (Libre, Ocupada, Cobro)
 * y los eventos de clic sobre cada mesa.
 */
public class MesaAdapter extends RecyclerView.Adapter<MesaAdapter.MesaViewHolder> {

    private List<Mesa> mesaList;
    private OnMesaClickListener listener;

    /**
     * Interfaz para manejar los eventos de clic en las mesas.
     */
    public interface OnMesaClickListener {
        /**
         * Se llama cuando el usuario toca una mesa.
         * @param mesa El objeto Mesa seleccionado.
         */
        void onMesaClick(Mesa mesa);
    }

    /**
     * Constructor del adaptador.
     * @param mesaList Lista de mesas a mostrar.
     * @param listener Listener para eventos de clic.
     */
    public MesaAdapter(List<Mesa> mesaList, OnMesaClickListener listener) {
        this.mesaList = mesaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mesa, parent, false);
        return new MesaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MesaViewHolder holder, int position) {
        Mesa mesa = mesaList.get(position);
        holder.tvMesaNumero.setText(holder.itemView.getContext().getString(R.string.mesa_name, mesa.getNumero()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMesaClick(mesa);
            }
        });

        switch (mesa.getEstado()) {
            case LIBRE:
                holder.container.setBackgroundResource(R.drawable.bg_mesa_libre);
                holder.tvStatusPrice.setText(R.string.status_libre);
                holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.mesa_texto_libre));
                break;
            case OCUPADA:
                holder.container.setBackgroundResource(R.drawable.bg_mesa_ocupada);
                holder.tvStatusPrice.setText(mesa.getPrecioFormateado());
                holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.terracota_oscuro));
                break;
            case COBRO:
                holder.container.setBackgroundResource(R.drawable.bg_mesa_cobro);
                holder.tvStatusPrice.setText(mesa.getPrecioFormateado());
                holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.terracota_oscuro));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mesaList.size();
    }

    static class MesaViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView tvMesaNumero;
        TextView tvStatusPrice;

        public MesaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ll_mesa_container);
            tvMesaNumero = itemView.findViewById(R.id.tv_mesa_numero);
            tvStatusPrice = itemView.findViewById(R.id.tv_mesa_status_price);
        }
    }
}
