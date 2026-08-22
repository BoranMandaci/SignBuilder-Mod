package com.boran.signbuilder.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

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

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                var stack = player.getMainHandItem();
                if (stack.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) {
                    stack.getOrCreateTag().putString("BlueprintText", text);
                }
            }
        });
        return true;
    }
}
