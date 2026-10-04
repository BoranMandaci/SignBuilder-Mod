package com.boran.signbuilder.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PaintBrushScreen extends Screen {

    private int startX;
    private int startY;
    private int gridWidth;
    private int gridHeight;
    private int gridColumns;
    private int matBtnWidth;
    private int matBtnX;
    private int dynamicPadding = 18;
    private boolean isSmartFillEnabled;

    public PaintBrushScreen() {
        super(Component.translatable("gui.signbuilder.title"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void init() {
        super.init();

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack mainItem = this.minecraft.player.getMainHandItem();
            ItemStack offItem = this.minecraft.player.getOffhandItem();
            this.isSmartFillEnabled = (!mainItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).isEmpty() && mainItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean("IsSmartFill")) ||
                    (!offItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).isEmpty() && offItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getBoolean("IsSmartFill"));
        }

        String[] colorNames = {
                "color.signbuilder.white", "color.signbuilder.orange", "color.signbuilder.magenta", "color.signbuilder.light_blue",
                "color.signbuilder.yellow", "color.signbuilder.lime", "color.signbuilder.pink", "color.signbuilder.gray",
                "color.signbuilder.light_gray", "color.signbuilder.cyan", "color.signbuilder.purple", "color.signbuilder.blue",
                "color.signbuilder.brown", "color.signbuilder.green", "color.signbuilder.red", "color.signbuilder.black"
        };
        int[] colorCodes = {
                0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C,
                0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0xCF2323, 0x191919
        };

        ItemStack brush = this.minecraft.player.getMainHandItem();
        int[] customColors = new int[0];
        if (!brush.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).isEmpty() && brush.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().contains("CustomColors")) {
            customColors = brush.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntArray("CustomColors");
        }

        int totalElements = 16 + 1 + customColors.length + 1;
        int maxAvailableHeight = this.height - 120;
        int maxAvailableWidth = this.width - 40;
        int preferredColumns = Math.max(4, Math.min(8, (totalElements + 3) / 4));
        int buttonSize = 8;
        int spacing = 2;
        this.gridColumns = 4;

        for (int columns = 4; columns <= preferredColumns; columns++) {
            int candidateSize = 26;
            int candidateSpacing = 8;
            int rows = (totalElements + columns - 1) / columns;

            while (candidateSize > 8) {
                int candidateWidth = candidateSize * columns + candidateSpacing * (columns - 1);
                int candidateHeight = candidateSize * rows + candidateSpacing * (rows - 1);
                if (candidateWidth <= maxAvailableWidth && candidateHeight <= maxAvailableHeight) break;
                candidateSize--;
                candidateSpacing = Math.max(2, candidateSize / 3);
            }

            if (candidateSize > buttonSize) {
                buttonSize = candidateSize;
                spacing = candidateSpacing;
                this.gridColumns = columns;
            }
            if (candidateSize >= 18) break;
        }

        int totalRows = (totalElements + this.gridColumns - 1) / this.gridColumns;
        this.dynamicPadding = Math.min(14, buttonSize);
        gridWidth = (buttonSize * this.gridColumns) + (spacing * (this.gridColumns - 1));
        gridHeight = (buttonSize * totalRows) + (spacing * (totalRows - 1));

        startX = (this.width - gridWidth) / 2;
        startY = (this.height - (gridHeight + 40)) / 2;

        int currentIndex = 0;
        for (int i = 0; i < 16; i++) {
            int row = currentIndex / this.gridColumns; int col = currentIndex % this.gridColumns;
            this.addRenderableWidget(new ColorButton(startX + (col * (buttonSize + spacing)), startY + (row * (buttonSize + spacing)), buttonSize, buttonSize, colorNames[i], colorCodes[i], i));
            currentIndex++;
        }

        int rRow = currentIndex / this.gridColumns; int rCol = currentIndex % this.gridColumns;
        this.addRenderableWidget(new RainbowButton(startX + (rCol * (buttonSize + spacing)), startY + (rRow * (buttonSize + spacing)), buttonSize, buttonSize));
        currentIndex++;

        for (int customColor : customColors) {
            int cRow = currentIndex / this.gridColumns; int cCol = currentIndex % this.gridColumns;
            this.addRenderableWidget(new CustomColorButton(startX + (cCol * (buttonSize + spacing)), startY + (cRow * (buttonSize + spacing)), buttonSize, buttonSize, customColor, this));
            currentIndex++;
        }

        int pRow = currentIndex / this.gridColumns; int pCol = currentIndex % this.gridColumns;
        this.addRenderableWidget(new AddColorButton(startX + (pCol * (buttonSize + spacing)), startY + (pRow * (buttonSize + spacing)), buttonSize, buttonSize, this));

        Component matText = Component.translatable("gui.signbuilder.select_material");
        matBtnWidth = Math.min(Math.max(0, this.width - 32), Math.max(gridWidth, this.font.width(matText) + 24));
        matBtnX = (this.width - matBtnWidth) / 2;
        int matBtnY = startY + gridHeight + 15;

        this.addRenderableWidget(new FlatMaterialButton(matBtnX, matBtnY, matBtnWidth, 20, this));
    }


    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Leave empty to prevent double rendering
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int panelLeft = Math.min(startX, matBtnX) - dynamicPadding;
        int panelRight = Math.max(startX + gridWidth, matBtnX + matBtnWidth) + dynamicPadding;
        int panelTop = startY - dynamicPadding;
        int panelBottom = startY + gridHeight + 45 + dynamicPadding;

        SignBuilderUi.drawPanel(pGuiGraphics, panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop);
        SignBuilderUi.drawHeader(pGuiGraphics, this.font, panelLeft, panelTop - 23, panelRight - panelLeft,
                this.title, null);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        drawSmartFillIndicator(pGuiGraphics, panelRight - 20, panelTop + 5, pMouseX, pMouseY);

        int escY = Math.min(this.height - 15, panelBottom + (dynamicPadding > 10 ? 10 : 4));
        SignBuilderUi.drawCenteredStringNoShadow(pGuiGraphics, this.font, Component.translatable("gui.signbuilder.press_esc"), this.width / 2, escY, 0x666666);
    }

    private void drawSmartFillIndicator(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        int color = isSmartFillEnabled ? 0xFF00FF00 : 0xFFFF0000;
        int glowColor = isSmartFillEnabled ? 0x6600FF00 : 0x66FF0000;

        graphics.fill(x - 2, y - 2, x + 6, y + 6, 0xFF111111);
        graphics.fill(x - 1, y - 1, x + 5, y + 5, 0xFF333333);

        graphics.fill(x - 1, y - 1, x + 5, y + 5, glowColor);
        graphics.fill(x, y, x + 4, y + 4, color);
        graphics.fill(x, y, x + 2, y + 2, 0xAAFFFFFF); // Minik beyaz yansıma

        if (mouseX >= x - 2 && mouseX <= x + 6 && mouseY >= y - 2 && mouseY <= y + 6) {
            Component sfPrefix = Component.translatable("gui.signbuilder.smart_fill");
            Component sfState = Component.translatable(this.isSmartFillEnabled ? "gui.signbuilder.on" : "gui.signbuilder.off")
                    .withStyle(isSmartFillEnabled ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED);
            graphics.renderTooltip(this.font, sfPrefix.copy().append(": ").append(sfState), mouseX, mouseY);
        }
    }

    private static class FlatMaterialButton extends AbstractButton {
        private final Screen parent;
        public FlatMaterialButton(int x, int y, int width, int height, Screen parent) {
            super(x, y, width, height, Component.translatable("gui.signbuilder.select_material"));
            this.parent = parent;
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
                    this.isHoveredOrFocused() ? 0xFF465260 : 0xFF303844, 0xFF1C222A);
            graphics.renderOutline(this.getX() - 1, this.getY() - 1, this.width + 2, this.height + 2,
                    this.isHoveredOrFocused() ? SignBuilderUi.ACCENT : 0xFF657181);
            SignBuilderUi.drawCenteredStringNoShadow(graphics, Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, SignBuilderUi.TEXT);
        }
        @Override
        public void onPress() { Minecraft.getInstance().setScreen(new MaterialPickerScreen(parent)); }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) { this.defaultButtonNarrationText(output); }
    }

    private static class ColorButton extends AbstractButton {
        private final int colorHex; private final int colorIndex;
        public ColorButton(int x, int y, int width, int height, String colorName, int colorHex, int colorIndex) {
            super(x, y, width, height, Component.translatable(colorName));
            this.colorHex = colorHex; this.colorIndex = colorIndex;
            this.setTooltip(Tooltip.create(Component.translatable(colorName).withStyle(Style.EMPTY.withColor(colorHex))));
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderColorButton(graphics, this.getX(), this.getY(), this.width, this.height, this.colorHex, this.isHoveredOrFocused()); }
        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.BrushColorPacket(this.colorIndex, false));
            net.minecraft.client.Minecraft.getInstance().setScreen(null);
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) { this.defaultButtonNarrationText(output); }
    }

    private static class RainbowButton extends AbstractButton {
        public RainbowButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.translatable("color.signbuilder.rainbow"));
            this.setTooltip(Tooltip.create(Component.translatable("color.signbuilder.rainbow").withStyle(Style.EMPTY.withColor(0xFF55FF))));
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            float hue = (Minecraft.getInstance().level.getGameTime() % 120) / 120f;
            renderColorButton(graphics, this.getX(), this.getY(), this.width, this.height, java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF, this.isHoveredOrFocused());
            SignBuilderUi.drawCenteredStringNoShadow(graphics, Minecraft.getInstance().font, "R", this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
        }
        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.BrushColorPacket(-1, false));
            net.minecraft.client.Minecraft.getInstance().setScreen(null);
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) { this.defaultButtonNarrationText(output); }
    }

    private static class CustomColorButton extends AbstractButton {
        private final int colorHex; private final PaintBrushScreen parentScreen;
        public CustomColorButton(int x, int y, int width, int height, int colorHex, PaintBrushScreen parentScreen) {
            super(x, y, width, height, Component.literal(""));
            this.colorHex = colorHex; this.parentScreen = parentScreen;
            this.setTooltip(Tooltip.create(Component.empty().append(Component.literal("#" + String.format("%06X", colorHex).toUpperCase()).withStyle(Style.EMPTY.withColor(colorHex))).append("\n").append(Component.translatable("gui.signbuilder.right_click_to_delete").withStyle(Style.EMPTY.withColor(0xAAAAAA)))));
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderColorButton(graphics, this.getX(), this.getY(), this.width, this.height, this.colorHex, this.isHoveredOrFocused()); }
        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.BrushColorPacket(this.colorHex, false));
            net.minecraft.client.Minecraft.getInstance().setScreen(null);
        }
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.active && this.visible && this.isHovered && button == 1) { removeThisColor(); return true; }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        private void removeThisColor() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ItemStack brush = mc.player.getMainHandItem();
            if (!brush.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).isEmpty() && brush.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().contains("CustomColors")) {
                List<Integer> newColors = new ArrayList<>();
                boolean removed = false;
                for (int c : brush.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntArray("CustomColors")) {
                    if (!removed && c == this.colorHex) { removed = true; continue; }
                    newColors.add(c);
                }
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, brush, t -> t.putIntArray("CustomColors", newColors.stream().mapToInt(i -> i).toArray()));
                com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.BrushColorPacket(this.colorHex, false, true));
                parentScreen.rebuildWidgets();
            }
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) { this.defaultButtonNarrationText(output); }
    }

    private static class AddColorButton extends AbstractButton {
        private final PaintBrushScreen parentScreen;
        public AddColorButton(int x, int y, int width, int height, PaintBrushScreen parentScreen) {
            super(x, y, width, height, Component.literal("+"));
            this.parentScreen = parentScreen;
            this.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.color_picker.add_color")));
        }
        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + 1, 0xFF373737);
            graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + 1, this.getY() + this.height + 1, 0xFF373737);
            graphics.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFFFFFFFF);
            graphics.fill(this.getX() + this.width, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFFFFFFFF);
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF555555);
            if (this.isHoveredOrFocused()) {
                graphics.fillGradient(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0x60FFFFFF, 0x10FFFFFF);
                graphics.renderOutline(this.getX() - 2, this.getY() - 2, this.width + 4, this.height + 4, 0xCCFFFFFF);
            }
            SignBuilderUi.drawCenteredStringNoShadow(graphics, Minecraft.getInstance().font, "+", this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
        }
        @Override
        public void onPress() { Minecraft.getInstance().setScreen(new ColorPickerScreen(Minecraft.getInstance().player.getMainHandItem(), parentScreen)); }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) { this.defaultButtonNarrationText(output); }
    }

    private static void renderColorButton(GuiGraphics graphics, int x, int y, int width, int height, int colorHex, boolean isHovered) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF0C1015);
        graphics.fill(x, y, x + width, y + height, 0xFF788493);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF000000 | colorHex);
        if (isHovered) {
            graphics.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, 0x42FFFFFF, 0x08FFFFFF);
            graphics.renderOutline(x - 2, y - 2, width + 4, height + 4, SignBuilderUi.ACCENT);
        }
    }
}
