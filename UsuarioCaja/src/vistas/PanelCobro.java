package vistas;

import modelos.Mesa;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.ApiService;
import network.RetrofitClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ═══════════════════════════════════════════════════════
 *  Vista: PanelCobro — Integrado con API (Retrofit)
 * ═══════════════════════════════════════════════════════
 */
public class PanelCobro extends JPanel {

    // ─── COLORES ───────────────────────────────────
    private static final Color COLOR_BG          = new Color(0xFBF5EC);
    private static final Color COLOR_ACCENT      = new Color(0x5C1F08);
    private static final Color COLOR_DIVIDER     = new Color(0xC8A882);
    private static final Color COLOR_MESA_HEADER = new Color(0x9B3D18);
    private static final Color COLOR_NUM_BG      = new Color(0x7A2E10);
    private static final Color COLOR_TOTAL_BG    = new Color(0xBE5A33);
    private static final Color COLOR_BTN_COBRAR  = new Color(0x7A2000);
    private static final Color COLOR_BTN_REGRESAR= new Color(0xEDE0D0);

    // ─── ESTADO DE RED Y BD ────────────────────────
    private ApiService apiService;
    private int pedidoIdActual;
    private double montoRecibidoActual = 0.0;

    // ─── COMPONENTES ───────────────────────────────
    private JLabel    lblMesaTitulo, lblMeseroInfo;
    private JPanel    panelItems;
    private JLabel    lblTotalPedido;
    private JLabel    lblTotalCobro, lblPagoRecibido, lblCambio;
    private JButton   btnEfectivo, btnTarjeta, btnMixto, btnActivo;
    private JButton   btnCobrar;
    private JCheckBox chkFactura, chkTicket;

    private VentanaPrincipal ventana;

    // ─── CONSTRUCTOR ───────────────────────────────
    public PanelCobro() {
        setLayout(new GridLayout(1, 2, 0, 0));
        setBackground(COLOR_BG);
        add(buildColumnaPedido());
        add(buildColumnaCobro());
    }

    public void setVentana(VentanaPrincipal v) {
        this.ventana = v;
    }

    /**
     * IMPORTANTE: Inicializa la conexión con el token del cajero
     */
    public void inicializarConexion(String tokenSanctum) {
        this.apiService = RetrofitClient.getClient(tokenSanctum).create(ApiService.class);
    }

