package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.item.SignBlueprintItem;
import com.boran.signbuilder.network.BlueprintTextC2SPacket;
import com.boran.signbuilder.network.BlueprintUndoC2SPacket;
import com.boran.signbuilder.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class BlueprintScreen extends Screen {
    private EditBox textField;
    private final String initialText;
    private boolean is2x2;
    private boolean isVertical;
    private boolean withBackplate;

    private Button sizeToggleButton;
    private Button dirToggleButton;
    private Button backplateToggleButton;

    public BlueprintScreen(String initialText) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
    }

    public BlueprintScreen(String initialText, boolean is2x2, boolean isVertical, boolean withBackplate) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
        this.is2x2 = is2x2;
        this.isVertical = isVertical;
        this.withBackplate = withBackplate;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!(stack.getItem() instanceof SignBlueprintItem)) {
                stack = this.minecraft.player.getOffhandItem();
            }
            if (stack.getItem() instanceof SignBlueprintItem && stack.getTag() != null) {
                this.is2x2 = stack.getTag().getBoolean("Is2x2");
                this.isVertical = stack.getTag().getBoolean("IsVertical");
                this.withBackplate = stack.getTag().getBoolean("WithBackplate");
            }
        }

        int btnWidth = 18;
        int btnHeight = 18;

        String[] row1Insert = {"Ä", "ß", "Ç", "Ğ", "İ", "Ö", "Ş", "Ü", "€", "$", "£", "¥", "₺", "♥", "*", "@", "&"};
        String[] row1Display = {"Ä", "ß", "Ç", "Ğ", "İ", "Ö", "Ş", "Ü", "€", "$", "£", "¥", "₺", "♥", "★", "@", "&"};
        String[] row1Tooltips = {
                "block.signbuilder.letter_a_de", "block.signbuilder.letter_eszett", "block.signbuilder.letter_c_tr",
                "block.signbuilder.letter_g_tr", "block.signbuilder.letter_i_tr", "block.signbuilder.letter_o_tr",
                "block.signbuilder.letter_s_tr", "block.signbuilder.letter_u_tr", "block.signbuilder.symbol_euro",
                "block.signbuilder.symbol_dollar", "block.signbuilder.symbol_pound", "block.signbuilder.symbol_yen",
                "block.signbuilder.symbol_tl", "block.signbuilder.symbol_heart", "block.signbuilder.symbol_star",
                "block.signbuilder.symbol_at", "block.signbuilder.symbol_ampersand"
        };

        String[] row2Insert = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "«", "•", "»", "|", "¦", ",", ":", ";", "#"};
        String[] row2Display = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "• ", "•", " •", ")(", "][", ",", ":", ";", "#"};
        String[] row2Tooltips = {
                "block.signbuilder.arrow_up", "block.signbuilder.arrow_down", "block.signbuilder.arrow_left",
                "block.signbuilder.arrow_right", "block.signbuilder.arrow_left_up", "block.signbuilder.arrow_right_up",
                "block.signbuilder.arrow_left_down", "block.signbuilder.arrow_right_down", "block.signbuilder.symbol_dot_left",
                "block.signbuilder.symbol_dot_center", "block.signbuilder.symbol_dot_right", "block.signbuilder.symbol_bracket_double",
                "block.signbuilder.symbol_square_bracket_double", "block.signbuilder.symbol_comma", "block.signbuilder.symbol_colon",
                "block.signbuilder.symbol_semicolon", "block.signbuilder.symbol_hashtag"
        };

        String[] row3Insert = {"+", "-", "÷", "=", "%", "<", ">", "(", ")", "[", "]", "/", "\\", "!", "?", "'", "\""};
        String[] row3Display = {"+", "-", "÷", "=", "%", "<", ">", "(", ")", "[", "]", "/", "\\", "!", "?", "'", "\""};
        String[] row3Tooltips = {
                "block.signbuilder.symbol_plus", "block.signbuilder.symbol_minus", "block.signbuilder.symbol_divide",
                "block.signbuilder.symbol_equals", "block.signbuilder.symbol_percent", "block.signbuilder.symbol_less_than",
                "block.signbuilder.symbol_greater_than", "block.signbuilder.symbol_bracket_left", "block.signbuilder.symbol_bracket_right",
                "block.signbuilder.symbol_square_bracket_left", "block.signbuilder.symbol_square_bracket_right", "block.signbuilder.symbol_slash",
                "block.signbuilder.symbol_backslash", "block.signbuilder.symbol_exclamation", "block.signbuilder.symbol_question",
                "block.signbuilder.symbol_apostrophe", "block.signbuilder.symbol_quotes"
        };

        int totalGridWidth = 17 * btnWidth;
        int startX = centerX - (totalGridWidth / 2);

        this.textField = new EditBox(this.font, startX, centerY - 58, totalGridWidth, 20, Component.literal("Word"));
        this.textField.setMaxLength(32);
        this.textField.setValue(this.initialText);
        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        for (int i = 0; i < row1Insert.length; i++) {
            String insert = row1Insert[i];
            Button.Builder btn = Button.builder(Component.literal(row1Display[i]), b -> this.textField.insertText(insert))
                    .bounds(startX + (i * btnWidth), centerY - 32, btnWidth, btnHeight)
                    .tooltip(Tooltip.create(Component.translatable(row1Tooltips[i])));
            this.addRenderableWidget(btn.build());
        }

        for (int i = 0; i < row2Insert.length; i++) {
            String insert = row2Insert[i];
            Button.Builder btn = Button.builder(Component.literal(row2Display[i]), b -> this.textField.insertText(insert))
                    .bounds(startX + (i * btnWidth), centerY - 12, btnWidth, btnHeight)
                    .tooltip(Tooltip.create(Component.translatable(row2Tooltips[i])));
            this.addRenderableWidget(btn.build());
        }

        for (int i = 0; i < row3Insert.length; i++) {
            String insert = row3Insert[i];
            Button.Builder btn = Button.builder(Component.literal(row3Display[i]), b -> this.textField.insertText(insert))
                    .bounds(startX + (i * btnWidth), centerY + 8, btnWidth, btnHeight)
                    .tooltip(Tooltip.create(Component.translatable(row3Tooltips[i])));
            this.addRenderableWidget(btn.build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.blueprint.undo").withStyle(ChatFormatting.RED), button -> {
            ModMessages.sendToServer(new BlueprintUndoC2SPacket());
            this.onClose();
        }).bounds(startX, centerY + 34, 42, 20).build());

        this.sizeToggleButton = Button.builder(getSizeButtonText(), button -> {
            this.is2x2 = !this.is2x2;
            button.setMessage(getSizeButtonText());
        }).bounds(startX + 45, centerY + 34, 46, 20).build();
        this.addRenderableWidget(this.sizeToggleButton);

        this.dirToggleButton = Button.builder(getDirButtonText(), button -> {
            this.isVertical = !this.isVertical;
            button.setMessage(getDirButtonText());
        }).bounds(startX + 94, centerY + 34, 74, 20).build();
        this.addRenderableWidget(this.dirToggleButton);

        this.backplateToggleButton = Button.builder(getBackplateButtonText(), button -> {
            this.withBackplate = !this.withBackplate;
            button.setMessage(getBackplateButtonText());
        }).bounds(startX + 171, centerY + 34, 72, 20).tooltip(Tooltip.create(Component.translatable("tooltip.signbuilder.blueprint.backplate_desc"))).build();
        this.addRenderableWidget(this.backplateToggleButton);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.signbuilder.blueprint.save"), button -> this.onClose())
                .bounds(startX + 246, centerY + 34, 60, 20).build());
    }

    private Component getSizeButtonText() {
        return Component.literal(this.is2x2 ? "2x2" : "1x1")
                .withStyle(this.is2x2 ? ChatFormatting.GOLD : ChatFormatting.AQUA);
    }

    private Component getDirButtonText() {
        return Component.literal(this.isVertical ? "↓ " : "→ ")
                .withStyle(this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN)
                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal"));
    }

    private Component getBackplateButtonText() {
        return Component.literal("🛡 ")
                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                        .withStyle(this.withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.signbuilder.blueprint.prompt"), this.width / 2, this.height / 2 - 74, 0x00FFFF);
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

        ModMessages.sendToServer(new BlueprintTextC2SPacket(enteredText, this.is2x2, this.isVertical, this.withBackplate));

        if (this.minecraft != null && this.minecraft.player != null) {
            if (!enteredText.isEmpty()) {
                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.signbuilder.blueprint.saved")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(enteredText).withStyle(ChatFormatting.AQUA))
                                .append(Component.literal(" [" + (this.is2x2 ? "2x2" : "1x1") + "] ").withStyle(this.is2x2 ? ChatFormatting.GOLD : ChatFormatting.AQUA))
                                .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                                        .withStyle(this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("[🛡 ").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                                        .withStyle(this.withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                                .append(Component.literal("]").withStyle(ChatFormatting.GRAY)),
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