package modelos;

public class Producto {
    private Integer id;
    private String nombre;
    private String categoria;
    private double precio;
    private String descripcion;
    private String estado; // "Activo" o "Pausado"
    private Integer recetaId;

    public Producto() {}

    public Producto(Integer id, String nombre, String categoria, double precio, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.estado = estado;
    }

    public Producto(String nombre, String categoria, double precio, String descripcion, String estado) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.descripcion = descripcion;
        this.estado = estado;
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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getRecetaId() { return recetaId; }
    public void setRecetaId(Integer recetaId) { this.recetaId = recetaId; }
}