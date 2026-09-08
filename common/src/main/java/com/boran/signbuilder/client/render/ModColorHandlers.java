package com.boran.signbuilder.client;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.item.PaintBrushItem;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ModColorHandlers {

    public static void register() {
        ItemLike[] signItems = ModBlocks.ALL_SIGN_BLOCKS.stream()
                .map(RegistrySupplier::get)
                .toArray(ItemLike[]::new);

        ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, signItems);
        ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, ModBlocks.BACKPLATE_ITEM.get());
    }

    public static int getItemColor(ItemStack stack, int tintIndex) {
        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        if (beTag == null) {
            return 0xFFFFFF;
        }

        if (stack.is(ModBlocks.BACKPLATE_ITEM.get())) {
            if (tintIndex == 0) {
                String fMat = beTag.contains("BackplateFrontMaterial") ? beTag.getString("BackplateFrontMaterial") : beTag.getString("backplateFrontMaterial");
                if (!fMat.isEmpty() && !fMat.equals("DEFAULT")) return 0xFFFFFF;

                if (beTag.getBoolean("BackplateFrontRainbow") || beTag.getBoolean("backplateFrontRainbow")) {
                    float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                    return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
                }
                if (beTag.contains("BackplateFrontColor")) return beTag.getInt("BackplateFrontColor");
                if (beTag.contains("backplateFrontColor")) return beTag.getInt("backplateFrontColor");
            } else if (tintIndex == 1) {
                String bMat = beTag.contains("BackplateBackMaterial") ? beTag.getString("BackplateBackMaterial") : beTag.getString("backplateBackMaterial");
                if (!bMat.isEmpty() && !bMat.equals("DEFAULT")) return 0xFFFFFF;

                if (beTag.getBoolean("BackplateBackRainbow") || beTag.getBoolean("backplateBackRainbow")) {
                    float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                    return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
                }
                if (beTag.contains("BackplateBackColor")) return beTag.getInt("BackplateBackColor");
                if (beTag.contains("backplateBackColor")) return beTag.getInt("backplateBackColor");
            }
            return 0xFFFFFF;
        }

        String mat = beTag.contains("SavedMaterial") ? beTag.getString("SavedMaterial") : beTag.getString("Material");
        if (!mat.isEmpty() && !mat.equals("DEFAULT")) {
            return 0xFFFFFF;
        }

        if (beTag.getBoolean("IsRainbow") || beTag.getBoolean("Rainbow") || beTag.getBoolean("isRainbow")) {
            float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
            return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
        }

        if (beTag.contains("RgbColor")) return beTag.getInt("RgbColor");
        if (beTag.contains("rgbColor")) return beTag.getInt("rgbColor");
        if (beTag.contains("ColorIndex")) return PaintBrushItem.getActualHexColor(beTag.getInt("ColorIndex"));
        if (beTag.contains("Color")) {
            int c = beTag.getInt("Color");
            return (c >= 0 && c <= 15) ? PaintBrushItem.getActualHexColor(c) : c;
        }
        if (beTag.contains("color")) {
            int c = beTag.getInt("color");
            return (c >= 0 && c <= 15) ? PaintBrushItem.getActualHexColor(c) : c;
        }

        return 0xFFFFFF;
    }
}