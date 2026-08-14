package vistas;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import servicios.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Vista: PanelReportes — Reportes semanales del Administrador
 */
public class PanelReportes extends JPanel implements Actualizables {

    private ApiClient apiClient;

    @Override
    public void recargarDatos() {
        cargarDatosDesdeBackend();
    }

    private static final Color C_BG      = new Color(0xFBF5EC);
    private static final Color C_ACCENT  = new Color(0x6B2D1A);
    private static final Color C_DIV     = new Color(0xC8A882);
    private static final Color C_BANNER  = new Color(0xA0401A);
    private static final Color C_BSEP    = new Color(0xBF6030);
    private static final Color C_WHITE   = Color.WHITE;
    private static final Color C_BAR_BG  = new Color(0xEEDDCC);
    private static final Color C_BAR_EF  = new Color(0x6B2D1A);
    private static final Color C_BAR_TA  = new Color(0xD48000);
    private static final Color C_BAR_MI  = new Color(0xC03020);
    private static final Color C_BARRA   = new Color(0xBE5A33);
    private static final Color C_HDR_TBL = new Color(0x7A2E10);
    private static final Color C_ALT_ROW = new Color(0xFAF4EE);
    private static final Color C_GRID    = new Color(0xE8D8C8);
    private static final Color C_CHECK   = new Color(0x7A2E10);

    static class DatoVenta {
        String dia; double ventas; int mesas;
        DatoVenta(String d, double v, int m) { dia=d; ventas=v; mesas=m; }
    }

    static class DatoPropina {
        String dia, propina; int mesas;
        DatoPropina(String d, String p, int m) { dia=d; propina=p; mesas=m; }
    }

    static class Transaccion {
        String id, hora, detalle, metodo, monto; boolean cfdi;
        Transaccion(String id,String h,String d,String m,String mo,boolean c){
            this.id=id; hora=h; detalle=d; metodo=m; monto=mo; cfdi=c;
        }
    }

    private String dVT="$0.00", dOrd="0", dProp="$0.00", dCfdi="0";
    private final List<DatoVenta>   ventas        = new ArrayList<>();
    private final List<DatoPropina> propinas      = new ArrayList<>();
    private final List<Transaccion> transacciones = new ArrayList<>();
    private double dEfM=0, dTaM=0, dMiM=0;
    private String dEfS="$0.00", dTaS="$0.00", dMiS="$0.00";

    private JLabel        lblVT, lblOrd, lblProp, lblCfdi;
    private GraficaBarras grafica;
    private Barra         bEf, bTa, bMi;
    private JLabel        lEf, lTa, lMi, lPEf, lPTa, lPMi;
    private DefaultTableModel mdlDet, mdlProp2;
    private JPanel        panelTx;
    private JPanel        contenido;

    public PanelReportes() {
        apiClient = new ApiClient();

        setLayout(new BorderLayout());
        setBackground(C_BG);

        contenido = buildContenido();

        JScrollPane scroll = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);

