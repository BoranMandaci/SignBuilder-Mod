package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.screen.WrenchScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.item.Items;
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

public class WrenchItem extends Item {

    public static final String[] MOD_KEYS = {
            "gui.signbuilder.wrench.mode.normal",
            "gui.signbuilder.wrench.mode.blink",
            "gui.signbuilder.wrench.mode.flicker",
            "gui.signbuilder.wrench.mode.wave",
            "gui.signbuilder.wrench.mode.breathing",
            "gui.signbuilder.wrench.mode.proximity",
            "gui.signbuilder.wrench.mode.night_shift"
    };

    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        int currentMode = 0;
        boolean isSmartFill = false;
        boolean detectsMonsters = true;
        boolean detectsAnimals = false;

        if (pStack.hasTag()) {
            if (pStack.getTag().contains("WrenchMode")) currentMode = pStack.getTag().getInt("WrenchMode");
            if (pStack.getTag().contains("IsSmartFill")) isSmartFill = pStack.getTag().getBoolean("IsSmartFill");
            if (pStack.getTag().contains("DetectsMonsters")) detectsMonsters = pStack.getTag().getBoolean("DetectsMonsters");
            if (pStack.getTag().contains("DetectsAnimals")) detectsAnimals = pStack.getTag().getBoolean("DetectsAnimals");
        }

