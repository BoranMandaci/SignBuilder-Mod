package com.boran.signbuilder.block;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = SignBuilder.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

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