        cargarDatosDesdeBackend();
    }

    private JPanel buildContenido() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(20, 32, 32, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx   = 0;

        JLabel tit = new JLabel("Reportes");
        tit.setFont(new Font("Arial", Font.BOLD, 30));
        tit.setForeground(C_ACCENT);
        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV);
        JPanel top = new JPanel(new BorderLayout(0,6));
        top.setBackground(C_BG);
        top.add(tit, BorderLayout.NORTH);
        top.add(sep, BorderLayout.CENTER);
        gc.gridy=0; gc.insets=new Insets(0,0,14,0);
        p.add(top, gc);

        gc.gridy=1; gc.insets=new Insets(0,0,16,0);
        p.add(buildBanner(), gc);

        JPanel fila1 = buildFila2Cols(
                buildCardGrafica(),
                buildCardMetodos(),
                280);
        gc.gridy=2; gc.insets=new Insets(0,0,16,0);
        p.add(fila1, gc);

        JPanel fila2 = buildFila2Cols(
                buildCardTabla("Detalles por día",
                        new String[]{"Día","Ventas","Mesas"}, true),
                buildCardTabla("Propinas",
                        new String[]{"Día","Propina","Mesas"}, false),
                285);
        gc.gridy=3; gc.insets=new Insets(0,0,24,0);
        p.add(fila2, gc);

        gc.gridy=4; gc.insets=new Insets(0,0,0,0);
        p.add(buildSeccionTx(), gc);

        return p;
    }

    private JPanel buildFila2Cols(JPanel izq, JPanel der, int altura) {
        return new JPanel(new BorderLayout(14,0)) {{
            setBackground(C_BG);
            setPreferredSize(new Dimension(0, altura));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, altura));
            setMinimumSize(new Dimension(0, altura));
            add(izq, BorderLayout.CENTER);

            JPanel derPanel = new JPanel(new GridLayout(1,1)) {
                @Override public Dimension getPreferredSize() {
                    Container p = getParent();
                    if (p == null) return new Dimension(400, altura);
                    return new Dimension((int)(p.getWidth() * 0.45), altura);
                }
            };
            derPanel.setBackground(C_BG);
            derPanel.add(der);
            add(derPanel, BorderLayout.EAST);
        }};
    }

    private JPanel buildBanner() {
        JPanel ban = new JPanel(new GridLayout(1,7,0,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BANNER);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
            }
        };
        ban.setOpaque(false);
        ban.setPreferredSize(new Dimension(0,70));
        ban.setMaximumSize(new Dimension(Integer.MAX_VALUE,70));

        lblVT=mkVB("$0.00"); lblOrd=mkVB("0");
        lblProp=mkVB("$0.00"); lblCfdi=mkVB("0");

        ban.add(mkCB("VENTAS TOTALES",lblVT)); ban.add(mkSB());
        ban.add(mkCB("ÓRDENES",       lblOrd)); ban.add(mkSB());
        ban.add(mkCB("PROPINAS",      lblProp)); ban.add(mkSB());
        ban.add(mkCB("MESAS OCUPADAS",lblCfdi));
        return ban;
    }

    private JPanel mkCB(String et, JLabel v) {
        JPanel p=new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        JLabel le=new JLabel(et,SwingConstants.CENTER);
        le.setFont(new Font("Arial",Font.PLAIN,11));
        le.setForeground(new Color(0xF5DEC8));
        le.setAlignmentX(CENTER_ALIGNMENT); v.setAlignmentX(CENTER_ALIGNMENT);
        p.add(le); p.add(Box.createRigidArea(new Dimension(0,3))); p.add(v);
        return p;
    }
    private JLabel mkVB(String t) {
        JLabel l=new JLabel(t,SwingConstants.CENTER);
        l.setFont(new Font("Arial",Font.BOLD,22)); l.setForeground(C_WHITE); return l;
    }
    private JPanel mkSB() {
        JPanel p=new JPanel(){@Override protected void paintComponent(Graphics g){
            g.setColor(C_BSEP); g.fillRect(getWidth()/2,10,1,getHeight()-20);}};
        p.setOpaque(false); p.setPreferredSize(new Dimension(2,0)); return p;
    }

    private JPanel buildCardGrafica() {
        JPanel card=mkCard();
        card.setLayout(new BorderLayout(0,10));
        card.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        grafica=new GraficaBarras();
        card.add(mkTit("Ventas por día"), BorderLayout.NORTH);
        card.add(grafica, BorderLayout.CENTER);
        return card;
    }

    private class GraficaBarras extends JPanel {
        private List<DatoVenta> datos=new ArrayList<>();
        GraficaBarras(){setOpaque(false);}
        void setDatos(List<DatoVenta> d){this.datos=new ArrayList<>(d);repaint();}
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(datos.isEmpty()) return;
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            int pL=46,pR=8,pT=8,pB=26;
            int aW=w-pL-pR, aH=h-pT-pB;
            if(aW<=0||aH<=0) return;

            double maxV=datos.stream().mapToDouble(d->d.ventas).max().orElse(1);
            double maxE=Math.ceil(maxV/1000.0)*1000;
            if (maxE == 0) maxE = 1000;

            g2.setFont(new Font("Arial",Font.PLAIN,10));
            for(int i=0;i<=6;i++){
                int y=pT+aH-(int)(aH*i/6.0);
                g2.setColor(C_GRID); g2.drawLine(pL,y,pL+aW,y);
                g2.setColor(new Color(0x999999));
                String lb=String.valueOf((int)(maxE*i/6));
                int lw=g2.getFontMetrics().stringWidth(lb);
                g2.drawString(lb,pL-lw-5,y+4);
            }

            int n=datos.size();
            int barW=Math.min(aW/(n+1),50);
            int espacio=(aW-barW*n)/(n+1);
            for(int i=0;i<n;i++){
                DatoVenta d=datos.get(i);
                int bH=(int)(aH*(d.ventas/maxE));
                int x=pL+espacio+i*(barW+espacio), y=pT+aH-bH;
                if(bH>0){
                    g2.setColor(C_BARRA);
                    g2.fillRoundRect(x,y,barW,bH,6,6);
                    if(bH>6) g2.fillRect(x,y+6,barW,bH-6);
                }
                g2.setFont(new Font("Arial",Font.PLAIN,11));
                g2.setColor(new Color(0x666666));
                int lw=g2.getFontMetrics().stringWidth(d.dia);
                g2.drawString(d.dia,x+(barW-lw)/2,pT+aH+18);
            }
        }
    }

    private JPanel buildCardMetodos() {
        JPanel card=mkCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));

        JPanel inner=new JPanel();
        inner.setLayout(new BoxLayout(inner,BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        inner.add(mkTit("Métodos de pago"));
        inner.add(Box.createRigidArea(new Dimension(0,16)));

        lEf=mkLM(); bEf=new Barra(C_BAR_EF,C_BAR_BG); lPEf=mkLP();
        lTa=mkLM(); bTa=new Barra(C_BAR_TA,C_BAR_BG); lPTa=mkLP();
        lMi=mkLM(); bMi=new Barra(C_BAR_MI,C_BAR_BG); lPMi=mkLP();

        inner.add(mkFilaMet("Efectivo",lEf,bEf,lPEf));
        inner.add(Box.createRigidArea(new Dimension(0,14)));
        inner.add(mkFilaMet("Tarjeta", lTa,bTa,lPTa));
        inner.add(Box.createRigidArea(new Dimension(0,14)));
        inner.add(mkFilaMet("Mixto",   lMi,bMi,lPMi));

        card.add(inner,BorderLayout.NORTH);
        return card;
    }

    private JPanel mkFilaMet(String nom,JLabel lM,Barra b,JLabel lP){
        JPanel f=new JPanel();
        f.setLayout(new BoxLayout(f,BoxLayout.Y_AXIS));
        f.setOpaque(false); f.setAlignmentX(LEFT_ALIGNMENT);

        JPanel top=new JPanel(new BorderLayout()); top.setOpaque(false);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE,22));
        JLabel lN=new JLabel(nom);
        lN.setFont(new Font("Arial",Font.PLAIN,14)); lN.setForeground(new Color(0x333333));
        lM.setFont(new Font("Arial",Font.PLAIN,14)); lM.setForeground(new Color(0x333333));
        lM.setHorizontalAlignment(SwingConstants.RIGHT);
        top.add(lN,BorderLayout.WEST); top.add(lM,BorderLayout.EAST);
        f.add(top);
        f.add(Box.createRigidArea(new Dimension(0,6)));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));
        b.setAlignmentX(LEFT_ALIGNMENT); f.add(b);
        f.add(Box.createRigidArea(new Dimension(0,4)));
        lP.setAlignmentX(RIGHT_ALIGNMENT); f.add(lP);
        return f;
    }

    private JPanel buildCardTabla(String titulo,String[] cols,boolean esDet){
        JPanel card=mkCard();
        card.setLayout(new BorderLayout(0,10));
        card.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        DefaultTableModel mdl=new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        if(esDet) mdlDet=mdl; else mdlProp2=mdl;

        JTable tabla=mkTablaEstilo(mdl);
        tabla.setFillsViewportHeight(true);

        JScrollPane scroll=new JScrollPane(tabla,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE8D8C8),1));
        scroll.getViewport().setBackground(C_WHITE);

        card.add(mkTit(titulo),BorderLayout.NORTH);
        card.add(scroll,BorderLayout.CENTER);
        return card;
    }

    private static final double[] PESOS  = {0.06,0.10,0.40,0.16,0.16,0.12};
    private static final String[] ENCABS = {"#","Hora","Detalle","Método","Monto","CFDI"};

    private JPanel buildSeccionTx() {
        JPanel sec=new JPanel(new BorderLayout(0,8));
        sec.setBackground(C_BG);

        JLabel tit=new JLabel("Resumen de transacciones");
        tit.setFont(new Font("Arial",Font.PLAIN,18));
        tit.setForeground(C_ACCENT);
        sec.add(tit,BorderLayout.NORTH);

        JPanel encWrap=new JPanel(new BorderLayout(0,0));
        encWrap.setBackground(C_BG);
        encWrap.add(buildEncTx(),BorderLayout.CENTER);
        JSeparator sepTx=new JSeparator();
        sepTx.setForeground(C_ACCENT);
        encWrap.add(sepTx,BorderLayout.SOUTH);
        sec.add(encWrap,BorderLayout.CENTER);

        panelTx=new JPanel();
        panelTx.setLayout(new BoxLayout(panelTx,BoxLayout.Y_AXIS));
        panelTx.setBackground(C_BG);
        sec.add(panelTx,BorderLayout.SOUTH);

        return sec;
    }

    private JPanel buildEncTx(){
        JPanel enc=new JPanel(new GridBagLayout());
        enc.setBackground(C_BG);
        enc.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        GridBagConstraints g=new GridBagConstraints();
        g.gridy=0; g.anchor=GridBagConstraints.WEST;
        for(int i=0;i<ENCABS.length;i++){
            g.gridx=i; g.weightx=PESOS[i];
            g.fill=GridBagConstraints.HORIZONTAL;
            g.insets=new Insets(0,i==0?0:8,0,0);
            JLabel l=new JLabel(ENCABS[i]);
            l.setFont(new Font("Arial",Font.PLAIN,13));
            l.setForeground(new Color(0x999999));
            enc.add(l,g);
        }
        return enc;
    }

    private JPanel buildFilaTx(Transaccion t,int idx){
        JPanel fila=new JPanel(new GridBagLayout());
        fila.setBackground(idx%2==0?C_WHITE:C_ALT_ROW);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        fila.setPreferredSize(new Dimension(0,40));
        fila.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));

        GridBagConstraints g=new GridBagConstraints();
        g.gridy=0; g.anchor=GridBagConstraints.WEST;
        String[] vals={t.id,t.hora,t.detalle,t.metodo,t.monto};
        for(int i=0;i<vals.length;i++){
            g.gridx=i; g.weightx=PESOS[i];
            g.fill=GridBagConstraints.HORIZONTAL;
            g.insets=new Insets(0,i==0?0:8,0,0);
            JLabel l=new JLabel(vals[i]);
            l.setFont(new Font("Arial",Font.PLAIN,13));
            l.setForeground(new Color(0x333333));
            fila.add(l,g);
        }
        g.gridx=5; g.weightx=PESOS[5];
        g.anchor=GridBagConstraints.CENTER;
        g.insets=new Insets(0,8,0,0);
        fila.add(buildCheckIcon(t.cfdi),g);
        return fila;
    }

    private JPanel buildCheckIcon(boolean cfdi){
        JPanel p=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                if(!cfdi) return;
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int cx=getWidth()/2,cy=getHeight()/2,r=11;
                g2.setColor(C_CHECK); g2.fillOval(cx-r,cy-r,r*2,r*2);
                g2.setColor(C_WHITE);
                g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                g2.drawLine(cx-5,cy,  cx-1,cy+4);
                g2.drawLine(cx-1,cy+4,cx+6,cy-4);
            }
        };
        p.setOpaque(false); p.setPreferredSize(new Dimension(26,26));
        return p;
    }

    private void poblarTx(){
        panelTx.removeAll();
        for(int i=0;i<transacciones.size();i++){
            panelTx.add(buildFilaTx(transacciones.get(i),i));
            JSeparator sep2=new JSeparator();
            sep2.setForeground(new Color(0xEEDDCC));
            sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
            panelTx.add(sep2);
        }
        panelTx.revalidate();
        panelTx.repaint();
        if (contenido != null) {
            contenido.revalidate();
            contenido.repaint();
        }
    }

    private JTable mkTablaEstilo(DefaultTableModel mdl){
        JTable t=new JTable(mdl);
        t.setFont(new Font("Arial",Font.PLAIN,13));
        t.setRowHeight(34);
        t.setBackground(C_WHITE); t.setForeground(new Color(0x3A3A3A));
        t.setGridColor(new Color(0xF0E0D0));
        t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(0xF5DEC8));
        t.setSelectionForeground(C_ACCENT);
        t.setIntercellSpacing(new Dimension(0,0));

        JTableHeader hdr=t.getTableHeader();
        hdr.setFont(new Font("Arial",Font.BOLD,13));
        hdr.setBackground(C_HDR_TBL); hdr.setForeground(C_WHITE);
        hdr.setPreferredSize(new Dimension(0,36));
        hdr.setReorderingAllowed(false);
        hdr.setResizingAllowed(false);

        hdr.setDefaultRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable tb,Object v,boolean s,boolean f,int row,int col){
                JLabel l=(JLabel)super.getTableCellRendererComponent(tb,v,s,f,row,col);
                l.setBackground(C_HDR_TBL); l.setForeground(C_WHITE);
                l.setFont(new Font("Arial",Font.BOLD,13));
                l.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                l.setOpaque(true);
                return l;
            }
        });

        t.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable tb,Object v,boolean s,boolean f,int row,int col){
                super.getTableCellRendererComponent(tb,v,s,f,row,col);
                if(!s){
                    setBackground(row%2==0 ? C_WHITE : C_ALT_ROW);
                    setForeground(new Color(0x3A3A3A));
                }
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                setOpaque(true);
                return this;
            }
        });
        return t;
    }

    private static class Barra extends JPanel {
        private final Color cF,cB; private double fr=0;
        Barra(Color f,Color b){cF=f;cB=b;setOpaque(false);
            setPreferredSize(new Dimension(100,11));setMinimumSize(new Dimension(10,11));}
        void setFr(double f){fr=Math.max(0,Math.min(1,f));repaint();}
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            g2.setColor(cB);g2.fillRoundRect(0,0,w,h,h,h);
            int fw=(int)(w*fr);if(fw>0){g2.setColor(cF);g2.fillRoundRect(0,0,fw,h,h,h);}
        }
    }

    private JLabel mkTit(String t){
        JLabel l=new JLabel(t);
        l.setFont(new Font("Arial",Font.BOLD,14)); l.setForeground(C_ACCENT); return l;
    }
    private JLabel mkLM(){JLabel l=new JLabel();l.setFont(new Font("Arial",Font.PLAIN,14));l.setForeground(new Color(0x333333));return l;}
    private JLabel mkLP(){JLabel l=new JLabel();l.setFont(new Font("Arial",Font.PLAIN,12));l.setForeground(new Color(0x999999));return l;}
    private JPanel mkCard(){
        JPanel c=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_DIV);g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
            }
        };
        c.setOpaque(false); return c;
    }

    private void cargarDatosDesdeBackend() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiClient.obtenerDashboardReportes();
            }

            @Override
            protected void done() {
                try {
                    String jsonResponse = get();
                    procesarJsonDashboard(jsonResponse);
                } catch (Exception e) {
                    System.err.println("Error cargando dashboard de reportes: " + e.getMessage());
                    inicializarDummy();
                    actualizarDatos();
                }
            }
        }.execute();
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

    private void procesarJsonDashboard(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            JSONObject resumen = obj.optJSONObject("resumen");
            double vHoy = 0;
            int ordenesBanner = 0;
            int mOcupadas = 0;

            if (resumen != null) {
                vHoy = resumen.optDouble("ventas_hoy", 0.0);
                // Priorizamos ordenes_hoy para que concuerde con el banner, sino pedidos_activos
                ordenesBanner = resumen.optInt("ordenes_hoy", resumen.optInt("pedidos_activos", 0));
                mOcupadas = resumen.optInt("mesas_ocupadas", resumen.optInt("personal_activo", 0));
            } else {
                vHoy = obj.optDouble("ventas_hoy", 0.0);
                ordenesBanner = obj.optInt("ordenes_hoy", 0);
                mOcupadas = obj.optInt("personal_activo", 0);
            }

            JSONArray diarios = obj.optJSONArray("datos_diarios");
            List<DatoVenta> nuevasVentas = new ArrayList<>();
            List<DatoPropina> nuevasPropinas = new ArrayList<>();
            double totalPropinas = 0;

            if (diarios != null) {
                for (int i = 0; i < diarios.length(); i++) {
                    JSONObject d = diarios.getJSONObject(i);
                    String dia = d.optString("dia", "Día " + (i + 1));
                    double v = d.optDouble("ventas", 0.0);
                    double p = d.optDouble("propina", 0.0);
                    int m = d.optInt("mesas", 0);

                    String diaCorto = dia.length() >= 3 ? dia.substring(0, 3) : dia;
                    nuevasVentas.add(new DatoVenta(diaCorto, v, m));
                    nuevasPropinas.add(new DatoPropina(dia, String.format("$%,.2f", p), m));
                    totalPropinas += p;
                }
            }

            JSONArray metodos = obj.optJSONArray("metodos_pago");
            double ef = 0, ta = 0, mi = 0;
            if (metodos != null) {
                for (int i = 0; i < metodos.length(); i++) {
                    JSONObject m = metodos.getJSONObject(i);
                    String tipo = m.optString("metodo_pago", m.optString("metodo", "")).toLowerCase();
                    double total = m.optDouble("total", m.optDouble("monto", 0.0));

                    if (tipo.contains("efectivo")) ef += total;
                    else if (tipo.contains("tarjeta")) ta += total;
                    else mi += total;
                }
            }

            // --- LECTURA DE TRANSACCIONES (AGREGADO) ---
            transacciones.clear();
            JSONArray txArray = obj.optJSONArray("transacciones");
            if (txArray != null) {
                for (int i = 0; i < txArray.length(); i++) {
                    JSONObject t = txArray.getJSONObject(i);

                    String id      = t.optString("id", "#" + (i + 1));
                    String hora    = t.optString("hora", "--:--");
                    String detalle = t.optString("detalle", "Sin detalle");
                    String metodo  = t.optString("metodo", "Efectivo");
                    String monto   = formatearMoneda(t.optDouble("monto", 0.0));
                    boolean cfdi   = t.optBoolean("cfdi", false);

                    transacciones.add(new Transaccion(id, hora, detalle, metodo, monto, cfdi));
                }
            }

            setMetricas(
                    formatearMoneda(vHoy),
                    String.valueOf(ordenesBanner),
                    String.format("$%,.2f", totalPropinas),
                    String.valueOf(mOcupadas)
            );

            if (!nuevasVentas.isEmpty()) setVentas(nuevasVentas);
            if (!nuevasPropinas.isEmpty()) setPropinas(nuevasPropinas);

            setMetodosPago(
                    ef, formatearMoneda(ef),
                    ta, formatearMoneda(ta),
                    mi, formatearMoneda(mi)
            );

            // actualizarDatos() ya invoca a poblarTx(), por lo que renderizará las transacciones mapeadas
            actualizarDatos();

        } catch (Exception e) {
            System.err.println("Error procesando JSON de Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void actualizarDatos(){
        lblVT.setText(dVT); lblOrd.setText(dOrd);
        lblProp.setText(dProp); lblCfdi.setText(dCfdi);

        grafica.setDatos(ventas);

        double tot=dEfM+dTaM+dMiM; if(tot==0)tot=1;
        lEf.setText(dEfS); bEf.setFr(dEfM/tot); lPEf.setText(String.format("%.1f %%",dEfM/tot*100));
        lTa.setText(dTaS); bTa.setFr(dTaM/tot); lPTa.setText(String.format("%.1f %%",dTaM/tot*100));
        lMi.setText(dMiS); bMi.setFr(dMiM/tot); lPMi.setText(String.format("%.1f %%",dMiM/tot*100));

        if(mdlDet!=null){
            mdlDet.setRowCount(0);
            for(DatoVenta d:ventas)
                mdlDet.addRow(new Object[]{d.dia,String.format("$%,.2f",d.ventas),d.mesas});
        }
        if(mdlProp2!=null){
            mdlProp2.setRowCount(0);
            for(DatoPropina p:propinas)
                mdlProp2.addRow(new Object[]{p.dia,p.propina,p.mesas});
        }

        poblarTx();
        repaint();
    }

    private void inicializarDummy(){
        dVT = "$23,430"; dOrd = "186"; dProp = "$2,404"; dCfdi = "13";
        ventas.clear();
        ventas.add(new DatoVenta("Lun",3200,24));
        ventas.add(new DatoVenta("Mar",4800,31));
        ventas.add(new DatoVenta("Mie",3900,27));
        ventas.add(new DatoVenta("Jue",5100,34));
        ventas.add(new DatoVenta("Vie",6200,42));
        ventas.add(new DatoVenta("Sab",4500,28));

        propinas.clear();
        propinas.add(new DatoPropina("Lunes",   "$240",24));
        propinas.add(new DatoPropina("Martes",  "$360",31));
        propinas.add(new DatoPropina("Miercoles","$290",27));
        propinas.add(new DatoPropina("Jueves",  "$410",34));
        propinas.add(new DatoPropina("Viernes", "$520",42));
        propinas.add(new DatoPropina("Sabado",  "$340",28));

        transacciones.clear();
        for(int i=0;i<8;i++)
            transacciones.add(new Transaccion(
                    "#04"+(i+1),"1"+i+":48","3 productos","Efectivo","$360",i==1||i==3||i==5));

        dEfM=7200; dTaM=4180; dMiM=1100;
        dEfS="$7,200.00"; dTaS="$4,180.00"; dMiS="$1,100.00";
    }

    public void setMetricas(String vt,String ord,String prop,String cfdi){dVT=vt;dOrd=ord;dProp=prop;dCfdi=cfdi;actualizarDatos();}
    public void setVentas(List<DatoVenta> l){ventas.clear();ventas.addAll(l);actualizarDatos();}
    public void setPropinas(List<DatoPropina> l){propinas.clear();propinas.addAll(l);actualizarDatos();}
    public void setMetodosPago(double ef,String efS,double ta,String taS,double mi,String miS){dEfM=ef;dEfS=efS;dTaM=ta;dTaS=taS;dMiM=mi;dMiS=miS;actualizarDatos();}
    public void setTransacciones(List<Transaccion> l){transacciones.clear();transacciones.addAll(l);actualizarDatos();}
}