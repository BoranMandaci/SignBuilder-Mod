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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LetterBlock extends Block implements EntityBlock {

    public static final EnumProperty<SignMaterial> MATERIAL = EnumProperty.create("material", SignMaterial.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public static final VoxelShape BP_FLOOR_NORTH = Block.box(0, 0, 11, 16, 16, 12);
    public static final VoxelShape BP_FLOOR_SOUTH = Block.box(0, 0, 4, 16, 16, 5);
    public static final VoxelShape BP_FLOOR_EAST  = Block.box(4, 0, 0, 5, 16, 16);
    public static final VoxelShape BP_FLOOR_WEST  = Block.box(11, 0, 0, 12, 16, 16);

    public static final VoxelShape BP_WALL_NORTH = Block.box(0, 0, 15, 16, 16, 16);
    public static final VoxelShape BP_WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
    public static final VoxelShape BP_WALL_EAST  = Block.box(0, 0, 0, 1, 16, 16);
    public static final VoxelShape BP_WALL_WEST  = Block.box(15, 0, 0, 16, 16, 16);

    public LetterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(ModBlocks.GLOWING, false)
                .setValue(ModBlocks.LOW_POWER, false)
                .setValue(ModBlocks.COLOR, 0)
                .setValue(MATERIAL, SignMaterial.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, ModBlocks.GLOWING, ModBlocks.LOW_POWER, ModBlocks.COLOR, MATERIAL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof PaintBrushItem) {
            return InteractionResult.PASS;
        }

        if (held.is(ModBlocks.BACKPLATE_ITEM.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity letter) {
                if (player.isShiftKeyDown()) {
                    if (letter.hasBackplate()) {
                        if (!level.isClientSide()) {
                            ItemStack detached = BackplateBlock.getDroppedBackplateItemStack(letter);

                            letter.setHasBackplate(false);
                            letter.setBackplateFrontMaterial(SignMaterial.DEFAULT);
                            letter.setBackplateBackMaterial(SignMaterial.DEFAULT);
                            letter.setBackplateFrontColor(0xFFFFFF);
                            letter.setBackplateBackColor(0xFFFFFF);
                            letter.setBackplateFrontRainbow(false);
                            letter.setBackplateBackRainbow(false);
                            letter.setChanged();
                            letter.sync();
                            level.sendBlockUpdated(pos, state, state, 3);

                            if (!player.getInventory().add(detached)) {
                                player.drop(detached, false);
                            }
                            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                        } else {
                            dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                                    com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                            );
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                } else if (!letter.hasBackplate()) {
                    letter.setHasBackplate(true);
                    CompoundTag beTag = held.getTagElement("BlockEntityTag");
                    if (beTag != null) {
                        BackplateBlock.applyBackplateTagToEntity(letter, beTag);
                    }

                    if (!level.isClientSide()) {
                        if (!player.isCreative()) {
                            held.shrink(1);
                        }
                        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                        letter.setChanged();
                        letter.sync();
                        level.sendBlockUpdated(pos, state, state, 3);
                    } else {
                        dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                                com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                        );
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    public static VoxelShape calculateHitbox(BlockState state, BlockGetter level, BlockPos pos, VoxelShape letterShape) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity letter && letter.hasBackplate()) {
            AttachFace face = state.hasProperty(FACE) ? state.getValue(FACE) : AttachFace.WALL;
            Direction dir = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;

            if (face == AttachFace.WALL) {
                VoxelShape plateShape = switch (dir) {
                    case SOUTH -> BP_WALL_SOUTH;
                    case EAST  -> BP_WALL_EAST;
                    case WEST  -> BP_WALL_WEST;
                    default    -> BP_WALL_NORTH;
                };

                double offX = dir.getStepX() * 0.0625;
                double offZ = dir.getStepZ() * 0.0625;
                VoxelShape shiftedLetter = letterShape.move(offX, 0, offZ);

                return Shapes.or(shiftedLetter, plateShape);
            } else {
                Direction plateDir = dir.getCounterClockWise();
                VoxelShape plateShape = switch (plateDir) {
                    case SOUTH -> BP_FLOOR_SOUTH;
                    case EAST  -> BP_FLOOR_EAST;
                    case WEST  -> BP_FLOOR_WEST;
                    default    -> BP_FLOOR_NORTH;
                };
                return Shapes.or(letterShape, plateShape);
            }
        }
        return letterShape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return calculateHitbox(state, level, pos, super.getShape(state, level, pos, context));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity lbe) {
            CompoundTag beTag = stack.getTagElement("BlockEntityTag");
            if (beTag != null) {
                lbe.load(beTag);

                BlockState newState = state;
                if (beTag.contains("SavedMaterial") && state.hasProperty(MATERIAL)) {
                    try {
                        SignMaterial mat = SignMaterial.valueOf(beTag.getString("SavedMaterial"));
                        if (mat != SignMaterial.DEFAULT) {
                            newState = newState.setValue(MATERIAL, mat);
                        }
                    } catch (Exception ignored) {}
                }
                if (beTag.contains("ColorIndex") && newState.hasProperty(ModBlocks.COLOR)) {
                    newState = newState.setValue(ModBlocks.COLOR, beTag.getInt("ColorIndex"));
                }
                if (beTag.contains("Glowing") && newState.hasProperty(ModBlocks.GLOWING)) {
                    newState = newState.setValue(ModBlocks.GLOWING, beTag.getBoolean("Glowing"));
                }

                if (newState != state) {
                    level.setBlock(pos, newState, 3);
                }

                lbe.setChanged();
                if (!level.isClientSide()) {
                    lbe.sync();
                } else {
                    dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                            com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                    );
                }
            }
            level.sendBlockUpdated(pos, state, level.getBlockState(pos), 3);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);

        boolean hasSilkTouch = tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

        if (be instanceof LetterBlockEntity lbe) {
            if (hasSilkTouch) {
                ItemStack letterStack = lbe.getDroppedItemStack(state);
                CompoundTag beTag = letterStack.getTagElement("BlockEntityTag");
                if (beTag == null) {
                    beTag = new CompoundTag();
                    letterStack.addTagElement("BlockEntityTag", beTag);
                }

                int col = lbe.getRgbColor();
                if (col != 0 && col != 0xFFFFFF) {
                    beTag.putInt("RgbColor", col);
                }
                if (state.hasProperty(ModBlocks.COLOR)) {
                    beTag.putInt("ColorIndex", state.getValue(ModBlocks.COLOR));
                }
                if (lbe.isRainbow()) {
                    beTag.putBoolean("IsRainbow", true);
                }
                SignMaterial mat = state.hasProperty(MATERIAL) ? state.getValue(MATERIAL) : SignMaterial.DEFAULT;
                if (mat != SignMaterial.DEFAULT) {
                    beTag.putString("SavedMaterial", mat.name());
                }

                beTag.remove("HasBackplate");
                beTag.remove("hasBackplate");
                beTag.remove("BackplateFrontMaterial");
                beTag.remove("backplateFrontMaterial");
                beTag.remove("BackplateBackMaterial");
                beTag.remove("backplateBackMaterial");
                beTag.remove("BackplateFrontColor");
                beTag.remove("backplateFrontColor");
                beTag.remove("BackplateBackColor");
                beTag.remove("backplateBackColor");
                beTag.remove("BackplateFrontRainbow");
                beTag.remove("backplateFrontRainbow");
                beTag.remove("BackplateBackRainbow");
                beTag.remove("backplateBackRainbow");

                boolean isDefault = (mat == SignMaterial.DEFAULT)
                        && (col == 0 || col == 0xFFFFFF)
                        && !lbe.isRainbow()
                        && !lbe.isActive()
                        && lbe.getWrenchMode() == 0;

                if (isDefault) {
                    letterStack.removeTagKey("BlockEntityTag");
                    if (letterStack.getTag() != null && letterStack.getTag().isEmpty()) {
                        letterStack.setTag(null);
                    }
                }

                drops.add(letterStack);

                if (lbe.hasBackplate()) {
                    drops.add(BackplateBlock.getDroppedBackplateItemStack(lbe));
                }
            } else {
                drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));
                SignMaterial mat = state.hasProperty(MATERIAL) ? state.getValue(MATERIAL) : SignMaterial.DEFAULT;
                if (mat != SignMaterial.DEFAULT) {
                    ItemStack matStack = BackplateBlock.getItemForMaterial(mat);
                    if (!matStack.isEmpty()) drops.add(matStack);
                }
                if (state.hasProperty(ModBlocks.GLOWING) && state.getValue(ModBlocks.GLOWING)) {
                    drops.add(new ItemStack(Items.GLOWSTONE_DUST, 1));
                }

                if (lbe.hasBackplate()) {
                    drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));
                    SignMaterial fMat = lbe.getBackplateFrontMaterial();
                    SignMaterial bMat = lbe.getBackplateBackMaterial();

                    if (fMat != SignMaterial.DEFAULT) {
                        ItemStack fStack = BackplateBlock.getItemForMaterial(fMat);
                        if (!fStack.isEmpty()) drops.add(fStack);
                    }
                    if (bMat != SignMaterial.DEFAULT) {
                        ItemStack bStack = BackplateBlock.getItemForMaterial(bMat);
                        if (!bStack.isEmpty()) drops.add(bStack);
                    }
                }
            }
        } else {
            drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));
        }
        return drops;
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            boolean ignoreRedstone = be instanceof LetterBlockEntity lbe && (lbe.isActive() || lbe.getWrenchMode() != 0);
            if (!ignoreRedstone) {
                boolean hasSignal = pLevel.hasNeighborSignal(pPos);
                if (pState.hasProperty(ModBlocks.GLOWING) && hasSignal != pState.getValue(ModBlocks.GLOWING)) {
                    pLevel.setBlock(pPos, pState.setValue(ModBlocks.GLOWING, hasSignal), 3);
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