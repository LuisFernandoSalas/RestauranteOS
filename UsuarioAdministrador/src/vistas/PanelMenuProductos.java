package vistas;

import modelos.Producto;
import servicios.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PanelMenuProductos extends JPanel implements Actualizables {

    @Override
    public void recargarDatos() {
        // Al presionar el botón en el menú lateral, esto volverá a hacer la petición HTTP
        cargarProductosDesdeBackend();
        cargarRecetasDesdeBackend();
    }

    // ─── COLORES ───────────────────────────────────
    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_CAMPO_BG  = new Color(0xEEE8DE);
    private static final Color C_CAMPO_BOR = new Color(0xD4C4A8);
    private static final Color C_BTN_DARK  = new Color(0x4A2010);

    private static final Color C_TBL_HDR   = new Color(0x7A3520);
    private static final Color C_TBL_HDR_T = Color.WHITE;

    private static final Color C_CAT_TEXT  = new Color(0xA05020);
    private static final Color C_ID_TEXT   = new Color(0xD48000);
    private static final Color C_ACT_TEXT  = new Color(0xD48000);
    private static final Color C_PAU_TEXT  = new Color(0x888888);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_ALT_ROW   = new Color(0xFAF4EE);
    private static final Color C_DIV_LINE  = new Color(0x8B2500);

    // ─── SERVICIO Y DATOS ──────────────────────────
    private final ApiClient apiClient = new ApiClient();
    private final List<Producto> productos = new ArrayList<>();

    // ─── COMPONENTES DEL FORMULARIO ────────────────
    private JTextField        txtNombre, txtPrecio, txtDescripcion;
    private JComboBox<String> cmbReceta, cmbCategoria;
    private JPanel            panelTabla;

    private Runnable listenerNavegarRecetas;

    // ─── CONSTRUCTOR ───────────────────────────────
    public PanelMenuProductos() {
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

        // Escuchador para refrescar automáticamente al mostrar/cambiar a esta pestaña
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                cargarProductosDesdeBackend();
                cargarRecetasDesdeBackend();
            }
        });

        // Cargar datos al iniciar
        cargarProductosDesdeBackend();
        cargarRecetasDesdeBackend();
    }

    public void setListenerNavegarRecetas(Runnable listener) {
        this.listenerNavegarRecetas = listener;
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        // Recargar productos Y recetas al regresar a esta vista
        if (aFlag) {
            cargarProductosDesdeBackend();
            cargarRecetasDesdeBackend();
        }
    }

    // ═══════════════════════════════════════════════
    // NORMALIZACIÓN DE CATEGORÍAS
    // ═══════════════════════════════════════════════
    private String normalizarCategoria(String cat) {
        if (cat == null) return "—";
        String c = cat.trim();
        if (c.equalsIgnoreCase("Bebida") || c.equalsIgnoreCase("Bebidas")) return "Bebidas";
        if (c.equalsIgnoreCase("Entrada") || c.equalsIgnoreCase("Entradas")) return "Entradas";
        if (c.equalsIgnoreCase("Plato fuerte") || c.equalsIgnoreCase("Platos fuertes")) return "Platos fuertes";
        if (c.equalsIgnoreCase("Postre") || c.equalsIgnoreCase("Postres")) return "Postres";
        if (c.equalsIgnoreCase("Combo") || c.equalsIgnoreCase("Combos")) return "Combos";
        if (c.equalsIgnoreCase("Categoria") || c.equalsIgnoreCase("Categoría") || c.isEmpty()) return "—";
        return c;
    }

    // ═══════════════════════════════════════════════
    // CONEXIÓN CON BACKEND LARAVEL
    // ═══════════════════════════════════════════════
    public void cargarProductosDesdeBackend() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiClient.obtenerProductos();
            }

            @Override
            protected void done() {
                try {
                    String json = get();
                    parsearYActualizarProductos(json);
                } catch (Exception e) {
                    System.err.println("Error al cargar productos desde Laravel: " + e.getMessage());
                    inicializarDummy();
                }
            }
        };
        worker.execute();
    }

    public void cargarRecetasDesdeBackend() {
        SwingWorker<Set<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected Set<String> doInBackground() throws Exception {
                String jsonRes = apiClient.obtenerRecetas();
                Set<String> recetasUnicas = new LinkedHashSet<>();

                if (jsonRes != null && !jsonRes.trim().isEmpty()) {
                    JSONArray arreglo = null;

                    if (jsonRes.trim().startsWith("{")) {
                        JSONObject obj = new JSONObject(jsonRes);
                        if (obj.has("data") && !obj.isNull("data")) {
                            arreglo = obj.getJSONArray("data");
                        }
                    } else if (jsonRes.trim().startsWith("[")) {
                        arreglo = new JSONArray(jsonRes);
                    }

                    if (arreglo != null) {
                        for (int i = 0; i < arreglo.length(); i++) {
                            Object elem = arreglo.get(i);
                            String nombre = "";

                            if (elem instanceof JSONObject) {
                                JSONObject item = (JSONObject) elem;

                                // 1. Intentar leer nombre directo en la raíz
                                if (item.has("nombre") && !item.isNull("nombre")) {
                                    nombre = item.optString("nombre");
                                }
                                // 2. Si no tiene 'nombre', buscar dentro del objeto 'producto'
                                else if (item.has("producto") && !item.isNull("producto")) {
                                    JSONObject prodObj = item.optJSONObject("producto");
                                    if (prodObj != null) {
                                        nombre = prodObj.optString("nombre", prodObj.optString("name", ""));
                                    }
                                }
                                // 3. Si tampoco, buscar dentro del objeto 'insumo'
                                else if (item.has("insumo") && !item.isNull("insumo")) {
                                    JSONObject insObj = item.optJSONObject("insumo");
                                    if (insObj != null) {
                                        nombre = insObj.optString("nombre", insObj.optString("name", ""));
                                    }
                                }
                            } else if (elem instanceof String) {
                                nombre = (String) elem;
                            }

                            // Agregar al Set (se ignoran duplicados)
                            if (nombre != null && !nombre.trim().isEmpty()) {
                                recetasUnicas.add(nombre.trim());
                            }
                        }
                    }
                }
                return recetasUnicas;
            }

            @Override
            protected void done() {
                try {
                    Set<String> recetas = get();
                    Object itemSeleccionado = cmbReceta.getSelectedItem();

                    cmbReceta.removeAllItems();
                    cmbReceta.addItem("Receta");

                    for (String rec : recetas) {
                        cmbReceta.addItem(rec);
                    }

                    if (itemSeleccionado != null && recetas.contains(itemSeleccionado.toString())) {
                        cmbReceta.setSelectedItem(itemSeleccionado);
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar combo de recetas: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void parsearYActualizarProductos(String jsonStr) {
        productos.clear();
        try {
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                JSONArray arreglo;
                if (jsonStr.trim().startsWith("{")) {
                    JSONObject obj = new JSONObject(jsonStr);
                    arreglo = obj.optJSONArray("data");
                } else {
                    arreglo = new JSONArray(jsonStr);
                }

                if (arreglo != null) {
                    for (int i = 0; i < arreglo.length(); i++) {
                        JSONObject item = arreglo.getJSONObject(i);
                        int id = item.optInt("id", 0);
                        String nombre = item.optString("nombre", item.optString("name", "Sin Nombre"));

                        // ── LECTURA Y DESMAPEO DE CATEGORÍA ──
                        String cat = "—";

                        // 1. Buscar si viene en los campos de ID
                        if (item.has("categoria_id") && !item.isNull("categoria_id")) {
                            cat = desmapearCategoriaPorId(item.optInt("categoria_id"));
                        } else if (item.has("category_id") && !item.isNull("category_id")) {
                            cat = desmapearCategoriaPorId(item.optInt("category_id"));
                        }
                        // 2. Si no, analizar el objeto o valor en 'categoria'
                        else if (item.has("categoria") && !item.isNull("categoria")) {
                            Object catObj = item.get("categoria");
                            if (catObj instanceof JSONObject) {
                                JSONObject catJson = (JSONObject) catObj;
                                cat = catJson.optString("nombre", catJson.optString("name", "—"));
                            } else {
                                String str = catObj.toString().trim();
                                // Si Laravel devuelve un ID numérico en texto (ej. "3")
                                if (str.matches("\\d+")) {
                                    cat = desmapearCategoriaPorId(Integer.parseInt(str));
                                } else {
                                    cat = str;
                                }
                            }
                        }
                        // 3. Analizar 'category'
                        else if (item.has("category") && !item.isNull("category")) {
                            Object catObj = item.get("category");
                            if (catObj instanceof JSONObject) {
                                JSONObject catJson = (JSONObject) catObj;
                                cat = catJson.optString("nombre", catJson.optString("name", "—"));
                            } else {
                                String str = catObj.toString().trim();
                                if (str.matches("\\d+")) {
                                    cat = desmapearCategoriaPorId(Integer.parseInt(str));
                                } else {
                                    cat = str;
                                }
                            }
                        }
                        // 4. Analizar 'categoria_nombre'
                        else if (item.has("categoria_nombre") && !item.isNull("categoria_nombre")) {
                            cat = item.optString("categoria_nombre");
                        }

                        cat = normalizarCategoria(cat);
                        double precio = item.optDouble("precio", item.optDouble("price", 0.0));

                        // ── DETECCIÓN ESTRICTA DE ESTADO Y DISPONIBILIDAD ──
                        boolean disponible = true;
                        if (item.has("is_disponible") && !item.isNull("is_disponible")) {
                            disponible = item.optBoolean("is_disponible", true);
                        } else if (item.has("disponible") && !item.isNull("disponible")) {
                            disponible = item.optBoolean("disponible", true);
                        }

                        String estadoRaw = item.optString("estado", item.optString("status", "")).toLowerCase();
                        boolean tienePausadoHasta = item.has("pausado_hasta") && !item.isNull("pausado_hasta") && !item.optString("pausado_hasta").isEmpty();

                        String estado = "Activo";
                        if (!disponible || tienePausadoHasta || estadoRaw.contains("paus") || estadoRaw.contains("inactiv") || estadoRaw.equals("false") || estadoRaw.equals("0")) {
                            estado = "Pausado";
                        }

                        productos.add(new Producto(id, nombre, cat, precio, estado));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parseando productos JSON: " + e.getMessage());
        }

        if (productos.isEmpty()) {
            inicializarDummy();
        } else {
            poblarTabla();
        }
    }

    private String desmapearCategoriaPorId(int id) {
        switch (id) {
            case 1: return "Platos fuertes";
            case 2: return "Entradas";
            case 3: return "Bebidas";
            case 4: return "Postres";
            case 5: return "Combos";
            default: return "Platos fuertes";
        }
    }

    // ═══════════════════════════════════════════════
    // CONTENIDO PRINCIPAL Y VISTA
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

        gc.gridy=0; gc.insets=new Insets(0,0,20,0);
        p.add(buildTituloPrincipal(), gc);

        gc.gridy=1; gc.insets=new Insets(0,0,12,0);
        p.add(mkSubtitulo("Crear producto"), gc);

        // Fila 1: Nombre | Precio
        JPanel f1 = new JPanel(new GridLayout(1,2,14,0));
        f1.setOpaque(false);
        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        f1.add(buildCampo(txtNombre, "Nombre del producto"));
        f1.add(buildCampo(txtPrecio, "Precio ($)"));
        gc.gridy=2; gc.insets=new Insets(0,0,10,0);
        p.add(f1, gc);

        // Fila 2: Receta | Categoria
        JPanel f2 = new JPanel(new GridLayout(1,2,14,0));
        f2.setOpaque(false);
        cmbReceta    = mkCombo(new String[]{"Receta"});
        cmbCategoria = mkCombo(new String[]{"Categoria", "Platos fuertes", "Entradas", "Bebidas", "Postres", "Combos"});

        cmbReceta.addActionListener(e -> {
            String recetaSel = (String) cmbReceta.getSelectedItem();
            if (recetaSel != null && !recetaSel.equals("Receta")) {
                String nomAct = txtNombre.getText().trim();
                if (nomAct.isEmpty() || nomAct.equals("Nombre del producto")) {
                    txtNombre.setText(recetaSel);
                    txtNombre.setForeground(new Color(0x333333));
                }
            }
        });

        f2.add(buildComboWrap(cmbReceta));
        f2.add(buildComboWrap(cmbCategoria));
        gc.gridy=3; gc.insets=new Insets(0,0,10,0);
        p.add(f2, gc);

        // Fila 3: Descripcion
        txtDescripcion = new JTextField();
        JPanel f3 = buildCampo(txtDescripcion, "Descripcion del producto (opcional)");
        gc.gridy=4; gc.insets=new Insets(0,0,12,0);
        p.add(f3, gc);

        // Fila 4: Botones
        JPanel f4 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        f4.setOpaque(false);
        JButton btnRec = buildBoton("+ Agregar Receta");
        JButton btnAdd = buildBoton("+ Agregar");

        btnRec.addActionListener(e -> onAgregarReceta());
        btnAdd.addActionListener(e -> onAgregarProducto());

        f4.add(btnRec); f4.add(btnAdd);
        gc.gridy=5; gc.insets=new Insets(0,0,28,0);
        p.add(f4, gc);

        gc.gridy=6; gc.insets=new Insets(0,0,10,0);
        p.add(mkSubtitulo("Productos registrados"), gc);

        panelTabla = new JPanel(new GridBagLayout());
        panelTabla.setOpaque(false);
        gc.gridy=7; gc.insets=new Insets(0,0,0,0);
        p.add(panelTabla, gc);

        gc.gridy=8; gc.weighty=1.0; gc.fill=GridBagConstraints.BOTH;
        JPanel sp = new JPanel(); sp.setOpaque(false);
        p.add(sp, gc);

        poblarTabla();
        return p;
    }

    private JPanel buildTituloPrincipal() {
        JLabel lbl = new JLabel("Menu y Productos");
        lbl.setFont(new Font("Arial", Font.BOLD, 30));
        lbl.setForeground(C_ACCENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV_LINE);
        sep.setBackground(C_DIV_LINE);

        JPanel p = new JPanel(new BorderLayout(0,8));
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

        String ph = placeholder;
        field.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e){
                if(field.getText().equals(ph)){
                    field.setText("");
                    field.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e){
                if(field.getText().trim().isEmpty()){
                    field.setText(ph);
                    field.setForeground(new Color(0xAAAAAA));
                }
            }
        });

        JPanel wrap = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(C_CAMPO_BOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
            }
        };
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(0,48));
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JComboBox<String> mkCombo(String[] items){
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Arial",Font.PLAIN,14));
        c.setForeground(new Color(0xAAAAAA));
        c.setBackground(C_CAMPO_BG);
        c.setBorder(BorderFactory.createEmptyBorder());
        return c;
    }

    private JPanel buildComboWrap(JComboBox<String> combo){
        JPanel wrap = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(C_CAMPO_BOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
            }
        };
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(0,48));
        combo.setOpaque(false);
        wrap.add(combo, BorderLayout.CENTER);
        return wrap;
    }

    private JButton buildBoton(String texto){
        JButton btn = new JButton(texto){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BTN_DARK);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(C_WHITE);
                g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial",Font.BOLD,13));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(170,40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════
    // TABLA
    // ═══════════════════════════════════════════════
    private static final double[] PW = {0.08, 0.28, 0.20, 0.12, 0.12, 0.20};
    private static final String[] COLS = {"ID", "Nombre producto", "Categoria", "Precio", "Estado", "Acciones"};

    private void poblarTabla() {
        panelTabla.removeAll();

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.WEST;

        // Encabezados
        g.gridy = 0;
        for (int i = 0; i < COLS.length; i++) {
            g.gridx = i;
            g.weightx = PW[i];

            JPanel cellHdr = new JPanel(new BorderLayout());
            cellHdr.setBackground(C_TBL_HDR);
            cellHdr.setPreferredSize(new Dimension(0, 42));
            cellHdr.setBorder(BorderFactory.createEmptyBorder(0, i == 0 ? 16 : 8, 0, 8));

            JLabel l = new JLabel(COLS[i], SwingConstants.LEFT);
            l.setFont(new Font("Arial", Font.BOLD, 13));
            l.setForeground(C_TBL_HDR_T);

            cellHdr.add(l, BorderLayout.WEST);
            panelTabla.add(cellHdr, g);
        }

        // Filas
        for (int row = 0; row < productos.size(); row++) {
            Producto p = productos.get(row);
            Color bgRow = (row % 2 == 0) ? C_WHITE : C_ALT_ROW;
            int currentGridY = (row * 2) + 1;

            g.gridy = currentGridY;

            String formattedId = String.format("#%03d", p.getId() != null ? p.getId() : (row + 1));
            String formattedPrecio = String.format("$%.2f", p.getPrecio());

            g.gridx = 0; g.weightx = PW[0];
            panelTabla.add(buildCeldaTexto(formattedId, C_ID_TEXT, true, bgRow, true), g);

            g.gridx = 1; g.weightx = PW[1];
            panelTabla.add(buildCeldaTexto(p.getNombre(), new Color(0x333333), false, bgRow, false), g);

            g.gridx = 2; g.weightx = PW[2];
            panelTabla.add(buildCeldaTexto(p.getCategoria(), C_CAT_TEXT, false, bgRow, false), g);

            g.gridx = 3; g.weightx = PW[3];
            panelTabla.add(buildCeldaTexto(formattedPrecio, new Color(0x333333), false, bgRow, false), g);

            g.gridx = 4; g.weightx = PW[4];
            boolean activo = p.getEstado().equalsIgnoreCase("Activo");
            panelTabla.add(buildCeldaTexto(p.getEstado(), activo ? C_ACT_TEXT : C_PAU_TEXT, false, bgRow, false), g);

            g.gridx = 5; g.weightx = PW[5];
            panelTabla.add(buildCeldaAcciones(p, bgRow), g);

            g.gridy = currentGridY + 1;
            g.gridx = 0;
            g.gridwidth = COLS.length;
            g.weightx = 1.0;

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0xEEDDCC));
            panelTabla.add(sep, g);

            g.gridwidth = 1;
        }

        panelTabla.revalidate();
        panelTabla.repaint();
    }

    private JPanel buildCeldaTexto(String texto, Color color, boolean bold, Color bg, boolean esPrimeraColumna) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setPreferredSize(new Dimension(0, 48));
        p.setBorder(BorderFactory.createEmptyBorder(0, esPrimeraColumna ? 16 : 8, 0, 8));

        JLabel l = new JLabel(texto, SwingConstants.LEFT);
        l.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(color);

        p.add(l, BorderLayout.WEST);
        return p;
    }

    private JPanel buildCeldaAcciones(Producto prod, Color bg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel editar = new JLabel("Editar");
        editar.setFont(new Font("Arial", Font.PLAIN, 13));
        editar.setForeground(C_ACCENT);
        editar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editar.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ onEditar(prod); }
        });

        JLabel pipe = new JLabel("|");
        pipe.setFont(new Font("Arial", Font.PLAIN, 13));
        pipe.setForeground(new Color(0xCCCCCC));

        JLabel eliminar = new JLabel("Eliminar");
        eliminar.setFont(new Font("Arial", Font.PLAIN, 13));
        eliminar.setForeground(new Color(0xC03020));
        eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eliminar.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ onEliminar(prod); }
        });

        p.add(editar);
        p.add(pipe);
        p.add(eliminar);

        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(bg);
        cell.setPreferredSize(new Dimension(140, 48));
        cell.setMinimumSize(new Dimension(140, 48));
        cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JPanel wrapperVertical = new JPanel(new GridBagLayout());
        wrapperVertical.setOpaque(false);
        wrapperVertical.add(p);

        cell.add(wrapperVertical, BorderLayout.WEST);
        return cell;
    }

    // ═══════════════════════════════════════════════
    // ACCIONES
    // ═══════════════════════════════════════════════
    private void onAgregarProducto() {
        String nom = txtNombre.getText().trim();
        String pre = txtPrecio.getText().trim();
        String desc = txtDescripcion.getText().trim();
        String ph1 = "Nombre del producto", ph2 = "Precio ($)", ph3 = "Descripcion del producto (opcional)";

        if (nom.isEmpty() || nom.equals(ph1)) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre del producto.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pre.isEmpty() || pre.equals(ph2)) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el precio del producto.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String preLimpio = pre.replaceAll("[^0-9.]", "");
        double precioNum;
        try {
            if (preLimpio.isEmpty()) throw new NumberFormatException();
            precioNum = Double.parseDouble(preLimpio);
            if (precioNum <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número mayor a cero.", "Formato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String catRaw = (String) cmbCategoria.getSelectedItem();
        if (catRaw == null || catRaw.equalsIgnoreCase("Categoria") || catRaw.equalsIgnoreCase("Categoría")) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una categoría válida.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cat = normalizarCategoria(catRaw);
        String descFinal = desc.equals(ph3) ? "" : desc;

        Producto nuevoProducto = new Producto(nom, cat, precioNum, descFinal, "Activo");

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                apiClient.crearProducto(nuevoProducto);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    // Refrescar productos y recetas para actualizar vinculaciones
                    cargarProductosDesdeBackend();
                    cargarRecetasDesdeBackend();

                    txtNombre.setText(ph1); txtNombre.setForeground(new Color(0xAAAAAA));
                    txtPrecio.setText(ph2); txtPrecio.setForeground(new Color(0xAAAAAA));
                    txtDescripcion.setText(ph3); txtDescripcion.setForeground(new Color(0xAAAAAA));
                    cmbReceta.setSelectedIndex(0); cmbCategoria.setSelectedIndex(0);

                    JOptionPane.showMessageDialog(PanelMenuProductos.this, "Producto \"" + nom + "\" agregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelMenuProductos.this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void onAgregarReceta() {
        if (listenerNavegarRecetas != null) {
            listenerNavegarRecetas.run();
        }
    }

    private void onEditar(Producto prod) {
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topWindow instanceof Frame ? (Frame) topWindow : null, "Editar Producto", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.weightx = 1.0; g.gridx = 0;

        String formattedId = String.format("#%03d", prod.getId() != null ? prod.getId() : 0);
        JLabel lblTitulo = new JLabel("Editar Producto " + formattedId);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(C_ACCENT);
        g.gridy = 0; p.add(lblTitulo, g);

        // Campo Nombre
        g.gridy++; p.add(mkLabelForm("Nombre del producto:"), g);
        JTextField txtEditNom = new JTextField(prod.getNombre());
        styleCampoDialog(txtEditNom);
        g.gridy++; p.add(txtEditNom, g);

        // Campo Categoría
        g.gridy++; p.add(mkLabelForm("Categoría:"), g);
        JComboBox<String> cmbEditCat = new JComboBox<>(new String[]{"Platos fuertes", "Entradas", "Bebidas", "Postres", "Combos"});

        // Seleccionar la categoría actual normalizada
        String catActual = normalizarCategoria(prod.getCategoria());
        cmbEditCat.setSelectedItem(catActual);
        cmbEditCat.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditCat.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditCat, g);

        // Campo Precio
        g.gridy++; p.add(mkLabelForm("Precio ($):"), g);
        JTextField txtEditPrecio = new JTextField(String.format("%.2f", prod.getPrecio()));
        styleCampoDialog(txtEditPrecio);
        g.gridy++; p.add(txtEditPrecio, g);

        // Campo Estado
        g.gridy++; p.add(mkLabelForm("Estado:"), g);
        JComboBox<String> cmbEditEst = new JComboBox<>(new String[]{"Activo", "Pausado"});
        cmbEditEst.setSelectedItem(prod.getEstado());
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

        JButton btnGuardar = buildBoton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(150, 38));
        btnGuardar.addActionListener(e -> {
            String nom = txtEditNom.getText().trim();
            String pre = txtEditPrecio.getText().trim();
            String catSeleccionada = (String) cmbEditCat.getSelectedItem();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El nombre del producto no puede estar vacío.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (pre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El precio no puede estar vacío.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String preLimpio = pre.replaceAll("[^0-9.]", "");
            double precioNum;
            try {
                if (preLimpio.isEmpty()) throw new NumberFormatException();
                precioNum = Double.parseDouble(preLimpio);
                if (precioNum <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de precio inválido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Asignación explícita de valores al objeto
            prod.setNombre(nom);
            prod.setCategoria(normalizarCategoria(catSeleccionada));
            prod.setPrecio(precioNum);
            prod.setEstado((String) cmbEditEst.getSelectedItem());

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    if (prod.getId() != null) {
                        apiClient.actualizarProducto(prod);
                    }
                    return null;
                }

                @Override protected void done() {
                    try {
                        get();
                        // Refrescar desde el servidor para confirmar persistencia
                        cargarProductosDesdeBackend();
                        cargarRecetasDesdeBackend();

                        dialog.dispose();
                        JOptionPane.showMessageDialog(PanelMenuProductos.this,
                                "Producto \"" + nom + "\" actualizado correctamente.",
                                "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Error al actualizar en backend:\n" + ex.getMessage(),
                                "Error de API", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        panelBtns.add(btnCancelar);
        panelBtns.add(btnGuardar);

        g.gridy++;
        g.insets = new Insets(16, 0, 0, 0);
        p.add(panelBtns, g);

        dialog.add(p);
        dialog.pack();
        dialog.setSize(380, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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

    private void onEliminar(Producto prod) {
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + prod.getNombre() + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    if (prod.getId() != null) {
                        apiClient.eliminarProducto(prod.getId());
                    }
                    return null;
                }

                @Override protected void done() {
                    try {
                        get();
                        // Refrescar productos y recetas al eliminar
                        cargarProductosDesdeBackend();
                        cargarRecetasDesdeBackend();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelMenuProductos.this,
                                "Error al eliminar el producto:\n" + ex.getMessage(),
                                "Error de API", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void inicializarDummy() {
        productos.clear();
        productos.add(new Producto(1, "Enchiladas verdes", "Platos fuertes", 85.00, "Activo"));
        productos.add(new Producto(2, "Pozole rojo",       "Platos fuertes", 95.00, "Activo"));
        productos.add(new Producto(3, "Tostadas de pata",  "Entradas",      45.00, "Pausado"));
        productos.add(new Producto(4, "Agua de Jamaica",   "Bebidas",       20.00, "Activo"));
        poblarTabla();
    }

    public void setCombos(String[] recetas, String[] cats) {
        cmbReceta.removeAllItems(); cmbReceta.addItem("Receta");
        for (String r : recetas) cmbReceta.addItem(r);
        cmbCategoria.removeAllItems(); cmbCategoria.addItem("Categoria");
        for (String c : cats) cmbCategoria.addItem(normalizarCategoria(c));
    }

}