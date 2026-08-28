package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.menu.SignPressMenu;
import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.SignPressCraftC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SignPressScreen extends AbstractContainerScreen<SignPressMenu> {

    private float scrollOffs = 0.0F;
    private boolean scrolling = false;
    private int startIndex = 0;
    private final int columns = 4;
    private final int visibleRows = 3;
    private String selectedBlock = "";

    private final List<String> allBlocks = List.of(
            "letter_a", "letter_b", "letter_c", "letter_d", "letter_e", "letter_f", "letter_g", "letter_h", "letter_i", "letter_j", "letter_k", "letter_l", "letter_m", "letter_n", "letter_o", "letter_p", "letter_r", "letter_s", "letter_t", "letter_u", "letter_v", "letter_w", "letter_x", "letter_y", "letter_z",
            "number_0", "number_1", "number_2", "number_3", "number_4", "number_5", "number_6", "number_7", "number_8", "number_9",
            "arrow_up", "arrow_down", "arrow_left", "arrow_right", "arrow_left_up", "arrow_right_up", "arrow_left_down", "arrow_right_down",
            "symbol_plus", "symbol_minus", "symbol_heart", "symbol_dot_left", "symbol_dot_center", "symbol_dot_right", "symbol_slash", "symbol_backslash", "symbol_bracket_left", "symbol_bracket_right", "symbol_bracket_double", "symbol_square_bracket_left", "symbol_square_bracket_right", "symbol_square_bracket_double", "symbol_hashtag", "symbol_star", "symbol_euro", "symbol_dollar", "symbol_pound", "symbol_tl"
    );

    public SignPressScreen(SignPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.startIndex = 0;
        this.scrollOffs = 0.0F;
    }

    protected int getOffs() {
        return (allBlocks.size() + columns - 1) / columns - visibleRows;
    }

    private boolean isScrollBarActive() {
        return allBlocks.size() > 12;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = false;

        if (this.isScrollBarActive()) {
            int scrollX = this.leftPos + 122;
            int scrollY = this.topPos + 16;
            if (mouseX >= scrollX && mouseX < scrollX + 6 && mouseY >= scrollY && mouseY < scrollY + 54) {
                this.scrolling = true;
                return true;
            }
        }

        if (button == 0) {
            int startX = leftPos + 48;
            int startY = topPos + 16;

            for (int i = 0; i < 12; i++) {
                int blockIndex = this.startIndex + i;
                if (blockIndex < allBlocks.size()) {
                    int slotX = startX + (i % columns) * 18;
                    int slotY = startY + (i / columns) * 18;

                    if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                        this.selectedBlock = allBlocks.get(blockIndex);

                        boolean isShift = Screen.hasShiftDown();

                        ModMessages.sendToServer(new SignPressCraftC2SPacket(this.selectedBlock, isShift));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling && this.isScrollBarActive()) {
            int scrollY = this.topPos + 16;
            int maxScrollY = scrollY + 54;
            this.scrollOffs = ((float)mouseY - (float)scrollY - 7.5F) / ((float)(maxScrollY - scrollY) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffs()) + 0.5D) * columns;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isScrollBarActive()) {
            int maxRows = this.getOffs();
            this.scrollOffs = (float)((double)this.scrollOffs - delta / (double)maxRows);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)maxRows) + 0.5D) * columns;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int startX = leftPos + 48;
        int startY = topPos + 16;
        for (int i = 0; i < 12; i++) {
            int blockIndex = this.startIndex + i;
            if (blockIndex < allBlocks.size()) {
                int slotX = startX + (i % columns) * 18;
                int slotY = startY + (i / columns) * 18;

                if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    String blockName = allBlocks.get(blockIndex);
                    ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", blockName)));
                    guiGraphics.renderTooltip(this.font, itemStack, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF222427);

        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 2, 0xFFFFCC00);
        guiGraphics.fill(leftPos, topPos + imageHeight - 2, leftPos + imageWidth, topPos + imageHeight, 0xFFFFCC00);
        guiGraphics.fill(leftPos, topPos, leftPos + 2, topPos + imageHeight, 0xFFFFCC00);
        guiGraphics.fill(leftPos + imageWidth - 2, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFFFCC00);

        guiGraphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 14, 0xFF141517);

        drawDarkSlot(guiGraphics, leftPos + 25, topPos + 34, 18, 18);
        drawDarkSlot(guiGraphics, leftPos + 138, topPos + 29, 26, 26);

        int startX = leftPos + 48;
        int startY = topPos + 16;

        for (int i = 0; i < 12; i++) {
            int blockIndex = this.startIndex + i;
            int slotX = startX + (i % columns) * 18;
            int slotY = startY + (i / columns) * 18;

            drawDarkSlot(guiGraphics, slotX, slotY, 18, 18);

            if (blockIndex < allBlocks.size()) {
                String blockName = allBlocks.get(blockIndex);

                if (blockName.equals(this.selectedBlock)) {
                    guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x60FFCC00);
                    guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF141517);
                }

                ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", blockName)));
                guiGraphics.renderItem(itemStack, slotX + 1, slotY + 1);

                if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                    guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0x40FFFFFF);
                }
            }
        }

        int scrollX = leftPos + 122;
        int scrollY = topPos + 16;

        guiGraphics.fill(scrollX, scrollY, scrollX + 6, scrollY + 54, 0xFF111214);

        if (this.isScrollBarActive()) {
            int thumbHeight = 15;
            int thumbY = scrollY + (int) ((54 - thumbHeight) * this.scrollOffs);

            guiGraphics.fill(scrollX, thumbY, scrollX + 6, thumbY + thumbHeight, 0xFF8B8B8B);
            guiGraphics.fill(scrollX, thumbY, scrollX + 5, thumbY + 1, 0xFFD4D4D4);
            guiGraphics.fill(scrollX, thumbY, scrollX + 1, thumbY + thumbHeight, 0xFFD4D4D4);
            guiGraphics.fill(scrollX + 5, thumbY + 1, scrollX + 6, thumbY + thumbHeight, 0xFF373737);
            guiGraphics.fill(scrollX + 1, thumbY + thumbHeight - 1, scrollX + 6, thumbY + thumbHeight, 0xFF373737);

            guiGraphics.fill(scrollX + 2, thumbY + 5, scrollX + 5, thumbY + 6, 0xFF4A4D53);
            guiGraphics.fill(scrollX + 2, thumbY + 7, scrollX + 5, thumbY + 8, 0xFF4A4D53);
            guiGraphics.fill(scrollX + 2, thumbY + 9, scrollX + 5, thumbY + 10, 0xFF4A4D53);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawDarkSlot(guiGraphics, leftPos + 7 + col * 18, topPos + 83 + row * 18, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawDarkSlot(guiGraphics, leftPos + 7 + col * 18, topPos + 141, 18, 18);
        }
    }

    private void drawDarkSlot(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFF111214);
        guiGraphics.fill(x, y, x + width - 1, y + 1, 0xFF050505);
        guiGraphics.fill(x, y, x + 1, y + height - 1, 0xFF050505);
        guiGraphics.fill(x + 1, y + height - 1, x + width, y + height, 0xFF4A4D53);
        guiGraphics.fill(x + width - 1, y + 1, x + width, y + height, 0xFF4A4D53);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0xFFFFCC00, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, 73, 0xFFAAAAAA, false);
    }
}