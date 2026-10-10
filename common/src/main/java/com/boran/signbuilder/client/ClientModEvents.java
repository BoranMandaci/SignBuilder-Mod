package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.grid.GridModeManager;
import com.boran.signbuilder.client.grid.GridRaytrace;
import com.boran.signbuilder.client.render.GridSignBlockEntityRenderer;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.screen.SignPressScreen;
import com.boran.signbuilder.menu.ModMenuTypes;
import com.boran.signbuilder.network.ModMessages;
import com.boran.signbuilder.network.PlaceGridLetterC2SPacket;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public class ClientModEvents {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        GridModeManager.init();
        ClientTickEvent.CLIENT_POST.register(HeldButtonController::tick);

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide() && GridModeManager.isGridModeActive()) {
                if (GridModeManager.isHoldingLetterBlock(player)) {
                    Minecraft client = Minecraft.getInstance();
                    HitResult rawHit = client.hitResult;
                    if (rawHit instanceof BlockHitResult hit && hit.getType() != HitResult.Type.MISS) {
                        int gridSize = GridModeManager.getCurrentGridSize();
                        GridRaytrace.GridHit gridHit = GridRaytrace.getGridHit(hit, gridSize);
                        if (gridHit != null) {
                            ItemStack item = player.getItemInHand(hand);
                            if (item.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
                                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(letterBlock);
                                String blockPath = blockId.getPath();

                                ModMessages.sendToServer(
                                        new PlaceGridLetterC2SPacket(
                                                gridHit.pos(), gridHit.face(), player.getDirection(), gridHit.index(), gridSize, blockPath
                                        )
                                );
                                player.swing(hand);
                                return EventResult.interruptFalse();
                            }
                        }
                    }
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        ClientLifecycleEvent.CLIENT_SETUP.register(client -> {
            KeyMappingRegistry.register(GridModeManager.TOGGLE_GRID_KEY);
            MenuRegistry.registerScreenFactory(ModMenuTypes.SIGN_PRESS_MENU.get(), SignPressScreen::new);

            BlockEntityRendererRegistry.register(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(ModBlockEntities.GRID_SIGN_BLOCK_ENTITY.get(), GridSignBlockEntityRenderer::new);

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
