package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;

public class ModColorHandlers {

    public static void register() {
        ModBlocks.ALL_SIGN_BLOCKS.forEach(supplier -> supplier.listen(block -> {
            ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> {
                if (tintIndex == 0 && level != null && pos != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof LetterBlockEntity letter) {
                        if (letter.isRainbow() || letter.getRgbColor() != 0xFFFFFF) {
                            return letter.getRgbColor();
                        }
                    }
                }
                if (tintIndex == 0 && state.hasProperty(ModBlocks.COLOR)) {
                    return LetterBlockEntity.getActualHexColor(state.getValue(ModBlocks.COLOR));
                }
                return 0xFFFFFF;
            }, block);
        }));

        ModBlocks.LETTER_ITEMS.forEach(supplier -> supplier.listen(item -> ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, item)));
        ModBlocks.NUMBER_ITEMS.forEach(supplier -> supplier.listen(item -> ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, item)));
        ModBlocks.SYMBOL_ITEMS.forEach(supplier -> supplier.listen(item -> ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, item)));
    }

    private static int getItemColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                if (tag.contains("BlockEntityTag")) {
                    CompoundTag beTag = tag.getCompound("BlockEntityTag");
                    if (beTag.contains("IsRainbow") && beTag.getBoolean("IsRainbow")) {
                        float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                        return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
                    }
                    if (beTag.contains("RGBColor")) {
                        int color = beTag.getInt("RGBColor");
                        if (color != 0xFFFFFF) return color;
                    }
                }
                if (tag.contains("BlockStateTag") && tag.getCompound("BlockStateTag").contains("color")) {
                    try { return LetterBlockEntity.getActualHexColor(Integer.parseInt(tag.getCompound("BlockStateTag").getString("color"))); } catch (Exception ignored) {}
                }
            }
        }
        return 0xFFFFFF;
    }
}