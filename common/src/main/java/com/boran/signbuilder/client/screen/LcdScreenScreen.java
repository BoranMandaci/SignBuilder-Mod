package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.block.entity.LcdTextFormatter;
import com.boran.signbuilder.client.LcdFontHelper;
import com.boran.signbuilder.client.render.LcdVisualAnimator;
import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.UpdateLcdScreenC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

public class LcdScreenScreen extends Screen {

    private final BlockPos masterPos;
    private String line1;
    private String line2;
    private int activeLine = 0;
    private int animEditLine = 0;
    private int mode;
    private int mode2;
    private int align;
    private int speed;
    private int fontSize;
    private int color;
    private boolean rainbow;
    private boolean glow;
    private int theme;
    private int widgetTL;
    private int widgetTR;
    private int widgetBL;
    private int widgetBR;
    private int fontStyle = 0;
    private int visualAnim;
    private int lastVisualAnim = 1;

    private int mainMode = 0;
    private int textTab = 0;
    private int visualTab = 0;
    private int visualPage = 0;

    private EditBox textField1;
    private EditBox textField2;
    private PillButton glowButton;

    private static final int PANEL_WIDTH = 356;
    private static final int PANEL_HEIGHT = 258;

    private static final int[] PRESET_COLORS = {
            0x00FF66,
            0x00E5FF,
            0x3388FF,
            0xFFD700,
            0xFF3333,
            0xCC33FF,
            0xFF55A3,
            0xFFFFFF
    };

    private static final String[] PRESET_COLOR_KEYS = {
            "color.signbuilder.green",
            "color.signbuilder.cyan",
            "color.signbuilder.blue",
            "color.signbuilder.yellow",
            "color.signbuilder.red",
            "color.signbuilder.purple",
            "color.signbuilder.pink",
            "color.signbuilder.white"
    };

    private static final String[] VAR_TAGS_R1 = { "{time}", "{weather}", "{player}", "{day}", "{online}" };
    private static final String[] VAR_NAMES_R1 = { "time", "weather", "player", "day", "online" };
    private static final String[] VAR_TAGS_R2 = { "{coords}", "{biome}", "{moon}", "{fps}", "{health}" };
    private static final String[] VAR_NAMES_R2 = { "coords", "biome", "moon", "fps", "health" };

