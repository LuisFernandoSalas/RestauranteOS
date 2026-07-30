package com.example.usuariococina.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usuariococina.R;
import com.example.usuariococina.api.ApiClient;
import com.example.usuariococina.api.LaravelApiService;
import com.example.usuariococina.models.OrderItem;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationAdapter extends RecyclerView.Adapter<PreparationAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> items;

    public PreparationAdapter(Context context, List<OrderItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usamos el layout que ya tienes para los detalles de los productos
        View view = LayoutInflater.from(context).inflate(R.layout.item_detail_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);

        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvName.setText(item.getName());

        // Manejo de las notas del cocinero
        if (item.getNote() != null && !item.getNote().isEmpty()) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(item.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
            View noteContainer = (View) holder.tvNote.getParent();
            if (noteContainer != null) noteContainer.setVisibility(View.GONE);
        }

        // Configuración inicial del estado visual del botón
        String estadoActual = item.getEstado() != null ? item.getEstado().toUpperCase() : "PENDIENTE";
        holder.btnStatus.setText(estadoActual);

        // 🚀 EL PODER DE RETROFIT: Avanzar el estado al hacer clic
        holder.btnStatus.setOnClickListener(v -> {
            String nuevoEstado = determinarSiguienteEstado(estadoActual);

            // Bloqueamos el botón temporalmente para evitar doble clic mientras responde Laravel
            holder.btnStatus.setEnabled(false);

            LaravelApiService apiService = ApiClient.getApiService(context);
            // Mandamos el ID del platillo (item.getId()) y el nuevo estado
            apiService.updatePlatilloEstado(item.getId(), nuevoEstado.toLowerCase()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    holder.btnStatus.setEnabled(true);
                    if (response.isSuccessful()) {
                        item.setEstado(nuevoEstado); // Actualizamos la memoria
                        holder.btnStatus.setText(nuevoEstado); // Pintamos el nuevo estado
                    } else {
                        Toast.makeText(context, "Error al actualizar en servidor", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    holder.btnStatus.setEnabled(true);
                    Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Lógica rápida para el ciclo de vida del platillo en cocina
    private String determinarSiguienteEstado(String estadoActual) {
        if (estadoActual.equals("PENDIENTE")) return "PREPARANDO";
        if (estadoActual.equals("PREPARANDO")) return "LISTO";
        return "LISTO"; // Si ya está listo, no avanza más desde aquí
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQty, tvName, tvNote;
        Button btnStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Asegúrate de que estos IDs coincidan con tu item_detail_product.xml
            tvQty = itemView.findViewById(R.id.tvDetailProductQty);
            tvName = itemView.findViewById(R.id.tvDetailProductName);
            tvNote = itemView.findViewById(R.id.tvDetailProductNote);
            btnStatus = itemView.findViewById(R.id.btnDetailProductStatus);
        }
    }
}