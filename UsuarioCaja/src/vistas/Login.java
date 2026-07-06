package vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ═══════════════════════════════════════════════════════
 *  Vista: Login
 * ═══════════════════════════════════════════════════════
 *  Primera pantalla del sistema. Valida usuario y
 *  contraseña antes de abrir VentanaPrincipal.
 *
 *  TODO (BD): en onIngresar() reemplazar la validación
 *  local por consulta a la tabla `usuarios`:
 *    SELECT id_usuario, nombre, rol
 *    FROM usuarios
 *    WHERE (correo = ? OR usuario = ?)
 *      AND contrasena = SHA2(?, 256);
 * ═══════════════════════════════════════════════════════
 */
public class Login extends JFrame {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // ─────────────────────────────────────────────
    private static final Color COLOR_HEADER   = new Color(0x6B2D1A);
    private static final Color COLOR_BG       = new Color(0xFBF5EC);
    private static final Color COLOR_ACCENT   = new Color(0x6B2D1A);
    private static final Color COLOR_INPUT_BG = new Color(0x2E2E2E);
    private static final Color COLOR_BTN      = new Color(0x2E2E2E);
    private static final Color COLOR_LINK     = new Color(0x7A3520);

    // ─────────────────────────────────────────────
    // CAMPOS DEL FORMULARIO
    // ─────────────────────────────────────────────
    private JTextField     txtUsuario;
    private JPasswordField txtPassword;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────
    public Login() {
        setTitle("RestaurantOS - Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildPanelCentral(), BorderLayout.CENTER);

        setVisible(true);
    }

    // ─────────────────────────────────────────────
    // ENCABEZADO SUPERIOR
    // ─────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));

        JLabel titulo = new JLabel("Restauran OS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        header.add(titulo, BorderLayout.CENTER);

        return header;
    }

    // ─────────────────────────────────────────────
    // PANEL CENTRAL - Formulario de login
    // ─────────────────────────────────────────────
    private JPanel buildPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill  = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblBienvenido = new JLabel("Bienvenido", SwingConstants.CENTER);
        lblBienvenido.setFont(new Font("Arial", Font.BOLD, 42));
        lblBienvenido.setForeground(COLOR_ACCENT);
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(lblBienvenido, gbc);

        // Subtítulo
        JLabel lblSub = new JLabel("Inicia sesión para continuar", SwingConstants.CENTER);
        lblSub.setFont(new Font("Arial", Font.PLAIN, 16));
        lblSub.setForeground(COLOR_ACCENT);
        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(lblSub, gbc);

        // Label usuario
        panel.add(buildLabel("Correo o usuario"), at(gbc, 2, new Insets(0, 0, 6, 0)));

        // Campo usuario — mismo ancho que el botón (370px), sin círculo
        txtUsuario = new JTextField();
        panel.add(buildInputField(txtUsuario), at(gbc, 3, new Insets(0, 0, 18, 0)));

        // Label contraseña
        panel.add(buildLabel("Contraseña"), at(gbc, 4, new Insets(0, 0, 6, 0)));

        // Campo contraseña — mismo ancho que el botón (370px), sin círculo
        txtPassword = new JPasswordField();
        panel.add(buildInputField(txtPassword), at(gbc, 5, new Insets(0, 0, 6, 0)));

        // Link olvidaste contraseña
        JLabel lblOlvido = new JLabel("¿Olvidaste tu contraseña?");
        lblOlvido.setFont(new Font("Arial", Font.PLAIN, 15));
        lblOlvido.setForeground(COLOR_LINK);
        lblOlvido.setHorizontalAlignment(SwingConstants.RIGHT);
        lblOlvido.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblOlvido.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onOlvidasteContrasena(); }
        });
        panel.add(lblOlvido, at(gbc, 6, new Insets(0, 0, 24, 0)));

        // Botón ingresar
        JButton btnIngresar = buildRoundedButton("Ingresar");
        btnIngresar.addActionListener(e -> onIngresar());
        panel.add(btnIngresar, at(gbc, 7, new Insets(0, 0, 0, 0)));

        return panel;
    }

    // ─────────────────────────────────────────────
    // HELPERS DE UI
    // ─────────────────────────────────────────────
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

    /**
     * Campo de entrada sin círculo, mismo ancho que el botón (370px).
     * Fondo oscuro con esquinas redondeadas.
     */
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
        // Mismo ancho y alto que el botón Ingresar
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

    // ─────────────────────────────────────────────
    // ACCIONES
    // ─────────────────────────────────────────────

    /**
     * Valida credenciales y abre la ventana principal.
     *
     * TODO (BD): reemplazar validación local por:
     *   1. Consultar tabla `usuarios` con usuario/correo + hash contraseña
     *   2. Obtener nombre y rol del ResultSet
     *   3. Pasar nombre y rol al constructor de VentanaPrincipal
     */
    private void onIngresar() {
        String usuario    = txtUsuario.getText().trim();
        String contrasena = new String(txtPassword.getPassword()).trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Por favor completa todos los campos.",
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🚀 Nuestro puente entra en acción en un hilo separado
        new Thread(() -> {
            try {
                // 1️⃣ Declaramos el jsonInput de forma segura dentro del hilo
                String jsonInput = "{\"username\": \"" + usuario + "\", \"password\": \"" + contrasena + "\"}";

                // Hacemos el POST usando NUESTRO ApiClient
                String jsonResponse = api.ApiClient.post("login", jsonInput);

                String tokenExtraido = "";

                // 🧽 LIMPIEZA ABSOLUTA DEL JSON
                String jsonLimpio = jsonResponse.replace("{", "").replace("}", "").replace("\"", "").replace(" ", "").trim();

                if (jsonLimpio.contains("token:")) {
                    tokenExtraido = jsonLimpio.split("token:")[1].split(",")[0].trim();

                    // 💾 Almacén global por si acaso
                    api.SessionManager.setToken(tokenExtraido);
                }

                // 🚨 ALERTA: Si falló, inspeccionamos qué mandó José
                if (tokenExtraido.isEmpty()) {
                    System.err.println("❌ ERROR CRÍTICO: El extractor falló. La API respondió esto: " + jsonResponse);
                } else {
                    System.out.println("✅ TOKEN EXTRAÍDO CON ÉXITO: " + tokenExtraido.substring(0, Math.min(15, tokenExtraido.length())) + "...");
                }

                // Preparamos el envío fijo
                final String tokenParaEnviar = tokenExtraido;
                final String usuarioParaEnviar = usuario; // Fijo para la lambda

                // Si todo salió bien, abrimos la ventana de Kevyn en el hilo principal
                java.awt.EventQueue.invokeLater(() -> {
                    dispose(); // Cierra el login

                    System.out.println("✈️ Enviando token directo a la ventana: [" + tokenParaEnviar + "]");

                    // 2️⃣ Le pasamos las variables fijas sin errores de compilación
                    VentanaPrincipal principal = new VentanaPrincipal(usuarioParaEnviar, "Cajero", tokenParaEnviar);
                    principal.setVisible(true);
                });

            } catch (Exception ex) {
                java.awt.EventQueue.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Usuario o contraseña incorrectos o error de red.",
                            "Error de inicio de sesión",
                            JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();
    }
    /**
     * TODO (BD): abrir JDialog de recuperación de contraseña.
     * Enviar token de recuperación por correo.
     */
    private void onOlvidasteContrasena() {
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de recuperación próximamente.",
                "Recuperar contraseña",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ─────────────────────────────────────────────
    // PUNTO DE ENTRADA
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}