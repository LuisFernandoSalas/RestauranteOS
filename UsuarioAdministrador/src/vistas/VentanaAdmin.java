package vistas;

import javax.swing.*;
import java.awt.*;



public class VentanaAdmin extends JFrame {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // Misma paleta terracota/crema del sistema
    // ─────────────────────────────────────────────
    private static final Color COLOR_SIDEBAR_BG     = new Color(0x7A2E10); // Terracota sidebar
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(0x9B3D18); // Item activo
    private static final Color COLOR_SIDEBAR_HEADER = new Color(0x5C1F08); // Encabezado sidebar
    private static final Color COLOR_BG             = new Color(0xFBF5EC); // Crema — fondo contenido
    private static final Color COLOR_TEXT_SIDEBAR   = new Color(0xF5DEC8); // Texto claro sidebar
    private static final Color COLOR_NARANJA        = new Color(0xE8A060); // Naranja — detalles

    // ─────────────────────────────────────────────
    // DATOS DE SESIÓN
    // Recibidos desde Login después de autenticar.
    // TODO (BD): cambiar a objeto Usuario con todos
    //            los campos del ResultSet
    // ─────────────────────────────────────────────
    private final String usuarioNombre; // Nombre del administrador logueado
    private final String usuarioRol;    // Rol: "Administrador"

    // ─────────────────────────────────────────────
    // COMPONENTES PRINCIPALES
    // ─────────────────────────────────────────────
    private CardLayout cardLayout;   // Controla qué panel se muestra
    private JPanel     contentPanel; // Panel contenedor de todas las vistas

    // Paneles de cada sección del menú
    private PanelGeneral       panelGeneral;
    private PanelReportes      panelReportes;
    private PanelMenuProductos panelMenuProductos;
    private PanelRecetas       panelRecetas;
    private PanelCombosPromos  panelCombosPromos;
    private PanelInventario    panelInventario;
    private PanelEmpleados     panelEmpleados;

    // Botones del sidebar para manejar estado activo
    private JButton btnGeneral;
    private JButton btnReportes;
    private JButton btnMenu;
    private JButton btnRecetas;
    private JButton btnCombos;
    private JButton btnInventario;
    private JButton btnEmpleados;
    private JButton btnActivo; // Botón actualmente seleccionado

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // @param usuarioNombre  Nombre del administrador
    // @param usuarioRol     Rol: "Administrador"
    // ─────────────────────────────────────────────
    public VentanaAdmin(String usuarioNombre, String usuarioRol) {
        this.usuarioNombre = usuarioNombre;
        this.usuarioRol    = usuarioRol;

        setTitle("RestaurantOS - Administrador");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildSidebar(),     BorderLayout.WEST);   // Sidebar fijo
        add(buildContentArea(), BorderLayout.CENTER); // Contenido dinámico

