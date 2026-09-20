package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.network.BlueprintTextC2SPacket;
import com.boran.signbuilder.network.BlueprintUndoC2SPacket;
import com.boran.signbuilder.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class BlueprintScreen extends Screen {
    private EditBox textField;
    private final String initialText;

    public BlueprintScreen(String initialText) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int btnWidth = 18;
        int btnHeight = 20;

        String[] row1Insert = {"Ä", "ß", "Ç", "Ğ", "İ", "Ö", "Ş", "Ü", "€", "$", "£", "¥", "₺", "♥", "*", "@", "&", "#"};
        String[] row1Display = {"Ä", "ß", "Ç", "Ğ", "İ", "Ö", "Ş", "Ü", "€", "$", "£", "¥", "₺", "♥", "★", "@", "&", "#"};
        String[] row1Tooltips = {
                null, null, null, null, null, null, null, null,
                "tooltip.signbuilder.blueprint.euro",
                "tooltip.signbuilder.blueprint.dollar",
                "tooltip.signbuilder.blueprint.pound",
                "tooltip.signbuilder.blueprint.yen",
                "tooltip.signbuilder.blueprint.tl",
                "tooltip.signbuilder.blueprint.heart",
                "tooltip.signbuilder.blueprint.star",
                null, null, null
        };

        String[] row2Insert = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "«", "•", "»", "|", "¦", "<", ">", "÷", "%", "="};
        String[] row2Display = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "•  ", "•", "  •", ")(", "][", "<", ">", "÷", "%", "="};
        String[] row2Tooltips = {
                null, null, null, null, null, null, null, null,
                "tooltip.signbuilder.blueprint.dot_left",
                "tooltip.signbuilder.blueprint.dot_center",
                "tooltip.signbuilder.blueprint.dot_right",
                "tooltip.signbuilder.blueprint.bracket_double",
                "tooltip.signbuilder.blueprint.square_bracket_double",
                null, null,
                "tooltip.signbuilder.blueprint.divide",
                null, null
        };

        int rowWidth = row1Insert.length * btnWidth;
        int startX = centerX - (rowWidth / 2);

        this.textField = new EditBox(this.font, startX, centerY - 45, rowWidth, 20, Component.literal("Word"));
        this.textField.setMaxLength(32);
        this.textField.setValue(this.initialText);
        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        for (int i = 0; i < row1Insert.length; i++) {
            String insert = row1Insert[i];
            Button.Builder btnBuilder = Button.builder(Component.literal(row1Display[i]), button -> {
                this.textField.insertText(insert);
            }).bounds(startX + (i * btnWidth), centerY - 18, btnWidth, btnHeight);

            if (row1Tooltips[i] != null) {
                btnBuilder.tooltip(Tooltip.create(Component.translatable(row1Tooltips[i])));
            }
            this.addRenderableWidget(btnBuilder.build());
        }

        for (int i = 0; i < row2Insert.length; i++) {
            String insert = row2Insert[i];
            Button.Builder btnBuilder = Button.builder(Component.literal(row2Display[i]), button -> {
                this.textField.insertText(insert);
            }).bounds(startX + (i * btnWidth), centerY + 7, btnWidth, btnHeight);

            if (row2Tooltips[i] != null) {
                btnBuilder.tooltip(Tooltip.create(Component.translatable(row2Tooltips[i])));
            }
            this.addRenderableWidget(btnBuilder.build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.blueprint.undo").withStyle(net.minecraft.ChatFormatting.RED), button -> {
            ModMessages.sendToServer(new BlueprintUndoC2SPacket());
            this.onClose();
        }).bounds(centerX - 100, centerY + 34, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.blueprint.save"), button -> this.onClose())
                .bounds(centerX - 25, centerY + 34, 125, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.blueprint.prompt"), this.width / 2, this.height / 2 - 60, 0x00FFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        StringBuilder sb = new StringBuilder();
        for (char ch : this.textField.getValue().toCharArray()) {
            if (ch == 'ß') {
                sb.append('ß');
            } else {
                sb.append(Character.toUpperCase(ch));
            }
        }
        String enteredText = sb.toString();

        ModMessages.sendToServer(new BlueprintTextC2SPacket(enteredText));

        if (this.minecraft != null && this.minecraft.player != null) {
            if (!enteredText.isEmpty()) {
                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.signbuilder.blueprint.saved")
                                .withStyle(net.minecraft.ChatFormatting.YELLOW)
                                .append(Component.literal(enteredText).withStyle(net.minecraft.ChatFormatting.AQUA)),
                        true
                );
            }
        }

        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}