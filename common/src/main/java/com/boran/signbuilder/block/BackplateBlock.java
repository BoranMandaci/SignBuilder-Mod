package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.item.PaintBrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BackplateBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final EnumProperty<SignMaterial> MATERIAL = EnumProperty.create("material", SignMaterial.class);

    private static final VoxelShape WALL_NORTH = Block.box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape WALL_SOUTH = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape WALL_EAST  = Block.box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape WALL_WEST  = Block.box(15, 0, 0, 16, 16, 16);

    private static final VoxelShape FLOOR_NORTH = Block.box(0, 0, 11, 16, 16, 12);
    private static final VoxelShape FLOOR_SOUTH = Block.box(0, 0, 4, 16, 16, 5);
    private static final VoxelShape FLOOR_EAST  = Block.box(4, 0, 0, 5, 16, 16);
    private static final VoxelShape FLOOR_WEST  = Block.box(11, 0, 0, 12, 16, 16);

    public BackplateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(MATERIAL, SignMaterial.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, MATERIAL);
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

        if (held.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
            BlockEntity be = level.getBlockEntity(pos);
            LetterBlockEntity letterBe = (be instanceof LetterBlockEntity lbe) ? lbe : null;

            SignMaterial fMat = letterBe != null ? letterBe.getBackplateFrontMaterial() : SignMaterial.DEFAULT;
            SignMaterial bMat = letterBe != null ? letterBe.getBackplateBackMaterial() : SignMaterial.DEFAULT;
            if (fMat == SignMaterial.DEFAULT && state.hasProperty(MATERIAL)) {
                fMat = state.getValue(MATERIAL);
            }

            int fColor = letterBe != null ? letterBe.getBackplateFrontColor() : 0xFFFFFF;
            int bColor = letterBe != null ? letterBe.getBackplateBackColor() : 0xFFFFFF;
            boolean fRainbow = letterBe != null && letterBe.isBackplateFrontRainbow();
            boolean bRainbow = letterBe != null && letterBe.isBackplateBackRainbow();

            AttachFace face = state.getValue(FACE);
            Direction facing = state.getValue(FACING);
            Direction letterFacing = (face == AttachFace.FLOOR) ? facing.getClockWise() : facing;

            BlockState newLetterState = letterBlock.defaultBlockState()
                    .setValue(LetterBlock.FACING, letterFacing)
                    .setValue(LetterBlock.FACE, face);

            if (!level.isClientSide()) {
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                level.setBlock(pos, newLetterState, 3);
                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe instanceof LetterBlockEntity newLetter) {
                    CompoundTag blockEntityTag = held.getTagElement("BlockEntityTag");
                    if (blockEntityTag != null) {
                        newLetter.load(blockEntityTag);
                    }
                    newLetter.setHasBackplate(true);
                    newLetter.setBackplateFrontMaterial(fMat);
                    newLetter.setBackplateBackMaterial(bMat);
                    newLetter.setBackplateFrontColor(fColor);
                    newLetter.setBackplateBackColor(bColor);
                    newLetter.setBackplateFrontRainbow(fRainbow);
                    newLetter.setBackplateBackRainbow(bRainbow);
                    newLetter.setChanged();
                    newLetter.sync();
                }
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.sendBlockUpdated(pos, state, newLetterState, 3);
            } else {
                level.setBlock(pos, newLetterState, 11);
                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe instanceof LetterBlockEntity newLetter) {
                    CompoundTag blockEntityTag = held.getTagElement("BlockEntityTag");
                    if (blockEntityTag != null) {
                        newLetter.load(blockEntityTag);
                    }
                    newLetter.setHasBackplate(true);
                    newLetter.setBackplateFrontMaterial(fMat);
                    newLetter.setBackplateBackMaterial(bMat);
                    newLetter.setBackplateFrontColor(fColor);
                    newLetter.setBackplateBackColor(bColor);
                    newLetter.setBackplateFrontRainbow(fRainbow);
                    newLetter.setBackplateBackRainbow(bRainbow);
                }
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                );
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
                applyBackplateTagToEntity(lbe, beTag);
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
                SignMaterial fMat = lbe.getBackplateFrontMaterial();
                if (fMat == SignMaterial.DEFAULT && state.hasProperty(MATERIAL)) {
                    lbe.setBackplateFrontMaterial(state.getValue(MATERIAL));
                }
                drops.add(getDroppedBackplateItemStack(lbe));
            } else {
                drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));

                SignMaterial fMat = lbe.getBackplateFrontMaterial();
                if (fMat == SignMaterial.DEFAULT && state.hasProperty(MATERIAL)) {
                    fMat = state.getValue(MATERIAL);
                }
                SignMaterial bMat = lbe.getBackplateBackMaterial();

                if (fMat != SignMaterial.DEFAULT) {
                    ItemStack fStack = getItemForMaterial(fMat);
                    if (!fStack.isEmpty()) drops.add(fStack);
                }
                if (bMat != SignMaterial.DEFAULT) {
                    ItemStack bStack = getItemForMaterial(bMat);
                    if (!bStack.isEmpty()) drops.add(bStack);
                }
            }
        } else {
            drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));
            SignMaterial mat = state.hasProperty(MATERIAL) ? state.getValue(MATERIAL) : SignMaterial.DEFAULT;
            if (mat != SignMaterial.DEFAULT) {
                ItemStack matStack = getItemForMaterial(mat);
                if (!matStack.isEmpty()) drops.add(matStack);
            }
        }
        return drops;
    }

    public static ItemStack getDroppedBackplateItemStack(LetterBlockEntity lbe) {
        SignMaterial fMat = lbe.getBackplateFrontMaterial();
        SignMaterial bMat = lbe.getBackplateBackMaterial();
        int fCol = lbe.getBackplateFrontColor();
        int bCol = lbe.getBackplateBackColor();
        boolean fRain = lbe.isBackplateFrontRainbow();
        boolean bRain = lbe.isBackplateBackRainbow();

        boolean isDefault = (fMat == null || fMat == SignMaterial.DEFAULT)
                && (bMat == null || bMat == SignMaterial.DEFAULT)
                && (fCol == 0xFFFFFF || fCol == 0)
                && (bCol == 0xFFFFFF || bCol == 0)
                && !fRain && !bRain;

        if (isDefault) {
            return new ItemStack(ModBlocks.BACKPLATE_ITEM.get());
        }

        ItemStack stack = new ItemStack(ModBlocks.BACKPLATE_ITEM.get());
        CompoundTag beTag = new CompoundTag();
        beTag.putBoolean("HasBackplate", true);

        if (fMat != null && fMat != SignMaterial.DEFAULT) {
            beTag.putString("BackplateFrontMaterial", fMat.name());
        }
        if (bMat != null && bMat != SignMaterial.DEFAULT) {
            beTag.putString("BackplateBackMaterial", bMat.name());
        }
        if (fCol != 0xFFFFFF && fCol != 0) {
            beTag.putInt("BackplateFrontColor", fCol);
        }
        if (bCol != 0xFFFFFF && bCol != 0) {
            beTag.putInt("BackplateBackColor", bCol);
        }
        if (fRain) {
            beTag.putBoolean("BackplateFrontRainbow", true);
        }
        if (bRain) {
            beTag.putBoolean("BackplateBackRainbow", true);
        }

        stack.addTagElement("BlockEntityTag", beTag);
        return stack;
    }

    public static void applyBackplateTagToEntity(LetterBlockEntity entity, CompoundTag beTag) {
        entity.setHasBackplate(true);

        if (beTag.contains("BackplateFrontMaterial")) {
            try { entity.setBackplateFrontMaterial(SignMaterial.valueOf(beTag.getString("BackplateFrontMaterial"))); } catch (Exception ignored) {}
        } else if (beTag.contains("backplateFrontMaterial")) {
            try { entity.setBackplateFrontMaterial(SignMaterial.valueOf(beTag.getString("backplateFrontMaterial"))); } catch (Exception ignored) {}
        }

        if (beTag.contains("BackplateBackMaterial")) {
            try { entity.setBackplateBackMaterial(SignMaterial.valueOf(beTag.getString("BackplateBackMaterial"))); } catch (Exception ignored) {}
        } else if (beTag.contains("backplateBackMaterial")) {
            try { entity.setBackplateBackMaterial(SignMaterial.valueOf(beTag.getString("backplateBackMaterial"))); } catch (Exception ignored) {}
        }

        if (beTag.contains("BackplateFrontColor")) {
            entity.setBackplateFrontColor(beTag.getInt("BackplateFrontColor"));
        } else if (beTag.contains("backplateFrontColor")) {
            entity.setBackplateFrontColor(beTag.getInt("backplateFrontColor"));
        }

        if (beTag.contains("BackplateBackColor")) {
            entity.setBackplateBackColor(beTag.getInt("BackplateBackColor"));
        } else if (beTag.contains("backplateBackColor")) {
            entity.setBackplateBackColor(beTag.getInt("backplateBackColor"));
        }

        if (beTag.contains("BackplateFrontRainbow")) {
            entity.setBackplateFrontRainbow(beTag.getBoolean("BackplateFrontRainbow"));
        } else if (beTag.contains("backplateFrontRainbow")) {
            entity.setBackplateFrontRainbow(beTag.getBoolean("backplateFrontRainbow"));
        }

        if (beTag.contains("BackplateBackRainbow")) {
            entity.setBackplateBackRainbow(beTag.getBoolean("BackplateBackRainbow"));
        } else if (beTag.contains("backplateBackRainbow")) {
            entity.setBackplateBackRainbow(beTag.getBoolean("backplateBackRainbow"));
        }
    }

    public static ItemStack getItemForMaterial(SignMaterial material) {
        if (material == null || material == SignMaterial.DEFAULT) return ItemStack.EMPTY;
        String regName = getRegistryNameForMaterial(material);
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(regName));
        if (item != null && item != Items.AIR) {
            return new ItemStack(item, 1);
        }
        return ItemStack.EMPTY;
    }

    public static String getRegistryNameForMaterial(SignMaterial material) {
        return switch (material) {
            case OAK -> "minecraft:oak_planks"; case SPRUCE -> "minecraft:spruce_planks"; case BIRCH -> "minecraft:birch_planks";
            case JUNGLE -> "minecraft:jungle_planks"; case ACACIA -> "minecraft:acacia_planks"; case DARK_OAK -> "minecraft:dark_oak_planks";
            case MANGROVE -> "minecraft:mangrove_planks"; case CHERRY -> "minecraft:cherry_planks"; case BAMBOO -> "minecraft:bamboo_planks";
            case IRON -> "minecraft:iron_block"; case ANDESITE -> "minecraft:polished_andesite";
            case GOLD -> "minecraft:gold_block"; case DIAMOND -> "minecraft:diamond_block"; case LAPIS -> "minecraft:lapis_block";
            case SMOOTH_STONE -> "minecraft:smooth_stone"; case POLISHED_DIORITE -> "minecraft:polished_diorite";
            case BRICKS -> "minecraft:bricks"; case STONE_BRICKS -> "minecraft:stone_bricks";
            default -> "minecraft:white_concrete";
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState state = this.defaultBlockState();

        if (clickedFace.getAxis() == Direction.Axis.Y) {
            return state.setValue(FACE, clickedFace == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        } else {
            return state.setValue(FACE, AttachFace.WALL)
                    .setValue(FACING, clickedFace);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AttachFace face = state.getValue(FACE);
        Direction dir = state.getValue(FACING);
        if (face == AttachFace.WALL) {
            return switch (dir) {
                case SOUTH -> WALL_SOUTH;
                case EAST  -> WALL_EAST;
                case WEST  -> WALL_WEST;
                default    -> WALL_NORTH;
            };
        }
        return switch (dir) {
            case SOUTH -> FLOOR_SOUTH;
            case EAST  -> FLOOR_EAST;
            case WEST  -> FLOOR_WEST;
            default    -> FLOOR_NORTH;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        LetterBlockEntity be = new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), pos, state);
        be.setHasBackplate(true);
        return be;
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