package com.boran.signbuilder.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;

public final class SignBuilderUi {
    public static final int ACCENT = 0xFFFFC857;
    public static final int TEXT = 0xFFF1F4F8;
    public static final int MUTED = 0xFF9AA7B5;
    public static final int DUMMY_FORCE_RECOMPILE = 1;

    private SignBuilderUi() {
    }

    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.fill(x - 5, y + 4, x + width + 5, y + height + 7, 0x55000000);
        graphics.fillGradient(x, y, x + width, y + height, 0xFF28303C, 0xFF161B23);
        graphics.fill(x, y, x + width, y + 2, ACCENT);
        graphics.renderOutline(x - 1, y - 1, width + 2, height + 2, 0xFF738092);
        graphics.renderOutline(x + 2, y + 3, width - 4, height - 6, 0x443C4654);
    }

    public static void drawHeader(GuiGraphics graphics, Font font, int x, int y, int width, Component title, Component detail) {
        graphics.fillGradient(x, y, x + width, y + 24, 0xFF343F4E, 0xFF252D38);
        graphics.fill(x, y, x + 3, y + 24, ACCENT);
        graphics.drawString(font, title, x + 10, y + 5, TEXT, true);
        if (detail != null) {
            graphics.drawString(font, detail, x + width - font.width(detail) - 8, y + 5, MUTED, true);
        }
        graphics.fill(x + 4, y + 23, x + width, y + 24, 0xFF4B5665);
    }

    public static void drawSectionLabel(GuiGraphics graphics, Font font, Component label, int x, int y, int width) {
        graphics.drawString(font, label, x, y, ACCENT, true);
        graphics.fill(x, y + 10, x + width, y + 11, 0x553B4654);
    }

    public static void drawCenteredStringNoShadow(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x - font.width(text) / 2, y, color, true);
    }
    
    public static void drawCenteredStringNoShadow(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x - font.width(text) / 2, y, color, true);
    }
}