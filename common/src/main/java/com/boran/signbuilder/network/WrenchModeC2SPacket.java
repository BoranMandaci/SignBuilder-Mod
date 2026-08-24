package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class WrenchModeC2SPacket {
    private final int mode;
    private final boolean detectsMonsters;
    private final boolean detectsAnimals;

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals) {
        this.mode = mode;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
    }

    public WrenchModeC2SPacket(FriendlyByteBuf buf) {
        this.mode = buf.readInt();
        this.detectsMonsters = buf.readBoolean();
        this.detectsAnimals = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(mode);
        buf.writeBoolean(detectsMonsters);
        buf.writeBoolean(detectsAnimals);
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            var stack = player.getMainHandItem();
            if (stack.getItem() instanceof com.boran.signbuilder.item.WrenchItem) {
                stack.getOrCreateTag().putInt("WrenchMode", mode);
                stack.getOrCreateTag().putBoolean("DetectsMonsters", detectsMonsters);
                stack.getOrCreateTag().putBoolean("DetectsAnimals", detectsAnimals);
            }
        }
    }
}