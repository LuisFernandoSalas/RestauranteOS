package vistas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;



public class PanelEmpleados extends JPanel {

    // ─── PALETA DE COLORES (congruente con el resto de RestaurantOS) ───
    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_DIV_LINE  = new Color(0xC8A882);
    private static final Color C_CAMPO_BG  = new Color(0xEEE8DE);
    private static final Color C_CAMPO_BOR = new Color(0xD4C4A8);
    private static final Color C_BTN_DARK  = new Color(0x4A2010);
    private static final Color C_TBL_HDR   = new Color(0x7A3520);
    private static final Color C_TBL_HDR_T = Color.WHITE;
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_ALT_ROW   = new Color(0xFAF4EE);
    private static final Color C_BTN_DEL   = new Color(0xC03020);

    // ─── COLORES POR ROL (badge + avatar) ───
    private static final Color ROL_MESERO_BG   = new Color(0xDCEAF9);
    private static final Color ROL_MESERO_T    = new Color(0x2D5F8A);
    private static final Color ROL_COCINA_BG   = new Color(0xF7E6C4);
    private static final Color ROL_COCINA_T    = new Color(0xB07A1E);
    private static final Color ROL_CAJERO_BG   = new Color(0xE9DCF4);
    private static final Color ROL_CAJERO_T    = new Color(0x7B4FA0);
    private static final Color ROL_ADMIN_BG    = new Color(0xF3DAD2);
    private static final Color ROL_ADMIN_T     = new Color(0x8A3A20);

    // ─── MODELO DE DATOS ────────────────────────────
    static class Empleado {
        String id, nombre, username, rol;
        Empleado(String id, String nombre, String username, String rol) {
            this.id = id; this.nombre = nombre; this.username = username; this.rol = rol;
        }
    }

    private final List<Empleado> empleados = new ArrayList<>();

    // ─── COMPONENTES ────────────────────────────────
    private JTextField txtNombre, txtUsername;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JComboBox<String> cmbRol;
    private JPanel panelTabla;

    private static final String[] ROLES = {"Rol asignado ▾", "Mesero", "Cocina", "Cajero", "Administrador"};

    // Anchos de columna de la tabla (suman 1.0)
    private static final double[] PW = {0.34, 0.28, 0.18, 0.20};
    private static final String[] COLS = {"Nombre", "UserName", "Rol", "Acciones"};

    // ─── CONSTRUCTOR ────────────────────────────────
    public PanelEmpleados() {
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
    // ESTRUCTURA PRINCIPAL
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

        // 1. Título "Empleados"
        gc.gridy = 0; gc.insets = new Insets(0, 0, 20, 0);
        p.add(buildTituloPrincipal(), gc);

        // 2. Subtítulo "Registrar nuevo empleado" + referencia RF-03 / RNF-08
        gc.gridy = 1; gc.insets = new Insets(0, 0, 14, 0);
        p.add(buildSubtituloConReferencia("Registrar nuevo empleado", "RF-03 / RNF-08"), gc);

        // 3. Fila 1 del formulario: Nombre completo / UserName
        JPanel f1 = new JPanel(new GridLayout(1, 2, 16, 0));
        f1.setOpaque(false);
        txtNombre   = new JTextField();
        txtUsername = new JTextField();
        f1.add(buildCampo(txtNombre, "Nombre completo"));
        f1.add(buildCampo(txtUsername, "UserName"));
        gc.gridy = 2; gc.insets = new Insets(0, 0, 12, 0);
        p.add(f1, gc);

        // 4. Fila 2 del formulario: Rol / Contraseña / Confirmar contraseña
        JPanel f2 = new JPanel(new GridLayout(1, 3, 16, 0));
        f2.setOpaque(false);
        cmbRol = mkCombo(ROLES);
        txtPassword        = new JPasswordField();
        txtConfirmPassword = new JPasswordField();
        f2.add(buildComboWrap(cmbRol));
        f2.add(buildCampoPassword(txtPassword, "Contraseña de acceso"));
        f2.add(buildCampoPassword(txtConfirmPassword, "Confirmar contraseña"));
        gc.gridy = 3; gc.insets = new Insets(0, 0, 18, 0);
        p.add(f2, gc);

        // 5. Botón "+ Registrar empleado" (alineado a la derecha)
        JPanel filaBoton = new JPanel(new BorderLayout());
        filaBoton.setOpaque(false);
        JButton btnRegistrar = buildBoton("+ Registrar empleado");
        btnRegistrar.addActionListener(e -> onRegistrarEmpleado());
        JPanel wrapBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrapBoton.setOpaque(false);
        wrapBoton.add(btnRegistrar);
        filaBoton.add(wrapBoton, BorderLayout.CENTER);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 28, 0);
        p.add(filaBoton, gc);

