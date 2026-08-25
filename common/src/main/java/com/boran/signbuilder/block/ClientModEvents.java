package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ClientModEvents {

    public static void init() {
        MenuRegistry.registerScreenFactory(ModMenuTypes.SIGN_PRESS_MENU.get(), SignPressScreen::new);

        ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> {
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
        }, getValidBlocks());
    }

    private static Block[] getValidBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (RegistrySupplier<Block> blockSupplier : ModBlocks.BLOCKS) {
            blocks.add(blockSupplier.get());
        }
        return blocks.toArray(new Block[0]);
    }
}