import java.time.LocalDateTime;
import java.util.List;

public class SatisIslemi {
    public String aciklama;
    public double netTutar;
    public double nakitOdemeTutari;
    public double krediOdemeTutari;
    public double indirimOrani;
    public LocalDateTime islemZamani;
    public List<SiparisDetayi> urunListesi;

    public SatisIslemi(String aciklama, double netTutar, double nakitOdemeTutari, double krediOdemeTutari, double indirimOrani, List<SiparisDetayi> urunListesi) {
        this.aciklama = aciklama;
        this.netTutar = netTutar;
        this.nakitOdemeTutari = nakitOdemeTutari;
        this.krediOdemeTutari = krediOdemeTutari;
        this.indirimOrani = indirimOrani;
        this.urunListesi = urunListesi;
        this.islemZamani = LocalDateTime.now();
    }
}