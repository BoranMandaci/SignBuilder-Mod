package com.boran.signbuilder.client;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class LcdFontHelper {

    public static final int FONT_DOT_MATRIX = 0;
    public static final int FONT_DIGITAL_SEGMENT = 1;
    public static final int FONT_MODERN_LCD = 2;
    public static final int FONT_RETRO_ARCADE = 3;
    public static final int FONT_VANILLA = 4;
    public static final int FONT_COUNT = 5;

    public static final ResourceLocation FONT_DOT_MATRIX_LOC = ResourceLocation.fromNamespaceAndPath("signbuilder", "dot_matrix");
    public static final ResourceLocation FONT_DIGITAL_SEGMENT_LOC = ResourceLocation.fromNamespaceAndPath("signbuilder", "digital_segment");
    public static final ResourceLocation FONT_MODERN_LCD_LOC = ResourceLocation.fromNamespaceAndPath("signbuilder", "modern_lcd");
    public static final ResourceLocation FONT_RETRO_ARCADE_LOC = ResourceLocation.fromNamespaceAndPath("signbuilder", "retro_arcade");

    public static final String[] FONT_STYLE_KEYS = {
            "gui.signbuilder.lcd.font.dot_matrix",
            "gui.signbuilder.lcd.font.digital_segment",
            "gui.signbuilder.lcd.font.modern_lcd",
            "gui.signbuilder.lcd.font.retro_arcade",
            "gui.signbuilder.lcd.font.vanilla"
    };

    public static final String[] FONT_STYLE_TOOLTIPS = {
            "gui.signbuilder.lcd.font.dot_matrix.tooltip",
            "gui.signbuilder.lcd.font.digital_segment.tooltip",
            "gui.signbuilder.lcd.font.modern_lcd.tooltip",
            "gui.signbuilder.lcd.font.retro_arcade.tooltip",
            "gui.signbuilder.lcd.font.vanilla.tooltip"
    };

    public static ResourceLocation getFontLocation(int fontStyle) {
        return switch (fontStyle) {
            case FONT_DOT_MATRIX -> FONT_DOT_MATRIX_LOC;
            case FONT_DIGITAL_SEGMENT -> FONT_DIGITAL_SEGMENT_LOC;
            case FONT_MODERN_LCD -> FONT_MODERN_LCD_LOC;
            case FONT_RETRO_ARCADE -> FONT_RETRO_ARCADE_LOC;
            default -> Style.DEFAULT_FONT;
        };
    }

    public static FormattedCharSequence toFormatted(String text, int fontStyle) {
        if (text == null || text.isEmpty()) return FormattedCharSequence.EMPTY;
        ResourceLocation loc = getFontLocation(fontStyle);
        Style baseStyle = Style.EMPTY.withFont(loc);
        return sink -> StringDecomposer.iterateFormatted(text, 0, baseStyle, baseStyle, sink);
    }

    public static int getWidth(Font font, String text, int fontStyle) {
        if (text == null || text.isEmpty() || font == null) return 0;
        return font.width(toFormatted(text, fontStyle));
    }

    public static Component getFontNameComponent(int fontStyle) {
        int idx = Math.max(0, Math.min(FONT_COUNT - 1, fontStyle));
        return Component.translatable(FONT_STYLE_KEYS[idx]);
    }
}
