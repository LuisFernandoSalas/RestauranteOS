package vistas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Login extends JFrame {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // Misma paleta terracota/crema del módulo Caja
    // para mantener consistencia visual del sistema
    // ─────────────────────────────────────────────
    private static final Color COLOR_HEADER   = new Color(0x6B2D1A); // Terracota oscuro — encabezado
    private static final Color COLOR_BG       = new Color(0xFBF5EC); // Crema — fondo general
    private static final Color COLOR_ACCENT   = new Color(0x6B2D1A); // Terracota — textos y labels
    private static final Color COLOR_INPUT_BG = new Color(0x2E2E2E); // Oscuro — fondo de inputs
    private static final Color COLOR_BTN      = new Color(0x2E2E2E); // Oscuro — botón ingresar
    private static final Color COLOR_LINK     = new Color(0x7A3520); // Terracota claro — link

    // ─────────────────────────────────────────────
    // CAMPOS DEL FORMULARIO
    // Se declaran como atributos de clase para poder
    // leer sus valores desde onIngresar()
    // ─────────────────────────────────────────────
    private JTextField     txtUsuario;  // Campo de texto: correo o nombre de usuario
    private JPasswordField txtPassword; // Campo de contraseña (oculta el texto)

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // Configura la ventana y agrega los paneles
    // ─────────────────────────────────────────────
    public Login() {
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
    // Barra terracota con el nombre del sistema
    // alineado a la izquierda
    // ═══════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setPreferredSize(new Dimension(0, 80)); // Altura fija del encabezado
        header.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));

        JLabel titulo = new JLabel("Restauran OS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        header.add(titulo, BorderLayout.CENTER);

        return header;
    }

    // ═══════════════════════════════════════════════
    // PANEL CENTRAL — Formulario de login
    // Usa GridBagLayout para centrar verticalmente
    // y horizontalmente todos los componentes
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
        // Mismo ancho (370px) y alto (52px) que el botón Ingresar
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
        JButton btnIngresar = buildRoundedButton("Ingresar");
        btnIngresar.addActionListener(e -> onIngresar());
        panel.add(btnIngresar, at(gbc, 7, new Insets(0, 0, 0, 0)));

        return panel;
    }

    // ═══════════════════════════════════════════════
    // HELPERS DE UI
    // ═══════════════════════════════════════════════

    /**
     * Crea un label de formulario con estilo terracota estandarizado.
     * @param texto Texto a mostrar en el label
     */
    private JLabel buildLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        lbl.setForeground(COLOR_ACCENT);
        return lbl;
    }

    /**
     * Atajo para configurar posición e insets del GridBagConstraints.
     * Evita repetir código al agregar cada componente al panel.
     * @param gbc    Constraints a modificar
     * @param gridy  Fila en el grid
     * @param insets Márgenes del componente
     */
    private GridBagConstraints at(GridBagConstraints gbc, int gridy, Insets insets) {
        gbc.gridy  = gridy;
        gbc.insets = insets;
        return gbc;
    }

    /**
     * Campo de entrada de texto estilizado.
     * Fondo oscuro con esquinas redondeadas, sin ícono circular.
     * Ancho y alto idénticos al botón Ingresar (370x52px).
     *
     * @param field JTextField o JPasswordField a envolver
     */
    private JPanel buildInputField(JTextField field) {
        // Estilo del campo de texto
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setForeground(Color.WHITE);       // Texto blanco sobre fondo oscuro
        field.setCaretColor(Color.WHITE);       // Cursor blanco
        field.setOpaque(false);                 // Fondo transparente (lo dibuja el wrapper)
        field.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        // Contenedor con fondo redondeado oscuro
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
        wrapper.setPreferredSize(new Dimension(370, 52)); // Igual que el botón
        wrapper.setMaximumSize(new Dimension(370, 52));
        return wrapper;
    }

    /**
     * Botón con fondo redondeado oscuro y texto blanco.
     * Dibuja su propio fondo con paintComponent (pill shape).
     *
     * @param text Texto del botón
     */
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
        btn.setContentAreaFilled(false); // Sin fondo por defecto de Swing
        btn.setBorderPainted(false);     // Sin borde por defecto de Swing
        btn.setFocusPainted(false);      // Sin indicador de foco
        btn.setPreferredSize(new Dimension(370, 52));
        btn.setMaximumSize(new Dimension(370, 52));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ═══════════════════════════════════════════════
    // ACCIONES
    // ═══════════════════════════════════════════════

    /**
     * Se ejecuta al presionar "Ingresar".
     * Por ahora valida que los campos no estén vacíos.
     *
     * TODO (BD): reemplazar la validación local por:
     *   1. Abrir conexión JDBC a MySQL
     *   2. Ejecutar:
     *        SELECT id_usuario, nombre, rol
     *        FROM usuarios
     *        WHERE (correo = ? OR usuario = ?)
     *          AND contrasena = SHA2(?, 256)
     *          AND rol = 'ADMINISTRADOR'
     *   3. Si hay resultado → obtener nombre del ResultSet
     *   4. Cerrar esta ventana y abrir VentanaAdmin
     *        con el nombre real del usuario
     *   5. Si no hay resultado → mostrar mensaje de error
     */
    private void onIngresar() {
        String usuario    = txtUsuario.getText().trim();
        String contrasena = new String(txtPassword.getPassword()).trim();

        // Validación básica: campos vacíos
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor completa todos los campos.",
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Placeholder hasta conectar BD:
        // Cierra el login y abre la ventana principal del administrador
        // pasando el usuario como nombre y "Administrador" como rol
        dispose();
        new VentanaAdmin(usuario, "Administrador");


    }

    /**
     * Se ejecuta al hacer clic en "¿Olvidaste tu contraseña?".
     *
     * TODO (BD): implementar flujo de recuperación:
     *   1. Abrir JDialog modal con campo de correo
     *   2. Generar token temporal en tabla `tokens_recuperacion`
     *   3. Enviar correo con enlace/token al usuario
     */
    private void onOlvidasteContrasena() {
        JOptionPane.showMessageDialog(
                this,
                "Funcionalidad de recuperación próximamente.",
                "Recuperar contraseña",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ─────────────────────────────────────────────
    // PUNTO DE ENTRADA
    // Lanza el Login en el hilo de UI de Swing (EDT)
    // ─────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Login::new);
    }
}