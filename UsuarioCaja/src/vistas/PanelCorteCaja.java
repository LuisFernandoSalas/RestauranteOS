package vistas;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Vista: PanelCorteCaja — Corte de caja
 * TODO (BD): consultas marcadas con TODO
 */
public class PanelCorteCaja extends JPanel {

    // ─── COLORES ───────────────────────────────────
    private static final Color C_BG        = new Color(0xFBF5EC);
    private static final Color C_ACCENT    = new Color(0x6B2D1A);
    private static final Color C_DIV       = new Color(0xC8A882);
    private static final Color C_BAN       = new Color(0xA0401A);
    private static final Color C_BSEP      = new Color(0xBF6030);
    private static final Color C_WHITE     = Color.WHITE;
    private static final Color C_CAMPO_BG  = new Color(0xFBF5EC);
    private static final Color C_DIF_BG    = new Color(0xFDF3E7);
    private static final Color C_DIF_BOR   = new Color(0xE8A060);
    private static final Color C_CON_BOR   = new Color(0xD48000);
    private static final Color C_CON_BG    = new Color(0xFFFBF3);
    private static final Color C_BTN       = new Color(0x3A2010);
    private static final Color C_BAR_BG    = new Color(0xEEDDCC);
    private static final Color C_BAR_EF    = new Color(0x6B2D1A);
    private static final Color C_BAR_TA    = new Color(0xD48000);
    private static final Color C_BAR_MI    = new Color(0xC03020);

    // ─── DATOS (dummy hasta BD) ────────────────────
    private double dTotal = 12_480; private int dOrd = 42; private int dCfdi = 7;
    private double dEfM = 7_200; private int dEfTx = 26;
    private double dTaM = 4_180; private int dTaTx = 13;
    private double dMiM = 1_100; private int dMiTx =  3;
    private double dSys = 7_200; private double dCaj = 7_200;
    private String dCajeroNom = "Hasiel...";
    private double dFAp = 500; private double dFSig = 500;

    // ─── LABELS ────────────────────────────────────
    private JLabel lTotal, lOrd, lCfdi;
    private JLabel lSys, lCaj, lDTxt, lDVal;
    private JLabel lMEf, lTEf, lPEf;
    private JLabel lMTa, lTTa, lPTa;
    private JLabel lMMi, lTMi, lPMi;
    private JLabel lCajero, lFAp, lFSig, lEnt;
    private Barra  bEf, bTa, bMi;

    public PanelCorteCaja() {
        setLayout(new BorderLayout(0, 14));
        setBackground(C_BG);
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        add(mkHeader(),  BorderLayout.NORTH);
        add(mkCuerpo(),  BorderLayout.CENTER);
        add(mkFooter(),  BorderLayout.SOUTH);
        refresh();
    }

    // ═══════════════════════════════════════════
    // HEADER: título + sep + banner
    // ═══════════════════════════════════════════
    private JPanel mkHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        JLabel t = new JLabel("Corte de caja");
        t.setFont(new Font("Arial", Font.BOLD, 30));
        t.setForeground(C_ACCENT);
        JSeparator sep = new JSeparator();
        sep.setForeground(C_DIV);
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(t, BorderLayout.NORTH);
        top.add(sep, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(mkBanner(), BorderLayout.CENTER);
        return p;
    }

    private JPanel mkBanner() {
        JPanel b = new JPanel(new GridLayout(1, 5, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BAN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(0, 76));
        lTotal = mkVB("$12,480"); lOrd = mkVB("42"); lCfdi = mkVB("7");
        b.add(mkCB("TOTAL DE TURNO", lTotal));
        b.add(mkSB());
        b.add(mkCB("ÓRDENES", lOrd));
        b.add(mkSB());
        b.add(mkCB("CFDIS EMITIDOS", lCfdi));
        return b;
    }

    private JPanel mkCB(String et, JLabel v) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        JLabel le = new JLabel(et, SwingConstants.CENTER);
        le.setFont(new Font("Arial", Font.PLAIN, 11)); le.setForeground(new Color(0xF5DEC8));
        le.setAlignmentX(CENTER_ALIGNMENT); v.setAlignmentX(CENTER_ALIGNMENT);
        p.add(le); p.add(Box.createRigidArea(new Dimension(0,3))); p.add(v);
        return p;
    }

