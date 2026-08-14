package vistas;

import modelos.Empleado;
import servicios.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class PanelEmpleados extends JPanel implements Actualizables {

    @Override
    public void recargarDatos() {
        // Al presionar el botón en el menú lateral, esto volverá a hacer la petición HTTP
        cargarEmpleadosDesdeBackend();
    }

    // ─── PALETA DE COLORES ───
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

    // ─── COLORES POR ROL ───
    private static final Color ROL_MESERO_BG   = new Color(0xDCEAF9);
    private static final Color ROL_MESERO_T    = new Color(0x2D5F8A);
    private static final Color ROL_COCINA_BG   = new Color(0xF7E6C4);
    private static final Color ROL_COCINA_T    = new Color(0xB07A1E);
    private static final Color ROL_CAJERO_BG   = new Color(0xE9DCF4);
    private static final Color ROL_CAJERO_T    = new Color(0x7B4FA0);
    private static final Color ROL_ADMIN_BG    = new Color(0xF3DAD2);
    private static final Color ROL_ADMIN_T     = new Color(0x8A3A20);

    // ─── SERVICIO Y DATOS ───
    private final ApiClient apiClient = new ApiClient();
    private final List<Empleado> empleados = new ArrayList<>();

    // ─── COMPONENTES ───
    private JTextField txtNombre, txtUsername;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JComboBox<String> cmbRol;
    private JPanel panelTabla;

    private static final String[] ROLES = {"Rol asignado", "Mesero", "Cocinero", "Cajero", "Admin"};
    private static final double[] PW = {0.34, 0.28, 0.18, 0.20};
    private static final String[] COLS = {"Nombre", "UserName", "Rol", "Acciones"};

    // ─── CONSTRUCTOR ───
    public PanelEmpleados() {
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

        cargarEmpleadosDesdeBackend();
    }

    // ═══════════════════════════════════════════════
    // CONEXIÓN BACKEND (SWINGWORKER)
    // ═══════════════════════════════════════════════
    private void cargarEmpleadosDesdeBackend() {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiClient.obtenerEmpleados();
            }

            @Override
            protected void done() {
                try {
                    String json = get();
                    parsearYActualizarEmpleados(json);
                } catch (Exception e) {
                    System.err.println("Error al obtener empleados de Laravel: " + e.getMessage());
                    inicializarDummy();
                }
            }
        };
        worker.execute();
    }

    private void parsearYActualizarEmpleados(String json) {
        empleados.clear();
        if (json != null && !json.trim().isEmpty()) {
            try {
                org.json.JSONArray array = new org.json.JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    org.json.JSONObject obj = array.getJSONObject(i);

                    int id = obj.optInt("id", 0);
                    String nombre = obj.optString("name", obj.optString("nombre", "Sin Nombre"));
                    String username = obj.optString("username", "usuario");
                    String rolRaw = obj.optString("role", obj.optString("rol", "mesero"));

                    empleados.add(new Empleado(id, nombre, username, normalizarRol(rolRaw)));
                }
            } catch (Exception e) {
                System.err.println("Error procesando JSON de empleados: " + e.getMessage());
            }
        }

        if (empleados.isEmpty()) {
            inicializarDummy();
        } else {
            poblarTabla();
        }
    }

    /**
     * Mapea los valores en minúscula de Laravel a las etiquetas formateadas de Java
     */
    private String normalizarRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) return "Mesero";
        String r = rol.toLowerCase().trim();

        if (r.contains("admin"))  return "Admin";
        if (r.contains("coci"))   return "Cocinero";
        if (r.contains("caj"))    return "Cajero";
        return "Mesero";
    }

    /**
     * Convierte los valores de Java al formato exigido por la API de Laravel
     */
    private String denormalizarRol(String rol) {
        if (rol == null) return "mesero";
        switch (rol) {
            case "Admin":    return "admin";
            case "Cocinero": return "cocinero";
            case "Cajero":   return "cajero";
            default:         return "mesero";
        }
    }

    private String extraerJson(String item, String key, String def) {
        try {
            String search = "\"" + key + "\":";
            int start = item.indexOf(search);
            if (start == -1) return def;
            start += search.length();
            if (item.charAt(start) == '"') {
                start++;
                int end = item.indexOf("\"", start);
                return item.substring(start, end);
            } else {
                int end = item.indexOf(",", start);
                if (end == -1) end = item.indexOf("}", start);
                if (end == -1) end = item.length();
                return item.substring(start, end).trim();
            }
        } catch (Exception e) {
            return def;
        }
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

        gc.gridy = 0; gc.insets = new Insets(0, 0, 20, 0);
        p.add(buildTituloPrincipal(), gc);

        gc.gridy = 1; gc.insets = new Insets(0, 0, 14, 0);
        p.add(buildSubtituloConReferencia("Registrar nuevo empleado"), gc);

        JPanel f1 = new JPanel(new GridLayout(1, 2, 16, 0));
        f1.setOpaque(false);
        txtNombre   = new JTextField();
        txtUsername = new JTextField();
        f1.add(buildCampo(txtNombre, "Nombre completo"));
        f1.add(buildCampo(txtUsername, "UserName"));
        gc.gridy = 2; gc.insets = new Insets(0, 0, 12, 0);
        p.add(f1, gc);

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

        gc.gridy = 5; gc.insets = new Insets(0, 0, 12, 0);
        p.add(mkSubtitulo("Empleados registrados"), gc);

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

    private JPanel buildSubtituloConReferencia(String texto) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(mkSubtitulo(texto), BorderLayout.WEST);

        JLabel lblRef = new JLabel("* Campos obligatorios");
        lblRef.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRef.setForeground(new Color(0x999999));
        p.add(lblRef, BorderLayout.EAST);
        return p;
    }

    // ═══════════════════════════════════════════════
    // TABLA Y RENDERS
    // ═══════════════════════════════════════════════
    private void poblarTabla() {
        panelTabla.removeAll();
        panelTabla.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 0;

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

        int gy = 1;
        for (int i = 0; i < empleados.size(); i++) {
            Empleado e = empleados.get(i);
            Color rowBg = (i % 2 == 0) ? C_WHITE : C_ALT_ROW;
            g.gridy = gy;

            g.gridx = 0; g.weightx = PW[0];
            panelTabla.add(buildCellWrapper(buildCeldaNombre(e), rowBg), g);

            g.gridx = 1; g.weightx = PW[1];
            panelTabla.add(buildCellWrapper(mkLbl("@" + e.getUsername(), new Color(0x666666)), rowBg), g);

            g.gridx = 2; g.weightx = PW[2];
            panelTabla.add(buildCellWrapper(buildBadgeRol(e.getRol()), rowBg), g);

            g.gridx = 3; g.weightx = PW[3];
            panelTabla.add(buildCellWrapper(buildAcciones(e), rowBg), g);

            gy++;

            g.gridy = gy; g.gridx = 0; g.gridwidth = 4; g.weightx = 1.0;
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

    private JPanel buildCeldaNombre(Empleado e) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        Color[] colores = coloresRol(e.getRol());
        JComponent avatar = buildAvatar(iniciales(e.getNombre()), colores[0], colores[1]);

        JLabel lblNombre = mkLbl(e.getNombre(), new Color(0x333333));
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

    private JComponent buildBadgeRol(String rol) {
        Color[] colores = coloresRol(rol);
        JLabel lbl = new JLabel(rol);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(colores[1]);
        return lbl;
    }

    private Color[] coloresRol(String rol) {
        if (rol == null) return new Color[]{new Color(0xEDEDED), new Color(0x666666)};
        switch (rol) {
            case "Mesero":   return new Color[]{ROL_MESERO_BG, ROL_MESERO_T};
            case "Cocinero": return new Color[]{ROL_COCINA_BG, ROL_COCINA_T};
            case "Cajero":   return new Color[]{ROL_CAJERO_BG, ROL_CAJERO_T};
            case "Admin":    return new Color[]{ROL_ADMIN_BG, ROL_ADMIN_T};
            default:         return new Color[]{new Color(0xEDEDED), new Color(0x666666)};
        }
    }

    private String iniciales(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isEmpty()) return "?";
        String[] partes = nombreCompleto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length && sb.length() < 2; i++) {
            if (!partes[i].isEmpty()) sb.append(Character.toUpperCase(partes[i].charAt(0)));
        }
        return sb.toString();
    }

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
    // EVENTOS Y REGISTRO EN LARAVEL
    // ═══════════════════════════════════════════════
    private void onRegistrarEmpleado() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String rolSeleccionado = (String) cmbRol.getSelectedItem();
        String pass = new String(txtPassword.getPassword()).trim();
        String confirmPass = new String(txtConfirmPassword.getPassword()).trim();

        if (nombre.isEmpty() || nombre.equals("Nombre completo")) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar: debes ingresar el nombre completo del empleado.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.isEmpty() || username.equals("UserName")) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar: debes ingresar un UserName.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (rolSeleccionado == null || rolSeleccionado.equals(ROLES[0])) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar: selecciona el rol asignado.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pass.isEmpty() || pass.equals("Contraseña de acceso")) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar: ingresa una contraseña de acceso.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (existeUsername(username, null)) {
            JOptionPane.showMessageDialog(this, "El UserName \"" + username + "\" ya está en uso. Elige otro.", "UserName duplicado", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Convertimos el rol a minúsculas para Laravel (ej: "mesero", "cocinero")
        String rolApi = denormalizarRol(rolSeleccionado);
        Empleado nuevoEmpleado = new Empleado(nombre, username, rolApi, pass);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiClient.crearEmpleado(nuevoEmpleado);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Lanza cualquier excepción ocurrida en doInBackground
                    cargarEmpleadosDesdeBackend();
                    limpiarFormulario();
                    JOptionPane.showMessageDialog(PanelEmpleados.this,
                            "Empleado \"" + nombre + "\" registrado correctamente.",
                            "Empleado registrado", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelEmpleados.this,
                            "Error al registrar en el servidor:\n" + ex.getCause().getMessage(),
                            "Error de API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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
        JTextField txtEditNom = new JTextField(item.getNombre());
        styleCampoDialog(txtEditNom);
        g.gridy++; p.add(txtEditNom, g);

        g.gridy++; p.add(mkLabelForm("UserName:"), g);
        JTextField txtEditUser = new JTextField(item.getUsername());
        styleCampoDialog(txtEditUser);
        g.gridy++; p.add(txtEditUser, g);

        g.gridy++; p.add(mkLabelForm("Rol asignado:"), g);
        JComboBox<String> cmbEditRol = new JComboBox<>(new String[]{"Mesero", "Cocinero", "Cajero", "Admin"});
        cmbEditRol.setSelectedItem(item.getRol());
        cmbEditRol.setFont(new Font("Arial", Font.PLAIN, 14));
        cmbEditRol.setBackground(C_CAMPO_BG);
        g.gridy++; p.add(cmbEditRol, g);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBtns.setOpaque(false);

        JButton btnCancelar = buildBoton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 38));
        btnCancelar.addActionListener(e -> dialog.dispose());

        JButton btnGuardar = buildBoton("Guardar Cambios");
        btnGuardar.setPreferredSize(new Dimension(160, 38));
        btnGuardar.addActionListener(e -> {
            String nom = txtEditNom.getText().trim();
            String user = txtEditUser.getText().trim();
            String rolUi = (String) cmbEditRol.getSelectedItem();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No se pudo guardar: el nombre completo no puede estar vacío.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No se pudo guardar: el UserName no puede estar vacío.", "Campo requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (existeUsername(user, item)) {
                JOptionPane.showMessageDialog(dialog, "El UserName \"" + user + "\" ya lo usa otro empleado. Elige otro.", "UserName duplicado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            item.setNombre(nom);
            item.setUsername(user);
            item.setRol(denormalizarRol(rolUi)); // Guardamos en minúsculas para enviar a la API

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    if (item.getId() != null) {
                        apiClient.actualizarEmpleado(item);
                    }
                    return null;
                }

                @Override protected void done() {
                    cargarEmpleadosDesdeBackend();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(PanelEmpleados.this,
                            "Empleado \"" + nom + "\" actualizado correctamente.",
                            "Cambios guardados", JOptionPane.INFORMATION_MESSAGE);
                }
            }.execute();
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
                "¿Eliminar al empleado \"" + item.getNombre() + "\"?", "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    if (item.getId() != null) {
                        apiClient.eliminarEmpleado(item.getId());
                    }
                    return null;
                }

                @Override protected void done() {
                    cargarEmpleadosDesdeBackend();
                }
            }.execute();
        }
    }

    private boolean existeUsername(String username, Empleado excluir) {
        for (Empleado e : empleados) {
            if (e == excluir) continue;
            if (e.getUsername() != null && e.getUsername().equalsIgnoreCase(username)) return true;
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

        return wrapCampo(field);
    }

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

    private JButton buildBotonSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo beige suave
                g2.setColor(C_CAMPO_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Borde sutil
                g2.setColor(C_CAMPO_BOR);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);

                // Texto en tono café
                g2.setColor(C_ACCENT);
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
        btn.setPreferredSize(new Dimension(110, 38));
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

    // ─── DATOS FALLBACK (DUMMY) ───
    private void inicializarDummy() {
        empleados.add(new Empleado(1, "Juan López",     "juan",   "Mesero"));
        empleados.add(new Empleado(2, "Ana Ríos",       "ana",    "Cocinero"));
        empleados.add(new Empleado(3, "Pedro González", "pedro",  "Cajero"));
        empleados.add(new Empleado(4, "Rosa Torres",    "rosa",   "Mesero"));
        empleados.add(new Empleado(5, "Carlos García",  "carlos", "Cocinero"));
        poblarTabla();
    }
}