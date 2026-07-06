package vistas;

import modelos.Mesa;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════
 *  Vista: PanelCobro — Solo lectura para el cajero.
 *  Cambios:
 *   - Título "Cobro" + botón "← Mesas" en la misma fila
 *   - Pedido alineado a la izquierda
 *   - Total resaltado con fondo terracota más grande
 *   - Al confirmar cobro regresa a PanelMesas
 *  TODO (BD): cargar items desde tabla `detalle_pedido`
 * ═══════════════════════════════════════════════════════
 */
public class PanelCobro extends JPanel {

    // ─── COLORES ───────────────────────────────────
    private static final Color COLOR_BG          = new Color(0xFBF5EC);
    private static final Color COLOR_ACCENT      = new Color(0x5C1F08);
    private static final Color COLOR_DIVIDER     = new Color(0xC8A882);
    private static final Color COLOR_MESA_HEADER = new Color(0x9B3D18);
    private static final Color COLOR_NUM_BG      = new Color(0x7A2E10);
    private static final Color COLOR_TOTAL_BG    = new Color(0xBE5A33);
    private static final Color COLOR_BTN_COBRAR  = new Color(0x7A2000);
    private static final Color COLOR_BTN_REGRESAR= new Color(0xEDE0D0); // crema oscuro

    // ─── MODELO ────────────────────────────────────
    public static class ItemPedido {
        int cantidad; String nombre; double subtotal;
        public ItemPedido(int c, String n, double s) {
            cantidad = c; nombre = n; subtotal = s;
        }
    }

    // ─── ESTADO ────────────────────────────────────
    private Mesa             mesaActual;
    private List<ItemPedido> items = new ArrayList<>();
    private double           total = 0;

    // ─── COMPONENTES ───────────────────────────────
    private JLabel    lblMesaTitulo, lblMeseroInfo;
    private JPanel    panelItems;
    private JLabel    lblTotalPedido;
    private JLabel    lblTotalCobro, lblPagoRecibido, lblCambio;
    private JButton   btnEfectivo, btnTarjeta, btnMixto, btnActivo;
    private JButton   btnCobrar;
    private JCheckBox chkFactura, chkTicket;

    // Referencia a VentanaPrincipal para poder regresar
    private VentanaPrincipal ventana;

    // ─── CONSTRUCTOR ───────────────────────────────
    public PanelCobro() {
        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(COLOR_BG);
        add(buildColumnaPedido());
        add(buildColumnaCobro());
    }

    /**
     * Guarda referencia a la ventana principal para
     * poder llamar navegarA("MESAS") al cobrar o regresar.
     * Llamar desde VentanaPrincipal después de crear el panel.
     */
    public void setVentana(VentanaPrincipal v) {
        this.ventana = v;
    }

