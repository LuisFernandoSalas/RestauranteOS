package vistas;

import javax.swing.*;
import java.awt.*;

// Importamos el ApiClient
import servicios.ApiClient;

public class VentanaAdmin extends JFrame {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // ─────────────────────────────────────────────
    private static final Color COLOR_SIDEBAR_BG     = new Color(0x7A2E10);
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(0x9B3D18);
    private static final Color COLOR_SIDEBAR_HEADER = new Color(0x5C1F08);
    private static final Color COLOR_BG             = new Color(0xFBF5EC);
    private static final Color COLOR_TEXT_SIDEBAR   = new Color(0xF5DEC8);
    private static final Color COLOR_NARANJA        = new Color(0xE8A060);

    // ─────────────────────────────────────────────
    // DATOS DE SESIÓN Y SERVICIOS
    // ─────────────────────────────────────────────
    private final String usuarioNombre;
    private final String usuarioRol;

    // Instancia compartida de ApiClient para los paneles que se comunican con Laravel
    private final ApiClient apiClient = new ApiClient();

    // ─────────────────────────────────────────────
    // COMPONENTES PRINCIPALES
    // ─────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel     contentPanel;

    private PanelGeneral       panelGeneral;
    private PanelReportes      panelReportes;
    private PanelMenuProductos panelMenuProductos;
    private PanelRecetas       panelRecetas;
    private PanelCombosPromos  panelCombosPromos;
    private PanelInventario    panelInventario;
    private PanelEmpleados     panelEmpleados;

    private JButton btnGeneral;
    private JButton btnReportes;
    private JButton btnMenu;
    private JButton btnRecetas;
    private JButton btnCombos;
    private JButton btnInventario;
    private JButton btnEmpleados;
    private JButton btnActivo;

    public VentanaAdmin(String usuarioNombre, String usuarioRol) {
        this.usuarioNombre = usuarioNombre;
        this.usuarioRol    = usuarioRol;

        setTitle("RestaurantOS - Administrador");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(),     BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(190, 0));

        JPanel sideHeader = new JPanel();
        sideHeader.setLayout(new BoxLayout(sideHeader, BoxLayout.Y_AXIS));
        sideHeader.setBackground(COLOR_SIDEBAR_HEADER);
        sideHeader.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblRol = new JLabel("Administrador");
        lblRol.setFont(new Font("Arial", Font.BOLD, 16));
        lblRol.setForeground(COLOR_NARANJA);
        lblRol.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSistema = new JLabel("Restaurant OS");
        lblSistema.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSistema.setForeground(COLOR_TEXT_SIDEBAR);
        lblSistema.setAlignmentX(Component.LEFT_ALIGNMENT);

        sideHeader.add(lblRol);
        sideHeader.add(Box.createRigidArea(new Dimension(0, 2)));
        sideHeader.add(lblSistema);
        sidebar.add(sideHeader, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(COLOR_SIDEBAR_BG);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        btnGeneral    = buildNavButton("Panel General");
        btnReportes   = buildNavButton("Reportes");
        btnMenu       = buildNavButton("Menú y Productos");
        btnRecetas    = buildNavButton("Recetas");
        btnCombos     = buildNavButton("Combos y promos");
        btnInventario = buildNavButton("Inventario");
        btnEmpleados  = buildNavButton("Empleados");

        btnGeneral.addActionListener(e    -> navegarA("GENERAL",    btnGeneral));
        btnReportes.addActionListener(e   -> navegarA("REPORTES",   btnReportes));
        btnMenu.addActionListener(e       -> navegarA("MENU",       btnMenu));
        btnRecetas.addActionListener(e    -> navegarA("RECETAS",    btnRecetas));
        btnCombos.addActionListener(e     -> navegarA("COMBOS",     btnCombos));
        btnInventario.addActionListener(e -> navegarA("INVENTARIO", btnInventario));
        btnEmpleados.addActionListener(e  -> navegarA("EMPLEADOS",  btnEmpleados));

        nav.add(btnGeneral);
        nav.add(btnReportes);
        nav.add(btnMenu);
        nav.add(btnRecetas);
        nav.add(btnCombos);
        nav.add(btnInventario);
        nav.add(btnEmpleados);

        sidebar.add(nav,               BorderLayout.CENTER);
        sidebar.add(buildUserFooter(), BorderLayout.SOUTH);

        setNavActivo(btnGeneral);

        return sidebar;
    }

