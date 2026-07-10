package vistas;

import modelos.Mesa;

import javax.swing.*;
import java.awt.*;

/**
 * ═══════════════════════════════════════════════════════
 *  Vista: VentanaPrincipal
 * ═══════════════════════════════════════════════════════
 *  JFrame raíz que contiene:
 *    - Sidebar fijo (navegación + datos del usuario)
 *    - Panel de contenido dinámico (CardLayout)
 *
 *  Los paneles de cada sección viven en sus propias
 *  clases (PanelMesas, PanelHistorial, PanelCorteCaja)
 *  y se cargan aquí como "cartas" del CardLayout.
 *
 *  Recibe nombre y rol del usuario desde Login para
 *  mostrarlos en el pie del sidebar.
 *
 *  TODO (BD): recibir un objeto Usuario completo
 *  (con id, nombre, rol) en lugar de Strings sueltos.
 * ═══════════════════════════════════════════════════════
 */
public class VentanaPrincipal extends JFrame {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // ─────────────────────────────────────────────
    private static final Color COLOR_SIDEBAR_BG     = new Color(0x7A2E10);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(0x9B3D18);
    private static final Color COLOR_SIDEBAR_HEADER = new Color(0x5C1F08);
    private static final Color COLOR_BG             = new Color(0xFBF5EC);
    private static final Color COLOR_TEXT_SIDEBAR   = new Color(0xF5DEC8);

    // ─────────────────────────────────────────────
    // DATOS DE SESIÓN
    // Recibidos desde Login después de autenticar
    // ─────────────────────────────────────────────
    private final String usuarioNombre;
    private final String usuarioRol;

    // ─────────────────────────────────────────────
    // COMPONENTES PRINCIPALES
    // ─────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel     contentPanel;

    // Paneles de cada sección
    private PanelMesas       panelMesas;
    private PanelHistorial   panelHistorial;
    private PanelCorteCaja   panelCorteCaja;
    private PanelCobro       panelCobro;

    // Botones del menú
    private JButton btnMesas;
    private JButton btnHistorial;
    private JButton btnCorte;
    private JButton btnActivo;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // @param usuarioNombre  nombre del usuario logueado
    // @param usuarioRol     rol: Cajero, Administrador, etc.
    // ─────────────────────────────────────────────
    // 🛠️ CONSTRUCTOR REACOMODADO
    public VentanaPrincipal(String usuarioNombre, String usuarioRol, String token) {
        this.usuarioNombre = usuarioNombre;
        this.usuarioRol    = usuarioRol;

        setTitle("RestaurantOS");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🔑 PRIMERO: Instanciamos el panel global pasándole el token directo
        this.panelMesas = new PanelMesas();

        // 🏗️ SEGUNDO: Construimos lo visual de forma segura
        add(buildSidebar(),      BorderLayout.WEST);
        add(buildContentArea(),  BorderLayout.CENTER);

        setVisible(true);
    }

    // 🛠️ ÁREA DE CONTENIDO REPARADA

    // ═══════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(190, 0));

        // Encabezado
        JPanel sideHeader = new JPanel();
        sideHeader.setLayout(new BoxLayout(sideHeader, BoxLayout.Y_AXIS));
        sideHeader.setBackground(COLOR_SIDEBAR_HEADER);
        sideHeader.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblRol = new JLabel("Caja");
        lblRol.setFont(new Font("Arial", Font.BOLD, 20));
        lblRol.setForeground(new Color(0xE8A060));
        lblRol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSistema = new JLabel("Restaurant OS");
        lblSistema.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSistema.setForeground(COLOR_TEXT_SIDEBAR);
        lblSistema.setAlignmentX(Component.LEFT_ALIGNMENT);

        sideHeader.add(lblRol);
        sideHeader.add(Box.createRigidArea(new Dimension(0, 2)));
        sideHeader.add(lblSistema);
        sidebar.add(sideHeader, BorderLayout.NORTH);

        // Navegación
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(COLOR_SIDEBAR_BG);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        btnMesas     = buildNavButton("Mesas");
        btnHistorial = buildNavButton("Historial");
        btnCorte     = buildNavButton("Corte de caja");

        btnMesas.addActionListener(e     -> navegarA("MESAS",     btnMesas));
        btnHistorial.addActionListener(e -> navegarA("HISTORIAL", btnHistorial));
        btnCorte.addActionListener(e     -> navegarA("CORTE",     btnCorte));

        nav.add(btnMesas);
        nav.add(btnHistorial);
        nav.add(btnCorte);

