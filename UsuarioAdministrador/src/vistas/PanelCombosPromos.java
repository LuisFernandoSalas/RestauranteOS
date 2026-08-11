package vistas;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista: PanelCombosPromos — Combos y Promos
 */
public class PanelCombosPromos extends JPanel {

    // ─── PALETA DE COLORES ─────────────────────────
    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_DIV_LINE  = new Color(0xC8A882);
    private static final Color C_CAMPO_BG  = new Color(0xEEE8DE);
    private static final Color C_CAMPO_BOR = new Color(0xD4C4A8);
    private static final Color C_BTN_DARK  = new Color(0x4A2010);
    private static final Color C_BTN_DONE  = new Color(0x6B2D1A);
    private static final Color C_BTN_DEL   = new Color(0xC03020);

    private static final Color C_TBL_HDR   = new Color(0x7A3520);
    private static final Color C_TBL_HDR_T = Color.WHITE;
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_ALT_ROW   = new Color(0xFAF4EE);
    private static final Color C_ACT_TEXT  = new Color(0xD48000);
    private static final Color C_PAU_TEXT  = new Color(0x888888);

    // ─── ESTRUCTURA Y PROPORCIONES DE LA TABLA ─────
    private static final double[] PW = {0.16, 0.27, 0.11, 0.11, 0.14, 0.10, 0.11};
    private static final String[] COLS = {"Nombre", "Productos", "Precio", "Ahorro", "Vigencia", "Estado", "Acciones"};
    private static final int[] COL_ALIGN = {
            SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.RIGHT,
            SwingConstants.RIGHT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER
    };

    // ─── MODELO DE DATOS ───────────────────────────
    static class ProductoItem {
        String id, nombre;
        double precio;
        ProductoItem(String id, String n, double p) {
            this.id = id; this.nombre = n; this.precio = p;
        }
    }

    static class ComboItem {
        String id, nombre, productosStr, precio, ahorro, vigencia, estado;
        ComboItem(String id, String n, String prod, String p, String a, String v, String e) {
            this.id = id; this.nombre = n; this.productosStr = prod;
            this.precio = p; this.ahorro = a; this.vigencia = v; this.estado = e;
        }
    }

    // ─── LISTAS DE DATOS ───────────────────────────
    private final List<ProductoItem> productosDisponibles = new ArrayList<>();
    private final List<ProductoItem> carritoCombo = new ArrayList<>();
    private final List<ComboItem> combosRegistrados = new ArrayList<>();

    // ─── COMPONENTES FORMULARIO ────────────────────
    private JTextField txtNombreCombo, txtPrecioEspecial, txtFechaInicio, txtFechaFin;
    private JPanel panelListaProds, panelCarritoItems, panelTabla;
    private JLabel lblPrecioInd, lblPrecioCombo, lblAhorroCliente;

    // ─── CONSTRUCTOR ───────────────────────────────
    public PanelCombosPromos() {
        setLayout(new BorderLayout());
        setBackground(C_BG);
        inicializarDatosDummy();

        JPanel contenido = buildContenido();
        JScrollPane scrollGeneral = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGeneral.setBorder(BorderFactory.createEmptyBorder());
        scrollGeneral.getViewport().setBackground(C_BG);
        scrollGeneral.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollGeneral, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════
    // ESTRUCTURA PRINCIPAL DE LA VISTA
    // ═══════════════════════════════════════════════
    private JPanel buildContenido() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.anchor  = GridBagConstraints.NORTH;
        gc.gridx   = 0;

        // 1. Título principal
        gc.gridy = 0; gc.insets = new Insets(0, 0, 18, 0);
        p.add(buildHeader(), gc);

        // 2. Subtítulo "Crear combo / promoción"
        gc.gridy = 1; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Crear combo / promoción"), gc);

        // 3. Campos superiores
        gc.gridy = 2; gc.insets = new Insets(0, 0, 16, 0);
        p.add(buildCamposCreacion(), gc);

        // 4. Panel de Selección y Carrito
        gc.gridy = 3; gc.insets = new Insets(0, 0, 28, 0);
        p.add(buildPanelSeleccionYCarrito(), gc);

        // 5. Subtítulo "Combos registrados"
        gc.gridy = 4; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Combos registrados"), gc);

        // 6. Tabla unificada (GridBagLayout para filas y encabezado juntos)
        panelTabla = new JPanel();
        panelTabla.setOpaque(false);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 0, 0);
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTH;
        gc.fill = GridBagConstraints.BOTH;
        p.add(panelTabla, gc);

