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
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BackplateBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final EnumProperty<SignMaterial> MATERIAL = EnumProperty.create("material", SignMaterial.class);
    public static final net.minecraft.world.level.block.state.properties.IntegerProperty LIGHT_MODE = LetterBlock.LIGHT_MODE;

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
                .setValue(MATERIAL, SignMaterial.DEFAULT)
                .setValue(LIGHT_MODE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, MATERIAL, LIGHT_MODE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof PaintBrushItem || held.getItem() instanceof com.boran.signbuilder.item.WrenchItem || held.getItem() instanceof com.boran.signbuilder.item.SignBlueprintItem) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (held.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
            BlockEntity be = level.getBlockEntity(pos);
            LetterBlockEntity letterBe = (be instanceof LetterBlockEntity lbe) ? lbe : null;

            SignMaterial fMat = letterBe != null ? letterBe.getBackplateFrontMaterial() : SignMaterial.DEFAULT;
            SignMaterial bMat = letterBe != null ? letterBe.getBackplateBackMaterial() : SignMaterial.DEFAULT;
            int facingRotation = letterBe != null ? letterBe.getFacingRotation() : SignRotation.fromDirection(state.getValue(FACING));
            if (fMat == SignMaterial.DEFAULT && state.hasProperty(MATERIAL)) {
                fMat = state.getValue(MATERIAL);
            }

            int fColor = letterBe != null ? letterBe.getBackplateFrontColor() : 0xFFFFFF;
            int bColor = letterBe != null ? letterBe.getBackplateBackColor() : 0xFFFFFF;
            boolean fRainbow = letterBe != null && letterBe.isBackplateFrontRainbow();
            boolean bRainbow = letterBe != null && letterBe.isBackplateBackRainbow();

            AttachFace face = state.getValue(FACE);
            Direction facing = state.getValue(FACING);
            boolean backSide = isOnBackSide(player, pos, facingRotation);
            if (face == AttachFace.WALL && backSide) {
                return net.minecraft.world.ItemInteractionResult.FAIL;
            }
            Direction sideFacing = backSide ? facing.getOpposite() : facing;
            int sideRotation = Math.floorMod(facingRotation + (backSide ? 4 : 0), 8);
            Direction letterFacing = (face != AttachFace.WALL) ? sideFacing.getClockWise() : sideFacing;
            int letterRotation = Math.floorMod(sideRotation + (face != AttachFace.WALL ? 2 : 0), 8);
            CompoundTag blockEntityTag = held.getOrDefault(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();

            BlockState newLetterState = letterBlock.defaultBlockState()
                    .setValue(LetterBlock.FACING, letterFacing)
                    .setValue(LetterBlock.FACE, face);
            if (letterBe != null && letterBe.isActive()) {
                newLetterState = newLetterState.setValue(LetterBlock.LIGHT_MODE, letterBe.getEmittedLightMode());
            }

            if (!level.isClientSide()) {
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                level.setBlock(pos, newLetterState, 3);
                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe instanceof LetterBlockEntity newLetter) {
                    if (blockEntityTag != null) {
                        newLetter.loadWithComponents(blockEntityTag, level.registryAccess());
                    }
                    newLetter.setFacingRotation(letterRotation);
                    newLetter.setHasBackplate(true);
                    newLetter.setBackplateFrontMaterial(fMat);
                    newLetter.setBackplateBackMaterial(bMat);
                    newLetter.setBackplateFrontColor(fColor);
                    newLetter.setBackplateBackColor(bColor);
                    newLetter.setBackplateFrontRainbow(fRainbow);
                    newLetter.setBackplateBackRainbow(bRainbow);
                    if (letterBe != null && (blockEntityTag == null || !blockEntityTag.contains("WrenchMode"))) {
                        newLetter.setLightConfiguration(letterBe.getWrenchMode(), letterBe.isActive(),
                                letterBe.doesDetectMonsters(), letterBe.doesDetectAnimals(),
                                letterBe.getCustomLightOnTicks(), letterBe.getCustomLightOffTicks(),
                                letterBe.getCustomLightType(), letterBe.getCustomLightRange(),
                                letterBe.getCustomLightOffRange(), letterBe.getCustomLightCloseDelayTicks(),
                                letterBe.isCustomLightNightOnly(), letterBe.doesCustomLightDetectPlayers(),
                                letterBe.isCustomLightLowPower(), letterBe.isCustomLightLookOnly());
                    }
                    newLetter.setChanged();
                    newLetter.sync();
                }
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.sendBlockUpdated(pos, state, newLetterState, 3);
            } else {
                level.setBlock(pos, newLetterState, 11);
                BlockEntity newBe = level.getBlockEntity(pos);
                if (newBe instanceof LetterBlockEntity newLetter) {
                    if (blockEntityTag != null) {
                        newLetter.loadWithComponents(blockEntityTag, level.registryAccess());
                    }
                    newLetter.setFacingRotation(letterRotation);
                    newLetter.setHasBackplate(true);
                    newLetter.setBackplateFrontMaterial(fMat);
                    newLetter.setBackplateBackMaterial(bMat);
                    newLetter.setBackplateFrontColor(fColor);
                    newLetter.setBackplateBackColor(bColor);
                    newLetter.setBackplateFrontRainbow(fRainbow);
                    newLetter.setBackplateBackRainbow(bRainbow);
                    if (letterBe != null && (blockEntityTag == null || !blockEntityTag.contains("WrenchMode"))) {
                        newLetter.setLightConfiguration(letterBe.getWrenchMode(), letterBe.isActive(),
                                letterBe.doesDetectMonsters(), letterBe.doesDetectAnimals(),
                                letterBe.getCustomLightOnTicks(), letterBe.getCustomLightOffTicks(),
                                letterBe.getCustomLightType(), letterBe.getCustomLightRange(),
                                letterBe.getCustomLightOffRange(), letterBe.getCustomLightCloseDelayTicks(),
                                letterBe.isCustomLightNightOnly(), letterBe.doesCustomLightDetectPlayers(),
                                letterBe.isCustomLightLowPower(), letterBe.isCustomLightLookOnly());
                    }
                }
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                        com.boran.signbuilder.client.ClientHooks.setBlocksDirty(pos)
                );
            }
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private static boolean isOnBackSide(Player player, BlockPos pos, int rotation) {
        double side = (player.getX() - (pos.getX() + 0.5)) * SignRotation.facingX(rotation)
                + (player.getZ() - (pos.getZ() + 0.5)) * SignRotation.facingZ(rotation);
        return side < 0.0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity lbe) {
            CompoundTag beTag = stack.getOrDefault(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
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
            if (state.getValue(FACE) == AttachFace.WALL) {
                lbe.setFacingRotation(SignRotation.fromDirection(state.getValue(FACING)));
            } else if (placer != null && (beTag == null || !beTag.contains("FacingRotation"))) {
                lbe.setFacingRotation(SignRotation.fromBackplatePlacement(placer.getYRot()));
            }
            if (lbe.isActive()) {
                LetterBlock.updateLightLevel(level, pos, state, lbe);
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

        boolean hasSilkTouch = tool != null && EnchantmentHelper.getItemEnchantmentLevel(params.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), tool) > 0;

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
                if (lbe.isActive()) {
                    drops.add(new ItemStack(Items.GLOWSTONE_DUST, 1));
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
                && !fRain && !bRain
                && lbe.getWrenchMode() == 0 && !lbe.isActive();

        if (isDefault) {
            return new ItemStack(ModBlocks.BACKPLATE_ITEM.get());
        }

        ItemStack stack = new ItemStack(ModBlocks.BACKPLATE_ITEM.get());
        CompoundTag beTag = new CompoundTag();
        beTag.putBoolean("HasBackplate", true);
        beTag.putString("id", "signbuilder:letter_block_entity");

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
        if (lbe.getWrenchMode() != 0) beTag.putInt("WrenchMode", lbe.getWrenchMode());
        if (lbe.isActive()) {
            beTag.putBoolean("IsActive", true);
            beTag.putBoolean("Glowing", true);
        }
        if (!lbe.doesDetectMonsters()) beTag.putBoolean("DetectsMonsters", false);
        if (lbe.doesDetectAnimals()) beTag.putBoolean("DetectsAnimals", true);
        if (lbe.getCustomLightOnTicks() != 10) beTag.putInt("CustomLightOnTicks", lbe.getCustomLightOnTicks());
        if (lbe.getCustomLightOffTicks() != 10) beTag.putInt("CustomLightOffTicks", lbe.getCustomLightOffTicks());
        if (lbe.getCustomLightType() != 0) beTag.putInt("CustomLightType", lbe.getCustomLightType());
        if (lbe.getCustomLightRange() != 8) beTag.putInt("CustomLightRange", lbe.getCustomLightRange());
        if (lbe.getCustomLightOffRange() != lbe.getCustomLightRange()) beTag.putInt("CustomLightOffRange", lbe.getCustomLightOffRange());
        if (lbe.getCustomLightCloseDelayTicks() != 0) beTag.putInt("CustomLightCloseDelayTicks", lbe.getCustomLightCloseDelayTicks());
        if (!lbe.isCustomLightNightOnly()) beTag.putBoolean("CustomLightNightOnly", false);
        if (!lbe.doesCustomLightDetectPlayers()) beTag.putBoolean("CustomLightPlayers", false);
        if (lbe.isCustomLightLowPower()) beTag.putBoolean("CustomLightLowPower", true);
        if (!lbe.isCustomLightLookOnly()) beTag.putBoolean("CustomLightLookOnly", false);

        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, stack, t -> t.merge(beTag));
        return stack;
    }

    public static void applyBackplateTagToEntity(LetterBlockEntity entity, CompoundTag beTag) {
        entity.setHasBackplate(true);
        if (beTag.contains("FacingRotation")) {
            entity.setFacingRotation(beTag.getInt("FacingRotation"));
        }

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

        if (beTag.contains("WrenchMode") || beTag.contains("IsActive") || beTag.contains("Glowing")) {
            int mode = beTag.getInt("WrenchMode");
            boolean active = beTag.getBoolean("IsActive") || beTag.getBoolean("Glowing");
            boolean detectsMonsters = !beTag.contains("DetectsMonsters") || beTag.getBoolean("DetectsMonsters");
            boolean detectsAnimals = beTag.getBoolean("DetectsAnimals");
            int onTicks = beTag.contains("CustomLightOnTicks") ? beTag.getInt("CustomLightOnTicks") : 10;
            int offTicks = beTag.contains("CustomLightOffTicks") ? beTag.getInt("CustomLightOffTicks") : 10;
            int customType = beTag.getInt("CustomLightType");
            int customRange = beTag.contains("CustomLightRange") ? beTag.getInt("CustomLightRange") : 8;
            int customOffRange = beTag.contains("CustomLightOffRange") ? beTag.getInt("CustomLightOffRange") : customRange;
            int closeDelayTicks = beTag.getInt("CustomLightCloseDelayTicks");
            boolean nightOnly = !beTag.contains("CustomLightNightOnly") || beTag.getBoolean("CustomLightNightOnly");
            boolean players = !beTag.contains("CustomLightPlayers") || beTag.getBoolean("CustomLightPlayers");
            boolean lowPower = beTag.getBoolean("CustomLightLowPower");
            boolean lookOnly = !beTag.contains("CustomLightLookOnly") || beTag.getBoolean("CustomLightLookOnly");

            entity.setLightConfiguration(mode, active, detectsMonsters, detectsAnimals, onTicks, offTicks, customType, customRange, customOffRange, closeDelayTicks, nightOnly, players, lowPower, lookOnly);
        }
    }

    public static ItemStack getItemForMaterial(SignMaterial material) {
        if (material == null || material == SignMaterial.DEFAULT) return ItemStack.EMPTY;
        String regName = getRegistryNameForMaterial(material);
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(regName));
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
            case IRON -> "minecraft:iron_ingot"; case ANDESITE -> "minecraft:polished_andesite";
            case GOLD -> "minecraft:gold_ingot"; case DIAMOND -> "minecraft:diamond"; case LAPIS -> "minecraft:lapis_lazuli";
            case SMOOTH_STONE -> "minecraft:smooth_stone"; case POLISHED_DIORITE -> "minecraft:polished_diorite";
            case BRICKS -> "minecraft:bricks"; case STONE_BRICKS -> "minecraft:stone_bricks";
            case REDSTONE_BLOCK -> "minecraft:redstone";
            case NETHERITE_BLOCK -> "minecraft:netherite_ingot";
            case QUARTZ_BLOCK -> "minecraft:quartz_block";
            case POLISHED_GRANITE -> "minecraft:polished_granite";
            case PURPUR_BLOCK -> "minecraft:purpur_block";
            case STONE -> "minecraft:stone";
            case EMERALD_BLOCK -> "minecraft:emerald";
            case SMOOTH_SANDSTONE -> "minecraft:smooth_sandstone";
            case SMOOTH_RED_SANDSTONE -> "minecraft:smooth_red_sandstone";
            case COPPER_BLOCK -> "minecraft:copper_ingot";
            case AMETHYST_BLOCK -> "minecraft:amethyst_shard";
            case COAL_BLOCK -> "minecraft:coal";
            case END_STONE_BRICKS -> "minecraft:end_stone_bricks";
            case NETHER_BRICKS -> "minecraft:nether_bricks";
            case RED_NETHER_BRICKS -> "minecraft:red_nether_bricks";
            case PRISMARINE_BRICKS -> "minecraft:prismarine_bricks";
            case MUD_BRICKS -> "minecraft:mud_bricks";
            case DEEPSLATE_BRICKS -> "minecraft:deepslate_bricks";
            case CRIMSON_PLANKS -> "minecraft:crimson_planks";
            case WARPED_PLANKS -> "minecraft:warped_planks";
            case MOSSY_STONE_BRICKS -> "minecraft:mossy_stone_bricks";
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

    public static VoxelShape calculateShape(AttachFace face, Direction dir, int facingRotation) {
        VoxelShape shape;
        if (face == AttachFace.WALL) {
            shape = switch (dir) {
                case SOUTH -> WALL_SOUTH;
                case EAST  -> WALL_EAST;
                case WEST  -> WALL_WEST;
                default    -> WALL_NORTH;
            };
        } else {
            shape = switch (dir) {
                case SOUTH -> FLOOR_SOUTH;
                case EAST  -> FLOOR_EAST;
                case WEST  -> FLOOR_WEST;
                default    -> FLOOR_NORTH;
            };
        }
        shape = SignRotation.rotateAroundBlockCenter(shape, SignRotation.deltaDegrees(facingRotation, dir));
        if (face != AttachFace.WALL) {
            AABB bounds = shape.bounds();
            shape = shape.move(0.5 - (bounds.minX + bounds.maxX) * 0.5, 0.0,
                    0.5 - (bounds.minZ + bounds.maxZ) * 0.5);
            shape = SignRotation.widenAlongTangent(shape, facingRotation, Math.sqrt(2.0));
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AttachFace face = state.getValue(FACE);
        Direction dir = state.getValue(FACING);
        BlockEntity be = level.getBlockEntity(pos);
        int rot = be instanceof LetterBlockEntity lbe ? lbe.getFacingRotation() : SignRotation.fromDirection(dir);
        return calculateShape(face, dir, rot);
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
