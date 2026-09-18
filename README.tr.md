<div align="center">
  <a href="README.md">🇬🇧 English</a> • <b>🇹🇷 Türkçe</b>
</div>

---

# 🪧 Sign Builder

Özel 3D tabelaları zahmetsizce inşa etmek, renklendirmek, ışıklandırmak ve dokulandırmak için geliştirilmiş, tam donanımlı ve platformlar arası bir Minecraft modu. Hayatta Kalma (Survival) moduyla mükemmel dengelenmiş, yüksek detaylı ve dinamik bir inşa sistemiyle şehirlerinizi süsleyin, depolarınızı düzenleyin ve parlayan neon tabelalı dükkan cepheleri tasarlayın.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Giriş

Sign Builder, Architectury API kullanılarak hem Fabric hem de Forge için sıfırdan geliştirilmiş, platformlar arası bir Minecraft modudur. Dünyanızda yüksek detaylı 3D yazılar oluşturmanız için kapsamlı bir iş akışı sunar. Sıradan vanilla tabelalar yerine harfler, sayılar, semboller ve modüler arka plakalar (backplate) için fiziksel bloklar sağlar; bunları dinamik olarak inşa etmek, boyamak, dokulandırmak ve aydınlatmak için özel araçlarla birlikte gelir.

## ✨ Temel Özellikler

*   🏢 **Otomatik 2x2 Çoklu Blok Tabelalar (YENİ!)** — Daha büyük tabelalara mı ihtiyacınız var? Aynı türdeki dört tabela bloğunu 2x2 düzeninde yerleştirdiğinizde, otomatik olarak 2.0x ölçekli devasa bir tabelaya dönüşür! Hem duvar hem de zemin yerleşimlerinde senkronize blok kırma, 4 katı malzeme harcayan/iade eden çoklu arka plakalar, dinamik çarpışma kutuları (hitbox) ve ana-kukla (master-dummy) veri senkronizasyonu sunar.
*   🛡️ **Modüler Arka Plaka (Backplate) Sistemi** — Tabelalarınıza derinlik ve kontrast kazandırın! Tabelaları önceden yerleştirilmiş arka plakalara monte edin veya tekli ya da 2x2 çoklu blok tabelaların üzerine doğrudan bir arka plaka oturtun. Ön ve arka yüzeyler bağımsız olarak dokulandırılabilir ve boyanabilir. Boş elle Shift + Sağ Tık yaparak plakayı güvenle sökebilir ve malzemelerinizi geri alabilirsiniz.
*   🧱 **28 Dinamik Materyal ve Arayüz Sayfalama** — Tabelalar artık sadece betondan ibaret değil! Yazılarınızı Kızıltaş Bloğu, Netherite Bloğu, Kuvars Bloğu, Zümrüt Bloğu, Purpur Bloğu, Cilalı Granit, Taş, Düzgün Kumtaşları, ahşaplar ve değerli madenler dahil olmak üzere 28 farklı materyalle özelleştirin. Boya Fırçası (Paint Brush) ekranındaki sayfa geçiş (`<` / `>`) butonlarıyla tüm materyallere kolayca göz atın.
*   🎒 **Hayatta Kalmaya Hazır ve Gerçekçi Ganimetler** — Bir tabelayı kırmak, yapımında kullanılan bileşenleri dinamik olarak düşürür (3x Beyaz Beton, Temel Materyaller, Işık Taşı Tozu). Arka plakaların standart düşüşleri temel betonu iade eder. **İpeksi Dokunuş (Silk Touch)** büyülü bir aletle kazıldığında, eşya üzerindeki tüm NBT verileri (renkler, materyaller, ışık durumları, animasyonlar) kusursuz şekilde korunur!
*   🏗️ **3D Harfler, Sayılar ve Genişletilmiş Semboller** — Özel olarak modellenmiş detaylı harfler (A-Z), sayılar (0-9), para birimi simgeleri (€, $, ₺, ¥), oklar, matematiksel operatörler ve noktalama işaretleri (`'`, `:`, `;`, `!`, `?`, `=`, `÷`, `"`, `#`, `@`, `&`, `%`, `*`). Tüm bloklar, duvar ve zemin yerleşimlerine doğal olarak uyum sağlayan piksel hassasiyetinde çarpışma kutularına sahiptir.
*   🗜️ **Tabela Presi (The Sign Press)** — Hayatta Kalma moduna özel bir zanaat istasyonu. Beyaz betonlarınızı harflere, sembollere ve arka plakalara temiz ve verimli bir şekilde presleyin. Otomatik üretim hatları için hunilerle (hopper) tam uyumludur.
*   🗺️ **Tabela Taslağı (The Sign Blueprint) ve Geri Alma (Undo)** — İstediğiniz kelimeyi Blueprint arayüzüne yazın. Tüm kelimeyi dünyaya otomatik olarak dizmek için bir bloğa sağ tıklayın. Hata mı yaptınız? Kırmızı Geri Al (Undo) butonunu kullanarak son yerleşiminizi güvenle kaldırabilir ve malzemelerinizi geri alabilirsiniz.
*   🎨 **Boya Fırçası ve Özel Palet** — Havaya sağ tıklayarak arayüzü açın. Kendi RGB/Hex kodlarınızı oluşturup kişisel paletinize 14 adede kadar özel renk kaydedin veya tabela yüzeylerine ve arka plakalara çok sayfalı menüden doğrudan materyal dokuları uygulayın.
*   🌈 **Akıllı Doldurma (Smart Fill) ve Gökkuşağı Modu** — Havaya Sneak + Sağ Tık yaparak sezgisel yeşil/kırmızı dairesel HUD göstergelerine sahip "Akıllı Doldurma" özelliğini açıp kapatın. Sıfır görsel gecikmeyle birbirine bağlı kelimelerin tamamını aynı anda boyayın, aydınlatın veya canlandırın.
*   💧 **Damlalık (Eyedropper) Mekaniği** — Dünyadaki boyalı herhangi bir bloğa Sneak + Sağ Tık yaparak bloğun birebir hex rengini doğrudan Boya Fırçanıza kopyalayın.
*   🔧 **Gelişmiş Neon Mekanikleri ve Kızıltaş** — Herhangi bir karaktere İngiliz Anahtarı (Wrench) ile sağ tıklayarak 11 farklı ışık modundan birini seçin (Ses Senkronizasyonu, Disko, Göz Teması, Düşük Güç, Dalga ve Nefes Alma dahil). Neon tabelalarınızı doğrudan kızıltaş sinyallerine bağlayarak otomatikleştirin!
*   🌍 **Küresel Yerelleştirme** — İngilizce, Türkçe, Rusça, İspanyolca, Basitleştirilmiş Çince, Almanca, Fransızca ve İtalyanca dillerine tamamen çevrilmiştir.