        if (currentMode >= 0 && currentMode < MOD_KEYS.length) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": "))
                    .append(Component.translatable(MOD_KEYS[currentMode]).withStyle(ChatFormatting.AQUA)));

            if (currentMode == 5) {
                pTooltipComponents.add(Component.translatable(detectsMonsters ? "gui.signbuilder.wrench.monster_toggle_on" : "gui.signbuilder.wrench.monster_toggle_off")
                        .withStyle(detectsMonsters ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
                pTooltipComponents.add(Component.translatable(detectsAnimals ? "gui.signbuilder.wrench.animal_toggle_on" : "gui.signbuilder.wrench.animal_toggle_off")
                        .withStyle(detectsAnimals ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_GRAY));
            }
        } else if (currentMode == -1) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": "))
                    .append(Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED)));
        }

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.smart_fill")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "))
                .append(Component.translatable(isSmartFill ? "gui.signbuilder.wrench.state_on" : "gui.signbuilder.wrench.state_off")
                        .withStyle(isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)));

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.usage")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.copy_usage")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide()) {
                boolean isSmartFill = stack.getOrCreateTag().getBoolean("IsSmartFill");
                stack.getOrCreateTag().putBoolean("IsSmartFill", !isSmartFill);

                pPlayer.displayClientMessage(
                        Component.translatable("message.signbuilder.wrench.smart_fill_toggle")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.translatable(!isSmartFill ? "gui.signbuilder.wrench.state_on" : "gui.signbuilder.wrench.state_off")
                                        .withStyle(!isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)),
                        true
                );

                pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.5F, !isSmartFill ? 1.5F : 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
        }

        if (pLevel.isClientSide()) {
            int currentMode = 0;
            boolean detectsMonsters = true;
            boolean detectsAnimals = false;

            if (stack.hasTag()) {
                if (stack.getTag().contains("WrenchMode")) currentMode = stack.getTag().getInt("WrenchMode");
                if (stack.getTag().contains("DetectsMonsters")) detectsMonsters = stack.getTag().getBoolean("DetectsMonsters");
                if (stack.getTag().contains("DetectsAnimals")) detectsAnimals = stack.getTag().getBoolean("DetectsAnimals");
            }

            pPlayer.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.5F, 1.2F);

            int finalCurrentMode = currentMode;
            boolean finalDetectsMonsters = detectsMonsters;
            boolean finalDetectsAnimals = detectsAnimals;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new WrenchScreen(finalCurrentMode, finalDetectsMonsters, finalDetectsAnimals));
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState clickedBlock = level.getBlockState(pos);
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();

        if (clickedBlock.hasProperty(ModBlocks.GLOWING)) {
            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof LetterBlockEntity letterEntity) {
                        int copiedMode = letterEntity.getWrenchMode();
                        boolean copiedDetectsMonsters = letterEntity.doesDetectMonsters();
                        boolean copiedDetectsAnimals = letterEntity.doesDetectAnimals();

                        stack.getOrCreateTag().putInt("WrenchMode", copiedMode);
                        stack.getOrCreateTag().putBoolean("DetectsMonsters", copiedDetectsMonsters);
                        stack.getOrCreateTag().putBoolean("DetectsAnimals", copiedDetectsAnimals);

                        player.displayClientMessage(
                                Component.translatable("message.signbuilder.wrench.mode_copied")
                                        .withStyle(ChatFormatting.YELLOW)
                                        .append(copiedMode == -1 ?
                                                Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED) :
                                                Component.translatable(MOD_KEYS[copiedMode]).withStyle(ChatFormatting.AQUA)),
                                true
                        );
                        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                int mode = 0;
                boolean isSmartFill = false;
                boolean detectsMonsters = true;
                boolean detectsAnimals = false;

                CompoundTag tag = stack.getTag();
                if (tag != null) {
                    if (tag.contains("WrenchMode")) mode = tag.getInt("WrenchMode");
                    if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
                    if (tag.contains("DetectsMonsters")) detectsMonsters = tag.getBoolean("DetectsMonsters");
                    if (tag.contains("DetectsAnimals")) detectsAnimals = tag.getBoolean("DetectsAnimals");
                }

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof LetterBlockEntity letterEntity) {
                    boolean wasActive = letterEntity.isActive();
                    boolean targetActive = wasActive;

                    if (mode != -1) {
                        if (letterEntity.getWrenchMode() == mode) targetActive = !wasActive;
                        else targetActive = true;
                    } else {
                        targetActive = false; 
                    }

                    if (isSmartFill) {
                        applyModeToConnected(level, pos, player, mode, targetActive, detectsMonsters, detectsAnimals);
                    } else {
                        if (player != null && !player.isCreative()) {
                            if (!wasActive && targetActive) {
                                if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= 1) {
                                    consumeItemFromInventory(player, Items.GLOWSTONE_DUST, 1);
                                } else {
                                    player.displayClientMessage(Component.translatable("message.signbuilder.wrench.no_glowstone").withStyle(ChatFormatting.RED), true);
                                    player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                    return InteractionResult.FAIL;
                                }
                            } else if (wasActive && !targetActive) {
                                ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, 1);
                                if (!player.getInventory().add(returnDust)) {
                                    player.drop(returnDust, false);
                                }
                            }
                        }

                        if (mode == -1) {
                            letterEntity.setWrenchMode(0);
                            letterEntity.setActive(false);
                            level.setBlock(pos, clickedBlock.setValue(ModBlocks.GLOWING, false), 2);
                        } else {
                            letterEntity.setWrenchMode(mode);
                            letterEntity.setActive(targetActive);
                            if (mode == 5) {
                                letterEntity.setDetectsMonsters(detectsMonsters);
                                letterEntity.setDetectsAnimals(detectsAnimals);
                            }

                            if (mode == 0) {
                                level.setBlock(pos, clickedBlock.setValue(ModBlocks.GLOWING, targetActive), 2);
                            } else if (!targetActive) {
                                level.setBlock(pos, clickedBlock.setValue(ModBlocks.GLOWING, false), 2);
                            }
                        }
                    }
                }
                level.playSound(null, pos, SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
            }

            if (player != null && !player.isCreative()) {
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private void applyModeToConnected(Level level, BlockPos startPos, Player player, int mode, boolean targetActive, boolean detectsMonsters, boolean detectsAnimals) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(startPos);
        visited.add(startPos);

        boolean ranOutOfDust = false;

        while (!queue.isEmpty() && visited.size() < 256) {
            BlockPos current = queue.poll();
            BlockEntity be = level.getBlockEntity(current);

            if (be instanceof LetterBlockEntity letter) {
                BlockState currentState = level.getBlockState(current);

                boolean wasActive = letter.isActive();
                boolean willBeActive = (mode != -1) && targetActive;

                if (player != null && !player.isCreative()) {
                    if (!wasActive && willBeActive) {
                        if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= 1) {
                            consumeItemFromInventory(player, Items.GLOWSTONE_DUST, 1);
                        } else {
                            ranOutOfDust = true;
                            break;
                        }
                    } else if (wasActive && !willBeActive) {
                        ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, 1);
                        if (!player.getInventory().add(returnDust)) {
                            player.drop(returnDust, false);
                        }
                    }
                }

                if (mode == -1) {
                    letter.setWrenchMode(0);
                    letter.setActive(false);
                    level.setBlock(current, currentState.setValue(ModBlocks.GLOWING, false), 2);
                } else {
                    letter.setWrenchMode(mode);
                    letter.setActive(willBeActive);
                    if (mode == 5) {
                        letter.setDetectsMonsters(detectsMonsters);
                        letter.setDetectsAnimals(detectsAnimals);
                    }

                    if (mode == 0) {
                        level.setBlock(current, currentState.setValue(ModBlocks.GLOWING, willBeActive), 2);
                    } else if (!willBeActive) {
                        level.setBlock(current, currentState.setValue(ModBlocks.GLOWING, false), 2);
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

        if (player != null) {
            if (ranOutOfDust) {
                player.displayClientMessage(Component.translatable("message.signbuilder.wrench.no_glowstone").withStyle(ChatFormatting.RED), true);
                player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.signbuilder.wrench.smart_fill_success").withStyle(ChatFormatting.GREEN), true
                );
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
}