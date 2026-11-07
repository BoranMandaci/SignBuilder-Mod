package com.boran.signbuilder.client;

import com.boran.signbuilder.client.render.StorageHighlightRenderer;
import com.boran.signbuilder.client.screen.BlueprintScreen;
import com.boran.signbuilder.client.screen.PaintBrushScreen;
import com.boran.signbuilder.client.screen.WrenchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
                                     int fontStyle, int visualAnim,
                                     int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle,
                                     int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems,
                                     boolean loopEnabled, boolean loopShuffle, int loopTransition,
                                     int[] loopAnims, int[] loopDurations, int[] loopColors,
                                     String[] loopTexts1, String[] loopTexts2, int[] loopModes1, int[] loopModes2, int[] loopAligns) {
        Minecraft.getInstance().setScreen(new com.boran.signbuilder.client.screen.LcdScreenScreen(
                masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim,
                widthBlocks, heightBlocks, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems,
                loopEnabled, loopShuffle, loopTransition, loopAnims, loopDurations, loopColors,
                loopTexts1, loopTexts2, loopModes1, loopModes2, loopAligns));
    }

    public static void openLcdScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                     int color, boolean rainbow, boolean glow,
                                     int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                     int fontStyle, int visualAnim,
                                     int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle,
                                     int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems,
                                     boolean loopEnabled, boolean loopShuffle, int loopTransition,
                                     int[] loopAnims, int[] loopDurations, int[] loopColors) {
        openLcdScreen(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim,
                widthBlocks, heightBlocks, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems,
                loopEnabled, loopShuffle, loopTransition, loopAnims, loopDurations, loopColors,
                null, null, null, null, null);
    }

    public static void openLcdScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                     int color, boolean rainbow, boolean glow,
                                     int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                     int fontStyle, int visualAnim,
                                     int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle,
                                     int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems) {
        openLcdScreen(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim,
                widthBlocks, heightBlocks, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems,
                false, false, 1, new int[0], new int[0], new int[0]);
    }

    public static void openLcdScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                     int color, boolean rainbow, boolean glow,
                                     int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                     int fontStyle, int visualAnim,
                                     int widthBlocks, int heightBlocks, int storageMonitorMode, int storageStyle) {
        openLcdScreen(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim, widthBlocks, heightBlocks, storageMonitorMode, storageStyle, 0, 0, List.of());
    }

    public static void openLcdScreen(BlockPos masterPos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                     int color, boolean rainbow, boolean glow,
                                     int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                     int fontStyle, int visualAnim) {
        openLcdScreen(masterPos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim, 1, 1, 0, 0, 0, 0, List.of());
    }

    public static void applyStorageHighlights(ItemStack targetStack, List<BlockPos> minPositions, List<BlockPos> maxPositions, int[] counts, int durationSeconds) {
        StorageHighlightRenderer.setHighlights(targetStack, minPositions, maxPositions, counts, durationSeconds);
    }

    public static void setBlocksDirty(BlockPos pos) {
        Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean isGridModeActive() {
        return com.boran.signbuilder.client.grid.GridModeManager.isGridModeActive();
    }
}
