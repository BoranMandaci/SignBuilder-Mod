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
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("creativetab.signbuilder_letters"))
                    .icon(() -> new ItemStack(ModBlocks.LETTER_A.get()))
                    .displayItems((flags, output) -> {
                        safeAccept(output, ModItems.PAINT_BRUSH);
                        safeAccept(output, ModItems.WRENCH);
                        safeAccept(output, ModItems.SIGN_BLUEPRINT);
                        safeAccept(output, ModBlocks.SIGN_PRESS_ITEM);
                        safeAccept(output, ModBlocks.BACKPLATE_ITEM);
            
                        String[] letterOrder = {
                                "letter_a", "letter_b", "letter_c", "letter_d", "letter_e", "letter_f",
                                "letter_g", "letter_h", "letter_i", "letter_j", "letter_k", "letter_l",
                                "letter_m", "letter_n", "letter_o", "letter_p", "letter_q", "letter_r",
                                "letter_s", "letter_t", "letter_u", "letter_v", "letter_w", "letter_x",
                                "letter_y", "letter_z",
                                "letter_a_de", "letter_c_tr", "letter_g_tr", "letter_i_tr",
                                "letter_o_tr", "letter_s_tr", "letter_u_tr", "letter_eszett"
                        };
            
                        for (String name : letterOrder) {
                            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("signbuilder", name);
                            if (BuiltInRegistries.ITEM.containsKey(id)) {
                                Item item = BuiltInRegistries.ITEM.get(id);
                                if (item != Items.AIR) {
                                    output.accept(item);
                                }
                            }
                        }
                    })));

    public static final RegistrySupplier<CreativeModeTab> NUMBERS_TAB = CREATIVE_MODE_TABS.register("numbers_tab",
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("creativetab.signbuilder_numbers"))
                    .icon(() -> new ItemStack(ModBlocks.NUMBER_0.get()))
                    .displayItems((flags, output) -> {
                        for (RegistrySupplier<Item> itemReg : ModBlocks.NUMBER_ITEMS) {
                            safeAccept(output, itemReg);
                        }
                    })));

    public static final RegistrySupplier<CreativeModeTab> SYMBOLS_TAB = CREATIVE_MODE_TABS.register("symbols_tab",
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("creativetab.signbuilder_symbols"))
                    .icon(() -> new ItemStack(ModBlocks.SYMBOL_PLUS.get()))
                    .displayItems((flags, output) -> {
                        String[] symbolOrder = {
                                "arrow_up", "arrow_down", "arrow_left", "arrow_right",
                                "arrow_left_up", "arrow_right_up", "arrow_left_down", "arrow_right_down",
                                "symbol_plus", "symbol_minus", "symbol_cross", "symbol_divide", "symbol_equals", "symbol_percent",
                                "symbol_greater_than", "symbol_less_than", "symbol_tilde",
                                "symbol_dot_left", "symbol_dot_center", "symbol_dot_right", "symbol_comma",
                                "symbol_question", "symbol_exclamation", "symbol_colon", "symbol_semicolon", "symbol_apostrophe", "symbol_quotes",
                                "symbol_slash", "symbol_backslash",
                                "symbol_bracket_left", "symbol_bracket_right", "symbol_bracket_double",
                                "symbol_square_bracket_left", "symbol_square_bracket_right", "symbol_square_bracket_double",
                                "symbol_hashtag", "symbol_heart", "symbol_star", "symbol_at", "symbol_ampersand",
                                "symbol_asterisk", "symbol_checkmark", "symbol_infinity",
                                "symbol_circle", "symbol_diamond", "symbol_note", "symbol_note_double",
                                "symbol_skull", "symbol_key", "symbol_lock", "symbol_trophy", "symbol_lightning",
                                "symbol_dollar", "symbol_euro", "symbol_pound", "symbol_yen", "symbol_tl",
                                "symbol_bitcoin"
                        };
            
                        for (String name : symbolOrder) {
                            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("signbuilder", name);
                            if (BuiltInRegistries.ITEM.containsKey(id)) {
                                Item item = BuiltInRegistries.ITEM.get(id);
                                if (item != Items.AIR) {
                                    output.accept(item);
                                }
                            }
                        }
                    })));

    public static void register() {
        CREATIVE_MODE_TABS.register();

        

        

        
    }

    private static void safeAccept(CreativeModeTab.Output output, RegistrySupplier<Item> supplier) {
        try {
            if (supplier != null && supplier.isPresent()) {
                output.accept(supplier.get());
            }
        } catch (Exception ignored) {}
    }
}