package com.boran.signbuilder.network;

import com.boran.signbuilder.item.SignBlueprintItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlueprintTextC2SPacket {
    private final String text;
    private final int size;
    private final boolean is2x2;
    private final boolean isVertical;
    private final boolean withBackplate;
    private final boolean isGridMode;
    private final int gridSize;
    private final boolean isBannerMode;
    private final String gridText;
    private final List<String> gridCells;

    public BlueprintTextC2SPacket(String text, int size, boolean isVertical, boolean withBackplate, boolean isGridMode, int gridSize, boolean isBannerMode, String gridText, List<String> gridCells) {
        this.text = text;
        this.size = size;
        this.is2x2 = (size == 2);
        this.isVertical = isVertical;
        this.withBackplate = withBackplate;
        this.isGridMode = isGridMode;
        this.gridSize = gridSize;
        this.isBannerMode = isBannerMode;
        this.gridText = gridText != null ? gridText : "";
        this.gridCells = gridCells != null ? new ArrayList<>(gridCells) : Collections.emptyList();
    }

    public BlueprintTextC2SPacket(String text, int size, boolean isVertical, boolean withBackplate, boolean isGridMode, int gridSize, String gridText, List<String> gridCells) {
        this(text, size, isVertical, withBackplate, isGridMode, gridSize, true, gridText, gridCells);
    }

    public BlueprintTextC2SPacket(String text, int size, boolean isVertical, boolean withBackplate) {
        this(text, size, isVertical, withBackplate, false, 3, true, "", Collections.emptyList());
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
        this.isGridMode = buf.readBoolean();
        this.gridSize = buf.readInt();
        this.isBannerMode = buf.readBoolean();
        this.gridText = buf.readUtf();
        int count = buf.readInt();
        this.gridCells = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            this.gridCells.add(buf.readUtf());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.text);
        buf.writeInt(this.size);
        buf.writeBoolean(this.isVertical);
        buf.writeBoolean(this.withBackplate);
        buf.writeBoolean(this.isGridMode);
        buf.writeInt(this.gridSize);
        buf.writeBoolean(this.isBannerMode);
        buf.writeUtf(this.gridText);
        buf.writeInt(this.gridCells.size());
        for (String cell : this.gridCells) {
            buf.writeUtf(cell != null ? cell : "");
        }
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof SignBlueprintItem)) {
                stack = player.getItemInHand(InteractionHand.OFF_HAND);
            }

            if (stack.getItem() instanceof SignBlueprintItem) {
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("BlueprintText", this.text);
                tag.putInt("Size", this.size);
                tag.putBoolean("Is2x2", this.is2x2);
                tag.putBoolean("IsVertical", this.isVertical);
                tag.putBoolean("WithBackplate", this.withBackplate);
                tag.putBoolean("IsGridMode", this.isGridMode);
                tag.putInt("GridSize", this.gridSize);
                tag.putBoolean("IsBannerMode", this.isBannerMode);
                tag.putString("GridText", this.gridText);
                ListTag list = new ListTag();
                for (int i = 0; i < this.gridCells.size(); i++) {
                    CompoundTag cellTag = new CompoundTag();
                    cellTag.putInt("Index", i);
                    cellTag.putString("Char", this.gridCells.get(i));
                    list.add(cellTag);
                }
                tag.put("GridCells", list);
            }
        }
    }
}