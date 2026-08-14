package com.boran.signbuilder.block;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LetterBlock extends Block implements EntityBlock {

    public LetterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide) {

            BlockEntity be = pLevel.getBlockEntity(pPos);
            boolean ignoreRedstone = false;

            if (be instanceof LetterBlockEntity letterEntity) {
                if (letterEntity.isActive() || letterEntity.getWrenchMode() != 0) {
                    ignoreRedstone = true;
                }
            }

            if (!ignoreRedstone) {
                boolean hasSignal = pLevel.hasNeighborSignal(pPos);

                if (pState.hasProperty(ModBlocks.GLOWING)) {
                    boolean isGlowing = pState.getValue(ModBlocks.GLOWING);

                    if (hasSignal != isGlowing) {
                        pLevel.setBlock(pPos, pState.setValue(ModBlocks.GLOWING, hasSignal), 3);
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