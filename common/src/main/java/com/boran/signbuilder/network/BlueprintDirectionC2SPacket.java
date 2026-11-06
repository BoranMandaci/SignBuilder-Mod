package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record BlueprintDirectionC2SPacket(int direction) implements CustomPacketPayload {
    public static final Type<BlueprintDirectionC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("signbuilder", "blueprint_direction"));

    public static final StreamCodec<FriendlyByteBuf, BlueprintDirectionC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.direction()),
            buf -> new BlueprintDirectionC2SPacket(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem)) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }
        if (!(stack.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem)) return;

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("PlacementDirection", this.direction);
        });
    }
}
