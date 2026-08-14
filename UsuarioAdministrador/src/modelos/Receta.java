package modelos;

import java.util.ArrayList;
import java.util.List;

public class Receta {
    private int id;
    private int productoId;
    private String nombre;
    private String productoVinculado;
    private int tiempoMin;
    private List<Ingrediente> ingredientes;

    public Receta(int id, int productoId, String nombre, String productoVinculado, int tiempoMin, List<Ingrediente> ingredientes) {
        this.id = id;
        this.productoId = productoId;
        this.nombre = nombre;
        this.productoVinculado = productoVinculado;
        this.tiempoMin = tiempoMin;
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getProductoVinculado() { return productoVinculado; }
    public void setProductoVinculado(String productoVinculado) { this.productoVinculado = productoVinculado; }

    public int getTiempoMin() { return tiempoMin; }
    public void setTiempoMin(int tiempoMin) { this.tiempoMin = tiempoMin; }

    public List<Ingrediente> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<Ingrediente> ingredientes) { this.ingredientes = ingredientes; }

    public double getCostoTotal() {
        return ingredientes.stream().mapToDouble(Ingrediente::subtotal).sum();
    }
}