package com.boran.signbuilder.item;

import com.boran.signbuilder.block.LetterBlock;
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
            "gui.signbuilder.wrench.mode.wave", "gui.signbuilder.wrench.mode.breathing", "gui.signbuilder.wrench.mode.proximity",
            "gui.signbuilder.wrench.mode.night_shift", "gui.signbuilder.wrench.mode.audio_sync", "gui.signbuilder.wrench.mode.disco",
            "gui.signbuilder.wrench.mode.eye_contact", "gui.signbuilder.wrench.mode.low_power"
    };

    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        int currentMode = 0;
        boolean isSmartFill = false;
        boolean detectsMonsters = true;
        boolean detectsAnimals = false;
        int buttonMode = 0;
        boolean syncWord = false;
        int activeTab = 0;
        String pinCode = "";

        CompoundTag tag = pStack.getTag();
        if (tag != null) {
            if (tag.contains("WrenchMode")) currentMode = tag.getInt("WrenchMode");
            if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
            if (tag.contains("DetectsMonsters")) detectsMonsters = tag.getBoolean("DetectsMonsters");
            if (tag.contains("DetectsAnimals")) detectsAnimals = tag.getBoolean("DetectsAnimals");
            if (tag.contains("ButtonMode")) buttonMode = tag.getInt("ButtonMode");
            if (tag.contains("SyncWord")) syncWord = tag.getBoolean("SyncWord");
            if (tag.contains("ActiveTab")) activeTab = tag.getInt("ActiveTab");
            if (tag.contains("PinCode")) pinCode = tag.getString("PinCode");
        }

        if (tag != null && tag.getBoolean("IsRecordingPin")) {
            String rec = tag.getString("RecordingPin");
            pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.recording_pin", rec).withStyle(ChatFormatting.RED));
        }

        if (activeTab == 0) {
            if (currentMode >= 0 && currentMode < MOD_KEYS.length) {
                pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(MOD_KEYS[currentMode]).withStyle(ChatFormatting.AQUA)));
                if (currentMode == 5) {
                    pTooltipComponents.add(Component.translatable(detectsMonsters ? "gui.signbuilder.wrench.monster_toggle_on" : "gui.signbuilder.wrench.monster_toggle_off").withStyle(detectsMonsters ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
                    pTooltipComponents.add(Component.translatable(detectsAnimals ? "gui.signbuilder.wrench.animal_toggle_on" : "gui.signbuilder.wrench.animal_toggle_off").withStyle(detectsAnimals ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_GRAY));
                }
            } else if (currentMode == -1) {
                pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED)));
            }
        } else {
            String btnKey = switch (buttonMode) {
                case 1 -> "gui.signbuilder.wrench.btn.pulse";
                case 2 -> "gui.signbuilder.wrench.btn.toggle";
                case 3 -> "gui.signbuilder.wrench.btn.pin";
                default -> "gui.signbuilder.wrench.btn.disabled";
            };
            pTooltipComponents.add(Component.translatable("gui.signbuilder.wrench.section.button_mode").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(btnKey).withStyle(ChatFormatting.YELLOW)));

            if (buttonMode == 3 && !pinCode.isEmpty()) {
                pTooltipComponents.add(Component.translatable("gui.signbuilder.wrench.pin.title").withStyle(ChatFormatting.GRAY).append(" ").append(Component.literal(pinCode).withStyle(ChatFormatting.GOLD)));
            }

            if (buttonMode != 0) {
                pTooltipComponents.add(Component.translatable("gui.signbuilder.wrench.scope.title").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(syncWord ? "gui.signbuilder.wrench.scope.word" : "gui.signbuilder.wrench.scope.single").withStyle(syncWord ? ChatFormatting.GREEN : ChatFormatting.GRAY)));
            }
        }

        pTooltipComponents.add(Component.translatable("tooltip.signbuilder.brush.smart_fill").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(isSmartFill ? "gui.signbuilder.on" : "gui.signbuilder.off").withStyle(isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    private void finishPinRecording(ItemStack stack, Player player, Level level) {
        stack.getOrCreateTag().putBoolean("IsRecordingPin", false);
        String recorded = stack.getOrCreateTag().getString("RecordingPin");
        stack.getOrCreateTag().remove("RecordingPin");
        if (!recorded.isEmpty()) {
            stack.getOrCreateTag().putString("PinCode", recorded);
            player.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.record_saved", recorded).withStyle(ChatFormatting.GREEN), true);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.2F);
        } else {
            player.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.record_cancelled").withStyle(ChatFormatting.RED), true);
            level.playSound(null, player.blockPosition(), SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if (stack.getOrCreateTag().getBoolean("IsRecordingPin")) {
            if (pPlayer.isShiftKeyDown()) {
                if (!pLevel.isClientSide()) {
                    finishPinRecording(stack, pPlayer, pLevel);
                }
                return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
            } else {
                if (!pLevel.isClientSide()) {
                    String current = stack.getOrCreateTag().getString("RecordingPin");
                    pPlayer.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.recording_hint", current).withStyle(ChatFormatting.YELLOW), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
            }
        }

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
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState clickedBlock = level.getBlockState(pos);
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();

        if (stack.getOrCreateTag().getBoolean("IsRecordingPin")) {
            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    finishPinRecording(stack, player, level);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (clickedBlock.getBlock() instanceof LetterBlock) {
                if (!level.isClientSide()) {
                    String symbol = LetterBlock.getCharacterFromBlock(clickedBlock.getBlock());
                    String current = stack.getOrCreateTag().getString("RecordingPin");
                    if (current.length() < 16) {
                        current += symbol;
                        stack.getOrCreateTag().putString("RecordingPin", current);
                        level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
                        if (player != null) {
                            player.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.recorded_char", symbol, current).withStyle(ChatFormatting.GOLD), true);
                        }
                    } else {
                        if (player != null) {
                            player.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.max_length").withStyle(ChatFormatting.RED), true);
                        }
                        level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.PLAYERS, 1.0F, 0.8F);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return InteractionResult.PASS;
        }

        if (clickedBlock.getBlock() instanceof LetterBlock && level.getBlockEntity(pos) instanceof LetterBlockEntity rawEntity) {
            LetterBlockEntity letterEntity = rawEntity.getEffectiveMaster();
            BlockPos targetPos = letterEntity.getBlockPos();
            BlockState targetState = level.getBlockState(targetPos);

            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    int copiedMode = letterEntity.getWrenchMode();
                    stack.getOrCreateTag().putInt("WrenchMode", copiedMode);
                    stack.getOrCreateTag().putBoolean("DetectsMonsters", letterEntity.doesDetectMonsters());
                    stack.getOrCreateTag().putBoolean("DetectsAnimals", letterEntity.doesDetectAnimals());
                    stack.getOrCreateTag().putInt("ButtonMode", letterEntity.getButtonMode());
                    stack.getOrCreateTag().putBoolean("SyncWord", letterEntity.isSyncWord());
                    stack.getOrCreateTag().putString("PinCode", letterEntity.getPinCode());

                    player.displayClientMessage(Component.translatable("message.signbuilder.wrench.mode_copied").withStyle(ChatFormatting.YELLOW).append(copiedMode == -1 ? Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED) : Component.translatable(MOD_KEYS[copiedMode]).withStyle(ChatFormatting.AQUA)), true);
                    level.playSound(null, targetPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                int mode = 0; boolean isSmartFill = false; boolean detectsMonsters = true; boolean detectsAnimals = false;
                int buttonMode = 0; boolean syncWord = false; int activeTab = 0;
                String pinCode = "";

                CompoundTag tag = stack.getTag();
                if (tag != null) {
                    if (tag.contains("WrenchMode")) mode = tag.getInt("WrenchMode");
                    if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
                    if (tag.contains("DetectsMonsters")) detectsMonsters = tag.getBoolean("DetectsMonsters");
                    if (tag.contains("DetectsAnimals")) detectsAnimals = tag.getBoolean("DetectsAnimals");
                    if (tag.contains("ButtonMode")) buttonMode = tag.getInt("ButtonMode");
                    if (tag.contains("SyncWord")) syncWord = tag.getBoolean("SyncWord");
                    if (tag.contains("ActiveTab")) activeTab = tag.getInt("ActiveTab");
                    if (tag.contains("PinCode")) pinCode = tag.getString("PinCode");
                }

                if (activeTab == 0) {
                    boolean wasActive = letterEntity.isActive();
                    boolean targetActive = (mode != -1);

                    boolean noChange = (mode == -1 ? (!wasActive && letterEntity.getWrenchMode() == 0) : (wasActive && letterEntity.getWrenchMode() == mode))
                            && (mode != 5 || (letterEntity.doesDetectMonsters() == detectsMonsters && letterEntity.doesDetectAnimals() == detectsAnimals));

                    if (noChange && !isSmartFill) {
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }

                    if (isSmartFill) {
                        applyLightModeToConnected(level, targetPos, player, stack, pContext.getHand(), mode, targetActive, detectsMonsters, detectsAnimals);
                    } else {
                        int dustCost = letterEntity.isBig() ? 4 : 1;

                        if (player != null && !player.isCreative()) {
                            if (!wasActive && targetActive) {
                                if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= dustCost) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, dustCost);
                                else {
                                    player.displayClientMessage(Component.translatable("message.signbuilder.missing_material").withStyle(ChatFormatting.RED), true);
                                    player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                    return InteractionResult.FAIL;
                                }
                            } else if (wasActive && !targetActive) {
                                ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, dustCost);
                                if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                            }
                            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
                        }

                        if (mode == -1) {
                            letterEntity.setWrenchMode(0);
                            letterEntity.setActive(false);
                        } else {
                            letterEntity.setWrenchMode(mode);
                            letterEntity.setActive(true);
                            if (mode == 5) {
                                letterEntity.setDetectsMonsters(detectsMonsters);
                                letterEntity.setDetectsAnimals(detectsAnimals);
                            }
                        }

                        letterEntity.setChanged();
                        letterEntity.sync();
                        LetterBlock.updateLightLevel(level, targetPos, targetState, letterEntity);
                        level.sendBlockUpdated(targetPos, targetState, targetState, 3);
                    }
                } else {
                    if (buttonMode == 3 || isSmartFill || syncWord) {
                        applyButtonModeToConnected(level, targetPos, player, stack, pContext.getHand(), buttonMode, syncWord, pinCode);
                    } else {
                        if (letterEntity.getButtonMode() == 2 && letterEntity.isPressed()) {
                            if (letterEntity.getButtonMode() != buttonMode || letterEntity.isSyncWord() != syncWord) {
                                if (player != null) {
                                    player.displayClientMessage(Component.translatable("message.signbuilder.wrench.toggle_active_warning").withStyle(ChatFormatting.RED), true);
                                    player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                }
                                return InteractionResult.FAIL;
                            }
                        }

                        boolean noChange = letterEntity.getButtonMode() == buttonMode && letterEntity.isSyncWord() == syncWord && letterEntity.getPinCode().equals(pinCode);
                        if (noChange) {
                            return InteractionResult.sidedSuccess(level.isClientSide());
                        }

                        if (player != null && !player.isCreative()) {
                            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(pContext.getHand()));
                        }
                        letterEntity.setButtonMode(buttonMode);
                        letterEntity.setSyncWord(syncWord);
                        letterEntity.setPinCode(pinCode);
                        letterEntity.setChanged();
                        letterEntity.sync();
                        level.sendBlockUpdated(targetPos, targetState, targetState, 3);
                    }
                }

                level.playSound(null, targetPos, SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private void applyLightModeToConnected(Level level, BlockPos startPos, Player player, ItemStack stack, InteractionHand hand, int mode, boolean targetActive, boolean detectsMonsters, boolean detectsAnimals) {
        List<BlockPos> targets = getConnectedBlocks(level, startPos);
        int blocksModified = 0; int failedMaterial = 0; int failedDurability = 0;
        int currentDamage = stack.getDamageValue(); int maxDamage = stack.getMaxDamage();

        for (BlockPos current : targets) {
            if (player != null && !player.isCreative() && (currentDamage + blocksModified) >= maxDamage) {
                failedDurability++;
                continue;
            }

            LetterBlockEntity rawLetter = (LetterBlockEntity) level.getBlockEntity(current);
            if (rawLetter == null) continue;
            LetterBlockEntity letter = rawLetter.getEffectiveMaster();
            BlockPos effectivePos = letter.getBlockPos();
            BlockState currentState = level.getBlockState(effectivePos);

            boolean wasActive = letter.isActive();

            boolean blockNoChange = (mode == -1 ? (!wasActive && letter.getWrenchMode() == 0) : (wasActive && letter.getWrenchMode() == mode))
                    && (mode != 5 || (letter.doesDetectMonsters() == detectsMonsters && letter.doesDetectAnimals() == detectsAnimals));

            if (blockNoChange) continue;

            int dustCost = letter.isBig() ? 4 : 1;

            if (player != null && !player.isCreative()) {
                if (!wasActive && targetActive) {
                    if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= dustCost) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, dustCost);
                    else { failedMaterial++; continue; }
                } else if (wasActive && !targetActive) {
                    ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, dustCost);
                    if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                }
            }

            if (mode == -1) {
                letter.setWrenchMode(0);
                letter.setActive(false);
            } else {
                letter.setWrenchMode(mode);
                letter.setActive(true);
                if (mode == 5) {
                    letter.setDetectsMonsters(detectsMonsters);
                    letter.setDetectsAnimals(detectsAnimals);
                }
            }

            letter.setChanged();
            letter.sync();
            LetterBlock.updateLightLevel(level, effectivePos, currentState, letter);
            level.sendBlockUpdated(effectivePos, currentState, currentState, 3);
            blocksModified++;
        }

        handleSmartFillFeedback(level, player, stack, hand, blocksModified, failedMaterial, failedDurability);
    }

    private void applyButtonModeToConnected(Level level, BlockPos startPos, Player player, ItemStack stack, InteractionHand hand, int buttonMode, boolean syncWord, String pinCode) {
        List<BlockPos> targets = getConnectedBlocks(level, startPos);
        int blocksModified = 0; int failedDurability = 0; int failedActiveToggle = 0;
        int currentDamage = stack.getDamageValue(); int maxDamage = stack.getMaxDamage();

        for (BlockPos current : targets) {
            if (player != null && !player.isCreative() && (currentDamage + blocksModified) >= maxDamage) {
                failedDurability++;
                continue;
            }

            LetterBlockEntity rawLetter = (LetterBlockEntity) level.getBlockEntity(current);
            if (rawLetter == null) continue;
            LetterBlockEntity letter = rawLetter.getEffectiveMaster();
            BlockPos effectivePos = letter.getBlockPos();
            BlockState currentState = level.getBlockState(effectivePos);

            if (letter.getButtonMode() == 2 && letter.isPressed()) {
                if (letter.getButtonMode() != buttonMode || letter.isSyncWord() != syncWord) {
                    failedActiveToggle++;
                    continue;
                }
            }

            if (letter.getButtonMode() == buttonMode && letter.isSyncWord() == syncWord && letter.getPinCode().equals(pinCode)) {
                continue;
            }

            letter.setButtonMode(buttonMode);
            letter.setSyncWord(syncWord);
            letter.setPinCode(pinCode);
            letter.setChanged();
            letter.sync();
            level.sendBlockUpdated(effectivePos, currentState, currentState, 3);
            blocksModified++;
        }

        if (failedActiveToggle > 0 && player != null && !level.isClientSide()) {
            player.displayClientMessage(Component.translatable("message.signbuilder.wrench.toggle_active_warning").withStyle(ChatFormatting.RED), true);
            player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
        }

        handleSmartFillFeedback(level, player, stack, hand, blocksModified, 0, failedDurability);
    }

    private List<BlockPos> getConnectedBlocks(Level level, BlockPos startPos) {
        List<BlockPos> targets = new ArrayList<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && visited.size() <= 256) {
            BlockPos current = queue.poll();
            if (level.getBlockEntity(current) instanceof LetterBlockEntity be) {
                BlockPos effectivePos = be.getEffectiveMaster().getBlockPos();
                if (!targets.contains(effectivePos)) {
                    targets.add(effectivePos);
                }
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos neighbor = current.offset(dx, dy, dz);
                            if (!visited.contains(neighbor) && level.getBlockState(neighbor).getBlock() instanceof LetterBlock) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        return targets;
    }

    private void handleSmartFillFeedback(Level level, Player player, ItemStack stack, InteractionHand hand, int blocksModified, int failedMaterial, int failedDurability) {
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
                if (s.getCount() >= amountLeft) {
                    s.shrink(amountLeft);
                    break;
                } else {
                    amountLeft -= s.getCount();
                    s.setCount(0);
                }
            }
        }
    }
}