package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HighlightChestsS2CPacket {
    private final ItemStack targetStack;
    private final List<BlockPos> minPositions;
    private final List<BlockPos> maxPositions;
    private final int[] counts;
    private final int durationSeconds;

    public HighlightChestsS2CPacket(ItemStack targetStack, List<BlockPos> minPositions, List<BlockPos> maxPositions, int[] counts, int durationSeconds) {
        this.targetStack = (targetStack != null) ? targetStack.copy() : ItemStack.EMPTY;
        this.minPositions = (minPositions != null) ? minPositions : List.of();
        this.maxPositions = (maxPositions != null) ? maxPositions : List.of();
        this.counts = (counts != null) ? counts : new int[0];
        this.durationSeconds = durationSeconds;
    }

    public HighlightChestsS2CPacket(FriendlyByteBuf buf) {
        this.targetStack = buf.readItem();
        int size = Math.min(64, Math.max(0, buf.readVarInt()));
        this.minPositions = new ArrayList<>(size);
        this.maxPositions = new ArrayList<>(size);
        this.counts = new int[size];
        for (int i = 0; i < size; i++) {
            this.minPositions.add(buf.readBlockPos());
            this.maxPositions.add(buf.readBlockPos());
            this.counts[i] = buf.readVarInt();
        }
        this.durationSeconds = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(this.targetStack);
        int size = Math.min(64, Math.min(this.minPositions.size(), Math.min(this.maxPositions.size(), this.counts.length)));
        buf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            buf.writeBlockPos(this.minPositions.get(i));
            buf.writeBlockPos(this.maxPositions.get(i));
            buf.writeVarInt(this.counts[i]);
        }
        buf.writeVarInt(this.durationSeconds);
    }

    public void handle(NetworkManager.PacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () ->
                com.boran.signbuilder.client.ClientHooks.applyStorageHighlights(
                        this.targetStack, this.minPositions, this.maxPositions, this.counts, this.durationSeconds
                )
        );
    }
}
