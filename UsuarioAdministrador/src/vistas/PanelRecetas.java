package vistas;

import modelos.Ingrediente;
import modelos.Insumo;
import modelos.Producto;
import modelos.Receta;

import servicios.ApiClient;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class PanelRecetas extends JPanel implements Actualizables {

    @Override
    public void recargarDatos() {
        // Al presionar el botón en el menú lateral, esto volverá a hacer la petición HTTP
        cargarRecetasDesdeBD();
    }

    // Paleta de Colores
    private static final Color C_BG        = new Color(0xF7F4F0);
    private static final Color C_CARD_BG   = Color.WHITE;
    private static final Color C_ACCENT    = new Color(0x7A2E0E);
    private static final Color C_TEXT      = new Color(0x2B2B2B);
    private static final Color C_MUTED     = new Color(0x757575);
    private static final Color C_BORDER    = new Color(0xE5DFD9);
    private static final Color C_ROW_EVEN  = new Color(0xFAFAFA);
    private static final Color C_ROW_HOVER = new Color(0xF3EFEA);
    private static final Color C_ROW_EXP   = new Color(0xFDF8F3);
    private static final Color C_ID_BG     = new Color(0xF5EBE6);
    private static final Color C_ID_BOR    = new Color(0xC49A82);
    private static final Color C_COSTO     = new Color(0x2E6D3E);
    private static final Color C_CAMPO_BG  = new Color(0xF9F9F9);
    private static final Color C_CAMPO_BOR = new Color(0xD0C8C0);
    private static final Color C_BTN_DARK  = new Color(0x4A1E08);
    private static final Color C_BTN_RBOR  = new Color(0xB00020);
    private static final Color C_WHITE     = Color.WHITE;

    private final List<Receta> recetas = new ArrayList<>();
    private final List<Insumo> listaInsumosInventario = new ArrayList<>();
    private final ApiClient apiClient;

    private JPanel bodyTabla;
    private JComboBox<String> cmbFiltro;
    private int filaExp = -1;

    public PanelRecetas(ApiClient apiClient) {
        this.apiClient = apiClient;
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(buildContenido());
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(C_BG);
        add(scroll, BorderLayout.CENTER);

        // Carga inicial desde el backend
        cargarRecetasDesdeBD();
    }

    // ═══════════════════════════════════════════════
    // SERVICIOS HTTP / LARAVEL INTEGRACIÓN
    // ═══════════════════════════════════════════════

    public void cargarRecetasDesdeBD() {
        new SwingWorker<List<Receta>, Void>() {
            @Override
            protected List<Receta> doInBackground() throws Exception {
                String jsonStr = apiClient.get("/api/recetas");
                JSONObject response = new JSONObject(jsonStr);
                JSONArray data = response.getJSONArray("data");

                // Clave única para agrupar ingredientes: "P_12" o "N_Agua de Limón"
                Map<String, Receta> mapaRecetas = new LinkedHashMap<>();

                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.getJSONObject(i);

                    int productoId = row.optInt("producto_id", 0);
                    if (row.isNull("producto_id")) {
                        productoId = 0;
                    }

                    // Lectura del nombre directo de la receta o del producto relacionado
                    String nombreReceta = row.optString("nombre", null);
                    JSONObject objProd = row.optJSONObject("producto");

                    if (nombreReceta == null || nombreReceta.trim().isEmpty()) {
                        if (objProd != null && objProd.has("nombre")) {
                            nombreReceta = objProd.getString("nombre");
                        } else {
                            nombreReceta = (productoId > 0) ? "Producto #" + productoId : "Receta Sin Nombre";
                        }
                    }

                    String nombreProdVinculado = (objProd != null && objProd.has("nombre"))
                            ? objProd.getString("nombre")
                            : "Sin vincular";

                    // Datos del insumo
                    JSONObject objIns = row.getJSONObject("insumo");
                    Insumo insumo = new Insumo(
                            objIns.getInt("id"),
                            objIns.getString("nombre"),
                            objIns.optString("categoria", "General"),
                            objIns.optString("unidad_medida", "pza"),
                            objIns.optDouble("costo_unitario", 0.0),
                            objIns.optDouble("stock_actual", 0.0),
                            objIns.optDouble("stock_minimo", 0.0)
                    );

                    double cantidad = row.getDouble("cantidad_por_porcion");
                    Ingrediente ingrediente = new Ingrediente(insumo, cantidad);

                    // Clave de agrupación
                    String groupKey = (productoId > 0) ? "P_" + productoId : "N_" + nombreReceta.trim().toLowerCase();

                    if (!mapaRecetas.containsKey(groupKey)) {
                        Receta receta = new Receta(
                                row.optInt("id", 0),
                                productoId,
                                nombreReceta,
                                nombreProdVinculado,
                                15,
                                new ArrayList<>()
                        );
                        mapaRecetas.put(groupKey, receta);
                    }

                    mapaRecetas.get(groupKey).getIngredientes().add(ingrediente);
                }

                return new ArrayList<>(mapaRecetas.values());
            }

            @Override
            protected void done() {
                try {
                    recetas.clear();
                    recetas.addAll(get());
                    actualizarComboFiltro();
                    poblarTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelRecetas.this,
                            "Error al cargar recetas del backend:\n" + ex.getMessage(),
                            "Error de Conexión", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void guardarRecetaEnBD(Receta r, Runnable onSuccess) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                JSONObject body = new JSONObject();

                // Si productoId > 0 enviamos el entero, de lo contrario enviamos NULL
                if (r.getProductoId() > 0) {
                    body.put("producto_id", r.getProductoId());
                } else {
                    body.put("producto_id", JSONObject.NULL);
                }

                body.put("nombre", r.getNombre());

                JSONArray arrInsumos = new JSONArray();
                for (Ingrediente ing : r.getIngredientes()) {
                    JSONObject item = new JSONObject();
                    item.put("insumo_id", ing.getInsumo().getId());
                    item.put("cantidad_por_porcion", ing.getCantidad());
                    arrInsumos.put(item);
                }
                body.put("insumos", arrInsumos);

                apiClient.post("/api/recetas", body.toString());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (onSuccess != null) onSuccess.run();
                    cargarRecetasDesdeBD();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelRecetas.this,
                            "Error al guardar receta en Laravel:\n" + ex.getMessage(),
                            "Error de API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void eliminarRecetaDeBD(Receta r) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (r.getProductoId() > 0) {
                    apiClient.delete("/api/recetas/producto/" + r.getProductoId());
                } else {
                    // Si no tiene producto_id, eliminamos la receta por su ID directo
                    apiClient.delete("/api/recetas/" + r.getId());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(PanelRecetas.this, "Receta eliminada con éxito.");
                    cargarRecetasDesdeBD();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelRecetas.this,
                            "Error al eliminar la receta: " + ex.getMessage(),
                            "Error de API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void abrirDialogoReceta(Receta recetaEditar) {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                List<Producto> productos = new ArrayList<>();
                String jsonStr = apiClient.get("/api/productos");

                JSONArray arr;
                if (jsonStr.trim().startsWith("{")) {
                    JSONObject obj = new JSONObject(jsonStr);
                    arr = obj.optJSONArray("data");
                } else {
                    arr = new JSONArray(jsonStr);
                }

                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        int id = item.optInt("id", 0);
                        String nombre = item.optString("nombre", item.optString("name", "Sin Nombre"));
                        double precio = item.optDouble("precio", item.optDouble("price", 0.0));
                        productos.add(new Producto(id, nombre, "—", precio, "Activo"));
                    }
                }
                return productos;
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    dlgReceta(recetaEditar, productos);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelRecetas.this,
                            "Error al cargar productos del menú:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ═══════════════════════════════════════════════
    // CONSTRUCCIÓN DE INTERFAZ GRÁFICA (UI)
    // ═══════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(C_BG);
        h.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        JLabel tit = new JLabel("Recetas de Cocina");
        tit.setFont(new Font("Arial", Font.BOLD, 22));
        tit.setForeground(C_TEXT);

        JLabel sub = new JLabel("Asocia insumos del inventario a cada producto del menú y calcula sus costos automáticamente");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(C_MUTED);

        JPanel pTit = new JPanel(new GridLayout(2, 1, 0, 2));
        pTit.setOpaque(false);
        pTit.add(tit); pTit.add(sub);

        JButton btnNueva = mkBtnDark("+ Nueva Receta");
        btnNueva.setPreferredSize(new Dimension(160, 38));
        btnNueva.addActionListener(e -> abrirDialogoReceta(null));

        h.add(pTit, BorderLayout.WEST);
        h.add(btnNueva, BorderLayout.EAST);
        return h;
    }

    private JPanel buildContenido() {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(C_BG);
        c.setBorder(BorderFactory.createEmptyBorder(0, 28, 24, 28));

        c.add(buildFiltros());
        c.add(Box.createRigidArea(new Dimension(0, 14)));
        c.add(buildTablaCard());

        return c;
    }

    private JPanel buildFiltros() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        f.setOpaque(false);
        f.setAlignmentX(LEFT_ALIGNMENT);

        cmbFiltro = new JComboBox<>(new String[]{"Todos los productos"});
        cmbFiltro.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbFiltro.setPreferredSize(new Dimension(200, 34));
        cmbFiltro.addActionListener(e -> filtrar());

        f.add(cmbFiltro);
        return f;
    }

    private void actualizarComboFiltro() {
        if (cmbFiltro == null) return;
        cmbFiltro.removeAllItems();
        cmbFiltro.addItem("Todos los productos");
        for (Receta r : recetas) {
            cmbFiltro.addItem(r.getNombre());
        }
    }

    private JPanel buildTablaCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(C_BORDER, 1, true));
        card.setAlignmentX(LEFT_ALIGNMENT);

        bodyTabla = new JPanel();
        bodyTabla.setLayout(new BoxLayout(bodyTabla, BoxLayout.Y_AXIS));
        bodyTabla.setBackground(C_CARD_BG);

        card.add(buildEncabezadoTabla(), BorderLayout.NORTH);
        card.add(bodyTabla, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildEncabezadoTabla() {
        JPanel enc = new JPanel(new GridBagLayout());
        enc.setBackground(new Color(0xEDE7E1));
        enc.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        enc.setPreferredSize(new Dimension(0, 36));
        enc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.gridy = 0; gc.weighty = 1.0;

        String[] cols = {"PROD. ID", "RECETA / PRODUCTO", "INGRED.", "COSTO EST.", "ESTADO", "ACCIONES"};
        double[] weights = {0.10, 0.30, 0.12, 0.15, 0.13, 0.20};

        for (int i = 0; i < cols.length; i++) {
            gc.gridx = i; gc.weightx = weights[i];
            JLabel l = new JLabel(cols[i], i == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
            l.setFont(new Font("Arial", Font.BOLD, 11));
            l.setForeground(C_MUTED);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            enc.add(l, gc);
        }
        return enc;
    }

    private void poblarTabla() {
        poblarCon(recetas);
    }

    private void poblarCon(List<Receta> lista) {
        bodyTabla.removeAll();
        for (int i = 0; i < lista.size(); i++) {
            Receta r = lista.get(i);
            boolean exp = (i == filaExp);
            bodyTabla.add(buildFilaTabla(r, i, exp));
            if (exp) {
                bodyTabla.add(buildDetalleExpanded(r));
            }
        }
        bodyTabla.add(Box.createVerticalGlue());
        bodyTabla.revalidate();
        bodyTabla.repaint();
    }

    private JPanel buildFilaTabla(Receta r, int idx, boolean exp) {
        JPanel fila = new JPanel(new GridBagLayout());
        Color bg = exp ? C_ROW_EXP : (idx % 2 == 0 ? C_CARD_BG : C_ROW_EVEN);
        fila.setBackground(bg);
        fila.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        Dimension dim = new Dimension(Integer.MAX_VALUE, 48);
        fila.setPreferredSize(new Dimension(0, 48));
        fila.setMaximumSize(dim);
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.gridy = 0; gc.weighty = 1.0;

        // Muestra #ID si tiene producto, o "--" si es independiente
        String idText = (r.getProductoId() > 0) ? "#" + r.getProductoId() : "--";
        gc.gridx = 0; gc.weightx = 0.10;
        fila.add(wrapInCell(mkBadgeId(idText)), gc);

        gc.gridx = 1; gc.weightx = 0.30;
        fila.add(wrapInCell(mkLblDatBold(r.getNombre())), gc);

        gc.gridx = 2; gc.weightx = 0.12;
        fila.add(wrapInCell(mkChip(r.getIngredientes().size() + " insumos")), gc);

        gc.gridx = 3; gc.weightx = 0.15;
        fila.add(wrapInCell(mkLblDatBold(String.format("$%.2f", r.getCostoTotal()))), gc);

        // Badge de Estado: Vinculado vs Sin Vincular
        gc.gridx = 4; gc.weightx = 0.13;
        fila.add(wrapInCell(r.getProductoId() > 0 ? mkBadgeVin() : mkBadgeSinVinculation()), gc);

        gc.gridx = 5; gc.weightx = 0.20;
        fila.add(buildAcciones(r), gc);

        fila.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    filaExp = (filaExp == idx) ? -1 : idx;
                    poblarTabla();
                }
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!exp) fila.setBackground(C_ROW_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!exp) fila.setBackground(bg);
            }
        });

        return fila;
    }

    private JPanel buildDetalleExpanded(Receta r) {
        JPanel det = new JPanel();
        det.setLayout(new BoxLayout(det, BoxLayout.Y_AXIS));
        det.setBackground(C_ROW_EXP);
        det.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
                BorderFactory.createEmptyBorder(12, 24, 16, 24)));

        JLabel t = new JLabel("DESGLOSE DE INGREDIENTES DE LA RECETA");
        t.setFont(new Font("Arial", Font.BOLD, 11));
        t.setForeground(C_ACCENT);
        det.add(t);
        det.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 6));
        grid.setOpaque(false);

        for (Ingrediente ing : r.getIngredientes()) {
            JPanel cardIng = new JPanel(new BorderLayout());
            cardIng.setBackground(C_WHITE);
            cardIng.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_CAMPO_BOR, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));

            JLabel lblNom = new JLabel(ing.getInsumo().getNombre());
            lblNom.setFont(new Font("Arial", Font.BOLD, 12));

            JLabel lblCant = new JLabel(ing.getCantidad() + " " + ing.getUnidad());
            lblCant.setFont(new Font("Arial", Font.PLAIN, 12));
            lblCant.setForeground(C_MUTED);

            cardIng.add(lblNom, BorderLayout.WEST);
            cardIng.add(lblCant, BorderLayout.EAST);
            grid.add(cardIng);
        }

        det.add(grid);
        det.setMaximumSize(new Dimension(Integer.MAX_VALUE, det.getPreferredSize().height));
        return det;
    }

    private JPanel wrapInCell(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        p.setOpaque(false);
        p.add(comp);
        return p;
    }

    // ═══════════════════════════════════════════════
    // DIÁLOGOS MODALES
    // ═══════════════════════════════════════════════

    private void dlgReceta(Receta recetaEditar, List<Producto> productosDisponibles) {
        boolean esEd = (recetaEditar != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "", true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            }
        };
        main.setOpaque(false);
        main.setPreferredSize(new Dimension(540, 580));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel fTit = new JPanel(new BorderLayout()); fTit.setOpaque(false);
        JLabel tit = new JLabel(esEd ? "Editar receta" : "Crear receta");
        tit.setFont(new Font("Arial", Font.BOLD, 22)); tit.setForeground(C_ACCENT);
        JButton btnX = mkBtnX(); btnX.addActionListener(e -> dlg.dispose());
        fTit.add(tit, BorderLayout.WEST); fTit.add(btnX, BorderLayout.EAST);
        body.add(fTit);
        body.add(Box.createRigidArea(new Dimension(0, 8)));

        // CAMPO: Seleccionar Producto del Menú
        body.add(mkLblSec("PRODUCTO DEL MENÚ (OPCIONAL)"));
        body.add(Box.createRigidArea(new Dimension(0, 4)));

        JComboBox<Producto> cmbProd = new JComboBox<>();
        cmbProd.setFont(new Font("Arial", Font.PLAIN, 13));
        cmbProd.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Producto) {
                    Producto p = (Producto) value;
                    if (p.getId() == -1) {
                        setText("-- Sin producto (Crear receta) --");
                    } else {
                        setText("#" + p.getId() + " - " + p.getNombre());
                    }
                }
                return this;
            }
        });

        // Opción neutra
        cmbProd.addItem(new Producto(-1, "-- Sin producto (Crear receta) --", "—", 0.0, "Activo"));

        // Filtrar productos que ya tienen receta asignada
        Set<Integer> idsConReceta = recetas.stream().map(Receta::getProductoId).collect(Collectors.toSet());

        if (productosDisponibles != null) {
            for (Producto p : productosDisponibles) {
                if (esEd) {
                    cmbProd.addItem(p);
                } else if (!idsConReceta.contains(p.getId())) {
                    cmbProd.addItem(p);
                }
            }
        }

        JPanel wrapCmbProd = mkCampoWrap(cmbProd, 0, 40);
        body.add(wrapCmbProd);
        body.add(Box.createRigidArea(new Dimension(0, 10)));

        // CAMPO: Nombre de la receta
        body.add(mkLblSec("NOMBRE DE LA RECETA"));
        body.add(Box.createRigidArea(new Dimension(0, 4)));

        JTextField txtNom = new JTextField();
        JPanel campoNom = mkCampo(txtNom, "Ej. Agua de Limón", 44);
        body.add(campoNom);
        body.add(Box.createRigidArea(new Dimension(0, 14)));

        // Lógica Selección/Edición
        if (esEd) {
            txtNom.setText(recetaEditar.getNombre());
            txtNom.setForeground(new Color(0x333333));

            for (int i = 0; i < cmbProd.getItemCount(); i++) {
                if (cmbProd.getItemAt(i).getId().equals(recetaEditar.getProductoId())) {
                    cmbProd.setSelectedIndex(i);
                    break;
                }
            }
            cmbProd.setEnabled(false);
        } else {
            cmbProd.addActionListener(e -> {
                Producto sel = (Producto) cmbProd.getSelectedItem();
                if (sel != null && sel.getId() != -1) {
                    txtNom.setText(sel.getNombre());
                    txtNom.setForeground(new Color(0x333333));
                }
            });
        }

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
            JPanel enc = new JPanel(new GridLayout(1, 4, 8, 0)); enc.setOpaque(false);
            enc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            enc.add(mkLblG("Insumo")); enc.add(mkLblG("Cantidad")); enc.add(mkLblG("Unidad")); enc.add(mkLblG(""));
            panelIng.add(enc);
            panelIng.add(Box.createRigidArea(new Dimension(0, 4)));

            for (int i = 0; i < ingsActuales.size(); i++) {
                final int idx = i;
                Ingrediente ing = ingsActuales.get(i);

                JPanel fila = new JPanel(new BorderLayout(8, 0)); fila.setOpaque(false);
                fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                fila.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xDDD0C0), 1, true),
                        BorderFactory.createEmptyBorder(2, 8, 2, 8)));

                JPanel pInfo = new JPanel(new GridLayout(1, 3, 8, 0)); pInfo.setOpaque(false);
                pInfo.add(mkLblDat(ing.getInsumo().getNombre()));
                pInfo.add(mkLblDat(String.valueOf(ing.getCantidad())));
                pInfo.add(mkLblDat(ing.getUnidad()));

                JButton btnElimIng = new JButton("✕");
                btnElimIng.setFont(new Font("Arial", Font.BOLD, 11));
                btnElimIng.setForeground(C_BTN_RBOR);
                btnElimIng.setContentAreaFilled(false);
                btnElimIng.setBorderPainted(false);
                btnElimIng.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnElimIng.addActionListener(ev -> {
                    ingsActuales.remove(idx);
                    rb[0].run();
                });

                fila.add(pInfo, BorderLayout.CENTER);
                fila.add(btnElimIng, BorderLayout.EAST);

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

        JPanel fCosto = new JPanel(new BorderLayout()); fCosto.setBackground(new Color(0xFDF3E7));
        fCosto.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fCosto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        fCosto.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblCostoT = new JLabel("Costo estimado de insumos");
        lblCostoT.setFont(new Font("Arial", Font.BOLD, 12)); lblCostoT.setForeground(C_COSTO);
        fCosto.add(lblCostoT, BorderLayout.WEST); fCosto.add(lblCostoVal, BorderLayout.EAST);
        body.add(fCosto);
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel fBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); fBtns.setOpaque(false);
        fBtns.setAlignmentX(LEFT_ALIGNMENT); fBtns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton btnCan = mkBtnBorde("Cancelar"); btnCan.setPreferredSize(new Dimension(100, 36));
        btnCan.addActionListener(e -> dlg.dispose());

        JButton btnSav = mkBtnDark(esEd ? "Guardar cambios" : "Crear receta");
        btnSav.setPreferredSize(new Dimension(150, 36));
        btnSav.addActionListener(e -> {
            Producto prodSeleccionado = (Producto) cmbProd.getSelectedItem();
            String nom = txtNom.getText().trim();

            if (nom.isEmpty() || nom.equals("Ej. Agua de Limón")) {
                JOptionPane.showMessageDialog(dlg, "Debes ingresar el nombre de la receta.", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ingsActuales.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Agrega al menos un ingrediente.", "Campo Requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int pId = (prodSeleccionado != null && prodSeleccionado.getId() != -1) ? prodSeleccionado.getId() : 0;
            Receta rNueva = new Receta(esEd ? recetaEditar.getId() : 0, pId, nom, nom, 15, ingsActuales);

            guardarRecetaEnBD(rNueva, () -> {
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Receta guardada exitosamente.");
            });
        });

        fBtns.add(btnCan); fBtns.add(btnSav);
        body.add(fBtns);

        main.add(body, BorderLayout.CENTER);
        dlg.setContentPane(main);
        dlg.pack();
        dlg.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
    }

    private void dlgAgregarIng(JDialog padre, List<Ingrediente> ings, Runnable rebuild) {
        new SwingWorker<List<Insumo>, Void>() {
            @Override
            protected List<Insumo> doInBackground() throws Exception {
                List<Insumo> fresca = new ArrayList<>();
                String resInsumos = apiClient.get("/api/insumos");

                JSONArray jsonIns = resInsumos.trim().startsWith("[")
                        ? new JSONArray(resInsumos)
                        : new JSONObject(resInsumos).getJSONArray("data");

                for (int i = 0; i < jsonIns.length(); i++) {
                    JSONObject item = jsonIns.getJSONObject(i);
                    fresca.add(new Insumo(
                            item.getInt("id"),
                            item.getString("nombre"),
                            item.optString("categoria", "General"),
                            item.optString("unidad_medida", "pza"),
                            item.optDouble("costo_unitario", 0.0),
                            item.optDouble("stock_actual", 0.0),
                            item.optDouble("stock_minimo", 0.0)
                    ));
                }
                return fresca;
            }

            @Override
            protected void done() {
                try {
                    listaInsumosInventario.clear();
                    listaInsumosInventario.addAll(get());
                    mostrarModalAgregarIngrediente(padre, ings, rebuild);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(padre,
                            "Error al sincronizar catálogo de insumos: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void mostrarModalAgregarIngrediente(JDialog padre, List<Ingrediente> ings, Runnable rebuild) {
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

        JPanel fTit = new JPanel(new BorderLayout()); fTit.setOpaque(false);
        JLabel tit = new JLabel("Agregar ingrediente");
        tit.setFont(new Font("Arial", Font.BOLD, 18)); tit.setForeground(C_ACCENT);
        JButton bx = mkBtnX(); bx.addActionListener(e -> dlg2.dispose());
        fTit.add(tit, BorderLayout.WEST); fTit.add(bx, BorderLayout.EAST);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 14, 0); main.add(fTit, gc);

        main.add(mkLblSec("BUSCAR INSUMO EN BASE DE DATOS"), setGC(gc, 1, new Insets(0, 0, 4, 0)));
        JTextField txtBuscar = new JTextField();
        JPanel cBuscar = mkCampo(txtBuscar, "Escribe para filtrar...", 38);
        main.add(cBuscar, setGC(gc, 2, new Insets(0, 0, 8, 0)));

        DefaultComboBoxModel<Insumo> comboModel = new DefaultComboBoxModel<>();
        for (Insumo ins : listaInsumosInventario) comboModel.addElement(ins);
        JComboBox<Insumo> cmbInsumos = new JComboBox<>(comboModel);
        cmbInsumos.setFont(new Font("Arial", Font.PLAIN, 13));

        cmbInsumos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Insumo) {
                    setText(((Insumo) value).getNombre());
                }
                return this;
            }
        });

        JPanel wCmb = mkCampoWrap(cmbInsumos, 0, 42);
        main.add(wCmb, setGC(gc, 3, new Insets(0, 0, 12, 0)));

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

        cmbInsumos.addActionListener(e -> {
            Insumo sel = (Insumo) cmbInsumos.getSelectedItem();
            if (sel != null) {
                txtUnidad.setText(sel.getUnidadMedida());
                txtUnidad.setForeground(new Color(0x333333));
                lblInfoStock.setText(String.format("Precio: $%.2f/%s | Stock: %.2f %s",
                        sel.getCostoUnitario(), sel.getUnidadMedida(), sel.getStockActual(), sel.getUnidadMedida()));
            } else {
                txtUnidad.setText("Unidad");
                lblInfoStock.setText(" ");
            }
        });
        if (cmbInsumos.getItemCount() > 0) cmbInsumos.setSelectedIndex(0);

        JPanel fB = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); fB.setOpaque(false);
        JButton bCan = mkBtnBorde("Cancelar"); bCan.setPreferredSize(new Dimension(100, 36));
        bCan.addActionListener(e -> dlg2.dispose());

        JButton bAdd = mkBtnDark("Agregar"); bAdd.setPreferredSize(new Dimension(100, 36));
        bAdd.addActionListener(e -> {
            Insumo sel = (Insumo) cmbInsumos.getSelectedItem();
            if (sel == null) {
                JOptionPane.showMessageDialog(dlg2, "Selecciona un insumo.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String cantTxt = txtCantidad.getText().trim();
            if (cantTxt.isEmpty() || cantTxt.equals("Cantidad")) {
                JOptionPane.showMessageDialog(dlg2, "Ingresa la cantidad del ingrediente.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double cant = Double.parseDouble(cantTxt);
                if (cant <= 0) throw new NumberFormatException();
                ings.add(new Ingrediente(sel, cant));
                rebuild.run();
                dlg2.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg2, "La cantidad debe ser mayor a 0.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
            }
        });

        fB.add(bCan); fB.add(bAdd);
        main.add(fB, setGC(gc, 7, new Insets(0, 0, 0, 0)));

        dlg2.setContentPane(main);
        dlg2.pack();
        dlg2.setLocationRelativeTo(padre);
        dlg2.setVisible(true);
    }

    private JPanel buildAcciones(Receta r) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(false);

        JButton btnEdit = new JButton("Editar");
        btnEdit.setFont(new Font("Arial", Font.BOLD, 11));
        btnEdit.setForeground(C_ACCENT);
        btnEdit.setContentAreaFilled(false);
        btnEdit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_ACCENT, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> abrirDialogoReceta(r));

        JButton btnDel = new JButton("Eliminar");
        btnDel.setFont(new Font("Arial", Font.BOLD, 11));
        btnDel.setForeground(C_BTN_RBOR);
        btnDel.setContentAreaFilled(false);
        btnDel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BTN_RBOR, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de eliminar la receta \"" + r.getNombre() + "\"?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                eliminarRecetaDeBD(r);
                filaExp = -1;
            }
        });

        p.add(btnEdit);
        p.add(btnDel);
        return p;
    }

    private void filtrar() {
        String sel = (String) cmbFiltro.getSelectedItem();
        if (sel == null || sel.equals("Todos los productos")) {
            poblarTabla();
            return;
        }
        List<Receta> filtradas = recetas.stream()
                .filter(r -> r.getNombre().equalsIgnoreCase(sel) || r.getProductoVinculado().toLowerCase().contains(sel.toLowerCase()))
                .collect(Collectors.toList());
        poblarCon(filtradas);
    }

    // ═══════════════════════════════════════════════
    // HELPERS DE INTERFAZ
    // ═══════════════════════════════════════════════

    private GridBagConstraints setGC(GridBagConstraints gc, int y, Insets ins) {
        gc.gridy = y; gc.insets = ins; return gc;
    }

    private JLabel mkLblSec(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 11)); l.setForeground(C_COSTO);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel mkLblG(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 11)); l.setForeground(new Color(0x777777));
        return l;
    }

    private JLabel mkLblDat(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.PLAIN, 13)); l.setForeground(new Color(0x333333));
        return l;
    }

    private JLabel mkLblDatBold(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 13)); l.setForeground(new Color(0x222222));
        return l;
    }

    private JPanel mkBadgeId(String txt) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        p.setBackground(C_ID_BG);
        p.setBorder(BorderFactory.createLineBorder(C_ID_BOR, 1, true));
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Arial", Font.BOLD, 11)); l.setForeground(C_ID_BOR);
        p.add(l);
        return p;
    }

    private JPanel mkBadgeVin() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        p.setBackground(new Color(0xE8F5E9));
        p.setBorder(BorderFactory.createLineBorder(new Color(0x2E7D32), 1, true));
        JLabel l = new JLabel("Vinculado");
        l.setFont(new Font("Arial", Font.BOLD, 11)); l.setForeground(new Color(0x2E7D32));
        p.add(l);
        return p;
    }

    private JPanel mkBadgeSinVinculation() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        p.setBackground(new Color(0xFFF3E0));
        p.setBorder(BorderFactory.createLineBorder(new Color(0xE65100), 1, true));
        JLabel l = new JLabel("Sin vincular");
        l.setFont(new Font("Arial", Font.BOLD, 11)); l.setForeground(new Color(0xE65100));
        p.add(l);
        return p;
    }

    private JPanel mkChip(String txt) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_CAMPO_BOR, 1, true),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Arial", Font.PLAIN, 12)); l.setForeground(new Color(0x444444));
        p.add(l);
        return p;
    }

    private JPanel mkCampo(JTextField txt, String ph, int alto) {
        txt.setFont(new Font("Arial", Font.PLAIN, 13));
        txt.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        txt.setBackground(C_CAMPO_BG); txt.setOpaque(false);

        if (txt.getText().isEmpty()) {
            txt.setText(ph); txt.setForeground(new Color(0xAAAAAA));
        }

        txt.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txt.getText().equals(ph)) {
                    txt.setText(""); txt.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txt.getText().trim().isEmpty()) {
                    txt.setText(ph); txt.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        JPanel w = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
        };
        w.setOpaque(false);
        w.setPreferredSize(new Dimension(0, alto));
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        w.setAlignmentX(LEFT_ALIGNMENT);
        w.add(txt, BorderLayout.CENTER);
        return w;
    }

    private JPanel mkCampoWrap(JComponent c, int ancho, int alto) {
        JPanel w = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(C_CAMPO_BOR); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
        };
        w.setOpaque(false);
        if (ancho > 0) w.setPreferredSize(new Dimension(ancho, alto));
        else w.setPreferredSize(new Dimension(0, alto));
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, alto));
        w.setAlignmentX(LEFT_ALIGNMENT);
        c.setOpaque(false);
        w.add(c, BorderLayout.CENTER);
        return w;
    }

    private JButton mkBtnDark(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setForeground(C_WHITE); b.setBackground(C_BTN_DARK);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton mkBtnBorde(String txt) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setForeground(new Color(0x555555));
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createLineBorder(C_CAMPO_BOR, 1, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton mkBtnX() {
        JButton b = new JButton("✕");
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setForeground(new Color(0x888888));
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String fmtCosto(List<Ingrediente> ings) {
        double total = ings.stream().mapToDouble(Ingrediente::subtotal).sum();
        return String.format("$%.2f", total);
    }
}