package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LcdScreenBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final VoxelShape WALL_NORTH = Block.box(0, 0, 14, 16, 16, 16);
    public static final VoxelShape WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    public static final VoxelShape WALL_EAST  = Block.box(0, 0, 0, 2, 16, 16);
    public static final VoxelShape WALL_WEST  = Block.box(14, 0, 0, 16, 16, 16);
    public static final VoxelShape FLOOR_AABB = Block.box(0, 0, 0, 16, 2, 16);
    public static final VoxelShape CEILING_AABB = Block.box(0, 14, 0, 16, 16, 16);

    public LcdScreenBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.WALL)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(LEFT, false)
                .setValue(RIGHT, false)
                .setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, UP, DOWN, LEFT, RIGHT, LIT);
    }

    public static Direction getScreenUpDir(Direction facing, AttachFace face) {
        if (face == AttachFace.WALL) return Direction.UP;
        return facing;
    }

    public static Direction getScreenDownDir(Direction facing, AttachFace face) {
        if (face == AttachFace.WALL) return Direction.DOWN;
        return facing.getOpposite();
    }

    public static Direction getScreenLeftDir(Direction facing, AttachFace face) {
        return facing.getClockWise();
    }

    public static Direction getScreenRightDir(Direction facing, AttachFace face) {
        return facing.getCounterClockWise();
    }

    public static boolean isConnectedNeighbor(LevelReader level, BlockPos pos, Direction dir, Direction facing, AttachFace face) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (!(neighborState.getBlock() instanceof LcdScreenBlock)) return false;
        return neighborState.getValue(FACING) == facing && neighborState.getValue(FACE) == face;
    }

    public static BlockPos getMasterPos(LevelReader level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);
        Direction upDir = getScreenUpDir(facing, face);
        Direction leftDir = getScreenLeftDir(facing, face);

        BlockPos cur = pos;
        for (int i = 0; i < 64; i++) {
            boolean moved = false;
            while (isConnectedNeighbor(level, cur, upDir, facing, face)) {
                cur = cur.relative(upDir);
                moved = true;
            }
            while (isConnectedNeighbor(level, cur, leftDir, facing, face)) {
                cur = cur.relative(leftDir);
                moved = true;
            }
            if (!moved) break;
        }
        return cur;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        Direction facing;
        AttachFace face = AttachFace.WALL;

        if (clickedFace.getAxis() == Direction.Axis.Y) {
            BlockPos neighborPos = pos.relative(clickedFace.getOpposite());
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof LcdScreenBlock && neighborState.getValue(FACE) == AttachFace.WALL) {
                facing = neighborState.getValue(FACING);
            } else {
                return null;
            }
        } else {
            facing = clickedFace;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(FACE, face)
                .setValue(UP, isConnectedNeighbor(level, pos, getScreenUpDir(facing, face), facing, face))
                .setValue(DOWN, isConnectedNeighbor(level, pos, getScreenDownDir(facing, face), facing, face))
                .setValue(LEFT, isConnectedNeighbor(level, pos, getScreenLeftDir(facing, face), facing, face))
                .setValue(RIGHT, isConnectedNeighbor(level, pos, getScreenRightDir(facing, face), facing, face))
                .setValue(LIT, true);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);
        return state
                .setValue(UP, isConnectedNeighbor(level, pos, getScreenUpDir(facing, face), facing, face))
                .setValue(DOWN, isConnectedNeighbor(level, pos, getScreenDownDir(facing, face), facing, face))
                .setValue(LEFT, isConnectedNeighbor(level, pos, getScreenLeftDir(facing, face), facing, face))
                .setValue(RIGHT, isConnectedNeighbor(level, pos, getScreenRightDir(facing, face), facing, face));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;

        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);
        Direction[] dirs = {
                getScreenDownDir(facing, face),
                getScreenRightDir(facing, face),
                getScreenUpDir(facing, face),
                getScreenLeftDir(facing, face)
        };
        for (Direction d : dirs) {
            BlockPos nPos = pos.relative(d);
            if (isConnectedNeighbor(level, pos, d, facing, face)) {
                BlockState nState = level.getBlockState(nPos);
                BlockPos nMasterPos = getMasterPos(level, nPos, nState);
                BlockEntity sourceBe = level.getBlockEntity(nMasterPos);
                if (!(sourceBe instanceof LcdScreenBlockEntity sourceLcd) || (sourceLcd.getText().isEmpty() && sourceLcd.getTheme() == 0 && sourceLcd.getVisualAnim() == 0)) {
                    sourceBe = level.getBlockEntity(nPos);
                }
                BlockEntity myBe = level.getBlockEntity(pos);
                if (sourceBe instanceof LcdScreenBlockEntity neighborLcd && myBe instanceof LcdScreenBlockEntity myLcd) {
                    myLcd.copyFrom(neighborLcd);
                    myLcd.setChanged();
                    myLcd.sync();
                    BlockPos newMasterPos = getMasterPos(level, pos, state);
                    if (!newMasterPos.equals(pos) && level.getBlockEntity(newMasterPos) instanceof LcdScreenBlockEntity newMasterLcd) {
                        newMasterLcd.copyFrom(neighborLcd);
                        newMasterLcd.setChanged();
                        newMasterLcd.sync();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            Direction facing = state.getValue(FACING);
            AttachFace face = state.getValue(FACE);
            Direction[] dirs = {
                    getScreenDownDir(facing, face),
                    getScreenRightDir(facing, face)
            };
            for (Direction d : dirs) {
                BlockPos nPos = pos.relative(d);
                if (isConnectedNeighbor(level, pos, d, facing, face)) {
                    BlockEntity curBe = level.getBlockEntity(pos);
                    BlockEntity nextBe = level.getBlockEntity(nPos);
                    if (curBe instanceof LcdScreenBlockEntity curLcd && nextBe instanceof LcdScreenBlockEntity nextLcd) {
                        nextLcd.copyFrom(curLcd);
                        nextLcd.setChanged();
                        nextLcd.sync();
                        break;
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AttachFace face = state.getValue(FACE);
        if (face == AttachFace.FLOOR) return FLOOR_AABB;
        if (face == AttachFace.CEILING) return CEILING_AABB;

        return switch (state.getValue(FACING)) {
            case NORTH -> WALL_NORTH;
            case SOUTH -> WALL_SOUTH;
            case EAST  -> WALL_EAST;
            case WEST  -> WALL_WEST;
            default    -> WALL_NORTH;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LcdScreenBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() && !held.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Direction facing = state.getValue(FACING);
        if (held.is(this.asItem()) && hit.getDirection() != facing) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        return openLcdGui(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        openLcdGui(level, pos, state, player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private ItemInteractionResult openLcdGui(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos masterPos = getMasterPos(level, pos, state);
        if (level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(masterPos);
            if (be instanceof LcdScreenBlockEntity lcdBe) {
                EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.openLcdScreen(
                        masterPos,
                        lcdBe.getText(),
                        lcdBe.getMode(),
                        lcdBe.getMode2(),
                        lcdBe.getAlign(),
                        lcdBe.getSpeed(),
                        lcdBe.getFontSize(),
                        lcdBe.getColor(),
                        lcdBe.isRainbow(),
                        lcdBe.isGlow(),
                        lcdBe.getTheme(),
                        lcdBe.getWidgetTL(),
                        lcdBe.getWidgetTR(),
                        lcdBe.getWidgetBL(),
                        lcdBe.getWidgetBR(),
                        lcdBe.getFontStyle(),
                        lcdBe.getVisualAnim()
                ));
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
