package com.boran.signbuilder.item;

import com.boran.signbuilder.block.LcdScreenBlock;
import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;

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
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return result;
        CompoundTag tag = data.copyTag();
        if (tag.contains("StoragePositions", Tag.TAG_LONG_ARRAY)) {
            long[] arr = tag.getLongArray("StoragePositions");
            for (long l : arr) {
                BlockPos p = BlockPos.of(l);
                if (!result.contains(p)) {
                    result.add(p);
                }
            }
        } else if (tag.contains("StoragePos")) {
            result.add(BlockPos.of(tag.getLong("StoragePos")));
        }
        return result;
    }

    public static void saveLinkedPositions(ItemStack stack, List<BlockPos> positions, String lastType) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove("StoragePos");
            if (positions == null || positions.isEmpty()) {
                tag.remove("StoragePositions");
                tag.remove("StorageType");
            } else {
                long[] arr = new long[positions.size()];
                for (int i = 0; i < positions.size(); i++) {
                    arr[i] = positions.get(i).asLong();
                }
                tag.putLongArray("StoragePositions", arr);
                if (lastType != null && !lastType.isEmpty()) {
                    tag.putString("StorageType", lastType);
                }
            }
        });
    }

    public static String getLinkedType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";
        CompoundTag tag = data.copyTag();
        return tag.getString("StorageType");
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
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container) {
                isContainer = true;
            }
        }

        if (isContainer) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            List<BlockPos> positions = getLinkedPositions(stack);
            int existingIdx = positions.indexOf(pos);
            if (existingIdx < 0 && partnerPos != null) {
                existingIdx = positions.indexOf(partnerPos);
            }

            if (existingIdx >= 0) {
                positions.remove(existingIdx);
                saveLinkedPositions(stack, positions, containerName);
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.removed_storage",
                                    positions.size(), MAX_TARGETS, containerName, pos.getX(), pos.getY(), pos.getZ())
                                    .withStyle(ChatFormatting.YELLOW),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.9f, 0.9f);
                }
                return InteractionResult.SUCCESS;
            }

            if (positions.size() >= MAX_TARGETS) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.signbuilder.storage_linker.max_reached", MAX_TARGETS)
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                return InteractionResult.FAIL;
            }

            positions.add(pos.immutable());
            saveLinkedPositions(stack, positions, containerName);

            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.signbuilder.storage_linker.added_storage",
                                positions.size(), MAX_TARGETS, containerName, pos.getX(), pos.getY(), pos.getZ())
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
                float pitch = Math.min(1.8f, 1.15f + positions.size() * 0.025f);
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
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        List<BlockPos> positions = getLinkedPositions(stack);
        if (!positions.isEmpty()) {
            if (positions.size() == 1) {
                BlockPos pos = positions.get(0);
                String type = getLinkedType(stack);
                tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.target",
                        type.isEmpty() ? "Storage" : type, pos.getX(), pos.getY(), pos.getZ())
                        .withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.multi_count",
                        positions.size(), MAX_TARGETS)
                        .withStyle(ChatFormatting.GOLD));
                int show = Math.min(4, positions.size());
                for (int i = 0; i < show; i++) {
                    BlockPos p = positions.get(i);
                    tooltip.add(Component.literal(" • [" + p.getX() + ", " + p.getY() + ", " + p.getZ() + "]")
                            .withStyle(ChatFormatting.GRAY));
                }
                if (positions.size() > show) {
                    tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.more", positions.size() - show)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.instruction_link_lcd")
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.instruction_reset")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.instruction_select")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.signbuilder.storage_linker.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
