package com.boran.signbuilder.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class MaterialPickerScreen extends Screen {
    private final Screen parentScreen;
    private int startX, startY;
    private int gridWidth, gridHeight;
    private int buttonSize = 36;
    private int spacing = 12;

    public MaterialPickerScreen(Screen parentScreen) {
        super(Component.translatable("gui.signbuilder.select_material"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        String[] materials = {
                "minecraft:white_concrete", "minecraft:oak_planks", "minecraft:spruce_planks",
                "minecraft:birch_planks", "minecraft:jungle_planks", "minecraft:acacia_planks",
                "minecraft:dark_oak_planks", "minecraft:mangrove_planks", "minecraft:cherry_planks",
                "minecraft:bamboo_planks", "minecraft:iron_block", "minecraft:polished_andesite"
        };

        ItemStack[] icons = {
                new ItemStack(Blocks.WHITE_CONCRETE), new ItemStack(Blocks.OAK_PLANKS), new ItemStack(Blocks.SPRUCE_PLANKS),
                new ItemStack(Blocks.BIRCH_PLANKS), new ItemStack(Blocks.JUNGLE_PLANKS), new ItemStack(Blocks.ACACIA_PLANKS),
                new ItemStack(Blocks.DARK_OAK_PLANKS), new ItemStack(Blocks.MANGROVE_PLANKS), new ItemStack(Blocks.CHERRY_PLANKS),
                new ItemStack(Blocks.BAMBOO_PLANKS), new ItemStack(Blocks.IRON_BLOCK), new ItemStack(Blocks.POLISHED_ANDESITE)
        };

        String[] translationKeys = {
                "white_concrete", "oak_planks", "spruce_planks", "birch_planks",
                "jungle_planks", "acacia_planks", "dark_oak_planks", "mangrove_planks",
                "cherry_planks", "bamboo_planks", "iron_block", "polished_andesite"
        };

        int cols = 4;
        int rows = 3;

        int maxAvailableHeight = this.height - 80;
        int maxAvailableWidth = this.width - 40;

        while (buttonSize > 16) {
            gridWidth = (buttonSize * cols) + (spacing * (cols - 1));
            gridHeight = (buttonSize * rows) + (spacing * (rows - 1));

            if (gridWidth <= maxAvailableWidth && gridHeight <= maxAvailableHeight) break;

            buttonSize -= 2;
            spacing = Math.max(4, buttonSize / 3);
        }

        gridWidth = (buttonSize * cols) + (spacing * (cols - 1));
        gridHeight = (buttonSize * rows) + (spacing * (rows - 1));

        startX = (this.width - gridWidth) / 2;
        startY = (this.height - (gridHeight + 40)) / 2;

        for (int i = 0; i < materials.length; i++) {
            int row = i / cols;
            int col = i % cols;
            int x = startX + (col * (buttonSize + spacing));
            int y = startY + (row * (buttonSize + spacing));

            this.addRenderableWidget(new VisualMaterialButton(x, y, buttonSize, buttonSize, materials[i], translationKeys[i], icons[i]));
        }

        int backBtnY = startY + gridHeight + 15;
        this.addRenderableWidget(new FlatBackButton(startX, backBtnY, gridWidth, 20, this.parentScreen));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int pad = 15;
        int panelLeft = startX - pad;
        int panelTop = startY - pad;
        int panelRight = startX + gridWidth + pad;
        int panelBottom = startY + gridHeight + 45 + pad;

        graphics.fillGradient(panelLeft, panelTop, panelRight, panelBottom, 0xEE101010, 0xFA050505);
        graphics.renderOutline(panelLeft - 1, panelTop - 1, (panelRight - panelLeft) + 2, (panelBottom - panelTop) + 2, 0x50FFFFFF);
        graphics.renderOutline(panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop, 0xAA000000);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop - 12, 0xFFD700);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static class FlatBackButton extends AbstractButton {
        private final Screen parent;

        public FlatBackButton(int x, int y, int width, int height, Screen parent) {
            super(x, y, width, height, Component.translatable("gui.signbuilder.back"));
            this.parent = parent;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF282828);
            int outlineColor = this.isHoveredOrFocused() ? 0xFFFFAA00 : 0xFF555555;
            graphics.renderOutline(this.getX() - 1, this.getY() - 1, this.width + 2, this.height + 2, outlineColor);
            graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
        }

        @Override
        public void onPress() {
            Minecraft.getInstance().setScreen(parent);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private static class VisualMaterialButton extends AbstractButton {
        private final String materialId;
        private final ItemStack iconStack;

        public VisualMaterialButton(int x, int y, int width, int height, String materialId, String translationKey, ItemStack iconStack) {
            super(x, y, width, height, Component.translatable("material.signbuilder." + translationKey));
            this.materialId = materialId;
            this.iconStack = iconStack;
            this.setTooltip(Tooltip.create(this.getMessage().copy().withStyle(net.minecraft.ChatFormatting.GOLD)));
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF282828);
            int borderColor = this.isHoveredOrFocused() ? 0xFFFFAA00 : 0xFF555555;
            graphics.renderOutline(this.getX() - 1, this.getY() - 1, this.width + 2, this.height + 2, borderColor);

            int iconX = this.getX() + (this.width - 16) / 2;
            int iconY = this.getY() + (this.height - 16) / 2;

            graphics.renderItem(this.iconStack, iconX, iconY);
        }

        @Override
        public void onPress() {
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.materialId));
            net.minecraft.client.Minecraft.getInstance().setScreen(null);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}