## 🛠️ Teknik Altyapı

**Modlama API'si ve Diller**
*   ☕ **Java** — Çekirdek mantık ve arka uç.
*   🧩 **Architectury API** — Forge ve Fabric platformlarında eşzamanlı geliştirme için soyutlama katmanı.
*   🦊 **Fabric** / 🔨 **Forge** — Mod yükleyicileri.

**Araçlar**
*   🧊 **Blockbench** — Tüm karakter, arka plaka ve alet blokları için özel 3D modelleme ve dokulandırma.
*   🐘 **Gradle** — Derleme otomasyonu ve bağımlılık yönetimi.

## 🚀 Başlarken

### Gereksinimler
*   Minecraft `1.20.1`
*   **Fabric** veya **Forge** Mod Yükleyici
*   [Architectury API](https://modrinth.com/mod/architectury-api) (Zorunlu Bağımlılık)

### Kurulum
1.  Modun en son sürümünü **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** veya **[Modrinth](https://modrinth.com/mod/sign-builder)** üzerinden indirin.
2.  Gerekli Architectury API sürümünü indirin (Fabric kullanıyorsanız Fabric API'yi de edinin).
3.  İndirdiğiniz `.jar` dosyalarını Minecraft klasörünüzdeki `mods` dizinine atın.
4.  Oyunu başlatın!

## 🤝 Katkıda Bulunma
Bu proje temel olarak kişisel bir portföy çalışmasıdır; ancak hata bildirimleri, öneriler ve pull request'ler her zaman memnuniyetle karşılanır. Büyük çaplı değişiklikler için lütfen önce bir issue açarak planlanan yaklaşımı tartışın.

## ⚖️ Sorumluluk Reddi ve Yasal Bildirim
*   Bu proje **GNU General Public License v3.0 (GPLv3)** lisansı altındadır. Detaylar için `LICENSE` dosyasına göz atabilirsiniz.
*   Bu proje tamamen hayran yapımı, açık kaynaklı bir Minecraft modifikasyonudur.
*   Tüm özel 3D modeller ve kod uygulamaları yazar tarafından üretilmiş özgün çalışmalardır.

**Boran Mandacı** tarafından geliştirilmiştir
