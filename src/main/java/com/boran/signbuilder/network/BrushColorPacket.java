package com.boran.signbuilder.network;

import com.boran.signbuilder.item.PaintBrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BrushColorPacket {
    private final int color;
    private final boolean isAddingCustom;

    public BrushColorPacket(int color) {
        this.color = color;
        this.isAddingCustom = false;
    }

    public BrushColorPacket(int color, boolean isAddingCustom) {
        this.color = color;
        this.isAddingCustom = isAddingCustom;
    }

    public BrushColorPacket(FriendlyByteBuf buf) {
        this.color = buf.readInt();
        this.isAddingCustom = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(color);
        buf.writeBoolean(isAddingCustom);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof PaintBrushItem) {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("SelectedColor", color);

                    if (isAddingCustom) {
                        int[] oldColors = tag.contains("CustomColors") ? tag.getIntArray("CustomColors") : new int[0];
                        java.util.List<Integer> colorList = new java.util.ArrayList<>();

                        for (int c : oldColors) {
                            if (c != color) colorList.add(c);
                        }

                        colorList.add(0, color);

                        while (colorList.size() > 8) {
                            colorList.remove(colorList.size() - 1);
                        }

                        tag.putIntArray("CustomColors", colorList.stream().mapToInt(i -> i).toArray());
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}