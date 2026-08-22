package com.boran.signbuilder.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BlueprintUndoC2SPacket {

    public BlueprintUndoC2SPacket() {}

    public BlueprintUndoC2SPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            InteractionHand hand = InteractionHand.MAIN_HAND;

            if (!stack.hasTag() || !stack.getTag().contains("UndoHistory")) {
                stack = player.getOffhandItem();
                hand = InteractionHand.OFF_HAND;
            }

            if (stack.hasTag() && stack.getTag().contains("UndoHistory")) {
                long[] history = stack.getTag().getLongArray("UndoHistory");
                Level level = player.level();
                int undoneCount = 0;

                for (long posLong : history) {
                    BlockPos pos = BlockPos.of(posLong);
                    BlockState state = level.getBlockState(pos);

                    net.minecraft.resources.ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    if (blockKey != null && blockKey.getNamespace().equals("signbuilder")) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                        if (!player.isCreative()) {
                            ItemStack drop = new ItemStack(state.getBlock().asItem());
                            if (!player.getInventory().add(drop)) {
                                player.drop(drop, false);
                            }
                        }
                        undoneCount++;
                    }
                }

                if (undoneCount > 0) {
                    stack.getTag().remove("UndoHistory");

                    if (!player.isCreative()) {
                        final InteractionHand finalHand = hand;
                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(finalHand));
                    }

                    player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.undo_success").withStyle(net.minecraft.ChatFormatting.GREEN), true);
                    level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
                } else {
                    player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.undo_fail").withStyle(net.minecraft.ChatFormatting.RED), true);
                }
            }
        });
        return true;
    }
}