package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.item.PaintBrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LetterBlock extends Block implements EntityBlock {

    public static final IntegerProperty LIGHT_MODE = IntegerProperty.create("light_mode", 0, 2);
    public static final EnumProperty<SignMaterial> MATERIAL = EnumProperty.create("material", SignMaterial.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public static final VoxelShape BP_WALL_NORTH = Block.box(0, 0, 15, 16, 16, 16);
    public static final VoxelShape BP_WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
    public static final VoxelShape BP_WALL_EAST  = Block.box(0, 0, 0, 1, 16, 16);
    public static final VoxelShape BP_WALL_WEST  = Block.box(15, 0, 0, 16, 16, 16);

    public static final VoxelShape BP_2X2_WALL_NORTH = Block.box(0, 0, 14, 16, 16, 16);
    public static final VoxelShape BP_2X2_WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 2);
    public static final VoxelShape BP_2X2_WALL_EAST  = Block.box(0, 0, 0, 2, 16, 16);
    public static final VoxelShape BP_2X2_WALL_WEST  = Block.box(14, 0, 0, 16, 16, 16);

    public static final VoxelShape BP_FLOOR_NORTH = Block.box(0, 0, 11, 16, 16, 12);
    public static final VoxelShape BP_FLOOR_SOUTH = Block.box(0, 0, 4, 16, 16, 5);
    public static final VoxelShape BP_FLOOR_EAST  = Block.box(4, 0, 0, 5, 16, 16);
    public static final VoxelShape BP_FLOOR_WEST  = Block.box(11, 0, 0, 12, 16, 16);

    private static final VoxelShape SHAPE_6PX_WALL_NORTH = Block.box(0, 0, 10, 16, 16, 16);
    private static final VoxelShape SHAPE_6PX_WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 6);
    private static final VoxelShape SHAPE_6PX_WALL_EAST  = Block.box(0, 0, 0, 6, 16, 16);
    private static final VoxelShape SHAPE_6PX_WALL_WEST  = Block.box(10, 0, 0, 16, 16, 16);

    private static final VoxelShape SHAPE_2X2_FLOOR_NS = Block.box(0, 0, 5, 16, 16, 11);
    private static final VoxelShape SHAPE_2X2_FLOOR_EW = Block.box(5, 0, 0, 11, 16, 16);

    private static final VoxelShape BP_2X2_FLOOR_NORTH = Block.box(0, 0, 11, 16, 16, 13);
    private static final VoxelShape BP_2X2_FLOOR_SOUTH = Block.box(0, 0, 3, 16, 16, 5);
    private static final VoxelShape BP_2X2_FLOOR_EAST  = Block.box(3, 0, 0, 5, 16, 16);
    private static final VoxelShape BP_2X2_FLOOR_WEST  = Block.box(11, 0, 0, 13, 16, 16);

    public LetterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(MATERIAL, SignMaterial.DEFAULT)
                .setValue(LIGHT_MODE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, MATERIAL, LIGHT_MODE);
    }

    public static void updateLightLevel(Level level, BlockPos pos, BlockState state, LetterBlockEntity entity) {
        if (level.isClientSide()) return;

        int targetMode = 0;
        if (entity.isActive()) {
            targetMode = (entity.getWrenchMode() == 10) ? 1 : 2;
        }

        if (entity.isBig()) {
            BlockPos[] positions = getBigBlockPositions(entity.getBlockPos(), level.getBlockState(entity.getBlockPos()));
            for (BlockPos p : positions) {
                BlockState s = level.getBlockState(p);
                if (s.hasProperty(LIGHT_MODE) && s.getValue(LIGHT_MODE) != targetMode) {
                    level.setBlock(p, s.setValue(LIGHT_MODE, targetMode), 3);
                }
            }
        } else {
            if (state.hasProperty(LIGHT_MODE) && state.getValue(LIGHT_MODE) != targetMode) {
                level.setBlock(pos, state.setValue(LIGHT_MODE, targetMode), 3);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return ModBlocks.getStandardPlacementState(context, this.defaultBlockState());
    }

    public static VoxelShape calculateHitbox(BlockState state, BlockGetter level, BlockPos pos, VoxelShape letterShape) {
        BlockEntity be = level.getBlockEntity(pos);
        AttachFace face = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
        Direction dir = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;

        if (be instanceof LetterBlockEntity rawLetter) {
            LetterBlockEntity master = findMaster(level, pos, rawLetter);
            if (master.isBig()) {
                if (face == AttachFace.WALL) {
                    VoxelShape base6pxShape = switch (dir) {
                        case SOUTH -> SHAPE_6PX_WALL_SOUTH;
                        case EAST  -> SHAPE_6PX_WALL_EAST;
                        case WEST  -> SHAPE_6PX_WALL_WEST;
                        default    -> SHAPE_6PX_WALL_NORTH;
                    };

                    if (master.hasBackplate()) {
                        VoxelShape plateShape = switch (dir) {
                            case SOUTH -> BP_2X2_WALL_SOUTH;
                            case EAST  -> BP_2X2_WALL_EAST;
                            case WEST  -> BP_2X2_WALL_WEST;
                            default    -> BP_2X2_WALL_NORTH;
                        };
                        double offX = dir.getStepX() * 0.125;
                        double offZ = dir.getStepZ() * 0.125;
                        return Shapes.or(base6pxShape.move(offX, 0, offZ), plateShape);
                    }
                    return base6pxShape;
                } else {
                    VoxelShape baseFloorShape = (dir == Direction.NORTH || dir == Direction.SOUTH)
                            ? SHAPE_2X2_FLOOR_EW
                            : SHAPE_2X2_FLOOR_NS;

                    if (master.hasBackplate()) {
                        VoxelShape plateFloorShape = switch (dir) {
                            case EAST  -> BP_2X2_FLOOR_NORTH;
                            case WEST  -> BP_2X2_FLOOR_SOUTH;
                            case SOUTH -> BP_2X2_FLOOR_EAST;
                            default    -> BP_2X2_FLOOR_WEST;
                        };
                        return Shapes.or(baseFloorShape, plateFloorShape);
                    }
                    return baseFloorShape;
                }
            }

            if (master.hasBackplate()) {
                if (face == AttachFace.WALL) {
                    VoxelShape plateShape = switch (dir) {
                        case SOUTH -> BP_WALL_SOUTH;
                        case EAST  -> BP_WALL_EAST;
                        case WEST  -> BP_WALL_WEST;
                        default    -> BP_WALL_NORTH;
                    };
                    double offX = dir.getStepX() * 0.0625;
                    double offZ = dir.getStepZ() * 0.0625;
                    return Shapes.or(letterShape.move(offX, 0, offZ), plateShape);
                } else {
                    VoxelShape plateShape = switch (dir) {
                        case EAST  -> BP_FLOOR_NORTH;
                        case WEST  -> BP_FLOOR_SOUTH;
                        case SOUTH -> BP_FLOOR_EAST;
                        default    -> BP_FLOOR_WEST;
                    };
                    return Shapes.or(letterShape, plateShape);
                }
            }
        }
        return letterShape;
    }

    public static LetterBlockEntity findMaster(BlockGetter level, BlockPos pos, LetterBlockEntity entity) {
        if (entity.isBig()) {
            return entity;
        }
        if (!entity.isDummy()) {
            return entity;
        }

        if (entity.getMasterPos() != null) {
            BlockEntity be = level.getBlockEntity(entity.getMasterPos());
            if (be instanceof LetterBlockEntity master && master.isBig() && !master.isDummy()) {
                return master;
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(p);
                    if (be instanceof LetterBlockEntity lbe && lbe.isBig() && !lbe.isDummy()) {
                        BlockState mState = level.getBlockState(p);
                        if (mState.hasProperty(FACING)) {
                            BlockPos[] bigPositions = getBigBlockPositions(p, mState);
                            for (BlockPos bp : bigPositions) {
                                if (bp.equals(pos)) {
                                    entity.setMasterPos(p);
                                    return lbe;
                                }
                            }
                        }
                    }
                }
            }
        }
        return entity;
    }

    public static BlockPos[] getBigBlockPositions(BlockPos masterPos, BlockState masterState) {
        Direction facing = masterState.getValue(FACING);
        Direction right = facing.getCounterClockWise();

        return new BlockPos[] {
                masterPos,
                masterPos.relative(right, 1),
                masterPos.relative(Direction.UP, 1),
                masterPos.relative(right, 1).relative(Direction.UP, 1)
        };
    }

    public static void updateAllEntitiesBackplate(Level level, BlockPos masterPos, BlockState masterState, boolean is2x2, boolean hasBackplate, SignMaterial fMat, SignMaterial bMat, int fCol, int bCol, boolean fRain, boolean bRain) {
        BlockPos[] targets = is2x2 ? getBigBlockPositions(masterPos, masterState) : new BlockPos[] { masterPos };
        for (BlockPos p : targets) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof LetterBlockEntity lbe) {
                lbe.setHasBackplate(hasBackplate);
                lbe.setBackplateFrontMaterial(fMat);
                lbe.setBackplateBackMaterial(bMat);
                lbe.setBackplateFrontColor(fCol);
                lbe.setBackplateBackColor(bCol);
                lbe.setBackplateFrontRainbow(fRain);
                lbe.setBackplateBackRainbow(bRain);
                lbe.setChanged();
                lbe.sync();
                BlockState s = level.getBlockState(p);
                level.sendBlockUpdated(p, s, s, 3);
            }
        }
    }

    public static boolean tryDetachBackplate(Level level, BlockPos clickedPos, Player player) {
        BlockEntity rawBe = level.getBlockEntity(clickedPos);
        if (!(rawBe instanceof LetterBlockEntity rawLetter)) return false;

        LetterBlockEntity master = findMaster(level, clickedPos, rawLetter);
        if (!master.hasBackplate()) return false;

        BlockPos mPos = master.getBlockPos();
        BlockState mState = level.getBlockState(mPos);
        boolean is2x2 = master.isBig();
        int cost = is2x2 ? 4 : 1;

        if (!level.isClientSide()) {
            ItemStack droppedItem = BackplateBlock.getDroppedBackplateItemStack(master);

            updateAllEntitiesBackplate(level, mPos, mState, is2x2, false, SignMaterial.DEFAULT, SignMaterial.DEFAULT, 0xFFFFFF, 0xFFFFFF, false, false);

            if (!player.isCreative()) {
                ItemStack refund = droppedItem.copy();
                refund.setCount(cost);
                if (!player.getInventory().add(refund)) {
                    player.drop(refund, false);
                }
            }

            level.playSound(null, clickedPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                com.boran.signbuilder.client.ClientHooks.setBlocksDirty(mPos);
                if (is2x2) {
                    for (BlockPos p : getBigBlockPositions(mPos, mState)) {
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                    }
                }
            });
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PaintBrushItem) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (tryDetachBackplate(level, pos, player)) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.PASS;
        }

        BlockEntity rawBe = level.getBlockEntity(pos);
        if (!(rawBe instanceof LetterBlockEntity rawLetter)) {
            return super.use(state, level, pos, player, hand, hit);
        }

        LetterBlockEntity master = findMaster(level, pos, rawLetter);
        BlockPos mPos = master.getBlockPos();
        BlockState mState = level.getBlockState(mPos);
        boolean is2x2 = master.isBig();
        int cost = is2x2 ? 4 : 1;

        if (held.is(ModBlocks.BACKPLATE_ITEM.get())) {
            if (master.hasBackplate()) {
                return InteractionResult.PASS;
            }

            if (!player.isCreative() && held.getCount() < cost) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                CompoundTag beTag = held.getTagElement("BlockEntityTag");
                LetterBlockEntity temp = new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), mPos, mState);
                if (beTag != null) {
                    BackplateBlock.applyBackplateTagToEntity(temp, beTag);
                }

                SignMaterial fMat = temp.getBackplateFrontMaterial();
                SignMaterial bMat = temp.getBackplateBackMaterial();
                int fCol = temp.getBackplateFrontColor();
                int bCol = temp.getBackplateBackColor();
                boolean fRain = temp.isBackplateFrontRainbow();
                boolean bRain = temp.isBackplateBackRainbow();

                updateAllEntitiesBackplate(level, mPos, mState, is2x2, true, fMat, bMat, fCol, bCol, fRain, bRain);

                if (!player.isCreative()) {
                    held.shrink(cost);
                }

                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                    com.boran.signbuilder.client.ClientHooks.setBlocksDirty(mPos);
                    if (is2x2) {
                        for (BlockPos p : getBigBlockPositions(mPos, mState)) {
                            com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                        }
                    }
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity lbe) {
            CompoundTag beTag = stack.getTagElement("BlockEntityTag");
            if (beTag != null) {
                lbe.load(beTag);
            }
            lbe.setBig(false);
            lbe.setDummy(false);
            lbe.setMasterPos(null);
            lbe.setChanged();
            if (!level.isClientSide()) {
                lbe.sync();
                updateLightLevel(level, pos, state, lbe);
            } else {
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                );
            }
            level.sendBlockUpdated(pos, state, level.getBlockState(pos), 3);
        }

        if (!level.isClientSide()) {
            checkAndForm2x2(level, pos, state);
        }
    }

    private void checkAndForm2x2(Level level, BlockPos placedPos, BlockState placedState) {
        Direction facing = placedState.getValue(FACING);
        Direction rightDir = facing.getCounterClockWise();

        if (scanAxis(level, placedPos, placedState.getBlock(), rightDir, facing)) return;
        if (scanAxis(level, placedPos, placedState.getBlock(), facing.getClockWise(), facing)) return;
        if (scanAxis(level, placedPos, placedState.getBlock(), Direction.EAST, facing)) return;
        scanAxis(level, placedPos, placedState.getBlock(), Direction.SOUTH, facing);
    }

    private boolean scanAxis(Level level, BlockPos placedPos, Block blockType, Direction horizontalStep, Direction finalFacing) {
        Direction upStep = Direction.UP;
        int[][] cornerOffsets = { {0, 0}, {-1, 0}, {0, -1}, {-1, -1} };

        for (int[] offset : cornerOffsets) {
            BlockPos b00 = placedPos.relative(horizontalStep, offset[0]).relative(upStep, offset[1]);
            BlockPos b10 = b00.relative(horizontalStep, 1);
            BlockPos b01 = b00.relative(upStep, 1);
            BlockPos b11 = b00.relative(horizontalStep, 1).relative(upStep, 1);

            if (tryMerge4(level, b00, b10, b01, b11, blockType, finalFacing)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryMerge4(Level level, BlockPos b00, BlockPos b10, BlockPos b01, BlockPos b11, Block expectedBlock, Direction finalFacing) {
        if (isValidSingle(level, b00, expectedBlock) &&
                isValidSingle(level, b10, expectedBlock) &&
                isValidSingle(level, b01, expectedBlock) &&
                isValidSingle(level, b11, expectedBlock)) {

            LetterBlockEntity master = (LetterBlockEntity) level.getBlockEntity(b00);
            LetterBlockEntity e10 = (LetterBlockEntity) level.getBlockEntity(b10);
            LetterBlockEntity e01 = (LetterBlockEntity) level.getBlockEntity(b01);
            LetterBlockEntity e11 = (LetterBlockEntity) level.getBlockEntity(b11);

            if (master != null && e10 != null && e01 != null && e11 != null) {
                BlockState mState = level.getBlockState(b00);
                AttachFace commonFace = mState.getValue(FACE);

                boolean hasAnyBackplate = master.hasBackplate() || e10.hasBackplate() || e01.hasBackplate() || e11.hasBackplate();
                SignMaterial fMat = master.hasBackplate() ? master.getBackplateFrontMaterial() : (e10.hasBackplate() ? e10.getBackplateFrontMaterial() : (e01.hasBackplate() ? e01.getBackplateFrontMaterial() : e11.getBackplateFrontMaterial()));
                SignMaterial bMat = master.hasBackplate() ? master.getBackplateBackMaterial() : (e10.hasBackplate() ? e10.getBackplateBackMaterial() : (e01.hasBackplate() ? e01.getBackplateBackMaterial() : e11.getBackplateBackMaterial()));
                int fCol = master.hasBackplate() ? master.getBackplateFrontColor() : (e10.hasBackplate() ? e10.getBackplateFrontColor() : 0xFFFFFF);
                int bCol = master.hasBackplate() ? master.getBackplateBackColor() : (e10.hasBackplate() ? e10.getBackplateBackColor() : 0xFFFFFF);
                boolean fRain = master.hasBackplate() ? master.isBackplateFrontRainbow() : e10.isBackplateFrontRainbow();
                boolean bRain = master.hasBackplate() ? master.isBackplateBackRainbow() : e10.isBackplateBackRainbow();

                level.setBlock(b00, level.getBlockState(b00).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
                level.setBlock(b10, level.getBlockState(b10).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
                level.setBlock(b01, level.getBlockState(b01).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);
                level.setBlock(b11, level.getBlockState(b11).setValue(FACING, finalFacing).setValue(FACE, commonFace), 3);

                LetterBlockEntity actualMaster = (LetterBlockEntity) level.getBlockEntity(b00);
                LetterBlockEntity actual10 = (LetterBlockEntity) level.getBlockEntity(b10);
                LetterBlockEntity actual01 = (LetterBlockEntity) level.getBlockEntity(b01);
                LetterBlockEntity actual11 = (LetterBlockEntity) level.getBlockEntity(b11);

                if (actualMaster != null && actual10 != null && actual01 != null && actual11 != null) {
                    actualMaster.setBig(true);
                    actualMaster.setDummy(false);
                    actualMaster.setMasterPos(null);
                    actualMaster.setChanged();
                    actualMaster.sync();

                    setupDummy(actual10, b00);
                    setupDummy(actual01, b00);
                    setupDummy(actual11, b00);

                    if (hasAnyBackplate) {
                        updateAllEntitiesBackplate(level, b00, mState, true, true, fMat, bMat, fCol, bCol, fRain, bRain);
                    }

                    updateLightLevel(level, b00, level.getBlockState(b00), actualMaster);

                    level.sendBlockUpdated(b00, level.getBlockState(b00), level.getBlockState(b00), 3);
                    level.sendBlockUpdated(b10, level.getBlockState(b10), level.getBlockState(b10), 3);
                    level.sendBlockUpdated(b01, level.getBlockState(b01), level.getBlockState(b01), 3);
                    level.sendBlockUpdated(b11, level.getBlockState(b11), level.getBlockState(b11), 3);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidSingle(Level level, BlockPos pos, Block expectedBlock) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() != expectedBlock) return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LetterBlockEntity lbe)) return false;

        return !lbe.isBig() && !lbe.isDummy();
    }

    private void setupDummy(LetterBlockEntity dummy, BlockPos masterPos) {
        dummy.setDummy(true);
        dummy.setBig(false);
        dummy.setMasterPos(masterPos);
        dummy.setChanged();
        dummy.sync();
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity rawLbe) {
                LetterBlockEntity master = findMaster(level, pos, rawLbe);
                BlockPos mPos = master.getBlockPos();
                BlockState mState = level.getBlockState(mPos);

                if (master.isBig()) {
                    if (!player.isCreative()) {
                        spawnLetterDrops(level, mPos, mState, master, 4, player.getMainHandItem());
                    }
                    destroyAll4Blocks(level, mPos, mState, pos);
                } else if (!rawLbe.isDummy()) {
                    if (!player.isCreative()) {
                        spawnLetterDrops(level, pos, state, rawLbe, 1, player.getMainHandItem());
                    }
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private static void destroyAll4Blocks(Level level, BlockPos masterPos, BlockState masterState, BlockPos triggeredPos) {
        BlockPos[] positions = getBigBlockPositions(masterPos, masterState);

        for (BlockPos p : positions) {
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof LetterBlockEntity lbe) {
                lbe.setBig(false);
                lbe.setDummy(false);
                lbe.setMasterPos(null);
                lbe.setChanged();
            }
            if (!p.equals(triggeredPos)) {
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = masterPos.offset(dx, dy, dz);
                    if (!check.equals(triggeredPos)) {
                        BlockEntity be = level.getBlockEntity(check);
                        if (be instanceof LetterBlockEntity lbe && lbe.isDummy() && masterPos.equals(lbe.getMasterPos())) {
                            lbe.setBig(false);
                            lbe.setDummy(false);
                            lbe.setMasterPos(null);
                            lbe.setChanged();
                            level.setBlock(check, Blocks.AIR.defaultBlockState(), 35);
                        }
                    }
                }
            }
        }
    }

    private void spawnLetterDrops(Level level, BlockPos dropPos, BlockState dropState, LetterBlockEntity lbe, int multiplier, ItemStack tool) {
        boolean hasSilkTouch = !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

        if (hasSilkTouch) {
            ItemStack letterStack = lbe.getDroppedItemStack(dropState);
            letterStack.setCount(multiplier);
            popResource(level, dropPos, letterStack);

            if (lbe.hasBackplate()) {
                ItemStack bp = BackplateBlock.getDroppedBackplateItemStack(lbe);
                bp.setCount(multiplier);
                popResource(level, dropPos, bp);
            }
        } else {
            popResource(level, dropPos, new ItemStack(Blocks.WHITE_CONCRETE, 3 * multiplier));
            SignMaterial mat = dropState.hasProperty(MATERIAL) ? dropState.getValue(MATERIAL) : lbe.getSavedMaterial();
            if (mat != SignMaterial.DEFAULT) {
                ItemStack matStack = BackplateBlock.getItemForMaterial(mat);
                if (!matStack.isEmpty()) {
                    matStack.setCount(multiplier);
                    popResource(level, dropPos, matStack);
                }
            }

            if (lbe.isActive()) {
                popResource(level, dropPos, new ItemStack(Items.GLOWSTONE_DUST, multiplier));
            }

            if (lbe.hasBackplate()) {
                popResource(level, dropPos, new ItemStack(Blocks.WHITE_CONCRETE, 3 * multiplier));
                SignMaterial fMat = lbe.getBackplateFrontMaterial();
                SignMaterial bMat = lbe.getBackplateBackMaterial();

                if (fMat != SignMaterial.DEFAULT) {
                    ItemStack fStack = BackplateBlock.getItemForMaterial(fMat);
                    if (!fStack.isEmpty()) {
                        fStack.setCount(multiplier);
                        popResource(level, dropPos, fStack);
                    }
                }
                if (bMat != SignMaterial.DEFAULT) {
                    ItemStack bStack = BackplateBlock.getItemForMaterial(bMat);
                    if (!bStack.isEmpty()) {
                        bStack.setCount(multiplier);
                        popResource(level, dropPos, bStack);
                    }
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity lbe) {
                if (lbe.isBig()) {
                    destroyAll4Blocks(level, pos, state, pos);
                } else if (lbe.isDummy() && lbe.getMasterPos() != null) {
                    BlockPos mPos = lbe.getMasterPos();
                    BlockEntity mBe = level.getBlockEntity(mPos);
                    if (mBe instanceof LetterBlockEntity masterLbe && masterLbe.isBig()) {
                        destroyAll4Blocks(level, mPos, level.getBlockState(mPos), pos);
                    }
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

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof LetterBlockEntity lbe) {
                LetterBlockEntity master = findMaster(pLevel, pPos, lbe);
                boolean ignoreRedstone = master.isActive() || master.getWrenchMode() != 0;
                if (!ignoreRedstone) {
                    boolean hasSignal = pLevel.hasNeighborSignal(pPos);
                    if (master.isActive() != hasSignal) {
                        master.setActive(hasSignal);
                        master.setChanged();
                        master.sync();
                        updateLightLevel(pLevel, master.getBlockPos(), pLevel.getBlockState(master.getBlockPos()), master);
                        pLevel.sendBlockUpdated(master.getBlockPos(), pLevel.getBlockState(master.getBlockPos()), pLevel.getBlockState(master.getBlockPos()), 3);
                    }
                }
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.LETTER_BLOCK_ENTITY.get()) {
            return (l, p, s, entity) -> LetterBlockEntity.tick(l, p, s, (LetterBlockEntity) entity);
        }
        return null;
    }
}