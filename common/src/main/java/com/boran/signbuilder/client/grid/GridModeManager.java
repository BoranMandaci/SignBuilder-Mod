package com.boran.signbuilder.client.grid;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class GridModeManager {
    public static final KeyMapping TOGGLE_GRID_KEY = new KeyMapping(
            "key.signbuilder.toggle_grid",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "category.signbuilder.keys"
    );

    private static int currentGridSize = 1;
    private static boolean wasKeyPressed = false;

    public static void init() {
        dev.architectury.event.events.client.ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> {
            com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.SyncGridModeC2SPacket(currentGridSize));
        });

        ClientTickEvent.CLIENT_POST.register(client -> {
            boolean isPressed = TOGGLE_GRID_KEY.isDown();
            if (isPressed && !wasKeyPressed) {
                Player player = client.player;
                if (player != null && (isHoldingLetterBlock(player))) {
                    if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT) ||
                            InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RSHIFT)) {
                        
                        currentGridSize++;
                        if (currentGridSize > 4) {
                            currentGridSize = 1;
                        }
                        
                        String message = currentGridSize == 1 ? "Normal Mode (1x1)" : "Grid Mode (" + currentGridSize + "x" + currentGridSize + ")";
                        player.displayClientMessage(Component.literal(message), true);
                        com.boran.signbuilder.network.ModMessages.sendToServer(new com.boran.signbuilder.network.SyncGridModeC2SPacket(currentGridSize));
                    }
                }
            }
            wasKeyPressed = isPressed;
        });
    }

    public static int getCurrentGridSize() {
        return currentGridSize;
    }

    public static boolean isGridModeActive() {
        return currentGridSize > 1;
    }

    public static boolean isHoldingLetterBlock(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return (main.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock) ||
               (off.getItem() instanceof BlockItem bi2 && bi2.getBlock() instanceof LetterBlock);
    }
}
