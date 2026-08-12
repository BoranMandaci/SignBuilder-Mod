package com.boran.signbuilder.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public class ColorPickerScreen extends Screen {
    private final ItemStack brushStack;
    private final Screen parentScreen;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float brightness = 1.0f;
    private int selectedColorHex = 0xFF0000;

    private boolean draggingSatBri = false;
    private boolean draggingHue = false;
    private boolean isUpdatingInputs = false;

    private EditBox rInput, gInput, bInput, hexInput;

    public ColorPickerScreen(ItemStack brushStack, Screen parentScreen) {
        super(Component.translatable("gui.signbuilder.color_picker.title"));
        this.brushStack = brushStack;
        this.parentScreen = parentScreen;

        CompoundTag tag = brushStack.getOrCreateTag();
        if (tag.contains("SelectedColor")) {
            int savedColor = tag.getInt("SelectedColor");
            if (savedColor > 15) {
                this.selectedColorHex = savedColor;
                float[] hsb = Color.RGBtoHSB((selectedColorHex >> 16) & 0xFF, (selectedColorHex >> 8) & 0xFF, selectedColorHex & 0xFF, null);
                this.hue = hsb[0];
                this.saturation = hsb[1];
                this.brightness = hsb[2];
            }
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.color_picker.add_color"), button -> {
            addCustomColorToBrush();
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.selectedColorHex, true));
            if (this.minecraft != null) this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX - 50, centerY + 56, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.color_picker.done"), button -> {
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.selectedColorHex, false));
            if (this.minecraft != null) this.minecraft.setScreen(null);
        }).bounds(centerX - 55, centerY + 80, 50, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.color_picker.cancel"), button -> {
            if (this.minecraft != null) this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX + 5, centerY + 80, 50, 20).build());

        int r = (selectedColorHex >> 16) & 0xFF;
        int g = (selectedColorHex >> 8) & 0xFF;
        int b = selectedColorHex & 0xFF;

        rInput = new EditBox(this.font, centerX + 55, centerY - 26, 32, 16, Component.literal("R"));
        rInput.setMaxLength(3);
        rInput.setValue(String.valueOf(r));
        rInput.setBordered(false);
        rInput.setResponder(text -> onRgbInputChanged());
        this.addRenderableWidget(rInput);

        gInput = new EditBox(this.font, centerX + 55, centerY - 4, 32, 16, Component.literal("G"));
        gInput.setMaxLength(3);
        gInput.setValue(String.valueOf(g));
        gInput.setBordered(false);
        gInput.setResponder(text -> onRgbInputChanged());
        this.addRenderableWidget(gInput);

        bInput = new EditBox(this.font, centerX + 55, centerY + 18, 32, 16, Component.literal("B"));
        bInput.setMaxLength(3);
        bInput.setValue(String.valueOf(b));
        bInput.setBordered(false);
        bInput.setResponder(text -> onRgbInputChanged());
        this.addRenderableWidget(bInput);

        hexInput = new EditBox(this.font, centerX + 48, centerY + 46, 48, 16, Component.literal("Hex"));
        hexInput.setMaxLength(6);
        hexInput.setValue(String.format("%06X", (0xFFFFFF & selectedColorHex)));
        hexInput.setBordered(false);
        hexInput.setResponder(this::onHexInputChanged);
        this.addRenderableWidget(hexInput);
    }

    private void onRgbInputChanged() {
        if (isUpdatingInputs) return;
        try {
            int r = Integer.parseInt(rInput.getValue());
            int g = Integer.parseInt(gInput.getValue());
            int b = Integer.parseInt(bInput.getValue());

            if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                this.selectedColorHex = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                this.hue = hsb[0];
                this.saturation = hsb[1];
                this.brightness = hsb[2];

                isUpdatingInputs = true;
                hexInput.setValue(String.format("%06X", (0xFFFFFF & selectedColorHex)));
                isUpdatingInputs = false;
            }
        } catch (NumberFormatException ignored) {}
    }

    private void onHexInputChanged(String text) {
        if (isUpdatingInputs) return;
        try {
            String cleanHex = text.trim();
            if (cleanHex.length() == 6) {
                int hex = Integer.parseInt(cleanHex, 16);
                this.selectedColorHex = hex & 0xFFFFFF;
                int r = (selectedColorHex >> 16) & 0xFF;
                int g = (selectedColorHex >> 8) & 0xFF;
                int b = selectedColorHex & 0xFF;

                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                this.hue = hsb[0];
                this.saturation = hsb[1];
                this.brightness = hsb[2];

                isUpdatingInputs = true;
                rInput.setValue(String.valueOf(r));
                gInput.setValue(String.valueOf(g));
                bInput.setValue(String.valueOf(b));
                isUpdatingInputs = false;
            }
        } catch (NumberFormatException ignored) {}
    }

    private void updateInputFieldsFromPicker() {
        if (rInput == null || gInput == null || bInput == null || hexInput == null) return;
        isUpdatingInputs = true;
        int r = (selectedColorHex >> 16) & 0xFF;
        int g = (selectedColorHex >> 8) & 0xFF;
        int b = selectedColorHex & 0xFF;

        rInput.setValue(String.valueOf(r));
        gInput.setValue(String.valueOf(g));
        bInput.setValue(String.valueOf(b));
        hexInput.setValue(String.format("%06X", (0xFFFFFF & selectedColorHex)));
        isUpdatingInputs = false;
    }

    private void addCustomColorToBrush() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        ItemStack currentBrush = this.minecraft.player.getMainHandItem();
        net.minecraft.nbt.CompoundTag tag = currentBrush.getOrCreateTag();

        int[] oldColors = tag.contains("CustomColors") ? tag.getIntArray("CustomColors") : new int[0];
        java.util.List<Integer> colorList = new java.util.ArrayList<>();

        for (int c : oldColors) {
            if (c != this.selectedColorHex) {
                colorList.add(c);
            }
        }

        colorList.add(0, this.selectedColorHex);

        while (colorList.size() > 14) {
            colorList.remove(colorList.size() - 1);
        }

        tag.putIntArray("CustomColors", colorList.stream().mapToInt(i -> i).toArray());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (mouseX >= centerX - 90 && mouseX <= centerX + 10 && mouseY >= centerY - 30 && mouseY <= centerY + 50) {
            draggingSatBri = true;
            updateSatBri(mouseX, mouseY, centerX, centerY);
            return true;
        } else if (mouseX >= centerX + 20 && mouseX <= centerX + 35 && mouseY >= centerY - 30 && mouseY <= centerY + 50) {
            draggingHue = true;
            updateHue(mouseY, centerY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (draggingSatBri) {
            updateSatBri(mouseX, mouseY, centerX, centerY);
            return true;
        } else if (draggingHue) {
            updateHue(mouseY, centerY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSatBri = false;
        draggingHue = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSatBri(double mouseX, double mouseY, int centerX, int centerY) {
        float s = (float) (mouseX - (centerX - 90)) / 100f;
        float b = 1.0f - ((float) (mouseY - (centerY - 30)) / 80f);
        this.saturation = Mth.clamp(s, 0.0f, 1.0f);
        this.brightness = Mth.clamp(b, 0.0f, 1.0f);
        this.selectedColorHex = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        updateInputFieldsFromPicker();
    }

    private void updateHue(double mouseY, int centerY) {
        float h = 1.0f - ((float) (mouseY - (centerY - 30)) / 80f);
        this.hue = Mth.clamp(h, 0.0f, 1.0f);
        this.selectedColorHex = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        updateInputFieldsFromPicker();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.fill(centerX - 100, centerY - 80, centerX + 100, centerY + 110, 0xFF202023);

        Component selectedColorText = Component.translatable("gui.signbuilder.color_picker.selected_color");
        guiGraphics.drawString(this.font, selectedColorText, centerX - (this.font.width(selectedColorText) / 2), centerY - 76, 0xFFAAAAAA, false);

        guiGraphics.fill(centerX - 90, centerY - 65, centerX + 90, centerY - 53, 0xFF000000 | selectedColorHex);

        Component paletteText = Component.translatable("gui.signbuilder.color_picker.palette");
        guiGraphics.drawString(this.font, paletteText, centerX - 90, centerY - 42, 0xFFAAAAAA, false);

        int boxX = centerX - 90;
        int boxY = centerY - 30;
        for (int x = 0; x < 100; x += 2) {
            for (int y = 0; y < 80; y += 2) {
                float s = x / 100f;
                float b = 1.0f - (y / 80f);
                int color = Color.HSBtoRGB(hue, s, b);
                guiGraphics.fill(boxX + x, boxY + y, boxX + x + 2, boxY + y + 2, color);
            }
        }

        int cursorX = boxX + (int) (saturation * 100);
        int cursorY = boxY + (int) ((1.0f - brightness) * 80);
        guiGraphics.fill(cursorX - 3, cursorY - 3, cursorX + 3, cursorY + 3, 0xFFFFFFFF);
        guiGraphics.fill(cursorX - 2, cursorY - 2, cursorX + 2, cursorY + 2, 0xFF000000 | selectedColorHex);

        int hueX = centerX + 20;
        for (int y = 0; y < 80; y += 2) {
            float h = 1.0f - (y / 80f);
            int color = Color.HSBtoRGB(h, 1.0f, 1.0f);
            guiGraphics.fill(hueX, boxY + y, hueX + 15, boxY + y + 2, color);
        }

        int hueCursorY = boxY + (int) ((1.0f - hue) * 80);
        guiGraphics.fill(hueX - 2, hueCursorY - 1, hueX + 17, hueCursorY + 1, 0xFFFFFFFF);

        guiGraphics.drawString(this.font, "R:", centerX + 40, centerY - 26, 0xFFAAAAAA, false);
        guiGraphics.drawString(this.font, "G:", centerX + 40, centerY - 4, 0xFFAAAAAA, false);
        guiGraphics.drawString(this.font, "B:", centerX + 40, centerY + 18, 0xFFAAAAAA, false);
        guiGraphics.drawString(this.font, "#", centerX + 38, centerY + 46, 0xFFAAAAAA, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}