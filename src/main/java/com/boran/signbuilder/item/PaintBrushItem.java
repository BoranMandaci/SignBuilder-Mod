package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.screen.PaintBrushScreen;
import net.minecraft.ChatFormatting;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class PaintBrushItem extends Item {

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();

        int selectedColor = 0;
        boolean isSmartFill = false;

        if (tag != null) {
            if (tag.contains("SelectedColor")) selectedColor = tag.getInt("SelectedColor");
            if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
        }

        Component label = Component.translatable("tooltip.signbuilder.selected_color");
        Component valueComponent;

        if (selectedColor == -1) {
            float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
            int dynamicColor = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);

            valueComponent = Component.translatable("color.signbuilder.rainbow")
                    .withStyle(Style.EMPTY.withColor(dynamicColor));
        } else if (selectedColor <= 15) {
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

        tooltipComponents.add(Component.translatable("tooltip.signbuilder.brush.smart_fill")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "))
                .append(Component.translatable(isSmartFill ? "gui.signbuilder.wrench.state_on" : "gui.signbuilder.wrench.state_off")
                        .withStyle(isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)));

        tooltipComponents.add(Component.translatable("tooltip.signbuilder.eyedropper_hint")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA).withItalic(true)));

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean isSmartFill = stack.getOrCreateTag().getBoolean("IsSmartFill");
                stack.getOrCreateTag().putBoolean("IsSmartFill", !isSmartFill);

                player.displayClientMessage(
                        Component.translatable("message.signbuilder.brush.smart_fill_toggle")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable(!isSmartFill ? "gui.signbuilder.wrench.state_on" : "gui.signbuilder.wrench.state_off")
                                        .withStyle(!isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)),
                        true
                );

                level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.5F, !isSmartFill ? 1.5F : 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new PaintBrushScreen());
            });
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

            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    int copiedColor = letterEntity.isRainbow() ? -1 : letterEntity.getRgbColor();

                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("SelectedColor", copiedColor);

                    if (copiedColor == -1) {
                        player.displayClientMessage(Component.translatable("message.signbuilder.color_copied")
                                .append(Component.literal(" [Rainbow]"))
                                .withStyle(Style.EMPTY.withColor(0xFF55FF)), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.signbuilder.color_copied")
                                .append(Component.literal(" [#" + String.format("%06X", copiedColor).toUpperCase() + "]"))
                                .withStyle(Style.EMPTY.withColor(copiedColor)), true);
                    }
                }

                if (player != null) {
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("SelectedColor")) {
                    int selectedColor = tag.getInt("SelectedColor");
                    boolean isSmartFill = tag.getBoolean("IsSmartFill");

                    if (isSmartFill) {
                        applyColorToConnected(level, pos, selectedColor);
                        if (player != null) {
                            player.displayClientMessage(
                                    Component.translatable("message.signbuilder.brush.smart_fill_success")
                                            .withStyle(ChatFormatting.GREEN), true
                            );
                        }
                    } else {
                        if (selectedColor == -1) {
                            letterEntity.setRainbow(true);
                        } else {
                            int hexColor = getActualHexColor(selectedColor);
                            letterEntity.setRgbColor(hexColor);

                            if (selectedColor <= 15 && state.hasProperty(ModBlocks.COLOR)) {
                                level.setBlock(pos, state.setValue(ModBlocks.COLOR, selectedColor), 3);
                            } else {
                                level.sendBlockUpdated(pos, state, state, 3);
                            }
                        }
                    }
                }
            }

            if (player != null) {
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide && player != null && !player.isShiftKeyDown()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new PaintBrushScreen());
            });
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void applyColorToConnected(Level level, BlockPos startPos, int selectedColor) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() < 256) {
            BlockPos current = queue.poll();
            BlockEntity be = level.getBlockEntity(current);

            if (be instanceof LetterBlockEntity letter) {
                BlockState currentState = level.getBlockState(current);

                if (selectedColor == -1) {
                    letter.setRainbow(true);
                } else {
                    int hexColor = getActualHexColor(selectedColor);
                    letter.setRgbColor(hexColor);

                    if (selectedColor <= 15 && currentState.hasProperty(ModBlocks.COLOR)) {
                        level.setBlock(current, currentState.setValue(ModBlocks.COLOR, selectedColor), 3);
                    } else {
                        level.sendBlockUpdated(current, currentState, currentState, 3);
                    }
                }

                for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && level.getBlockEntity(neighbor) instanceof LetterBlockEntity) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private static String getColorNameKey(int index) {
        return switch (index) {
            case 0 -> "color.signbuilder.white"; case 1 -> "color.signbuilder.orange";
            case 2 -> "color.signbuilder.magenta"; case 3 -> "color.signbuilder.light_blue";
            case 4 -> "color.signbuilder.yellow"; case 5 -> "color.signbuilder.lime";
            case 6 -> "color.signbuilder.pink"; case 7 -> "color.signbuilder.gray";
            case 8 -> "color.signbuilder.light_gray"; case 9 -> "color.signbuilder.cyan";
            case 10 -> "color.signbuilder.purple"; case 11 -> "color.signbuilder.blue";
            case 12 -> "color.signbuilder.brown"; case 13 -> "color.signbuilder.green";
            case 14 -> "color.signbuilder.red"; case 15 -> "color.signbuilder.black";
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