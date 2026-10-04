package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public class WrenchModeC2SPacket {
    private final int mode;
    private final boolean detectsMonsters;
    private final boolean detectsAnimals;
    private final int buttonMode;
    private final boolean syncWord;
    private final int activeTab;
    private final String pinCode;
    private final boolean startPinRecording;
    private final int customLightOnTicks;
    private final int customLightOffTicks;
    private final int customLightType;
    private final int customLightRange;
    private final int customLightOffRange;
    private final int customLightCloseDelayTicks;
    private final boolean customLightNightOnly;
    private final boolean customLightPlayers;
    private final boolean customLightLowPower;
    private final boolean customLightLookOnly;

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording, int customLightOnTicks, int customLightOffTicks, int customLightType, int customLightRange, int customLightOffRange, int customLightCloseDelayTicks, boolean customLightNightOnly, boolean customLightPlayers, boolean customLightLowPower, boolean customLightLookOnly) {
        this.mode = mode;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
        this.buttonMode = buttonMode;
        this.syncWord = syncWord;
        this.activeTab = activeTab;
        this.pinCode = pinCode != null ? pinCode : "";
        this.startPinRecording = startPinRecording;
        this.customLightOnTicks = Math.max(1, Math.min(1200, customLightOnTicks));
        this.customLightOffTicks = Math.max(1, Math.min(1200, customLightOffTicks));
        this.customLightType = Math.max(0, Math.min(3, customLightType));
        this.customLightRange = Math.max(1, Math.min(32, customLightRange));
        this.customLightOffRange = Math.max(this.customLightRange, Math.min(32, customLightOffRange));
        this.customLightCloseDelayTicks = Math.max(0, Math.min(100, customLightCloseDelayTicks));
        this.customLightNightOnly = customLightNightOnly;
        this.customLightPlayers = customLightPlayers;
        this.customLightLowPower = customLightLowPower;
        this.customLightLookOnly = customLightLookOnly;
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording, int customLightOnTicks, int customLightOffTicks, int customLightType, int customLightRange, int customLightOffRange, boolean customLightNightOnly, boolean customLightPlayers, boolean customLightLowPower, boolean customLightLookOnly) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, startPinRecording, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightOffRange, 0, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording, int customLightOnTicks, int customLightOffTicks, int customLightType, int customLightRange, boolean customLightNightOnly, boolean customLightPlayers, boolean customLightLowPower, boolean customLightLookOnly) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, startPinRecording, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightRange, 0, customLightNightOnly, customLightPlayers, customLightLowPower, customLightLookOnly);
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording, int customLightOnTicks, int customLightOffTicks) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, startPinRecording, customLightOnTicks, customLightOffTicks, 0, 8, 8, true, true, false, true);
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording, int customLightOnTicks, int customLightOffTicks, int customLightType, int customLightRange, boolean customLightNightOnly) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, startPinRecording, customLightOnTicks, customLightOffTicks, customLightType, customLightRange, customLightRange, customLightNightOnly, true, false, true);
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, startPinRecording, 10, 10);
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, false, 10, 10);
    }

    public WrenchModeC2SPacket(FriendlyByteBuf buf) {
        this.mode = buf.readInt();
        this.detectsMonsters = buf.readBoolean();
        this.detectsAnimals = buf.readBoolean();
        this.buttonMode = buf.readInt();
        this.syncWord = buf.readBoolean();
        this.activeTab = buf.readInt();
        this.pinCode = buf.readUtf(32767);
        this.startPinRecording = buf.readBoolean();
        this.customLightOnTicks = Math.max(1, Math.min(1200, buf.readInt()));
        this.customLightOffTicks = Math.max(1, Math.min(1200, buf.readInt()));
        this.customLightType = Math.max(0, Math.min(3, buf.readInt()));
        this.customLightRange = Math.max(1, Math.min(32, buf.readInt()));
        this.customLightOffRange = Math.max(this.customLightRange, Math.min(32, buf.readInt()));
        this.customLightCloseDelayTicks = Math.max(0, Math.min(100, buf.readInt()));
        this.customLightNightOnly = buf.readBoolean();
        this.customLightPlayers = buf.readBoolean();
        this.customLightLowPower = buf.readBoolean();
        this.customLightLookOnly = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(mode);
        buf.writeBoolean(detectsMonsters);
        buf.writeBoolean(detectsAnimals);
        buf.writeInt(buttonMode);
        buf.writeBoolean(syncWord);
        buf.writeInt(activeTab);
        buf.writeUtf(pinCode);
        buf.writeBoolean(startPinRecording);
        buf.writeInt(customLightOnTicks);
        buf.writeInt(customLightOffTicks);
        buf.writeInt(customLightType);
        buf.writeInt(customLightRange);
        buf.writeInt(customLightOffRange);
        buf.writeInt(customLightCloseDelayTicks);
        buf.writeBoolean(customLightNightOnly);
        buf.writeBoolean(customLightPlayers);
        buf.writeBoolean(customLightLowPower);
        buf.writeBoolean(customLightLookOnly);
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof com.boran.signbuilder.item.WrenchItem)) stack = player.getOffhandItem();
            if (stack.getItem() instanceof com.boran.signbuilder.item.WrenchItem) {
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                    t.putInt("WrenchMode", mode);
                    t.putBoolean("DetectsMonsters", detectsMonsters);
                    t.putBoolean("DetectsAnimals", detectsAnimals);
                    t.putInt("ButtonMode", buttonMode);
                    t.putBoolean("SyncWord", syncWord);
                    t.putInt("ActiveTab", activeTab);
                    t.putString("PinCode", pinCode);
                    t.putInt("CustomLightOnTicks", customLightOnTicks);
                    t.putInt("CustomLightOffTicks", customLightOffTicks);
                    t.putInt("CustomLightType", customLightType);
                    t.putInt("CustomLightRange", customLightRange);
                    t.putInt("CustomLightOffRange", customLightOffRange);
                    t.putInt("CustomLightCloseDelayTicks", customLightCloseDelayTicks);
                    t.putBoolean("CustomLightNightOnly", customLightNightOnly);
                    t.putBoolean("CustomLightPlayers", customLightPlayers);
                    t.putBoolean("CustomLightLowPower", customLightLowPower);
                    t.putBoolean("CustomLightLookOnly", customLightLookOnly);
                });

                if (startPinRecording) {
                    net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, t -> {
                        t.putBoolean("IsRecordingPin", true);
                        t.putString("RecordingPin", "");
                    });
                    player.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.record_start").withStyle(ChatFormatting.GOLD), true);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.5F);
                }
            }
        }
    }
}
