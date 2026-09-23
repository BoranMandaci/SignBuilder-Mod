package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.WrenchModeC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class WrenchScreen extends Screen {

    private int currentMode;
    private boolean detectsMonsters;
    private boolean detectsAnimals;
    private boolean isSmartFillEnabled;

    private int buttonMode = 0;
    private boolean syncWord = false;
    private int activeTab = 0;
    private String pinCode = "";

    private EditBox pinInput;

    private final String[] MOD_KEYS = {
            "gui.signbuilder.wrench.mode.normal",
            "gui.signbuilder.wrench.mode.blink",
            "gui.signbuilder.wrench.mode.flicker",
            "gui.signbuilder.wrench.mode.wave",
            "gui.signbuilder.wrench.mode.breathing",
            "gui.signbuilder.wrench.mode.proximity",
            "gui.signbuilder.wrench.mode.night_shift",
            "gui.signbuilder.wrench.mode.audio_sync",
            "gui.signbuilder.wrench.mode.disco",
            "gui.signbuilder.wrench.mode.eye_contact",
            "gui.signbuilder.wrench.mode.low_power"
    };

    private int panelWidth = 236;
    private int rowHeight = 18;
    private int panelHeight;

    public WrenchScreen(int currentMode, boolean detectsMonsters, boolean detectsAnimals) {
        super(Component.translatable("gui.signbuilder.wrench.title"));
        this.currentMode = currentMode;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
    }

    @Override
    protected void init() {
        super.init();

        rowHeight = 18;
        panelHeight = 26 + ((MOD_KEYS.length + 1) * rowHeight) + 10;
        int maxAvailableHeight = this.height - 35;

        while (panelHeight > maxAvailableHeight && rowHeight > 12) {
            rowHeight--;
            panelHeight = 26 + ((MOD_KEYS.length + 1) * rowHeight) + 10;
        }

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack mainItem = this.minecraft.player.getMainHandItem();
            ItemStack offItem = this.minecraft.player.getOffhandItem();

            CompoundTag mainTag = mainItem.getTag();
            CompoundTag offTag = offItem.getTag();

            this.isSmartFillEnabled = (mainTag != null && mainTag.getBoolean("IsSmartFill")) ||
                    (offTag != null && offTag.getBoolean("IsSmartFill"));

            CompoundTag targetTag = mainTag != null ? mainTag : offTag;
            if (targetTag != null) {
                if (targetTag.contains("ButtonMode")) this.buttonMode = targetTag.getInt("ButtonMode");
                if (targetTag.contains("SyncWord")) this.syncWord = targetTag.getBoolean("SyncWord");
                if (targetTag.contains("ActiveTab")) this.activeTab = targetTag.getInt("ActiveTab");
                if (targetTag.contains("PinCode")) this.pinCode = targetTag.getString("PinCode");
            }
        }

        int centerX = this.width / 2;
        int startX = centerX - (panelWidth / 2);
        int startY = Math.max(18, (this.height - panelHeight) / 2);
        int contentStartY = startY + 24;

        this.pinInput = new EditBox(this.font, startX + 12, contentStartY + 104, panelWidth - 64, 16, Component.translatable("gui.signbuilder.wrench.pin.title"));
        this.pinInput.setMaxLength(16);
        this.pinInput.setValue(this.pinCode);
        this.pinInput.setResponder(text -> {
            this.pinCode = text;
            sendSyncPacket(false);
        });
        this.addWidget(this.pinInput);
        updatePinVisibility();
    }

    private void updatePinVisibility() {
        if (this.pinInput != null) {
            boolean visible = (this.activeTab == 1 && this.buttonMode == 3);
            this.pinInput.setVisible(visible);
            this.pinInput.setEditable(visible);
            int startX = (this.width - panelWidth) / 2;
            int startY = Math.max(18, (this.height - panelHeight) / 2);
            int contentStartY = startY + 24;
            this.pinInput.setX(startX + 12);
            this.pinInput.setY(contentStartY + 104);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.pinInput != null && this.pinInput.isVisible() && this.pinInput.isFocused()) {
            if (keyCode != 256) {
                return this.pinInput.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.pinInput != null && this.pinInput.isVisible() && this.pinInput.isFocused()) {
            return this.pinInput.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int startX = centerX - (panelWidth / 2);
        int startY = Math.max(18, (this.height - panelHeight) / 2);

        int titleY = Math.max(4, startY - 14);
        guiGraphics.drawCenteredString(this.font, this.title, centerX, titleY, 0xFFD700);

        guiGraphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xEE1A1A1A);

        int borderColor = 0x88FFFFFF;
        guiGraphics.renderOutline(startX - 1, startY - 1, panelWidth + 2, panelHeight + 2, borderColor);

        drawSmartFillIndicator(guiGraphics, startX + panelWidth - 12, startY + 8, mouseX, mouseY);

        renderTabs(guiGraphics, startX, startY, mouseX, mouseY);

        Component tooltipToRender = null;

        if (activeTab == 0) {
            tooltipToRender = renderNeonTab(guiGraphics, startX, startY + 24, mouseX, mouseY);
        } else {
            tooltipToRender = renderRedstoneTab(guiGraphics, startX, startY + 24, mouseX, mouseY, partialTick);
        }

        int promptY = Math.min(this.height - 10, startY + panelHeight + 6);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.wrench.close_prompt"), centerX, promptY, 0x888888);

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

        int neonBg = (activeTab == 0) ? 0xFF005577 : (neonHover ? 0xFF333333 : 0xFF222222);
        int neonBorder = (activeTab == 0) ? 0xFF00FFFF : 0xFF444444;
        guiGraphics.fill(neonX, tabY, neonX + tabWidth, tabY + tabHeight, neonBg);
        guiGraphics.renderOutline(neonX, tabY, tabWidth, tabHeight, neonBorder);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.wrench.tab.neon"), neonX + tabWidth / 2, tabY + 4, activeTab == 0 ? 0x00FFFF : 0xAAAAAA);

        int redstoneBg = (activeTab == 1) ? 0xFF772200 : (redstoneHover ? 0xFF333333 : 0xFF222222);
        int redstoneBorder = (activeTab == 1) ? 0xFFFF5500 : 0xFF444444;
        guiGraphics.fill(redstoneX, tabY, redstoneX + tabWidth, tabY + tabHeight, redstoneBg);
        guiGraphics.renderOutline(redstoneX, tabY, tabWidth, tabHeight, redstoneBorder);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.wrench.tab.redstone"), redstoneX + tabWidth / 2, tabY + 4, activeTab == 1 ? 0xFFAA00 : 0xAAAAAA);
    }

    private Component renderNeonTab(GuiGraphics guiGraphics, int startX, int contentStartY, int mouseX, int mouseY) {
        long time = Util.getMillis();
        Component tooltipToRender = null;

        for (int i = 0; i < MOD_KEYS.length; i++) {
            int rowY = contentStartY + (i * rowHeight);
            boolean isHovered = mouseX >= startX + 4 && mouseX <= startX + panelWidth - 4 && mouseY >= rowY && mouseY < rowY + rowHeight;

            if (isHovered && tooltipToRender == null) {
                guiGraphics.fill(startX + 4, rowY, startX + panelWidth - 4, rowY + rowHeight, 0x44FFFFFF);
                tooltipToRender = Component.translatable(MOD_KEYS[i] + ".desc");
            }

            if (i == 5) {
                int toggleSize = 13;
                int monsterToggleX = startX + panelWidth - 20;
                int animalToggleX = startX + panelWidth - 36;
                int toggleY = rowY + (rowHeight - toggleSize) / 2;

                drawCreeperIcon(guiGraphics, monsterToggleX, toggleY, toggleSize, this.detectsMonsters);
                drawPigIcon(guiGraphics, animalToggleX, toggleY, toggleSize, this.detectsAnimals);

                if (mouseX >= monsterToggleX && mouseX <= monsterToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                    tooltipToRender = Component.translatable(this.detectsMonsters ? "gui.signbuilder.wrench.monster_toggle_on" : "gui.signbuilder.wrench.monster_toggle_off");
                } else if (mouseX >= animalToggleX && mouseX <= animalToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                    tooltipToRender = Component.translatable(this.detectsAnimals ? "gui.signbuilder.wrench.animal_toggle_on" : "gui.signbuilder.wrench.animal_toggle_off");
                }
            }

            int alpha;
            int ledColorHex = 0x00FFFF;

            if (i == 0) alpha = 255;
            else if (i == 1) alpha = ((time / 250L) % 2 == 0) ? 255 : 40;
            else if (i == 2) alpha = (Math.random() > 0.7) ? 255 : 40;
            else if (i == 3) alpha = (int)(((Math.sin(time / 200.0) + 1.0) / 2.0) * 200) + 55;
            else if (i == 4) alpha = (int)(((Math.sin(time / 600.0) + 1.0) / 2.0) * 200) + 55;
            else if (i == 5) alpha = (time % 1500L < 200) ? 255 : 40;
            else if (i == 6) alpha = ((time / 2000L) % 2 == 0) ? 255 : 40;
            else if (i == 7) alpha = (Math.random() > 0.4) ? 255 : 40;
            else if (i == 8) alpha = ((time / 100L) % 2 == 0) ? 255 : 40;
            else if (i == 10) alpha = 130;
            else alpha = 255;

            int color = (alpha << 24) | ledColorHex;
            drawLedCircle(guiGraphics, startX + 16, rowY + (rowHeight / 2), color);

            Component text = Component.translatable(MOD_KEYS[i]);
            Component displayText = (i == currentMode) ? Component.literal("> ").append(text) : text;

            int textXOffset = (i == currentMode) ? 26 : 30;
            int textY = rowY + (rowHeight - 8) / 2;
            guiGraphics.drawString(this.font, displayText, startX + textXOffset, textY, 0x00FFFF);
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
        guiGraphics.drawString(this.font, offDisplay, startX + offTextOffset, offTextY, 0xFF5555);

        return tooltipToRender;
    }

    private Component renderRedstoneTab(GuiGraphics guiGraphics, int startX, int contentStartY, int mouseX, int mouseY, float partialTick) {
        Component tooltip = null;

        guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.section.button_mode"), startX + 10, contentStartY + 2, 0xFFD700);

        String[] btnModes = {
                "gui.signbuilder.wrench.btn.disabled",
                "gui.signbuilder.wrench.btn.pulse",
                "gui.signbuilder.wrench.btn.toggle",
                "gui.signbuilder.wrench.btn.pin"
        };
        int[] btnColors = { 0xAAAAAA, 0x55FF55, 0x55FFFF, 0xFFAA00 };

        for (int i = 0; i < 4; i++) {
            int rowY = contentStartY + 16 + (i * 18);
            boolean isHovered = mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= rowY && mouseY < rowY + 16;
            if (isHovered) {
                guiGraphics.fill(startX + 8, rowY, startX + panelWidth - 8, rowY + 16, 0x33FFFFFF);
                tooltip = Component.translatable(btnModes[i] + ".desc");
            }

            int checkColor = (buttonMode == i) ? 0xFF00FF00 : 0xFF444444;
            guiGraphics.renderOutline(startX + 12, rowY + 2, 11, 11, checkColor);
            if (buttonMode == i) {
                guiGraphics.fill(startX + 14, rowY + 4, startX + 21, rowY + 11, 0xFF00FF00);
            }

            guiGraphics.drawString(this.font, Component.translatable(btnModes[i]), startX + 28, rowY + 4, btnColors[i]);
        }

        if (this.buttonMode == 3) {
            guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.pin.title"), startX + 10, contentStartY + 92, 0xFFD700);
            if (this.pinInput != null) {
                this.pinInput.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            int recX = startX + panelWidth - 48;
            int recY = contentStartY + 104;
            boolean isRecHovered = mouseX >= recX && mouseX <= recX + 36 && mouseY >= recY && mouseY <= recY + 16;
            guiGraphics.fill(recX, recY, recX + 36, recY + 16, isRecHovered ? 0xFF992222 : 0xFF661111);
            guiGraphics.renderOutline(recX, recY, 36, 16, isRecHovered ? 0xFFFF6666 : 0xFFFF3333);
            guiGraphics.drawCenteredString(this.font, "⏺ REC", recX + 18, recY + 4, 0xFFFFFF);

            if (isRecHovered) {
                tooltip = Component.translatable("gui.signbuilder.wrench.pin.rec_tooltip");
            }
        }

        int scopeTitleY = (this.buttonMode == 3) ? (contentStartY + 126) : (contentStartY + 92);
        guiGraphics.drawString(this.font, Component.translatable("gui.signbuilder.wrench.scope.title"), startX + 10, scopeTitleY, 0xFFD700);

        int scopeRowY = (this.buttonMode == 3) ? (contentStartY + 140) : (contentStartY + 106);
        boolean isScopeHovered = mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= scopeRowY && mouseY < scopeRowY + 16;
        if (isScopeHovered) {
            guiGraphics.fill(startX + 8, scopeRowY, startX + panelWidth - 8, scopeRowY + 16, 0x33FFFFFF);
            tooltip = Component.translatable("gui.signbuilder.wrench.scope.desc");
        }

        int scopeCheckColor = syncWord ? 0xFF00FF00 : 0xFF444444;
        guiGraphics.renderOutline(startX + 12, scopeRowY + 2, 11, 11, scopeCheckColor);
        if (syncWord) {
            guiGraphics.fill(startX + 14, scopeRowY + 4, startX + 21, scopeRowY + 11, 0xFF00FF00);
        }
        guiGraphics.drawString(this.font, Component.translatable(syncWord ? "gui.signbuilder.wrench.scope.word" : "gui.signbuilder.wrench.scope.single"), startX + 28, scopeRowY + 4, syncWord ? 0x55FF55 : 0xAAAAAA);

        return tooltip;
    }

    private void drawCreeperIcon(GuiGraphics guiGraphics, int x, int y, int size, boolean active) {
        int bg = active ? 0xFF388E3C : 0xFF2A2A2A;
        int border = active ? 0xFF81C784 : 0xFF444444;
        guiGraphics.fill(x, y, x + size, y + size, bg);
        guiGraphics.renderOutline(x, y, size, size, border);

        int ox = x + (size - 8) / 2;
        int oy = y + (size - 8) / 2;
        int faceColor = active ? 0xFF000000 : 0xFF141414;

        guiGraphics.fill(ox + 1, oy + 1, ox + 3, oy + 3, faceColor);
        guiGraphics.fill(ox + 5, oy + 1, ox + 7, oy + 3, faceColor);
        guiGraphics.fill(ox + 3, oy + 3, ox + 5, oy + 5, faceColor);
        guiGraphics.fill(ox + 2, oy + 4, ox + 6, oy + 6, faceColor);
        guiGraphics.fill(ox + 2, oy + 6, ox + 3, oy + 7, faceColor);
        guiGraphics.fill(ox + 5, oy + 6, ox + 6, oy + 7, faceColor);
    }

    private void drawPigIcon(GuiGraphics guiGraphics, int x, int y, int size, boolean active) {
        int bg = active ? 0xFFF06292 : 0xFF2A2A2A;
        int border = active ? 0xFFF8BBD0 : 0xFF444444;
        guiGraphics.fill(x, y, x + size, y + size, bg);
        guiGraphics.renderOutline(x, y, size, size, border);

        int ox = x + (size - 8) / 2;
        int oy = y + (size - 8) / 2;

        int eyeWhite = active ? 0xFFFFFFFF : 0xFF555555;
        int eyePupil = active ? 0xFF2A1515 : 0xFF141414;
        int snout = active ? 0xFFC2185B : 0xFF1E1E1E;
        int nostril = active ? 0xFF5E092B : 0xFF0A0A0A;

        guiGraphics.fill(ox + 1, oy + 2, ox + 2, oy + 3, eyeWhite);
        guiGraphics.fill(ox + 2, oy + 2, ox + 3, oy + 3, eyePupil);
        guiGraphics.fill(ox + 5, oy + 2, ox + 6, oy + 3, eyePupil);
        guiGraphics.fill(ox + 6, oy + 2, ox + 7, oy + 3, eyeWhite);

        guiGraphics.fill(ox + 2, oy + 4, ox + 6, oy + 7, snout);
        guiGraphics.fill(ox + 2, oy + 5, ox + 3, oy + 6, nostril);
        guiGraphics.fill(ox + 5, oy + 5, ox + 6, oy + 6, nostril);
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
        ModMessages.sendToServer(new WrenchModeC2SPacket(currentMode, this.detectsMonsters, this.detectsAnimals, this.buttonMode, this.syncWord, this.activeTab, this.pinCode, startRecording));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startX = (this.width - panelWidth) / 2;
            int startY = Math.max(18, (this.height - panelHeight) / 2);

            int tabY = startY + 4;
            int tabWidth = (panelWidth - 28) / 2;
            int tabHeight = 16;
            int neonX = startX + 6;
            int redstoneX = neonX + tabWidth + 4;

            if (mouseX >= neonX && mouseX <= neonX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight) {
                this.activeTab = 0;
                updatePinVisibility();
                sendSyncPacket(false);
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                }
                return true;
            }

            if (mouseX >= redstoneX && mouseX <= redstoneX + tabWidth && mouseY >= tabY && mouseY <= tabY + tabHeight) {
                this.activeTab = 1;
                updatePinVisibility();
                sendSyncPacket(false);
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.2F);
                }
                return true;
            }

            int contentStartY = startY + 24;

            if (activeTab == 0) {
                for (int i = 0; i < MOD_KEYS.length; i++) {
                    int rowY = contentStartY + (i * rowHeight);

                    if (i == 5) {
                        int toggleSize = 13;
                        int monsterToggleX = startX + panelWidth - 20;
                        int animalToggleX = startX + panelWidth - 36;
                        int toggleY = rowY + (rowHeight - toggleSize) / 2;

                        if (mouseX >= animalToggleX && mouseX <= animalToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                            this.detectsAnimals = !this.detectsAnimals;
                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, this.detectsAnimals ? 1.2F : 0.8F);
                            }
                            sendSyncPacket(false);
                            return true;
                        }

                        if (mouseX >= monsterToggleX && mouseX <= monsterToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                            this.detectsMonsters = !this.detectsMonsters;
                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, this.detectsMonsters ? 1.2F : 0.8F);
                            }
                            sendSyncPacket(false);
                            return true;
                        }
                    }

                    if (mouseX >= startX + 4 && mouseX <= startX + panelWidth - 4 && mouseY >= rowY && mouseY < rowY + rowHeight) {
                        this.currentMode = i;
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
                for (int i = 0; i < 4; i++) {
                    int rowY = contentStartY + 16 + (i * 18);
                    if (mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= rowY && mouseY < rowY + 16) {
                        this.buttonMode = i;
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
                    int recY = contentStartY + 104;
                    if (mouseX >= recX && mouseX <= recX + 36 && mouseY >= recY && mouseY <= recY + 16) {
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

                int scopeRowY = (this.buttonMode == 3) ? (contentStartY + 140) : (contentStartY + 106);
                if (mouseX >= startX + 8 && mouseX <= startX + panelWidth - 8 && mouseY >= scopeRowY && mouseY < scopeRowY + 16) {
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
}