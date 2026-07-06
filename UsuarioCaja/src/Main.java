import vistas.Login;

/**
 * ═══════════════════════════════════════════════════════
 *  RestaurantOS - Punto de entrada principal
 * ═══════════════════════════════════════════════════════
 *  Esta clase es el inicio de la aplicación.
 *  IntelliJ la detecta automáticamente como punto
 *  de entrada por estar en la raíz de src/ sin paquete.
 *
 *  Flujo de la aplicación:
 *    Main → Login → VentanaPrincipal
 *                      ├── PanelMesas
 *                      ├── PanelHistorial
 *                      └── PanelCorteCaja
 * ═══════════════════════════════════════════════════════
 */
public class Main {

    public static void main(String[] args) {
        // Lanza la pantalla de login en el hilo de UI de Swing
        javax.swing.SwingUtilities.invokeLater(Login::new);
    }
}