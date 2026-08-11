package vistas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista: PanelMenuProductos — Menu y Productos
 */
public class PanelMenuProductos extends JPanel {

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

    // ─── MODELO ────────────────────────────────────
    static class Producto {
        String id, nombre, categoria, precio, estado;
        Producto(String id, String n, String c, String p, String e){
            this.id = id; nombre = n; categoria = c; precio = p; estado = e;
        }
    }

    // ─── DATOS DUMMY ───────────────────────────────
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

    public void setListenerNavegarRecetas(Runnable listener) {
        this.listenerNavegarRecetas = listener;
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

        // ── Titulo principal ──
        gc.gridy=0; gc.insets=new Insets(0,0,20,0);
        p.add(buildTituloPrincipal(), gc);

        // ── Subtitulo "Crear producto" ──
        gc.gridy=1; gc.insets=new Insets(0,0,12,0);
        p.add(mkSubtitulo("Crear producto"), gc);

        // ── Fila 1: Nombre | Precio ──
        JPanel f1 = new JPanel(new GridLayout(1,2,14,0));
        f1.setOpaque(false);
        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        f1.add(buildCampo(txtNombre, "Nombre del producto"));
        f1.add(buildCampo(txtPrecio, "Precio ($)"));
        gc.gridy=2; gc.insets=new Insets(0,0,10,0);
        p.add(f1, gc);

        // ── Fila 2: Receta | Categoria ──
        JPanel f2 = new JPanel(new GridLayout(1,2,14,0));
        f2.setOpaque(false);
        cmbReceta    = mkCombo(new String[]{"Receta","Enchiladas verdes","Pozole rojo"});
        cmbCategoria = mkCombo(new String[]{"Categoria","Plato fuerte","Entrada","Bebida","Combo"});
        f2.add(buildComboWrap(cmbReceta));
        f2.add(buildComboWrap(cmbCategoria));
        gc.gridy=3; gc.insets=new Insets(0,0,10,0);
        p.add(f2, gc);

        // ── Fila 3: Descripcion ──
        txtDescripcion = new JTextField();
        JPanel f3 = buildCampo(txtDescripcion, "Descripcion del producto (opcional)");
        gc.gridy=4; gc.insets=new Insets(0,0,12,0);
        p.add(f3, gc);

        // ── Fila 4: Botones ──
        JPanel f4 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        f4.setOpaque(false);
        JButton btnRec = buildBoton("+ Agregar Receta");
        JButton btnAdd = buildBoton("+ Agregar");

        btnRec.addActionListener(e -> onAgregarReceta());
        btnAdd.addActionListener(e -> onAgregarProducto());

        f4.add(btnRec); f4.add(btnAdd);
        gc.gridy=5; gc.insets=new Insets(0,0,28,0);
        p.add(f4, gc);

        // ── Subtitulo "Productos registrados" ──
        gc.gridy=6; gc.insets=new Insets(0,0,10,0);
        p.add(mkSubtitulo("Productos registrados"), gc);

        // ── Tabla Contenedora Única ──
        panelTabla = new JPanel(new GridBagLayout());
        panelTabla.setOpaque(false);
        gc.gridy=7; gc.insets=new Insets(0,0,0,0);
        p.add(panelTabla, gc);

        // Relleno inferior
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
    // CONSTRUCCIÓN DE LA TABLA MATRICIAL PERFECTA
    // ═══════════════════════════════════════════════
    // Se ajustaron las proporciones: Acciones ahora tiene 0.20 de espacio garantizado
    private static final double[] PW = {0.08, 0.28, 0.20, 0.12, 0.12, 0.20};
    private static final String[] COLS = {"ID", "Nombre producto", "Categoria", "Precio", "Estado", "Acciones"};

    private void poblarTabla() {
        panelTabla.removeAll();

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.WEST;

        // 1. DIBUJAR ENCABEZADO
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

        // 2. DIBUJAR FILAS
        for (int row = 0; row < productos.size(); row++) {
            Producto p = productos.get(row);
            Color bgRow = (row % 2 == 0) ? C_WHITE : C_ALT_ROW;
            int currentGridY = (row * 2) + 1;

            g.gridy = currentGridY;

            // ID
            g.gridx = 0; g.weightx = PW[0];
            panelTabla.add(buildCeldaTexto(p.id, C_ID_TEXT, true, bgRow, true), g);

            // Nombre
            g.gridx = 1; g.weightx = PW[1];
            panelTabla.add(buildCeldaTexto(p.nombre, new Color(0x333333), false, bgRow, false), g);

            // Categoria
            g.gridx = 2; g.weightx = PW[2];
            panelTabla.add(buildCeldaTexto(p.categoria, C_CAT_TEXT, false, bgRow, false), g);

            // Precio
            g.gridx = 3; g.weightx = PW[3];
            panelTabla.add(buildCeldaTexto(p.precio, new Color(0x333333), false, bgRow, false), g);

            // Estado
            g.gridx = 4; g.weightx = PW[4];
            boolean activo = p.estado.equalsIgnoreCase("Activo");
            panelTabla.add(buildCeldaTexto(p.estado, activo ? C_ACT_TEXT : C_PAU_TEXT, false, bgRow, false), g);

            // Acciones
            g.gridx = 5; g.weightx = PW[5];
            panelTabla.add(buildCeldaAcciones(p, bgRow), g);

            // Separador horizontal
            g.gridy = currentGridY + 1;
            g.gridx = 0;
            g.gridwidth = COLS.length;
            g.weightx = 1.0;

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0xEEDDCC));
            panelTabla.add(sep, g);

            g.gridwidth = 1; // Restaurar gridwidth por defecto
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

        // Contenedor centrado verticalmente
        JPanel wrapperVertical = new JPanel(new GridBagLayout());
        wrapperVertical.setOpaque(false);
        wrapperVertical.add(p);

        cell.add(wrapperVertical, BorderLayout.WEST);
        return cell;
    }

