package vistas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista: PanelInventario — Inventario
 *
 * Estructura corregida:
 *   - Tabla completa renderizada bajo una única cuadrícula (GridBagLayout máster)
 *     para garantizar alineación perfecta entre encabezados y filas.
 */
public class PanelInventario extends JPanel {

    // ─── COLORES ───────────────────────────────────
    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_CAMPO_BG  = new Color(0xEEE8DE);
    private static final Color C_CAMPO_BOR = new Color(0xD4C4A8);
    private static final Color C_BTN_DARK  = new Color(0x4A2010);
    private static final Color C_TBL_HDR   = new Color(0x7A3520);
    private static final Color C_TBL_HDR_T = Color.WHITE;
    private static final Color C_CAT_TEXT  = new Color(0x8B5A3C);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_ALT_ROW   = new Color(0xFAF4EE);
    private static final Color C_DIV_LINE  = new Color(0xC8A882);

    // Indicadores de nivel de stock
    private static final Color C_STOCK_RED   = new Color(0xB83227);
    private static final Color C_STOCK_WARN  = new Color(0xD97724);
    private static final Color C_STOCK_OK    = new Color(0x6B2D1A);
    private static final Color C_BAR_BG      = new Color(0xEAE2D5);

    // ─── MODELO ────────────────────────────────────
    static class Insumo {
        String id, nombre, categoria, unidad;
        double cantidad;
        int porcentaje; // 0 a 100 para la barra visual

        Insumo(String id, String nombre, String categoria, double cantidad, String unidad, int porcentaje) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.porcentaje = porcentaje;
        }

