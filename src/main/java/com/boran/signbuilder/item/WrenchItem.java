package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {

    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState clickedBlock = level.getBlockState(pos);
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();

        if (clickedBlock.hasProperty(ModBlocks.GLOWING)) {
            if (!level.isClientSide()) {
                boolean isGlowing = clickedBlock.getValue(ModBlocks.GLOWING);
                level.setBlock(pos, clickedBlock.setValue(ModBlocks.GLOWING, !isGlowing), 2);
                level.playSound(null, pos, SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
            }

            if (player != null && !player.isCreative()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}