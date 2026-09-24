package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignBlueprintItem extends Item {

    private static final Map<Character, Block> CHAR_BLOCK_CACHE = new HashMap<>(128);

    public SignBlueprintItem(Properties pProperties) {
        super(pProperties.durability(32));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        String currentText = "";
        int size = 1;
        boolean isVertical = false;
        boolean withBackplate = false;
        CompoundTag tag = pStack.getTag();
        if (tag != null) {
            if (tag.contains("BlueprintText")) currentText = tag.getString("BlueprintText");
            if (tag.contains("Size")) size = tag.getInt("Size");
            else if (tag.getBoolean("Is2x2")) size = 2;
            if (tag.contains("IsVertical")) isVertical = tag.getBoolean("IsVertical");
            if (tag.contains("WithBackplate")) withBackplate = tag.getBoolean("WithBackplate");
        }

        if (!currentText.isEmpty()) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.current_text")
                    .withStyle(ChatFormatting.GRAY).append(Component.literal(": "))
                    .append(Component.literal(currentText).withStyle(ChatFormatting.AQUA)));
        } else {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.empty")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        String sizeText = (size == 3) ? "3x3 Multi-Block" : (size == 2 ? "2x2 Multi-Block" : "1x1 Normal");
        ChatFormatting sizeColor = (size == 3) ? ChatFormatting.LIGHT_PURPLE : (size == 2 ? ChatFormatting.GOLD : ChatFormatting.AQUA);
        pTooltipComponents.add(Component.literal("Size: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(sizeText).withStyle(sizeColor)));

        pTooltipComponents.add(Component.literal("Direction: ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(isVertical ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                        .withStyle(isVertical ? ChatFormatting.YELLOW : ChatFormatting.GREEN)));

        pTooltipComponents.add(Component.literal("Backplate: ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(withBackplate ? "gui.signbuilder.on" : "gui.signbuilder.off")
                        .withStyle(withBackplate ? ChatFormatting.GREEN : ChatFormatting.GRAY)));

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.blueprint.usage")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide()) {
                int size = 1;
                if (stack.getOrCreateTag().contains("Size")) size = stack.getOrCreateTag().getInt("Size");
                else if (stack.getOrCreateTag().getBoolean("Is2x2")) size = 2;

                boolean isVert = stack.getOrCreateTag().getBoolean("IsVertical");

                int nextSize;
                boolean nextVert;

                if (size == 1 && !isVert) {
                    nextSize = 1; nextVert = true;
                } else if (size == 1 && isVert) {
                    nextSize = 2; nextVert = false;
                } else if (size == 2 && !isVert) {
                    nextSize = 2; nextVert = true;
                } else if (size == 2 && isVert) {
                    nextSize = 3; nextVert = false;
                } else if (size == 3 && !isVert) {
                    nextSize = 3; nextVert = true;
                } else {
                    nextSize = 1; nextVert = false;
                }

                stack.getOrCreateTag().putInt("Size", nextSize);
                stack.getOrCreateTag().putBoolean("Is2x2", nextSize == 2);
                stack.getOrCreateTag().putBoolean("IsVertical", nextVert);

                String sizeStr = (nextSize == 3) ? "3x3" : (nextSize == 2 ? "2x2" : "1x1");
                ChatFormatting color = (nextSize == 3) ? ChatFormatting.LIGHT_PURPLE : (nextSize == 2 ? ChatFormatting.GOLD : ChatFormatting.AQUA);

                pPlayer.displayClientMessage(
                        Component.literal("Blueprint: ").withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(sizeStr).withStyle(color))
                                .append(Component.literal(" "))
                                .append(Component.translatable(nextVert ? "gui.signbuilder.blueprint.vertical" : "gui.signbuilder.blueprint.horizontal")
                                        .withStyle(nextVert ? ChatFormatting.YELLOW : ChatFormatting.GREEN)),
                        true
                );
                pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.2F);
            }
            return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
        }

        if (pLevel.isClientSide()) {
            pPlayer.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
            String currentText = "";
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("BlueprintText")) currentText = tag.getString("BlueprintText");
            String finalCurrentText = currentText;

            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.openBlueprintScreen(finalCurrentText));
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();

        if (level.isClientSide() || player == null) return InteractionResult.SUCCESS;

        String text = "";
        int size = 1;
        boolean isVertical = false;
        boolean withBackplate = false;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("BlueprintText")) text = tag.getString("BlueprintText");
            if (tag.contains("Size")) size = tag.getInt("Size");
            else if (tag.getBoolean("Is2x2")) size = 2;
            if (tag.contains("IsVertical")) isVertical = tag.getBoolean("IsVertical");
            if (tag.contains("WithBackplate")) withBackplate = tag.getBoolean("WithBackplate");
        }

        if (text.isEmpty()) {
            player.displayClientMessage(Component.translatable("tooltip.signbuilder.blueprint.empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        int blocksPerChar = (size == 3) ? 9 : (size == 2 ? 4 : 1);

        if (!player.isCreative()) {
            Map<Item, Integer> requiredItems = new HashMap<>();
            int totalBackplatesNeeded = 0;

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == ' ') continue;
                Block blockForChar = getBlockForChar(c);
                if (blockForChar != null) {
                    Item itemForChar = blockForChar.asItem();
                    requiredItems.put(itemForChar, requiredItems.getOrDefault(itemForChar, 0) + blocksPerChar);
                    if (withBackplate) {
                        totalBackplatesNeeded += blocksPerChar;
                    }
                }
            }

            if (withBackplate && totalBackplatesNeeded > 0) {
                Item bpItem = ModBlocks.BACKPLATE_ITEM.get();
                requiredItems.put(bpItem, requiredItems.getOrDefault(bpItem, 0) + totalBackplatesNeeded);
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

        int stepDirY = (clickedFace == Direction.UP) ? 1 : -1;
        int blocksPlaced = 0;
        List<Long> placedPositions = new ArrayList<>();

        int effectiveIdx = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') continue;

            Block blockToPlace = getBlockForChar(c);
            if (blockToPlace == null) continue;

            if (!isVertical) {
                if (size == 1) {
                    BlockPos currentPos = startPos.relative(rightDir, effectiveIdx);
                    if (!level.getBlockState(currentPos).canBeReplaced()) break;

                    placeSingleBlock(level, player, pContext, blockToPlace, currentPos, clickedFace, withBackplate);
                    blocksPlaced++;
                    placedPositions.add(currentPos.asLong());
                } else if (size == 2) {
                    BlockPos basePos = startPos.relative(rightDir, effectiveIdx * 2);
                    BlockPos p00 = basePos;
                    BlockPos p10 = basePos.relative(rightDir, 1);
                    BlockPos p01 = basePos.relative(Direction.UP, 1);
                    BlockPos p11 = basePos.relative(rightDir, 1).relative(Direction.UP, 1);

                    if (!level.getBlockState(p00).canBeReplaced() || !level.getBlockState(p10).canBeReplaced() ||
                            !level.getBlockState(p01).canBeReplaced() || !level.getBlockState(p11).canBeReplaced()) {
                        break;
                    }

                    BlockPos[] quad = {p00, p10, p01, p11};
                    for (BlockPos p : quad) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                } else {
                    BlockPos basePos = startPos.relative(rightDir, effectiveIdx * 3);
                    boolean canPlaceAll = true;
                    BlockPos[] grid = new BlockPos[9];
                    int gIdx = 0;
                    for (int dy = 0; dy < 3; dy++) {
                        for (int dx = 0; dx < 3; dx++) {
                            BlockPos p = basePos.relative(rightDir, dx).relative(Direction.UP, dy);
                            if (!level.getBlockState(p).canBeReplaced()) {
                                canPlaceAll = false;
                                break;
                            }
                            grid[gIdx++] = p;
                        }
                        if (!canPlaceAll) break;
                    }

                    if (!canPlaceAll) break;

                    for (BlockPos p : grid) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                }
            } else {
                if (size == 1) {
                    int posY = (stepDirY == 1)
                            ? startPos.getY() + effectiveIdx
                            : startPos.getY() - effectiveIdx;

                    BlockPos currentPos = new BlockPos(startPos.getX(), posY, startPos.getZ());
                    if (!level.getBlockState(currentPos).canBeReplaced()) break;

                    placeSingleBlock(level, player, pContext, blockToPlace, currentPos, clickedFace, withBackplate);
                    blocksPlaced++;
                    placedPositions.add(currentPos.asLong());
                } else if (size == 2) {
                    int baseY = (stepDirY == 1)
                            ? startPos.getY() + (effectiveIdx * 2)
                            : startPos.getY() - 1 - (effectiveIdx * 2);

                    BlockPos basePos = new BlockPos(startPos.getX(), baseY, startPos.getZ());
                    BlockPos p00 = basePos;
                    BlockPos p10 = basePos.relative(rightDir, 1);
                    BlockPos p01 = basePos.relative(Direction.UP, 1);
                    BlockPos p11 = basePos.relative(rightDir, 1).relative(Direction.UP, 1);

                    if (!level.getBlockState(p00).canBeReplaced() || !level.getBlockState(p10).canBeReplaced() ||
                            !level.getBlockState(p01).canBeReplaced() || !level.getBlockState(p11).canBeReplaced()) {
                        break;
                    }

                    BlockPos[] quad = {p00, p10, p01, p11};
                    for (BlockPos p : quad) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                } else {
                    int baseY = (stepDirY == 1)
                            ? startPos.getY() + (effectiveIdx * 3)
                            : startPos.getY() - 2 - (effectiveIdx * 3);

                    BlockPos basePos = new BlockPos(startPos.getX(), baseY, startPos.getZ());
                    boolean canPlaceAll = true;
                    BlockPos[] grid = new BlockPos[9];
                    int gIdx = 0;
                    for (int dy = 0; dy < 3; dy++) {
                        for (int dx = 0; dx < 3; dx++) {
                            BlockPos p = basePos.relative(rightDir, dx).relative(Direction.UP, dy);
                            if (!level.getBlockState(p).canBeReplaced()) {
                                canPlaceAll = false;
                                break;
                            }
                            grid[gIdx++] = p;
                        }
                        if (!canPlaceAll) break;
                    }

                    if (!canPlaceAll) break;

                    for (BlockPos p : grid) {
                        placeSingleBlock(level, player, pContext, blockToPlace, p, clickedFace, withBackplate);
                        placedPositions.add(p.asLong());
                        blocksPlaced++;
                    }
                }
            }
            effectiveIdx++;
        }

        if (blocksPlaced > 0) {
            level.playSound(null, startPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            long[] posArray = placedPositions.stream().mapToLong(l -> l).toArray();
            stack.getOrCreateTag().putLongArray("UndoHistory", posArray);

            if (player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
                if (size == 2) {
                    Advancement adv = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("signbuilder", "wide_format"));
                    if (adv != null) {
                        serverPlayer.getAdvancements().award(adv, "placed_2x2");
                    }
                } else if (size == 3) {
                    Advancement adv = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("signbuilder", "billboard"));
                    if (adv != null) {
                        serverPlayer.getAdvancements().award(adv, "placed_3x3");
                    }
                }
            }

            if (!player.isCreative()) stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
        }
        return InteractionResult.SUCCESS;
    }

    private void placeSingleBlock(Level level, Player player, UseOnContext pContext, Block blockToPlace, BlockPos pos, Direction clickedFace, boolean withBackplate) {
        BlockHitResult hitResult = new BlockHitResult(pContext.getClickLocation(), clickedFace, pos, pContext.isInside());
        UseOnContext offsetContext = new UseOnContext(player, pContext.getHand(), hitResult);
        BlockPlaceContext placeContext = new BlockPlaceContext(offsetContext);
        BlockState stateToPlace = blockToPlace.getStateForPlacement(placeContext);
        if (stateToPlace == null) stateToPlace = blockToPlace.defaultBlockState();

        level.setBlock(pos, stateToPlace, 3);
        blockToPlace.setPlacedBy(level, pos, stateToPlace, player, new ItemStack(blockToPlace));

        if (withBackplate) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LetterBlockEntity letterBe) {
                letterBe.setHasBackplate(true);
            }
        }
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

    public static Block getBlockForChar(char c) {
        Block cached = CHAR_BLOCK_CACHE.get(c);
        if (cached != null) return cached;

        String blockId = switch (c) {
            case 'A', 'a' -> "letter_a";
            case 'Ä', 'ä' -> "letter_a_de";
            case 'B', 'b' -> "letter_b";
            case 'C', 'c' -> "letter_c";
            case 'Ç', 'ç' -> "letter_c_tr";
            case 'D', 'd' -> "letter_d";
            case 'E', 'e' -> "letter_e";
            case 'F', 'f' -> "letter_f";
            case 'G', 'g' -> "letter_g";
            case 'Ğ', 'ğ' -> "letter_g_tr";
            case 'H', 'h' -> "letter_h";
            case 'I', 'i', 'ı' -> "letter_i";
            case 'İ' -> "letter_i_tr";
            case 'J', 'j' -> "letter_j";
            case 'K', 'k' -> "letter_k";
            case 'L', 'l' -> "letter_l";
            case 'M', 'm' -> "letter_m";
            case 'N', 'n' -> "letter_n";
            case 'O', 'o' -> "letter_o";
            case 'Ö', 'ö' -> "letter_o_tr";
            case 'P', 'p' -> "letter_p";
            case 'Q', 'q' -> "letter_q";
            case 'R', 'r' -> "letter_r";
            case 'S', 's' -> "letter_s";
            case 'Ş', 'ş' -> "letter_s_tr";
            case 'T', 't' -> "letter_t";
            case 'U', 'u' -> "letter_u";
            case 'Ü', 'ü' -> "letter_u_tr";
            case 'V', 'v' -> "letter_v";
            case 'W', 'w' -> "letter_w";
            case 'X', 'x' -> "letter_x";
            case 'Y', 'y' -> "letter_y";
            case 'Z', 'z' -> "letter_z";
            case 'ß' -> "letter_eszett";
            case '0' -> "number_0";
            case '1' -> "number_1"; case '2' -> "number_2"; case '3' -> "number_3"; case '4' -> "number_4";
            case '5' -> "number_5"; case '6' -> "number_6"; case '7' -> "number_7"; case '8' -> "number_8";
            case '9' -> "number_9";
            case '+' -> "symbol_plus"; case '-' -> "symbol_minus"; case '✗', '×' -> "symbol_cross"; case '/' -> "symbol_slash";
            case '\\' -> "symbol_backslash"; case '#' -> "symbol_hashtag";
            case '*' -> "symbol_asterisk"; case '★' -> "symbol_star";
            case '✓' -> "symbol_checkmark"; case '∞' -> "symbol_infinity";
            case '○', '●' -> "symbol_circle"; case '◆', '◇' -> "symbol_diamond";
            case '♪' -> "symbol_note"; case '♫' -> "symbol_note_double"; case '☠' -> "symbol_skull";
            case '♥' -> "symbol_heart"; case '€' -> "symbol_euro"; case '$' -> "symbol_dollar"; case '£' -> "symbol_pound";
            case '¥' -> "symbol_yen"; case '₺' -> "symbol_tl"; case '@' -> "symbol_at"; case '&' -> "symbol_ampersand";
            case ',' -> "symbol_comma"; case '%' -> "symbol_percent";
            case '<' -> "symbol_less_than"; case '>' -> "symbol_greater_than";
            case '«' -> "symbol_dot_left"; case '•' -> "symbol_dot_center"; case '»' -> "symbol_dot_right";
            case '(' -> "symbol_bracket_left"; case ')' -> "symbol_bracket_right"; case '|' -> "symbol_bracket_double";
            case '[' -> "symbol_square_bracket_left"; case ']' -> "symbol_square_bracket_right"; case '¦' -> "symbol_square_bracket_double";
            case '↑' -> "arrow_up"; case '↓' -> "arrow_down"; case '←' -> "arrow_left"; case '→' -> "arrow_right";
            case '↖' -> "arrow_left_up"; case '↗' -> "arrow_right_up"; case '↙' -> "arrow_left_down"; case '↘' -> "arrow_right_down";
            case ':' -> "symbol_colon"; case ';' -> "symbol_semicolon"; case '!' -> "symbol_exclamation";
            case '?' -> "symbol_question"; case '=' -> "symbol_equals"; case '÷' -> "symbol_divide";
            case '\'' -> "symbol_apostrophe"; case '"' -> "symbol_quotes";
            default -> null;
        };

        if (blockId != null) {
            Block targetBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("signbuilder", blockId));
            if (targetBlock != Blocks.AIR) {
                CHAR_BLOCK_CACHE.put(c, targetBlock);
                return targetBlock;
            }
        }
        return null;
    }
}