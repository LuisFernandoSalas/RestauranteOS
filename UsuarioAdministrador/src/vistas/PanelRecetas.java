package vistas;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista: PanelRecetas
 *
 * - Ventanas modales ajustadas al tamaño exacto (sin marco negro).
 * - Selección de Insumos con buscador dinámico y lectura de Stock de Inventario.
 * - Estructura preparada para integración con Base de Datos.
 * - Tabla con diseño consistente al Panel de Inventario.
 */
public class PanelRecetas extends JPanel {

    // ─── COLORES DE MARCA Y TABLA ───────────────────
    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_DIV       = new Color(0xC8A882);
    private static final Color C_HDR_BG    = new Color(0x5C1A0A);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_ALT       = new Color(0xFAF4EE);
    private static final Color C_ID_BG     = new Color(0xFDF3E7);
    private static final Color C_ID_BOR    = new Color(0xD48000);
    private static final Color C_EXP_BG    = new Color(0xFFF8F0);
    private static final Color C_EXP_BOR   = new Color(0xD48000);
    private static final Color C_BTN_DARK  = new Color(0x3A1808);
    private static final Color C_BTN_RBOR  = new Color(0xC03020);
    private static final Color C_CAMPO_BG  = new Color(0xEEE8DE);
    private static final Color C_CAMPO_BOR = new Color(0xC8A882);
    private static final Color C_COSTO     = new Color(0xA05020);

    // ─── MODELOS DE DATOS (DB READY) ───────────────
    public static class Insumo {
        private String id;
        private String nombre;
        private String unidad;
        private double precioUnitario;
        private double stockActual;

        public Insumo(String id, String nombre, String unidad, double precioUnitario, double stockActual) {
            this.id = id;
            this.nombre = nombre;
            this.unidad = unidad;
            this.precioUnitario = precioUnitario;
            this.stockActual = stockActual;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getUnidad() { return unidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getStockActual() { return stockActual; }

        @Override
        public String toString() {
            return nombre + " (" + unidad + ") — Stock: " + stockActual + " " + unidad;
        }
    }

    public static class Ingrediente {
        private Insumo insumo;
        private double cantidad;

        public Ingrediente(Insumo insumo, double cantidad) {
            this.insumo = insumo;
            this.cantidad = cantidad;
        }

        public Insumo getInsumo() { return insumo; }
        public double getCantidad() { return cantidad; }
        public String getUnidad() { return insumo.getUnidad(); }
        public double getPrecioUnitario() { return insumo.getPrecioUnitario(); }
        public double subtotal() { return cantidad * insumo.getPrecioUnitario(); }

        public String textoChip() {
            String cant = cantidad == Math.floor(cantidad)
                    ? String.valueOf((int) cantidad)
                    : String.valueOf(cantidad);
            return insumo.getNombre() + " " + cant + " " + insumo.getUnidad();
        }
    }

    public static class Receta {
        private String id;
        private String nombre;
        private String productoVinculado;
        private List<Ingrediente> ingredientes;
        private int tiempoMin;

        public Receta(String id, String nombre, String productoVinculado, int tiempoMin, List<Ingrediente> ings) {
            this.id = id;
            this.nombre = nombre;
            this.productoVinculado = productoVinculado;
            this.tiempoMin = tiempoMin;
            this.ingredientes = new ArrayList<>(ings);
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getProductoVinculado() { return productoVinculado; }
        public List<Ingrediente> getIngredientes() { return ingredientes; }
        public int getTiempoMin() { return tiempoMin; }
        public String numIng() { return ingredientes.size() + " ing."; }
        public double costo() { return ingredientes.stream().mapToDouble(Ingrediente::subtotal).sum(); }
        public String costoStr() { return String.format("$%.2f", costo()); }
        public String tiempoStr() { return tiempoMin + " min"; }
    }

    // ─── COLECCIONES EN MEMORIA (SIMULADOR DE BD) ──
    private final List<Insumo> listaInsumosInventario = new ArrayList<>();
    private final List<Receta> recetas = new ArrayList<>();

    // CONFIGURACIÓN DE TABLA UNIFICADA (Consistencia con Inventario)
    private static final double[] PW = {0.07, 0.16, 0.22, 0.11, 0.10, 0.09, 0.10, 0.15};
    private static final String[] COL_N = {"ID", "Receta", "Producto vinculado", "Ingredientes", "Costo", "Tiempo", "Estado", "Acciones"};

    // ESTADO DE INTERFAZ
    private int filaExp = -1;
    private JPanel panelTabla;
    private JComboBox<String> cmbFiltro;

    public PanelRecetas() {
        setLayout(new BorderLayout());
        setBackground(C_BG);

        inicializarDatosDummy();

        JPanel contenido = buildContenido();
        JScrollPane scroll = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════
    // ESTRUCTURA PRINCIPAL
    // ═══════════════════════════════════════════════
    private JPanel buildContenido() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0; gc.gridx = 0; gc.weighty = 0;

        // Título de Sección
        JLabel tit = new JLabel("Recetas");
        tit.setFont(new Font("Arial", Font.BOLD, 30)); tit.setForeground(C_ACCENT);
        JSeparator sep = new JSeparator(); sep.setForeground(new Color(0x8B2500));
        JPanel hdr = new JPanel(new BorderLayout(0, 8)); hdr.setOpaque(false);
        hdr.add(tit, BorderLayout.NORTH); hdr.add(sep, BorderLayout.CENTER);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 16, 0); p.add(hdr, gc);

        // Barra Filtro + Botón Acción
        gc.gridy = 1; gc.insets = new Insets(0, 0, 12, 0); p.add(buildBarra(), gc);

        // Tabla Unificada
        panelTabla = new JPanel();
        panelTabla.setOpaque(false);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 0, 0); p.add(panelTabla, gc);

        // Relleno inferior
        gc.gridy = 3; gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        JPanel sp = new JPanel(); sp.setOpaque(false);
        p.add(sp, gc);

        poblarTabla();
        return p;
    }

