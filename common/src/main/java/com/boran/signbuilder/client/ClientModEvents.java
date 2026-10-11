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
    }
}
