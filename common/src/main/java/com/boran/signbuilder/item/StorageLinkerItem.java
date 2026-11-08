package com.boran.signbuilder.item;

import com.boran.signbuilder.block.LcdScreenBlock;
import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class StorageLinkerItem extends Item {

    public static final int MAX_TARGETS = LcdScreenBlockEntity.MAX_LINKED_STORAGES;
    public static final double MAX_RANGE = LcdScreenBlockEntity.MAX_LINK_DISTANCE;

    public StorageLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasLinkedStorage(stack) || super.isFoil(stack);
    }

    public static boolean hasLinkedStorage(ItemStack stack) {
        return !getLinkedPositions(stack).isEmpty();
    }

    public static List<BlockPos> getLinkedPositions(ItemStack stack) {
        List<BlockPos> result = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null) return result;
        if (tag.contains("StoragePositions", Tag.TAG_LONG_ARRAY)) {
            long[] arr = tag.getLongArray("StoragePositions");
            for (long val : arr) {
                result.add(BlockPos.of(val));
            }
        } else if (tag.contains("StoragePos")) {
            long val = tag.getLong("StoragePos");
            if (val != 0L) {
                result.add(BlockPos.of(val));
            }
        }
        return result;
    }

    public static String getLinkedType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("StorageType")) {
            return tag.getString("StorageType");
        }
        return "";
    }

    public static void saveLinkedPositions(ItemStack stack, List<BlockPos> positions, String type) {
        CompoundTag tag = stack.getOrCreateTag();
        if (positions == null || positions.isEmpty()) {
            tag.remove("StoragePositions");
            tag.remove("StoragePos");
            tag.remove("StorageType");
            return;
        }
        long[] arr = new long[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            arr[i] = positions.get(i).asLong();
        }
        tag.putLongArray("StoragePositions", arr);
        tag.putLong("StoragePos", arr[0]);
        if (type != null && !type.isEmpty()) {
            tag.putString("StorageType", type);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof LcdScreenBlock) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            List<BlockPos> selected = getLinkedPositions(stack);
            if (selected.isEmpty()) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.no_storage")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.FAIL;
            }

            BlockPos masterPos = LcdScreenBlock.getMasterPos(level, pos, state);
            double maxDistSq = MAX_RANGE * MAX_RANGE;
            List<BlockPos> inRange = new ArrayList<>();
            for (BlockPos sp : selected) {
                if (masterPos.distSqr(sp) <= maxDistSq) {
                    inRange.add(sp);
                }
            }

            if (inRange.isEmpty()) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.out_of_range", (int) MAX_RANGE)
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.FAIL;
            }

            BlockEntity be = level.getBlockEntity(masterPos);
            if (be instanceof LcdScreenBlockEntity lcdBe) {
                lcdBe.setLinkedStoragePositions(inRange);
                lcdBe.setVisualAnim(0);
                lcdBe.setStorageMonitorMode(1);
                lcdBe.refreshStorageStats(level);
                lcdBe.setChanged();
                lcdBe.sync();

                Direction facing = state.getValue(LcdScreenBlock.FACING);
                AttachFace face = state.getValue(LcdScreenBlock.FACE);
                Direction[] dirs = {
                        LcdScreenBlock.getScreenUpDir(facing, face),
                        LcdScreenBlock.getScreenDownDir(facing, face),
                        LcdScreenBlock.getScreenLeftDir(facing, face),
                        LcdScreenBlock.getScreenRightDir(facing, face)
                };
                Set<BlockPos> visited = new HashSet<>();
                Queue<BlockPos> queue = new ArrayDeque<>();
                queue.add(masterPos);
                visited.add(masterPos);
                while (!queue.isEmpty() && visited.size() <= 128) {
                    BlockPos cur = queue.poll();
                    if (!cur.equals(masterPos) && level.getBlockEntity(cur) instanceof LcdScreenBlockEntity childBe) {
                        childBe.copyFrom(lcdBe);
                        childBe.setChanged();
                        childBe.sync();
                    }
                    for (Direction d : dirs) {
                        if (LcdScreenBlock.isConnectedNeighbor(level, cur, d, facing, face)) {
                            BlockPos next = cur.relative(d);
                            if (visited.add(next)) {
                                queue.add(next);
                            }
                        }
                    }
                }

                if (player != null) {
                    if (inRange.size() == 1) {
                        BlockPos first = inRange.get(0);
                        player.displayClientMessage(
                                Component.translatable("message.signbuilder.storage_linker.linked_lcd",
                                        first.getX(), first.getY(), first.getZ())
                                        .withStyle(ChatFormatting.GREEN),
                                true
                        );
                    } else {
                        player.displayClientMessage(
                                Component.translatable("message.signbuilder.storage_linker.linked_lcd_multi", inRange.size())
                                        .withStyle(ChatFormatting.GREEN),
                                true
                        );
                    }
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.4f);
                }
                return InteractionResult.SUCCESS;
            }
        }

        boolean isContainer = false;
        String containerName = state.getBlock().getName().getString();
        BlockPos partnerPos = null;
        if (state.getBlock() instanceof ChestBlock) {
            isContainer = true;
            if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                partnerPos = pos.relative(ChestBlock.getConnectedDirection(state));
            }
        } else {
            BlockEntity targetBe = level.getBlockEntity(pos);
            if (targetBe instanceof Container) {
                isContainer = true;
            }
        }

        if (isContainer) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            List<BlockPos> current = new ArrayList<>(getLinkedPositions(stack));
            boolean removing = current.contains(pos) || (partnerPos != null && current.contains(partnerPos));

            if (removing) {
                current.remove(pos);
                if (partnerPos != null) current.remove(partnerPos);
                saveLinkedPositions(stack, current, containerName);
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.removed",
                                    pos.getX(), pos.getY(), pos.getZ())
                                    .withStyle(ChatFormatting.YELLOW),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.PLAYERS, 1.0f, 1.2f);
                }
                return InteractionResult.SUCCESS;
            }

            int toAdd = (partnerPos != null) ? 2 : 1;
            if (current.size() + toAdd > MAX_TARGETS) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.max_limit", MAX_TARGETS)
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.FAIL;
            }

            if (!current.contains(pos)) current.add(pos);
            if (partnerPos != null && !current.contains(partnerPos)) current.add(partnerPos);
            saveLinkedPositions(stack, current, containerName);

            if (player != null) {
                if (current.size() == 1) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.saved",
                                    containerName, pos.getX(), pos.getY(), pos.getZ())
                                    .withStyle(ChatFormatting.AQUA),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.saved_multi",
                                    containerName, current.size(), MAX_TARGETS)
                                    .withStyle(ChatFormatting.AQUA),
                            true
                    );
                }
                float pitch = 1.0f + (current.size() / (float) MAX_TARGETS) * 0.5f;
                level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, pitch);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && hasLinkedStorage(stack)) {
            if (level.isClientSide()) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            saveLinkedPositions(stack, null, null);
            player.displayClientMessage(
                    Component.translatable("message.signbuilder.storage_linker.reset")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
            level.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        List<BlockPos> positions = getLinkedPositions(stack);
        if (!positions.isEmpty()) {
            if (positions.size() == 1) {
                BlockPos pos = positions.get(0);
                String type = getLinkedType(stack);
                tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.target",
                                type.isEmpty() ? "Storage" : type, pos.getX(), pos.getY(), pos.getZ())
                        .withStyle(ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.target_multi",
                                positions.size(), MAX_TARGETS)
                        .withStyle(ChatFormatting.AQUA));
            }
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.hint_attach")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.hint_reset")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.hint_select")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
