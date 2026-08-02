package com.boran.signbuilder.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class PaintBrushScreen extends Screen {

    public PaintBrushScreen() {
        super(Component.translatable("gui.signbuilder.title"));
    }

    @Override
    protected void init() {
        super.init();

        String[] colorNames = {
                "color.signbuilder.white", "color.signbuilder.orange", "color.signbuilder.magenta", "color.signbuilder.light_blue",
                "color.signbuilder.yellow", "color.signbuilder.lime", "color.signbuilder.pink", "color.signbuilder.gray",
                "color.signbuilder.light_gray", "color.signbuilder.cyan", "color.signbuilder.purple", "color.signbuilder.blue",
                "color.signbuilder.brown", "color.signbuilder.green", "color.signbuilder.red", "color.signbuilder.black"
        };

        int[] colorCodes = {
                0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6699D8,
                0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C,
                0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2,
                0x664C33, 0x667F33, 0x993333, 0x191919
        };

        int buttonSize = 24;
        int spacing = 6;
        int columns = 4;

        int totalWidth = (buttonSize * columns) + (spacing * (columns - 1));
        int totalHeight = (buttonSize * columns) + (spacing * (columns - 1));

        int startX = (this.width - totalWidth) / 2;
        int startY = (this.height - totalHeight) / 2;

        for (int i = 0; i < 16; i++) {
            int row = i / 4;
            int col = i % 4;
            int x = startX + (col * (buttonSize + spacing));
            int y = startY + (row * (buttonSize + spacing));

            this.addRenderableWidget(new ColorButton(x, y, buttonSize, buttonSize, colorNames[i], colorCodes[i], i));
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, (this.height - 120) / 2 - 30, 0xFFFFFF);
    }

    private static class ColorButton extends AbstractButton {
        private final int colorHex;
        private final int colorIndex;

        public ColorButton(int x, int y, int width, int height, String colorName, int colorHex, int colorIndex) {
            // BURASI GÜNCELLENDİ: Component.literal yerine Component.translatable kullanıldı.
            super(x, y, width, height, Component.translatable(colorName));
            this.colorHex = colorHex;
            this.colorIndex = colorIndex;

            // BURASI GÜNCELLENDİ: Tooltip (mouse ile üzerine gelince çıkan yazı) için de translatable kullanıldı.
            this.setTooltip(Tooltip.create(Component.translatable(colorName).withStyle(Style.EMPTY.withColor(colorHex))));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int borderColor = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF000000;

            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);

            int fillColor = 0xFF000000 | this.colorHex;
            graphics.fill(this.getX() + 2, this.getY() + 2, this.getX() + this.width - 2, this.getY() + this.height - 2, fillColor);
        }

        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.colorIndex));

            net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("message.signbuilder.color_selected", this.getMessage())
                            .withStyle(Style.EMPTY.withColor(this.colorHex)), true);

            net.minecraft.client.Minecraft.getInstance().setScreen(null);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}