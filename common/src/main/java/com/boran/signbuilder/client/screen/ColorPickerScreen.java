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
    private float layoutScale = 1.0F;

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
        this.layoutScale = Math.min(1.0F, Math.min((this.width - 24.0F) / 208.0F, (this.height - 24.0F) / 198.0F));
        this.layoutScale = Math.max(0.35F, this.layoutScale);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        Component addColorText = Component.translatable("gui.signbuilder.color_picker.add_color");
        Component doneText = Component.translatable("gui.signbuilder.color_picker.done");
        Component cancelText = Component.translatable("gui.signbuilder.color_picker.cancel");
        int addButtonWidth = Math.min(scaled(180), Math.max(scaled(100), this.font.width(addColorText) + scaled(16)));
        int doneButtonWidth = Math.max(scaled(50), this.font.width(doneText) + scaled(8));
        int cancelButtonWidth = Math.max(scaled(50), this.font.width(cancelText) + scaled(8));
        int buttonGap = scaled(10);
        int footerWidth = doneButtonWidth + cancelButtonWidth + buttonGap;
        int addButtonY = getHexInputY(centerY) + getRgbInputHeight() + scaled(4);
        int footerButtonY = addButtonY + scaled(22);

        this.addRenderableWidget(Button.builder(addColorText, button -> {
            addCustomColorToBrush();
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.selectedColorHex, true));
            if (this.minecraft != null) this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX - addButtonWidth / 2, addButtonY, addButtonWidth, scaled(20)).build());

        this.addRenderableWidget(Button.builder(doneText, button -> {
            com.boran.signbuilder.network.ModMessages.sendToServer(
                    new com.boran.signbuilder.network.BrushColorPacket(this.selectedColorHex, false));
            if (this.minecraft != null) this.minecraft.setScreen(null);
        }).bounds(centerX - footerWidth / 2, footerButtonY, doneButtonWidth, scaled(20)).build());

        this.addRenderableWidget(Button.builder(cancelText, button -> {
            if (this.minecraft != null) this.minecraft.setScreen(this.parentScreen);
        }).bounds(centerX - footerWidth / 2 + doneButtonWidth + buttonGap, footerButtonY, cancelButtonWidth, scaled(20)).build());

        int r = (selectedColorHex >> 16) & 0xFF;
        int g = (selectedColorHex >> 8) & 0xFF;
        int b = selectedColorHex & 0xFF;

        int rgbInputHeight = getRgbInputHeight();
        rInput = new EditBox(this.font, centerX + scaled(55), getRgbInputY(centerY, 0), scaled(32), rgbInputHeight, Component.literal("R"));
        rInput.setMaxLength(3);
        rInput.setValue(String.valueOf(r));
        rInput.setBordered(false);
        rInput.setResponder(text -> onRgbInputChanged());
        this.addRenderableWidget(rInput);

        gInput = new EditBox(this.font, centerX + scaled(55), getRgbInputY(centerY, 1), scaled(32), rgbInputHeight, Component.literal("G"));
        gInput.setMaxLength(3);
        gInput.setValue(String.valueOf(g));
        gInput.setBordered(false);
        gInput.setResponder(text -> onRgbInputChanged());
        this.addRenderableWidget(gInput);

        bInput = new EditBox(this.font, centerX + scaled(55), getRgbInputY(centerY, 2), scaled(32), rgbInputHeight, Component.literal("B"));
        bInput.setMaxLength(3);
        bInput.setValue(String.valueOf(b));
        bInput.setBordered(false);
        bInput.setResponder(text -> onRgbInputChanged());
        this.addRenderableWidget(bInput);

        hexInput = new EditBox(this.font, centerX + scaled(48), getHexInputY(centerY), scaled(48), rgbInputHeight, Component.literal("Hex"));
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

        if (mouseX >= centerX - scaled(90) && mouseX <= centerX + scaled(10) && mouseY >= centerY - scaled(30) && mouseY <= centerY + scaled(50)) {
            draggingSatBri = true;
            updateSatBri(mouseX, mouseY, centerX, centerY);
            return true;
        } else if (mouseX >= centerX + scaled(20) && mouseX <= centerX + scaled(35) && mouseY >= centerY - scaled(30) && mouseY <= centerY + scaled(50)) {
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
        float s = (float) (mouseX - (centerX - scaled(90))) / scaled(100);
        float b = 1.0f - ((float) (mouseY - (centerY - scaled(30))) / scaled(80));
        this.saturation = Mth.clamp(s, 0.0f, 1.0f);
        this.brightness = Mth.clamp(b, 0.0f, 1.0f);
        this.selectedColorHex = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        updateInputFieldsFromPicker();
    }

    private void updateHue(double mouseY, int centerY) {
        float h = 1.0f - ((float) (mouseY - (centerY - scaled(30))) / scaled(80));
        this.hue = Mth.clamp(h, 0.0f, 1.0f);
        this.selectedColorHex = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        updateInputFieldsFromPicker();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int panelWidth = scaled(208);
        int panelHeight = scaled(198);
        SignBuilderUi.drawPanel(guiGraphics, centerX - scaled(104), centerY - scaled(88), panelWidth, panelHeight);
        SignBuilderUi.drawHeader(guiGraphics, this.font, centerX - scaled(103), centerY - scaled(87), scaled(206), this.title,
                Component.literal(String.format("#%06X", 0xFFFFFF & selectedColorHex)));

        guiGraphics.fill(centerX - scaled(90), centerY - scaled(59), centerX + scaled(90), centerY - scaled(47), 0xFF000000 | selectedColorHex);
        guiGraphics.renderOutline(centerX - scaled(91), centerY - scaled(60), scaled(182), scaled(14), 0xFF9AA7B5);

        Component paletteText = Component.translatable("gui.signbuilder.color_picker.palette");
        SignBuilderUi.drawSectionLabel(guiGraphics, this.font, paletteText, centerX - scaled(90), centerY - scaled(42), scaled(110));

        int boxX = centerX - scaled(90);
        int boxY = centerY - scaled(30);
        int boxWidth = scaled(100);
        int boxHeight = scaled(80);
        int halfBoxHeight = scaled(40);
        int gradientStep = Math.max(1, scaled(2));
        for (int x = 0; x < boxWidth; x += gradientStep) {
            float s = x / (float) boxWidth;
            int topColor = Color.HSBtoRGB(hue, s, 1.0f);
            int middleColor = Color.HSBtoRGB(hue, s, 0.5f);
            int columnEnd = Math.min(boxWidth, x + gradientStep);
            guiGraphics.fillGradient(boxX + x, boxY, boxX + columnEnd, boxY + halfBoxHeight, topColor, middleColor);
            guiGraphics.fillGradient(boxX + x, boxY + halfBoxHeight, boxX + columnEnd, boxY + boxHeight, middleColor, 0xFF000000);
        }
        guiGraphics.renderOutline(boxX - 1, boxY - 1, boxWidth + 2, boxHeight + 2, 0xFF9AA7B5);

        int cursorSize = Math.max(2, scaled(3));
        int cursorX = boxX + (int) (saturation * boxWidth);
        int cursorY = boxY + (int) ((1.0f - brightness) * boxHeight);
        guiGraphics.fill(cursorX - cursorSize, cursorY - cursorSize, cursorX + cursorSize, cursorY + cursorSize, 0xFFFFFFFF);
        guiGraphics.fill(cursorX - cursorSize + 1, cursorY - cursorSize + 1, cursorX + cursorSize - 1, cursorY + cursorSize - 1, 0xFF000000 | selectedColorHex);

        int hueX = centerX + scaled(20);
        int hueWidth = scaled(15);
        for (int y = 0; y < boxHeight; y += gradientStep) {
            float h = 1.0f - (y / (float) boxHeight);
            int color = Color.HSBtoRGB(h, 1.0f, 1.0f);
            guiGraphics.fill(hueX, boxY + y, hueX + hueWidth, boxY + Math.min(boxHeight, y + gradientStep), color);
        }
        guiGraphics.renderOutline(hueX - 1, boxY - 1, hueWidth + 2, boxHeight + 2, 0xFF9AA7B5);

        int hueCursorY = boxY + (int) ((1.0f - hue) * boxHeight);
        guiGraphics.fill(hueX - 2, hueCursorY - 1, hueX + hueWidth + 2, hueCursorY + 1, 0xFFFFFFFF);

        int rgbInputHeight = getRgbInputHeight();
        for (int i = 0; i < 3; i++) {
            int inputY = getRgbInputY(centerY, i);
            guiGraphics.fillGradient(centerX + scaled(52), inputY - scaled(3), centerX + scaled(92), inputY + rgbInputHeight + scaled(3), 0xFF303844, 0xFF1C222A);
        }
        int hexInputY = getHexInputY(centerY);
        guiGraphics.fillGradient(centerX + scaled(46), hexInputY - scaled(3), centerX + scaled(98), hexInputY + rgbInputHeight + scaled(3), 0xFF303844, 0xFF1C222A);
        guiGraphics.drawString(this.font, "R:", centerX + scaled(40), getRgbLabelY(centerY, 0), SignBuilderUi.ACCENT, false);
        guiGraphics.drawString(this.font, "G:", centerX + scaled(40), getRgbLabelY(centerY, 1), SignBuilderUi.ACCENT, false);
        guiGraphics.drawString(this.font, "B:", centerX + scaled(40), getRgbLabelY(centerY, 2), SignBuilderUi.ACCENT, false);
        guiGraphics.drawString(this.font, "#", centerX + scaled(38), hexInputY + (rgbInputHeight - 8) / 2, SignBuilderUi.ACCENT, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int scaled(int value) {
        return Math.max(1, Math.round(value * this.layoutScale));
    }

    private int getRgbInputHeight() {
        return Math.max(12, scaled(16));
    }

    private int getRgbRowSpacing() {
        return Math.max(getRgbInputHeight() + scaled(6), scaled(26));
    }

    private int getRgbInputY(int centerY, int row) {
        return centerY - getRgbRowSpacing() + row * getRgbRowSpacing();
    }

    private int getRgbLabelY(int centerY, int row) {
        return getRgbInputY(centerY, row) + (getRgbInputHeight() - 8) / 2;
    }

    private int getHexInputY(int centerY) {
        return getRgbInputY(centerY, 2) + getRgbInputHeight() + scaled(6);
    }
}
