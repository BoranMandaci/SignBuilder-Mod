package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ModMessages {
    public static final ResourceLocation BRUSH_COLOR = ResourceLocation.fromNamespaceAndPath("signbuilder", "brush_color");
    public static final ResourceLocation WRENCH_MODE = ResourceLocation.fromNamespaceAndPath("signbuilder", "wrench_mode");
    public static final ResourceLocation WRENCH_HOLD = ResourceLocation.fromNamespaceAndPath("signbuilder", "wrench_hold");
    public static final ResourceLocation BLUEPRINT_TEXT = ResourceLocation.fromNamespaceAndPath("signbuilder", "blueprint_text");
    public static final ResourceLocation SIGN_PRESS_CRAFT = ResourceLocation.fromNamespaceAndPath("signbuilder", "sign_press_craft");
    public static final ResourceLocation BLUEPRINT_UNDO = ResourceLocation.fromNamespaceAndPath("signbuilder", "blueprint_undo");

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BrushColorPacket.TYPE, BrushColorPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WrenchModeC2SPacket.TYPE, WrenchModeC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WrenchHoldC2SPacket.TYPE, WrenchHoldC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BlueprintTextC2SPacket.TYPE, BlueprintTextC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SignPressCraftC2SPacket.TYPE, SignPressCraftC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BlueprintUndoC2SPacket.TYPE, BlueprintUndoC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PlaceGridLetterC2SPacket.TYPE, PlaceGridLetterC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SyncGridModeC2SPacket.TYPE, SyncGridModeC2SPacket.STREAM_CODEC, (payload, context) -> {
            context.queue(() -> payload.handle(context));
        });
    }

    public static <MSG> void sendToServer(MSG message) {
        if (message instanceof BrushColorPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof WrenchModeC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof WrenchHoldC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof BlueprintTextC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof SignPressCraftC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof BlueprintUndoC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof PlaceGridLetterC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        } else if (message instanceof SyncGridModeC2SPacket packet) {
            NetworkManager.sendToServer(packet);
        }
    }
}
