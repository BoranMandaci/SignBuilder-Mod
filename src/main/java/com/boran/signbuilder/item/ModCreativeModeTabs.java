package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "signbuilder");

    public static final RegistryObject<CreativeModeTab> SIGNBUILDER_TAB = CREATIVE_MODE_TABS.register("signbuilder_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.LETTER_A_ITEM.get()))
                    .title(Component.translatable("creativetab.signbuilder_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.PAINT_BRUSH.get());
                        pOutput.accept(ModItems.WRENCH.get());

                        pOutput.accept(ModItems.LETTER_A_ITEM.get());
                        pOutput.accept(ModItems.LETTER_B_ITEM.get());
                        pOutput.accept(ModItems.LETTER_C_ITEM.get());
                        pOutput.accept(ModItems.LETTER_D_ITEM.get());
                        pOutput.accept(ModItems.LETTER_E_ITEM.get());
                        pOutput.accept(ModItems.LETTER_F_ITEM.get());
                        pOutput.accept(ModItems.LETTER_G_ITEM.get());
                        pOutput.accept(ModItems.LETTER_H_ITEM.get());
                        pOutput.accept(ModItems.LETTER_I_ITEM.get());
                        pOutput.accept(ModItems.LETTER_J_ITEM.get());
                        pOutput.accept(ModItems.LETTER_K_ITEM.get());
                        pOutput.accept(ModItems.LETTER_L_ITEM.get());
                        pOutput.accept(ModItems.LETTER_M_ITEM.get());
                        pOutput.accept(ModItems.LETTER_N_ITEM.get());
                        pOutput.accept(ModItems.LETTER_O_ITEM.get());
                        pOutput.accept(ModItems.LETTER_P_ITEM.get());
                        pOutput.accept(ModItems.LETTER_R_ITEM.get());
                        pOutput.accept(ModItems.LETTER_S_ITEM.get());
                        pOutput.accept(ModItems.LETTER_T_ITEM.get());
                        pOutput.accept(ModItems.LETTER_U_ITEM.get());
                        pOutput.accept(ModItems.LETTER_V_ITEM.get());
                        pOutput.accept(ModItems.LETTER_W_ITEM.get());
                        pOutput.accept(ModItems.LETTER_X_ITEM.get());
                        pOutput.accept(ModItems.LETTER_Y_ITEM.get());
                        pOutput.accept(ModItems.LETTER_Z_ITEM.get());

                        pOutput.accept(ModItems.NUMBER_0_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_1_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_2_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_3_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_4_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_5_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_6_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_7_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_8_ITEM.get());
                        pOutput.accept(ModItems.NUMBER_9_ITEM.get());

                        pOutput.accept(ModItems.ARROW_UP.get());
                        pOutput.accept(ModItems.ARROW_DOWN.get());
                        pOutput.accept(ModItems.ARROW_LEFT.get());
                        pOutput.accept(ModItems.ARROW_RIGHT.get());
                        pOutput.accept(ModItems.ARROW_RIGHT_DOWN.get());
                        pOutput.accept(ModItems.ARROW_RIGHT_UP.get());
                        pOutput.accept(ModItems.ARROW_LEFT_DOWN.get());
                        pOutput.accept(ModItems.ARROW_LEFT_UP.get());

                        pOutput.accept(ModItems.SYMBOL_PLUS.get());
                        pOutput.accept(ModItems.SYMBOL_MINUS.get());
                        pOutput.accept(ModItems.SYMBOL_DOT_LEFT.get());
                        pOutput.accept(ModItems.SYMBOL_DOT_CENTER.get());
                        pOutput.accept(ModItems.SYMBOL_DOT_RIGHT.get());
                        pOutput.accept(ModItems.SYMBOL_HEART.get());
                        pOutput.accept(ModItems.SYMBOL_SLASH.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}