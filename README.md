## 📂 Proje Mimarisi ve Dosya Yapısı

Proje, "Yazılım İnşası" prensiplerine uygun olarak, tek bir dosya yerine **görevlerine göre ayrılmış sınıflardan** oluşmaktadır.

ID:Adisyon
Şifre:Premium

```
AdisyonSistemi/
├── src/
│   ├── AdisyonSistemi.java   # Ana kontrolcü ve Arayüz Yönetimi
│   ├── LoginEkrani.java      # Güvenli Giriş Sistemi (Entry Point)
│   ├── MutfakSiparisi.java   # Mutfak işlemleri veri modeli
│   ├── SatisIslemi.java      # Satış kaydı ve ciro hesaplama modeli
│   ├── SiparisDetayi.java    # Sipariş kalemleri veri yapısı
│   ├── VeriYoneticisi.java   # Dosya okuma/yazma (File I/O) işlemleri
│   └── Tema.java             # UI Renk paleti ve tasarım sabitleri
│
├── docs/
│   ├── ANALİZ RAPORU (ANALYSIS REPORT) (2).docx
│   ├── TASARIM RAPORU (FİNAL REPORT).docx
│   └── FİNAL RAPORU (FINAL REPORT).docx
│
└── README.md

```

---

## ✨ Temel Özellikler

### 1. Modüler Tasarım (OOP)

Spagetti kod yapısından kaçınılarak, her sınıfın tek bir sorumluluğu üstlendiği (Single Responsibility) bir yapı kurulmuştur:

* **Veri Yönetimi:** Tüm dosya işlemleri `VeriYoneticisi` sınıfında izole edilmiştir.
* **Tasarım:** Renk kodları ve fontlar `Tema` sınıfından çekilir, böylece tasarım tutarlılığı sağlanır.

### 2. Gelişmiş Mutfak Simülasyonu (Concurrency)

* **Otomatik Süreç:** Siparişler mutfağa düştüğünde `javax.swing.Timer` ile simüle edilir.
* **Durum Takibi:** Ürünler sırasıyla *Bekliyor* ➔ *Hazırlanıyor* ➔ *Hazır* durumlarına geçer.
* **Akıllı Servis:** Çay/Meşrubat gibi hızlı ürünler bekleme süresine takılmadan servis edilir.

### 3. Kritik Kontroller

* **Rezervasyon:** Rezerve edilmiş masalara (1.5 saat kuralına göre) oturulmak istendiğinde sistem uyarır.
* **Ödeme Validasyonu:** Eksik veya hatalı ödeme girişleri engellenir.

---

## 🚀 Kurulum ve Çalıştırma

Projeyi yerel makinenizde çalıştırmak için:

1. Bu repoyu klonlayın:
```bash
git clone https://github.com/MertAlbuzlu/AdisyonSistemi.git

```


2. Proje klasörünü IDE (IntelliJ IDEA veya Eclipse) ile açın.
3. `src` klasörü altındaki **`LoginEkrani.java`** dosyasını çalıştırın.
4. **Giriş Bilgileri:**
* **Kullanıcı ID:** `Adisyon`
* **Şifre:** `Premium`



---



---

## 📄 Proje Dokümantasyonu

Projenin teknik detayları, analiz ve tasarım süreçleri `docs` klasöründe sunulmuştur:

---

## 👨‍💻 Geliştirici

**Mert Can ALBUZLU**
**Şeyhmus SÜMER**
---

*Bu proje Yazılım İnşası dersi kapsamında geliştirilmiştir.*

```