    // ═══════════════════════════════════════════════
    // COLUMNA IZQUIERDA — PEDIDO (alineado izquierda)
    // ═══════════════════════════════════════════════
    private JPanel buildColumnaPedido() {
        JPanel col = new JPanel(new BorderLayout(0, 0));
        col.setBackground(COLOR_BG);
        col.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 28));

        // ── Título + separador ──
        JLabel lblTitulo = new JLabel("Pedido");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        lblTitulo.setForeground(COLOR_ACCENT);
        lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);

        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_DIVIDER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel tituloBox = new JPanel(new BorderLayout());
        tituloBox.setOpaque(false);
        tituloBox.add(lblTitulo, BorderLayout.WEST);
        tituloBox.add(sep, BorderLayout.SOUTH);

        // ── Header mesa terracota ──
        lblMesaTitulo = new JLabel("Mesa —");
        lblMesaTitulo.setFont(new Font("Arial", Font.BOLD, 15));
        lblMesaTitulo.setForeground(Color.WHITE);

        lblMeseroInfo = new JLabel("Mesero: —");
        lblMeseroInfo.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMeseroInfo.setForeground(new Color(0xF5DEC8));

        JPanel mesaContent = new JPanel();
        mesaContent.setLayout(new BoxLayout(mesaContent, BoxLayout.Y_AXIS));
        mesaContent.setOpaque(false);
        mesaContent.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        mesaContent.add(lblMesaTitulo);
        mesaContent.add(Box.createRigidArea(new Dimension(0, 4)));
        mesaContent.add(lblMeseroInfo);

        JPanel mesaHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_MESA_HEADER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        mesaHeader.setOpaque(false);
        mesaHeader.add(mesaContent, BorderLayout.CENTER);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        norte.add(tituloBox);
        norte.add(Box.createRigidArea(new Dimension(0, 14)));
        norte.add(mesaHeader);
        norte.add(Box.createRigidArea(new Dimension(0, 8)));

        // ── Lista de items con scroll ──
        panelItems = new JPanel();
        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));
        panelItems.setOpaque(false);
        panelItems.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scroll = new JScrollPane(panelItems,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // ── Total resaltado ──
        lblTotalPedido = new JLabel("Total: $0");
        lblTotalPedido.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalPedido.setForeground(COLOR_ACCENT);
        lblTotalPedido.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        lblTotalPedido.setHorizontalAlignment(SwingConstants.LEFT);

        col.add(norte,         BorderLayout.NORTH);
        col.add(scroll,        BorderLayout.CENTER);
        col.add(lblTotalPedido,BorderLayout.SOUTH);
        return col;
    }

    // ═══════════════════════════════════════════════
    // COLUMNA DERECHA — COBRO
    // ═══════════════════════════════════════════════
    private JPanel buildColumnaCobro() {
        JPanel col = new JPanel(new BorderLayout());
        col.setBackground(COLOR_BG);
        col.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_DIVIDER),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        // ── Fila título "Cobro" + botón "← Mesas" ──
        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel lblTitulo = new JLabel("Cobro");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        lblTitulo.setForeground(COLOR_ACCENT);

        JButton btnRegresar = buildBotonRegresar();
        filaTitulo.add(lblTitulo,   BorderLayout.WEST);
        filaTitulo.add(btnRegresar, BorderLayout.EAST);

        inner.add(filaTitulo);
        inner.add(Box.createRigidArea(new Dimension(0, 18)));

        // ── Método de pago ──
        JLabel lblMetodo = buildLabelGris("Método de pago");
        lblMetodo.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(lblMetodo);
        inner.add(Box.createRigidArea(new Dimension(0, 8)));
        inner.add(buildMetodosPago());
        inner.add(Box.createRigidArea(new Dimension(0, 18)));

        // ── Labels Pago Recibido / Cambio ──
        JPanel labelsPC = new JPanel(new GridLayout(1, 2, 12, 0));
        labelsPC.setOpaque(false);
        labelsPC.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelsPC.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        labelsPC.add(buildLabelGris("Pago Recibido"));
        labelsPC.add(buildLabelGris("Cambio"));
        inner.add(labelsPC);
        inner.add(Box.createRigidArea(new Dimension(0, 6)));

        // ── Campos Pago Recibido / Cambio ──
        lblPagoRecibido = new JLabel("$0.00", SwingConstants.CENTER);
        lblPagoRecibido.setFont(new Font("Arial", Font.PLAIN, 20));
        lblPagoRecibido.setForeground(new Color(0x333333));

        lblCambio = new JLabel("$0.00", SwingConstants.CENTER);
        lblCambio.setFont(new Font("Arial", Font.PLAIN, 20));
        lblCambio.setForeground(new Color(0x333333));

        JPanel camposPC = new JPanel(new GridLayout(1, 2, 12, 0));
        camposPC.setOpaque(false);
        camposPC.setAlignmentX(Component.LEFT_ALIGNMENT);
        camposPC.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        camposPC.add(buildCampoRedondeado(lblPagoRecibido));
        camposPC.add(buildCampoRedondeado(lblCambio));
        inner.add(camposPC);
        inner.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── Checkboxes ──
        chkFactura = buildCheckbox("Solicitar factura");
        chkTicket  = buildCheckbox("Solicitar ticket");
        inner.add(chkFactura);
        inner.add(Box.createRigidArea(new Dimension(0, 8)));
        inner.add(chkTicket);
        inner.add(Box.createRigidArea(new Dimension(0, 18)));

        // ── Tarjeta total resaltada ──
        inner.add(buildTarjetaTotal());
        inner.add(Box.createRigidArea(new Dimension(0, 12)));

        // ── Botón cobrar ──
        btnCobrar = buildBotonCobrar();
        inner.add(btnCobrar);

        col.add(inner, BorderLayout.NORTH);
        return col;
    }

    // ─── BOTÓN REGRESAR ← MESAS ────────────────────
    private JButton buildBotonRegresar() {
        JButton btn = new JButton("← Mesas") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo crema con borde terracota suave
                g2.setColor(COLOR_BTN_REGRESAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
                // Texto
                g2.setColor(COLOR_ACCENT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Regresa a PanelMesas sin cobrar
        btn.addActionListener(e -> regresarAMesas());
        return btn;
    }

    // ─── MÉTODO DE PAGO ────────────────────────────
    private JPanel buildMetodosPago() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnEfectivo = buildBtnMetodo("Efectivo");
        btnTarjeta  = buildBtnMetodo("Tarjeta");
        btnMixto    = buildBtnMetodo("Mixto");

        btnEfectivo.addActionListener(e -> activarMetodo(btnEfectivo));
        btnTarjeta.addActionListener(e  -> activarMetodo(btnTarjeta));
        btnMixto.addActionListener(e    -> activarMetodo(btnMixto));

        p.add(btnEfectivo);
        p.add(btnTarjeta);
        p.add(btnMixto);
        activarMetodo(btnEfectivo);
        return p;
    }

    private JButton buildBtnMetodo(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean on = (this == btnActivo);
                g2.setColor(on ? new Color(0xFBF5EC) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(on ? COLOR_TOTAL_BG : COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(on ? 2.2f : 1.4f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 14, 14);
                g2.setColor(on ? COLOR_TOTAL_BG : new Color(0x444444));
                g2.setFont(on ? getFont().deriveFont(Font.BOLD,14f) : getFont().deriveFont(Font.PLAIN,14f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(115, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void activarMetodo(JButton btn) {
        btnActivo = btn;
        btnEfectivo.repaint(); btnTarjeta.repaint(); btnMixto.repaint();
    }

    // ─── CAMPO REDONDEADO ──────────────────────────
    private JPanel buildCampoRedondeado(JLabel lbl) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 14, 14);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ─── CHECKBOX ──────────────────────────────────
    private JCheckBox buildCheckbox(String texto) {
        JCheckBox chk = new JCheckBox(texto);
        chk.setFont(new Font("Arial", Font.PLAIN, 14));
        chk.setForeground(new Color(0x444444));
        chk.setOpaque(false);
        chk.setAlignmentX(Component.LEFT_ALIGNMENT);
        chk.setIcon(iconCheck(false));
        chk.setSelectedIcon(iconCheck(true));
        return chk;
    }

    private Icon iconCheck(boolean checked) {
        return new Icon() {
            public int getIconWidth()  { return 22; }
            public int getIconHeight() { return 22; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(checked ? new Color(0xFBF5EC) : Color.WHITE);
                g2.fillRoundRect(x, y, 20, 20, 6, 6);
                g2.setColor(checked ? COLOR_TOTAL_BG : COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(x, y, 20, 20, 6, 6);
                if (checked) {
                    g2.setColor(COLOR_TOTAL_BG);
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x+4, y+10, x+8,  y+14);
                    g2.drawLine(x+8, y+14, x+16, y+6);
                }
                g2.dispose();
            }
        };
    }

    // ─── TARJETA TOTAL (más grande y resaltada) ────
    private JPanel buildTarjetaTotal() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_TOTAL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Altura mayor para que resalte más
        card.setMinimumSize(new Dimension(0, 95));
        card.setPreferredSize(new Dimension(Integer.MAX_VALUE, 95));

        JLabel lblEt = new JLabel("Total a cobrar");
        lblEt.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEt.setForeground(new Color(0xF5DEC8));

        lblTotalCobro = new JLabel("$0.00");
        lblTotalCobro.setFont(new Font("Arial", Font.BOLD, 36)); // más grande
        lblTotalCobro.setForeground(Color.WHITE);

        JPanel t = new JPanel();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setOpaque(false);
        t.add(lblEt);
        t.add(Box.createRigidArea(new Dimension(0, 4)));
        t.add(lblTotalCobro);

        card.add(t, BorderLayout.CENTER);
        return card;
    }

    // ─── BOTÓN COBRAR ──────────────────────────────
    private JButton buildBotonCobrar() {
        JButton btn = new JButton("Cobrar $0.00") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN_COBRAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onCobrar());
        return btn;
    }

    // ─── HELPER ────────────────────────────────────
    private JLabel buildLabelGris(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(new Color(0x888888));
        return l;
    }

    // ═══════════════════════════════════════════════
    // CARGAR PEDIDO
    // ═══════════════════════════════════════════════
    public void cargarPedido(Mesa mesa, String mesero, int minutos, List<ItemPedido> itemsList) {
        this.mesaActual = mesa;
        this.items      = itemsList;

        lblMesaTitulo.setText("Mesa " + mesa.getNumero());
        lblMeseroInfo.setText("Mesero: " + mesero + "    " + minutos + " min activa");

        panelItems.removeAll();
        total = 0;
        for (ItemPedido item : items) {
            panelItems.add(buildRowItem(item));
            panelItems.add(buildSep());
            total += item.subtotal;
        }

        lblTotalPedido.setText(String.format("<html><b>Total:</b> $%.0f</html>", total));
        lblTotalCobro.setText(String.format("$%.2f", total));
        btnCobrar.setText(String.format("Cobrar $%.2f", total));

        double pagoEj = total + 40;
        lblPagoRecibido.setText(String.format("$%.2f", pagoEj));
        lblCambio.setText(String.format("$%.2f", pagoEj - total));

        panelItems.revalidate();
        panelItems.repaint();
    }

    // ─── FILA ITEM ─────────────────────────────────
    private JPanel buildRowItem(ItemPedido item) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel num = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_NUM_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = String.valueOf(item.cantidad);
                g2.drawString(t,
                        (getWidth()-fm.stringWidth(t))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        num.setOpaque(false);
        num.setPreferredSize(new Dimension(36, 36));
        num.setMinimumSize(new Dimension(36, 36));
        num.setMaximumSize(new Dimension(36, 36));

        JLabel lblN = new JLabel(item.nombre);
        lblN.setFont(new Font("Arial", Font.PLAIN, 15));
        lblN.setForeground(new Color(0x333333));

        JLabel lblS = new JLabel(String.format("$%.2f", item.subtotal), SwingConstants.RIGHT);
        lblS.setFont(new Font("Arial", Font.PLAIN, 15));
        lblS.setForeground(new Color(0x333333));

        row.add(num,  BorderLayout.WEST);
        row.add(lblN, BorderLayout.CENTER);
        row.add(lblS, BorderLayout.EAST);
        return row;
    }

    private JSeparator buildSep() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(0xDDCCBB));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    // ═══════════════════════════════════════════════
    // ACCIONES
    // ═══════════════════════════════════════════════

    /** Confirma el cobro y regresa a PanelMesas */
    private void onCobrar() {
        String metodo = btnActivo == btnEfectivo ? "Efectivo"
                : btnActivo == btnTarjeta  ? "Tarjeta" : "Mixto";
        int ok = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "¿Confirmar cobro de " + lblTotalCobro.getText() + " con " + metodo + "?",
                "Confirmar cobro", JOptionPane.YES_NO_OPTION);

        if (ok == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Cobro registrado correctamente.\n(Guardado en BD próximamente)",
                    "Cobro exitoso", JOptionPane.INFORMATION_MESSAGE);

            // TODO (BD): registrar pago en tabla `pagos`,
            //            actualizar mesa a LIBRE, cerrar pedido

            regresarAMesas(); // Regresar automáticamente
        }
    }

    /** Regresa a PanelMesas sin cobrar */
    private void regresarAMesas() {
        if (ventana != null) {
            ventana.navegarAMesas();
        }
    }

    // ═══════════════════════════════════════════════
    // DATOS DE PRUEBA
    // ═══════════════════════════════════════════════
    public static List<ItemPedido> itemsDummy() {
        List<ItemPedido> l = new ArrayList<>();
        l.add(new ItemPedido(2, "Enchiladas verdes", 120.00));
        l.add(new ItemPedido(1, "Agua de Jamaica",    40.00));
        l.add(new ItemPedido(1, "Agua de Horchata",   40.00));
        return l;
    }
}
