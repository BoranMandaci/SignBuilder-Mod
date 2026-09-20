package com.boran.signbuilder.network;

import com.boran.signbuilder.item.SignBlueprintItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class BlueprintTextC2SPacket {
    private final String text;
    private final boolean is2x2;
    private final boolean isVertical;
    private final boolean withBackplate;

    public BlueprintTextC2SPacket(String text, boolean is2x2, boolean isVertical, boolean withBackplate) {
        this.text = text;
        this.is2x2 = is2x2;
        this.isVertical = isVertical;
        this.withBackplate = withBackplate;
    }

    public BlueprintTextC2SPacket(String text, boolean is2x2, boolean isVertical) {
        this(text, is2x2, isVertical, false);
    }

    public BlueprintTextC2SPacket(String text, boolean is2x2) {
        this(text, is2x2, false, false);
    }

    public BlueprintTextC2SPacket(String text) {
        this(text, false, false, false);
    }

    public BlueprintTextC2SPacket(FriendlyByteBuf buf) {
        this.text = buf.readUtf();
        this.is2x2 = buf.readBoolean();
        this.isVertical = buf.readBoolean();
        this.withBackplate = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.text);
        buf.writeBoolean(this.is2x2);
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
                stack.getOrCreateTag().putBoolean("Is2x2", this.is2x2);
                stack.getOrCreateTag().putBoolean("IsVertical", this.isVertical);
                stack.getOrCreateTag().putBoolean("WithBackplate", this.withBackplate);
            }
        }
    }
}