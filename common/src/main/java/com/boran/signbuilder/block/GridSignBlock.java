package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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

    public static int getBackOffset(int gridSize) {
        return gridSize >= 5 ? 32 : 16;
    }

    public static boolean isBackIndex(int rawIndex, int gridSize) {
        return (rawIndex >= 32) || (gridSize < 5 && rawIndex >= 16);
    }

    public static int getLocalIndex(int rawIndex, int gridSize) {
        return isBackIndex(rawIndex, gridSize) ? (rawIndex >= 32 ? rawIndex - 32 : rawIndex - 16) : rawIndex;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GridSignBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
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

    public record GridHitRay(double u, double v, boolean isBack) {}

    public static GridHitRay calculateHitRay(Level level, BlockPos pos, BlockState state, GridSignBlockEntity gridBe, @Nullable Player player, BlockHitResult hit) {
        Direction face = hit.getDirection();
        Vec3 loc = hit.getLocation();
        double rx = loc.x - pos.getX();
        double ry = loc.y - pos.getY();
        double rz = loc.z - pos.getZ();

        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : face;
        int rotation = gridBe.getFacingRotation();
        AttachFace aFace = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;

        boolean isBack = false;
        if (aFace == AttachFace.WALL) {
            Direction blockFacing = state.hasProperty(FACING) ? state.getValue(FACING) : face;
            if (face == blockFacing.getOpposite()) {
                isBack = true;
            } else if (face == blockFacing) {
                isBack = false;
            } else if (player != null) {
                int rot = SignRotation.fromDirection(blockFacing);
                double side = (player.getX() - (pos.getX() + 0.5)) * SignRotation.facingX(rot)
                        + (player.getZ() - (pos.getZ() + 0.5)) * SignRotation.facingZ(rot);
                isBack = side < 0.0;
            }
        } else {
            Direction frontDirection = SignRotation.cardinalDirection(rotation);
            if (frontDirection != null && face == frontDirection.getOpposite()) {
                isBack = true;
            } else if (frontDirection != null && face == frontDirection) {
                isBack = false;
            } else if (player != null) {
                Vec3 look = player.getLookAngle();
                double dot = look.x * SignRotation.facingX(rotation) + look.z * SignRotation.facingZ(rotation);
                if (dot > 0.05) {
                    isBack = true;
                } else if (dot < -0.05) {
                    isBack = false;
                } else {
                    double side = (player.getX() - (pos.getX() + 0.5)) * SignRotation.facingX(rotation)
                            + (player.getZ() - (pos.getZ() + 0.5)) * SignRotation.facingZ(rotation);
                    isBack = side < 0.0;
                }
            }
        }

        int rotationDegrees = SignRotation.deltaDegrees(rotation, facing);
        double radians = Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double dx = rx - 0.5;
        double dz = rz - 0.5;
        double localX = dx * cos - dz * sin;
        double localZ = dx * sin + dz * cos;

        double widthFactor = (rotationDegrees != 0 && aFace != AttachFace.WALL) ? Math.sqrt(2.0) : 1.0;

        Direction effectiveFacing = isBack ? facing.getOpposite() : facing;
        double u = switch (effectiveFacing) {
            case NORTH -> 0.5 - (localX / widthFactor);
            case SOUTH -> 0.5 + (localX / widthFactor);
            case EAST  -> 0.5 - (localZ / widthFactor);
            case WEST  -> 0.5 + (localZ / widthFactor);
            default    -> 0.5;
        };
        double v = 1.0 - Math.max(0.0, Math.min(1.0, ry));
        u = Math.max(0.0, Math.min(1.0, u));
        v = Math.max(0.0, Math.min(1.0, v));

        return new GridHitRay(u, v, isBack);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.is(Items.GLOW_INK_SAC) || held.is(Items.INK_SAC)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GridSignBlockEntity gridBe) {
                GridHitRay ray = calculateHitRay(level, pos, state, gridBe, player, hit);
                GridSignBlockEntity.CellData cell = gridBe.getCellAt((float) ray.u(), (float) ray.v(), ray.isBack());
                boolean shiftDown = player.isShiftKeyDown();
                boolean targetBackGlyph = ray.isBack() && !gridBe.getBackGlyph().isEmpty() && !shiftDown;
                boolean targetFrontGlyph = !ray.isBack() && !gridBe.getFrontGlyph().isEmpty() && !shiftDown;

                boolean targetBackplate = false;
                if (gridBe.hasBackplate()) {
                    if (shiftDown) {
                        targetBackplate = true;
                    } else if (cell == null && !targetBackGlyph && !targetFrontGlyph) {
                        targetBackplate = true;
                    }
                }

                if (held.is(Items.GLOW_INK_SAC)) {
                    if (targetBackplate) {
                        boolean currentGlow = ray.isBack() ? gridBe.isBackplateBackGlowInked() : gridBe.isBackplateFrontGlowInked();
                        if (!currentGlow) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                if (ray.isBack()) {
                                    gridBe.setBackplateBackGlowInked(true);
                                } else {
                                    gridBe.setBackplateFrontGlowInked(true);
                                }
                                if (!player.isCreative()) held.shrink(1);
                                level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    } else if (targetBackGlyph) {
                        if (!gridBe.isBackGlyphGlowInked()) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                gridBe.setBackGlyphGlowInked(true);
                                if (!player.isCreative()) held.shrink(1);
                                level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    } else if (targetFrontGlyph) {
                        if (!gridBe.isFrontGlyphGlowInked()) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                gridBe.setFrontGlyphGlowInked(true);
                                if (!player.isCreative()) held.shrink(1);
                                level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    } else if (cell != null) {
                        if (!cell.glowInked) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                cell.glowInked = true;
                                gridBe.markRenderDirty();
                                gridBe.setChanged();
                                gridBe.sync();
                                if (!player.isCreative()) held.shrink(1);
                                level.playSound(null, pos, SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                } else {
                    if (targetBackplate) {
                        boolean currentGlow = ray.isBack() ? gridBe.isBackplateBackGlowInked() : gridBe.isBackplateFrontGlowInked();
                        if (currentGlow) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                if (ray.isBack()) {
                                    gridBe.setBackplateBackGlowInked(false);
                                } else {
                                    gridBe.setBackplateFrontGlowInked(false);
                                }
                                if (!player.isCreative()) {
                                    held.shrink(1);
                                    ItemStack refund = new ItemStack(Items.GLOW_INK_SAC, 1);
                                    if (!player.getInventory().add(refund)) {
                                        player.drop(refund, false);
                                    }
                                }
                                level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    } else if (targetBackGlyph) {
                        if (gridBe.isBackGlyphGlowInked()) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                gridBe.setBackGlyphGlowInked(false);
                                if (!player.isCreative()) {
                                    held.shrink(1);
                                    ItemStack refund = new ItemStack(Items.GLOW_INK_SAC, 1);
                                    if (!player.getInventory().add(refund)) {
                                        player.drop(refund, false);
                                    }
                                }
                                level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    } else if (targetFrontGlyph) {
                        if (gridBe.isFrontGlyphGlowInked()) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                gridBe.setFrontGlyphGlowInked(false);
                                if (!player.isCreative()) {
                                    held.shrink(1);
                                    ItemStack refund = new ItemStack(Items.GLOW_INK_SAC, 1);
                                    if (!player.getInventory().add(refund)) {
                                        player.drop(refund, false);
                                    }
                                }
                                level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    } else if (cell != null) {
                        if (cell.glowInked) {
                            if (!player.isCreative() && held.getCount() < 1) {
                                return InteractionResult.FAIL;
                            }
                            if (!level.isClientSide()) {
                                cell.glowInked = false;
                                gridBe.markRenderDirty();
                                gridBe.setChanged();
                                gridBe.sync();
                                if (!player.isCreative()) {
                                    held.shrink(1);
                                    ItemStack refund = new ItemStack(Items.GLOW_INK_SAC, 1);
                                    if (!player.getInventory().add(refund)) {
                                        player.drop(refund, false);
                                    }
                                }
                                level.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        if (held.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
            boolean isGridMode = level.isClientSide()
                    ? dev.architectury.utils.EnvExecutor.getInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.isGridModeActive()).orElse(false)
                    : com.boran.signbuilder.server.ServerGridModeState.getGridSize(player.getUUID()) > 1;
            if (isGridMode) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GridSignBlockEntity gridBe && gridBe.hasBackplate()) {
                boolean isBack = false;
                AttachFace aFace = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
                if (aFace == AttachFace.WALL) {
                    Direction blockFacing = state.hasProperty(FACING) ? state.getValue(FACING) : hit.getDirection();
                    Direction face = hit.getDirection();
                    if (face == blockFacing.getOpposite()) {
                        isBack = true;
                    } else if (face == blockFacing) {
                        isBack = false;
                    } else if (player != null) {
                        int rot = SignRotation.fromDirection(blockFacing);
                        double side = (player.getX() - (pos.getX() + 0.5)) * SignRotation.facingX(rot)
                                + (player.getZ() - (pos.getZ() + 0.5)) * SignRotation.facingZ(rot);
                        isBack = side < 0.0;
                    }
                } else {
                    int rot = gridBe.getFacingRotation();
                    Direction frontDirection = SignRotation.cardinalDirection(rot);
                    Direction face = hit.getDirection();
                    if (frontDirection != null && face == frontDirection) {
                        isBack = false;
                    } else if (frontDirection != null && face == frontDirection.getOpposite()) {
                        isBack = true;
                    } else if (player != null) {
                        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                        double dot = look.x * SignRotation.facingX(rot) + look.z * SignRotation.facingZ(rot);
                        if (dot > 0.05) {
                            isBack = true;
                        } else if (dot < -0.05) {
                            isBack = false;
                        } else {
                            double side = (player.getX() - (pos.getX() + 0.5)) * SignRotation.facingX(rot)
                                    + (player.getZ() - (pos.getZ() + 0.5)) * SignRotation.facingZ(rot);
                            isBack = side < 0.0;
                        }
                    }
                }

                boolean hasBackGridCells = false;
                boolean hasFrontGridCells = false;
                for (GridSignBlockEntity.CellData cell : gridBe.getCells().values()) {
                    if (cell.isBack) {
                        hasBackGridCells = true;
                    } else {
                        hasFrontGridCells = true;
                    }
                }

                if (isBack) {
                    if (!hasBackGridCells && gridBe.getBackGlyph().isEmpty()) {
                        if (!level.isClientSide()) {
                            gridBe.setBackGlyph(held);
                            if (!player.isCreative()) {
                                held.shrink(1);
                            }
                            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.WOOD_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                } else {
                    if (!hasFrontGridCells && gridBe.getFrontGlyph().isEmpty()) {
                        if (!level.isClientSide()) {
                            gridBe.setFrontGlyph(held);
                            if (!player.isCreative()) {
                                held.shrink(1);
                            }
                            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.WOOD_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.use(state, level, pos, player, hand, hit);
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
            if (gridBe.getBackplateFrontMaterial() != SignMaterial.DEFAULT) {
                ItemStack fStack = BackplateBlock.getItemForMaterial(gridBe.getBackplateFrontMaterial());
                if (!fStack.isEmpty()) Block.popResource(level, pos, fStack);
            }
            if (gridBe.getBackplateBackMaterial() != SignMaterial.DEFAULT) {
                ItemStack bStack = BackplateBlock.getItemForMaterial(gridBe.getBackplateBackMaterial());
                if (!bStack.isEmpty()) Block.popResource(level, pos, bStack);
            }
            if (gridBe.isBackplateFrontGlowInked()) {
                Block.popResource(level, pos, new ItemStack(Items.GLOW_INK_SAC));
            }
            if (gridBe.isBackplateBackGlowInked()) {
                Block.popResource(level, pos, new ItemStack(Items.GLOW_INK_SAC));
            }
        }
        for (GridSignBlockEntity.CellData cell : gridBe.getCells().values()) {
            Block letterBlock = Registry.BLOCK.get(new ResourceLocation("signbuilder", cell.character));
            if (letterBlock != null && letterBlock != Blocks.AIR) {
                Block.popResource(level, pos, new ItemStack(letterBlock));
            }
            if (cell.material != null && cell.material != SignMaterial.DEFAULT) {
                ItemStack matStack = BackplateBlock.getItemForMaterial(cell.material);
                if (!matStack.isEmpty()) {
                    Block.popResource(level, pos, matStack);
                }
            }
            if (cell.glowInked) {
                Block.popResource(level, pos, new ItemStack(Items.GLOW_INK_SAC));
            }
        }
        if (!gridBe.getBackGlyph().isEmpty()) {
            Block.popResource(level, pos, gridBe.getBackGlyph().copy());
            if (gridBe.isBackGlyphGlowInked()) {
                Block.popResource(level, pos, new ItemStack(Items.GLOW_INK_SAC));
            }
        }
        if (!gridBe.getFrontGlyph().isEmpty()) {
            Block.popResource(level, pos, gridBe.getFrontGlyph().copy());
            if (gridBe.isFrontGlyphGlowInked()) {
                Block.popResource(level, pos, new ItemStack(Items.GLOW_INK_SAC));
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
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
        return List.of();
    }
}
