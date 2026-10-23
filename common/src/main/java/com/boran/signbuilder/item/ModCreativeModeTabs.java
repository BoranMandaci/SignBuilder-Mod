package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {

    public static final CreativeModeTab LETTERS_TAB = CreativeTabRegistry.create(
            new ResourceLocation("signbuilder", "letters_tab"),
            () -> new ItemStack(ModBlocks.LETTER_A.get()));

    public static final CreativeModeTab NUMBERS_TAB = CreativeTabRegistry.create(
            new ResourceLocation("signbuilder", "numbers_tab"),
            () -> new ItemStack(ModBlocks.NUMBER_0.get()));

    public static final CreativeModeTab SYMBOLS_TAB = CreativeTabRegistry.create(
            new ResourceLocation("signbuilder", "symbols_tab"),
            () -> new ItemStack(ModBlocks.SYMBOL_PLUS.get()));

    public static void register() {
    }
}
