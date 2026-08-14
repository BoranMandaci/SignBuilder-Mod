package com.boran.signbuilder.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

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

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                var stack = player.getMainHandItem();
                if (stack.getItem() instanceof com.boran.signbuilder.item.WrenchItem) {
                    stack.getOrCreateTag().putInt("WrenchMode", mode);
                    stack.getOrCreateTag().putBoolean("DetectsMonsters", detectsMonsters);
                    stack.getOrCreateTag().putBoolean("DetectsAnimals", detectsAnimals);
                }
            }
        });
        return true;
    }
}