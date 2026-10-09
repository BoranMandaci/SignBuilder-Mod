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
import net.minecraft.world.level.block.Block;
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

    public WrenchItem(Properties pProperties) {
        super(pProperties);
    }

    private static String getModeTranslationKey(int mode) {
        return switch (mode) {
            case 0 -> "gui.signbuilder.wrench.mode.normal";
            case 1 -> "gui.signbuilder.wrench.mode.blink";
            case 2 -> "gui.signbuilder.wrench.mode.flicker";
            case 3 -> "gui.signbuilder.wrench.mode.wave";
            case 4 -> "gui.signbuilder.wrench.mode.breathing";
            case 7 -> "gui.signbuilder.wrench.mode.audio_sync";
            case 8 -> "gui.signbuilder.wrench.mode.disco";
            case 10 -> "gui.signbuilder.wrench.mode.low_power";
            case 11 -> "gui.signbuilder.wrench.mode.custom";
            default -> "gui.signbuilder.wrench.mode.legacy";
        };
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @NotNull net.minecraft.world.item.Item.TooltipContext pLevel, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pIsAdvanced) {
        int currentMode = 0;
        boolean isSmartFill = false;
        boolean detectsMonsters = true;
        boolean detectsAnimals = false;
        int buttonMode = 0;
        boolean syncWord = false;
        int activeTab = 0;
        String pinCode = "";

        CompoundTag tag = pStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
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
            if (currentMode >= 0) {
                pTooltipComponents.add(Component.translatable("tooltip.signbuilder.wrench.current_mode").withStyle(ChatFormatting.GRAY).append(": ").append(Component.translatable(getModeTranslationKey(currentMode)).withStyle(ChatFormatting.AQUA)));
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
                case 4 -> "gui.signbuilder.wrench.btn.hold";
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
        
    }

    private void finishPinRecording(ItemStack stack, Player player, Level level) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean("IsRecordingPin", false));
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        String recorded = tag.getString("RecordingPin");
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
            t.remove("RecordingPin");
            if (!recorded.isEmpty()) {
                t.putString("PinCode", recorded);
            }
        });
        if (!recorded.isEmpty()) {
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

        CompoundTag tagData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (tagData.getBoolean("IsRecordingPin")) {
            if (pPlayer.isShiftKeyDown()) {
                if (!pLevel.isClientSide()) {
                    finishPinRecording(stack, pPlayer, pLevel);
                }
                return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
            } else {
                if (!pLevel.isClientSide()) {
                    String current = tagData.getString("RecordingPin");
                    pPlayer.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.recording_hint", current).withStyle(ChatFormatting.YELLOW), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
            }
        }

        if (pPlayer.isShiftKeyDown()) {
            if (!pLevel.isClientSide()) {
                boolean isSmartFill = tagData.getBoolean("IsSmartFill");
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean("IsSmartFill", !isSmartFill));
                pPlayer.displayClientMessage(Component.translatable("message.signbuilder.wrench.smart_fill_toggle").withStyle(ChatFormatting.YELLOW).append(Component.translatable(!isSmartFill ? "gui.signbuilder.on" : "gui.signbuilder.off").withStyle(!isSmartFill ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
                pLevel.playSound(null, pPlayer.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, !isSmartFill ? 1.5F : 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
        }

        if (pLevel.isClientSide()) {
            int cMode = 0; boolean dMonsters = true; boolean dAnimals = false;
            CompoundTag tag = tagData;
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

        CompoundTag tagData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (tagData.getBoolean("IsRecordingPin")) {
            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    finishPinRecording(stack, player, level);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (clickedBlock.getBlock() instanceof LetterBlock) {
                if (!level.isClientSide()) {
                    String symbol = LetterBlock.getCharacterFromBlock(clickedBlock.getBlock());
                    String current = tagData.getString("RecordingPin");
                    if (current.length() < 16) {
                        current += symbol;
                        final String finalCurrent = current;
                        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> t.putString("RecordingPin", finalCurrent));
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

        if (clickedBlock.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock && level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
            return handleGridSignWrench(level, pos, clickedBlock, gridBe, player, stack, pContext);
        }

        if ((clickedBlock.getBlock() instanceof LetterBlock || clickedBlock.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock) && level.getBlockEntity(pos) instanceof LetterBlockEntity rawEntity) {
            LetterBlockEntity letterEntity = rawEntity.getEffectiveMaster();
            BlockPos targetPos = letterEntity.getBlockPos();
            BlockState targetState = level.getBlockState(targetPos);

            if (player != null && player.isShiftKeyDown()) {
                if (!level.isClientSide()) {
                    int copiedMode = letterEntity.getWrenchMode();
                    net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                        t.putInt("WrenchMode", copiedMode);
                        t.putBoolean("DetectsMonsters", letterEntity.doesDetectMonsters());
                        t.putBoolean("DetectsAnimals", letterEntity.doesDetectAnimals());
                        t.putInt("ButtonMode", letterEntity.getButtonMode());
                        t.putBoolean("SyncWord", letterEntity.isSyncWord());
                        t.putString("PinCode", letterEntity.getPinCode());
                        t.putInt("CustomLightOnTicks", letterEntity.getCustomLightOnTicks());
                        t.putInt("CustomLightOffTicks", letterEntity.getCustomLightOffTicks());
                        t.putInt("CustomLightType", letterEntity.getCustomLightType());
                        t.putInt("CustomLightRange", letterEntity.getCustomLightRange());
                        t.putInt("CustomLightOffRange", letterEntity.getCustomLightOffRange());
                        t.putInt("CustomLightCloseDelayTicks", letterEntity.getCustomLightCloseDelayTicks());
                        t.putBoolean("CustomLightNightOnly", letterEntity.isCustomLightNightOnly());
                        t.putBoolean("CustomLightPlayers", letterEntity.doesCustomLightDetectPlayers());
                        t.putBoolean("CustomLightLowPower", letterEntity.isCustomLightLowPower());
                        t.putBoolean("CustomLightLookOnly", letterEntity.isCustomLightLookOnly());
                    });

                    player.displayClientMessage(Component.translatable("message.signbuilder.wrench.mode_copied").withStyle(ChatFormatting.YELLOW).append(copiedMode == -1 ? Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED) : Component.translatable(getModeTranslationKey(copiedMode)).withStyle(ChatFormatting.AQUA)), true);
                    level.playSound(null, targetPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                int mode = 0; boolean isSmartFill = false; boolean detectsMonsters = true; boolean detectsAnimals = false;
                int buttonMode = 0; boolean syncWord = false; int activeTab = 0;
                int customLightOnTicks = 10; int customLightOffTicks = 10;
                int customLightType = 0; int customLightRange = 8; int customLightOffRange = 8; int customLightCloseDelayTicks = 0; boolean customLightNightOnly = true;
                boolean customLightPlayers = true; boolean customLightLowPower = false; boolean customLightLookOnly = true;
                String pinCode = "";

                CompoundTag tag = tagData;
                if (tag != null) {
                    if (tag.contains("WrenchMode")) mode = tag.getInt("WrenchMode");
                    if (tag.contains("IsSmartFill")) isSmartFill = tag.getBoolean("IsSmartFill");
                    if (tag.contains("DetectsMonsters")) detectsMonsters = tag.getBoolean("DetectsMonsters");
                    if (tag.contains("DetectsAnimals")) detectsAnimals = tag.getBoolean("DetectsAnimals");
                    if (tag.contains("ButtonMode")) buttonMode = tag.getInt("ButtonMode");
                    if (tag.contains("SyncWord")) syncWord = tag.getBoolean("SyncWord");
                    if (tag.contains("ActiveTab")) activeTab = tag.getInt("ActiveTab");
                    if (tag.contains("PinCode")) pinCode = tag.getString("PinCode");
                    if (tag.contains("CustomLightOnTicks")) customLightOnTicks = Math.max(1, Math.min(1200, tag.getInt("CustomLightOnTicks")));
                    if (tag.contains("CustomLightOffTicks")) customLightOffTicks = Math.max(1, Math.min(1200, tag.getInt("CustomLightOffTicks")));
                    if (tag.contains("CustomLightType")) customLightType = Math.max(0, Math.min(3, tag.getInt("CustomLightType")));
                    if (tag.contains("CustomLightRange")) customLightRange = Math.max(1, Math.min(32, tag.getInt("CustomLightRange")));
                    if (tag.contains("CustomLightOffRange")) customLightOffRange = Math.max(customLightRange, Math.min(32, tag.getInt("CustomLightOffRange")));
                    else customLightOffRange = customLightRange;
                    if (tag.contains("CustomLightCloseDelayTicks")) customLightCloseDelayTicks = Math.max(0, Math.min(100, tag.getInt("CustomLightCloseDelayTicks")));
                    if (tag.contains("CustomLightNightOnly")) customLightNightOnly = tag.getBoolean("CustomLightNightOnly");
                    if (tag.contains("CustomLightPlayers")) customLightPlayers = tag.getBoolean("CustomLightPlayers");
                    if (tag.contains("CustomLightLowPower")) customLightLowPower = tag.getBoolean("CustomLightLowPower");
                    if (tag.contains("CustomLightLookOnly")) customLightLookOnly = tag.getBoolean("CustomLightLookOnly");
                }

                if (activeTab == 0) {
                    boolean wasActive = letterEntity.isActive();
                    boolean wasConfigured = letterEntity.getWrenchMode() != 0 || wasActive;
                    boolean targetActive = (mode != -1);

                    boolean noChange = (mode == -1 ? (!wasConfigured && letterEntity.getWrenchMode() == 0) : (wasConfigured && letterEntity.getWrenchMode() == mode))
                            && (mode != 5 || (letterEntity.doesDetectMonsters() == detectsMonsters && letterEntity.doesDetectAnimals() == detectsAnimals))
                            && (mode != 11 || customLightConfigurationMatches(letterEntity, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly, detectsMonsters, detectsAnimals));

                    if (noChange && !isSmartFill) {
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }

                    if (isSmartFill) {
                        applyLightModeToConnected(level, targetPos, player, stack, pContext.getHand(), mode, targetActive, detectsMonsters, detectsAnimals, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
                    } else {
                        int dustCost = (letterEntity.getSize() == 3) ? 9 : (letterEntity.getSize() == 2 ? 4 : 1);

                        if (player != null && !player.isCreative()) {
                            if (!wasConfigured && targetActive) {
                                if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= dustCost) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, dustCost);
                                else {
                                    player.displayClientMessage(Component.translatable("message.signbuilder.missing_material").withStyle(ChatFormatting.RED), true);
                                    player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                    return InteractionResult.FAIL;
                                }
                            } else if (wasConfigured && !targetActive) {
                                ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, dustCost);
                                if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                            }
                            stack.hurtAndBreak(1, player, pContext.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                        }

                        letterEntity.setLightConfiguration(mode == -1 ? 0 : mode, targetActive, detectsMonsters, detectsAnimals, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
                        LetterBlock.updateLightLevel(level, targetPos, targetState, letterEntity);
                        level.sendBlockUpdated(targetPos, targetState, targetState, 3);
                    }
                } else {
                    if (clickedBlock.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock) {
                        return InteractionResult.PASS;
                    }
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
                            stack.hurtAndBreak(1, player, pContext.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
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

    private void applyLightModeToConnected(Level level, BlockPos startPos, Player player, ItemStack stack, InteractionHand hand, int mode, boolean targetActive, boolean detectsMonsters, boolean detectsAnimals, int customLightOnTicks, int customLightOffTicks, int customLightType, int customLightRange, int customLightOffRange, int customLightCloseDelayTicks, boolean customLightNightOnly, boolean customLightPlayers, boolean customLightLowPower, boolean customLightLookOnly) {
        List<BlockPos> targets = getConnectedBlocks(level, startPos);
        int blocksModified = 0; int failedMaterial = 0; int failedDurability = 0;
        int currentDamage = stack.getDamageValue(); int maxDamage = stack.getMaxDamage();

        for (BlockPos current : targets) {
            if (player != null && !player.isCreative() && (currentDamage + blocksModified) >= maxDamage) {
                failedDurability++;
                continue;
            }

            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof LetterBlockEntity rawLetter) {
                LetterBlockEntity letter = rawLetter.getEffectiveMaster();
                BlockPos effectivePos = letter.getBlockPos();
                BlockState currentState = level.getBlockState(effectivePos);

                boolean wasActive = letter.isActive();
                boolean wasConfigured = letter.getWrenchMode() != 0 || wasActive;

                boolean blockNoChange = (mode == -1 ? (!wasConfigured && letter.getWrenchMode() == 0) : (wasConfigured && letter.getWrenchMode() == mode))
                        && (mode != 5 || (letter.doesDetectMonsters() == detectsMonsters && letter.doesDetectAnimals() == detectsAnimals))
                        && (mode != 11 || customLightConfigurationMatches(letter, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly, detectsMonsters, detectsAnimals));

                if (blockNoChange) continue;

                int dustCost = (letter.getSize() == 3) ? 9 : (letter.getSize() == 2 ? 4 : 1);

                if (player != null && !player.isCreative()) {
                    if (!wasConfigured && targetActive) {
                        if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= dustCost) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, dustCost);
                        else { failedMaterial++; continue; }
                    } else if (wasConfigured && !targetActive) {
                        ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, dustCost);
                        if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                    }
                }

                letter.setLightConfiguration(mode == -1 ? 0 : mode, targetActive, detectsMonsters, detectsAnimals, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
                LetterBlock.updateLightLevel(level, effectivePos, currentState, letter);
                level.sendBlockUpdated(effectivePos, currentState, currentState, 3);
                blocksModified++;
            } else if (be instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe) {
                BlockState currentState = level.getBlockState(current);
                boolean wasActive = gridBe.isActive();
                boolean wasConfigured = gridBe.getWrenchMode() != 0 || wasActive;

                boolean blockNoChange = (mode == -1 ? (!wasConfigured && gridBe.getWrenchMode() == 0) : (wasConfigured && gridBe.getWrenchMode() == mode))
                        && (mode != 5 || (gridBe.doesDetectMonsters() == detectsMonsters && gridBe.doesDetectAnimals() == detectsAnimals))
                        && (mode != 11 || customLightConfigurationMatchesGrid(gridBe, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly, detectsMonsters, detectsAnimals));

                if (blockNoChange) continue;

                int dustCost = 1;

                if (player != null && !player.isCreative()) {
                    if (!wasConfigured && targetActive) {
                        if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= dustCost) consumeItemFromInventory(player, Items.GLOWSTONE_DUST, dustCost);
                        else { failedMaterial++; continue; }
                    } else if (wasConfigured && !targetActive) {
                        ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, dustCost);
                        if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                    }
                }

                gridBe.setLightConfiguration(mode == -1 ? 0 : mode, targetActive, detectsMonsters, detectsAnimals, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
                com.boran.signbuilder.block.GridSignBlock.updateLightLevel(level, current, currentState, gridBe);
                level.sendBlockUpdated(current, currentState, currentState, 3);
                blocksModified++;
            }
        }

        handleSmartFillFeedback(level, player, stack, hand, blocksModified, failedMaterial, failedDurability);
    }

    private static boolean customLightConfigurationMatches(LetterBlockEntity letter, int onTicks, int offTicks, int type, int range, int offRange, int closeDelayTicks, boolean nightOnly, boolean players, boolean lowPower, boolean lookOnly, boolean detectsMonsters, boolean detectsAnimals) {
        if (letter.getCustomLightType() != type) return false;
        if (letter.isCustomLightLowPower() != lowPower) return false;
        return switch (type) {
            case 1 -> letter.getCustomLightRange() == range
                    && letter.getCustomLightOffRange() == offRange
                    && letter.getCustomLightCloseDelayTicks() == closeDelayTicks
                    && letter.doesCustomLightDetectPlayers() == players
                    && letter.doesDetectMonsters() == detectsMonsters
                    && letter.doesDetectAnimals() == detectsAnimals;
            case 2 -> letter.isCustomLightNightOnly() == nightOnly;
            case 3 -> letter.isCustomLightLookOnly() == lookOnly;
            default -> letter.getCustomLightOnTicks() == onTicks && letter.getCustomLightOffTicks() == offTicks;
        };
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
            BlockEntity currentBe = level.getBlockEntity(current);
            if (currentBe instanceof LetterBlockEntity be) {
                BlockPos effectivePos = be.getEffectiveMaster().getBlockPos();
                if (!targets.contains(effectivePos)) {
                    targets.add(effectivePos);
                }
            } else if (currentBe instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity) {
                if (!targets.contains(current)) {
                    targets.add(current);
                }
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (!visited.contains(neighbor)) {
                            Block neighborBlock = level.getBlockState(neighbor).getBlock();
                            if (neighborBlock instanceof LetterBlock
                                    || neighborBlock instanceof com.boran.signbuilder.block.BackplateBlock
                                    || neighborBlock instanceof com.boran.signbuilder.block.GridSignBlock) {
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
            if (blocksModified > 0 && !player.isCreative()) stack.hurtAndBreak(blocksModified, player, hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);

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

    private InteractionResult handleGridSignWrench(Level level, BlockPos pos, BlockState state, com.boran.signbuilder.block.entity.GridSignBlockEntity gridBe, Player player, ItemStack stack, UseOnContext context) {
        if (player != null && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                int copiedMode = !gridBe.isActive() && gridBe.getWrenchMode() == 0 ? -1 : gridBe.getWrenchMode();
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                    t.putInt("WrenchMode", copiedMode);
                    t.putBoolean("DetectsMonsters", gridBe.doesDetectMonsters());
                    t.putBoolean("DetectsAnimals", gridBe.doesDetectAnimals());
                    t.putInt("CustomLightOnTicks", gridBe.getCustomLightOnTicks());
                    t.putInt("CustomLightOffTicks", gridBe.getCustomLightOffTicks());
                    t.putInt("CustomLightType", gridBe.getCustomLightType());
                    t.putInt("CustomLightRange", gridBe.getCustomLightRange());
                    t.putInt("CustomLightOffRange", gridBe.getCustomLightOffRange());
                    t.putInt("CustomLightCloseDelayTicks", gridBe.getCustomLightCloseDelayTicks());
                    t.putBoolean("CustomLightNightOnly", gridBe.isCustomLightNightOnly());
                    t.putBoolean("CustomLightPlayers", gridBe.doesCustomLightDetectPlayers());
                    t.putBoolean("CustomLightLowPower", gridBe.isCustomLightLowPower());
                    t.putBoolean("CustomLightLookOnly", gridBe.isCustomLightLookOnly());
                });
                player.displayClientMessage(Component.translatable("message.signbuilder.wrench.mode_copied").withStyle(ChatFormatting.YELLOW).append(copiedMode == -1 ? Component.translatable("gui.signbuilder.wrench.mode.turn_off").withStyle(ChatFormatting.RED) : Component.translatable(getModeTranslationKey(copiedMode)).withStyle(ChatFormatting.AQUA)), true);
                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        CompoundTag tagData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        int mode = 0;
        int activeTab = 0;
        boolean isSmartFill = false;
        boolean detectsMonsters = true;
        boolean detectsAnimals = false;
        int customLightOnTicks = 10;
        int customLightOffTicks = 10;
        int customLightType = 0;
        int customLightRange = 8;
        int customLightOffRange = 8;
        int customLightCloseDelayTicks = 0;
        boolean customLightNightOnly = true;
        boolean customLightPlayers = true;
        boolean customLightLowPower = false;
        boolean customLightLookOnly = true;

        if (tagData != null) {
            if (tagData.contains("WrenchMode")) mode = tagData.getInt("WrenchMode");
            if (tagData.contains("ActiveTab")) activeTab = tagData.getInt("ActiveTab");
            if (tagData.contains("IsSmartFill")) isSmartFill = tagData.getBoolean("IsSmartFill");
            if (tagData.contains("DetectsMonsters")) detectsMonsters = tagData.getBoolean("DetectsMonsters");
            if (tagData.contains("DetectsAnimals")) detectsAnimals = tagData.getBoolean("DetectsAnimals");
            if (tagData.contains("CustomLightOnTicks")) customLightOnTicks = Math.max(1, Math.min(1200, tagData.getInt("CustomLightOnTicks")));
            if (tagData.contains("CustomLightOffTicks")) customLightOffTicks = Math.max(1, Math.min(1200, tagData.getInt("CustomLightOffTicks")));
            if (tagData.contains("CustomLightType")) customLightType = Math.max(0, Math.min(3, tagData.getInt("CustomLightType")));
            if (tagData.contains("CustomLightRange")) customLightRange = Math.max(1, Math.min(32, tagData.getInt("CustomLightRange")));
            if (tagData.contains("CustomLightOffRange")) customLightOffRange = Math.max(customLightRange, Math.min(32, tagData.getInt("CustomLightOffRange")));
            else customLightOffRange = customLightRange;
            if (tagData.contains("CustomLightCloseDelayTicks")) customLightCloseDelayTicks = Math.max(0, Math.min(100, tagData.getInt("CustomLightCloseDelayTicks")));
            if (tagData.contains("CustomLightNightOnly")) customLightNightOnly = tagData.getBoolean("CustomLightNightOnly");
            if (tagData.contains("CustomLightPlayers")) customLightPlayers = tagData.getBoolean("CustomLightPlayers");
            if (tagData.contains("CustomLightLowPower")) customLightLowPower = tagData.getBoolean("CustomLightLowPower");
            if (tagData.contains("CustomLightLookOnly")) customLightLookOnly = tagData.getBoolean("CustomLightLookOnly");
        }

        if (activeTab == 0) {
            boolean wasActive = gridBe.isActive();
            boolean wasConfigured = gridBe.getWrenchMode() != 0 || wasActive;
            boolean targetActive = (mode != -1);

            boolean noChange = (mode == -1 ? (!wasConfigured && gridBe.getWrenchMode() == 0) : (wasConfigured && gridBe.getWrenchMode() == mode))
                    && (mode != 5 || (gridBe.doesDetectMonsters() == detectsMonsters && gridBe.doesDetectAnimals() == detectsAnimals))
                    && (mode != 11 || customLightConfigurationMatchesGrid(gridBe, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly, detectsMonsters, detectsAnimals));

            if (noChange && !isSmartFill) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            if (!level.isClientSide()) {
                if (isSmartFill) {
                    applyLightModeToConnected(level, pos, player, stack, context.getHand(), mode, targetActive, detectsMonsters, detectsAnimals, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
                } else {
                    if (player != null && !player.isCreative()) {
                        int dustCost = 1;
                        if (!wasConfigured && targetActive) {
                            if (countItemInInventory(player, Items.GLOWSTONE_DUST) >= dustCost) {
                                consumeItemFromInventory(player, Items.GLOWSTONE_DUST, dustCost);
                            } else {
                                player.displayClientMessage(Component.translatable("message.signbuilder.missing_material").withStyle(ChatFormatting.RED), true);
                                player.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                return InteractionResult.FAIL;
                            }
                        } else if (wasConfigured && !targetActive) {
                            ItemStack returnDust = new ItemStack(Items.GLOWSTONE_DUST, dustCost);
                            if (!player.getInventory().add(returnDust)) player.drop(returnDust, false);
                        }
                        stack.hurtAndBreak(1, player, context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                    }

                    gridBe.setLightConfiguration(mode == -1 ? 0 : mode, targetActive, detectsMonsters, detectsAnimals, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, customLightCloseDelayTicks, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
                    com.boran.signbuilder.block.GridSignBlock.updateLightLevel(level, pos, state, gridBe);
                    level.sendBlockUpdated(pos, state, state, 3);
                }
                level.playSound(null, pos, SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 1.0F, 1.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    private static boolean customLightConfigurationMatchesGrid(com.boran.signbuilder.block.entity.GridSignBlockEntity entity, int onTicks, int offTicks, int type, int range, int offRange, int closeDelayTicks, boolean nightOnly, boolean players, boolean lowPower, boolean lookOnly, boolean detectsMonsters, boolean detectsAnimals) {
        if (entity.getCustomLightType() != type) return false;
        if (entity.isCustomLightLowPower() != lowPower) return false;
        return switch (type) {
            case 1 -> entity.getCustomLightRange() == range
                    && entity.getCustomLightOffRange() == offRange
                    && entity.getCustomLightCloseDelayTicks() == closeDelayTicks
                    && entity.doesCustomLightDetectPlayers() == players
                    && entity.doesDetectMonsters() == detectsMonsters
                    && entity.doesDetectAnimals() == detectsAnimals;
            case 2 -> entity.isCustomLightNightOnly() == nightOnly;
            case 3 -> entity.isCustomLightLookOnly() == lookOnly;
            default -> entity.getCustomLightOnTicks() == onTicks && entity.getCustomLightOffTicks() == offTicks;
        };
    }
}
