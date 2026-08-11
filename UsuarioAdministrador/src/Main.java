import vistas.Login;

/**
 * ═══════════════════════════════════════════════════════
 *  RestaurantOS - Administrador — Punto de entrada
 * ═══════════════════════════════════════════════════════
 *  Lanza la pantalla de Login en el hilo de UI de Swing.
 *
 *  Flujo de la aplicación:
 *    Main → Login → VentanaAdmin
 *                      ├── PanelGeneral
 *                      ├── PanelReportes
 *                      ├── PanelMenuProductos
 *                      ├── PanelRecetas
 *                      ├── PanelCombosPromos
 *                      ├── PanelInventario
 *                      └── PanelEmpleados
 * ═══════════════════════════════════════════════════════
 */
public class Main {

    public static void main(String[] args) {
        // Ejecutar en el Event Dispatch Thread (EDT) de Swing
        javax.swing.SwingUtilities.invokeLater(Login::new);
    }
}
