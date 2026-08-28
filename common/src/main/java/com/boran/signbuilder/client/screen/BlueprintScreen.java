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

        this.textField = new EditBox(this.font, centerX - 100, centerY - 25, 200, 20, Component.literal("Word"));
        this.textField.setMaxLength(32);
        this.textField.setValue(this.initialText);
        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        String[] insertChars = {"«", "•", "»", "↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "|", "¦", "*", "♥", "€", "$", "£", "₺"};

        String[] displayChars = {"•  ", "•", "  •", "↑", "↓", "←", "→", "↖", "↗", "↙", "↘", ")(", "][", "★", "♥", "€", "$", "£", "₺"};

        String[] tooltipKeys = {
                "tooltip.signbuilder.blueprint.dot_left",
                "tooltip.signbuilder.blueprint.dot_center",
                "tooltip.signbuilder.blueprint.dot_right",
                null, null, null, null, null, null, null, null,
                "tooltip.signbuilder.blueprint.bracket_double",
                "tooltip.signbuilder.blueprint.square_bracket_double",
                "tooltip.signbuilder.blueprint.star",
                "tooltip.signbuilder.blueprint.heart",
                "tooltip.signbuilder.blueprint.euro",
                "tooltip.signbuilder.blueprint.dollar",
                "tooltip.signbuilder.blueprint.pound",
                "tooltip.signbuilder.blueprint.tl"
        };

        int btnWidth = 18;
        int startX = centerX - ((insertChars.length * btnWidth) / 2);

        for (int i = 0; i < insertChars.length; i++) {
            String insert = insertChars[i];

            Button.Builder btnBuilder = Button.builder(Component.literal(displayChars[i]), button -> {
                this.textField.insertText(insert);
            }).bounds(startX + (i * btnWidth), centerY + 5, btnWidth, 20);

            if (tooltipKeys[i] != null) {
                btnBuilder.tooltip(Tooltip.create(Component.translatable(tooltipKeys[i])));
            }

            this.addRenderableWidget(btnBuilder.build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.blueprint.undo").withStyle(net.minecraft.ChatFormatting.RED), button -> {
            ModMessages.sendToServer(new BlueprintUndoC2SPacket());
            this.onClose();
        }).bounds(centerX - 100, centerY + 35, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.blueprint.save"), button -> this.onClose())
                .bounds(centerX - 25, centerY + 35, 125, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.blueprint.prompt"), this.width / 2, this.height / 2 - 45, 0x00FFFF);
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
        String enteredText = this.textField.getValue().toUpperCase();
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