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

*   🏢 **Automatic 2x2 Multi-Block Signs** — Need bigger signage? Placing four matching sign blocks in a 2x2 square seamlessly merges them into a giant 2.0x scaled sign! Features synchronized block breaking, multi-block backplates (consuming and refunding 4x materials), dynamic hitboxes, off-screen FOV protection, and master-dummy syncing across wall, floor, and ceiling orientations.
*   🛡️ **Modular Backplate System** — Give your signage depth and contrast! Mount signs onto pre-placed backplates or snap a backplate directly onto existing single or 2x2 multi-block signs. Front and back faces can be textured and dyed independently. Shift + Right-Click with an empty hand safely detaches the plate while refunding materials.
*   🧱 **40 Dynamic Materials & 2-Page GUI Layout** — Signs are no longer just concrete! Customize your text with 40 distinct materials—including Copper, Amethyst, Coal, Deepslate Bricks, Mud Bricks, Nether Bricks, Crimson & Warped Planks, Redstone, Netherite, Quartz, Emerald, woods, and minerals. Browse them effortlessly in the Paint Brush screen with a balanced 20-item-per-page grid (`<` / `>`).
*   🎒 **Survival Ready & Realistic Loot** — Breaking a sign dynamically drops its crafted components (3x White Concrete, Base Materials, Glowstone Dust). Standard drops for backplates refund base concrete. Mining with a **Silk Touch** tool flawlessly retains all NBT data (colors, materials, glowing states, animations) on the dropped item for exact redeployment!
*   🏗️ **Overhauled 3D Models & International Symbols** — Redesigned 3D character sets with pixel-perfect hitboxes: Latin letters (A-Z), German & Turkish special letters (`Ä`, `ß`, `Ç`, `Ğ`, `İ`, `Ö`, `Ş`, `Ü`), numbers (0-9), cardinal arrows, currency symbols (€, $, ₺, ¥), comparison operators (`<`, `>`), and diverse symbols (`'`, `:`, `;`, `!`, `?`, `=`, `÷`, `"`, `#`, `@`, `&`, `%`, `*`, `★`, `✓`, `∞`).
*   🗜️ **The Sign Press** — A dedicated survival crafting station. Stamp your white concrete into specific letters, symbols, and backplates cleanly and efficiently. Fully compatible with hoppers for automated workflows.
*   🗺️ **Holographic Blueprint, Vertical Placement & Undo** — Type your text into the compact Blueprint GUI with direct symbol buttons. Enjoy real-time **translucent 3D ghost previews** directly in the world, dynamically shifting between **green** (valid) and **red** (obstructed) with flawless multi-directional alignment. Supports horizontal or Y-axis locked **vertical placement**, instant **2x2 multi-block sizing**, and an **automatic backplate toggle**. The one-click Undo system removes constructions and refunds all blocks and backplates.
*   🎨 **The Paint Brush & Custom Palette** — Right-click in the air to open a responsive GUI. Mix your own RGB/Hex codes and save up to 14 custom colors in your personal palette, or apply material textures directly to sign faces and backplates across multiple pages.
*   🌈 **Smart Fill & Rainbow Mode** — Sneak + Right-click in the air to toggle "Smart Fill", featuring intuitive green/red circular HUD indicators. Instantly paint, light, or animate entire connected words at once with zero visual delay.
*   💧 **Eyedropper Mechanic** — Sneak + Right-click on any painted block in the world to copy its exact hex color directly to your Paint Brush.
*   🔧 **Advanced Neon Mechanics & Redstone** — Right-click any character with the Wrench to select from 11 operational light modes (including Audio Sync, Disco, Eye Contact, Low Power, Wave, and Breathing). Automate your neon signs by connecting them directly to redstone signals!
*   🌍 **Global Localization** — Fully translated into English, Turkish, German, Russian, Spanish, Simplified Chinese, French, and Italian.

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
