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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlueprintScreen extends Screen {
    private static final int PANEL_WIDTH = 520;
    private static final int PANEL_HEIGHT = 218;
    private static final int SYMBOL_SIZE = 20;
    private static final int SYMBOL_GAP = 2;

    private EditBox textField;
    private EditBox backTextField;
    private BlueprintActionButton copyNormalButton;

    private final String initialText;
    private String normalFrontText = "";
    private String normalBackText = "";
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
    private final String[] gridCells = new String[25];
    private final Map<Integer, String> multiGridCells = new LinkedHashMap<>();
    private int selectedGridCell = -1;
    private EditBox gridTextField;
    private boolean isUpdatingGridText = false;

    private BlueprintActionButton tabNormalButton;
    private BlueprintActionButton tabGridButton;
    private BlueprintActionButton gridSize2Btn;
    private BlueprintActionButton gridSize3Btn;
    private BlueprintActionButton gridSize4Btn;
    private BlueprintActionButton gridSize5Btn;
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
                CompoundTag tag = stack.getTag();
                if (tag == null) tag = new CompoundTag();
                if (!tag.isEmpty()) {
                    if (tag.contains("WithBackplate")) {
                        this.withBackplate = tag.getBoolean("WithBackplate");
                    }
                    if (tag.contains("Size")) {
                        this.size = tag.getInt("Size");
                    } else if (tag.contains("Is2x2")) {
                        this.size = tag.getBoolean("Is2x2") ? 2 : 1;
                    }
                    if (tag.contains("IsVertical")) {
                        this.isVertical = tag.getBoolean("IsVertical");
                    }
                    if (tag.contains("IsGridMode")) {
                        this.activeTab = tag.getBoolean("IsGridMode") ? 1 : 0;
                    }
                    if (tag.contains("GridSize")) {
                        int gs = tag.getInt("GridSize");
                        if (gs >= 2 && gs <= 5) this.gridSize = gs;
                    }
                    if (tag.contains("IsBannerMode")) {
                        this.isBannerMode = tag.getBoolean("IsBannerMode");
                    }
                    if (tag.contains("BlueprintText")) {
                        this.normalFrontText = tag.getString("BlueprintText");
                    }
                    if (tag.contains("BlueprintBackText")) {
                        this.normalBackText = tag.getString("BlueprintBackText");
                    }
                    if (tag.contains("GridText")) {
                        this.gridText = tag.getString("GridText");
                    }
                    Arrays.fill(this.gridCells, "");
                    this.multiGridCells.clear();
                    if (tag.contains("GridCells", Tag.TAG_LIST)) {
                        ListTag list = tag.getList("GridCells", Tag.TAG_COMPOUND);
                        for (int i = 0; i < list.size(); i++) {
                            CompoundTag ctag = list.getCompound(i);
                            String cPath = ctag.getString("Char");
                            if (cPath.isEmpty()) continue;
                            boolean cellIsBack = ctag.contains("IsBack") ? ctag.getBoolean("IsBack") : (ctag.getInt("Index") >= 10000);
                            if (cellIsBack) continue;
                            int key;
                            if (ctag.contains("Col") && ctag.contains("Row")) {
                                int gSize = ctag.contains("GridSize") ? ctag.getInt("GridSize") : this.gridSize;
                                int col = ctag.getInt("Col");
                                int row = ctag.getInt("Row");
                                key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, gSize, col, row);
                            } else {
                                int idx = ctag.getInt("Index");
                                if (idx >= 200) {
                                    key = idx;
                                } else {
                                    int col = idx % this.gridSize;
                                    int row = idx / this.gridSize;
                                    key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                                }
                            }
                            this.multiGridCells.put(key, cPath);
                            if (key < 25) {
                                this.gridCells[key] = cPath;
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

        int tabW = Math.max(76, Math.min(108, contentWidth / 4));
        int tabY = this.panelY + scaledY(24);
        int tab1X = this.panelX + 14;
        int tab2X = tab1X + tabW + 4;
        this.tabNormalButton = new BlueprintActionButton(tab1X, tabY, tabW, scaledY(16), Component.translatable("gui.signbuilder.blueprint.tab_normal"), SignBuilderUi.ACCENT, this.activeTab == 0, () -> switchTab(0));
        this.tabGridButton = new BlueprintActionButton(tab2X, tabY, tabW, scaledY(16), Component.translatable("gui.signbuilder.blueprint.tab_grid"), 0xFF57C8D9, this.activeTab == 1, () -> switchTab(1));
        this.addRenderableWidget(this.tabNormalButton);
        this.addRenderableWidget(this.tabGridButton);

        int inputX = this.panelX + 14;
        int inputY = this.panelY + scaledY(58);
        int inputHeight = scaledY(18);
        int textOffsetY = Math.max(0, (inputHeight - this.font.lineHeight) / 2);

        String initialNormalText = (this.normalFrontText != null && !this.normalFrontText.isEmpty()) ? this.normalFrontText : ((this.initialText != null) ? this.initialText : "");
        this.textField = new EditBox(this.font, inputX + 7, inputY + textOffsetY, Math.max(0, contentWidth - 14), Math.max(1, inputHeight - textOffsetY), Component.literal("FrontWord"));
        this.textField.setMaxLength(32);
        this.textField.setValue(initialNormalText);
        this.textField.setBordered(false);
        this.textField.setTextColor(SignBuilderUi.TEXT);
        this.textField.setTextColorUneditable(SignBuilderUi.MUTED);
        this.textField.setResponder(val -> this.normalFrontText = val);
        this.addRenderableWidget(this.textField);

        this.backTextField = new EditBox(this.font, inputX + 7, inputY + textOffsetY, Math.max(0, contentWidth - 14), Math.max(1, inputHeight - textOffsetY), Component.literal("BackWord"));
        this.backTextField.setMaxLength(32);
        this.backTextField.setValue(this.normalBackText);
        this.backTextField.setBordered(false);
        this.backTextField.setTextColor(0xFFFFC857);
        this.backTextField.setTextColorUneditable(SignBuilderUi.MUTED);
        this.backTextField.setResponder(val -> this.normalBackText = val);
        this.addRenderableWidget(this.backTextField);

        this.copyNormalButton = new BlueprintActionButton(0, 0, 24, inputHeight, Component.literal("\u2398"), 0xFF5EEAD4, false, this::copyNormalFrontToBack);
        this.copyNormalButton.setTooltip(Tooltip.create(Component.translatable("gui.signbuilder.blueprint.copy_front")));
        this.addRenderableWidget(this.copyNormalButton);

        int rightX = this.panelX + 106;
        rightW = this.panelWidth - 120;
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
        int miniBtnW = 17;
        int miniBtnGap = 2;
        this.gridSize2Btn = new BlueprintActionButton(btnDockX, btnDockY, miniBtnW, scaledY(16), Component.literal("2x2"), 0xFFFFC857, this.gridSize == 2, () -> setGridSize(2));
        this.gridSize3Btn = new BlueprintActionButton(btnDockX + (miniBtnW + miniBtnGap), btnDockY, miniBtnW, scaledY(16), Component.literal("3x3"), 0xFF57C8D9, this.gridSize == 3, () -> setGridSize(3));
        this.gridSize4Btn = new BlueprintActionButton(btnDockX + (miniBtnW + miniBtnGap) * 2, btnDockY, miniBtnW, scaledY(16), Component.literal("4x4"), 0xFFCE8AF1, this.gridSize == 4, () -> setGridSize(4));
        this.gridSize5Btn = new BlueprintActionButton(btnDockX + (miniBtnW + miniBtnGap) * 3, btnDockY, miniBtnW, scaledY(16), Component.literal("5x5"), 0xFF5EEAD4, this.gridSize == 5, () -> setGridSize(5));
        this.addRenderableWidget(this.gridSize2Btn);
        this.addRenderableWidget(this.gridSize3Btn);
        this.addRenderableWidget(this.gridSize4Btn);
        this.addRenderableWidget(this.gridSize5Btn);

        String[] row1Insert = {"↑", "↓", "←", "→", "↖", "↗", "↙", "↘", "+", "-", "✗", "÷", "=", "%", ">", "<", "~", "*", "#"};
        String[] row1Tooltips = {
                "block.signbuilder.arrow_up", "block.signbuilder.arrow_down", "block.signbuilder.arrow_left",
                "block.signbuilder.arrow_right", "block.signbuilder.arrow_left_up", "block.signbuilder.arrow_right_up",
                "block.signbuilder.arrow_left_down", "block.signbuilder.arrow_right_down", "block.signbuilder.symbol_plus",
                "block.signbuilder.symbol_minus", "block.signbuilder.symbol_cross", "block.signbuilder.symbol_divide", "block.signbuilder.symbol_equals",
                "block.signbuilder.symbol_percent", "block.signbuilder.symbol_greater_than", "block.signbuilder.symbol_less_than", "block.signbuilder.symbol_tilde",
                "block.signbuilder.symbol_asterisk", "block.signbuilder.symbol_hashtag"
        };
        for (int i = 0; i < row1Insert.length; i++) {
            SymbolButton sb = new SymbolButton(0, 0, this.symbolSize, scaledY(SYMBOL_SIZE), row1Insert[i], row1Tooltips[i]);
            this.symbolButtons.add(sb);
            this.addRenderableWidget(sb);
        }

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
        for (int i = 0; i < row2Insert.length; i++) {
            SymbolButton sb = new SymbolButton(0, 0, this.symbolSize, scaledY(SYMBOL_SIZE), row2Insert[i], row2Display[i], row2Tooltips[i]);
            this.symbolButtons.add(sb);
            this.addRenderableWidget(sb);
        }

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
        for (int i = 0; i < row3Insert.length; i++) {
            SymbolButton sb = new SymbolButton(0, 0, this.symbolSize, scaledY(SYMBOL_SIZE), row3Insert[i], row3Display[i], row3Tooltips[i]);
            this.symbolButtons.add(sb);
            this.addRenderableWidget(sb);
        }

        int buttonY = this.panelY + this.panelHeight - scaledY(30);
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
        this.sizeButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[1], sizeText, getSizeAccent(), true, () -> {
            if (this.activeTab == 0) {
                this.size = (this.size % 3) + 1;
                sizeButtonRefresh();
            } else {
                int next = (this.gridSize == 2) ? 3 : (this.gridSize == 3 ? 4 : (this.gridSize == 4 ? 5 : 2));
                setGridSize(next);
            }
        });
        this.addRenderableWidget(this.sizeButton);

        buttonX += buttonWidths[1] + buttonGap;
        this.dirButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[2], dirText, getDirAccent(), this.activeTab == 0 ? this.isVertical : this.isBannerMode, () -> {
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
            }
            dirButtonRefresh();
            updateTabWidgets();
        });
        this.addRenderableWidget(this.dirButton);

        buttonX += buttonWidths[2] + buttonGap;
        this.backplateButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[3], backplateText, getBackplateAccent(), this.withBackplate, () -> {
            this.withBackplate = !this.withBackplate;
            backplateButtonRefresh();
            updateTabWidgets();
        });
        this.addRenderableWidget(this.backplateButton);

        buttonX += buttonWidths[3] + buttonGap;
        this.saveButton = new BlueprintActionButton(buttonX, buttonY, buttonWidths[4], saveText, 0xFF79D67D, false, this::onClose);
        this.addRenderableWidget(this.saveButton);

        updateTabWidgets();
    }

    private void updateTabWidgets() {
        boolean isNormal = (this.activeTab == 0);
        int contentWidth = Math.max(0, this.panelWidth - 28);
        int inputX = this.panelX + 14;
        int inputY = this.panelY + scaledY(58);
        int inputHeight = scaledY(18);
        int textOffsetY = Math.max(0, (inputHeight - this.font.lineHeight) / 2);

        if (this.tabNormalButton != null) this.tabNormalButton.setActive(isNormal);
        if (this.tabGridButton != null) this.tabGridButton.setActive(!isNormal);

        if (isNormal) {
            if (!this.withBackplate) {
                if (this.textField != null) {
                    this.textField.setX(inputX + 7);
                    this.textField.setY(inputY + textOffsetY);
                    this.textField.setWidth(Math.max(0, contentWidth - 14));
                    this.textField.visible = true;
                }
                if (this.backTextField != null) this.backTextField.visible = false;
                if (this.copyNormalButton != null) this.copyNormalButton.visible = false;
            } else {
                int gap = 8;
                int copyBtnW = 24;
                int halfW = Math.max(40, (contentWidth - copyBtnW - gap * 2) / 2);
                if (this.textField != null) {
                    this.textField.setX(inputX + 7);
                    this.textField.setY(inputY + textOffsetY);
                    this.textField.setWidth(Math.max(0, halfW - 14));
                    this.textField.visible = true;
                }
                if (this.copyNormalButton != null) {
                    this.copyNormalButton.setX(inputX + halfW + gap);
                    this.copyNormalButton.setY(inputY);
                    this.copyNormalButton.setWidth(copyBtnW);
                    this.copyNormalButton.visible = true;
                }
                if (this.backTextField != null) {
                    int backX = inputX + halfW + gap + copyBtnW + gap;
                    this.backTextField.setX(backX + 7);
                    this.backTextField.setY(inputY + textOffsetY);
                    this.backTextField.setWidth(Math.max(0, halfW - 14));
                    this.backTextField.visible = true;
                }
            }
            if (this.gridTextField != null) this.gridTextField.visible = false;
            if (this.clearGridButton != null) this.clearGridButton.visible = false;
            if (this.gridSize2Btn != null) this.gridSize2Btn.visible = false;
            if (this.gridSize3Btn != null) this.gridSize3Btn.visible = false;
            if (this.gridSize4Btn != null) this.gridSize4Btn.visible = false;
            if (this.gridSize5Btn != null) this.gridSize5Btn.visible = false;
        } else {
            if (this.textField != null) this.textField.visible = false;
            if (this.backTextField != null) this.backTextField.visible = false;
            if (this.copyNormalButton != null) this.copyNormalButton.visible = false;

            int rightX = this.panelX + 106;
            int rightW = this.panelWidth - 120;
            int clearBtnW = Math.max(48, this.font.width(Component.translatable("gui.signbuilder.blueprint.clear_all")) + 12);
            int gridInputW = rightW - clearBtnW - 6;

            if (this.clearGridButton != null) {
                this.clearGridButton.setX(rightX + gridInputW + 6);
                this.clearGridButton.setY(inputY);
                this.clearGridButton.visible = true;
            }

            if (this.gridTextField != null) {
                this.gridTextField.setX(rightX + 6);
                this.gridTextField.setY(inputY + textOffsetY);
                this.gridTextField.setWidth(Math.max(0, gridInputW - 12));
                this.gridTextField.visible = true;
            }

            if (this.gridSize2Btn != null) this.gridSize2Btn.visible = true;
            if (this.gridSize3Btn != null) this.gridSize3Btn.visible = true;
            if (this.gridSize4Btn != null) this.gridSize4Btn.visible = true;
            if (this.gridSize5Btn != null) this.gridSize5Btn.visible = true;
        }

        positionSymbolButtons();
    }

    private void positionSymbolButtons() {
        int r1Count = 19;
        int r2Count = 19;
        int r3Count = 20;

        int row1Y, row2Y, row3Y;
        int row1X, row2X, row3X;

        if (this.activeTab == 0) {
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

    private void switchTab(int tab) {
        this.activeTab = tab;
        if (this.sizeButton != null) sizeButtonRefresh();
        if (this.dirButton != null) dirButtonRefresh();
        if (this.backplateButton != null) backplateButtonRefresh();
        updateTabWidgets();
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void copyNormalFrontToBack() {
        if (!this.withBackplate) return;
        this.normalBackText = this.textField != null ? this.textField.getValue() : this.normalFrontText;
        if (this.backTextField != null) {
            this.backTextField.setValue(this.normalBackText);
        }
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void setGridSize(int newSize) {
        if (newSize < 2 || newSize > 5) return;
        this.gridSize = newSize;
        if (this.sizeButton != null) sizeButtonRefresh();
        if (this.gridSize2Btn != null) this.gridSize2Btn.setActive(this.gridSize == 2);
        if (this.gridSize3Btn != null) this.gridSize3Btn.setActive(this.gridSize == 3);
        if (this.gridSize4Btn != null) this.gridSize4Btn.setActive(this.gridSize == 4);
        if (this.gridSize5Btn != null) this.gridSize5Btn.setActive(this.gridSize == 5);
        if (!this.isBannerMode && this.gridTextField != null) {
            this.gridTextField.setMaxLength(this.gridSize * this.gridSize);
        }
        this.selectedGridCell = -1;
        syncGridTextFromCells();
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void clearGridCells() {
        Arrays.fill(this.gridCells, "");
        this.multiGridCells.clear();
        this.gridText = "";
        if (this.gridTextField != null) this.gridTextField.setValue("");
        this.selectedGridCell = -1;
        syncGridTextFromCells();
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void removeCollidingCells(int targetGridSize, int targetCol, int targetRow) {
        float tScale = 1.0f / targetGridSize;
        float tu1 = targetCol * tScale;
        float tu2 = tu1 + tScale;
        float tv1 = targetRow * tScale;
        float tv2 = tv1 + tScale;

        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : this.multiGridCells.entrySet()) {
            int key = entry.getKey();
            if (key >= 10000) continue;

            int gSize, col, row;
            if (key >= 200) {
                gSize = (key % 10000) / 100;
                row = (key % 100) / 10;
                col = key % 10;
            } else {
                gSize = this.gridSize;
                col = key % this.gridSize;
                row = key / this.gridSize;
            }
            float eScale = 1.0f / gSize;
            float eu1 = col * eScale;
            float eu2 = eu1 + eScale;
            float ev1 = row * eScale;
            float ev2 = ev1 + eScale;

            if (tu1 < eu2 && tu2 > eu1 && tv1 < ev2 && tv2 > ev1) {
                toRemove.add(key);
            }
        }
        for (int key : toRemove) {
            this.multiGridCells.remove(key);
            if (key < 25) {
                this.gridCells[key] = "";
            }
        }
    }

    private void onGridTextChanged(String text) {
        if (this.isUpdatingGridText) return;
        this.isUpdatingGridText = true;
        this.gridText = text;

        if (this.isBannerMode) {
            this.multiGridCells.clear();
            Arrays.fill(this.gridCells, "");
            int targetRow = (this.gridSize == 2) ? 0 : 1;
            if (this.isVertical) {
                int col = (this.gridSize - 1) / 2;
                int max = Math.min(text.length(), this.gridSize);
                for (int i = 0; i < max; i++) {
                    char ch = text.charAt(i);
                    if (ch != ' ') {
                        int cp = SignBlueprintItem.toUpperChar(ch);
                        String path = SignBlueprintItem.getBlockPathForChar(cp);
                        if (path != null) {
                            if (i * this.gridSize + col < 25) this.gridCells[i * this.gridSize + col] = path;
                            this.multiGridCells.put(com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, i), path);
                        }
                    }
                }
            } else {
                int max = Math.min(text.length(), this.gridSize);
                for (int i = 0; i < max; i++) {
                    char ch = text.charAt(i);
                    if (ch != ' ') {
                        int cp = SignBlueprintItem.toUpperChar(ch);
                        String path = SignBlueprintItem.getBlockPathForChar(cp);
                        if (path != null) {
                            if (targetRow * this.gridSize + i < 25) this.gridCells[targetRow * this.gridSize + i] = path;
                            this.multiGridCells.put(com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, i, targetRow), path);
                        }
                    }
                }
            }
        } else {
            int max = this.gridSize * this.gridSize;
            for (int i = 0; i < max; i++) {
                int col = i % this.gridSize;
                int row = i / this.gridSize;
                int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                if (i < text.length()) {
                    char ch = text.charAt(i);
                    if (ch == ' ') {
                        this.multiGridCells.remove(key);
                        if (i < 25) this.gridCells[i] = "";
                    } else {
                        int cp = SignBlueprintItem.toUpperChar(ch);
                        String path = SignBlueprintItem.getBlockPathForChar(cp);
                        if (path != null) {
                            removeCollidingCells(this.gridSize, col, row);
                            this.multiGridCells.put(key, path);
                            if (i < 25) this.gridCells[i] = path;
                        } else {
                            this.multiGridCells.remove(key);
                            if (i < 25) this.gridCells[i] = "";
                        }
                    }
                } else {
                    this.multiGridCells.remove(key);
                    if (i < 25) this.gridCells[i] = "";
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
            int col = i % this.gridSize;
            int row = i / this.gridSize;
            int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
            String p = this.multiGridCells.get(key);
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
            int col = this.selectedGridCell % this.gridSize;
            int row = this.selectedGridCell / this.gridSize;
            removeCollidingCells(this.gridSize, col, row);
            if (!path.isEmpty()) {
                int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                this.multiGridCells.put(key, path);
                if (this.selectedGridCell < 25) {
                    this.gridCells[this.selectedGridCell] = path;
                }
            }
            this.selectedGridCell = (this.selectedGridCell + 1) % max;
            syncGridTextFromCells();
        } else {
            int target = -1;
            for (int i = 0; i < max; i++) {
                int col = i % this.gridSize;
                int row = i / this.gridSize;
                int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                if (!this.multiGridCells.containsKey(key)) {
                    target = i;
                    break;
                }
            }
            if (target != -1) {
                int col = target % this.gridSize;
                int row = target / this.gridSize;
                removeCollidingCells(this.gridSize, col, row);
                if (!path.isEmpty()) {
                    int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                    this.multiGridCells.put(key, path);
                    if (target < 25) {
                        this.gridCells[target] = path;
                    }
                }
                this.selectedGridCell = (target + 1) % max;
                syncGridTextFromCells();
            }
        }
    }

    private void onSymbolClicked(String symbol) {
        if (symbol.isEmpty()) return;
        if (this.activeTab == 0) {
            if (this.withBackplate && this.backTextField != null && this.backTextField.isFocused()) {
                this.backTextField.insertText(symbol);
            } else if (this.textField != null) {
                this.textField.insertText(symbol);
            }
        } else {
            onSymbolClickedInGridMode(symbol);
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
                    return Component.literal("\u2193 ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.translatable("gui.signbuilder.blueprint.mode_banner_vert"));
                } else {
                    return Component.literal("\u2192 ").withStyle(ChatFormatting.GREEN)
                            .append(Component.translatable("gui.signbuilder.blueprint.mode_banner_horiz"));
                }
            } else {
                return Component.literal("\u25A0 ").withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable("gui.signbuilder.blueprint.mode_single"));
            }
        }
        return Component.literal(this.isVertical ? "\u2193 " : "\u2192 ")
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
            return Component.literal("Grid " + this.gridSize + "x" + this.gridSize + "  \u00B7  ")
                    .append(modeComp)
                    .append(Component.literal("  \u00B7  "))
                    .append(Component.translatable("block.signbuilder.backplate"))
                    .append(Component.literal(": "))
                    .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off"));
        }
        return Component.literal((this.size == 3 ? "3x3" : this.size == 2 ? "2x2" : "1x1") + "  \u00B7  ")
                .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal"))
                .append(Component.literal("  \u00B7  "))
                .append(Component.translatable("block.signbuilder.backplate"))
                .append(Component.literal(": "))
                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off"));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        SignBuilderUi.drawPanel(guiGraphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight);
        SignBuilderUi.drawHeader(guiGraphics, this.font, this.panelX + 1, this.panelY + 1, this.panelWidth - 2, this.title, getHeaderDetail());

        int contentX = this.panelX + 14;
        int contentWidth = Math.max(0, this.panelWidth - 28);
        int inputY = this.panelY + scaledY(58);
        int inputH = scaledY(18);

        if (this.activeTab == 0) {
            if (!this.withBackplate) {
                Component prompt = Component.translatable("gui.signbuilder.blueprint.prompt");
                SignBuilderUi.drawSectionLabel(guiGraphics, this.font, prompt, contentX, this.panelY + scaledY(44), contentWidth);

                String characterCount = this.textField.getValue().length() + "/32";
                guiGraphics.drawString(this.font, characterCount, contentX + contentWidth - this.font.width(characterCount), this.panelY + scaledY(44), SignBuilderUi.MUTED, true);

                int inputX = contentX;
                guiGraphics.fillGradient(inputX, inputY, inputX + contentWidth, inputY + inputH, 0xFF141A22, 0xFF1B232D);
                guiGraphics.renderOutline(inputX, inputY, contentWidth, inputH, this.textField.isFocused() ? SignBuilderUi.ACCENT : 0xFF596675);
                guiGraphics.renderOutline(inputX + 2, inputY + scaledY(2), contentWidth - 4, inputH - scaledY(4), 0x443F4B5A);
            } else {
                int gap = 8;
                int copyBtnW = 24;
                int halfW = Math.max(40, (contentWidth - copyBtnW - gap * 2) / 2);

                Component frontPrompt = Component.translatable("gui.signbuilder.blueprint.side_front");
                SignBuilderUi.drawSectionLabel(guiGraphics, this.font, frontPrompt, contentX, this.panelY + scaledY(44), halfW);
                String frontCount = this.textField.getValue().length() + "/32";
                guiGraphics.drawString(this.font, frontCount, contentX + halfW - this.font.width(frontCount), this.panelY + scaledY(44), SignBuilderUi.MUTED, true);

                guiGraphics.fillGradient(contentX, inputY, contentX + halfW, inputY + inputH, 0xFF141A22, 0xFF1B232D);
                guiGraphics.renderOutline(contentX, inputY, halfW, inputH, this.textField.isFocused() ? SignBuilderUi.ACCENT : 0xFF596675);
                guiGraphics.renderOutline(contentX + 2, inputY + scaledY(2), halfW - 4, inputH - scaledY(4), 0x443F4B5A);

                int backX = contentX + halfW + gap + copyBtnW + gap;
                Component backPrompt = Component.translatable("gui.signbuilder.blueprint.side_back");
                SignBuilderUi.drawSectionLabel(guiGraphics, this.font, backPrompt, backX, this.panelY + scaledY(44), halfW);
                String backCount = this.backTextField.getValue().length() + "/32";
                guiGraphics.drawString(this.font, backCount, backX + halfW - this.font.width(backCount), this.panelY + scaledY(44), SignBuilderUi.MUTED, true);

                guiGraphics.fillGradient(backX, inputY, backX + halfW, inputY + inputH, 0xFF141A22, 0xFF1B232D);
                guiGraphics.renderOutline(backX, inputY, halfW, inputH, this.backTextField.isFocused() ? 0xFFFFB347 : 0xFF596675);
                guiGraphics.renderOutline(backX + 2, inputY + scaledY(2), halfW - 4, inputH - scaledY(4), 0x443F4B5A);
            }
        } else {
            int rightX = this.panelX + 106;
            int rightW = this.panelWidth - 120;
            int clearBtnW = Math.max(48, this.font.width(Component.translatable("gui.signbuilder.blueprint.clear_all")) + 12);
            int gridInputW = rightW - clearBtnW - 6;

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

            guiGraphics.fillGradient(rightX, inputY, rightX + gridInputW, inputY + inputH, 0xFF141A22, 0xFF1B232D);
            guiGraphics.renderOutline(rightX, inputY, gridInputW, inputH, (this.gridTextField != null && this.gridTextField.isFocused()) ? 0xFF57C8D9 : 0xFF596675);
            guiGraphics.renderOutline(rightX + 2, inputY + scaledY(2), gridInputW - 4, inputH - scaledY(4), 0x443F4B5A);

            renderVisualGrid(guiGraphics, mouseX, mouseY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderVisualGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int plateX = this.panelX + 18;
        int plateY = this.panelY + scaledY(44);
        int plateSize = scaledY(68);

        int borderCol = 0xFF4A5568;
        int bgCol1 = 0xFF1A202C;
        int bgCol2 = 0xFF10141D;

        guiGraphics.fillGradient(plateX, plateY, plateX + plateSize, plateY + plateSize, bgCol1, bgCol2);
        guiGraphics.renderOutline(plateX, plateY, plateSize, plateSize, borderCol);

        float cellPixelSize = (float) plateSize / this.gridSize;

        for (int i = 1; i < this.gridSize; i++) {
            int linePos = Math.round(plateX + i * cellPixelSize);
            guiGraphics.fill(linePos, plateY, linePos + 1, plateY + plateSize, 0x443B4556);
            int linePosY = Math.round(plateY + i * cellPixelSize);
            guiGraphics.fill(plateX, linePosY, plateX + plateSize, linePosY + 1, 0x443B4556);
        }

        for (Map.Entry<Integer, String> entry : this.multiGridCells.entrySet()) {
            int key = entry.getKey();
            if (key >= 10000) continue;

            String path = entry.getValue();
            if (path == null || path.isEmpty()) continue;

            int gSize, col, row;
            if (key >= 200) {
                gSize = (key % 10000) / 100;
                row = (key % 100) / 10;
                col = key % 10;
            } else {
                gSize = this.gridSize;
                col = key % this.gridSize;
                row = key / this.gridSize;
            }

            float eCellW = (float) plateSize / gSize;
            int cx = Math.round(plateX + col * eCellW);
            int cy = Math.round(plateY + row * eCellW);
            int cw = Math.round(eCellW);

            guiGraphics.fill(cx + 1, cy + 1, cx + cw - 1, cy + cw - 1, 0x5557C8D9);

            String disp = SignBlueprintItem.getDisplayCharForBlockPath(path);
            int fontW = this.font.width(disp);
            float fontScale = Math.min(1.0F, (eCellW - 2) / Math.max(1, fontW));
            if (fontScale < 0.9F) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(cx + (cw - fontW * fontScale) / 2.0F, cy + (cw - 8 * fontScale) / 2.0F, 0);
                guiGraphics.pose().scale(fontScale, fontScale, 1.0F);
                guiGraphics.drawString(this.font, disp, 0, 0, 0xFFFFFFFF, false);
                guiGraphics.pose().popPose();
            } else {
                int tx = cx + (cw - fontW) / 2;
                int ty = cy + (cw - 8) / 2;
                guiGraphics.drawString(this.font, disp, tx, ty, 0xFFFFFFFF, false);
            }
        }

        if (this.selectedGridCell >= 0 && this.selectedGridCell < this.gridSize * this.gridSize) {
            int scol = this.selectedGridCell % this.gridSize;
            int srow = this.selectedGridCell / this.gridSize;
            int scx = Math.round(plateX + scol * cellPixelSize);
            int scy = Math.round(plateY + srow * cellPixelSize);
            int scw = Math.round(cellPixelSize);
            guiGraphics.renderOutline(scx, scy, scw, scw, 0xFFFFD700);
        }

        if (mouseX >= plateX && mouseX < plateX + plateSize && mouseY >= plateY && mouseY < plateY + plateSize) {
            int hcol = Math.min(this.gridSize - 1, (int) ((mouseX - plateX) / cellPixelSize));
            int hrow = Math.min(this.gridSize - 1, (int) ((mouseY - plateY) / cellPixelSize));
            int hcx = Math.round(plateX + hcol * cellPixelSize);
            int hcy = Math.round(plateY + hrow * cellPixelSize);
            int hcw = Math.round(cellPixelSize);
            guiGraphics.renderOutline(hcx, hcy, hcw, hcw, 0xAA57C8D9);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.activeTab == 1) {
            int plateX = this.panelX + 18;
            int plateY = this.panelY + scaledY(44);
            int plateSize = scaledY(68);

            if (mouseX >= plateX && mouseX < plateX + plateSize && mouseY >= plateY && mouseY < plateY + plateSize) {
                float cellPixelSize = (float) plateSize / this.gridSize;
                int col = Math.min(this.gridSize - 1, (int) ((mouseX - plateX) / cellPixelSize));
                int row = Math.min(this.gridSize - 1, (int) ((mouseY - plateY) / cellPixelSize));
                int idx = row * this.gridSize + col;

                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    this.selectedGridCell = idx;
                    Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    return true;
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    removeCollidingCells(this.gridSize, col, row);
                    int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                    this.multiGridCells.remove(key);
                    if (idx < 25) this.gridCells[idx] = "";
                    syncGridTextFromCells();
                    Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 0.8F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.activeTab == 1 && !this.isBannerMode && this.selectedGridCell >= 0) {
            String path = SignBlueprintItem.getBlockPathForChar(codePoint);
            if (path != null) {
                onSymbolClickedInGridMode(String.valueOf(codePoint));
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.activeTab == 1 && !this.isBannerMode && this.selectedGridCell >= 0) {
            int max = this.gridSize * this.gridSize;
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                int col = this.selectedGridCell % this.gridSize;
                int row = this.selectedGridCell / this.gridSize;
                removeCollidingCells(this.gridSize, col, row);
                int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, this.gridSize, col, row);
                this.multiGridCells.remove(key);
                if (this.selectedGridCell < 25) this.gridCells[this.selectedGridCell] = "";
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
        this.normalFrontText = this.textField != null ? this.textField.getValue() : this.normalFrontText;
        this.normalBackText = this.backTextField != null ? this.backTextField.getValue() : this.normalBackText;
        this.gridText = this.gridTextField != null ? this.gridTextField.getValue() : this.gridText;

        String enteredFrontText = formatText(this.normalFrontText);
        String enteredBackText = this.withBackplate ? formatText(this.normalBackText) : "";

        int maxGridCells = this.gridSize * this.gridSize;
        List<String> cellList = new ArrayList<>(maxGridCells);
        for (int i = 0; i < maxGridCells; i++) {
            cellList.add(this.gridCells[i] != null ? this.gridCells[i] : "");
        }

        List<BlueprintTextC2SPacket.GridCellSpec> multiGridList = new ArrayList<>();
        if (!this.isBannerMode) {
            for (Map.Entry<Integer, String> entry : this.multiGridCells.entrySet()) {
                int key = entry.getKey();
                String path = entry.getValue();
                if (path == null || path.isEmpty()) continue;
                if (key >= 10000) continue;
                int gSize, col, row;
                if (key >= 200) {
                    gSize = (key % 10000) / 100;
                    row = (key % 100) / 10;
                    col = key % 10;
                } else {
                    gSize = this.gridSize;
                    col = key % this.gridSize;
                    row = key / this.gridSize;
                }
                multiGridList.add(new BlueprintTextC2SPacket.GridCellSpec(gSize, col, row, false, path));
            }
        }

        String currentGridText = this.gridText != null ? this.gridText : "";

        ModMessages.sendToServer(new BlueprintTextC2SPacket(
                enteredFrontText,
                enteredBackText,
                this.size,
                this.isVertical,
                this.withBackplate,
                this.activeTab == 1,
                this.gridSize,
                this.isBannerMode,
                currentGridText,
                "",
                cellList,
                multiGridList
        ));

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack stack = this.minecraft.player.getMainHandItem();
            if (!(stack.getItem() instanceof SignBlueprintItem)) {
                stack = this.minecraft.player.getOffhandItem();
            }
            if (stack.getItem() instanceof SignBlueprintItem) {
                boolean isGrid = (this.activeTab == 1);
                CompoundTag t = stack.getOrCreateTag();
                t.putString("BlueprintText", enteredFrontText);
                if (!enteredBackText.isEmpty()) {
                    t.putString("BlueprintBackText", enteredBackText);
                } else {
                    t.remove("BlueprintBackText");
                }
                t.putInt("Size", this.size);
                t.putBoolean("IsVertical", this.isVertical);
                t.putBoolean("WithBackplate", this.withBackplate);
                t.putBoolean("IsGridMode", isGrid);
                t.putInt("GridSize", this.gridSize);
                t.putBoolean("IsBannerMode", this.isBannerMode);
                t.putString("GridText", currentGridText);
                t.remove("GridBackText");

                ListTag list = new ListTag();
                if (!this.isBannerMode) {
                    for (BlueprintTextC2SPacket.GridCellSpec spec : multiGridList) {
                        CompoundTag cellTag = new CompoundTag();
                        int key = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(false, spec.gridSize(), spec.col(), spec.row());
                        cellTag.putInt("Index", key);
                        cellTag.putInt("GridSize", spec.gridSize());
                        cellTag.putInt("Col", spec.col());
                        cellTag.putInt("Row", spec.row());
                        cellTag.putBoolean("IsBack", false);
                        cellTag.putString("Char", spec.charPath());
                        list.add(cellTag);
                    }
                } else {
                    for (int i = 0; i < cellList.size(); i++) {
                        CompoundTag cellTag = new CompoundTag();
                        cellTag.putInt("Index", i);
                        cellTag.putBoolean("IsBack", false);
                        cellTag.putString("Char", cellList.get(i));
                        list.add(cellTag);
                    }
                }
                t.put("GridCells", list);
            }

            boolean isGrid = (this.activeTab == 1);
            if (isGrid) {
                Component modeComp;
                if (this.isBannerMode) {
                    modeComp = Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.mode_banner_vert" : "gui.signbuilder.blueprint.mode_banner_horiz");
                } else {
                    modeComp = Component.translatable("gui.signbuilder.blueprint.mode_single");
                }
                ChatFormatting modeColor = this.isBannerMode ? (this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN) : ChatFormatting.AQUA;
                String extraInfo = !this.isBannerMode ? " (" + multiGridList.size() + " letters)" : "";
                this.minecraft.player.displayClientMessage(
                        Component.translatable("message.signbuilder.blueprint.grid_saved")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal("[" + this.gridSize + "x" + this.gridSize + "] ").withStyle(ChatFormatting.GOLD))
                                .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                                .append(modeComp.copy().withStyle(modeColor))
                                .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(currentGridText + extraInfo).withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable("block.signbuilder.backplate").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(this.withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                                        .withStyle(this.withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                                .append(Component.literal("]").withStyle(ChatFormatting.GRAY)),
                        true
                );
            } else if (!enteredFrontText.isEmpty()) {
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

                boolean isDouble = this.withBackplate && !enteredBackText.isEmpty();
                net.minecraft.network.chat.MutableComponent msg = Component.translatable("message.signbuilder.blueprint.saved")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(enteredFrontText).withStyle(ChatFormatting.WHITE));
                if (isDouble) {
                    msg.append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                       .append(Component.literal(enteredBackText).withStyle(ChatFormatting.GOLD));
                }
                msg.append(Component.literal(" [" + sizeStr + "] ").withStyle(sizeColor))
                        .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                        .append(Component.translatable(this.isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                                .withStyle(this.isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN))
                        .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("[").withStyle(ChatFormatting.GRAY))
                        .append(Component.translatable("block.signbuilder.backplate").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                        .append(Component.translatable(this.withBackplate ? (isDouble ? "gui.signbuilder.blueprint.double_sided" : "gui.signbuilder.on") : "gui.signbuilder.off")
                                .withStyle(this.withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY))
                        .append(Component.literal("]").withStyle(ChatFormatting.GRAY));

                this.minecraft.player.displayClientMessage(msg, true);
            }
        }

        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int scaledY(int value) {
        return Math.max(1, Math.round(value * this.layoutScaleY));
    }

    private class SymbolButton extends AbstractButton {
        private final String insertText;

        private SymbolButton(int x, int y, int width, int height, String insert, String tooltipKey) {
            this(x, y, width, height, insert, insert, tooltipKey);
        }

        private SymbolButton(int x, int y, int width, int height, String insert, String display, String tooltipKey) {
            super(x, y, width, height, Component.literal(display));
            this.insertText = insert;
            if (tooltipKey != null && !tooltipKey.isEmpty()) {
                this.setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
            }
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            boolean hovered = this.isHoveredOrFocused();

            graphics.fillGradient(x, y, x + this.width, y + this.height,
                    hovered ? 0xFF465260 : 0xFF303844,
                    hovered ? 0xFF29323D : 0xFF1C222A);
            graphics.renderOutline(x, y, this.width, this.height,
                    hovered ? SignBuilderUi.ACCENT : 0xFF596675);
            graphics.renderOutline(x + 1, y + 1, this.width - 2, this.height - 2, 0x3328323E);

            int textY = y + (this.height - 8) / 2;
            SignBuilderUi.drawCenteredStringNoShadow(graphics, BlueprintScreen.this.font, this.getMessage(), x + this.width / 2, textY, hovered ? SignBuilderUi.TEXT : SignBuilderUi.MUTED);
        }

        @Override
        public void onPress() {
            onSymbolClicked(this.insertText);
            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }

    private class BlueprintActionButton extends AbstractButton {
        private final Runnable action;
        private int accentColor;
        private boolean active;

        private BlueprintActionButton(int x, int y, int width, int height, Component message, int accentColor, boolean active, Runnable action) {
            super(x, y, width, height, message);
            this.accentColor = accentColor;
            this.active = active;
            this.action = action;
        }

        private BlueprintActionButton(int x, int y, int width, Component message, int accentColor, boolean active, Runnable action) {
            this(x, y, width, scaledY(20), message, accentColor, active, action);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();

            graphics.fillGradient(x, y, x + this.width, y + this.height,
                    this.isHoveredOrFocused() ? 0xFF465260 : 0xFF303844,
                    0xFF1C222A);
            graphics.renderOutline(x, y, this.width, this.height,
                    this.active ? this.accentColor : (this.isHoveredOrFocused() ? 0xFF8FA1B3 : 0xFF596675));
            graphics.renderOutline(x + 1, y + 1, this.width - 2, this.height - 2,
                    this.active ? (0x55000000 | (this.accentColor & 0x00FFFFFF)) : 0x3328323E);

            int textY = y + (this.height - 8) / 2;
            int textColor = this.active ? 0xFFFFFFFF : (this.isHoveredOrFocused() ? 0xFFEEEEEE : SignBuilderUi.TEXT);
            SignBuilderUi.drawCenteredStringNoShadow(graphics, BlueprintScreen.this.font, this.getMessage(), x + this.width / 2, textY, textColor);
        }

        @Override
        public void onPress() {
            this.action.run();
        }

        private void setAccentColor(int color) {
            this.accentColor = color;
        }

        private void setActive(boolean active) {
            this.active = active;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }

    private static String formatText(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append((char) SignBlueprintItem.toUpperChar(c));
        }
        return sb.toString();
    }
}
