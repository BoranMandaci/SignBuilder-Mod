package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class BlueprintTextC2SPacket {
    private final String text;

    public BlueprintTextC2SPacket(String text) {
        this.text = text;
    }

    public BlueprintTextC2SPacket(FriendlyByteBuf buf) {
        this.text = buf.readUtf(256);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(text, 256);
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            var stack = player.getMainHandItem();
            if (stack.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) {
                stack.getOrCreateTag().putString("BlueprintText", text);
            }
        }
    }
}