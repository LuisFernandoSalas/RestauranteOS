package vistas;

import servicios.ApiClient;
import modelos.Producto;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista: PanelCombosPromos — Combos y Promos
 * Conectada a Backend Laravel mediante ApiClient y utilizando el Modelo Oficial Producto.
 */
public class PanelCombosPromos extends JPanel implements Actualizables {

    @Override
    public void recargarDatos() {
        // Al presionar el botón en el menú lateral, esto volverá a hacer la petición HTTP
        cargarProductosDesdeApi();
        cargarCombosDesdeApi();
    }

    // ─── INSTANCIA DEL SERVICIO API ────────────────
    private final ApiClient apiClient = new ApiClient();

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

    // ─── ESTRUCTURAS AUXILIARES ────────────────────
    static class CarritoItem {
        Producto producto;
        int cantidad;

        CarritoItem(Producto p, int c) {
            this.producto = p;
            this.cantidad = c;
        }
    }

    static class ComboItem {
        int id;
        String nombre, productosStr, estado, fechaInicio, fechaFin;
        double precioEspecial, ahorroCalculado;
        List<CarritoItem> items;

        ComboItem(int id, String n, String prodStr, double pEspecial, double ahorro, String fIni, String fFin, String est) {
            this.id = id;
            this.nombre = n;
            this.productosStr = prodStr;
            this.precioEspecial = pEspecial;
            this.ahorroCalculado = ahorro;
            this.fechaInicio = fIni;
            this.fechaFin = fFin;
            this.estado = est;
            this.items = new ArrayList<>();
        }
    }

    // ─── LISTAS DE DATOS ───────────────────────────
    private final List<Producto> productosDisponibles = new ArrayList<>();
    private final List<CarritoItem> carritoCombo = new ArrayList<>();
    private final List<ComboItem> combosRegistrados = new ArrayList<>();

    // ─── COMPONENTES FORMULARIO ────────────────────
    private JTextField txtNombreCombo, txtPrecioEspecial, txtFechaInicio, txtFechaFin;
    private JPanel panelListaProds, panelCarritoItems, panelTabla;
    private JLabel lblPrecioInd, lblPrecioCombo, lblAhorroCliente;

    // ─── CONSTRUCTOR ───────────────────────────────
    public PanelCombosPromos() {
        setLayout(new BorderLayout());
        setBackground(C_BG);

        JPanel contenido = buildContenido();
        JScrollPane scrollGeneral = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGeneral.setBorder(BorderFactory.createEmptyBorder());
        scrollGeneral.getViewport().setBackground(C_BG);
        scrollGeneral.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollGeneral, BorderLayout.CENTER);

        // Cargar datos reales mediante ApiClient
        cargarProductosDesdeApi();
        cargarCombosDesdeApi();
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

        // 6. Tabla unificada
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
        panel.add(buildCampo(txtFechaInicio, "Fecha inicio (YYYY-MM-DD)"));
        panel.add(buildCampo(txtFechaFin, "Fecha fin (YYYY-MM-DD)"));

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

