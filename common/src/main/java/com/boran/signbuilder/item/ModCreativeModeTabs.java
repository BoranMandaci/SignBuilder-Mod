package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create("signbuilder", Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> LETTERS_TAB = CREATIVE_MODE_TABS.register("letters_tab",
            () -> CreativeTabRegistry.create(Component.translatable("creativetab.signbuilder_letters"),
                    () -> new ItemStack(ModBlocks.LETTER_A.get())));

    public static final RegistrySupplier<CreativeModeTab> NUMBERS_TAB = CREATIVE_MODE_TABS.register("numbers_tab",
            () -> CreativeTabRegistry.create(Component.translatable("creativetab.signbuilder_numbers"),
                    () -> new ItemStack(ModBlocks.NUMBER_0.get())));

    public static final RegistrySupplier<CreativeModeTab> SYMBOLS_TAB = CREATIVE_MODE_TABS.register("symbols_tab",
            () -> CreativeTabRegistry.create(Component.translatable("creativetab.signbuilder_symbols"),
                    () -> new ItemStack(ModBlocks.SYMBOL_PLUS.get())));

    public static void register() {
        CREATIVE_MODE_TABS.register();

        CreativeTabRegistry.modify(LETTERS_TAB, (flags, output, hasOp) -> {
            safeAccept(output, ModItems.PAINT_BRUSH);
            safeAccept(output, ModItems.WRENCH);
            safeAccept(output, ModItems.SIGN_BLUEPRINT);
            safeAccept(output, ModBlocks.SIGN_PRESS_ITEM);
            safeAccept(output, ModBlocks.BACKPLATE_ITEM);

            for (RegistrySupplier<Item> itemReg : ModBlocks.LETTER_ITEMS) {
                safeAccept(output, itemReg);
            }
        });

        CreativeTabRegistry.modify(NUMBERS_TAB, (flags, output, hasOp) -> {
            for (RegistrySupplier<Item> itemReg : ModBlocks.NUMBER_ITEMS) {
                safeAccept(output, itemReg);
            }
        });

        CreativeTabRegistry.modify(SYMBOLS_TAB, (flags, output, hasOp) -> {
            String[] symbolOrder = {
                    "arrow_up", "arrow_down", "arrow_left", "arrow_right",
                    "arrow_left_up", "arrow_right_up", "arrow_left_down", "arrow_right_down",
                    "symbol_plus", "symbol_minus", "symbol_divide", "symbol_equals", "symbol_percent",
                    "symbol_dot_left", "symbol_dot_center", "symbol_dot_right", "symbol_comma",
                    "symbol_question", "symbol_exclamation", "symbol_colon", "symbol_semicolon", "symbol_apostrophe", "symbol_quotes",
                    "symbol_slash", "symbol_backslash",
                    "symbol_bracket_left", "symbol_bracket_right", "symbol_bracket_double",
                    "symbol_square_bracket_left", "symbol_square_bracket_right", "symbol_square_bracket_double",
                    "symbol_hashtag", "symbol_heart", "symbol_star", "symbol_at", "symbol_ampersand",
                    "symbol_dollar", "symbol_euro", "symbol_pound", "symbol_yen", "symbol_tl"
            };

            for (String name : symbolOrder) {
                ResourceLocation id = new ResourceLocation("signbuilder", name);
                if (BuiltInRegistries.ITEM.containsKey(id)) {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != null && item != Items.AIR) {
                        output.accept(item);
                    }
                }
            }
        });
    }

    private static void safeAccept(CreativeModeTab.Output output, RegistrySupplier<Item> supplier) {
        try {
            if (supplier != null && supplier.isPresent()) {
                output.accept(supplier.get());
            }
        } catch (Exception ignored) {}
    }
}