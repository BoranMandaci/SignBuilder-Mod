package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GridSignBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final IntegerProperty LIGHT_MODE = LetterBlock.LIGHT_MODE;

    public static final VoxelShape WALL_NORTH = Block.box(0, 0, 15, 16, 16, 16);
    public static final VoxelShape WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
    public static final VoxelShape WALL_EAST  = Block.box(0, 0, 0, 1, 16, 16);
    public static final VoxelShape WALL_WEST  = Block.box(15, 0, 0, 16, 16, 16);
    public static final VoxelShape FLOOR_AABB = Block.box(0, 0, 0, 16, 1, 16);
    public static final VoxelShape CEILING_AABB = Block.box(0, 15, 0, 16, 16, 16);

    public GridSignBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.WALL)
                .setValue(LIGHT_MODE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, LIGHT_MODE);
    }

    public static void updateLightLevel(Level level, BlockPos pos, BlockState state, GridSignBlockEntity entity) {
        if (level.isClientSide()) return;
        int targetMode = entity.getEmittedLightMode();
        if (state.hasProperty(LIGHT_MODE) && state.getValue(LIGHT_MODE) != targetMode) {
            level.setBlock(pos, state.setValue(LIGHT_MODE, targetMode), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GridSignBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.GRID_SIGN_BLOCK_ENTITY.get()) {
            return (l, p, s, entity) -> GridSignBlockEntity.tick(l, p, s, (GridSignBlockEntity) entity);
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AttachFace face = state.getValue(FACE);
        Direction facing = state.getValue(FACING);

        if (level.getBlockEntity(pos) instanceof GridSignBlockEntity gridBe && gridBe.hasBackplate()) {
            return BackplateBlock.calculateShape(face, facing, gridBe.getFacingRotation());
        }

        if (face == AttachFace.FLOOR) return FLOOR_AABB;
        if (face == AttachFace.CEILING) return CEILING_AABB;
        
        return switch (facing) {
            case NORTH -> WALL_NORTH;
            case SOUTH -> WALL_SOUTH;
            case EAST -> WALL_EAST;
            case WEST -> WALL_WEST;
            default -> WALL_NORTH;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GridSignBlockEntity gridBe) {
                if (!player.isCreative()) {
                    dropGridSignResources(level, pos, gridBe);
                }
                gridBe.setSuppressDrops(true);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private void dropGridSignResources(Level level, BlockPos pos, GridSignBlockEntity gridBe) {
        if (gridBe.hasBackplate()) {
            Block.popResource(level, pos, new ItemStack(ModBlocks.BACKPLATE_ITEM.get()));
        }
        for (GridSignBlockEntity.CellData cell : gridBe.getCells().values()) {
            Block letterBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("signbuilder", cell.character));
            if (letterBlock != null && letterBlock != Blocks.AIR) {
                Block.popResource(level, pos, new ItemStack(letterBlock));
            }
        }
        if (gridBe.isActive() || gridBe.getWrenchMode() != 0) {
            Block.popResource(level, pos, new ItemStack(net.minecraft.world.item.Items.GLOWSTONE_DUST));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GridSignBlockEntity gridBe) {
                if (!gridBe.shouldSuppressDrops()) {
                    dropGridSignResources(level, pos, gridBe);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }
}
