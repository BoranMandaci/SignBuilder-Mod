<div align="center">
  <a href="README.md">🇬🇧 English</a> • <b>🇹🇷 Türkçe</b>
</div>

---

# 🪧 Sign Builder

Özel 3D tabelaları sorunsuzca inşa etmek, renklendirmek, aydınlatmak ve materyalleştirmek için tam özellikli, platformlar arası bir Minecraft modu. Hayatta Kalma (Survival) modu için mükemmel şekilde dengelenmiş son derece ayrıntılı, dinamik bir inşa sistemiyle şehirlerinizi süsleyin, etkileşimli redstone tuş takımı kilitleri oluşturun ve parlayan neon dükkan vitrinleri ya da devasa panolar inşa edin.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Giriş

Sign Builder, Architectury API kullanılarak hem Fabric hem de Forge için sıfırdan geliştirilmiş, platformlar arası bir Minecraft modudur. Dünyanızda son derece ayrıntılı 3D metinler oluşturmanız için kapsamlı bir iş akışı sunar. Basit vanilla tabelalara bağlı kalmak yerine harfler, sayılar, semboller ve modüler arka plakalar için fiziksel bloklar sağlarken; bunları dinamik olarak inşa etmek, boyamak, dokulandırmak, otomatikleştirmek ve aydınlatmak için özel araçlar sunar.

## ✨ Temel Özellikler

*   🏢 **Devasa 2x2 & 3x3 Çoklu Blok (Multi-Block) Tabelalar** — Daha büyük tabelalara mı ihtiyacınız var? Dört eşleşen tabela bloğunu 2x2 kare şeklinde yerleştirmek 2.0x boyutunda bir tabela oluştururken, 3x3 bir kare otomatik olarak **3.0x devasa bloklara** dönüşür! Duvar, zemin ve tavan yönlerinde senkronize çoklu blok kırma, ölçeklendirilmiş malzeme iadesi (4x / 9x), dinamik hitbox'lar, ekran dışı görüş açısı (FOV) çizim koruması, master-dummy veri senkronizasyonu ve çoklu blok arka plakaları içerir.
*   🔴 **Etkileşimli Butonlar & Redstone Otomasyonu** — Herhangi bir karakteri etkileşimli bir mekanizmaya dönüştürün! İngiliz Anahtarını (Wrench) kullanarak tabelaları **Kısa Darbeli Buton (Pulse)** veya **Şalter (Lever)** olarak ayarlayın:
    *   **Yönlü Çıkış:** Tam Redstone gücünü (15 sinyal gücü) yalnızca bloğun arkasındaki montaj yüzeyinden yayar.
    *   **Materyale Duyarlı Zamanlama:** Ahşap tabelalar 1,5 saniye (30 tick) basılı kalırken, metal/taş materyaller kendine has fiziksel ses profilleri eşliğinde 1,0 saniye (20 tick) sonra sıfırlanır.
    *   **Kelime Çapında Senkronizasyon:** Harfleri bağımsız basılacak şekilde yapılandırın veya flood-fill mantığıyla bağlı tüm kelimeyi aynı anda tetikleyin.
    *   **Menzilli Tetikleme:** Uzaktan etkinleştirmek için duvardaki butonlara ok, mızrak veya kartopu ile ateş edin!
    *   **Analog Karşılaştırıcı Desteği:** Sayı blokları (0–9), sayısal değerlerini (0–9 sinyal gücü) doğrudan Redstone Karşılaştırıcılarına (Comparator) aktarır.
*   🔐 **PIN Tuş Takımı Güvenlik Sistemi** — Yazıları gizli şifreli kilitlere dönüştürün!
    *   **Sıralı Kilitleme:** Karakterler basıldıkça içe doğru kilitlenir. Doğru dizilim girildiğinde girilen tüm bloklar boyunca 2 saniyelik bir redstone darbesi yayar; geçersiz bir dizilim girildiğinde ise sesli geri bildirimle kilidi anında sıfırlar.
    *   **Dokunarak Kaydetme ("REC"):** Dünyada doğrudan PIN kodlarını zahmetsizce yapılandırın! Wrench arayüzündeki "REC" butonuna dokunun, bloklara sırayla tıklayın ve kaydetmek için Shift + Sağ Tıklayın.
