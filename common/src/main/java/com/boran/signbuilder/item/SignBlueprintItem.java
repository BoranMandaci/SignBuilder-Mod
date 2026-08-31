package com.boran.signbuilder.item;

import com.boran.signbuilder.client.screen.BlueprintScreen;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignBlueprintItem extends Item {

    public SignBlueprintItem(Properties pProperties) {
        super(pProperties.durability(32));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        String currentText = "";

        CompoundTag tag = pStack.getTag();
        if (tag != null && tag.contains("BlueprintText")) {
            currentText = tag.getString("BlueprintText");
        }

        if (!currentText.isEmpty()) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.current_text")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": "))
                    .append(Component.literal(currentText).withStyle(ChatFormatting.AQUA)));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.empty")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.usage")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pLevel.isClientSide()) {
            pPlayer.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);

            String currentText = "";
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("BlueprintText")) {
                currentText = tag.getString("BlueprintText");
            }

            String finalCurrentText = currentText;

            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> Minecraft.getInstance().setScreen(new BlueprintScreen(finalCurrentText)));
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();

        if (level.isClientSide() || player == null) {
            return InteractionResult.SUCCESS;
        }

        String text = "";
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("BlueprintText")) {
            text = tag.getString("BlueprintText");
        }

        if (text.isEmpty()) {
            player.displayClientMessage(Component.translatable("tooltip.signbuilder.blueprint.empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        if (!player.isCreative()) {
            Map<Item, Integer> requiredItems = new HashMap<>();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == ' ') continue;

                Block blockForChar = getBlockForChar(c);
                if (blockForChar != null) {
                    Item itemForChar = blockForChar.asItem();
                    requiredItems.put(itemForChar, requiredItems.getOrDefault(itemForChar, 0) + 1);
                }
            }

            boolean hasAllItems = true;
            StringBuilder missingItemsText = new StringBuilder();

            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                Item requiredItem = entry.getKey();
                int requiredAmount = entry.getValue();
                int playerAmount = countItemInInventory(player, requiredItem);

                if (playerAmount < requiredAmount) {
                    hasAllItems = false;
                    int missing = requiredAmount - playerAmount;
                    missingItemsText.append(requiredItem.getDescription().getString()).append(" (x").append(missing).append("), ");
                }
            }

            if (!hasAllItems) {
                String missingStr = missingItemsText.substring(0, missingItemsText.length() - 2);

                player.displayClientMessage(Component.translatable("message.signbuilder.blueprint.missing_materials").withStyle(ChatFormatting.RED)
                        .append(Component.literal(": " + missingStr).withStyle(ChatFormatting.YELLOW)), true);

                player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                return InteractionResult.FAIL;
            }

            for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
                consumeItemFromInventory(player, entry.getKey(), entry.getValue());
            }
        }

        Direction clickedFace = pContext.getClickedFace();
        BlockPos startPos = pContext.getClickedPos().relative(clickedFace);

        Direction playerFacing = pContext.getHorizontalDirection();
        Direction rightDir = playerFacing.getClockWise();

        int blocksPlaced = 0;
        java.util.List<Long> placedPositions = new java.util.ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') continue;

            BlockPos currentPos = startPos.relative(rightDir, i);
            BlockState existingState = level.getBlockState(currentPos);

            if (!existingState.canBeReplaced()) break;

            Block blockToPlace = getBlockForChar(c);

            if (blockToPlace != null) {
                BlockHitResult hitResult = new BlockHitResult(
                        pContext.getClickLocation(), clickedFace, currentPos, pContext.isInside());

                UseOnContext offsetContext = new UseOnContext(player, pContext.getHand(), hitResult);
                BlockPlaceContext placeContext = new BlockPlaceContext(offsetContext);

                BlockState stateToPlace = blockToPlace.getStateForPlacement(placeContext);
                if (stateToPlace == null) stateToPlace = blockToPlace.defaultBlockState();

                level.setBlock(currentPos, stateToPlace, 3);
                blocksPlaced++;
                placedPositions.add(currentPos.asLong());
            }
        }

        if (blocksPlaced > 0) {
            level.playSound(null, startPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

            long[] posArray = placedPositions.stream().mapToLong(l -> l).toArray();
            stack.getOrCreateTag().putLongArray("UndoHistory", posArray);

            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private int countItemInInventory(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    private void consumeItemFromInventory(Player player, Item item, int amount) {
        int amountLeftToRemove = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                if (stack.getCount() >= amountLeftToRemove) {
                    stack.shrink(amountLeftToRemove);
                    break;
                } else {
                    amountLeftToRemove -= stack.getCount();
                    stack.setCount(0);
                }
            }
        }
    }

    private Block getBlockForChar(char c) {
        String blockId = switch (c) {
            case 'A', 'a' -> "letter_a";
            case 'B', 'b' -> "letter_b";
            case 'C', 'c', 'Ç', 'ç' -> "letter_c";
            case 'D', 'd' -> "letter_d";
            case 'E', 'e' -> "letter_e";
            case 'F', 'f' -> "letter_f";
            case 'G', 'g', 'Ğ', 'ğ' -> "letter_g";
            case 'H', 'h' -> "letter_h";
            case 'I', 'i', 'İ', 'ı' -> "letter_i";
            case 'J', 'j' -> "letter_j";
            case 'K', 'k' -> "letter_k";
            case 'L', 'l' -> "letter_l";
            case 'M', 'm' -> "letter_m";
            case 'N', 'n' -> "letter_n";
            case 'O', 'o', 'Ö', 'ö' -> "letter_o";
            case 'P', 'p' -> "letter_p";
            case 'R', 'r' -> "letter_r";
            case 'S', 's', 'Ş', 'ş' -> "letter_s";
            case 'T', 't' -> "letter_t";
            case 'U', 'u', 'Ü', 'ü' -> "letter_u";
            case 'V', 'v' -> "letter_v";
            case 'W', 'w' -> "letter_w";
            case 'X', 'x' -> "letter_x";
            case 'Y', 'y' -> "letter_y";
            case 'Z', 'z' -> "letter_z";
            case '0' -> "number_0";
            case '1' -> "number_1";
            case '2' -> "number_2";
            case '3' -> "number_3";
            case '4' -> "number_4";
            case '5' -> "number_5";
            case '6' -> "number_6";
            case '7' -> "number_7";
            case '8' -> "number_8";
            case '9' -> "number_9";
            case '+' -> "symbol_plus";
            case '-' -> "symbol_minus";
            case '/' -> "symbol_slash";
            case '\\' -> "symbol_backslash";
            case '#' -> "symbol_hashtag";
            case '*' -> "symbol_star";
            case '♥' -> "symbol_heart";
            case '€' -> "symbol_euro";
            case '$' -> "symbol_dollar";
            case '£' -> "symbol_pound";
            case '₺' -> "symbol_tl";
            case '«' -> "symbol_dot_left";
            case '•' -> "symbol_dot_center";
            case '»' -> "symbol_dot_right";
            case '(' -> "symbol_bracket_left";
            case ')' -> "symbol_bracket_right";
            case '|' -> "symbol_bracket_double";
            case '[' -> "symbol_square_bracket_left";
            case ']' -> "symbol_square_bracket_right";
            case '¦' -> "symbol_square_bracket_double";
            case '↑' -> "arrow_up";
            case '↓' -> "arrow_down";
            case '←' -> "arrow_left";
            case '→' -> "arrow_right";
            case '↖' -> "arrow_left_up";
            case '↗' -> "arrow_right_up";
            case '↙' -> "arrow_left_down";
            case '↘' -> "arrow_right_down";
            default -> null;
        };

        if (blockId != null) {
            Block targetBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("signbuilder", blockId));
            if (targetBlock != Blocks.AIR) {
                return targetBlock;
            }
        }
        return null;
    }
}