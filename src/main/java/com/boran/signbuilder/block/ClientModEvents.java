package com.boran.signbuilder.block;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = SignBuilder.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.SIGN_PRESS_MENU.get(), SignPressScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof LetterBlockEntity letterEntity) {
                    return letterEntity.getRgbColor();
                }
            }

            if (state != null && state.hasProperty(ModBlocks.COLOR)) {
                return LetterBlockEntity.getActualHexColor(state.getValue(ModBlocks.COLOR));
            }
            return 0xFFFFFF;
        }, ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).toArray(Block[]::new));
    }
}