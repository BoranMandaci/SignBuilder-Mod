package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.WrenchModeC2SPacket;
import com.boran.signbuilder.item.WrenchItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class WrenchScreen extends Screen {

    private int currentMode;
    private boolean detectsMonsters;
    private boolean detectsAnimals;
    private boolean isSmartFillEnabled;

    private int buttonMode = 0;
    private boolean syncWord = false;
    private int activeTab = 0;
    private String pinCode = "";
    private int customLightOnTicks = 10;
    private int customLightOffTicks = 10;
    private int customLightType = 0;
    private int customLightRange = 8;
    private int customLightOffRange = 8;
    private int customLightCloseDelayTicks = 0;
    private boolean customLightNightOnly = true;
    private boolean customLightPlayers = true;
    private boolean customLightLowPower = false;
    private boolean customLightLookOnly = true;
    private boolean customModeEditor = false;

    private EditBox pinInput;
    private EditBox customOnInput;
    private EditBox customOffInput;
    private EditBox customRangeInput;
    private EditBox customOffRangeInput;
    private EditBox customCloseDelayInput;

    private static final int[] MODE_VALUES = { 0, 1, 2, 3, 4, 8, 7, 10, 11 };
    private static final String[] MOD_KEYS = {
            "gui.signbuilder.wrench.mode.normal",
            "gui.signbuilder.wrench.mode.blink",
            "gui.signbuilder.wrench.mode.flicker",
            "gui.signbuilder.wrench.mode.wave",
            "gui.signbuilder.wrench.mode.breathing",
            "gui.signbuilder.wrench.mode.disco",
            "gui.signbuilder.wrench.mode.audio_sync",
            "gui.signbuilder.wrench.mode.low_power",
            "gui.signbuilder.wrench.mode.custom"
    };
    private static final String[] BUTTON_MODE_KEYS = {
            "gui.signbuilder.wrench.btn.disabled",
            "gui.signbuilder.wrench.btn.pulse",
            "gui.signbuilder.wrench.btn.toggle",
            "gui.signbuilder.wrench.btn.hold",
            "gui.signbuilder.wrench.btn.pin"
    };
    private static final int[] BUTTON_MODE_VALUES = { 0, 1, 2, 4, 3 };
    private static final int[] BUTTON_MODE_COLORS = { 0xAAAAAA, 0x55FF55, 0x55FFFF, 0x55AAFF, 0xFFAA00 };
    private static final String[] CUSTOM_LIGHT_TYPE_KEYS = {
            "gui.signbuilder.wrench.custom_cycle.type.cycle",
            "gui.signbuilder.wrench.custom_cycle.type.proximity",
            "gui.signbuilder.wrench.custom_cycle.type.schedule",
            "gui.signbuilder.wrench.custom_cycle.type.eye_contact"
    };

    private int panelWidth = 236;
    private int rowHeight = 18;
    private int panelHeight;
    private float redstoneScale = 1.0F;

    public WrenchScreen(int currentMode, boolean detectsMonsters, boolean detectsAnimals) {
        super(Component.literal("WRENCH MODES"));
        this.currentMode = currentMode;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
    }

    @Override
    protected void init() {
        super.init();

        panelWidth = Math.min(236, Math.max(0, this.width - 24));
        rowHeight = 18;
        redstoneScale = Math.max(0.35F, Math.min(1.0F, Math.min((this.height - 52.0F) / 180.0F, (this.height - 68.0F) / 176.0F)));
        updatePanelHeight();

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack mainItem = this.minecraft.player.getMainHandItem();
            ItemStack offItem = this.minecraft.player.getOffhandItem();

            CompoundTag mainTag = mainItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            CompoundTag offTag = offItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();

            this.isSmartFillEnabled = (mainTag != null && mainTag.getBoolean("IsSmartFill")) ||
                    (offTag != null && offTag.getBoolean("IsSmartFill"));

            CompoundTag targetTag = mainItem.getItem() instanceof WrenchItem
                    ? mainTag
                    : (offItem.getItem() instanceof WrenchItem ? offTag : (mainTag != null ? mainTag : offTag));
            if (targetTag != null) {
                if (targetTag.contains("ButtonMode")) this.buttonMode = targetTag.getInt("ButtonMode");
                if (targetTag.contains("SyncWord")) this.syncWord = targetTag.getBoolean("SyncWord");
                if (targetTag.contains("ActiveTab")) this.activeTab = targetTag.getInt("ActiveTab");
                if (targetTag.contains("PinCode")) this.pinCode = targetTag.getString("PinCode");
                if (targetTag.contains("CustomLightOnTicks")) this.customLightOnTicks = Math.max(1, Math.min(1200, targetTag.getInt("CustomLightOnTicks")));
                if (targetTag.contains("CustomLightOffTicks")) this.customLightOffTicks = Math.max(1, Math.min(1200, targetTag.getInt("CustomLightOffTicks")));
                if (targetTag.contains("CustomLightType")) this.customLightType = Math.max(0, Math.min(3, targetTag.getInt("CustomLightType")));
                if (targetTag.contains("CustomLightRange")) this.customLightRange = Math.max(1, Math.min(32, targetTag.getInt("CustomLightRange")));
                if (targetTag.contains("CustomLightOffRange")) this.customLightOffRange = Math.max(this.customLightRange, Math.min(32, targetTag.getInt("CustomLightOffRange")));
                else this.customLightOffRange = this.customLightRange;
                if (targetTag.contains("CustomLightCloseDelayTicks")) this.customLightCloseDelayTicks = Math.max(0, Math.min(100, targetTag.getInt("CustomLightCloseDelayTicks")));
                if (targetTag.contains("CustomLightNightOnly")) this.customLightNightOnly = targetTag.getBoolean("CustomLightNightOnly");
                if (targetTag.contains("CustomLightPlayers")) this.customLightPlayers = targetTag.getBoolean("CustomLightPlayers");
                if (targetTag.contains("CustomLightLowPower")) this.customLightLowPower = targetTag.getBoolean("CustomLightLowPower");
                if (targetTag.contains("CustomLightLookOnly")) this.customLightLookOnly = targetTag.getBoolean("CustomLightLookOnly");
            }
        }
        this.customLightOffRange = Math.max(this.customLightRange, Math.min(32, this.customLightOffRange));

        int centerX = this.width / 2;
        int startX = centerX - (panelWidth / 2);
        int startY = getPanelTop();
        int contentStartY = startY + 24;

        this.pinInput = new EditBox(this.font, startX + 12, contentStartY + scaledRedstone(124), Math.max(24, panelWidth - 64), scaledRedstone(16), Component.translatable("gui.signbuilder.wrench.pin.title"));
        this.pinInput.setMaxLength(16);
        this.pinInput.setValue(this.pinCode);
        this.pinInput.setResponder(text -> {
            this.pinCode = text;
            sendSyncPacket(false);
        });
        this.addWidget(this.pinInput);

        this.customOnInput = createCustomSecondsInput();
        this.customOffInput = createCustomSecondsInput();
        this.customRangeInput = new CenteredEditBox(this.font, 0, 0, scaledRedstone(40), getCycleControlButtonSize(), Component.translatable("gui.signbuilder.wrench.custom_cycle.open_range"));
        this.customRangeInput.setBordered(false);
        this.customRangeInput.setMaxLength(2);
        this.customRangeInput.setFilter(value -> value.matches("[0-9]{0,2}"));
        this.customRangeInput.setTextColor(SignBuilderUi.TEXT);
        this.customRangeInput.setValue(Integer.toString(this.customLightRange));
        this.customRangeInput.setResponder(value -> {
            if (!value.isEmpty()) {
                this.customLightRange = parseRange(value, this.customLightRange);
                if (this.customLightOffRange < this.customLightRange) {
                    this.customLightOffRange = this.customLightRange;
                    this.customOffRangeInput.setValue(Integer.toString(this.customLightOffRange));
                }
            }
        });
        this.addRenderableWidget(this.customRangeInput);
        this.customOffRangeInput = createRangeInput(Component.translatable("gui.signbuilder.wrench.custom_cycle.close_range"));
        this.customOffRangeInput.setValue(Integer.toString(this.customLightOffRange));
        this.customOffRangeInput.setResponder(value -> {
            if (!value.isEmpty()) {
                int parsedRange = parseRange(value, this.customLightOffRange);
                this.customLightOffRange = Math.max(this.customLightRange, Math.min(32, parsedRange));
            }
        });
        this.addRenderableWidget(this.customOffRangeInput);
        this.customCloseDelayInput = createCustomCloseDelayInput();
        this.customCloseDelayInput.setValue(formatCloseDelaySeconds(this.customLightCloseDelayTicks));
        this.customCloseDelayInput.setResponder(value -> this.customLightCloseDelayTicks = parseCloseDelayToTicks(value, this.customLightCloseDelayTicks));
        this.addRenderableWidget(this.customCloseDelayInput);
        this.customOnInput.setValue(formatInputSeconds(this.customLightOnTicks));
        this.customOffInput.setValue(formatInputSeconds(this.customLightOffTicks));
        this.customOnInput.setResponder(value -> this.customLightOnTicks = parseSecondsToTicks(value, this.customLightOnTicks));
        this.customOffInput.setResponder(value -> this.customLightOffTicks = parseSecondsToTicks(value, this.customLightOffTicks));
        this.addRenderableWidget(this.customOnInput);
        this.addRenderableWidget(this.customOffInput);
        updateCustomInputLayout();
        updateCustomInputVisibility();
        updatePinVisibility();
    }

    private EditBox createCustomSecondsInput() {
        EditBox input = new CenteredEditBox(this.font, 0, 0, getCycleValueWidth(0), getCycleControlButtonSize(), Component.translatable("gui.signbuilder.wrench.custom_cycle.seconds"));
        input.setBordered(false);
        input.setMaxLength(6);
        input.setFilter(value -> value.matches("[0-9]{0,3}([.,][0-9]{0,2})?"));
        input.setTextColor(SignBuilderUi.TEXT);
        return input;
    }

    private EditBox createRangeInput(Component narration) {
        EditBox input = new CenteredEditBox(this.font, 0, 0, scaledRedstone(40), getCycleControlButtonSize(), narration);
        input.setBordered(false);
        input.setMaxLength(2);
        input.setFilter(value -> value.matches("[0-9]{0,2}"));
        input.setTextColor(SignBuilderUi.TEXT);
        return input;
    }

    private EditBox createCustomCloseDelayInput() {
        EditBox input = new CenteredEditBox(this.font, 0, 0, getCycleValueWidth(0), getCycleControlButtonSize(), Component.translatable("gui.signbuilder.wrench.custom_cycle.close_delay"));
        input.setBordered(false);
        input.setMaxLength(4);
        input.setFilter(value -> value.isEmpty() || value.matches("[0-4]([.,][0-9]{0,2})?|5([.,]0{0,2})?"));
        input.setTextColor(SignBuilderUi.TEXT);
        return input;
    }

    private static final class CenteredEditBox extends EditBox {
        private final net.minecraft.client.gui.Font textFont;

        private CenteredEditBox(net.minecraft.client.gui.Font font, int x, int y, int width, int height, Component narration) {
            super(font, x, y, width, height, narration);
            this.textFont = font;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int originalX = this.getX();
            int textWidth = this.textFont.width(this.getValue());
            if (textWidth < this.getWidth()) {
                this.setX(originalX + (this.getWidth() - textWidth) / 2);
            }
            try {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            } finally {
                this.setX(originalX);
            }
        }
    }

    private void updateCustomInputLayout() {
        if (this.customOnInput == null) return;
        int startX = (this.width - panelWidth) / 2;
        int contentStartY = getPanelTop() + 24;
        int buttonSize = getCycleControlButtonSize();
        int gap = getCycleControlGap();
        int valueX = getCycleControlX(startX, this.customLightOnTicks) + buttonSize + gap;
        this.customOnInput.setX(valueX);
        this.customOnInput.setY(contentStartY + scaledRedstone(62) + getCycleInputYOffset());
        valueX = getCycleControlX(startX, this.customLightOffTicks) + buttonSize + gap;
        this.customOffInput.setX(valueX);
        this.customOffInput.setY(contentStartY + scaledRedstone(84) + getCycleInputYOffset());
        this.customRangeInput.setX(getRangeControlX(startX) + buttonSize + gap);
        this.customRangeInput.setY(contentStartY + scaledRedstone(58) + getCycleInputYOffset());
        this.customOffRangeInput.setX(getRangeControlX(startX) + buttonSize + gap);
        this.customOffRangeInput.setY(contentStartY + scaledRedstone(78) + getCycleInputYOffset());
        this.customCloseDelayInput.setX(getCycleControlX(startX, this.customLightCloseDelayTicks) + buttonSize + gap);
        this.customCloseDelayInput.setY(contentStartY + scaledRedstone(98) + getCycleInputYOffset());
    }

    private void updateCustomInputVisibility() {
        if (this.customOnInput == null) return;
        boolean editorVisible = this.activeTab == 0 && this.customModeEditor;
        this.customOnInput.setVisible(editorVisible && this.customLightType == 0);
        this.customOffInput.setVisible(editorVisible && this.customLightType == 0);
        this.customRangeInput.setVisible(editorVisible && this.customLightType == 1);
        this.customOffRangeInput.setVisible(editorVisible && this.customLightType == 1);
        this.customCloseDelayInput.setVisible(editorVisible && this.customLightType == 1);
        if (!this.customOnInput.isVisible()) this.customOnInput.setFocused(false);
        if (!this.customOffInput.isVisible()) this.customOffInput.setFocused(false);
        if (!this.customRangeInput.isVisible()) this.customRangeInput.setFocused(false);
        if (!this.customOffRangeInput.isVisible()) this.customOffRangeInput.setFocused(false);
        if (!this.customCloseDelayInput.isVisible()) this.customCloseDelayInput.setFocused(false);
        updatePanelHeight();
        updateCustomInputLayout();
    }

    private void commitCustomInputs() {
        this.customLightOnTicks = parseSecondsToTicks(this.customOnInput.getValue(), this.customLightOnTicks);
        this.customLightOffTicks = parseSecondsToTicks(this.customOffInput.getValue(), this.customLightOffTicks);
        this.customLightRange = parseRange(this.customRangeInput.getValue(), this.customLightRange);
        this.customLightOffRange = Math.max(this.customLightRange, parseRange(this.customOffRangeInput.getValue(), this.customLightOffRange));
        this.customLightCloseDelayTicks = parseCloseDelayToTicks(this.customCloseDelayInput.getValue(), this.customLightCloseDelayTicks);
        this.customOnInput.setValue(formatInputSeconds(this.customLightOnTicks));
        this.customOffInput.setValue(formatInputSeconds(this.customLightOffTicks));
        this.customRangeInput.setValue(Integer.toString(this.customLightRange));
        this.customOffRangeInput.setValue(Integer.toString(this.customLightOffRange));
        this.customCloseDelayInput.setValue(formatCloseDelaySeconds(this.customLightCloseDelayTicks));
        updateCustomInputLayout();
    }

    private int parseRange(String value, int fallback) {
        try {
            return Math.max(1, Math.min(32, Integer.parseInt(value)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private int parseSecondsToTicks(String value, int fallback) {
        try {
            double seconds = Double.parseDouble(value.replace(',', '.'));
            return (int) Math.max(1, Math.min(1200, Math.round(seconds * 20.0)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private int parseCloseDelayToTicks(String value, int fallback) {
        try {
            double seconds = Double.parseDouble(value.replace(',', '.'));
            return (int) Math.max(0, Math.min(100, Math.round(seconds * 20.0)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String formatInputSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.2f", ticks / 20.0);
    }

    private String formatCloseDelaySeconds(int ticks) {
        return String.format(Locale.ROOT, "%.2f", Math.max(0, Math.min(100, ticks)) / 20.0);
    }

    private void updatePinVisibility() {
        if (this.pinInput != null) {
            boolean visible = (this.activeTab == 1 && this.buttonMode == 3);
            this.pinInput.setVisible(visible);
            this.pinInput.setEditable(visible);
            int startX = (this.width - panelWidth) / 2;
            int startY = getPanelTop();
            int contentStartY = startY + 24;
            this.pinInput.setX(startX + 12);
            this.pinInput.setY(contentStartY + scaledRedstone(124));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        EditBox focusedCustomInput = getFocusedCustomInput();
        if (focusedCustomInput != null && keyCode != 256) {
            if (keyCode == 257 || keyCode == 335) {
                commitCustomInputs();
                focusedCustomInput.setFocused(false);
                this.setFocused(null);
                return true;
            }
            return focusedCustomInput.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.pinInput != null && this.pinInput.isVisible() && this.pinInput.isFocused()) {
            if (keyCode != 256) {
                return this.pinInput.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        EditBox focusedCustomInput = getFocusedCustomInput();
        if (focusedCustomInput != null) return focusedCustomInput.charTyped(codePoint, modifiers);
        if (this.pinInput != null && this.pinInput.isVisible() && this.pinInput.isFocused()) {
            return this.pinInput.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    private EditBox getFocusedCustomInput() {
        if (this.customOnInput != null && this.customOnInput.isVisible() && this.customOnInput.isFocused()) return this.customOnInput;
        if (this.customOffInput != null && this.customOffInput.isVisible() && this.customOffInput.isFocused()) return this.customOffInput;
        if (this.customRangeInput != null && this.customRangeInput.isVisible() && this.customRangeInput.isFocused()) return this.customRangeInput;
        if (this.customOffRangeInput != null && this.customOffRangeInput.isVisible() && this.customOffRangeInput.isFocused()) return this.customOffRangeInput;
        if (this.customCloseDelayInput != null && this.customCloseDelayInput.isVisible() && this.customCloseDelayInput.isFocused()) return this.customCloseDelayInput;
        return null;
    }


    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Leave empty to prevent double rendering
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        

        int centerX = this.width / 2;
        int startX = centerX - (panelWidth / 2);
        int startY = getPanelTop();

        SignBuilderUi.drawPanel(guiGraphics, startX, startY, panelWidth, panelHeight);
        SignBuilderUi.drawHeader(guiGraphics, this.font, startX, Math.max(2, startY - 25), panelWidth, this.title,
                Component.translatable(customModeEditor ? "gui.signbuilder.wrench.section.custom_light" : (activeTab == 0 ? "gui.signbuilder.wrench.tab.neon" : "gui.signbuilder.wrench.tab.redstone")));

        drawSmartFillIndicator(guiGraphics, startX + panelWidth - 12, startY + 8, mouseX, mouseY);

        renderTabs(guiGraphics, startX, startY, mouseX, mouseY);

        Component tooltipToRender = null;

        if (activeTab == 0) {
            tooltipToRender = renderNeonTab(guiGraphics, startX, startY + 24, mouseX, mouseY);
        } else {
            tooltipToRender = renderRedstoneTab(guiGraphics, startX, startY + 24, mouseX, mouseY, partialTick);
        }

        int promptY = Math.min(this.height - 10, startY + panelHeight + 6);
        SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.close_prompt"), centerX, promptY, 0x888888);

        if (tooltipToRender != null) {
            guiGraphics.renderTooltip(this.font, tooltipToRender, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        int tabY = startY + 4;
        int tabWidth = (panelWidth - 28) / 2;
        int tabHeight = 16;

        int neonX = startX + 6;
        int redstoneX = neonX + tabWidth + 4;

        boolean neonHover = mouseX >= neonX && mouseX <= neonX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight;
        boolean redstoneHover = mouseX >= redstoneX && mouseX <= redstoneX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight;

        int neonBg = (activeTab == 0) ? 0xFF244A52 : (neonHover ? 0xFF38414D : 0xFF222A34);
        int neonBorder = (activeTab == 0) ? 0xFF52DBC8 : 0xFF485463;
        guiGraphics.fill(neonX, tabY, neonX + tabWidth, tabY + tabHeight, neonBg);
        guiGraphics.renderOutline(neonX, tabY, tabWidth, tabHeight, neonBorder);
        SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.tab.neon"), neonX + tabWidth / 2, tabY + 4, activeTab == 0 ? 0x00FFFF : 0xAAAAAA);

        int redstoneBg = (activeTab == 1) ? 0xFF654624 : (redstoneHover ? 0xFF38414D : 0xFF222A34);
        int redstoneBorder = (activeTab == 1) ? SignBuilderUi.ACCENT : 0xFF485463;
        guiGraphics.fill(redstoneX, tabY, redstoneX + tabWidth, tabY + tabHeight, redstoneBg);
        guiGraphics.renderOutline(redstoneX, tabY, tabWidth, tabHeight, redstoneBorder);
        SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.tab.redstone"), redstoneX + tabWidth / 2, tabY + 4, activeTab == 1 ? 0xFFAA00 : 0xAAAAAA);
    }

    private Component renderNeonTab(GuiGraphics guiGraphics, int startX, int contentStartY, int mouseX, int mouseY) {
        if (customModeEditor) {
            return renderCustomCycleEditor(guiGraphics, startX, contentStartY, mouseX, mouseY);
        }
        long time = Util.getMillis();
        Component tooltipToRender = null;

        for (int i = 0; i < MOD_KEYS.length; i++) {
            int rowY = contentStartY + (i * rowHeight);
            int mode = MODE_VALUES[i];
            boolean isHovered = mouseX >= startX + 4 && mouseX <= startX + panelWidth - 4 && mouseY >= rowY && mouseY < rowY + rowHeight;

            if (mode == currentMode) {
                guiGraphics.fill(startX + 6, rowY + 1, startX + panelWidth - 6, rowY + rowHeight - 1, 0x3348CDBA);
                guiGraphics.fill(startX + 6, rowY + 1, startX + 8, rowY + rowHeight - 1, 0xFF52DBC8);
            } else if ((i & 1) == 0) {
                guiGraphics.fill(startX + 6, rowY + 1, startX + panelWidth - 6, rowY + rowHeight - 1, 0x11FFFFFF);
            }

            if (isHovered && tooltipToRender == null) {
                guiGraphics.fill(startX + 4, rowY, startX + panelWidth - 4, rowY + rowHeight, 0x22FFFFFF);
                tooltipToRender = Component.translatable(MOD_KEYS[i] + ".desc");
            }

            int alpha;
            int ledColorHex = 0x00FFFF;

            if (mode == 0) alpha = 255;
            else if (mode == 1) alpha = ((time / 250L) % 2 == 0) ? 255 : 40;
            else if (mode == 2) alpha = (Math.random() > 0.7) ? 255 : 40;
            else if (mode == 3) alpha = (int)(((Math.sin(time / 200.0) + 1.0) / 2.0) * 200) + 55;
            else if (mode == 4) alpha = (int)(((Math.sin(time / 600.0) + 1.0) / 2.0) * 200) + 55;
            else if (mode == 7) alpha = (Math.random() > 0.4) ? 255 : 40;
            else if (mode == 8) alpha = ((time / 100L) % 2 == 0) ? 255 : 40;
            else if (mode == 10) alpha = 130;
            else alpha = 255;

            int color = (alpha << 24) | ledColorHex;
            drawLedCircle(guiGraphics, startX + 16, rowY + (rowHeight / 2), color);

            Component text = Component.translatable(MOD_KEYS[i]);
            Component displayText = (mode == currentMode) ? Component.literal("> ").append(text) : text;

            int textXOffset = (mode == currentMode) ? 26 : 30;
            int textY = rowY + (rowHeight - 8) / 2;
            guiGraphics.drawString(this.font, displayText, startX + textXOffset, textY, mode == currentMode ? SignBuilderUi.TEXT : 0xFF78DCCF, true);
        }

        int turnOffY = contentStartY + (MOD_KEYS.length * rowHeight);
        boolean isTurnOffHovered = mouseX >= startX + 4 && mouseX <= startX + panelWidth - 4 && mouseY >= turnOffY && mouseY < turnOffY + rowHeight;
        if (isTurnOffHovered && tooltipToRender == null) {
            guiGraphics.fill(startX + 4, turnOffY, startX + panelWidth - 4, turnOffY + rowHeight, 0x44FF0000);
        }
        drawLedCircle(guiGraphics, startX + 16, turnOffY + (rowHeight / 2), 0xFF333333);

        Component offText = Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED);
        Component offDisplay = (currentMode == -1) ? Component.literal("> ").append(offText) : offText;
        int offTextOffset = (currentMode == -1) ? 26 : 30;
        int offTextY = turnOffY + (rowHeight - 8) / 2;
        guiGraphics.drawString(this.font, offDisplay, startX + offTextOffset, offTextY, 0xFF5555, true);

        return tooltipToRender;
    }

    private Component renderCustomCycleEditor(GuiGraphics guiGraphics, int startX, int contentStartY, int mouseX, int mouseY) {
        SignBuilderUi.drawSectionLabel(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.title"), startX + 10, contentStartY + scaledRedstone(2), panelWidth - 20);
        drawCustomTypeSelector(guiGraphics, startX, contentStartY, mouseX, mouseY);

        if (customLightType == 0) {
            int onY = contentStartY + scaledRedstone(62);
            int offY = contentStartY + scaledRedstone(84);
            guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.on"), startX + 12, onY + scaledRedstone(4), SignBuilderUi.TEXT, true);
            guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.off"), startX + 12, offY + scaledRedstone(4), SignBuilderUi.TEXT, true);
            drawCycleControl(guiGraphics, startX, onY, customLightOnTicks, mouseX, mouseY);
            drawCycleControl(guiGraphics, startX, offY, customLightOffTicks, mouseX, mouseY);
            drawCompactHint(guiGraphics, Component.translatable("gui.signbuilder.wrench.custom_cycle.hint"), startX, contentStartY + scaledRedstone(108));
        } else if (customLightType == 1) {
            int openRangeY = contentStartY + scaledRedstone(58);
            int closeRangeY = contentStartY + scaledRedstone(78);
            int closeDelayY = contentStartY + scaledRedstone(98);
            guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.open_range"), startX + 12, openRangeY + scaledRedstone(4), SignBuilderUi.TEXT, true);
            guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.close_range"), startX + 12, closeRangeY + scaledRedstone(4), SignBuilderUi.TEXT, true);
            guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.close_delay"), startX + 12, closeDelayY + scaledRedstone(4), SignBuilderUi.TEXT, true);
            drawRangeControl(guiGraphics, startX, openRangeY, mouseX, mouseY);
            drawRangeControl(guiGraphics, startX, closeRangeY, mouseX, mouseY);
            drawCycleControl(guiGraphics, startX, closeDelayY, customLightCloseDelayTicks, mouseX, mouseY);
            int filterY = contentStartY + scaledRedstone(116);
            int filterColumn = (panelWidth - scaledRedstone(20)) / 3;
            drawCustomFilter(guiGraphics, startX + scaledRedstone(8), filterY, Component.translatable("gui.signbuilder.wrench.custom_cycle.monsters"), detectsMonsters, mouseX, mouseY);
            drawCustomFilter(guiGraphics, startX + scaledRedstone(8) + filterColumn, filterY, Component.translatable("gui.signbuilder.wrench.custom_cycle.animals"), detectsAnimals, mouseX, mouseY);
            drawCustomFilter(guiGraphics, startX + scaledRedstone(8) + filterColumn * 2, filterY, Component.translatable("gui.signbuilder.wrench.custom_cycle.players"), customLightPlayers, mouseX, mouseY);
        } else if (customLightType == 2) {
            int choiceY = contentStartY + scaledRedstone(72);
            int gap = scaledRedstone(8);
            int choiceWidth = (panelWidth - scaledRedstone(32)) / 2;
            int nightX = startX + scaledRedstone(10);
            int dayX = nightX + choiceWidth + gap;
            int choiceHeight = scaledRedstone(22);
            boolean nightHovered = mouseX >= nightX && mouseX <= nightX + choiceWidth && mouseY >= choiceY && mouseY <= choiceY + choiceHeight;
            boolean dayHovered = mouseX >= dayX && mouseX <= dayX + choiceWidth && mouseY >= choiceY && mouseY <= choiceY + choiceHeight;
            drawEditorButton(guiGraphics, nightX, choiceY, choiceWidth, choiceHeight, Component.translatable("gui.signbuilder.wrench.custom_cycle.night"), nightHovered, customLightNightOnly);
            drawEditorButton(guiGraphics, dayX, choiceY, choiceWidth, choiceHeight, Component.translatable("gui.signbuilder.wrench.custom_cycle.day"), dayHovered, !customLightNightOnly);
            SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.schedule_hint"), startX + panelWidth / 2, contentStartY + scaledRedstone(106), 0xFF9AA8B8);
        } else {
            int choiceY = contentStartY + scaledRedstone(72);
            int gap = scaledRedstone(8);
            int choiceWidth = (panelWidth - scaledRedstone(32)) / 2;
            int lookX = startX + scaledRedstone(10);
            int notLookX = lookX + choiceWidth + gap;
            int choiceHeight = scaledRedstone(22);
            boolean lookHovered = mouseX >= lookX && mouseX <= lookX + choiceWidth && mouseY >= choiceY && mouseY <= choiceY + choiceHeight;
            boolean notLookHovered = mouseX >= notLookX && mouseX <= notLookX + choiceWidth && mouseY >= choiceY && mouseY <= choiceY + choiceHeight;
            drawEditorButton(guiGraphics, lookX, choiceY, choiceWidth, choiceHeight, Component.translatable("gui.signbuilder.wrench.custom_cycle.looked_at"), lookHovered, customLightLookOnly);
            drawEditorButton(guiGraphics, notLookX, choiceY, choiceWidth, choiceHeight, Component.translatable("gui.signbuilder.wrench.custom_cycle.not_looked_at"), notLookHovered, !customLightLookOnly);
            SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.custom_cycle.eye_contact_hint"), startX + panelWidth / 2, contentStartY + scaledRedstone(106), 0xFF9AA8B8);
        }

        int lowPowerY = contentStartY + scaledRedstone(customLightType == 1 ? 136 : 120);
        drawCustomFilter(guiGraphics, startX + scaledRedstone(12), lowPowerY,
                Component.translatable("gui.signbuilder.wrench.custom_cycle.low_power"), customLightLowPower, mouseX, mouseY);

        int buttonY = contentStartY + scaledRedstone(customLightType == 1 ? 154 : 140);
        int buttonHeight = scaledRedstone(20);
        int gap = scaledRedstone(8);
        int buttonWidth = (panelWidth - scaledRedstone(32)) / 2;
        int backX = startX + scaledRedstone(10);
        int applyX = backX + buttonWidth + gap;
        boolean backHovered = mouseX >= backX && mouseX <= backX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
        boolean applyHovered = mouseX >= applyX && mouseX <= applyX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
        drawEditorButton(guiGraphics, backX, buttonY, buttonWidth, buttonHeight, Component.translatable("gui.signbuilder.wrench.custom_cycle.back"), backHovered, false);
        drawEditorButton(guiGraphics, applyX, buttonY, buttonWidth, buttonHeight, Component.translatable("gui.signbuilder.wrench.custom_cycle.apply"), applyHovered, true);
        return null;
    }

    private void drawCustomTypeSelector(GuiGraphics guiGraphics, int startX, int contentStartY, int mouseX, int mouseY) {
        int y = contentStartY + scaledRedstone(18);
        int gap = scaledRedstone(4);
        int buttonWidth = (panelWidth - scaledRedstone(28)) / 2;
        int buttonHeight = scaledRedstone(16);
        for (int i = 0; i < CUSTOM_LIGHT_TYPE_KEYS.length; i++) {
            int x = startX + scaledRedstone(10) + (i % 2) * (buttonWidth + gap);
            int rowY = y + (i / 2) * (buttonHeight + gap);
            boolean hovered = mouseX >= x && mouseX <= x + buttonWidth && mouseY >= rowY && mouseY <= rowY + buttonHeight;
            drawEditorButton(guiGraphics, x, rowY, buttonWidth, buttonHeight, Component.translatable(CUSTOM_LIGHT_TYPE_KEYS[i]), hovered, customLightType == i);
        }
    }

    private void drawCompactHint(GuiGraphics guiGraphics, Component hint, int startX, int y) {
        int availableWidth = panelWidth - scaledRedstone(20);
        int textWidth = this.font.width(hint);
        float scale = textWidth > availableWidth ? availableWidth / (float) textWidth : 1.0F;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(startX + panelWidth / 2.0F, y, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, hint, 0, 0, 0xFF9AA8B8);
        guiGraphics.pose().popPose();
    }

    private void drawRangeControl(GuiGraphics guiGraphics, int startX, int rowY, int mouseX, int mouseY) {
        int buttonSize = getCycleControlButtonSize();
        int gap = getCycleControlGap();
        int valueWidth = scaledRedstone(40);
        int controlX = startX + panelWidth - scaledRedstone(10) - buttonSize * 2 - gap * 2 - valueWidth;
        drawEditorButton(guiGraphics, controlX, rowY, buttonSize, buttonSize, Component.literal("−"), mouseX >= controlX && mouseX <= controlX + buttonSize && mouseY >= rowY && mouseY <= rowY + buttonSize, false);
        int valueX = controlX + buttonSize + gap;
        guiGraphics.fill(valueX, rowY, valueX + valueWidth, rowY + buttonSize, 0xFF151C25);
        guiGraphics.renderOutline(valueX, rowY, valueWidth, buttonSize, 0xFF485463);
        int plusX = valueX + valueWidth + gap;
        drawEditorButton(guiGraphics, plusX, rowY, buttonSize, buttonSize, Component.literal("+"), mouseX >= plusX && mouseX <= plusX + buttonSize && mouseY >= rowY && mouseY <= rowY + buttonSize, false);
    }

    private boolean clickCustomInput(EditBox input, int rowY, double mouseX, double mouseY, int button) {
        int rowHeight = getCycleControlButtonSize();
        if (!input.isVisible() || mouseX < input.getX() || mouseX > input.getX() + input.getWidth()
                || mouseY < rowY || mouseY > rowY + rowHeight) return false;
        double inputY = Math.max(input.getY(), Math.min(mouseY, input.getY() + input.getHeight() - 1));
        input.mouseClicked(mouseX, inputY, button);
        return true;
    }

    private void focusCustomInput(EditBox focused) {
        this.customOnInput.setFocused(focused == this.customOnInput);
        this.customOffInput.setFocused(focused == this.customOffInput);
        this.customRangeInput.setFocused(focused == this.customRangeInput);
        this.customOffRangeInput.setFocused(focused == this.customOffRangeInput);
        this.customCloseDelayInput.setFocused(focused == this.customCloseDelayInput);
        this.setFocused(focused);
    }

    private void drawCustomFilter(GuiGraphics guiGraphics, int x, int y, Component label, boolean enabled, int mouseX, int mouseY) {
        int width = getCustomFilterWidth(label);
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + scaledRedstone(16);
        if (hovered) guiGraphics.fill(x - scaledRedstone(2), y, x + width, y + scaledRedstone(16), 0x33FFFFFF);
        int checkColor = enabled ? 0xFF55FF55 : 0xFF444444;
        int checkSize = scaledRedstone(11);
        guiGraphics.renderOutline(x, y + scaledRedstone(2), checkSize, checkSize, checkColor);
        if (enabled) {
            int inset = Math.max(1, scaledRedstone(2));
            guiGraphics.fill(x + inset, y + scaledRedstone(2) + inset, x + checkSize - inset, y + scaledRedstone(2) + checkSize - inset, 0xFF55FF55);
        }
        guiGraphics.drawString(this.font, label, x + scaledRedstone(16), y + scaledRedstone(4), enabled ? 0xFF55FF55 : 0xFFAAAAAA, true);
    }

    private int getCustomFilterWidth(Component label) {
        return this.font.width(label) + scaledRedstone(18);
    }

    private int getRangeControlX(int startX) {
        int buttonSize = getCycleControlButtonSize();
        int gap = getCycleControlGap();
        return startX + panelWidth - scaledRedstone(10) - buttonSize * 2 - gap * 2 - scaledRedstone(40);
    }

    private void drawCycleControl(GuiGraphics guiGraphics, int startX, int rowY, int ticks, int mouseX, int mouseY) {
        int controlX = getCycleControlX(startX, ticks);
        int buttonSize = getCycleControlButtonSize();
        int gap = getCycleControlGap();
        int valueWidth = getCycleValueWidth(ticks);
        drawEditorButton(guiGraphics, controlX, rowY, buttonSize, buttonSize, Component.literal("−"), mouseX >= controlX && mouseX <= controlX + buttonSize && mouseY >= rowY && mouseY <= rowY + buttonSize, false);
        int valueX = controlX + buttonSize + gap;
        guiGraphics.fill(valueX, rowY, valueX + valueWidth, rowY + buttonSize, 0xFF151C25);
        guiGraphics.renderOutline(valueX, rowY, valueWidth, buttonSize, 0xFF485463);
        int plusX = valueX + valueWidth + gap;
        drawEditorButton(guiGraphics, plusX, rowY, buttonSize, buttonSize, Component.literal("+"), mouseX >= plusX && mouseX <= plusX + buttonSize && mouseY >= rowY && mouseY <= rowY + buttonSize, false);
    }

    private int getCycleControlButtonSize() {
        return scaledRedstone(16);
    }

    private int getCycleInputYOffset() {
        return Math.max(0, (getCycleControlButtonSize() - 8) / 2);
    }

    private static String getModeTranslationKey(int mode) {
        for (int i = 0; i < MODE_VALUES.length; i++) {
            if (MODE_VALUES[i] == mode) return MOD_KEYS[i];
        }
        return "gui.signbuilder.wrench.mode.legacy";
    }

    private int getCycleControlGap() {
        return scaledRedstone(3);
    }

    private int getCycleValueWidth(int ticks) {
        return scaledRedstone(52);
    }

    private int getCycleControlX(int startX, int ticks) {
        int buttonSize = getCycleControlButtonSize();
        int gap = getCycleControlGap();
        int valueWidth = getCycleValueWidth(ticks);
        return startX + panelWidth - scaledRedstone(10) - (buttonSize * 2) - (gap * 2) - valueWidth;
    }

    private void drawEditorButton(GuiGraphics guiGraphics, int x, int y, int width, int height, Component label, boolean hovered, boolean primary) {
        guiGraphics.fill(x, y, x + width, y + height, primary ? (hovered ? 0xFF385A4D : 0xFF263D35) : (hovered ? 0xFF38414D : 0xFF222A34));
        guiGraphics.renderOutline(x, y, width, height, primary ? SignBuilderUi.ACCENT : 0xFF485463);
        SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, label, x + width / 2, y + Math.max(0, (height - 8) / 2), primary ? SignBuilderUi.TEXT : 0xFFCCCCCC);
    }

    private Component renderRedstoneTab(GuiGraphics guiGraphics, int startX, int contentStartY, int mouseX, int mouseY, float partialTick) {
        Component tooltip = null;

        SignBuilderUi.drawSectionLabel(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.section.button_mode"), startX + 10, contentStartY + scaledRedstone(2), panelWidth - 20);

        for (int i = 0; i < BUTTON_MODE_KEYS.length; i++) {
            int rowY = contentStartY + scaledRedstone(16) + (i * scaledRedstone(18));
            int rowHeight = scaledRedstone(16);
            boolean isHovered = mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= rowY && mouseY < rowY + rowHeight;
            if (isHovered) {
                guiGraphics.fill(startX + 8, rowY, startX + panelWidth - 8, rowY + rowHeight, 0x33FFFFFF);
                tooltip = Component.translatable(BUTTON_MODE_KEYS[i] + ".desc");
            }

            int modeValue = BUTTON_MODE_VALUES[i];
            int checkColor = (buttonMode == modeValue) ? 0xFF00FF00 : 0xFF444444;
            int checkSize = scaledRedstone(11);
            guiGraphics.renderOutline(startX + 12, rowY + scaledRedstone(2), checkSize, checkSize, checkColor);
            if (buttonMode == modeValue) {
                int inset = Math.max(1, scaledRedstone(2));
                guiGraphics.fill(startX + 12 + inset, rowY + scaledRedstone(2) + inset,
                        startX + 12 + checkSize - inset, rowY + scaledRedstone(2) + checkSize - inset, 0xFF00FF00);
            }

            guiGraphics.drawString(this.font, Component.translatable(BUTTON_MODE_KEYS[i]), startX + 28, rowY + scaledRedstone(4), BUTTON_MODE_COLORS[i], true);
        }

        if (this.buttonMode == 3) {
            SignBuilderUi.drawSectionLabel(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.pin.title"), startX + 10, contentStartY + scaledRedstone(112), panelWidth - 20);
            if (this.pinInput != null) {
                this.pinInput.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            int recX = startX + panelWidth - 48;
            int recWidth = scaledRedstone(36);
            int recHeight = scaledRedstone(16);
            int recY = contentStartY + scaledRedstone(124);
            boolean isRecHovered = mouseX >= recX && mouseX <= recX + recWidth && mouseY >= recY && mouseY <= recY + recHeight;
            guiGraphics.fill(recX, recY, recX + recWidth, recY + recHeight, isRecHovered ? 0xFF992222 : 0xFF661111);
            guiGraphics.renderOutline(recX, recY, recWidth, recHeight, isRecHovered ? 0xFFFF6666 : 0xFFFF3333);
            SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, "⏺ REC", recX + recWidth / 2, recY + scaledRedstone(4), 0xFFFFFF);

            if (isRecHovered) {
                tooltip = Component.translatable("gui.signbuilder.wrench.pin.rec_tooltip");
            }
        }

        int scopeTitleY = contentStartY + scaledRedstone(this.buttonMode == 3 ? 146 : 112);
        SignBuilderUi.drawSectionLabel(guiGraphics, this.font, Component.translatable("gui.signbuilder.wrench.scope.title"), startX + 10, scopeTitleY, panelWidth - 20);

        int scopeRowY = contentStartY + scaledRedstone(this.buttonMode == 3 ? 160 : 126);
        int scopeRowHeight = scaledRedstone(16);
        boolean isScopeHovered = mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= scopeRowY && mouseY < scopeRowY + scopeRowHeight;
        if (isScopeHovered) {
            guiGraphics.fill(startX + 8, scopeRowY, startX + panelWidth - 8, scopeRowY + scopeRowHeight, 0x33FFFFFF);
            tooltip = Component.translatable("gui.signbuilder.wrench.scope.desc");
        }

        int scopeCheckColor = syncWord ? 0xFF00FF00 : 0xFF444444;
        int scopeCheckSize = scaledRedstone(11);
        guiGraphics.renderOutline(startX + 12, scopeRowY + scaledRedstone(2), scopeCheckSize, scopeCheckSize, scopeCheckColor);
        if (syncWord) {
            int inset = Math.max(1, scaledRedstone(2));
            guiGraphics.fill(startX + 12 + inset, scopeRowY + scaledRedstone(2) + inset,
                    startX + 12 + scopeCheckSize - inset, scopeRowY + scaledRedstone(2) + scopeCheckSize - inset, 0xFF00FF00);
        }
        guiGraphics.drawString(this.font, Component.translatable(syncWord ? "gui.signbuilder.wrench.scope.word" : "gui.signbuilder.wrench.scope.single"), startX + 28, scopeRowY + scaledRedstone(4), syncWord ? 0x55FF55 : 0xAAAAAA, true);

        return tooltip;
    }

    private void drawSmartFillIndicator(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        int color = isSmartFillEnabled ? 0xFF00FF00 : 0xFFFF0000;
        int glowColor = isSmartFillEnabled ? 0x6600FF00 : 0x66FF0000;

        graphics.fill(x - 2, y - 2, x + 6, y + 6, 0xFF111111);
        graphics.fill(x - 1, y - 1, x + 5, y + 5, 0xFF333333);

        graphics.fill(x - 1, y - 1, x + 5, y + 5, glowColor);
        graphics.fill(x, y, x + 4, y + 4, color);
        graphics.fill(x, y, x + 2, y + 2, 0xAAFFFFFF);

        if (mouseX >= x - 2 && mouseX <= x + 6 && mouseY >= y - 2 && mouseY <= y + 6) {
            Component sfPrefix = Component.translatable("gui.signbuilder.smart_fill");
            Component sfState = Component.translatable(this.isSmartFillEnabled ? "gui.signbuilder.on" : "gui.signbuilder.off")
                    .withStyle(isSmartFillEnabled ? ChatFormatting.GREEN : ChatFormatting.RED);
            graphics.renderTooltip(this.font, sfPrefix.copy().append(": ").append(sfState), mouseX, mouseY);
        }
    }

    private void drawLedCircle(GuiGraphics guiGraphics, int x, int y, int color) {
        int bg = 0xFF111111;
        guiGraphics.fill(x - 2, y - 3, x + 3, y + 4, bg);
        guiGraphics.fill(x - 3, y - 2, x + 4, y + 3, bg);

        guiGraphics.fill(x - 1, y - 2, x + 2, y - 1, color);
        guiGraphics.fill(x - 2, y - 1, x + 3, y + 2, color);
        guiGraphics.fill(x - 1, y + 2, x + 2, y + 3, color);

        int alpha = (color >> 24) & 0xFF;
        if (alpha > 100) {
            guiGraphics.fill(x - 1, y - 1, x, y, 0x88FFFFFF);
        }
    }

    private void sendSyncPacket(boolean startRecording) {
        ModMessages.sendToServer(new WrenchModeC2SPacket(currentMode, this.detectsMonsters, this.detectsAnimals, this.buttonMode, this.syncWord, this.activeTab, this.pinCode, startRecording, this.customLightOnTicks, this.customLightOffTicks, this.customLightType, this.customLightRange, this.customLightOffRange, this.customLightCloseDelayTicks, this.customLightNightOnly, this.customLightPlayers, this.customLightLowPower, this.customLightLookOnly));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.activeTab == 0 && this.customModeEditor) {
            int contentStartY = getPanelTop() + 24;
            if (clickCustomInput(this.customOnInput, contentStartY + scaledRedstone(62), mouseX, mouseY, button)) {
                focusCustomInput(this.customOnInput);
                return true;
            }
            if (clickCustomInput(this.customOffInput, contentStartY + scaledRedstone(84), mouseX, mouseY, button)) {
                focusCustomInput(this.customOffInput);
                return true;
            }
            if (clickCustomInput(this.customRangeInput, contentStartY + scaledRedstone(58), mouseX, mouseY, button)) {
                focusCustomInput(this.customRangeInput);
                return true;
            }
            if (clickCustomInput(this.customOffRangeInput, contentStartY + scaledRedstone(78), mouseX, mouseY, button)) {
                focusCustomInput(this.customOffRangeInput);
                return true;
            }
            if (clickCustomInput(this.customCloseDelayInput, contentStartY + scaledRedstone(98), mouseX, mouseY, button)) {
                focusCustomInput(this.customCloseDelayInput);
                return true;
            }
            boolean hadFocusedInput = this.customOnInput.isFocused() || this.customOffInput.isFocused() || this.customRangeInput.isFocused() || this.customOffRangeInput.isFocused() || this.customCloseDelayInput.isFocused();
            this.customOnInput.setFocused(false);
            this.customOffInput.setFocused(false);
            this.customRangeInput.setFocused(false);
            this.customOffRangeInput.setFocused(false);
            this.customCloseDelayInput.setFocused(false);
            if (hadFocusedInput) commitCustomInputs();
            this.setFocused(null);
        }
        if (button == 0) {
            int startX = (this.width - panelWidth) / 2;
            int startY = getPanelTop();

            int tabY = startY + 4;
            int tabWidth = (panelWidth - 28) / 2;
            int tabHeight = 16;
            int neonX = startX + 6;
            int redstoneX = neonX + tabWidth + 4;

            if (mouseX >= neonX && mouseX <= neonX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight) {
                this.activeTab = 0;
                updatePanelHeight();
                updateCustomInputVisibility();
                updatePinVisibility();
                sendSyncPacket(false);
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                }
                return true;
            }

            if (mouseX >= redstoneX && mouseX <= redstoneX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight) {
                this.activeTab = 1;
                this.customModeEditor = false;
                updatePanelHeight();
                updateCustomInputVisibility();
                updatePinVisibility();
                sendSyncPacket(false);
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.2F);
                }
                return true;
            }

            int contentStartY = startY + 24;

            if (activeTab == 0) {
                if (customModeEditor) {
                    int selectorY = contentStartY + scaledRedstone(18);
                    int selectorGap = scaledRedstone(4);
                    int selectorWidth = (panelWidth - scaledRedstone(28)) / 2;
                    int selectorHeight = scaledRedstone(16);
                    for (int i = 0; i < 4; i++) {
                        int selectorX = startX + scaledRedstone(10) + (i % 2) * (selectorWidth + selectorGap);
                        int selectorRowY = selectorY + (i / 2) * (selectorHeight + selectorGap);
                        if (mouseX >= selectorX && mouseX <= selectorX + selectorWidth && mouseY >= selectorRowY && mouseY <= selectorRowY + selectorHeight) {
                            commitCustomInputs();
                            this.customLightType = i;
                            updateCustomInputVisibility();
                            return true;
                        }
                    }

                    int lowPowerY = contentStartY + scaledRedstone(customLightType == 1 ? 136 : 120);
                    int lowPowerX = startX + scaledRedstone(12);
                    int lowPowerWidth = getCustomFilterWidth(Component.translatable("gui.signbuilder.wrench.custom_cycle.low_power"));
                    if (mouseY >= lowPowerY && mouseY <= lowPowerY + scaledRedstone(16)
                            && mouseX >= lowPowerX && mouseX <= lowPowerX + lowPowerWidth) {
                        this.customLightLowPower = !this.customLightLowPower;
                        return true;
                    }

                    int buttonY = contentStartY + scaledRedstone(customLightType == 1 ? 154 : 140);
                    int buttonHeight = scaledRedstone(20);
                    int gap = scaledRedstone(8);
                    int buttonWidth = (panelWidth - scaledRedstone(32)) / 2;
                    int backX = startX + scaledRedstone(10);
                    int applyX = backX + buttonWidth + gap;
                    if (mouseX >= backX && mouseX <= backX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                        commitCustomInputs();
                        this.customModeEditor = false;
                        updatePanelHeight();
                        updateCustomInputVisibility();
                        return true;
                    }
                    if (mouseX >= applyX && mouseX <= applyX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                        commitCustomInputs();
                        this.currentMode = MODE_VALUES[MODE_VALUES.length - 1];
                        sendSyncPacket(false);
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.displayClientMessage(
                                    Component.translatable("message.signbuilder.wrench.mode_selected")
                                            .withStyle(ChatFormatting.YELLOW)
                                            .append(Component.translatable(getModeTranslationKey(this.currentMode)).withStyle(ChatFormatting.AQUA)),
                                    true
                            );
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.5F);
                        }
                        if (this.minecraft != null) this.minecraft.setScreen(null);
                        return true;
                    }

                    int buttonSize = getCycleControlButtonSize();
                    int step = Screen.hasShiftDown() ? 100 : 20;
                    if (customLightType == 0) {
                        int onY = contentStartY + scaledRedstone(62);
                        int offY = contentStartY + scaledRedstone(84);
                        int onControlX = getCycleControlX(startX, customLightOnTicks);
                        int onPlusX = onControlX + buttonSize + getCycleControlGap() + getCycleValueWidth(customLightOnTicks) + getCycleControlGap();
                        if (mouseY >= onY && mouseY <= onY + buttonSize) {
                            if (mouseX >= onControlX && mouseX <= onControlX + buttonSize) {
                                this.customLightOnTicks = Math.max(1, this.customLightOnTicks - step);
                                this.customOnInput.setValue(formatInputSeconds(this.customLightOnTicks));
                                return true;
                            }
                            if (mouseX >= onPlusX && mouseX <= onPlusX + buttonSize) {
                                this.customLightOnTicks = Math.min(1200, this.customLightOnTicks + step);
                                this.customOnInput.setValue(formatInputSeconds(this.customLightOnTicks));
                                return true;
                            }
                        }
                        int offControlX = getCycleControlX(startX, customLightOffTicks);
                        int offPlusX = offControlX + buttonSize + getCycleControlGap() + getCycleValueWidth(customLightOffTicks) + getCycleControlGap();
                        if (mouseY >= offY && mouseY <= offY + buttonSize) {
                            if (mouseX >= offControlX && mouseX <= offControlX + buttonSize) {
                                this.customLightOffTicks = Math.max(1, this.customLightOffTicks - step);
                                this.customOffInput.setValue(formatInputSeconds(this.customLightOffTicks));
                                return true;
                            }
                            if (mouseX >= offPlusX && mouseX <= offPlusX + buttonSize) {
                                this.customLightOffTicks = Math.min(1200, this.customLightOffTicks + step);
                                this.customOffInput.setValue(formatInputSeconds(this.customLightOffTicks));
                                return true;
                            }
                        }
                    } else if (customLightType == 1) {
                        int openRangeY = contentStartY + scaledRedstone(58);
                        int closeRangeY = contentStartY + scaledRedstone(78);
                        int rangeX = getRangeControlX(startX);
                        int rangePlusX = rangeX + buttonSize + getCycleControlGap() + scaledRedstone(40) + getCycleControlGap();
                        int rangeStep = Screen.hasShiftDown() ? 8 : 1;
                        if (mouseY >= openRangeY && mouseY <= openRangeY + buttonSize) {
                            if (mouseX >= rangeX && mouseX <= rangeX + buttonSize) {
                                this.customLightRange = Math.max(1, this.customLightRange - rangeStep);
                                this.customLightOffRange = Math.max(this.customLightOffRange, this.customLightRange);
                                this.customRangeInput.setValue(Integer.toString(this.customLightRange));
                                this.customOffRangeInput.setValue(Integer.toString(this.customLightOffRange));
                                return true;
                            }
                            if (mouseX >= rangePlusX && mouseX <= rangePlusX + buttonSize) {
                                this.customLightRange = Math.min(32, this.customLightRange + rangeStep);
                                this.customRangeInput.setValue(Integer.toString(this.customLightRange));
                                return true;
                            }
                        }
                        if (mouseY >= closeRangeY && mouseY <= closeRangeY + buttonSize) {
                            if (mouseX >= rangeX && mouseX <= rangeX + buttonSize) {
                                this.customLightOffRange = Math.max(this.customLightRange, this.customLightOffRange - rangeStep);
                                this.customOffRangeInput.setValue(Integer.toString(this.customLightOffRange));
                                return true;
                            }
                            if (mouseX >= rangePlusX && mouseX <= rangePlusX + buttonSize) {
                                this.customLightOffRange = Math.min(32, this.customLightOffRange + rangeStep);
                                this.customOffRangeInput.setValue(Integer.toString(this.customLightOffRange));
                                return true;
                            }
                        }

                        int closeDelayY = contentStartY + scaledRedstone(98);
                        int closeDelayControlX = getCycleControlX(startX, customLightCloseDelayTicks);
                        int closeDelayPlusX = closeDelayControlX + buttonSize + getCycleControlGap() + getCycleValueWidth(customLightCloseDelayTicks) + getCycleControlGap();
                        int closeDelayStep = Screen.hasShiftDown() ? 100 : 20;
                        if (mouseY >= closeDelayY && mouseY <= closeDelayY + buttonSize) {
                            if (mouseX >= closeDelayControlX && mouseX <= closeDelayControlX + buttonSize) {
                                this.customLightCloseDelayTicks = Math.max(0, this.customLightCloseDelayTicks - closeDelayStep);
                                this.customCloseDelayInput.setValue(formatCloseDelaySeconds(this.customLightCloseDelayTicks));
                                return true;
                            }
                            if (mouseX >= closeDelayPlusX && mouseX <= closeDelayPlusX + buttonSize) {
                                this.customLightCloseDelayTicks = Math.min(100, this.customLightCloseDelayTicks + closeDelayStep);
                                this.customCloseDelayInput.setValue(formatCloseDelaySeconds(this.customLightCloseDelayTicks));
                                return true;
                            }
                        }

                        int filterY = contentStartY + scaledRedstone(116);
                        int filterHeight = scaledRedstone(16);
                        int filterColumn = (panelWidth - scaledRedstone(20)) / 3;
                        int monsterX = startX + scaledRedstone(8);
                        int animalX = monsterX + filterColumn;
                        int playerX = monsterX + filterColumn * 2;
                        int monsterWidth = getCustomFilterWidth(Component.translatable("gui.signbuilder.wrench.custom_cycle.monsters"));
                        int animalWidth = getCustomFilterWidth(Component.translatable("gui.signbuilder.wrench.custom_cycle.animals"));
                        int playerWidth = getCustomFilterWidth(Component.translatable("gui.signbuilder.wrench.custom_cycle.players"));
                        if (mouseY >= filterY && mouseY <= filterY + filterHeight) {
                            if (mouseX >= monsterX && mouseX <= monsterX + monsterWidth) {
                                this.detectsMonsters = !this.detectsMonsters;
                                return true;
                            }
                            if (mouseX >= animalX && mouseX <= animalX + animalWidth) {
                                this.detectsAnimals = !this.detectsAnimals;
                                return true;
                            }
                            if (mouseX >= playerX && mouseX <= playerX + playerWidth) {
                                this.customLightPlayers = !this.customLightPlayers;
                                return true;
                            }
                        }
                    } else if (customLightType == 2) {
                        int choiceY = contentStartY + scaledRedstone(72);
                        int choiceGap = scaledRedstone(8);
                        int choiceWidth = (panelWidth - scaledRedstone(32)) / 2;
                        int nightX = startX + scaledRedstone(10);
                        int dayX = nightX + choiceWidth + choiceGap;
                        int choiceHeight = scaledRedstone(22);
                        if (mouseY >= choiceY && mouseY <= choiceY + choiceHeight) {
                            if (mouseX >= nightX && mouseX <= nightX + choiceWidth) {
                                this.customLightNightOnly = true;
                                return true;
                            }
                            if (mouseX >= dayX && mouseX <= dayX + choiceWidth) {
                                this.customLightNightOnly = false;
                                return true;
                            }
                        }
                    } else {
                        int choiceY = contentStartY + scaledRedstone(72);
                        int choiceGap = scaledRedstone(8);
                        int choiceWidth = (panelWidth - scaledRedstone(32)) / 2;
                        int lookX = startX + scaledRedstone(10);
                        int notLookX = lookX + choiceWidth + choiceGap;
                        int choiceHeight = scaledRedstone(22);
                        if (mouseY >= choiceY && mouseY <= choiceY + choiceHeight) {
                            if (mouseX >= lookX && mouseX <= lookX + choiceWidth) {
                                this.customLightLookOnly = true;
                                return true;
                            }
                            if (mouseX >= notLookX && mouseX <= notLookX + choiceWidth) {
                                this.customLightLookOnly = false;
                                return true;
                            }
                        }
                    }
                    return true;
                }

                for (int i = 0; i < MOD_KEYS.length; i++) {
                    int rowY = contentStartY + (i * rowHeight);

                    if (mouseX >= startX + 4 && mouseX <= startX + panelWidth - 4 && mouseY >= rowY && mouseY < rowY + rowHeight) {
                        if (i == MOD_KEYS.length - 1) {
                            this.customModeEditor = true;
                            updatePanelHeight();
                            updateCustomInputVisibility();
                            return true;
                        }
                        this.currentMode = MODE_VALUES[i];
                        sendSyncPacket(false);
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.displayClientMessage(
                                    Component.translatable("message.signbuilder.wrench.mode_selected")
                                            .withStyle(ChatFormatting.YELLOW)
                                            .append(Component.translatable(MOD_KEYS[i]).withStyle(ChatFormatting.AQUA)),
                                    true
                            );
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.5F);
                        }
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(null);
                        }
                        return true;
                    }
                }

                int turnOffY = contentStartY + (MOD_KEYS.length * rowHeight);
                if (mouseX >= startX + 4 && mouseX <= startX + panelWidth - 4 && mouseY >= turnOffY && mouseY < turnOffY + rowHeight) {
                    this.currentMode = -1;
                    sendSyncPacket(false);
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(
                                Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED), true
                        );
                        this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.5F);
                    }
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(null);
                    }
                    return true;
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    int rowY = contentStartY + scaledRedstone(16) + (i * scaledRedstone(18));
                    int redstoneRowHeight = scaledRedstone(16);
                    if (mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= rowY && mouseY < rowY + redstoneRowHeight) {
                        this.buttonMode = BUTTON_MODE_VALUES[i];
                        updatePinVisibility();
                        sendSyncPacket(false);
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.2F);
                        }
                        return true;
                    }
                }

                if (this.buttonMode == 3) {
                    int recX = startX + panelWidth - 48;
                    int recY = contentStartY + scaledRedstone(124);
                    int recWidth = scaledRedstone(36);
                    int recHeight = scaledRedstone(16);
                    if (mouseX >= recX && mouseX <= recX + recWidth && mouseY >= recY && mouseY <= recY + recHeight) {
                        sendSyncPacket(true);
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.5F);
                        }
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(null);
                        }
                        return true;
                    }
                }

                int scopeRowY = contentStartY + scaledRedstone(this.buttonMode == 3 ? 160 : 126);
                int scopeRowHeight = scaledRedstone(16);
                if (mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= scopeRowY && mouseY < scopeRowY + scopeRowHeight) {
                    this.syncWord = !this.syncWord;
                    sendSyncPacket(false);
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, this.syncWord ? 1.4F : 0.8F);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getPanelTop() {
        return Math.max(28, (this.height - panelHeight) / 2);
    }

    private void updatePanelHeight() {
        int maxAvailableHeight = this.height - 54;
        int neonHeight = 26 + ((MOD_KEYS.length + 1) * rowHeight) + 10;
        while (!customModeEditor && neonHeight > maxAvailableHeight && rowHeight > 8) {
            rowHeight--;
            neonHeight = 26 + ((MOD_KEYS.length + 1) * rowHeight) + 10;
        }
        panelHeight = customModeEditor
                ? 24 + scaledRedstone(customLightType == 1 ? 176 : 170)
                : Math.max(neonHeight, 24 + scaledRedstone(176));
    }

    private int scaledRedstone(int value) { return value; }

}
