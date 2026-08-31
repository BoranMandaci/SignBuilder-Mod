package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LetterBlock extends Block implements EntityBlock {

    public static final EnumProperty<SignMaterial> MATERIAL = EnumProperty.create("material", SignMaterial.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public LetterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(ModBlocks.GLOWING, false)
                .setValue(ModBlocks.COLOR, 0)
                .setValue(MATERIAL, SignMaterial.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, ModBlocks.GLOWING, ModBlocks.COLOR, MATERIAL);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LetterBlockEntity) {
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
                drops.add(lbe.getDroppedItemStack(state));
            } else {
                drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));
                SignMaterial mat = state.hasProperty(MATERIAL) ? state.getValue(MATERIAL) : SignMaterial.DEFAULT;
                if (mat != SignMaterial.DEFAULT) {
                    String regName = getRegistryNameForMaterial(mat);
                    net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(regName));
                    if (item != null && item != Items.AIR) drops.add(new ItemStack(item, 1));
                }
                if (state.hasProperty(ModBlocks.GLOWING) && state.getValue(ModBlocks.GLOWING)) {
                    drops.add(new ItemStack(Items.GLOWSTONE_DUST, 1));
                }
            }
        } else {
            drops.add(new ItemStack(Blocks.WHITE_CONCRETE, 3));
        }
        return drops;
    }

    private String getRegistryNameForMaterial(SignMaterial material) {
        return switch (material) {
            case OAK -> "minecraft:oak_planks"; case SPRUCE -> "minecraft:spruce_planks"; case BIRCH -> "minecraft:birch_planks";
            case JUNGLE -> "minecraft:jungle_planks"; case ACACIA -> "minecraft:acacia_planks"; case DARK_OAK -> "minecraft:dark_oak_planks";
            case MANGROVE -> "minecraft:mangrove_planks"; case CHERRY -> "minecraft:cherry_planks"; case BAMBOO -> "minecraft:bamboo_planks";
            case IRON -> "minecraft:iron_block"; case ANDESITE -> "minecraft:polished_andesite"; default -> "minecraft:white_concrete";
        };
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