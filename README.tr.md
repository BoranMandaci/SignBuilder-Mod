<div align="center">
  <a href="README.md">🇬🇧 English</a> • <b>🇹🇷 Türkçe</b>
</div>

---

# 🪧 Sign Builder

Özel 3D tabelalar inşa etmek, renklendirmek, aydınlatmak ve materyallerle kaplamak için tasarlanmış kapsamlı, platformlar arası bir Minecraft modu. Şehirlerinizi süsleyin, depolarınızı düzenleyin ve Hayatta Kalma (Survival) modu için dengelenmiş dinamik bir inşa sistemiyle parlayan neon vitrinler oluşturun.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Giriş

Sign Builder, Architectury API kullanılarak hem Fabric hem de Forge için sıfırdan geliştirilmiş, platformlar arası bir Minecraft modudur. Dünyanızda yüksek detaylı 3D metinler oluşturabilmeniz için eksiksiz bir iş akışı sunar. Sıradan vanilya tabelalar yerine harfler, sayılar, semboller ve modüler arka plakalar için fiziksel bloklar sağlarken; bunları dinamik olarak inşa etmenizi, boyamanızı, dokulandırmanızı ve aydınlatmanızı sağlayan özel araçlar içerir.

## ✨ Temel Özellikler

*   🏢 **Otomatik 2x2 Çoklu Blok Tabelalar** — Daha büyük tabelalara mı ihtiyacınız var? Dört adet eşleşen tabela bloğunu 2x2 kare şeklinde yerleştirmek, onları sorunsuz bir şekilde 2.0 kat büyütülmüş dev bir tabelada birleştirir! Senkronize blok kırma, çoklu blok arka plakaları (4 kat malzeme harcar ve iade eder), dinamik çarpışma kutuları (hitbox), ekran dışı görüş açısı (FOV) koruması ve hem duvar hem de zemin yönlerinde ana-kukla (master-dummy) senkronizasyonu içerir.
*   🛡️ **Modüler Arka Plaka Sistemi** — Tabelalarınıza derinlik ve kontrast katın! Tabelaları önceden yerleştirilmiş arka plakaların üzerine monte edin veya doğrudan mevcut tekli ya da 2x2 çoklu blok tabelalara bir arka plaka takın. Ön ve arka yüzeyler birbirinden bağımsız olarak dokulandırılabilir ve boyanabilir. Boş elle Shift + Sağ Tıklamak, malzemeleri iade ederek plakayı güvenle söker.
*   🧱 **40 Dinamik Materyal ve 2 Sayfalı Arayüz Düzeni** — Tabelalar artık sadece betondan ibaret değil! Metinlerinizi; Bakır, Ametist, Kömür, Derinkayrak Tuğlası, Çamur Tuğlası, Nether Tuğlası, Kızıl ve Çarpık Tahtalar, Kızıltaş, Netherit, Kuvars, Zümrüt, ahşaplar ve maden blokları dahil 40 farklı materyalle özelleştirin. Boya Fırçası menüsündeki sayfa başına 20 blok barındıran dengeli 2 sayfalık ızgara düzeninde (`<` / `>`) kolayca gezinin.
*   🎒 **Hayatta Kalma Uyumlu ve Gerçekçi Ganimet** — Bir tabelayı kırmak, üretiminde kullanılan bileşenleri (3x Beyaz Beton, Temel Malzemeler, Işık Taşı Tozu) dinamik olarak düşürür. Arka plakaların standart düşüşleri ise harcanan betonu iade eder. **İpeksi Dokunuş (Silk Touch)** özellikli bir aletle kırmak, düşen eşya üzerindeki tüm NBT verilerini (renkler, materyaller, parlama durumları, animasyonlar) kusursuz bir şekilde koruyarak tam olarak aynı ayarlarla yeniden yerleştirilmesini sağlar!
*   🏗️ **Yenilenmiş 3D Modeller ve Uluslararası Semboller** — Piksel hassasiyetinde çarpışma kutularına (hitbox) sahip baştan tasarlanmış 3D karakter seti: Latin harfleri (A-Z), Almanca ve Türkçe özel harfler (`Ä`, `ß`, `Ç`, `Ğ`, `İ`, `Ö`, `Ş`, `Ü`), sayılar (0-9), ana yön okları, para birimi sembolleri (€, $, ₺, ¥), karşılaştırma operatörleri (`<`, `>`) ve zengin sembol kütüphanesi (`'`, `:`, `;`, `!`, `?`, `=`, `÷`, `"`, `#`, `@`, `&`, `%`, `*`, `★`, `✓`, `∞`).
*   🗜️ **Sign Press (Tabela Presi)** — Özel bir hayatta kalma üretim istasyonu. Beyaz betonlarınızı belirli harflere, sembollere ve arka plakalara temiz ve verimli bir şekilde dönüştürün. Otomatik iş akışları için hunilerle (hopper) tam uyumludur.
*   🗺️ **Holografik Taslak (Blueprint), Dikey Yerleşim ve Geri Alma (Undo)** — Metninizi kompakt Blueprint arayüzündeki doğrudan sembol butonlarıyla yazın. Blokları yerleştirmeden önce dünyada beliren ve yön fark etmeksizin milimetrik hizalanan, engellere göre **yeşil** (uygun) ile **kırmızı** (engelli) arasında geçiş yapan **3D hayalet önizlemenin** keyfini çıkarın. Yatay veya Y eksenine kilitli **dikey yerleşimi**, anında **2x2 çoklu blok boyutlandırmasını** ve **otomatik arka plaka seçeneğini** destekler. Tek tıkla çalışan Geri Alma sistemi inşaatı kaldırır ve harcanan tüm blokları eksiksiz iade eder.
*   🎨 **Boya Fırçası (Paint Brush) ve Özel Palet** — Havaya sağ tıklayarak tepkisel arayüzü açın. Kendi RGB/Hex kodlarınızı oluşturup kişisel paletinize 14 adede kadar özel renk kaydedin veya birden fazla sayfaya yayılan materyal dokularını doğrudan tabela yüzeylerine ve arka plakalara uygulayın.
*   🌈 **Akıllı Doldurma ve Gökkuşağı Modu** — Havada Eğilerek + Sağ Tıklayarak sezgisel yeşil/kırmızı dairesel göstergelere sahip "Akıllı Doldurma" modunu açıp kapatın. Bağlantılı kelimelerin tamamını hiçbir gecikme olmadan tek seferde boyayın, aydınlatın veya canlandırın.
*   💧 **Damlalık Mekaniği** — Dünyadaki boyalı herhangi bir bloğa Eğilerek + Sağ Tıklayarak tam hex rengini doğrudan Boya Fırçanıza kopyalayın.
*   🔧 **Gelişmiş Neon Mekanikleri ve Kızıltaş** — İngiliz Anahtarı (Wrench) ile herhangi bir karaktere sağ tıklayarak 11 çalışma ışık modundan (Müzik Senkronizasyonu, Disko, Göz Teması, Düşük Güç, Dalga ve Nefes Alma dahil) birini seçin. Neon tabelalarınızı doğrudan kızıltaş sinyallerine bağlayarak otomatikleştirin!
*   🌍 **Küresel Yerelleştirme** — İngilizce, Türkçe, Almanca, Rusça, İspanyolca, Basitleştirilmiş Çince, Fransızca ve İtalyanca dillerine tamamen çevrilmiştir.

