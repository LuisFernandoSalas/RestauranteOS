package vistas;

import modelos.Insumo;
import org.json.JSONArray;
import org.json.JSONObject;
import servicios.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class PanelInventario extends JPanel  {

    // ─── COLORES Y CONFIGURACIÓN VISUAL ───
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

    private static final Color C_STOCK_RED   = new Color(0xB83227);
    private static final Color C_STOCK_OK    = new Color(0x6B2D1A);
    private static final Color C_BAR_BG      = new Color(0xEAE2D5);

    private static final String PH_NOMBRE    = "Nombre del insumo";
    private static final String PH_CANTIDAD  = "Cantidad";
    private static final String PH_MINIMO    = "Stock mínimo";
    private static final String PH_MAXIMO    = "Stock máximo";
    private static final String PH_NOTA      = "Nota o motivo del movimiento (opcional)";

    private static final String[] CATEGORIAS = {
            "Categoría ▾", "Verduras", "Frutas", "Carnes rojas", "Aves",
            "Pescados y mariscos", "Lácteos", "Granos y cereales", "Legumbres",
            "Panadería", "Especias y condimentos", "Salsas y aderezos",
            "Aceites y grasas", "Bebidas", "Congelados", "Desechables", "Limpieza", "Abarrotes", "Carnes", "General"
    };

    // ─── MODELO Y SERVICIOS ───
    private final ApiClient apiClient = new ApiClient();
    private final List<Insumo> insumosList = new ArrayList<>();

    // ─── COMPONENTES ───
    private JTextField txtNombre, txtCantidad, txtMinimo, txtMaximo, txtNota;
    private JComboBox<String> cmbUnidad, cmbCategoria, cmbTipoMov;
    private JPanel panelTabla;

    private static final double[] PW = {0.20, 0.15, 0.38, 0.09, 0.18};
    private static final String[] COLS = {"Insumo", "Categoría", "Stock actual", "Unidad", "Acciones"};

    public PanelInventario() {
        setLayout(new BorderLayout());
        setBackground(C_BG);

        JPanel contenido = buildContenido();
        JScrollPane scroll = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        cargarInsumosDesdeApi();
    }

    private JPanel buildContenido() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 32, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0; gc.gridx = 0; gc.weighty = 0;

        // Título
        gc.gridy = 0; gc.insets = new Insets(0, 0, 20, 0);
        p.add(buildTituloPrincipal(), gc);

        // Subtítulo
        gc.gridy = 1; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Registrar movimiento / Insumo"), gc);

        // Formulario Fila 1 (Nombre, Categoría, Cantidad, Unidad)
        JPanel f1 = new JPanel(new GridBagLayout());
        f1.setOpaque(false);
        GridBagConstraints g1 = new GridBagConstraints();
        g1.fill = GridBagConstraints.HORIZONTAL; g1.insets = new Insets(0, 0, 0, 12);

        txtNombre = new JTextField();
        cmbCategoria = mkCombo(CATEGORIAS);
        txtCantidad = new JTextField();
        cmbUnidad = mkCombo(new String[]{"Unidad ▾", "kg", "g", "l", "ml", "pz", "pza"});

        g1.gridx = 0; g1.weightx = 0.32; f1.add(buildCampo(txtNombre, PH_NOMBRE), g1);
        g1.gridx = 1; g1.weightx = 0.20; f1.add(buildComboWrap(cmbCategoria), g1);
        g1.gridx = 2; g1.weightx = 0.24; f1.add(buildCampo(txtCantidad, PH_CANTIDAD), g1);
        g1.gridx = 3; g1.weightx = 0.24; g1.insets = new Insets(0, 0, 0, 0);
        f1.add(buildComboWrap(cmbUnidad), g1);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 10, 0);
        p.add(f1, gc);

        // Formulario Fila 2 (Tipo Movimiento, Stock Mínimo, Stock Máximo)
        JPanel f2 = new JPanel(new GridBagLayout());
        f2.setOpaque(false);
        GridBagConstraints g2 = new GridBagConstraints();
        g2.fill = GridBagConstraints.HORIZONTAL; g2.insets = new Insets(0, 0, 0, 12);

        cmbTipoMov = mkCombo(new String[]{"Tipo de movimiento ▾", "ENTRADA", "MERMA", "AJUSTE"});
        txtMinimo = new JTextField();
        txtMaximo = new JTextField();

        g2.gridx = 0; g2.weightx = 0.34; f2.add(buildComboWrap(cmbTipoMov), g2);
        g2.gridx = 1; g2.weightx = 0.33; f2.add(buildCampo(txtMinimo, PH_MINIMO), g2);
        g2.gridx = 2; g2.weightx = 0.33; g2.insets = new Insets(0, 0, 0, 0);
        f2.add(buildCampo(txtMaximo, PH_MAXIMO), g2);

        gc.gridy = 3; gc.insets = new Insets(0, 0, 10, 0);
        p.add(f2, gc);

        // Formulario Fila 3 (Nota / Motivo y Botón Guardar)
        JPanel f3 = new JPanel(new GridBagLayout());
        f3.setOpaque(false);
        GridBagConstraints g3 = new GridBagConstraints();
        g3.fill = GridBagConstraints.HORIZONTAL; g3.insets = new Insets(0, 0, 0, 12);

        txtNota = new JTextField();
        JButton btnGuardar = buildBoton("Guardar");
        btnGuardar.addActionListener(e -> onGuardarMovimiento());

        g3.gridx = 0; g3.weightx = 0.82; f3.add(buildCampo(txtNota, PH_NOTA), g3);
        g3.gridx = 1; g3.weightx = 0.18; g3.insets = new Insets(0, 0, 0, 0);
        f3.add(btnGuardar, g3);

        gc.gridy = 4; gc.insets = new Insets(0, 0, 28, 0);
        p.add(f3, gc);

        // Sección Tabla
        gc.gridy = 5; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Insumos registrados"), gc);

        panelTabla = new JPanel();
        panelTabla.setOpaque(false);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        p.add(panelTabla, gc);

        return p;
    }

    // ─── FUNCIONES AUXILIARES ───
    /**
     * Remueve acentos, convierte a minúsculas y elimina espacios extras
     * para asegurar comparaciones precisas (ej. "Maíz" == "Maiz").
     */
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    /**
     * Llena el formulario con los datos del insumo seleccionado de la tabla.
     */
    private void seleccionarInsumo(Insumo item) {
        txtNombre.setText(item.getNombre());
        txtNombre.setForeground(new Color(0x333333));

        txtMinimo.setText(String.valueOf(item.getStockMinimo()));
        txtMinimo.setForeground(new Color(0x333333));

        txtMaximo.setText(String.valueOf(item.getStockMaximo()));
        txtMaximo.setForeground(new Color(0x333333));

        if (item.getCategoria() != null) {
            cmbCategoria.setSelectedItem(item.getCategoria());
        }

        if (item.getUnidadMedida() != null) {
            cmbUnidad.setSelectedItem(item.getUnidadMedida());
        }

        // Reset de campos no persistentes
        txtCantidad.setText(PH_CANTIDAD);
        txtCantidad.setForeground(new Color(0xAAAAAA));
        txtNota.setText(PH_NOTA);
        txtNota.setForeground(new Color(0xAAAAAA));
        cmbTipoMov.setSelectedIndex(0);
    }

    /**
     * Hace que un panel actúe como botón interactivo para seleccionar el insumo.
     */
    private void hacerClicable(JPanel panel, Insumo item) {
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarInsumo(item);
            }
        });
    }

    // ─── LÓGICA API ───
    private void cargarInsumosDesdeApi() {
        new SwingWorker<List<Insumo>, Void>() {
            @Override
            protected List<Insumo> doInBackground() throws Exception {
                String jsonResponse = apiClient.obtenerInsumos();
                JSONObject response = new JSONObject(jsonResponse);
                JSONArray data = response.getJSONArray("data");

                List<Insumo> lista = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    Insumo ins = new Insumo();
                    ins.setId(obj.getInt("id"));
                    ins.setNombre(obj.getString("nombre"));
                    ins.setCategoria(obj.optString("categoria", "General"));
                    ins.setUnidadMedida(obj.getString("unidad_medida"));
                    ins.setStockActual(obj.getDouble("stock_actual"));
                    ins.setStockMinimo(obj.optDouble("stock_minimo", 5.0));
                    ins.setStockMaximo(obj.optDouble("stock_maximo", 100.0));
                    lista.add(ins);
                }
                return lista;
            }

            @Override
            protected void done() {
                try {
                    insumosList.clear();
                    insumosList.addAll(get());
                    poblarTabla();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PanelInventario.this,
                            e.getMessage(), "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void onGuardarMovimiento() {
        String nom = txtNombre.getText().trim();
        String cantStr = txtCantidad.getText().trim();
        String minStr = txtMinimo.getText().trim();
        String maxStr = txtMaximo.getText().trim();
        String tipoMov = (String) cmbTipoMov.getSelectedItem();

        if (nom.equals(PH_NOMBRE) || nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el nombre del insumo.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Comparación flexible e insensible a acentos/mayúsculas
        String nomNormalizado = normalizarTexto(nom);

        Insumo existente = insumosList.stream()
                .filter(i -> normalizarTexto(i.getNombre()).equals(nomNormalizado))
                .findFirst().orElse(null);

        if (existente == null) {
            // REGISTRAR NUEVO INSUMO
            double cant = (cantStr.equals(PH_CANTIDAD) || cantStr.isEmpty()) ? 0.0 : Double.parseDouble(cantStr);
            double min  = (minStr.equals(PH_MINIMO) || minStr.isEmpty()) ? 5.0 : Double.parseDouble(minStr);
            double max  = (maxStr.equals(PH_MAXIMO) || maxStr.isEmpty()) ? 100.0 : Double.parseDouble(maxStr);
            String unidad = cmbUnidad.getSelectedIndex() > 0 ? (String) cmbUnidad.getSelectedItem() : "kg";
            String cat    = cmbCategoria.getSelectedIndex() > 0 ? (String) cmbCategoria.getSelectedItem() : "General";

            Insumo nuevo = new Insumo();
            nuevo.setNombre(nom);
            nuevo.setCategoria(cat);
            nuevo.setUnidadMedida(unidad);
            nuevo.setStockActual(cant);
            nuevo.setStockMinimo(min);
            nuevo.setStockMaximo(max);

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    apiClient.crearInsumo(nuevo);
                    return null;
                }
                @Override protected void done() {
                    limpiarFormulario();
                    cargarInsumosDesdeApi();
                }
            }.execute();

        } else {
            // INSUMO EXISTENTE: Actualizar límites (Mín/Máx) y/o registrar movimiento
            boolean tieneMovimiento = tipoMov != null && !tipoMov.contains("▾");
            boolean tieneCantidad   = !cantStr.equals(PH_CANTIDAD) && !cantStr.isEmpty();

            if (!minStr.equals(PH_MINIMO) && !minStr.isEmpty()) {
                existente.setStockMinimo(Double.parseDouble(minStr));
            }
            if (!maxStr.equals(PH_MAXIMO) && !maxStr.isEmpty()) {
                existente.setStockMaximo(Double.parseDouble(maxStr));
            }
            if (cmbCategoria.getSelectedIndex() > 0) {
                existente.setCategoria((String) cmbCategoria.getSelectedItem());
            }
            if (cmbUnidad.getSelectedIndex() > 0) {
                existente.setUnidadMedida((String) cmbUnidad.getSelectedItem());
            }

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    // 1. Enviar cambios de configuración al servidor
                    apiClient.actualizarInsumo(existente);

                    // 2. Si se especificó un movimiento y cantidad, registrarlo
                    if (tieneMovimiento && tieneCantidad) {
                        double cant = Double.parseDouble(cantStr);
                        String nota = txtNota.getText().equals(PH_NOTA) ? "" : txtNota.getText();
                        apiClient.registrarMovimiento(existente.getId(), tipoMov, cant, nota);
                    }
                    return null;
                }
                @Override protected void done() {
                    limpiarFormulario();
                    cargarInsumosDesdeApi();
                }
            }.execute();
        }
    }

    private void onEliminar(Insumo item) {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar \"" + item.getNombre() + "\" del inventario?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (ok == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    return apiClient.eliminarInsumo(item.getId());
                }
                @Override protected void done() {
                    cargarInsumosDesdeApi();
                }
            }.execute();
        }
    }

    // ─── RENDERIZADO DE TABLA Y BARRA DE STOCK ───
    private void poblarTabla() {
        panelTabla.removeAll();
        panelTabla.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;

        // Encabezados
        g.gridy = 0;
        for (int i = 0; i < COLS.length; i++) {
            g.gridx = i; g.weightx = PW[i];
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

        // Filas
        int currentGridY = 1;
        for (int i = 0; i < insumosList.size(); i++) {
            Insumo item = insumosList.get(i);
            Color rowBg = (i % 2 == 0) ? C_WHITE : C_ALT_ROW;

            // Fila interactiva: cada celda permite autocompletar al dar clic
            JPanel cNombre    = buildCellWrapper(mkLbl(item.getNombre(), new Color(0x333333)), rowBg);
            JPanel cCategoria = buildCellWrapper(mkLbl(item.getCategoria(), C_CAT_TEXT), rowBg);
            JPanel cStock     = buildCeldaStockConBarra(item, rowBg);
            JPanel cUnidad    = buildCellWrapper(mkLbl(item.getUnidadMedida(), new Color(0x333333)), rowBg);
            JPanel cAcciones  = buildCellWrapper(buildAcciones(item), rowBg);

            hacerClicable(cNombre, item);
            hacerClicable(cCategoria, item);
            hacerClicable(cStock, item);
            hacerClicable(cUnidad, item);

            g.gridy = currentGridY;
            g.gridx = 0; g.weightx = PW[0]; panelTabla.add(cNombre, g);
            g.gridx = 1; g.weightx = PW[1]; panelTabla.add(cCategoria, g);
            g.gridx = 2; g.weightx = PW[2]; panelTabla.add(cStock, g);
            g.gridx = 3; g.weightx = PW[3]; panelTabla.add(cUnidad, g);
            g.gridx = 4; g.weightx = PW[4]; panelTabla.add(cAcciones, g);

            currentGridY++;
        }

        panelTabla.revalidate();
        panelTabla.repaint();
    }

    /**
     * Construye el componente visual con el valor numérico y la barra de progreso
     */
    private JPanel buildCeldaStockConBarra(Insumo item, Color bg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bg);
        p.setPreferredSize(new Dimension(0, 48));
        p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        double actual = item.getStockActual();
        double min = item.getStockMinimo();
        double max = item.getStockMaximo() > 0 ? item.getStockMaximo() : Math.max(actual, min * 2);

        boolean bajo = actual <= min;
        Color colorBarra = bajo ? C_STOCK_RED : C_STOCK_OK;

        JLabel lblText = new JLabel(actual + " " + item.getUnidadMedida());
        lblText.setFont(new Font("Arial", Font.BOLD, 13));
        lblText.setForeground(colorBarra);
        lblText.setPreferredSize(new Dimension(80, 20));

        JPanel barra = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(C_BAR_BG);
                g2.fillRoundRect(0, (h - 8) / 2, w, 8, 8, 8);

                double ratio = Math.min(1.0, Math.max(0.0, actual / max));
                int fillWidth = (int) (w * ratio);

                if (fillWidth > 0) {
                    g2.setColor(colorBarra);
                    g2.fillRoundRect(0, (h - 8) / 2, fillWidth, 8, 8, 8);
                }
            }
        };
        barra.setOpaque(false);
        barra.setPreferredSize(new Dimension(140, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 0.0; g.anchor = GridBagConstraints.WEST;
        p.add(lblText, g);

        g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 10, 0, 0);
        p.add(barra, g);

        return p;
    }

    private JPanel buildAcciones(Insumo item) {
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        inner.setOpaque(false);

        JLabel eliminar = new JLabel("Eliminar");
        eliminar.setFont(new Font("Arial", Font.PLAIN, 13));
        eliminar.setForeground(new Color(0xC03020));
        eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eliminar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onEliminar(item); }
        });

        inner.add(eliminar);
        return inner;
    }

    private JPanel buildCellWrapper(JComponent comp, Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setPreferredSize(new Dimension(0, 48));
        p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        p.add(comp, BorderLayout.WEST);
        return p;
    }

    private JLabel mkLbl(String t, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(c);
        return l;
    }

    private JPanel buildTituloPrincipal() {
        JLabel lbl = new JLabel("Inventario");
        lbl.setFont(new Font("Arial", Font.BOLD, 30));
        lbl.setForeground(C_ACCENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV_LINE);

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

    private JPanel buildCampo(JTextField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        field.setForeground(new Color(0xAAAAAA));
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText(""); field.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder); field.setForeground(new Color(0xAAAAAA));
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
        return c;
    }

    private JPanel buildComboWrap(JComboBox<String> combo) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(0, 48));
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

    private void limpiarFormulario() {
        txtNombre.setText(PH_NOMBRE); txtNombre.setForeground(new Color(0xAAAAAA));
        txtCantidad.setText(PH_CANTIDAD); txtCantidad.setForeground(new Color(0xAAAAAA));
        txtMinimo.setText(PH_MINIMO); txtMinimo.setForeground(new Color(0xAAAAAA));
        txtMaximo.setText(PH_MAXIMO); txtMaximo.setForeground(new Color(0xAAAAAA));
        txtNota.setText(PH_NOTA); txtNota.setForeground(new Color(0xAAAAAA));
        cmbCategoria.setSelectedIndex(0); cmbUnidad.setSelectedIndex(0); cmbTipoMov.setSelectedIndex(0);
    }
}