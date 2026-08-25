package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.WrenchModeC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.Util;

public class WrenchScreen extends Screen {

    private final int currentMode;
    private boolean detectsMonsters;
    private boolean detectsAnimals;
    private boolean isSmartFillEnabled;

    private final String[] MOD_KEYS = {
            "gui.signbuilder.wrench.mode.normal",
            "gui.signbuilder.wrench.mode.blink",
            "gui.signbuilder.wrench.mode.flicker",
            "gui.signbuilder.wrench.mode.wave",
            "gui.signbuilder.wrench.mode.breathing",
            "gui.signbuilder.wrench.mode.proximity",
            "gui.signbuilder.wrench.mode.night_shift"
    };

    private final int panelWidth = 160;
    private final int rowHeight = 22;
    private final int panelHeight = ((MOD_KEYS.length + 1) * rowHeight) + 10;

    public WrenchScreen(int currentMode, boolean detectsMonsters, boolean detectsAnimals) {
        super(Component.translatable("gui.signbuilder.wrench.title"));
        this.currentMode = currentMode;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
    }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack mainItem = this.minecraft.player.getMainHandItem();
            ItemStack offItem = this.minecraft.player.getOffhandItem();

            CompoundTag mainTag = mainItem.getTag();
            CompoundTag offTag = offItem.getTag();

            this.isSmartFillEnabled = (mainTag != null && mainTag.getBoolean("IsSmartFill")) ||
                    (offTag != null && offTag.getBoolean("IsSmartFill"));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int startX = centerX - (panelWidth / 2);
        int startY = (this.height - panelHeight) / 2;

        guiGraphics.drawCenteredString(this.font, this.title, centerX, startY - 20, 0xFFD700);
        guiGraphics.fill(startX, startY, startX + panelWidth, startY + panelHeight, 0xDD222222);

        int borderColor = 0xFFFFFFFF;
        guiGraphics.fill(startX - 1, startY - 1, startX + panelWidth + 1, startY, borderColor);
        guiGraphics.fill(startX - 1, startY + panelHeight, startX + panelWidth + 1, startY + panelHeight + 1, borderColor);
        guiGraphics.fill(startX - 1, startY, startX, startY + panelHeight, borderColor);
        guiGraphics.fill(startX + panelWidth, startY, startX + panelWidth + 1, startY + panelHeight, borderColor);

        Component sfPrefix = Component.translatable("gui.signbuilder.smart_fill");
        Component sfState = Component.translatable(this.isSmartFillEnabled ? "gui.signbuilder.on" : "gui.signbuilder.off");
        Component sfText = sfPrefix.copy().append(": ").append(sfState);

        int sfWidth = this.font.width(sfText) + 6;
        int sfHeight = 11;
        int sfX = startX + panelWidth - sfWidth - 2;
        int sfY = startY + 2;

        int sfBgColor = this.isSmartFillEnabled ? 0xFF114411 : 0xFF441111;
        int sfTextColor = this.isSmartFillEnabled ? 0x55FF55 : 0xFF5555;

        guiGraphics.fill(sfX, sfY, sfX + sfWidth, sfY + sfHeight, sfBgColor);
        guiGraphics.renderOutline(sfX, sfY, sfWidth, sfHeight, 0xFF777777);
        guiGraphics.drawCenteredString(this.font, sfText, sfX + (sfWidth / 2), sfY + 2, sfTextColor);

        long time = Util.getMillis();
        Component tooltipToRender = null;

        if (mouseX >= sfX && mouseX <= sfX + sfWidth && mouseY >= sfY && mouseY <= sfY + sfHeight) {
            tooltipToRender = Component.translatable("tooltip.signbuilder.smart_fill_desc");
        }

