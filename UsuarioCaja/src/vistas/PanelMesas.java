package vistas;

import modelos.Mesa;
import modelos.Mesa.EstadoMesa;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════
 *  Vista: PanelMesas
 * ═══════════════════════════════════════════════════════
 *  Muestra el grid de tarjetas de mesas con su estado
 *  visual (color de borde) e información de cada una.
 *
 *  Preparado para BD:
 *    - cargarMesasDesdeBD() es el punto de integración
 *    - Un javax.swing.Timer hace polling cada 10 segundos
 *    - actualizarGrid() refresca la UI con los nuevos datos
 *
 *  TODO (BD): en cargarMesasDesdeBD() ejecutar:
 *    SELECT id_mesa, numero, estado, total, tiempo_min
 *    FROM mesas ORDER BY numero ASC;
 * ═══════════════════════════════════════════════════════
 */
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
    public PanelMesas(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
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

    public void obtenerMesasDelServidor() {
        String token = network.TokenManager.TOKEN;
        if (token == null || token.isEmpty()) return;

        network.ApiService api = network.RetrofitClient.getClient().create(network.ApiService.class);

        api.getMesas("Bearer " + token).enqueue(new retrofit2.Callback<com.google.gson.JsonArray>() {
            @Override
            public void onResponse(retrofit2.Call<com.google.gson.JsonArray> call, retrofit2.Response<com.google.gson.JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.google.gson.JsonArray mesasJson = response.body();
                    List<Mesa> nuevasMesas = new ArrayList<>();

                    for (com.google.gson.JsonElement elemento : mesasJson) {
                        com.google.gson.JsonObject obj = elemento.getAsJsonObject();

                        int id = obj.get("id").getAsInt();
                        int numero = obj.get("numero").getAsInt();
                        String estadoStr = obj.get("estado").getAsString(); // "libre", "ocupada", "cobro"
                        double total = obj.has("total_actual") && !obj.get("total_actual").isJsonNull() ? obj.get("total_actual").getAsDouble() : 0.0;

                        // Mapeamos el texto de Laravel a tu Enum de Java
                        Mesa.EstadoMesa estadoEnum = Mesa.EstadoMesa.LIBRE;
                        if (estadoStr.equalsIgnoreCase("ocupada")) estadoEnum = Mesa.EstadoMesa.OCUPADO;
                        else if (estadoStr.equalsIgnoreCase("cobro")) estadoEnum = Mesa.EstadoMesa.COBRO;

                        // Creamos la mesa con sus datos reales
                        nuevasMesas.add(new Mesa(id, numero, estadoEnum, total, 0));
                    }

                    // Actualizamos la interfaz gráfica de Swing
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        mesas.clear();
                        mesas.addAll(nuevasMesas);
                        actualizarGrid(); // Este es tu método que repinta las tarjetitas
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.google.gson.JsonArray> call, Throwable t) {
                System.out.println("Error cargando las mesas desde Laravel: " + t.getMessage());
            }
        });
    }

    private List<Mesa> cargarMesasDesdeBD() {
        return new ArrayList<>(mesas); // PLACEHOLDER
    }

    // ═══════════════════════════════════════════════
    // POLLING - refresco automático desde BD
    // TODO: descomentar las líneas internas cuando
    //       cargarMesasDesdeBD() esté implementado
    // ═══════════════════════════════════════════════
    private void iniciarPolling() {
        // Llama a la BD inmediatamente al abrir el panel
        obtenerMesasDelServidor();

        // Configura el temporizador para que pida actualizaciones cada 10 segundos
        Timer timer = new Timer(10_000, e -> {
            obtenerMesasDelServidor();
        });
        timer.setRepeats(true);
        timer.start();
    }

    // ═══════════════════════════════════════════════
    // ACCIÓN: clic en tarjeta
    // TODO: abrir JDialog con detalle del pedido
    // ═══════════════════════════════════════════════
    private void onMesaSeleccionada(Mesa mesa) {
        if (mesa.getEstado() == EstadoMesa.COBRO) {
            VentanaPrincipal ventana = (VentanaPrincipal)
                    SwingUtilities.getWindowAncestor(this);
        }
        // LIBRE y OCUPADO: sin acción por ahora
        // TODO: LIBRE → asignar mesa, OCUPADO → ver pedido activo
    }

}