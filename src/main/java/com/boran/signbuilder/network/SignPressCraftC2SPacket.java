package com.boran.signbuilder.network;

import com.boran.signbuilder.menu.SignPressMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SignPressCraftC2SPacket {
    private final String blockName;
    private final boolean isShiftDown;

    public SignPressCraftC2SPacket(String blockName, boolean isShiftDown) {
        this.blockName = blockName;
        this.isShiftDown = isShiftDown;
    }

    public SignPressCraftC2SPacket(FriendlyByteBuf buf) {
        this.blockName = buf.readUtf();
        this.isShiftDown = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.blockName);
        buf.writeBoolean(this.isShiftDown);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof SignPressMenu menu) {
                menu.setSelectedBlock(blockName, isShiftDown);
            }
        });
        return true;
    }
}