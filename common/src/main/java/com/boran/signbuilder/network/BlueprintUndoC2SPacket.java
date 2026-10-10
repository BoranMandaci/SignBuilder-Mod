package com.boran.signbuilder.network;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class BlueprintUndoC2SPacket implements CustomPacketPayload {
    public static final Type<BlueprintUndoC2SPacket> TYPE = new Type<>(com.boran.signbuilder.network.ModMessages.BLUEPRINT_UNDO);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintUndoC2SPacket> STREAM_CODEC = StreamCodec.ofMember(BlueprintUndoC2SPacket::toBytes, BlueprintUndoC2SPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public BlueprintUndoC2SPacket() {}

    public BlueprintUndoC2SPacket(RegistryFriendlyByteBuf buf) {}

    public void toBytes(RegistryFriendlyByteBuf buf) {}

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        InteractionHand hand = InteractionHand.MAIN_HAND;

        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (tag.isEmpty() || !tag.contains("UndoHistory")) {
            stack = player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
            tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        }

        if (!tag.isEmpty() && tag.contains("UndoHistory")) {
            long[] history = tag.getLongArray("UndoHistory");
            Level level = player.level();

            List<ItemStack> itemsToRefund = new ArrayList<>();
            List<BlockPos> positionsToClear = new ArrayList<>();

            for (long posLong : history) {
                BlockPos pos = BlockPos.of(posLong);
                BlockState state = level.getBlockState(pos);

                ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (blockKey != null && blockKey.getNamespace().equals("signbuilder")) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
                        gridBe.setSuppressDrops(true);
                        if (!player.isCreative()) {
                            for (com.boran.signbuilder.block.entity.GridSignBlockEntity.CellData cell : gridBe.getCells().values()) {
                                net.minecraft.world.level.block.Block letterBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("signbuilder", cell.character));
                                if (letterBlock != null && letterBlock != Blocks.AIR) {
                                    itemsToRefund.add(new ItemStack(letterBlock.asItem()));
                                }
                            }
                            if (gridBe.hasBackplate()) {
                                itemsToRefund.add(new ItemStack(ModBlocks.BACKPLATE_ITEM.get()));
                            }
                            if (gridBe.isActive() || gridBe.getWrenchMode() != 0) {
                                itemsToRefund.add(new ItemStack(net.minecraft.world.item.Items.GLOWSTONE_DUST));
                            }
                        }
                    } else {
                        if (!player.isCreative()) {
                            itemsToRefund.add(new ItemStack(state.getBlock().asItem()));
                            if (be instanceof LetterBlockEntity letterBe) {
                                if (letterBe.hasBackplate()) {
                                    itemsToRefund.add(new ItemStack(ModBlocks.BACKPLATE_ITEM.get()));
                                }
                            }
                        }
                    }

                    positionsToClear.add(pos);
                }
            }

            int undoneCount = positionsToClear.size();

            if (undoneCount > 0) {
                for (BlockPos pos : positionsToClear) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
                        gridBe.setSuppressDrops(true);
                    }
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                if (!player.isCreative()) {
                    List<ItemStack> consolidated = consolidateItemStacks(itemsToRefund);
                    for (ItemStack drop : consolidated) {
                        if (!player.getInventory().add(drop)) {
                            player.drop(drop, false);
                        }
                    }

                    final InteractionHand finalHand = hand;
                    stack.hurtAndBreak(1, player, finalHand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                }

                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.remove("UndoHistory"));

                if (player.getServer() != null) {
                    net.minecraft.advancements.AdvancementHolder adv = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath("signbuilder", "ctrl_z"));
                    if (adv != null) {
                        player.getAdvancements().award(adv, "undo_used");
                    }
                }

                player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.undo_success").withStyle(net.minecraft.ChatFormatting.GREEN), true);
                level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
            } else {
                player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.undo_fail").withStyle(net.minecraft.ChatFormatting.RED), true);
            }
        }
    }

    private static List<ItemStack> consolidateItemStacks(List<ItemStack> raw) {
        List<ItemStack> consolidated = new ArrayList<>();
        for (ItemStack stack : raw) {
            if (stack == null || stack.isEmpty()) continue;
            ItemStack remaining = stack.copy();
            for (ItemStack existing : consolidated) {
                if (existing.getItem() == remaining.getItem() && ItemStack.isSameItemSameComponents(existing, remaining) && existing.getCount() < existing.getMaxStackSize()) {
                    int toAdd = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(toAdd);
                    remaining.shrink(toAdd);
                    if (remaining.isEmpty()) break;
                }
            }
            if (!remaining.isEmpty()) {
                consolidated.add(remaining);
            }
        }
        return consolidated;
    }
}