package vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════
 *  Vista: PanelHistorial — Historial de Transacciones
 * ═══════════════════════════════════════════════════════
 *  Lista de pedidos cerrados. Al hacer clic en el
 *  botón circular de cada fila se abre un JDialog
 *  modal (ventana emergente) con el detalle completo.
 *  Mientras el dialog está abierto, el fondo queda
 *  bloqueado e inactivo.
 *
 *  TODO (BD): todas las consultas marcadas con TODO
 * ═══════════════════════════════════════════════════════
 */
public class PanelHistorial extends JPanel {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // ─────────────────────────────────────────────
    private static final Color COLOR_BG          = new Color(0xFBF5EC);
    private static final Color COLOR_ACCENT      = new Color(0x6B2D1A);
    private static final Color COLOR_DIVIDER     = new Color(0xC8A882);
    private static final Color COLOR_ROW_HOVER   = new Color(0xF5EDE0);
    private static final Color COLOR_DETALLE_BG  = new Color(0xFFFDF9); // blanco crema
    private static final Color COLOR_DETALLE_BOR = new Color(0xBE5A33); // borde terracota

    // ─────────────────────────────────────────────
    // MODELO DE TRANSACCIÓN
    // TODO (BD): mapear desde tabla `pedidos` + `detalle_pedido`
    // ─────────────────────────────────────────────
    public static class Transaccion {
        String         id;          // #042
        String         hora;        // 19:48
        String         mesaDetalle; // Mesa 7 / Hasiel... 3 productos
        String         metodo;      // Efectivo | Tarjeta | Mixto
        String         monto;       // $360
        List<String[]> productos;   // {cantidad, nombre, subtotal}
        String         subtotal;
        String         propina;
        String         recibio;
        String         cambio;
        String         fecha;
        String         horaCobro;
        String         mesaActiva;  // "45 min"
        String         cfdi;        // "No solicitado" | "Solicitado"

        public Transaccion(String id, String hora, String mesaDetalle,
                           String metodo, String monto,
                           List<String[]> productos, String subtotal,
                           String propina, String recibio, String cambio,
                           String fecha, String horaCobro,
                           String mesaActiva, String cfdi) {
            this.id = id; this.hora = hora; this.mesaDetalle = mesaDetalle;
            this.metodo = metodo; this.monto = monto;
            this.productos = productos; this.subtotal = subtotal;
            this.propina = propina; this.recibio = recibio; this.cambio = cambio;
            this.fecha = fecha; this.horaCobro = horaCobro;
            this.mesaActiva = mesaActiva; this.cfdi = cfdi;
        }
    }

