package com.example.usuariomesero.adapters;

import com.example.usuariomesero.models.Mesa;
import com.example.usuariomesero.R;

import android.graphics.Color;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class MesaAdapter extends RecyclerView.Adapter<MesaAdapter.MesaViewHolder> {

    private List<Mesa> mesaList;
    private OnMesaClickListener listener;

    public interface OnMesaClickListener {
        void onMesaClick(Mesa mesa);
    }

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

        // Evento de clic que se envía a la Activity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMesaClick(mesa);
            }
        });

        Mesa.Estado estadoMesa = mesa.getEstado();
        if (estadoMesa == null) {
            estadoMesa = Mesa.Estado.LIBRE;
        }

        switch (estadoMesa) {
            case LIBRE:
                holder.container.setBackgroundResource(R.drawable.bg_mesa_libre);
                holder.tvStatusPrice.setText(R.string.status_libre);
                holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.mesa_texto_libre));
                break;

            case OCUPADA:
                double totalOcupada = mesa.getTotal() != null ? mesa.getTotal() : 0.0;
                String estadoPed = mesa.getEstadoPedido() != null ? mesa.getEstadoPedido() : "";

                if ("listo".equalsIgnoreCase(estadoPed)) {
                    holder.container.setBackgroundResource(R.drawable.bg_mesa_libre);
                    holder.tvStatusPrice.setText("🍽️ ¡LISTO PARA SERVIR!");
                    holder.tvStatusPrice.setTextColor(Color.parseColor("#1B5E20"));
                }
                else if ("entregado".equalsIgnoreCase(estadoPed)) {
                    holder.container.setBackgroundResource(R.drawable.bg_mesa_ocupada);
                    // Texto actualizado para invitar al cobro
                    holder.tvStatusPrice.setText(String.format(Locale.US, "$%.2f (Clic para cobrar 💵)", totalOcupada));
                    holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.terracota_oscuro));
                }
                else if ("cobro".equalsIgnoreCase(estadoPed)) {
                    holder.container.setBackgroundResource(R.drawable.bg_mesa_ocupada);
                    // Texto actualizado
                    holder.tvStatusPrice.setText(String.format(Locale.US, "$%.2f (Cobrar Ahora 💰)", totalOcupada));
                    holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.terracota_oscuro));
                }
                else {
                    holder.container.setBackgroundResource(R.drawable.bg_mesa_ocupada);
                    holder.tvStatusPrice.setText(String.format(Locale.US, "$%.2f (En cocina 👨‍🍳)", totalOcupada));
                    holder.tvStatusPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.terracota_oscuro));
                }
                break;

            case COBRO:
                holder.container.setBackgroundResource(R.drawable.bg_mesa_cobro);
                double totalCobro = mesa.getTotal() != null ? mesa.getTotal() : 0.0;
                holder.tvStatusPrice.setText(String.format(Locale.US, "$%.2f (Cobrar Ahora)", totalCobro));
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