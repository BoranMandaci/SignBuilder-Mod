package com.boran.signbuilder.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
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

    private int currentPage = 0;
    private final int itemsPerPage = 20;

    private static final String[] ALL_MATERIALS = {
            "minecraft:white_concrete",
            "minecraft:oak_planks", "minecraft:spruce_planks", "minecraft:birch_planks",
            "minecraft:jungle_planks", "minecraft:acacia_planks", "minecraft:dark_oak_planks",
            "minecraft:mangrove_planks", "minecraft:cherry_planks", "minecraft:bamboo_planks",
            "minecraft:crimson_planks", "minecraft:warped_planks",
            "minecraft:bricks", "minecraft:stone_bricks", "minecraft:mossy_stone_bricks",
            "minecraft:deepslate_bricks", "minecraft:mud_bricks", "minecraft:nether_bricks",
            "minecraft:red_nether_bricks", "minecraft:end_stone_bricks",
            "minecraft:purpur_block", "minecraft:prismarine_bricks", "minecraft:stone",
            "minecraft:smooth_stone", "minecraft:smooth_sandstone", "minecraft:smooth_red_sandstone",
            "minecraft:polished_andesite", "minecraft:polished_diorite", "minecraft:polished_granite",
            "minecraft:quartz_block", "minecraft:coal_block", "minecraft:amethyst_block",
            "minecraft:copper_block", "minecraft:redstone_block", "minecraft:lapis_block",
            "minecraft:iron_block", "minecraft:gold_block", "minecraft:diamond_block",
            "minecraft:emerald_block", "minecraft:netherite_block"
    };

    private static final String[] ALL_KEYS = {
            "white_concrete",
            "oak_planks", "spruce_planks", "birch_planks",
            "jungle_planks", "acacia_planks", "dark_oak_planks",
            "mangrove_planks", "cherry_planks", "bamboo_planks",
            "crimson_planks", "warped_planks",
            "bricks", "stone_bricks", "mossy_stone_bricks",
            "deepslate_bricks", "mud_bricks", "nether_bricks",
            "red_nether_bricks", "end_stone_bricks",
            "purpur_block", "prismarine_bricks", "stone",
            "smooth_stone", "smooth_sandstone", "smooth_red_sandstone",
            "polished_andesite", "polished_diorite", "polished_granite",
            "quartz_block", "coal_block", "amethyst_block",
            "copper_block", "redstone_block", "lapis_block",
            "iron_block", "gold_block", "diamond_block",
            "emerald_block", "netherite_block"
    };

    public MaterialPickerScreen(Screen parentScreen) {
        super(Component.translatable("gui.signbuilder.select_material"));
        this.parentScreen = parentScreen;
    }

    private ItemStack[] createItemStacks() {
        return new ItemStack[]{
                new ItemStack(Blocks.WHITE_CONCRETE),
                new ItemStack(Blocks.OAK_PLANKS), new ItemStack(Blocks.SPRUCE_PLANKS), new ItemStack(Blocks.BIRCH_PLANKS),
                new ItemStack(Blocks.JUNGLE_PLANKS), new ItemStack(Blocks.ACACIA_PLANKS), new ItemStack(Blocks.DARK_OAK_PLANKS),
                new ItemStack(Blocks.MANGROVE_PLANKS), new ItemStack(Blocks.CHERRY_PLANKS), new ItemStack(Blocks.BAMBOO_PLANKS),
                new ItemStack(Blocks.CRIMSON_PLANKS), new ItemStack(Blocks.WARPED_PLANKS),
                new ItemStack(Blocks.BRICKS), new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.MOSSY_STONE_BRICKS),
                new ItemStack(Blocks.DEEPSLATE_BRICKS), new ItemStack(Blocks.MUD_BRICKS), new ItemStack(Blocks.NETHER_BRICKS),
                new ItemStack(Blocks.RED_NETHER_BRICKS), new ItemStack(Blocks.END_STONE_BRICKS),
                new ItemStack(Blocks.PURPUR_BLOCK), new ItemStack(Blocks.PRISMARINE_BRICKS), new ItemStack(Blocks.STONE),
                new ItemStack(Blocks.SMOOTH_STONE), new ItemStack(Blocks.SMOOTH_SANDSTONE), new ItemStack(Blocks.SMOOTH_RED_SANDSTONE),
                new ItemStack(Blocks.POLISHED_ANDESITE), new ItemStack(Blocks.POLISHED_DIORITE), new ItemStack(Blocks.POLISHED_GRANITE),
                new ItemStack(Blocks.QUARTZ_BLOCK), new ItemStack(Blocks.COAL_BLOCK), new ItemStack(Blocks.AMETHYST_BLOCK),
                new ItemStack(Blocks.COPPER_BLOCK), new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Blocks.LAPIS_BLOCK),
                new ItemStack(Blocks.IRON_BLOCK), new ItemStack(Blocks.GOLD_BLOCK), new ItemStack(Blocks.DIAMOND_BLOCK),
                new ItemStack(Blocks.EMERALD_BLOCK), new ItemStack(Blocks.NETHERITE_BLOCK)
        };
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        ItemStack[] icons = createItemStacks();

        int cols = 5;
        int rows = 4;

        int maxAvailableHeight = this.height - 70;
        int maxAvailableWidth = this.width - 20;

        buttonSize = 36;
        spacing = 12;

        while (buttonSize > 10) {
            gridWidth = (buttonSize * cols) + (spacing * (cols - 1));
            gridHeight = (buttonSize * rows) + (spacing * (rows - 1));

            if (gridWidth <= maxAvailableWidth && gridHeight <= maxAvailableHeight) break;

            buttonSize -= 2;
            spacing = Math.max(2, buttonSize / 4);
        }

        gridWidth = (buttonSize * cols) + (spacing * (cols - 1));
        gridHeight = (buttonSize * rows) + (spacing * (rows - 1));

        startX = (this.width - gridWidth) / 2;
        startY = Math.max(25, (this.height - (gridHeight + 45)) / 2);

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, ALL_MATERIALS.length);

        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            int row = slot / cols;
            int col = slot % cols;
            int x = startX + (col * (buttonSize + spacing));
            int y = startY + (row * (buttonSize + spacing));

            this.addRenderableWidget(new VisualMaterialButton(x, y, buttonSize, buttonSize, ALL_MATERIALS[i], ALL_KEYS[i], icons[i]));
        }

        int navY = startY + gridHeight + 6;
        int maxPages = (int) Math.ceil((double) ALL_MATERIALS.length / itemsPerPage);

        this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
            if (currentPage > 0) {
                currentPage--;
                this.init();
            }
        }).bounds(startX, navY, 24, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
            if (currentPage < maxPages - 1) {
                currentPage++;
                this.init();
            }
        }).bounds(startX + gridWidth - 24, navY, 24, 18).build());

        int backBtnY = navY + 22;
        this.addRenderableWidget(new FlatBackButton(startX, backBtnY, gridWidth, 18, this.parentScreen));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int pad = 10;
        int panelLeft = startX - pad;
        int panelTop = startY - pad;
        int panelRight = startX + gridWidth + pad;
        int panelBottom = startY + gridHeight + 52 + pad;

        graphics.fillGradient(panelLeft, panelTop, panelRight, panelBottom, 0xEE101010, 0xFA050505);
        graphics.renderOutline(panelLeft - 1, panelTop - 1, (panelRight - panelLeft) + 2, (panelBottom - panelTop) + 2, 0x50FFFFFF);
        graphics.renderOutline(panelLeft, panelTop, panelRight - panelLeft, panelBottom - panelTop, 0xAA000000);

        int titleY = Math.max(5, panelTop - 12);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFD700);

        int maxPages = (int) Math.ceil((double) ALL_MATERIALS.length / itemsPerPage);
        int navY = startY + gridHeight + 11;
        graphics.drawCenteredString(this.font, (currentPage + 1) + " / " + maxPages, this.width / 2, navY, 0xAAAAAA);

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