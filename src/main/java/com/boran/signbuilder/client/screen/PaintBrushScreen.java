package com.boran.signbuilder.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class PaintBrushScreen extends Screen {

    private int startX;
    private int startY;
    private int totalWidth;
    private int totalHeight;
    private final int PADDING = 18;

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

        int buttonSize = 26;
        int spacing = 10;

        ItemStack brush = this.minecraft.player.getMainHandItem();
        int[] customColors = new int[0];
        if (brush.hasTag() && brush.getTag().contains("CustomColors")) {
            customColors = brush.getTag().getIntArray("CustomColors");
        }

        int plusIndex = customColors.length;
        int totalRows = 4 + (plusIndex / 4) + 1;

        totalWidth = (buttonSize * 4) + (spacing * 3);
        totalHeight = (buttonSize * totalRows) + (spacing * (totalRows - 1));

        startX = (this.width - totalWidth) / 2;
        startY = (this.height - totalHeight) / 2;

        for (int i = 0; i < 16; i++) {
            int row = i / 4;
            int col = i % 4;
            int x = startX + (col * (buttonSize + spacing));
            int y = startY + (row * (buttonSize + spacing));

            this.addRenderableWidget(new ColorButton(x, y, buttonSize, buttonSize, colorNames[i], colorCodes[i], i));
        }

        for (int i = 0; i < customColors.length; i++) {
            int row = 4 + (i / 4);
            int col = i % 4;

            int x = startX + (col * (buttonSize + spacing));
            int y = startY + (row * (buttonSize + spacing));

            this.addRenderableWidget(new CustomColorButton(x, y, buttonSize, buttonSize, customColors[i]));
        }

        int plusRow = 4 + (plusIndex / 4);
        int plusCol = plusIndex % 4;

        int plusX = startX + (plusCol * (buttonSize + spacing));
        int plusY = startY + (plusRow * (buttonSize + spacing));

        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            ItemStack currentBrush = this.minecraft.player.getMainHandItem();
            this.minecraft.setScreen(new ColorPickerScreen(currentBrush, this));
        }).bounds(plusX, plusY, buttonSize, buttonSize).build());
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);

        int panelLeft = startX - PADDING;
        int panelTop = startY - PADDING;
        int panelRight = startX + totalWidth + PADDING;
        int panelBottom = startY + totalHeight + PADDING;

        pGuiGraphics.fillGradient(panelLeft, panelTop, panelRight, panelBottom, 0xEE101010, 0xFA050505);
        pGuiGraphics.renderOutline(panelLeft - 1, panelTop - 1, (panelRight - panelLeft) + 2, (panelBottom - panelTop) + 2, 0x50FFFFFF);
        pGuiGraphics.renderOutline(panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop, 0xAA000000);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int titleY = panelTop - 24;
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFD700);
        pGuiGraphics.fill((this.width / 2) - 40, titleY + 12, (this.width / 2) + 40, titleY + 13, 0x40FFFFFF);
        pGuiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.press_esc"), this.width / 2, panelBottom + 10, 0x666666);
    }

    private static class ColorButton extends AbstractButton {
        private final int colorHex;
        private final int colorIndex;

        public ColorButton(int x, int y, int width, int height, String colorName, int colorHex, int colorIndex) {
            super(x, y, width, height, Component.translatable(colorName));
            this.colorHex = colorHex;
            this.colorIndex = colorIndex;
            this.setTooltip(Tooltip.create(Component.translatable(colorName).withStyle(Style.EMPTY.withColor(colorHex))));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderColorButton(graphics, this.getX(), this.getY(), this.width, this.height, this.colorHex, this.isHoveredOrFocused());
        }

        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.colorIndex, false));

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

    private static class CustomColorButton extends AbstractButton {
        private final int colorHex;

        public CustomColorButton(int x, int y, int width, int height, int colorHex) {
            super(x, y, width, height, Component.literal(""));
            this.colorHex = colorHex;
            this.setTooltip(Tooltip.create(Component.literal("#" + String.format("%06X", colorHex).toUpperCase()).withStyle(Style.EMPTY.withColor(colorHex))));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderColorButton(graphics, this.getX(), this.getY(), this.width, this.height, this.colorHex, this.isHoveredOrFocused());
        }

        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.colorHex, false));

            net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("message.signbuilder.custom_color_selected", "#" + String.format("%06X", colorHex).toUpperCase())
                            .withStyle(Style.EMPTY.withColor(this.colorHex)), true);

            net.minecraft.client.Minecraft.getInstance().setScreen(null);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static void renderColorButton(GuiGraphics graphics, int x, int y, int width, int height, int colorHex, boolean isHovered) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + 1, 0xFF373737);
        graphics.fill(x - 1, y - 1, x + 1, y + height + 1, 0xFF373737);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, 0xFFFFFFFF);
        graphics.fill(x + width, y - 1, x + width + 1, y + height + 1, 0xFFFFFFFF);

        graphics.fill(x, y, x + width, y + height, 0xFF000000);

        int fillColor = 0xFF000000 | colorHex;
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fillColor);

        if (isHovered) {
            graphics.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, 0x60FFFFFF, 0x10FFFFFF);
            graphics.renderOutline(x - 2, y - 2, width + 4, height + 4, 0xCCFFFFFF);
        }
    }
}