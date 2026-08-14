package modelos;

public class RecetaItem {
    private Integer insumoId;
    private String nombreInsumo;
    private double cantidad;
    private String unidadMedida;
    private double costoEstimado;

    public RecetaItem(Integer insumoId, String nombreInsumo, double cantidad, String unidadMedida, double costoEstimado) {
        this.insumoId = insumoId;
        this.nombreInsumo = nombreInsumo;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.costoEstimado = costoEstimado;
    }

    public Integer getInsumoId() { return insumoId; }
    public String getNombreInsumo() { return nombreInsumo; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getCostoEstimado() { return costoEstimado; }
}