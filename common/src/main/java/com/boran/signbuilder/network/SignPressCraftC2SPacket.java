package com.boran.signbuilder.network;

import com.boran.signbuilder.menu.SignPressMenu;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SignPressCraftC2SPacket implements CustomPacketPayload {
    public static final Type<SignPressCraftC2SPacket> TYPE = new Type<>(com.boran.signbuilder.network.ModMessages.SIGN_PRESS_CRAFT);
    public static final StreamCodec<RegistryFriendlyByteBuf, SignPressCraftC2SPacket> STREAM_CODEC = StreamCodec.ofMember(SignPressCraftC2SPacket::toBytes, SignPressCraftC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private final String blockName;
    private final boolean isShiftDown;

    public SignPressCraftC2SPacket(String blockName, boolean isShiftDown) {
        this.blockName = blockName;
        this.isShiftDown = isShiftDown;
    }

    public SignPressCraftC2SPacket(RegistryFriendlyByteBuf buf) {
        this.blockName = buf.readUtf();
        this.isShiftDown = buf.readBoolean();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(this.blockName);
        buf.writeBoolean(this.isShiftDown);
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null && player.containerMenu instanceof SignPressMenu menu) {
            menu.setSelectedBlock(blockName, isShiftDown);
        }
    }
}