        for (int i = 0; i < MOD_KEYS.length; i++) {
            int rowY = startY + 5 + (i * rowHeight);
            boolean isHovered = mouseX >= startX && mouseX <= startX + panelWidth && mouseY >= rowY && mouseY < rowY + rowHeight;

            if (isHovered && tooltipToRender == null) {
                guiGraphics.fill(startX, rowY, startX + panelWidth, rowY + rowHeight, 0x44FFFFFF);
                tooltipToRender = Component.translatable(MOD_KEYS[i] + ".desc");
            }

            if (i == 5) {
                int toggleSize = 16;
                int monsterToggleX = startX + panelWidth - 24;
                int animalToggleX = startX + panelWidth - 44;
                int toggleY = rowY + 3;

                boolean isHoveredMonster = mouseX >= monsterToggleX && mouseX <= monsterToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize;
                boolean isHoveredAnimal = mouseX >= animalToggleX && mouseX <= animalToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize;

                int animalBg = this.detectsAnimals ? 0xFF22AA22 : 0xFFAA2222;
                guiGraphics.fill(animalToggleX, toggleY, animalToggleX + toggleSize, toggleY + toggleSize, animalBg);
                guiGraphics.fill(animalToggleX + 2, toggleY + 2, animalToggleX + 14, toggleY + 14, 0xFFFFAAEE);
                guiGraphics.fill(animalToggleX + 3, toggleY + 5, animalToggleX + 5, toggleY + 7, 0xFF111111);
                guiGraphics.fill(animalToggleX + 11, toggleY + 5, animalToggleX + 13, toggleY + 7, 0xFF111111);
                guiGraphics.fill(animalToggleX + 5, toggleY + 8, animalToggleX + 11, toggleY + 12, 0xFFE585A6);
                guiGraphics.fill(animalToggleX + 6, toggleY + 9, animalToggleX + 7, toggleY + 11, 0xFF994466);
                guiGraphics.fill(animalToggleX + 9, toggleY + 9, animalToggleX + 10, toggleY + 11, 0xFF994466);

                int monsterBg = this.detectsMonsters ? 0xFF22AA22 : 0xFFAA2222;
                guiGraphics.fill(monsterToggleX, toggleY, monsterToggleX + toggleSize, toggleY + toggleSize, monsterBg);
                int faceColor = 0xFF111111;
                guiGraphics.fill(monsterToggleX + 2, toggleY + 3, monsterToggleX + 6, toggleY + 7, faceColor);
                guiGraphics.fill(monsterToggleX + 10, toggleY + 3, monsterToggleX + 14, toggleY + 7, faceColor);
                guiGraphics.fill(monsterToggleX + 6, toggleY + 7, monsterToggleX + 10, toggleY + 11, faceColor);
                guiGraphics.fill(monsterToggleX + 4, toggleY + 11, monsterToggleX + 6, toggleY + 15, faceColor);
                guiGraphics.fill(monsterToggleX + 10, toggleY + 11, monsterToggleX + 12, toggleY + 15, faceColor);

                if (isHoveredMonster) {
                    tooltipToRender = Component.translatable(this.detectsMonsters ? "gui.signbuilder.wrench.monster_toggle_on" : "gui.signbuilder.wrench.monster_toggle_off");
                } else if (isHoveredAnimal) {
                    tooltipToRender = Component.translatable(this.detectsAnimals ? "gui.signbuilder.wrench.animal_toggle_on" : "gui.signbuilder.wrench.animal_toggle_off");
                }
            }

            int alpha;
            if (i == 0) alpha = 255;
            else if (i == 1) alpha = ((time / 250L) % 2 == 0) ? 255 : 40;
            else if (i == 2) alpha = (Math.random() > 0.7) ? 255 : 40;
            else if (i == 3) alpha = (int)(((Math.sin(time / 200.0) + 1.0) / 2.0) * 200) + 55;
            else if (i == 4) alpha = (int)(((Math.sin(time / 600.0) + 1.0) / 2.0) * 200) + 55;
            else if (i == 5) alpha = (time % 1500L < 200) ? 255 : 40;
            else alpha = ((time / 2000L) % 2 == 0) ? 255 : 40;

            int color = (alpha << 24) | 0x00FFFF;
            drawLedCircle(guiGraphics, startX + 16, rowY + (rowHeight / 2), color);

            Component text = Component.translatable(MOD_KEYS[i]);
            Component displayText = (i == currentMode) ? Component.literal("> ").append(text) : text;

            int textXOffset = (i == currentMode) ? 26 : 32;
            guiGraphics.drawString(this.font, displayText, startX + textXOffset, rowY + 7, 0x00FFFF);
        }

