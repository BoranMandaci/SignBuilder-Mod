package com.boran.signbuilder.client;

import com.boran.signbuilder.client.screen.BlueprintScreen;
import com.boran.signbuilder.client.screen.PaintBrushScreen;
import com.boran.signbuilder.client.screen.WrenchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientHooks {
    public static void openBlueprintScreen(String text) {
        Minecraft.getInstance().setScreen(new BlueprintScreen(text));
    }

    public static void openPaintBrushScreen() {
        Minecraft.getInstance().setScreen(new PaintBrushScreen());
    }

    public static void openWrenchScreen(int mode, boolean detectsMonsters, boolean detectsAnimals) {
        Minecraft.getInstance().setScreen(new WrenchScreen(mode, detectsMonsters, detectsAnimals));
    }

    public static void openLcdScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                     int color, boolean rainbow, boolean glow,
                                     int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                     int fontStyle, int visualAnim) {
        Minecraft.getInstance().setScreen(new com.boran.signbuilder.client.screen.LcdScreenScreen(
                masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim));
    }

    public static void setBlocksDirty(BlockPos pos) {
        Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isGridModeActive() {
        return com.boran.signbuilder.client.grid.GridModeManager.isGridModeActive();
    }
}