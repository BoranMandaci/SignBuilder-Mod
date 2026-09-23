package com.boran.signbuilder.item;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BackplateItem extends BlockItem {

    public BackplateItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof LetterBlock) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity rawLetter) {
                LetterBlockEntity letter = rawLetter.getEffectiveMaster();
                BlockPos mPos = letter.getBlockPos();
                BlockState mState = level.getBlockState(mPos);
                int size = letter.getSize();
                int cost = (size == 3) ? 9 : (size == 2 ? 4 : 1);

                if (player != null && player.isShiftKeyDown()) {
                    if (LetterBlock.tryDetachBackplate(level, pos, player)) {
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                    return InteractionResult.PASS;
                } else if (!letter.hasBackplate()) {
                    if (player != null && !player.isCreative() && stack.getCount() < cost) {
                        return InteractionResult.FAIL;
                    }

                    CompoundTag beTag = stack.getTagElement("BlockEntityTag");
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

                    if (!level.isClientSide()) {
                        LetterBlock.updateAllEntitiesBackplate(level, mPos, mState, size, true, fMat, bMat, fCol, bCol, fRain, bRain);

                        if (player != null && !player.isCreative()) {
                            stack.shrink(cost);
                        }
                        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                            com.boran.signbuilder.client.ClientHooks.setBlocksDirty(mPos);
                            if (size == 3) {
                                for (BlockPos p : LetterBlock.get3x3BlockPositions(mPos, mState)) {
                                    com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                                }
                            } else if (size == 2) {
                                for (BlockPos p : LetterBlock.getBigBlockPositions(mPos, mState)) {
                                    com.boran.signbuilder.client.ClientHooks.setBlocksDirty(p);
                                }
                            }
                        });
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        return super.useOn(context);
    }
}