        // 6. Subtítulo "Empleados registrados"
        gc.gridy = 5; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Empleados registrados"), gc);

        // 7. Tabla (encabezado + filas en una sola cuadrícula)
        panelTabla = new JPanel();
        panelTabla.setOpaque(false);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        gc.weighty = 1.0;
        gc.anchor  = GridBagConstraints.NORTH;
        gc.fill    = GridBagConstraints.BOTH;
        p.add(panelTabla, gc);

        poblarTabla();
        return p;
    }

    // ─── TÍTULO Y SUBTÍTULOS ────────────────────────
    private JPanel buildTituloPrincipal() {
        JLabel lbl = new JLabel("Empleados");
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

    private JPanel buildSubtituloConReferencia(String texto, String referencia) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(mkSubtitulo(texto), BorderLayout.WEST);

        JLabel lblRef = new JLabel(referencia);
        lblRef.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRef.setForeground(new Color(0x999999));
        p.add(lblRef, BorderLayout.EAST);
        return p;
    }

    // ═══════════════════════════════════════════════
    // TABLA (encabezado + filas, misma cuadrícula)
    // ═══════════════════════════════════════════════
    private void poblarTabla() {
        panelTabla.removeAll();
        panelTabla.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0;

        // ── Encabezado ──
        g.gridy = 0;
        for (int i = 0; i < COLS.length; i++) {
            g.gridx = i; g.weightx = PW[i];

            JPanel cellHeader = new JPanel(new BorderLayout());
            cellHeader.setBackground(C_TBL_HDR);
            cellHeader.setPreferredSize(new Dimension(0, 42));
            cellHeader.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

            JLabel lbl = new JLabel(COLS[i]);
            lbl.setFont(new Font("Arial", Font.BOLD, 13));
            lbl.setForeground(C_TBL_HDR_T);
            cellHeader.add(lbl, BorderLayout.WEST);

            panelTabla.add(cellHeader, g);
        }

        // ── Filas ──
        int gy = 1;
        for (int i = 0; i < empleados.size(); i++) {
            Empleado e = empleados.get(i);
            Color rowBg = (i % 2 == 0) ? C_WHITE : C_ALT_ROW;
            g.gridy = gy;

            g.gridx = 0; g.weightx = PW[0];
            panelTabla.add(buildCellWrapper(buildCeldaNombre(e), rowBg), g);

            g.gridx = 1; g.weightx = PW[1];
            panelTabla.add(buildCellWrapper(mkLbl(e.username, new Color(0x666666)), rowBg), g);

            g.gridx = 2; g.weightx = PW[2];
            panelTabla.add(buildCellWrapper(buildBadgeRol(e.rol), rowBg), g);

            g.gridx = 3; g.weightx = PW[3];
            panelTabla.add(buildCellWrapper(buildAcciones(e), rowBg), g);

            gy++;

            g.gridy = gy; g.gridx = 0; g.gridwidth = 4; g.weightx = 1.0;
            // El ÚLTIMO separador recibe weighty=1.0 y anchor NORTH: así el
            // espacio sobrante de panelTabla se va DEBAJO de la tabla y no
            // centra todo el bloque (el mismo bug que ya corregimos afuera,
            // pero esta vez ocurría DENTRO del GridBagLayout de la tabla).
            boolean esUltimaFila = (i == empleados.size() - 1);
            if (esUltimaFila) {
                g.weighty = 1.0;
                g.anchor = GridBagConstraints.NORTH;
                g.fill = GridBagConstraints.HORIZONTAL;
            }
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(0xEEDDCC));
            sep.setBackground(new Color(0xEEDDCC));
            panelTabla.add(sep, g);
            g.gridwidth = 1;
            g.weighty = 0;
            gy++;
        }

        panelTabla.revalidate();
        panelTabla.repaint();
    }

    private JPanel buildCellWrapper(JComponent comp, Color bg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setOpaque(true);
        p.setPreferredSize(new Dimension(0, 56));
        p.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        p.add(comp, BorderLayout.WEST);
        return p;
    }

    /** Celda "Nombre": avatar circular con iniciales + nombre completo */
    private JPanel buildCeldaNombre(Empleado e) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        Color[] colores = coloresRol(e.rol);
        JComponent avatar = buildAvatar(iniciales(e.nombre), colores[0], colores[1]);

        JLabel lblNombre = mkLbl(e.nombre, new Color(0x333333));
        lblNombre.setFont(new Font("Arial", Font.BOLD, 13));

        p.add(avatar);
        p.add(lblNombre);
        return p;
    }

    private JComponent buildAvatar(String iniciales, Color bg, Color fg) {
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(fg);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(iniciales)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(iniciales, tx, ty);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(34, 34));
        return avatar;
    }

    /** Nombre del rol como texto plano, coloreado según el tipo (sin relleno/píldora) */
    private JComponent buildBadgeRol(String rol) {
        Color[] colores = coloresRol(rol);
        Color fg = colores[1];

        JLabel lbl = new JLabel(rol);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(fg);
        return lbl;
    }

    private Color[] coloresRol(String rol) {
        switch (rol) {
            case "Mesero":        return new Color[]{ROL_MESERO_BG, ROL_MESERO_T};
            case "Cocina":         return new Color[]{ROL_COCINA_BG, ROL_COCINA_T};
            case "Cajero":         return new Color[]{ROL_CAJERO_BG, ROL_CAJERO_T};
            case "Administrador":  return new Color[]{ROL_ADMIN_BG, ROL_ADMIN_T};
            default:               return new Color[]{new Color(0xEDEDED), new Color(0x666666)};
        }
    }

    private String iniciales(String nombreCompleto) {
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length && sb.length() < 2; i++) {
            if (!partes[i].isEmpty()) sb.append(Character.toUpperCase(partes[i].charAt(0)));
        }
        return sb.toString();
    }

    /** Editar | Eliminar como texto clickeable, igual que en las demás tablas */
    private JPanel buildAcciones(Empleado item) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel editar = new JLabel("Editar");
        editar.setFont(new Font("Arial", Font.PLAIN, 13));
        editar.setForeground(C_ACCENT);
        editar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onEditarEmpleado(item); }
        });

        JLabel pipe = new JLabel("|");
        pipe.setFont(new Font("Arial", Font.PLAIN, 13));
        pipe.setForeground(new Color(0xCCCCCC));

        JLabel eliminar = new JLabel("Eliminar");
        eliminar.setFont(new Font("Arial", Font.PLAIN, 13));
        eliminar.setForeground(C_BTN_DEL);
        eliminar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eliminar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onEliminarEmpleado(item); }
        });

        p.add(editar); p.add(pipe); p.add(eliminar);
        return p;
    }

    private JLabel mkLbl(String t, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(c);
        return l;
    }

    // ═══════════════════════════════════════════════
    // LÓGICA DE EVENTOS
    // ═══════════════════════════════════════════════
    private void onRegistrarEmpleado() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String rol = (String) cmbRol.getSelectedItem();
        String pass = new String(txtPassword.getPassword()).trim();
        String confirmPass = new String(txtConfirmPassword.getPassword()).trim();

        // ── Validar nombre ──
        if (nombre.isEmpty() || nombre.equals("Nombre completo")) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar: debes ingresar el nombre completo del empleado.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar username vacío ──
        if (username.isEmpty() || username.equals("UserName")) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar: debes ingresar un UserName.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar rol ──
        if (rol == null || rol.equals(ROLES[0])) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar: selecciona el rol asignado.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar contraseña vacía ──
        if (pass.isEmpty() || pass.equals("Contraseña de acceso")) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar: ingresa una contraseña de acceso.",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Validar que las contraseñas coincidan ──
        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this,
                    "Las contraseñas no coinciden.",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ── Validar que el UserName no esté ya en uso ──
        if (existeUsername(username, null)) {
            JOptionPane.showMessageDialog(this,
                    "El UserName \"" + username + "\" ya está en uso. Elige otro.",
                    "UserName duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        empleados.add(new Empleado(String.format("#E%03d", empleados.size() + 1), nombre, username, rol));
        poblarTabla();
        limpiarFormulario();

        JOptionPane.showMessageDialog(this,
                "Empleado \"" + nombre + "\" registrado correctamente.",
                "Empleado registrado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onEditarEmpleado(Empleado item) {
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(topWindow instanceof Frame ? (Frame) topWindow : null,
                "Editar Empleado", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.weightx = 1.0; g.gridx = 0;

        JLabel lblTitulo = new JLabel("Editar Empleado");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(C_ACCENT);
        g.gridy = 0; p.add(lblTitulo, g);

        g.gridy++; p.add(mkLabelForm("Nombre completo:"), g);
        JTextField txtEditNom = new JTextField(item.nombre);
        styleCampoDialog(txtEditNom);
        g.gridy++; p.add(txtEditNom, g);

        g.gridy++; p.add(mkLabelForm("UserName:"), g);
        JTextField txtEditUser = new JTextField(item.username);
        styleCampoDialog(txtEditUser);
        g.gridy++; p.add(txtEditUser, g);

        g.gridy++; p.add(mkLabelForm("Rol asignado:"), g);
        JComboBox<String> cmbEditRol = new JComboBox<>(new String[]{"Mesero", "Cocina", "Cajero", "Administrador"});
        cmbEditRol.setSelectedItem(item.rol);
        cmbEditRol.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditRol.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditRol, g);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtns.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = buildBoton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(160, 38));
        btnGuardar.addActionListener(e -> {
            String nom = txtEditNom.getText().trim();
            String user = txtEditUser.getText().trim();

            // ── Validar nombre ──
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: el nombre completo no puede estar vacío.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar username vacío ──
            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "No se pudo guardar: el UserName no puede estar vacío.",
                        "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── Validar que el UserName no choque con otro empleado ──
            if (existeUsername(user, item)) {
                JOptionPane.showMessageDialog(dialog,
                        "El UserName \"" + user + "\" ya lo usa otro empleado. Elige otro.",
                        "UserName duplicado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            item.nombre = nom;
            item.username = user;
            item.rol = (String) cmbEditRol.getSelectedItem();
            poblarTabla();
            dialog.dispose();

            JOptionPane.showMessageDialog(this,
                    "Empleado \"" + nom + "\" actualizado correctamente.",
                    "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
        });

        panelBtns.add(btnCancelar);
        panelBtns.add(btnGuardar);

        g.gridy++; g.insets = new Insets(16, 0, 0, 0);
        p.add(panelBtns, g);

        dialog.add(p);
        dialog.pack();
        dialog.setSize(400, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void onEliminarEmpleado(Empleado item) {
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al empleado \"" + item.nombre + "\"?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            empleados.remove(item);
            poblarTabla();
        }
    }

    /**
     * Verifica si un username ya está en uso por otro empleado.
     * @param username    el username a verificar (comparación case-insensitive)
     * @param excluir     empleado a ignorar en la búsqueda (para permitir
     *                    guardar sin cambios al editar); usar null al registrar
     */
    private boolean existeUsername(String username, Empleado excluir) {
        for (Empleado e : empleados) {
            if (e == excluir) continue;
            if (e.username.equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    private void limpiarFormulario() {
        txtNombre.setText("Nombre completo"); txtNombre.setForeground(new Color(0xAAAAAA));
        txtUsername.setText("UserName"); txtUsername.setForeground(new Color(0xAAAAAA));
        cmbRol.setSelectedIndex(0);
        restaurarPlaceholderPassword(txtPassword, "Contraseña de acceso");
        restaurarPlaceholderPassword(txtConfirmPassword, "Confirmar contraseña");
    }

    // ═══════════════════════════════════════════════
    // UTILIDADES DE DISEÑO (mismo lenguaje visual que Combos/Inventario)
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

        return wrapCampo(field);
    }

    /** Campo de contraseña con placeholder visible en texto plano hasta que se enfoca */
    private JPanel buildCampoPassword(JPasswordField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        field.setForeground(new Color(0xAAAAAA));
        field.setEchoChar((char) 0);
        field.setText(placeholder);

        String ph = placeholder;
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(ph)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(new Color(0x333333));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).trim().isEmpty()) {
                    restaurarPlaceholderPassword(field, ph);
                }
            }
        });

        return wrapCampo(field);
    }

    private void restaurarPlaceholderPassword(JPasswordField field, String placeholder) {
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(new Color(0xAAAAAA));
    }

    private JPanel wrapCampo(JTextField field) {
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
        btn.setPreferredSize(new Dimension(200, 46));
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

    // ─── DATOS INICIALES DUMMY ──────────────────────
    private void inicializarDummy() {
        empleados.add(new Empleado("#E001", "Juan López",     "juan",   "Mesero"));
        empleados.add(new Empleado("#E002", "Ana Ríos",       "ana",    "Cocina"));
        empleados.add(new Empleado("#E003", "Pedro González", "pedro",  "Cajero"));
        empleados.add(new Empleado("#E004", "Rosa Torres",    "rosa",   "Mesero"));
        empleados.add(new Empleado("#E005", "Carlos García",  "carlos", "Cocina"));
    }
}