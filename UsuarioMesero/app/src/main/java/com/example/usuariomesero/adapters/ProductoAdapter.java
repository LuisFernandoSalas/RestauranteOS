package com.example.usuariomesero.adapters;

import com.example.usuariomesero.R;
import com.example.usuariomesero.models.Producto;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar la lista de productos disponibles en el menú.
 * Permite filtrar por categorías y seleccionar productos para añadir a la orden.
 */
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> productos;
    private OnProductoClickListener listener;

    /**
     * Interfaz para manejar la selección de un producto.
     */
    public interface OnProductoClickListener {
        /**
         * Se ejecuta al seleccionar un producto para añadirlo al pedido.
         * @param producto El producto seleccionado.
         */
        void onProductoClick(Producto producto);
    }

    /**
     * Constructor del adaptador de productos.
     * @param productos Lista de productos a mostrar.
     * @param listener Callback para la selección de productos.
     */
    public ProductoAdapter(List<Producto> productos, OnProductoClickListener listener) {
        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "$%.2f", producto.getPrecio()));

        holder.itemView.setOnClickListener(v -> listener.onProductoClick(producto));
        if (holder.btnAdd != null) {
            holder.btnAdd.setOnClickListener(v -> listener.onProductoClick(producto));
        }
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio;
        View btnAdd;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_producto);
            tvPrecio = itemView.findViewById(R.id.tv_precio_producto);
            btnAdd = itemView.findViewById(R.id.btn_add_product);
        }
    }
}
