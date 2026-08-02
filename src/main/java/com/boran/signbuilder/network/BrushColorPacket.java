package com.boran.signbuilder.network;

import com.boran.signbuilder.item.PaintBrushItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BrushColorPacket {
    private final int color;

    public BrushColorPacket(int color) {
        this.color = color;
    }

    public BrushColorPacket(FriendlyByteBuf buf) {
        this.color = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(color);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof PaintBrushItem) {
                    stack.getOrCreateTag().putInt("SelectedColor", color);
                }
            }
        });
        context.setPacketHandled(true);
    }
}