package com.boran.signbuilder.item;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.ModBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SignBuilder.MODID, Registries.ITEM);

    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().defaultDurability(256)));

    public static final RegistrySupplier<Item> SIGN_BLUEPRINT = ITEMS.register("sign_blueprint",
            () -> new SignBlueprintItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> LETTER_A_ITEM = ITEMS.register("letter_a",
            () -> new BlockItem(ModBlocks.LETTER_A.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_B_ITEM = ITEMS.register("letter_b",
            () -> new BlockItem(ModBlocks.LETTER_B.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_C_ITEM = ITEMS.register("letter_c",
            () -> new BlockItem(ModBlocks.LETTER_C.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_D_ITEM = ITEMS.register("letter_d",
            () -> new BlockItem(ModBlocks.LETTER_D.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_E_ITEM = ITEMS.register("letter_e",
            () -> new BlockItem(ModBlocks.LETTER_E.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_F_ITEM = ITEMS.register("letter_f",
            () -> new BlockItem(ModBlocks.LETTER_F.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_G_ITEM = ITEMS.register("letter_g",
            () -> new BlockItem(ModBlocks.LETTER_G.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_H_ITEM = ITEMS.register("letter_h",
            () -> new BlockItem(ModBlocks.LETTER_H.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_I_ITEM = ITEMS.register("letter_i",
            () -> new BlockItem(ModBlocks.LETTER_I.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_J_ITEM = ITEMS.register("letter_j",
            () -> new BlockItem(ModBlocks.LETTER_J.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_K_ITEM = ITEMS.register("letter_k",
            () -> new BlockItem(ModBlocks.LETTER_K.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_L_ITEM = ITEMS.register("letter_l",
            () -> new BlockItem(ModBlocks.LETTER_L.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_M_ITEM = ITEMS.register("letter_m",
            () -> new BlockItem(ModBlocks.LETTER_M.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_N_ITEM = ITEMS.register("letter_n",
            () -> new BlockItem(ModBlocks.LETTER_N.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_O_ITEM = ITEMS.register("letter_o",
            () -> new BlockItem(ModBlocks.LETTER_O.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_P_ITEM = ITEMS.register("letter_p",
            () -> new BlockItem(ModBlocks.LETTER_P.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_R_ITEM = ITEMS.register("letter_r",
            () -> new BlockItem(ModBlocks.LETTER_R.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_S_ITEM = ITEMS.register("letter_s",
            () -> new BlockItem(ModBlocks.LETTER_S.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_T_ITEM = ITEMS.register("letter_t",
            () -> new BlockItem(ModBlocks.LETTER_T.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_U_ITEM = ITEMS.register("letter_u",
            () -> new BlockItem(ModBlocks.LETTER_U.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_V_ITEM = ITEMS.register("letter_v",
            () -> new BlockItem(ModBlocks.LETTER_V.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_Y_ITEM = ITEMS.register("letter_y",
            () -> new BlockItem(ModBlocks.LETTER_Y.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_Z_ITEM = ITEMS.register("letter_z",
            () -> new BlockItem(ModBlocks.LETTER_Z.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_W_ITEM = ITEMS.register("letter_w",
            () -> new BlockItem(ModBlocks.LETTER_W.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> LETTER_X_ITEM = ITEMS.register("letter_x",
            () -> new BlockItem(ModBlocks.LETTER_X.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().defaultDurability(256)));

    public static final RegistrySupplier<Item> NUMBER_0_ITEM = ITEMS.register("number_0",
            () -> new BlockItem(ModBlocks.NUMBER_0.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_1_ITEM = ITEMS.register("number_1",
            () -> new BlockItem(ModBlocks.NUMBER_1.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_2_ITEM = ITEMS.register("number_2",
            () -> new BlockItem(ModBlocks.NUMBER_2.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_3_ITEM = ITEMS.register("number_3",
            () -> new BlockItem(ModBlocks.NUMBER_3.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_4_ITEM = ITEMS.register("number_4",
            () -> new BlockItem(ModBlocks.NUMBER_4.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_5_ITEM = ITEMS.register("number_5",
            () -> new BlockItem(ModBlocks.NUMBER_5.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_6_ITEM = ITEMS.register("number_6",
            () -> new BlockItem(ModBlocks.NUMBER_6.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_7_ITEM = ITEMS.register("number_7",
            () -> new BlockItem(ModBlocks.NUMBER_7.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_8_ITEM = ITEMS.register("number_8",
            () -> new BlockItem(ModBlocks.NUMBER_8.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> NUMBER_9_ITEM = ITEMS.register("number_9",
            () -> new BlockItem(ModBlocks.NUMBER_9.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_UP = ITEMS.register("arrow_up",
            () -> new BlockItem(ModBlocks.ARROW_UP.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_DOWN = ITEMS.register("arrow_down",
            () -> new BlockItem(ModBlocks.ARROW_DOWN.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_LEFT = ITEMS.register("arrow_left",
            () -> new BlockItem(ModBlocks.ARROW_LEFT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_RIGHT = ITEMS.register("arrow_right",
            () -> new BlockItem(ModBlocks.ARROW_RIGHT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_RIGHT_DOWN = ITEMS.register("arrow_right_down",
            () -> new BlockItem(ModBlocks.ARROW_RIGHT_DOWN.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_RIGHT_UP = ITEMS.register("arrow_right_up",
            () -> new BlockItem(ModBlocks.ARROW_RIGHT_UP.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_LEFT_DOWN = ITEMS.register("arrow_left_down",
            () -> new BlockItem(ModBlocks.ARROW_LEFT_DOWN.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> ARROW_LEFT_UP = ITEMS.register("arrow_left_up",
            () -> new BlockItem(ModBlocks.ARROW_LEFT_UP.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_PLUS = ITEMS.register("symbol_plus",
            () -> new BlockItem(ModBlocks.SYMBOL_PLUS.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_MINUS = ITEMS.register("symbol_minus",
            () -> new BlockItem(ModBlocks.SYMBOL_MINUS.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_DOT_LEFT = ITEMS.register("symbol_dot_left",
            () -> new BlockItem(ModBlocks.SYMBOL_DOT_LEFT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_DOT_CENTER = ITEMS.register("symbol_dot_center",
            () -> new BlockItem(ModBlocks.SYMBOL_DOT_CENTER.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_DOT_RIGHT = ITEMS.register("symbol_dot_right",
            () -> new BlockItem(ModBlocks.SYMBOL_DOT_RIGHT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_HEART = ITEMS.register("symbol_heart",
            () -> new BlockItem(ModBlocks.SYMBOL_HEART.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_DOLLAR = ITEMS.register("symbol_dollar",
            () -> new BlockItem(ModBlocks.SYMBOL_DOLLAR.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_EURO = ITEMS.register("symbol_euro",
            () -> new BlockItem(ModBlocks.SYMBOL_EURO.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_POUND = ITEMS.register("symbol_pound",
            () -> new BlockItem(ModBlocks.SYMBOL_POUND.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_TL = ITEMS.register("symbol_tl",
            () -> new BlockItem(ModBlocks.SYMBOL_TL.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_HASHTAG = ITEMS.register("symbol_hashtag",
            () -> new BlockItem(ModBlocks.SYMBOL_HASHTAG.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_STAR = ITEMS.register("symbol_star",
            () -> new BlockItem(ModBlocks.SYMBOL_STAR.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_SLASH = ITEMS.register("symbol_slash",
            () -> new BlockItem(ModBlocks.SYMBOL_SLASH.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_BACKSLASH = ITEMS.register("symbol_backslash",
            () -> new BlockItem(ModBlocks.SYMBOL_BACKSLASH.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_BRACKET_DOUBLE = ITEMS.register("symbol_bracket_double",
            () -> new BlockItem(ModBlocks.SYMBOL_BRACKET_DOUBLE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_BRACKET_LEFT = ITEMS.register("symbol_bracket_left",
            () -> new BlockItem(ModBlocks.SYMBOL_BRACKET_LEFT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_BRACKET_RIGHT = ITEMS.register("symbol_bracket_right",
            () -> new BlockItem(ModBlocks.SYMBOL_BRACKET_RIGHT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_SQUARE_BRACKET_DOUBLE = ITEMS.register("symbol_square_bracket_double",
            () -> new BlockItem(ModBlocks.SYMBOL_SQUARE_BRACKET_DOUBLE.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_SQUARE_BRACKET_LEFT = ITEMS.register("symbol_square_bracket_left",
            () -> new BlockItem(ModBlocks.SYMBOL_SQUARE_BRACKET_LEFT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SYMBOL_SQUARE_BRACKET_RIGHT = ITEMS.register("symbol_square_bracket_right",
            () -> new BlockItem(ModBlocks.SYMBOL_SQUARE_BRACKET_RIGHT.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> SIGN_PRESS = ITEMS.register("sign_press",
            () -> new BlockItem(ModBlocks.SIGN_PRESS.get(), new Item.Properties()));

    public static void register() {
        ITEMS.register();
    }
}