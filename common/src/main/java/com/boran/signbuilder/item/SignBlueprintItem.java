package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.client.screen.BlueprintScreen;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
        return switch (c) {
            case 'A', 'a' -> ModBlocks.LETTER_A.get();
            case 'B', 'b' -> ModBlocks.LETTER_B.get();
            case 'C', 'c', 'Ç', 'ç' -> ModBlocks.LETTER_C.get();
            case 'D', 'd' -> ModBlocks.LETTER_D.get();
            case 'E', 'e' -> ModBlocks.LETTER_E.get();
            case 'F', 'f' -> ModBlocks.LETTER_F.get();
            case 'G', 'g', 'Ğ', 'ğ' -> ModBlocks.LETTER_G.get();
            case 'H', 'h' -> ModBlocks.LETTER_H.get();
            case 'I', 'i', 'İ', 'ı' -> ModBlocks.LETTER_I.get();
            case 'J', 'j' -> ModBlocks.LETTER_J.get();
            case 'K', 'k' -> ModBlocks.LETTER_K.get();
            case 'L', 'l' -> ModBlocks.LETTER_L.get();
            case 'M', 'm' -> ModBlocks.LETTER_M.get();
            case 'N', 'n' -> ModBlocks.LETTER_N.get();
            case 'O', 'o', 'Ö', 'ö' -> ModBlocks.LETTER_O.get();
            case 'P', 'p' -> ModBlocks.LETTER_P.get();
            case 'R', 'r' -> ModBlocks.LETTER_R.get();
            case 'S', 's', 'Ş', 'ş' -> ModBlocks.LETTER_S.get();
            case 'T', 't' -> ModBlocks.LETTER_T.get();
            case 'U', 'u', 'Ü', 'ü' -> ModBlocks.LETTER_U.get();
            case 'V', 'v' -> ModBlocks.LETTER_V.get();
            case 'W', 'w' -> ModBlocks.LETTER_W.get();
            case 'X', 'x' -> ModBlocks.LETTER_X.get();
            case 'Y', 'y' -> ModBlocks.LETTER_Y.get();
            case 'Z', 'z' -> ModBlocks.LETTER_Z.get();
            case '0' -> ModBlocks.NUMBER_0.get();
            case '1' -> ModBlocks.NUMBER_1.get();
            case '2' -> ModBlocks.NUMBER_2.get();
            case '3' -> ModBlocks.NUMBER_3.get();
            case '4' -> ModBlocks.NUMBER_4.get();
            case '5' -> ModBlocks.NUMBER_5.get();
            case '6' -> ModBlocks.NUMBER_6.get();
            case '7' -> ModBlocks.NUMBER_7.get();
            case '8' -> ModBlocks.NUMBER_8.get();
            case '9' -> ModBlocks.NUMBER_9.get();
            case '+' -> ModBlocks.SYMBOL_PLUS.get();
            case '-' -> ModBlocks.SYMBOL_MINUS.get();
            case '/' -> ModBlocks.SYMBOL_SLASH.get();
            case '\\' -> ModBlocks.SYMBOL_BACKSLASH.get();
            case '#' -> ModBlocks.SYMBOL_HASHTAG.get();
            case '*' -> ModBlocks.SYMBOL_STAR.get();
            case '♥' -> ModBlocks.SYMBOL_HEART.get();
            case '€' -> ModBlocks.SYMBOL_EURO.get();
            case '$' -> ModBlocks.SYMBOL_DOLLAR.get();
            case '£' -> ModBlocks.SYMBOL_POUND.get();
            case '₺' -> ModBlocks.SYMBOL_TL.get();
            case '«' -> ModBlocks.SYMBOL_DOT_LEFT.get();
            case '•' -> ModBlocks.SYMBOL_DOT_CENTER.get();
            case '»' -> ModBlocks.SYMBOL_DOT_RIGHT.get();
            case '(' -> ModBlocks.SYMBOL_BRACKET_LEFT.get();
            case ')' -> ModBlocks.SYMBOL_BRACKET_RIGHT.get();
            case '|' -> ModBlocks.SYMBOL_BRACKET_DOUBLE.get();
            case '[' -> ModBlocks.SYMBOL_SQUARE_BRACKET_LEFT.get();
            case ']' -> ModBlocks.SYMBOL_SQUARE_BRACKET_RIGHT.get();
            case '¦' -> ModBlocks.SYMBOL_SQUARE_BRACKET_DOUBLE.get();
            case '↑' -> ModBlocks.ARROW_UP.get();
            case '↓' -> ModBlocks.ARROW_DOWN.get();
            case '←' -> ModBlocks.ARROW_LEFT.get();
            case '→' -> ModBlocks.ARROW_RIGHT.get();
            case '↖' -> ModBlocks.ARROW_LEFT_UP.get();
            case '↗' -> ModBlocks.ARROW_RIGHT_UP.get();
            case '↙' -> ModBlocks.ARROW_LEFT_DOWN.get();
            case '↘' -> ModBlocks.ARROW_RIGHT_DOWN.get();
            default -> null;
        };
    }
}