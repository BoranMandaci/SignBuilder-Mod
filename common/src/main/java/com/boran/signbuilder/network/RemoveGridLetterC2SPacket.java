package com.boran.signbuilder.network;

import com.boran.signbuilder.block.GridSignBlock;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record RemoveGridLetterC2SPacket(BlockPos pos, int cellKey) implements CustomPacketPayload {
    public static final Type<RemoveGridLetterC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("signbuilder", "remove_grid_letter"));

    public static final StreamCodec<FriendlyByteBuf, RemoveGridLetterC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                buf.writeInt(packet.cellKey());
            },
            buf -> new RemoveGridLetterC2SPacket(
                    buf.readBlockPos(),
                    buf.readInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        Level level = player.level();
        if (level == null || !level.isLoaded(pos)) return;
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) return;

        if (level.getBlockEntity(pos) instanceof GridSignBlockEntity gridBe) {
            if (cellKey == -1) {
                if (!gridBe.getBackGlyph().isEmpty()) {
                    boolean wasGlowInked = gridBe.isBackGlyphGlowInked();
                    ItemStack glyph = gridBe.getBackGlyph().copy();
                    gridBe.setBackGlyph(ItemStack.EMPTY);
                    if (!player.isCreative()) {
                        if (!player.getInventory().add(glyph)) {
                            Block.popResource(level, pos, glyph);
                        }
                        if (wasGlowInked) {
                            ItemStack ink = new ItemStack(net.minecraft.world.item.Items.GLOW_INK_SAC);
                            if (!player.getInventory().add(ink)) {
                                Block.popResource(level, pos, ink);
                            }
                        }
                        player.containerMenu.broadcastChanges();
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (gridBe.getCells().isEmpty() && !gridBe.hasBackplate() && gridBe.getFrontGlyph().isEmpty()) {
                        level.removeBlock(pos, false);
                    } else {
                        GridSignBlock.updateLightLevel(level, pos, gridBe.getBlockState(), gridBe);
                    }
                }
                return;
            }
            if (cellKey == -2) {
                if (!gridBe.getFrontGlyph().isEmpty()) {
                    boolean wasGlowInked = gridBe.isFrontGlyphGlowInked();
                    ItemStack glyph = gridBe.getFrontGlyph().copy();
                    gridBe.setFrontGlyph(ItemStack.EMPTY);
                    if (!player.isCreative()) {
                        if (!player.getInventory().add(glyph)) {
                            Block.popResource(level, pos, glyph);
                        }
                        if (wasGlowInked) {
                            ItemStack ink = new ItemStack(net.minecraft.world.item.Items.GLOW_INK_SAC);
                            if (!player.getInventory().add(ink)) {
                                Block.popResource(level, pos, ink);
                            }
                        }
                        player.containerMenu.broadcastChanges();
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (gridBe.getCells().isEmpty() && !gridBe.hasBackplate() && gridBe.getBackGlyph().isEmpty()) {
                        level.removeBlock(pos, false);
                    } else {
                        GridSignBlock.updateLightLevel(level, pos, gridBe.getBlockState(), gridBe);
                    }
                }
                return;
            }

            GridSignBlockEntity.CellData removed = gridBe.getCells().remove(cellKey);
            if (removed != null) {
                gridBe.markRenderDirty();
                gridBe.setChanged();
                gridBe.sync();

                if (!player.isCreative()) {
                    Block letterBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("signbuilder", removed.character));
                    if (letterBlock != null && letterBlock != Blocks.AIR) {
                        ItemStack letterStack = new ItemStack(letterBlock);
                        if (!player.getInventory().add(letterStack)) {
                            Block.popResource(level, pos, letterStack);
                        }
                    }
                    if (removed.material != null && removed.material != SignMaterial.DEFAULT) {
                        ItemStack matStack = BackplateBlock.getItemForMaterial(removed.material);
                        if (!matStack.isEmpty()) {
                            if (!player.getInventory().add(matStack)) {
                                Block.popResource(level, pos, matStack);
                            }
                        }
                    }
                    if (removed.glowInked) {
                        ItemStack ink = new ItemStack(net.minecraft.world.item.Items.GLOW_INK_SAC);
                        if (!player.getInventory().add(ink)) {
                            Block.popResource(level, pos, ink);
                        }
                    }
                    player.containerMenu.broadcastChanges();
                }

                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (gridBe.getCells().isEmpty() && !gridBe.hasBackplate() && gridBe.getFrontGlyph().isEmpty() && gridBe.getBackGlyph().isEmpty()) {
                    level.removeBlock(pos, false);
                } else {
                    GridSignBlock.updateLightLevel(level, pos, gridBe.getBlockState(), gridBe);
                }
            }
        }
    }
}