    // ─────────────────────────────────────────────
    // ESTADO
    // ─────────────────────────────────────────────
    private final List<Transaccion> transacciones = new ArrayList<>();
    private JPanel                  listaFilasRef;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────
    public PanelHistorial() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildContenido(), BorderLayout.CENTER);

        cargarDatosDummy();
    }

    // ─────────────────────────────────────────────
    // ENCABEZADO
    // ─────────────────────────────────────────────
    private JPanel buildHeader() {
        JLabel lblTitulo = new JLabel("Historial de Transacciones");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        lblTitulo.setForeground(COLOR_ACCENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_DIVIDER);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(lblTitulo, BorderLayout.WEST);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(topBar, BorderLayout.CENTER);
        header.add(sep,    BorderLayout.SOUTH);
        return header;
    }

    // ─────────────────────────────────────────────
    // CONTENIDO: encabezados + lista con scroll
    // ─────────────────────────────────────────────
    private JPanel buildContenido() {
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        contenido.add(buildEncabezados(), BorderLayout.NORTH);

        listaFilasRef = new JPanel();
        listaFilasRef.setLayout(new BoxLayout(listaFilasRef, BoxLayout.Y_AXIS));
        listaFilasRef.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listaFilasRef,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        contenido.add(scroll, BorderLayout.CENTER);
        return contenido;
    }

    // ─────────────────────────────────────────────
    // ENCABEZADOS DE COLUMNA
    // ─────────────────────────────────────────────
    private JPanel buildEncabezados() {
        JPanel enc = new JPanel(new GridBagLayout());
        enc.setOpaque(false);
        enc.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_ACCENT));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.insets = new Insets(0, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        String[] cols  = {"#", "Hora", "Mesa/Detalle", "Método", "Monto", ""};
        double[] pesos = {0.06, 0.10,   0.45,           0.15,     0.14,   0.10};

        for (int i = 0; i < cols.length; i++) {
            g.gridx = i; g.weightx = pesos[i];
            g.fill  = GridBagConstraints.HORIZONTAL;
            JLabel lbl = new JLabel(cols[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 13));
            lbl.setForeground(new Color(0x8B5A3A));
            enc.add(lbl, g);
        }
        return enc;
    }

    // ─────────────────────────────────────────────
    // FILA DE TRANSACCIÓN
    // ─────────────────────────────────────────────
    private JPanel buildFila(Transaccion t, int index) {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(true);
        Color bgNormal = index % 2 == 0 ? Color.WHITE : new Color(0xFAF4EE);
        fila.setBackground(bgNormal);
        fila.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE8D8C8)));
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        fila.setPreferredSize(new Dimension(0, 52));

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.insets = new Insets(0, 8, 0, 8);
        g.anchor = GridBagConstraints.WEST;

        String[] datos  = {t.id, t.hora, t.mesaDetalle, t.metodo, t.monto};
        double[] pesos  = {0.06, 0.10,    0.45,          0.15,     0.14};

        for (int i = 0; i < datos.length; i++) {
            g.gridx = i; g.weightx = pesos[i];
            g.fill  = GridBagConstraints.HORIZONTAL;
            JLabel lbl = new JLabel(datos[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 14));
            lbl.setForeground(new Color(0x3A3A3A));
            fila.add(lbl, g);
        }

        // Botón círculo → abre JDialog modal
        g.gridx = 5; g.weightx = 0.10;
        g.anchor = GridBagConstraints.CENTER;
        fila.add(buildBtnCirculo(t), g);

        // Hover
        fila.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { fila.setBackground(COLOR_ROW_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { fila.setBackground(bgNormal); }
        });

        return fila;
    }

    // ─────────────────────────────────────────────
    // BOTÓN CÍRCULO
    // ─────────────────────────────────────────────
    private JButton buildBtnCirculo(Transaccion t) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(2, 2, 24, 24);
                g2.setColor(COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(2, 2, 24, 24);
            }
        };
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Al hacer clic abre el JDialog modal
        btn.addActionListener(e -> abrirDialogDetalle(t));
        return btn;
    }

    // ═══════════════════════════════════════════════
    // JDIALOG MODAL — ventana emergente de detalle
    // El fondo queda bloqueado hasta que se cierre.
    // ═══════════════════════════════════════════════
    private void abrirDialogDetalle(Transaccion t) {
        // Obtener la ventana padre
        Window padre = SwingUtilities.getWindowAncestor(this);

        // JDialog modal: bloquea la ventana padre
        JDialog dialog = new JDialog((Frame) padre, "", true);
        dialog.setUndecorated(true); // sin barra de título del SO
        dialog.setBackground(new Color(0, 0, 0, 0));

        // Panel principal con borde redondeado y fondo crema
        JPanel contenido = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_DETALLE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(COLOR_DETALLE_BOR);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 18, 18);
            }
        };
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // 3 columnas: Productos | Pago | Información
        JPanel cols = new JPanel(new GridLayout(1, 3, 24, 0));
        cols.setOpaque(false);
        cols.add(buildColProductos(t, dialog));
        cols.add(buildColPago(t));
        cols.add(buildColInfo(t, dialog));

        contenido.add(cols, BorderLayout.CENTER);

        dialog.setContentPane(contenido);
        dialog.pack();
        dialog.setSize(820, 280);

        // Centrar sobre la ventana padre
        dialog.setLocationRelativeTo(padre);

        // Cerrar con ESC
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.setVisible(true); // bloquea hasta que se cierre
    }

    // ─────────────────────────────────────────────
    // COLUMNA PRODUCTOS
    // ─────────────────────────────────────────────
    private JPanel buildColProductos(Transaccion t, JDialog dialog) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel titulo = new JLabel("PRODUCTOS");
        titulo.setFont(new Font("Arial", Font.BOLD, 13));
        titulo.setForeground(COLOR_ACCENT);
        col.add(titulo);
        col.add(Box.createRigidArea(new Dimension(0, 12)));

        // TODO (BD): cargar desde tabla `detalle_pedido`
        for (String[] prod : t.productos) {
            JPanel fila = new JPanel(new BorderLayout(12, 0));
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

            JLabel lblNom = new JLabel(prod[0] + "× " + prod[1]);
            lblNom.setFont(new Font("Arial", Font.PLAIN, 13));
            lblNom.setForeground(new Color(0x3A3A3A));

            JLabel lblSub = new JLabel(prod[2], SwingConstants.RIGHT);
            lblSub.setFont(new Font("Arial", Font.PLAIN, 13));
            lblSub.setForeground(new Color(0x3A3A3A));

            fila.add(lblNom, BorderLayout.WEST);
            fila.add(lblSub, BorderLayout.EAST);
            col.add(fila);
            col.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        // Separador + Total
        col.add(Box.createRigidArea(new Dimension(0, 8)));
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        col.add(sep);
        col.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel filaTotal = new JPanel(new BorderLayout());
        filaTotal.setOpaque(false);
        filaTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lblTotalT = new JLabel("Total");
        lblTotalT.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalT.setForeground(COLOR_ACCENT);

        JLabel lblTotalV = new JLabel(t.subtotal, SwingConstants.RIGHT);
        lblTotalV.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalV.setForeground(COLOR_ACCENT);

        filaTotal.add(lblTotalT, BorderLayout.WEST);
        filaTotal.add(lblTotalV, BorderLayout.EAST);
        col.add(filaTotal);

        return col;
    }

    // ─────────────────────────────────────────────
    // COLUMNA PAGO
    // ─────────────────────────────────────────────
    private JPanel buildColPago(Transaccion t) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel titulo = new JLabel("PAGO");
        titulo.setFont(new Font("Arial", Font.BOLD, 13));
        titulo.setForeground(COLOR_ACCENT);
        col.add(titulo);
        col.add(Box.createRigidArea(new Dimension(0, 12)));

        // TODO (BD): campos desde tabla `pagos`
        col.add(buildFilaDetalle("Subtotal",      t.subtotal));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("Propina (15%)", t.propina));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("Método",        t.metodo));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("Recibió",       t.recibio));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("Cambio",        t.cambio));

        return col;
    }

    // ─────────────────────────────────────────────
    // COLUMNA INFORMACIÓN
    // ─────────────────────────────────────────────
    private JPanel buildColInfo(Transaccion t, JDialog dialog) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // Título + botón X (cierra el dialog)
        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel titulo = new JLabel("INFORMACIÓN");
        titulo.setFont(new Font("Arial", Font.BOLD, 13));
        titulo.setForeground(COLOR_ACCENT);

        JButton btnX = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_DETALLE_BG);
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, getWidth()-2, getHeight()-2);
                g2.setColor(new Color(0x777777));
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "✕";
                g2.drawString(txt,
                        (getWidth() - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnX.setPreferredSize(new Dimension(24, 24));
        btnX.setMaximumSize(new Dimension(24, 24));
        btnX.setContentAreaFilled(false);
        btnX.setBorderPainted(false);
        btnX.setFocusPainted(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose()); // cierra el dialog

        filaTitulo.add(titulo, BorderLayout.WEST);
        filaTitulo.add(btnX,   BorderLayout.EAST);
        col.add(filaTitulo);
        col.add(Box.createRigidArea(new Dimension(0, 12)));

        // TODO (BD): campos desde tabla `pedidos`
        col.add(buildFilaDetalle("Fecha",       t.fecha));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("Hora cobro",  t.horaCobro));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("Mesa activa", t.mesaActiva));
        col.add(Box.createRigidArea(new Dimension(0, 6)));
        col.add(buildFilaDetalle("CFDI",        t.cfdi));
        col.add(Box.createRigidArea(new Dimension(0, 16)));

        // Botones acción
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);
        botones.add(buildBtnAccion("Reimprimir ticket", dialog));
        botones.add(buildBtnAccion("Generar CFDI",      dialog));
        col.add(botones);

        return col;
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    private JPanel buildFilaDetalle(String etiqueta, String valor) {
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblEt = new JLabel(etiqueta);
        lblEt.setFont(new Font("Arial", Font.PLAIN, 13));
        lblEt.setForeground(new Color(0x666666));

        JLabel lblVal = new JLabel(valor, SwingConstants.RIGHT);
        lblVal.setFont(new Font("Arial", Font.PLAIN, 13));
        lblVal.setForeground(new Color(0x3A3A3A));

        fila.add(lblEt,  BorderLayout.WEST);
        fila.add(lblVal, BorderLayout.EAST);
        return fila;
    }

    private JButton buildBtnAccion(String texto, JDialog dialog) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(COLOR_DETALLE_BOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
                g2.setColor(COLOR_ACCENT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 34));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // TODO (BD): implementar reimprimir ticket y generar CFDI
        btn.addActionListener(e -> JOptionPane.showMessageDialog(
                dialog, texto + " — próximamente", "Acción",
                JOptionPane.INFORMATION_MESSAGE));
        return btn;
    }

    // ═══════════════════════════════════════════════
    // RECONSTRUIR LISTA
    // ═══════════════════════════════════════════════
    private void reconstruirFilas() {
        listaFilasRef.removeAll();
        for (int i = 0; i < transacciones.size(); i++) {
            listaFilasRef.add(buildFila(transacciones.get(i), i));
        }
        listaFilasRef.revalidate();
        listaFilasRef.repaint();
    }

    // ═══════════════════════════════════════════════
    // DATOS DE PRUEBA
    // TODO: eliminar y usar cargarDesdeBD()
    // ═══════════════════════════════════════════════
    private void cargarDatosDummy() {
        List<String[]> prods = new ArrayList<>();
        prods.add(new String[]{"2", "Enchiladas verdes", "$170.00"});
        prods.add(new String[]{"1", "Pozole rojo",       "$95.00"});
        prods.add(new String[]{"2", "Agua de Jamaica",   "$40.00"});

        for (int i = 0; i < 8; i++) {
            transacciones.add(new Transaccion(
                    "#042", "19:48", "Mesa 7/ Hasiel... 3 productos",
                    "Efectivo", "$360",
                    prods, "$350.75", "$45.75", "$400.00", "$49.25",
                    "5 jun 2026", "19:48", "45 min", "No solicitado"
            ));
        }
        reconstruirFilas();
    }

    /**
     * Recarga el historial desde la base de datos.
     *
     * TODO (BD): ejecutar:
     *   SELECT p.id_pedido, p.hora_cierre, m.numero,
     *          u.nombre AS mesero, p.total, p.metodo_pago
     *   FROM pedidos p
     *   JOIN mesas m    ON p.id_mesa   = m.id_mesa
     *   JOIN usuarios u ON p.id_mesero = u.id_usuario
     *   WHERE p.estado = 'CERRADO'
     *     AND DATE(p.hora_cierre) = CURDATE()
     *   ORDER BY p.hora_cierre DESC;
     *
     * Por cada fila del ResultSet crear un Transaccion
     * y agregarlo a `transacciones`, luego reconstruirFilas().
     */
    public void cargarDesdeBD() {
        transacciones.clear();
        // TODO: JDBC aquí
        reconstruirFilas();
    }
}