    // ═══════════════════════════════════════════════
    // ACCIONES Y DIÁLOGO EMERGENTE DE EDICIÓN
    // ═══════════════════════════════════════════════
    private void onAgregarProducto(){
        String nom = txtNombre.getText().trim();
        String pre = txtPrecio.getText().trim();
        String ph1 = "Nombre del producto", ph2 = "Precio ($)";

        // ── Validar nombre ──
        if (nom.isEmpty() || nom.equals(ph1)) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo agregar: debes ingresar el nombre del producto.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar que el precio no esté vacío ──
        if (pre.isEmpty() || pre.equals(ph2)) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo agregar: debes ingresar el precio del producto.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar que el precio sea un número válido ──
        String preLimpio = pre.replaceAll("[^0-9.]", "");
        double precioNum;
        try {
            if (preLimpio.isEmpty()) throw new NumberFormatException();
            precioNum = Double.parseDouble(preLimpio);
            if (precioNum <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El precio debe ser un número válido mayor a cero (ej. 85.00).",
                    "Formato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String cat = (String) cmbCategoria.getSelectedItem();
        if("Categoria".equals(cat)) cat = "—";

        productos.add(new Producto(
                String.format("#%03d", productos.size() + 1), nom, cat,
                "$" + String.format("%.2f", precioNum), "Activo"));
        poblarTabla();

        txtNombre.setText(ph1); txtNombre.setForeground(new Color(0xAAAAAA));
        txtPrecio.setText(ph2); txtPrecio.setForeground(new Color(0xAAAAAA));
        txtDescripcion.setText("Descripcion del producto (opcional)");
        txtDescripcion.setForeground(new Color(0xAAAAAA));
        cmbReceta.setSelectedIndex(0); cmbCategoria.setSelectedIndex(0);

        JOptionPane.showMessageDialog(this,
                "Producto \"" + nom + "\" agregado correctamente.",
                "Producto creado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onAgregarReceta(){
        if (listenerNavegarRecetas != null) {
            listenerNavegarRecetas.run();
        }
    }

    private void onEditar(Producto prod){
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topWindow instanceof Frame ? (Frame) topWindow : null, "Editar Producto", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.weightx = 1.0; g.gridx = 0;

        JLabel lblTitulo = new JLabel("Editar Producto " + prod.id);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(C_ACCENT);
        g.gridy = 0; p.add(lblTitulo, g);

        g.gridy++; p.add(mkLabelForm("Nombre del producto:"), g);
        JTextField txtEditNom = new JTextField(prod.nombre);
        styleCampoDialog(txtEditNom);
        g.gridy++; p.add(txtEditNom, g);

        g.gridy++; p.add(mkLabelForm("Categoría:"), g);
        JComboBox<String> cmbEditCat = new JComboBox<>(new String[]{"Plato fuerte", "Entrada", "Bebida", "Combo", "—"});
        cmbEditCat.setSelectedItem(prod.categoria);
        cmbEditCat.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditCat.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditCat, g);

        g.gridy++; p.add(mkLabelForm("Precio ($):"), g);
        JTextField txtEditPrecio = new JTextField(prod.precio.replace("$", "").trim());
        styleCampoDialog(txtEditPrecio);
        g.gridy++; p.add(txtEditPrecio, g);

        g.gridy++; p.add(mkLabelForm("Estado:"), g);
        JComboBox<String> cmbEditEst = new JComboBox<>(new String[]{"Activo", "Pausado"});
        cmbEditEst.setSelectedItem(prod.estado);
        cmbEditEst.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditEst.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditEst, g);

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

            // ── Validar nombre ──
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: el nombre del producto no puede estar vacío.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que el precio no esté vacío ──
            if (pre.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: el precio no puede estar vacío.",
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
                        "El precio debe ser un número válido mayor a cero (ej. 85.00).",
                        "Formato inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }

            prod.nombre = nom;
            prod.categoria = (String) cmbEditCat.getSelectedItem();
            prod.precio = "$" + String.format("%.2f", precioNum);
            prod.estado = (String) cmbEditEst.getSelectedItem();

            poblarTabla();
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                    "Producto \"" + nom + "\" actualizado correctamente.",
                    "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
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

    private void onEliminar(Producto prod){
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + prod.nombre + "\"?","Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if(ok == JOptionPane.YES_OPTION){ productos.remove(prod); poblarTabla(); }
    }

    private void inicializarDummy(){
        productos.add(new Producto("#001", "Enchiladas verdes", "Plato fuerte", "$85.00", "Activo"));
        productos.add(new Producto("#002", "Pozole rojo",       "Plato fuerte", "$95.00", "Activo"));
        productos.add(new Producto("#003", "Tostadas de pata",  "Entrada",      "$45.00", "Pausado"));
        productos.add(new Producto("#004", "Agua de Jamaica",   "Bebida",       "$20.00", "Activo"));
    }

    public void setProductos(List<Producto> lista){ productos.clear(); productos.addAll(lista); poblarTabla(); }

    public void setCombos(String[] recetas, String[] cats){
        cmbReceta.removeAllItems(); cmbReceta.addItem("Receta");
        for(String r:recetas) cmbReceta.addItem(r);
        cmbCategoria.removeAllItems(); cmbCategoria.addItem("Categoria");
        for(String c:cats) cmbCategoria.addItem(c);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sistema de Gestión");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);

            CardLayout cardLayout = new CardLayout();
            JPanel contenedorPrincipal = new JPanel(cardLayout);

            PanelMenuProductos panelProductos = new PanelMenuProductos();

            JPanel panelRecetas = new JPanel(new GridBagLayout());
            panelRecetas.setBackground(new Color(0xFBF5EC));
            JLabel lblRecetas = new JLabel(" Apartado de Recetas ");
            lblRecetas.setFont(new Font("Arial", Font.BOLD, 24));
            lblRecetas.setForeground(new Color(0x6B2D1A));
            panelRecetas.add(lblRecetas);

            contenedorPrincipal.add(panelProductos, "PRODUCTOS");
            contenedorPrincipal.add(panelRecetas, "RECETAS");

            panelProductos.setListenerNavegarRecetas(() -> cardLayout.show(contenedorPrincipal, "RECETAS"));

            frame.add(contenedorPrincipal);
            frame.setVisible(true);
        });
    }
}