    private static final String[] FORMAT_COLOR_CODES = { "&a", "&b", "&9", "&e", "&6", "&c", "&d", "&5", "&f", "&7" };
    private static final int[] FORMAT_COLOR_RGB = {
            0x00FF66,
            0x00E5FF,
            0x3388FF,
            0xFFFF33,
            0xFFAA00,
            0xFF3333,
            0xFF55FF,
            0xAA00AA,
            0xFFFFFF,
            0xAAAAAA
    };

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR, int fontStyle, int visualAnim) {
        super(Component.translatable("gui.signbuilder.lcd.title"));
        this.masterPos = masterPos;
        if (text != null && text.contains("\n")) {
            String[] parts = text.split("\n", 2);
            this.line1 = parts[0];
            this.line2 = parts.length > 1 ? parts[1] : "";
        } else {
            this.line1 = text != null ? text : "";
            this.line2 = "";
        }
        this.mode = Math.max(0, Math.min(9, mode));
        this.mode2 = Math.max(0, Math.min(9, mode2));
        this.align = Math.max(0, Math.min(2, align));
        this.speed = Math.max(1, Math.min(4, speed));
        this.fontSize = Math.max(0, Math.min(3, fontSize));
        this.color = color;
        this.rainbow = rainbow;
        this.glow = glow;
        this.theme = Math.max(0, Math.min(14, theme));
        this.widgetTL = Math.max(0, Math.min(10, widgetTL));
        this.widgetTR = Math.max(0, Math.min(10, widgetTR));
        this.widgetBL = Math.max(0, Math.min(10, widgetBL));
        this.widgetBR = Math.max(0, Math.min(10, widgetBR));
        this.fontStyle = Math.max(0, Math.min(4, fontStyle));
        this.visualAnim = Math.max(0, Math.min(60, visualAnim));
        this.lastVisualAnim = (this.visualAnim > 0) ? this.visualAnim : 1;
        this.visualPage = (this.visualAnim > 0) ? ((this.visualAnim - 1) / 12) : 0;
        this.mainMode = (this.visualAnim > 0) ? 1 : 0;
        this.textTab = 0;
        this.visualTab = 0;
    }

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR, int visualAnim) {
        this(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, 0, visualAnim);
    }

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, boolean showTime, boolean showWeather, boolean showPlayer, boolean showDay, int visualAnim) {
        this(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme,
             showTime ? 1 : 0, showWeather ? 2 : 0, showPlayer ? 3 : 0, showDay ? 4 : 0, 0, visualAnim);
    }

    @Override
    protected void init() {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int mainModeY = panelY + 18;
        this.addRenderableWidget(new PillButton(
                panelX + 14,
                mainModeY,
                162,
                18,
                Component.translatable("gui.signbuilder.lcd.main_mode.text"),
                () -> {
                    if (this.mainMode != 0) {
                        syncInputsToState();
                        this.mainMode = 0;
                        this.visualAnim = 0;
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> this.mainMode == 0,
                0xFF00FFCC,
                0xFF103438
        ));

        this.addRenderableWidget(new PillButton(
                panelX + 180,
                mainModeY,
                162,
                18,
                Component.translatable("gui.signbuilder.lcd.main_mode.visual"),
                () -> {
                    if (this.mainMode != 1) {
                        syncInputsToState();
                        this.mainMode = 1;
                        this.visualAnim = (this.lastVisualAnim > 0 ? this.lastVisualAnim : 1);
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> this.mainMode == 1,
                0xFFFF00D4,
                0xFF3B1032
        ));

        int subTabY = panelY + 91;
        if (this.mainMode == 0) {
            this.addRenderableWidget(new PillButton(
                    panelX + 14, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.text"),
                    () -> switchTextTab(0),
                    () -> this.textTab == 0,
                    0xFF00FFCC, 0xFF103438
            ));
            this.addRenderableWidget(new PillButton(
                    panelX + 125, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.effects"),
                    () -> switchTextTab(1),
                    () -> this.textTab == 1,
                    0xFF00FF88, 0xFF123526
            ));
            this.addRenderableWidget(new PillButton(
                    panelX + 236, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.theme_color"),
                    () -> switchTextTab(2),
                    () -> this.textTab == 2,
                    0xFFFF00D4, 0xFF3B1032
            ));

            if (this.textTab == 0) {
                initTextAndSymbolsTab(panelX, panelY);
            } else if (this.textTab == 1) {
                initTextEffectsAndWidgetsTab(panelX, panelY);
            } else {
                initThemeAndColorTab(panelX, panelY);
            }
        } else {
            this.addRenderableWidget(new PillButton(
                    panelX + 14, subTabY, 162, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.anims"),
                    () -> switchVisualTab(0),
                    () -> this.visualTab == 0,
                    0xFFFFAA00, 0xFF3A2510
            ));
            this.addRenderableWidget(new PillButton(
                    panelX + 180, subTabY, 162, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.theme_color"),
                    () -> switchVisualTab(1),
                    () -> this.visualTab == 1,
                    0xFFFF00D4, 0xFF3B1032
            ));

            if (this.visualTab == 0) {
                initVisualAnimationsTab(panelX, panelY);
            } else {
                initThemeAndColorTab(panelX, panelY);
            }
        }

        int bottomY = panelY + 231;
        this.addRenderableWidget(new ModernActionButton(
                panelX + 14, bottomY, 162, 20,
                Component.translatable("gui.signbuilder.lcd.save"),
                this::saveAndClose,
                0xFF103A25, 0xFF00FF7F, 0xFFFFFFFF
        ));
        this.addRenderableWidget(new ModernActionButton(
                panelX + 180, bottomY, 162, 20,
                Component.translatable("gui.signbuilder.lcd.cancel"),
                this::onClose,
                0xFF26303B, 0xFF546477, 0xFFD4E0EE
        ));
    }

    private void switchTextTab(int newTab) {
        if (this.textTab != newTab) {
            syncInputsToState();
            this.textTab = newTab;
            this.clearWidgets();
            this.init();
        }
    }

    private void switchVisualTab(int newTab) {
        if (this.visualTab != newTab) {
            syncInputsToState();
            this.visualTab = newTab;
            this.clearWidgets();
            this.init();
        }
    }

    private void syncInputsToState() {
        if (this.textField1 != null) this.line1 = this.textField1.getValue();
        if (this.textField2 != null) this.line2 = this.textField2.getValue();
    }

    private void selectActiveLine(int lineIdx) {
        this.activeLine = lineIdx;
        if (this.textField1 != null && this.textField2 != null) {
            boolean isL1 = (lineIdx == 0);
            this.textField1.setFocused(isL1);
            this.textField2.setFocused(!isL1);
            this.setFocused(isL1 ? this.textField1 : this.textField2);
        }
    }

    private void initTextAndSymbolsTab(int panelX, int panelY) {
        int line1Y = panelY + 112;
        this.addRenderableWidget(new PillButton(
                panelX + 14, line1Y, 64, 18,
                Component.translatable("gui.signbuilder.lcd.line1_label"),
                () -> selectActiveLine(0),
                () -> this.activeLine == 0,
                0xFF00FFCC, 0xFF103438
        ));

        this.textField1 = new EditBox(this.font, panelX + 82, line1Y, 240, 18, Component.translatable("gui.signbuilder.lcd.line1_hint"));
        this.textField1.setMaxLength(256);
        this.textField1.setValue(this.line1);
        this.textField1.setResponder(s -> {
            this.line1 = s;
            if (!s.isEmpty()) this.visualAnim = 0;
        });
        this.textField1.setTextColor(0xFFFFFFFF);
        this.textField1.setFocused(this.activeLine == 0);
        this.addRenderableWidget(this.textField1);

        ModernActionButton clearBtn1 = new ModernActionButton(
                panelX + 325, line1Y, 17, 18, Component.literal("✕"),
                () -> {
                    this.line1 = "";
                    this.textField1.setValue("");
                    selectActiveLine(0);
                },
                0xFF2A1C24, 0xFFFF4466, 0xFFFFAABB
        );
        clearBtn1.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.clear")));
        this.addRenderableWidget(clearBtn1);

        int line2Y = panelY + 133;
        this.addRenderableWidget(new PillButton(
                panelX + 14, line2Y, 64, 18,
                Component.translatable("gui.signbuilder.lcd.line2_label"),
                () -> selectActiveLine(1),
                () -> this.activeLine == 1,
                0xFF00FFCC, 0xFF103438
        ));

        this.textField2 = new EditBox(this.font, panelX + 82, line2Y, 240, 18, Component.translatable("gui.signbuilder.lcd.line2_hint"));
        this.textField2.setMaxLength(256);
        this.textField2.setValue(this.line2);
        this.textField2.setResponder(s -> {
            this.line2 = s;
            if (!s.isEmpty()) this.visualAnim = 0;
        });
        this.textField2.setTextColor(0xFFFFFFFF);
        this.textField2.setFocused(this.activeLine == 1);
        this.addRenderableWidget(this.textField2);

        ModernActionButton clearBtn2 = new ModernActionButton(
                panelX + 325, line2Y, 17, 18, Component.literal("✕"),
                () -> {
                    this.line2 = "";
                    this.textField2.setValue("");
                    selectActiveLine(1);
                },
                0xFF2A1C24, 0xFFFF4466, 0xFFFFAABB
        );
        clearBtn2.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.clear")));
        this.addRenderableWidget(clearBtn2);

        int toolY = panelY + 153;

        ModernActionButton boldBtn = new ModernActionButton(
                panelX + 14, toolY, 18, 16, Component.literal("B"),
                () -> insertSymbolIntoActiveLine("&l"),
                0xFF15202D, 0xFF354B64, 0xFF00FFCC
        );
        boldBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.bold")));
        this.addRenderableWidget(boldBtn);

        ModernActionButton italicBtn = new ModernActionButton(
                panelX + 35, toolY, 18, 16, Component.literal("I"),
                () -> insertSymbolIntoActiveLine("&o"),
                0xFF15202D, 0xFF354B64, 0xFF00FFCC
        );
        italicBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.italic")));
        this.addRenderableWidget(italicBtn);

        ModernActionButton underBtn = new ModernActionButton(
                panelX + 56, toolY, 18, 16, Component.literal("U"),
                () -> insertSymbolIntoActiveLine("&n"),
                0xFF15202D, 0xFF354B64, 0xFF00FFCC
        );
        underBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.underline")));
        this.addRenderableWidget(underBtn);

        ModernActionButton resetBtn = new ModernActionButton(
                panelX + 77, toolY, 21, 16, Component.literal("⟲"),
                () -> insertSymbolIntoActiveLine("&r"),
                0xFF2A1C24, 0xFFFF4466, 0xFFFFAABB
        );
        resetBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.reset")));
        this.addRenderableWidget(resetBtn);

        int colorStartX = panelX + 102;
        int swatchW = 20;
        int swatchGap = 4;
        for (int i = 0; i < FORMAT_COLOR_CODES.length; i++) {
            final String code = FORMAT_COLOR_CODES[i];
            int colRgb = FORMAT_COLOR_RGB[i];
            int cx = colorStartX + i * (swatchW + swatchGap);
            ModernActionButton colBtn = new ModernActionButton(
                    cx, toolY, swatchW, 16, Component.literal("■"),
                    () -> insertSymbolIntoActiveLine(code),
                    0xFF0E141C, colRgb, 0xFF000000 | colRgb
            );
            colBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.color_code_insert", code)));
            this.addRenderableWidget(colBtn);
        }

        int dockRow1Y = panelY + 174;
        int dockRow2Y = panelY + 200;
        int varStartX = panelX + 17;
        int varW = 62;
        int varH = 22;
        int varGap = 3;

        for (int i = 0; i < VAR_TAGS_R1.length; i++) {
            final String tag = VAR_TAGS_R1[i];
            final String nameKey = "gui.signbuilder.lcd.var." + VAR_NAMES_R1[i];
            final String tipKey = "gui.signbuilder.lcd.var." + VAR_NAMES_R1[i] + ".tooltip";
            int vx = varStartX + i * (varW + varGap);
            ModernActionButton varBtn = new ModernActionButton(
                    vx, dockRow1Y, varW, varH, Component.translatable(nameKey),
                    () -> insertSymbolIntoActiveLine(tag),
                    0xFF102838, 0xFF00E5FF, 0xFF00FFCC
            );
            varBtn.setTooltip(Tooltip.create(Component.translatable(tipKey)));
            this.addRenderableWidget(varBtn);
        }

        for (int i = 0; i < VAR_TAGS_R2.length; i++) {
            final String tag = VAR_TAGS_R2[i];
            final String nameKey = "gui.signbuilder.lcd.var." + VAR_NAMES_R2[i];
            final String tipKey = "gui.signbuilder.lcd.var." + VAR_NAMES_R2[i] + ".tooltip";
            int vx = varStartX + i * (varW + varGap);
            ModernActionButton varBtn = new ModernActionButton(
                    vx, dockRow2Y, varW, varH, Component.translatable(nameKey),
                    () -> insertSymbolIntoActiveLine(tag),
                    0xFF122230, 0xFF00FF88, 0xFFFFAA00
            );
            varBtn.setTooltip(Tooltip.create(Component.translatable(tipKey)));
            this.addRenderableWidget(varBtn);
        }

        if (this.activeLine == 1) {
            this.setFocused(this.textField2);
        } else {
            this.setFocused(this.textField1);
        }
    }

    private void initTextEffectsAndWidgetsTab(int panelX, int panelY) {
        String[] alignLabels = {
                "gui.signbuilder.lcd.align.left",
                "gui.signbuilder.lcd.align.center",
                "gui.signbuilder.lcd.align.right"
        };
        int[] alignX = { panelX + 14, panelX + 67, panelX + 122 };
        int[] alignW = { 50, 52, 50 };
        int alignY = panelY + 112;

        for (int i = 0; i < 3; i++) {
            final int a = i;
            this.addRenderableWidget(new PillButton(
                    alignX[i], alignY, alignW[i], 18,
                    Component.translatable(alignLabels[i]),
                    () -> this.align = a,
                    () -> this.align == a,
                    0xFF00FFCC, 0xFF123438
            ));
        }

        String[] sizeLabels = {
                "gui.signbuilder.lcd.size.0",
                "gui.signbuilder.lcd.size.1",
                "gui.signbuilder.lcd.size.2",
                "gui.signbuilder.lcd.size.3"
        };
        String[] sizeTooltips = {
                "gui.signbuilder.lcd.size.0.tooltip",
                "gui.signbuilder.lcd.size.1.tooltip",
                "gui.signbuilder.lcd.size.2.tooltip",
                "gui.signbuilder.lcd.size.3.tooltip"
        };
        int[] sizeX = { panelX + 180, panelX + 221, panelX + 263, panelX + 304 };
        int[] sizeW = { 38, 39, 38, 38 };

        for (int i = 0; i < 4; i++) {
            final int fs = i;
            PillButton sizeBtn = new PillButton(
                    sizeX[i], alignY, sizeW[i], 18,
                    Component.translatable(sizeLabels[i]),
                    () -> this.fontSize = fs,
                    () -> this.fontSize == fs,
                    0xFFFFC857, 0xFF3A2C12
            );
            sizeBtn.setTooltip(Tooltip.create(Component.translatable(sizeTooltips[i])));
            this.addRenderableWidget(sizeBtn);
        }

        int lineTargetY = panelY + 133;
        this.addRenderableWidget(new PillButton(
                panelX + 14, lineTargetY, 160, 16,
                Component.translatable("gui.signbuilder.lcd.line1_label"),
                () -> this.animEditLine = 0,
                () -> this.animEditLine == 0,
                0xFF00FFCC, 0xFF103438
        ));
        this.addRenderableWidget(new PillButton(
                panelX + 178, lineTargetY, 164, 16,
                Component.translatable("gui.signbuilder.lcd.line2_label"),
                () -> this.animEditLine = 1,
                () -> this.animEditLine == 1,
                0xFF00FFCC, 0xFF103438
        ));

        String[] modeLabels = {
                "gui.signbuilder.lcd.mode.static",
                "gui.signbuilder.lcd.mode.scroll_left",
                "gui.signbuilder.lcd.mode.scroll_right",
                "gui.signbuilder.lcd.mode.blink",
                "gui.signbuilder.lcd.mode.typewriter",
                "gui.signbuilder.lcd.mode.pulse",
                "gui.signbuilder.lcd.mode.wave",
                "gui.signbuilder.lcd.mode.glitch",
                "gui.signbuilder.lcd.mode.rainbow",
                "gui.signbuilder.lcd.mode.auto_scroll"
        };
        String[] modeTooltips = {
                "gui.signbuilder.lcd.mode.static.tooltip",
                "gui.signbuilder.lcd.mode.scroll_left.tooltip",
                "gui.signbuilder.lcd.mode.scroll_right.tooltip",
                "gui.signbuilder.lcd.mode.blink.tooltip",
                "gui.signbuilder.lcd.mode.typewriter.tooltip",
                "gui.signbuilder.lcd.mode.pulse.tooltip",
                "gui.signbuilder.lcd.mode.wave.tooltip",
                "gui.signbuilder.lcd.mode.glitch.tooltip",
                "gui.signbuilder.lcd.mode.rainbow.tooltip",
                "gui.signbuilder.lcd.mode.auto_scroll.tooltip"
        };
        int[] modeX = { panelX + 14, panelX + 80, panelX + 146, panelX + 212, panelX + 278 };
        int[] modeW = { 62, 62, 62, 62, 64 };
        int modeRow1Y = panelY + 152;
        int modeRow2Y = panelY + 171;

        for (int i = 0; i < 10; i++) {
            final int m = i;
            int col = i % 5;
            int bx = modeX[col];
            int bw = modeW[col];
            int by = (i < 5) ? modeRow1Y : modeRow2Y;
            PillButton btn = new PillButton(
                    bx, by, bw, 17,
                    Component.translatable(modeLabels[i]),
                    () -> {
                        if (this.animEditLine == 1) {
                            this.mode2 = m;
                        } else {
                            this.mode = m;
                        }
                    },
                    () -> (this.animEditLine == 1 ? this.mode2 : this.mode) == m,
                    0xFF00FF88, 0xFF123526
            );
            btn.setTooltip(Tooltip.create(Component.translatable(modeTooltips[i])));
            this.addRenderableWidget(btn);
        }

        String[] speedLabels = {
                "gui.signbuilder.lcd.speed.1",
                "gui.signbuilder.lcd.speed.2",
                "gui.signbuilder.lcd.speed.3",
                "gui.signbuilder.lcd.speed.4"
        };
        int[] speedX = { panelX + 14, panelX + 96, panelX + 178, panelX + 260 };
        int[] speedW = { 78, 78, 78, 82 };
        int speedY = panelY + 191;

        for (int i = 0; i < 4; i++) {
            final int s = i + 1;
            this.addRenderableWidget(new PillButton(
                    speedX[i], speedY, speedW[i], 17,
                    Component.translatable(speedLabels[i]),
                    () -> this.speed = s,
                    () -> this.speed == s,
                    0xFF00E5FF, 0xFF102E3B
            ));
        }

        int widgetY = panelY + 210;
        int[] cornerX = { panelX + 14, panelX + 97, panelX + 180, panelX + 263 };
        int[] cornerW = { 79, 79, 79, 79 };

        PillButton btnTL = new PillButton(
                cornerX[0], widgetY, cornerW[0], 17,
                getWidgetCornerLabel("↖", this.widgetTL),
                () -> {
                    this.widgetTL = (this.widgetTL + 1) % LcdTextFormatter.WIDGET_COUNT;
                    this.clearWidgets();
                    this.init();
                },
                () -> this.widgetTL > 0,
                0xFF00FFCC, 0xFF103438
        );
        btnTL.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.corner_tooltip", Component.translatable("gui.signbuilder.lcd.corner.tl"))));
        this.addRenderableWidget(btnTL);

        PillButton btnTR = new PillButton(
                cornerX[1], widgetY, cornerW[1], 17,
                getWidgetCornerLabel("↗", this.widgetTR),
                () -> {
                    this.widgetTR = (this.widgetTR + 1) % LcdTextFormatter.WIDGET_COUNT;
                    this.clearWidgets();
                    this.init();
                },
                () -> this.widgetTR > 0,
                0xFF00FFCC, 0xFF103438
        );
        btnTR.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.corner_tooltip", Component.translatable("gui.signbuilder.lcd.corner.tr"))));
        this.addRenderableWidget(btnTR);

        PillButton btnBL = new PillButton(
                cornerX[2], widgetY, cornerW[2], 17,
                getWidgetCornerLabel("↙", this.widgetBL),
                () -> {
                    this.widgetBL = (this.widgetBL + 1) % LcdTextFormatter.WIDGET_COUNT;
                    this.clearWidgets();
                    this.init();
                },
                () -> this.widgetBL > 0,
                0xFF00FFCC, 0xFF103438
        );
        btnBL.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.corner_tooltip", Component.translatable("gui.signbuilder.lcd.corner.bl"))));
        this.addRenderableWidget(btnBL);

        PillButton btnBR = new PillButton(
                cornerX[3], widgetY, cornerW[3], 17,
                getWidgetCornerLabel("↘", this.widgetBR),
                () -> {
                    this.widgetBR = (this.widgetBR + 1) % LcdTextFormatter.WIDGET_COUNT;
                    this.clearWidgets();
                    this.init();
                },
                () -> this.widgetBR > 0,
                0xFF00FFCC, 0xFF103438
        );
        btnBR.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.corner_tooltip", Component.translatable("gui.signbuilder.lcd.corner.br"))));
        this.addRenderableWidget(btnBR);
    }

    private Component getWidgetCornerLabel(String cornerPrefix, int widgetType) {
        String key = switch (widgetType) {
            case 1 -> "gui.signbuilder.lcd.widget.time";
            case 2 -> "gui.signbuilder.lcd.widget.weather";
            case 3 -> "gui.signbuilder.lcd.widget.player";
            case 4 -> "gui.signbuilder.lcd.widget.day";
            case 5 -> "gui.signbuilder.lcd.widget.online";
            case 6 -> "gui.signbuilder.lcd.widget.coords";
            case 7 -> "gui.signbuilder.lcd.widget.biome";
            case 8 -> "gui.signbuilder.lcd.widget.moon";
            case 9 -> "gui.signbuilder.lcd.widget.fps";
            case 10 -> "gui.signbuilder.lcd.widget.health";
            default -> "gui.signbuilder.lcd.widget.none";
        };
        return Component.literal(cornerPrefix + " ").append(Component.translatable(key));
    }

    private void insertSymbolIntoActiveLine(String symbol) {
        this.visualAnim = 0;
        if (this.activeLine == 1 && this.textField2 != null) {
            this.textField2.insertText(symbol);
            this.line2 = this.textField2.getValue();
            selectActiveLine(1);
        } else if (this.textField1 != null) {
            this.textField1.insertText(symbol);
            this.line1 = this.textField1.getValue();
            selectActiveLine(0);
        }
    }

    private void initVisualAnimationsTab(int panelX, int panelY) {
        int totalAnims = 60;
        int totalPages = (totalAnims + 11) / 12;
        int startIndex = this.visualPage * 12;
        int endIndex = Math.min(startIndex + 12, totalAnims);

        int[] colX = { panelX + 14, panelX + 97, panelX + 180, panelX + 263 };
        int colW = 79;
        int row1Y = panelY + 111;
        int row2Y = panelY + 129;
        int row3Y = panelY + 147;

        for (int i = startIndex; i < endIndex; i++) {
            final int v = i + 1;
            int slot = i - startIndex;
            int col = slot % 4;
            int by = (slot < 4) ? row1Y : ((slot < 8) ? row2Y : row3Y);
            PillButton btn = new PillButton(
                    colX[col], by, colW, 16,
                    Component.translatable("gui.signbuilder.lcd.visual." + v),
                    () -> {
                        this.visualAnim = v;
                        this.lastVisualAnim = v;
                    },
                    () -> this.visualAnim == v,
                    0xFFFFAA00, 0xFF3A2510
            );
            btn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.visual." + v + ".tooltip")));
            this.addRenderableWidget(btn);
        }

        int pageY = panelY + 166;
        PillButton prevBtn = new PillButton(
                panelX + 14, pageY, 44, 15,
                Component.translatable("gui.signbuilder.lcd.prev_page"),
                () -> {
                    if (this.visualPage > 0) {
                        this.visualPage--;
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> false,
                0xFFFFAA00, 0xFF2A1C10
        );
        prevBtn.active = (this.visualPage > 0);
        this.addRenderableWidget(prevBtn);

        this.addRenderableWidget(new PillButton(
                panelX + 62, pageY, 232, 15,
                Component.translatable("gui.signbuilder.lcd.page", (this.visualPage + 1), totalPages),
                () -> {},
                () -> true,
                0xFFFFAA00, 0xFF141C25
        ));

        PillButton nextBtn = new PillButton(
                panelX + 298, pageY, 44, 15,
                Component.translatable("gui.signbuilder.lcd.next_page"),
                () -> {
                    if (this.visualPage < totalPages - 1) {
                        this.visualPage++;
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> false,
                0xFFFFAA00, 0xFF2A1C10
        );
        nextBtn.active = (this.visualPage < totalPages - 1);
        this.addRenderableWidget(nextBtn);

        String[] speedLabels = {
                "gui.signbuilder.lcd.speed.1",
                "gui.signbuilder.lcd.speed.2",
                "gui.signbuilder.lcd.speed.3",
                "gui.signbuilder.lcd.speed.4"
        };
        int[] speedColX = { panelX + 14, panelX + 96, panelX + 178, panelX + 260 };
        int[] speedColW = { 78, 78, 78, 82 };
        int speedY = panelY + 184;

        for (int i = 0; i < 4; i++) {
            final int s = i + 1;
            this.addRenderableWidget(new PillButton(
                    speedColX[i], speedY, speedColW[i], 16,
                    Component.translatable(speedLabels[i]),
                    () -> this.speed = s,
                    () -> this.speed == s,
                    0xFF00E5FF, 0xFF102E3B
            ));
        }
    }

    private void initThemeAndColorTab(int panelX, int panelY) {
        String[] themeLabels = {
                "gui.signbuilder.lcd.theme.oled",
                "gui.signbuilder.lcd.theme.matrix",
                "gui.signbuilder.lcd.theme.amber",
                "gui.signbuilder.lcd.theme.cyber",
                "gui.signbuilder.lcd.theme.alert",
                "gui.signbuilder.lcd.theme.synthwave",
                "gui.signbuilder.lcd.theme.blueprint",
                "gui.signbuilder.lcd.theme.arcade",
                "gui.signbuilder.lcd.theme.pipboy",
                "gui.signbuilder.lcd.theme.cryo",
                "gui.signbuilder.lcd.theme.magma",
                "gui.signbuilder.lcd.theme.glitch",
                "gui.signbuilder.lcd.theme.eink",
                "gui.signbuilder.lcd.theme.gold",
                "gui.signbuilder.lcd.theme.steampunk"
        };
        String[] themeTooltips = {
                "gui.signbuilder.lcd.theme.oled.tooltip",
                "gui.signbuilder.lcd.theme.matrix.tooltip",
                "gui.signbuilder.lcd.theme.amber.tooltip",
                "gui.signbuilder.lcd.theme.cyber.tooltip",
                "gui.signbuilder.lcd.theme.alert.tooltip",
                "gui.signbuilder.lcd.theme.synthwave.tooltip",
                "gui.signbuilder.lcd.theme.blueprint.tooltip",
                "gui.signbuilder.lcd.theme.arcade.tooltip",
                "gui.signbuilder.lcd.theme.pipboy.tooltip",
                "gui.signbuilder.lcd.theme.cryo.tooltip",
                "gui.signbuilder.lcd.theme.magma.tooltip",
                "gui.signbuilder.lcd.theme.glitch.tooltip",
                "gui.signbuilder.lcd.theme.eink.tooltip",
                "gui.signbuilder.lcd.theme.gold.tooltip",
                "gui.signbuilder.lcd.theme.steampunk.tooltip"
        };
        int[] themeColX = { panelX + 14, panelX + 80, panelX + 146, panelX + 212, panelX + 278 };
        int themeColW = 64;
        int themeRow1Y = panelY + 112;
        int themeRow2Y = panelY + 131;
        int themeRow3Y = panelY + 150;

        for (int i = 0; i < 15; i++) {
            final int t = i;
            int col = i % 5;
            int by = (i < 5) ? themeRow1Y : ((i < 10) ? themeRow2Y : themeRow3Y);
            PillButton btn = new PillButton(
                    themeColX[col], by, themeColW, 16,
                    Component.translatable(themeLabels[i]),
                    () -> {
                        this.theme = t;
                        applyThemeColorPreset(t);
                    },
                    () -> this.theme == t,
                    0xFFFF00D4, 0xFF3B1032
            );
            btn.setTooltip(Tooltip.create(Component.translatable(themeTooltips[i])));
            this.addRenderableWidget(btn);
        }

        int colorStartX = panelX + 14;
        int colorY = panelY + 171;
        int swatchW = 20;
        int swatchGap = 4;

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            final int c = PRESET_COLORS[i];
            final String cKey = PRESET_COLOR_KEYS[i];
            int sx = colorStartX + i * (swatchW + swatchGap);
            ColorSwatchButton swatchBtn = new ColorSwatchButton(
                    sx, colorY, swatchW, 18, c, cKey,
                    () -> { this.color = c; this.rainbow = false; },
                    () -> !this.rainbow && this.color == c
            );
            this.addRenderableWidget(swatchBtn);
        }

        int rainbowX = panelX + 208;
        int rainbowW = 60;
        PillButton rainbowBtn = new PillButton(
                rainbowX, colorY, rainbowW, 18,
                Component.translatable("gui.signbuilder.lcd.rainbow"),
                () -> this.rainbow = !this.rainbow,
                () -> this.rainbow,
                0xFFFF00D4, 0xFF3B1032
        );
        rainbowBtn.setTooltip(Tooltip.create(Component.translatable("color.signbuilder.rainbow")));
        this.addRenderableWidget(rainbowBtn);

        int pickerX = panelX + 272;
        int pickerW = 70;
        PillButton pickerBtn = new PillButton(
                pickerX, colorY, pickerW, 18,
                Component.translatable("gui.signbuilder.lcd.color_picker"),
                () -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new ColorPickerScreen(this.color, this, newCol -> {
                            this.color = newCol;
                            this.rainbow = false;
                        }));
                    }
                },
                () -> !this.rainbow && !isPresetColor(this.color),
                0xFFFFAA00, 0xFF3A2810
        );
        pickerBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.color_picker.title")));
        this.addRenderableWidget(pickerBtn);

        int glowY = panelY + 195;
        if (this.mainMode == 0) {
            this.glowButton = new PillButton(
                    panelX + 14, glowY, 162, 18,
                    getGlowComponent(),
                    () -> {
                        this.glow = !this.glow;
                        this.glowButton.setMessage(getGlowComponent());
                    },
                    () -> this.glow,
                    0xFFFFC857, 0xFF3B3012
            );
            this.glowButton.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.glow_tooltip")));
            this.addRenderableWidget(this.glowButton);

            PillButton fontBtn = new PillButton(
                    panelX + 180, glowY, 162, 18,
                    getFontStyleComponent(),
                    () -> {
                        this.fontStyle = (this.fontStyle + 1) % LcdFontHelper.FONT_COUNT;
                        this.clearWidgets();
                        this.init();
                    },
                    () -> true,
                    0xFF00FFCC, 0xFF103438
            );
            fontBtn.setTooltip(Tooltip.create(Component.translatable(LcdFontHelper.FONT_STYLE_TOOLTIPS[this.fontStyle])));
            this.addRenderableWidget(fontBtn);
        } else {
            this.glowButton = new PillButton(
                    panelX + 14, glowY, 328, 18,
                    getGlowComponent(),
                    () -> {
                        this.glow = !this.glow;
                        this.glowButton.setMessage(getGlowComponent());
                    },
                    () -> this.glow,
                    0xFFFFC857, 0xFF3B3012
            );
            this.glowButton.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.glow_tooltip")));
            this.addRenderableWidget(this.glowButton);
        }
    }

    private void applyThemeColorPreset(int t) {
        if (t == 1) { this.color = 0x00FF44; this.rainbow = false; }
        else if (t == 2) { this.color = 0xFFAA00; this.rainbow = false; }
        else if (t == 3) { this.color = 0x00E5FF; this.rainbow = false; }
        else if (t == 4) { this.color = 0xFF2233; this.rainbow = false; }
        else if (t == 5) { this.color = 0xFF22AA; this.rainbow = false; }
        else if (t == 6) { this.color = 0x00D4FF; this.rainbow = false; }
        else if (t == 7) { this.color = 0x99B830; this.rainbow = false; }
        else if (t == 8) { this.color = 0x22EE55; this.rainbow = false; }
        else if (t == 9) { this.color = 0x66EEFF; this.rainbow = false; }
        else if (t == 10) { this.color = 0xFF5500; this.rainbow = false; }
        else if (t == 11) { this.color = 0xFF0055; this.rainbow = false; }
        else if (t == 12) { this.color = 0x111822; this.rainbow = false; }
        else if (t == 13) { this.color = 0xFFD700; this.rainbow = false; }
        else if (t == 14) { this.color = 0xFF9922; this.rainbow = false; }
    }

    private boolean isPresetColor(int c) {
        for (int p : PRESET_COLORS) {
            if (p == c) return true;
        }
        return false;
    }

    private Component getGlowComponent() {
        return Component.translatable(this.glow ? "gui.signbuilder.lcd.glow_on" : "gui.signbuilder.lcd.glow_off");
    }

    private Component getFontStyleComponent() {
        return Component.translatable("gui.signbuilder.lcd.font_label", LcdFontHelper.getFontNameComponent(this.fontStyle));
    }

    private void saveAndClose() {
        syncInputsToState();
        String combined = this.line1;
        if (this.line2 != null && !this.line2.isEmpty()) {
            combined = this.line1 + "\n" + this.line2;
        }
        int effectiveVisualAnim = (this.mainMode == 1) ? this.visualAnim : 0;
        ModMessages.sendToServer(new UpdateLcdScreenC2SPacket(
                this.masterPos,
                combined,
                this.mode,
                this.mode2,
                this.align,
                this.speed,
                this.fontSize,
                this.color,
                this.rainbow,
                this.glow,
                this.theme,
                (this.mainMode == 0) ? this.widgetTL : 0,
                (this.mainMode == 0) ? this.widgetTR : 0,
                (this.mainMode == 0) ? this.widgetBL : 0,
                (this.mainMode == 0) ? this.widgetBR : 0,
                this.fontStyle,
                effectiveVisualAnim
        ));
        this.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.mainMode == 0 && this.textTab == 0) {
            if (this.textField1 != null && this.textField1.isMouseOver(mouseX, mouseY)) {
                this.activeLine = 0;
            } else if (this.textField2 != null && this.textField2.isMouseOver(mouseX, mouseY)) {
                this.activeLine = 1;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            saveAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        guiGraphics.fill(panelX - 4, panelY + 4, panelX + PANEL_WIDTH + 4, panelY + PANEL_HEIGHT + 6, 0x66000000);
        guiGraphics.fillGradient(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xFF1B232E, 0xFF111720);
        guiGraphics.renderOutline(panelX - 1, panelY - 1, PANEL_WIDTH + 2, PANEL_HEIGHT + 2, 0xFF354457);
        guiGraphics.renderOutline(panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 0x442B3746);

        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 2, 0xFF00E5FF);

        guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.lcd.title"), panelX + 14, panelY + 6, 0xFF00E5FF, true);

        int prevX = panelX + 14;
        int prevY = panelY + 39;
        int prevW = 328;
        int prevH = 48;

        renderThemedMonitor(guiGraphics, prevX, prevY, prevW, prevH);

        if (this.mainMode == 0 && this.textTab == 0) {
            int dockX = panelX + 14;
            int dockY = panelY + 171;
            int dockW = 328;
            int dockH = 55;
            guiGraphics.fill(dockX, dockY, dockX + dockW, dockY + dockH, 0xFF0B1017);
            guiGraphics.renderOutline(dockX, dockY, dockW, dockH, 0xFF233242);
        } else if (this.mainMode == 1 && this.visualTab == 0) {
            int infoX = panelX + 14;
            int infoY = panelY + 203;
            int infoW = 328;
            int infoH = 25;
            guiGraphics.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0xFF0B1017);
            guiGraphics.renderOutline(infoX, infoY, infoW, infoH, 0xFF283647);

            String tipKey = "gui.signbuilder.lcd.visual." + this.visualAnim + ".tooltip";
            String titleKey = "gui.signbuilder.lcd.visual." + this.visualAnim;
            guiGraphics.drawString(this.font, Component.translatable(titleKey), infoX + 8, infoY + 3, 0xFFFFAA00, true);
            net.minecraft.network.chat.FormattedText trimmed = this.font.substrByWidth(Component.translatable(tipKey), infoW - 16);
            guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimmed), infoX + 8, infoY + 13, 0xFF8EA0B4, false);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderThemedMonitor(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        int bg = switch (this.theme) {
            case 1 -> 0xFF05240E;
            case 2 -> 0xFF2A1502;
            case 3 -> 0xFF081938;
            case 4 -> {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 250.0);
                int r = (int) (48 + pulse * 45);
                yield 0xFF000000 | (r << 16) | (0x07 << 8) | 0x0D;
            }
            case 5 -> 0xFF1C0826;
            case 6 -> 0xFF041230;
            case 7 -> 0xFF1E280E;
            case 8 -> 0xFF081C0C;
            case 9 -> 0xFF041B2B;
            case 10 -> {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 280.0);
                int r = (int) (38 + pulse * 25);
                int g = (int) (12 + pulse * 14);
                yield 0xFF000000 | (r << 16) | (g << 8) | 0x04;
            }
            case 11 -> 0xFF18061E;
            case 12 -> 0xFFE8EBE6;
            case 13 -> 0xFF0A140E;
            case 14 -> 0xFF1C120A;
            default -> 0xFF0A0F17;
        };
        int border = switch (this.theme) {
            case 1 -> 0xFF00FF55;
            case 2 -> 0xFFFFB300;
            case 3 -> 0xFF00F0FF;
            case 4 -> 0xFFFF2233;
            case 5 -> 0xFFFF1493;
            case 6 -> 0xFF00D4FF;
            case 7 -> 0xFF99B830;
            case 8 -> 0xFF22EE55;
            case 9 -> 0xFF66EEFF;
            case 10 -> 0xFFFF5500;
            case 11 -> 0xFFFF006E;
            case 12 -> 0xFF323840;
            case 13 -> 0xFFFFD700;
            case 14 -> 0xFFCD853F;
            default -> 0xFF1F3A4D;
        };

        guiGraphics.fill(x, y, x + width, y + height, bg);
        guiGraphics.renderOutline(x, y, width, height, border);
        guiGraphics.renderOutline(x - 1, y - 1, width + 2, height + 2, 0xFF0B1017);

        int scanColor = switch (this.theme) {
            case 1 -> 0x3000FF44;
            case 2 -> 0x30FFAA00;
            case 3 -> 0x3000E5FF;
            case 4 -> 0x38FF2233;
            case 5 -> 0x30FF1493;
            case 6 -> 0x3000D4FF;
            case 7 -> 0x3099B830;
            case 8 -> 0x3522EE55;
            case 9 -> 0x3066EEFF;
            case 10 -> 0x38FF5500;
            case 11 -> 0x38FF0055;
            case 12 -> 0x0C000000;
            case 13 -> 0x30FFD700;
            case 14 -> 0x30FFAA22;
            default -> 0x18000000;
        };
        for (int lineY = y + 1; lineY < y + height - 1; lineY += 2) {
            guiGraphics.fill(x + 1, lineY, x + width - 1, lineY + 1, scanColor);
        }

        renderLivePreview(guiGraphics, x, y, width, height);
    }

    private void renderLivePreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.enableScissor(x + 2, y + 2, x + width - 2, y + height - 2);

        net.minecraft.world.level.Level clientLevel = this.minecraft != null ? this.minecraft.level : null;

        int renderCol = this.rainbow ? com.boran.signbuilder.util.RainbowColorHelper.getRainbowRgb() : this.color;

        if (this.mainMode == 0 && this.visualAnim == 0) {
            int widgetCol = 0xFF000000 | (renderCol & 0x00FFFFFF);

            float wScale = 0.70f;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(wScale, wScale, 1.0f);
            int sxLeft = (int) ((x + 5) / wScale);
            int sxRight = (int) ((x + width - 5) / wScale);
            int syTop = (int) ((y + 4) / wScale);
            int syBottom = (int) ((y + height - 4) / wScale) - this.font.lineHeight;

            if (this.widgetTL > 0) {
                String str = LcdTextFormatter.getWidgetText(this.widgetTL, clientLevel, this.masterPos);
                if (!str.isEmpty()) guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(str, this.fontStyle), sxLeft, syTop, widgetCol, false);
            }
            if (this.widgetTR > 0) {
                String str = LcdTextFormatter.getWidgetText(this.widgetTR, clientLevel, this.masterPos);
                if (!str.isEmpty()) guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(str, this.fontStyle), sxRight - LcdFontHelper.getWidth(this.font, str, this.fontStyle), syTop, widgetCol, false);
            }
            if (this.widgetBL > 0) {
                String str = LcdTextFormatter.getWidgetText(this.widgetBL, clientLevel, this.masterPos);
                if (!str.isEmpty()) guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(str, this.fontStyle), sxLeft, syBottom, widgetCol, false);
            }
            if (this.widgetBR > 0) {
                String str = LcdTextFormatter.getWidgetText(this.widgetBR, clientLevel, this.masterPos);
                if (!str.isEmpty()) guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(str, this.fontStyle), sxRight - LcdFontHelper.getWidth(this.font, str, this.fontStyle), syBottom, widgetCol, false);
            }
            guiGraphics.pose().popPose();
        }

        long timeMs = System.currentTimeMillis();

        if (this.mainMode == 1 && this.visualAnim > 0) {
            LcdVisualAnimator.render2D(guiGraphics, x + 4, x + width - 4, y + 3, y + height - 3,
                    this.visualAnim, this.speed, renderCol, this.rainbow, timeMs);
            guiGraphics.disableScissor();
            return;
        }

        String l1 = (this.textField1 != null ? this.textField1.getValue() : this.line1);
        String l2 = (this.textField2 != null ? this.textField2.getValue() : this.line2);

        l1 = LcdTextFormatter.resolveAll(l1, clientLevel, this.masterPos);
        l2 = LcdTextFormatter.resolveAll(l2, clientLevel, this.masterPos);

        boolean hasL1 = !l1.isEmpty();
        boolean hasL2 = !l2.isEmpty();

        float scale = switch (this.fontSize) {
            case 0 -> 0.75f;
            case 2 -> 1.35f;
            case 3 -> 1.75f;
            default -> 1.0f;
        };

        double pixelsPerSec = switch (this.speed) {
            case 1 -> 25.0;
            case 2 -> 50.0;
            case 3 -> 85.0;
            case 4 -> 135.0;
            default -> 50.0;
        };

        if (hasL1 && hasL2) {
            float lineH = (this.font.lineHeight + 1) * scale;
            float totalH = lineH * 2;
            int startY = y + (int) ((height - totalH) / 2);

            renderPreviewSingleLine(guiGraphics, l1, this.align, this.mode, pixelsPerSec, timeMs,
                    renderCol, x + 6, startY, width - 12, scale);
            renderPreviewSingleLine(guiGraphics, l2, this.align, this.mode2, pixelsPerSec, timeMs,
                    renderCol, x + 6, startY + (int) lineH, width - 12, scale);
        } else if (hasL1) {
            int lineY = y + (int) ((height - (this.font.lineHeight * scale)) / 2);
            renderPreviewSingleLine(guiGraphics, l1, this.align, this.mode, pixelsPerSec, timeMs,
                    renderCol, x + 6, lineY, width - 12, scale);
        } else if (hasL2) {
            int lineY = y + (int) ((height - (this.font.lineHeight * scale)) / 2);
            renderPreviewSingleLine(guiGraphics, l2, this.align, this.mode2, pixelsPerSec, timeMs,
                    renderCol, x + 6, lineY, width - 12, scale);
        }

        guiGraphics.disableScissor();
    }

    private void renderPreviewSingleLine(GuiGraphics guiGraphics, String lineText, int align, int mode,
                                         double pixelsPerSec, long timeMs, int renderCol,
                                         int x, int y, int width, float scale) {
        if (lineText.isEmpty()) return;

        float textWidth = LcdFontHelper.getWidth(this.font, lineText, this.fontStyle) * scale;
        float alignedBaseX = switch (align) {
            case 0 -> 0;
            case 2 -> width - textWidth;
            default -> (width - textWidth) / 2.0f;
        };

        float startX = alignedBaseX;
        String textToDraw = lineText;

        if (mode == 1) {
            double track = width + textWidth + 12.0;
            double cycleMs = Math.max(100.0, (track / pixelsPerSec) * 1000.0);
            double progress = (timeMs % (long) cycleMs) / cycleMs;
            startX = (float) (width - (progress * track));
        } else if (mode == 2) {
            double track = width + textWidth + 12.0;
            double cycleMs = Math.max(100.0, (track / pixelsPerSec) * 1000.0);
            double progress = (timeMs % (long) cycleMs) / cycleMs;
            startX = (float) (-textWidth + (progress * track));
        } else if (mode == 3) {
            int blinkInterval = switch (this.speed) {
                case 1 -> 800;
                case 2 -> 500;
                case 3 -> 300;
                case 4 -> 150;
                default -> 500;
            };
            if (((timeMs / blinkInterval) % 2) != 0) return;
        } else if (mode == 4) {
            int charDelay = switch (this.speed) {
                case 1 -> 250;
                case 2 -> 150;
                case 3 -> 90;
                case 4 -> 50;
                default -> 150;
            };
            int visLen = LcdTextFormatter.countVisibleChars(lineText);
            int cycle = (int) ((timeMs / charDelay) % (visLen + 10));
            int showChars = Math.min(visLen, cycle);
            textToDraw = LcdTextFormatter.substringVisibleChars(lineText, showChars);
        } else if (mode == 5) {
            double freq = switch (this.speed) {
                case 1 -> 0.002;
                case 2 -> 0.004;
                case 3 -> 0.007;
                case 4 -> 0.012;
                default -> 0.004;
            };
            double timeMod = (timeMs % 10000000L);
            float factor = (float) (0.625 + 0.375 * Math.sin(timeMod * freq));
            int r = (int) (((renderCol >> 16) & 0xFF) * factor);
            int g = (int) (((renderCol >> 8) & 0xFF) * factor);
            int b = (int) ((renderCol & 0xFF) * factor);
            renderCol = (r << 16) | (g << 8) | b;
        } else if (mode == 6) {
            renderPreviewWaveLine(guiGraphics, lineText, alignedBaseX, x, y, scale, renderCol, this.glow, this.speed, timeMs);
            return;
        } else if (mode == 7) {
            textToDraw = LcdTextFormatter.buildGlitchText(lineText, this.speed, timeMs);
        } else if (mode == 8) {
            renderPreviewRainbowLine(guiGraphics, lineText, alignedBaseX, x, y, scale, this.glow, this.speed, timeMs);
            return;
        } else if (mode == 9) {
            if (textWidth <= width) {
                startX = alignedBaseX;
            } else {
                double track = width + textWidth + 12.0;
                double cycleMs = Math.max(100.0, (track / pixelsPerSec) * 1000.0);
                double progress = (timeMs % (long) cycleMs) / cycleMs;
                startX = (float) (width - (progress * track));
            }
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(textToDraw, this.fontStyle), (int) ((x / scale) + startX), (int) (y / scale), 0xFF000000 | renderCol, this.glow);
        guiGraphics.pose().popPose();
    }

    private void renderPreviewWaveLine(GuiGraphics guiGraphics, String lineText, float startX,
                                       int x, int y, float scale, int renderCol,
                                       boolean glow, int speed, long timeMs) {
        double freq = switch (speed) {
            case 1 -> 0.003;
            case 2 -> 0.006;
            case 3 -> 0.010;
            case 4 -> 0.016;
            default -> 0.006;
        };

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        float curX = startX;
        float baseY = y / scale;
        StringBuilder activeStyle = new StringBuilder();
        int charIdx = 0;

        for (int i = 0; i < lineText.length(); ) {
            if (lineText.charAt(i) == '§' && i + 1 < lineText.length()) {
                char code = lineText.charAt(i + 1);
                if (code == 'r' || code == 'R') activeStyle.setLength(0);
                else activeStyle.append('§').append(code);
                i += 2;
                continue;
            }

            int codePoint = lineText.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String chStr = lineText.substring(i, i + charCount);
            int chWidth = LcdFontHelper.getWidth(this.font, chStr, this.fontStyle);

            float waveY = (float) (Math.sin((timeMs * freq) + (charIdx * 0.45)) * 1.8f);
            String toRender = activeStyle.isEmpty() ? chStr : (activeStyle + chStr);

            guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(toRender, this.fontStyle), (int) ((x / scale) + curX), (int) (baseY + waveY), 0xFF000000 | renderCol, glow);

            curX += chWidth;
            charIdx++;
            i += charCount;
        }

        guiGraphics.pose().popPose();
    }

    private void renderPreviewRainbowLine(GuiGraphics guiGraphics, String lineText, float startX,
                                          int x, int y, float scale,
                                          boolean glow, int speed, long timeMs) {
        double flowSpeed = switch (speed) {
            case 1 -> 0.0008;
            case 2 -> 0.0016;
            case 3 -> 0.0028;
            case 4 -> 0.0045;
            default -> 0.0016;
        };

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        float curX = startX;
        float baseY = y / scale;
        StringBuilder activeStyle = new StringBuilder();
        int charIdx = 0;

        for (int i = 0; i < lineText.length(); ) {
            if (lineText.charAt(i) == '§' && i + 1 < lineText.length()) {
                char code = lineText.charAt(i + 1);
                if (code == 'r' || code == 'R') activeStyle.setLength(0);
                else activeStyle.append('§').append(code);
                i += 2;
                continue;
            }

            int codePoint = lineText.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String chStr = lineText.substring(i, i + charCount);
            int chWidth = LcdFontHelper.getWidth(this.font, chStr, this.fontStyle);

            float hue = (float) ((timeMs * flowSpeed + charIdx * 0.065) % 1.0);
            if (hue < 0) hue += 1.0f;
            int charCol = 0xFF000000 | com.boran.signbuilder.util.RainbowColorHelper.hsbToRgb(hue, 0.88f, 1.0f);

            String toRender = activeStyle.isEmpty() ? chStr : (activeStyle + chStr);
            guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(toRender, this.fontStyle), (int) ((x / scale) + curX), (int) baseY, charCol, glow);

            curX += chWidth;
            charIdx++;
            i += charCount;
        }

        guiGraphics.pose().popPose();
    }


    private static class PillButton extends AbstractButton {
        private final Runnable onClick;
        private final BooleanSupplier isSelected;
        private final int activeBorderColor;
        private final int activeBgColor;

        public PillButton(int x, int y, int width, int height, Component message,
                          Runnable onClick, BooleanSupplier isSelected,
                          int activeBorderColor, int activeBgColor) {
            super(x, y, width, height, message);
            this.onClick = onClick;
            this.isSelected = isSelected;
            this.activeBorderColor = activeBorderColor;
            this.activeBgColor = activeBgColor;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = this.isSelected != null && this.isSelected.getAsBoolean();
            boolean hovered = this.isHovered();

            int bg = selected ? this.activeBgColor : (hovered ? 0xFF2A3646 : 0xFF171E27);
            int border = selected ? this.activeBorderColor : (hovered ? 0xFF5B6E84 : 0xFF2C3949);
            int textColor = selected ? 0xFFFFFFFF : (hovered ? 0xFFE2EBF5 : 0xFFA0B2C4);

            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);

            if (selected) {
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + 2, this.activeBorderColor);
            }

            drawAutoScaledButtonText(graphics, Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY(), this.width, this.height, textColor);
        }

        @Override
        public void onPress() {
            if (this.onClick != null) {
                this.onClick.run();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static class ColorSwatchButton extends AbstractButton {
        private final int colorHex;
        private final Runnable onClick;
        private final BooleanSupplier isSelected;

        public ColorSwatchButton(int x, int y, int width, int height, int colorHex, String colorKey, Runnable onClick, BooleanSupplier isSelected) {
            super(x, y, width, height, Component.empty());
            this.colorHex = colorHex;
            this.onClick = onClick;
            this.isSelected = isSelected;
            this.setTooltip(Tooltip.create(Component.translatable(colorKey).withStyle(Style.EMPTY.withColor(colorHex))));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = this.isSelected != null && this.isSelected.getAsBoolean();
            boolean hovered = this.isHovered();

            int border = selected ? 0xFFFFFFFF : (hovered ? 0xFF9EABB8 : 0xFF283444);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF0E131A);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);

            graphics.fill(this.getX() + 2, this.getY() + 2, this.getX() + this.width - 2, this.getY() + this.height - 2, 0xFF000000 | this.colorHex);

            if (selected) {
                graphics.renderOutline(this.getX() - 1, this.getY() - 1, this.width + 2, this.height + 2, 0xFFFFFFFF);
            }
        }

        @Override
        public void onPress() {
            if (this.onClick != null) {
                this.onClick.run();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static class ModernActionButton extends AbstractButton {
        private final Runnable onClick;
        private final int baseBg;
        private final int border;
        private final int textColor;

        public ModernActionButton(int x, int y, int width, int height, Component message,
                                  Runnable onClick, int baseBg, int border, int textColor) {
            super(x, y, width, height, message);
            this.onClick = onClick;
            this.baseBg = baseBg;
            this.border = border;
            this.textColor = textColor;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHovered();
            int bg = hovered ? (this.baseBg + 0x00151515) : this.baseBg;
            int bColor = hovered ? 0xFFFFFFFF : this.border;

            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, bColor);

            int textColor = hovered ? 0xFFFFFFFF : this.textColor;
            drawAutoScaledButtonText(graphics, Minecraft.getInstance().font, this.getMessage(), this.getX(), this.getY(), this.width, this.height, textColor);
        }

        @Override
        public void onPress() {
            if (this.onClick != null) {
                this.onClick.run();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static void drawAutoScaledButtonText(GuiGraphics graphics, Font font, Component message, int btnX, int btnY, int btnW, int btnH, int textColor) {
        int maxTextWidth = btnW - 6;
        int textWidth = font.width(message);

        if (textWidth > maxTextWidth && textWidth > 0) {
            float scale = (float) maxTextWidth / textWidth;
            graphics.pose().pushPose();
            float centerX = btnX + btnW / 2.0f;
            float centerY = btnY + btnH / 2.0f;
            graphics.pose().translate(centerX, centerY, 0.0f);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(font, message, -textWidth / 2, -font.lineHeight / 2, textColor, true);
            graphics.pose().popPose();
        } else {
            int textX = btnX + (btnW - textWidth) / 2;
            int textY = btnY + (btnH - font.lineHeight) / 2;
            graphics.drawString(font, message, textX, textY, textColor, true);
        }
    }
}
