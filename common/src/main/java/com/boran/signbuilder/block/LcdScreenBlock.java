package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LcdScreenBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private static final VoxelShape FLOOR_NORTH = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape FLOOR_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape FLOOR_EAST  = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape FLOOR_WEST  = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    private static final VoxelShape CEILING_NORTH = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape CEILING_SOUTH = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape CEILING_EAST  = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape CEILING_WEST  = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);

    private static final VoxelShape WALL_NORTH = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WALL_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape WALL_EAST  = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape WALL_WEST  = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);

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
        return switch (face) {
            case FLOOR   -> facing;
            case CEILING -> facing.getOpposite();
            case WALL    -> Direction.UP;
        };
    }

    public static Direction getScreenDownDir(Direction facing, AttachFace face) {
        return switch (face) {
            case FLOOR   -> facing.getOpposite();
            case CEILING -> facing;
            case WALL    -> Direction.DOWN;
        };
    }

    public static Direction getScreenLeftDir(Direction facing, AttachFace face) {
        return switch (face) {
            case FLOOR   -> facing.getCounterClockWise();
            case CEILING -> facing.getClockWise();
            case WALL    -> facing.getClockWise();
        };
    }

    public static Direction getScreenRightDir(Direction facing, AttachFace face) {
        return switch (face) {
            case FLOOR   -> facing.getClockWise();
            case CEILING -> facing.getCounterClockWise();
            case WALL    -> facing.getCounterClockWise();
        };
    }

    public static boolean isConnectedNeighbor(BlockGetter level, BlockPos pos, Direction dir, Direction facing, AttachFace face) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof LcdScreenBlock) {
            return neighborState.getValue(FACING) == facing && neighborState.getValue(FACE) == face;
        }
        return false;
    }

    public static BlockPos getMasterPos(LevelReader level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);
        Direction upDir = getScreenUpDir(facing, face);
        Direction leftDir = getScreenLeftDir(facing, face);

        BlockPos current = pos;

        for (int i = 0; i < 32; i++) {
            if (isConnectedNeighbor(level, current, upDir, facing, face)) {
                current = current.relative(upDir);
            } else {
                break;
            }
        }

        for (int i = 0; i < 32; i++) {
            if (isConnectedNeighbor(level, current, leftDir, facing, face)) {
                current = current.relative(leftDir);
            } else {
                break;
            }
        }

        return current;
    }

    public static int[] getScreenDimensions(LevelReader level, BlockPos masterPos, Direction facing, AttachFace face) {
        Direction rightDir = getScreenRightDir(facing, face);
        Direction downDir = getScreenDownDir(facing, face);
        int width = 1;
        BlockPos checkRight = masterPos;
        for (int i = 1; i < 32; i++) {
            if (isConnectedNeighbor(level, checkRight, rightDir, facing, face)) {
                width++;
                checkRight = checkRight.relative(rightDir);
            } else {
                break;
            }
        }
        int height = 1;
        BlockPos checkDown = masterPos;
        for (int i = 1; i < 32; i++) {
            if (isConnectedNeighbor(level, checkDown, downDir, facing, face)) {
                height++;
                checkDown = checkDown.relative(downDir);
            } else {
                break;
            }
        }
        return new int[]{width, height};
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Direction horizontalFacing = context.getHorizontalDirection().getOpposite();

        AttachFace face;
        Direction facing;

        if (clickedFace == Direction.UP) {
            face = AttachFace.FLOOR;
            facing = horizontalFacing;
        } else if (clickedFace == Direction.DOWN) {
            face = AttachFace.CEILING;
            facing = horizontalFacing;
        } else {
            face = AttachFace.WALL;
            facing = clickedFace;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);

        return state
                .setValue(UP, isConnectedNeighbor(level, pos, getScreenUpDir(facing, face), facing, face))
                .setValue(DOWN, isConnectedNeighbor(level, pos, getScreenDownDir(facing, face), facing, face))
                .setValue(LEFT, isConnectedNeighbor(level, pos, getScreenLeftDir(facing, face), facing, face))
                .setValue(RIGHT, isConnectedNeighbor(level, pos, getScreenRightDir(facing, face), facing, face))
                .setValue(LIT, true);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;

        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);

        Direction[] neighborDirs = {
                getScreenUpDir(facing, face),
                getScreenLeftDir(facing, face),
                getScreenDownDir(facing, face),
                getScreenRightDir(facing, face)
        };

        for (Direction d : neighborDirs) {
            BlockPos nPos = pos.relative(d);
            if (isConnectedNeighbor(level, pos, d, facing, face)) {
                BlockEntity sourceBe = level.getBlockEntity(nPos);
                BlockEntity myBe = level.getBlockEntity(pos);
                if (sourceBe instanceof LcdScreenBlockEntity neighborLcd && myBe instanceof LcdScreenBlockEntity myLcd) {
                    myLcd.copyFrom(neighborLcd);
                    break;
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);

        return switch (face) {
            case FLOOR -> switch (facing) {
                case SOUTH -> FLOOR_SOUTH;
                case EAST  -> FLOOR_EAST;
                case WEST  -> FLOOR_WEST;
                default    -> FLOOR_NORTH;
            };
            case CEILING -> switch (facing) {
                case SOUTH -> CEILING_SOUTH;
                case EAST  -> CEILING_EAST;
                case WEST  -> CEILING_WEST;
                default    -> CEILING_NORTH;
            };
            case WALL -> switch (facing) {
                case SOUTH -> WALL_SOUTH;
                case EAST  -> WALL_EAST;
                case WEST  -> WALL_WEST;
                default    -> WALL_NORTH;
            };
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LcdScreenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        if (type == com.boran.signbuilder.block.entity.ModBlockEntities.LCD_SCREEN_BLOCK_ENTITY.get()) {
            return (l, p, s, entity) -> com.boran.signbuilder.block.entity.LcdScreenBlockEntity.serverTick(l, p, s, (com.boran.signbuilder.block.entity.LcdScreenBlockEntity) entity);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof com.boran.signbuilder.item.StorageLinkerItem) {
            return held.useOn(new net.minecraft.world.item.context.UseOnContext(player, hand, hit));
        }

        if (player.isShiftKeyDown() && !held.isEmpty()) {
            return InteractionResult.PASS;
        }

        Direction facing = state.getValue(FACING);
        if (held.is(this.asItem()) && hit.getDirection() != facing) {
            return InteractionResult.PASS;
        }

        BlockPos masterPos = getMasterPos(level, pos, state);
        if (level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(masterPos);
            if (be instanceof LcdScreenBlockEntity lcdBe) {
                int[] dims = getScreenDimensions(level, masterPos, state.getValue(FACING), state.getValue(FACE));

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
                        lcdBe.getVisualAnim(),
                        dims[0],
                        dims[1],
                        lcdBe.getStorageMonitorMode(),
                        lcdBe.getStorageStyle(),
                        lcdBe.getStorageFilterMode(),
                        lcdBe.getStorageFlowMode(),
                        lcdBe.getWatchlistItems(),
                        lcdBe.isLoopEnabled(),
                        lcdBe.isLoopShuffle(),
                        lcdBe.getLoopTransition(),
                        lcdBe.getLoopAnims(),
                        lcdBe.getLoopDurations(),
                        lcdBe.getLoopColors(),
                        lcdBe.getLoopTexts1(),
                        lcdBe.getLoopTexts2(),
                        lcdBe.getLoopModes1(),
                        lcdBe.getLoopModes2(),
                        lcdBe.getLoopAligns()
                ));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