## 🛠️ Teknoloji Yığını

**Modlama API'si ve Diller**
*   ☕ **Java** — Çekirdek mantık ve arka uç.
*   🧩 **Architectury API** — Eşzamanlı Forge ve Fabric geliştirmesi için platformlar arası soyutlama katmanı.
*   🦊 **Fabric** / 🔨 **Forge** — Mod yükleyicileri.

**Araçlar**
*   🧊 **Blockbench** — Tüm karakter, arka plaka ve alet blokları için özel 3D modelleme ve dokulandırma.
*   🐘 **Gradle** — Yapı otomasyonu ve bağımlılık yönetimi.

## 🚀 Başlarken

### Ön Koşullar
*   Minecraft `1.20.1`
*   **Fabric** veya **Forge** Mod Yükleyicisi
*   [Architectury API](https://modrinth.com/mod/architectury-api) (Zorunlu Bağımlılık)

### Kurulum
1.  Modun en son sürümünü **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** veya **[Modrinth](https://modrinth.com/mod/sign-builder)** üzerinden indirin.
2.  Gerekli Architectury API sürümünü (Fabric kullanıyorsanız ayrıca Fabric API'yi) indirin.
3.  İndirdiğiniz `.jar` dosyalarını Minecraft `mods` klasörünüze atın.
4.  Oyunu başlatın!

## 🤝 Katkıda Bulunma
Bu proje öncelikli olarak kişisel bir portfolyo çalışmasıdır; ancak hata bildirimleri, öneriler ve çekme istekleri (pull request) memnuniyetle karşılanır. Ufak olmayan değişiklikler için yaklaşımı tartışabilmemiz adına lütfen önce bir sorun (issue) açın.

## ⚖️ Feragatname ve Yasal Bildirim
*   Bu proje **GNU Genel Kamu Lisansı v3.0 (GPLv3)** kapsamında lisanslanmıştır. Daha fazla ayrıntı için `LICENSE` dosyasına bakın.
*   Bu proje tamamen Minecraft için hayran yapımı, açık kaynaklı bir modifikasyondur.
*   Tüm özel 3D modeller ve kod uygulamaları yazar tarafından oluşturulmuş orijinal çalışmalardır.

**Boran Mandacı** tarafından geliştirildi
