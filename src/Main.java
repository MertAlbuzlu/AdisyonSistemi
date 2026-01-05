import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class AdisyonSistemi extends JFrame {

    private JTable siparisTable;
    private JLabel toplamLabel;
    private JLabel seciliMasaLabel;
    private JTextField txtAdet;

    private double toplamTutar = 0.0;
    private double hamTutar = 0.0;

    private DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(new Locale("tr", "TR")));

    private String aktifMasaKey = null;

    private Map<String, DefaultTableModel> masaSiparisleri = new HashMap<>();
    private Map<String, JButton> masaButonlari = new HashMap<>();

    // Veri Yöneticisi Bağlantısı
    private VeriYoneticisi veriYoneticisi;

    private Map<String, LocalDateTime> rezervasyonListesi = new HashMap<>();
    private Map<String, Double> masaIndirimleri = new HashMap<>();
    private Set<String> aktifOturanMasalar = new HashSet<>();

    private java.util.List<MutfakSiparisi> mutfakListesi = new ArrayList<>();
    private DefaultTableModel mutfakTableModel;
    private JTable mutfakTable;

    private java.util.List<SatisIslemi> gunlukSatislar = new ArrayList<>();

    public AdisyonSistemi() {
        // Veri yöneticisini başlat (Ürünleri yükler)
        veriYoneticisi = new VeriYoneticisi();

        setTitle("Zümrüt POS - Profesyonel (2025 v2.2)");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Tema.COL_BG_MAIN);

        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage("logo.png"));
        } catch (Exception e) {}

        masalariHazirla();
        mutfakSisteminiHazirla();

        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Tema.COL_BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createLeftPanel(), BorderLayout.WEST);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createRightPanel(), BorderLayout.EAST);

        add(mainPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);

        bosEkraniHazirla();
    }

    private void mutfakSisteminiHazirla() {
        mutfakTableModel = new DefaultTableModel(new String[]{"MASA", "ÜRÜN", "ADET", "SAAT", "DURUM"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        mutfakTable = new JTable(mutfakTableModel);
        mutfakTable.setRowHeight(35);
        mutfakTable.setFont(Tema.FONT_BOLD);
        mutfakTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mutfakTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        mutfakTable.getColumnModel().getColumn(0).setMaxWidth(60);
        mutfakTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        mutfakTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        mutfakTable.getColumnModel().getColumn(2).setMaxWidth(50);
        mutfakTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        mutfakTable.getColumnModel().getColumn(3).setMaxWidth(70);
        mutfakTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        mutfakTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String durum = table.getValueAt(row, 4).toString();
                if (durum.equals("HAZIR")) {
                    c.setBackground(Tema.COL_MUTFAK_HAZIR); c.setForeground(Color.WHITE);
                } else if (durum.equals("HAZIRLANIYOR")) {
                    c.setBackground(Tema.COL_MUTFAK_ISLEMDE); c.setForeground(Color.BLACK);
                } else if (durum.equals("BEKLENİYOR")) {
                    c.setBackground(Tema.COL_MUTFAK_BEKLEYEN); c.setForeground(Color.WHITE);
                }
                if (isSelected) {
                    c.setBackground(Color.WHITE); c.setForeground(Color.BLACK);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }

    private void bosEkraniHazirla() {
        seciliMasaLabel.setText("LÜTFEN BİR MASA SEÇİNİZ");
        seciliMasaLabel.setForeground(Color.GRAY);
        toplamLabel.setText("---");
        siparisTable.setModel(new DefaultTableModel(new String[]{"Ürün", "Adet", "Fiyat", "Toplam"}, 0));
        renkleriGuncelle();
    }

    private void masalariHazirla() {
        String[] columns = {"Ürün", "Adet", "Fiyat", "Toplam"};
        for (int i = 1; i <= 8; i++) createTableData("Zemin", i, columns);
        for (int i = 1; i <= 12; i++) createTableData("Kat1", i, columns);
        for (int i = 1; i <= 4; i++) createTableData("Balkon", i, columns);
    }

    private void createTableData(String prefix, int no, String[] columns) {
        String key = prefix + "-" + no;
        DefaultTableModel model = new DefaultTableModel(columns,0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        masaSiparisleri.put(key, model);
        masaIndirimleri.put(key, 0.0);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Tema.COL_BG_PANEL);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 65, 81)));

        JLabel lblTitle = new JLabel("  ZÜMRÜT POS SİSTEMİ");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel lblTime = new JLabel();
        lblTime.setForeground(Tema.COL_TEXT_MAIN);
        lblTime.setFont(new Font("Consolas", Font.BOLD, 16));
        lblTime.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> lblTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss", new Locale("tr", "TR")))));
        timer.start();

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblTime, BorderLayout.EAST);
        return header;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Tema.COL_BG_MAIN);
        panel.setPreferredSize(new Dimension(500, 0));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Tema.COL_BG_MAIN);
        tabbedPane.setForeground(Tema.COL_TEXT_MAIN);
        tabbedPane.setFont(Tema.FONT_BOLD);
        tabbedPane.setPreferredSize(new Dimension(0, 350));

        tabbedPane.addTab(" ZEMİN KAT ", createZonePanel("Zemin", 8, "Z"));
        tabbedPane.addTab(" 1. KAT ", createZonePanel("Kat1", 12, "K"));
        tabbedPane.addTab(" BALKON ", createZonePanel("Balkon", 4, "B"));
        tabbedPane.addTab(" MUTFAK 👨‍🍳 ", createMutfakPanel());

        JPanel siparisPanel = new JPanel(new BorderLayout(5, 5));
        siparisPanel.setBackground(Tema.COL_BG_PANEL);
        siparisPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        seciliMasaLabel = new JLabel("MASA SEÇİNİZ");
        seciliMasaLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        seciliMasaLabel.setForeground(Tema.COL_ACCENT);

        siparisTable = new JTable();
        siparisTable.setRowHeight(30);
        siparisTable.setFont(Tema.FONT_NORMAL);
        siparisTable.setBackground(Tema.COL_BG_MAIN);
        siparisTable.setForeground(Tema.COL_TEXT_MAIN);
        siparisTable.setGridColor(Tema.COL_BTN_DARK);
        siparisTable.setSelectionBackground(Tema.COL_ACCENT);
        siparisTable.setSelectionForeground(Color.WHITE);
        siparisTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JTableHeader header = siparisTable.getTableHeader();
        header.setBackground(Tema.COL_BG_PANEL);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(siparisTable);
        scrollPane.getViewport().setBackground(Tema.COL_BG_MAIN);
        scrollPane.setBorder(BorderFactory.createLineBorder(Tema.COL_BTN_DARK));

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Tema.COL_BG_PANEL);

        toplamLabel = new JLabel("0.00 TL");
        toplamLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        toplamLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        toplamLabel.setForeground(Tema.COL_TEXT_MAIN);

        JPanel actionButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionButtonPanel.setBackground(Tema.COL_BG_PANEL);
        JButton ikramButton = createFlatButton("İKRAM ET", Tema.COL_IKRAM, Color.WHITE);
        ikramButton.addActionListener(e -> seciliUrunuIkramYap());
        JButton silButton = createFlatButton("SİL", Tema.COL_BTN_DARK, Tema.COL_DANGER);
        silButton.addActionListener(e -> siparistenSil());

        actionButtonPanel.add(ikramButton);
        actionButtonPanel.add(silButton);
        bottomPanel.add(toplamLabel, BorderLayout.NORTH);
        bottomPanel.add(actionButtonPanel, BorderLayout.SOUTH);
        siparisPanel.add(seciliMasaLabel, BorderLayout.NORTH);
        siparisPanel.add(scrollPane, BorderLayout.CENTER);
        siparisPanel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(tabbedPane, BorderLayout.NORTH); panel.add(siparisPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMutfakPanel() {
        JPanel mutfakPanel = new JPanel(new BorderLayout(5, 5));
        mutfakPanel.setBackground(Tema.COL_BG_PANEL);
        mutfakPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scroll = new JScrollPane(mutfakTable);
        scroll.getViewport().setBackground(Tema.COL_BG_MAIN);
        mutfakPanel.add(scroll, BorderLayout.CENTER);
        return mutfakPanel;
    }

    private JPanel createZonePanel(String prefix, int masaSayisi, String kisaKod) {
        JPanel zonePanel = new JPanel(new GridLayout(0, 4, 8, 8));
        zonePanel.setBackground(Tema.COL_BG_PANEL);
        zonePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int i = 1; i <= masaSayisi; i++) {
            String masaNo = String.valueOf(i);
            JButton btn = createFlatButton(kisaKod + i, Tema.COL_TABLE_EMPTY, Color.WHITE);
            btn.addActionListener(e -> masaDegistir(prefix, masaNo, true));
            masaButonlari.put(prefix + "-" + masaNo, btn);
            zonePanel.add(btn);
        }
        return zonePanel;
    }

    private void masaDegistir(String prefix, String no, boolean uyariGoster) {
        String yeniKey = prefix + "-" + no;
        if (yeniKey.equals(aktifMasaKey) && uyariGoster) return;

        if (uyariGoster && rezervasyonListesi.containsKey(yeniKey)) {
            DefaultTableModel kontrolModel = masaSiparisleri.get(yeniKey);
            if (kontrolModel.getRowCount() == 0 && !aktifOturanMasalar.contains(yeniKey)) {

                LocalDateTime rezZamani = rezervasyonListesi.get(yeniKey);
                LocalDateTime suAn = LocalDateTime.now();
                long dakikaFarki = ChronoUnit.MINUTES.between(suAn, rezZamani);

                if (dakikaFarki > 0 && dakikaFarki <= 90) {
                    Toolkit.getDefaultToolkit().beep();
                    String uyariMesaji = "⚠️ DİKKAT! BU MASA REZERVE ⚠️\n\n" +
                            "Rezervasyon Saati: " + rezZamani.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
                            "Kalan Süre: " + dakikaFarki + " dakika.\n\n" +
                            "1.5 saat kuralı nedeniyle yeni müşteri alınması SAKINCALIDIR.\n" +
                            "Gelen kişi rezervasyon sahibi mi?";

                    Object[] options = {"Evet (Masayı Aç)", "Hayır (İptal)"};
                    int secim = JOptionPane.showOptionDialog(this, uyariMesaji, "Kritik Rezervasyon Uyarısı",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

                    if (secim != 0) return;

                    rezervasyonListesi.remove(yeniKey);
                    aktifOturanMasalar.add(yeniKey);

                } else {
                    String bilgi = "ℹ️ BİLGİ: Bu masa saat " + rezZamani.format(DateTimeFormatter.ofPattern("HH:mm")) + " için rezerve.\n" +
                            "Henüz vakit olduğu için işlem yapabilirsiniz.";
                    JOptionPane.showMessageDialog(this, bilgi, "Rezervasyon Bilgisi", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        aktifMasaKey = yeniKey;
        renkleriGuncelle();
        siparisTable.setModel(masaSiparisleri.get(yeniKey));
        seciliMasaLabel.setText((prefix.equals("Kat1")?"1. KAT":prefix.toUpperCase()) + " - MASA " + no);
        seciliMasaLabel.setForeground(Tema.COL_ACCENT);
        toplamHesapla();
        siparisTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        siparisTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        if (txtAdet != null) txtAdet.setText("1");
    }

    private void renkleriGuncelle() {
        for (Map.Entry<String, DefaultTableModel> entry : masaSiparisleri.entrySet()) {
            String key = entry.getKey();
            DefaultTableModel model = entry.getValue();
            if (masaButonlari.containsKey(key)) {
                JButton btn = masaButonlari.get(key);
                if(key.equals(aktifMasaKey)) { btn.setBackground(Tema.COL_TABLE_SELECTED); btn.setForeground(Color.BLACK); }
                else if (model.getRowCount() > 0 || aktifOturanMasalar.contains(key)) { btn.setBackground(Tema.COL_DANGER); btn.setForeground(Color.WHITE); }
                else if (rezervasyonListesi.containsKey(key)) { btn.setBackground(Tema.COL_RESERVED); btn.setForeground(Color.BLACK); }
                else { btn.setBackground(Tema.COL_TABLE_EMPTY); btn.setForeground(Color.WHITE); }
            }
        }
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Tema.COL_BG_MAIN);
        JPanel categoryPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        categoryPanel.setBackground(Tema.COL_BG_MAIN);
        categoryPanel.setPreferredSize(new Dimension(0, 50));
        JButton yiyecekBtn = createFlatButton("YİYECEKLER", Tema.COL_BG_PANEL, Tema.COL_ACCENT);
        JButton icecekBtn = createFlatButton("İÇECEKLER", Tema.COL_BG_PANEL, Tema.COL_ACCENT);
        JButton tatliBtn = createFlatButton("TATLILAR", Tema.COL_BG_PANEL, Tema.COL_ACCENT);
        JButton kampanyaBtn = createFlatButton("KAMPANYALAR", Tema.COL_RESERVED, Color.BLACK);

        categoryPanel.add(yiyecekBtn); categoryPanel.add(icecekBtn); categoryPanel.add(tatliBtn); categoryPanel.add(kampanyaBtn);

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        qtyPanel.setBackground(Tema.COL_BG_MAIN);
        qtyPanel.setPreferredSize(new Dimension(0, 45));
        txtAdet = new JTextField("1"); txtAdet.setPreferredSize(new Dimension(60, 40));
        txtAdet.setHorizontalAlignment(JTextField.CENTER); txtAdet.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtAdet.setBackground(Tema.COL_QTY_BG); txtAdet.setForeground(Tema.COL_ACCENT); txtAdet.setBorder(new LineBorder(Tema.COL_ACCENT, 1));
        qtyPanel.add(createQtyButton("+")); qtyPanel.add(createQtyButton("-")); qtyPanel.add(createQtyButton("C")); qtyPanel.add(txtAdet);

        JPanel urunPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        urunPanel.setBackground(Tema.COL_BG_MAIN);
        JScrollPane scrollPane = new JScrollPane(urunPanel);
        scrollPane.setBorder(null); scrollPane.getViewport().setBackground(Tema.COL_BG_MAIN);

        yiyecekSecimEkraniGoster(urunPanel);
        yiyecekBtn.addActionListener(e -> yiyecekSecimEkraniGoster(urunPanel));
        icecekBtn.addActionListener(e -> icecekSecimEkraniGoster(urunPanel));
        tatliBtn.addActionListener(e -> tatlilariGoster(urunPanel));
        kampanyaBtn.addActionListener(e -> kampanyalariGoster(urunPanel));

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(Tema.COL_BG_MAIN);
        topContainer.add(categoryPanel, BorderLayout.NORTH); topContainer.add(qtyPanel, BorderLayout.CENTER);
        panel.add(topContainer, BorderLayout.NORTH); panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton createQtyButton(String text) {
        JButton btn = createFlatButton(text, Tema.COL_QTY_BG, Color.WHITE);
        btn.setPreferredSize(new Dimension(45, 40));
        btn.addActionListener(e -> {
            int mevcut = 1; try { mevcut = Integer.parseInt(txtAdet.getText()); } catch (Exception ex) { mevcut = 0; }
            if (text.equals("C")) mevcut = 1; else if (text.equals("+")) mevcut++; else if (text.equals("-")) { if (mevcut > 1) mevcut--; } else mevcut = Integer.parseInt(text);
            txtAdet.setText(String.valueOf(mevcut));
        });
        return btn;
    }

    private void siparisEkle(String urunAdi) {
        if (aktifMasaKey == null) { JOptionPane.showMessageDialog(this, "Lütfen önce bir masa seçiniz!"); return; }
        aktifOturanMasalar.add(aktifMasaKey);
        DefaultTableModel model = masaSiparisleri.get(aktifMasaKey);
        double fiyat = VeriYoneticisi.urunFiyatlari.getOrDefault(urunAdi, 0.0);
        int adet = 1; try { adet = Integer.parseInt(txtAdet.getText()); if(adet<=0) adet=1; } catch(Exception e) { adet=1; }

        boolean bulundu = false;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(urunAdi)) {
                int yeniAdet = (int) model.getValueAt(i, 1) + adet;
                model.setValueAt(yeniAdet, i, 1);
                model.setValueAt(df.format(yeniAdet * fiyat) + " TL", i, 3);
                bulundu = true; break;
            }
        }
        if (!bulundu) model.addRow(new Object[]{urunAdi, adet, df.format(fiyat) + " TL", df.format(adet * fiyat) + " TL"});

        String masaKisaAd = aktifMasaKey;
        String saat = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        boolean isHizli = VeriYoneticisi.hizliServisUrunleri.contains(urunAdi);
        String baslangicDurumu = isHizli ? "HAZIR" : "BEKLENİYOR";

        MutfakSiparisi ms = new MutfakSiparisi(masaKisaAd, urunAdi, adet, saat, baslangicDurumu);
        mutfakListesi.add(ms);

        String tableDisplayId = masaKisaAd.replace("Zemin-", "Z").replace("Kat1-", "K").replace("Balkon-", "B");
        mutfakTableModel.addRow(new Object[]{tableDisplayId, ms.urunAdi, ms.adet, ms.saat, ms.durum});

        if (isHizli) {
            javax.swing.Timer tHizli = new javax.swing.Timer(5000, e -> mutfaktanOtomatikSil(ms));
            tHizli.setRepeats(false); tHizli.start();
        } else {
            javax.swing.Timer tHazirlaniyor = new javax.swing.Timer(20000, e -> {
                if(!mutfakListesi.contains(ms)) return;
                ms.durum = "HAZIRLANIYOR";
                mutfakDurumGuncelle(ms);
                toplamHesapla();

                javax.swing.Timer tHazir = new javax.swing.Timer(20000, e2 -> {
                    if(!mutfakListesi.contains(ms)) return;
                    ms.durum = "HAZIR";
                    mutfakDurumGuncelle(ms);
                    Toolkit.getDefaultToolkit().beep();

                    javax.swing.Timer tSil = new javax.swing.Timer(5000, e3 -> mutfaktanOtomatikSil(ms));
                    tSil.setRepeats(false); tSil.start();
                });
                tHazir.setRepeats(false); tHazir.start();
            });
            tHazirlaniyor.setRepeats(false); tHazirlaniyor.start();
        }

        toplamHesapla();
        txtAdet.setText("1"); renkleriGuncelle();
    }

    private void mutfaktanOtomatikSil(MutfakSiparisi ms) {
        int index = mutfakListesi.indexOf(ms);
        if (index != -1) {
            mutfakListesi.remove(index);
            mutfakTableModel.removeRow(index);
        }
    }

    private void mutfakDurumGuncelle(MutfakSiparisi ms) {
        int index = mutfakListesi.indexOf(ms);
        if (index != -1) {
            mutfakTableModel.setValueAt(ms.durum, index, 4);
            mutfakTable.repaint();
        }
    }

    private void siparistenSil() {
        if (aktifMasaKey == null) return;
        DefaultTableModel model = masaSiparisleri.get(aktifMasaKey);
        int[] selectedRows = siparisTable.getSelectedRows();

        if (selectedRows.length == 0) return;

        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int row = selectedRows[i];
            String urunAdi = model.getValueAt(row, 0).toString();
            String targetMasaKey = aktifMasaKey;

            MutfakSiparisi hedefSiparis = null;
            for(MutfakSiparisi ms : mutfakListesi) {
                if(ms.masaAdi.equals(targetMasaKey) && ms.urunAdi.equals(urunAdi)) {
                    hedefSiparis = ms; break;
                }
            }

            if (hedefSiparis != null) {
                if (hedefSiparis.durum.equals("HAZIRLANIYOR") || hedefSiparis.durum.equals("HAZIR")) {
                    JOptionPane.showMessageDialog(this,
                            "UYARI: '" + urunAdi + "' mutfakta işleme alındığı için İPTAL EDİLEMEZ!",
                            "İptal Reddedildi", JOptionPane.WARNING_MESSAGE);
                    continue;
                } else if (hedefSiparis.durum.equals("BEKLENİYOR")) {
                    mutfaktanOtomatikSil(hedefSiparis);
                }
            }
            model.removeRow(row);
        }
        toplamHesapla(); renkleriGuncelle();
    }

    private void tumunuTemizle() {
        if (aktifMasaKey == null) return;

        String targetMasaKey = aktifMasaKey;

        for (MutfakSiparisi ms : mutfakListesi) {
            if (ms.masaAdi.equals(targetMasaKey)) {
                if (ms.durum.equals("HAZIRLANIYOR") || ms.durum.equals("HAZIR")) {
                    JOptionPane.showMessageDialog(this,
                            "MASA İPTAL EDİLEMEZ!\nMutfakta hazırlanan veya hazır olan ürünler var.\nLütfen önce ödeme alınız veya mutfakla görüşünüz.",
                            "Güvenlik Uyarısı", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (JOptionPane.showConfirmDialog(this, "Masayı tamamen silmek istediğine emin misin?", "Onay", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            for (int i = mutfakListesi.size() - 1; i >= 0; i--) {
                MutfakSiparisi ms = mutfakListesi.get(i);
                if (ms.masaAdi.equals(targetMasaKey)) {
                    mutfakListesi.remove(i);
                    mutfakTableModel.removeRow(i);
                }
            }

            masaSiparisleri.get(aktifMasaKey).setRowCount(0);
            masaIndirimleri.put(aktifMasaKey, 0.0);
            aktifOturanMasalar.remove(aktifMasaKey);
            toplamHesapla();
            renkleriGuncelle();
        }
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 0)); panel.setBackground(Tema.COL_BG_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JLabel lblIslemler = new JLabel("KASA İŞLEMLERİ");
        lblIslemler.setForeground(Tema.COL_HEADER_TEXT);
        lblIslemler.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblIslemler.setHorizontalAlignment(SwingConstants.CENTER);
        lblIslemler.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));

        JPanel buttonsContainer = new JPanel(new GridLayout(6, 1, 0, 15));
        buttonsContainer.setBackground(Tema.COL_BG_PANEL);

        JButton btnOdemeAl = createFlatButton("<html><center><font size='5'>HESAP ÖDE</font><br><font size='3'>ÖDEME EKRANI</font></center></html>", Tema.COL_ACCENT, Color.WHITE);
        btnOdemeAl.setBorder(new LineBorder(Color.WHITE, 1));
        JButton btnRezerve = createFlatButton("REZERVASYON", Tema.COL_BTN_DARK, Color.ORANGE);
        btnRezerve.setBorder(new LineBorder(Color.ORANGE, 1));
        JButton iptal = createFlatButton("MASAYI İPTAL ET", Tema.COL_DANGER, Color.WHITE);
        JButton btnSonIslemler = createFlatButton("SON İŞLEMLER", new Color(70, 130, 180), Color.WHITE);
        JButton gunSonu = createFlatButton("GÜN SONU (Z)", Tema.COL_BTN_DARK, Color.LIGHT_GRAY);

        btnOdemeAl.addActionListener(e -> odemeEkraniniAc());
        iptal.addActionListener(e -> tumunuTemizle());
        gunSonu.addActionListener(e -> gunSonuRaporuGoster());
        btnRezerve.addActionListener(e -> rezerveIslemi());
        btnSonIslemler.addActionListener(e -> sonIslemleriGoster());

        buttonsContainer.add(btnOdemeAl); buttonsContainer.add(new JLabel(""));
        buttonsContainer.add(btnRezerve); buttonsContainer.add(iptal);
        buttonsContainer.add(btnSonIslemler); buttonsContainer.add(gunSonu);

        panel.add(lblIslemler, BorderLayout.NORTH); panel.add(buttonsContainer, BorderLayout.CENTER);
        return panel;
    }

    private void odemeEkraniniAc() {
        if (aktifMasaKey == null || masaSiparisleri.get(aktifMasaKey).getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Ödenecek sipariş yok!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog odemeDialog = new JDialog(this, "Ödeme Terminali - " + seciliMasaLabel.getText(), true);
        odemeDialog.setSize(600, 750);
        odemeDialog.setLocationRelativeTo(this);
        odemeDialog.getContentPane().setBackground(Tema.COL_BG_MAIN);
        odemeDialog.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBackground(Tema.COL_BG_PANEL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel lblBaslik = new JLabel("TOPLAM ÖDENECEK TUTAR", SwingConstants.CENTER);
        lblBaslik.setForeground(Color.GRAY);
        lblBaslik.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblGuncelTutar = new JLabel(df.format(toplamTutar) + " TL");
        lblGuncelTutar.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblGuncelTutar.setForeground(Tema.COL_ACCENT);
        lblGuncelTutar.setHorizontalAlignment(SwingConstants.CENTER);

        topPanel.add(lblBaslik);
        topPanel.add(lblGuncelTutar);

        JPanel centerPanel = new JPanel(null);
        centerPanel.setBackground(Tema.COL_BG_MAIN);

        JLabel lblVerilen = new JLabel("Müşteriden Alınan Nakit:");
        lblVerilen.setForeground(Color.WHITE);
        lblVerilen.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblVerilen.setBounds(50, 20, 250, 30);

        JTextField txtAlinanNakit = new JTextField("");
        txtAlinanNakit.setFont(new Font("Segoe UI", Font.BOLD, 30));
        txtAlinanNakit.setHorizontalAlignment(JTextField.CENTER);
        txtAlinanNakit.setBackground(Color.WHITE);
        txtAlinanNakit.setForeground(Color.BLACK);
        txtAlinanNakit.setBorder(new LineBorder(Tema.COL_ACCENT, 2));
        txtAlinanNakit.setBounds(50, 60, 500, 60);

        JPanel pnlInfo = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlInfo.setBounds(50, 140, 500, 100);
        pnlInfo.setBackground(Tema.COL_BG_MAIN);

        JPanel pnlParaUstu = new JPanel(new GridLayout(2, 1));
        pnlParaUstu.setBackground(Tema.COL_BG_PANEL);
        pnlParaUstu.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        JLabel lblParaUstuBaslik = new JLabel("PARA ÜSTÜ", SwingConstants.CENTER);
        lblParaUstuBaslik.setForeground(Color.GRAY);
        JLabel lblParaUstuTutar = new JLabel("0,00 TL", SwingConstants.CENTER);
        lblParaUstuTutar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblParaUstuTutar.setForeground(new Color(255, 165, 0)); // Turuncu
        pnlParaUstu.add(lblParaUstuBaslik); pnlParaUstu.add(lblParaUstuTutar);

        JPanel pnlKalan = new JPanel(new GridLayout(2, 1));
        pnlKalan.setBackground(Tema.COL_BG_PANEL);
        pnlKalan.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        JLabel lblKalanBaslik = new JLabel("KARTTAN ÇEKİLECEK", SwingConstants.CENTER);
        lblKalanBaslik.setForeground(Color.GRAY);
        JLabel lblKalanTutar = new JLabel(df.format(toplamTutar) + " TL", SwingConstants.CENTER);
        lblKalanTutar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblKalanTutar.setForeground(Color.WHITE);
        pnlKalan.add(lblKalanBaslik); pnlKalan.add(lblKalanTutar);

        pnlInfo.add(pnlParaUstu);
        pnlInfo.add(pnlKalan);

        centerPanel.add(lblVerilen);
        centerPanel.add(txtAlinanNakit);
        centerPanel.add(pnlInfo);

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        btnPanel.setPreferredSize(new Dimension(0, 80));
        btnPanel.setBackground(Tema.COL_BG_MAIN);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton btnNakitBitir = createFlatButton("<html><center>NAKİT<br>TAMAMLA</center></html>", Tema.COL_ACCENT, Color.WHITE);
        JButton btnKartBitir = createFlatButton("<html><center>KREDİ KARTI<br>TAMAMLA</center></html>", new Color(70, 130, 180), Color.WHITE);
        JButton btnParcali = createFlatButton("<html><center>PARÇALI<br>ÖDE</center></html>", Tema.COL_BTN_DARK, Color.LIGHT_GRAY);
        btnParcali.setEnabled(false);

        txtAlinanNakit.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    String text = txtAlinanNakit.getText().replace(",", ".");
                    double girilen = text.isEmpty() ?0.0 : Double.parseDouble(text);

                    if (girilen >= toplamTutar) {
                        double paraUstu = girilen - toplamTutar;
                        lblParaUstuTutar.setText(df.format(paraUstu) + " TL");
                        lblParaUstuTutar.setForeground(Tema.COL_ACCENT);

                        lblKalanTutar.setText("0,00 TL");
                        btnParcali.setEnabled(false);
                        btnNakitBitir.setText("<html><center>NAKİT AL<br>PARA ÜSTÜ VER</center></html>");
                    } else {
                        double kalan = toplamTutar - girilen;
                        lblParaUstuTutar.setText("0,00 TL");

                        lblKalanTutar.setText(df.format(kalan) + " TL");
                        lblKalanTutar.setForeground(Tema.COL_DANGER);

                        btnParcali.setEnabled(girilen > 0);
                        btnNakitBitir.setText("<html><center>NAKİT<br>TAMAMLA</center></html>");
                    }
                } catch (NumberFormatException ex) {}
            }
        });

        // --- DÜZELTİLEN KISIM: NAKİT GİRİŞ ZORUNLULUĞU ---
        btnNakitBitir.addActionListener(e -> {
            try {
                String text = txtAlinanNakit.getText().replace(",", ".");

                // 1. KONTROL: Kutu boş mu?
                if (text.isEmpty()) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(odemeDialog,
                            "Lütfen alınan nakit tutarı giriniz!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double girilen = Double.parseDouble(text);

                // 2. KONTROL: Sıfır veya negatif mi?
                if (girilen <= 0) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(odemeDialog, "Geçersiz tutar!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (girilen < toplamTutar) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(odemeDialog,
                            "⚠️ GİRİLEN TUTAR EKSİK!\n\n" +
                                    "Toplam Borç: " + df.format(toplamTutar) + " TL\n" +
                                    "Verilen: " + df.format(girilen) + " TL\n\n" +
                                    "Eğer paranın bir kısmı alındıysa lütfen\n'PARÇALI ÖDE' butonunu kullanın.",
                            "Ödeme Hatası", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                islemiTamamla(odemeDialog, toplamTutar, 0);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(odemeDialog, "Hatalı sayı girişi!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnKartBitir.addActionListener(e -> {
            try {
                String text = txtAlinanNakit.getText().replace(",", ".");
                double girilen = text.isEmpty() ?0.0 : Double.parseDouble(text);

                if (girilen > toplamTutar) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(odemeDialog,
                            "⚠️ HATALI İŞLEM!\n\n" +
                                    "Nakit kutusuna toplam tutardan fazla (" + df.format(girilen) + " TL) giriş yapılmış.\n" +
                                    "Bu işlem 'Para Üstü' gerektirir.\n\n" +
                                    "Lütfen 'NAKİT TAMAMLA' butonunu kullanın veya kutuyu temizleyin.",
                            "Mantıksal Hata", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                islemiTamamla(odemeDialog, 0, toplamTutar);

            } catch (Exception ex) {
                islemiTamamla(odemeDialog, 0, toplamTutar);
            }
        });

        btnParcali.addActionListener(e -> {
            try {
                double nakit = Double.parseDouble(txtAlinanNakit.getText().replace(",", "."));
                double kalan = toplamTutar - nakit;
                if (kalan < 0) kalan = 0;
                islemiTamamla(odemeDialog, nakit, kalan);
            } catch (Exception ex) { JOptionPane.showMessageDialog(odemeDialog, "Tutar Hatası!"); }
        });

        JButton btnIndirim = new JButton("% İndirim");
        btnIndirim.setBounds(450, 20, 100, 30);
        btnIndirim.addActionListener(e -> {
            indirimPenceresiAc();
            lblGuncelTutar.setText(df.format(toplamTutar) + " TL");
            txtAlinanNakit.setText("");
            lblKalanTutar.setText(df.format(toplamTutar) + " TL");
        });
        centerPanel.add(btnIndirim);

        btnPanel.add(btnNakitBitir);
        btnPanel.add(btnKartBitir);
        btnPanel.add(btnParcali);

        odemeDialog.add(topPanel, BorderLayout.NORTH);
        odemeDialog.add(centerPanel, BorderLayout.CENTER);
        odemeDialog.add(btnPanel, BorderLayout.SOUTH);

        odemeDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtAlinanNakit.requestFocus();
            }
        });

        odemeDialog.setVisible(true);
    }

    private void islemiTamamla(JDialog dialog, double nakitTutar, double krediTutar) {
        DefaultTableModel model = masaSiparisleri.get(aktifMasaKey);
        java.util.List<SiparisDetayi> detayListesi = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++)
            detayListesi.add(new SiparisDetayi(model.getValueAt(i,0).toString(), (int)model.getValueAt(i, 1), model.getValueAt(i, 3).toString()));

        gunlukSatislar.add(new SatisIslemi(seciliMasaLabel.getText(), toplamTutar, nakitTutar, krediTutar, masaIndirimleri.getOrDefault(aktifMasaKey, 0.0), detayListesi));
        model.setRowCount(0); masaIndirimleri.put(aktifMasaKey, 0.0); aktifOturanMasalar.remove(aktifMasaKey);
        toplamHesapla(); renkleriGuncelle(); txtAdet.setText("1"); dialog.dispose();
    }

    private void seciliUrunuIkramYap() {
        if (aktifMasaKey == null) return;
        int[] selectedRows = siparisTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        DefaultTableModel model = masaSiparisleri.get(aktifMasaKey);
        if (JOptionPane.showConfirmDialog(this, "İKRAM yapılacak?", "Onay", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {
                model.setValueAt("(İKRAM) " + model.getValueAt(row, 0), row, 0);
                model.setValueAt("0.00 TL", row, 2); model.setValueAt("0.00 TL", row, 3);
            }
            toplamHesapla();
        }
    }

    private JButton createMetallicButton(String text, Color topColor, Color bottomColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setBorder(new LineBorder(Color.DARK_GRAY, 1));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }

    private void indirimPenceresiAc() {
        if (aktifMasaKey == null) { JOptionPane.showMessageDialog(this, "Lütfen önce masa seçiniz!"); return; }

        JDialog dialog = new JDialog(this, "İndirim", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Tema.COL_BG_PANEL);

        JButton btnUye = createMetallicButton("ÜYE KARTI (%10)", new Color(229, 228, 226), new Color(176, 176, 176));
        JButton btnSosyal = createMetallicButton("SOSYAL KULÜP (%7)", new Color(255, 215, 0), new Color(218, 165, 32));
        JButton btnPersonel = createMetallicButton("GARSON İNİSİYATİFİ (%5)", new Color(205, 127, 50), new Color(139, 69, 19));
        JButton btnSifirla = createFlatButton("İNDİRİMİ KALDIR", Tema.COL_DANGER, Color.WHITE);

        JPanel manualPanel = new JPanel(new FlowLayout());
        manualPanel.setBackground(Tema.COL_BG_PANEL);
        JLabel lblManuel = new JLabel("Manuel %: ");
        lblManuel.setForeground(Color.WHITE);
        JTextField txtManuel = new JTextField(5);
        JButton btnUygula = new JButton("Uygula");

        btnUye.addActionListener(e -> { indirimUygula(10.0); dialog.dispose(); });
        btnSosyal.addActionListener(e -> { indirimUygula(7.0); dialog.dispose(); });
        btnPersonel.addActionListener(e -> { indirimUygula(5.0); dialog.dispose(); });
        btnSifirla.addActionListener(e -> { indirimUygula(0.0); dialog.dispose(); });

        btnUygula.addActionListener(e -> {
            try {
                double oran = Double.parseDouble(txtManuel.getText());
                indirimUygula(oran);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Geçersiz Sayı!");
            }
        });

        manualPanel.add(lblManuel); manualPanel.add(txtManuel); manualPanel.add(btnUygula);

        dialog.add(btnUye);
        dialog.add(btnSosyal);
        dialog.add(btnPersonel);
        dialog.add(manualPanel);
        dialog.add(btnSifirla);

        dialog.setVisible(true);
    }

    private void indirimUygula(double oran) {
        masaIndirimleri.put(aktifMasaKey, oran);
        toplamHesapla();
    }

    private void rezerveIslemi() {
        if (aktifMasaKey == null) return;
        if (rezervasyonListesi.containsKey(aktifMasaKey)) {
            rezervasyonListesi.remove(aktifMasaKey);
        } else {
            String saat = JOptionPane.showInputDialog("Saat (HH:mm):");
            if(saat!=null) try { rezervasyonListesi.put(aktifMasaKey, LocalDateTime.of(LocalDate.now(), LocalTime.parse(saat))); } catch(Exception e){}
        }
        renkleriGuncelle();
    }

    private JButton createFlatButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) g.setColor(bg.brighter()); else g.setColor(bg);
                g.fillRect(0, 0, getWidth(), getHeight()); super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setBackground(bg); btn.setFocusPainted(false); btn.setBorderPainted(false);
        return btn;
    }

    private JButton createUrunButton(String urunAdi, String emoji) {
        String htmlText = "<html><div style='text-align:center; padding:5px;'>" +
                "<span style='font-size:24px; font-family:\"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Symbola\", \"SansSerif\"'>" + emoji + "</span><br>"+
                "<span style='font-size:14px; font-weight:bold; color:#F3F4F6'>" + urunAdi + "</span><br>" +
                "<span style='font-size:13px; color:#10B981; font-weight:bold'>" + df.format(VeriYoneticisi.urunFiyatlari.getOrDefault(urunAdi, 0.0)) + " TL</span></div></html>";
        JButton btn = createFlatButton(htmlText,Tema.COL_BG_PANEL, Tema.COL_TEXT_MAIN);
        btn.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(55, 65, 81), 1), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        btn.addActionListener(e -> siparisEkle(urunAdi));
        return btn;
    }

    private void yiyecekSecimEkraniGoster(JPanel panel) {
        panel.removeAll(); panel.setLayout(new GridLayout(0, 3, 15, 15));
        String[] alt = {"ANA YEMEK ", "SALATA ", "ÇORBA ", "MEZE ", "APERATİF ", "KAHVALTI "};
        String[] ikon = {"🍖", "🥗", "🍲", "🧆", "🍟", "🍳"};
        for(int i=0; i<alt.length; i++) {
            final String kat = alt[i];
            String html = "<html><center><span style='font-size:36px; font-family:\"Segoe UI Emoji\", \"Segoe UI Symbol\", \"Symbola\"'>" + ikon[i] + "</span><br><br><span style='font-size:16px'>" + kat + "</span></center></html>";
            JButton btn = createFlatButton(html,Tema.COL_BG_PANEL, Tema.COL_TEXT_MAIN);
            btn.setPreferredSize(new Dimension(0, 100));
            btn.setBorder(new LineBorder(Tema.COL_ACCENT, 2));
            btn.addActionListener(e -> {
                panel.setLayout(new GridLayout(0, 4, 10, 10));
                if(kat.contains("ANA")) anaYemekleriGoster(panel);
                else if(kat.contains("SALATA")) salatalariGoster(panel);
                else if(kat.contains("ÇORBA")) corbalariGoster(panel);
                else if(kat.contains("MEZE")) mezeleriGoster(panel);
                else if(kat.contains("APERATİF")) aperatifleriGoster(panel);
                else kahvaltilariGoster(panel);
            });
            panel.add(btn);
        }
        refreshPanel(panel);
    }
    private void icecekSecimEkraniGoster(JPanel panel) {
        panel.removeAll(); panel.setLayout(new GridLayout(1, 2, 20, 20));
        JButton btnS = createFlatButton("<html><center><span style='font-size:36px; font-family:\"Segoe UI Emoji\", \"Segoe UI Symbol\"'>☕</span><br><br><span style='font-size:18px'>SICAKLAR</span></center></html>", Tema.COL_BG_PANEL, Tema.COL_TEXT_MAIN);
        btnS.setBorder(new LineBorder(new Color(139, 69, 19), 2));
        JButton btnC = createFlatButton("<html><center><span style='font-size:36px; font-family:\"Segoe UI Emoji\", \"Segoe UI Symbol\"'>❄️</span><br><br><span style='font-size:18px'>SOĞUKLAR</span></center></html>", Tema.COL_BG_PANEL, Tema.COL_TEXT_MAIN);
        btnC.setBorder(new LineBorder(new Color(0, 191, 255), 2));
        btnS.addActionListener(e -> { panel.setLayout(new GridLayout(0, 4, 10, 10)); sicaklariGoster(panel); });
        btnC.addActionListener(e -> { panel.setLayout(new GridLayout(0, 4, 10, 10)); soguklariGoster(panel); });
        panel.add(btnS); panel.add(btnC);
        refreshPanel(panel);
    }

    private void anaYemekleriGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Izgara Köfte", "🧆"}, {"Bonfile", "🥩"}, {"Tavuk Şiş", "🍢"}, {"Adana Kebap", "🌶️"}, {"Urfa Kebap", "🍽️"}, {"Kanat", "🍗"}, {"Çökertme Kebabı", "🥙"}, {"Mantı", "🥟"}, {"İskender", "🥙"}, {"Ali Nazik", "🍆"}, {"Hamburger", "🍔"}, {"Pizza", "🍕"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void salatalariGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Sezar Salata", "🥗"}, {"Mevsim Salata", "🥬"}, {"Çoban Salata", "🍅"}, {"Ton Balıklı", "🐟"}, {"Akdeniz Salata", "🌽"}, {"Hellim Salata", "🧀"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void corbalariGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Mercimek Çorbası", "🥣"}, {"Ezogelin Çorbası", "🥣"}, {"Domates Çorbası", "🍅"}, {"Yayla Çorbası", "🥣"}, {"İşkembe Çorbası", "🥣"}, {"Kelle Paça Çorbası", "🍲"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void mezeleriGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Haydari", "🥣"}, {"Humus", "🥣"}, {"Acılı Ezme", "🌶️"}, {"Şakşuka", "🍆"}, {"Fava", "🫘"}, {"Atom", "🔥"}, {"Girit Ezme", "🧀"}, {"Patlıcan Salatası", "🍆"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void aperatifleriGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Patates Kızartması", "🍟"}, {"Soğan Halkası", "🧅"}, {"Sigara Böreği", "🌯"}, {"Paçanga", "🥓"}, {"Sosis Tabağı", "🌭"}, {"Çıtır Tavuk", "🍗"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void kahvaltilariGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Serpme Kahvaltı", "🥐"}, {"Kahvaltı Tabağı", "🧀"}, {"Menemen", "🥘"}, {"Sahanda Yumurta", "🍳"}, {"Sucuklu Yumurta", "🍳"}, {"Omlet", "🥚"}, {"Tost", "🥪"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void kampanyalariGoster(JPanel panel) { panel.removeAll(); panel.setLayout(new GridLayout(0, 4, 10, 10)); String[][] u = {{"2'li Burger Menü", "🍔"}, {"Aile Boyu Pizza", "🍕"}, {"Köfte + Ayran", "🥩"}, {"Kahve + Tatlı", "🍰"}, {"Çocuk Menüsü", "🧸"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void sicaklariGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Çay", "🍵"}, {"Fincan Çay", "🍵"}, {"Türk Kahvesi", "☕"}, {"Espresso", "☕"}, {"Latte", "🥛"}, {"Sahlep", "🥣"}, {"Bitki Çayı", "🌿"}, {"Sıcak Çikolata", "🍫"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void soguklariGoster(JPanel panel) { panel.removeAll(); String[][] u = {{"Kola", "🥤"}, {"Fanta", "🍊"}, {"Ayran", "🥛"}, {"Su", "💧"}, {"Limonata", "🍋"}, {"Meyve Suyu", "🍎"}, {"Soda", "🍾"}, {"Şalgam", "🍷"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }
    private void tatlilariGoster(JPanel panel) { panel.removeAll(); panel.setLayout(new GridLayout(0, 4, 10, 10)); String[][] u = {{"Porsiyon Baklava", "🍯"}, {"Künefe", "🥧"}, {"Sütlaç", "🍮"}, {"Top Dondurma", "🍨"}, {"Kazandibi", "🍮"}, {"Dilim Pasta", "🍰"}, {"Waffle", "🧇"}, {"Trileçe", "🍰"}}; for(String[] i:u) panel.add(createUrunButton(i[0], i[1])); refreshPanel(panel); }

    private void toplamHesapla() {
        if (aktifMasaKey == null) { toplamLabel.setText("---"); return; }

        String masaKisaAd = aktifMasaKey;

        // 1. ADİSYON TOPLAMI (Tüm Ürünler)
        double adisyonToplami = 0.0;
        DefaultTableModel model = masaSiparisleri.get(aktifMasaKey);
        for(int i=0; i<model.getRowCount(); i++) {
            String urun = model.getValueAt(i, 0).toString();
            if(urun.contains("(İKRAM)")) continue;
            int adet = (int) model.getValueAt(i, 1);
            double fiyat = VeriYoneticisi.urunFiyatlari.getOrDefault(urun, 0.0);
            adisyonToplami += (adet * fiyat);
        }

        // 2. BEKLEYENLERİ DÜŞ
        double bekleyenTutar = 0.0;
        for (MutfakSiparisi ms : mutfakListesi) {
            if (ms.masaAdi.equals(masaKisaAd) && ms.durum.equals("BEKLENİYOR")) {
                double birimFiyat = VeriYoneticisi.urunFiyatlari.getOrDefault(ms.urunAdi, 0.0);
                bekleyenTutar += (birimFiyat * ms.adet);
            }
        }

        hamTutar = adisyonToplami - bekleyenTutar;
        if(hamTutar < 0) hamTutar = 0;

        double indirim = masaIndirimleri.getOrDefault(aktifMasaKey, 0.0);
        toplamTutar = hamTutar - (hamTutar * (indirim / 100.0));
        toplamLabel.setText(df.format(toplamTutar) + " TL");
    }

    private void dosyayaKaydet(String icerik, String dosyaAdi) {
        try {
            File file = new File(dosyaAdi);
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(icerik);
                writer.newLine(); // Satır sonu
                writer.write("------------------------------------------------------------"); // Ayraç
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Dosya yazma hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void sonIslemleriGoster() {
        if (gunlukSatislar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Henüz hiç satış yapılmadı.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Son İşlemler Geçmişi", true);
        dialog.setSize(600, 750);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Tema.COL_BG_MAIN);
        dialog.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Tema.COL_BG_PANEL);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblHeader = new JLabel("SON İŞLEM KAYITLARI");
        lblHeader.setForeground(Tema.COL_ACCENT);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnKaydet = createFlatButton("LİSTEYİ KAYDET", Tema.COL_BTN_DARK, Color.WHITE);
        btnKaydet.setPreferredSize(new Dimension(140, 35));

        headerPanel.add(lblHeader, BorderLayout.WEST);
        headerPanel.add(btnKaydet, BorderLayout.EAST);

        // --- HTML String Oluşturma ---
        StringBuilder sbHtml = new StringBuilder("<html><body style='background-color:#111827; color:#F9FAFB; font-family:Segoe UI; padding:10px;'>");
        StringBuilder sbText = new StringBuilder("=== İŞLEM GEÇMİŞİ LİSTESİ ===\n\n"); // Dosya çıktısı için düz metin

        for (int i = gunlukSatislar.size() - 1; i >= 0; i--) {
            SatisIslemi s = gunlukSatislar.get(i);
            String zaman = s.islemZamani.format(DateTimeFormatter.ofPattern("HH:mm"));
            String tarih = s.islemZamani.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            // KART GÖRÜNÜMÜ
            sbHtml.append("<div style='background-color:#1F2937; border:1px solid #374151; padding:15px; margin-bottom:15px; border-radius:8px;'>");
            sbHtml.append("<div style='border-bottom:1px solid #4B5563; padding-bottom:8px; margin-bottom:8px;'>");
            sbHtml.append("<b style='font-size:16px; color:#10B981;'>").append(s.aciklama).append("</b>"); // Masa Adı
            sbHtml.append("<span style='float:right; font-size:12px; color:#9CA3AF;'>").append(tarih).append(" ").append(zaman).append("</span></div>");

            sbHtml.append("<div style='font-size:13px; color:#D1D5DB; margin-bottom:10px;'>");

            sbText.append("İşlem: ").append(s.aciklama).append(" | Tarih: ").append(tarih).append(" ").append(zaman).append("\n");

            for (SiparisDetayi det : s.urunListesi) {
                sbHtml.append("• ").append(det.adet).append("x ").append(det.urunAdi)
                        .append(" <span style='color:#6B7280;'>(").append(det.toplamFiyat).append(")</span><br>");
                sbText.append("  * ").append(det.adet).append("x ").append(det.urunAdi).append(" (").append(det.toplamFiyat).append(")\n");
            }
            sbHtml.append("</div>");

            sbHtml.append("<div style='background-color:#111827; padding:8px; border-radius:4px; font-size:12px; margin-bottom:5px;'>");
            if(s.nakitOdemeTutari > 0) sbHtml.append("<span style='color:#60A5FA;'>💵 Nakit: ").append(df.format(s.nakitOdemeTutari)).append(" TL</span> &nbsp; ");
            if(s.krediOdemeTutari > 0) sbHtml.append("<span style='color:#F59E0B;'>💳 Kredi: ").append(df.format(s.krediOdemeTutari)).append(" TL</span> &nbsp; ");
            if(s.indirimOrani > 0) sbHtml.append("<span style='color:#EF4444;'>🔻 İndirim: %").append(s.indirimOrani).append("</span>");
            sbHtml.append("</div>");

            sbHtml.append("<div style='text-align:right;'>");
            sbHtml.append("<b style='font-size:18px; color:#F9FAFB;'>TOPLAM: ").append(df.format(s.netTutar)).append(" TL</b>");
            sbHtml.append("</div></div>");

            sbText.append("Ödeme Detayı: Nakit[").append(df.format(s.nakitOdemeTutari))
                    .append("] Kredi[").append(df.format(s.krediOdemeTutari))
                    .append("] İndirim[%").append(s.indirimOrani).append("]\n");
            sbText.append("NET TUTAR: ").append(df.format(s.netTutar)).append(" TL\n--------------------------------------------------\n");
        }
        sbHtml.append("</body></html>");

        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(sbHtml.toString());
        textPane.setEditable(false);
        textPane.setBackground(Tema.COL_BG_MAIN);
        textPane.setCaretPosition(0);

        btnKaydet.addActionListener(e -> {
            dosyayaKaydet(sbText.toString(), "Detayli_Islem_Gecmisi.txt");
            JOptionPane.showMessageDialog(dialog, "Liste 'Detayli_Islem_Gecmisi.txt' olarak kaydedildi.");
        });

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void gunSonuRaporuGoster() {
        double toplamCiro = 0;
        double toplamNakit = 0;
        double toplamKredi = 0;
        double toplamIndirim = 0;
        int islemSayisi = gunlukSatislar.size();

        Map<String, Integer> satilanUrunToplami = new HashMap<>();
        int enUzunIsim = 0;

        for (SatisIslemi s : gunlukSatislar) {
            toplamCiro += s.netTutar;
            toplamNakit += s.nakitOdemeTutari;
            toplamKredi += s.krediOdemeTutari;

            if (s.indirimOrani > 0) {
                double ham = s.netTutar / (1.0 - (s.indirimOrani / 100.0));
                toplamIndirim += (ham - s.netTutar);
            }

            for (SiparisDetayi sd : s.urunListesi) {
                satilanUrunToplami.put(sd.urunAdi, satilanUrunToplami.getOrDefault(sd.urunAdi, 0) + sd.adet);
                if (sd.urunAdi.length() > enUzunIsim) {
                    enUzunIsim = sd.urunAdi.length();
                }
            }
        }

        int padUrun = Math.max(20, enUzunIsim + 2);
        String urunFormat = "- %-" + padUrun + "s : %3d Adet\n";
        String finansFormat = "%-25s : %15s TL\n";

        // --- RAPOR METNİ OLUŞTURMA ---
        StringBuilder raporDetay = new StringBuilder();
        raporDetay.append("=== Z RAPORU (GÜN SONU) ===\n");
        raporDetay.append("Tarih: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))).append("\n\n");

        raporDetay.append("FİNANSAL ÖZET:\n");
        raporDetay.append(String.format(finansFormat,"Toplam Ciro", df.format(toplamCiro)));
        raporDetay.append(String.format(finansFormat,"Nakit Tahsilat", df.format(toplamNakit)));
        raporDetay.append(String.format(finansFormat,"Kredi Kartı", df.format(toplamKredi)));
        raporDetay.append(String.format(finansFormat,"Yapılan İndirim", df.format(toplamIndirim)));
        raporDetay.append(String.format("%-25s : %15d\n\n", "Toplam İşlem", islemSayisi));

        raporDetay.append("SATILAN ÜRÜN DÖKÜMÜ:\n");
        if (satilanUrunToplami.isEmpty()) {
            raporDetay.append("  (Satış Yok)\n");
        } else {
            for (Map.Entry<String, Integer> entry : satilanUrunToplami.entrySet()) {
                raporDetay.append(String.format(urunFormat, entry.getKey(), entry.getValue()));
            }
        }
        raporDetay.append("\n=== RAPOR SONU ===");

        JDialog zDialog = new JDialog(this, "Z RAPORU - GÜN SONU", true);
        zDialog.setSize(550, 700);
        zDialog.setLocationRelativeTo(this);
        zDialog.getContentPane().setBackground(Tema.COL_BG_MAIN);
        zDialog.setLayout(new BorderLayout(15, 15));

        JLabel lblTitle = new JLabel("Z RAPORU DETAYI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Tema.COL_ACCENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        infoGrid.setBackground(Tema.COL_BG_MAIN);
        infoGrid.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        infoGrid.add(createInfoPanel("TOPLAM CİRO", df.format(toplamCiro) + " TL", Tema.COL_ACCENT));
        infoGrid.add(createInfoPanel("İŞLEM ADEDİ", String.valueOf(islemSayisi), Color.WHITE));
        infoGrid.add(createInfoPanel("NAKİT TOPLAM", df.format(toplamNakit) + " TL", new Color(100, 200, 255)));
        infoGrid.add(createInfoPanel("KREDİ TOPLAM", df.format(toplamKredi) + " TL", new Color(255, 200, 100)));

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(Tema.COL_BG_MAIN);
        mainContentPanel.add(infoGrid, BorderLayout.NORTH);

        JTextArea txtEkranRaporu = new JTextArea();
        txtEkranRaporu.setText(raporDetay.toString());
        txtEkranRaporu.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtEkranRaporu.setBackground(Tema.COL_BG_PANEL);
        txtEkranRaporu.setForeground(Color.LIGHT_GRAY);
        txtEkranRaporu.setEditable(false);
        txtEkranRaporu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane textScroll = new JScrollPane(txtEkranRaporu);
        textScroll.setBorder(new LineBorder(Tema.COL_BTN_DARK));
        textScroll.getVerticalScrollBar().setUnitIncrement(16);

        mainContentPanel.add(textScroll, BorderLayout.CENTER);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Tema.COL_BG_MAIN);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton btnKapat = createFlatButton("KAPAT VE KAYDET", Tema.COL_BTN_DARK, Color.WHITE);
        btnKapat.setPreferredSize(new Dimension(0, 50));
        btnKapat.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnKapat.addActionListener(e -> {
            dosyayaKaydet(raporDetay.toString(), "Gunluk_Z_Raporlari.txt");
            JOptionPane.showMessageDialog(zDialog, "Z Raporu 'Gunluk_Z_Raporlari.txt' dosyasına kaydedildi.");
            zDialog.dispose();
        });

        bottomPanel.add(btnKapat, BorderLayout.SOUTH);

        zDialog.add(lblTitle, BorderLayout.NORTH);
        zDialog.add(mainContentPanel, BorderLayout.CENTER);
        zDialog.add(bottomPanel, BorderLayout.SOUTH);
        zDialog.setVisible(true);
    }

    private JPanel createInfoPanel(String title, String value, Color valueColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.COL_BG_PANEL);
        p.setBorder(new LineBorder(Tema.COL_BTN_DARK, 1));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(valueColor);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        p.add(lblTitle, BorderLayout.NORTH);
        p.add(lblValue, BorderLayout.CENTER);
        return p;
    }

    private void refreshPanel(JPanel panel) { panel.revalidate(); panel.repaint(); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> LoginEkrani.goster());
    }
}