        poblarTablaCombos();
        return p;
    }

    private JPanel buildHeader() {
        JLabel lblTitulo = new JLabel("Combos y Promos");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(C_ACCENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV_LINE);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(lblTitulo, BorderLayout.NORTH);
        p.add(sep, BorderLayout.CENTER);
        return p;
    }

    private JLabel mkSubtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.BOLD, 17));
        l.setForeground(C_ACCENT);
        return l;
    }

    // ═══════════════════════════════════════════════
    // FORMULARIO DE CREACIÓN DE COMBOS
    // ═══════════════════════════════════════════════
    private JPanel buildCamposCreacion() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 16, 12));
        panel.setOpaque(false);

        txtNombreCombo    = new JTextField();
        txtPrecioEspecial = new JTextField();
        txtFechaInicio    = new JTextField();
        txtFechaFin       = new JTextField();

        panel.add(buildCampo(txtNombreCombo, "Nombre del combo"));
        panel.add(buildCampo(txtPrecioEspecial, "Precio especial ($)"));
        panel.add(buildCampo(txtFechaInicio, "Fecha inicio"));
        panel.add(buildCampo(txtFechaFin, "Fecha fin (vacío = permanente)"));

        txtPrecioEspecial.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                actualizarResumenCarrito();
            }
        });

        return panel;
    }

    private JPanel buildPanelSeleccionYCarrito() {
        JPanel grid = new JPanel(new GridLayout(1, 2, 24, 0));
        grid.setOpaque(false);

        // ── COLUMNA IZQUIERDA: PRODUCTOS DISPONIBLES ──
        JPanel colIzql = new JPanel(new BorderLayout(0, 10));
        colIzql.setOpaque(false);

        JLabel lblH1 = buildHeaderSeccion("PRODUCTOS DISPONIBLES", C_TBL_HDR);
        JTextField txtBuscar = new JTextField();
        JPanel wrapSearch = buildCampo(txtBuscar, "Buscar producto...");

        panelListaProds = new JPanel();
        panelListaProds.setLayout(new BoxLayout(panelListaProds, BoxLayout.Y_AXIS));
        panelListaProds.setOpaque(false);

        JScrollPane scrollProds = new JScrollPane(panelListaProds,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollProds.setBorder(BorderFactory.createEmptyBorder());
        scrollProds.setOpaque(false);
        scrollProds.getViewport().setOpaque(false);
        scrollProds.setPreferredSize(new Dimension(0, 220));
        scrollProds.getVerticalScrollBar().setUnitIncrement(12);

        poblarListaProductos();

        colIzql.add(lblH1, BorderLayout.NORTH);

        JPanel pCentroIzql = new JPanel(new BorderLayout(0, 10));
        pCentroIzql.setOpaque(false);
        pCentroIzql.add(wrapSearch, BorderLayout.NORTH);
        pCentroIzql.add(scrollProds, BorderLayout.CENTER);
        colIzql.add(pCentroIzql, BorderLayout.CENTER);

        // ── COLUMNA DERECHA: CARRITO DEL COMBO ──
        JPanel colDer = new JPanel(new BorderLayout(0, 10));
        colDer.setOpaque(false);

        JLabel lblH2 = buildHeaderSeccion("CARRITO DEL COMBO", C_TBL_HDR);

        panelCarritoItems = new JPanel();
        panelCarritoItems.setLayout(new BoxLayout(panelCarritoItems, BoxLayout.Y_AXIS));
        panelCarritoItems.setOpaque(false);

        JScrollPane scrollCarrito = new JScrollPane(panelCarritoItems,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCarrito.setBorder(BorderFactory.createEmptyBorder());
        scrollCarrito.setOpaque(false);
        scrollCarrito.getViewport().setOpaque(false);
        scrollCarrito.setPreferredSize(new Dimension(0, 160));
        scrollCarrito.getVerticalScrollBar().setUnitIncrement(12);

        JPanel panelResumen = new JPanel(new GridLayout(3, 1, 0, 4));
        panelResumen.setOpaque(false);
        panelResumen.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        lblPrecioInd      = new JLabel("Precio individual: $0.00");
        lblPrecioCombo    = new JLabel("Precio combo: $0.00");
        lblAhorroCliente  = new JLabel("Ahorro cliente: $0.00 (0%)");

        lblPrecioInd.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPrecioCombo.setFont(new Font("Arial", Font.BOLD, 14));
        lblPrecioCombo.setForeground(C_ACCENT);
        lblAhorroCliente.setFont(new Font("Arial", Font.BOLD, 14));
        lblAhorroCliente.setForeground(C_ACT_TEXT);

        panelResumen.add(lblPrecioInd);
        panelResumen.add(lblPrecioCombo);
        panelResumen.add(lblAhorroCliente);

        JButton btnCrear = new JButton("Crear combo") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_TBL_HDR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(C_WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnCrear.setFont(new Font("Arial", Font.BOLD, 15));
        btnCrear.setContentAreaFilled(false); btnCrear.setBorderPainted(false); btnCrear.setFocusPainted(false);
        btnCrear.setPreferredSize(new Dimension(0, 42));
        btnCrear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCrear.addActionListener(e -> onCrearCombo());

        JPanel pCentroDer = new JPanel(new BorderLayout(0, 6));
        pCentroDer.setOpaque(false);
        pCentroDer.add(scrollCarrito, BorderLayout.NORTH);
        pCentroDer.add(panelResumen, BorderLayout.CENTER);
        pCentroDer.add(btnCrear, BorderLayout.SOUTH);

        colDer.add(lblH2, BorderLayout.NORTH);
        colDer.add(pCentroDer, BorderLayout.CENTER);

        grid.add(colIzql);
        grid.add(colDer);
        return grid;
    }

    private JLabel buildHeaderSeccion(String titulo, Color bg) {
        JLabel l = new JLabel(titulo, SwingConstants.LEFT);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(C_WHITE);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return l;
    }

    private void poblarListaProductos() {
        panelListaProds.removeAll();
        for (ProductoItem prod : productosDisponibles) {
            boolean enCarrito = carritoCombo.contains(prod);

            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(enCarrito ? new Color(0xF3E5D8) : C_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(enCarrito ? C_CAMPO_BOR : new Color(0xE5E5E5), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JLabel lblInfo = new JLabel("<html><b>" + prod.nombre + "</b><br><font color='#888888'>$" + String.format("%.2f", prod.precio) + "</font></html>");

            JButton btnAccion = new JButton(enCarrito ? "Listo" : "+ Agregar") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(enCarrito ? C_BTN_DONE : C_BTN_DARK);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(C_WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
            };
            btnAccion.setFont(new Font("Arial", Font.BOLD, 12));
            btnAccion.setContentAreaFilled(false); btnAccion.setBorderPainted(false); btnAccion.setFocusPainted(false);
            btnAccion.setPreferredSize(new Dimension(85, 28));
            btnAccion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnAccion.addActionListener(e -> {
                if (enCarrito) {
                    carritoCombo.remove(prod);
                } else {
                    carritoCombo.add(prod);
                }
                poblarListaProductos();
                poblarCarritoUI();
            });

            card.add(lblInfo, BorderLayout.CENTER);
            card.add(btnAccion, BorderLayout.EAST);

            panelListaProds.add(card);
            panelListaProds.add(Box.createVerticalStrut(6));
        }
        panelListaProds.revalidate(); panelListaProds.repaint();
    }

    private void poblarCarritoUI() {
        panelCarritoItems.removeAll();
        for (ProductoItem prod : carritoCombo) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(C_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE5E5E5), 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            JLabel lblInfo = new JLabel("<html><b>" + prod.nombre + "</b><br><font color='#888888'>$" + String.format("%.2f", prod.precio) + "</font></html>");

            JButton btnEliminar = new JButton("Eliminar") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(C_BTN_DEL);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.setColor(C_WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                }
            };
            btnEliminar.setFont(new Font("Arial", Font.BOLD, 11));
            btnEliminar.setContentAreaFilled(false); btnEliminar.setBorderPainted(false); btnEliminar.setFocusPainted(false);
            btnEliminar.setPreferredSize(new Dimension(75, 26));
            btnEliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnEliminar.addActionListener(e -> {
                carritoCombo.remove(prod);
                poblarListaProductos();
                poblarCarritoUI();
            });

            card.add(lblInfo, BorderLayout.CENTER);
            card.add(btnEliminar, BorderLayout.EAST);

            panelCarritoItems.add(card);
            panelCarritoItems.add(Box.createVerticalStrut(6));
        }
        actualizarResumenCarrito();
        panelCarritoItems.revalidate(); panelCarritoItems.repaint();
    }

    private void actualizarResumenCarrito() {
        double ind = 0.0;
        for (ProductoItem p : carritoCombo) ind += p.precio;

        double comboVal = 0.0;
        try {
            String txtP = txtPrecioEspecial.getText().replaceAll("[^0-9.]", "").trim();
            if (!txtP.isEmpty()) comboVal = Double.parseDouble(txtP);
        } catch (Exception ignored) {}

        double ahorro = Math.max(0, ind - comboVal);
        double pct = (ind > 0 && ahorro > 0) ? (ahorro / ind) * 100 : 0.0;

        lblPrecioInd.setText("Precio individual: $" + String.format("%.2f", ind));
        lblPrecioCombo.setText("Precio combo: $" + String.format("%.2f", comboVal));
        lblAhorroCliente.setText(String.format("Ahorro cliente: $%.2f (%.0f%%)", ahorro, pct));
    }

    // ═══════════════════════════════════════════════
    // TABLA REGISTRADOS (GridBagLayout UNIFICADO)
    // ═══════════════════════════════════════════════
    private void poblarTablaCombos() {
        panelTabla.removeAll();
        panelTabla.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0.0;

        // 1. DIBUJAR ENCABEZADO (Fila 0)
        for (int col = 0; col < COLS.length; col++) {
            g.gridx = col;
            g.gridy = 0;
            g.weightx = PW[col];

            JLabel lblH = new JLabel(COLS[col], COL_ALIGN[col]);
            lblH.setFont(new Font("Arial", Font.BOLD, 13));
            lblH.setForeground(C_TBL_HDR_T);
            lblH.setOpaque(true);
            lblH.setBackground(C_TBL_HDR);
            lblH.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            panelTabla.add(lblH, g);
        }

        // 2. DIBUJAR FILAS DE DATOS (Filas 1 en adelante)
        for (int row = 0; row < combosRegistrados.size(); row++) {
            ComboItem item = combosRegistrados.get(row);
            Color bgRow = (row % 2 == 0) ? C_WHITE : C_ALT_ROW;
            int gridY = row + 1;

            Border cellBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEDDCC)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            );

            // Nombre
            g.gridx = 0; g.gridy = gridY; g.weightx = PW[0];
            panelTabla.add(mkLblCell(item.nombre, new Color(0x333333), true, COL_ALIGN[0], bgRow, cellBorder), g);

            // Productos (Alineación perfecta asegurada)
            g.gridx = 1; g.gridy = gridY; g.weightx = PW[1];
            panelTabla.add(mkLblCell(item.productosStr, new Color(0x666666), false, COL_ALIGN[1], bgRow, cellBorder), g);

            // Precio
            g.gridx = 2; g.gridy = gridY; g.weightx = PW[2];
            panelTabla.add(mkLblCell(item.precio, new Color(0x333333), true, COL_ALIGN[2], bgRow, cellBorder), g);

            // Ahorro
            g.gridx = 3; g.gridy = gridY; g.weightx = PW[3];
            panelTabla.add(mkLblCell(item.ahorro, C_ACCENT, false, COL_ALIGN[3], bgRow, cellBorder), g);

            // Vigencia
            g.gridx = 4; g.gridy = gridY; g.weightx = PW[4];
            panelTabla.add(mkLblCell(item.vigencia, new Color(0x333333), false, COL_ALIGN[4], bgRow, cellBorder), g);

            // Estado
            g.gridx = 5; g.gridy = gridY; g.weightx = PW[5];
            panelTabla.add(buildTextoEstadoCell(item.estado, bgRow, cellBorder), g);

            // Acciones
            g.gridx = 6; g.gridy = gridY; g.weightx = PW[6];
            panelTabla.add(buildAccionesCell(item, bgRow, cellBorder), g);
        }

        // Espaciador para empujar las filas hacia arriba
        g.gridx = 0;
        g.gridy = combosRegistrados.size() + 1;
        g.gridwidth = COLS.length;
        g.weighty = 1.0;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        panelTabla.add(filler, g);

        panelTabla.revalidate();
        panelTabla.repaint();
    }

    private JLabel mkLblCell(String text, Color fg, boolean bold, int align, Color bg, Border border) {
        JLabel l = new JLabel(text, align);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(border);
        return l;
    }

    private JLabel buildTextoEstadoCell(String estado, Color bg, Border border) {
        boolean activo = estado.equalsIgnoreCase("Activo") || estado.equalsIgnoreCase("Temporal");
        JLabel l = new JLabel(estado, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(activo ? C_ACT_TEXT : C_PAU_TEXT);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(border);
        return l;
    }

    private JPanel buildAccionesCell(ComboItem item, Color bg, Border border) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(border);

        JLabel editar = new JLabel("Editar");
        editar.setFont(new Font("Arial", Font.PLAIN, 13));
        editar.setForeground(C_ACCENT);
        editar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onEditarCombo(item); }
        });

        JLabel pipe = new JLabel("|");
        pipe.setFont(new Font("Arial", Font.PLAIN, 13));
        pipe.setForeground(new Color(0xCCCCCC));

        JLabel eliminar = new JLabel("Eliminar");
        eliminar.setFont(new Font("Arial", Font.PLAIN, 13));
        eliminar.setForeground(C_BTN_DEL);
        eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eliminar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onEliminarCombo(item); }
        });

        p.add(editar); p.add(pipe); p.add(eliminar);
        return p;
    }

    // ═══════════════════════════════════════════════
    // DIÁLOGOS Y LÓGICA DE ACCIONES
    // ═══════════════════════════════════════════════
    private void onCrearCombo() {
        String nom = txtNombreCombo.getText().trim();
        String pre = txtPrecioEspecial.getText().trim();
        String fIni = txtFechaInicio.getText().trim();
        String fFin = txtFechaFin.getText().trim();

        // ── Validar nombre ──
        if (nom.isEmpty() || nom.equals("Nombre del combo")) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo agregar: debes ingresar el nombre del combo.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar que el precio no esté vacío ──
        if (pre.isEmpty() || pre.equals("Precio especial ($)")) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo agregar: debes ingresar el precio especial del combo.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar que haya al menos un producto en el carrito ──
        if (carritoCombo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo agregar: selecciona al menos un producto para el combo.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar que el precio sea un número válido ──
        String preLimpio = pre.replaceAll("[^0-9.]", "");
        double comboP;
        try {
            if (preLimpio.isEmpty()) throw new NumberFormatException();
            comboP = Double.parseDouble(preLimpio);
            if (comboP <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El precio especial debe ser un número válido mayor a cero.",
                    "Formato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sbProds = new StringBuilder();
        for (int i = 0; i < carritoCombo.size(); i++) {
            sbProds.append(carritoCombo.get(i).nombre);
            if (i < carritoCombo.size() - 1) sbProds.append(" · ");
        }

        double ind = 0;
        for (ProductoItem p : carritoCombo) ind += p.precio;
        double ahorro = Math.max(0, ind - comboP);

        String vigencia = "Permanente";
        String estado = "Activo";
        if (!fIni.isEmpty() && !fIni.equals("Fecha inicio")) {
            vigencia = fIni + (fFin.isEmpty() || fFin.equals("Fecha fin (vacío = permanente)") ? "" : " - " + fFin);
            estado = "Temporal";
        }

        combosRegistrados.add(new ComboItem(
                String.format("#C%03d", combosRegistrados.size() + 1),
                nom, sbProds.toString(), "$" + String.format("%.2f", comboP),
                "$" + String.format("%.2f", ahorro), vigencia, estado));

        poblarTablaCombos();
        limpiarFormulario();

        JOptionPane.showMessageDialog(this,
                "Combo \"" + nom + "\" agregado correctamente.",
                "Combo creado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onEditarCombo(ComboItem item) {
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topWindow instanceof Frame ? (Frame) topWindow : null, "Editar Combo / Promoción", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.weightx = 1.0; g.gridx = 0;

        JLabel lblTitulo = new JLabel("Editar Combo");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(C_ACCENT);
        g.gridy = 0; p.add(lblTitulo, g);

        // Nombre
        g.gridy++; p.add(mkLabelForm("Nombre del combo:"), g);
        JTextField txtEditNom = new JTextField(item.nombre);
        styleCampoDialog(txtEditNom);
        g.gridy++; p.add(txtEditNom, g);

        // Productos
        g.gridy++; p.add(mkLabelForm("Productos incluidos:"), g);
        JTextField txtEditProds = new JTextField(item.productosStr);
        styleCampoDialog(txtEditProds);
        g.gridy++; p.add(txtEditProds, g);

        // Precio
        g.gridy++; p.add(mkLabelForm("Precio combo ($):"), g);
        JTextField txtEditPrecio = new JTextField(item.precio.replace("$", "").trim());
        styleCampoDialog(txtEditPrecio);
        g.gridy++; p.add(txtEditPrecio, g);

        // Vigencia
        g.gridy++; p.add(mkLabelForm("Vigencia:"), g);
        JTextField txtEditVig = new JTextField(item.vigencia);
        styleCampoDialog(txtEditVig);
        g.gridy++; p.add(txtEditVig, g);

        // Estado
        g.gridy++; p.add(mkLabelForm("Estado:"), g);
        JComboBox<String> cmbEditEst = new JComboBox<>(new String[]{"Activo", "Temporal", "Pausado"});
        cmbEditEst.setSelectedItem(item.estado);
        cmbEditEst.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditEst.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditEst, g);

        // Botones
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtns.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = new JButton("Guardar Cambios") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BTN_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(C_WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.setContentAreaFilled(false); btnGuardar.setBorderPainted(false); btnGuardar.setFocusPainted(false);
        btnGuardar.setPreferredSize(new Dimension(140, 36));
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnGuardar.addActionListener(e -> {
            String nom = txtEditNom.getText().trim();
            String prods = txtEditProds.getText().trim();
            String pre = txtEditPrecio.getText().trim();
            String vig = txtEditVig.getText().trim();

            // ── Validar nombre ──
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: el nombre del combo no puede estar vacío.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar productos ──
            if (prods.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: debes indicar los productos incluidos.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que el precio no esté vacío ──
            if (pre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: el precio del combo no puede estar vacío.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que el precio sea numérico ──
            String preLimpio = pre.replaceAll("[^0-9.]", "");
            double precioNum;
            try {
                if (preLimpio.isEmpty()) throw new NumberFormatException();
                precioNum = Double.parseDouble(preLimpio);
                if (precioNum <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "El precio debe ser un número válido mayor a cero (ej. 150.00).",
                        "Formato inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ── Validar vigencia ──
            if (vig.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: la vigencia no puede estar vacía.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            item.nombre = nom;
            item.productosStr = prods;
            item.precio = "$" + String.format("%.2f", precioNum);
            item.vigencia = vig;
            item.estado = (String) cmbEditEst.getSelectedItem();

            poblarTablaCombos();
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                    "Combo \"" + nom + "\" actualizado correctamente.",
                    "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
        });

        panelBtns.add(btnCancelar);
        panelBtns.add(btnGuardar);

        g.gridy++; g.insets = new Insets(16, 0, 0, 0);
        p.add(panelBtns, g);

        dialog.add(p);
        dialog.pack();
        dialog.setSize(420, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void onEliminarCombo(ComboItem item) {
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + item.nombre + "\"?","Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) { combosRegistrados.remove(item); poblarTablaCombos(); }
    }

    private void limpiarFormulario() {
        txtNombreCombo.setText("Nombre del combo"); txtNombreCombo.setForeground(new Color(0xAAAAAA));
        txtPrecioEspecial.setText("Precio especial ($)"); txtPrecioEspecial.setForeground(new Color(0xAAAAAA));
        txtFechaInicio.setText("Fecha inicio"); txtFechaInicio.setForeground(new Color(0xAAAAAA));
        txtFechaFin.setText("Fecha fin (vacío = permanente)"); txtFechaFin.setForeground(new Color(0xAAAAAA));
        carritoCombo.clear();
        poblarListaProductos();
        poblarCarritoUI();
    }

    // ═══════════════════════════════════════════════
    // UTILIDADES DE DISEÑO
    // ═══════════════════════════════════════════════
    private JPanel buildCampo(JTextField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        field.setForeground(new Color(0xAAAAAA));
        field.setText(placeholder);

        String ph = placeholder;
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(ph)) {
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(ph);
                    field.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_CAMPO_BOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            }
        };
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(0, 44));
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JLabel mkLabelForm(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(C_ACCENT);
        return l;
    }

    private void styleCampoDialog(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBackground(C_CAMPO_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_CAMPO_BOR, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private void inicializarDatosDummy() {
        productosDisponibles.add(new ProductoItem("#001", "Enchiladas verdes", 85.00));
        productosDisponibles.add(new ProductoItem("#002", "Pozole rojo", 95.00));
        productosDisponibles.add(new ProductoItem("#003", "Agua de Jamaica", 20.00));
        productosDisponibles.add(new ProductoItem("#004", "Caldo de res", 90.00));
        productosDisponibles.add(new ProductoItem("#005", "Tostadas de pata", 45.00));
        productosDisponibles.add(new ProductoItem("#006", "Sopa de lima", 55.00));
        productosDisponibles.add(new ProductoItem("#007", "Flautas de pollo", 70.00));
        productosDisponibles.add(new ProductoItem("#008", "Refresco 600ml", 25.00));

        combosRegistrados.add(new ComboItem("#C001", "Combo familiar", "Enchiladas · Pozole · ×2 Jamaica", "$210.00", "$45.00", "Permanente", "Activo"));
        combosRegistrados.add(new ComboItem("#C002", "Promo fin de semana", "Caldo · ×2 Tostadas · Bebida", "$150.00", "$30.00", "30 may – 15 jun", "Temporal"));
        combosRegistrados.add(new ComboItem("#C003", "Menú ejecutivo", "Sopa lima · Enchiladas · Agua", "$95.00", "$20.00", "Permanente", "Activo"));
        combosRegistrados.add(new ComboItem("#C004", "Combo pareja", "×2 Pozole · ×2 Bebida", "$140.00", "$25.00", "Permanente", "Pausado"));
    }
}