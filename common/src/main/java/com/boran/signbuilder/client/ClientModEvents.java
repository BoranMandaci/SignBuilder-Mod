package com.boran.signbuilder.client;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.client.grid.GridModeManager;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;

public class ClientModEvents {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        GridModeManager.init();
        KeyMappingRegistry.register(GridModeManager.TOGGLE_GRID_KEY);
        ClientTickEvent.CLIENT_POST.register(HeldButtonController::tick);
        
        dev.architectury.event.events.common.InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide()) {
                net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
                net.minecraft.world.phys.HitResult rawHit = client.hitResult;
                if (rawHit instanceof net.minecraft.world.phys.BlockHitResult hit && hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                    net.minecraft.world.item.ItemStack held = player.getItemInHand(hand);
                    if (held.getItem() instanceof com.boran.signbuilder.item.PaintBrushItem ||
                            held.getItem() instanceof com.boran.signbuilder.item.WrenchItem ||
                            held.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) {
                        return dev.architectury.event.EventResult.pass();
                    }

                    if (player.isShiftKeyDown()) {
                        net.minecraft.world.level.block.state.BlockState targetState = client.level.getBlockState(hit.getBlockPos());
                        if (targetState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock) {
                            if (hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
                                com.boran.signbuilder.client.grid.GridRaytrace.GridRay ray = com.boran.signbuilder.client.grid.GridRaytrace.getRay(hit);
                                if (ray != null && client.level.getBlockEntity(ray.pos()) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
                                    com.boran.signbuilder.block.entity.GridSignBlockEntity.CellData cell = gbe.getCellAt((float) ray.u(), (float) ray.v(), ray.isBack());
                                    if (cell != null) {
                                        int cellKey = com.boran.signbuilder.block.entity.GridSignBlockEntity.makeKey(cell.isBack, cell.gridSize, cell.col, cell.row);
                                        com.boran.signbuilder.network.ModMessages.sendToServer(
                                                new com.boran.signbuilder.network.RemoveGridLetterC2SPacket(ray.pos(), cellKey)
                                        );
                                        player.swing(hand);
                                        client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
                                        return dev.architectury.event.EventResult.interruptFalse();
                                    } else if (ray.isBack() && !gbe.getBackGlyph().isEmpty()) {
                                        com.boran.signbuilder.network.ModMessages.sendToServer(
                                                new com.boran.signbuilder.network.RemoveGridLetterC2SPacket(ray.pos(), -1)
                                        );
                                        player.swing(hand);
                                        client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
                                        return dev.architectury.event.EventResult.interruptFalse();
                                    } else if (!ray.isBack() && !gbe.getFrontGlyph().isEmpty()) {
                                        com.boran.signbuilder.network.ModMessages.sendToServer(
                                                new com.boran.signbuilder.network.RemoveGridLetterC2SPacket(ray.pos(), -2)
                                        );
                                        player.swing(hand);
                                        client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
                                        return dev.architectury.event.EventResult.interruptFalse();
                                    }
                                }
                            }
                            return dev.architectury.event.EventResult.interruptFalse();
                        }
                    }

                    if (GridModeManager.isGridModeActive() && GridModeManager.isHoldingLetterBlock(player)) {
                        int gridSize = GridModeManager.getCurrentGridSize();
                        com.boran.signbuilder.client.grid.GridRaytrace.GridHit gridHit = com.boran.signbuilder.client.grid.GridRaytrace.getGridHit(hit, gridSize);
                        if (gridHit != null) {
                            net.minecraft.core.BlockPos targetPos = gridHit.pos();
                            boolean isBack = com.boran.signbuilder.block.GridSignBlock.isBackIndex(gridHit.index(), gridSize);
                            int rawIndex = com.boran.signbuilder.block.GridSignBlock.getLocalIndex(gridHit.index(), gridSize);
                            int col = rawIndex % gridSize;
                            int row = rawIndex / gridSize;

                            if (client.level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
                                if (gbe.hasCollision(gridSize, col, row, isBack)) {
                                    client.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F));
                                    return dev.architectury.event.EventResult.interruptFalse();
                                }
                            }

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
                        return dev.architectury.event.EventResult.interruptFalse();
                    }
                }
            }
            return dev.architectury.event.EventResult.pass();
        });
    }
}
