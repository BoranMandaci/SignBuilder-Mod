package com.boran.signbuilder.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("signbuilder", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(BrushColorPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(buf -> new BrushColorPacket(buf))
                .encoder((packet, buf) -> packet.toBytes(buf))
                .consumerMainThread((packet, contextSupplier) -> packet.handle(contextSupplier))
                .add();

        net.messageBuilder(WrenchModeC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(WrenchModeC2SPacket::new)
                .encoder(WrenchModeC2SPacket::toBytes)
                .consumerMainThread(WrenchModeC2SPacket::handle)
                .add();

        net.messageBuilder(BlueprintTextC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(BlueprintTextC2SPacket::new)
                .encoder(BlueprintTextC2SPacket::toBytes)
                .consumerMainThread(BlueprintTextC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}