package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.screen.PaintBrushScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaintBrushItem extends Item {

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            Minecraft.getInstance().setScreen(new PaintBrushScreen());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof LetterBlockEntity letterEntity) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("SelectedColor")) {
                    int selectedColor = tag.getInt("SelectedColor");
                    int hexColor = getActualHexColor(selectedColor);

                    letterEntity.setRgbColor(hexColor);

                    if (selectedColor <= 15 && state.hasProperty(ModBlocks.COLOR)) {
                        level.setBlock(pos, state.setValue(ModBlocks.COLOR, selectedColor), 3);
                    } else {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }

            if (player != null) {
                level.playSound(player, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide && player != null) {
            Minecraft.getInstance().setScreen(new PaintBrushScreen());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();

        int selectedColor = 0;
        if (tag != null && tag.contains("SelectedColor")) {
            selectedColor = tag.getInt("SelectedColor");
        }

        Component label = Component.translatable("tooltip.signbuilder.selected_color");
        Component valueComponent;

        if (selectedColor <= 15) {
            String colorNameKey = getColorNameKey(selectedColor);
            int hexColor = getActualHexColor(selectedColor);
            valueComponent = Component.translatable(colorNameKey)
                    .withStyle(Style.EMPTY.withColor(hexColor));
        } else {
            String hexString = "#" + String.format("%06X", selectedColor).toUpperCase();
            valueComponent = Component.literal(hexString)
                    .withStyle(Style.EMPTY.withColor(selectedColor));
        }

        tooltipComponents.add(Component.empty().append(label).append(" ").append(valueComponent));

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    private static String getColorNameKey(int index) {
        return switch (index) {
            case 0 -> "color.signbuilder.white";
            case 1 -> "color.signbuilder.orange";
            case 2 -> "color.signbuilder.magenta";
            case 3 -> "color.signbuilder.light_blue";
            case 4 -> "color.signbuilder.yellow";
            case 5 -> "color.signbuilder.lime";
            case 6 -> "color.signbuilder.pink";
            case 7 -> "color.signbuilder.gray";
            case 8 -> "color.signbuilder.light_gray";
            case 9 -> "color.signbuilder.cyan";
            case 10 -> "color.signbuilder.purple";
            case 11 -> "color.signbuilder.blue";
            case 12 -> "color.signbuilder.brown";
            case 13 -> "color.signbuilder.green";
            case 14 -> "color.signbuilder.red";
            case 15 -> "color.signbuilder.black";
            default -> "color.signbuilder.white";
        };
    }

    public static int getActualHexColor(int colorValue) {
        if (colorValue > 15) return colorValue;
        return switch (colorValue) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8;
            case 4 -> 0xE5E533; case 5 -> 0x7FCC19; case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C;
            case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2; case 11 -> 0x334CB2;
            case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0xCF2323; case 15 -> 0x191919;
            default -> 0xFFFFFF;
        };
    }
}