    private JPanel buildBarra() {
        JPanel bar = new JPanel(new BorderLayout(0, 0)); bar.setOpaque(false);
        cmbFiltro = new JComboBox<>(new String[]{"Todos los productos",
                "Enchiladas verdes", "Pozole rojo", "Tostadas de pata", "Caldo de res"});
        cmbFiltro.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbFiltro.addActionListener(e -> filtrar());
        JPanel cw = mkCampoWrap(cmbFiltro, 200, 38);

        JButton btn = mkBtnDark("+ Nueva receta");
        btn.setPreferredSize(new Dimension(160, 38));
        btn.addActionListener(e -> dlgReceta(null));

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); der.setOpaque(false);
        der.add(btn);
        bar.add(cw, BorderLayout.WEST); bar.add(der, BorderLayout.EAST);
        return bar;
    }

    // ═══════════════════════════════════════════════
    // RENDERIZADO UNIFICADO DE LA TABLA (IGUAL A INVENTARIO)
    // ═══════════════════════════════════════════════
    private void poblarTabla() { poblarCon(recetas); }

    private void poblarCon(List<Receta> lista) {
        panelTabla.removeAll();
        panelTabla.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0;

        // 1. ENCABEZADO DE TABLA (FILA 0)
        g.gridy = 0;
        for (int i = 0; i < COL_N.length; i++) {
            g.gridx = i;
            g.weightx = PW[i];

            JPanel cellHeader = new JPanel(new BorderLayout());
            cellHeader.setBackground(C_HDR_BG);
            cellHeader.setPreferredSize(new Dimension(0, 42));
            cellHeader.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

            JLabel lbl = new JLabel(COL_N[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            lbl.setForeground(C_WHITE);
            cellHeader.add(lbl, BorderLayout.WEST);

            panelTabla.add(cellHeader, g);
        }

        // 2. FILAS DE DATOS
        int currentGridY = 1;
        for (int i = 0; i < lista.size(); i++) {
            Receta r = lista.get(i);
            int ri = recetas.indexOf(r);
            Color rowBg = (i % 2 == 0) ? C_WHITE : C_ALT;

            g.gridy = currentGridY;

            // Col 0: ID Badge
            g.gridx = 0; g.weightx = PW[0];
            panelTabla.add(buildCellWrapper(mkBadgeId(r.getId()), rowBg, ri), g);

            // Col 1: Receta
            g.gridx = 1; g.weightx = PW[1];
            panelTabla.add(buildCellWrapper(mkLblDatBold(r.getNombre()), rowBg, ri), g);

            // Col 2: Producto vinculado
            g.gridx = 2; g.weightx = PW[2];
            panelTabla.add(buildCellWrapper(mkLblDatMuted(r.getProductoVinculado()), rowBg, ri), g);

            // Col 3: Ingredientes
            g.gridx = 3; g.weightx = PW[3];
            JLabel lI = new JLabel(r.numIng());
            lI.setFont(new Font("Arial", Font.PLAIN, 13)); lI.setForeground(C_COSTO);
            lI.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panelTabla.add(buildCellWrapper(lI, rowBg, ri), g);

            // Col 4: Costo
            g.gridx = 4; g.weightx = PW[4];
            panelTabla.add(buildCellWrapper(mkLblDatBold(r.costoStr()), rowBg, ri), g);

            // Col 5: Tiempo
            g.gridx = 5; g.weightx = PW[5];
            panelTabla.add(buildCellWrapper(mkLblDat(r.tiempoStr()), rowBg, ri), g);

            // Col 6: Estado Badge
            g.gridx = 6; g.weightx = PW[6];
            panelTabla.add(buildCellWrapper(mkBadgeVin(), rowBg, ri), g);

            // Col 7: Acciones
            g.gridx = 7; g.weightx = PW[7];
            panelTabla.add(buildCellWrapper(buildAcciones(r), rowBg, -1), g);

            currentGridY++;

            // FILA EXPANSIÓN
            if (filaExp >= 0 && ri == filaExp) {
                g.gridy = currentGridY;
                g.gridx = 0;
                g.gridwidth = 8;
                g.weightx = 1.0;
                panelTabla.add(buildExpansion(r), g);
                g.gridwidth = 1;
                currentGridY++;
            }

            // SEPARADOR
            g.gridy = currentGridY;
            g.gridx = 0;
            g.gridwidth = 8;
            g.weightx = 1.0;

            JSeparator s = new JSeparator();
            s.setForeground(new Color(0xEEDDCC));
            s.setBackground(new Color(0xEEDDCC));
            panelTabla.add(s, g);

            g.gridwidth = 1;
            currentGridY++;
        }

        panelTabla.revalidate();
        panelTabla.repaint();
    }

    private JPanel buildCellWrapper(JComponent comp, Color bg, int ri) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bg);
        p.setOpaque(true);
        p.setPreferredSize(new Dimension(0, 52));
        p.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        p.add(comp, gbc);

        if (ri >= 0) {
            p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    filaExp = (filaExp == ri) ? -1 : ri;
                    poblarTabla();
                }
            });
        }
        return p;
    }

    private JPanel buildExpansion(Receta r) {
        JPanel exp = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_EXP_BG); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(C_EXP_BOR); g2.setStroke(new BasicStroke(1.4f));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        exp.setOpaque(false);
        exp.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel tit = new JLabel("INGREDIENTES POR PORCIÓN");
        tit.setFont(new Font("Arial", Font.BOLD, 11)); tit.setForeground(C_COSTO);
        exp.add(tit, BorderLayout.NORTH);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4)); chips.setOpaque(false);
        for (Ingrediente ing : r.getIngredientes()) chips.add(mkChip(ing.textoChip()));
        exp.add(chips, BorderLayout.CENTER);
        return exp;
    }

    // ═══════════════════════════════════════════════
    // MODAL 1: CREAR / EDITAR RECETA (SIN FONDO NEGRO)
    // ═══════════════════════════════════════════════
    private void dlgReceta(Receta recetaEditar) {
        boolean esEd = recetaEditar != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "", true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0)); // Transparente para esquinas redondeadas

        // Tarjeta principal ajustada exactamente
        JPanel main = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            }
        };
        main.setOpaque(false);
        main.setPreferredSize(new Dimension(620, 580));
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Encabezado + Botón Cierre
        JPanel fTit = new JPanel(new BorderLayout()); fTit.setOpaque(false);
        fTit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel tit = new JLabel(esEd ? "Editar receta" : "Crear receta");
        tit.setFont(new Font("Arial", Font.BOLD, 22)); tit.setForeground(C_ACCENT);
        JButton btnX = mkBtnX();
        btnX.addActionListener(e -> dlg.dispose());
        fTit.add(tit, BorderLayout.WEST); fTit.add(btnX, BorderLayout.EAST);
        body.add(fTit);
        body.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel desc = new JLabel("<html><body>Cada receta se vincula a un producto. Al registrar un pedido, el sistema descuenta automáticamente del inventario.</body></html>");
        desc.setFont(new Font("Arial", Font.PLAIN, 12)); desc.setForeground(new Color(0x666666));
        desc.setAlignmentX(LEFT_ALIGNMENT);
        body.add(desc);
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // CAMPO NOMBRE
        body.add(mkLblSec("NOMBRE DE LA RECETA"));
        body.add(Box.createRigidArea(new Dimension(0, 4)));
        JTextField txtNom = new JTextField(esEd ? recetaEditar.getNombre() : "");
        JPanel campoNom = mkCampo(txtNom, "Ej. Enchiladas verdes", 44);
        body.add(campoNom);
        body.add(Box.createRigidArea(new Dimension(0, 14)));

        // INSTRUCCIONES
        body.add(mkLblSec("INSTRUCCIONES DE PREPARACIÓN (opcional)"));
        body.add(Box.createRigidArea(new Dimension(0, 4)));
        JTextArea txtInstr = new JTextArea(3, 1);
        txtInstr.setFont(new Font("Arial", Font.PLAIN, 13));
        txtInstr.setLineWrap(true); txtInstr.setWrapStyleWord(true);
        txtInstr.setBackground(C_CAMPO_BG); txtInstr.setOpaque(true);
        txtInstr.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        txtInstr.setText("Pasos para preparar el platillo...");
        txtInstr.setForeground(new Color(0xAAAAAA));

        txtInstr.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtInstr.getText().equals("Pasos para preparar el platillo...")) {
                    txtInstr.setText(""); txtInstr.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtInstr.getText().trim().isEmpty()) {
                    txtInstr.setText("Pasos para preparar el platillo...");
                    txtInstr.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        JPanel wInstr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            }
        };
        wInstr.setOpaque(false); wInstr.setPreferredSize(new Dimension(0, 70));
        wInstr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        wInstr.setAlignmentX(LEFT_ALIGNMENT);
        wInstr.add(txtInstr, BorderLayout.CENTER);
        body.add(wInstr);
        body.add(Box.createRigidArea(new Dimension(0, 12)));

        // TABLA DINÁMICA DE INGREDIENTES
        List<Ingrediente> ingsActuales = esEd ? new ArrayList<>(recetaEditar.getIngredientes()) : new ArrayList<>();
        JPanel panelIng = new JPanel();
        panelIng.setLayout(new BoxLayout(panelIng, BoxLayout.Y_AXIS));
        panelIng.setBackground(new Color(0xFAF4EE));
        panelIng.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_CAMPO_BOR, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        panelIng.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblCostoVal = new JLabel(fmtCosto(ingsActuales), SwingConstants.RIGHT);
        lblCostoVal.setFont(new Font("Arial", Font.BOLD, 14)); lblCostoVal.setForeground(C_COSTO);

        Runnable[] rb = {null};
        rb[0] = () -> {
            panelIng.removeAll();
            JPanel enc = new JPanel(new GridLayout(1, 3, 8, 0)); enc.setOpaque(false);
            enc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            enc.add(mkLblG("Insumo")); enc.add(mkLblG("Cantidad")); enc.add(mkLblG("Unidad"));
            panelIng.add(enc);
            panelIng.add(Box.createRigidArea(new Dimension(0, 4)));

            for (Ingrediente ing : ingsActuales) {
                JPanel fila = new JPanel(new GridLayout(1, 3, 8, 0)); fila.setOpaque(false);
                fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                fila.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xDDD0C0), 1, true),
                        BorderFactory.createEmptyBorder(2, 8, 2, 8)));
                fila.add(mkLblDat(ing.getInsumo().getNombre()));
                fila.add(mkLblDat(String.valueOf(ing.getCantidad())));
                fila.add(mkLblDat(ing.getUnidad()));
                panelIng.add(fila);
                panelIng.add(Box.createRigidArea(new Dimension(0, 4)));
            }
            panelIng.revalidate(); panelIng.repaint();
            lblCostoVal.setText(fmtCosto(ingsActuales));
        };
        rb[0].run();

        JPanel fBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); fBtn.setOpaque(false);
        fBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        fBtn.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnAddIng = mkBtnDark("+ Agregar ingrediente");
        btnAddIng.setPreferredSize(new Dimension(200, 34));
        btnAddIng.addActionListener(e -> dlgAgregarIng(dlg, ingsActuales, rb[0]));
        fBtn.add(btnAddIng);
        body.add(fBtn);
        body.add(Box.createRigidArea(new Dimension(0, 6)));

        JScrollPane scrollIng = new JScrollPane(panelIng, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollIng.setBorder(BorderFactory.createEmptyBorder());
        scrollIng.setPreferredSize(new Dimension(0, 130));
        scrollIng.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        scrollIng.setAlignmentX(LEFT_ALIGNMENT);
        body.add(scrollIng);

        // PIE COSTO
        JPanel fCosto = new JPanel(new BorderLayout()); fCosto.setBackground(new Color(0xFDF3E7));
        fCosto.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fCosto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        fCosto.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblCostoT = new JLabel("Costo estimado de insumos");
        lblCostoT.setFont(new Font("Arial", Font.BOLD, 12)); lblCostoT.setForeground(C_COSTO);
        fCosto.add(lblCostoT, BorderLayout.WEST); fCosto.add(lblCostoVal, BorderLayout.EAST);
        body.add(fCosto);
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // BOTONES
        JPanel fBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); fBtns.setOpaque(false);
        fBtns.setAlignmentX(LEFT_ALIGNMENT); fBtns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton btnCan = mkBtnBorde("Cancelar"); btnCan.setPreferredSize(new Dimension(100, 36));
        btnCan.addActionListener(e -> dlg.dispose());

        JButton btnSav = mkBtnDark(esEd ? "Guardar cambios" : "Crear receta");
        btnSav.setPreferredSize(new Dimension(150, 36));
        btnSav.addActionListener(e -> {
            String nom = txtNom.getText().trim();

            // ── Validar nombre ──
            if (nom.isEmpty() || nom.equals("Ej. Enchiladas verdes")) {
                JOptionPane.showMessageDialog(dlg,
                        "No se pudo guardar: debes ingresar el nombre de la receta.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que haya al menos un ingrediente ──
            if (ingsActuales.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "No se pudo guardar: agrega al menos un ingrediente a la receta.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (esEd) {
                recetaEditar.getIngredientes().clear();
                recetaEditar.getIngredientes().addAll(ingsActuales);
                poblarTabla();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "Receta \"" + nom + "\" actualizada correctamente.",
                        "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String nid = String.format("#R%02d", recetas.size() + 1);
                Receta nueva = new Receta(nid, nom, nom + " — $0.00", 10, ingsActuales);
                recetas.add(nueva);
                guardarRecetaEnBD(nueva);
                poblarTabla();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "Receta \"" + nom + "\" creada correctamente.",
                        "Receta creada", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        fBtns.add(btnCan); fBtns.add(btnSav);
        body.add(fBtns);

        main.add(body, BorderLayout.CENTER);
        dlg.setContentPane(main);
        dlg.pack();
        dlg.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
    }

    // ═══════════════════════════════════════════════
    // MODAL 2: AGREGAR INGREDIENTE CON BUSCADOR Y STOCK
    // ═══════════════════════════════════════════════
    private void dlgAgregarIng(JDialog padre, List<Ingrediente> ings, Runnable rebuild) {
        JDialog dlg2 = new JDialog(padre, "", true);
        dlg2.setUndecorated(true);
        dlg2.setBackground(new Color(0, 0, 0, 0));

        JPanel main = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            }
        };
        main.setOpaque(false);
        main.setPreferredSize(new Dimension(480, 390));
        main.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0; gc.gridx = 0; gc.weighty = 0;

        // Encabezado
        JPanel fTit = new JPanel(new BorderLayout()); fTit.setOpaque(false);
        JLabel tit = new JLabel("Agregar ingrediente");
        tit.setFont(new Font("Arial", Font.BOLD, 18)); tit.setForeground(C_ACCENT);
        JButton bx = mkBtnX(); bx.addActionListener(e -> dlg2.dispose());
        fTit.add(tit, BorderLayout.WEST); fTit.add(bx, BorderLayout.EAST);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 14, 0); main.add(fTit, gc);

        // BUSCADOR EN TIEMPO REAL DE INSUMOS
        main.add(mkLblSec("BUSCAR INSUMO (INVENTARIO)"), setGC(gc, 1, new Insets(0, 0, 4, 0)));
        JTextField txtBuscar = new JTextField();
        JPanel cBuscar = mkCampo(txtBuscar, "Escribe para filtrar...", 38);
        main.add(cBuscar, setGC(gc, 2, new Insets(0, 0, 8, 0)));

        // COMBOBOX DE INSUMOS
        DefaultComboBoxModel<Insumo> comboModel = new DefaultComboBoxModel<>();
        for (Insumo ins : listaInsumosInventario) comboModel.addElement(ins);
        JComboBox<Insumo> cmbInsumos = new JComboBox<>(comboModel);
        cmbInsumos.setFont(new Font("Arial", Font.PLAIN, 13));
        JPanel wCmb = mkCampoWrap(cmbInsumos, 0, 42);
        main.add(wCmb, setGC(gc, 3, new Insets(0, 0, 12, 0)));

        // EVENTO FILTRO DE BÚSQUEDA
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            private void filtrar() {
                String query = txtBuscar.getText().trim().toLowerCase();
                comboModel.removeAllElements();
                if (query.equals("escribe para filtrar...") || query.isEmpty()) {
                    for (Insumo i : listaInsumosInventario) comboModel.addElement(i);
                } else {
                    for (Insumo i : listaInsumosInventario) {
                        if (i.getNombre().toLowerCase().contains(query)) comboModel.addElement(i);
                    }
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        // CANTIDAD Y UNIDAD
        main.add(mkLblSec("CANTIDAD Y UNIDAD"), setGC(gc, 4, new Insets(0, 0, 4, 0)));
        JTextField txtCantidad = new JTextField();
        JTextField txtUnidad = new JTextField(); txtUnidad.setEditable(false);

        JPanel fCU = new JPanel(new GridLayout(1, 2, 10, 0)); fCU.setOpaque(false);
        fCU.add(mkCampo(txtCantidad, "Cantidad", 42));
        fCU.add(mkCampo(txtUnidad, "Unidad", 42));
        main.add(fCU, setGC(gc, 5, new Insets(0, 0, 6, 0)));

        JLabel lblInfoStock = new JLabel(" ");
        lblInfoStock.setFont(new Font("Arial", Font.PLAIN, 12)); lblInfoStock.setForeground(C_COSTO);
        main.add(lblInfoStock, setGC(gc, 6, new Insets(0, 0, 16, 0)));

        // Listener para auto-completar unidad y mostrar precio/stock
        cmbInsumos.addActionListener(e -> {
            Insumo sel = (Insumo) cmbInsumos.getSelectedItem();
            if (sel != null) {
                txtUnidad.setText(sel.getUnidad());
                txtUnidad.setForeground(new Color(0x333333));
                lblInfoStock.setText(String.format("Precio: $%.2f/%s | Stock: %.2f %s",
                        sel.getPrecioUnitario(), sel.getUnidad(), sel.getStockActual(), sel.getUnidad()));
            } else {
                txtUnidad.setText("Unidad");
                lblInfoStock.setText(" ");
            }
        });
        if (cmbInsumos.getItemCount() > 0) cmbInsumos.setSelectedIndex(0);

        // BOTONES DE ACCIÓN
        JPanel fB = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); fB.setOpaque(false);
        JButton bCan = mkBtnBorde("Cancelar"); bCan.setPreferredSize(new Dimension(100, 36));
        bCan.addActionListener(e -> dlg2.dispose());

        JButton bAdd = mkBtnDark("Agregar"); bAdd.setPreferredSize(new Dimension(100, 36));
        bAdd.addActionListener(e -> {
            // ── Validar que haya insumos disponibles para elegir ──
            Insumo sel = (Insumo) cmbInsumos.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(dlg2,
                        "No hay insumos disponibles para seleccionar. Verifica el inventario.",
                        "Sin insumos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que el campo de cantidad no esté vacío ──
            String cantTxt = txtCantidad.getText().trim();
            if (cantTxt.isEmpty() || cantTxt.equals("Cantidad")) {
                JOptionPane.showMessageDialog(dlg2,
                        "No se pudo agregar: debes ingresar la cantidad del insumo.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que la cantidad sea un número válido ──
            double cant;
            try {
                cant = Double.parseDouble(cantTxt);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg2,
                        "La cantidad debe ser un número válido (ej. 0.5 o 2).",
                        "Formato inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ── Validar que la cantidad sea mayor a cero ──
            if (cant <= 0) {
                JOptionPane.showMessageDialog(dlg2,
                        "La cantidad debe ser mayor a cero.",
                        "Formato inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ── Advertencia (no bloqueante) si excede el stock actual ──
            if (cant > sel.getStockActual()) {
                JOptionPane.showMessageDialog(dlg2,
                        String.format("Atención: la cantidad ingresada (%.2f %s) supera el stock actual de \"%s\" (%.2f %s). Se agregará de todas formas.",
                                cant, sel.getUnidad(), sel.getNombre(), sel.getStockActual(), sel.getUnidad()),
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            }

            ings.add(new Ingrediente(sel, cant));
            rebuild.run();
            dlg2.dispose();
        });

        fB.add(bCan); fB.add(bAdd);
        main.add(fB, setGC(gc, 7, new Insets(0, 0, 0, 0)));

        dlg2.setContentPane(main);
        dlg2.pack();
        dlg2.setLocationRelativeTo(padre);
        dlg2.setVisible(true);
    }

    // ─── HELPERS GRÁFICOS Y COMPONENTES ────────────
    private JLabel mkLblSec(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.BOLD, 11));
        l.setForeground(C_ACCENT); l.setAlignmentX(LEFT_ALIGNMENT); return l;
    }
    private JLabel mkLblG(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.PLAIN, 11));
        l.setForeground(new Color(0x999999)); return l;
    }
    private JLabel mkLblDat(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(new Color(0x333333)); return l;
    }
    private JLabel mkLblDatBold(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(0x333333)); return l;
    }
    private JLabel mkLblDatMuted(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Arial", Font.PLAIN, 12));
        l.setForeground(new Color(0x666666)); return l;
    }
    private JLabel mkChip(String txt) {
        JLabel l = new JLabel(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_DIV); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Arial", Font.PLAIN, 12)); l.setForeground(new Color(0x333333));
        l.setOpaque(false); l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return l;
    }
    private JLabel mkBadgeId(String id) {
        JLabel l = new JLabel(id);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(C_ID_BOR);
        return l;
    }
    private JLabel mkBadgeVin() {
        JLabel l = new JLabel("Vinculada");
        l.setFont(new Font("Arial", Font.BOLD, 12));
        l.setForeground(C_ID_BOR);
        return l;
    }
    private JLabel mkLinkAccion(String texto, Color color, Runnable accion) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(color);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { accion.run(); }
        });
        return l;
    }
    private JPanel buildAcciones(Receta r) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0)); p.setOpaque(false);
        JLabel ed = mkLinkAccion("Editar", C_BTN_DARK, () -> dlgReceta(r));
        JLabel el = mkLinkAccion("Eliminar", C_BTN_RBOR, () -> onEliminar(r));
        p.add(ed); p.add(el); return p;
    }
    private JButton mkBtnDark(String t) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BTN_DARK); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton mkBtnBorde(String t) {
        JButton b = new JButton(t) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_BTN_RBOR); g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.setColor(C_BTN_RBOR); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        b.setFont(new Font("Arial", Font.PLAIN, 12));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JButton mkBtnX() {
        JButton b = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF0F0F0)); g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(0x777777)); g2.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("✕", (getWidth() - fm.stringWidth("✕")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        b.setPreferredSize(new Dimension(28, 28)); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JPanel mkCampo(JTextField f, String ph, int h) {
        f.setFont(new Font("Arial", Font.PLAIN, 13)); f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        if (f.getText().isEmpty()) { f.setText(ph); f.setForeground(new Color(0xAAAAAA)); }
        else f.setForeground(new Color(0x333333));

        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) { f.setText(""); f.setForeground(new Color(0x333333)); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().trim().isEmpty()) { f.setText(ph); f.setForeground(new Color(0xAAAAAA)); }
            }
        });

        JPanel w = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            }
        };
        w.setOpaque(false); w.setPreferredSize(new Dimension(0, h));
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, h)); w.setAlignmentX(LEFT_ALIGNMENT);
        w.add(f, BorderLayout.CENTER); return w;
    }
    private JPanel mkCampoWrap(JComboBox<?> c, int w, int h) {
        c.setOpaque(false);
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            }
        };
        p.setOpaque(false);
        if (w > 0) p.setPreferredSize(new Dimension(w, h));
        else { p.setPreferredSize(new Dimension(0, h)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, h)); }
        p.add(c, BorderLayout.CENTER); return p;
    }
    private GridBagConstraints setGC(GridBagConstraints gc, int g, Insets i) { gc.gridy = g; gc.insets = i; return gc; }
    private String fmtCosto(List<Ingrediente> l) {
        double t = l.stream().mapToDouble(Ingrediente::subtotal).sum();
        return String.format("$%.2f", t);
    }

    // ─── ACCIONES Y FILTROS ────────────────────────
    private void filtrar() {
        String s = (String) cmbFiltro.getSelectedItem();
        if ("Todos los productos".equals(s)) { poblarTabla(); return; }
        List<Receta> f = recetas.stream()
                .filter(r -> r.getProductoVinculado().startsWith(s))
                .collect(Collectors.toList());
        poblarCon(f);
    }

    private void onEliminar(Receta r) {
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar la receta \"" + r.getNombre() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            recetas.remove(r);
            eliminarRecetaEnBD(r.getId());
            if (filaExp >= recetas.size()) filaExp = -1;
            poblarTabla();
        }
    }

    // ─── MOCK / DATOS DE PRUEBA ────────────────────
    private void inicializarDatosDummy() {
        // Insumos provienen del módulo de Inventario
        listaInsumosInventario.add(new Insumo("I01", "Pollo", "kg", 40.0, 15.0));
        listaInsumosInventario.add(new Insumo("I02", "Tortillas", "kg", 8.0, 30.0));
        listaInsumosInventario.add(new Insumo("I03", "Queso Oaxaca", "kg", 120.0, 5.0));
        listaInsumosInventario.add(new Insumo("I04", "Jitomate", "kg", 15.0, 20.0));
        listaInsumosInventario.add(new Insumo("I05", "Chile", "kg", 20.0, 8.0));
        listaInsumosInventario.add(new Insumo("I06", "Cebolla", "kg", 12.0, 12.0));
        listaInsumosInventario.add(new Insumo("I07", "Crema", "L", 35.0, 10.0));
        listaInsumosInventario.add(new Insumo("I08", "Frijol", "kg", 18.0, 25.0));

        // Recetas precargadas
        List<Ingrediente> i1 = new ArrayList<>();
        i1.add(new Ingrediente(listaInsumosInventario.get(0), 0.3));
        i1.add(new Ingrediente(listaInsumosInventario.get(1), 0.1));
        i1.add(new Ingrediente(listaInsumosInventario.get(2), 0.05));
        i1.add(new Ingrediente(listaInsumosInventario.get(3), 0.1));

        List<Ingrediente> i2 = new ArrayList<>();
        i2.add(new Ingrediente(listaInsumosInventario.get(0), 0.4));
        i2.add(new Ingrediente(listaInsumosInventario.get(4), 0.05));
        i2.add(new Ingrediente(listaInsumosInventario.get(5), 0.1));
        i2.add(new Ingrediente(listaInsumosInventario.get(3), 0.15));

        recetas.add(new Receta("#R01", "Enchiladas verdes", "Enchiladas verdes — $85.00", 12, i1));
        recetas.add(new Receta("#R02", "Pozole rojo", "Pozole rojo — $95.00", 18, i2));
    }

    // ═══════════════════════════════════════════════
    // MÉTODOS PARA CONEXIÓN A BASE DE DATOS (BD API)
    // ═══════════════════════════════════════════════
    /** Carga la lista de insumos recuperados desde la consulta SQL en el Inventario */
    public void setInsumosInventario(List<Insumo> insumosDB) {
        listaInsumosInventario.clear();
        listaInsumosInventario.addAll(insumosDB);
    }

    /** Carga la lista completa de recetas leídas desde la BD */
    public void setRecetasDB(List<Receta> recetasDB) {
        recetas.clear();
        recetas.addAll(recetasDB);
        filaExp = -1;
        poblarTabla();
    }

    /** TODO (BD): Implementar la inserción en BD (Tabla `recetas` e `ingredientes_receta`) */
    private void guardarRecetaEnBD(Receta r) {
        // Ejemplo: DAO.insertarReceta(r);
    }

    /** TODO (BD): Implementar el borrado en BD */
    private void eliminarRecetaEnBD(String recetaId) {
        // Ejemplo: DAO.eliminarReceta(recetaId);
    }
}