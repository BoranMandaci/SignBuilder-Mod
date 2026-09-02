<div align="center">
  <a href="README.md">🇬🇧 English</a> • <b>🇹🇷 Türkçe</b>
</div>

---

# 🪧 Sign Builder

Özel 3D tabelaları sorunsuzca inşa etmenizi, renklendirmenizi, ışıklandırmanızı ve materyallerini değiştirmenizi sağlayan, tam donanımlı, platformlar arası bir Minecraft modu. Hayatta Kalma (Survival) modu için mükemmel bir şekilde dengelenmiş son derece detaylı ve dinamik bir inşa sistemiyle şehirlerinizi dekore edin, depolarınızı düzenleyin ve parlayan neon dükkan tabelaları oluşturun.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Giriş

Sign Builder, Architectury API kullanılarak hem Fabric hem de Forge için sıfırdan geliştirilmiş platformlar arası bir Minecraft modudur. Dünyanızda son derece detaylı 3D metinler oluşturmak için kapsamlı bir iş akışı sunar. Basit varsayılan (vanilla) tabelalara güvenmek yerine, bu mod harfler, sayılar ve semboller için fiziksel bloklar sunar; üstüne bunları dinamik olarak inşa etmek, boyamak, dokulandırmak ve ışıklandırmak için özel araçlarla birlikte gelir.

## ✨ Temel Özellikler

*   🧱 **Dinamik Materyal Sistemi (YENİ!)** — Tabelalar artık sadece betondan ibaret değil! Farklı temel materyalleri (tüm ahşap türleri, Demir Bloğu ve Cilalı Andezit desteklenir) yerleştirildiği an sıfır gecikmeli görsel güncellemelerle tabelalarınıza entegre edin.
*   🎒 **Hayatta Kalma Uyumlu ve Gerçekçi Ganimetler (YENİ!)** — Bir tabelayı kırmak artık dinamik olarak üretildiği bileşenleri (3x Beyaz Beton, 1x Temel Materyal, 1x Işıktaşı Tozu) düşürür. **İpeksi Dokunuş (Silk Touch)** aletiyle kırmak, düşen eşya üzerindeki tüm NBT verilerini (renk, materyal, parlama durumu, animasyonlar) kusursuz bir şekilde koruyarak birebir aynı şekilde tekrar yerleştirmenizi sağlar!
*   🏗️ **3D Harfler, Sayılar ve Semboller** — Son derece detaylı, özel modellenmiş harfler (A-Z), sayılar (0-9) ve geniş bir sembol seti. Tüm bloklar, duvar ve zemin yerleşimlerine mükemmel uyum sağlayan gelişmiş hitbox'lara (çarpışma kutularına) sahiptir.
*   🗜️ **Tabela Presi (Sign Press)** — Hayatta kalma modu için özel bir üretim istasyonu. Beyaz betonunuzu temiz ve verimli bir şekilde belirli harflere ve sembollere dönüştürün. Otomasyon için hunilerle (hopper) tam uyumludur.
*   🗺️ **Tabela Planı (Blueprint) ve Geri Alma** — İstediğiniz kelimeyi Blueprint arayüzüne yazın. Bir bloğa sağ tıkladığınızda tüm kelime otomatik olarak dünyaya inşa edilir. Bir hata mı yaptınız? Son yerleştirdiğiniz bloğu güvenle kaldırmak ve malzemelerinizi geri almak için kırmızı Geri Al (Undo) butonunu kullanın.
*   🎨 **Boya Fırçası (Paint Brush) ve Özel Palet** — Havaya sağ tıklayarak hızlı yanıt veren bir arayüz açın. Kendi RGB/Hex kodlarınızı karıştırın ve kişisel paletinize 14 adede kadar özel renk kaydedin.
*   🌈 **Akıllı Doldurma (Smart Fill) ve Gökkuşağı Modu** — Sezgisel yeşil/kırmızı dairesel HUD göstergelerine sahip "Akıllı Doldurma" modunu açmak için Eğilme (Shift) + Havaya Sağ Tık yapın. İyimser istemci tarafı işleme (optimistic client-side rendering) sayesinde sıfır görsel gecikme ile birbirine bağlı tüm kelimeleri tek tıkla anında boyayın veya hareketlendirin.
*   💧 **Damlalık (Eyedropper) Mekaniği** — Dünyadaki boyanmış herhangi bir bloğun tam hex rengini doğrudan Boya Fırçanıza kopyalamak için Eğilme + Sağ Tık yapın.
*   🔧 **Neon Mekanikleri ve Kızıltaş (Redstone)** — Herhangi bir karaktere İngiliz Anahtarı (Wrench) ile sağ tıklayarak 7 farklı ışık modundan (Blink, Wave, Breathing vb.) birini uygulayın. Neon tabelalarınızı doğrudan kızıltaş sinyallerine bağlayarak otomatikleştirin!
*   🌍 **Küresel Yerelleştirme** — İngilizce, Türkçe, Rusça, İspanyolca, Basitleştirilmiş Çince, Almanca, Fransızca ve İtalyanca dillerine tamamen çevrilmiştir.

## 🛠️ Teknoloji Yığını

**Modlama API'si ve Diller**
*   ☕ **Java** — Temel mantık ve arka plan.
*   🧩 **Architectury API** — Eşzamanlı Forge ve Fabric geliştirme için platformlar arası soyutlama katmanı.
*   🦊 **Fabric** / 🔨 **Forge** — Mod yükleyicileri.

**Araçlar**
*   🧊 **Blockbench** — Tüm karakter ve araç blokları için özel 3D modelleme ve dokulandırma.
*   🐘 **Gradle** — Derleme otomasyonu ve bağımlılık yönetimi.

## 🚀 Başlangıç

### Ön Koşullar
*   Minecraft `1.20.1`
*   **Fabric** veya **Forge** Mod Yükleyicisi
*   [Architectury API](https://modrinth.com/mod/architectury-api) (Zorunlu Bağımlılık)

### Kurulum
1.  Modun en son sürümünü **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** veya **[Modrinth](https://modrinth.com/mod/sign-builder)** üzerinden indirin.
2.  Architectury API'nin gerekli sürümünü indirin (Fabric kullanıyorsanız Fabric API'yi de ekleyin).
3.  İndirdiğiniz `.jar` dosyalarını Minecraft `mods` klasörünüze atın.
4.  Oyunu başlatın!

## 🤝 Katkıda Bulunma
Bu öncelikle kişisel bir portföy projesidir, ancak sorun bildirimleri (issues), öneriler ve pull request'ler her zaman kabul edilir. Karmaşık değişiklikler için yaklaşımı tartışabilmemiz adına lütfen önce bir issue açın.

## ⚖️ Sorumluluk Reddi ve Yasal Uyarı
*   Bu proje **GNU General Public License v3.0 (GPLv3)** altında lisanslanmıştır. Daha fazla detay için `LICENSE` dosyasına bakın.
*   Bu proje, Minecraft için tamamen hayran yapımı, açık kaynaklı bir modifikasyondur.
*   Tüm özel 3D modeller ve kod uygulamaları, yazar tarafından oluşturulan orijinal çalışmalardır.

**Boran Mandacı** tarafından geliştirilmiştir.
