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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create("signbuilder", Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> LETTERS_TAB = CREATIVE_MODE_TABS.register("letters_tab",
            () -> CreativeTabRegistry.create(Component.translatable("creativetab.signbuilder_letters"),
                    () -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", "letter_a")))));

    public static final RegistrySupplier<CreativeModeTab> NUMBERS_TAB = CREATIVE_MODE_TABS.register("numbers_tab",
            () -> CreativeTabRegistry.create(Component.translatable("creativetab.signbuilder_numbers"),
                    () -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", "number_0")))));

    public static final RegistrySupplier<CreativeModeTab> SYMBOLS_TAB = CREATIVE_MODE_TABS.register("symbols_tab",
            () -> CreativeTabRegistry.create(Component.translatable("creativetab.signbuilder_symbols"),
                    () -> new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", "symbol_plus")))));

    public static void register() {
        CREATIVE_MODE_TABS.register();

        CreativeTabRegistry.modify(LETTERS_TAB, (flags, output, hasOp) -> {
            output.accept(ModItems.PAINT_BRUSH.get());
            output.accept(ModItems.WRENCH.get());
            output.accept(ModItems.SIGN_BLUEPRINT.get());
            output.accept(ModBlocks.SIGN_PRESS_ITEM.get());

            for (RegistrySupplier<Item> itemReg : ModBlocks.LETTER_ITEMS) {
                output.accept(itemReg.get());
            }
        });

        CreativeTabRegistry.modify(NUMBERS_TAB, (flags, output, hasOp) -> {
            for (RegistrySupplier<Item> itemReg : ModBlocks.NUMBER_ITEMS) {
                output.accept(itemReg.get());
            }
        });

        CreativeTabRegistry.modify(SYMBOLS_TAB, (flags, output, hasOp) -> {
            String[] symbolOrder = {
                    "arrow_up", "arrow_down", "arrow_left", "arrow_right",
                    "arrow_left_up", "arrow_right_up", "arrow_left_down", "arrow_right_down",
                    "symbol_plus", "symbol_minus", "symbol_percent",
                    "symbol_dot_left", "symbol_dot_center", "symbol_dot_right", "symbol_comma",
                    "symbol_slash", "symbol_backslash",
                    "symbol_bracket_left", "symbol_bracket_right", "symbol_bracket_double",
                    "symbol_square_bracket_left", "symbol_square_bracket_right", "symbol_square_bracket_double",
                    "symbol_hashtag", "symbol_heart", "symbol_star", "symbol_at", "symbol_ampersand",
                    "symbol_dollar", "symbol_euro", "symbol_pound", "symbol_yen", "symbol_tl"
            };

            for (String name : symbolOrder) {
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("signbuilder", name));
                if (item != Items.AIR) output.accept(item);
            }
        });
    }
}