        String getStockTexto() {
            if (cantidad == (long) cantidad) {
                return String.format("%d %s", (long) cantidad, unidad);
            } else {
                return String.format("%.1f %s", cantidad, unidad);
            }
        }
    }

    // ─── DATOS DUMMY ───────────────────────────────
    private final List<Insumo> insumos = new ArrayList<>();

    // ─── COMPONENTES ───────────────────────────────
    private JTextField txtNombre, txtCantidad, txtNota;
    private JComboBox<String> cmbUnidad, cmbTipoMov;
    private JPanel panelTabla;

    // Configuración de columnas (Weights sumados dan 1.0)
    private static final double[] PW = {0.20, 0.15, 0.38, 0.09, 0.18};
    private static final String[] COLS = {"Insumo", "Categoría", "Stock actual", "Unidad", "Acciones"};

    // ─── CONSTRUCTOR ───────────────────────────────
    public PanelInventario() {
        setLayout(new BorderLayout());
        setBackground(C_BG);
        inicializarDummy();

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
    // CONTENIDO PRINCIPAL
    // ═══════════════════════════════════════════════
    private JPanel buildContenido() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx   = 0;
        gc.weighty = 0;

        // ── Título principal ──
        gc.gridy = 0; gc.insets = new Insets(0, 0, 20, 0);
        p.add(buildTituloPrincipal(), gc);

        // ── Subtítulo "Registrar movimiento de insumo" ──
        gc.gridy = 1; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Registrar movimiento de insumo"), gc);

        // ── Fila 1 Formulario ──
        JPanel f1 = new JPanel(new GridBagLayout());
        f1.setOpaque(false);
        GridBagConstraints g1 = new GridBagConstraints();
        g1.fill = GridBagConstraints.HORIZONTAL;
        g1.insets = new Insets(0, 0, 0, 12);

        txtNombre = new JTextField();
        txtCantidad = new JTextField();
        cmbUnidad = mkCombo(new String[]{"Unidad ▾", "kg", "g", "l", "ml", "pz"});

        g1.gridx = 0; g1.weightx = 0.50;
        f1.add(buildCampo(txtNombre, "Nombre del insumo"), g1);

        g1.gridx = 1; g1.weightx = 0.28;
        f1.add(buildCampo(txtCantidad, "Cantidad"), g1);

        g1.gridx = 2; g1.weightx = 0.22; g1.insets = new Insets(0, 0, 0, 0);
        f1.add(buildComboWrap(cmbUnidad), g1);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 10, 0);
        p.add(f1, gc);

        // ── Fila 2 Formulario ──
        JPanel f2 = new JPanel(new GridBagLayout());
        f2.setOpaque(false);
        GridBagConstraints g2 = new GridBagConstraints();
        g2.fill = GridBagConstraints.HORIZONTAL;
        g2.insets = new Insets(0, 0, 0, 12);

        cmbTipoMov = mkCombo(new String[]{"Tipo de movimiento ▾", "Entrada / Reabasto", "Salida / Consumo", "Ajuste"});
        txtNota = new JTextField();
        JButton btnGuardar = buildBoton("Guardar");
        btnGuardar.addActionListener(e -> onGuardarMovimiento());

        g2.gridx = 0; g2.weightx = 0.35;
        f2.add(buildComboWrap(cmbTipoMov), g2);

        g2.gridx = 1; g2.weightx = 0.45;
        f2.add(buildCampo(txtNota, "Nota o motivo del movimiento (opcional)"), g2);

        g2.gridx = 2; g2.weightx = 0.20; g2.insets = new Insets(0, 0, 0, 0);
        f2.add(btnGuardar, g2);

        gc.gridy = 3; gc.insets = new Insets(0, 0, 28, 0);
        p.add(f2, gc);

        // ── Subtítulo "Insumos registrados" ──
        gc.gridy = 4; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Insumos registrados"), gc);

        // ── Contenedor de la Tabla Unificada ──
        panelTabla = new JPanel();
        panelTabla.setOpaque(false);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 0, 0);
        p.add(panelTabla, gc);

        // Relleno inferior
        gc.gridy = 6; gc.weighty = 1.0; gc.fill = GridBagConstraints.BOTH;
        JPanel sp = new JPanel(); sp.setOpaque(false);
        p.add(sp, gc);

        poblarTabla();
        return p;
    }

    // ─── TÍTULO Y SUBTÍTULO ────────────────────────
    private JPanel buildTituloPrincipal() {
        JLabel lbl = new JLabel("Inventario");
        lbl.setFont(new Font("Arial", Font.BOLD, 30));
        lbl.setForeground(C_ACCENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV_LINE);
        sep.setBackground(C_DIV_LINE);

        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(lbl, BorderLayout.NORTH);
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
    // RENDERIZADO UNIFICADO DE LA TABLA
    // ═══════════════════════════════════════════════
    private void poblarTabla() {
        panelTabla.removeAll();
        panelTabla.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0;

        // ─── 1. RENDERIZAR ENCABEZADO (FILA 0) ───
        g.gridy = 0;
        for (int i = 0; i < COLS.length; i++) {
            g.gridx = i;
            g.weightx = PW[i];

            JPanel cellHeader = new JPanel(new BorderLayout());
            cellHeader.setBackground(C_TBL_HDR);
            cellHeader.setPreferredSize(new Dimension(0, 40));
            cellHeader.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

            JLabel lbl = new JLabel(COLS[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 13));
            lbl.setForeground(C_TBL_HDR_T);
            cellHeader.add(lbl, BorderLayout.WEST);

            panelTabla.add(cellHeader, g);
        }

        // ─── 2. RENDERIZAR FILAS DE DATOS ───
        int currentGridY = 1;
        for (int i = 0; i < insumos.size(); i++) {
            Insumo item = insumos.get(i);
            Color rowBg = (i % 2 == 0) ? C_WHITE : C_ALT_ROW;

            g.gridy = currentGridY;

            // Col 0: Insumo
            g.gridx = 0; g.weightx = PW[0];
            panelTabla.add(buildCellWrapper(mkLbl(item.nombre, new Color(0x333333), false), rowBg), g);

            // Col 1: Categoría
            g.gridx = 1; g.weightx = PW[1];
            panelTabla.add(buildCellWrapper(mkLbl(item.categoria, C_CAT_TEXT, false), rowBg), g);

            // Col 2: Stock actual (Texto + Barra)
            g.gridx = 2; g.weightx = PW[2];
            panelTabla.add(buildCeldaStock(item, rowBg), g);

            // Col 3: Unidad
            g.gridx = 3; g.weightx = PW[3];
            panelTabla.add(buildCellWrapper(mkLbl(item.unidad, new Color(0x333333), false), rowBg), g);

            // Col 4: Acciones
            g.gridx = 4; g.weightx = PW[4];
            panelTabla.add(buildCellWrapper(buildAcciones(item), rowBg), g);

            currentGridY++;

            // Línea divisoria suave
            g.gridy = currentGridY;
            g.gridx = 0;
            g.gridwidth = 5;
            g.weightx = 1.0;

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0xEEDDCC));
            sep.setBackground(new Color(0xEEDDCC));
            panelTabla.add(sep, g);

            g.gridwidth = 1; // Restaurar ancho estándar
            currentGridY++;
        }

        panelTabla.revalidate();
        panelTabla.repaint();
    }

    // Wrap estándar para que cada celda tenga altura constante y margen interior perfecto
    private JPanel buildCellWrapper(JComponent comp, Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setOpaque(true);
        p.setPreferredSize(new Dimension(0, 48));
        p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        p.add(comp, BorderLayout.WEST);
        return p;
    }

    /**
     * Celda para la columna "Stock actual":
     * Muestra la cantidad a la izquierda y la barra alineada perfectamente a su derecha.
     */
    private JPanel buildCeldaStock(Insumo item, Color bg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bg);
        p.setOpaque(true);
        p.setPreferredSize(new Dimension(0, 48));
        p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        Color color;
        if (item.porcentaje <= 15) {
            color = C_STOCK_RED;
        } else if (item.porcentaje <= 40) {
            color = C_STOCK_WARN;
        } else {
            color = C_STOCK_OK;
        }

        JLabel lblText = new JLabel(item.getStockTexto());
        lblText.setFont(new Font("Arial", Font.BOLD, 13));
        lblText.setForeground(color);
        lblText.setPreferredSize(new Dimension(65, 20)); // Ancho reservado constante para el texto

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int fillW = Math.max(6, (int) (w * (item.porcentaje / 100.0)));

                // Fondo
                g2.setColor(C_BAR_BG);
                g2.fillRoundRect(0, (h - 10) / 2, w, 10, 10, 10);

                // Relleno de nivel
                g2.setColor(color);
                g2.fillRoundRect(0, (h - 10) / 2, fillW, 10, 10, 10);
            }
        };
        bar.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;

        g.gridx = 0; g.weightx = 0.0; g.anchor = GridBagConstraints.WEST;
        p.add(lblText, g);

        g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 10, 0, 20);
        p.add(bar, g);

        return p;
    }

    /** Reabasto | Eliminar como texto clickeable */
    private JPanel buildAcciones(Insumo item) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel reabasto = new JLabel("Reabasto");
        reabasto.setFont(new Font("Arial", Font.PLAIN, 13));
        reabasto.setForeground(C_ACCENT);
        reabasto.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reabasto.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onReabasto(item); }
        });

        JLabel pipe = new JLabel("|");
        pipe.setFont(new Font("Arial", Font.PLAIN, 13));
        pipe.setForeground(new Color(0xCCCCCC));

        JLabel eliminar = new JLabel("Eliminar");
        eliminar.setFont(new Font("Arial", Font.PLAIN, 13));
        eliminar.setForeground(new Color(0xC03020));
        eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eliminar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onEliminar(item); }
        });

        p.add(reabasto); p.add(pipe); p.add(eliminar);
        return p;
    }

    private JLabel mkLbl(String t, Color c, boolean bold) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(c);
        return l;
    }

    // ─── COMPONENTES ESTILIZADOS DEL FORMULARIO ───
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
        wrap.setPreferredSize(new Dimension(0, 48));
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JComboBox<String> mkCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Arial", Font.PLAIN, 14));
        c.setForeground(new Color(0x666666));
        c.setBackground(C_CAMPO_BG);
        c.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        return c;
    }

    private JPanel buildComboWrap(JComboBox<String> combo) {
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
        wrap.setPreferredSize(new Dimension(0, 48));
        combo.setOpaque(false);
        wrap.add(combo, BorderLayout.CENTER);
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
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 48));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════
    // LÓGICA DE EVENTOS INTERACTIVOS
    // ═══════════════════════════════════════════════
    private void onGuardarMovimiento() {
        String nom = txtNombre.getText().trim();
        String cantStr = txtCantidad.getText().trim();
        String ph1 = "Nombre del insumo";
        String ph2 = "Cantidad";

        if (nom.equals(ph1) || nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa el nombre del insumo.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cantStr.equals(ph2) || cantStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa la cantidad del movimiento.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double cantidad;
        try {
            cantidad = Double.parseDouble(cantStr);
            if (cantidad < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido para la cantidad.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String unidad = (String) cmbUnidad.getSelectedItem();
        if ("Unidad ▾".equals(unidad) || unidad == null) unidad = "kg";

        String tipoMov = (String) cmbTipoMov.getSelectedItem();
        boolean esConsumo = "Salida / Consumo".equals(tipoMov);

        Insumo existente = null;
        for (Insumo ins : insumos) {
            if (ins.nombre.equalsIgnoreCase(nom)) {
                existente = ins;
                break;
            }
        }

        if (existente != null) {
            if (esConsumo) {
                existente.cantidad = Math.max(0, existente.cantidad - cantidad);
            } else {
                existente.cantidad += cantidad;
            }
            existente.porcentaje = Math.min(100, Math.max(0, (int) (existente.cantidad * 10)));
        } else {
            int pct = Math.min(100, Math.max(10, (int) (cantidad * 10)));
            Insumo nuevo = new Insumo("#" + String.format("%03d", insumos.size() + 1), nom, "General", cantidad, unidad, pct);
            insumos.add(nuevo);
        }

        poblarTabla();

        txtNombre.setText(ph1); txtNombre.setForeground(new Color(0xAAAAAA));
        txtCantidad.setText(ph2); txtCantidad.setForeground(new Color(0xAAAAAA));
        txtNota.setText("Nota o motivo del movimiento (opcional)"); txtNota.setForeground(new Color(0xAAAAAA));
        cmbUnidad.setSelectedIndex(0);
        cmbTipoMov.setSelectedIndex(0);
    }

    private void onReabasto(Insumo item) {
        String input = JOptionPane.showInputDialog(this,
                "Ingresa la cantidad a reabastecer de \"" + item.nombre + "\" (" + item.unidad + "):",
                "Reabasto rápido",
                JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double cant = Double.parseDouble(input.trim());
                if (cant > 0) {
                    item.cantidad += cant;
                    item.porcentaje = Math.min(100, (int) (item.porcentaje + (cant * 10)));
                    poblarTabla();
                } else {
                    JOptionPane.showMessageDialog(this, "Ingresa una cantidad mayor a cero.", "Atención", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cantidad no válida.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEliminar(Insumo item) {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el insumo \"" + item.nombre + "\" del inventario?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            insumos.remove(item);
            poblarTabla();
        }
    }

    // ─── DATOS INICIALES DUMMY ─────────────────────
    private void inicializarDummy() {
        insumos.add(new Insumo("#001", "Jitomate",       "Verdura", 12.0,  "kg", 100));
        insumos.add(new Insumo("#002", "Maíz cacahuaz.", "Grano",   0.0,   "kg", 5));
        insumos.add(new Insumo("#003", "Pollo",          "Carne",   4.5,   "kg", 60));
        insumos.add(new Insumo("#004", "Tortillas",      "Grano",   8.0,   "kg", 85));
        insumos.add(new Insumo("#005", "Chile ancho",    "Verdura", 1.2,   "kg", 50));
        insumos.add(new Insumo("#006", "dawdaw",         "General", 20.0,  "kg", 100));
    }
}