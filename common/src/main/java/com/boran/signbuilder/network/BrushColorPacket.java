package com.boran.signbuilder.network;

import com.boran.signbuilder.item.PaintBrushItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BrushColorPacket {
    private final int color;
    private final boolean isAddingCustom;
    private final boolean isRemovingCustom;
    private final String materialTexture;

    public BrushColorPacket(int color) {
        this.color = color;
        this.isAddingCustom = false;
        this.isRemovingCustom = false;
        this.materialTexture = "";
    }

    public BrushColorPacket(int color, boolean isAddingCustom) {
        this.color = color;
        this.isAddingCustom = isAddingCustom;
        this.isRemovingCustom = false;
        this.materialTexture = "";
    }

    public BrushColorPacket(int color, boolean isAddingCustom, boolean isRemovingCustom) {
        this.color = color;
        this.isAddingCustom = isAddingCustom;
        this.isRemovingCustom = isRemovingCustom;
        this.materialTexture = "";
    }

    public BrushColorPacket(String materialTexture) {
        this.color = 0;
        this.isAddingCustom = false;
        this.isRemovingCustom = false;
        this.materialTexture = materialTexture;
    }

    public BrushColorPacket(FriendlyByteBuf buf) {
        this.color = buf.readInt();
        this.isAddingCustom = buf.readBoolean();
        this.isRemovingCustom = buf.readBoolean();
        this.materialTexture = buf.readUtf(256);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(color);
        buf.writeBoolean(isAddingCustom);
        buf.writeBoolean(isRemovingCustom);
        buf.writeUtf(materialTexture != null ? materialTexture : "");
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof PaintBrushItem) {
                if (!materialTexture.isEmpty()) {
                    net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.putString("SelectedMaterial", materialTexture));
                    return;
                }

                if (isRemovingCustom) {
                    CompoundTag tagData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                    if (tagData.contains("CustomColors")) {
                        int[] oldColors = tagData.getIntArray("CustomColors");
                        List<Integer> colorList = new ArrayList<>();
                        boolean removed = false;
                        for (int c : oldColors) {
                            if (!removed && c == color) { removed = true; continue; }
                            colorList.add(c);
                        }
                        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.putIntArray("CustomColors", colorList.stream().mapToInt(i -> i).toArray()));
                    }
                } else if (isAddingCustom) {
                    CompoundTag tagData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
                    int[] oldColors = tagData.contains("CustomColors") ? tagData.getIntArray("CustomColors") : new int[0];
                    List<Integer> colorList = new ArrayList<>();
                    for (int c : oldColors) { if (c != color) colorList.add(c); }
                    colorList.add(0, color);
                    while (colorList.size() > 14) { colorList.remove(colorList.size() - 1); }
                    net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                        t.putInt("SelectedColor", color);
                        t.putIntArray("CustomColors", colorList.stream().mapToInt(i -> i).toArray());
                    });
                } else {
                    net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.putInt("SelectedColor", color));
                }
            }
        }
    }
}