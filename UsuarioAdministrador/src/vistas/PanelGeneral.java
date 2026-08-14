package vistas;

import servicios.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista: PanelGeneral — Dashboard del Administrador
 */
public class PanelGeneral extends JPanel implements Actualizables {

    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_DIV       = new Color(0xC8A882);
    private static final Color C_BANNER    = new Color(0xA0401A);
    private static final Color C_BSEP      = new Color(0xBF6030);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_BAR_BG   = new Color(0xEEDDCC);
    private static final Color C_BAR_EF   = new Color(0x6B2D1A);
    private static final Color C_BAR_TA   = new Color(0xD48000);
    private static final Color C_BAR_MI   = new Color(0xC03020);
    private static final Color C_ACT_BG   = new Color(0xFDF3E7);
    private static final Color C_ACT_BOR  = new Color(0xD48000);
    private static final Color C_PAU_BG   = new Color(0xF0F0F0);
    private static final Color C_BTN_DK   = new Color(0x3A2010);
    private static final Color C_BTN_OR   = new Color(0xD48000);

    private static final int HEADER_MAX_HEIGHT = 148;
    private static final int FILA_ALTO = 420;

    private final VentanaAdmin ventana;
    private ApiClient apiClient;

    public static class Producto {
        String nombre, categoria, precio, estado;
        public Producto(String n, String c, String p, String e) {
            nombre=n; categoria=c; precio=p; estado=e;
        }
    }

    public static class ItemInventario {
        String nombre, cantidad;
        double fraccion;
        Color colorBarra;
        public ItemInventario(String n, String c, double f, Color col) {
            nombre=n; cantidad=c; fraccion=f; colorBarra=col;
        }
    }

    public static class Asistencia {
        String nombre, rol;
        public Asistencia(String n, String r) { nombre=n; rol=r; }
    }

    private String dVentasHoy = "$0.00", dOrdenes = "0", dVentasSem = "$0.00", dPersonal = "0";
    private final List<Producto>       productos   = new ArrayList<>();
    private final List<ItemInventario> inventario  = new ArrayList<>();
    private final List<Asistencia>     asistencias = new ArrayList<>();
    private double dEfM=0, dTaM=0, dMiM=0;
    private String dEfS="$0.00", dTaS="$0.00", dMiS="$0.00";

    private JLabel lblVH, lblOrd, lblVS, lblPer;
    private JPanel pProductos, pInventario, pAsistencias;
    private Barra  bEf, bTa, bMi;
    private JLabel lEf, lTa, lMi;

    public PanelGeneral(VentanaAdmin ventana) {
        this(ventana, new ApiClient());
    }

    public PanelGeneral(VentanaAdmin ventana, ApiClient apiClient) {
        this.ventana = ventana;
        this.apiClient = apiClient;
        setLayout(new BorderLayout());
        setBackground(C_BG);

        inicializarDummy();

        JPanel contenido = buildContenido();
        JScrollPane scrollGeneral = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollGeneral.setBorder(BorderFactory.createEmptyBorder());
        scrollGeneral.getViewport().setBackground(C_BG);
        scrollGeneral.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollGeneral, BorderLayout.CENTER);
        actualizarDatos();

