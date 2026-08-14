package vistas;

import servicios.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // ─────────────────────────────────────────────
    private static final Color COLOR_HEADER   = new Color(0x6B2D1A); // Terracota oscuro
    private static final Color COLOR_BG       = new Color(0xFBF5EC); // Crema
    private static final Color COLOR_ACCENT   = new Color(0x6B2D1A); // Terracota
    private static final Color COLOR_INPUT_BG = new Color(0x2E2E2E); // Oscuro
    private static final Color COLOR_BTN      = new Color(0x2E2E2E); // Oscuro
    private static final Color COLOR_LINK     = new Color(0x7A3520); // Terracota claro

    // ─────────────────────────────────────────────
    // ATRIBUTOS
    // ─────────────────────────────────────────────
    private JTextField     txtUsuario;
    private JPasswordField txtPassword;
    private JButton        btnIngresar;
    private final ApiClient apiClient; // Instancia del cliente HTTP

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────
    public Login() {
        this.apiClient = new ApiClient(); // Inicializamos el cliente de la API

        setTitle("RestaurantOS - Administrador");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeader(),       BorderLayout.NORTH);  // Encabezado terracota
        add(buildPanelCentral(), BorderLayout.CENTER); // Formulario centrado

        setVisible(true);
    }

    // ═══════════════════════════════════════════════
    // ENCABEZADO SUPERIOR
    // ═══════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));

        JLabel titulo = new JLabel("Restaurant OS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        header.add(titulo, BorderLayout.CENTER);

        return header;
    }

    // ═══════════════════════════════════════════════
    // PANEL CENTRAL — Formulario de login
    // ═══════════════════════════════════════════════
    private JPanel buildPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;

        // ── Título principal ──
        JLabel lblBienvenido = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblBienvenido.setFont(new Font("Arial", Font.BOLD, 42));
        lblBienvenido.setForeground(COLOR_ACCENT);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(lblBienvenido, gbc);

        // ── Subtítulo ──
        JLabel lblSub = new JLabel("Inicia sesión para continuar", SwingConstants.CENTER);
        lblSub.setFont(new Font("Arial", Font.PLAIN, 16));
        lblSub.setForeground(COLOR_ACCENT);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(lblSub, gbc);

        // ── Label correo/usuario ──
        panel.add(buildLabel("Correo o usuario"), at(gbc, 2, new Insets(0, 0, 6, 0)));

        // ── Campo usuario ──
        txtUsuario = new JTextField();
        panel.add(buildInputField(txtUsuario), at(gbc, 3, new Insets(0, 0, 18, 0)));

        // ── Label contraseña ──
        panel.add(buildLabel("Contraseña"), at(gbc, 4, new Insets(0, 0, 6, 0)));

        // ── Campo contraseña ──
        txtPassword = new JPasswordField();
        panel.add(buildInputField(txtPassword), at(gbc, 5, new Insets(0, 0, 6, 0)));

        // ── Link olvidaste contraseña ──
        JLabel lblOlvido = new JLabel("¿Olvidaste tu contraseña?");
        lblOlvido.setFont(new Font("Arial", Font.PLAIN, 15));
        lblOlvido.setForeground(COLOR_LINK);
        lblOlvido.setHorizontalAlignment(SwingConstants.RIGHT);
        lblOlvido.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblOlvido.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onOlvidasteContrasena();
            }
        });
        panel.add(lblOlvido, at(gbc, 6, new Insets(0, 0, 24, 0)));

        // ── Botón ingresar ──
        btnIngresar = buildRoundedButton("Ingresar");
        btnIngresar.addActionListener(e -> onIngresar());
        panel.add(btnIngresar, at(gbc, 7, new Insets(0, 0, 0, 0)));

        return panel;
    }

    // ═══════════════════════════════════════════════
    // HELPERS DE UI
    // ═══════════════════════════════════════════════
    private JLabel buildLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        lbl.setForeground(COLOR_ACCENT);
        return lbl;
    }

    private GridBagConstraints at(GridBagConstraints gbc, int gridy, Insets insets) {
        gbc.gridy  = gridy;
        gbc.insets = insets;
        return gbc;
    }

    private JPanel buildInputField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(field, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(370, 52));
        wrapper.setMaximumSize(new Dimension(370, 52));
        return wrapper;
    }

    private JButton buildRoundedButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 17));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(370, 52));
        btn.setMaximumSize(new Dimension(370, 52));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════
    // ACCIONES (CONEXIÓN CON API LARAVEL)
    // ═══════════════════════════════════════════════
    private void onIngresar() {
        String usuario    = txtUsuario.getText().trim();
        String contrasena = new String(txtPassword.getPassword()).trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor completa todos los campos.",
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Bloqueamos el botón temporalmente para dar feedback visual
        btnIngresar.setEnabled(false);

        // Hilo de ejecución en segundo plano para no congelar la UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Petición POST a Laravel
                return apiClient.login(usuario, contrasena);
            }

            @Override
            protected void done() {
                try {
                    boolean loginCorrecto = get();

                    if (loginCorrecto) {
                        // Cierra esta ventana y abre VentanaAdmin
                        dispose();
                        new VentanaAdmin(usuario, "Administrador");
                    } else {
                        JOptionPane.showMessageDialog(
                                Login.this,
                                "Credenciales incorrectas o no tienes permisos de Administrador.",
                                "Error de autenticación",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            Login.this,
                            "No se pudo conectar con el servidor API. Revisa que Laravel esté corriendo.",
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    btnIngresar.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void onOlvidasteContrasena() {
        JOptionPane.showMessageDialog(
                this,
                "Funcionalidad de recuperación próximamente.",
                "Recuperar contraseña",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}