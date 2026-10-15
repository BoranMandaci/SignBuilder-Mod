package com.boran.signbuilder.network;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.GridSignBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public record ApplyBackplateInkC2SPacket(
        BlockPos pos,
        Direction hitFace,
        double hitX,
        double hitY,
        double hitZ,
        boolean isMainHand
) implements CustomPacketPayload {
    public static final Type<ApplyBackplateInkC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("signbuilder", "apply_backplate_ink"));

    public static final StreamCodec<FriendlyByteBuf, ApplyBackplateInkC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                buf.writeEnum(packet.hitFace());
                buf.writeDouble(packet.hitX());
                buf.writeDouble(packet.hitY());
                buf.writeDouble(packet.hitZ());
                buf.writeBoolean(packet.isMainHand());
            },
            buf -> new ApplyBackplateInkC2SPacket(
                    buf.readBlockPos(),
                    buf.readEnum(Direction.class),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readBoolean()
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

        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack held = player.getItemInHand(hand);
        boolean isGlowInk = held.is(Items.GLOW_INK_SAC);
        boolean isRegularInk = held.is(Items.INK_SAC);
        if (!isGlowInk && !isRegularInk) return;

        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof LetterBlockEntity lbe) {
            LetterBlockEntity master = LetterBlock.findMaster(level, pos, lbe);
            if (master == null || !master.hasBackplate()) return;

            BlockPos mPos = master.getBlockPos();
            BlockState mState = level.getBlockState(mPos);
            int size = master.getSize();
            int cost = (size == 3) ? 9 : (size == 2 ? 4 : 1);

            boolean onBack;
            AttachFace face;
            if (mState.getBlock() instanceof BackplateBlock) {
                face = mState.getValue(BackplateBlock.FACE);
                int rot = master.getFacingRotation();
                onBack = BackplateBlock.isOnBackSide(player, pos, rot);
            } else {
                face = mState.hasProperty(LetterBlock.FACE) ? mState.getValue(LetterBlock.FACE) : AttachFace.WALL;
                onBack = LetterBlock.isOnBackSide(player, pos, mState, master.getFacingRotation());
            }

            if (face == AttachFace.WALL && onBack) {
                return;
            }

            if (isGlowInk) {
                boolean currentGlow = onBack ? master.isBackplateBackGlowInked() : master.isBackplateFrontGlowInked();
                if (!currentGlow) {
                    if (!player.isCreative() && held.getCount() < cost) return;
                    LetterBlock.updateAllEntitiesBackplateGlow(level, mPos, mState, size, onBack, true);
                    if (!player.isCreative()) {
                        held.shrink(cost);
                        player.containerMenu.broadcastChanges();
                    }
                    level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            } else if (isRegularInk) {
                boolean currentGlow = onBack ? master.isBackplateBackGlowInked() : master.isBackplateFrontGlowInked();
                if (currentGlow) {
                    if (!player.isCreative() && held.getCount() < cost) return;
                    LetterBlock.updateAllEntitiesBackplateGlow(level, mPos, mState, size, onBack, false);
                    if (!player.isCreative()) {
                        held.shrink(cost);
                        ItemStack refund = new ItemStack(Items.GLOW_INK_SAC, cost);
                        if (!player.getInventory().add(refund)) {
                            player.drop(refund, false);
                        }
                        player.containerMenu.broadcastChanges();
                    }
                    level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        } else if (be instanceof GridSignBlockEntity gridBe) {
            if (!gridBe.hasBackplate()) return;

            BlockHitResult hit = new BlockHitResult(new Vec3(hitX, hitY, hitZ), hitFace, pos, false);
            GridSignBlock.GridHitRay ray = GridSignBlock.calculateHitRay(level, pos, state, gridBe, player, hit);
            boolean onBack = ray.isBack();

            AttachFace face = state.hasProperty(GridSignBlock.FACE) ? state.getValue(GridSignBlock.FACE) : AttachFace.WALL;
            if (face == AttachFace.WALL && onBack) {
                return;
            }

            int cost = 1;
            if (isGlowInk) {
                boolean currentGlow = onBack ? gridBe.isBackplateBackGlowInked() : gridBe.isBackplateFrontGlowInked();
                if (!currentGlow) {
                    if (!player.isCreative() && held.getCount() < cost) return;
                    if (onBack) {
                        gridBe.setBackplateBackGlowInked(true);
                    } else {
                        gridBe.setBackplateFrontGlowInked(true);
                    }
                    gridBe.markRenderDirty();
                    gridBe.setChanged();
                    gridBe.sync();
                    level.sendBlockUpdated(pos, state, state, 3);

                    if (!player.isCreative()) {
                        held.shrink(cost);
                        player.containerMenu.broadcastChanges();
                    }
                    level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            } else if (isRegularInk) {
                boolean currentGlow = onBack ? gridBe.isBackplateBackGlowInked() : gridBe.isBackplateFrontGlowInked();
                if (currentGlow) {
                    if (!player.isCreative() && held.getCount() < cost) return;
                    if (onBack) {
                        gridBe.setBackplateBackGlowInked(false);
                    } else {
                        gridBe.setBackplateFrontGlowInked(false);
                    }
                    gridBe.markRenderDirty();
                    gridBe.setChanged();
                    gridBe.sync();
                    level.sendBlockUpdated(pos, state, state, 3);

                    if (!player.isCreative()) {
                        held.shrink(cost);
                        ItemStack refund = new ItemStack(Items.GLOW_INK_SAC, cost);
                        if (!player.getInventory().add(refund)) {
                            player.drop(refund, false);
                        }
                        player.containerMenu.broadcastChanges();
                    }
                    level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
    }
}