    // ═══════════════════════════════════════════════
    // COLUMNA IZQUIERDA — PEDIDO (alineado izquierda)
    // ═══════════════════════════════════════════════
    private JPanel buildColumnaPedido() {
        JPanel col = new JPanel(new BorderLayout(0, 0));
        col.setBackground(COLOR_BG);
        col.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 28));

        JLabel lblTitulo = new JLabel("Pedido");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        lblTitulo.setForeground(COLOR_ACCENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_DIVIDER);

        JPanel tituloBox = new JPanel(new BorderLayout());
        tituloBox.setOpaque(false);
        tituloBox.add(lblTitulo, BorderLayout.WEST);
        tituloBox.add(sep, BorderLayout.SOUTH);

        lblMesaTitulo = new JLabel("Cargando...");
        lblMesaTitulo.setFont(new Font("Arial", Font.BOLD, 15));
        lblMesaTitulo.setForeground(Color.WHITE);

        lblMeseroInfo = new JLabel("Obteniendo datos...");
        lblMeseroInfo.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMeseroInfo.setForeground(new Color(0xF5DEC8));

        JPanel mesaContent = new JPanel();
        mesaContent.setLayout(new BoxLayout(mesaContent, BoxLayout.Y_AXIS));
        mesaContent.setOpaque(false);
        mesaContent.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        mesaContent.add(lblMesaTitulo);
        mesaContent.add(Box.createRigidArea(new Dimension(0, 4)));
        mesaContent.add(lblMeseroInfo);

        JPanel mesaHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_MESA_HEADER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        mesaHeader.setOpaque(false);
        mesaHeader.add(mesaContent, BorderLayout.CENTER);

        JPanel norte = new JPanel();
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.setOpaque(false);
        norte.add(tituloBox);
        norte.add(Box.createRigidArea(new Dimension(0, 14)));
        norte.add(mesaHeader);
        norte.add(Box.createRigidArea(new Dimension(0, 8)));

        panelItems = new JPanel();
        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));
        panelItems.setOpaque(false);
        panelItems.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scroll = new JScrollPane(panelItems, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        lblTotalPedido = new JLabel("Total: $0.00");
        lblTotalPedido.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalPedido.setForeground(COLOR_ACCENT);
        lblTotalPedido.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        col.add(norte, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);
        col.add(lblTotalPedido, BorderLayout.SOUTH);
        return col;
    }

    // ═══════════════════════════════════════════════
    // COLUMNA DERECHA — COBRO
    // ═══════════════════════════════════════════════
    private JPanel buildColumnaCobro() {
        JPanel col = new JPanel(new BorderLayout());
        col.setBackground(COLOR_BG);
        col.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_DIVIDER),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Cobro");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 30));
        lblTitulo.setForeground(COLOR_ACCENT);

        JButton btnRegresar = buildBotonRegresar();
        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(btnRegresar, BorderLayout.EAST);

        inner.add(filaTitulo);
        inner.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel lblMetodo = buildLabelGris("Método de pago");
        inner.add(lblMetodo);
        inner.add(Box.createRigidArea(new Dimension(0, 8)));
        inner.add(buildMetodosPago());
        inner.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel labelsPC = new JPanel(new GridLayout(1, 2, 12, 0));
        labelsPC.setOpaque(false);
        labelsPC.add(buildLabelGris("Pago Recibido"));
        labelsPC.add(buildLabelGris("Cambio"));
        inner.add(labelsPC);
        inner.add(Box.createRigidArea(new Dimension(0, 6)));

        lblPagoRecibido = new JLabel("$0.00", SwingConstants.CENTER);
        lblPagoRecibido.setFont(new Font("Arial", Font.PLAIN, 20));
        lblCambio = new JLabel("$0.00", SwingConstants.CENTER);
        lblCambio.setFont(new Font("Arial", Font.PLAIN, 20));

        JPanel camposPC = new JPanel(new GridLayout(1, 2, 12, 0));
        camposPC.setOpaque(false);
        camposPC.add(buildCampoRedondeado(lblPagoRecibido));
        camposPC.add(buildCampoRedondeado(lblCambio));
        inner.add(camposPC);
        inner.add(Box.createRigidArea(new Dimension(0, 16)));

        chkFactura = buildCheckbox("Solicitar factura");
        chkTicket  = buildCheckbox("Solicitar ticket");
        inner.add(chkFactura);
        inner.add(Box.createRigidArea(new Dimension(0, 8)));
        inner.add(chkTicket);
        inner.add(Box.createRigidArea(new Dimension(0, 18)));

        inner.add(buildTarjetaTotal());
        inner.add(Box.createRigidArea(new Dimension(0, 12)));

        btnCobrar = buildBotonCobrar();
        inner.add(btnCobrar);

        col.add(inner, BorderLayout.NORTH);
        return col;
    }

    private JButton buildBotonRegresar() {
        JButton btn = new JButton("← Mesas") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN_REGRESAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
                g2.setColor(COLOR_ACCENT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> regresarAMesas());
        return btn;
    }

    private JPanel buildMetodosPago() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        btnEfectivo = buildBtnMetodo("Efectivo");
        btnTarjeta  = buildBtnMetodo("Tarjeta");
        btnMixto    = buildBtnMetodo("Mixto");

        btnEfectivo.addActionListener(e -> activarMetodo(btnEfectivo));
        btnTarjeta.addActionListener(e  -> activarMetodo(btnTarjeta));
        btnMixto.addActionListener(e    -> activarMetodo(btnMixto));

        p.add(btnEfectivo); p.add(btnTarjeta); p.add(btnMixto);
        activarMetodo(btnEfectivo);
        return p;
    }

    private JButton buildBtnMetodo(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean on = (this == btnActivo);
                g2.setColor(on ? new Color(0xFBF5EC) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(on ? COLOR_TOTAL_BG : COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(on ? 2.2f : 1.4f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 14, 14);
                g2.setColor(on ? COLOR_TOTAL_BG : new Color(0x444444));
                g2.setFont(on ? getFont().deriveFont(Font.BOLD,14f) : getFont().deriveFont(Font.PLAIN,14f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(115, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void activarMetodo(JButton btn) {
        btnActivo = btn;
        btnEfectivo.repaint(); btnTarjeta.repaint(); btnMixto.repaint();
    }

    private JPanel buildCampoRedondeado(JLabel lbl) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(COLOR_DIVIDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 14, 14);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JCheckBox buildCheckbox(String texto) {
        JCheckBox chk = new JCheckBox(texto);
        chk.setFont(new Font("Arial", Font.PLAIN, 14));
        chk.setOpaque(false);
        return chk;
    }

    private JPanel buildTarjetaTotal() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_TOTAL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(Integer.MAX_VALUE, 95));

        JLabel lblEt = new JLabel("Total a cobrar");
        lblEt.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEt.setForeground(new Color(0xF5DEC8));

        lblTotalCobro = new JLabel("$0.00");
        lblTotalCobro.setFont(new Font("Arial", Font.BOLD, 36));
        lblTotalCobro.setForeground(Color.WHITE);

        JPanel t = new JPanel();
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setOpaque(false);
        t.add(lblEt);
        t.add(lblTotalCobro);

        card.add(t, BorderLayout.CENTER);
        return card;
    }

    private JButton buildBotonCobrar() {
        JButton btn = new JButton("Cobrar $0.00") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN_COBRAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onCobrar());
        return btn;
    }

    private JLabel buildLabelGris(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(new Color(0x888888));
        return l;
    }

    // ═══════════════════════════════════════════════
    // CONEXIÓN CON BD: CARGAR PEDIDO REAL
    // ═══════════════════════════════════════════════
    public void cargarPedidoReal(int pedidoId) {
        if (apiService == null) {
            JOptionPane.showMessageDialog(this, "Error: ApiService no inicializado. Llama a inicializarConexion() primero.");
            return;
        }

        this.pedidoIdActual = pedidoId;
        lblMesaTitulo.setText("Cargando...");
        btnCobrar.setEnabled(false);

        // GET /api/v1/pedidos/{id}
        apiService.obtenerPedidoPorId(pedidoId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JsonObject pedido = new Gson().fromJson(jsonResponse, JsonObject.class);

                        // Actualizar UI en el hilo de Swing
                        SwingUtilities.invokeLater(() -> renderizarPedidoEnUI(pedido));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(PanelCobro.this, "Error al obtener pedido: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(PanelCobro.this, "Fallo de red: " + t.getMessage()));
            }
        });
    }

    private void renderizarPedidoEnUI(JsonObject pedido) {
        // Cabecera Mesa
        if (pedido.has("mesa") && !pedido.get("mesa").isJsonNull()) {
            lblMesaTitulo.setText("Mesa " + pedido.getAsJsonObject("mesa").get("numero").getAsString());
        }
        if (pedido.has("mesero") && !pedido.get("mesero").isJsonNull()) {
            lblMeseroInfo.setText("Mesero: " + pedido.getAsJsonObject("mesero").get("name").getAsString());
        }

        // Limpiar items anteriores
        panelItems.removeAll();

        JsonArray detalles = pedido.getAsJsonArray("detalles");
        double totalCalculado = pedido.get("total").getAsDouble();

        if (detalles != null) {
            for (int i = 0; i < detalles.size(); i++) {
                JsonObject item = detalles.get(i).getAsJsonObject();
                int cant = item.get("cantidad").getAsInt();
                String nombre = item.getAsJsonObject("producto").get("nombre").getAsString();
                double sub = item.get("subtotal").getAsDouble();

                panelItems.add(buildRowItem(cant, nombre, sub));

                JSeparator s = new JSeparator();
                s.setForeground(new Color(0xDDCCBB));
                s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                panelItems.add(s);
            }
        }

        // Totales
        lblTotalPedido.setText(String.format("<html><b>Total:</b> $%.2f</html>", totalCalculado));
        lblTotalCobro.setText(String.format("$%.2f", totalCalculado));
        btnCobrar.setText(String.format("Cobrar $%.2f", totalCalculado));
        btnCobrar.setEnabled(true);

        // Datos del pre-cobro del mesero
        if (pedido.has("pago_efectivo") && !pedido.get("pago_efectivo").isJsonNull()) {
            montoRecibidoActual = pedido.get("pago_efectivo").getAsDouble();
            lblPagoRecibido.setText(String.format("$%.2f", montoRecibidoActual));

            double cambio = Math.max(0, montoRecibidoActual - totalCalculado);
            lblCambio.setText(String.format("$%.2f", cambio));
        }

        if (pedido.has("requiere_factura") && !pedido.get("requiere_factura").isJsonNull()) {
            chkFactura.setSelected(pedido.get("requiere_factura").getAsBoolean());
        }

        panelItems.revalidate();
        panelItems.repaint();
    }

    private JPanel buildRowItem(int cantidad, String nombre, double subtotal) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 2));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel num = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_NUM_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = String.valueOf(cantidad);
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        num.setOpaque(false);
        num.setPreferredSize(new Dimension(36, 36));
        num.setMaximumSize(new Dimension(36, 36));

        JLabel lblN = new JLabel(nombre);
        lblN.setFont(new Font("Arial", Font.PLAIN, 15));

        JLabel lblS = new JLabel(String.format("$%.2f", subtotal), SwingConstants.RIGHT);
        lblS.setFont(new Font("Arial", Font.PLAIN, 15));

        row.add(num, BorderLayout.WEST);
        row.add(lblN, BorderLayout.CENTER);
        row.add(lblS, BorderLayout.EAST);
        return row;
    }

    // ═══════════════════════════════════════════════
    // CONEXIÓN CON BD: PROCESAR PAGO REAL
    // ═══════════════════════════════════════════════
    private void onCobrar() {
        if (pedidoIdActual == 0) return;

        String metodoUI = btnActivo == btnEfectivo ? "efectivo" : btnActivo == btnTarjeta ? "tarjeta" : "mixto";

        int ok = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "¿Confirmar cobro por " + lblTotalCobro.getText() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (ok == JOptionPane.YES_OPTION) {
            btnCobrar.setEnabled(false);

            // Preparar el JSON para enviar al backend
            Map<String, Object> payload = new HashMap<>();
            payload.put("pedido_id", pedidoIdActual);
            payload.put("metodo_pago", metodoUI);
            payload.put("monto_recibido", montoRecibidoActual);
            payload.put("propina", 0.00);
            payload.put("requiere_factura", chkFactura.isSelected());

            // POST /api/v1/pagos/cobrar
            apiService.procesarPago(payload).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    SwingUtilities.invokeLater(() -> {
                        if (response.isSuccessful()) {
                            JOptionPane.showMessageDialog(PanelCobro.this, "✅ Cobro registrado correctamente.");
                            regresarAMesas();
                        } else {
                            btnCobrar.setEnabled(true);
                            JOptionPane.showMessageDialog(PanelCobro.this, "❌ Error al cobrar: " + response.code());
                        }
                    });
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    SwingUtilities.invokeLater(() -> {
                        btnCobrar.setEnabled(true);
                        JOptionPane.showMessageDialog(PanelCobro.this, "Error de red: " + t.getMessage());
                    });
                }
            });
        }
    }

    private void regresarAMesas() {
        if (ventana != null) {
            ventana.navegarAMesas();
        }
    }
}