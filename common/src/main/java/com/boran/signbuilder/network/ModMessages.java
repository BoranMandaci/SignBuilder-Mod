package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ModMessages {
    public static final ResourceLocation BRUSH_COLOR = new ResourceLocation("signbuilder", "brush_color");
    public static final ResourceLocation WRENCH_MODE = new ResourceLocation("signbuilder", "wrench_mode");
    public static final ResourceLocation BLUEPRINT_TEXT = new ResourceLocation("signbuilder", "blueprint_text");
    public static final ResourceLocation SIGN_PRESS_CRAFT = new ResourceLocation("signbuilder", "sign_press_craft");
    public static final ResourceLocation BLUEPRINT_UNDO = new ResourceLocation("signbuilder", "blueprint_undo");

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BRUSH_COLOR, (buf, context) -> {
            BrushColorPacket packet = new BrushColorPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WRENCH_MODE, (buf, context) -> {
            WrenchModeC2SPacket packet = new WrenchModeC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BLUEPRINT_TEXT, (buf, context) -> {
            BlueprintTextC2SPacket packet = new BlueprintTextC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SIGN_PRESS_CRAFT, (buf, context) -> {
            SignPressCraftC2SPacket packet = new SignPressCraftC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BLUEPRINT_UNDO, (buf, context) -> {
            BlueprintUndoC2SPacket packet = new BlueprintUndoC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });
    }

    public static <MSG> void sendToServer(MSG message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        if (message instanceof BrushColorPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BRUSH_COLOR, buf);
        } else if (message instanceof WrenchModeC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(WRENCH_MODE, buf);
        } else if (message instanceof BlueprintTextC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BLUEPRINT_TEXT, buf);
        } else if (message instanceof SignPressCraftC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(SIGN_PRESS_CRAFT, buf);
        } else if (message instanceof BlueprintUndoC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BLUEPRINT_UNDO, buf);
        }
    }
}