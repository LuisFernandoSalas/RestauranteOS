package modelos;

/**
 * ═══════════════════════════════════════════════════════
 *  Modelo: Mesa
 * ═══════════════════════════════════════════════════════
 *  Representa una mesa del restaurante.
 *
 *  TODO (BD): mapear contra tabla `mesas` en MySQL:
 *    CREATE TABLE mesas (
 *      id_mesa    INT PRIMARY KEY AUTO_INCREMENT,
 *      numero     INT NOT NULL,
 *      estado     ENUM('LIBRE','OCUPADO','COBRO') DEFAULT 'LIBRE',
 *      total      DECIMAL(10,2) DEFAULT 0.00,
 *      tiempo_min INT DEFAULT 0
 *    );
 * ═══════════════════════════════════════════════════════
 */
public class Mesa {

    // ─────────────────────────────────────────────
    // ENUM DE ESTADO
    // Controla el color del borde en la tarjeta:
    //   LIBRE   → borde negro
    //   OCUPADO → borde dorado/naranja
    //   COBRO   → borde terracota/rojo
    // ─────────────────────────────────────────────
    public enum EstadoMesa {
        LIBRE,
        OCUPADO,
        COBRO
    }

    // ─────────────────────────────────────────────
    // ATRIBUTOS
    // Corresponden a las columnas de la tabla `mesas`
    // ─────────────────────────────────────────────
    private int        idMesa;     // PK de la tabla
    private int        numero;     // Número visible en la tarjeta
    private EstadoMesa estado;     // Estado actual de la mesa
    private double     total;      // Total acumulado del pedido ($)
    private int        tiempoMin;  // Minutos desde que se ocupó (0 = libre)

    // ─────────────────────────────────────────────
    // CONSTRUCTOR COMPLETO (con id, para uso con BD)
    // ─────────────────────────────────────────────
    public Mesa(int idMesa, int numero, EstadoMesa estado, double total, int tiempoMin) {
        this.idMesa    = idMesa;
        this.numero    = numero;
        this.estado    = estado;
        this.total     = total;
        this.tiempoMin = tiempoMin;
    }

    // ─────────────────────────────────────────────
    // CONSTRUCTOR SIN ID (para datos de prueba)
    // ─────────────────────────────────────────────
    public Mesa(int numero, EstadoMesa estado, double total, int tiempoMin) {
        this(0, numero, estado, total, tiempoMin);
    }

    // ─────────────────────────────────────────────
    // GETTERS Y SETTERS
    // Los setters permiten actualizar el estado
    // cuando lleguen datos nuevos de la BD
    // ─────────────────────────────────────────────
    public int        getIdMesa()   { return idMesa; }
    public int        getNumero()   { return numero; }
    public EstadoMesa getEstado()   { return estado; }
    public double     getTotal()    { return total; }
    public int        getTiempoMin(){ return tiempoMin; }

    public void setIdMesa(int idMesa)         { this.idMesa    = idMesa; }
    public void setNumero(int numero)         { this.numero    = numero; }
    public void setEstado(EstadoMesa estado)  { this.estado    = estado; }
    public void setTotal(double total)        { this.total     = total; }
    public void setTiempoMin(int tiempoMin)   { this.tiempoMin = tiempoMin; }
}
