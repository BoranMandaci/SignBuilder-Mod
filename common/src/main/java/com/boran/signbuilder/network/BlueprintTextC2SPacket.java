package com.boran.signbuilder.network;

import com.boran.signbuilder.item.SignBlueprintItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class BlueprintTextC2SPacket implements CustomPacketPayload {
    public static final Type<BlueprintTextC2SPacket> TYPE = new Type<>(com.boran.signbuilder.network.ModMessages.BLUEPRINT_TEXT);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintTextC2SPacket> STREAM_CODEC = StreamCodec.ofMember(BlueprintTextC2SPacket::toBytes, BlueprintTextC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

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

    public BlueprintTextC2SPacket(RegistryFriendlyByteBuf buf) {
        this.text = buf.readUtf();
        this.size = buf.readInt();
        this.is2x2 = (this.size == 2);
        this.isVertical = buf.readBoolean();
        this.withBackplate = buf.readBoolean();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
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
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                    t.putString("BlueprintText", this.text);
                    t.putInt("Size", this.size);
                    t.putBoolean("Is2x2", this.is2x2);
                    t.putBoolean("IsVertical", this.isVertical);
                    t.putBoolean("WithBackplate", this.withBackplate);
                });
            }
        }
    }
}