        sidebar.add(nav,              BorderLayout.CENTER);
        sidebar.add(buildUserFooter(),BorderLayout.SOUTH);

        setNavActivo(btnMesas);
        return sidebar;
    }

    private JButton buildNavButton(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground().equals(COLOR_SIDEBAR_ACTIVE)) {
                    g2.setColor(new Color(0xE8A060));
                    g2.fillRect(0, 0, 4, getHeight());
                }
                g2.setColor(getBackground());
                g2.fillRect(4, 0, getWidth() - 4, getHeight());
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 15));
        btn.setForeground(COLOR_TEXT_SIDEBAR);
        btn.setBackground(COLOR_SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }

    private void setNavActivo(JButton btn) {
        if (btnActivo != null) {
            btnActivo.setBackground(COLOR_SIDEBAR_BG);
            btnActivo.setFont(new Font("Arial", Font.PLAIN, 15));
        }
        btnActivo = btn;
        btn.setBackground(COLOR_SIDEBAR_ACTIVE);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.repaint();
    }

    private void navegarA(String card, JButton btn) {
        cardLayout.show(contentPanel, card);
        setNavActivo(btn);
    }

    // ─────────────────────────────────────────────
    // PIE DEL SIDEBAR: avatar + nombre + rol
    // ─────────────────────────────────────────────
    private JPanel buildUserFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        panel.setBackground(COLOR_SIDEBAR_HEADER);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE8A060));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String inicial = String.valueOf(usuarioNombre.charAt(0)).toUpperCase();
                g2.drawString(inicial,
                        (getWidth()  - fm.stringWidth(inicial)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // Truncar nombre si es muy largo
        String nombreCorto = usuarioNombre.length() > 12
                ? usuarioNombre.substring(0, 12) + "..."
                : usuarioNombre;

        JLabel lblNombre = new JLabel(nombreCorto);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 13));
        lblNombre.setForeground(Color.WHITE);

        JLabel lblRolUser = new JLabel(usuarioRol);
        lblRolUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRolUser.setForeground(COLOR_TEXT_SIDEBAR);

        info.add(lblNombre);
        info.add(lblRolUser);
        panel.add(avatar);
        panel.add(info);
        return panel;
    }

    // ═══════════════════════════════════════════════
    // ÁREA DE CONTENIDO (CardLayout)
    // Instancia cada panel una sola vez y lo registra
    // ═══════════════════════════════════════════════
    private JPanel buildContentArea() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_BG);

        // ❌ BORRA la línea que decía: panelMesas = new PanelMesas();
        // Ya lo creamos arriba en el constructor con su token.

        panelHistorial  = new PanelHistorial();
        panelCorteCaja  = new PanelCorteCaja();
        panelCobro      = new PanelCobro();
        panelCobro.setVentana(this);

        // Agregamos al contenedor (panelMesas ya está vivo y tiene el token)
        contentPanel.add(panelMesas,     "MESAS");
        contentPanel.add(panelHistorial, "HISTORIAL");
        contentPanel.add(panelCorteCaja, "CORTE");
        contentPanel.add(panelCobro,     "COBRO_MESA");

        cardLayout.show(contentPanel, "MESAS");
        return contentPanel;
    }


    // ═══════════════════════════════════════════════
    // NAVEGACIÓN PÚBLICA
    // Llamada desde PanelCobro para regresar a mesas
    // ═══════════════════════════════════════════════

    /** Regresa a PanelMesas y activa su botón en el sidebar */
    public void navegarAMesas() {
        cardLayout.show(contentPanel, "MESAS");
        setNavActivo(btnMesas);
    }

    /** Abre PanelCobro cargando los datos de la mesa */
    /** Abre PanelCobro preparando la llamada a la API */
    public void abrirCobro(Mesa mesa) {
        // 🚀 MAÑANA: Haremos la petición a Laravel en este hilo secundario
        new Thread(() -> {
            try {
                // Mañana aquí irá: DetalleCobro detalle = api.CobroService.obtenerDetalle(mesa.getId());

                SwingUtilities.invokeLater(() -> {
                    // Por esta noche, lo dejamos con valores vacíos para que compile limpio
                    panelCobro.cargarPedido(
                            mesa,
                            "Cargando...",    // Mañana será detalle.getMesero()
                            0,                // Mañana será detalle.getMinutos()
                            new ArrayList<>() // Mañana serán los items reales
                    );
                    cardLayout.show(contentPanel, "COBRO_MESA");
                    setNavActivo(btnMesas);
                });
            } catch (Exception e) {
                System.err.println("Error preparando la vista de cobro: " + e.getMessage());
            }
        }).start();
    }
}