        JButton btnCrear = buildBoton("Crear combo");
        btnCrear.setPreferredSize(new Dimension(0, 42));
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
        for (Producto prod : productosDisponibles) {
            boolean enCarrito = carritoCombo.stream().anyMatch(ci -> ci.producto.getId().equals(prod.getId()));

            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(enCarrito ? new Color(0xF3E5D8) : C_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(enCarrito ? C_CAMPO_BOR : new Color(0xE5E5E5), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JLabel lblInfo = new JLabel("<html><b>" + prod.getNombre() + "</b><br><font color='#888888'>$" + String.format("%.2f", prod.getPrecio()) + "</font></html>");

            JButton btnAccion = new JButton(enCarrito ? "Agregado" : "+ Agregar") {
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
            btnAccion.setOpaque(false);
            btnAccion.setContentAreaFilled(false);
            btnAccion.setBorderPainted(false);
            btnAccion.setFocusPainted(false);
            btnAccion.setBorder(BorderFactory.createEmptyBorder());
            btnAccion.setPreferredSize(new Dimension(85, 28));
            btnAccion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnAccion.addActionListener(e -> {
                if (!enCarrito) {
                    carritoCombo.add(new CarritoItem(prod, 1));
                    poblarListaProductos();
                    poblarCarritoUI();
                }
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
        for (CarritoItem item : carritoCombo) {
            JPanel card = new JPanel(new BorderLayout(8, 0));
            card.setBackground(C_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE5E5E5), 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            JLabel lblInfo = new JLabel("<html><b>" + item.producto.getNombre() + "</b><br><font color='#888888'>$" + String.format("%.2f", item.producto.getPrecio()) + "</font></html>");

            JPanel panelCant = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            panelCant.setOpaque(false);

            JButton btnMinus = new JButton("-");
            btnMinus.setFont(new Font("Arial", Font.BOLD, 12));
            btnMinus.setPreferredSize(new Dimension(24, 24));
            btnMinus.setMargin(new Insets(0, 0, 0, 0));
            btnMinus.setFocusable(false);

            btnMinus.addActionListener(e -> {
                if (item.cantidad > 1) {
                    item.cantidad--;
                } else {
                    carritoCombo.remove(item);
                }
                poblarListaProductos();
                poblarCarritoUI();
            });

            JLabel lblCant = new JLabel(String.valueOf(item.cantidad));
            lblCant.setFont(new Font("Arial", Font.BOLD, 13));

            JButton btnPlus = new JButton("+");
            btnPlus.setFont(new Font("Arial", Font.BOLD, 12));
            btnPlus.setPreferredSize(new Dimension(24, 24));
            btnPlus.setMargin(new Insets(0, 0, 0, 0));
            btnPlus.addActionListener(e -> {
                item.cantidad++;
                actualizarResumenCarrito();
                poblarCarritoUI();
            });

            panelCant.add(btnMinus);
            panelCant.add(lblCant);
            panelCant.add(btnPlus);

            JButton btnEliminar = new JButton("X") {
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
            btnEliminar.setOpaque(false);
            btnEliminar.setContentAreaFilled(false);
            btnEliminar.setBorderPainted(false);
            btnEliminar.setFocusPainted(false);
            btnEliminar.setBorder(BorderFactory.createEmptyBorder());
            btnEliminar.setPreferredSize(new Dimension(28, 26));
            btnEliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnEliminar.addActionListener(e -> {
                carritoCombo.remove(item);
                poblarListaProductos();
                poblarCarritoUI();
            });

            JPanel accGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            accGroup.setOpaque(false);
            accGroup.add(panelCant);
            accGroup.add(btnEliminar);

            card.add(lblInfo, BorderLayout.CENTER);
            card.add(accGroup, BorderLayout.EAST);

            panelCarritoItems.add(card);
            panelCarritoItems.add(Box.createVerticalStrut(6));
        }
        actualizarResumenCarrito();
        panelCarritoItems.revalidate(); panelCarritoItems.repaint();
    }

    private void actualizarResumenCarrito() {
        double ind = 0.0;
        for (CarritoItem item : carritoCombo) {
            ind += (item.producto.getPrecio() * item.cantidad);
        }

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

        // Encabezados
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

        // Filas
        for (int row = 0; row < combosRegistrados.size(); row++) {
            ComboItem item = combosRegistrados.get(row);
            Color bgRow = (row % 2 == 0) ? C_WHITE : C_ALT_ROW;
            int gridY = row + 1;

            Border cellBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEDDCC)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            );

            String vigencia = "Permanente";
            if (item.fechaInicio != null && !item.fechaInicio.isEmpty()) {
                vigencia = item.fechaInicio + (item.fechaFin != null ? " - " + item.fechaFin : "");
            }

            g.gridx = 0; g.gridy = gridY; g.weightx = PW[0];
            panelTabla.add(mkLblCell(item.nombre, new Color(0x333333), true, COL_ALIGN[0], bgRow, cellBorder), g);

            g.gridx = 1; g.gridy = gridY; g.weightx = PW[1];
            panelTabla.add(mkLblCell(item.productosStr, new Color(0x666666), false, COL_ALIGN[1], bgRow, cellBorder), g);

            g.gridx = 2; g.gridy = gridY; g.weightx = PW[2];
            panelTabla.add(mkLblCell("$" + String.format("%.2f", item.precioEspecial), new Color(0x333333), true, COL_ALIGN[2], bgRow, cellBorder), g);

            g.gridx = 3; g.gridy = gridY; g.weightx = PW[3];
            panelTabla.add(mkLblCell("$" + String.format("%.2f", item.ahorroCalculado), C_ACCENT, false, COL_ALIGN[3], bgRow, cellBorder), g);

            g.gridx = 4; g.gridy = gridY; g.weightx = PW[4];
            panelTabla.add(mkLblCell(vigencia, new Color(0x333333), false, COL_ALIGN[4], bgRow, cellBorder), g);

            g.gridx = 5; g.gridy = gridY; g.weightx = PW[5];
            panelTabla.add(buildTextoEstadoCell(item.estado, bgRow, cellBorder), g);

            g.gridx = 6; g.gridy = gridY; g.weightx = PW[6];
            panelTabla.add(buildAccionesCell(item, bgRow, cellBorder), g);
        }

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
        boolean activo = estado != null && (estado.equalsIgnoreCase("activo") || estado.equalsIgnoreCase("temporal"));
        JLabel l = new JLabel(estado != null ? estado.toUpperCase() : "DESCONOCIDO", SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 12));
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
    // CONEXIÓN CON BACKEND LARAVEL
    // ═══════════════════════════════════════════════

    private void cargarProductosDesdeApi() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return apiClient.obtenerProductos();
            }

            @Override protected void done() {
                try {
                    String res = get();
                    // IMPRIMIR EN CONSOLA PARA VER LA RESPUESTA REAL DE LARAVEL
                    System.out.println(">>> RESPUESTA PRODUCTOS DE LARAVEL: " + res);

                    if (res != null && !res.trim().isEmpty()) {
                        res = res.trim();
                        JSONArray jsonArray = null;

                        if (res.startsWith("[")) {
                            // Viene directamente como lista JSON [...]
                            jsonArray = new JSONArray(res);
                        } else if (res.startsWith("{")) {
                            // Viene envuelto en un objeto JSON {...}
                            JSONObject jsonObj = new JSONObject(res);
                            if (jsonObj.has("data")) {
                                jsonArray = jsonObj.getJSONArray("data");
                            } else if (jsonObj.has("message")) {
                                System.err.println("⚠️ Mensaje del Servidor: " + jsonObj.getString("message"));
                                return;
                            }
                        } else {
                            System.err.println("⚠️ La respuesta del backend no es un JSON válido.");
                            return;
                        }

                        if (jsonArray != null) {
                            productosDisponibles.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);
                                Producto p = new Producto(
                                        obj.getInt("id"),
                                        obj.optString("name", obj.optString("nombre")),
                                        obj.optString("category", obj.optString("categoria", "")),
                                        obj.optDouble("price", obj.optDouble("precio", 0.0)),
                                        obj.optString("status", obj.optString("estado", "Activo"))
                                );
                                productosDisponibles.add(p);
                            }
                            poblarListaProductos();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar productos: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void cargarCombosDesdeApi() {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return apiClient.obtenerCombos();
            }

            @Override protected void done() {
                try {
                    String res = get();
                    System.out.println(">>> RESPUESTA COMBOS DE LARAVEL: " + res);

                    if (res != null && !res.trim().isEmpty()) {
                        res = res.trim();
                        JSONArray jsonArray = null;

                        if (res.startsWith("[")) {
                            jsonArray = new JSONArray(res);
                        } else if (res.startsWith("{")) {
                            JSONObject jsonObj = new JSONObject(res);
                            if (jsonObj.has("data")) {
                                jsonArray = jsonObj.getJSONArray("data");
                            } else if (jsonObj.has("message")) {
                                System.err.println("⚠️ Mensaje del Servidor Combos: " + jsonObj.getString("message"));
                                return;
                            }
                        }

                        if (jsonArray != null) {
                            combosRegistrados.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                StringBuilder prodsSb = new StringBuilder();
                                double totalIndiv = 0.0;

                                JSONArray prodsArray = obj.optJSONArray("productos");
                                if (prodsArray == null) {
                                    prodsArray = obj.optJSONArray("products");
                                }

                                if (prodsArray != null) {
                                    for (int j = 0; j < prodsArray.length(); j++) {
                                        JSONObject pObj = prodsArray.getJSONObject(j);
                                        String nombreP = pObj.optString("name", pObj.optString("nombre"));
                                        double precioP = pObj.optDouble("price", pObj.optDouble("precio"));

                                        JSONObject pivot = pObj.optJSONObject("pivot");
                                        int cantidad = pivot != null ? pivot.optInt("cantidad", 1) : 1;

                                        totalIndiv += (precioP * cantidad);
                                        prodsSb.append(cantidad > 1 ? "x" + cantidad + " " : "").append(nombreP);
                                        if (j < prodsArray.length() - 1) prodsSb.append(" · ");
                                    }
                                }

                                double precioEspecial = obj.optDouble("precio_especial", obj.optDouble("precio", 0.0));
                                double ahorro = Math.max(0, totalIndiv - precioEspecial);

                                ComboItem combo = new ComboItem(
                                        obj.getInt("id"),
                                        obj.optString("nombre", obj.optString("name")),
                                        prodsSb.toString(),
                                        precioEspecial,
                                        ahorro,
                                        obj.optString("fecha_inicio", null),
                                        obj.optString("fecha_fin", null),
                                        obj.optString("estado", "activo")
                                );
                                combosRegistrados.add(combo);
                            }
                            poblarTablaCombos();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar combos: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void onCrearCombo() {
        String nom = txtNombreCombo.getText().trim();
        String pre = txtPrecioEspecial.getText().trim();
        String fIni = txtFechaInicio.getText().trim();
        String fFin = txtFechaFin.getText().trim();

        if (nom.isEmpty() || nom.equals("Nombre del combo")) {
            JOptionPane.showMessageDialog(this, "Ingresa el nombre del combo.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pre.isEmpty() || pre.equals("Precio especial ($)")) {
            JOptionPane.showMessageDialog(this, "Ingresa el precio especial del combo.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (carritoCombo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos un producto.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double comboP;
        try {
            comboP = Double.parseDouble(pre.replaceAll("[^0-9.]", ""));
            if (comboP <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio especial debe ser mayor a cero.", "Formato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("nombre", nom);
        jsonPayload.put("precio_especial", comboP);
        jsonPayload.put("estado", "activo");

        if (!fIni.isEmpty() && !fIni.contains("YYYY")) jsonPayload.put("fecha_inicio", fIni);
        if (!fFin.isEmpty() && !fFin.contains("YYYY")) jsonPayload.put("fecha_fin", fFin);

        JSONArray prodsArr = new JSONArray();
        for (CarritoItem item : carritoCombo) {
            JSONObject pObj = new JSONObject();
            pObj.put("producto_id", item.producto.getId());
            pObj.put("cantidad", item.cantidad);
            prodsArr.put(pObj);
        }
        jsonPayload.put("productos", prodsArr);

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return apiClient.crearCombo(jsonPayload.toString());
            }

            @Override protected void done() {
                try {
                    String res = get();
                    if (res != null && !res.contains("\"error\"")) {
                        JOptionPane.showMessageDialog(PanelCombosPromos.this, "Combo creado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        limpiarFormulario();
                        cargarCombosDesdeApi();
                    } else {
                        JOptionPane.showMessageDialog(PanelCombosPromos.this, "Error del servidor: " + res, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelCombosPromos.this, "Error de red al conectar con el API", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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

        JLabel lblTitulo = new JLabel("Editar Combo #" + item.id);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(C_ACCENT);
        g.gridy = 0; p.add(lblTitulo, g);

        g.gridy++; p.add(mkLabelForm("Nombre del combo:"), g);
        JTextField txtEditNom = new JTextField(item.nombre);
        styleCampoDialog(txtEditNom);
        g.gridy++; p.add(txtEditNom, g);

        g.gridy++; p.add(mkLabelForm("Precio combo ($):"), g);
        JTextField txtEditPrecio = new JTextField(String.valueOf(item.precioEspecial));
        styleCampoDialog(txtEditPrecio);
        g.gridy++; p.add(txtEditPrecio, g);

        g.gridy++; p.add(mkLabelForm("Estado:"), g);
        JComboBox<String> cmbEditEst = new JComboBox<>(new String[]{"activo", "pausado"});
        cmbEditEst.setSelectedItem(item.estado);
        cmbEditEst.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditEst.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditEst, g);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtns.setOpaque(false);

        JButton btnCancelar = buildBoton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = buildBoton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(160, 38));
        btnGuardar.addActionListener(e -> {
            String nom = txtEditNom.getText().trim();
            String pre = txtEditPrecio.getText().trim();

            if (nom.isEmpty() || pre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Los campos no pueden estar vacíos.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double preNum = Double.parseDouble(pre);

            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("nombre", nom);
            jsonPayload.put("precio_especial", preNum);
            jsonPayload.put("estado", cmbEditEst.getSelectedItem().toString());

            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return apiClient.actualizarCombo(item.id, jsonPayload.toString());
                }

                @Override protected void done() {
                    try {
                        String res = get();
                        if (res != null && !res.contains("\"error\"")) {
                            dialog.dispose();
                            cargarCombosDesdeApi();
                            JOptionPane.showMessageDialog(PanelCombosPromos.this, "Combo actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Error: " + res, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Error de red", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
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
        if (ok == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    return apiClient.eliminarCombo(item.id);
                }

                @Override protected void done() {
                    try {
                        if (get()) {
                            cargarCombosDesdeApi();
                            JOptionPane.showMessageDialog(PanelCombosPromos.this, "Combo eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelCombosPromos.this, "Error al eliminar combo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void limpiarFormulario() {
        txtNombreCombo.setText("Nombre del combo"); txtNombreCombo.setForeground(new Color(0xAAAAAA));
        txtPrecioEspecial.setText("Precio especial ($)"); txtPrecioEspecial.setForeground(new Color(0xAAAAAA));
        txtFechaInicio.setText("Fecha inicio (YYYY-MM-DD)"); txtFechaInicio.setForeground(new Color(0xAAAAAA));
        txtFechaFin.setText("Fecha fin (YYYY-MM-DD)"); txtFechaFin.setForeground(new Color(0xAAAAAA));
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

    private JButton buildBoton(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BTN_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
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

}