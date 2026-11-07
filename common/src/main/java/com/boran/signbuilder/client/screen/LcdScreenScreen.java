package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import com.boran.signbuilder.block.entity.LcdTextFormatter;
import com.boran.signbuilder.client.LcdFontHelper;
import com.boran.signbuilder.client.render.LcdStorageRenderer;
import com.boran.signbuilder.client.render.LcdVisualAnimator;
import com.boran.signbuilder.network.LocateStorageItemC2SPacket;
import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.UpdateLcdScreenC2SPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.boran.signbuilder.util.RainbowColorHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

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
    private int widthBlocks = 1;
    private int heightBlocks = 1;
    private int storageMonitorMode = 0;
    private int storageStyle = 0;
    private int storageFilterMode = 0;
    private int storageFlowMode = 0;
    private final List<ItemStack> watchlist = new ArrayList<>();
    private String storageSearchQuery = "";
    private boolean pinFromInventoryMode = false;
    private int storageTab = 0;
    private int storageScrollRow = 0;

    private int mainMode = 0;
    private int textTab = 0;
    private int visualTab = 0;
    private int visualPage = 0;

    private boolean loopEnabled = false;
    private boolean loopShuffle = false;
    private int loopTransition = 1;
    private final List<LoopStep> loopSteps = new ArrayList<>();
    private int loopScrollRow = 0;
    private int loopSubView = 0;
    private int loopPickerTarget = -1;
    private int loopPickerPage = 0;
    private String loopAnimSearchQuery = "";
    private final List<Integer> filteredPickerAnims = new ArrayList<>();
    private EditBox loopAnimSearchField;

    private int editingDurStepIdx = -1;
    private String editingDurBuffer = "";
    private int editingDurBackup = 6;
    private boolean editingDurSelectAll = false;

    private int loopColorStepIdx = -1;
    private int stepPickerHex = 0x00FFCC;
    private float stepHue = 0.5f;
    private float stepSat = 1.0f;
    private float stepBri = 1.0f;
    private boolean draggingStepSatBri = false;
    private boolean draggingStepHue = false;
    private boolean updatingStepColorInputs = false;
    private EditBox stepRInput;
    private EditBox stepGInput;
    private EditBox stepBInput;
    private EditBox stepHexInput;
    private int loopTextStepIdx = -1;
    private int stepTextActiveLine = 0;
    private EditBox stepText1Input;
    private EditBox stepText2Input;

    private int previewFocusStepIdx = -1;
    private long previewFocusUntilMs = 0L;
    private long loopCopiedFlashUntilMs = 0L;
    private static LoopClipboard copiedLoopClipboard = null;

    private EditBox textField1;
    private EditBox textField2;
    private EditBox storageSearchField;
    private PillButton glowButton;

    private static final int PANEL_WIDTH = 356;
    private static final int PANEL_HEIGHT = 258;
    private static final int INSPECTOR_COLS = 8;
    private static final int INSPECTOR_VIS_ROWS = 3;
    private static final int LOOP_VIS_ROWS = 3;

    private static final int[] STEP_QUICK_SWATCHES = {
            0x00FFCC, 0x00E5FF, 0x3388FF, 0x00FF66,
            0xAAFF00, 0xFFD700, 0xFF8800, 0xFF3344,
            0xFF44AA, 0xCC44FF, 0x8866FF, 0xFFFFFF
    };

    private static final int[][] LOOP_PRESET_POOLS = {
            { 1, 2, 3, 4, 5, 6, 7, 8 },
            { 9, 10, 11, 12, 21, 22, 40 },
            { 13, 14, 15, 16, 17, 18, 19 },
            { 25, 26, 27, 28, 29, 30, 31, 32, 33, 34 },
            { 20, 35, 36, 37, 38, 39 },
            { 44, 45, 46, 50, 51, 52, 53, 54, 55 },
            { 23, 24, 42, 43, 47, 48, 49 },
            { 41, 56, 57, 58, 59, 60 },
            {}
    };

    private static final class LoopStep {
        int animId;
        int durationSec;
        int stepColor;
        String text1;
        String text2;
        int mode1;
        int mode2;
        int align;

        LoopStep(int animId, int durationSec, int stepColor, String text1, String text2, int mode1, int mode2, int align) {
            this.animId = Math.max(0, Math.min(60, animId));
            this.durationSec = Math.max(1, Math.min(999, durationSec));
            this.stepColor = (stepColor < 0) ? Math.max(-2, stepColor) : (stepColor & 0xFFFFFF);
            this.text1 = text1 != null ? text1 : "";
            this.text2 = text2 != null ? text2 : "";
            this.mode1 = (mode1 >= 0 && mode1 <= 9) ? mode1 : -1;
            this.mode2 = (mode2 >= 0 && mode2 <= 9) ? mode2 : -1;
            this.align = (align >= 0 && align <= 2) ? align : -1;
        }

        LoopStep(int animId, int durationSec, int stepColor) {
            this(animId, durationSec, stepColor, "", "", -1, -1, -1);
        }

        boolean hasCustomText() {
            return !text1.isEmpty() || !text2.isEmpty();
        }

        boolean hasCustomEffects() {
            return mode1 >= 0 || mode2 >= 0 || align >= 0;
        }

        LoopStep copy() {
            return new LoopStep(this.animId, this.durationSec, this.stepColor, this.text1, this.text2, this.mode1, this.mode2, this.align);
        }
    }

    private record LoopClipboard(boolean shuffle, int transition, List<LoopStep> steps) {}

    private record LoopRowLayout(int ry, int subY, int subH, int badgeX, int badgeW, int animX, int animW,
                                 int colX, int colW, int minusX, int minusW, int durX, int durW,
                                 int plusX, int plusW, int upX, int upW, int downX, int downW, int delX, int delW) {}

    private record InspectorEntry(ItemStack stack, String name, int count, int share, int rateHr, boolean pinned) {}

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

    private static final String[] LCD_MODE_KEYS = {
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

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR, int fontStyle, int visualAnim,
                           int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle,
                           int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems,
                           boolean loopEnabled, boolean loopShuffle, int loopTransition,
                           int[] loopAnims, int[] loopDurations, int[] loopColors,
                           String[] loopTexts1, String[] loopTexts2, int[] loopModes1, int[] loopModes2, int[] loopAligns) {
        this(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim,
                widthBlocks, heightBlocks, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems);
        this.loopEnabled = loopEnabled;
        this.loopShuffle = loopShuffle;
        this.loopTransition = Math.max(0, Math.min(9, loopTransition));
        this.loopSteps.clear();
        if (loopAnims != null) {
            int n = Math.min(LcdScreenBlockEntity.MAX_LOOP_STEPS, loopAnims.length);
            for (int i = 0; i < n; i++) {
                int a = Math.max(0, Math.min(60, loopAnims[i]));
                int d = (loopDurations != null && i < loopDurations.length) ? Math.max(1, Math.min(999, loopDurations[i])) : 6;
                int c = (loopColors != null && i < loopColors.length) ? loopColors[i] : -1;
                String t1 = (loopTexts1 != null && i < loopTexts1.length && loopTexts1[i] != null) ? loopTexts1[i] : "";
                String t2 = (loopTexts2 != null && i < loopTexts2.length && loopTexts2[i] != null) ? loopTexts2[i] : "";
                int m1 = (loopModes1 != null && i < loopModes1.length && loopModes1[i] >= 0 && loopModes1[i] <= 9) ? loopModes1[i] : -1;
                int m2 = (loopModes2 != null && i < loopModes2.length && loopModes2[i] >= 0 && loopModes2[i] <= 9) ? loopModes2[i] : -1;
                int al = (loopAligns != null && i < loopAligns.length && loopAligns[i] >= 0 && loopAligns[i] <= 2) ? loopAligns[i] : -1;
                this.loopSteps.add(new LoopStep(a, d, c, t1, t2, m1, m2, al));
            }
        }
        if (this.storageMonitorMode == 0 && this.loopEnabled && !this.loopSteps.isEmpty()) {
            this.mainMode = 1;
            this.visualTab = 1;
            if (this.visualAnim <= 0) {
                this.visualAnim = this.lastVisualAnim > 0 ? this.lastVisualAnim : 1;
            }
        }
    }

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR, int fontStyle, int visualAnim,
                           int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle,
                           int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems,
                           boolean loopEnabled, boolean loopShuffle, int loopTransition,
                           int[] loopAnims, int[] loopDurations, int[] loopColors) {
        this(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim,
                widthBlocks, heightBlocks, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems,
                loopEnabled, loopShuffle, loopTransition, loopAnims, loopDurations, loopColors,
                null, null, null, null, null);
    }

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR, int fontStyle, int visualAnim,
                           int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle,
                           int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems) {
        this(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim,
                widthBlocks, heightBlocks, storageMonitorMode, storageStyle);
        this.storageFilterMode = Math.max(0, Math.min(2, storageFilterMode));
        this.storageFlowMode = Math.max(0, Math.min(2, storageFlowMode));
        this.watchlist.clear();
        if (watchlistItems != null) {
            for (ItemStack st : watchlistItems) {
                if (st != null && !st.isEmpty()) {
                    this.watchlist.add(LcdScreenBlockEntity.cleanWatchlistCopy(st));
                    if (this.watchlist.size() >= LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS) {
                        break;
                    }
                }
            }
        }
    }

    public LcdScreenScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize, int color, boolean rainbow, boolean glow,
                           int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR, int fontStyle, int visualAnim,
                           int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle) {
        this(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim);
        this.widthBlocks = Math.max(1, widthBlocks);
        this.heightBlocks = Math.max(1, heightBlocks);
        this.storageMonitorMode = storageMonitorMode;
        this.storageStyle = storageStyle;
        if (storageMonitorMode == 1) {
            this.mainMode = 2;
        }
    }

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
        this.widgetTL = Math.max(0, Math.min(11, widgetTL));
        this.widgetTR = Math.max(0, Math.min(11, widgetTR));
        this.widgetBL = Math.max(0, Math.min(11, widgetBL));
        this.widgetBR = Math.max(0, Math.min(11, widgetBR));
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
        this.textField1 = null;
        this.textField2 = null;
        this.storageSearchField = null;
        this.loopAnimSearchField = null;
        this.stepRInput = null;
        this.stepGInput = null;
        this.stepBInput = null;
        this.stepHexInput = null;
        this.draggingStepSatBri = false;
        this.draggingStepHue = false;

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = (this.height - PANEL_HEIGHT) / 2;

        int mainModeY = panelY + 18;
        this.addRenderableWidget(new PillButton(
                panelX + 14,
                mainModeY,
                106,
                18,
                Component.translatable("gui.signbuilder.lcd.main_mode.text"),
                () -> {
                    if (this.mainMode != 0) {
                        syncInputsToState();
                        this.mainMode = 0;
                        this.visualAnim = 0;
                        this.storageMonitorMode = 0;
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> this.mainMode == 0,
                0xFF00FFCC,
                0xFF103438
        ));

        this.addRenderableWidget(new PillButton(
                panelX + 125,
                mainModeY,
                106,
                18,
                Component.translatable("gui.signbuilder.lcd.main_mode.visual"),
                () -> {
                    if (this.mainMode != 1) {
                        syncInputsToState();
                        this.mainMode = 1;
                        this.visualAnim = (this.lastVisualAnim > 0 ? this.lastVisualAnim : 1);
                        this.storageMonitorMode = 0;
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> this.mainMode == 1,
                0xFFFF00D4,
                0xFF3B1032
        ));

        this.addRenderableWidget(new PillButton(
                panelX + 236,
                mainModeY,
                106,
                18,
                Component.translatable("gui.signbuilder.lcd.main_mode.storage"),
                () -> {
                    if (this.mainMode != 2) {
                        syncInputsToState();
                        this.mainMode = 2;
                        this.visualAnim = 0;
                        this.storageMonitorMode = 1;
                        this.clearWidgets();
                        this.init();
                    }
                },
                () -> this.mainMode == 2,
                0xFFFFAA00,
                0xFF3A2510
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
        } else if (this.mainMode == 1) {
            this.addRenderableWidget(new PillButton(
                    panelX + 14, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.anims"),
                    () -> switchVisualTab(0),
                    () -> this.visualTab == 0,
                    0xFFFFAA00, 0xFF3A2510
            ));
            this.addRenderableWidget(new DynamicPillButton(
                    panelX + 125, subTabY, 106, 17,
                    this::getLoopSubTabLabel,
                    () -> switchVisualTab(1),
                    () -> this.visualTab == 1,
                    0xFF00E5FF, 0xFF102E3B
            ));
            this.addRenderableWidget(new PillButton(
                    panelX + 236, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.theme_color"),
                    () -> switchVisualTab(2),
                    () -> this.visualTab == 2,
                    0xFFFF00D4, 0xFF3B1032
            ));

            if (this.visualTab == 0) {
                initVisualAnimationsTab(panelX, panelY);
            } else if (this.visualTab == 1) {
                initVisualLoopTab(panelX, panelY);
            } else {
                initThemeAndColorTab(panelX, panelY);
            }
        } else {
            this.addRenderableWidget(new PillButton(
                    panelX + 14, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.storage.tab.finder"),
                    () -> switchStorageTab(0),
                    () -> this.storageTab == 0,
                    0xFF00E5FF, 0xFF102E3B
            ));
            this.addRenderableWidget(new PillButton(
                    panelX + 125, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.storage.tab.layout"),
                    () -> switchStorageTab(1),
                    () -> this.storageTab == 1,
                    0xFFFFAA00, 0xFF3A2510
            ));
            this.addRenderableWidget(new PillButton(
                    panelX + 236, subTabY, 106, 17,
                    Component.translatable("gui.signbuilder.lcd.tab.theme_color"),
                    () -> switchStorageTab(2),
                    () -> this.storageTab == 2,
                    0xFFFF00D4, 0xFF3B1032
            ));

            if (this.storageTab == 0) {
                initStorageFinderTab(panelX, panelY);
            } else if (this.storageTab == 1) {
                initStorageMonitorTab(panelX, panelY);
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
        if (this.visualTab != newTab || this.loopSubView != 0) {
            syncInputsToState();
            syncStepTextInputsToState();
            this.visualTab = newTab;
            this.loopSubView = 0;
            this.loopPickerTarget = -1;
            this.loopColorStepIdx = -1;
            this.loopTextStepIdx = -1;
            this.clearWidgets();
            this.init();
        }
    }

    private void switchStorageTab(int newTab) {
        if (this.storageTab != newTab) {
            syncInputsToState();
            this.storageTab = newTab;
            this.clearWidgets();
            this.init();
        }
    }

    private void initStorageFinderTab(int panelX, int panelY) {
        int barY = panelY + 112;

        this.storageSearchField = new EditBox(
                this.font,
                panelX + 35,
                barY + 5,
                187,
                10,
                Component.translatable("gui.signbuilder.lcd.storage.search_hint")
        );
        this.storageSearchField.setMaxLength(64);
        this.storageSearchField.setBordered(false);
        this.storageSearchField.setHint(Component.translatable("gui.signbuilder.lcd.storage.search_hint").withStyle(ChatFormatting.DARK_GRAY));
        this.storageSearchField.setValue(this.storageSearchQuery);
        this.storageSearchField.setResponder(s -> {
            this.storageSearchQuery = s;
            this.storageScrollRow = 0;
        });
        this.storageSearchField.setTextColor(0xFFFFFFFF);
        this.addRenderableWidget(this.storageSearchField);

        ModernActionButton clearSearchBtn = new ModernActionButton(
                panelX + 229,
                barY,
                18,
                18,
                Component.literal("✕"),
                () -> {
                    if (this.storageSearchQuery != null && !this.storageSearchQuery.isEmpty()) {
                        this.storageSearchQuery = "";
                        if (this.storageSearchField != null) {
                            this.storageSearchField.setValue("");
                        }
                        this.storageScrollRow = 0;
                    } else if (Screen.hasShiftDown() && !this.watchlist.isEmpty()) {
                        this.watchlist.clear();
                    }
                },
                0xFF2A1C24,
                0xFFFF4466,
                0xFFFFAABB
        );
        clearSearchBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.storage.clear_btn.tooltip")));
        this.addRenderableWidget(clearSearchBtn);

        PillButton invBtn = new PillButton(
                panelX + 250,
                barY,
                92,
                18,
                Component.translatable("gui.signbuilder.lcd.storage.inv_btn"),
                () -> {
                    this.pinFromInventoryMode = !this.pinFromInventoryMode;
                    this.storageScrollRow = 0;
                },
                () -> this.pinFromInventoryMode,
                0xFFFFCC22,
                0xFF3A2B0E
        );
        invBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.storage.inv_btn.tooltip")));
        this.addRenderableWidget(invBtn);
    }

    private void initStorageMonitorTab(int panelX, int panelY) {
        String[] styleIcons = { "▦", "❖", "▤", "⇄", "◎", "≈" };
        int tileStartX = panelX + 21;
        int tileW = 102;
        int tileGap = 4;
        int row1Y = panelY + 123;
        int row2Y = panelY + 142;

        for (int i = 0; i < 6; i++) {
            final int s = i;
            int col = i % 3;
            int rowY = (i < 3) ? row1Y : row2Y;
            String labelKey = "gui.signbuilder.lcd.storage.style." + i;
            String tipKey = "gui.signbuilder.lcd.storage.style." + i + ".tooltip";

            StyleTileButton styleBtn = new StyleTileButton(
                    tileStartX + col * (tileW + tileGap), rowY, tileW, 17,
                    styleIcons[i],
                    Component.translatable(labelKey),
                    () -> this.storageStyle = s,
                    () -> this.storageStyle == s
            );
            styleBtn.setTooltip(Tooltip.create(Component.translatable(tipKey)));
            this.addRenderableWidget(styleBtn);
        }

        int[] filterSegX = { panelX + 21, panelX + 71, panelX + 120 };
        int[] segW = { 50, 49, 50 };
        int segY = panelY + 181;

        for (int i = 0; i < 3; i++) {
            final int fm = i;
            String fKey = "gui.signbuilder.lcd.storage.filter_short." + i;
            String fTip = "gui.signbuilder.lcd.storage.filter." + i + ".tooltip";
            int accent = (i == 0) ? 0xFF4CE0FF : 0xFFFFCC22;
            int fill = (i == 0) ? 0xFF122C38 : 0xFF3D2C0B;
            SegmentedOptionButton filterBtn = new SegmentedOptionButton(
                    filterSegX[i], segY, segW[i], 19, i,
                    Component.translatable(fKey),
                    () -> this.storageFilterMode = fm,
                    () -> this.storageFilterMode == fm,
                    accent, fill
            );
            filterBtn.setTooltip(Tooltip.create(Component.translatable(fTip)));
            this.addRenderableWidget(filterBtn);
        }

        int[] flowSegX = { panelX + 188, panelX + 238, panelX + 287 };
        for (int i = 0; i < 3; i++) {
            final int flm = i;
            String flKey = "gui.signbuilder.lcd.storage.flow_short." + i;
            String flTip = "gui.signbuilder.lcd.storage.flow." + i + ".tooltip";
            int accent = (i == 0) ? 0xFF8AA0B8 : 0xFF00FF88;
            int fill = (i == 0) ? 0xFF1D2733 : 0xFF0E3624;
            SegmentedOptionButton flowBtn = new SegmentedOptionButton(
                    flowSegX[i], segY, segW[i], 19, i,
                    Component.translatable(flKey),
                    () -> this.storageFlowMode = flm,
                    () -> this.storageFlowMode == flm,
                    accent, fill
            );
            flowBtn.setTooltip(Tooltip.create(Component.translatable(flTip)));
            this.addRenderableWidget(flowBtn);
        }
    }

    private void syncInputsToState() {
        commitDurationEdit(false);
        if (this.textField1 != null) this.line1 = this.textField1.getValue();
        if (this.textField2 != null) this.line2 = this.textField2.getValue();
        if (this.storageSearchField != null) this.storageSearchQuery = this.storageSearchField.getValue();
        if (this.loopAnimSearchField != null) this.loopAnimSearchQuery = this.loopAnimSearchField.getValue();
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
            case 11 -> "gui.signbuilder.lcd.widget.storage";
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
            AnimTileButton btn = new AnimTileButton(
                    colX[col], by, colW, 16,
                    Component.translatable("gui.signbuilder.lcd.visual." + v),
                    () -> {
                        this.visualAnim = v;
                        this.lastVisualAnim = v;
                        this.loopEnabled = false;
                    },
                    () -> toggleAnimInLoop(v),
                    () -> !this.loopEnabled && this.visualAnim == v,
                    () -> getLoopBadgeForAnim(v),
                    0xFFFFAA00, 0xFF3A2510
            );
            Component tip = Component.translatable("gui.signbuilder.lcd.visual." + v + ".tooltip")
                    .copy()
                    .append(Component.literal("\n"))
                    .append(Component.translatable("gui.signbuilder.lcd.loop.anim_tile_hint").withStyle(ChatFormatting.AQUA));
            btn.setTooltip(Tooltip.create(tip));
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

    private void initVisualLoopTab(int panelX, int panelY) {
        if (this.loopSubView == 0) {
            int row1Y = panelY + 111;
            DynamicPillButton loopPowerBtn = new DynamicPillButton(
                    panelX + 14, row1Y, 84, 17,
                    () -> Component.translatable(this.loopEnabled ? "gui.signbuilder.lcd.loop.on" : "gui.signbuilder.lcd.loop.off"),
                    () -> {
                        commitDurationEdit(false);
                        this.loopEnabled = !this.loopEnabled;
                        if (this.loopEnabled && this.loopSteps.isEmpty()) {
                            int defAnim = this.visualAnim > 0 ? this.visualAnim : (this.lastVisualAnim > 0 ? this.lastVisualAnim : 1);
                            this.loopSteps.add(new LoopStep(defAnim, 6, -1));
                        }
                    },
                    null,
                    () -> this.loopEnabled && !this.loopSteps.isEmpty(),
                    null,
                    0xFF00FF88, 0xFF0E3322
            );
            loopPowerBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.toggle.tooltip")));
            this.addRenderableWidget(loopPowerBtn);

            SegmentedOptionButton seqBtn = new SegmentedOptionButton(
                    panelX + 101, row1Y, 48, 17, 0,
                    Component.translatable("gui.signbuilder.lcd.loop.order.seq"),
                    () -> {
                        commitDurationEdit(false);
                        this.loopShuffle = false;
                    },
                    () -> !this.loopShuffle,
                    0xFF00E5FF, 0xFF0E2C3A
            );
            seqBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.order.seq.tooltip")));
            this.addRenderableWidget(seqBtn);

            SegmentedOptionButton shufBtn = new SegmentedOptionButton(
                    panelX + 149, row1Y, 48, 17, 2,
                    Component.translatable("gui.signbuilder.lcd.loop.order.shuffle"),
                    () -> {
                        commitDurationEdit(false);
                        this.loopShuffle = true;
                    },
                    () -> this.loopShuffle,
                    0xFFFFAA00, 0xFF3A2510
            );
            shufBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.order.shuffle.tooltip")));
            this.addRenderableWidget(shufBtn);

            DynamicPillButton transBtn = new DynamicPillButton(
                    panelX + 200, row1Y, 98, 17,
                    () -> Component.translatable("gui.signbuilder.lcd.loop.trans." + this.loopTransition),
                    () -> {
                        commitDurationEdit(false);
                        this.loopTransition = (this.loopTransition + 1) % 10;
                    },
                    () -> {
                        commitDurationEdit(false);
                        this.loopTransition = (this.loopTransition + 9) % 10;
                    },
                    () -> this.loopTransition > 0,
                    null,
                    0xFFD966FF, 0xFF2B1338
            );
            transBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.trans.tooltip")));
            this.addRenderableWidget(transBtn);

            DynamicPillButton copyBtn = new DynamicPillButton(
                    panelX + 301, row1Y, 19, 17,
                    () -> Component.literal(System.currentTimeMillis() < this.loopCopiedFlashUntilMs ? "✓" : "⧉"),
                    () -> {
                        commitDurationEdit(false);
                        copyLoopToClipboard();
                    },
                    null,
                    () -> System.currentTimeMillis() < this.loopCopiedFlashUntilMs,
                    () -> !this.loopSteps.isEmpty(),
                    0xFF00FF88, 0xFF0E3322
            );
            copyBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.copy.tooltip")));
            this.addRenderableWidget(copyBtn);

            DynamicPillButton pasteBtn = new DynamicPillButton(
                    panelX + 323, row1Y, 19, 17,
                    () -> Component.literal("⇩"),
                    () -> {
                        commitDurationEdit(false);
                        pasteLoopFromClipboard();
                    },
                    null,
                    () -> false,
                    () -> copiedLoopClipboard != null && !copiedLoopClipboard.steps().isEmpty(),
                    0xFF00E5FF, 0xFF102E3B
            );
            pasteBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.paste.tooltip")));
            this.addRenderableWidget(pasteBtn);

            int row2Y = panelY + 130;
            PillButton addAnimBtn = new PillButton(
                    panelX + 14, row2Y, 90, 16,
                    Component.translatable("gui.signbuilder.lcd.loop.btn.add_anim"),
                    () -> {
                        commitDurationEdit(false);
                        this.loopPickerTarget = -2;
                        this.loopPickerPage = 0;
                        this.loopAnimSearchQuery = "";
                        this.loopSubView = 1;
                        rebuildFilteredPickerAnims();
                        this.clearWidgets();
                        this.init();
                    },
                    () -> false,
                    0xFF00E5FF, 0xFF102E3B
            );
            addAnimBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.add_anim.tooltip")));
            this.addRenderableWidget(addAnimBtn);

            PillButton presetsBtn = new PillButton(
                    panelX + 107, row2Y, 88, 16,
                    Component.translatable("gui.signbuilder.lcd.loop.btn.presets"),
                    () -> {
                        commitDurationEdit(false);
                        this.loopSubView = 2;
                        this.clearWidgets();
                        this.init();
                    },
                    () -> false,
                    0xFFFFAA00, 0xFF3A2510
            );
            presetsBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.presets.tooltip")));
            this.addRenderableWidget(presetsBtn);

            PillButton addTextBtn = new PillButton(
                    panelX + 198, row2Y, 78, 16,
                    Component.translatable("gui.signbuilder.lcd.loop.btn.add_text"),
                    () -> {
                        commitDurationEdit(false);
                        if (this.loopSteps.size() < LcdScreenBlockEntity.MAX_LOOP_STEPS) {
                            this.loopSteps.add(new LoopStep(0, getDefaultStepDuration(), -1));
                            this.loopEnabled = true;
                            this.loopScrollRow = Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS);
                            openStepTextStudio(this.loopSteps.size() - 1);
                        }
                    },
                    () -> false,
                    0xFF00FFCC, 0xFF0F2E28
            );
            addTextBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.add_text.tooltip")));
            this.addRenderableWidget(addTextBtn);

            PillButton clearBtn = new PillButton(
                    panelX + 279, row2Y, 63, 16,
                    Component.translatable("gui.signbuilder.lcd.loop.btn.clear"),
                    () -> {
                        commitDurationEdit(false);
                        this.loopSteps.clear();
                        this.loopEnabled = false;
                        this.loopScrollRow = 0;
                    },
                    () -> false,
                    0xFFFF4455, 0xFF36121A
            );
            clearBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.clear.tooltip")));
            this.addRenderableWidget(clearBtn);
        } else if (this.loopSubView == 1) {
            rebuildFilteredPickerAnims();
            int headY = panelY + 111;

            PillButton textStepBtn = new PillButton(
                    panelX + 14, headY, 88, 17,
                    Component.translatable("gui.signbuilder.lcd.loop.text_step"),
                    () -> {
                        if (this.loopPickerTarget >= 0 && this.loopPickerTarget < this.loopSteps.size()) {
                            int target = this.loopPickerTarget;
                            this.loopPickerTarget = -1;
                            openStepTextStudio(target);
                        } else if (Screen.hasShiftDown()) {
                            if (this.loopSteps.size() < LcdScreenBlockEntity.MAX_LOOP_STEPS) {
                                this.loopSteps.add(new LoopStep(0, getDefaultStepDuration(), -1));
                                this.loopEnabled = true;
                                openStepTextStudio(this.loopSteps.size() - 1);
                            }
                        } else {
                            if (this.loopSteps.size() < LcdScreenBlockEntity.MAX_LOOP_STEPS) {
                                this.loopSteps.add(new LoopStep(0, getDefaultStepDuration(), -1));
                                this.loopEnabled = true;
                                openStepTextStudio(this.loopSteps.size() - 1);
                            }
                        }
                    },
                    () -> (this.loopPickerTarget >= 0 && this.loopPickerTarget < this.loopSteps.size())
                            ? (this.loopSteps.get(this.loopPickerTarget).animId == 0)
                            : hasAnimInLoop(0),
                    0xFF00FFCC, 0xFF0F2E28
            );
            textStepBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.btn.add_text.tooltip")));
            this.addRenderableWidget(textStepBtn);

            this.loopAnimSearchField = new EditBox(
                    this.font,
                    panelX + 123,
                    headY + 5,
                    112,
                    10,
                    Component.translatable("gui.signbuilder.lcd.loop.search_hint")
            );
            this.loopAnimSearchField.setMaxLength(48);
            this.loopAnimSearchField.setBordered(false);
            this.loopAnimSearchField.setHint(Component.translatable("gui.signbuilder.lcd.loop.search_hint").withStyle(ChatFormatting.DARK_GRAY));
            this.loopAnimSearchField.setValue(this.loopAnimSearchQuery);
            this.loopAnimSearchField.setResponder(s -> {
                this.loopAnimSearchQuery = s;
                this.loopPickerPage = 0;
                rebuildFilteredPickerAnims();
            });
            this.loopAnimSearchField.setTextColor(0xFFFFFFFF);
            this.addRenderableWidget(this.loopAnimSearchField);

            DynamicPillButton doneBtn = new DynamicPillButton(
                    panelX + 256, headY, 86, 17,
                    () -> Component.translatable("gui.signbuilder.lcd.loop.btn.done", this.loopSteps.size()),
                    () -> {
                        this.loopPickerTarget = -1;
                        this.loopSubView = 0;
                        this.clearWidgets();
                        this.init();
                    },
                    null,
                    () -> true,
                    null,
                    0xFF00FF88, 0xFF0E3322
            );
            this.addRenderableWidget(doneBtn);

            int[] colX = { panelX + 14, panelX + 97, panelX + 180, panelX + 263 };
            int colW = 79;
            int row1Y = panelY + 131;
            int row2Y = panelY + 149;
            int row3Y = panelY + 167;

            for (int slot = 0; slot < 12; slot++) {
                final int slotIdx = slot;
                int col = slot % 4;
                int by = (slot < 4) ? row1Y : ((slot < 8) ? row2Y : row3Y);
                DynamicAnimTileButton btn = new DynamicAnimTileButton(
                        colX[col], by, colW, 16,
                        () -> getPickerSlotAnim(slotIdx),
                        v -> {
                            if (this.loopPickerTarget >= 0 && this.loopPickerTarget < this.loopSteps.size()) {
                                this.loopSteps.get(this.loopPickerTarget).animId = v;
                                this.loopPickerTarget = -1;
                                this.loopSubView = 0;
                                this.clearWidgets();
                                this.init();
                            } else if (Screen.hasShiftDown()) {
                                if (this.loopSteps.size() < LcdScreenBlockEntity.MAX_LOOP_STEPS) {
                                    this.loopSteps.add(new LoopStep(v, getDefaultStepDuration(), -1));
                                    this.loopEnabled = true;
                                }
                            } else {
                                toggleAnimInLoop(v);
                            }
                        },
                        v -> {
                            if (this.loopPickerTarget >= 0 && this.loopPickerTarget < this.loopSteps.size()) {
                                this.loopSteps.get(this.loopPickerTarget).animId = v;
                                this.loopPickerTarget = -1;
                                this.loopSubView = 0;
                                this.clearWidgets();
                                this.init();
                            } else {
                                toggleAnimInLoop(v);
                            }
                        },
                        v -> (this.loopPickerTarget >= 0 && this.loopPickerTarget < this.loopSteps.size())
                                ? (this.loopSteps.get(this.loopPickerTarget).animId == v)
                                : hasAnimInLoop(v),
                        this::getLoopBadgeForAnim,
                        0xFF00E5FF, 0xFF102E3B
                );
                this.addRenderableWidget(btn);
            }

            int pageY = panelY + 186;
            DynamicPillButton prevBtn = new DynamicPillButton(
                    panelX + 14, pageY, 44, 15,
                    () -> Component.translatable("gui.signbuilder.lcd.prev_page"),
                    () -> {
                        if (this.loopPickerPage > 0) {
                            this.loopPickerPage--;
                        }
                    },
                    null,
                    () -> false,
                    () -> this.loopPickerPage > 0,
                    0xFF00E5FF, 0xFF102E3B
            );
            this.addRenderableWidget(prevBtn);

            this.addRenderableWidget(new DynamicPillButton(
                    panelX + 62, pageY, 232, 15,
                    () -> {
                        int totalPages = Math.max(1, (this.filteredPickerAnims.size() + 11) / 12);
                        int curPage = Math.min(totalPages, this.loopPickerPage + 1);
                        return Component.translatable("gui.signbuilder.lcd.page", curPage, totalPages);
                    },
                    () -> {},
                    null,
                    () -> true,
                    null,
                    0xFF00E5FF, 0xFF141C25
            ));

            DynamicPillButton nextBtn = new DynamicPillButton(
                    panelX + 298, pageY, 44, 15,
                    () -> Component.translatable("gui.signbuilder.lcd.next_page"),
                    () -> {
                        int totalPages = Math.max(1, (this.filteredPickerAnims.size() + 11) / 12);
                        if (this.loopPickerPage < totalPages - 1) {
                            this.loopPickerPage++;
                        }
                    },
                    null,
                    () -> false,
                    () -> this.loopPickerPage < Math.max(1, (this.filteredPickerAnims.size() + 11) / 12) - 1,
                    0xFF00E5FF, 0xFF102E3B
            );
            this.addRenderableWidget(nextBtn);
        } else if (this.loopSubView == 2) {
            int headY = panelY + 111;
            DynamicPillButton backBtn = new DynamicPillButton(
                    panelX + 256, headY, 86, 17,
                    () -> Component.translatable("gui.signbuilder.lcd.loop.btn.done", this.loopSteps.size()),
                    () -> {
                        this.loopSubView = 0;
                        this.clearWidgets();
                        this.init();
                    },
                    null,
                    () -> true,
                    null,
                    0xFF00FF88, 0xFF0E3322
            );
            this.addRenderableWidget(backBtn);

            int[] colX = { panelX + 14, panelX + 125, panelX + 236 };
            int colW = 106;
            int[] rowY = { panelY + 131, panelY + 152, panelY + 173 };
            int[] accents = {
                    0xFFFFAA00, 0xFF00E5FF, 0xFF00FF88,
                    0xFFFF55CC, 0xFFB866FF, 0xFF33CCFF,
                    0xFFFF4455, 0xFF66EE66, 0xFFFFD700
            };
            int[] bgs = {
                    0xFF36230E, 0xFF0E2B38, 0xFF0E3322,
                    0xFF36132B, 0xFF28153B, 0xFF102A3A,
                    0xFF381218, 0xFF143018, 0xFF362C0E
            };

            for (int p = 0; p < 9; p++) {
                final int presetIdx = p;
                int c = p % 3;
                int r = p / 3;
                PillButton pBtn = new PillButton(
                        colX[c], rowY[r], colW, 19,
                        Component.translatable("gui.signbuilder.lcd.loop.preset." + p),
                        () -> applyPresetPool(presetIdx, Screen.hasShiftDown()),
                        () -> false,
                        accents[p], bgs[p]
                );
                pBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.preset." + p + ".tooltip")));
                this.addRenderableWidget(pBtn);
            }
        } else if (this.loopSubView == 3) {
            if (this.loopColorStepIdx < 0 || this.loopColorStepIdx >= this.loopSteps.size()) {
                this.loopSubView = 0;
                this.loopColorStepIdx = -1;
                initVisualLoopTab(panelX, panelY);
                return;
            }
            int headY = panelY + 111;
            DynamicPillButton autoColBtn = new DynamicPillButton(
                    panelX + 14, headY, 74, 17,
                    () -> Component.translatable("gui.signbuilder.lcd.loop.col_mode.auto"),
                    () -> {
                        LoopStep st = getEditingColorStep();
                        if (st != null) st.stepColor = -1;
                    },
                    null,
                    () -> {
                        LoopStep st = getEditingColorStep();
                        return st != null && st.stepColor == -1;
                    },
                    null,
                    0xFF00E5FF, 0xFF102E3B
            );
            this.addRenderableWidget(autoColBtn);

            DynamicPillButton rainbowColBtn = new DynamicPillButton(
                    panelX + 91, headY, 80, 17,
                    () -> Component.translatable("gui.signbuilder.lcd.loop.col_mode.rainbow"),
                    () -> {
                        LoopStep st = getEditingColorStep();
                        if (st != null) st.stepColor = -2;
                    },
                    null,
                    () -> {
                        LoopStep st = getEditingColorStep();
                        return st != null && st.stepColor == -2;
                    },
                    null,
                    0xFFFF44CC, 0xFF36122E
            );
            this.addRenderableWidget(rainbowColBtn);

            PillButton applyAllBtn = new PillButton(
                    panelX + 174, headY, 84, 17,
                    Component.translatable("gui.signbuilder.lcd.loop.col_mode.apply_all"),
                    () -> {
                        LoopStep st = getEditingColorStep();
                        if (st != null) {
                            for (LoopStep s : this.loopSteps) {
                                s.stepColor = st.stepColor;
                            }
                        }
                    },
                    () -> false,
                    0xFFFFAA00, 0xFF3A2510
            );
            applyAllBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.col_mode.apply_all.tooltip")));
            this.addRenderableWidget(applyAllBtn);

            PillButton doneColBtn = new PillButton(
                    panelX + 261, headY, 81, 17,
                    Component.translatable("gui.signbuilder.color_picker.done"),
                    () -> {
                        this.loopSubView = 0;
                        this.loopColorStepIdx = -1;
                        this.clearWidgets();
                        this.init();
                    },
                    () -> true,
                    0xFF00FF88, 0xFF0E3322
            );
            this.addRenderableWidget(doneColBtn);

            int cardX = panelX + 14;
            int cardY = panelY + 131;
            int rVal = (this.stepPickerHex >> 16) & 0xFF;
            int gVal = (this.stepPickerHex >> 8) & 0xFF;
            int bVal = this.stepPickerHex & 0xFF;

            this.stepRInput = new EditBox(this.font, cardX + 151, cardY + 12, 36, 10, Component.literal("R"));
            this.stepRInput.setMaxLength(3);
            this.stepRInput.setFilter(s -> s.matches("\\d{0,3}"));
            this.stepRInput.setBordered(false);
            this.stepRInput.setValue(String.valueOf(rVal));
            this.stepRInput.setTextColor(0xFFFFFFFF);
            this.stepRInput.setResponder(s -> onStepRgbTyped());
            this.addRenderableWidget(this.stepRInput);

            this.stepGInput = new EditBox(this.font, cardX + 151, cardY + 32, 36, 10, Component.literal("G"));
            this.stepGInput.setMaxLength(3);
            this.stepGInput.setFilter(s -> s.matches("\\d{0,3}"));
            this.stepGInput.setBordered(false);
            this.stepGInput.setValue(String.valueOf(gVal));
            this.stepGInput.setTextColor(0xFFFFFFFF);
            this.stepGInput.setResponder(s -> onStepRgbTyped());
            this.addRenderableWidget(this.stepGInput);

            this.stepBInput = new EditBox(this.font, cardX + 151, cardY + 52, 36, 10, Component.literal("B"));
            this.stepBInput.setMaxLength(3);
            this.stepBInput.setFilter(s -> s.matches("\\d{0,3}"));
            this.stepBInput.setBordered(false);
            this.stepBInput.setValue(String.valueOf(bVal));
            this.stepBInput.setTextColor(0xFFFFFFFF);
            this.stepBInput.setResponder(s -> onStepRgbTyped());
            this.addRenderableWidget(this.stepBInput);

            this.stepHexInput = new EditBox(this.font, cardX + 151, cardY + 73, 54, 10, Component.literal("HEX"));
            this.stepHexInput.setMaxLength(6);
            this.stepHexInput.setFilter(s -> s.matches("[0-9a-fA-F]{0,6}"));
            this.stepHexInput.setBordered(false);
            this.stepHexInput.setValue(String.format(Locale.ROOT, "%06X", this.stepPickerHex & 0xFFFFFF));
            this.stepHexInput.setTextColor(0xFFFFFFFF);
            this.stepHexInput.setResponder(this::onStepHexTyped);
            this.addRenderableWidget(this.stepHexInput);
        } else if (this.loopSubView == 4) {
            LoopStep curStep = getEditingTextStep();
            if (curStep == null) {
                this.loopSubView = 0;
                this.loopTextStepIdx = -1;
                initVisualLoopTab(panelX, panelY);
                return;
            }
            int headY = panelY + 111;

            DynamicPillButton backBtn = new DynamicPillButton(
                    panelX + 14, headY, 96, 17,
                    () -> Component.literal("← #" + (this.loopTextStepIdx + 1) + " ").append(Component.translatable("gui.signbuilder.lcd.loop.text_studio.back")),
                    () -> {
                        syncStepTextInputsToState();
                        this.loopSubView = 0;
                        this.loopTextStepIdx = -1;
                        this.clearWidgets();
                        this.init();
                    },
                    null,
                    () -> false,
                    null,
                    0xFF00E5FF, 0xFF102E3B
            );
            backBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.text_studio.back.tooltip")));
            this.addRenderableWidget(backBtn);

            PillButton resetBtn = new PillButton(
                    panelX + 114, headY, 72, 17,
                    Component.translatable("gui.signbuilder.lcd.loop.text_studio.reset"),
                    () -> {
                        LoopStep st = getEditingTextStep();
                        if (st != null) {
                            st.text1 = "";
                            st.text2 = "";
                            st.mode1 = -1;
                            st.mode2 = -1;
                            st.align = -1;
                            if (this.stepText1Input != null) this.stepText1Input.setValue("");
                            if (this.stepText2Input != null) this.stepText2Input.setValue("");
                        }
                    },
                    () -> false,
                    0xFFFF8844, 0xFF351B0F
            );
            resetBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.text_studio.reset.tooltip")));
            this.addRenderableWidget(resetBtn);

            DynamicPillButton lineToggleBtn = new DynamicPillButton(
                    panelX + 190, headY, 72, 17,
                    () -> Component.translatable(this.stepTextActiveLine == 0 ? "gui.signbuilder.lcd.loop.text_studio.active_l1" : "gui.signbuilder.lcd.loop.text_studio.active_l2"),
                    () -> {
                        this.stepTextActiveLine = (this.stepTextActiveLine == 0) ? 1 : 0;
                        if (this.stepTextActiveLine == 0 && this.stepText1Input != null) {
                            this.setFocused(this.stepText1Input);
                        } else if (this.stepTextActiveLine == 1 && this.stepText2Input != null) {
                            this.setFocused(this.stepText2Input);
                        }
                    },
                    null,
                    () -> false,
                    null,
                    0xFF00FFCC, 0xFF0E3029
            );
            lineToggleBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.text_studio.active_line.tooltip")));
            this.addRenderableWidget(lineToggleBtn);

            PillButton doneBtn = new PillButton(
                    panelX + 266, headY, 76, 17,
                    Component.translatable("gui.signbuilder.color_picker.done"),
                    () -> {
                        syncStepTextInputsToState();
                        this.loopSubView = 0;
                        this.loopTextStepIdx = -1;
                        this.clearWidgets();
                        this.init();
                    },
                    () -> true,
                    0xFF00FF88, 0xFF0E3322
            );
            this.addRenderableWidget(doneBtn);

            int row1Y = panelY + 132;
            DynamicPillButton l1Btn = new DynamicPillButton(
                    panelX + 14, row1Y, 28, 18,
                    () -> Component.literal("L1"),
                    () -> {
                        this.stepTextActiveLine = 0;
                        if (this.stepText1Input != null) this.setFocused(this.stepText1Input);
                    },
                    null,
                    () -> this.stepTextActiveLine == 0,
                    null,
                    0xFF00FFCC, 0xFF0E3029
            );
            this.addRenderableWidget(l1Btn);

            this.stepText1Input = new EditBox(
                    this.font,
                    panelX + 50,
                    row1Y + 5,
                    160,
                    10,
                    Component.translatable("gui.signbuilder.lcd.loop.text_studio.line1_hint")
            );
            this.stepText1Input.setMaxLength(128);
            this.stepText1Input.setBordered(false);
            this.stepText1Input.setHint(Component.translatable("gui.signbuilder.lcd.loop.text_studio.line1_hint").withStyle(ChatFormatting.DARK_GRAY));
            this.stepText1Input.setValue(curStep.text1);
            this.stepText1Input.setResponder(s -> {
                LoopStep st = getEditingTextStep();
                if (st != null) st.text1 = s;
            });
            this.stepText1Input.setTextColor(0xFFFFFFFF);
            this.addRenderableWidget(this.stepText1Input);

            ModernActionButton clearBtn1 = new ModernActionButton(
                    panelX + 218, row1Y, 16, 18, Component.literal("✕"),
                    () -> {
                        curStep.text1 = "";
                        if (this.stepText1Input != null) this.stepText1Input.setValue("");
                        this.stepTextActiveLine = 0;
                        if (this.stepText1Input != null) this.setFocused(this.stepText1Input);
                    },
                    0xFF2A1C24, 0xFFFF4466, 0xFFFFAABB
            );
            clearBtn1.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.clear")));
            this.addRenderableWidget(clearBtn1);

            DynamicPillButton mode1Btn = new DynamicPillButton(
                    panelX + 238, row1Y, 104, 18,
                    () -> curStep.mode1 == -1
                            ? Component.translatable("gui.signbuilder.lcd.loop.text_studio.mode_inherit")
                            : Component.translatable(LCD_MODE_KEYS[curStep.mode1]),
                    () -> {
                        int idx = curStep.mode1 + 1;
                        curStep.mode1 = ((idx + 1) % 11) - 1;
                    },
                    () -> {
                        int idx = curStep.mode1 + 1;
                        curStep.mode1 = ((idx + 10) % 11) - 1;
                    },
                    () -> curStep.mode1 >= 0,
                    null,
                    0xFFD966FF, 0xFF2B1338
            );
            mode1Btn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.text_studio.mode.tooltip")));
            this.addRenderableWidget(mode1Btn);

            int row2Y = panelY + 154;
            DynamicPillButton l2Btn = new DynamicPillButton(
                    panelX + 14, row2Y, 28, 18,
                    () -> Component.literal("L2"),
                    () -> {
                        this.stepTextActiveLine = 1;
                        if (this.stepText2Input != null) this.setFocused(this.stepText2Input);
                    },
                    null,
                    () -> this.stepTextActiveLine == 1,
                    null,
                    0xFF00FFCC, 0xFF0E3029
            );
            this.addRenderableWidget(l2Btn);

            this.stepText2Input = new EditBox(
                    this.font,
                    panelX + 50,
                    row2Y + 5,
                    160,
                    10,
                    Component.translatable("gui.signbuilder.lcd.loop.text_studio.line2_hint")
            );
            this.stepText2Input.setMaxLength(128);
            this.stepText2Input.setBordered(false);
            this.stepText2Input.setHint(Component.translatable("gui.signbuilder.lcd.loop.text_studio.line2_hint").withStyle(ChatFormatting.DARK_GRAY));
            this.stepText2Input.setValue(curStep.text2);
            this.stepText2Input.setResponder(s -> {
                LoopStep st = getEditingTextStep();
                if (st != null) st.text2 = s;
            });
            this.stepText2Input.setTextColor(0xFFFFFFFF);
            this.addRenderableWidget(this.stepText2Input);

            ModernActionButton clearBtn2 = new ModernActionButton(
                    panelX + 218, row2Y, 16, 18, Component.literal("✕"),
                    () -> {
                        curStep.text2 = "";
                        if (this.stepText2Input != null) this.stepText2Input.setValue("");
                        this.stepTextActiveLine = 1;
                        if (this.stepText2Input != null) this.setFocused(this.stepText2Input);
                    },
                    0xFF2A1C24, 0xFFFF4466, 0xFFFFAABB
            );
            clearBtn2.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.clear")));
            this.addRenderableWidget(clearBtn2);

            DynamicPillButton mode2Btn = new DynamicPillButton(
                    panelX + 238, row2Y, 104, 18,
                    () -> curStep.mode2 == -1
                            ? Component.translatable("gui.signbuilder.lcd.loop.text_studio.mode_inherit")
                            : Component.translatable(LCD_MODE_KEYS[curStep.mode2]),
                    () -> {
                        int idx = curStep.mode2 + 1;
                        curStep.mode2 = ((idx + 1) % 11) - 1;
                    },
                    () -> {
                        int idx = curStep.mode2 + 1;
                        curStep.mode2 = ((idx + 10) % 11) - 1;
                    },
                    () -> curStep.mode2 >= 0,
                    null,
                    0xFFD966FF, 0xFF2B1338
            );
            mode2Btn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.text_studio.mode.tooltip")));
            this.addRenderableWidget(mode2Btn);

            int row3Y = panelY + 176;
            int alignW = 28;
            int[] alignVals = { -1, 0, 1, 2 };
            String[] alignKeys = {
                    "gui.signbuilder.lcd.loop.text_studio.align_inherit",
                    "gui.signbuilder.lcd.align.left",
                    "gui.signbuilder.lcd.align.center",
                    "gui.signbuilder.lcd.align.right"
            };
            int[] alignRoundings = { 0, 1, 1, 2 };
            for (int i = 0; i < 4; i++) {
                final int av = alignVals[i];
                SegmentedOptionButton alBtn = new SegmentedOptionButton(
                        panelX + 14 + i * (alignW + 1), row3Y, alignW, 18, alignRoundings[i],
                        Component.translatable(alignKeys[i]),
                        () -> curStep.align = av,
                        () -> curStep.align == av,
                        0xFF00E5FF, 0xFF0E2C3A
                );
                if (av == -1) {
                    alBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.loop.text_studio.align_auto.tooltip")));
                }
                this.addRenderableWidget(alBtn);
            }

            int varStartX = panelX + 136;
            int varW = 38;
            for (int i = 0; i < 5; i++) {
                final String tag = VAR_TAGS_R1[i];
                final String nameKey = "gui.signbuilder.lcd.var." + VAR_NAMES_R1[i];
                final String tipKey = "gui.signbuilder.lcd.var." + VAR_NAMES_R1[i] + ".tooltip";
                ModernActionButton varBtn = new ModernActionButton(
                        varStartX + i * (varW + 4), row3Y, varW, 18,
                        Component.translatable(nameKey),
                        () -> insertStepSymbolIntoActiveLine(tag),
                        0xFF102838, 0xFF00E5FF, 0xFF00FFCC
                );
                varBtn.setTooltip(Tooltip.create(Component.translatable(tipKey)));
                this.addRenderableWidget(varBtn);
            }

            int row4Y = panelY + 198;
            ModernActionButton boldBtn = new ModernActionButton(
                    panelX + 14, row4Y, 20, 18, Component.literal("B"),
                    () -> insertStepSymbolIntoActiveLine("&l"),
                    0xFF15202D, 0xFF354B64, 0xFF00FFCC
            );
            boldBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.bold")));
            this.addRenderableWidget(boldBtn);

            ModernActionButton italicBtn = new ModernActionButton(
                    panelX + 36, row4Y, 20, 18, Component.literal("I"),
                    () -> insertStepSymbolIntoActiveLine("&o"),
                    0xFF15202D, 0xFF354B64, 0xFF00FFCC
            );
            italicBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.italic")));
            this.addRenderableWidget(italicBtn);

            ModernActionButton underBtn = new ModernActionButton(
                    panelX + 58, row4Y, 20, 18, Component.literal("U"),
                    () -> insertStepSymbolIntoActiveLine("&n"),
                    0xFF15202D, 0xFF354B64, 0xFF00FFCC
            );
            underBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.underline")));
            this.addRenderableWidget(underBtn);

            ModernActionButton rstBtn = new ModernActionButton(
                    panelX + 80, row4Y, 22, 18, Component.literal("⟲"),
                    () -> insertStepSymbolIntoActiveLine("&r"),
                    0xFF2A1C24, 0xFFFF4466, 0xFFFFAABB
            );
            rstBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.format.reset")));
            this.addRenderableWidget(rstBtn);

            int swatchStartX = panelX + 106;
            for (int i = 0; i < FORMAT_COLOR_CODES.length; i++) {
                final String code = FORMAT_COLOR_CODES[i];
                int colRgb = FORMAT_COLOR_RGB[i];
                ModernActionButton colBtn = new ModernActionButton(
                        swatchStartX + i * 24, row4Y, 20, 18, Component.literal("■"),
                        () -> insertStepSymbolIntoActiveLine(code),
                        0xFF0E141C, colRgb, 0xFF000000 | colRgb
                );
                colBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.color_code_insert", code)));
                this.addRenderableWidget(colBtn);
            }

            if (this.stepTextActiveLine == 1 && this.stepText2Input != null) {
                this.setFocused(this.stepText2Input);
            } else if (this.stepText1Input != null) {
                this.setFocused(this.stepText1Input);
            }
        }
    }

    private LoopStep getEditingColorStep() {
        if (this.loopColorStepIdx >= 0 && this.loopColorStepIdx < this.loopSteps.size()) {
            return this.loopSteps.get(this.loopColorStepIdx);
        }
        return null;
    }

    private void openStepColorStudio(int stepIdx) {
        if (stepIdx < 0 || stepIdx >= this.loopSteps.size()) return;
        commitDurationEdit(false);
        this.loopColorStepIdx = stepIdx;
        LoopStep st = this.loopSteps.get(stepIdx);
        this.stepPickerHex = (st.stepColor >= 0) ? (st.stepColor & 0xFFFFFF) : (this.color & 0xFFFFFF);
        float[] hsb = RainbowColorHelper.rgbToHsb(
                (this.stepPickerHex >> 16) & 0xFF,
                (this.stepPickerHex >> 8) & 0xFF,
                this.stepPickerHex & 0xFF,
                null
        );
        this.stepHue = hsb[0];
        this.stepSat = hsb[1];
        this.stepBri = hsb[2];
        this.loopSubView = 3;
        this.clearWidgets();
        this.init();
    }

    private void applyCustomColorToEditingStep(int rgb) {
        this.stepPickerHex = rgb & 0xFFFFFF;
        float[] hsb = RainbowColorHelper.rgbToHsb(
                (this.stepPickerHex >> 16) & 0xFF,
                (this.stepPickerHex >> 8) & 0xFF,
                this.stepPickerHex & 0xFF,
                null
        );
        this.stepHue = hsb[0];
        this.stepSat = hsb[1];
        this.stepBri = hsb[2];
        LoopStep st = getEditingColorStep();
        if (st != null) {
            st.stepColor = this.stepPickerHex;
        }
        syncStepColorFieldsFromPicker();
    }

    private void syncStepColorFieldsFromPicker() {
        this.updatingStepColorInputs = true;
        int r = (this.stepPickerHex >> 16) & 0xFF;
        int g = (this.stepPickerHex >> 8) & 0xFF;
        int b = this.stepPickerHex & 0xFF;
        if (this.stepRInput != null) this.stepRInput.setValue(String.valueOf(r));
        if (this.stepGInput != null) this.stepGInput.setValue(String.valueOf(g));
        if (this.stepBInput != null) this.stepBInput.setValue(String.valueOf(b));
        if (this.stepHexInput != null) this.stepHexInput.setValue(String.format(Locale.ROOT, "%06X", this.stepPickerHex & 0xFFFFFF));
        this.updatingStepColorInputs = false;
    }

    private void onStepRgbTyped() {
        if (this.updatingStepColorInputs || this.stepRInput == null || this.stepGInput == null || this.stepBInput == null) return;
        String rs = this.stepRInput.getValue();
        String gs = this.stepGInput.getValue();
        String bs = this.stepBInput.getValue();
        if (rs.isEmpty() || gs.isEmpty() || bs.isEmpty()) return;
        try {
            int r = Integer.parseInt(rs);
            int g = Integer.parseInt(gs);
            int b = Integer.parseInt(bs);
            if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                this.stepPickerHex = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
                float[] hsb = RainbowColorHelper.rgbToHsb(r, g, b, null);
                this.stepHue = hsb[0];
                this.stepSat = hsb[1];
                this.stepBri = hsb[2];
                LoopStep st = getEditingColorStep();
                if (st != null) st.stepColor = this.stepPickerHex;
                this.updatingStepColorInputs = true;
                if (this.stepHexInput != null) {
                    this.stepHexInput.setValue(String.format(Locale.ROOT, "%06X", this.stepPickerHex));
                }
                this.updatingStepColorInputs = false;
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void onStepHexTyped(String hexText) {
        if (this.updatingStepColorInputs || hexText == null || hexText.length() != 6) return;
        try {
            int rgb = Integer.parseInt(hexText, 16) & 0xFFFFFF;
            this.stepPickerHex = rgb;
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            float[] hsb = RainbowColorHelper.rgbToHsb(r, g, b, null);
            this.stepHue = hsb[0];
            this.stepSat = hsb[1];
            this.stepBri = hsb[2];
            LoopStep st = getEditingColorStep();
            if (st != null) st.stepColor = this.stepPickerHex;
            this.updatingStepColorInputs = true;
            if (this.stepRInput != null) this.stepRInput.setValue(String.valueOf(r));
            if (this.stepGInput != null) this.stepGInput.setValue(String.valueOf(g));
            if (this.stepBInput != null) this.stepBInput.setValue(String.valueOf(b));
            this.updatingStepColorInputs = false;
        } catch (NumberFormatException ignored) {
        }
    }

    private void updateStepSatBriDrag(double mouseX, double mouseY, int sbX, int sbY, int sbW, int sbH) {
        this.stepSat = Mth.clamp((float) (mouseX - sbX) / (float) sbW, 0.0f, 1.0f);
        this.stepBri = Mth.clamp(1.0f - ((float) (mouseY - sbY) / (float) sbH), 0.0f, 1.0f);
        this.stepPickerHex = RainbowColorHelper.hsbToRgb(this.stepHue, this.stepSat, this.stepBri) & 0xFFFFFF;
        LoopStep st = getEditingColorStep();
        if (st != null) st.stepColor = this.stepPickerHex;
        syncStepColorFieldsFromPicker();
    }

    private void updateStepHueDrag(double mouseY, int hueY, int hueH) {
        this.stepHue = Mth.clamp(1.0f - ((float) (mouseY - hueY) / (float) hueH), 0.0f, 1.0f);
        this.stepPickerHex = RainbowColorHelper.hsbToRgb(this.stepHue, this.stepSat, this.stepBri) & 0xFFFFFF;
        LoopStep st = getEditingColorStep();
        if (st != null) st.stepColor = this.stepPickerHex;
        syncStepColorFieldsFromPicker();
    }

    private void applyPresetPool(int presetIdx, boolean appendMode) {
        int defDur = getDefaultStepDuration();
        if (!appendMode) {
            this.loopSteps.clear();
            this.loopScrollRow = 0;
        }
        if (presetIdx == 8) {
            for (int a = 1; a <= 60 && this.loopSteps.size() < LcdScreenBlockEntity.MAX_LOOP_STEPS; a++) {
                if (!hasAnimInLoop(a)) {
                    this.loopSteps.add(new LoopStep(a, defDur, -1));
                }
            }
        } else if (presetIdx >= 0 && presetIdx < LOOP_PRESET_POOLS.length) {
            for (int a : LOOP_PRESET_POOLS[presetIdx]) {
                if (this.loopSteps.size() >= LcdScreenBlockEntity.MAX_LOOP_STEPS) break;
                if (!appendMode || !hasAnimInLoop(a)) {
                    this.loopSteps.add(new LoopStep(a, defDur, -1));
                }
            }
        }
        if (!this.loopSteps.isEmpty()) {
            this.loopEnabled = true;
        }
        this.loopSubView = 0;
        this.clearWidgets();
        this.init();
    }

    private void copyLoopToClipboard() {
        if (this.loopSteps.isEmpty()) return;
        List<LoopStep> copied = new ArrayList<>(this.loopSteps.size());
        for (LoopStep st : this.loopSteps) {
            copied.add(st.copy());
        }
        copiedLoopClipboard = new LoopClipboard(this.loopShuffle, this.loopTransition, copied);
        this.loopCopiedFlashUntilMs = System.currentTimeMillis() + 1500L;
    }

    private void pasteLoopFromClipboard() {
        if (copiedLoopClipboard == null || copiedLoopClipboard.steps().isEmpty()) return;
        this.loopSteps.clear();
        for (LoopStep st : copiedLoopClipboard.steps()) {
            if (this.loopSteps.size() >= LcdScreenBlockEntity.MAX_LOOP_STEPS) break;
            this.loopSteps.add(st.copy());
        }
        this.loopShuffle = copiedLoopClipboard.shuffle();
        this.loopTransition = Math.max(0, Math.min(9, copiedLoopClipboard.transition()));
        this.loopEnabled = !this.loopSteps.isEmpty();
        this.loopScrollRow = 0;
    }

    private static String normalizeSearchText(String s) {
        if (s == null) return "";
        return s.replace('İ', 'i').replace('I', 'ı').toLowerCase(Locale.ROOT).trim();
    }

    private void rebuildFilteredPickerAnims() {
        this.filteredPickerAnims.clear();
        String q = normalizeSearchText(this.loopAnimSearchQuery);
        for (int v = 1; v <= 60; v++) {
            if (q.isEmpty()) {
                this.filteredPickerAnims.add(v);
            } else {
                String name = normalizeSearchText(Component.translatable("gui.signbuilder.lcd.visual." + v).getString());
                String tip = normalizeSearchText(Component.translatable("gui.signbuilder.lcd.visual." + v + ".tooltip").getString());
                String num = String.valueOf(v);
                if (name.contains(q) || tip.contains(q) || num.equals(q) || ("#" + num).equals(q)) {
                    this.filteredPickerAnims.add(v);
                }
            }
        }
        int totalPages = Math.max(1, (this.filteredPickerAnims.size() + 11) / 12);
        if (this.loopPickerPage >= totalPages) {
            this.loopPickerPage = totalPages - 1;
        }
    }

    private int getPickerSlotAnim(int slot) {
        int idx = this.loopPickerPage * 12 + slot;
        if (idx >= 0 && idx < this.filteredPickerAnims.size()) {
            return this.filteredPickerAnims.get(idx);
        }
        return -1;
    }

    private void startDurationEdit(int stepIdx) {
        if (stepIdx < 0 || stepIdx >= this.loopSteps.size()) return;
        if (this.editingDurStepIdx >= 0 && this.editingDurStepIdx != stepIdx) {
            commitDurationEdit(false);
        }
        LoopStep st = this.loopSteps.get(stepIdx);
        this.editingDurStepIdx = stepIdx;
        this.editingDurBackup = st.durationSec;
        this.editingDurBuffer = String.valueOf(st.durationSec);
        this.editingDurSelectAll = true;
    }

    private void commitDurationEdit(boolean applyToAll) {
        if (this.editingDurStepIdx < 0) return;
        if (this.editingDurStepIdx < this.loopSteps.size()) {
            int val = this.editingDurBackup;
            if (this.editingDurBuffer != null && !this.editingDurBuffer.isEmpty()) {
                try {
                    val = Math.max(1, Math.min(999, Integer.parseInt(this.editingDurBuffer)));
                } catch (NumberFormatException ignored) {
                }
            } else {
                val = Math.max(1, this.editingDurBackup);
            }
            if (applyToAll) {
                for (LoopStep s : this.loopSteps) {
                    s.durationSec = val;
                }
            } else {
                this.loopSteps.get(this.editingDurStepIdx).durationSec = val;
            }
        }
        this.editingDurStepIdx = -1;
        this.editingDurBuffer = "";
        this.editingDurSelectAll = false;
    }

    private void cancelDurationEdit() {
        if (this.editingDurStepIdx >= 0 && this.editingDurStepIdx < this.loopSteps.size()) {
            this.loopSteps.get(this.editingDurStepIdx).durationSec = this.editingDurBackup;
        }
        this.editingDurStepIdx = -1;
        this.editingDurBuffer = "";
        this.editingDurSelectAll = false;
    }

    private Component getLoopSubTabLabel() {
        MutableComponent base = Component.translatable("gui.signbuilder.lcd.tab.loop");
        if (!this.loopSteps.isEmpty()) {
            String suffix = this.loopEnabled ? (" (" + this.loopSteps.size() + ") ●") : (" (" + this.loopSteps.size() + ")");
            return base.append(Component.literal(suffix).withStyle(this.loopEnabled ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        return base;
    }

    private int getDefaultStepDuration() {
        if (!this.loopSteps.isEmpty()) {
            return this.loopSteps.get(this.loopSteps.size() - 1).durationSec;
        }
        return 6;
    }

    private boolean hasAnimInLoop(int animId) {
        for (LoopStep st : this.loopSteps) {
            if (st.animId == animId) return true;
        }
        return false;
    }

    private void toggleAnimInLoop(int animId) {
        boolean removed = false;
        for (int i = this.loopSteps.size() - 1; i >= 0; i--) {
            if (this.loopSteps.get(i).animId == animId) {
                this.loopSteps.remove(i);
                removed = true;
            }
        }
        if (!removed && this.loopSteps.size() < LcdScreenBlockEntity.MAX_LOOP_STEPS) {
            this.loopSteps.add(new LoopStep(animId, getDefaultStepDuration(), -1));
            this.loopEnabled = true;
        } else if (this.loopSteps.isEmpty()) {
            this.loopEnabled = false;
        }
        int maxScroll = Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS);
        this.loopScrollRow = Math.min(this.loopScrollRow, maxScroll);
    }

    private String getLoopBadgeForAnim(int animId) {
        int firstIdx = -1;
        int count = 0;
        for (int i = 0; i < this.loopSteps.size(); i++) {
            if (this.loopSteps.get(i).animId == animId) {
                if (firstIdx == -1) firstIdx = i;
                count++;
            }
        }
        if (count == 0) return "";
        if (this.loopShuffle) return count > 1 ? ("⇄" + count) : "⇄";
        return count > 1 ? ("#" + (firstIdx + 1) + "+") : ("#" + (firstIdx + 1));
    }

    private LoopRowLayout computeLoopRowLayout(int boxX, int boxY, int boxW, int visRow, boolean hasScroll) {
        int ry = boxY + 15 + visRow * 21;
        int subY = ry + 2;
        int subH = 15;
        int rowRight = hasScroll ? (boxX + boxW - 10) : (boxX + boxW - 5);
        int delW = 15;
        int delX = rowRight - delW;
        int downW = 14;
        int downX = delX - 2 - downW;
        int upW = 14;
        int upX = downX - 1 - upW;
        int plusW = 14;
        int plusX = upX - 4 - plusW;
        int durW = 36;
        int durX = plusX - 1 - durW;
        int minusW = 14;
        int minusX = durX - 1 - minusW;
        int colW = 16;
        int colX = minusX - 4 - colW;
        int badgeX = boxX + 5;
        int badgeW = 24;
        int animX = badgeX + badgeW + 3;
        int animW = Math.max(40, colX - 4 - animX);
        return new LoopRowLayout(ry, subY, subH, badgeX, badgeW, animX, animW, colX, colW,
                minusX, minusW, durX, durW, plusX, plusW, upX, upW, downX, downW, delX, delW);
    }

    private int[] buildLoopAnimsArray() {
        int[] out = new int[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).animId;
        return out;
    }

    private int[] buildLoopDurationsArray() {
        int[] out = new int[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).durationSec;
        return out;
    }

    private int[] buildLoopColorsArray() {
        int[] out = new int[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).stepColor;
        return out;
    }

    private String[] buildLoopTexts1Array() {
        String[] out = new String[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).text1;
        return out;
    }

    private String[] buildLoopTexts2Array() {
        String[] out = new String[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).text2;
        return out;
    }

    private int[] buildLoopModes1Array() {
        int[] out = new int[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).mode1;
        return out;
    }

    private int[] buildLoopModes2Array() {
        int[] out = new int[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).mode2;
        return out;
    }

    private int[] buildLoopAlignsArray() {
        int[] out = new int[this.loopSteps.size()];
        for (int i = 0; i < out.length; i++) out[i] = this.loopSteps.get(i).align;
        return out;
    }

    private void openStepTextStudio(int stepIdx) {
        if (stepIdx < 0 || stepIdx >= this.loopSteps.size()) return;
        commitDurationEdit(false);
        this.loopSteps.get(stepIdx).animId = 0;
        this.loopTextStepIdx = stepIdx;
        this.stepTextActiveLine = 0;
        this.loopSubView = 4;
        this.clearWidgets();
        this.init();
    }

    private LoopStep getEditingTextStep() {
        if (this.loopTextStepIdx >= 0 && this.loopTextStepIdx < this.loopSteps.size()) {
            return this.loopSteps.get(this.loopTextStepIdx);
        }
        return null;
    }

    private void syncStepTextInputsToState() {
        LoopStep st = getEditingTextStep();
        if (st != null) {
            if (this.stepText1Input != null) st.text1 = this.stepText1Input.getValue();
            if (this.stepText2Input != null) st.text2 = this.stepText2Input.getValue();
        }
    }

    private void insertStepSymbolIntoActiveLine(String symbol) {
        if (this.stepTextActiveLine == 1 && this.stepText2Input != null) {
            this.stepText2Input.insertText(symbol);
            LoopStep st = getEditingTextStep();
            if (st != null) st.text2 = this.stepText2Input.getValue();
            this.setFocused(this.stepText2Input);
        } else if (this.stepText1Input != null) {
            this.stepText1Input.insertText(symbol);
            LoopStep st = getEditingTextStep();
            if (st != null) st.text1 = this.stepText1Input.getValue();
            this.setFocused(this.stepText1Input);
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

            PillButton speedCycleBtn = new PillButton(
                    panelX + 180, glowY, 162, 18,
                    Component.translatable("gui.signbuilder.lcd.speed." + Math.max(1, Math.min(4, this.speed))),
                    () -> {
                        this.speed = (this.speed % 4) + 1;
                        this.clearWidgets();
                        this.init();
                    },
                    () -> true,
                    0xFF00E5FF, 0xFF102A38
            );
            speedCycleBtn.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.lcd.speed")));
            this.addRenderableWidget(speedCycleBtn);
        }
    }

    private void applyThemeColorPreset(int t) {
        if (t == 0) { this.color = 0x00FF66; this.rainbow = false; }
        else if (t == 1) { this.color = 0x00FF44; this.rainbow = false; }
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
        else if (t == 12) { this.color = 0x1E293B; this.rainbow = false; }
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

    private void sendSavePacket() {
        syncInputsToState();
        syncStepTextInputsToState();
        String combined = this.line1;
        if (this.line2 != null && !this.line2.isEmpty()) {
            combined = this.line1 + "\n" + this.line2;
        }
        boolean effectiveLoopEnabled = (this.mainMode == 1) && this.loopEnabled && !this.loopSteps.isEmpty();
        int effectiveVisualAnim = (this.mainMode == 1)
                ? (this.visualAnim > 0 ? this.visualAnim : (this.lastVisualAnim > 0 ? this.lastVisualAnim : 1))
                : 0;
        int effectiveStorageMode = (this.mainMode == 2) ? 1 : 0;
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
                this.widgetTL,
                this.widgetTR,
                this.widgetBL,
                this.widgetBR,
                this.fontStyle,
                effectiveVisualAnim,
                effectiveStorageMode,
                this.storageStyle,
                this.storageFilterMode,
                this.storageFlowMode,
                this.watchlist,
                effectiveLoopEnabled,
                this.loopShuffle,
                this.loopTransition,
                buildLoopAnimsArray(),
                buildLoopDurationsArray(),
                buildLoopColorsArray(),
                buildLoopTexts1Array(),
                buildLoopTexts2Array(),
                buildLoopModes1Array(),
                buildLoopModes2Array(),
                buildLoopAlignsArray()
        ));
    }

    private void saveAndClose() {
        sendSavePacket();
        this.onClose();
    }

    private boolean isPinnedInGui(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (ItemStack ws : this.watchlist) {
            if (LcdScreenBlockEntity.matchesTargetItem(stack, ws)) {
                return true;
            }
        }
        return false;
    }

    private void toggleWatchlistItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        boolean removed = false;
        for (int i = 0; i < this.watchlist.size(); i++) {
            if (LcdScreenBlockEntity.matchesTargetItem(stack, this.watchlist.get(i))) {
                this.watchlist.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            if (this.watchlist.size() >= LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS) {
                if (this.minecraft != null) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 0.65f));
                }
                return;
            }
            this.watchlist.add(LcdScreenBlockEntity.cleanWatchlistCopy(stack));
        }
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, removed ? 0.85f : 1.18f));
        }
    }

    private static boolean matchesSearchQuery(ItemStack stack, String displayName, String queryLower) {
        if (queryLower == null || queryLower.isEmpty()) return true;
        if (displayName != null && displayName.toLowerCase(Locale.ROOT).contains(queryLower)) {
            return true;
        }
        if (stack != null && !stack.isEmpty()) {
            String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase(Locale.ROOT);
            return path.contains(queryLower) || path.replace('_', ' ').contains(queryLower);
        }
        return false;
    }

    private List<InspectorEntry> buildInspectorEntries(LcdScreenBlockEntity master) {
        List<InspectorEntry> result = new ArrayList<>();
        String q = (this.storageSearchQuery == null) ? "" : this.storageSearchQuery.trim().toLowerCase(Locale.ROOT);

        if (this.pinFromInventoryMode) {
            Map<String, ItemStack> repStacks = new LinkedHashMap<>();
            Map<String, Integer> counts = new LinkedHashMap<>();
            Inventory inv = (this.minecraft != null && this.minecraft.player != null) ? this.minecraft.player.getInventory() : null;
            if (inv != null) {
                int size = inv.getContainerSize();
                for (int i = 0; i < size; i++) {
                    ItemStack st = inv.getItem(i);
                    if (st.isEmpty()) continue;
                    String key = LcdScreenBlockEntity.makeItemKey(st);
                    if (!repStacks.containsKey(key)) {
                        repStacks.put(key, st.copyWithCount(1));
                        counts.put(key, st.getCount());
                    } else {
                        counts.put(key, counts.get(key) + st.getCount());
                    }
                }
            }
            for (ItemStack ws : this.watchlist) {
                if (ws == null || ws.isEmpty()) continue;
                String key = LcdScreenBlockEntity.makeItemKey(ws);
                if (!repStacks.containsKey(key)) {
                    repStacks.put(key, ws.copyWithCount(1));
                    counts.put(key, 0);
                }
            }
            for (Map.Entry<String, ItemStack> e : repStacks.entrySet()) {
                ItemStack st = e.getValue();
                String name = st.getHoverName().getString();
                if (!matchesSearchQuery(st, name, q)) continue;
                int cnt = counts.getOrDefault(e.getKey(), 0);
                result.add(new InspectorEntry(st, name, cnt, 0, 0, isPinnedInGui(st)));
            }
            return result;
        }

        if (master == null || !master.hasLinkedStorage()) {
            return result;
        }

        int rawCount = master.getStorageEntryCount();
        for (ItemStack ws : this.watchlist) {
            if (ws == null || ws.isEmpty()) continue;
            boolean existsInStock = false;
            for (int i = 0; i < rawCount; i++) {
                if (master.getStorageTopCount(i) > 0 && LcdScreenBlockEntity.matchesTargetItem(master.getStorageTopStack(i), ws)) {
                    existsInStock = true;
                    break;
                }
            }
            if (!existsInStock) {
                ItemStack clean = ws.copyWithCount(1);
                String name = clean.getHoverName().getString();
                if (matchesSearchQuery(clean, name, q)) {
                    result.add(new InspectorEntry(clean, name, 0, 0, 0, true));
                }
            }
        }

        for (int i = 0; i < rawCount; i++) {
            int cnt = master.getStorageTopCount(i);
            if (cnt <= 0) continue;
            ItemStack st = master.getStorageTopStack(i);
            String name = master.getStorageTopDisplayName(i);
            if (!matchesSearchQuery(st, name, q)) continue;
            result.add(new InspectorEntry(
                    st,
                    name,
                    cnt,
                    master.getStorageTopShare(i),
                    master.getStorageTopRatePerHour(i),
                    isPinnedInGui(st)
            ));
        }

        return result;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.editingDurStepIdx >= 0) {
            boolean clickedActiveDurBox = false;
            if (this.mainMode == 1 && this.visualTab == 1 && this.loopSubView == 0 && !this.loopSteps.isEmpty()) {
                int panelX = (this.width - PANEL_WIDTH) / 2;
                int panelY = (this.height - PANEL_HEIGHT) / 2;
                int maxScroll = Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS);
                int scrollRow = Math.max(0, Math.min(maxScroll, this.loopScrollRow));
                int visRow = this.editingDurStepIdx - scrollRow;
                if (visRow >= 0 && visRow < LOOP_VIS_ROWS) {
                    LoopRowLayout rl = computeLoopRowLayout(panelX + 14, panelY + 148, 328, visRow, this.loopSteps.size() > LOOP_VIS_ROWS);
                    if (mouseX >= rl.durX() && mouseX < rl.durX() + rl.durW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH()) {
                        clickedActiveDurBox = true;
                    }
                }
            }
            if (!clickedActiveDurBox) {
                commitDurationEdit(false);
            }
        }

        if (this.mainMode == 0 && this.textTab == 0) {
            if (this.textField1 != null && this.textField1.isMouseOver(mouseX, mouseY)) {
                this.activeLine = 0;
            } else if (this.textField2 != null && this.textField2.isMouseOver(mouseX, mouseY)) {
                this.activeLine = 1;
            }
        } else if (this.mainMode == 1 && this.visualTab == 1 && this.loopSubView == 4) {
            int pX = (this.width - PANEL_WIDTH) / 2;
            int pY = (this.height - PANEL_HEIGHT) / 2;
            int bX = pX + 46;
            int bW = 168;
            if ((mouseX >= bX && mouseX < bX + bW && mouseY >= pY + 132 && mouseY < pY + 150)
                    || (this.stepText1Input != null && this.stepText1Input.isMouseOver(mouseX, mouseY))) {
                this.stepTextActiveLine = 0;
                if (this.stepText1Input != null) this.setFocused(this.stepText1Input);
            } else if ((mouseX >= bX && mouseX < bX + bW && mouseY >= pY + 154 && mouseY < pY + 172)
                    || (this.stepText2Input != null && this.stepText2Input.isMouseOver(mouseX, mouseY))) {
                this.stepTextActiveLine = 1;
                if (this.stepText2Input != null) this.setFocused(this.stepText2Input);
            }
        } else if (this.mainMode == 1 && this.visualTab == 1 && (button == 0 || button == 1)) {
            int panelX = (this.width - PANEL_WIDTH) / 2;
            int panelY = (this.height - PANEL_HEIGHT) / 2;

            if (this.loopSubView == 0 && !this.loopSteps.isEmpty()) {
                int boxX = panelX + 14;
                int boxY = panelY + 148;
                int boxW = 328;
                int maxScroll = Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS);
                int scrollRow = Math.max(0, Math.min(maxScroll, this.loopScrollRow));
                boolean hasScroll = this.loopSteps.size() > LOOP_VIS_ROWS;

                for (int r = 0; r < LOOP_VIS_ROWS; r++) {
                    int idx = scrollRow + r;
                    if (idx >= this.loopSteps.size()) break;
                    LoopRowLayout rl = computeLoopRowLayout(boxX, boxY, boxW, r, hasScroll);
                    if (mouseY < rl.subY() || mouseY >= rl.subY() + rl.subH()) continue;

                    LoopStep step = this.loopSteps.get(idx);
                    if (button == 0 && mouseX >= rl.badgeX() && mouseX < rl.badgeX() + rl.badgeW()) {
                        playUiClick();
                        this.previewFocusStepIdx = idx;
                        this.previewFocusUntilMs = System.currentTimeMillis() + 4000L;
                        return true;
                    }
                    if (mouseX >= rl.animX() && mouseX < rl.animX() + rl.animW()) {
                        playUiClick();
                        if (step.animId == 0 && button == 0) {
                            openStepTextStudio(idx);
                            return true;
                        }
                        this.loopPickerTarget = idx;
                        this.loopAnimSearchQuery = "";
                        rebuildFilteredPickerAnims();
                        this.loopPickerPage = step.animId > 0 ? (step.animId - 1) / 12 : 0;
                        this.loopSubView = 1;
                        this.clearWidgets();
                        this.init();
                        return true;
                    }
                    if (mouseX >= rl.colX() && mouseX < rl.colX() + rl.colW()) {
                        playUiClick();
                        openStepColorStudio(idx);
                        return true;
                    }
                    if (button == 0 && mouseX >= rl.minusX() && mouseX < rl.minusX() + rl.minusW()) {
                        playUiClick();
                        int delta = Screen.hasShiftDown() ? 5 : 1;
                        int nextDur = Math.max(1, step.durationSec - delta);
                        if (Screen.hasControlDown()) {
                            for (LoopStep s : this.loopSteps) s.durationSec = nextDur;
                        } else {
                            step.durationSec = nextDur;
                        }
                        return true;
                    }
                    if (mouseX >= rl.durX() && mouseX < rl.durX() + rl.durW()) {
                        playUiClick();
                        if (button == 0) {
                            if (this.editingDurStepIdx == idx) {
                                this.editingDurSelectAll = false;
                            } else {
                                startDurationEdit(idx);
                            }
                        } else {
                            commitDurationEdit(false);
                            int delta = Screen.hasShiftDown() ? 5 : 1;
                            int nextDur = Math.max(1, step.durationSec - delta);
                            if (Screen.hasControlDown()) {
                                for (LoopStep s : this.loopSteps) s.durationSec = nextDur;
                            } else {
                                step.durationSec = nextDur;
                            }
                        }
                        return true;
                    }
                    if (button == 0 && mouseX >= rl.plusX() && mouseX < rl.plusX() + rl.plusW()) {
                        playUiClick();
                        int delta = Screen.hasShiftDown() ? 5 : 1;
                        int nextDur = Math.min(999, step.durationSec + delta);
                        if (Screen.hasControlDown()) {
                            for (LoopStep s : this.loopSteps) s.durationSec = nextDur;
                        } else {
                            step.durationSec = nextDur;
                        }
                        return true;
                    }
                    if (button == 0 && mouseX >= rl.upX() && mouseX < rl.upX() + rl.upW()) {
                        if (idx > 0) {
                            playUiClick();
                            LoopStep prev = this.loopSteps.get(idx - 1);
                            this.loopSteps.set(idx - 1, step);
                            this.loopSteps.set(idx, prev);
                        }
                        return true;
                    }
                    if (button == 0 && mouseX >= rl.downX() && mouseX < rl.downX() + rl.downW()) {
                        if (idx < this.loopSteps.size() - 1) {
                            playUiClick();
                            LoopStep next = this.loopSteps.get(idx + 1);
                            this.loopSteps.set(idx + 1, step);
                            this.loopSteps.set(idx, next);
                        }
                        return true;
                    }
                    if (button == 0 && mouseX >= rl.delX() && mouseX < rl.delX() + rl.delW()) {
                        playUiClick();
                        this.loopSteps.remove(idx);
                        if (this.loopSteps.isEmpty()) {
                            this.loopEnabled = false;
                        }
                        this.loopScrollRow = Math.min(this.loopScrollRow, Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS));
                        return true;
                    }
                }
            } else if (this.loopSubView == 1 && button == 0 && this.loopAnimSearchField != null) {
                int sbX = panelX + 105;
                int sbY = panelY + 111;
                int sbW = 148;
                int sbH = 17;
                if (mouseX >= sbX && mouseX < sbX + sbW && mouseY >= sbY && mouseY < sbY + sbH) {
                    if (!this.loopAnimSearchQuery.isEmpty() && mouseX >= sbX + sbW - 15) {
                        playUiClick();
                        this.loopAnimSearchField.setValue("");
                        this.loopAnimSearchField.setFocused(true);
                        this.setFocused(this.loopAnimSearchField);
                        return true;
                    }
                    this.loopAnimSearchField.setFocused(true);
                    this.setFocused(this.loopAnimSearchField);
                    return super.mouseClicked(Math.max(panelX + 123, Math.min(panelX + 234, mouseX)), sbY + 7, button);
                }
            } else if (this.loopSubView == 3 && button == 0) {
                int cardX = panelX + 14;
                int cardY = panelY + 131;
                int sbX = cardX + 8;
                int sbY = cardY + 8;
                int sbW = 102;
                int sbH = 81;
                int hueX = cardX + 115;
                int hueW = 14;

                if (mouseX >= sbX && mouseX <= sbX + sbW && mouseY >= sbY && mouseY <= sbY + sbH) {
                    this.draggingStepSatBri = true;
                    updateStepSatBriDrag(mouseX, mouseY, sbX, sbY, sbW, sbH);
                    return true;
                }
                if (mouseX >= hueX && mouseX <= hueX + hueW && mouseY >= sbY && mouseY <= sbY + sbH) {
                    this.draggingStepHue = true;
                    updateStepHueDrag(mouseY, sbY, sbH);
                    return true;
                }

                int swStartX = cardX + 216;
                int swStartY = cardY + 32;
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 4; c++) {
                        int sx = swStartX + c * 26;
                        int sy = swStartY + r * 19;
                        if (mouseX >= sx && mouseX < sx + 24 && mouseY >= sy && mouseY < sy + 17) {
                            playUiClick();
                            applyCustomColorToEditingStep(STEP_QUICK_SWATCHES[r * 4 + c]);
                            return true;
                        }
                    }
                }

                int[] inYs = { cardY + 8, cardY + 28, cardY + 48, cardY + 69 };
                int[] inWs = { 42, 42, 42, 60 };
                EditBox[] boxes = { this.stepRInput, this.stepGInput, this.stepBInput, this.stepHexInput };
                for (int i = 0; i < 4; i++) {
                    if (boxes[i] != null && mouseX >= cardX + 148 && mouseX < cardX + 148 + inWs[i]
                            && mouseY >= inYs[i] && mouseY < inYs[i] + 16) {
                        for (int j = 0; j < 4; j++) {
                            if (boxes[j] != null) boxes[j].setFocused(j == i);
                        }
                        this.setFocused(boxes[i]);
                        return super.mouseClicked(Math.max(cardX + 151, Math.min(cardX + 148 + inWs[i] - 4, mouseX)), inYs[i] + 6, button);
                    }
                }
            }
        } else if (this.mainMode == 2 && this.storageTab == 0 && (button == 0 || button == 1)) {
            int panelX = (this.width - PANEL_WIDTH) / 2;
            int panelY = (this.height - PANEL_HEIGHT) / 2;
            int barY = panelY + 112;
            if (button == 0 && this.storageSearchField != null
                    && mouseX >= panelX + 14 && mouseX < panelX + 226
                    && mouseY >= barY && mouseY < barY + 18) {
                this.storageSearchField.setFocused(true);
                this.setFocused(this.storageSearchField);
                return super.mouseClicked(Math.max(panelX + 35, mouseX), barY + 7, button);
            }

            net.minecraft.world.level.Level level = this.minecraft != null ? this.minecraft.level : null;
            LcdScreenBlockEntity master = (level != null && this.masterPos != null && level.getBlockEntity(this.masterPos) instanceof LcdScreenBlockEntity be) ? be : null;
            if (this.pinFromInventoryMode || (master != null && master.hasLinkedStorage())) {
                List<InspectorEntry> entries = buildInspectorEntries(master);
                if (!entries.isEmpty()) {
                    int boxX = panelX + 14;
                    int boxY = panelY + 133;
                    int boxW = 328;
                    int totalRows = Math.max(1, (entries.size() + INSPECTOR_COLS - 1) / INSPECTOR_COLS);
                    int maxScroll = Math.max(0, totalRows - INSPECTOR_VIS_ROWS);
                    int scrollRow = Math.max(0, Math.min(maxScroll, this.storageScrollRow));
                    boolean hasScroll = totalRows > INSPECTOR_VIS_ROWS;
                    int availGridW = hasScroll ? (boxW - 20) : (boxW - 12);
                    int cellGap = 2;
                    int cellW = (availGridW - (INSPECTOR_COLS - 1) * cellGap) / INSPECTOR_COLS;
                    int cellH = 21;
                    int gridStartY = boxY + 17;

                    for (int r = 0; r < INSPECTOR_VIS_ROWS; r++) {
                        int actualRow = scrollRow + r;
                        for (int c = 0; c < INSPECTOR_COLS; c++) {
                            int idx = actualRow * INSPECTOR_COLS + c;
                            if (idx >= entries.size()) break;
                            int cx = boxX + 6 + c * (cellW + cellGap);
                            int cy = gridStartY + r * (cellH + cellGap);
                            if (mouseX >= cx && mouseX < cx + cellW && mouseY >= cy && mouseY < cy + cellH) {
                                InspectorEntry clicked = entries.get(idx);
                                if (this.pinFromInventoryMode || button == 1) {
                                    toggleWatchlistItem(clicked.stack());
                                    return true;
                                }
                                if (this.minecraft != null) {
                                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                                }
                                sendSavePacket();
                                ModMessages.sendToServer(new LocateStorageItemC2SPacket(this.masterPos, clicked.stack()));
                                this.onClose();
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playUiClick() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.mainMode == 1 && this.visualTab == 1 && this.loopSubView == 3 && (this.draggingStepSatBri || this.draggingStepHue)) {
            int panelX = (this.width - PANEL_WIDTH) / 2;
            int panelY = (this.height - PANEL_HEIGHT) / 2;
            int cardX = panelX + 14;
            int cardY = panelY + 131;
            if (this.draggingStepSatBri) {
                updateStepSatBriDrag(mouseX, mouseY, cardX + 8, cardY + 8, 102, 81);
                return true;
            }
            if (this.draggingStepHue) {
                updateStepHueDrag(mouseY, cardY + 8, 81);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingStepSatBri = false;
        this.draggingStepHue = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.mainMode == 1 && this.visualTab == 1) {
            if (this.loopSubView == 0) {
                commitDurationEdit(false);
                int maxScroll = Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS);
                if (scrollY < 0) {
                    this.loopScrollRow = Math.min(maxScroll, this.loopScrollRow + 1);
                    return true;
                } else if (scrollY > 0) {
                    this.loopScrollRow = Math.max(0, this.loopScrollRow - 1);
                    return true;
                }
            } else if (this.loopSubView == 1) {
                int totalPages = Math.max(1, (this.filteredPickerAnims.size() + 11) / 12);
                if (scrollY < 0 && this.loopPickerPage < totalPages - 1) {
                    this.loopPickerPage++;
                    return true;
                } else if (scrollY > 0 && this.loopPickerPage > 0) {
                    this.loopPickerPage--;
                    return true;
                }
            }
        } else if (this.mainMode == 2 && this.storageTab == 0) {
            net.minecraft.world.level.Level level = this.minecraft != null ? this.minecraft.level : null;
            LcdScreenBlockEntity master = (level != null && this.masterPos != null && level.getBlockEntity(this.masterPos) instanceof LcdScreenBlockEntity be) ? be : null;
            List<InspectorEntry> entries = buildInspectorEntries(master);
            int totalRows = Math.max(1, (entries.size() + INSPECTOR_COLS - 1) / INSPECTOR_COLS);
            int maxScroll = Math.max(0, totalRows - INSPECTOR_VIS_ROWS);
            if (scrollY < 0) {
                this.storageScrollRow = Math.min(maxScroll, this.storageScrollRow + 1);
                return true;
            } else if (scrollY > 0) {
                this.storageScrollRow = Math.max(0, this.storageScrollRow - 1);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.editingDurStepIdx >= 0 && this.editingDurStepIdx < this.loopSteps.size()) {
            if (codePoint >= '0' && codePoint <= '9') {
                if (this.editingDurSelectAll) {
                    if (codePoint == '0') {
                        return true;
                    }
                    this.editingDurBuffer = String.valueOf(codePoint);
                    this.editingDurSelectAll = false;
                } else {
                    if (this.editingDurBuffer.isEmpty() && codePoint == '0') {
                        return true;
                    }
                    if (this.editingDurBuffer.length() < 3) {
                        this.editingDurBuffer = this.editingDurBuffer + codePoint;
                    }
                }
                if (!this.editingDurBuffer.isEmpty()) {
                    try {
                        int val = Math.max(1, Math.min(999, Integer.parseInt(this.editingDurBuffer)));
                        this.loopSteps.get(this.editingDurStepIdx).durationSec = val;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.editingDurStepIdx >= 0 && this.editingDurStepIdx < this.loopSteps.size()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                playUiClick();
                commitDurationEdit(Screen.hasControlDown());
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                playUiClick();
                cancelDurationEdit();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                if (this.editingDurSelectAll) {
                    this.editingDurBuffer = "";
                    this.editingDurSelectAll = false;
                } else if (!this.editingDurBuffer.isEmpty()) {
                    this.editingDurBuffer = this.editingDurBuffer.substring(0, this.editingDurBuffer.length() - 1);
                    if (!this.editingDurBuffer.isEmpty()) {
                        try {
                            int val = Math.max(1, Math.min(999, Integer.parseInt(this.editingDurBuffer)));
                            this.loopSteps.get(this.editingDurStepIdx).durationSec = val;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
                int delta = (Screen.hasShiftDown() ? 5 : 1) * (keyCode == GLFW.GLFW_KEY_UP ? 1 : -1);
                LoopStep st = this.loopSteps.get(this.editingDurStepIdx);
                int nextVal = Math.max(1, Math.min(999, st.durationSec + delta));
                st.durationSec = nextVal;
                this.editingDurBuffer = String.valueOf(nextVal);
                this.editingDurSelectAll = false;
                return true;
            }
            return true;
        }

        EditBox[] customInputs = { this.storageSearchField, this.loopAnimSearchField, this.stepRInput, this.stepGInput, this.stepBInput, this.stepHexInput, this.stepText1Input, this.stepText2Input };
        for (EditBox eb : customInputs) {
            if (eb != null && eb.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    eb.setFocused(false);
                    this.setFocused(null);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    eb.setFocused(false);
                    this.setFocused(null);
                    return true;
                }
                if (eb.keyPressed(keyCode, scanCode, modifiers) || eb.canConsumeInput()) {
                    return true;
                }
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.mainMode == 1 && this.visualTab == 1 && this.loopSubView != 0) {
            syncStepTextInputsToState();
            this.loopSubView = 0;
            this.loopPickerTarget = -1;
            this.loopColorStepIdx = -1;
            this.loopTextStepIdx = -1;
            this.clearWidgets();
            this.init();
            return true;
        }
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

        Component badgeComp = Component.translatable("gui.signbuilder.lcd.screen_size_badge", this.widthBlocks, this.heightBlocks, this.widthBlocks * this.heightBlocks);
        int badgeW = this.font.width(badgeComp) + 8;
        int badgeX = panelX + PANEL_WIDTH - 14 - badgeW;
        guiGraphics.fill(badgeX, panelY + 4, badgeX + badgeW, panelY + 16, 0x55000000);
        guiGraphics.renderOutline(badgeX, panelY + 4, badgeW, 12, 0xFF00FFCC);
        guiGraphics.drawString(this.font, badgeComp, badgeX + 4, panelY + 6, 0xFF00FFCC, false);

        int maxPrevW = 328;
        int maxPrevH = 48;
        float screenRatio = (float) this.widthBlocks / (float) this.heightBlocks;
        int prevH = maxPrevH;
        int prevW = Math.min(maxPrevW, Math.max(80, Math.round(prevH * screenRatio)));
        int prevX = panelX + 14 + (maxPrevW - prevW) / 2;
        int prevY = panelY + 39;

        renderThemedMonitor(guiGraphics, prevX, prevY, prevW, prevH);

        List<Component> storageHoverLines = null;

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

            Component rClickHint = Component.translatable("gui.signbuilder.lcd.loop.anim_tile_hint");
            float hintW = this.font.width(rClickHint) * 0.68f;
            if (hintW < infoW - 110) {
                drawScaledText(guiGraphics, this.font, rClickHint, infoX + infoW - 7.0f - hintW, infoY + 4.5f, 0.68f, 0xFF00E5FF, false);
            }

            net.minecraft.network.chat.FormattedText trimmed = this.font.substrByWidth(Component.translatable(tipKey), infoW - 16);
            guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimmed), infoX + 8, infoY + 13, 0xFF8EA0B4, false);
        } else if (this.mainMode == 1 && this.visualTab == 1) {
            if (this.loopSubView == 0) {
                guiGraphics.fill(panelX + 100, panelY + 110, panelX + 198, panelY + 129, 0xFF060A0F);
                guiGraphics.renderOutline(panelX + 100, panelY + 110, 98, 19, 0xFF223244);

                int boxX = panelX + 14;
                int boxY = panelY + 148;
                int boxW = 328;
                int boxH = 80;
                boolean activeLoop = this.loopEnabled && !this.loopSteps.isEmpty();
                int accentCol = activeLoop ? (this.loopShuffle ? 0xFFFFAA00 : 0xFF00E5FF) : 0xFF3E4E60;
                int borderCol = activeLoop ? (this.loopShuffle ? 0xFF593D14 : 0xFF1F4B5E) : 0xFF233242;

                guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF090E15);
                guiGraphics.renderOutline(boxX, boxY, boxW, boxH, borderCol);
                guiGraphics.fill(boxX, boxY, boxX + 3, boxY + boxH, accentCol);
                guiGraphics.fill(boxX + 3, boxY + 13, boxX + boxW - 2, boxY + 14, 0x302C4259);

                int totalSec = 0;
                for (LoopStep st : this.loopSteps) {
                    totalSec += st.durationSec;
                }
                Component sumComp = Component.translatable(
                        this.loopShuffle ? "gui.signbuilder.lcd.loop.summary_shuffle" : "gui.signbuilder.lcd.loop.summary_seq",
                        this.loopSteps.size(), totalSec
                );
                drawScaledText(guiGraphics, this.font, sumComp, boxX + 7.0f, boxY + 3.5f, 0.70f,
                        activeLoop ? (this.loopShuffle ? 0xFFFFCA62 : 0xFF66EEFF) : 0xFF7D93AA, false);

                LcdScreenBlockEntity.LoopPlaybackState liveState = null;
                if (activeLoop) {
                    long seed = this.masterPos != null ? this.masterPos.asLong() : 0L;
                    liveState = LcdScreenBlockEntity.resolveLoopPlayback(
                            buildLoopAnimsArray(),
                            buildLoopDurationsArray(),
                            buildLoopColorsArray(),
                            this.loopShuffle,
                            this.loopTransition,
                            seed,
                            System.currentTimeMillis()
                    );
                    Component curName = liveState.animId() == 0
                            ? Component.translatable("gui.signbuilder.lcd.loop.text_step")
                            : Component.translatable("gui.signbuilder.lcd.visual." + liveState.animId());
                    int remSec = Math.max(1, (int) Math.ceil((liveState.stepDurationMs() - liveState.elapsedMs()) / 1000.0));
                    String liveBadge = "▶ #" + (liveState.stepIndex() + 1) + " " + curName.getString() + " (" + remSec + "s)";
                    float lbW = this.font.width(liveBadge) * 0.66f;
                    if (lbW < boxW - 150) {
                        drawScaledText(guiGraphics, this.font, Component.literal(liveBadge),
                                boxX + boxW - 6.0f - lbW, boxY + 3.8f, 0.66f, 0xFF33FF99, false);
                    }
                }

                if (this.loopSteps.isEmpty()) {
                    Component emptyTitle = Component.translatable("gui.signbuilder.lcd.loop.empty_title");
                    Component emptyDesc = Component.translatable("gui.signbuilder.lcd.loop.empty_desc");
                    drawScaledText(guiGraphics, this.font, emptyTitle, boxX + boxW / 2.0f, boxY + 31.0f, 0.85f, 0xFF00E5FF, true);
                    net.minecraft.network.chat.FormattedText trimDesc = this.font.substrByWidth(emptyDesc, Math.round((boxW - 24) / 0.72f));
                    float descW = this.font.width(trimDesc) * 0.72f;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(boxX + (boxW - descW) / 2.0f, boxY + 47.0f, 0.0f);
                    guiGraphics.pose().scale(0.72f, 0.72f, 1.0f);
                    guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimDesc), 0, 0, 0xFF849BB3, false);
                    guiGraphics.pose().popPose();
                } else {
                    int maxScroll = Math.max(0, this.loopSteps.size() - LOOP_VIS_ROWS);
                    this.loopScrollRow = Math.max(0, Math.min(maxScroll, this.loopScrollRow));
                    boolean hasScroll = this.loopSteps.size() > LOOP_VIS_ROWS;

                    for (int r = 0; r < LOOP_VIS_ROWS; r++) {
                        int idx = this.loopScrollRow + r;
                        if (idx >= this.loopSteps.size()) break;
                        LoopStep step = this.loopSteps.get(idx);
                        LoopRowLayout rl = computeLoopRowLayout(boxX, boxY, boxW, r, hasScroll);
                        boolean isPlayingRow = (liveState != null && liveState.stepIndex() == idx);
                        boolean isPreviewFocused = (System.currentTimeMillis() < this.previewFocusUntilMs && this.previewFocusStepIdx == idx);

                        int rowLeft = boxX + 4;
                        int rowRight = rl.delX() + rl.delW() + 2;
                        guiGraphics.fill(rowLeft, rl.ry(), rowRight, rl.ry() + 19, isPlayingRow ? 0xFF102230 : 0xFF0D141E);
                        guiGraphics.renderOutline(rowLeft, rl.ry(), rowRight - rowLeft, 19,
                                isPreviewFocused ? 0xFF00E5FF : (isPlayingRow ? 0xFF1E576E : 0xFF1A2736));
                        if (isPlayingRow && liveState.stepDurationMs() > 0L) {
                            float prog = Math.max(0.0f, Math.min(1.0f, (float) liveState.elapsedMs() / (float) liveState.stepDurationMs()));
                            int progW = Math.round((rowRight - rowLeft - 2) * prog);
                            if (progW > 0) {
                                guiGraphics.fill(rowLeft + 1, rl.ry() + 17, rowLeft + 1 + progW, rl.ry() + 18, 0xFF00FF99);
                            }
                        }

                        boolean hovBadge = mouseX >= rl.badgeX() && mouseX < rl.badgeX() + rl.badgeW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        String stepBadgeStr = this.loopShuffle ? ("⇄" + (idx + 1)) : ("#" + (idx + 1));
                        int badgeBg = isPreviewFocused ? 0xFF143C52 : (isPlayingRow ? 0xFF123C30 : (hovBadge ? 0xFF1B2B3D : 0xFF121C28));
                        int badgeFg = isPreviewFocused ? 0xFFFFFFFF : (isPlayingRow ? 0xFF33FF99 : (this.loopShuffle ? 0xFFFFB833 : 0xFF55D8FF));
                        guiGraphics.fill(rl.badgeX(), rl.subY(), rl.badgeX() + rl.badgeW(), rl.subY() + rl.subH(), badgeBg);
                        guiGraphics.renderOutline(rl.badgeX(), rl.subY(), rl.badgeW(), rl.subH(),
                                hovBadge || isPreviewFocused ? 0xFF00E5FF : (isPlayingRow ? 0xFF288060 : 0xFF233447));
                        drawAutoScaledButtonText(guiGraphics, this.font, Component.literal(stepBadgeStr),
                                rl.badgeX(), rl.subY(), rl.badgeW(), rl.subH(), badgeFg);
                        if (hovBadge) {
                            storageHoverLines = List.of(Component.translatable("gui.signbuilder.lcd.loop.tip.preview_step").withStyle(ChatFormatting.AQUA));
                        }

                        boolean hovAnim = mouseX >= rl.animX() && mouseX < rl.animX() + rl.animW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        guiGraphics.fill(rl.animX(), rl.subY(), rl.animX() + rl.animW(), rl.subY() + rl.subH(), hovAnim ? 0xFF1C2A3B : 0xFF131D29);
                        guiGraphics.renderOutline(rl.animX(), rl.subY(), rl.animW(), rl.subH(), hovAnim ? 0xFF00E5FF : 0xFF283A4E);
                        Component animLabel;
                        if (step.animId == 0) {
                            if (step.hasCustomText()) {
                                String displayTxt = !step.text1.isEmpty() ? step.text1 : step.text2;
                                animLabel = Component.literal("✎ " + displayTxt);
                            } else {
                                animLabel = Component.translatable("gui.signbuilder.lcd.loop.text_step");
                            }
                        } else {
                            animLabel = Component.translatable("gui.signbuilder.lcd.visual." + step.animId);
                        }
                        int animTextCol = step.animId == 0 ? 0xFF44FFE0 : (hovAnim ? 0xFFFFFFFF : 0xFFDCE8F5);
                        net.minecraft.network.chat.FormattedText trimAnim = this.font.substrByWidth(animLabel, rl.animW() - 16);
                        guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimAnim),
                                rl.animX() + 5, rl.subY() + 4, animTextCol, false);
                        guiGraphics.drawString(this.font, "▾", rl.animX() + rl.animW() - 8, rl.subY() + 4, hovAnim ? 0xFF00E5FF : 0xFF6B8299, false);
                        if (hovAnim) {
                            if (step.animId == 0) {
                                storageHoverLines = List.of(
                                        animLabel.copy().withStyle(ChatFormatting.AQUA),
                                        Component.translatable("gui.signbuilder.lcd.loop.tip.text_left_edit").withStyle(ChatFormatting.YELLOW),
                                        Component.translatable("gui.signbuilder.lcd.loop.tip.text_right_change").withStyle(ChatFormatting.GRAY)
                                );
                            } else {
                                storageHoverLines = List.of(
                                        animLabel.copy().withStyle(ChatFormatting.GOLD),
                                        Component.translatable("gui.signbuilder.lcd.loop.tip.change_anim").withStyle(ChatFormatting.GRAY)
                                );
                            }
                        }

                        boolean hovCol = mouseX >= rl.colX() && mouseX < rl.colX() + rl.colW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        if (step.stepColor == -1) {
                            guiGraphics.fill(rl.colX(), rl.subY(), rl.colX() + rl.colW(), rl.subY() + rl.subH(), hovCol ? 0xFF202E40 : 0xFF141E2A);
                            drawAutoScaledButtonText(guiGraphics, this.font, Component.literal("A"),
                                    rl.colX(), rl.subY(), rl.colW(), rl.subH(), 0xFF9AB2CA);
                        } else if (step.stepColor == -2) {
                            int sw = rl.colW() - 2;
                            int sx = rl.colX() + 1;
                            int sy = rl.subY() + 1;
                            int sh = rl.subH() - 2;
                            guiGraphics.fill(sx, sy, sx + sw / 4, sy + sh, 0xFFFF3355);
                            guiGraphics.fill(sx + sw / 4, sy, sx + sw / 2, sy + sh, 0xFFFFCC00);
                            guiGraphics.fill(sx + sw / 2, sy, sx + (sw * 3) / 4, sy + sh, 0xFF00FF66);
                            guiGraphics.fill(sx + (sw * 3) / 4, sy, sx + sw, sy + sh, 0xFF00CCFF);
                        } else {
                            guiGraphics.fill(rl.colX() + 1, rl.subY() + 1, rl.colX() + rl.colW() - 1, rl.subY() + rl.subH() - 1, 0xFF000000 | step.stepColor);
                        }
                        guiGraphics.renderOutline(rl.colX(), rl.subY(), rl.colW(), rl.subH(), hovCol ? 0xFFFFFFFF : 0xFF394D63);
                        if (hovCol) {
                            Component colTitle = step.stepColor == -1
                                    ? Component.translatable("gui.signbuilder.lcd.loop.tip.color_auto").withStyle(ChatFormatting.AQUA)
                                    : (step.stepColor == -2
                                    ? Component.translatable("gui.signbuilder.lcd.loop.tip.color_rainbow").withStyle(ChatFormatting.LIGHT_PURPLE)
                                    : Component.translatable("gui.signbuilder.lcd.loop.tip.color_custom", String.format(Locale.ROOT, "#%06X", step.stepColor))
                                            .withStyle(Style.EMPTY.withColor(step.stepColor)));
                            storageHoverLines = List.of(
                                    colTitle,
                                    Component.translatable("gui.signbuilder.lcd.loop.tip.color_help").withStyle(ChatFormatting.GRAY)
                            );
                        }

                        boolean hovMinus = mouseX >= rl.minusX() && mouseX < rl.minusX() + rl.minusW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        boolean hovDur = mouseX >= rl.durX() && mouseX < rl.durX() + rl.durW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        boolean hovPlus = mouseX >= rl.plusX() && mouseX < rl.plusX() + rl.plusW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        boolean editingThisDur = (this.editingDurStepIdx == idx);

                        guiGraphics.fill(rl.minusX(), rl.subY(), rl.minusX() + rl.minusW(), rl.subY() + rl.subH(), hovMinus ? 0xFF223346 : 0xFF141E2B);
                        guiGraphics.renderOutline(rl.minusX(), rl.subY(), rl.minusW(), rl.subH(), hovMinus ? 0xFF00E5FF : 0xFF283A4E);
                        drawAutoScaledButtonText(guiGraphics, this.font, Component.literal("-"), rl.minusX(), rl.subY(), rl.minusW(), rl.subH(), hovMinus ? 0xFFFFFFFF : 0xFFAEC2D6);

                        Component durComp = Component.translatable("gui.signbuilder.lcd.loop.sec_unit", step.durationSec);
                        if (editingThisDur) {
                            guiGraphics.fill(rl.durX(), rl.subY(), rl.durX() + rl.durW(), rl.subY() + rl.subH(), 0xFF072233);
                            if (this.editingDurSelectAll && !this.editingDurBuffer.isEmpty()) {
                                guiGraphics.fill(rl.durX() + 3, rl.subY() + 2, rl.durX() + rl.durW() - 3, rl.subY() + rl.subH() - 2, 0x6600B8FF);
                            }
                            guiGraphics.renderOutline(rl.durX(), rl.subY(), rl.durW(), rl.subH(), 0xFF00FFCC);
                            boolean blinkOn = ((System.currentTimeMillis() / 420L) % 2L) == 0L;
                            String editStr = this.editingDurBuffer + (blinkOn && !this.editingDurSelectAll ? "|" : "");
                            drawAutoScaledButtonText(guiGraphics, this.font, Component.literal(editStr), rl.durX(), rl.subY(), rl.durW(), rl.subH(), 0xFFFFFFFF);
                        } else {
                            guiGraphics.fill(rl.durX(), rl.subY(), rl.durX() + rl.durW(), rl.subY() + rl.subH(), hovDur ? 0xFF142434 : 0xFF0A1018);
                            guiGraphics.renderOutline(rl.durX(), rl.subY(), rl.durW(), rl.subH(), hovDur ? 0xFF00E5FF : 0xFF223346);
                            drawAutoScaledButtonText(guiGraphics, this.font, durComp, rl.durX(), rl.subY(), rl.durW(), rl.subH(), 0xFF66EEFF);
                        }

                        guiGraphics.fill(rl.plusX(), rl.subY(), rl.plusX() + rl.plusW(), rl.subY() + rl.subH(), hovPlus ? 0xFF223346 : 0xFF141E2B);
                        guiGraphics.renderOutline(rl.plusX(), rl.subY(), rl.plusW(), rl.subH(), hovPlus ? 0xFF00E5FF : 0xFF283A4E);
                        drawAutoScaledButtonText(guiGraphics, this.font, Component.literal("+"), rl.plusX(), rl.subY(), rl.plusW(), rl.subH(), hovPlus ? 0xFFFFFFFF : 0xFFAEC2D6);

                        if (hovMinus || hovDur || hovPlus) {
                            storageHoverLines = List.of(
                                    durComp.copy().withStyle(ChatFormatting.AQUA),
                                    Component.translatable("gui.signbuilder.lcd.loop.tip.duration").withStyle(ChatFormatting.GRAY)
                            );
                        }

                        boolean canUp = idx > 0;
                        boolean hovUp = canUp && mouseX >= rl.upX() && mouseX < rl.upX() + rl.upW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        guiGraphics.fill(rl.upX(), rl.subY(), rl.upX() + rl.upW(), rl.subY() + rl.subH(), hovUp ? 0xFF223346 : 0xFF131C27);
                        guiGraphics.renderOutline(rl.upX(), rl.subY(), rl.upW(), rl.subH(), hovUp ? 0xFF00E5FF : 0xFF243446);
                        drawAutoScaledButtonText(guiGraphics, this.font, Component.literal("▲"), rl.upX(), rl.subY(), rl.upW(), rl.subH(),
                                canUp ? (hovUp ? 0xFFFFFFFF : 0xFF9AB0C6) : 0xFF3E4E60);
                        if (hovUp) {
                            storageHoverLines = List.of(Component.translatable("gui.signbuilder.lcd.loop.tip.move_up").withStyle(ChatFormatting.GRAY));
                        }

                        boolean canDown = idx < this.loopSteps.size() - 1;
                        boolean hovDown = canDown && mouseX >= rl.downX() && mouseX < rl.downX() + rl.downW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        guiGraphics.fill(rl.downX(), rl.subY(), rl.downX() + rl.downW(), rl.subY() + rl.subH(), hovDown ? 0xFF223346 : 0xFF131C27);
                        guiGraphics.renderOutline(rl.downX(), rl.subY(), rl.downW(), rl.subH(), hovDown ? 0xFF00E5FF : 0xFF243446);
                        drawAutoScaledButtonText(guiGraphics, this.font, Component.literal("▼"), rl.downX(), rl.subY(), rl.downW(), rl.subH(),
                                canDown ? (hovDown ? 0xFFFFFFFF : 0xFF9AB0C6) : 0xFF3E4E60);
                        if (hovDown) {
                            storageHoverLines = List.of(Component.translatable("gui.signbuilder.lcd.loop.tip.move_down").withStyle(ChatFormatting.GRAY));
                        }

                        boolean hovDel = mouseX >= rl.delX() && mouseX < rl.delX() + rl.delW() && mouseY >= rl.subY() && mouseY < rl.subY() + rl.subH();
                        guiGraphics.fill(rl.delX(), rl.subY(), rl.delX() + rl.delW(), rl.subY() + rl.subH(), hovDel ? 0xFF42151E : 0xFF1F1218);
                        guiGraphics.renderOutline(rl.delX(), rl.subY(), rl.delW(), rl.subH(), hovDel ? 0xFFFF4455 : 0xFF4A222C);
                        drawAutoScaledButtonText(guiGraphics, this.font, Component.literal("✕"), rl.delX(), rl.subY(), rl.delW(), rl.subH(),
                                hovDel ? 0xFFFF7788 : 0xFFD96C7A);
                        if (hovDel) {
                            storageHoverLines = List.of(Component.translatable("gui.signbuilder.lcd.loop.tip.remove").withStyle(ChatFormatting.RED));
                        }
                    }

                    if (hasScroll) {
                        int sbX = boxX + boxW - 6;
                        int sbY = boxY + 15;
                        int sbH = LOOP_VIS_ROWS * 21 - 2;
                        guiGraphics.fill(sbX, sbY, sbX + 3, sbY + sbH, 0xFF141E2A);
                        int thumbH = Math.max(8, Math.round(sbH * ((float) LOOP_VIS_ROWS / (float) this.loopSteps.size())));
                        int thumbY = sbY + Math.round((sbH - thumbH) * ((float) this.loopScrollRow / (float) maxScroll));
                        guiGraphics.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, activeLoop ? accentCol : 0xFF5D7690);
                    }
                }
            } else if (this.loopSubView == 1) {
                int sbX = panelX + 105;
                int sbY = panelY + 111;
                int sbW = 148;
                int sbH = 17;
                boolean searchFocused = this.loopAnimSearchField != null && this.loopAnimSearchField.isFocused();
                guiGraphics.fill(sbX, sbY, sbX + sbW, sbY + sbH, 0xFF090E15);
                guiGraphics.fill(sbX + 1, sbY + 1, sbX + 16, sbY + sbH - 1, searchFocused ? 0xFF0E2534 : 0xFF0E1621);
                guiGraphics.fill(sbX + 16, sbY + 3, sbX + 17, sbY + sbH - 3, 0xFF1F2F42);
                guiGraphics.renderOutline(sbX, sbY, sbW, sbH, searchFocused ? 0xFF00E5FF : 0xFF2B3E52);
                guiGraphics.drawString(this.font, "⌖", sbX + 5, sbY + 5, searchFocused ? 0xFF00FFFF : 0xFF6BA8C9, false);
                if (!this.loopAnimSearchQuery.isEmpty()) {
                    boolean hovClear = mouseX >= sbX + sbW - 15 && mouseX < sbX + sbW && mouseY >= sbY && mouseY < sbY + sbH;
                    guiGraphics.drawString(this.font, "✕", sbX + sbW - 11, sbY + 5, hovClear ? 0xFFFF6677 : 0xFF8EA0B4, false);
                }

                if (this.filteredPickerAnims.isEmpty()) {
                    guiGraphics.fill(panelX + 14, panelY + 131, panelX + 342, panelY + 183, 0xFF090E15);
                    guiGraphics.renderOutline(panelX + 14, panelY + 131, 328, 52, 0xFF233242);
                    drawScaledText(guiGraphics, this.font, Component.translatable("gui.signbuilder.lcd.loop.search_empty"),
                            panelX + PANEL_WIDTH / 2.0f, panelY + 152.0f, 0.82f, 0xFFFFAA00, true);
                } else {
                    int[] colX = { panelX + 14, panelX + 97, panelX + 180, panelX + 263 };
                    int[] rowY = { panelY + 131, panelY + 149, panelY + 167 };
                    for (int slot = 0; slot < 12; slot++) {
                        int animId = getPickerSlotAnim(slot);
                        if (animId <= 0) continue;
                        int bx = colX[slot % 4];
                        int by = rowY[slot / 4];
                        if (mouseX >= bx && mouseX < bx + 79 && mouseY >= by && mouseY < by + 16) {
                            storageHoverLines = List.of(
                                    Component.translatable("gui.signbuilder.lcd.visual." + animId).withStyle(ChatFormatting.GOLD),
                                    Component.translatable("gui.signbuilder.lcd.visual." + animId + ".tooltip").withStyle(ChatFormatting.GRAY)
                            );
                        }
                    }
                }

                int infoX = panelX + 14;
                int infoY = panelY + 204;
                int infoW = 328;
                int infoH = 24;
                guiGraphics.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0xFF0B1017);
                guiGraphics.renderOutline(infoX, infoY, infoW, infoH, 0xFF283C50);
                guiGraphics.fill(infoX, infoY, infoX + 3, infoY + infoH, 0xFF00E5FF);

                Component pickerHint = (this.loopPickerTarget >= 0)
                        ? Component.translatable("gui.signbuilder.lcd.loop.picker_hint_single", this.loopPickerTarget + 1)
                        : Component.translatable("gui.signbuilder.lcd.loop.picker_hint_multi");
                net.minecraft.network.chat.FormattedText trimHint = this.font.substrByWidth(pickerHint, Math.round((infoW - 14) / 0.75f));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(infoX + 8.0f, infoY + 8.5f, 0.0f);
                guiGraphics.pose().scale(0.75f, 0.75f, 1.0f);
                guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimHint), 0, 0, 0xFF8CE6FF, false);
                guiGraphics.pose().popPose();
            } else if (this.loopSubView == 2) {
                int hdrX = panelX + 14;
                int hdrY = panelY + 111;
                int hdrW = 239;
                int hdrH = 17;
                guiGraphics.fill(hdrX, hdrY, hdrX + hdrW, hdrY + hdrH, 0xFF0B1119);
                guiGraphics.renderOutline(hdrX, hdrY, hdrW, hdrH, 0xFF382A14);
                guiGraphics.fill(hdrX, hdrY, hdrX + 3, hdrY + hdrH, 0xFFFFAA00);
                drawScaledText(guiGraphics, this.font, Component.translatable("gui.signbuilder.lcd.loop.presets_header"),
                        hdrX + 8.0f, hdrY + 5.0f, 0.78f, 0xFFFFCA62, false);

                int infoX = panelX + 14;
                int infoY = panelY + 195;
                int infoW = 328;
                int infoH = 33;
                guiGraphics.fill(infoX, infoY, infoX + infoW, infoY + infoH, 0xFF0B1017);
                guiGraphics.renderOutline(infoX, infoY, infoW, infoH, 0xFF283C50);
                guiGraphics.fill(infoX, infoY, infoX + 3, infoY + infoH, 0xFFFFAA00);
                List<FormattedCharSequence> lines = this.font.split(Component.translatable("gui.signbuilder.lcd.loop.presets_hint"), Math.round((infoW - 16) / 0.72f));
                for (int li = 0; li < Math.min(2, lines.size()); li++) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(infoX + 8.0f, infoY + 6.5f + li * 11.0f, 0.0f);
                    guiGraphics.pose().scale(0.72f, 0.72f, 1.0f);
                    guiGraphics.drawString(this.font, lines.get(li), 0, 0, li == 0 ? 0xFFFFDF8E : 0xFF8EA8C2, false);
                    guiGraphics.pose().popPose();
                }
            } else if (this.loopSubView == 3) {
                int cardX = panelX + 14;
                int cardY = panelY + 131;
                int cardW = 328;
                int cardH = 97;
                guiGraphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xFF090E15);
                guiGraphics.renderOutline(cardX, cardY, cardW, cardH, 0xFF26384B);
                guiGraphics.fill(cardX, cardY, cardX + 3, cardY + cardH, 0xFFFF00D4);

                int sbX = cardX + 8;
                int sbY = cardY + 8;
                int sbW = 102;
                int sbH = 81;
                int halfH = sbH / 2;
                for (int px = 0; px < sbW; px += 2) {
                    float s = px / (float) sbW;
                    int topCol = 0xFF000000 | RainbowColorHelper.hsbToRgb(this.stepHue, s, 1.0f);
                    int midCol = 0xFF000000 | RainbowColorHelper.hsbToRgb(this.stepHue, s, 0.5f);
                    int colEnd = Math.min(sbW, px + 2);
                    guiGraphics.fillGradient(sbX + px, sbY, sbX + colEnd, sbY + halfH, topCol, midCol);
                    guiGraphics.fillGradient(sbX + px, sbY + halfH, sbX + colEnd, sbY + sbH, midCol, 0xFF000000);
                }
                guiGraphics.renderOutline(sbX - 1, sbY - 1, sbW + 2, sbH + 2, 0xFF445A72);

                int curX = sbX + Math.round(this.stepSat * sbW);
                int curY = sbY + Math.round((1.0f - this.stepBri) * sbH);
                guiGraphics.fill(curX - 3, curY - 3, curX + 3, curY + 3, 0xFFFFFFFF);
                guiGraphics.fill(curX - 2, curY - 2, curX + 2, curY + 2, 0xFF000000 | this.stepPickerHex);

                int hueX = cardX + 115;
                int hueW = 14;
                for (int py = 0; py < sbH; py += 2) {
                    float h = 1.0f - (py / (float) sbH);
                    int hCol = 0xFF000000 | RainbowColorHelper.hsbToRgb(h, 1.0f, 1.0f);
                    guiGraphics.fill(hueX, sbY + py, hueX + hueW, sbY + Math.min(sbH, py + 2), hCol);
                }
                guiGraphics.renderOutline(hueX - 1, sbY - 1, hueW + 2, sbH + 2, 0xFF445A72);
                int hueCurY = sbY + Math.round((1.0f - this.stepHue) * sbH);
                guiGraphics.fill(hueX - 2, hueCurY - 1, hueX + hueW + 2, hueCurY + 2, 0xFFFFFFFF);

                int[] inYs = { cardY + 8, cardY + 28, cardY + 48, cardY + 69 };
                int[] inWs = { 42, 42, 42, 60 };
                String[] inLabels = { "R", "G", "B", "#" };
                int[] labelCols = { 0xFFFF6677, 0xFF55FF88, 0xFF55BBFF, 0xFF00E5FF };
                EditBox[] boxes = { this.stepRInput, this.stepGInput, this.stepBInput, this.stepHexInput };
                for (int i = 0; i < 4; i++) {
                    boolean foc = boxes[i] != null && boxes[i].isFocused();
                    guiGraphics.drawString(this.font, inLabels[i], cardX + 137, inYs[i] + 4, labelCols[i], false);
                    guiGraphics.fill(cardX + 148, inYs[i], cardX + 148 + inWs[i], inYs[i] + 16, 0xFF0D141E);
                    guiGraphics.renderOutline(cardX + 148, inYs[i], inWs[i], 16, foc ? 0xFF00E5FF : 0xFF2B3E52);
                }

                LoopStep editStep = getEditingColorStep();
                int banX = cardX + 216;
                int banY = cardY + 8;
                int banW = 104;
                int banH = 20;
                if (editStep != null && editStep.stepColor == -1) {
                    guiGraphics.fill(banX, banY, banX + banW, banY + banH, 0xFF102534);
                    guiGraphics.renderOutline(banX, banY, banW, banH, 0xFF00E5FF);
                    drawAutoScaledButtonText(guiGraphics, this.font,
                            Component.literal("#" + (this.loopColorStepIdx + 1) + " • ").append(Component.translatable("gui.signbuilder.lcd.loop.col_mode.auto")),
                            banX, banY, banW, banH, 0xFF66EEFF);
                } else if (editStep != null && editStep.stepColor == -2) {
                    int qW = (banW - 2) / 4;
                    guiGraphics.fill(banX + 1, banY + 1, banX + 1 + qW, banY + banH - 1, 0xFF991833);
                    guiGraphics.fill(banX + 1 + qW, banY + 1, banX + 1 + qW * 2, banY + banH - 1, 0xFF886600);
                    guiGraphics.fill(banX + 1 + qW * 2, banY + 1, banX + 1 + qW * 3, banY + banH - 1, 0xFF0E6633);
                    guiGraphics.fill(banX + 1 + qW * 3, banY + 1, banX + banW - 1, banY + banH - 1, 0xFF0E4477);
                    guiGraphics.renderOutline(banX, banY, banW, banH, 0xFFFF66DD);
                    drawAutoScaledButtonText(guiGraphics, this.font,
                            Component.literal("#" + (this.loopColorStepIdx + 1) + " • ").append(Component.translatable("gui.signbuilder.lcd.loop.col_mode.rainbow")),
                            banX, banY, banW, banH, 0xFFFFFFFF);
                } else {
                    int showCol = (editStep != null && editStep.stepColor >= 0) ? editStep.stepColor : this.stepPickerHex;
                    guiGraphics.fill(banX + 1, banY + 1, banX + banW - 1, banY + banH - 1, 0xFF000000 | showCol);
                    guiGraphics.renderOutline(banX, banY, banW, banH, 0xFFFFFFFF);
                    String hexLabel = "#" + (this.loopColorStepIdx + 1) + " • " + String.format(Locale.ROOT, "#%06X", showCol & 0xFFFFFF);
                    guiGraphics.fill(banX + 4, banY + 4, banX + banW - 4, banY + banH - 4, 0xAA080D14);
                    drawAutoScaledButtonText(guiGraphics, this.font, Component.literal(hexLabel), banX + 4, banY + 4, banW - 8, banH - 8, 0xFFFFFFFF);
                }

                int swStartX = cardX + 216;
                int swStartY = cardY + 32;
                for (int r = 0; r < 3; r++) {
                    for (int c = 0; c < 4; c++) {
                        int swCol = STEP_QUICK_SWATCHES[r * 4 + c];
                        int sx = swStartX + c * 26;
                        int sy = swStartY + r * 19;
                        boolean hovSw = mouseX >= sx && mouseX < sx + 24 && mouseY >= sy && mouseY < sy + 17;
                        boolean selSw = editStep != null && editStep.stepColor == swCol;
                        guiGraphics.fill(sx, sy, sx + 24, sy + 17, 0xFF0B1017);
                        guiGraphics.fill(sx + 2, sy + 2, sx + 22, sy + 15, 0xFF000000 | swCol);
                        guiGraphics.renderOutline(sx, sy, 24, 17, selSw ? 0xFFFFFFFF : (hovSw ? 0xFF00E5FF : 0xFF2C3D50));
                    }
                }
            } else if (this.loopSubView == 4) {
                int box1X = panelX + 46;
                int box1Y = panelY + 132;
                int box1W = 168;
                int box1H = 18;
                boolean f1 = this.stepText1Input != null && this.stepText1Input.isFocused();
                guiGraphics.fill(box1X, box1Y, box1X + box1W, box1Y + box1H, 0xFF0D141E);
                guiGraphics.renderOutline(box1X, box1Y, box1W, box1H, f1 ? 0xFF00FFCC : 0xFF2B3E52);

                int box2X = panelX + 46;
                int box2Y = panelY + 154;
                int box2W = 168;
                int box2H = 18;
                boolean f2 = this.stepText2Input != null && this.stepText2Input.isFocused();
                guiGraphics.fill(box2X, box2Y, box2X + box2W, box2Y + box2H, 0xFF0D141E);
                guiGraphics.renderOutline(box2X, box2Y, box2W, box2H, f2 ? 0xFF00FFCC : 0xFF2B3E52);
            }
        } else if (this.mainMode == 2 && this.storageTab == 0) {
            int searchBarX = panelX + 14;
            int searchBarY = panelY + 112;
            int searchBarW = 212;
            int searchBarH = 18;
            boolean searchFocused = this.storageSearchField != null && this.storageSearchField.isFocused();
            guiGraphics.fill(searchBarX, searchBarY, searchBarX + searchBarW, searchBarY + searchBarH, 0xFF090E15);
            guiGraphics.fill(searchBarX + 1, searchBarY + 1, searchBarX + 18, searchBarY + searchBarH - 1, searchFocused ? 0xFF0E2534 : 0xFF0E1621);
            guiGraphics.fill(searchBarX + 18, searchBarY + 3, searchBarX + 19, searchBarY + searchBarH - 3, 0xFF1F2F42);
            guiGraphics.renderOutline(searchBarX, searchBarY, searchBarW, searchBarH, searchFocused ? 0xFF00E5FF : 0xFF2B3E52);
            guiGraphics.drawString(this.font, "⌖", searchBarX + 6, searchBarY + 5, searchFocused ? 0xFF00FFFF : 0xFF6BA8C9, false);

            int statX = panelX + 14;
            int statY = panelY + 133;
            int statW = 328;
            int statH = 95;
            int accentStrip = this.pinFromInventoryMode ? 0xFFFFCC22 : 0xFF00E5FF;
            guiGraphics.fill(statX, statY, statX + statW, statY + statH, 0xFF0B1017);
            guiGraphics.renderOutline(statX, statY, statW, statH, this.pinFromInventoryMode ? 0xFF665018 : 0xFF253649);
            guiGraphics.fill(statX, statY, statX + 2, statY + statH, accentStrip);

            net.minecraft.world.level.Level level = this.minecraft != null ? this.minecraft.level : null;
            LcdScreenBlockEntity master = (level != null && this.masterPos != null && level.getBlockEntity(this.masterPos) instanceof LcdScreenBlockEntity be) ? be : null;

            if (this.pinFromInventoryMode || (master != null && master.hasLinkedStorage())) {
                if (this.pinFromInventoryMode) {
                    String pinBadge = "★ " + this.watchlist.size() + "/" + LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS;
                    int pinW = this.font.width(pinBadge);
                    Component invHeader = Component.translatable("gui.signbuilder.lcd.storage.inv_mode_header");
                    net.minecraft.network.chat.FormattedText trimHdr = this.font.substrByWidth(invHeader, statW - pinW - 20);
                    guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimHdr), statX + 6, statY + 4, 0xFFFFCC22, true);
                    guiGraphics.drawString(this.font, pinBadge, statX + statW - pinW - 6, statY + 4, 0xFFFFD700, true);
                    guiGraphics.fill(statX + 6, statY + 14, statX + statW - 6, statY + 15, 0x55FFCC22);
                } else {
                    int linkedCnt = master.getLinkedStorageCount();
                    String posHeader;
                    if (linkedCnt > 1) {
                        posHeader = "● " + Component.translatable("gui.signbuilder.lcd.storage.linked_multi_title", linkedCnt).getString();
                    } else {
                        BlockPos sp = master.getLinkedStoragePos();
                        posHeader = "● " + Component.translatable("gui.signbuilder.lcd.storage.linked_title").getString() + " [" + sp.getX() + ", " + sp.getY() + ", " + sp.getZ() + "]";
                    }
                    if (!this.watchlist.isEmpty()) {
                        posHeader += " • ★" + this.watchlist.size();
                    }
                    int percent = master.getStoragePercent();
                    int gaugeCol = (percent >= 90) ? 0xFFFF3333 : (percent >= 75 ? 0xFFFFB300 : 0xFF00FF88);
                    String rightStat = percent + "% • " + master.getStorageFilledSlots() + "/" + master.getStorageTotalSlots();
                    String netFlow = LcdScreenBlockEntity.formatFlowRateText(master.getStorageNetRatePerHour(), this.storageFlowMode > 0 ? this.storageFlowMode : 1, true, false);
                    if (!netFlow.isEmpty()) {
                        rightStat += " • " + netFlow;
                    }
                    int rightW = this.font.width(rightStat);

                    net.minecraft.network.chat.FormattedText trimmedHeader = this.font.substrByWidth(Component.literal(posHeader), Math.max(40, statW - rightW - 18));
                    guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimmedHeader), statX + 6, statY + 3, 0xFF00FFCC, true);
                    guiGraphics.drawString(this.font, rightStat, statX + statW - rightW - 6, statY + 3, gaugeCol, true);

                    int barX = statX + 6;
                    int barY = statY + 12;
                    int barW = statW - 12;
                    int barH = 3;
                    guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF15202E);
                    int fillW = Math.round(barW * (percent / 100.0f));
                    if (fillW == 0 && master.getStorageTotalItems() > 0) fillW = 2;
                    if (fillW > 0) {
                        guiGraphics.fill(barX, barY, barX + fillW, barY + barH, gaugeCol);
                    }
                }

                List<InspectorEntry> entries = buildInspectorEntries(master);
                boolean hasSearch = this.storageSearchQuery != null && !this.storageSearchQuery.trim().isEmpty();
                if (entries.isEmpty()) {
                    Component emptyComp = Component.translatable(hasSearch
                            ? "gui.signbuilder.lcd.storage.search_empty"
                            : "gui.signbuilder.lcd.storage.empty");
                    int ew = this.font.width(emptyComp);
                    guiGraphics.drawString(this.font, emptyComp, statX + (statW - ew) / 2, statY + 46, 0xFF6E8298, false);
                } else {
                    int totalRows = Math.max(1, (entries.size() + INSPECTOR_COLS - 1) / INSPECTOR_COLS);
                    int maxScroll = Math.max(0, totalRows - INSPECTOR_VIS_ROWS);
                    if (this.storageScrollRow > maxScroll) this.storageScrollRow = maxScroll;
                    if (this.storageScrollRow < 0) this.storageScrollRow = 0;

                    boolean hasScroll = totalRows > INSPECTOR_VIS_ROWS;
                    int availGridW = hasScroll ? (statW - 20) : (statW - 12);
                    int cellGap = 2;
                    int cellW = (availGridW - (INSPECTOR_COLS - 1) * cellGap) / INSPECTOR_COLS;
                    int cellH = 21;
                    int gridStartY = statY + 17;
                    boolean blink = (System.currentTimeMillis() / 420L) % 2L == 0L;

                    for (int r = 0; r < INSPECTOR_VIS_ROWS; r++) {
                        int actualRow = this.storageScrollRow + r;
                        for (int c = 0; c < INSPECTOR_COLS; c++) {
                            int idx = actualRow * INSPECTOR_COLS + c;
                            int cx = statX + 6 + c * (cellW + cellGap);
                            int cy = gridStartY + r * (cellH + cellGap);

                            if (idx >= entries.size()) {
                                guiGraphics.fill(cx, cy, cx + cellW, cy + cellH, 0xFF0E151E);
                                guiGraphics.renderOutline(cx, cy, cellW, cellH, 0xFF17212E);
                                continue;
                            }

                            InspectorEntry entry = entries.get(idx);
                            boolean isOut = !this.pinFromInventoryMode && entry.pinned() && entry.count() <= 0;
                            boolean hovered = mouseX >= cx && mouseX < cx + cellW && mouseY >= cy && mouseY < cy + cellH;

                            int cellBg;
                            int cellBorder;
                            if (isOut) {
                                cellBg = hovered ? 0xFF3A141D : 0xFF260D13;
                                cellBorder = hovered ? 0xFFFF8888 : (blink ? 0xFFFF3B3B : 0xFFAA2233);
                            } else if (entry.pinned()) {
                                cellBg = hovered ? 0xFF2B2818 : 0xFF1A1912;
                                cellBorder = hovered ? 0xFFFFEE55 : 0xFFD4A017;
                            } else {
                                cellBg = hovered ? 0xFF1A2A3C : 0xFF121C28;
                                cellBorder = hovered ? 0xFF00E5FF : 0xFF263A50;
                            }

                            guiGraphics.fill(cx, cy, cx + cellW, cy + cellH, cellBg);
                            guiGraphics.renderOutline(cx, cy, cellW, cellH, cellBorder);

                            if (!entry.stack().isEmpty()) {
                                guiGraphics.renderFakeItem(entry.stack(), cx + 2, cy + 2);
                            }

                            if (entry.pinned()) {
                                guiGraphics.pose().pushPose();
                                float pSc = 0.65f;
                                int sw = Math.round(this.font.width("★") * pSc);
                                guiGraphics.pose().translate(cx + cellW - sw - 2, cy + 2, 200.0f);
                                guiGraphics.pose().scale(pSc, pSc, 1.0f);
                                guiGraphics.drawString(this.font, "★", 0, 0, 0xFFFFD700, true);
                                guiGraphics.pose().popPose();
                            }

                            if (!this.pinFromInventoryMode && entry.rateHr() != 0) {
                                guiGraphics.pose().pushPose();
                                float fSc = 0.55f;
                                String arrow = entry.rateHr() > 0 ? "▲" : "▼";
                                int arrowCol = entry.rateHr() > 0 ? 0xFF22FF88 : 0xFFFF6644;
                                guiGraphics.pose().translate(cx + 2, cy + 2, 200.0f);
                                guiGraphics.pose().scale(fSc, fSc, 1.0f);
                                guiGraphics.drawString(this.font, arrow, 0, 0, arrowCol, true);
                                guiGraphics.pose().popPose();
                            }

                            String cntStr = isOut ? "x0" : LcdScreenBlockEntity.formatCompactCount(entry.count());
                            int cntCol = isOut ? 0xFFFF5555 : (this.pinFromInventoryMode ? 0xFFB8CCE0 : 0xFFFFCC44);
                            guiGraphics.pose().pushPose();
                            float sc = 0.65f;
                            int cw = Math.round(this.font.width(cntStr) * sc);
                            guiGraphics.pose().translate(cx + cellW - cw - 2, cy + cellH - 7, 200.0f);
                            guiGraphics.pose().scale(sc, sc, 1.0f);
                            guiGraphics.drawString(this.font, cntStr, 0, 0, cntCol, true);
                            guiGraphics.pose().popPose();

                            if (hovered) {
                                storageHoverLines = new ArrayList<>(5);
                                MutableComponent titleLine = Component.literal((entry.pinned() ? "★ " : "") + entry.name())
                                        .withStyle(entry.pinned() ? ChatFormatting.GOLD : ChatFormatting.WHITE);
                                if (isOut) {
                                    titleLine.append(Component.literal("  ⚠ ").withStyle(ChatFormatting.RED))
                                            .append(Component.translatable("gui.signbuilder.lcd.storage.out_of_stock").withStyle(ChatFormatting.RED));
                                } else {
                                    String shareSuffix = (!this.pinFromInventoryMode && entry.share() > 0) ? (" (" + entry.share() + "%)") : "";
                                    titleLine.append(Component.literal("  x" + entry.count() + shareSuffix).withStyle(ChatFormatting.YELLOW));
                                }
                                storageHoverLines.add(titleLine);

                                if (!this.pinFromInventoryMode && entry.rateHr() != 0) {
                                    int tipFlowMode = this.storageFlowMode > 0 ? this.storageFlowMode : 1;
                                    String flowTxt = LcdScreenBlockEntity.formatFlowRateText(entry.rateHr(), tipFlowMode, true, false);
                                    storageHoverLines.add(Component.literal(flowTxt)
                                            .withStyle(entry.rateHr() > 0 ? ChatFormatting.GREEN : ChatFormatting.GOLD));
                                }

                                if (entry.pinned()) {
                                    storageHoverLines.add(Component.translatable("gui.signbuilder.lcd.storage.pinned_status")
                                            .withStyle(ChatFormatting.GOLD));
                                }

                                if (this.pinFromInventoryMode) {
                                    storageHoverLines.add(Component.translatable("gui.signbuilder.lcd.storage.tip_inv_pin")
                                            .withStyle(ChatFormatting.AQUA));
                                } else {
                                    storageHoverLines.add(Component.translatable("gui.signbuilder.lcd.storage.tip_left_find")
                                            .withStyle(ChatFormatting.AQUA));
                                    storageHoverLines.add(Component.translatable("gui.signbuilder.lcd.storage.tip_right_pin")
                                            .withStyle(ChatFormatting.GRAY));
                                }
                            }
                        }
                    }

                    if (hasScroll) {
                        int sbX = statX + statW - 10;
                        int sbY = gridStartY;
                        int sbH = INSPECTOR_VIS_ROWS * cellH + (INSPECTOR_VIS_ROWS - 1) * cellGap;
                        guiGraphics.fill(sbX, sbY, sbX + 3, sbY + sbH, 0xFF141E2A);
                        int thumbH = Math.max(8, Math.round(sbH * ((float) INSPECTOR_VIS_ROWS / (float) totalRows)));
                        int thumbY = sbY + Math.round((sbH - thumbH) * ((float) this.storageScrollRow / (float) maxScroll));
                        guiGraphics.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, 0xFF00E5FF);
                    }
                }

                guiGraphics.fill(statX + 6, statY + 85, statX + statW - 6, statY + 86, 0x4424374C);
                Component quickHint = Component.translatable(this.pinFromInventoryMode
                        ? "gui.signbuilder.lcd.storage.quick_hint_inv"
                        : "gui.signbuilder.lcd.storage.quick_hint");
                drawScaledText(guiGraphics, this.font, quickHint,
                        statX + statW / 2.0f, statY + 87.5f, 0.68f,
                        this.pinFromInventoryMode ? 0xFFD4B54A : 0xFF7FA6C9, true);
            } else {
                Component warnTitle = Component.translatable("gui.signbuilder.lcd.storage.no_linked");
                Component warnDesc = Component.translatable("gui.signbuilder.lcd.storage.no_linked_desc");
                guiGraphics.drawString(this.font, warnTitle, statX + 10, statY + 24, 0xFFFFAA00, true);
                net.minecraft.network.chat.FormattedText trimmed = this.font.substrByWidth(warnDesc, statW - 20);
                guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimmed), statX + 10, statY + 40, 0xFF8EA0B4, false);
            }
        } else if (this.mainMode == 2 && this.storageTab == 1) {
            net.minecraft.world.level.Level level = this.minecraft != null ? this.minecraft.level : null;
            LcdScreenBlockEntity master = (level != null && this.masterPos != null && level.getBlockEntity(this.masterPos) instanceof LcdScreenBlockEntity be) ? be : null;

            int styleCardX = panelX + 14;
            int styleCardY = panelY + 111;
            int styleCardW = 328;
            int styleCardH = 52;
            guiGraphics.fill(styleCardX, styleCardY, styleCardX + styleCardW, styleCardY + styleCardH, 0xFF0B1119);
            guiGraphics.renderOutline(styleCardX, styleCardY, styleCardW, styleCardH, 0xFF263648);
            guiGraphics.fill(styleCardX, styleCardY, styleCardX + 3, styleCardY + styleCardH, 0xFFFFAA00);
            guiGraphics.fill(styleCardX + 3, styleCardY + 10, styleCardX + styleCardW - 2, styleCardY + 11, 0x26FFAA00);

            drawScaledText(guiGraphics, this.font,
                    Component.translatable("gui.signbuilder.lcd.storage.sec_style"),
                    styleCardX + 8.0f, styleCardY + 2.5f, 0.70f, 0xFFFFB833, false);

            Component activeStyleComp = Component.translatable("gui.signbuilder.lcd.storage.style." + Math.max(0, Math.min(5, this.storageStyle)));
            float activeStyleW = this.font.width(activeStyleComp) * 0.68f;
            drawScaledText(guiGraphics, this.font, activeStyleComp,
                    styleCardX + styleCardW - 6.0f - activeStyleW, styleCardY + 2.5f, 0.68f, 0xFFFFDF8E, false);

            int filterCardX = panelX + 14;
            int modCardY = panelY + 166;
            int modCardW = 161;
            int modCardH = 39;
            guiGraphics.fill(filterCardX, modCardY, filterCardX + modCardW, modCardY + modCardH, 0xFF0B1119);
            guiGraphics.renderOutline(filterCardX, modCardY, modCardW, modCardH, 0xFF333120);
            guiGraphics.fill(filterCardX, modCardY, filterCardX + 3, modCardY + modCardH, 0xFFFFCC22);
            guiGraphics.fill(filterCardX + 7, modCardY + 15, filterCardX + 7 + 149, modCardY + 15 + 19, 0xFF060A0F);
            guiGraphics.renderOutline(filterCardX + 6, modCardY + 14, 151, 21, 0xFF223040);

            drawScaledText(guiGraphics, this.font,
                    Component.translatable("gui.signbuilder.lcd.storage.sec_filter"),
                    filterCardX + 8.0f, modCardY + 3.5f, 0.68f, 0xFFFFD24C, false);
            String pinShort = "★" + this.watchlist.size() + "/" + LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS;
            float pinShortW = this.font.width(pinShort) * 0.66f;
            drawScaledText(guiGraphics, this.font, Component.literal(pinShort),
                    filterCardX + modCardW - 5.0f - pinShortW, modCardY + 3.5f, 0.66f, 0xFFFFE680, false);

            int flowCardX = panelX + 181;
            guiGraphics.fill(flowCardX, modCardY, flowCardX + modCardW, modCardY + modCardH, 0xFF0B1119);
            guiGraphics.renderOutline(flowCardX, modCardY, modCardW, modCardH, 0xFF1F3830);
            guiGraphics.fill(flowCardX, modCardY, flowCardX + 3, modCardY + modCardH, 0xFF00FF88);
            guiGraphics.fill(flowCardX + 7, modCardY + 15, flowCardX + 7 + 149, modCardY + 15 + 19, 0xFF060A0F);
            guiGraphics.renderOutline(flowCardX + 6, modCardY + 14, 151, 21, 0xFF223040);

            drawScaledText(guiGraphics, this.font,
                    Component.translatable("gui.signbuilder.lcd.storage.sec_flow"),
                    flowCardX + 8.0f, modCardY + 3.5f, 0.68f, 0xFF33FF99, false);
            if (master != null && master.hasLinkedStorage()) {
                int dispFlowMode = this.storageFlowMode > 0 ? this.storageFlowMode : 1;
                String liveFlow = LcdScreenBlockEntity.formatFlowRateText(master.getStorageNetRatePerHour(), dispFlowMode, true, true);
                int liveFlowCol = (this.storageFlowMode == 0)
                        ? 0xFF64788C
                        : (master.getStorageNetRatePerHour() > 0 ? 0xFF22FF88 : (master.getStorageNetRatePerHour() < 0 ? 0xFFFF6644 : 0xFF8EA0B4));
                float lfW = this.font.width(liveFlow) * 0.66f;
                drawScaledText(guiGraphics, this.font, Component.literal(liveFlow),
                        flowCardX + modCardW - 5.0f - lfW, modCardY + 3.5f, 0.66f, liveFlowCol, false);
            }

            int sumX = panelX + 14;
            int sumY = panelY + 208;
            int sumW = 328;
            int sumH = 20;
            guiGraphics.fill(sumX, sumY, sumX + sumW, sumY + sumH, 0xFF090E15);
            guiGraphics.renderOutline(sumX, sumY, sumW, sumH, 0xFF223244);

            if (master != null && master.hasLinkedStorage()) {
                guiGraphics.fill(sumX, sumY, sumX + 3, sumY + sumH, 0xFF00E5FF);
                int linkedCnt = master.getLinkedStorageCount();
                String leftTop = (linkedCnt > 1)
                        ? ("● " + Component.translatable("gui.signbuilder.lcd.storage.linked_multi_title", linkedCnt).getString())
                        : ("● " + Component.translatable("gui.signbuilder.lcd.storage.linked_title").getString());

                int pct = master.getStoragePercent();
                int gaugeCol = (pct >= 90) ? 0xFFFF3333 : (pct >= 75 ? 0xFFFFB300 : 0xFF00FF88);
                String slotInfo = pct + "% (" + master.getStorageFilledSlots() + "/" + master.getStorageTotalSlots() + ")";
                int slotInfoW = Math.round(this.font.width(slotInfo) * 0.75f);
                int miniBarW = 48;
                int miniBarX = sumX + sumW - miniBarW - 7;
                int miniBarY = sumY + 7;

                guiGraphics.fill(miniBarX, miniBarY, miniBarX + miniBarW, miniBarY + 6, 0xFF141F2C);
                int fillPx = Math.round((miniBarW - 2) * (pct / 100.0f));
                if (fillPx == 0 && master.getStorageTotalItems() > 0) fillPx = 2;
                if (fillPx > 0) {
                    guiGraphics.fill(miniBarX + 1, miniBarY + 1, miniBarX + 1 + fillPx, miniBarY + 5, gaugeCol);
                }
                guiGraphics.renderOutline(miniBarX, miniBarY, miniBarW, 6, 0xFF2B3E52);

                drawScaledText(guiGraphics, this.font, Component.literal(slotInfo),
                        miniBarX - slotInfoW - 6.0f, sumY + 7.0f, 0.75f, gaugeCol, false);

                int maxLeftW = Math.max(40, miniBarX - slotInfoW - sumX - 18);
                net.minecraft.network.chat.FormattedText trimTop = this.font.substrByWidth(Component.literal(leftTop), Math.round(maxLeftW / 0.78f));
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(sumX + 8.0f, sumY + 6.8f, 0.0f);
                guiGraphics.pose().scale(0.78f, 0.78f, 1.0f);
                guiGraphics.drawString(this.font, net.minecraft.locale.Language.getInstance().getVisualOrder(trimTop), 0, 0, 0xFF00FFCC, true);
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.fill(sumX, sumY, sumX + 3, sumY + sumH, 0xFFFFAA00);
                drawScaledText(guiGraphics, this.font, Component.translatable("gui.signbuilder.lcd.storage.no_linked"),
                        sumX + 8.0f, sumY + 6.8f, 0.78f, 0xFFFFAA00, false);
                Component noLinkDesc = Component.translatable("gui.signbuilder.lcd.storage.no_linked_desc");
                float descW = this.font.width(noLinkDesc) * 0.68f;
                if (descW < sumW - 120) {
                    drawScaledText(guiGraphics, this.font, noLinkDesc,
                            sumX + sumW - descW - 7.0f, sumY + 7.2f, 0.68f, 0xFF7D92A8, false);
                }
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (storageHoverLines != null && !storageHoverLines.isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, storageHoverLines, mouseX, mouseY);
        }
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

        int rawCol = this.rainbow ? com.boran.signbuilder.util.RainbowColorHelper.getRainbowRgb() : this.color;
        int renderCol = com.boran.signbuilder.client.render.LcdScreenBlockEntityRenderer.resolveTextThemeColor(rawCol, this.theme);

        long timeMs = System.currentTimeMillis();

        if (this.mainMode == 2) {
            renderStoragePreview(guiGraphics, x, y, width, height, rawCol, timeMs);
            guiGraphics.disableScissor();
            return;
        }

        if (this.mainMode == 1) {
            LoopStep focusStep = null;
            if (this.visualTab == 1 && this.loopSubView == 4 && this.loopTextStepIdx >= 0 && this.loopTextStepIdx < this.loopSteps.size()) {
                focusStep = this.loopSteps.get(this.loopTextStepIdx);
            } else if (this.visualTab == 1 && this.loopSubView == 3 && this.loopColorStepIdx >= 0 && this.loopColorStepIdx < this.loopSteps.size()) {
                focusStep = this.loopSteps.get(this.loopColorStepIdx);
            } else if (this.visualTab == 1 && this.loopSubView == 0 && timeMs < this.previewFocusUntilMs
                    && this.previewFocusStepIdx >= 0 && this.previewFocusStepIdx < this.loopSteps.size()) {
                focusStep = this.loopSteps.get(this.previewFocusStepIdx);
            }
            if (focusStep != null) {
                int stepSpec = focusStep.stepColor;
                boolean stepRainbow = (stepSpec == -2) || (stepSpec == -1 && this.rainbow);
                int stepRawCol = stepRainbow
                        ? RainbowColorHelper.getRainbowRgb()
                        : (stepSpec >= 0 ? stepSpec : this.color);
                int stepRenderCol = com.boran.signbuilder.client.render.LcdScreenBlockEntityRenderer.resolveTextThemeColor(stepRawCol, this.theme);
                if (focusStep.animId == 0) {
                    renderPreviewCornerWidgets(guiGraphics, clientLevel, x, y, width, height, stepRenderCol);
                    String pL1 = (this.stepText1Input != null && focusStep == getEditingTextStep()) ? this.stepText1Input.getValue() : focusStep.text1;
                    String pL2 = (this.stepText2Input != null && focusStep == getEditingTextStep()) ? this.stepText2Input.getValue() : focusStep.text2;
                    renderPreviewTextContent(guiGraphics, clientLevel, x, y, width, height, stepRenderCol, timeMs,
                            pL1, pL2, focusStep.align, focusStep.mode1, focusStep.mode2);
                } else {
                    LcdVisualAnimator.render2D(guiGraphics, x + 4, x + width - 4, y + 3, y + height - 3,
                            focusStep.animId, this.speed, stepRenderCol, stepRainbow, timeMs);
                }
                guiGraphics.disableScissor();
                return;
            }
        }

        if (this.mainMode == 1 && this.loopEnabled && !this.loopSteps.isEmpty()) {
            long seed = this.masterPos != null ? this.masterPos.asLong() : 0L;
            LcdScreenBlockEntity.LoopPlaybackState loopState = LcdScreenBlockEntity.resolveLoopPlayback(
                    buildLoopAnimsArray(),
                    buildLoopDurationsArray(),
                    buildLoopColorsArray(),
                    this.loopShuffle,
                    this.loopTransition,
                    seed,
                    timeMs
            );
            int stepSpec = loopState.stepColor();
            boolean stepRainbow = (stepSpec == -2) || (stepSpec == -1 && this.rainbow);
            int stepRawCol = stepRainbow
                    ? com.boran.signbuilder.util.RainbowColorHelper.getRainbowRgb()
                    : (stepSpec >= 0 ? stepSpec : this.color);
            int stepRenderCol = com.boran.signbuilder.client.render.LcdScreenBlockEntityRenderer.resolveTextThemeColor(stepRawCol, this.theme);

            if (loopState.animId() == 0) {
                renderPreviewCornerWidgets(guiGraphics, clientLevel, x, y, width, height, stepRenderCol);
                int sIdx = loopState.stepIndex();
                LoopStep sStep = (sIdx >= 0 && sIdx < this.loopSteps.size()) ? this.loopSteps.get(sIdx) : null;
                String pL1 = sStep != null ? sStep.text1 : "";
                String pL2 = sStep != null ? sStep.text2 : "";
                int sAlign = sStep != null ? sStep.align : -1;
                int sMode1 = sStep != null ? sStep.mode1 : -1;
                int sMode2 = sStep != null ? sStep.mode2 : -1;
                renderPreviewTextContent(guiGraphics, clientLevel, x, y, width, height, stepRenderCol, timeMs,
                        pL1, pL2, sAlign, sMode1, sMode2);
            } else {
                LcdVisualAnimator.render2D(guiGraphics, x + 4, x + width - 4, y + 3, y + height - 3,
                        loopState.animId(), this.speed, stepRenderCol, stepRainbow, timeMs);
            }
            if (loopState.effectiveTransition() > 0 && loopState.transitionIntensity() > 0.005f) {
                LcdVisualAnimator.renderTransition2D(guiGraphics, x + 2, x + width - 2, y + 2, y + height - 2,
                        loopState.effectiveTransition(), loopState.transitionIntensity(), loopState.transitionOutgoing(),
                        stepRenderCol, this.theme, timeMs);
            }
            guiGraphics.disableScissor();
            return;
        }

        if (this.mainMode == 1 && this.visualAnim > 0) {
            LcdVisualAnimator.render2D(guiGraphics, x + 4, x + width - 4, y + 3, y + height - 3,
                    this.visualAnim, this.speed, renderCol, this.rainbow, timeMs);
            guiGraphics.disableScissor();
            return;
        }

        if (this.mainMode == 0) {
            renderPreviewCornerWidgets(guiGraphics, clientLevel, x, y, width, height, renderCol);
        }
        renderPreviewTextContent(guiGraphics, clientLevel, x, y, width, height, renderCol, timeMs);

        guiGraphics.disableScissor();
    }

    private void renderPreviewCornerWidgets(GuiGraphics guiGraphics, net.minecraft.world.level.Level clientLevel,
                                            int x, int y, int width, int height, int renderCol) {
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

    private void renderPreviewTextContent(GuiGraphics guiGraphics, net.minecraft.world.level.Level clientLevel,
                                          int x, int y, int width, int height, int renderCol, long timeMs) {
        renderPreviewTextContent(guiGraphics, clientLevel, x, y, width, height, renderCol, timeMs, null, null, -1, -1, -1);
    }

    private void renderPreviewTextContent(GuiGraphics guiGraphics, net.minecraft.world.level.Level clientLevel,
                                          int x, int y, int width, int height, int renderCol, long timeMs,
                                          String customL1, String customL2, int customAlign, int customMode1, int customMode2) {
        String l1 = (customL1 != null && !customL1.isEmpty()) ? customL1 : (this.textField1 != null ? this.textField1.getValue() : this.line1);
        String l2 = (customL2 != null && !customL2.isEmpty()) ? customL2 : (this.textField2 != null ? this.textField2.getValue() : this.line2);
        int effAlign = (customAlign >= 0 && customAlign <= 2) ? customAlign : this.align;
        int effMode1 = (customMode1 >= 0 && customMode1 <= 9) ? customMode1 : this.mode;
        int effMode2 = (customMode2 >= 0 && customMode2 <= 9) ? customMode2 : this.mode2;

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

            renderPreviewSingleLine(guiGraphics, l1, effAlign, effMode1, pixelsPerSec, timeMs,
                    renderCol, x + 6, startY, width - 12, scale);
            renderPreviewSingleLine(guiGraphics, l2, effAlign, effMode2, pixelsPerSec, timeMs,
                    renderCol, x + 6, startY + (int) lineH, width - 12, scale);
        } else if (hasL1) {
            int lineY = y + (int) ((height - (this.font.lineHeight * scale)) / 2);
            renderPreviewSingleLine(guiGraphics, l1, effAlign, effMode1, pixelsPerSec, timeMs,
                    renderCol, x + 6, lineY, width - 12, scale);
        } else if (hasL2) {
            int lineY = y + (int) ((height - (this.font.lineHeight * scale)) / 2);
            renderPreviewSingleLine(guiGraphics, l2, effAlign, effMode2, pixelsPerSec, timeMs,
                    renderCol, x + 6, lineY, width - 12, scale);
        }
    }

    private void renderStoragePreview(GuiGraphics guiGraphics, int x, int y, int width, int height, int renderCol, long timeMs) {
        net.minecraft.world.level.Level level = this.minecraft != null ? this.minecraft.level : null;
        LcdScreenBlockEntity master = (level != null && this.masterPos != null && level.getBlockEntity(this.masterPos) instanceof LcdScreenBlockEntity be) ? be : null;
        LcdStorageRenderer.render2D(this.font, guiGraphics, master, x, y, width, height,
                this.widthBlocks, this.heightBlocks, this.storageStyle, this.speed,
                this.storageFilterMode, this.storageFlowMode, this.watchlist,
                this.theme, renderCol, this.glow, this.fontStyle, timeMs);
    }

    private void renderPreviewSingleLine(GuiGraphics guiGraphics, String lineText, int align, int mode,
                                         double pixelsPerSec, long timeMs, int renderCol,
                                         int x, int y, int width, float scale) {
        if (lineText.isEmpty()) return;

        boolean textShadow = this.glow && this.theme != 12;
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
            float factor = (this.theme == 12)
                    ? (float) (0.80 + 0.20 * Math.sin(timeMod * freq))
                    : (float) (0.625 + 0.375 * Math.sin(timeMod * freq));
            int r = (int) (((renderCol >> 16) & 0xFF) * factor);
            int g = (int) (((renderCol >> 8) & 0xFF) * factor);
            int b = (int) ((renderCol & 0xFF) * factor);
            renderCol = (r << 16) | (g << 8) | b;
        } else if (mode == 6) {
            renderPreviewWaveLine(guiGraphics, lineText, alignedBaseX, x, y, scale, renderCol, textShadow, this.speed, timeMs);
            return;
        } else if (mode == 7) {
            textToDraw = LcdTextFormatter.buildGlitchText(lineText, this.speed, timeMs);
        } else if (mode == 8) {
            renderPreviewRainbowLine(guiGraphics, lineText, alignedBaseX, x, y, scale, textShadow, this.speed, timeMs);
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
        guiGraphics.drawString(this.font, LcdFontHelper.toFormatted(textToDraw, this.fontStyle), (int) ((x / scale) + startX), (int) (y / scale), 0xFF000000 | renderCol, textShadow);
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
            boolean isEInk = (this.theme == 12);
            int charCol = 0xFF000000 | com.boran.signbuilder.util.RainbowColorHelper.hsbToRgb(hue, isEInk ? 0.92f : 0.88f, isEInk ? 0.60f : 1.0f);

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

    private static class StyleTileButton extends AbstractButton {
        private final String iconGlyph;
        private final Runnable onClick;
        private final BooleanSupplier isSelected;

        public StyleTileButton(int x, int y, int width, int height, String iconGlyph, Component message,
                               Runnable onClick, BooleanSupplier isSelected) {
            super(x, y, width, height, message);
            this.iconGlyph = iconGlyph != null ? iconGlyph : "";
            this.onClick = onClick;
            this.isSelected = isSelected;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = this.isSelected != null && this.isSelected.getAsBoolean();
            boolean hovered = this.isHovered();
            Font font = Minecraft.getInstance().font;

            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;
            int iconBoxW = 16;

            if (selected) {
                graphics.fillGradient(x, y, x + w, y + h, 0xFF38230B, 0xFF221507);
                graphics.fill(x + 1, y + 1, x + iconBoxW, y + h - 1, 0xFFFFAA00);
                graphics.renderOutline(x, y, w, h, 0xFFFFB833);
            } else {
                int bg = hovered ? 0xFF1D2938 : 0xFF121A24;
                int border = hovered ? 0xFF4F6882 : 0xFF212F3F;
                graphics.fill(x, y, x + w, y + h, bg);
                graphics.fill(x + 1, y + 1, x + iconBoxW, y + h - 1, hovered ? 0xFF141E2B : 0xFF0C1219);
                graphics.fill(x + iconBoxW, y + 2, x + iconBoxW + 1, y + h - 2, 0xFF1E2C3C);
                graphics.renderOutline(x, y, w, h, border);
            }

            int iconW = font.width(this.iconGlyph);
            int iconX = x + 1 + (iconBoxW - 1 - iconW) / 2;
            int iconY = y + (h - font.lineHeight) / 2 + 1;
            graphics.drawString(font, this.iconGlyph, iconX, iconY,
                    selected ? 0xFF170E03 : (hovered ? 0xFFFFC44D : 0xFFD49632), false);

            String raw = this.getMessage().getString();
            if (!this.iconGlyph.isEmpty() && raw.startsWith(this.iconGlyph)) {
                raw = raw.substring(this.iconGlyph.length()).trim();
            }
            Component cleanLabel = Component.literal(raw);
            int textCol = selected ? 0xFFFFFFFF : (hovered ? 0xFFE6EFF8 : 0xFF93A7BC);
            drawAutoScaledButtonText(graphics, font, cleanLabel, x + iconBoxW + 2, y, w - iconBoxW - 3, h, textCol);
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

    private static class SegmentedOptionButton extends AbstractButton {
        private final int segmentPos;
        private final Runnable onClick;
        private final BooleanSupplier isSelected;
        private final int activeAccent;
        private final int activeFill;

        public SegmentedOptionButton(int x, int y, int width, int height, int segmentPos, Component message,
                                     Runnable onClick, BooleanSupplier isSelected,
                                     int activeAccent, int activeFill) {
            super(x, y, width, height, message);
            this.segmentPos = segmentPos;
            this.onClick = onClick;
            this.isSelected = isSelected;
            this.activeAccent = activeAccent;
            this.activeFill = activeFill;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = this.isSelected != null && this.isSelected.getAsBoolean();
            boolean hovered = this.isHovered();
            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;

            if (selected) {
                graphics.fill(x, y, x + w, y + h, this.activeFill);
                graphics.renderOutline(x, y, w, h, this.activeAccent);
                graphics.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, this.activeAccent);
            } else {
                if (hovered) {
                    graphics.fill(x, y, x + w, y + h, 0xFF15202D);
                }
                if (this.segmentPos < 2) {
                    graphics.fill(x + w - 1, y + 3, x + w, y + h - 3, 0xFF1A2634);
                }
            }

            int textCol = selected ? 0xFFFFFFFF : (hovered ? 0xFFDCE8F5 : 0xFF7D93AA);
            drawAutoScaledButtonText(graphics, Minecraft.getInstance().font, this.getMessage(), x, y, w, h, textCol);
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

    private static class DynamicPillButton extends AbstractButton {
        private final Supplier<Component> labelSupplier;
        private final Runnable onLeftClick;
        private final Runnable onRightClick;
        private final BooleanSupplier isSelected;
        private final BooleanSupplier isEnabledSupplier;
        private final int activeBorderColor;
        private final int activeBgColor;

        public DynamicPillButton(int x, int y, int width, int height, Supplier<Component> labelSupplier,
                                 Runnable onLeftClick, BooleanSupplier isSelected,
                                 int activeBorderColor, int activeBgColor) {
            this(x, y, width, height, labelSupplier, onLeftClick, null, isSelected, null, activeBorderColor, activeBgColor);
        }

        public DynamicPillButton(int x, int y, int width, int height, Supplier<Component> labelSupplier,
                                 Runnable onLeftClick, Runnable onRightClick,
                                 BooleanSupplier isSelected, BooleanSupplier isEnabledSupplier,
                                 int activeBorderColor, int activeBgColor) {
            super(x, y, width, height, labelSupplier != null ? labelSupplier.get() : Component.empty());
            this.labelSupplier = labelSupplier;
            this.onLeftClick = onLeftClick;
            this.onRightClick = onRightClick;
            this.isSelected = isSelected;
            this.isEnabledSupplier = isEnabledSupplier;
            this.activeBorderColor = activeBorderColor;
            this.activeBgColor = activeBgColor;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.active = this.isEnabledSupplier == null || this.isEnabledSupplier.getAsBoolean();
            Component msg = this.labelSupplier != null ? this.labelSupplier.get() : this.getMessage();
            boolean selected = this.active && this.isSelected != null && this.isSelected.getAsBoolean();
            boolean hovered = this.active && this.isHovered();

            int bg = !this.active ? 0xFF10151C : (selected ? this.activeBgColor : (hovered ? 0xFF2A3646 : 0xFF171E27));
            int border = !this.active ? 0xFF1D2733 : (selected ? this.activeBorderColor : (hovered ? 0xFF5B6E84 : 0xFF2C3949));
            int textColor = !this.active ? 0xFF47586A : (selected ? 0xFFFFFFFF : (hovered ? 0xFFE2EBF5 : 0xFFA0B2C4));

            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, border);
            if (selected) {
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + 2, this.activeBorderColor);
            }
            drawAutoScaledButtonText(graphics, Minecraft.getInstance().font, msg, this.getX(), this.getY(), this.width, this.height, textColor);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.visible) return false;
            this.active = this.isEnabledSupplier == null || this.isEnabledSupplier.getAsBoolean();
            if (!this.active) return false;
            if (this.isMouseOver(mouseX, mouseY)) {
                if (button == 0) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    if (this.onLeftClick != null) this.onLeftClick.run();
                    return true;
                } else if (button == 1 && this.onRightClick != null) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onRightClick.run();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onPress() {
            if (this.onLeftClick != null) {
                this.onLeftClick.run();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static class DynamicAnimTileButton extends AbstractButton {
        private final IntSupplier animIdSupplier;
        private final IntConsumer onLeftClick;
        private final IntConsumer onRightClick;
        private final IntPredicate isSelected;
        private final IntFunction<String> badgeFn;
        private final int activeBorderColor;
        private final int activeBgColor;

        public DynamicAnimTileButton(int x, int y, int width, int height,
                                     IntSupplier animIdSupplier,
                                     IntConsumer onLeftClick, IntConsumer onRightClick,
                                     IntPredicate isSelected, IntFunction<String> badgeFn,
                                     int activeBorderColor, int activeBgColor) {
            super(x, y, width, height, Component.empty());
            this.animIdSupplier = animIdSupplier;
            this.onLeftClick = onLeftClick;
            this.onRightClick = onRightClick;
            this.isSelected = isSelected;
            this.badgeFn = badgeFn;
            this.activeBorderColor = activeBorderColor;
            this.activeBgColor = activeBgColor;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int animId = this.animIdSupplier != null ? this.animIdSupplier.getAsInt() : -1;
            if (animId <= 0) {
                this.active = false;
                return;
            }
            this.active = true;
            Component label = Component.translatable("gui.signbuilder.lcd.visual." + animId);
            boolean selected = this.isSelected != null && this.isSelected.test(animId);
            boolean hovered = this.isHovered();
            String badge = this.badgeFn != null ? this.badgeFn.apply(animId) : "";
            boolean inLoop = badge != null && !badge.isEmpty();

            int bg = selected ? this.activeBgColor : (inLoop ? 0xFF102430 : (hovered ? 0xFF2A3646 : 0xFF171E27));
            int border = selected ? this.activeBorderColor : (inLoop ? 0xFF00B8D4 : (hovered ? 0xFF5B6E84 : 0xFF2C3949));
            int textColor = selected ? 0xFFFFFFFF : (inLoop ? 0xFFD9F7FF : (hovered ? 0xFFE2EBF5 : 0xFFA0B2C4));

            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;

            graphics.fill(x, y, x + w, y + h, bg);
            graphics.renderOutline(x, y, w, h, border);
            if (selected) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + 2, this.activeBorderColor);
            } else if (inLoop) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF00E5FF);
            }

            Font font = Minecraft.getInstance().font;
            if (inLoop) {
                float bScale = 0.65f;
                int bTextW = Math.round(font.width(badge) * bScale);
                int bBoxW = bTextW + 4;
                int bx = x + w - bBoxW - 1;
                graphics.fill(bx, y + 1, x + w - 1, y + h - 1, 0xCC071822);
                drawScaledText(graphics, font, Component.literal(badge), bx + 2.0f, y + (h - font.lineHeight * bScale) / 2.0f + 0.5f, bScale, 0xFF33FF99, false);
                drawAutoScaledButtonText(graphics, font, label, x + 1, y, Math.max(20, w - bBoxW - 2), h, textColor);
            } else {
                drawAutoScaledButtonText(graphics, font, label, x, y, w, h, textColor);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.visible) return false;
            int animId = this.animIdSupplier != null ? this.animIdSupplier.getAsInt() : -1;
            if (animId <= 0) return false;
            if (this.isMouseOver(mouseX, mouseY)) {
                if (button == 0) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    if (this.onLeftClick != null) this.onLeftClick.accept(animId);
                    return true;
                } else if (button == 1 && this.onRightClick != null) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onRightClick.accept(animId);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onPress() {
            int animId = this.animIdSupplier != null ? this.animIdSupplier.getAsInt() : -1;
            if (animId > 0 && this.onLeftClick != null) {
                this.onLeftClick.accept(animId);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static class AnimTileButton extends AbstractButton {
        private final Runnable onLeftClick;
        private final Runnable onRightClick;
        private final BooleanSupplier isSelected;
        private final Supplier<String> badgeSupplier;
        private final int activeBorderColor;
        private final int activeBgColor;

        public AnimTileButton(int x, int y, int width, int height, Component message,
                              Runnable onLeftClick, Runnable onRightClick,
                              BooleanSupplier isSelected, Supplier<String> badgeSupplier,
                              int activeBorderColor, int activeBgColor) {
            super(x, y, width, height, message);
            this.onLeftClick = onLeftClick;
            this.onRightClick = onRightClick;
            this.isSelected = isSelected;
            this.badgeSupplier = badgeSupplier;
            this.activeBorderColor = activeBorderColor;
            this.activeBgColor = activeBgColor;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = this.isSelected != null && this.isSelected.getAsBoolean();
            boolean hovered = this.isHovered();
            String badge = this.badgeSupplier != null ? this.badgeSupplier.get() : "";
            boolean inLoop = badge != null && !badge.isEmpty();

            int bg = selected ? this.activeBgColor : (inLoop ? 0xFF102430 : (hovered ? 0xFF2A3646 : 0xFF171E27));
            int border = selected ? this.activeBorderColor : (inLoop ? 0xFF00B8D4 : (hovered ? 0xFF5B6E84 : 0xFF2C3949));
            int textColor = selected ? 0xFFFFFFFF : (inLoop ? 0xFFD9F7FF : (hovered ? 0xFFE2EBF5 : 0xFFA0B2C4));

            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;

            graphics.fill(x, y, x + w, y + h, bg);
            graphics.renderOutline(x, y, w, h, border);
            if (selected) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + 2, this.activeBorderColor);
            } else if (inLoop) {
                graphics.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF00E5FF);
            }

            Font font = Minecraft.getInstance().font;
            if (inLoop) {
                float bScale = 0.65f;
                int bTextW = Math.round(font.width(badge) * bScale);
                int bBoxW = bTextW + 4;
                int bx = x + w - bBoxW - 1;
                graphics.fill(bx, y + 1, x + w - 1, y + h - 1, 0xCC071822);
                drawScaledText(graphics, font, Component.literal(badge), bx + 2.0f, y + (h - font.lineHeight * bScale) / 2.0f + 0.5f, bScale, 0xFF33FF99, false);
                drawAutoScaledButtonText(graphics, font, this.getMessage(), x + 1, y, Math.max(20, w - bBoxW - 2), h, textColor);
            } else {
                drawAutoScaledButtonText(graphics, font, this.getMessage(), x, y, w, h, textColor);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.visible) return false;
            if (this.isMouseOver(mouseX, mouseY)) {
                if (button == 0) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    if (this.onLeftClick != null) this.onLeftClick.run();
                    return true;
                } else if (button == 1 && this.onRightClick != null) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onRightClick.run();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onPress() {
            if (this.onLeftClick != null) {
                this.onLeftClick.run();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static void drawScaledText(GuiGraphics graphics, Font font, Component text, float x, float y, float scale, int color, boolean centered) {
        graphics.pose().pushPose();
        float drawX = centered ? (x - (font.width(text) * scale) / 2.0f) : x;
        graphics.pose().translate(drawX, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
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
