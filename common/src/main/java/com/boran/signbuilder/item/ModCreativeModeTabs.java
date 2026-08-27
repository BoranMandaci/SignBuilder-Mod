package com.boran.signbuilder.item;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create("signbuilder", Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> LETTERS_TAB = CREATIVE_MODE_TABS.register("letters_tab",
            () -> CreativeTabRegistry.create(
                    Component.translatable("creativetab.signbuilder_letters"),
                    () -> new ItemStack(ModItems.LETTER_A_ITEM.get())
            ));

    public static final RegistrySupplier<CreativeModeTab> NUMBERS_TAB = CREATIVE_MODE_TABS.register("numbers_tab",
            () -> CreativeTabRegistry.create(
                    Component.translatable("creativetab.signbuilder_numbers"),
                    () -> new ItemStack(ModItems.NUMBER_1_ITEM.get())
            ));

    public static final RegistrySupplier<CreativeModeTab> SYMBOLS_TAB = CREATIVE_MODE_TABS.register("symbols_tab",
            () -> CreativeTabRegistry.create(
                    Component.translatable("creativetab.signbuilder_symbols"),
                    () -> new ItemStack(ModItems.SYMBOL_PLUS.get())
            ));

    public static void register() {
        CREATIVE_MODE_TABS.register();

        CreativeTabRegistry.modify(LETTERS_TAB, (flags, output, hasOp) -> {
            output.accept(ModItems.PAINT_BRUSH.get());
            output.accept(ModItems.WRENCH.get());
            output.accept(ModItems.SIGN_BLUEPRINT.get());
            output.accept(ModItems.SIGN_PRESS.get());

            output.accept(ModItems.LETTER_A_ITEM.get());
            output.accept(ModItems.LETTER_B_ITEM.get());
            output.accept(ModItems.LETTER_C_ITEM.get());
            output.accept(ModItems.LETTER_D_ITEM.get());
            output.accept(ModItems.LETTER_E_ITEM.get());
            output.accept(ModItems.LETTER_F_ITEM.get());
            output.accept(ModItems.LETTER_G_ITEM.get());
            output.accept(ModItems.LETTER_H_ITEM.get());
            output.accept(ModItems.LETTER_I_ITEM.get());
            output.accept(ModItems.LETTER_J_ITEM.get());
            output.accept(ModItems.LETTER_K_ITEM.get());
            output.accept(ModItems.LETTER_L_ITEM.get());
            output.accept(ModItems.LETTER_M_ITEM.get());
            output.accept(ModItems.LETTER_N_ITEM.get());
            output.accept(ModItems.LETTER_O_ITEM.get());
            output.accept(ModItems.LETTER_P_ITEM.get());
            output.accept(ModItems.LETTER_R_ITEM.get());
            output.accept(ModItems.LETTER_S_ITEM.get());
            output.accept(ModItems.LETTER_T_ITEM.get());
            output.accept(ModItems.LETTER_U_ITEM.get());
            output.accept(ModItems.LETTER_V_ITEM.get());
            output.accept(ModItems.LETTER_W_ITEM.get());
            output.accept(ModItems.LETTER_X_ITEM.get());
            output.accept(ModItems.LETTER_Y_ITEM.get());
            output.accept(ModItems.LETTER_Z_ITEM.get());
        });

        CreativeTabRegistry.modify(NUMBERS_TAB, (flags, output, hasOp) -> {
            output.accept(ModItems.NUMBER_0_ITEM.get());
            output.accept(ModItems.NUMBER_1_ITEM.get());
            output.accept(ModItems.NUMBER_2_ITEM.get());
            output.accept(ModItems.NUMBER_3_ITEM.get());
            output.accept(ModItems.NUMBER_4_ITEM.get());
            output.accept(ModItems.NUMBER_5_ITEM.get());
            output.accept(ModItems.NUMBER_6_ITEM.get());
            output.accept(ModItems.NUMBER_7_ITEM.get());
            output.accept(ModItems.NUMBER_8_ITEM.get());
            output.accept(ModItems.NUMBER_9_ITEM.get());
        });

        CreativeTabRegistry.modify(SYMBOLS_TAB, (flags, output, hasOp) -> {
            output.accept(ModItems.ARROW_UP.get());
            output.accept(ModItems.ARROW_DOWN.get());
            output.accept(ModItems.ARROW_LEFT.get());
            output.accept(ModItems.ARROW_RIGHT.get());
            output.accept(ModItems.ARROW_RIGHT_DOWN.get());
            output.accept(ModItems.ARROW_RIGHT_UP.get());
            output.accept(ModItems.ARROW_LEFT_DOWN.get());
            output.accept(ModItems.ARROW_LEFT_UP.get());

            output.accept(ModItems.SYMBOL_PLUS.get());
            output.accept(ModItems.SYMBOL_MINUS.get());
            output.accept(ModItems.SYMBOL_DOT_LEFT.get());
            output.accept(ModItems.SYMBOL_DOT_CENTER.get());
            output.accept(ModItems.SYMBOL_DOT_RIGHT.get());
            output.accept(ModItems.SYMBOL_HEART.get());
            output.accept(ModItems.SYMBOL_HASHTAG.get());
            output.accept(ModItems.SYMBOL_SLASH.get());
            output.accept(ModItems.SYMBOL_BRACKET_LEFT.get());
            output.accept(ModItems.SYMBOL_BRACKET_RIGHT.get());
            output.accept(ModItems.SYMBOL_BRACKET_DOUBLE.get());
            output.accept(ModItems.SYMBOL_DOLLAR.get());
            output.accept(ModItems.SYMBOL_EURO.get());
            output.accept(ModItems.SYMBOL_TL.get());
        });
    }
}