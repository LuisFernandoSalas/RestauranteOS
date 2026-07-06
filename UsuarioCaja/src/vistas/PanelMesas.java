package vistas;

import modelos.Mesa;
import modelos.Mesa.EstadoMesa;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class PanelMesas extends JPanel {

    // ─────────────────────────────────────────────
    // PALETA DE COLORES
    // ─────────────────────────────────────────────
    private static final Color COLOR_BG      = new Color(0xFBF5EC);
    private static final Color COLOR_ACCENT  = new Color(0x6B2D1A);
    private static final Color COLOR_DIVIDER = new Color(0xC8A882);

    private static final Color BORDER_LIBRE   = new Color(0x2E2E2E);
    private static final Color BORDER_OCUPADO = new Color(0xD48000);
    private static final Color BORDER_COBRO   = new Color(0xB83C10);

    // ─────────────────────────────────────────────
    // DATOS Y COMPONENTES
    // ─────────────────────────────────────────────
    private final List<Mesa> mesas = new ArrayList<>();
    private JPanel           gridMesas;

    // ─────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────
    // 🔑 Creamos una variable local para el panel
    // 1. Agrega esta variable al inicio de la clase PanelMesas
    private String tokenLocal;

    // 2. Modifica el constructor para que reciba el token
    public PanelMesas(String token) {
        this.tokenLocal = token; // 🔑 Guardado físico sin depender de estáticos

        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCentro(),  BorderLayout.CENTER);

        iniciarPolling();
    }
    // ─────────────────────────────────────────────
    // ENCABEZADO: título + separador
    // ─────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel lblTitulo = new JLabel("Mesas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 34));
        lblTitulo.setForeground(COLOR_ACCENT);

        JLabel lblEstado = new JLabel("Cobro Activo");
        lblEstado.setFont(new Font("Arial", Font.PLAIN, 16));
        lblEstado.setForeground(COLOR_ACCENT);
        lblEstado.setHorizontalAlignment(SwingConstants.RIGHT);

        topBar.add(lblTitulo, BorderLayout.WEST);
        topBar.add(lblEstado, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_DIVIDER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(topBar, BorderLayout.CENTER);
        header.add(sep,    BorderLayout.SOUTH);
        return header;
    }

    // ─────────────────────────────────────────────
    // CENTRO: leyenda + grid
    // ─────────────────────────────────────────────
    private JPanel buildCentro() {
        JPanel leyenda = buildLeyenda();
        leyenda.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));

        gridMesas = new JPanel(new GridLayout(0, 3, 18, 18));
        gridMesas.setOpaque(false);
        actualizarGrid();

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(leyenda,   BorderLayout.NORTH);
        centro.add(gridMesas, BorderLayout.CENTER);
        return centro;
    }

    // ─────────────────────────────────────────────
    // LEYENDA DE ESTADOS
    // ─────────────────────────────────────────────
    private JPanel buildLeyenda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        p.setOpaque(false);
        p.add(buildItemLeyenda("Libre",   BORDER_LIBRE));
        p.add(buildItemLeyenda("Ocupado", BORDER_OCUPADO));
        p.add(buildItemLeyenda("Cobro",   BORDER_COBRO));
        return p;
    }

    private JPanel buildItemLeyenda(String texto, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);

        JPanel circulo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BG);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
            }
        };
        circulo.setOpaque(false);
        circulo.setPreferredSize(new Dimension(20, 20));

        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(COLOR_ACCENT);

        item.add(circulo);
        item.add(lbl);
        return item;
    }

    // ═══════════════════════════════════════════════
    // TARJETA DE MESA
    // ═══════════════════════════════════════════════
    private JPanel buildTarjeta(Mesa mesa) {
        Color colorBorde = switch (mesa.getEstado()) {
            case LIBRE   -> BORDER_LIBRE;
            case OCUPADO -> BORDER_OCUPADO;
            case COBRO   -> BORDER_COBRO;
        };

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(colorBorde);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Número de mesa
        JLabel lblNum = new JLabel(String.valueOf(mesa.getNumero()), SwingConstants.CENTER);
        lblNum.setFont(new Font("Arial", Font.PLAIN, 36));
        lblNum.setForeground(new Color(0x3A3A3A));
        lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(lblNum);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        if (mesa.getEstado() == EstadoMesa.LIBRE) {
            JLabel lblLibre = new JLabel("Libre", SwingConstants.CENTER);
            lblLibre.setFont(new Font("Arial", Font.PLAIN, 15));
            lblLibre.setForeground(new Color(0x888888));
            lblLibre.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblLibre);
        } else {
            JLabel lblTotal = new JLabel(String.format("$%.2f", mesa.getTotal()), SwingConstants.CENTER);
            lblTotal.setFont(new Font("Arial", Font.PLAIN, 16));
            lblTotal.setForeground(new Color(0x3A3A3A));
            lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblTotal);

            if (mesa.getTiempoMin() > 0) {
                JLabel lblTiempo = new JLabel(mesa.getTiempoMin() + " min", SwingConstants.CENTER);
                lblTiempo.setFont(new Font("Arial", Font.PLAIN, 13));
                lblTiempo.setForeground(new Color(0xAAAAAA));
                lblTiempo.setAlignmentX(Component.CENTER_ALIGNMENT);
                card.add(Box.createRigidArea(new Dimension(0, 2)));
                card.add(lblTiempo);
            }
        }

        card.add(Box.createVerticalGlue());

        // TODO: abrir PanelDetallePedido al hacer clic
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMesaSeleccionada(mesa);
            }
        });

        return card;
    }

    /**
     * Reconstruye el grid completo con los datos actuales de `mesas`.
     * Llamar después de cada lectura de BD.
     */
    public void actualizarGrid() {
        gridMesas.removeAll();
        for (Mesa m : mesas) {
            gridMesas.add(buildTarjeta(m));
        }
        gridMesas.revalidate();
        gridMesas.repaint();
    }


    /**
     * Carga las mesas desde la base de datos.
     *
     * TODO (BD): implementar con JDBC:
     *   Connection con = DriverManager.getConnection(URL, USER, PASS);
     *   PreparedStatement ps = con.prepareStatement(
     *     "SELECT id_mesa, numero, estado, total, tiempo_min " +
     *     "FROM mesas ORDER BY numero ASC");
     *   ResultSet rs = ps.executeQuery();
     *   List<Mesa> lista = new ArrayList<>();
     *   while (rs.next()) {
     *     lista.add(new Mesa(
     *       rs.getInt("id_mesa"),
     *       rs.getInt("numero"),
     *       EstadoMesa.valueOf(rs.getString("estado")),
     *       rs.getDouble("total"),
     *       rs.getInt("tiempo_min")
     *     ));
     *   }
     *   return lista;
     */

    // ═══════════════════════════════════════════════
    // POLLING - refresco automático desde BD
    // TODO: descomentar las líneas internas cuando
    //       cargarMesasDesdeBD() esté implementado
    // ═══════════════════════════════════════════════
    private void iniciarPolling() {
        Timer timer = new Timer(4_000, e -> {
            // Hacemos una copia fija (effectively final) para que la lambda no truene
            final String tokenFijo = this.tokenLocal;

            new Thread(() -> {
                try {
                    // 🔌 Inyectamos el token al ApiClient justo antes de pedir las mesas
                    api.ApiClient.token = tokenFijo;

                    List<Mesa> listaActualizada = cargarMesasDesdeBD();
                    SwingUtilities.invokeLater(() -> {
                        mesas.clear();
                        mesas.addAll(listaActualizada);
                        actualizarGrid();
                    });
                } catch (Exception ex) {
                    System.err.println("Error en mesas: " + ex.getMessage());
                }
            }).start();
        });
        timer.setRepeats(true);
        timer.start();
    }

    private List<Mesa> cargarMesasDesdeBD() {
        List<Mesa> lista = new ArrayList<>();
        try {
            // Hacemos el GET al endpoint de mesas de José (ajusta "mesas" si su ruta es diferente)
            String jsonJson = api.ApiClient.get("mesas");

            // Parseo express del JSON (Mientras no tengamos Gson, este regex limpia y separa las tarjetas)
            String[] objetos = jsonJson.split("\\},\\{");
            for (String obj : objetos) {
                int id = Integer.parseInt(extraerValor(obj, "id_mesa"));
                int numero = Integer.parseInt(extraerValor(obj, "numero"));
                String estadoStr = extraerValor(obj, "estado").toUpperCase();
                double total = Double.parseDouble(extraerValor(obj, "total"));
                int tiempoMin = Integer.parseInt(extraerValor(obj, "tiempo_min"));

                EstadoMesa estado = EstadoMesa.valueOf(estadoStr);
                lista.add(new Mesa(numero, estado, total, tiempoMin)); // Ajusta al constructor de tu clase Mesa
            }
        } catch (Exception e) {
            System.err.println("Error parseando mesas: " + e.getMessage());
            // Si falla la API, dejamos los dummies de Kevyn para que no se rompa la pantalla
            return mesas;
        }
        return lista;
    }

    private String extraerValor(String json, String llave) {
        try {
            String patron = "\"" + llave + "\":";
            int inicio = json.indexOf(patron) + patron.length();
            if (json.charAt(inicio) == '"') {
                inicio++;
                int fin = json.indexOf("\"", inicio);
                return json.substring(inicio, fin);
            } else {
                int fin = json.indexOf(",", inicio);
                if (fin == -1) fin = json.indexOf("}", inicio);
                return json.substring(inicio, fin).trim();
            }
        } catch (Exception e) {
            return "0";
        }
    }

    // ═══════════════════════════════════════════════
    // ACCIÓN: clic en tarjeta
    // TODO: abrir JDialog con detalle del pedido
    // ═══════════════════════════════════════════════
    private void onMesaSeleccionada(Mesa mesa) {
        if (mesa.getEstado() == EstadoMesa.COBRO) {
            VentanaPrincipal ventana = (VentanaPrincipal)
                    SwingUtilities.getWindowAncestor(this);
            ventana.abrirCobro(mesa);
        }
        // LIBRE y OCUPADO: sin acción por ahora
        // TODO: LIBRE → asignar mesa, OCUPADO → ver pedido activo
    }
}