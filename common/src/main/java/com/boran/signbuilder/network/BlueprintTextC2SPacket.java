package com.boran.signbuilder.network;

import com.boran.signbuilder.item.SignBlueprintItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class BlueprintTextC2SPacket {
    private final String text;
    private final int size;
    private final boolean is2x2;
    private final boolean isVertical;
    private final boolean withBackplate;

    public BlueprintTextC2SPacket(String text, int size, boolean isVertical, boolean withBackplate) {
        this.text = text;
        this.size = size;
        this.is2x2 = (size == 2);
        this.isVertical = isVertical;
        this.withBackplate = withBackplate;
    }

    public BlueprintTextC2SPacket(String text, boolean is2x2, boolean isVertical, boolean withBackplate) {
        this(text, is2x2 ? 2 : 1, isVertical, withBackplate);
    }

    public BlueprintTextC2SPacket(String text, boolean is2x2, boolean isVertical) {
        this(text, is2x2 ? 2 : 1, isVertical, false);
    }

    public BlueprintTextC2SPacket(String text, boolean is2x2) {
        this(text, is2x2 ? 2 : 1, false, false);
    }

    public BlueprintTextC2SPacket(String text) {
        this(text, 1, false, false);
    }

    public BlueprintTextC2SPacket(FriendlyByteBuf buf) {
        this.text = buf.readUtf();
        this.size = buf.readInt();
        this.is2x2 = (this.size == 2);
        this.isVertical = buf.readBoolean();
        this.withBackplate = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.text);
        buf.writeInt(this.size);
        buf.writeBoolean(this.isVertical);
        buf.writeBoolean(this.withBackplate);
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof SignBlueprintItem)) {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
            }

            if (stack.getItem() instanceof SignBlueprintItem) {
                stack.getOrCreateTag().putString("BlueprintText", this.text);
                stack.getOrCreateTag().putInt("Size", this.size);
                stack.getOrCreateTag().putBoolean("Is2x2", this.is2x2);
                stack.getOrCreateTag().putBoolean("IsVertical", this.isVertical);
                stack.getOrCreateTag().putBoolean("WithBackplate", this.withBackplate);
            }
        }
    }
}