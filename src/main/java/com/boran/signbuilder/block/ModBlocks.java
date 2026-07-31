package com.boran.signbuilder.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "signbuilder");

    public static final RegistryObject<Block> HARF_A = BLOCKS.register("harf_a",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_B = BLOCKS.register("harf_b",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_C = BLOCKS.register("harf_c",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_D = BLOCKS.register("harf_d",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_E = BLOCKS.register("harf_e",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_F = BLOCKS.register("harf_f",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_G = BLOCKS.register("harf_g",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_H = BLOCKS.register("harf_h",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_I = BLOCKS.register("harf_i",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 6.0, 11.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 6.0, 8.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(6.0, 0.0, 8.0, 10.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(6.0, 0.0, 5.0, 10.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_J = BLOCKS.register("harf_j",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_K = BLOCKS.register("harf_k",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_L = BLOCKS.register("harf_l",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_M = BLOCKS.register("harf_m",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_N = BLOCKS.register("harf_n",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_O = BLOCKS.register("harf_o",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_P = BLOCKS.register("harf_p",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_R = BLOCKS.register("harf_r",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_S = BLOCKS.register("harf_s",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_T = BLOCKS.register("harf_t",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_U = BLOCKS.register("harf_u",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_V = BLOCKS.register("harf_v",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_Y = BLOCKS.register("harf_y",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_Z = BLOCKS.register("harf_z",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_W = BLOCKS.register("harf_w",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });

    public static final RegistryObject<Block> HARF_X = BLOCKS.register("harf_x",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                        case SOUTH: return SHAPE_SOUTH;
                        case EAST:  return SHAPE_EAST;
                        case WEST:  return SHAPE_WEST;
                        case NORTH:
                        default:    return SHAPE_NORTH;
                    }
                }
            });


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}