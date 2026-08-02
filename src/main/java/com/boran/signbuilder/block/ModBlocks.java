package com.boran.signbuilder.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
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
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty COLOR = net.minecraft.world.level.block.state.properties.IntegerProperty.create("color", 0, 15);

    public static final RegistryObject<Block> LETTER_A = BLOCKS.register("letter_a",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_B = BLOCKS.register("letter_b",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_C = BLOCKS.register("letter_c",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_D = BLOCKS.register("letter_d",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_E = BLOCKS.register("letter_e",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_F = BLOCKS.register("letter_f",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_G = BLOCKS.register("letter_g",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_H = BLOCKS.register("letter_h",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_I = BLOCKS.register("letter_i",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 6.0, 11.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 6.0, 8.0, 14.0, 10.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(6.0, 0.0, 8.0, 10.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(6.0, 0.0, 5.0, 10.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(6.0, 1.0, 13.0, 10.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(6.0, 1.0, 0.0, 10.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 6.0, 3.0, 15.0, 10.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 6.0, 16.0, 15.0, 10.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_J = BLOCKS.register("letter_j",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_K = BLOCKS.register("letter_k",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_L = BLOCKS.register("letter_l",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_M = BLOCKS.register("letter_m",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_N = BLOCKS.register("letter_n",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_O = BLOCKS.register("letter_o",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_P = BLOCKS.register("letter_p",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_R = BLOCKS.register("letter_r",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_S = BLOCKS.register("letter_s",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_T = BLOCKS.register("letter_t",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_U = BLOCKS.register("letter_u",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_V = BLOCKS.register("letter_v",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_W = BLOCKS.register("letter_w",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_X = BLOCKS.register("letter_x",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_Y = BLOCKS.register("letter_y",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });

    public static final RegistryObject<Block> LETTER_Z = BLOCKS.register("letter_z",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).noOcclusion()) {
                private static final VoxelShape SHAPE_FLOOR_NORTH = Block.box(8.0, 0.0, 3.0, 11.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_SOUTH = Block.box(5.0, 0.0, 3.0, 8.0, 14.0, 13.0);
                private static final VoxelShape SHAPE_FLOOR_EAST  = Block.box(3.0, 0.0, 8.0, 13.0, 14.0, 11.0);
                private static final VoxelShape SHAPE_FLOOR_WEST  = Block.box(3.0, 0.0, 5.0, 13.0, 14.0, 8.0);

                private static final VoxelShape SHAPE_WALL_NORTH = Block.box(3.0, 1.0, 13.0, 13.0, 15.0, 16.0);
                private static final VoxelShape SHAPE_WALL_SOUTH = Block.box(3.0, 1.0, 0.0, 13.0, 15.0, 3.0);
                private static final VoxelShape SHAPE_WALL_EAST  = Block.box(0.0, 1.0, 3.0, 3.0, 15.0, 13.0);
                private static final VoxelShape SHAPE_WALL_WEST  = Block.box(13.0, 1.0, 3.0, 16.0, 15.0, 13.0);

                @Override
                protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                    builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.ATTACH_FACE, COLOR);
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
                            case NORTH:
                            default:    return SHAPE_WALL_NORTH;
                        }
                    } else {
                        switch (direction) {
                            case EAST:  return SHAPE_FLOOR_EAST;
                            case WEST:  return SHAPE_FLOOR_WEST;
                            case SOUTH: return SHAPE_FLOOR_SOUTH;
                            case NORTH:
                            default:    return SHAPE_FLOOR_NORTH;
                        }
                    }
                }
            });


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}