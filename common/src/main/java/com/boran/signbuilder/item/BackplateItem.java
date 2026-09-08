package com.boran.signbuilder.item;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
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
            if (be instanceof LetterBlockEntity letter) {
                if (player != null && player.isShiftKeyDown()) {
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
                    CompoundTag beTag = stack.getTagElement("BlockEntityTag");
                    if (beTag != null) {
                        BackplateBlock.applyBackplateTagToEntity(letter, beTag);
                    }

                    if (!level.isClientSide()) {
                        if (player != null && !player.isCreative()) {
                            stack.shrink(1);
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

        return super.useOn(context);
    }
}