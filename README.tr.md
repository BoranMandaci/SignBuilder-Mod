<div align="center">
  <a href="README.md">🇬🇧 English</a> • <b>🇹🇷 Türkçe</b>
</div>

---

# 🪧 Sign Builder

Özel 3D tabelaları sorunsuzca inşa etmek, renklendirmek, aydınlatmak ve materyalleştirmek için tam özellikli, platformlar arası bir Minecraft modu. Hayatta Kalma (Survival) modu için mükemmel şekilde dengelenmiş son derece ayrıntılı, dinamik bir inşa sistemiyle şehirlerinizi süsleyin, etkileşimli Redstone tuş takımı kilitleri oluşturun ve parlayan neon dükkan vitrinleri ya da devasa panolar inşa edin.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1_%E2%80%94_1.21.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![NeoForge](https://img.shields.io/badge/NeoForge-Supported-EF7625?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Giriş

Sign Builder, Architectury API kullanılarak **Fabric, Forge ve NeoForge** için sıfırdan geliştirilmiş, platformlar arası bir Minecraft modudur. Basit vanilla tabelalara bağlı kalmak yerine harfler, sayılar, semboller, blok içi mini harfler ve modüler arka plakalar için fiziksel bloklar sağlarken; bunları dinamik olarak inşa etmek, boyamak, dokulandırmak, otomatikleştirmek ve aydınlatmak için özel araçlar sunar.

---

## ✨ Temel Özellikler

* 🔤 **Grid (Mini Harf) Modu — *v1.3 ile Yeni!*** — `Shift + V` tuşuyla Grid Moduna geçiş yaparak tek bir blok içine 2x2 (4 harf), 3x3 (9 harf) veya 4x4 (16 harf) şeklinde küçük harfler yerleştirin. Çift taraflı yerleşimi, hassas nişangah algılamasını ve zemin/tavan ile 45° çapraz arka plakalarda piksel hassasiyetinde hizalamayı destekler.
* 🏢 **Devasa Çoklu Bloklar & Modüler Arka Plakalar** — Dört eşleşen tabela bloğunu 2x2 veya dokuz tanesini 3x3 kare şeklinde yerleştirmek, bunları dinamik çarpışma kutularına sahip devasa **2.0x** ve **3.0x** panolarda otomatik olarak birleştirir. Tabelaları modüler arka plakaların her iki yüzüne takabilir, ön ve arka yüzeyleri bağımsız olarak renklendirebilir ve 45° çapraz açıyla yerleştirebilirsiniz.
* 🗺️ **Holografik Taslak & Hassas Damgalama** — Metninizi yazın ve blokları yerleştirmeden önce dünyada gerçek zamanlı **3D hayalet hologram** (yeşil = geçerli, kırmızı = engelli) ile önizleyin. Nişangahın baktığı kutucuktan başlayan Banner yerleşimi, arka plaka sınır taşma koruması, ardışık kelime akışı ve tek tıkla **Geri Alma (Undo)** sistemi içerir.
* 🎨 **40 Dinamik Materyal & Özel Boyama** — Metinleri koca bloklar yerine sadece külçe ve cevherler harcayarak 40 farklı hayatta kalma dengeli dokuyla (ahşap, taş, bakır, altın, elmas, zümrüt vb.) özelleştirin. Özel Hex/RGB renkleri karıştırın, akıcı Gökkuşağı animasyonunu açın veya **Smart Fill** ve **Damlalık (Eyedropper)** ile tüm kelimeyi tek tıkla biçimlendirin.
* 💡 **Animasyonlu Neon Aydınlatma & Gerçek Işık** — Tabelaları gerçek dünya ışığı (seviye 6–15) yayacak şekilde 11 hazır ayarla (Dalga, Yanıp Sönme, Nabız, Yaklaşma Sensörü, Gece Lambası) donatın ya da zaman döngüleri, gece/gündüz saatleri ve varlık filtreleriyle özel ışık davranışları kurgulayın.
* 🔴 **Etkileşimli Butonlar & PIN Tuş Takımı Kilitleri** — Herhangi bir karakteri kısa darbeli butona, şaltere veya basılı tutmalı kontrole dönüştürün. Sayı bloklarına (0–9) Redstone Karşılaştırıcı (Comparator) bağlayarak analog sinyal alın veya doğru şifre girildiğinde 2 saniyelik sinyal yayan gizli PIN kilitleri oluşturun.
* 🗜️ **Tabela Presi & Hayatta Kalma Dengesi** — Tüm karakterleri, sembolleri ve arka plakaları huni uyumlu özel bir 3D atölye masasında üretin. Tabelaları normal kırmak bloğun kendisini ve uygulanan materyalleri düşürür; **İpeksi Dokunuş (Silk Touch)** ise tüm özel renk, ışık ve PIN ayarlarını kusursuz korur.
* ⚡ **Sıfır Gecikmeli Render Önbelleği (Fast-Path)** — Gelişmiş istemci tarafı mesh önbelleklemesi FPS düşüşlerini ve anlık takılmaları ortadan kaldırarak yoğun neon panoların yanında bile son derece akıcı bir deneyim sunar.
* 🌍 **Küresel Yerelleştirme** — Türkçe, İngilizce, Almanca, Fransızca, İspanyolca, İtalyanca, Rusça, Basitleştirilmiş Çince, Geleneksel Çince, Japonca, Korece, Lehçe ve Brezilya Portekizcesi olmak üzere 13 dile eksiksiz çevrilmiştir.

---

## 🛠️ Teknoloji Yığını

**Modlama API'si & Diller**
* ☕ **Java** — Çekirdek mantık ve arka uç.
* 🧩 **Architectury API** — Eşzamanlı Forge, NeoForge ve Fabric geliştirmesi için platformlar arası soyutlama katmanı.
* 🦊 **Fabric** / 🔨 **Forge** / 🟠 **NeoForge** — Desteklenen mod yükleyicileri.

**Araçlar & Tasarım**
* 🧊 **Blockbench** — Tüm karakter, arka plaka ve atölye modelleri için özel 3D modelleme ve dokulandırma.
* 🐘 **Gradle** — Çoklu proje derleme otomasyonu ve bağımlılık yönetimi.

---

## 🚀 Başlarken

### Gereksinimler
* Minecraft `1.20.1` veya `1.21.1`
* **Fabric**, **Forge** veya **NeoForge** Mod Yükleyicisi
* [Architectury API](https://modrinth.com/mod/architectury-api) (Tüm yükleyiciler için zorunlu bağımlılık)
* [Fabric API](https://modrinth.com/mod/fabric-api) (Yalnızca Fabric için gereklidir)

### Kurulum
1. Modun en son sürümünü **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** veya **[Modrinth](https://modrinth.com/mod/sign-builder)** üzerinden indirin.
2. Architectury API'nin gerekli sürümünü indirin (Fabric kullanıyorsanız Fabric API'yi de indirin).
3. `.jar` dosyalarını Minecraft `mods` klasörünüze bırakın.
4. Oyunu başlatın!

---

## 🤝 Katkıda Bulunma
Bu proje öncelikli olarak kişisel bir portföy projesidir; ancak sorun bildirimleri, öneriler ve pull request'ler memnuniyetle karşılanır. Yaklaşımı tartışabilmemiz adına önemsiz olmayan her şey için lütfen önce bir konu (issue) açın.

## ⚖️ Sorumluluk Reddi & Yasal Bildirim
* Bu proje **GNU General Public License v3.0 (GPLv3)** kapsamında lisanslanmıştır. Daha fazla ayrıntı için `LICENSE` dosyasına bakın.
* Bu proje tamamen Minecraft için hayran yapımı, açık kaynaklı bir modifikasyondur.
* Tüm özel 3D modeller ve kod uygulamaları yazar tarafından oluşturulmuş orijinal eserlerdir.

**Boran Mandacı** tarafından geliştirildi