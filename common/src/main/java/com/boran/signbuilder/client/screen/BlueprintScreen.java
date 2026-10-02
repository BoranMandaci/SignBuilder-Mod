package com.boran.signbuilder.client.screen;

import com.boran.signbuilder.item.SignBlueprintItem;
import com.boran.signbuilder.network.BlueprintTextC2SPacket;
import com.boran.signbuilder.network.BlueprintUndoC2SPacket;
import com.boran.signbuilder.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class BlueprintScreen extends Screen {
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 218;
    private static final int SYMBOL_SIZE = 20;
    private static final int SYMBOL_GAP = 2;

    private EditBox textField;
    private final String initialText;
    private int size = 1;
    private boolean isVertical;
    private boolean withBackplate;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int symbolSize;
    private float layoutScaleY = 1.0F;

    public BlueprintScreen(String initialText) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
    }

    public BlueprintScreen(String initialText, int size, boolean isVertical, boolean withBackplate) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
        this.size = size;
        this.isVertical = isVertical;
        this.withBackplate = withBackplate;
    }

    public BlueprintScreen(String initialText, boolean is2x2, boolean isVertical, boolean withBackplate) {
        this(initialText, is2x2 ? 2 : 1, isVertical, withBackplate);
    }

    @Override
    protected void init() {
        super.init();
        this.panelWidth = Math.min(PANEL_WIDTH, Math.max(0, this.width - 32));
        this.layoutScaleY = Math.max(0.35F, Math.min(1.0F, (this.height - 24.0F) / PANEL_HEIGHT));
        this.panelHeight = scaledY(PANEL_HEIGHT);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!(stack.getItem() instanceof SignBlueprintItem)) {
                stack = this.minecraft.player.getOffhandItem();
            }
            if (stack.getItem() instanceof SignBlueprintItem && stack.getTag() != null) {
                if (stack.getTag().contains("Size")) {
                    this.size = stack.getTag().getInt("Size");
                } else if (stack.getTag().getBoolean("Is2x2")) {
                    this.size = 2;
                } else {
                    this.size = 1;
                }
                this.isVertical = stack.getTag().getBoolean("IsVertical");
                this.withBackplate = stack.getTag().getBoolean("WithBackplate");
            }
        }

        int contentWidth = Math.max(0, this.panelWidth - 28);
        this.symbolSize = Math.min(scaledY(SYMBOL_SIZE), Math.max(8, (contentWidth - 40) / 21));

        String[] row1Insert = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "+", "-", "✗", "÷", "=", "%", ">", "<"};
        String[] row1Tooltips = {
                "block.signbuilder.arrow_up", "block.signbuilder.arrow_down", "block.signbuilder.arrow_left",
                "block.signbuilder.arrow_right", "block.signbuilder.arrow_left_up", "block.signbuilder.arrow_right_up",
                "block.signbuilder.arrow_left_down", "block.signbuilder.arrow_right_down", "block.signbuilder.symbol_plus",
                "block.signbuilder.symbol_minus", "block.signbuilder.symbol_cross", "block.signbuilder.symbol_divide", "block.signbuilder.symbol_equals",
                "block.signbuilder.symbol_percent", "block.signbuilder.symbol_greater_than", "block.signbuilder.symbol_less_than"
        };

        String[] row2Insert = {"«", "•", "»", ",", "?", "!", ":", ";", "'", "\"", "/", "\\", "(", ")", "|"};
        String[] row2Display = {"• ", "•", " •", ",", "?", "!", ":", ";", "'", "\"", "/", "\\", "(", ")", ")("};
        String[] row2Tooltips = {
                "block.signbuilder.symbol_dot_left", "block.signbuilder.symbol_dot_center", "block.signbuilder.symbol_dot_right",
                "block.signbuilder.symbol_comma", "block.signbuilder.symbol_question", "block.signbuilder.symbol_exclamation",
                "block.signbuilder.symbol_colon", "block.signbuilder.symbol_semicolon", "block.signbuilder.symbol_apostrophe",
                "block.signbuilder.symbol_quotes", "block.signbuilder.symbol_slash", "block.signbuilder.symbol_backslash",
                "block.signbuilder.symbol_bracket_left", "block.signbuilder.symbol_bracket_right", "block.signbuilder.symbol_bracket_double"
        };

        String[] row3Insert = {"[", "]", "¦", "#", "♥", "★", "@", "&", "*", "✓", "∞", "○", "◆", "♪", "♫", "☠", "$", "€", "£", "¥", "₺"};
        String[] row3Display = {"[", "]", "][", "#", "♥", "★", "@", "&", "*", "✓", "∞", "○", "◆", "♪", "♫", "☠", "$", "€", "£", "¥", "₺"};
        String[] row3Tooltips = {
                "block.signbuilder.symbol_square_bracket_left", "block.signbuilder.symbol_square_bracket_right", "block.signbuilder.symbol_square_bracket_double",
                "block.signbuilder.symbol_hashtag", "block.signbuilder.symbol_heart", "block.signbuilder.symbol_star",
                "block.signbuilder.symbol_at", "block.signbuilder.symbol_ampersand", "block.signbuilder.symbol_asterisk",
                "block.signbuilder.symbol_checkmark", "block.signbuilder.symbol_infinity",
                "block.signbuilder.symbol_circle", "block.signbuilder.symbol_diamond",
                "block.signbuilder.symbol_note", "block.signbuilder.symbol_note_double", "block.signbuilder.symbol_skull",
                "block.signbuilder.symbol_dollar", "block.signbuilder.symbol_euro", "block.signbuilder.symbol_pound", "block.signbuilder.symbol_yen", "block.signbuilder.symbol_tl"
        };

        int inputX = this.panelX + 14;
        int inputWidth = Math.max(0, this.panelWidth - 28);
        int inputY = this.panelY + scaledY(47);
        this.textField = new EditBox(this.font, inputX + 7, inputY + scaledY(2), Math.max(0, inputWidth - 14), scaledY(20), Component.literal("Word"));
        this.textField.setMaxLength(32);
        this.textField.setValue(this.initialText);
        this.textField.setBordered(false);
        this.textField.setTextColor(SignBuilderUi.TEXT);
        this.textField.setTextColorUneditable(SignBuilderUi.MUTED);
        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        int row1X = this.width / 2 - getSymbolRowWidth(row1Insert.length) / 2;
        for (int i = 0; i < row1Insert.length; i++) {
            this.addRenderableWidget(new SymbolButton(row1X + i * (this.symbolSize + SYMBOL_GAP), this.panelY + scaledY(82), this.symbolSize, scaledY(SYMBOL_SIZE), row1Insert[i], row1Tooltips[i]));
        }

        int row2X = this.width / 2 - getSymbolRowWidth(row2Insert.length) / 2;
        for (int i = 0; i < row2Insert.length; i++) {
            this.addRenderableWidget(new SymbolButton(row2X + i * (this.symbolSize + SYMBOL_GAP), this.panelY + scaledY(107), this.symbolSize, scaledY(SYMBOL_SIZE), row2Insert[i], row2Display[i], row2Tooltips[i]));
        }

        int row3X = this.width / 2 - getSymbolRowWidth(row3Insert.length) / 2;
        for (int i = 0; i < row3Insert.length; i++) {
            this.addRenderableWidget(new SymbolButton(row3X + i * (this.symbolSize + SYMBOL_GAP), this.panelY + scaledY(132), this.symbolSize, scaledY(SYMBOL_SIZE), row3Insert[i], row3Display[i], row3Tooltips[i]));
        }

        int buttonY = this.panelY + scaledY(178);
        int buttonGap = Math.min(5, Math.max(2, contentWidth / 100));
        Component undoText = Component.translatable("gui.signbuilder.blueprint.undo").withStyle(ChatFormatting.RED);
        Component sizeText = getSizeButtonText();
        Component dirText = getDirButtonText();
        Component backplateText = getBackplateButtonText();
        Component saveText = Component.translatable("gui.signbuilder.blueprint.save");
        int[] buttonWidths = {
                Math.max(42, this.font.width(undoText) + 14),
                Math.max(50, this.font.width(sizeText) + 16),
                Math.max(72, this.font.width(dirText) + 18),
                Math.max(92, this.font.width(backplateText) + 18),
                Math.max(54, this.font.width(saveText) + 16)
        };
        int[] minimumButtonWidths = {36, 42, 58, 74, 42};
        int[] preferredButtonWidths = {78, 72, 112, 140, 104};
        int availableButtonWidth = Math.max(0, contentWidth - buttonGap * 4);
        int totalButtonWidth = 0;
        for (int width : buttonWidths) totalButtonWidth += width;
        while (totalButtonWidth > availableButtonWidth) {
            boolean reduced = false;
            for (int i = 0; i < buttonWidths.length && totalButtonWidth > availableButtonWidth; i++) {
                if (buttonWidths[i] > minimumButtonWidths[i]) {
                    buttonWidths[i]--;
                    totalButtonWidth--;
                    reduced = true;
                }
            }
            if (!reduced) break;
        }
        while (totalButtonWidth < availableButtonWidth) {
            boolean expanded = false;
            for (int i = 0; i < buttonWidths.length && totalButtonWidth < availableButtonWidth; i++) {
                if (buttonWidths[i] < preferredButtonWidths[i]) {
                    buttonWidths[i]++;
                    totalButtonWidth++;
                    expanded = true;
                }
            }
            if (!expanded) break;
        }
        int buttonX = this.panelX + (this.panelWidth - totalButtonWidth - buttonGap * 4) / 2;

        BlueprintActionButton undoButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[0], undoText, 0xFFE06A70, false, () -> {
            ModMessages.sendToServer(new BlueprintUndoC2SPacket());
            this.onClose();
        });
        this.addRenderableWidget(undoButton);

        buttonX += buttonWidths[0] + buttonGap;
        BlueprintActionButton sizeButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[1], sizeText, getSizeAccent(), true, () -> {
            this.size = (this.size % 3) + 1;
            sizeButtonRefresh();
        });
        this.addRenderableWidget(sizeButton);

        buttonX += buttonWidths[1] + buttonGap;
        BlueprintActionButton dirButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[2], dirText, getDirAccent(), this.isVertical, () -> {
            this.isVertical = !this.isVertical;
            dirButtonRefresh();
        });
        this.addRenderableWidget(dirButton);

        buttonX += buttonWidths[2] + buttonGap;
        BlueprintActionButton backplateButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[3], backplateText, getBackplateAccent(), this.withBackplate, () -> {
            this.withBackplate = !this.withBackplate;
            backplateButtonRefresh();
        });
        backplateButton.setTooltip(Tooltip.create(Component.translatable("tooltip.signbuilder.blueprint.backplate_desc")));
        this.addRenderableWidget(backplateButton);

        buttonX += buttonWidths[3] + buttonGap;
        BlueprintActionButton saveButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[4], saveText, SignBuilderUi.ACCENT, true, this::onClose);
        this.addRenderableWidget(saveButton);

        this.sizeButton = sizeButton;
        this.dirButton = dirButton;
        this.backplateButton = backplateButton;
    }

    private BlueprintActionButton sizeButton;
    private BlueprintActionButton dirButton;
    private BlueprintActionButton backplateButton;

    private void sizeButtonRefresh() {
        this.sizeButton.setMessage(getSizeButtonText());
        this.sizeButton.setAccentColor(getSizeAccent());
    }

    private void dirButtonRefresh() {
        this.dirButton.setMessage(getDirButtonText());
        this.dirButton.setAccentColor(getDirAccent());
        this.dirButton.setActive(this.isVertical);
    }

    private void backplateButtonRefresh() {
        this.backplateButton.setMessage(getBackplateButtonText());
        this.backplateButton.setAccentColor(getBackplateAccent());
        this.backplateButton.setActive(this.withBackplate);
    }

    private int getSymbolRowWidth(int count) {
        return count * this.symbolSize + Math.max(0, count - 1) * SYMBOL_GAP;
    }

    private Component getSizeButtonText() {
        String label = switch (this.size) {
            case 3 -> "3x3";
            case 2 -> "2x2";
            default -> "1x1";
        };
        ChatFormatting color = switch (this.size) {
            case 3 -> ChatFormatting.LIGHT_PURPLE;
            case 2 -> ChatFormatting.GOLD;
            default -> ChatFormatting.AQUA;
        };
        return Component.literal(label).withStyle(color);
    }

    private int getSizeAccent() {
        return switch (this.size) {
            case 3 -> 0xFFCE8AF1;
            case 2 -> 0xFFFFC857;
            default -> 0xFF57C8D9;
        };
    }

    private Component getDirButtonText() {
        return Component.literal(this.isVertical ? "↓ " : "→ ")
                .withStyle(this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN)
                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal"));
    }

    private int getDirAccent() {
        return this.isVertical ? 0xFFE5C85E : 0xFF79D67D;
    }

    private Component getBackplateButtonText() {
        return Component.translatable("block.signbuilder.backplate")
                .append(Component.literal(": "))
                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                        .withStyle(this.withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }

    private int getBackplateAccent() {
        return this.withBackplate ? 0xFF79D67D : 0xFF778391;
    }

    private Component getHeaderDetail() {
        return Component.literal((this.size == 3 ? "3x3" : this.size == 2 ? "2x2" : "1x1") + "  ·  ")
                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal"))
                .append(Component.literal("  ·  "))
                .append(Component.translatable("block.signbuilder.backplate"))
                .append(Component.literal(": "))
                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        SignBuilderUi.drawPanel(guiGraphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight);
        SignBuilderUi.drawHeader(guiGraphics, this.font, this.panelX + 1, this.panelY + 1, this.panelWidth - 2, this.title, getHeaderDetail());

        int contentX = this.panelX + 14;
        int contentWidth = Math.max(0, this.panelWidth - 28);
        Component prompt = Component.translatable("gui.signbuilder.blueprint.prompt");
        SignBuilderUi.drawSectionLabel(guiGraphics, this.font, prompt, contentX, this.panelY + scaledY(31), contentWidth);

        String characterCount = this.textField.getValue().length() + "/32";
        guiGraphics.drawString(this.font, characterCount, contentX + contentWidth - this.font.width(characterCount), this.panelY + scaledY(31), SignBuilderUi.MUTED, false);

        int inputX = contentX;
        int inputY = this.panelY + scaledY(47);
        guiGraphics.fillGradient(inputX, inputY, inputX + contentWidth, inputY + scaledY(24), 0xFF141A22, 0xFF1B232D);
        guiGraphics.renderOutline(inputX, inputY, contentWidth, scaledY(24), this.textField.isFocused() ? SignBuilderUi.ACCENT : 0xFF596675);
        guiGraphics.renderOutline(inputX + 2, inputY + scaledY(2), contentWidth - 4, scaledY(20), 0x443F4B5A);

        guiGraphics.fill(contentX, this.panelY + scaledY(164), contentX + contentWidth, this.panelY + scaledY(165), 0x553B4654);
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

        ModMessages.sendToServer(new BlueprintTextC2SPacket(enteredText, this.size, this.isVertical, this.withBackplate));

        if (this.minecraft != null && this.minecraft.player != null) {
            if (!enteredText.isEmpty()) {
                String sizeStr = switch (this.size) {
                    case 3 -> "3x3";
                    case 2 -> "2x2";
                    default -> "1x1";
                };
                ChatFormatting sizeColor = switch (this.size) {
                    case 3 -> ChatFormatting.LIGHT_PURPLE;
                    case 2 -> ChatFormatting.GOLD;
                    default -> ChatFormatting.AQUA;
                };

                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.signbuilder.blueprint.saved")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(enteredText).withStyle(ChatFormatting.AQUA))
                                .append(Component.literal(" [" + sizeStr + "] ").withStyle(sizeColor))
                                .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                                        .withStyle(this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("[▣ ").withStyle(ChatFormatting.GRAY))
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

    private int scaledY(int value) {
        return Math.max(1, Math.round(value * this.layoutScaleY));
    }

    private class SymbolButton extends AbstractButton {
        private final String insert;

        private SymbolButton(int x, int y, int width, int height, String insert, String tooltipKey) {
            this(x, y, width, height, insert, insert, tooltipKey);
        }

        private SymbolButton(int x, int y, int width, int height, String insert, String display, String tooltipKey) {
            super(x, y, width, height, Component.literal(display));
            this.insert = insert;
            this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            boolean hovered = this.isHoveredOrFocused();
            graphics.fillGradient(x, y, x + this.width, y + this.height,
                    hovered ? 0xFF465260 : 0xFF303844,
                    hovered ? 0xFF29323D : 0xFF1C222A);
            graphics.renderOutline(x, y, this.width, this.height, hovered ? SignBuilderUi.ACCENT : 0xFF657181);
            graphics.drawCenteredString(BlueprintScreen.this.font, this.getMessage(), x + this.width / 2, y + (this.height - 8) / 2, SignBuilderUi.TEXT);
        }

        @Override
        public void onPress() {
            BlueprintScreen.this.textField.insertText(this.insert);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private class BlueprintActionButton extends AbstractButton {
        private final Runnable action;
        private int accentColor;
        private boolean active;

        private BlueprintActionButton(int x, int y, int width, Component label, int accentColor, boolean active, Runnable action) {
            super(x, y, width, BlueprintScreen.this.scaledY(24), label);
            this.action = action;
            this.accentColor = accentColor;
            this.active = active;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            boolean highlighted = this.isHoveredOrFocused() || this.active;
            graphics.fillGradient(x, y, x + this.width, y + this.height,
                    this.isHoveredOrFocused() ? 0xFF465260 : 0xFF303844,
                    0xFF1C222A);
            graphics.renderOutline(x, y, this.width, this.height, highlighted ? this.accentColor : 0xFF657181);
            if (this.active || this.isHoveredOrFocused()) {
                graphics.fill(x + 1, y + 1, x + 3, y + this.height - 1, this.accentColor);
            }
            graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), x + this.width / 2, y + (this.height - 8) / 2, SignBuilderUi.TEXT);
        }

        private void setAccentColor(int accentColor) {
            this.accentColor = accentColor;
        }

        private void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public void onPress() {
            this.action.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
