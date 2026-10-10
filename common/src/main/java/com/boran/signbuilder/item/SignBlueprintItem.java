package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignRotation;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignBlueprintItem extends Item {

    private static final Map<Integer, Block> CHAR_BLOCK_CACHE = new HashMap<>(128);

    public SignBlueprintItem(Properties pProperties) {
        super(pProperties.durability(32));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        String currentText = "";
        int size = 1;
        boolean isVertical = false;
        boolean withBackplate = false;
        boolean isGridMode = false;
        int gridSize = 3;
        String gridText = "";

        CompoundTag tag = pStack.getTag();
        if (tag != null) {
            if (tag.contains("BlueprintText")) currentText = tag.getString("BlueprintText");
            if (tag.contains("Size")) size = tag.getInt("Size");
            else if (tag.getBoolean("Is2x2")) size = 2;
            if (tag.contains("IsVertical")) isVertical = tag.getBoolean("IsVertical");
            if (tag.contains("WithBackplate")) withBackplate = tag.getBoolean("WithBackplate");
            if (tag.contains("IsGridMode")) isGridMode = tag.getBoolean("IsGridMode");
            if (tag.contains("GridSize")) gridSize = tag.getInt("GridSize");
            if (tag.contains("GridText")) gridText = tag.getString("GridText");
        }

        if (isGridMode) {
            boolean isBannerMode = tag.contains("IsBannerMode") ? tag.getBoolean("IsBannerMode") : true;
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.mode.grid")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(" (" + gridSize + "x" + gridSize + ")").withStyle(ChatFormatting.GOLD)));

            boolean isVert = tag.getBoolean("IsVertical");
            Component modeComp;
            if (isBannerMode) {
                modeComp = Component.translatable(isVert ? "gui.signbuilder.blueprint.mode_banner_vert" : "gui.signbuilder.blueprint.mode_banner_horiz")
                        .withStyle(isVert ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
            } else {
                modeComp = Component.translatable("gui.signbuilder.blueprint.mode_single")
                        .withStyle(ChatFormatting.AQUA);
            }
            pTooltipComponents.add(Component.literal("Mode: ").withStyle(ChatFormatting.GRAY).append(modeComp));

            if (!gridText.isEmpty()) {
                pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.current_text")
                        .withStyle(ChatFormatting.GRAY).append(Component.literal(": "))
                        .append(Component.literal(gridText).withStyle(ChatFormatting.AQUA)));
            } else {
                pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.empty")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }

            pTooltipComponents.add(Component.literal("Backplate: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                            .withStyle(withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY)));

            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.usage")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
            return;
        }

        if (!currentText.isEmpty()) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.current_text")
                    .withStyle(ChatFormatting.GRAY).append(Component.literal(": "))
                    .append(Component.literal(currentText).withStyle(ChatFormatting.AQUA)));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.empty")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        String sizeText = (size == 3) ? "3x3 Multi-Block" : (size == 2 ? "2x2 Multi-Block" : "1x1 Normal");
        ChatFormatting sizeColor = (size == 3) ? ChatFormatting.LIGHT_PURPLE : (size == 2 ? ChatFormatting.GOLD : ChatFormatting.AQUA);
        pTooltipComponents.add(Component.literal("Size: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(sizeText).withStyle(sizeColor)));

        pTooltipComponents.add(Component.literal("Direction: ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                        .withStyle(isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN)));

        pTooltipComponents.add(Component.literal("Backplate: ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                        .withStyle(withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY)));

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.usage")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide()) {
                CompoundTag currentTag = stack.getTag() != null ? stack.getTag() : new CompoundTag();
                boolean isGrid = currentTag.getBoolean("IsGridMode");
                if (isGrid) {
                    int gSize = currentTag.contains("GridSize") ? currentTag.getInt("GridSize") : 3;
                    if (gSize < 2) gSize = 3;
                    int nextGridSize = (gSize == 2) ? 3 : ((gSize == 3) ? 4 : 2);
                    stack.getOrCreateTag().putInt("GridSize", nextGridSize);
                    pPlayer.displayClientMessage(
                            Component.literal("Blueprint Grid: ").withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal(nextGridSize + "x" + nextGridSize).withStyle(ChatFormatting.GOLD)),
                            true
                    );
                    pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.2F);
                    return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
                }

                int size = 1;
                if (currentTag.contains("Size")) size = currentTag.getInt("Size");
                else if (currentTag.getBoolean("Is2x2")) size = 2;

                boolean isVert = currentTag.getBoolean("IsVertical");

                int nextSize;
                boolean nextVert;

                if (size == 1 && !isVert) {
                    nextSize = 1; nextVert = true;
                } else if (size == 1) {
                    nextSize = 2; nextVert = false;
                } else if (size == 2 && !isVert) {
                    nextSize = 2; nextVert = true;
                } else if (size == 2) {
                    nextSize = 3; nextVert = false;
                } else if (size == 3 && !isVert) {
                    nextSize = 3; nextVert = true;
                } else {
                    nextSize = 1; nextVert = false;
                }

                stack.getOrCreateTag().putInt("Size", nextSize);
                stack.getOrCreateTag().putBoolean("Is2x2", nextSize == 2);
                stack.getOrCreateTag().putBoolean("IsVertical", nextVert);

                String sizeStr = (nextSize == 3) ? "3x3" : (nextSize == 2 ? "2x2" : "1x1");
                ChatFormatting color = (nextSize == 3) ? ChatFormatting.LIGHT_PURPLE : (nextSize == 2 ? ChatFormatting.GOLD : ChatFormatting.AQUA);

                pPlayer.displayClientMessage(
                        Component.literal("Blueprint: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(sizeStr).withStyle(color))
                                .append(Component.literal(" "))
                                .append(Component.translatable(nextVert ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                                        .withStyle(nextVert ? ChatFormatting.YELLOW : ChatFormatting.GREEN)),
                        true
                );
                pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.2F);
            }
        } else {
            if (pLevel.isClientSide()) {
                String currentText = "";
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("BlueprintText")) currentText = tag.getString("BlueprintText");
                String finalCurrentText = currentText;

                pPlayer.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.openBlueprintScreen(finalCurrentText));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();

        if (level.isClientSide() || player == null) return InteractionResult.SUCCESS;

        String text = "";
        int size = 1;
        boolean isVertical = false;
        boolean withBackplate = false;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("BlueprintText")) text = tag.getString("BlueprintText");
            if (tag.contains("Size")) size = tag.getInt("Size");
            else if (tag.getBoolean("Is2x2")) size = 2;
            if (tag.contains("IsVertical")) isVertical = tag.getBoolean("IsVertical");
            if (tag.contains("WithBackplate")) withBackplate = tag.getBoolean("WithBackplate");
        }

        if (tag != null && tag.getBoolean("IsGridMode")) {
            return useOnGrid(pContext, level, player, stack, tag, withBackplate);
        }

        if (text.isEmpty()) {
            player.displayClientMessage(Component.translatable("tooltip.signbuilder.blueprint.empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        Direction clickedFace = pContext.getClickedFace();
        BlockPos startPos = pContext.getClickedPos().relative(clickedFace);
        boolean wall = clickedFace.getAxis() != Direction.Axis.Y;
        int rotation = wall
                ? SignRotation.fromDirection(clickedFace)
                : size > 1
                ? SignRotation.fromDirection(player.getDirection().getCounterClockWise())
                : SignRotation.fromYaw(player.getYRot(), -2);
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);

        int stepDirY = (clickedFace == Direction.UP) ? 1 : -1;
        int blocksPlaced = 0;
        List<Long> placedPositions = new ArrayList<>();

        int effectiveIdx = 0;
        int blocksPerChar = (size == 3) ? 9 : (size == 2 ? 4 : 1);
        for (int i = 0; i < text.length(); ) {
            int c = text.codePointAt(i);
            i += Character.charCount(c);
            if (c == ' ' || c == 0xFE0F) continue;

            Block blockToPlace = getBlockForChar(c);
            if (blockToPlace == null) continue;

            if (!isVertical) {
                if (size == 1) {
                    BlockPos currentPos = offsetRight(startPos, rightX, rightZ, effectiveIdx);
                    if (!level.getBlockState(currentPos).canBeReplaced()) break;

                    if (!player.isCreative()) {
                        int bpCost = withBackplate ? blocksPerChar : 0;
                        if (countItemInInventory(player, blockToPlace.asItem()) < blocksPerChar) break;
                        if (bpCost > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < bpCost) break;

                        consumeItemFromInventory(player, blockToPlace.asItem(), blocksPerChar);
                        if (bpCost > 0) consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), bpCost);
                    }

                    placeSingleBlock(level, player, pContext, blockToPlace, currentPos, clickedFace, withBackplate, rotation, size);
                    blocksPlaced++;
                    placedPositions.add(currentPos.asLong());
                } else if (size == 2) {
                    BlockPos basePos = offsetRight(startPos, rightX, rightZ, effectiveIdx * 2);
                    BlockPos p10 = offsetRight(basePos, rightX, rightZ, 1);
                    BlockPos p01 = basePos.relative(Direction.UP, 1);
                    BlockPos p11 = offsetRight(basePos, rightX, rightZ, 1).relative(Direction.UP, 1);

                    if (!level.getBlockState(basePos).canBeReplaced() || !level.getBlockState(p10).canBeReplaced() ||
                            !level.getBlockState(p01).canBeReplaced() || !level.getBlockState(p11).canBeReplaced()) {
                        break;
                    }

                    if (!player.isCreative()) {
                        int bpCost = withBackplate ? blocksPerChar : 0;
                        if (countItemInInventory(player, blockToPlace.asItem()) < blocksPerChar) break;
                        if (bpCost > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < bpCost) break;

                        consumeItemFromInventory(player, blockToPlace.asItem(), blocksPerChar);
                        if (bpCost > 0) consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), bpCost);
                    }

                    BlockPos[] quad = {basePos, p10, p01, p11};
                    for (BlockPos p : quad) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate, rotation, size);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                } else {
                    BlockPos basePos = offsetRight(startPos, rightX, rightZ, effectiveIdx * 3);
                    boolean canPlaceAll = true;
                    BlockPos[] grid = new BlockPos[9];
                    int gIdx = 0;
                    for (int dy = 0; dy < 3; dy++) {
                        for (int dx = 0; dx < 3; dx++) {
                            BlockPos p = offsetRight(basePos, rightX, rightZ, dx).relative(Direction.UP, dy);
                            if (!level.getBlockState(p).canBeReplaced()) {
                                canPlaceAll = false;
                                break;
                            }
                            grid[gIdx++] = p;
                        }
                        if (!canPlaceAll) break;
                    }

                    if (!canPlaceAll) break;

                    if (!player.isCreative()) {
                        int bpCost = withBackplate ? blocksPerChar : 0;
                        if (countItemInInventory(player, blockToPlace.asItem()) < blocksPerChar) break;
                        if (bpCost > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < bpCost) break;

                        consumeItemFromInventory(player, blockToPlace.asItem(), blocksPerChar);
                        if (bpCost > 0) consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), bpCost);
                    }

                    for (BlockPos p : grid) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate, rotation, size);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                }
            } else {
                if (size == 1) {
                    int posY = (stepDirY == 1)
                            ? startPos.getY() + effectiveIdx
                            : startPos.getY() - effectiveIdx;

                    BlockPos currentPos = new BlockPos(startPos.getX(), posY, startPos.getZ());
                    if (!level.getBlockState(currentPos).canBeReplaced()) break;

                    if (!player.isCreative()) {
                        int bpCost = withBackplate ? blocksPerChar : 0;
                        if (countItemInInventory(player, blockToPlace.asItem()) < blocksPerChar) break;
                        if (bpCost > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < bpCost) break;

                        consumeItemFromInventory(player, blockToPlace.asItem(), blocksPerChar);
                        if (bpCost > 0) consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), bpCost);
                    }

                    placeSingleBlock(level, player, pContext, blockToPlace, currentPos, clickedFace, withBackplate, rotation, size);
                    blocksPlaced++;
                    placedPositions.add(currentPos.asLong());
                } else if (size == 2) {
                    int baseY = (stepDirY == 1)
                            ? startPos.getY() + (effectiveIdx * 2)
                            : startPos.getY() - 1 - (effectiveIdx * 2);

                    BlockPos basePos = new BlockPos(startPos.getX(), baseY, startPos.getZ());
                    BlockPos p10 = offsetRight(basePos, rightX, rightZ, 1);
                    BlockPos p01 = basePos.relative(Direction.UP, 1);
                    BlockPos p11 = offsetRight(basePos, rightX, rightZ, 1).relative(Direction.UP, 1);

                    if (!level.getBlockState(basePos).canBeReplaced() || !level.getBlockState(p10).canBeReplaced() ||
                            !level.getBlockState(p01).canBeReplaced() || !level.getBlockState(p11).canBeReplaced()) {
                        break;
                    }

                    if (!player.isCreative()) {
                        int bpCost = withBackplate ? blocksPerChar : 0;
                        if (countItemInInventory(player, blockToPlace.asItem()) < blocksPerChar) break;
                        if (bpCost > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < bpCost) break;

                        consumeItemFromInventory(player, blockToPlace.asItem(), blocksPerChar);
                        if (bpCost > 0) consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), bpCost);
                    }

                    BlockPos[] quad = {basePos, p10, p01, p11};
                    for (BlockPos p : quad) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate, rotation, size);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                } else {
                    int baseY = (stepDirY == 1)
                            ? startPos.getY() + (effectiveIdx * 3)
                            : startPos.getY() - 2 - (effectiveIdx * 3);

                    BlockPos basePos = new BlockPos(startPos.getX(), baseY, startPos.getZ());
                    boolean canPlaceAll = true;
                    BlockPos[] grid = new BlockPos[9];
                    int gIdx = 0;
                    for (int dy = 0; dy < 3; dy++) {
                        for (int dx = 0; dx < 3; dx++) {
                            BlockPos p = offsetRight(basePos, rightX, rightZ, dx).relative(Direction.UP, dy);
                            if (!level.getBlockState(p).canBeReplaced()) {
                                canPlaceAll = false;
                                break;
                            }
                            grid[gIdx++] = p;
                        }
                        if (!canPlaceAll) break;
                    }

                    if (!canPlaceAll) break;

                    if (!player.isCreative()) {
                        int bpCost = withBackplate ? blocksPerChar : 0;
                        if (countItemInInventory(player, blockToPlace.asItem()) < blocksPerChar) break;
                        if (bpCost > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < bpCost) break;

                        consumeItemFromInventory(player, blockToPlace.asItem(), blocksPerChar);
                        if (bpCost > 0) consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), bpCost);
                    }

                    for (BlockPos p : grid) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate, rotation, size);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                }
            }

            effectiveIdx++;
        }

        if (blocksPlaced > 0) {
            level.playSound(null, startPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

            long[] posArray = placedPositions.stream().mapToLong(l -> l).toArray();
            stack.getOrCreateTag().putLongArray("UndoHistory", posArray);

            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void placeSingleBlock(Level level, Player player, UseOnContext pContext, Block blockToPlace, BlockPos pos, Direction clickedFace, boolean withBackplate, int rotation, int size) {
        BlockHitResult hitResult = new BlockHitResult(pContext.getClickLocation(), clickedFace, pos, pContext.isInside());
        UseOnContext offsetContext = new UseOnContext(player, pContext.getHand(), hitResult);
        BlockPlaceContext placeContext = new BlockPlaceContext(offsetContext);
        BlockState stateToPlace = blockToPlace.getStateForPlacement(placeContext);
        if (stateToPlace == null) stateToPlace = blockToPlace.defaultBlockState();

        level.setBlock(pos, stateToPlace, 3);
        ItemStack placedStack = new ItemStack(blockToPlace);
        if (size > 1) {
            CompoundTag blockEntityTag = placedStack.getOrCreateTagElement("BlockEntityTag");
            blockEntityTag.putInt("FacingRotation", rotation);
            blockEntityTag.putString("id", "signbuilder:letter_block_entity");
        }
        blockToPlace.setPlacedBy(level, pos, stateToPlace, player, placedStack);

        if (withBackplate) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity letterBe) {
                letterBe.setHasBackplate(true);
            }
        }
    }

    private static BlockPos offsetRight(BlockPos pos, int rightX, int rightZ, int distance) {
        return pos.offset(rightX * distance, 0, rightZ * distance);
    }

    private int countItemInInventory(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private void consumeItemFromInventory(Player player, Item item, int amount) {
        int amountLeftToRemove = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                if (stack.getCount() >= amountLeftToRemove) {
                    stack.shrink(amountLeftToRemove);
                    break;
                } else {
                    amountLeftToRemove -= stack.getCount();
                    stack.setCount(0);
                }
            }
        }
    }

    public static int getHitSubCellRow(Vec3 hitLocation, int gridSize) {
        double fy = hitLocation.y - Math.floor(hitLocation.y);
        double v = 1.0 - fy;
        return Mth.clamp((int) Math.floor(v * gridSize), 0, gridSize - 1);
    }

    public static int getHitSubCellCol(Direction face, Vec3 hitLocation, int gridSize) {
        boolean wall = face.getAxis() != Direction.Axis.Y;
        int stepRotation = wall ? SignRotation.fromDirection(face) : 0;
        int rightX = SignRotation.horizontalStepX(stepRotation, wall);
        int rightZ = SignRotation.horizontalStepZ(stepRotation, wall);
        double u;
        if (rightX != 0) {
            double fx = hitLocation.x - Math.floor(hitLocation.x);
            u = (rightX > 0) ? fx : (1.0 - fx);
        } else {
            double fz = hitLocation.z - Math.floor(hitLocation.z);
            u = (rightZ > 0) ? fz : (1.0 - fz);
        }
        return Mth.clamp((int) Math.floor(u * gridSize), 0, gridSize - 1);
    }

    public static Map<Integer, String> getBannerCellsForBlock(String text, int blockIdx, int gridSize) {
        int targetRow = (gridSize == 2) ? 0 : 1;
        return getBannerCellsForBlock(text, blockIdx, gridSize, false, targetRow);
    }

    public static Map<Integer, String> getBannerCellsForBlock(String text, int blockIdx, int gridSize, boolean isVertical, int targetLine) {
        Map<Integer, String> cells = new HashMap<>();
        int charsPerBlock = gridSize;
        int startChar = blockIdx * charsPerBlock;
        if (startChar >= text.length()) return cells;

        int endChar = Math.min(startChar + charsPerBlock, text.length());
        String sub = text.substring(startChar, endChar);

        for (int i = 0; i < sub.length(); i++) {
            char ch = sub.charAt(i);
            if (ch != ' ') {
                int cp = (ch != 'ß') ? Character.toUpperCase(ch) : ch;
                String path = getBlockPathForChar(cp);
                if (path != null) {
                    int cellIndex = isVertical
                            ? (i * gridSize + targetLine)
                            : (targetLine * gridSize + i);
                    cells.put(cellIndex, path);
                }
            }
        }
        return cells;
    }

    private void stampOrPlaceGridSign(Level level, Player player, BlockPos targetPos, Direction clickedFace, int gridSize, Map<Integer, String> cells, boolean withBackplate, boolean isBannerMode, boolean isVertical, int targetLine) {
        BlockState currentState = level.getBlockState(targetPos);
        boolean isBackplate = currentState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
        boolean isLetterWithBackplate = false;
        if (currentState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
            if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                com.boran.signbuilder.block.entity.LetterBlockEntity eff = lbe;
                if (lbe.isDummy()) {
                    BlockEntity me = level.getBlockEntity(lbe.getMasterPos());
                    if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity masterBe) eff = masterBe;
                }
                isLetterWithBackplate = eff.hasBackplate();
            }
        }
        boolean isGridSign = currentState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock;

        if (isGridSign) {
            if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
                gridBe.setGridSize(gridSize);
                boolean isBack = false;
                AttachFace aFace = currentState.hasProperty(com.boran.signbuilder.block.GridSignBlock.FACE) ? currentState.getValue(com.boran.signbuilder.block.GridSignBlock.FACE) : AttachFace.WALL;
                if (aFace != AttachFace.WALL) {
                    int rot = gridBe.getFacingRotation();
                    double side = (player.getX() - (targetPos.getX() + 0.5)) * SignRotation.facingX(rot)
                            + (player.getZ() - (targetPos.getZ() + 0.5)) * SignRotation.facingZ(rot);
                    if (side < 0.0) isBack = true;
                }
                int offsetIdx = isBack ? 16 : 0;
                if (isBannerMode) {
                    if (isVertical) {
                        for (int r = 0; r < gridSize; r++) {
                            gridBe.getCells().remove(offsetIdx + r * gridSize + targetLine);
                        }
                    } else {
                        for (int c = 0; c < gridSize; c++) {
                            gridBe.getCells().remove(offsetIdx + targetLine * gridSize + c);
                        }
                    }
                } else {
                    for (int i = 0; i < 16; i++) {
                        gridBe.getCells().remove(offsetIdx + i);
                    }
                }
                for (Map.Entry<Integer, String> entry : cells.entrySet()) {
                    int cIdx = entry.getKey();
                    if (cIdx >= 0 && cIdx < gridSize * gridSize) {
                        gridBe.setCell(offsetIdx + cIdx, entry.getValue(), com.boran.signbuilder.block.SignMaterial.DEFAULT, 0xFFFFFF, false, 0);
                    }
                }
                gridBe.markRenderDirty();
                gridBe.setChanged();
                gridBe.sync();
            }
        } else if (isBackplate || isLetterWithBackplate) {
            com.boran.signbuilder.block.SignMaterial bpFMat = com.boran.signbuilder.block.SignMaterial.DEFAULT;
            com.boran.signbuilder.block.SignMaterial bpBMat = com.boran.signbuilder.block.SignMaterial.DEFAULT;
            int bpFColor = 0xFFFFFF, bpBColor = 0xFFFFFF;
            boolean bpFRainbow = false, bpBRainbow = false;
            int bpFacingRotation = 0;
            int bpWrenchMode = 0;
            boolean bpActive = false;
            boolean bpDetectsMonsters = true, bpDetectsAnimals = false;
            int bpOnTicks = 10, bpOffTicks = 10, bpType = 0, bpRange = 8, bpOffRange = 8, bpCloseDelay = 0;
            boolean bpNightOnly = true, bpPlayers = true, bpLowPower = false, bpLookOnly = true;

            if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                com.boran.signbuilder.block.entity.LetterBlockEntity eff = lbe;
                if (lbe.isDummy()) {
                    BlockEntity me = level.getBlockEntity(lbe.getMasterPos());
                    if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity mBe) eff = mBe;
                }
                bpFMat = eff.getBackplateFrontMaterial();
                bpBMat = eff.getBackplateBackMaterial();
                bpFColor = eff.getBackplateFrontColor();
                bpBColor = eff.getBackplateBackColor();
                bpFRainbow = eff.isBackplateFrontRainbow();
                bpBRainbow = eff.isBackplateBackRainbow();
                bpFacingRotation = eff.getFacingRotation();
                bpWrenchMode = eff.getWrenchMode();
                bpActive = eff.isActive();
                bpDetectsMonsters = eff.doesDetectMonsters();
                bpDetectsAnimals = eff.doesDetectAnimals();
                bpOnTicks = eff.getCustomLightOnTicks();
                bpOffTicks = eff.getCustomLightOffTicks();
                bpType = eff.getCustomLightType();
                bpRange = eff.getCustomLightRange();
                bpOffRange = eff.getCustomLightOffRange();
                bpCloseDelay = eff.getCustomLightCloseDelayTicks();
                bpNightOnly = eff.isCustomLightNightOnly();
                bpPlayers = eff.doesCustomLightDetectPlayers();
                bpLowPower = eff.isCustomLightLowPower();
                bpLookOnly = eff.isCustomLightLookOnly();
            } else if (currentState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                bpFacingRotation = SignRotation.fromDirection(currentState.getValue(BlockStateProperties.HORIZONTAL_FACING));
            }

            AttachFace attachFace = AttachFace.WALL;
            if (currentState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                attachFace = currentState.getValue(BlockStateProperties.ATTACH_FACE);
            }
            Direction blockFacing = clickedFace;
            if (currentState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                blockFacing = currentState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }

            BlockState newState = com.boran.signbuilder.block.ModBlocks.GRID_SIGN.get().defaultBlockState()
                    .setValue(com.boran.signbuilder.block.GridSignBlock.FACE, attachFace)
                    .setValue(com.boran.signbuilder.block.GridSignBlock.FACING, blockFacing);
            level.setBlock(targetPos, newState, 3);

            if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
                gridBe.setHasBackplate(true);
                gridBe.setBackplateFrontMaterial(bpFMat);
                gridBe.setBackplateBackMaterial(bpBMat);
                gridBe.setBackplateFrontColor(bpFColor);
                gridBe.setBackplateBackColor(bpBColor);
                gridBe.setBackplateFrontRainbow(bpFRainbow);
                gridBe.setBackplateBackRainbow(bpBRainbow);
                gridBe.setFacingRotation(bpFacingRotation);
                gridBe.setLightConfiguration(bpWrenchMode, bpActive, bpDetectsMonsters, bpDetectsAnimals, bpOnTicks, bpOffTicks, bpType, bpRange, bpOffRange, bpCloseDelay, bpNightOnly, bpPlayers, bpLowPower, bpLookOnly);
                com.boran.signbuilder.block.GridSignBlock.updateLightLevel(level, targetPos, newState, gridBe);

                gridBe.setGridSize(gridSize);
                for (Map.Entry<Integer, String> entry : cells.entrySet()) {
                    int cIdx = entry.getKey();
                    if (cIdx >= 0 && cIdx < gridSize * gridSize) {
                        gridBe.setCell(cIdx, entry.getValue(), com.boran.signbuilder.block.SignMaterial.DEFAULT, 0xFFFFFF, false, 0);
                    }
                }
                gridBe.markRenderDirty();
                gridBe.setChanged();
                gridBe.sync();
            }
        } else {
            AttachFace attachFace;
            Direction blockFacing;
            if (clickedFace.getAxis() != Direction.Axis.Y) {
                attachFace = AttachFace.WALL;
                blockFacing = clickedFace;
            } else if (clickedFace == Direction.UP) {
                attachFace = AttachFace.FLOOR;
                blockFacing = player.getDirection().getOpposite();
            } else {
                attachFace = AttachFace.CEILING;
                blockFacing = player.getDirection().getOpposite();
            }

            BlockState newState = com.boran.signbuilder.block.ModBlocks.GRID_SIGN.get().defaultBlockState()
                    .setValue(com.boran.signbuilder.block.GridSignBlock.FACE, attachFace)
                    .setValue(com.boran.signbuilder.block.GridSignBlock.FACING, blockFacing);
            level.setBlock(targetPos, newState, 3);

            if (level.getBlockEntity(targetPos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
                gridBe.setGridSize(gridSize);
                int rot = SignRotation.fromDirection(blockFacing);
                gridBe.setFacingRotation(rot);
                gridBe.setHasBackplate(withBackplate);
                for (Map.Entry<Integer, String> entry : cells.entrySet()) {
                    int cIdx = entry.getKey();
                    if (cIdx >= 0 && cIdx < gridSize * gridSize) {
                        gridBe.setCell(cIdx, entry.getValue(), com.boran.signbuilder.block.SignMaterial.DEFAULT, 0xFFFFFF, false, 0);
                    }
                }
                gridBe.markRenderDirty();
                gridBe.setChanged();
                gridBe.sync();
            }
        }
    }

    private InteractionResult useOnGrid(UseOnContext pContext, Level level, Player player, ItemStack stack, CompoundTag tag, boolean withBackplate) {
        int gridSize = tag.contains("GridSize") ? tag.getInt("GridSize") : 3;
        if (gridSize < 2) gridSize = 3;
        boolean isBannerMode = tag.contains("IsBannerMode") ? tag.getBoolean("IsBannerMode") : true;
        String gridText = tag.getString("GridText");

        BlockPos clickedPos = pContext.getClickedPos();
        Direction clickedFace = pContext.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);

        boolean clickedIsStampable = (clickedState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock)
                || (clickedState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock)
                || (clickedState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock);

        if (clickedFace.getAxis() == Direction.Axis.Y && !clickedIsStampable) {
            player.displayClientMessage(Component.translatable("tooltip.signbuilder.blueprint.wall_only").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        Direction effectiveFace = clickedFace;
        if (clickedFace.getAxis() == Direction.Axis.Y && clickedIsStampable) {
            if (clickedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                effectiveFace = clickedState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
        }

        boolean wall = effectiveFace.getAxis() != Direction.Axis.Y;
        int rotation = wall
                ? SignRotation.fromDirection(effectiveFace)
                : SignRotation.fromDirection(player.getDirection().getCounterClockWise());
        int rightX = SignRotation.horizontalStepX(rotation, wall);
        int rightZ = SignRotation.horizontalStepZ(rotation, wall);

        BlockPos startPos = clickedIsStampable ? clickedPos : clickedPos.relative(clickedFace);

        boolean isVertical = tag.getBoolean("IsVertical");
        Vec3 hitLoc = pContext.getClickLocation();
        int targetLine;
        if (isVertical) {
            targetLine = getHitSubCellCol(effectiveFace, hitLoc, gridSize);
        } else {
            targetLine = getHitSubCellRow(hitLoc, gridSize);
        }

        int numBlocks;
        List<Map<Integer, String>> blocksCells = new ArrayList<>();

        if (isBannerMode && !gridText.isEmpty()) {
            int charsPerBlock = gridSize;
            numBlocks = (gridText.length() + charsPerBlock - 1) / charsPerBlock;
            for (int b = 0; b < numBlocks; b++) {
                blocksCells.add(getBannerCellsForBlock(gridText, b, gridSize, isVertical, targetLine));
            }
        } else {
            numBlocks = 1;
            Map<Integer, String> blueprintCells = new HashMap<>();
            if (tag.contains("GridCells", net.minecraft.nbt.Tag.TAG_LIST)) {
                net.minecraft.nbt.ListTag list = tag.getList("GridCells", net.minecraft.nbt.Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag ctag = list.getCompound(i);
                    int idx = ctag.getInt("Index");
                    String cPath = ctag.getString("Char");
                    if (!cPath.isEmpty()) {
                        blueprintCells.put(idx, cPath);
                    }
                }
            }
            if (blueprintCells.isEmpty() && !gridText.isEmpty()) {
                int max = Math.min(gridText.length(), gridSize * gridSize);
                for (int i = 0; i < max; i++) {
                    char ch = gridText.charAt(i);
                    if (ch != ' ') {
                        String path = getBlockPathForChar(ch);
                        if (path != null) blueprintCells.put(i, path);
                    }
                }
            }
            if (blueprintCells.isEmpty()) {
                player.displayClientMessage(Component.translatable("tooltip.signbuilder.blueprint.empty").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            blocksCells.add(blueprintCells);
        }

        List<BlockPos> targetPositions = new ArrayList<>();
        for (int b = 0; b < numBlocks; b++) {
            BlockPos bPos = (isBannerMode && isVertical)
                    ? startPos.below(b)
                    : offsetRight(startPos, rightX, rightZ, b);
            BlockState bState = level.getBlockState(bPos);
            boolean bStamp = (bState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock)
                    || (bState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock)
                    || (bState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock);
            if (!bStamp && !bState.canBeReplaced()) {
                player.displayClientMessage(Component.translatable("tooltip.signbuilder.blueprint.obstructed").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            targetPositions.add(bPos);
        }

        if (!player.isCreative()) {
            int backplatesNeeded = 0;
            Map<Item, Integer> lettersNeeded = new HashMap<>();

            for (int b = 0; b < numBlocks; b++) {
                BlockPos bPos = targetPositions.get(b);
                BlockState bState = level.getBlockState(bPos);
                boolean bStamp = (bState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock)
                        || (bState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock)
                        || (bState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock);
                if (!bStamp && withBackplate) {
                    backplatesNeeded++;
                }

                for (String charPath : blocksCells.get(b).values()) {
                    Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation("signbuilder", charPath));
                    if (block != Blocks.AIR) {
                        lettersNeeded.merge(block.asItem(), 1, Integer::sum);
                    }
                }
            }

            if (backplatesNeeded > 0 && countItemInInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get()) < backplatesNeeded) {
                player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.missing_materials").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            for (Map.Entry<Item, Integer> req : lettersNeeded.entrySet()) {
                if (countItemInInventory(player, req.getKey()) < req.getValue()) {
                    player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.missing_materials").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
            }

            if (backplatesNeeded > 0) {
                consumeItemFromInventory(player, com.boran.signbuilder.block.ModBlocks.BACKPLATE_ITEM.get(), backplatesNeeded);
            }
            for (Map.Entry<Item, Integer> req : lettersNeeded.entrySet()) {
                consumeItemFromInventory(player, req.getKey(), req.getValue());
            }
        }

        List<Long> placedPositions = new ArrayList<>();
        for (int b = 0; b < numBlocks; b++) {
            BlockPos bPos = targetPositions.get(b);
            stampOrPlaceGridSign(level, player, bPos, effectiveFace, gridSize, blocksCells.get(b), withBackplate, isBannerMode, isVertical, targetLine);
            placedPositions.add(bPos.asLong());
        }

        level.playSound(null, startPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        long[] posArray = placedPositions.stream().mapToLong(l -> l).toArray();
        stack.getOrCreateTag().putLongArray("UndoHistory", posArray);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    public static Block getBlockForChar(char c) {
        return getBlockForChar((int) c);
    }

    public static Block getBlockForChar(int c) {
        Block cached = CHAR_BLOCK_CACHE.get(c);
        if (cached != null) return cached;

        String blockId = getBlockPathForChar(c);
        if (blockId != null) {
            Block targetBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("signbuilder", blockId));
            if (targetBlock != Blocks.AIR) {
                CHAR_BLOCK_CACHE.put(c, targetBlock);
                return targetBlock;
            }
        }
        return null;
    }

    public static String getBlockPathForChar(int c) {
        return switch (c) {
            case 'A', 'a' -> "letter_a";
            case 'Ä', 'ä' -> "letter_a_de";
            case 'B', 'b' -> "letter_b";
            case 'C', 'c' -> "letter_c";
            case 'Ç', 'ç' -> "letter_c_tr";
            case 'D', 'd' -> "letter_d";
            case 'E', 'e' -> "letter_e";
            case 'F', 'f' -> "letter_f";
            case 'G', 'g' -> "letter_g";
            case 'Ğ', 'ğ' -> "letter_g_tr";
            case 'H', 'h' -> "letter_h";
            case 'I', 'i', 'ı' -> "letter_i";
            case 'İ' -> "letter_i_tr";
            case 'J', 'j' -> "letter_j";
            case 'K', 'k' -> "letter_k";
            case 'L', 'l' -> "letter_l";
            case 'M', 'm' -> "letter_m";
            case 'N', 'n' -> "letter_n";
            case 'O', 'o' -> "letter_o";
            case 'Ö', 'ö' -> "letter_o_tr";
            case 'P', 'p' -> "letter_p";
            case 'Q', 'q' -> "letter_q";
            case 'R', 'r' -> "letter_r";
            case 'S', 's' -> "letter_s";
            case 'Ş', 'ş' -> "letter_s_tr";
            case 'T', 't' -> "letter_t";
            case 'U', 'u' -> "letter_u";
            case 'Ü', 'ü' -> "letter_u_tr";
            case 'V', 'v' -> "letter_v";
            case 'W', 'w' -> "letter_w";
            case 'X', 'x' -> "letter_x";
            case 'Y', 'y' -> "letter_y";
            case 'Z', 'z' -> "letter_z";
            case 'ß' -> "letter_eszett";
            case '0' -> "number_0";
            case '1' -> "number_1"; case '2' -> "number_2"; case '3' -> "number_3"; case '4' -> "number_4";
            case '5' -> "number_5"; case '6' -> "number_6"; case '7' -> "number_7"; case '8' -> "number_8";
            case '9' -> "number_9";
            case '+' -> "symbol_plus"; case '-' -> "symbol_minus"; case '✗', '×' -> "symbol_cross"; case '/' -> "symbol_slash";
            case '\\' -> "symbol_backslash"; case '#' -> "symbol_hashtag";
            case '*' -> "symbol_asterisk"; case '★' -> "symbol_star";
            case '✓' -> "symbol_checkmark"; case '∞' -> "symbol_infinity";
            case '○', '●' -> "symbol_circle"; case '◆', '◇' -> "symbol_diamond";
            case '♪' -> "symbol_note"; case '♫' -> "symbol_note_double"; case '☠' -> "symbol_skull";
            case 0x1F5DD -> "symbol_key";        // 🗝
            case 0x1F512 -> "symbol_lock";       // 🔒
            case 0x1F3C6 -> "symbol_trophy";     // 🏆
            case '⚡' -> "symbol_lightning";
            case '♥' -> "symbol_heart"; case '€' -> "symbol_euro"; case '$' -> "symbol_dollar"; case '£' -> "symbol_pound";
            case '¥' -> "symbol_yen"; case '₺' -> "symbol_tl"; case '₿' -> "symbol_bitcoin"; case '@' -> "symbol_at"; case '&' -> "symbol_ampersand";
            case ',' -> "symbol_comma"; case '%' -> "symbol_percent";
            case '<' -> "symbol_less_than"; case '>' -> "symbol_greater_than"; case '~' -> "symbol_tilde";
            case '«' -> "symbol_dot_left"; case '•' -> "symbol_dot_center"; case '»' -> "symbol_dot_right";
            case '(' -> "symbol_bracket_left"; case ')' -> "symbol_bracket_right"; case '|' -> "symbol_bracket_double";
            case '[' -> "symbol_square_bracket_left"; case ']' -> "symbol_square_bracket_right"; case '¦' -> "symbol_square_bracket_double";
            case '↑' -> "arrow_up"; case '↓' -> "arrow_down"; case '←' -> "arrow_left"; case '→' -> "arrow_right";
            case '↖' -> "arrow_left_up"; case '↗' -> "arrow_right_up"; case '↙' -> "arrow_left_down"; case '↘' -> "arrow_right_down";
            case ':' -> "symbol_colon"; case ';' -> "symbol_semicolon"; case '!' -> "symbol_exclamation";
            case '?' -> "symbol_question"; case '=' -> "symbol_equals"; case '÷' -> "symbol_divide";
            case '\'' -> "symbol_apostrophe"; case '"' -> "symbol_quotes";
            default -> null;
        };
    }

    private static final Map<String, String> PATH_TO_DISPLAY = new HashMap<>(128);
    static {
        for (char c = 'A'; c <= 'Z'; c++) PATH_TO_DISPLAY.put("letter_" + Character.toLowerCase(c), String.valueOf(c));
        PATH_TO_DISPLAY.put("letter_a_de", "Ä");
        PATH_TO_DISPLAY.put("letter_eszett", "ß");
        PATH_TO_DISPLAY.put("letter_c_tr", "Ç");
        PATH_TO_DISPLAY.put("letter_g_tr", "Ğ");
        PATH_TO_DISPLAY.put("letter_i_tr", "İ");
        PATH_TO_DISPLAY.put("letter_o_tr", "Ö");
        PATH_TO_DISPLAY.put("letter_s_tr", "Ş");
        PATH_TO_DISPLAY.put("letter_u_tr", "Ü");
        for (int i = 0; i <= 9; i++) PATH_TO_DISPLAY.put("number_" + i, String.valueOf(i));
        PATH_TO_DISPLAY.put("symbol_plus", "+");
        PATH_TO_DISPLAY.put("symbol_minus", "-");
        PATH_TO_DISPLAY.put("symbol_cross", "×");
        PATH_TO_DISPLAY.put("symbol_divide", "÷");
        PATH_TO_DISPLAY.put("symbol_equals", "=");
        PATH_TO_DISPLAY.put("symbol_percent", "%");
        PATH_TO_DISPLAY.put("symbol_hashtag", "#");
        PATH_TO_DISPLAY.put("symbol_slash", "/");
        PATH_TO_DISPLAY.put("symbol_backslash", "\\");
        PATH_TO_DISPLAY.put("symbol_asterisk", "*");
        PATH_TO_DISPLAY.put("symbol_star", "★");
        PATH_TO_DISPLAY.put("symbol_heart", "♥");
        PATH_TO_DISPLAY.put("symbol_checkmark", "✓");
        PATH_TO_DISPLAY.put("symbol_infinity", "∞");
        PATH_TO_DISPLAY.put("symbol_circle", "●");
        PATH_TO_DISPLAY.put("symbol_diamond", "◆");
        PATH_TO_DISPLAY.put("symbol_note", "♪");
        PATH_TO_DISPLAY.put("symbol_note_double", "♫");
        PATH_TO_DISPLAY.put("symbol_skull", "☠");
        PATH_TO_DISPLAY.put("symbol_lightning", "⚡");
        PATH_TO_DISPLAY.put("symbol_euro", "€");
        PATH_TO_DISPLAY.put("symbol_dollar", "$");
        PATH_TO_DISPLAY.put("symbol_pound", "£");
        PATH_TO_DISPLAY.put("symbol_yen", "¥");
        PATH_TO_DISPLAY.put("symbol_tl", "₺");
        PATH_TO_DISPLAY.put("symbol_bitcoin", "₿");
        PATH_TO_DISPLAY.put("symbol_at", "@");
        PATH_TO_DISPLAY.put("symbol_ampersand", "&");
        PATH_TO_DISPLAY.put("symbol_comma", ",");
        PATH_TO_DISPLAY.put("symbol_colon", ":");
        PATH_TO_DISPLAY.put("symbol_semicolon", ";");
        PATH_TO_DISPLAY.put("symbol_exclamation", "!");
        PATH_TO_DISPLAY.put("symbol_question", "?");
        PATH_TO_DISPLAY.put("symbol_apostrophe", "'");
        PATH_TO_DISPLAY.put("symbol_quotes", "\"");
        PATH_TO_DISPLAY.put("symbol_less_than", "<");
        PATH_TO_DISPLAY.put("symbol_greater_than", ">");
        PATH_TO_DISPLAY.put("symbol_tilde", "~");
        PATH_TO_DISPLAY.put("symbol_dot_left", "«");
        PATH_TO_DISPLAY.put("symbol_dot_center", "•");
        PATH_TO_DISPLAY.put("symbol_dot_right", "»");
        PATH_TO_DISPLAY.put("symbol_bracket_left", "(");
        PATH_TO_DISPLAY.put("symbol_bracket_right", ")");
        PATH_TO_DISPLAY.put("symbol_bracket_double", "|");
        PATH_TO_DISPLAY.put("symbol_square_bracket_left", "[");
        PATH_TO_DISPLAY.put("symbol_square_bracket_right", "]");
        PATH_TO_DISPLAY.put("symbol_square_bracket_double", "¦");
        PATH_TO_DISPLAY.put("arrow_up", "↑");
        PATH_TO_DISPLAY.put("arrow_down", "↓");
        PATH_TO_DISPLAY.put("arrow_left", "←");
        PATH_TO_DISPLAY.put("arrow_right", "→");
        PATH_TO_DISPLAY.put("arrow_left_up", "↖");
        PATH_TO_DISPLAY.put("arrow_right_up", "↗");
        PATH_TO_DISPLAY.put("arrow_left_down", "↙");
        PATH_TO_DISPLAY.put("arrow_right_down", "↘");
        PATH_TO_DISPLAY.put("symbol_key", "🗝");
        PATH_TO_DISPLAY.put("symbol_lock", "🔒");
        PATH_TO_DISPLAY.put("symbol_trophy", "🏆");
    }

    public static String getDisplayCharForBlockPath(String path) {
        if (path == null || path.isEmpty()) return "";
        return PATH_TO_DISPLAY.getOrDefault(path, path.replace("letter_", "").replace("number_", "").replace("symbol_", "").toUpperCase());
    }
}