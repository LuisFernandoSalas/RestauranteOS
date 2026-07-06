public class PedidoCocina {
    private int id;
    private String mesa;
    private String platillo;
    private int cantidad;
    private String notas;
    private String hora; // Para saber cuánto tiempo lleva esperando

    // Constructor, Getters y Setters
    public PedidoCocina(int id, String mesa, String platillo, int cantidad, String notas, String hora) {
        this.id = id;
        this.mesa = mesa;
        this.platillo = platillo;
        this.cantidad = cantidad;
        this.notas = notas;
        this.hora = hora;
    }

    public String getMesa() { return mesa; }
    public String getPlatillo() { return "x" + cantidad + " " + platillo; }
    public String getNotas() { return notas; }
    public String getHora() { return hora; }
}