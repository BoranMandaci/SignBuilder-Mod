package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

import com.boran.signbuilder.client.grid.GridModeManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

public class ClientModEvents {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        GridModeManager.init();
        ClientTickEvent.CLIENT_POST.register(HeldButtonController::tick);
        
        dev.architectury.event.events.common.InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide() && GridModeManager.isGridModeActive()) {
                if (GridModeManager.isHoldingLetterBlock(player)) {
                    net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                    net.minecraft.world.phys.HitResult rawHit = client.hitResult;
                    if (rawHit instanceof net.minecraft.world.phys.BlockHitResult hit && hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                        int gridSize = GridModeManager.getCurrentGridSize();
                        com.boran.signbuilder.client.grid.GridRaytrace.GridHit gridHit = com.boran.signbuilder.client.grid.GridRaytrace.getGridHit(hit, gridSize);
                        if (gridHit != null) {
                            net.minecraft.world.item.ItemStack item = player.getItemInHand(hand);
                            if (item.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
                                net.minecraft.resources.ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(letterBlock);
                                String blockPath = blockId.getPath();
                                
                                com.boran.signbuilder.network.ModMessages.sendToServer(
                                        new com.boran.signbuilder.network.PlaceGridLetterC2SPacket(
                                                gridHit.pos(), gridHit.face(), player.getDirection(), gridHit.index(), gridSize, blockPath
                                        )
                                );
                                player.swing(hand);
                                return dev.architectury.event.EventResult.interruptFalse();
                            }
                        }
                    }
                    return dev.architectury.event.EventResult.interruptFalse();
                }
            }
            return dev.architectury.event.EventResult.pass();
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(client -> {
            KeyMappingRegistry.register(GridModeManager.TOGGLE_GRID_KEY);
            BlockEntityRendererRegistry.register(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.GRID_SIGN_BLOCK_ENTITY.get(), com.boran.signbuilder.client.render.GridSignBlockEntityRenderer::new);

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
