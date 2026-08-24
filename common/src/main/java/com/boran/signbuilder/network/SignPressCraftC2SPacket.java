package com.boran.signbuilder.network;

import com.boran.signbuilder.menu.SignPressMenu;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SignPressCraftC2SPacket {
    private final String blockName;
    private final boolean isShiftDown;

    public SignPressCraftC2SPacket(String blockName, boolean isShiftDown) {
        this.blockName = blockName;
        this.isShiftDown = isShiftDown;
    }

    public SignPressCraftC2SPacket(FriendlyByteBuf buf) {
        this.blockName = buf.readUtf();
        this.isShiftDown = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.blockName);
        buf.writeBoolean(this.isShiftDown);
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null && player.containerMenu instanceof SignPressMenu menu) {
            menu.setSelectedBlock(blockName, isShiftDown);
        }
    }
}