    private JLabel mkVB(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, 24)); l.setForeground(C_WHITE); return l;
    }

    private JPanel mkSB() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(C_BSEP); g.fillRect(getWidth()/2, 10, 1, getHeight()-20);
            }
        };
        p.setOpaque(false); p.setPreferredSize(new Dimension(2,0)); return p;
    }

    // ═══════════════════════════════════════════
    // CUERPO: dos paneles con altura NATURAL
    // Clave: envolver cada panel en un JPanel
    // con FlowLayout para que NO se estiren.
    // ═══════════════════════════════════════════
    private JPanel mkCuerpo() {
        JPanel p = new JPanel(new GridLayout(1, 2, 18, 0));
        p.setOpaque(false);

        // Columna izquierda: cuadre en la parte TOP, sin estirarse
        JPanel colIzq = new JPanel(new BorderLayout());
        colIzq.setOpaque(false);
        colIzq.add(mkCardCuadre(), BorderLayout.NORTH); // NORTH = tamaño natural

        // Columna derecha: desglose NORTH + confirmar CENTER
        JPanel colDer = new JPanel(new BorderLayout(0, 14));
        colDer.setOpaque(false);
        colDer.add(mkCardDesglose(),  BorderLayout.NORTH);
        colDer.add(mkCardConfirmar(), BorderLayout.CENTER);

        p.add(colIzq);
        p.add(colDer);
        return p;
    }

    // ─── TARJETA CUADRE ────────────────────────────
    private JPanel mkCardCuadre() {
        // Panel con BoxLayout Y_AXIS — todo compacto, sin glue
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_DIV); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));

        // Fila título + RF
        JPanel fTit = new JPanel(new BorderLayout());
        fTit.setOpaque(false);
        fTit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel tit = new JLabel("Cuadre de caja");
        tit.setFont(new Font("Arial", Font.BOLD, 15)); tit.setForeground(C_ACCENT);
        JLabel rf = new JLabel("RF-20");
        rf.setFont(new Font("Arial", Font.PLAIN, 12)); rf.setForeground(new Color(0xAAAAAA));
        fTit.add(tit, BorderLayout.WEST); fTit.add(rf, BorderLayout.EAST);
        card.add(fTit);
        card.add(Box.createRigidArea(new Dimension(0,14)));

        // Campos sistema / caja — altura fija
        JPanel campos = new JPanel(new GridLayout(1,2,10,0));
        campos.setOpaque(false);
        campos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        campos.setPreferredSize(new Dimension(Integer.MAX_VALUE, 70));
        lSys = mkVC(); lCaj = mkVC();
        campos.add(mkCampoCuadre("SISTEMA (EFECTIVO)", lSys));
        campos.add(mkCampoCuadre("CONTADO EN CAJA",    lCaj));
        card.add(campos);
        card.add(Box.createRigidArea(new Dimension(0,10)));

        // Diferencia — altura fija
        JPanel dif = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_DIF_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(C_DIF_BOR); g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
            }
        };
        dif.setOpaque(false);
        dif.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        dif.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        dif.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        lDTxt = new JLabel("Sin diferencia");
        lDTxt.setFont(new Font("Arial", Font.PLAIN, 13)); lDTxt.setForeground(C_ACCENT);
        lDVal = new JLabel("$0.00");
        lDVal.setFont(new Font("Arial", Font.BOLD, 13)); lDVal.setForeground(C_ACCENT);
        dif.add(lDTxt, BorderLayout.WEST); dif.add(lDVal, BorderLayout.EAST);
        card.add(dif);

        return card;
    }

    private JPanel mkCampoCuadre(String et, JLabel val) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CAMPO_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(C_DIV); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,10,10);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JLabel le = new JLabel(et);
        le.setFont(new Font("Arial", Font.PLAIN, 10)); le.setForeground(new Color(0x999999));
        p.add(le, BorderLayout.NORTH); p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JLabel mkVC() {
        JLabel l = new JLabel();
        l.setFont(new Font("Arial", Font.PLAIN, 19)); l.setForeground(new Color(0x333333));
        return l;
    }

    // ─── TARJETA DESGLOSE ──────────────────────────
    // ─── TARJETA DESGLOSE (GridBagLayout = control total) ──
    private JPanel mkCardDesglose() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_DIV); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
            }
        };
        card.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0; gc.gridx = 0;

        // Título
        gc.gridy = 0; gc.insets = new Insets(18, 22, 16, 22);
        JPanel fTit = new JPanel(new BorderLayout()); fTit.setOpaque(false);
        JLabel tit = new JLabel("Desglose por método de pago");
        tit.setFont(new Font("Arial", Font.BOLD, 15)); tit.setForeground(C_ACCENT);
        JLabel rf = new JLabel("RF-12 / RF-13");
        rf.setFont(new Font("Arial", Font.PLAIN, 12)); rf.setForeground(new Color(0xAAAAAA));
        fTit.add(tit, BorderLayout.WEST); fTit.add(rf, BorderLayout.EAST);
        card.add(fTit, gc);

        // Efectivo
        lMEf = new JLabel(); lTEf = new JLabel(); lPEf = new JLabel();
        bEf = new Barra(C_BAR_EF, C_BAR_BG);
        gc.gridy=1; gc.insets=new Insets(0,22,0,22);   card.add(mkFNom("Efectivo",lMEf), gc);
        gc.gridy=2; gc.insets=new Insets(5,22,0,22);   card.add(bEf, gc);
        gc.gridy=3; gc.insets=new Insets(3,22,14,22);  card.add(mkFSub(lTEf,lPEf), gc);

        // Tarjeta
        lMTa = new JLabel(); lTTa = new JLabel(); lPTa = new JLabel();
        bTa = new Barra(C_BAR_TA, C_BAR_BG);
        gc.gridy=4; gc.insets=new Insets(0,22,0,22);   card.add(mkFNom("Tarjeta",lMTa), gc);
        gc.gridy=5; gc.insets=new Insets(5,22,0,22);   card.add(bTa, gc);
        gc.gridy=6; gc.insets=new Insets(3,22,14,22);  card.add(mkFSub(lTTa,lPTa), gc);

        // Mixto
        lMMi = new JLabel(); lTMi = new JLabel(); lPMi = new JLabel();
        bMi = new Barra(C_BAR_MI, C_BAR_BG);
        gc.gridy=7; gc.insets=new Insets(0,22,0,22);   card.add(mkFNom("Mixto",lMMi), gc);
        gc.gridy=8; gc.insets=new Insets(5,22,0,22);   card.add(bMi, gc);
        gc.gridy=9; gc.insets=new Insets(3,22,18,22);  card.add(mkFSub(lTMi,lPMi), gc);

        return card;
    }

    private JPanel mkFNom(String nom, JLabel lM) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel lN = new JLabel(nom);
        lN.setFont(new Font("Arial", Font.PLAIN, 14)); lN.setForeground(new Color(0x333333));
        lM.setFont(new Font("Arial", Font.PLAIN, 14)); lM.setForeground(new Color(0x333333));
        lM.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lN, BorderLayout.WEST); p.add(lM, BorderLayout.EAST);
        return p;
    }

    private JPanel mkFSub(JLabel lT, JLabel lP) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        lT.setFont(new Font("Arial", Font.PLAIN, 12)); lT.setForeground(new Color(0x999999));
        lP.setFont(new Font("Arial", Font.PLAIN, 12)); lP.setForeground(new Color(0x999999));
        lP.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(lT, BorderLayout.WEST); p.add(lP, BorderLayout.EAST);
        return p;
    }

    // Stubs requeridos por refresh()
    private JLabel mkLD() { return new JLabel(); }
    private JLabel mkLS() { return new JLabel(); }

    // ─── TARJETA CONFIRMAR ─────────────────────────
    // El contenido va en NORTH de un wrap para no estirarse
    private JPanel mkCardConfirmar() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_CON_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_CON_BOR); g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));

        JLabel tit = new JLabel("Confirmar cierre de caja", SwingConstants.CENTER);
        tit.setFont(new Font("Arial", Font.BOLD, 14)); tit.setForeground(C_ACCENT);
        card.add(tit, BorderLayout.NORTH);

        // Wrapper: datos en NORTH = tamaño natural, no se estiran
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        JPanel datos = new JPanel(new GridLayout(4,1,0,8)); datos.setOpaque(false);
        datos.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
        lCajero = mkLC(); lFAp = mkLC(); lFSig = mkLC(); lEnt = mkLC();
        datos.add(lCajero); datos.add(lFAp); datos.add(lFSig); datos.add(lEnt);
        wrap.add(datos, BorderLayout.NORTH);
        card.add(wrap, BorderLayout.CENTER);
        return card;
    }

    private JLabel mkLC() {
        JLabel l = new JLabel();
        l.setFont(new Font("Arial", Font.PLAIN, 13)); l.setForeground(new Color(0x3A3A3A));
        return l;
    }

    // ─── FOOTER ────────────────────────────────────
    private JPanel mkFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        f.setOpaque(false);
        JButton btn = new JButton("Confirmar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BTN); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(C_WHITE); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
            }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(190, 50));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onConfirmar());
        f.add(btn); return f;
    }

    // ─── TARJETA BLANCA BASE ───────────────────────
    private JPanel mkCardB() {
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(C_DIV); g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(1,1,getWidth()-3,getHeight()-3,14,14);
            }
        };
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); c.setOpaque(false); return c;
    }

    // ═══════════════════════════════════════════
    // BARRA PROPORCIONAL
    // ═══════════════════════════════════════════
    private static class Barra extends JPanel {
        private final Color cF, cB; private double fr = 0;
        Barra(Color f, Color b) { cF=f; cB=b; setOpaque(false); setPreferredSize(new Dimension(100,11)); setMinimumSize(new Dimension(10,11)); }
        void setFr(double f) { fr = Math.max(0, Math.min(1,f)); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            g2.setColor(cB); g2.fillRoundRect(0,0,w,h,h,h);
            int fw=(int)(w*fr); if(fw>0){g2.setColor(cF); g2.fillRoundRect(0,0,fw,h,h,h);}
        }
    }

    // ═══════════════════════════════════════════
    // REFRESH
    // ═══════════════════════════════════════════
    public void refresh() {
        double tot = dEfM + dTaM + dMiM; if(tot==0) tot=1;
        double pEf=dEfM/tot, pTa=dTaM/tot, pMi=dMiM/tot;

        lTotal.setText(fmt(dTotal)); lOrd.setText(""+dOrd); lCfdi.setText(""+dCfdi);
        lSys.setText(fmt(dSys)); lCaj.setText(fmt(dCaj));

        double diff = dCaj - dSys;
        lDTxt.setText(diff==0?"Sin diferencia":diff>0?"Sobrante":"Faltante");
        lDVal.setText(fmt(Math.abs(diff)));
        Color cd = diff<0?new Color(0xC03020):C_ACCENT;
        lDTxt.setForeground(cd); lDVal.setForeground(cd);

        lMEf.setText(fmt(dEfM)); lTEf.setText(dEfTx+" transacciones"); lPEf.setText(pct(pEf)); bEf.setFr(pEf);
        lMTa.setText(fmt(dTaM)); lTTa.setText(dTaTx+" transacciones"); lPTa.setText(pct(pTa)); bTa.setFr(pTa);
        lMMi.setText(fmt(dMiM)); lTMi.setText(dMiTx+" transacciones"); lPMi.setText(pct(pMi)); bMi.setFr(pMi);

        lCajero.setText("Cajero: "+dCajeroNom);
        lFAp.setText("Fondo apertura: $"+(int)dFAp);
        lFSig.setText("Fondo para siguiente turno: $"+(int)dFSig);
        lEnt.setText("Total a entregar: "+fmt(dTotal));
        repaint();
    }

    private String fmt(double v){return String.format("$%,.2f",v);}
    private String pct(double v){return String.format("%.1f %%",v*100);}

    private void onConfirmar() {
        int ok = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this),
                "¿Confirmar el cierre de caja?\nEsta acción cerrará el turno actual.",
                "Confirmar cierre", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if(ok==JOptionPane.YES_OPTION)
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Corte registrado.\n(BD próximamente)","Corte cerrado",JOptionPane.INFORMATION_MESSAGE);
        // TODO (BD): INSERT en `cortes`
    }

    // API BD
    public void cargarDesdeBD(double total, int ord, int cfdi,
                              double efM, int efTx, double taM, int taTx, double miM, int miTx,
                              double sys, double caj, String cajero, double fAp, double fSig) {
        dTotal=total; dOrd=ord; dCfdi=cfdi;
        dEfM=efM; dEfTx=efTx; dTaM=taM; dTaTx=taTx; dMiM=miM; dMiTx=miTx;
        dSys=sys; dCaj=caj; dCajeroNom=cajero; dFAp=fAp; dFSig=fSig;
        refresh();
    }
}
