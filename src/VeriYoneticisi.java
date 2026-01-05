import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VeriYoneticisi {

    public static Map<String, Double> urunFiyatlari = new HashMap<>();
    public static Set<String> hizliServisUrunleri = new HashSet<>();

    // Constructor çalıştığında verileri yükler
    public VeriYoneticisi() {
        urunFiyatlariYukle();
        hizliServisListesiYukle();
    }

    private void hizliServisListesiYukle() {
        String[] sicaklar = {"Çay", "Fincan Çay", "Türk Kahvesi", "Espresso", "Latte", "Sahlep", "Bitki Çayı", "Sıcak Çikolata"};
        for(String s : sicaklar) hizliServisUrunleri.add(s);
        String[] soguklar = {"Kola", "Fanta", "Ayran", "Su", "Limonata", "Meyve Suyu", "Soda", "Şalgam"};
        for(String s : soguklar) hizliServisUrunleri.add(s);
        String[] tatlilar = {"Porsiyon Baklava", "Künefe", "Sütlaç", "Top Dondurma", "Kazandibi", "Dilim Pasta", "Waffle", "Trileçe"};
        for(String s : tatlilar) hizliServisUrunleri.add(s);
    }

    private void urunFiyatlariYukle() {
        urunFiyatlari.put("Izgara Köfte", 380.00); urunFiyatlari.put("Bonfile", 560.00); urunFiyatlari.put("Tavuk Şiş", 250.00);
        urunFiyatlari.put("Adana Kebap", 420.00); urunFiyatlari.put("Urfa Kebap", 410.00); urunFiyatlari.put("Kanat", 300.00);
        urunFiyatlari.put("Çökertme Kebabı", 540.00); urunFiyatlari.put("Mantı", 240.00); urunFiyatlari.put("İskender", 480.00);
        urunFiyatlari.put("Ali Nazik", 320.00); urunFiyatlari.put("Hamburger", 320.00); urunFiyatlari.put("Pizza", 400.00);
        urunFiyatlari.put("Sezar Salata", 280.00); urunFiyatlari.put("Mevsim Salata", 150.00); urunFiyatlari.put("Çoban Salata", 180.00);
        urunFiyatlari.put("Ton Balıklı", 320.00); urunFiyatlari.put("Akdeniz Salata", 240.00); urunFiyatlari.put("Hellim Salata", 260.00);
        urunFiyatlari.put("Mercimek Çorbası", 100.00); urunFiyatlari.put("Ezogelin Çorbası", 100.00); urunFiyatlari.put("Domates Çorbası", 100.00);
        urunFiyatlari.put("Yayla Çorbası", 100.00); urunFiyatlari.put("İşkembe Çorbası", 160.00); urunFiyatlari.put("Kelle Paça Çorbası", 180.00);
        urunFiyatlari.put("Haydari", 150.00); urunFiyatlari.put("Humus", 130.00); urunFiyatlari.put("Acılı Ezme", 80.00);
        urunFiyatlari.put("Şakşuka", 140.00); urunFiyatlari.put("Fava", 100.00); urunFiyatlari.put("Atom", 160.00);
        urunFiyatlari.put("Girit Ezme", 140.00); urunFiyatlari.put("Patlıcan Salatası", 100.00);
        urunFiyatlari.put("Patates Kızartması", 140.00); urunFiyatlari.put("Soğan Halkası", 120.00); urunFiyatlari.put("Sigara Böreği", 160.00);
        urunFiyatlari.put("Paçanga", 220.00); urunFiyatlari.put("Sosis Tabağı", 240.00); urunFiyatlari.put("Çıtır Tavuk", 260.00);
        urunFiyatlari.put("Serpme Kahvaltı", 650.00); urunFiyatlari.put("Kahvaltı Tabağı", 350.00); urunFiyatlari.put("Menemen", 180.00);
        urunFiyatlari.put("Sahanda Yumurta", 180.00); urunFiyatlari.put("Sucuklu Yumurta", 220.00); urunFiyatlari.put("Omlet", 180.00); urunFiyatlari.put("Tost", 150.00);
        urunFiyatlari.put("2'li Burger Menü", 580.00); urunFiyatlari.put("Aile Boyu Pizza", 650.00); urunFiyatlari.put("Köfte + Ayran", 380.00);
        urunFiyatlari.put("Kahve + Tatlı", 280.00); urunFiyatlari.put("Çocuk Menüsü", 300.00);
        urunFiyatlari.put("Çay", 35.00); urunFiyatlari.put("Fincan Çay", 50.00); urunFiyatlari.put("Türk Kahvesi", 120.00);
        urunFiyatlari.put("Espresso", 150.00); urunFiyatlari.put("Latte", 150.00); urunFiyatlari.put("Sahlep", 100.00);
        urunFiyatlari.put("Bitki Çayı", 80.00); urunFiyatlari.put("Sıcak Çikolata", 100.00);
        urunFiyatlari.put("Kola", 65.00); urunFiyatlari.put("Fanta", 65.00); urunFiyatlari.put("Ayran", 40.00);
        urunFiyatlari.put("Su", 25.00); urunFiyatlari.put("Limonata", 120.00); urunFiyatlari.put("Meyve Suyu", 65.00);
        urunFiyatlari.put("Soda", 40.00); urunFiyatlari.put("Şalgam", 60.00);
        urunFiyatlari.put("Porsiyon Baklava", 180.00); urunFiyatlari.put("Künefe", 280.00); urunFiyatlari.put("Sütlaç", 160.00);
        urunFiyatlari.put("Top Dondurma", 80.00); urunFiyatlari.put("Kazandibi", 180.00); urunFiyatlari.put("Dilim Pasta", 180.00);
        urunFiyatlari.put("Waffle", 300.00); urunFiyatlari.put("Trileçe", 180.00);
    }
}