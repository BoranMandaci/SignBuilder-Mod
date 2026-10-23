<div align="center">
  <b>🇬🇧 English</b> • <a href="README.tr.md">🇹🇷 Türkçe</a>
</div>

---

# 🪧 Sign Builder

A full-featured, cross-platform Minecraft mod to seamlessly build, color, illuminate, and materialize custom 3D signs and futuristic LCD screens. Decorate your cities, build interactive Redstone keypad locks, and construct glowing neon shop fronts, towering billboards, or animated displays with a highly detailed, dynamic building system perfectly balanced for Survival mode.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1_%E2%80%94_1.21.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![NeoForge](https://img.shields.io/badge/NeoForge-Supported-EF7625?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Introduction

Sign Builder is a cross-platform Minecraft mod built from the ground up for **Fabric, Forge, and NeoForge** using the Architectury API. Rather than relying on simple vanilla signs, this mod introduces physical blocks for letters, numbers, symbols, sub-block mini letters, modular backplates, and multi-block LCD screens—accompanied by custom tools to construct, paint, texture, automate, and illuminate them dynamically.

---

## ✨ Key Features

* 🖥️ **Multi-Block LCD Screens — *Updated in v1.4.1!*** — Seamless connected displays with dynamic borders that automatically merge into single giant display surfaces. Features 5 digital fonts (Modern LCD, 7-Segment, Dot Matrix, Retro Arcade, Vanilla), real-time dynamic variables (`{player}`, `{time}`, `{fps}`, `{coords}`, `{biome}`, etc.), 15 color themes, and **60 standalone procedural animations** organized into 5 thematic categories (Storefronts, Sports & Arenas, Retro Arcade Games, Cyber/Gauges, Science & Nature).
* 🔤 **Grid Mode (Mini Letters & Multi-Grid) — *Expanded in v1.4!*** — Toggle Grid Mode with `Shift + V` to fit 2x2, 3x3, 4x4, or 5x5 sub-block mini letters inside a single block. Includes sub-cell collision allowing different letter sizes to coexist, `Shift + Right-Click` letter removal, and dual-sided placement across walls, floors, ceilings, and 45° diagonal backplates.
* 🌟 **Glow Ink Illumination** — Apply Glow Ink Sacs to any letter, backplate, or grid sign to make its surface glow full-bright in the dark without altering world light levels. Revert anytime with a regular Ink Sac.
* 🏢 **Giant Multi-Blocks & Modular Backplates** — Placing matching sign blocks in a 2x2 or 3x3 square automatically merges them into massive **2.0x** and **3.0x** multi-block billboards with synchronized hitboxes. Mount signs on both sides of modular backplates, attach 1x1 hybrid glyphs alongside grid letters, texture front/back independently, or rotate signs at 45° diagonal angles.
* 🗺️ **Holographic Blueprint & Precision Stamping** — Type your text and preview it directly in the world via a real-time **3D ghost hologram** (green = valid, red = obstructed). Features crosshair-targeted Banner placement, backplate boundary overflow protection, continuous word flows, and an instant **Undo** system.
* 🎨 **40 Dynamic Materials & Custom Dyeing** — Customize text with 40 distinct survival-balanced textures (wood, stone, copper, iron, gold, diamond, emerald, and more) using ingots and gems instead of full blocks. Mix custom Hex/RGB colors, activate dynamic cycling Rainbow mode, or use **Smart Fill** and the **Eyedropper** to style entire words in one click.
* 💡 **Animated Neon Lighting & Light Emission** — Configure signs with 11 lighting presets (Blink, Flicker, Pulse, Wave, Motion Sensor, Night Only) emitting real world light (levels 6–15), or build custom light behaviors with timed cycles, day/night schedules, and proximity filters.
* 🔴 **Interactive Buttons & PIN Keypad Locks** — Turn any character into a momentary Pulse button, Toggle lever, or continuous Hold-to-Activate control. Connect Redstone Comparators to number blocks (0–9) for analog readouts, or create secret PIN passcode locks that emit a 2-second Redstone pulse upon entering the correct sequence.
* 🗜️ **The Sign Press & Survival Balance** — Manufacture all characters, symbols, and backplates in a dedicated, hopper-compatible 3D workstation. Breaking signs normally drops the block itself and any applied materials, while **Silk Touch** perfectly preserves all custom colors, light modes, and PIN configurations.
* ⚡ **Zero-Lag Render Fast-Path — *Optimized in v1.4.1!*** — Advanced tick-based caching for LCD text, pre-allocated static animation memory, and restored camera frustum culling ensure silky-smooth performance even around dense neon signage and LCD matrices.
* 🌍 **Global Localization** — Fully translated into 13 languages: English, Turkish, German, French, Spanish, Italian, Russian, Simplified Chinese, Traditional Chinese, Japanese, Korean, Polish, and Brazilian Portuguese.

---

## 🛠️ Tech Stack

**Modding API & Languages**
* ☕ **Java** — Core logic and backend.
* 🧩 **Architectury API** — Cross-platform abstraction layer for simultaneous Forge, NeoForge, and Fabric development.
* 🦊 **Fabric** / 🔨 **Forge** / 🟠 **NeoForge** — Supported mod loaders.

**Tools & Design**
* 🧊 **Blockbench** — Custom 3D modeling and texturing for all character, backplate, and workstation models.
* 🐘 **Gradle** — Multi-project build automation and dependency management.

---

## 🚀 Getting Started

### Prerequisites
* Minecraft `1.20.1` or `1.21.1`
* **Fabric**, **Forge**, or **NeoForge** Mod Loader
* [Architectury API](https://modrinth.com/mod/architectury-api) (Required Dependency on all loaders)
* [Fabric API](https://modrinth.com/mod/fabric-api) (Required on Fabric only)

### Installation
1. Download the latest version of the mod from **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sign-builder)** or **[Modrinth](https://modrinth.com/mod/sign-builder)**.
2. Download the required version of Architectury API (and Fabric API if using Fabric).
3. Drop the `.jar` files into your Minecraft `mods` folder.
4. Launch the game!

---

## 🤝 Contributing
This is primarily a personal portfolio project, but issues, suggestions, and pull requests are welcome. Please open an issue first for anything non-trivial so we can discuss the approach.

## ⚖️ Disclaimer & Legal Notice
* This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See the `LICENSE` file for more details.
* This project is strictly a fan-made, open-source modification for Minecraft.
* All custom 3D models and code implementations are original works created by the author.

Built by **Boran Mandacı**
