package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PaintBrushItem extends Item {

    public PaintBrushItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel.isClientSide()) {
            net.minecraft.client.Minecraft.getInstance().setScreen(new com.boran.signbuilder.client.screen.PaintBrushScreen());
        }

        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();
        BlockPos pos = pContext.getClickedPos();
        ItemStack brush = pContext.getItemInHand();
        BlockState clickedBlock = level.getBlockState(pos);

        if (clickedBlock.hasProperty(ModBlocks.COLOR)) {

            if (!level.isClientSide() && player != null) {
                CompoundTag nbt = brush.getTag();

                if (nbt != null && nbt.contains("SelectedColor")) {
                    int colorIndex = nbt.getInt("SelectedColor");

                    BlockState newBlockState = clickedBlock.setValue(ModBlocks.COLOR, colorIndex);
                    level.setBlock(pos, newBlockState, 3);

                    level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.2F);
                } else {
                    player.displayClientMessage(Component.literal("Lütfen önce havaya sağ tıklayarak bir renk seçin!"), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
