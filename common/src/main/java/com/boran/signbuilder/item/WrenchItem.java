package com.boran.signbuilder.item;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class WrenchItem extends Item {

    public static final String[] MOD_KEYS = {
            "gui.signbuilder.wrench.mode.normal", "gui.signbuilder.wrench.mode.blink", "gui.signbuilder.wrench.mode.flicker",
            "gui.signbuilder.wrench.mode.wave", "gui.signbuilder.wrench.mode.breathing", "gui.signbuilder.wrench.mode.proximity", "gui.signbuilder.wrench.mode.night_shift"
    };

    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        int currentMode = 0; boolean isSmartFill = false; boolean detectsMonsters = true; boolean detectsAnimals = false;

        CompoundTag tag = pStack.getTag();
        if (tag != null) {
            if (tag.contains("WrenchMode")) currentMode = tag.getInt("WrenchMode");
            if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
            if (tag.contains("DetectsMonsters")) detectsMonsters = tag.getBoolean("DetectsMonsters");
            if (tag.contains("DetectsAnimals")) detectsAnimals = tag.getBoolean("DetectsAnimals");
        }

        if (currentMode >= 0 && currentMode < MOD_KEYS.length) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(MOD_KEYS[currentMode]).withStyle(ChatFormatting.AQUA)));
            if (currentMode == 5) {
                pTooltipComponents.add(Component.translatable(detectsMonsters ? "gui.signbuilder.wrench.monster_toggle_on" : "gui.signbuilder.wrench.monster_toggle_off").withStyle(detectsMonsters ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
                pTooltipComponents.add(Component.translatable(detectsAnimals ? "gui.signbuilder.wrench.animal_toggle_on" : "gui.signbuilder.wrench.animal_toggle_off").withStyle(detectsAnimals ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_GRAY));
            }
        } else if (currentMode == -1) {
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED)));
        }

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.brush.smart_fill").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(isSmartFill ? "gui.signbuilder.on" : "gui.signbuilder.off").withStyle(isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide()) {
                boolean isSmartFill = stack.getOrCreateTag().getBoolean("IsSmartFill");
                stack.getOrCreateTag().putBoolean("IsSmartFill", !isSmartFill);
                pPlayer.displayClientMessage(Component.translatable("message.signbuilder.wrench.smart_fill_toggle").withStyle(ChatFormatting.YELLOW).append(Component.translatable(!isSmartFill ? "gui.signbuilder.on" : "gui.signbuilder.off").withStyle(!isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
                pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, !isSmartFill ? 1.5F : 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
        }

        if (pLevel.isClientSide()) {
            int cMode = 0; boolean dMonsters = true; boolean dAnimals = false;
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                if (tag.contains("WrenchMode")) cMode = tag.getInt("WrenchMode");
                if (tag.contains("DetectsMonsters")) dMonsters = tag.getBoolean("DetectsMonsters");
                if (tag.contains("DetectsAnimals")) dAnimals = tag.getBoolean("DetectsAnimals");
            }
            pPlayer.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.2F);
            int fc = cMode; boolean fm = dMonsters; boolean fa = dAnimals;
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> com.boran.signbuilder.client.ClientHooks.openWrenchScreen(fc, fm, fa));
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        Level level = pContext.getLevel(); BlockPos pos = pContext.getClickedPos(); BlockState clickedBlock = level.getBlockState(pos);
        Player player = pContext.getPlayer(); ItemStack stack = pContext.getItemInHand();

        if (clickedBlock.hasProperty(ModBlocks.GLOWING)) {
            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    if (level.getBlockEntity(pos) instanceof LetterBlockEntity letterEntity) {
                        int copiedMode = letterEntity.getWrenchMode();
                        stack.getOrCreateTag().putInt("WrenchMode", copiedMode);
                        stack.getOrCreateTag().putBoolean("DetectsMonsters", letterEntity.doesDetectMonsters());
                        stack.getOrCreateTag().putBoolean("DetectsAnimals", letterEntity.doesDetectAnimals());
                        player.displayClientMessage(Component.translatable("message.signbuilder.wrench.mode_copied").withStyle(ChatFormatting.YELLOW).append(copiedMode == -1 ? Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED) : Component.translatable(MOD_KEYS[copiedMode]).withStyle(ChatFormatting.AQUA)), true);
                        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                int mode = 0; boolean isSmartFill = false; boolean detectsMonsters = true; boolean detectsAnimals = false;
                CompoundTag tag = stack.getTag();
                if (tag != null) {
                    if (tag.contains("WrenchMode")) mode = tag.getInt("WrenchMode");
                    if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
                    if (tag.contains("DetectsMonsters")) detectsMonsters = tag.getBoolean("DetectsMonsters");
                    if (tag.contains("DetectsAnimals")) detectsAnimals = tag.getBoolean("DetectsAnimals");
                }

                if (level.getBlockEntity(pos) instanceof LetterBlockEntity letterEntity) {
                    boolean wasActive = letterEntity.isActive();
                    boolean targetActive = (mode != -1) && (letterEntity.getWrenchMode() != mode || !wasActive);

                    if (isSmartFill) {
                        applyModeToConnected(level, pos, player, stack, pContext.getHand(), mode, targetActive, detectsMonsters, detectsAnimals);
                    } else {
                        if (player != null && !player.isCreative()) {
                            if (!wasActive && targetActive) {
                                if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= 1) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, 1);
                                else {
                                    player.displayClientMessage(Component.translatable("message.signbuilder.missing_material").withStyle(ChatFormatting.RED), true);
                                    player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                    return InteractionResult.FAIL;
                                }
                            } else if (wasActive && !targetActive) {
                                ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, 1);
                                if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                            }
                            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
                        }

                        if (mode == -1) {
                            letterEntity.setWrenchMode(0); letterEntity.setActive(false);
                            level.setBlock(pos, clickedBlock.setValue(ModBlocks.GLOWING, false), 2);
                        } else {
                            letterEntity.setWrenchMode(mode); letterEntity.setActive(targetActive);
                            if (mode == 5) { letterEntity.setDetectsMonsters(detectsMonsters); letterEntity.setDetectsAnimals(detectsAnimals); }
                            level.setBlock(pos, clickedBlock.setValue(ModBlocks.GLOWING, mode == 0 ? targetActive : false), 2);
                        }
                    }
                }
                level.playSound(null, pos, SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private void applyModeToConnected(Level level, BlockPos startPos, Player player, ItemStack stack, InteractionHand hand, int mode, boolean targetActive, boolean detectsMonsters, boolean detectsAnimals) {
        List<BlockPos> targets = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 256) {
            BlockPos current = queue.poll();
            if (level.getBlockEntity(current) instanceof LetterBlockEntity) {
                targets.add(current);
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && level.getBlockEntity(neighbor) instanceof LetterBlockEntity) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        int blocksModified = 0; int failedMaterial = 0; int failedDurability = 0;
        int currentDamage = stack.getDamageValue(); int maxDamage = stack.getMaxDamage();

        for (BlockPos current : targets) {
            if (player != null && !player.isCreative() && (currentDamage + blocksModified) >= maxDamage) {
                failedDurability++;
                continue;
            }

            LetterBlockEntity letter = (LetterBlockEntity) level.getBlockEntity(current);
            BlockState currentState = level.getBlockState(current);
            boolean wasActive = letter.isActive();
            boolean willBeActive = (mode != -1) && targetActive;

            if (player != null && !player.isCreative()) {
                if (!wasActive && willBeActive) {
                    if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= 1) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, 1);
                    else { failedMaterial++; continue; }
                } else if (wasActive && !willBeActive) {
                    ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, 1);
                    if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                }
            }

            if (mode == -1) {
                letter.setWrenchMode(0); letter.setActive(false);
                level.setBlock(current, currentState.setValue(ModBlocks.GLOWING, false), 2);
            } else {
                letter.setWrenchMode(mode); letter.setActive(willBeActive);
                if (mode == 5) { letter.setDetectsMonsters(detectsMonsters); letter.setDetectsAnimals(detectsAnimals); }
                level.setBlock(current, currentState.setValue(ModBlocks.GLOWING, mode == 0 ? willBeActive : false), 2);
            }
            blocksModified++;
        }

        if (player != null && !level.isClientSide()) {
            if (blocksModified > 0 && !player.isCreative()) stack.hurtAndBreak(blocksModified, player, (p) -> p.broadcastBreakEvent(hand));

            if (failedMaterial > 0) {
                player.displayClientMessage(Component.translatable("message.signbuilder.smart_fill.partial_material", blocksModified, failedMaterial).withStyle(ChatFormatting.YELLOW), true);
                player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            } else if (failedDurability > 0) {
                player.displayClientMessage(Component.translatable("message.signbuilder.smart_fill.partial_durability", blocksModified, failedDurability).withStyle(ChatFormatting.YELLOW), true);
            } else if (blocksModified > 0) {
                player.displayClientMessage(Component.translatable("message.signbuilder.smart_fill.success", blocksModified).withStyle(ChatFormatting.GREEN), true);
            }
        }
    }

    private int countItemInInventory(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() == item) count += s.getCount();
        }
        return count;
    }

    private void consumeItemFromInventory(Player player, Item item, int amount) {
        int amountLeft = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() == item) {
                if (s.getCount() >= amountLeft) { s.shrink(amountLeft); break; }
                else { amountLeft -= s.getCount(); s.setCount(0); }
            }
        }
    }
}