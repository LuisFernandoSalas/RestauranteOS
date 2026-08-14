package modelos;

public class Insumo {

    private int id;
    private String nombre;
    private String categoria;
    private String unidadMedida;
    private double stockActual;
    private double stockMinimo;
    private double stockMaximo;
    private double costoUnitario;

    // ─── CONSTRUCTORES ───

    public Insumo() {
    }

    public Insumo(String nombre, String categoria, String unidadMedida, double stockActual, double cantidad, double stockMinimo) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = 100.0;
        this.costoUnitario = 0.0;
    }

    public Insumo(int id, String nombre, String categoria, String unidadMedida, double stockActual, double stockMinimo, double stockMaximo) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.costoUnitario = 0.0;
    }

    public Insumo(int id, String nombre, String categoria, String unidadMedida, double stockActual, double stockMinimo, double stockMaximo, double costoUnitario) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.stockMaximo = stockMaximo;
        this.costoUnitario = costoUnitario;
    }

    // ─── GETTERS Y SETTERS ───

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }

    public double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public double getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(double stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}