*   🛡️ **Modüler Arka Plaka (Backplate) Sistemi** — Tabelalarınıza derinlik ve kontrast kazandırın! Tabelaları önceden yerleştirilmiş arka plakalara monte edin veya doğrudan mevcut tekli, 2x2 ya da 3x3 tabelalara bir arka plaka takın. Ön ve arka yüzeyler bağımsız olarak dokulandırılabilir ve boyanabilir. Boş elle Shift + Sağ Tık yapmak, materyalleri iade ederek plakayı güvenle söker.
*   🧱 **40 Dinamik Materyal & 2 Sayfalı Arayüz Düzeni** — Tabelalar artık sadece betondan ibaret değil! Metinlerinizi Bakır, Ametist, Kömür, Derintaş Tuğlaları, Çamur Tuğlaları, Nether Tuğlaları, Kızıl & Çarpık Tahtalar, Redstone, Netherit, Kuvars, Zümrüt, ahşaplar ve madenler dahil 40 farklı materyalle özelleştirin. Boya Fırçası ekranındaki 20 öğelik dengeli ızgara (`<` / `>`) ile sayfalar arasında kolayca gezinin.
*   🎒 **Hayatta Kalmaya Hazır & Gerçekçi Ganimet** — Bir tabelayı kırmak, üretilen bileşenlerini düşürür (3x Beyaz Beton, Temel Materyaller, Işık Taşı Tozu). Düşen eşyalar çoklu blok boyutuna göre dinamik olarak ölçeklenir (2x2 için 4x, 3x3 panolar için 9x). **İpeksi Dokunuş (Silk Touch)** aletiyle kırmak, dropped eşya üzerindeki tüm özel NBT verilerini (renkler, materyaller, parlama durumları, PIN kodları, animasyonlar) kusursuz şekilde korur!
*   🏗️ **Geliştirilmiş 3D Modeller & Uluslararası Semboller** — Piksel hassasiyetinde hitbox'lara sahip yenilenmiş 3D karakter setleri: Latin harfleri (A-Z), Almanca ve Türkçe özel harfler, sayılar, ana ve çapraz yön okları, para birimi sembolleri (€, $, ₺, ¥, £), matematiksel operatörler ve kapsamlı bir sembol listesi: Çarpı (`✗`), Daire (`○`), Karo (`◆`), Müzik Notaları (`♪`, `♫`), Kurukafa (`☠`), Kalp (`♥`), Yıldız (`★`), Onay İmi (`✓`), Sonsuzluk (`∞`), parantezler ve noktalama işaretleri.
*   🗜️ **Tabela Presi (Sign Press)** — Özel bir hayatta kalma üretim istasyonu. Beyaz betonlarınızı temiz ve verimli bir şekilde belirli harflere, sembollere ve arka plakalara dönüştürün. Otomatik iş akışları için hunilerle (hopper) tam uyumludur.
*   🗺️ **Holografik Taslak (Blueprint), 3 Yönlü Boyutlandırma & Geri Alma (Undo)** — Metninizi doğrudan sembol butonları içeren kompakt Blueprint arayüzüne yazın. Dünyada **yeşil** (geçerli) ve **kırmızı** (engelli) arasında dinamik olarak geçiş yapan gerçek zamanlı **yarı saydam 3D hayalet önizlemenin** keyfini çıkarın. Yatay veya Y eksenine kilitli **dikey yerleştirme**, 3 yönlü boyut döngüsü (**1x1**, **2x2**, **3x3**) ve **otomatik arka plaka anahtarı** sunar. Tek tıklamayla Geri Alma (Undo) sistemi, hatalı yerleştirmeleri temiz bir şekilde kaldırır ve tüm blokları ile arka plakaları eksiksiz iade eder.
*   🎨 **Boya Fırçası & Özel Palet** — Duyarlı arayüzü açmak için havaya sağ tıklayın. Kendi RGB/Hex kodlarınızı karıştırın ve kişisel paletinize 14 adede kadar özel renk kaydedin ya da birden çok sayfa boyunca materyal dokularını doğrudan tabela yüzeylerine ve arka plakalara uygulayın.
*   🌈 **Akıllı Doldurma (Smart Fill) & Gökkuşağı Modu** — Sezgisel dairesel HUD göstergeleri eşliğinde "Smart Fill" özelliğini açıp kapatmak için havaya Shift + Sağ Tıklayın. Bağlı tüm kelimeleri sıfır görsel gecikmeyle anında tek seferde boyayın, aydınlatın veya canlandırın.
*   💧 **Damlalık (Eyedropper) Mekaniği** — Dünyadaki herhangi bir boyalı bloğun tam hex rengini doğrudan Boya Fırçanıza kopyalamak için bloğa Shift + Sağ Tıklayın.
*   🌍 **Küresel Yerelleştirme** — 13 dile tamamen çevrilmiştir: İngilizce, Türkçe, Almanca, Fransızca, İspanyolca, İtalyanca, Rusça, Basitleştirilmiş Çince, Brezilya Portekizcesi, Japonca, Korece, Lehçe ve Geleneksel Çince.

## 🛠️ Teknoloji Yığını

**Modlama API'si & Diller**
*   ☕ **Java** — Çekirdek mantık ve arka uç.
*   🧩 **Architectury API** — Eşzamanlı Forge ve Fabric geliştirmesi için platformlar arası soyutlama katmanı.
*   🦊 **Fabric** / 🔨 **Forge** — Mod yükleyicileri.

**Araçlar**
*   🧊 **Blockbench** — Tüm karakter, arka plaka ve alet blokları için özel 3D modelleme ve dokulandırma.
*   🐘 **Gradle** — Derleme otomasyonu ve bağımlılık yönetimi.

## 🚀 Başlarken

### Gereksinimler
*   Minecraft `1.20.1`
*   **Fabric** veya **Forge** Mod Yükleyicisi
*   [Architectury API](https://modrinth.com/mod/architectury-api) (Zorunlu Bağımlılık)

### Kurulum
1.  Modun en son sürümünü **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** veya **[Modrinth](https://modrinth.com/mod/sign-builder)** üzerinden indirin.
2.  Architectury API'nin gerekli sürümünü indirin (Fabric kullanıyorsanız Fabric API'yi de indirin).
3.  `.jar` dosyalarını Minecraft `mods` klasörünüze bırakın.
4.  Oyunu başlatın!

## 🤝 Katkıda Bulunma
Bu proje öncelikli olarak kişisel bir portföy projesidir; ancak sorun bildirimleri, öneriler ve pull request'ler memnuniyetle karşılanır. Yaklaşımı tartışabilmemiz adına önemsiz olmayan her şey için lütfen önce bir konu (issue) açın.

## ⚖️ Sorumluluk Reddi & Yasal Bildirim
*   Bu proje **GNU General Public License v3.0 (GPLv3)** kapsamında lisanslanmıştır. Daha fazla ayrıntı için `LICENSE` dosyasına bakın.
*   Bu proje tamamen Minecraft için hayran yapımı, açık kaynaklı bir modifikasyondur.
*   Tüm özel 3D modeller ve kod uygulamaları yazar tarafından oluşturulmuş orijinal eserlerdir.

**Boran Mandacı** tarafından geliştirildi
