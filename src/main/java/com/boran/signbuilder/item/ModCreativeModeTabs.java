package com.boran.signbuilder.item; // Kendi paket adınla aynı olduğuna emin ol

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
                    .icon(() -> new ItemStack(ModItems.HARF_A_ITEM.get()))
                    .title(Component.translatable("creativetab.signbuilder_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.PAINT_BRUSH.get());
                        pOutput.accept(ModItems.HARF_A_ITEM.get());
                        pOutput.accept(ModItems.HARF_B_ITEM.get());
                        pOutput.accept(ModItems.HARF_C_ITEM.get());
                        pOutput.accept(ModItems.HARF_D_ITEM.get());
                        pOutput.accept(ModItems.HARF_E_ITEM.get());
                        pOutput.accept(ModItems.HARF_F_ITEM.get());
                        pOutput.accept(ModItems.HARF_G_ITEM.get());
                        pOutput.accept(ModItems.HARF_H_ITEM.get());
                        pOutput.accept(ModItems.HARF_I_ITEM.get());
                        pOutput.accept(ModItems.HARF_J_ITEM.get());
                        pOutput.accept(ModItems.HARF_K_ITEM.get());
                        pOutput.accept(ModItems.HARF_L_ITEM.get());
                        pOutput.accept(ModItems.HARF_M_ITEM.get());
                        pOutput.accept(ModItems.HARF_N_ITEM.get());
                        pOutput.accept(ModItems.HARF_O_ITEM.get());
                        pOutput.accept(ModItems.HARF_P_ITEM.get());
                        pOutput.accept(ModItems.HARF_R_ITEM.get());
                        pOutput.accept(ModItems.HARF_S_ITEM.get());
                        pOutput.accept(ModItems.HARF_T_ITEM.get());
                        pOutput.accept(ModItems.HARF_U_ITEM.get());
                        pOutput.accept(ModItems.HARF_V_ITEM.get());
                        pOutput.accept(ModItems.HARF_W_ITEM.get());
                        pOutput.accept(ModItems.HARF_X_ITEM.get());
                        pOutput.accept(ModItems.HARF_Y_ITEM.get());
                        pOutput.accept(ModItems.HARF_Z_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
