package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncGridModeC2SPacket(int gridSize) implements CustomPacketPayload {
    public static final Type<SyncGridModeC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("signbuilder", "sync_grid_mode"));

    public static final StreamCodec<FriendlyByteBuf, SyncGridModeC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.gridSize()),
            buf -> new SyncGridModeC2SPacket(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        com.boran.signbuilder.server.ServerGridModeState.setGridSize(context.getPlayer().getUUID(), gridSize);
    }
}
