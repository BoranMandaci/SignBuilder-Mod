package com.boran.signbuilder.network;

import com.boran.signbuilder.item.PaintBrushItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BrushColorPacket {
    private final int color;
    private final boolean isAddingCustom;
    private final boolean isRemovingCustom;

    public BrushColorPacket(int color) {
        this.color = color;
        this.isAddingCustom = false;
        this.isRemovingCustom = false;
    }

    public BrushColorPacket(int color, boolean isAddingCustom) {
        this.color = color;
        this.isAddingCustom = isAddingCustom;
        this.isRemovingCustom = false;
    }

    public BrushColorPacket(int color, boolean isAddingCustom, boolean isRemovingCustom) {
        this.color = color;
        this.isAddingCustom = isAddingCustom;
        this.isRemovingCustom = isRemovingCustom;
    }

    public BrushColorPacket(FriendlyByteBuf buf) {
        this.color = buf.readInt();
        this.isAddingCustom = buf.readBoolean();
        this.isRemovingCustom = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(color);
        buf.writeBoolean(isAddingCustom);
        buf.writeBoolean(isRemovingCustom);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof PaintBrushItem) {
                    CompoundTag tag = stack.getOrCreateTag();

                    if (isRemovingCustom) {
                        if (tag.contains("CustomColors")) {
                            int[] oldColors = tag.getIntArray("CustomColors");
                            List<Integer> colorList = new ArrayList<>();
                            boolean removed = false;

                            for (int c : oldColors) {
                                if (!removed && c == color) {
                                    removed = true;
                                    continue;
                                }
                                colorList.add(c);
                            }

                            tag.putIntArray("CustomColors", colorList.stream().mapToInt(i -> i).toArray());
                        }
                    } else if (isAddingCustom) {
                        tag.putInt("SelectedColor", color);
                        int[] oldColors = tag.contains("CustomColors") ? tag.getIntArray("CustomColors") : new int[0];
                        List<Integer> colorList = new ArrayList<>();

                        for (int c : oldColors) {
                            if (c != color) colorList.add(c);
                        }

                        colorList.add(0, color);

                        while (colorList.size() > 11) {
                            colorList.remove(colorList.size() - 1);
                        }

                        tag.putIntArray("CustomColors", colorList.stream().mapToInt(i -> i).toArray());
                    } else {
                        tag.putInt("SelectedColor", color);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}