    private JButton buildNavButton(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground().equals(COLOR_SIDEBAR_ACTIVE)) {
                    g2.setColor(COLOR_NARANJA);
                    g2.fillRect(0, 0, 4, getHeight());
                }
                g2.setColor(getBackground());
                g2.fillRect(4, 0, getWidth() - 4, getHeight());
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setForeground(COLOR_TEXT_SIDEBAR);
        btn.setBackground(COLOR_SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        return btn;
    }

    private void setNavActivo(JButton btn) {
        if (btnActivo != null) {
            btnActivo.setBackground(COLOR_SIDEBAR_BG);
            btnActivo.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        btnActivo = btn;
        btn.setBackground(COLOR_SIDEBAR_ACTIVE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.repaint();
    }

    public void navegarA(String card) {
        switch (card) {
            case "GENERAL"    -> navegarA(card, btnGeneral);
            case "REPORTES"   -> navegarA(card, btnReportes);
            case "MENU"       -> navegarA(card, btnMenu);
            case "RECETAS"    -> navegarA(card, btnRecetas);
            case "COMBOS"     -> navegarA(card, btnCombos);
            case "INVENTARIO" -> navegarA(card, btnInventario);
            case "EMPLEADOS"  -> navegarA(card, btnEmpleados);
        }
    }

    private void navegarA(String card, JButton btn) {
        // 1. Cambia la pantalla visible y marca el botón activo
        cardLayout.show(contentPanel, card);
        setNavActivo(btn);

        // 2. Identifica qué panel se acaba de mostrar
        JPanel panelActual = switch (card) {
            case "GENERAL"    -> panelGeneral;
            case "REPORTES"   -> panelReportes;
            case "MENU"       -> panelMenuProductos;
            case "RECETAS"    -> panelRecetas;
            case "COMBOS"     -> panelCombosPromos;
            case "INVENTARIO" -> panelInventario;
            case "EMPLEADOS"  -> panelEmpleados;
            default           -> null;
        };

        // 3. Si el panel implementa 'Actualizables', le pide datos frescos al servidor
        if (panelActual instanceof Actualizables) {
            ((Actualizables) panelActual).recargarDatos();
        }
    }

    private JPanel buildUserFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        panel.setBackground(COLOR_SIDEBAR_HEADER);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_NARANJA);
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
    // ÁREA DE CONTENIDO PRINCIPAL (CardLayout)
    // ═══════════════════════════════════════════════
    private JPanel buildContentArea() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(COLOR_BG);

        // Instanciar cada panel una sola vez
        panelGeneral       = new PanelGeneral(this);
        panelReportes      = new PanelReportes();
        panelMenuProductos = new PanelMenuProductos();

        // AQUÍ ESTABA EL DETALLE: Pasamos la instancia de apiClient
        panelRecetas       = new PanelRecetas(apiClient);

        panelCombosPromos  = new PanelCombosPromos();
        panelInventario    = new PanelInventario();
        panelEmpleados     = new PanelEmpleados();

        // Registrar cada panel con su clave en el CardLayout
        contentPanel.add(panelGeneral,       "GENERAL");
        contentPanel.add(panelReportes,      "REPORTES");
        contentPanel.add(panelMenuProductos, "MENU");
        contentPanel.add(panelRecetas,       "RECETAS");
        contentPanel.add(panelCombosPromos,  "COMBOS");
        contentPanel.add(panelInventario,    "INVENTARIO");
        contentPanel.add(panelEmpleados,     "EMPLEADOS");

        panelMenuProductos.setListenerNavegarRecetas(() -> navegarA("RECETAS"));

        cardLayout.show(contentPanel, "GENERAL");
        return contentPanel;
    }
}