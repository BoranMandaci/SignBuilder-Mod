package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCreativeModeTabs {

    public static final CreativeModeTab LETTERS_TAB = CreativeTabHelper.create(
            new ResourceLocation("signbuilder", "letters_tab"),
            () -> new ItemStack(ModBlocks.LETTER_A.get()),
            output -> {
                safeAccept(output, ModItems.PAINT_BRUSH);
                safeAccept(output, ModItems.WRENCH);
                safeAccept(output, ModItems.SIGN_BLUEPRINT);
                safeAccept(output, ModBlocks.SIGN_PRESS_ITEM);
                safeAccept(output, ModBlocks.BACKPLATE_ITEM);
                safeAccept(output, ModBlocks.LCD_SCREEN_ITEM);

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
                    ResourceLocation id = new ResourceLocation("signbuilder", name);
                    if (Registry.ITEM.containsKey(id)) {
                        Item item = Registry.ITEM.get(id);
                        safeAccept(output, item);
                    }
                }
            });

    public static final CreativeModeTab NUMBERS_TAB = CreativeTabHelper.create(
            new ResourceLocation("signbuilder", "numbers_tab"),
            () -> new ItemStack(ModBlocks.NUMBER_0.get()),
            output -> {
                for (RegistrySupplier<Item> itemReg : ModBlocks.NUMBER_ITEMS) {
                    safeAccept(output, itemReg);
                }
            });

    public static final CreativeModeTab SYMBOLS_TAB = CreativeTabHelper.create(
            new ResourceLocation("signbuilder", "symbols_tab"),
            () -> new ItemStack(ModBlocks.SYMBOL_PLUS.get()),
            output -> {
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
                    ResourceLocation id = new ResourceLocation("signbuilder", name);
                    if (Registry.ITEM.containsKey(id)) {
                        Item item = Registry.ITEM.get(id);
                        safeAccept(output, item);
                    }
                }
            });

    private static void safeAccept(NonNullList<ItemStack> output, RegistrySupplier<Item> supplier) {
        try {
            if (supplier != null && supplier.isPresent()) {
                output.add(new ItemStack(supplier.get()));
            }
        } catch (Exception ignored) {}
    }

    private static void safeAccept(NonNullList<ItemStack> output, Item item) {
        try {
            if (item != null && item != Items.AIR) {
                output.add(new ItemStack(item));
            }
        } catch (Exception ignored) {}
    }

    public static void register() {
    }
}
