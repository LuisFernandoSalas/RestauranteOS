package modelos;

public class Platillo {
    private Integer id;
    private String nombre;
    private String categoria; // "Entradas", "Platos Fuertes", "Bebidas", "Postres"
    private double precio;
    private String descripcion;
    private boolean disponible;

    public Platillo() {}

    public Platillo(Integer id, String nombre, String categoria, double precio, String descripcion, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.descripcion = descripcion;
        this.disponible = disponible;
    }

    public Platillo(String nombre, String categoria, double precio, String descripcion, boolean disponible) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.descripcion = descripcion;
        this.disponible = disponible;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}