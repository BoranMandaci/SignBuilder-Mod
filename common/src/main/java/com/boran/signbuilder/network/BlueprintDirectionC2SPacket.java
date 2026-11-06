package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class BlueprintDirectionC2SPacket {
    private final int direction;

    public BlueprintDirectionC2SPacket(int direction) {
        this.direction = direction;
    }

    public BlueprintDirectionC2SPacket(FriendlyByteBuf buf) {
        this.direction = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.direction);
    }

    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem)) {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
            }
            if (stack.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.putInt("PlacementDirection", this.direction);
            }
        }
    }
}
