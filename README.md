<div align="center">
  <b>🇬🇧 English</b> • <a href="README.tr.md">🇹🇷 Türkçe</a>
</div>

---

# 🪧 Sign Builder

A full-featured, cross-platform Minecraft mod to seamlessly build, color, illuminate, and materialize custom 3D signs. Decorate your cities, organize your storage, and build glowing neon shop fronts with a highly detailed, dynamic building system perfectly balanced for Survival mode.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Introduction

Sign Builder is a cross-platform Minecraft mod built from the ground up for both Fabric and Forge using the Architectury API. It introduces a comprehensive workflow for creating highly detailed 3D text in your world. Rather than relying on simple vanilla signs, this mod provides physical blocks for letters, numbers, symbols, and modular backplates, coupled with custom tools to construct, paint, texture, and illuminate them dynamically.

## ✨ Key Features

*   🛡️ **Modular Backplate System (NEW!)** — Give your signage depth and contrast! Mount signs onto pre-placed backplates or snap a backplate directly onto existing signs. Front and back faces can be textured and dyed independently. Shift + Right-Click with a backplate item detaches the plate cleanly while preserving all custom NBT data.
*   🧱 **Dynamic Material System** — Signs are no longer just concrete! Integrate a broad selection of base materials (all vanilla woods, Iron Block, Polished Andesite, Gold Block, Diamond Block, Lapis Lazuli Block, Smooth Stone, Polished Diorite, Bricks, and Stone Bricks) with zero-latency visual updates.
*   🎒 **Survival Ready & Realistic Loot** — Breaking a sign dynamically drops its crafted components (3x White Concrete, 1x Base Material, 1x Glowstone Dust). Standard drops for backplates refund base concrete. Mining with a **Silk Touch** tool flawlessly retains all NBT data (colors, materials, glowing states, animations) on the dropped item for exact redeployment!
*   🏗️ **3D Letters, Numbers & Expanded Symbols** — Highly detailed, custom-modeled letters (A-Z), numbers (0-9), and an extensive set of symbols (including arrows, math operators, and punctuation like `'`, `:`, `;`, `!`, `?`, `=`, `÷`, `"`). All blocks feature pixel-accurate hitboxes that adapt naturally to wall and floor placements.
*   🗜️ **The Sign Press** — A dedicated survival crafting station. Stamp your white concrete into specific letters, symbols, and backplates cleanly and efficiently. Fully compatible with hoppers for automated workflows.
*   🗺️ **The Sign Blueprint & Undo** — Type your desired word into the Blueprint GUI. Right-click a block to automatically construct the entire word in the world. Made a mistake? Use the red Undo button to safely remove your last placement and refund your materials.
*   🎨 **The Paint Brush & Custom Palette** — Right-click in the air to open a responsive GUI. Mix your own RGB/Hex codes and save up to 14 custom colors in your personal palette, or apply material textures directly to sign faces and backplates.
*   🌈 **Smart Fill & Rainbow Mode** — Sneak + Right-click in the air to toggle "Smart Fill", featuring intuitive green/red circular HUD indicators. Instantly paint or animate entire connected words at once with optimistic client-side rendering for zero visual delay.
*   💧 **Eyedropper Mechanic** — Sneak + Right-click on any painted block in the world to copy its exact hex color directly to your Paint Brush.
*   🔧 **Advanced Neon Mechanics & Redstone** — Right-click any character with the Wrench to select from 11 operational light modes (including Audio Sync, Disco, Eye Contact, Low Power, Wave, and Breathing). Automate your neon signs by connecting them directly to redstone signals!
*   🌍 **Global Localization** — Fully translated into English, Turkish, Russian, Spanish, Simplified Chinese, German, French, and Italian.

## 🛠️ Tech Stack

**Modding API & Languages**
*   ☕ **Java** — Core logic and backend.
*   🧩 **Architectury API** — Cross-platform abstraction layer for simultaneous Forge and Fabric development.
*   🦊 **Fabric** / 🔨 **Forge** — Mod loaders.

**Tools**
*   🧊 **Blockbench** — Custom 3D modeling and texturing for all character, backplate, and tool blocks.
*   🐘 **Gradle** — Build automation and dependency management.

## 🚀 Getting Started

### Prerequisites
*   Minecraft `1.20.1`
*   **Fabric** or **Forge** Mod Loader
*   [Architectury API](https://modrinth.com/mod/architectury-api) (Required Dependency)

### Installation
1.  Download the latest version of the mod from **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** or **[Modrinth](https://modrinth.com/mod/sign-builder)**.
2.  Download the required version of the Architectury API (and Fabric API if using Fabric).
3.  Drop the `.jar` files into your Minecraft `mods` folder.
4.  Launch the game!

## 🤝 Contributing
This is primarily a personal portfolio project, but issues, suggestions, and pull requests are welcome. Please open an issue first for anything non-trivial so we can discuss the approach.

## ⚖️ Disclaimer & Legal Notice
*   This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the `LICENSE` file for more details.
*   This project is strictly a fan-made, open-source modification for Minecraft.
*   All custom 3D models and code implementations are original works created by the author.

Built by **Boran Mandacı**