        setVisible(true);
    }

    // ═══════════════════════════════════════════════
    // SIDEBAR
    // Panel lateral fijo con:
    //   - Encabezado: rol + nombre del sistema
    //   - Menú de navegación con 7 opciones
    //   - Pie: avatar circular + nombre + rol
    // ═══════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(190, 0));

        // ── Encabezado del sidebar ──
        JPanel sideHeader = new JPanel();
        sideHeader.setLayout(new BoxLayout(sideHeader, BoxLayout.Y_AXIS));
        sideHeader.setBackground(COLOR_SIDEBAR_HEADER);
        sideHeader.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Rol del usuario (naranja para distinguir del cajero)
        JLabel lblRol = new JLabel("Administrador");
        lblRol.setFont(new Font("Arial", Font.BOLD, 16));
        lblRol.setForeground(COLOR_NARANJA);
        lblRol.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nombre del sistema
        JLabel lblSistema = new JLabel("Restaurant OS");
        lblSistema.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSistema.setForeground(COLOR_TEXT_SIDEBAR);
        lblSistema.setAlignmentX(Component.LEFT_ALIGNMENT);

        sideHeader.add(lblRol);
        sideHeader.add(Box.createRigidArea(new Dimension(0, 2)));
        sideHeader.add(lblSistema);
        sidebar.add(sideHeader, BorderLayout.NORTH);

        // ── Menú de navegación ──
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(COLOR_SIDEBAR_BG);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Crear botones del menú
        btnGeneral    = buildNavButton("Panel General");
        btnReportes   = buildNavButton("Reportes");
        btnMenu       = buildNavButton("Menú y Productos");
        btnRecetas    = buildNavButton("Recetas");
        btnCombos     = buildNavButton("Combos y promos");
        btnInventario = buildNavButton("Inventario");
        btnEmpleados  = buildNavButton("Empleados");

        // Asignar acción de navegación a cada botón
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

        // Activar Panel General por defecto al abrir
        setNavActivo(btnGeneral);

        return sidebar;
    }

    /**
     * Crea un botón de navegación del sidebar con estilo uniforme.
     * Pinta su propio fondo para mostrar el indicador izquierdo
     * naranja cuando está activo.
     *
     * @param texto Texto a mostrar en el botón
     */
    private JButton buildNavButton(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Barra izquierda naranja si está activo
                if (getBackground().equals(COLOR_SIDEBAR_ACTIVE)) {
                    g2.setColor(COLOR_NARANJA);
                    g2.fillRect(0, 0, 4, getHeight());
                }
                // Fondo del botón
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

    /**
     * Cambia el botón activo en el sidebar:
     * - Quita el resaltado del botón anterior
     * - Aplica fondo activo y texto en negrita al nuevo
     *
     * @param btn Botón a activar
     */
    private void setNavActivo(JButton btn) {
        // Restaurar estilo del botón anterior
        if (btnActivo != null) {
            btnActivo.setBackground(COLOR_SIDEBAR_BG);
            btnActivo.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        // Aplicar estilo activo al nuevo botón
        btnActivo = btn;
        btn.setBackground(COLOR_SIDEBAR_ACTIVE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.repaint();
    }

    /**
     * Navega a la sección indicada:
     * - Muestra la carta correspondiente en el CardLayout
     * - Actualiza el botón activo en el sidebar
     *
     * @param card Nombre de la carta en el CardLayout
     * @param btn  Botón del sidebar que se activa
     */
    /**
     * Navegación pública — permite que paneles hijos
     * (como PanelGeneral o PanelMenuProductos) puedan
     * cambiar la sección activa.
     * @param card Clave del CardLayout
     */
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

    /** Versión privada usada internamente por el sidebar */
    private void navegarA(String card, JButton btn) {
        cardLayout.show(contentPanel, card);
        setNavActivo(btn);
    }

    /**
     * Pie del sidebar: avatar circular con inicial del nombre,
     * nombre del usuario y su rol.
     *
     * TODO (BD): usar nombre real del objeto Usuario
     *            obtenido en el login
     */
    private JPanel buildUserFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        panel.setBackground(COLOR_SIDEBAR_HEADER);

        // Avatar circular con la inicial del nombre
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Círculo naranja
                g2.setColor(COLOR_NARANJA);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Inicial del nombre en blanco
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

        // Info: nombre + rol
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // Truncar nombre si supera 12 caracteres
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
        panelGeneral       = new PanelGeneral(this); // Pasar referencia para acciones rápidas
        panelReportes      = new PanelReportes();
        panelMenuProductos = new PanelMenuProductos();
        panelRecetas       = new PanelRecetas();
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

        // Conectar el botón "+ Agregar Receta" de Menú y Productos
        // para que navegue directo al apartado de Recetas
        panelMenuProductos.setListenerNavegarRecetas(() -> navegarA("RECETAS"));

        // Mostrar Panel General al abrir
        cardLayout.show(contentPanel, "GENERAL");
        return contentPanel;
    }
}