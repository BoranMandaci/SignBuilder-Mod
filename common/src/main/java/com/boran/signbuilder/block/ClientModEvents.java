package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ClientModEvents {

    public static void init() {
        MenuRegistry.registerScreenFactory(ModMenuTypes.SIGN_PRESS_MENU.get(), SignPressScreen::new);

        ColorHandlerRegistry.registerBlockColors((state, level, pos, tintIndex) -> {
            if (level != null && pos != null) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof LetterBlockEntity letter) {
                    return letter.getRgbColor();
                }
            }
            return 0xFFFFFF;
        }, getLetterBlocks());
    }

    private static Block[] getLetterBlocks() {
        List<Block> letters = new ArrayList<>();
        for (var blockSupplier : ModBlocks.BLOCKS) {
            Block block = blockSupplier.get();
            if (block instanceof LetterBlock) {
                letters.add(block);
            }
        }
        return letters.toArray(new Block[0]);
    }
}