package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

public record SyncGridModeC2SPacket(int gridSize) {

    public SyncGridModeC2SPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.gridSize);
    }

    public void handle(NetworkManager.PacketContext context) {
        com.boran.signbuilder.server.ServerGridModeState.setGridSize(context.getPlayer().getUUID(), gridSize);
    }
}