        int turnOffY = startY + 5 + (MOD_KEYS.length * rowHeight);
        boolean isTurnOffHovered = mouseX >= startX && mouseX <= startX + panelWidth && mouseY >= turnOffY && mouseY < turnOffY + rowHeight;
        if (isTurnOffHovered && tooltipToRender == null) {
            guiGraphics.fill(startX, turnOffY, startX + panelWidth, turnOffY + rowHeight, 0x44FF0000);
        }
        drawLedCircle(guiGraphics, startX + 16, turnOffY + (rowHeight / 2), 0xFF333333);

        Component offText = Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(net.minecraft.ChatFormatting.RED);
        Component offDisplay = (currentMode == -1) ? Component.literal("> ").append(offText) : offText;
        int offTextOffset = (currentMode == -1) ? 26 : 32;
        guiGraphics.drawString(this.font, offDisplay, startX + offTextOffset, turnOffY + 7, 0xFF5555);

        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.wrench.close_prompt"), centerX, startY + panelHeight + 10, 0xFFFFFF);

        if (tooltipToRender != null) {
            guiGraphics.renderTooltip(this.font, tooltipToRender, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startX = (this.width - panelWidth) / 2;
            int startY = (this.height - panelHeight) / 2;

            Component sfPrefix = Component.translatable("gui.signbuilder.smart_fill");
            Component sfState = Component.translatable(this.isSmartFillEnabled ? "gui.signbuilder.on" : "gui.signbuilder.off");
            int sfWidth = this.font.width(sfPrefix.copy().append(": ").append(sfState)) + 6;
            int sfX = startX + panelWidth - sfWidth - 2;
            int sfY = startY + 2;
            if (mouseX >= sfX && mouseX <= sfX + sfWidth && mouseY >= sfY && mouseY <= sfY + 11) {
                return true;
            }

            for (int i = 0; i < MOD_KEYS.length; i++) {
                int rowY = startY + 5 + (i * rowHeight);

                if (i == 5) {
                    int toggleSize = 16;
                    int monsterToggleX = startX + panelWidth - 24;
                    int animalToggleX = startX + panelWidth - 44;
                    int toggleY = rowY + 3;

                    if (mouseX >= animalToggleX && mouseX <= animalToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                        this.detectsAnimals = !this.detectsAnimals;
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, this.detectsAnimals ? 1.2F : 0.8F);
                        }
                        ModMessages.sendToServer(new WrenchModeC2SPacket(currentMode, this.detectsMonsters, this.detectsAnimals));
                        return true;
                    }

                    if (mouseX >= monsterToggleX && mouseX <= monsterToggleX + toggleSize && mouseY >= toggleY && mouseY <= toggleY + toggleSize) {
                        this.detectsMonsters = !this.detectsMonsters;
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, this.detectsMonsters ? 1.2F : 0.8F);
                        }
                        ModMessages.sendToServer(new WrenchModeC2SPacket(currentMode, this.detectsMonsters, this.detectsAnimals));
                        return true;
                    }
                }

                if (mouseX >= startX && mouseX <= startX + panelWidth && mouseY >= rowY && mouseY < rowY + rowHeight) {
                    ModMessages.sendToServer(new WrenchModeC2SPacket(i, this.detectsMonsters, this.detectsAnimals));
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(
                                Component.translatable("message.signbuilder.wrench.mode_selected")
                                        .withStyle(net.minecraft.ChatFormatting.YELLOW)
                                        .append(Component.translatable(MOD_KEYS[i]).withStyle(net.minecraft.ChatFormatting.AQUA)),
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

            int turnOffY = startY + 5 + (MOD_KEYS.length * rowHeight);
            if (mouseX >= startX && mouseX <= startX + panelWidth && mouseY >= turnOffY && mouseY < turnOffY + rowHeight) {
                ModMessages.sendToServer(new WrenchModeC2SPacket(-1, this.detectsMonsters, this.detectsAnimals));
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.displayClientMessage(
                            Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(net.minecraft.ChatFormatting.RED), true
                    );
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.6F, 1.5F);
                }

                if (this.minecraft != null) {
                    this.minecraft.setScreen(null);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}