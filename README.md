<div align="center">
  <b>🇬🇧 English</b> • <a href="README.tr.md">🇹🇷 Türkçe</a>
</div>

---

# 🪧 Sign Builder

A full-featured, cross-platform Minecraft mod to seamlessly build, color, illuminate, and materialize custom 3D signs. Decorate your cities, build interactive redstone keypad locks, and construct glowing neon shop fronts or towering billboards with a highly detailed, dynamic building system perfectly balanced for Survival mode.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-238749?style=flat-square&logo=minecraft) ![Fabric](https://img.shields.io/badge/Fabric-Supported-D1C4AC?style=flat-square) ![Forge](https://img.shields.io/badge/Forge-Supported-DF9D6B?style=flat-square) ![Architectury](https://img.shields.io/badge/Architectury-API-1572B6?style=flat-square) ![License](https://img.shields.io/badge/license-GPLv3-blue?style=flat-square)

## 📖 Introduction

Sign Builder is a cross-platform Minecraft mod built from the ground up for both Fabric and Forge using the Architectury API. It introduces a comprehensive workflow for creating highly detailed 3D text in your world. Rather than relying on simple vanilla signs, this mod provides physical blocks for letters, numbers, symbols, and modular backplates, coupled with custom tools to construct, paint, texture, automate, and illuminate them dynamically.

## ✨ Key Features

*   🏢 **Giant 2x2 & 3x3 Multi-Block Signs** — Need massive signage? Placing four matching sign blocks in a 2x2 square creates a 2.0x display, while a 3x3 square automatically forms a towering **3.0x giant blocks**! Features synchronized multi-block destruction, scaled material drops (4x / 9x), dynamic hitboxes, off-screen FOV rendering protection, master-dummy data sync, and multi-block backplates across wall, floor, and ceiling orientations.
*   🔴 **Interactive Buttons & Redstone Automation** — Turn any character into an interactive mechanism! Using the Wrench, configure signs as **Momentary Pulse Buttons** or **Toggle Switches (Levers)**:
    *   **Directional Output:** Emits full Redstone power (signal strength 15) strictly from the back attachment face.
    *   **Material-Aware Timing:** Wooden signs stay depressed for 1.5 seconds (30 ticks), while metal/stone materials reset after 1.0 second (20 ticks), complete with unique physical audio profiles.
    *   **Word-Wide Synchronization:** Configure letters to press independently or trigger the entire connected word simultaneously via flood-fill logic.
    *   **Ranged Triggering:** Shoot wall buttons with arrows, tridents, or snowballs to activate them from a distance!
    *   **Analog Comparator Support:** Number blocks (0–9) output their literal numerical value (signal strength 0–9) directly into Redstone Comparators.
*   🔐 **PIN Keypad Security System** — Turn text into secret passcode locks!
    *   **Sequence Locking:** Characters latch inward as you press them. Entering the correct sequence emits a 2-second redstone pulse across all entered blocks; entering an invalid sequence immediately resets the lock with audio feedback.
    *   **Touch-to-Record ("REC"):** Effortlessly configure PINs directly in the world! Tap the "REC" button in the Wrench GUI, click the blocks in order, and Shift + Right-Click to save.
*   🛡️ **Double-Sided Modular Backplates** — Give your signage depth and contrast or create free-standing signs! Mount signs onto pre-placed backplates or snap a backplate directly onto existing single, 2x2, or 3x3 signs. **Signs can be placed on both the front and back faces of a backplate.** Front and back surfaces can be textured and dyed independently. Shift + Right-Click with an empty hand safely detaches the plate while refunding materials.
*   🧱 **40 Dynamic Materials & 2-Page GUI Layout** — Signs are no longer just concrete! Customize your text with 40 distinct materials—including Copper, Amethyst, Coal, Deepslate Bricks, Mud Bricks, Nether Bricks, Crimson & Warped Planks, Redstone, Netherite, Quartz, Emerald, woods, and minerals. Browse them effortlessly in the Paint Brush screen with a balanced 20-item-per-page grid (`<` / `>`).
*   🎒 **Survival Ready & Realistic Loot** — Breaking a sign normally drops its crafted components (3x White Concrete, Base Materials, Glowstone Dust). Drops dynamically scale with multiblock size (4x for 2x2s, 9x for 3x3 billboards). Mining with a **Silk Touch** tool flawlessly retains all custom NBT data (colors, materials, glowing states, PIN codes, animations) directly on the dropped item!
*   🏗️ **Expansive 3D Models & International Symbols** — Redesigned 3D character sets with pixel-perfect hitboxes: Latin letters (A-Z), German & Turkish special letters (`Ä`, `ß`, `Ç`, `Ğ`, `İ`, `Ö`, `Ş`, `Ü`), numbers, cardinal and diagonal arrows, currency symbols (€, $, ₺, ¥, £, **₿**), mathematical operators, and an extensive symbol roster: Tilde (`~`), Key (`🗝`), Lock (`🔒`), Trophy (`🏆`), Lightning (`⚡`), Bitcoin (`₿`), Cross (`✗`), Circle (`○`), Diamond (`◆`), Musical Notes (`♪`, `♫`), Skull (`☠`), Heart (`♥`), Star (`★`), Checkmark (`✓`), Infinity (`∞`), brackets, and punctuation.
*   🗜️ **The Sign Press** — A dedicated survival crafting station. Stamp your white concrete into specific letters, symbols, and backplates cleanly and efficiently with a refreshed, scrollable interface. Fully compatible with hoppers for automated workflows.
*   🗺️ **Holographic Blueprint, Diagonal Angles & Undo** — Type your text into the modernized Blueprint GUI with direct symbol buttons, full GUI scale support, and Unicode handling. Enjoy real-time **translucent 3D ghost previews** directly in the world, dynamically shifting between **green** (valid) and **red** (obstructed). Features horizontal, 45-degree diagonal (1x1), and Y-axis locked **vertical placement**, 3-way size cycling (**1x1**, **2x2**, **3x3**), and an **automatic backplate toggle**. The one-click Undo system cleanly removes misplaced constructions and refunds all blocks and backplates.
*   🎨 **The Paint Brush & Custom Palette** — Right-click in the air to open a responsive GUI. Mix your own RGB/Hex codes and save up to 14 custom colors in your personal palette, or apply material textures directly to sign faces and backplates across multiple pages.
*   🌈 **Smart Fill & Rainbow Mode** — Sneak + Right-click in the air to toggle "Smart Fill", featuring intuitive circular HUD indicators. Instantly paint, light, or animate entire connected words at once with zero visual delay.
*   💧 **Eyedropper Mechanic** — Sneak + Right-click on any painted block in the world to copy its exact hex color directly to your Paint Brush.
*   🌍 **Global Localization** — Fully translated into 13 languages: English, Turkish, German, French, Spanish, Italian, Russian, Simplified Chinese, Brazilian Portuguese, Japanese, Korean, Polish, and Traditional Chinese.

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
