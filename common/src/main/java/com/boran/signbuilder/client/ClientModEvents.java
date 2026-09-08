package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ClientModEvents {

    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(client -> {
            MenuRegistry.registerScreenFactory(ModMenuTypes.SIGN_PRESS_MENU.get(), SignPressScreen::new);

            BlockEntityRendererRegistry.register(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);

            ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> {
                if (level != null && pos != null) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof LetterBlockEntity letter) {
                        if (state.is(ModBlocks.BACKPLATE.get())) {
                            return tintIndex == 1 ? letter.getBackplateBackColor() : letter.getBackplateFrontColor();
                        }
                        if (tintIndex == 0) {
                            return letter.getRgbColor();
                        }
                        if (tintIndex == 1) {
                            return letter.getBackplateBackColor();
                        }
                    }
                }
                return 0xFFFFFF;
            }, getLetterBlocksAndBackplate());
        });
    }

    private static Block[] getLetterBlocksAndBackplate() {
        List<Block> blocks = new ArrayList<>();
        for (var blockSupplier : ModBlocks.BLOCKS) {
            Block block = blockSupplier.get();
            if (block instanceof LetterBlock || block == ModBlocks.BACKPLATE.get()) {
                blocks.add(block);
            }
        }
        return blocks.toArray(new Block[0]);
    }
}