        if (this.apiClient != null) {
            cargarDatosDesdeBackend(this.apiClient);
        }
    }

    @Override
    public void recargarDatos() {
        if (this.apiClient != null) {
            cargarDatosDesdeBackend(this.apiClient);
        }
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        cargarDatosDesdeBackend(this.apiClient);
    }

    private JPanel buildContenido() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 28, 24, 28));

        p.add(buildHeader());
        p.add(Box.createVerticalStrut(16));
        p.add(buildCuerpo());
        p.add(Box.createVerticalGlue());

        return p;
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JLabel t = new JLabel("Panel General");
        t.setFont(new Font("Arial", Font.BOLD, 26));
        t.setForeground(C_ACCENT);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(t, BorderLayout.WEST);

        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(titlePanel, BorderLayout.NORTH);
        top.add(sep, BorderLayout.CENTER);

        p.add(top, BorderLayout.NORTH);
        p.add(buildBanner(), BorderLayout.CENTER);

        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEADER_MAX_HEIGHT));
        p.setPreferredSize(new Dimension(0, HEADER_MAX_HEIGHT));

        return p;
    }

    private JPanel buildBanner() {
        JPanel ban = new JPanel(new GridLayout(1, 7, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BANNER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        ban.setOpaque(false);
        ban.setPreferredSize(new Dimension(0, 84));
        ban.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        lblVH  = mkVB("$0.00"); lblOrd = mkVB("0");
        lblVS  = mkVB("$0.00"); lblPer = mkVB("0");

        ban.add(mkCB("VENTAS HOY",  lblVH));  ban.add(mkSB());
        ban.add(mkCB("ÓRDENES",     lblOrd)); ban.add(mkSB());
        ban.add(mkCB("VENTAS SEM.", lblVS));  ban.add(mkSB());
        ban.add(mkCB("PERSONAL",    lblPer));
        return ban;
    }

    private JPanel mkCB(String et, JLabel v) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        JLabel le = new JLabel(et, SwingConstants.CENTER);
        le.setFont(new Font("Arial", Font.BOLD, 12));
        le.setForeground(new Color(0xF5DEC8));
        le.setAlignmentX(CENTER_ALIGNMENT);
        v.setAlignmentX(CENTER_ALIGNMENT);
        p.add(le);
        p.add(Box.createRigidArea(new Dimension(0,3)));
        p.add(v);
        return p;
    }

    private JLabel mkVB(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 26));
        l.setForeground(C_WHITE);
        return l;
    }

    private JPanel mkSB() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(C_BSEP);
                g.fillRect(getWidth()/2, 10, 1, getHeight()-20);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPanel buildCuerpo() {
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);
        cuerpo.setMaximumSize(new Dimension(Integer.MAX_VALUE, FILA_ALTO + 20 + FILA_ALTO));

        JPanel fila1 = new JPanel(new GridLayout(1, 2, 16, 0));
        fila1.setOpaque(false);
        fila1.setPreferredSize(new Dimension(0, FILA_ALTO));
        fila1.setMaximumSize(new Dimension(Integer.MAX_VALUE, FILA_ALTO));
        fila1.add(buildCardCatalogo());
        fila1.add(buildCardInventario());

        JPanel fila2 = new JPanel(new GridLayout(1, 3, 16, 0));
        fila2.setOpaque(false);
        fila2.setPreferredSize(new Dimension(0, FILA_ALTO));
        fila2.setMaximumSize(new Dimension(Integer.MAX_VALUE, FILA_ALTO));
        fila2.add(buildCardCorte());
        fila2.add(buildCardAsistencias());
        fila2.add(buildCardAcciones());

        cuerpo.add(fila1);
        cuerpo.add(Box.createVerticalStrut(16));
        cuerpo.add(fila2);

        return cuerpo;
    }

    private JPanel buildCardCatalogo() {
        JPanel card = mkCard();
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel tit = mkTitCard("Catálogo de productos");
        card.add(tit, BorderLayout.NORTH);

        pProductos = new JPanel(new GridBagLayout());
        pProductos.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pProductos,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void poblarProductos() {
        pProductos.removeAll();
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        g.insets = new Insets(9, 0, 9, 14);

        for (Producto p : productos) {
            g.gridx=0; g.weightx=0.40; pProductos.add(mkLProd(p.nombre, false), g);
            g.gridx=1; g.weightx=0.25; pProductos.add(mkLProd(p.categoria, true), g);
            g.gridx=2; g.weightx=0.15; pProductos.add(mkLProd(p.precio, false), g);
            g.gridx=3; g.weightx=0.20; pProductos.add(mkBadge(p.estado), g);
            g.gridy++;
        }
        pProductos.revalidate(); pProductos.repaint();
    }

    private JLabel mkLProd(String t, boolean gris) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.PLAIN, 15));
        l.setForeground(gris ? new Color(0x888888) : new Color(0x333333));
        return l;
    }

    private JLabel mkBadge(String estado) {
        boolean activo = estado.equalsIgnoreCase("Activo");
        JLabel b = new JLabel(estado, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(activo ? C_ACT_BG : C_PAU_BG);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(activo ? C_ACT_BOR : new Color(0xBBBBBB));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setForeground(activo ? C_ACT_BOR : new Color(0x777777));
        b.setPreferredSize(new Dimension(84, 26));
        b.setOpaque(false);
        return b;
    }

    private JPanel buildCardInventario() {
        JPanel card = mkCard();
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.add(mkTitCard("Inventario"), BorderLayout.NORTH);

        pInventario = new JPanel();
        pInventario.setLayout(new BoxLayout(pInventario, BoxLayout.Y_AXIS));
        pInventario.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pInventario,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void poblarInventario() {
        pInventario.removeAll();
        for (ItemInventario item : inventario) {
            JPanel fila = new JPanel(new BorderLayout(14, 0));
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel lN = new JLabel(item.nombre);
            lN.setFont(new Font("Arial", Font.PLAIN, 15));
            lN.setForeground(new Color(0x333333));
            lN.setPreferredSize(new Dimension(130, 24));

            Barra b = new Barra(item.colorBarra, C_BAR_BG);
            b.setFr(item.fraccion);
            b.setPreferredSize(new Dimension(100, 14));

            JLabel lC = new JLabel(item.cantidad, SwingConstants.RIGHT);
            lC.setFont(new Font("Arial", Font.PLAIN, 14));
            lC.setForeground(new Color(0x666666));
            lC.setPreferredSize(new Dimension(70, 24));

            fila.add(lN, BorderLayout.WEST);
            fila.add(b,  BorderLayout.CENTER);
            fila.add(lC, BorderLayout.EAST);

            pInventario.add(fila);
            pInventario.add(Box.createVerticalStrut(16));
        }
        pInventario.revalidate(); pInventario.repaint();
    }

    private JPanel buildCardCorte() {
        JPanel card = mkCard();
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.add(mkTitCard("Corte de caja"), BorderLayout.NORTH);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        lEf = mkLM(); bEf = new Barra(C_BAR_EF, C_BAR_BG);
        lTa = mkLM(); bTa = new Barra(C_BAR_TA, C_BAR_BG);
        lMi = mkLM(); bMi = new Barra(C_BAR_MI, C_BAR_BG);

        inner.add(mkFilaCorte("Efectivo", lEf, bEf));
        inner.add(Box.createVerticalStrut(24));
        inner.add(mkFilaCorte("Tarjeta",  lTa, bTa));
        inner.add(Box.createVerticalStrut(24));
        inner.add(mkFilaCorte("Mixto",    lMi, bMi));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JLabel mkLM() {
        JLabel l = new JLabel("", SwingConstants.RIGHT);
        l.setFont(new Font("Arial", Font.BOLD, 16));
        l.setForeground(new Color(0x333333));
        l.setPreferredSize(new Dimension(80, 24));
        return l;
    }

    private JPanel mkFilaCorte(String nom, JLabel lM, Barra b) {
        JPanel fila = new JPanel(new BorderLayout(14, 0));
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lN = new JLabel(nom);
        lN.setFont(new Font("Arial", Font.PLAIN, 15));
        lN.setForeground(new Color(0x333333));
        lN.setPreferredSize(new Dimension(85, 24));

        b.setPreferredSize(new Dimension(100, 14));
        fila.add(lN, BorderLayout.WEST);
        fila.add(b,  BorderLayout.CENTER);
        fila.add(lM, BorderLayout.EAST);
        return fila;
    }

    private JPanel buildCardAsistencias() {
        JPanel card = mkCard();
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.add(mkTitCard("Asistencias hoy"), BorderLayout.NORTH);

        pAsistencias = new JPanel();
        pAsistencias.setLayout(new BoxLayout(pAsistencias, BoxLayout.Y_AXIS));
        pAsistencias.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pAsistencias,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(10);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void poblarAsistencias() {
        pAsistencias.removeAll();
        for (Asistencia a : asistencias) {
            JPanel fila = new JPanel(new BorderLayout());
            fila.setOpaque(false);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

            JLabel lN = new JLabel(a.nombre);
            lN.setFont(new Font("Arial", Font.PLAIN, 15));
            lN.setForeground(new Color(0x333333));

            JLabel lR = new JLabel(a.rol, SwingConstants.RIGHT);
            lR.setFont(new Font("Arial", Font.PLAIN, 13));
            lR.setForeground(new Color(0x888888));

            fila.add(lN, BorderLayout.WEST);
            fila.add(lR, BorderLayout.EAST);
            pAsistencias.add(fila);
            pAsistencias.add(Box.createVerticalStrut(10));
        }
        pAsistencias.revalidate(); pAsistencias.repaint();
    }

    private JPanel buildCardAcciones() {
        JPanel card = mkCard();
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.add(mkTitCard("Acciones rápidas"), BorderLayout.NORTH);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        inner.add(mkBtnAccion("Agregar producto",  true,  "MENU"));
        inner.add(Box.createVerticalStrut(14));
        inner.add(mkBtnAccion("Crear combo",       false, "COMBOS"));
        inner.add(Box.createVerticalStrut(14));
        inner.add(mkBtnAccion("Registrar empleado",false, "EMPLEADOS"));
        inner.add(Box.createVerticalStrut(14));
        inner.add(mkBtnAccion("Inventario",        false, "INVENTARIO"));

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JButton mkBtnAccion(String texto, boolean primario, String cardKey) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primario) {
                    g2.setColor(C_BTN_DK);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
                    g2.setColor(C_WHITE);
                } else {
                    g2.setColor(C_WHITE);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),22,22);
                    g2.setColor(C_BTN_OR);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,22,22);
                    g2.setColor(C_BTN_OR);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", primario ? Font.BOLD : Font.PLAIN, 15));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (ventana != null) ventana.navegarA(cardKey);
        });

        return btn;
    }

    private static class Barra extends JPanel {
        private final Color cF, cB;
        private double fr = 0;

        Barra(Color f, Color b) {
            cF=f; cB=b; setOpaque(false);
            setPreferredSize(new Dimension(100, 10));
            setMinimumSize(new Dimension(10, 10));
        }

        void setFr(double f) {
            fr = Math.max(0, Math.min(1, f));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int y = (h - 10) / 2;
            g2.setColor(cB);
            g2.fillRoundRect(0, y, w, 10, 10, 10);

            int fw = (int) (w * fr);
            if (fw > 0) {
                g2.setColor(cF);
                g2.fillRoundRect(0, y, fw, 10, 10, 10);
            }
        }
    }

    private JLabel mkTitCard(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Arial", Font.BOLD, 18));
        l.setForeground(C_ACCENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        return l;
    }

    private JPanel mkCard() {
        JPanel c = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(C_DIV);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            }
        };
        c.setOpaque(false);
        return c;
    }

    public void actualizarDatos() {
        lblVH.setText(dVentasHoy); lblOrd.setText(dOrdenes);
        lblVS.setText(dVentasSem); lblPer.setText(dPersonal);

        poblarProductos();
        poblarInventario();

        double tot = dEfM + dTaM + dMiM; if (tot == 0) tot = 1;
        lEf.setText(dEfS); bEf.setFr(dEfM / tot);
        lTa.setText(dTaS); bTa.setFr(dTaM / tot);
        lMi.setText(dMiS); bMi.setFr(dMiM / tot);

        poblarAsistencias();
        repaint();
    }

    private void inicializarDummy() {
        productos.add(new Producto("Enchiladas verdes","Plato fuerte","$85.00","Activo"));
        productos.add(new Producto("Pozole rojo",      "Plato fuerte","$95.00","Activo"));
        productos.add(new Producto("Tostadas de pata", "Entrada",     "$45.00","Pausado"));
        productos.add(new Producto("Agua de Jamaica",  "Bebida",      "$20.00","Activo"));

        inventario.add(new ItemInventario("Jitomate",    "2 kg",   0.15, C_BAR_MI));
        inventario.add(new ItemInventario("Queso Oaxaca","500 g",  0.35, C_BAR_TA));
        inventario.add(new ItemInventario("Pollo",       "4.5 kg", 0.60, C_BAR_EF));

        asistencias.add(new Asistencia("Hasiel","Mesero"));
        asistencias.add(new Asistencia("Marcos","Mesero"));
    }

    private JSONArray obtenerJsonArraySeguro(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return new JSONArray();
        }
        String trimmed = jsonText.trim();
        if (trimmed.startsWith("{")) {
            JSONObject obj = new JSONObject(trimmed);
            if (obj.has("data") && !obj.isNull("data")) {
                return obj.getJSONArray("data");
            }
            return new JSONArray();
        } else if (trimmed.startsWith("[")) {
            return new JSONArray(trimmed);
        }
        return new JSONArray();
    }

    private String formatearMoneda(Object val) {
        if (val == null) return "$0.00";
        if (val instanceof Number) {
            return String.format("$%,.2f", ((Number) val).doubleValue());
        }
        String s = val.toString().trim();
        if (s.startsWith("$")) return s;
        try {
            double d = Double.parseDouble(s);
            return String.format("$%,.2f", d);
        } catch (Exception e) {
            return s.isEmpty() ? "$0.00" : s;
        }
    }

    public void cargarDatosDesdeBackend(ApiClient client) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String vh = dVentasHoy, ord = dOrdenes, vs = dVentasSem, per = dPersonal;
            private final List<Producto> listaProds = new ArrayList<>();
            private final List<ItemInventario> listaInv = new ArrayList<>();
            private final List<Asistencia> listaAsist = new ArrayList<>();
            private double ef = dEfM, ta = dTaM, mi = dMiM;
            private String efS = dEfS, taS = dTaS, miS = dMiS;

            @Override
            protected Void doInBackground() {
                // 1. Cargar Reportes / Corte (Parsing flexible plano o anidado)
                try {
                    String jsonRep = client.obtenerDashboardReportes();
                    if (jsonRep != null && jsonRep.trim().startsWith("{")) {
                        JSONObject objRep = new JSONObject(jsonRep);

                        JSONObject resumen = objRep.optJSONObject("resumen");
                        if (resumen != null) {
                            vh = formatearMoneda(resumen.opt("ventas_hoy"));
                            ord = String.valueOf(resumen.optInt("ordenes_hoy", resumen.optInt("pedidos_activos", 0)));
                            vs = formatearMoneda(resumen.opt("ventas_semana"));
                            per = String.valueOf(resumen.optInt("personal_activo", resumen.optInt("mesas_ocupadas", 0)));
                        } else {
                            if (objRep.has("ventas_hoy")) vh = formatearMoneda(objRep.get("ventas_hoy"));
                            if (objRep.has("ordenes_hoy")) ord = String.valueOf(objRep.optInt("ordenes_hoy", 0));
                            if (objRep.has("ventas_semana")) vs = formatearMoneda(objRep.get("ventas_semana"));
                            if (objRep.has("personal_activo")) per = String.valueOf(objRep.optInt("personal_activo", 0));
                        }

                        JSONObject corte = objRep.optJSONObject("corte_caja");
                        if (corte != null) {
                            ef = corte.optDouble("efectivo_monto", corte.optDouble("efectivo", 0.0));
                            efS = corte.has("efectivo_texto") ? corte.optString("efectivo_texto") : formatearMoneda(ef);

                            ta = corte.optDouble("tarjeta_monto", corte.optDouble("tarjeta", 0.0));
                            taS = corte.has("tarjeta_texto") ? corte.optString("tarjeta_texto") : formatearMoneda(ta);

                            mi = corte.optDouble("mixto_monto", corte.optDouble("mixto", 0.0));
                            miS = corte.has("mixto_texto") ? corte.optString("mixto_texto") : formatearMoneda(mi);
                        } else if (objRep.has("metodos_pago")) {
                            JSONArray metodos = objRep.optJSONArray("metodos_pago");
                            if (metodos != null) {
                                ef = 0; ta = 0; mi = 0;
                                for (int i = 0; i < metodos.length(); i++) {
                                    JSONObject m = metodos.getJSONObject(i);
                                    String tipo = m.optString("metodo_pago", m.optString("metodo", "")).toLowerCase();
                                    double total = m.optDouble("total", m.optDouble("monto", 0.0));
                                    if (tipo.contains("efectivo")) ef += total;
                                    else if (tipo.contains("tarjeta")) ta += total;
                                    else mi += total;
                                }
                                efS = formatearMoneda(ef);
                                taS = formatearMoneda(ta);
                                miS = formatearMoneda(mi);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar reportes: " + e.getMessage());
                }

                // 2. Cargar Productos
                try {
                    String jsonProds = client.obtenerProductos();
                    JSONArray arrProds = obtenerJsonArraySeguro(jsonProds);

                    for (int i = 0; i < arrProds.length(); i++) {
                        JSONObject p = arrProds.getJSONObject(i);
                        String nombre = p.optString("nombre", p.optString("name", "Producto"));

                        String cat = "General";
                        if (p.has("categoria") && !p.isNull("categoria")) {
                            Object catObj = p.get("categoria");
                            if (catObj instanceof JSONObject) {
                                cat = ((JSONObject) catObj).optString("nombre", "General");
                            } else {
                                cat = catObj.toString();
                            }
                        } else if (p.has("category") && !p.isNull("category")) {
                            Object catObj = p.get("category");
                            if (catObj instanceof JSONObject) {
                                cat = ((JSONObject) catObj).optString("nombre", "General");
                            } else {
                                cat = catObj.toString();
                            }
                        }

                        double precVal = p.optDouble("precio", p.optDouble("price", 0.0));
                        String precio = String.format("$%.2f", precVal);

                        boolean disponible = true;
                        if (p.has("is_disponible") && !p.isNull("is_disponible")) {
                            disponible = p.optBoolean("is_disponible", true);
                        } else if (p.has("disponible") && !p.isNull("disponible")) {
                            disponible = p.optBoolean("disponible", true);
                        }

                        String estadoRaw = p.optString("estado", p.optString("status", "")).toLowerCase();
                        boolean tienePausadoHasta = p.has("pausado_hasta")
                                && !p.isNull("pausado_hasta")
                                && !p.optString("pausado_hasta").trim().isEmpty();

                        String estado = "Activo";
                        if (!disponible || tienePausadoHasta || estadoRaw.contains("paus") || estadoRaw.contains("inactiv") || estadoRaw.equals("false") || estadoRaw.equals("0")) {
                            estado = "Pausado";
                        }

                        listaProds.add(new Producto(nombre, cat, precio, estado));
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar productos: " + e.getMessage());
                }

                // 3. Cargar Insumos (Inventario)
                try {
                    String jsonInsumos = client.obtenerInsumos();
                    JSONArray arrIns = obtenerJsonArraySeguro(jsonInsumos);

                    for (int i = 0; i < arrIns.length(); i++) {
                        JSONObject ins = arrIns.getJSONObject(i);
                        String nombre = ins.optString("nombre", ins.optString("name", "Insumo"));

                        double stockAct = ins.has("stock_actual") ? ins.getDouble("stock_actual") : ins.optDouble("stock", 0.0);
                        double stockMin = ins.optDouble("stock_minimo", 5.0);
                        double stockMax = ins.optDouble("stock_maximo", Math.max(stockAct * 1.5, stockMin * 3));
                        String unidad = ins.optString("unidad_medida", ins.optString("unidad", ""));

                        double fraccion = (stockMax > 0) ? Math.min(1.0, stockAct / stockMax) : 0.5;

                        Color colBarra;
                        if (stockAct <= stockMin) {
                            colBarra = C_BAR_MI;
                        } else if (stockAct <= stockMin * 1.5) {
                            colBarra = C_BAR_TA;
                        } else {
                            colBarra = C_BAR_EF;
                        }

                        String ctnTexto = (stockAct % 1 == 0)
                                ? String.format("%.0f %s", stockAct, unidad).trim()
                                : String.format("%.1f %s", stockAct, unidad).trim();

                        listaInv.add(new ItemInventario(nombre, ctnTexto, fraccion, colBarra));
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar inventario: " + e.getMessage());
                }

                // 4. Cargar Empleados
                try {
                    String jsonEmp = client.obtenerEmpleados();
                    JSONArray arrEmp = obtenerJsonArraySeguro(jsonEmp);

                    for (int i = 0; i < arrEmp.length(); i++) {
                        JSONObject emp = arrEmp.getJSONObject(i);
                        String nombre = emp.optString("name", emp.optString("nombre", "Empleado"));
                        String rol = emp.optString("role", emp.optString("rol", "Mesero"));
                        listaAsist.add(new Asistencia(nombre, rol));
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar empleados: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                if (!listaProds.isEmpty()) setProductos(listaProds);
                if (!listaInv.isEmpty())   setInventario(listaInv);
                if (!listaAsist.isEmpty()) setAsistencias(listaAsist);

                setMetricas(vh, ord, vs, per);
                setCorte(ef, efS, ta, taS, mi, miS);
            }
        };

        worker.execute();
    }

    public void setMetricas(String vh, String ord, String vs, String per) {
        dVentasHoy=vh; dOrdenes=ord; dVentasSem=vs; dPersonal=per; actualizarDatos();
    }
    public void setProductos(List<Producto> l) { productos.clear(); productos.addAll(l); actualizarDatos(); }
    public void setInventario(List<ItemInventario> l) { inventario.clear(); inventario.addAll(l); actualizarDatos(); }
    public void setCorte(double ef,String efS,double ta,String taS,double mi,String miS) {
        dEfM=ef;dEfS=efS; dTaM=ta;dTaS=taS; dMiM=mi;dMiS=miS; actualizarDatos();
    }
    public void setAsistencias(List<Asistencia> l) { asistencias.clear(); asistencias.addAll(l); actualizarDatos(); }
}