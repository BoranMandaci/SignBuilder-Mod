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

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode, boolean startPinRecording) {
        this.mode = mode;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
        this.buttonMode = buttonMode;
        this.syncWord = syncWord;
        this.activeTab = activeTab;
        this.pinCode = pinCode != null ? pinCode : "";
        this.startPinRecording = startPinRecording;
    }

    public WrenchModeC2SPacket(int mode, boolean detectsMonsters, boolean detectsAnimals, int buttonMode, boolean syncWord, int activeTab, String pinCode) {
        this(mode, detectsMonsters, detectsAnimals, buttonMode, syncWord, activeTab, pinCode, false);
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
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof com.boran.signbuilder.item.WrenchItem) {
                stack.getOrCreateTag().putInt("WrenchMode", mode);
                stack.getOrCreateTag().putBoolean("DetectsMonsters", detectsMonsters);
                stack.getOrCreateTag().putBoolean("DetectsAnimals", detectsAnimals);
                stack.getOrCreateTag().putInt("ButtonMode", buttonMode);
                stack.getOrCreateTag().putBoolean("SyncWord", syncWord);
                stack.getOrCreateTag().putInt("ActiveTab", activeTab);
                stack.getOrCreateTag().putString("PinCode", pinCode);

                if (startPinRecording) {
                    stack.getOrCreateTag().putBoolean("IsRecordingPin", true);
                    stack.getOrCreateTag().putString("RecordingPin", "");
                    player.displayClientMessage(Component.translatable("message.signbuilder.wrench.pin.record_start").withStyle(ChatFormatting.GOLD), true);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.5F);
                }
            }
        }
    }
}