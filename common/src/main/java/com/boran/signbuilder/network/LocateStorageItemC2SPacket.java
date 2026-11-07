package com.boran.signbuilder.network;

import com.boran.signbuilder.block.LcdScreenBlock;
import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class LocateStorageItemC2SPacket implements CustomPacketPayload {
    public static final Type<LocateStorageItemC2SPacket> TYPE = new Type<>(ModMessages.LOCATE_STORAGE_ITEM);
    public static final StreamCodec<RegistryFriendlyByteBuf, LocateStorageItemC2SPacket> STREAM_CODEC =
            StreamCodec.ofMember(LocateStorageItemC2SPacket::toBytes, LocateStorageItemC2SPacket::new);

    private static final int HIGHLIGHT_DURATION_SECONDS = 30;

    private final BlockPos pos;
    private final ItemStack targetStack;

    public LocateStorageItemC2SPacket(BlockPos pos, ItemStack targetStack) {
        this.pos = pos;
        this.targetStack = LcdScreenBlockEntity.cleanWatchlistCopy(targetStack);
    }

    public LocateStorageItemC2SPacket(RegistryFriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.targetStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.targetStack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null || this.targetStack.isEmpty()) return;
        Level level = player.level();
        if (!level.isLoaded(this.pos)) return;
        if (player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) > 64.0 * 64.0) {
            return;
        }

        BlockState state = level.getBlockState(this.pos);
        if (!(state.getBlock() instanceof LcdScreenBlock)) return;

        BlockPos masterPos = LcdScreenBlock.getMasterPos(level, this.pos, state);
        if (!(level.getBlockEntity(masterPos) instanceof LcdScreenBlockEntity masterBe)) return;

        List<LcdScreenBlockEntity.LocatedContainer> results = masterBe.locateItemInContainers(level, this.targetStack);
        String itemName = this.targetStack.getHoverName().getString();

        if (results.isEmpty()) {
            ModMessages.sendToPlayer(player, new HighlightChestsS2CPacket(ItemStack.EMPTY, List.of(), List.of(), new int[0], 0));
            player.displayClientMessage(
                    Component.translatable("message.signbuilder.storage_linker.locate_none", itemName)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.7f, 1.1f);
            return;
        }

        int size = Math.min(64, results.size());
        List<BlockPos> minPositions = new ArrayList<>(size);
        List<BlockPos> maxPositions = new ArrayList<>(size);
        int[] counts = new int[size];
        int totalFound = 0;

        for (int i = 0; i < size; i++) {
            LcdScreenBlockEntity.LocatedContainer lc = results.get(i);
            minPositions.add(lc.minPos());
            maxPositions.add(lc.maxPos());
            counts[i] = lc.itemCount();
            totalFound += lc.itemCount();
        }

        ModMessages.sendToPlayer(player, new HighlightChestsS2CPacket(
                this.targetStack, minPositions, maxPositions, counts, HIGHLIGHT_DURATION_SECONDS
        ));

        player.displayClientMessage(
                Component.translatable(
                        "message.signbuilder.storage_linker.locate_found",
                        itemName,
                        totalFound,
                        size,
                        HIGHLIGHT_DURATION_SECONDS
                ).withStyle(ChatFormatting.AQUA),
                true
        );
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.25f);
    }
}
