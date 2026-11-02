package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.item.PaintBrushItem;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ModColorHandlers {

    public static void register() {
        List<ItemLike> signItems = new ArrayList<>();
        for (RegistrySupplier<Block> supplier : ModBlocks.ALL_SIGN_BLOCKS) {
            try {
                if (supplier != null && supplier.isPresent()) {
                    signItems.add(supplier.get());
                }
            } catch (Exception ignored) {}
        }

        if (!signItems.isEmpty()) {
            ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, signItems.toArray(new ItemLike[0]));
        }

        try {
            if (ModBlocks.BACKPLATE_ITEM.isPresent()) {
                ColorHandlerRegistry.registerItemColors(ModColorHandlers::getItemColor, ModBlocks.BACKPLATE_ITEM.get());
            }
        } catch (Exception ignored) {}
    }

    public static int getItemColor(ItemStack stack, int tintIndex) {
        CompoundTag beTag = stack.getTagElement("BlockEntityTag");
        if (beTag == null || beTag.isEmpty()) {
            return 0xFFFFFF;
        }

        if (stack.is(ModBlocks.BACKPLATE_ITEM.get())) {
            if (tintIndex == 0) {
                String fMat = getTagString(beTag, "BackplateFrontMaterial", "backplateFrontMaterial", "BPFrontMat", "BPFMat");
                if (!fMat.isEmpty() && !"DEFAULT".equalsIgnoreCase(fMat)) return 0xFFFFFF;

                if (beTag.getBoolean("BackplateFrontRainbow") || beTag.getBoolean("backplateFrontRainbow")
                        || beTag.getBoolean("BPFrontRainbow") || beTag.getBoolean("BPFRainbow")) {
                    return calculateRainbow();
                }
                if (beTag.contains("BackplateFrontColor")) return beTag.getInt("BackplateFrontColor");
                if (beTag.contains("backplateFrontColor")) return beTag.getInt("backplateFrontColor");
                if (beTag.contains("BPFrontColor")) return beTag.getInt("BPFrontColor");
                if (beTag.contains("BPFColor")) return beTag.getInt("BPFColor");
            } else if (tintIndex == 1) {
                String bMat = getTagString(beTag, "BackplateBackMaterial", "backplateBackMaterial", "BPBackMat", "BPBMat");
                if (!bMat.isEmpty() && !"DEFAULT".equalsIgnoreCase(bMat)) return 0xFFFFFF;

                if (beTag.getBoolean("BackplateBackRainbow") || beTag.getBoolean("backplateBackRainbow")
                        || beTag.getBoolean("BPBackRainbow") || beTag.getBoolean("BPBRainbow")) {
                    return calculateRainbow();
                }
                if (beTag.contains("BackplateBackColor")) return beTag.getInt("BackplateBackColor");
                if (beTag.contains("backplateBackColor")) return beTag.getInt("backplateBackColor");
                if (beTag.contains("BPBackColor")) return beTag.getInt("BPBackColor");
                if (beTag.contains("BPBColor")) return beTag.getInt("BPBColor");
            }
            return 0xFFFFFF;
        }

        if (tintIndex != 0 && tintIndex != 1) {
            return 0xFFFFFF;
        }

        String mat = getTagString(beTag, "SavedMaterial", "savedMaterial", "Material", "material");
        if (!mat.isEmpty() && !"DEFAULT".equalsIgnoreCase(mat)) {
            return 0xFFFFFF;
        }

        if (beTag.getBoolean("IsRainbow") || beTag.getBoolean("Rainbow") || beTag.getBoolean("isRainbow")) {
            return calculateRainbow();
        }

        if (beTag.contains("RGBColor")) return beTag.getInt("RGBColor");
        if (beTag.contains("RgbColor")) return beTag.getInt("RgbColor");
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

    private static int calculateRainbow() {
        return com.boran.signbuilder.util.RainbowColorHelper.getRainbowRgb();
    }

    private static String getTagString(CompoundTag tag, String... keys) {
        for (String key : keys) {
            if (tag.contains(key)) {
                return tag.getString(key);
            }
        }
        return "";
    }
}