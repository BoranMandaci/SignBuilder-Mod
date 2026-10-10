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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private int activeTab = 0;
    private int gridSize = 3;
    private boolean isBannerMode = true;
    private String gridText = "";
    private final String[] gridCells = new String[16];
    private int selectedGridCell = -1;
    private EditBox gridTextField;
    private boolean isUpdatingGridText = false;

    private BlueprintActionButton tabNormalButton;
    private BlueprintActionButton tabGridButton;
    private BlueprintActionButton gridSize2Btn;
    private BlueprintActionButton gridSize3Btn;
    private BlueprintActionButton gridSize4Btn;
    private BlueprintActionButton clearGridButton;
    private BlueprintActionButton sizeButton;
    private BlueprintActionButton dirButton;
    private BlueprintActionButton backplateButton;
    private BlueprintActionButton saveButton;
    private final List<SymbolButton> symbolButtons = new ArrayList<>();

    public BlueprintScreen(String initialText) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
        Arrays.fill(this.gridCells, "");
    }

    public BlueprintScreen(String initialText, int size, boolean isVertical, boolean withBackplate) {
        super(Component.literal("Sign Blueprint"));
        this.initialText = initialText;
        this.size = size;
        this.isVertical = isVertical;
        this.withBackplate = withBackplate;
        Arrays.fill(this.gridCells, "");
    }

    public BlueprintScreen(String initialText, boolean is2x2, boolean isVertical, boolean withBackplate) {
        this(initialText, is2x2 ? 2 : 1, isVertical, withBackplate);
    }

    @Override
    protected void init() {
        super.init();
        this.symbolButtons.clear();
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
            if (stack.getItem() instanceof SignBlueprintItem) {
                CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                if (!tag.isEmpty()) {
                    if (tag.contains("Size")) {
                        this.size = tag.getInt("Size");
                    } else if (tag.getBoolean("Is2x2")) {
                        this.size = 2;
                    } else {
                        this.size = 1;
                    }
                    this.isVertical = tag.getBoolean("IsVertical");
                    this.withBackplate = tag.getBoolean("WithBackplate");

                    if (tag.contains("IsGridMode")) {
                        this.activeTab = tag.getBoolean("IsGridMode") ? 1 : 0;
                    }
                    if (tag.contains("GridSize")) {
                        this.gridSize = tag.getInt("GridSize");
                        if (this.gridSize < 2) this.gridSize = 3;
                    }
                    if (tag.contains("IsBannerMode")) {
                        this.isBannerMode = tag.getBoolean("IsBannerMode");
                    }
                    if (tag.contains("GridText")) {
                        this.gridText = tag.getString("GridText");
                    }
                    Arrays.fill(this.gridCells, "");
                    if (tag.contains("GridCells", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("GridCells", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag ctag = list.getCompound(i);
                            int idx = ctag.getInt("Index");
                            if (idx >= 0 && idx < 16) {
                                this.gridCells[idx] = ctag.getString("Char");
                            }
                        }
                    } else if (!this.gridText.isEmpty()) {
                        for (int i = 0; i < Math.min(this.gridText.length(), 16); i++) {
                            char ch = this.gridText.charAt(i);
                            if (ch != ' ') {
                                String path = SignBlueprintItem.getBlockPathForChar(ch);
                                if (path != null) this.gridCells[i] = path;
                            }
                        }
                    }
                }
            }
        }

        int contentWidth = Math.max(0, this.panelWidth - 28);
        int rightW = this.panelWidth - 120;
        int maxRowSymbols = 20;
        this.symbolSize = Math.min(scaledY(17), Math.max(8, (rightW - (maxRowSymbols - 1) * SYMBOL_GAP) / maxRowSymbols));
        this.symbolSize = Math.min(this.symbolSize, 17);

        int tabW = Math.min(130, (contentWidth - 6) / 2);
        int tab1X = this.panelX + (this.panelWidth / 2) - tabW - 3;
        int tab2X = this.panelX + (this.panelWidth / 2) + 3;
        int tabY = this.panelY + scaledY(26);

        this.tabNormalButton = new BlueprintActionButton(tab1X, tabY, tabW, scaledY(16), Component.translatable("gui.signbuilder.blueprint.tab_normal"), SignBuilderUi.ACCENT, this.activeTab == 0, () -> switchTab(0));
        this.tabGridButton = new BlueprintActionButton(tab2X, tabY, tabW, scaledY(16), Component.translatable("gui.signbuilder.blueprint.tab_grid"), 0xFF57C8D9, this.activeTab == 1, () -> switchTab(1));
        this.addRenderableWidget(this.tabNormalButton);
        this.addRenderableWidget(this.tabGridButton);

        int inputX = this.panelX + 14;
        int inputWidth = contentWidth;
        int inputY = this.panelY + scaledY(58);
        int inputHeight = scaledY(18);
        int textOffsetY = Math.max(0, (inputHeight - this.font.lineHeight) / 2);
        String initialNormalText = (this.initialText != null && !this.initialText.isEmpty()) ? this.initialText : "";
        if (initialNormalText.isEmpty() && this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!(stack.getItem() instanceof SignBlueprintItem)) stack = this.minecraft.player.getOffhandItem();
            if (stack.getItem() instanceof SignBlueprintItem) {
                CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                if (tag.contains("BlueprintText")) initialNormalText = tag.getString("BlueprintText");
            }
        }
        this.textField = new EditBox(this.font, inputX + 7, inputY + textOffsetY, Math.max(0, inputWidth - 14), Math.max(1, inputHeight - textOffsetY), Component.literal("Word"));
        this.textField.setMaxLength(32);
        this.textField.setValue(initialNormalText);
        this.textField.setBordered(false);
        this.textField.setTextColor(SignBuilderUi.TEXT);
        this.textField.setTextColorUneditable(SignBuilderUi.MUTED);
        this.addRenderableWidget(this.textField);

        int rightX = this.panelX + 106;
        int clearBtnW = Math.max(48, this.font.width(Component.translatable("gui.signbuilder.blueprint.clear_all")) + 12);
        int gridInputW = rightW - clearBtnW - 6;
        this.gridTextField = new EditBox(this.font, rightX + 6, inputY + textOffsetY, Math.max(0, gridInputW - 12), Math.max(1, inputHeight - textOffsetY), Component.literal("GridWord"));
        this.gridTextField.setMaxLength(this.isBannerMode ? 64 : this.gridSize * this.gridSize);
        this.gridTextField.setBordered(false);
        this.gridTextField.setTextColor(SignBuilderUi.TEXT);
        this.gridTextField.setTextColorUneditable(SignBuilderUi.MUTED);
        this.gridTextField.setResponder(this::onGridTextChanged);
        if (!this.gridText.isEmpty()) {
            this.gridTextField.setValue(this.gridText);
        } else {
            syncGridTextFromCells();
        }
        this.addRenderableWidget(this.gridTextField);

        int clearBtnX = rightX + gridInputW + 6;
        this.clearGridButton = new BlueprintActionButton(clearBtnX, inputY, clearBtnW, inputHeight, Component.translatable("gui.signbuilder.blueprint.clear_all"), 0xFFE06A70, false, this::clearGridCells);
        this.addRenderableWidget(this.clearGridButton);

        int btnDockX = this.panelX + 18;
        int btnDockY = this.panelY + scaledY(128);
        int miniBtnW = 24;
        this.gridSize2Btn = new BlueprintActionButton(btnDockX, btnDockY, miniBtnW, scaledY(16), Component.literal("2x2"), 0xFFFFC857, this.gridSize == 2, () -> setGridSize(2));
        this.gridSize3Btn = new BlueprintActionButton(btnDockX + miniBtnW + 2, btnDockY, miniBtnW, scaledY(16), Component.literal("3x3"), 0xFF57C8D9, this.gridSize == 3, () -> setGridSize(3));
        this.gridSize4Btn = new BlueprintActionButton(btnDockX + (miniBtnW + 2) * 2, btnDockY, miniBtnW, scaledY(16), Component.literal("4x4"), 0xFFCE8AF1, this.gridSize == 4, () -> setGridSize(4));
        this.addRenderableWidget(this.gridSize2Btn);
        this.addRenderableWidget(this.gridSize3Btn);
        this.addRenderableWidget(this.gridSize4Btn);

        String[] row1Insert = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "+", "-", "✗", "÷", "=", "%", ">", "<", "~", "*", "#"};
        String[] row1Tooltips = {
                "block.signbuilder.arrow_up", "block.signbuilder.arrow_down", "block.signbuilder.arrow_left",
                "block.signbuilder.arrow_right", "block.signbuilder.arrow_left_up", "block.signbuilder.arrow_right_up",
                "block.signbuilder.arrow_left_down", "block.signbuilder.arrow_right_down", "block.signbuilder.symbol_plus",
                "block.signbuilder.symbol_minus", "block.signbuilder.symbol_cross", "block.signbuilder.symbol_divide", "block.signbuilder.symbol_equals",
                "block.signbuilder.symbol_percent", "block.signbuilder.symbol_greater_than", "block.signbuilder.symbol_less_than", "block.signbuilder.symbol_tilde",
                "block.signbuilder.symbol_asterisk", "block.signbuilder.symbol_hashtag"
        };

        String[] row2Insert = {"«", "•", "»", ",", "?", "!", ":", ";", "'", "\"", "/", "\\", "(", ")", "|", "[", "]", "¦", "@"};
        String[] row2Display = {"• ", "•", " •", ",", "?", "!", ":", ";", "'", "\"", "/", "\\", "(", ")", ")(", "[", "]", "][", "@"};
        String[] row2Tooltips = {
                "block.signbuilder.symbol_dot_left", "block.signbuilder.symbol_dot_center", "block.signbuilder.symbol_dot_right",
                "block.signbuilder.symbol_comma", "block.signbuilder.symbol_question", "block.signbuilder.symbol_exclamation",
                "block.signbuilder.symbol_colon", "block.signbuilder.symbol_semicolon", "block.signbuilder.symbol_apostrophe",
                "block.signbuilder.symbol_quotes", "block.signbuilder.symbol_slash", "block.signbuilder.symbol_backslash",
                "block.signbuilder.symbol_bracket_left", "block.signbuilder.symbol_bracket_right", "block.signbuilder.symbol_bracket_double",
                "block.signbuilder.symbol_square_bracket_left", "block.signbuilder.symbol_square_bracket_right", "block.signbuilder.symbol_square_bracket_double",
                "block.signbuilder.symbol_at"
        };

        String[] row3Insert = {"♥", "★", "&", "✓", "∞", "○", "◆", "♪", "♫", "☠", "🗝", "🔒", "🏆", "⚡", "$", "€", "£", "¥", "₺", "₿"};
        String[] row3Display = {"♥", "★", "&", "✓", "∞", "○", "◆", "♪", "♫", "☠", "🗝", "🔒", "🏆", "⚡", "$", "€", "£", "¥", "₺", "₿"};
        String[] row3Tooltips = {
                "block.signbuilder.symbol_heart", "block.signbuilder.symbol_star", "block.signbuilder.symbol_ampersand",
                "block.signbuilder.symbol_checkmark", "block.signbuilder.symbol_infinity",
                "block.signbuilder.symbol_circle", "block.signbuilder.symbol_diamond",
                "block.signbuilder.symbol_note", "block.signbuilder.symbol_note_double", "block.signbuilder.symbol_skull",
                "block.signbuilder.symbol_key", "block.signbuilder.symbol_lock", "block.signbuilder.symbol_trophy", "block.signbuilder.symbol_lightning",
                "block.signbuilder.symbol_dollar", "block.signbuilder.symbol_euro", "block.signbuilder.symbol_pound", "block.signbuilder.symbol_yen", "block.signbuilder.symbol_tl",
                "block.signbuilder.symbol_bitcoin"
        };

        for (int i = 0; i < row1Insert.length; i++) {
            SymbolButton sb = new SymbolButton(0, 0, this.symbolSize, scaledY(SYMBOL_SIZE), row1Insert[i], row1Tooltips[i]);
            this.symbolButtons.add(sb);
            this.addRenderableWidget(sb);
        }
        for (int i = 0; i < row2Insert.length; i++) {
            SymbolButton sb = new SymbolButton(0, 0, this.symbolSize, scaledY(SYMBOL_SIZE), row2Insert[i], row2Display[i], row2Tooltips[i]);
            this.symbolButtons.add(sb);
            this.addRenderableWidget(sb);
        }
        for (int i = 0; i < row3Insert.length; i++) {
            SymbolButton sb = new SymbolButton(0, 0, this.symbolSize, scaledY(SYMBOL_SIZE), row3Insert[i], row3Display[i], row3Tooltips[i]);
            this.symbolButtons.add(sb);
            this.addRenderableWidget(sb);
        }

        int buttonY = this.panelY + scaledY(172);
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
            if (this.activeTab == 0) {
                this.size = (this.size % 3) + 1;
                sizeButtonRefresh();
            } else {
                int next = (this.gridSize == 2) ? 3 : (this.gridSize == 3 ? 4 : 2);
                setGridSize(next);
            }
        });
        this.addRenderableWidget(sizeButton);

        buttonX += buttonWidths[1] + buttonGap;
        BlueprintActionButton dirButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[2], dirText, getDirAccent(), this.activeTab == 0 ? this.isVertical : this.isBannerMode, () -> {
            if (this.activeTab == 0) {
                this.isVertical = !this.isVertical;
            } else {
                if (this.isBannerMode && !this.isVertical) {
                    this.isVertical = true;
                } else if (this.isBannerMode && this.isVertical) {
                    this.isBannerMode = false;
                    this.isVertical = false;
                    if (this.gridTextField != null) {
                        int maxChars = this.gridSize * this.gridSize;
                        this.gridTextField.setMaxLength(maxChars);
                        if (this.gridTextField.getValue().length() > maxChars) {
                            this.gridTextField.setValue(this.gridTextField.getValue().substring(0, maxChars));
                        }
                    }
                } else {
                    this.isBannerMode = true;
                    this.isVertical = false;
                    if (this.gridTextField != null) {
                        this.gridTextField.setMaxLength(64);
                    }
                }
                if (this.gridTextField != null) {
                    onGridTextChanged(this.gridTextField.getValue());
                }
            }
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
        this.saveButton = saveButton;

        syncGridTextFromCells();
        updateTabWidgets();
        if (this.activeTab == 0) {
            this.setInitialFocus(this.textField);
        } else {
            this.setInitialFocus(this.gridTextField);
        }
    }

    private void switchTab(int tab) {
        this.activeTab = tab;
        this.tabNormalButton.setActive(tab == 0);
        this.tabGridButton.setActive(tab == 1);
        updateTabWidgets();
        if (tab == 0) {
            this.setFocused(this.textField);
        } else {
            this.setFocused(this.gridTextField);
        }
    }

    private void setGridSize(int size) {
        this.gridSize = size;
        if (this.selectedGridCell >= size * size) {
            this.selectedGridCell = size * size - 1;
        }
        if (this.gridTextField != null) {
            int maxChars = this.isBannerMode ? 64 : size * size;
            this.gridTextField.setMaxLength(maxChars);
            if (!this.isBannerMode && this.gridTextField.getValue().length() > maxChars) {
                this.gridTextField.setValue(this.gridTextField.getValue().substring(0, maxChars));
            }
        }
        if (this.isBannerMode && this.gridTextField != null) {
            onGridTextChanged(this.gridTextField.getValue());
        } else {
            syncGridTextFromCells();
        }
        updateGridSizeButtons();
        sizeButtonRefresh();
    }

    private void updateGridSizeButtons() {
        if (this.gridSize2Btn != null) this.gridSize2Btn.setActive(this.gridSize == 2);
        if (this.gridSize3Btn != null) this.gridSize3Btn.setActive(this.gridSize == 3);
        if (this.gridSize4Btn != null) this.gridSize4Btn.setActive(this.gridSize == 4);
    }

    private void clearGridCells() {
        Arrays.fill(this.gridCells, "");
        this.selectedGridCell = 0;
        if (this.gridTextField != null) {
            this.gridTextField.setValue("");
        }
    }

    private void onGridTextChanged(String text) {
        if (this.isUpdatingGridText) return;
        this.isUpdatingGridText = true;
        this.gridText = text;
        if (this.isBannerMode) {
            Arrays.fill(this.gridCells, "");
            int maxChars = Math.min(text.length(), this.gridSize);
            if (this.isVertical) {
                int targetCol = (this.gridSize == 2) ? 0 : 1;
                for (int i = 0; i < maxChars; i++) {
                    char ch = text.charAt(i);
                    if (ch != ' ') {
                        int cp = (ch != 'ß') ? Character.toUpperCase(ch) : ch;
                        String path = SignBlueprintItem.getBlockPathForChar(cp);
                        this.gridCells[i * this.gridSize + targetCol] = (path != null) ? path : "";
                    }
                }
            } else {
                int targetRow = (this.gridSize == 2) ? 0 : 1;
                for (int i = 0; i < maxChars; i++) {
                    char ch = text.charAt(i);
                    if (ch != ' ') {
                        int cp = (ch != 'ß') ? Character.toUpperCase(ch) : ch;
                        String path = SignBlueprintItem.getBlockPathForChar(cp);
                        this.gridCells[targetRow * this.gridSize + i] = (path != null) ? path : "";
                    }
                }
            }
        } else {
            int max = this.gridSize * this.gridSize;
            for (int i = 0; i < max; i++) {
                if (i < text.length()) {
                    char ch = text.charAt(i);
                    if (ch == ' ') {
                        this.gridCells[i] = "";
                    } else {
                        int cp = (ch != 'ß') ? Character.toUpperCase(ch) : ch;
                        String path = SignBlueprintItem.getBlockPathForChar(cp);
                        this.gridCells[i] = (path != null) ? path : "";
                    }
                } else {
                    this.gridCells[i] = "";
                }
            }
        }
        this.isUpdatingGridText = false;
    }

    private void syncGridTextFromCells() {
        if (this.isBannerMode) return;
        StringBuilder sb = new StringBuilder();
        int max = this.gridSize * this.gridSize;
        for (int i = 0; i < max; i++) {
            String p = this.gridCells[i];
            if (p != null && !p.isEmpty()) {
                sb.append(SignBlueprintItem.getDisplayCharForBlockPath(p));
            } else {
                sb.append(" ");
            }
        }
        String trimmed = sb.toString().stripTrailing();
        this.isUpdatingGridText = true;
        if (this.gridTextField != null) {
            this.gridTextField.setValue(trimmed);
        }
        this.isUpdatingGridText = false;
    }

    private void onSymbolClickedInGridMode(String symbol) {
        if (symbol.isEmpty()) return;
        if (this.isBannerMode) {
            if (this.gridTextField != null) {
                this.gridTextField.insertText(symbol);
            }
            return;
        }
        int cp = symbol.codePointAt(0);
        String path = SignBlueprintItem.getBlockPathForChar(cp);
        if (path == null) path = "";

        int max = this.gridSize * this.gridSize;
        if (this.selectedGridCell >= 0 && this.selectedGridCell < max) {
            this.gridCells[this.selectedGridCell] = path;
            this.selectedGridCell = (this.selectedGridCell + 1) % max;
            syncGridTextFromCells();
        } else {
            int target = -1;
            for (int i = 0; i < max; i++) {
                if (this.gridCells[i] == null || this.gridCells[i].isEmpty()) {
                    target = i;
                    break;
                }
            }
            if (target != -1) {
                this.gridCells[target] = path;
                this.selectedGridCell = (target + 1) % max;
                syncGridTextFromCells();
            }
        }
    }

    private void updateTabWidgets() {
        boolean isNormal = (this.activeTab == 0);
        this.textField.visible = isNormal;
        this.gridTextField.visible = !isNormal;
        this.clearGridButton.visible = !isNormal;
        this.gridSize2Btn.visible = !isNormal;
        this.gridSize3Btn.visible = !isNormal;
        this.gridSize4Btn.visible = !isNormal;
        this.dirButton.visible = true;

        updateGridSizeButtons();
        sizeButtonRefresh();
        dirButtonRefresh();

        String[] row1Insert = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "+", "-", "✗", "÷", "=", "%", ">", "<", "~", "*", "#"};
        String[] row2Insert = {"«", "•", "»", ",", "?", "!", ":", ";", "'", "\"", "/", "\\", "(", ")", "|", "[", "]", "¦", "@"};
        String[] row3Insert = {"♥", "★", "&", "✓", "∞", "○", "◆", "♪", "♫", "☠", "🗝", "🔒", "🏆", "⚡", "$", "€", "£", "¥", "₺", "₿"};

        int r1Count = row1Insert.length;
        int r2Count = row2Insert.length;
        int r3Count = row3Insert.length;

        int row1Y, row2Y, row3Y;
        int row1X, row2X, row3X;

        if (isNormal) {
            row1Y = this.panelY + scaledY(80);
            row2Y = this.panelY + scaledY(104);
            row3Y = this.panelY + scaledY(128);

            row1X = this.width / 2 - getSymbolRowWidth(r1Count) / 2;
            row2X = this.width / 2 - getSymbolRowWidth(r2Count) / 2;
            row3X = this.width / 2 - getSymbolRowWidth(r3Count) / 2;
        } else {
            row1Y = this.panelY + scaledY(78);
            row2Y = this.panelY + scaledY(102);
            row3Y = this.panelY + scaledY(126);

            int rightX = this.panelX + 106;
            int rightW = this.panelWidth - 120;

            row1X = rightX + Math.max(0, (rightW - getSymbolRowWidth(r1Count)) / 2);
            row2X = rightX + Math.max(0, (rightW - getSymbolRowWidth(r2Count)) / 2);
            row3X = rightX + Math.max(0, (rightW - getSymbolRowWidth(r3Count)) / 2);
        }

        int btnIdx = 0;
        for (int i = 0; i < r1Count && btnIdx < this.symbolButtons.size(); i++, btnIdx++) {
            SymbolButton sb = this.symbolButtons.get(btnIdx);
            sb.setX(row1X + i * (this.symbolSize + SYMBOL_GAP));
            sb.setY(row1Y);
        }
        for (int i = 0; i < r2Count && btnIdx < this.symbolButtons.size(); i++, btnIdx++) {
            SymbolButton sb = this.symbolButtons.get(btnIdx);
            sb.setX(row2X + i * (this.symbolSize + SYMBOL_GAP));
            sb.setY(row2Y);
        }
        for (int i = 0; i < r3Count && btnIdx < this.symbolButtons.size(); i++, btnIdx++) {
            SymbolButton sb = this.symbolButtons.get(btnIdx);
            sb.setX(row3X + i * (this.symbolSize + SYMBOL_GAP));
            sb.setY(row3Y);
        }
    }

    private void sizeButtonRefresh() {
        this.sizeButton.setMessage(getSizeButtonText());
        this.sizeButton.setAccentColor(getSizeAccent());
    }

    private void dirButtonRefresh() {
        this.dirButton.setMessage(getDirButtonText());
        this.dirButton.setAccentColor(getDirAccent());
        this.dirButton.setActive(this.activeTab == 0 ? this.isVertical : this.isBannerMode);
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
        if (this.activeTab == 1) {
            String label = this.gridSize + "x" + this.gridSize;
            return Component.literal(label).withStyle(ChatFormatting.GOLD);
        }
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
        if (this.activeTab == 1) {
            return 0xFFFFC857;
        }
        return switch (this.size) {
            case 3 -> 0xFFCE8AF1;
            case 2 -> 0xFFFFC857;
            default -> 0xFF57C8D9;
        };
    }

    private Component getDirButtonText() {
        if (this.activeTab == 1) {
            if (this.isBannerMode) {
                if (this.isVertical) {
                    return Component.literal("↓ ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.translatable("gui.signbuilder.blueprint.mode_banner_vert"));
                } else {
                    return Component.literal("→ ").withStyle(ChatFormatting.GREEN)
                            .append(Component.translatable("gui.signbuilder.blueprint.mode_banner_horiz"));
                }
            } else {
                return Component.literal("■ ").withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable("gui.signbuilder.blueprint.mode_single"));
            }
        }
        return Component.literal(this.isVertical ? "↓ " : "→ ")
                .withStyle(this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN)
                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal"));
    }

    private int getDirAccent() {
        if (this.activeTab == 1) {
            if (this.isBannerMode) {
                return this.isVertical ? 0xFFE5C85E : 0xFF79D67D;
            }
            return 0xFF57C8D9;
        }
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
        if (this.activeTab == 1) {
            Component modeComp;
            if (this.isBannerMode) {
                modeComp = Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.mode_banner_vert" : "gui.signbuilder.blueprint.mode_banner_horiz");
            } else {
                modeComp = Component.translatable("gui.signbuilder.blueprint.mode_single");
            }
            return Component.literal("Grid " + this.gridSize + "x" + this.gridSize + "  ·  ")
                    .append(modeComp)
                    .append(Component.literal("  ·  "))
                    .append(Component.translatable("block.signbuilder.backplate"))
                    .append(Component.literal(": "))
                    .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off"));
        }
        return Component.literal((this.size == 3 ? "3x3" : this.size == 2 ? "2x2" : "1x1") + "  ·  ")
                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal"))
                .append(Component.literal("  ·  "))
                .append(Component.translatable("block.signbuilder.backplate"))
                .append(Component.literal(": "))
                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off"));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Leave empty to prevent double rendering
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        SignBuilderUi.drawPanel(guiGraphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight);
        SignBuilderUi.drawHeader(guiGraphics, this.font, this.panelX + 1, this.panelY + 1, this.panelWidth - 2, this.title, getHeaderDetail());

        int contentX = this.panelX + 14;
        int contentWidth = Math.max(0, this.panelWidth - 28);

        if (this.activeTab == 0) {
            Component prompt = Component.translatable("gui.signbuilder.blueprint.prompt");
            SignBuilderUi.drawSectionLabel(guiGraphics, this.font, prompt, contentX, this.panelY + scaledY(44), contentWidth);

            String characterCount = this.textField.getValue().length() + "/32";
            guiGraphics.drawString(this.font, characterCount, contentX + contentWidth - this.font.width(characterCount), this.panelY + scaledY(44), SignBuilderUi.MUTED, true);

            int inputX = contentX;
            int inputY = this.panelY + scaledY(58);
            int inputH = scaledY(18);
            guiGraphics.fillGradient(inputX, inputY, inputX + contentWidth, inputY + inputH, 0xFF141A22, 0xFF1B232D);
            guiGraphics.renderOutline(inputX, inputY, contentWidth, inputH, this.textField.isFocused() ? SignBuilderUi.ACCENT : 0xFF596675);
            guiGraphics.renderOutline(inputX + 2, inputY + scaledY(2), contentWidth - 4, inputH - scaledY(4), 0x443F4B5A);
        } else {
            int rightX = this.panelX + 106;
            int rightW = this.panelWidth - 120;
            int inputY = this.panelY + scaledY(58);
            int inputH = scaledY(18);

            Component prompt = Component.translatable("gui.signbuilder.blueprint.grid_text");
            SignBuilderUi.drawSectionLabel(guiGraphics, this.font, prompt, rightX, this.panelY + scaledY(44), rightW);

            String characterCount;
            if (this.isBannerMode) {
                int len = this.gridTextField != null ? this.gridTextField.getValue().length() : 0;
                int bCount = len == 0 ? 0 : (len + this.gridSize - 1) / this.gridSize;
                characterCount = len + "/64 (" + bCount + " " + (bCount == 1 ? "block" : "blocks") + ")";
            } else {
                int len = this.gridTextField != null ? this.gridTextField.getValue().length() : 0;
                characterCount = len + "/" + (this.gridSize * this.gridSize);
            }
            guiGraphics.drawString(this.font, characterCount, rightX + rightW - this.font.width(characterCount), this.panelY + scaledY(44), SignBuilderUi.MUTED, true);

            int gridInputW = rightW - this.clearGridButton.getWidth() - 6;
            guiGraphics.fillGradient(rightX, inputY, rightX + gridInputW, inputY + inputH, 0xFF141A22, 0xFF1B232D);
            guiGraphics.renderOutline(rightX, inputY, gridInputW, inputH, (this.gridTextField != null && this.gridTextField.isFocused()) ? 0xFF57C8D9 : 0xFF596675);
            guiGraphics.renderOutline(rightX + 2, inputY + scaledY(2), gridInputW - 4, inputH - scaledY(4), 0x443F4B5A);

            renderVisualGrid(guiGraphics, mouseX, mouseY);
        }

        guiGraphics.fill(contentX, this.panelY + scaledY(160), contentX + contentWidth, this.panelY + scaledY(161), 0x553B4654);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderVisualGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int gridX = this.panelX + 18;
        int gridY = this.panelY + scaledY(46);
        int totalPlateSize = 76;

        guiGraphics.fill(gridX - 3, gridY - 3, gridX + totalPlateSize + 3, gridY + totalPlateSize + 3, 0xFF141920);
        guiGraphics.renderOutline(gridX - 3, gridY - 3, totalPlateSize + 6, totalPlateSize + 6, 0xFF4A5564);

        int cellSize;
        int gap;
        if (this.gridSize == 2) {
            cellSize = 36;
            gap = 4;
        } else if (this.gridSize == 3) {
            cellSize = 23;
            gap = 3;
        } else {
            cellSize = 17;
            gap = 2;
        }

        int max = this.gridSize * this.gridSize;
        for (int i = 0; i < max; i++) {
            int row = i / this.gridSize;
            int col = i % this.gridSize;
            int cX = gridX + col * (cellSize + gap);
            int cY = gridY + row * (cellSize + gap);

            boolean hovered = mouseX >= cX && mouseX < cX + cellSize && mouseY >= cY && mouseY < cY + cellSize;
            boolean selected = (i == this.selectedGridCell);

            int bg = selected ? 0xFF2A3D54 : (hovered ? 0xFF232D3B : 0xFF161D26);
            int border = selected ? SignBuilderUi.ACCENT : (hovered ? 0xFF7D8C9E : 0xFF3D4756);

            guiGraphics.fill(cX, cY, cX + cellSize, cY + cellSize, bg);
            guiGraphics.renderOutline(cX, cY, cellSize, cellSize, border);
            if (selected) {
                guiGraphics.renderOutline(cX + 1, cY + 1, cellSize - 2, cellSize - 2, 0x88FFC857);
            }

            String path = this.gridCells[i];
            String display = SignBlueprintItem.getDisplayCharForBlockPath(path);
            if (display.isEmpty()) {
                SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, "·", cX + cellSize / 2, cY + (cellSize - 8) / 2, 0x55888888);
            } else {
                int textColor = selected ? SignBuilderUi.ACCENT : 0xFFFFFFFF;
                SignBuilderUi.drawCenteredStringNoShadow(guiGraphics, this.font, display, cX + cellSize / 2, cY + (cellSize - 8) / 2, textColor);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.activeTab == 1) {
            int gridX = this.panelX + 18;
            int gridY = this.panelY + scaledY(46);
            int cellSize = (this.gridSize == 2) ? 36 : (this.gridSize == 3 ? 23 : 17);
            int gap = (this.gridSize == 2) ? 4 : (this.gridSize == 3 ? 3 : 2);

            int max = this.gridSize * this.gridSize;
            for (int i = 0; i < max; i++) {
                int row = i / this.gridSize;
                int col = i % this.gridSize;
                int cX = gridX + col * (cellSize + gap);
                int cY = gridY + row * (cellSize + gap);

                if (mouseX >= cX && mouseX < cX + cellSize && mouseY >= cY && mouseY < cY + cellSize) {
                    if (this.isBannerMode) {
                        this.isBannerMode = false;
                        this.isVertical = false;
                        if (this.gridTextField != null) {
                            this.gridTextField.setMaxLength(this.gridSize * this.gridSize);
                        }
                        dirButtonRefresh();
                    }
                    if (button == 0) {
                        this.selectedGridCell = i;
                        if (this.gridTextField != null) this.gridTextField.setFocused(false);
                        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F));
                        return true;
                    } else if (button == 1) {
                        this.gridCells[i] = "";
                        this.selectedGridCell = i;
                        syncGridTextFromCells();
                        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 0.8F));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.activeTab == 1 && this.selectedGridCell >= 0 && (this.gridTextField == null || !this.gridTextField.isFocused())) {
            if (codePoint >= 32) {
                int cp = (codePoint != 'ß') ? Character.toUpperCase(codePoint) : codePoint;
                String path = SignBlueprintItem.getBlockPathForChar(cp);
                if (path != null) {
                    if (this.isBannerMode) {
                        this.isBannerMode = false;
                        this.isVertical = false;
                        if (this.gridTextField != null) {
                            this.gridTextField.setMaxLength(this.gridSize * this.gridSize);
                        }
                        dirButtonRefresh();
                    }
                    this.gridCells[this.selectedGridCell] = path;
                    int max = this.gridSize * this.gridSize;
                    this.selectedGridCell = (this.selectedGridCell + 1) % max;
                    syncGridTextFromCells();
                    return true;
                }
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.activeTab == 1 && this.selectedGridCell >= 0 && (this.gridTextField == null || !this.gridTextField.isFocused())) {
            int max = this.gridSize * this.gridSize;
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                if (this.isBannerMode) {
                    this.isBannerMode = false;
                    this.isVertical = false;
                    if (this.gridTextField != null) {
                        this.gridTextField.setMaxLength(this.gridSize * this.gridSize);
                    }
                    dirButtonRefresh();
                }
                this.gridCells[this.selectedGridCell] = "";
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE && this.selectedGridCell > 0) {
                    this.selectedGridCell--;
                }
                syncGridTextFromCells();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_LEFT && this.selectedGridCell > 0) {
                this.selectedGridCell--;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT && this.selectedGridCell < max - 1) {
                this.selectedGridCell++;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP && this.selectedGridCell >= this.gridSize) {
                this.selectedGridCell -= this.gridSize;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN && this.selectedGridCell + this.gridSize < max) {
                this.selectedGridCell += this.gridSize;
                return true;
            }
        }

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

        List<String> cellList = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            cellList.add(this.gridCells[i] != null ? this.gridCells[i] : "");
        }

        boolean isGrid = (this.activeTab == 1);
        String currentGridText = this.gridTextField != null ? this.gridTextField.getValue() : "";

        ModMessages.sendToServer(new BlueprintTextC2SPacket(
                enteredText, this.size, this.isVertical, this.withBackplate,
                isGrid, this.gridSize, this.isBannerMode, currentGridText, cellList
        ));

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!(stack.getItem() instanceof SignBlueprintItem)) {
                stack = this.minecraft.player.getOffhandItem();
            }
            if (stack.getItem() instanceof SignBlueprintItem) {
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                    t.putString("BlueprintText", enteredText);
                    t.putInt("Size", this.size);
                    t.putBoolean("Is2x2", this.size == 2);
                    t.putBoolean("IsVertical", this.isVertical);
                    t.putBoolean("WithBackplate", this.withBackplate);
                    t.putBoolean("IsGridMode", isGrid);
                    t.putInt("GridSize", this.gridSize);
                    t.putBoolean("IsBannerMode", this.isBannerMode);
                    t.putString("GridText", currentGridText);
                    ListTag list = new ListTag();
                    for (int i = 0; i < cellList.size(); i++) {
                        CompoundTag cellTag = new CompoundTag();
                        cellTag.putInt("Index", i);
                        cellTag.putString("Char", cellList.get(i));
                        list.add(cellTag);
                    }
                    t.put("GridCells", list);
                });
            }

            if (isGrid) {
                Component modeComp;
                if (this.isBannerMode) {
                    modeComp = Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.mode_banner_vert" : "gui.signbuilder.blueprint.mode_banner_horiz");
                } else {
                    modeComp = Component.translatable("gui.signbuilder.blueprint.mode_single");
                }
                ChatFormatting modeColor = this.isBannerMode ? (this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN) : ChatFormatting.AQUA;
                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.signbuilder.blueprint.grid_saved")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal("[" + this.gridSize + "x" + this.gridSize + "] ").withStyle(ChatFormatting.GOLD))
                                .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                                .append(modeComp.copy().withStyle(modeColor))
                                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(currentGridText).withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(" [▣ ").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                                        .withStyle(this.withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                                .append(Component.literal("]").withStyle(ChatFormatting.GRAY)),
                        true
                );
            } else if (!enteredText.isEmpty()) {
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
        return value;
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
            SignBuilderUi.drawCenteredStringNoShadow(graphics, BlueprintScreen.this.font, this.getMessage(), x + this.width / 2, y + (this.height - 8) / 2, SignBuilderUi.TEXT);
        }

        @Override
        public void onPress() {
            if (BlueprintScreen.this.activeTab == 0) {
                BlueprintScreen.this.textField.insertText(this.insert);
            } else {
                BlueprintScreen.this.onSymbolClickedInGridMode(this.insert);
            }
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

        private BlueprintActionButton(int x, int y, int width, int height, Component label, int accentColor, boolean active, Runnable action) {
            super(x, y, width, height, label);
            this.action = action;
            this.accentColor = accentColor;
            this.active = active;
        }

        private BlueprintActionButton(int x, int y, int width, Component label, int accentColor, boolean active, Runnable action) {
            this(x, y, width, BlueprintScreen.this.scaledY(24), label, accentColor, active, action);
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
            SignBuilderUi.drawCenteredStringNoShadow(graphics, Minecraft.getInstance().font, this.getMessage(), x + this.width / 2, y + (this.height - 8) / 2, SignBuilderUi.TEXT);
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
