package modelos;

public class Ingrediente {
    private Insumo insumo;
    private double cantidad;

    public Ingrediente(Insumo insumo, double cantidad) {
        this.insumo = insumo;
        this.cantidad = cantidad;
    }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() {
        return insumo != null ? insumo.getUnidadMedida() : "";
    }

    public double subtotal() {
        return insumo != null ? cantidad * insumo.getCostoUnitario() : 0.0;
    }
}