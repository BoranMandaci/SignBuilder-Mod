package com.boran.signbuilder.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "signbuilder");
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    private static BlockBehaviour.Properties createLetterProperties() {
        return BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE)
                .noOcclusion()
                .lightLevel(state -> state.getValue(GLOWING) ? 15 : 0)
                .hasPostProcess((state, level, pos) -> state.getValue(GLOWING))
                .emissiveRendering((state, level, pos) -> state.getValue(GLOWING));
    }

    public static final RegistryObject<Block> LETTER_A = BLOCKS.register("letter_a",
            () -> new LetterBlock(createLetterProperties()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);
                private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    Direction clickedFace = context.getClickedFace();
                    BlockState state = this.defaultBlockState();
                    if (clickedFace.getAxis() == Direction.Axis.Y) {
                        return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                    } else {
                        return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                    }
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                        switch (direction) {
                            case EAST:  return SHAPE_WALL_EAST;
                            case WEST:  return SHAPE_WALL_WEST;
                            case SOUTH: return SHAPE_WALL_SOUTH;
                            case NORTH: default: return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH: default: return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_B = createLetterBlock("letter_b");
    public static final RegistryObject<Block> LETTER_C = createLetterBlock("letter_c");
    public static final RegistryObject<Block> LETTER_D = createLetterBlock("letter_d");
    public static final RegistryObject<Block> LETTER_E = createLetterBlock("letter_e");
    public static final RegistryObject<Block> LETTER_F = createLetterBlock("letter_f");
    public static final RegistryObject<Block> LETTER_G = createLetterBlock("letter_g");
    public static final RegistryObject<Block> LETTER_H = createLetterBlock("letter_h");

    public static final RegistryObject<Block> LETTER_I = BLOCKS.register("letter_i",
            () -> new LetterBlock(createLetterProperties()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 6.0, 11.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 6.0, 8.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(6.0, 0.0, 8.0, 10.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(6.0, 0.0, 5.0, 10.0, 14.0, 8.0);
                private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(6.0, 1.0, 13.0, 10.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(6.0, 1.0, 0.0, 10.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 6.0, 3.0, 15.0, 10.0);
                private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 6.0, 16.0, 15.0, 10.0);

                { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    Direction clickedFace = context.getClickedFace();
                    BlockState state = this.defaultBlockState();
                    if (clickedFace.getAxis() == Direction.Axis.Y) {
                        return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                    } else {
                        return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                    }
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                        switch (direction) {
                            case EAST:  return SHAPE_WALL_EAST;
                            case WEST:  return SHAPE_WALL_WEST;
                            case SOUTH: return SHAPE_WALL_SOUTH;
                            case NORTH: default: return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH: default: return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_J = createLetterBlock("letter_j");
    public static final RegistryObject<Block> LETTER_K = createLetterBlock("letter_k");
    public static final RegistryObject<Block> LETTER_L = createLetterBlock("letter_l");
    public static final RegistryObject<Block> LETTER_M = createLetterBlock("letter_m");
    public static final RegistryObject<Block> LETTER_N = createLetterBlock("letter_n");
    public static final RegistryObject<Block> LETTER_O = createLetterBlock("letter_o");
    public static final RegistryObject<Block> LETTER_P = createLetterBlock("letter_p");
    public static final RegistryObject<Block> LETTER_R = createLetterBlock("letter_r");
    public static final RegistryObject<Block> LETTER_S = createLetterBlock("letter_s");
    public static final RegistryObject<Block> LETTER_T = createLetterBlock("letter_t");
    public static final RegistryObject<Block> LETTER_U = createLetterBlock("letter_u");
    public static final RegistryObject<Block> LETTER_V = createLetterBlock("letter_v");
    public static final RegistryObject<Block> LETTER_W = createLetterBlock("letter_w");
    public static final RegistryObject<Block> LETTER_X = createLetterBlock("letter_x");
    public static final RegistryObject<Block> LETTER_Y = createLetterBlock("letter_y");
    public static final RegistryObject<Block> LETTER_Z = createLetterBlock("letter_z");

    public static final RegistryObject<Block> NUMBER_0 = createLetterBlock("number_0");

    public static final RegistryObject<Block> NUMBER_1 = BLOCKS.register("number_1",
            () -> new LetterBlock(createLetterProperties()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 6.0, 11.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 6.0, 8.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(6.0, 0.0, 8.0, 10.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(6.0, 0.0, 5.0, 10.0, 14.0, 8.0);
                private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(6.0, 1.0, 13.0, 10.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(6.0, 1.0, 0.0, 10.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 6.0, 3.0, 15.0, 10.0);
                private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 6.0, 16.0, 15.0, 10.0);

                { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
                }

                @Override
                public BlockState getStateForPlacement(BlockPlaceContext context) {
                    Direction clickedFace = context.getClickedFace();
                    BlockState state = this.defaultBlockState();
                    if (clickedFace.getAxis() == Direction.Axis.Y) {
                        return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                    } else {
                        return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                    }
                }

                @Override
                public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                    net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                    Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                        switch (direction) {
                            case EAST:  return SHAPE_WALL_EAST;
                            case WEST:  return SHAPE_WALL_WEST;
                            case SOUTH: return SHAPE_WALL_SOUTH;
                            case NORTH: default: return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH: default: return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> NUMBER_2 = createLetterBlock("number_2");
    public static final RegistryObject<Block> NUMBER_3 = createLetterBlock("number_3");
    public static final RegistryObject<Block> NUMBER_4 = createLetterBlock("number_4");
    public static final RegistryObject<Block> NUMBER_5 = createLetterBlock("number_5");
    public static final RegistryObject<Block> NUMBER_6 = createLetterBlock("number_6");
    public static final RegistryObject<Block> NUMBER_7 = createLetterBlock("number_7");
    public static final RegistryObject<Block> NUMBER_8 = createLetterBlock("number_8");
    public static final RegistryObject<Block> NUMBER_9 = createLetterBlock("number_9");

    public static final RegistryObject<Block> ARROW_UP = createLetterBlock("arrow_up");
    public static final RegistryObject<Block> ARROW_DOWN = createLetterBlock("arrow_down");

    public static final RegistryObject<Block> ARROW_LEFT = createHorizontalArrowBlock("arrow_left");
    public static final RegistryObject<Block> ARROW_RIGHT = createHorizontalArrowBlock("arrow_right");

    public static final RegistryObject<Block> SYMBOL_PLUS = createPlusBlock("symbol_plus");
    public static final RegistryObject<Block> SYMBOL_MINUS = createMinusBlock("symbol_minus");
    public static final RegistryObject<Block> SYMBOL_DOT = createDotBlock("symbol_dot");
    public static final RegistryObject<Block> SYMBOL_HEART = createHeartBlock("symbol_heart");

    private static RegistryObject<Block> createLetterBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

            { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                Direction clickedFace = context.getClickedFace();
                BlockState state = this.defaultBlockState();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                } else {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                }
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                    switch (direction) {
                        case EAST:  return SHAPE_WALL_EAST;
                        case WEST:  return SHAPE_WALL_WEST;
                        case SOUTH: return SHAPE_WALL_SOUTH;
                        case NORTH: default: return SHAPE_WALL_NORTH;
                    }
                } else {
                    switch (direction) {
                        case EAST:  return SHAPE_FLOOR_EAST;
                        case WEST:  return SHAPE_FLOOR_WEST;
                        case SOUTH: return SHAPE_FLOOR_SOUTH;
                        case NORTH: default: return SHAPE_FLOOR_NORTH;
                    }
                }
            }
        });
    }

    private static RegistryObject<Block> createHorizontalArrowBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.0, 11.5, 12.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(4.5, 0.0, 1.0, 8.0, 12.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(1.0, 0.0, 8.0, 15.0, 12.0, 11.5);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.0, 0.0, 4.5, 15.0, 12.0, 8.0);

            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.0, 3.0, 13.0, 15.0, 13.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(1.0, 3.0, 0.0, 15.0, 13.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 3.0, 1.0, 3.0, 13.0, 15.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 3.0, 1.0, 16.0, 13.0, 15.0);

            { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                Direction clickedFace = context.getClickedFace();
                BlockState state = this.defaultBlockState();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                } else {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                }
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                    switch (direction) {
                        case EAST:  return SHAPE_WALL_EAST;
                        case WEST:  return SHAPE_WALL_WEST;
                        case SOUTH: return SHAPE_WALL_SOUTH;
                        case NORTH: default: return SHAPE_WALL_NORTH;
                    }
                } else {
                    switch (direction) {
                        case EAST:  return SHAPE_FLOOR_EAST;
                        case WEST:  return SHAPE_FLOOR_WEST;
                        case SOUTH: return SHAPE_FLOOR_SOUTH;
                        case NORTH: default: return SHAPE_FLOOR_NORTH;
                    }
                }
            }
        });
    }

    private static RegistryObject<Block> createPlusBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.0, 11.0, 14.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 1.0, 8.0, 14.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(1.0, 0.0, 8.0, 15.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.0, 0.0, 5.0, 15.0, 14.0, 8.0);

            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.0, 1.0, 13.0, 15.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(1.0, 1.0, 0.0, 15.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 1.0, 3.0, 15.0, 15.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 1.0, 16.0, 15.0, 15.0);

            { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                Direction clickedFace = context.getClickedFace();
                BlockState state = this.defaultBlockState();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                } else {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                }
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                    switch (direction) {
                        case EAST:  return SHAPE_WALL_EAST;
                        case WEST:  return SHAPE_WALL_WEST;
                        case SOUTH: return SHAPE_WALL_SOUTH;
                        case NORTH: default: return SHAPE_WALL_NORTH;
                    }
                } else {
                    switch (direction) {
                        case EAST:  return SHAPE_FLOOR_EAST;
                        case WEST:  return SHAPE_FLOOR_WEST;
                        case SOUTH: return SHAPE_FLOOR_SOUTH;
                        case NORTH: default: return SHAPE_FLOOR_NORTH;
                    }
                }
            }
        });
    }

    private static RegistryObject<Block> createHeartBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.0, 11.0, 13.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 1.0, 8.0, 13.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(1.0, 0.0, 8.0, 15.0, 13.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.0, 0.0, 5.0, 15.0, 13.0, 8.0);

            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.0, 1.0, 13.0, 15.0, 14.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(1.0, 1.0, 0.0, 15.0, 14.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 1.0, 3.0, 14.0, 15.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 1.0, 16.0, 14.0, 15.0);

            { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                Direction clickedFace = context.getClickedFace();
                BlockState state = this.defaultBlockState();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                } else {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                }
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                    switch (direction) {
                        case EAST:  return SHAPE_WALL_EAST;
                        case WEST:  return SHAPE_WALL_WEST;
                        case SOUTH: return SHAPE_WALL_SOUTH;
                        case NORTH: default: return SHAPE_WALL_NORTH;
                    }
                } else {
                    switch (direction) {
                        case EAST:  return SHAPE_FLOOR_EAST;
                        case WEST:  return SHAPE_FLOOR_WEST;
                        case SOUTH: return SHAPE_FLOOR_SOUTH;
                        case NORTH: default: return SHAPE_FLOOR_NORTH;
                    }
                }
            }
        });
    }

    private static RegistryObject<Block> createMinusBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.0, 11.5, 9.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(4.5, 0.0, 1.0, 8.0, 9.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(1.0, 0.0, 8.0, 15.0, 9.0, 11.5);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.0, 0.0, 4.5, 15.0, 9.0, 8.0);

            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.0, 6.0, 13.0, 15.0, 10.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(1.0, 6.0, 0.0, 15.0, 10.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 6.0, 1.0, 3.0, 10.0, 15.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 6.0, 1.0, 16.0, 10.0, 15.0);

            { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                Direction clickedFace = context.getClickedFace();
                BlockState state = this.defaultBlockState();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                } else {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                }
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                    switch (direction) {
                        case EAST:  return SHAPE_WALL_EAST;
                        case WEST:  return SHAPE_WALL_WEST;
                        case SOUTH: return SHAPE_WALL_SOUTH;
                        case NORTH: default: return SHAPE_WALL_NORTH;
                    }
                } else {
                    switch (direction) {
                        case EAST:  return SHAPE_FLOOR_EAST;
                        case WEST:  return SHAPE_FLOOR_WEST;
                        case SOUTH: return SHAPE_FLOOR_SOUTH;
                        case NORTH: default: return SHAPE_FLOOR_NORTH;
                    }
                }
            }
        });
    }

    private static RegistryObject<Block> createDotBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 3.0, 6.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 10.0, 8.0, 3.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(10.0, 0.0, 8.0, 13.0, 3.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 6.0, 3.0, 8.0);

            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(10.0, 1.0, 13.0, 13.0, 4.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(3.0, 1.0, 0.0, 6.0, 4.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 10.0, 3.0, 4.0, 13.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 3.0, 16.0, 4.0, 6.0);

            { this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false)); }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR, GLOWING);
            }

            @Override
            public BlockState getStateForPlacement(BlockPlaceContext context) {
                Direction clickedFace = context.getClickedFace();
                BlockState state = this.defaultBlockState();
                if (clickedFace.getAxis() == Direction.Axis.Y) {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, clickedFace == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : net.minecraft.world.level.block.state.properties.AttachFace.CEILING)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getCounterClockWise());
                } else {
                    return state.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, clickedFace);
                }
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
                net.minecraft.world.level.block.state.properties.AttachFace face = state.getValue(BlockStateProperties.ATTACH_FACE);
                Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (face == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                    switch (direction) {
                        case EAST:  return SHAPE_WALL_EAST;
                        case WEST:  return SHAPE_WALL_WEST;
                        case SOUTH: return SHAPE_WALL_SOUTH;
                        case NORTH: default: return SHAPE_WALL_NORTH;
                    }
                } else {
                    switch (direction) {
                        case EAST:  return SHAPE_FLOOR_EAST;
                        case WEST:  return SHAPE_FLOOR_WEST;
                        case SOUTH: return SHAPE_FLOOR_SOUTH;
                        case NORTH: default: return SHAPE_FLOOR_NORTH;
                    }
                }
            }
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}