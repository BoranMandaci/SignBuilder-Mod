package com.boran.signbuilder.block;

import com.boran.signbuilder.SignBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
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

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create("signbuilder", Registries.BLOCK);
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    private static BlockBehaviour.Properties createLetterProperties() {
        return BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE)
                .noOcclusion()
                .lightLevel(state -> state.getValue(GLOWING) ? 15 : 0)
                .hasPostProcess((state, level, pos) -> state.getValue(GLOWING))
                .emissiveRendering((state, level, pos) -> state.getValue(GLOWING));
    }

    public static final RegistrySupplier<Block> LETTER_A = BLOCKS.register("letter_a",
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

    public static final RegistrySupplier<Block> LETTER_B = createLetterBlock("letter_b");
    public static final RegistrySupplier<Block> LETTER_C = createLetterBlock("letter_c");
    public static final RegistrySupplier<Block> LETTER_D = createLetterBlock("letter_d");
    public static final RegistrySupplier<Block> LETTER_E = createLetterBlock("letter_e");
    public static final RegistrySupplier<Block> LETTER_F = createLetterBlock("letter_f");
    public static final RegistrySupplier<Block> LETTER_G = createLetterBlock("letter_g");
    public static final RegistrySupplier<Block> LETTER_H = createLetterBlock("letter_h");

    public static final RegistrySupplier<Block> LETTER_I = BLOCKS.register("letter_i",
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

    public static final RegistrySupplier<Block> LETTER_J = createLetterBlock("letter_j");
    public static final RegistrySupplier<Block> LETTER_K = createLetterBlock("letter_k");
    public static final RegistrySupplier<Block> LETTER_L = createLetterBlock("letter_l");
    public static final RegistrySupplier<Block> LETTER_M = createMBlock("letter_m");
    public static final RegistrySupplier<Block> LETTER_N = createLetterBlock("letter_n");
    public static final RegistrySupplier<Block> LETTER_O = createLetterBlock("letter_o");
    public static final RegistrySupplier<Block> LETTER_P = createLetterBlock("letter_p");
    public static final RegistrySupplier<Block> LETTER_R = createLetterBlock("letter_r");
    public static final RegistrySupplier<Block> LETTER_S = createLetterBlock("letter_s");
    public static final RegistrySupplier<Block> LETTER_T = createLetterBlock("letter_t");
    public static final RegistrySupplier<Block> LETTER_U = createLetterBlock("letter_u");
    public static final RegistrySupplier<Block> LETTER_V = createLetterBlock("letter_v");
    public static final RegistrySupplier<Block> LETTER_W = createWBlock("letter_w");
    public static final RegistrySupplier<Block> LETTER_X = createLetterBlock("letter_x");
    public static final RegistrySupplier<Block> LETTER_Y = createLetterBlock("letter_y");
    public static final RegistrySupplier<Block> LETTER_Z = createLetterBlock("letter_z");

    public static final RegistrySupplier<Block> NUMBER_0 = createLetterBlock("number_0");

    public static final RegistrySupplier<Block> NUMBER_1 = BLOCKS.register("number_1",
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

    public static final RegistrySupplier<Block> NUMBER_2 = createLetterBlock("number_2");
    public static final RegistrySupplier<Block> NUMBER_3 = createLetterBlock("number_3");
    public static final RegistrySupplier<Block> NUMBER_4 = createLetterBlock("number_4");
    public static final RegistrySupplier<Block> NUMBER_5 = createLetterBlock("number_5");
    public static final RegistrySupplier<Block> NUMBER_6 = createLetterBlock("number_6");
    public static final RegistrySupplier<Block> NUMBER_7 = createLetterBlock("number_7");
    public static final RegistrySupplier<Block> NUMBER_8 = createLetterBlock("number_8");
    public static final RegistrySupplier<Block> NUMBER_9 = createLetterBlock("number_9");

    public static final RegistrySupplier<Block> ARROW_UP = createLetterBlock("arrow_up");
    public static final RegistrySupplier<Block> ARROW_DOWN = createLetterBlock("arrow_down");

    public static final RegistrySupplier<Block> ARROW_LEFT = createHorizontalArrowBlock("arrow_left");
    public static final RegistrySupplier<Block> ARROW_RIGHT = createHorizontalArrowBlock("arrow_right");

    public static final RegistrySupplier<Block> SYMBOL_PLUS = createPlusBlock("symbol_plus");
    public static final RegistrySupplier<Block> SYMBOL_MINUS = createMinusBlock("symbol_minus");
    public static final RegistrySupplier<Block> SYMBOL_HEART = createHeartBlock("symbol_heart");

    public static final RegistrySupplier<Block> SYMBOL_DOT_LEFT = createLeftDotBlock("symbol_dot_left");
    public static final RegistrySupplier<Block> SYMBOL_DOT_CENTER = createCenterDotBlock("symbol_dot_center");
    public static final RegistrySupplier<Block> SYMBOL_DOT_RIGHT = createRightDotBlock("symbol_dot_right");

    public static final RegistrySupplier<Block> SYMBOL_SLASH = createSlashBlock("symbol_slash");

    public static final RegistrySupplier<Block> ARROW_LEFT_UP = createDiagonalArrowBlock("arrow_left_up");
    public static final RegistrySupplier<Block> ARROW_RIGHT_UP = createDiagonalArrowBlock("arrow_right_up");
    public static final RegistrySupplier<Block> ARROW_LEFT_DOWN = createDiagonalArrowBlock("arrow_left_down");
    public static final RegistrySupplier<Block> ARROW_RIGHT_DOWN = createDiagonalArrowBlock("arrow_right_down");

    public static final RegistrySupplier<Block> SYMBOL_BRACKET_LEFT = createLeftSquareBracketBlock("symbol_bracket_left");
    public static final RegistrySupplier<Block> SYMBOL_BRACKET_RIGHT = createRightSquareBracketBlock("symbol_bracket_right");
    public static final RegistrySupplier<Block> SYMBOL_BRACKET_DOUBLE = createDoubleBracketBlock("symbol_bracket_double");

    public static final RegistrySupplier<Block> SYMBOL_SQUARE_BRACKET_LEFT = createLeftSquareBracketBlock("symbol_square_bracket_left");
    public static final RegistrySupplier<Block> SYMBOL_SQUARE_BRACKET_RIGHT = createRightSquareBracketBlock("symbol_square_bracket_right");
    public static final RegistrySupplier<Block> SYMBOL_SQUARE_BRACKET_DOUBLE = createDoubleBracketBlock("symbol_square_bracket_double");

    public static final RegistrySupplier<Block> SYMBOL_HASHTAG = createHashtagBlock("symbol_hashtag");

    public static final RegistrySupplier<Block> SYMBOL_EURO = createPlusBlock("symbol_euro");
    public static final RegistrySupplier<Block> SYMBOL_DOLLAR = createLetterBlock("symbol_dollar");
    public static final RegistrySupplier<Block> SYMBOL_TL = createTLBlock("symbol_tl");

    public static final RegistrySupplier<Block> SYMBOL_BACKSLASH = createBackslashBlock("symbol_backslash");
    public static final RegistrySupplier<Block> SYMBOL_STAR = createStarBlock("symbol_star");
    public static final RegistrySupplier<Block> SYMBOL_POUND = createLetterBlock("symbol_pound");

    public static final RegistrySupplier<Block> SIGN_PRESS = BLOCKS.register("sign_press",
            () -> new SignPressBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(5.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static void register() {
        BLOCKS.register();
    }

    private static RegistrySupplier<Block> createLetterBlock(String name) {
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

    private static RegistrySupplier<Block> createMBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 2.0, 11.0, 14.0, 14.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 2.0, 8.0, 14.0, 14.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(2.0, 0.0, 8.0, 14.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(2.0, 0.0, 5.0, 14.0, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(2.0, 1.0, 13.0, 14.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(2.0, 1.0, 0.0, 14.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 2.0, 3.0, 15.0, 14.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 2.0, 16.0, 15.0, 14.0);

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

    private static RegistrySupplier<Block> createWBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.5, 11.0, 14.0, 14.5);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 1.5, 8.0, 14.0, 14.5);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(1.5, 0.0, 8.0, 14.5, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.5, 0.0, 5.0, 14.5, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.5, 1.0, 13.0, 14.5, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(1.5, 1.0, 0.0, 14.5, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 1.5, 3.0, 15.0, 14.5);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 1.5, 16.0, 15.0, 14.5);

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

    private static RegistrySupplier<Block> createSlashBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 15.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 15.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 15.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 15.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(3.0, 1.0, 13.0, 13.0, 16.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(3.0, 1.0, 0.0, 13.0, 16.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 3.0, 3.0, 16.0, 13.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 3.0, 16.0, 16.0, 13.0);

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

    private static RegistrySupplier<Block> createBackslashBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 4.0, 11.0, 15.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 15.0, 12.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 12.0, 15.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(4.0, 0.0, 5.0, 13.0, 15.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(4.0, 1.0, 13.0, 13.0, 16.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(3.0, 1.0, 0.0, 12.0, 16.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 4.0, 3.0, 16.0, 13.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 3.0, 16.0, 16.0, 12.0);

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

    private static RegistrySupplier<Block> createHorizontalArrowBlock(String name) {
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

    private static RegistrySupplier<Block> createPlusBlock(String name) {
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

    private static RegistrySupplier<Block> createDiagonalArrowBlock(String name) {
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

    private static RegistrySupplier<Block> createHeartBlock(String name) {
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

    private static RegistrySupplier<Block> createMinusBlock(String name) {
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

    private static RegistrySupplier<Block> createLeftDotBlock(String name) {
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

    private static RegistrySupplier<Block> createCenterDotBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 6.5, 11.0, 3.0, 9.5);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 6.5, 8.0, 3.0, 9.5);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(6.5, 0.0, 8.0, 9.5, 3.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(6.5, 0.0, 5.0, 9.5, 3.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(6.5, 1.0, 13.0, 9.5, 4.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(6.5, 1.0, 0.0, 9.5, 4.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 6.5, 3.0, 4.0, 9.5);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 6.5, 16.0, 4.0, 9.5);

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

    private static RegistrySupplier<Block> createRightDotBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 10.0, 11.0, 3.0, 13.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 3.0, 6.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 6.0, 3.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(10.0, 0.0, 5.0, 13.0, 3.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(3.0, 1.0, 13.0, 6.0, 4.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(10.0, 1.0, 0.0, 13.0, 4.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 3.0, 3.0, 4.0, 6.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 10.0, 16.0, 4.0, 13.0);

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

    private static RegistrySupplier<Block> createLeftSquareBracketBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 10.0, 11.0, 14.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 1.0, 8.0, 14.0, 6.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(1.0, 0.0, 8.0, 6.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(10.0, 0.0, 5.0, 15.0, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.0, 1.0, 13.0, 6.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(10.0, 1.0, 0.0, 15.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 1.0, 3.0, 15.0, 6.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 10.0, 16.0, 15.0, 15.0);

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
                    return switch (direction) { case EAST -> SHAPE_WALL_EAST; case WEST -> SHAPE_WALL_WEST; case SOUTH -> SHAPE_WALL_SOUTH; default -> SHAPE_WALL_NORTH; };
                } else {
                    return switch (direction) { case EAST -> SHAPE_FLOOR_EAST; case WEST -> SHAPE_FLOOR_WEST; case SOUTH -> SHAPE_FLOOR_SOUTH; default -> SHAPE_FLOOR_NORTH; };
                }
            }
        });
    }

    private static RegistrySupplier<Block> createRightSquareBracketBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.0, 11.0, 14.0, 6.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 10.0, 8.0, 14.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(10.0, 0.0, 8.0, 15.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.0, 0.0, 5.0, 6.0, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(10.0, 1.0, 13.0, 15.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(1.0, 1.0, 0.0, 6.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 10.0, 3.0, 15.0, 15.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 1.0, 16.0, 15.0, 6.0);

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
                    return switch (direction) { case EAST -> SHAPE_WALL_EAST; case WEST -> SHAPE_WALL_WEST; case SOUTH -> SHAPE_WALL_SOUTH; default -> SHAPE_WALL_NORTH; };
                } else {
                    return switch (direction) { case EAST -> SHAPE_FLOOR_EAST; case WEST -> SHAPE_FLOOR_WEST; case SOUTH -> SHAPE_FLOOR_SOUTH; default -> SHAPE_FLOOR_NORTH; };
                }
            }
        });
    }

    private static RegistrySupplier<Block> createStarBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 0.0, 11.0, 16.0, 16.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 0.0, 8.0, 16.0, 16.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(0.0, 0.0, 8.0, 16.0, 16.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(0.0, 0.0, 5.0, 16.0, 16.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);

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

    private static RegistrySupplier<Block> createDoubleBracketBlock(String name) {
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

    private static RegistrySupplier<Block> createHashtagBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 2.0, 11.0, 14.0, 14.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 2.0, 8.0, 14.0, 14.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(2.0, 0.0, 8.0, 14.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(2.0, 0.0, 5.0, 14.0, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(2.0, 1.0, 13.0, 14.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(2.0, 1.0, 0.0, 14.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 2.0, 3.0, 15.0, 14.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 2.0, 16.0, 15.0, 14.0);

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

    private static RegistrySupplier<Block> createTLBlock(String name) {
        return BLOCKS.register(name, () -> new LetterBlock(createLetterProperties()) {
            private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 1.0, 11.0, 14.0, 14.0);
            private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 2.0, 8.0, 14.0, 15.0);
            private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(2.0, 0.0, 8.0, 15.0, 14.0, 11.0);
            private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(1.0, 0.0, 5.0, 14.0, 14.0, 8.0);
            private static final VoxelShape SHAPE_WALL_NORTH  = Block.box(1.0, 1.0, 13.0, 14.0, 15.0, 16.0);
            private static final VoxelShape SHAPE_WALL_SOUTH  = Block.box(2.0, 1.0, 0.0, 15.0, 15.0, 3.0);
            private static final VoxelShape SHAPE_WALL_EAST   = Block.box(0.0, 1.0, 1.0, 3.0, 15.0, 14.0);
            private static final VoxelShape SHAPE_WALL_WEST   = Block.box(13.0, 1.0, 2.0, 16.0, 15.0, 15.0);

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
}