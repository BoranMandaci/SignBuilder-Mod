package com.boran.signbuilder.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ModMessages {
    public static final ResourceLocation BRUSH_COLOR = new ResourceLocation("signbuilder", "brush_color");
    public static final ResourceLocation WRENCH_MODE = new ResourceLocation("signbuilder", "wrench_mode");
    public static final ResourceLocation WRENCH_HOLD = new ResourceLocation("signbuilder", "wrench_hold");
    public static final ResourceLocation BLUEPRINT_TEXT = new ResourceLocation("signbuilder", "blueprint_text");
    public static final ResourceLocation SIGN_PRESS_CRAFT = new ResourceLocation("signbuilder", "sign_press_craft");
    public static final ResourceLocation BLUEPRINT_UNDO = new ResourceLocation("signbuilder", "blueprint_undo");
    public static final ResourceLocation PLACE_GRID_LETTER = new ResourceLocation("signbuilder", "place_grid_letter");
    public static final ResourceLocation SYNC_GRID_MODE = new ResourceLocation("signbuilder", "sync_grid_mode");
    public static final ResourceLocation REMOVE_GRID_LETTER = new ResourceLocation("signbuilder", "remove_grid_letter");
    public static final ResourceLocation APPLY_BACKPLATE_INK = new ResourceLocation("signbuilder", "apply_backplate_ink");
    public static final ResourceLocation UPDATE_LCD_SCREEN = new ResourceLocation("signbuilder", "update_lcd_screen");
    public static final ResourceLocation BLUEPRINT_DIRECTION = new ResourceLocation("signbuilder", "blueprint_direction");
    public static final ResourceLocation LOCATE_STORAGE_ITEM = new ResourceLocation("signbuilder", "locate_storage_item");
    public static final ResourceLocation HIGHLIGHT_CHESTS = new ResourceLocation("signbuilder", "highlight_chests");

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BRUSH_COLOR, (buf, context) -> {
            BrushColorPacket packet = new BrushColorPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WRENCH_MODE, (buf, context) -> {
            WrenchModeC2SPacket packet = new WrenchModeC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, WRENCH_HOLD, (buf, context) -> {
            WrenchHoldC2SPacket packet = new WrenchHoldC2SPacket(buf);
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

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, PLACE_GRID_LETTER, (buf, context) -> {
            PlaceGridLetterC2SPacket packet = new PlaceGridLetterC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SYNC_GRID_MODE, (buf, context) -> {
            SyncGridModeC2SPacket packet = new SyncGridModeC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REMOVE_GRID_LETTER, (buf, context) -> {
            RemoveGridLetterC2SPacket packet = new RemoveGridLetterC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, APPLY_BACKPLATE_INK, (buf, context) -> {
            ApplyBackplateInkC2SPacket packet = new ApplyBackplateInkC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, UPDATE_LCD_SCREEN, (buf, context) -> {
            UpdateLcdScreenC2SPacket packet = new UpdateLcdScreenC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, BLUEPRINT_DIRECTION, (buf, context) -> {
            BlueprintDirectionC2SPacket packet = new BlueprintDirectionC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, LOCATE_STORAGE_ITEM, (buf, context) -> {
            LocateStorageItemC2SPacket packet = new LocateStorageItemC2SPacket(buf);
            context.queue(() -> packet.handle(context));
        });

        EnvExecutor.runInEnv(Env.CLIENT, () -> () ->
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, HIGHLIGHT_CHESTS, (buf, context) -> {
                    HighlightChestsS2CPacket packet = new HighlightChestsS2CPacket(buf);
                    context.queue(() -> packet.handle(context));
                }));
    }

    public static <MSG> void sendToServer(MSG message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        if (message instanceof BrushColorPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BRUSH_COLOR, buf);
        } else if (message instanceof WrenchModeC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(WRENCH_MODE, buf);
        } else if (message instanceof WrenchHoldC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(WRENCH_HOLD, buf);
        } else if (message instanceof BlueprintTextC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BLUEPRINT_TEXT, buf);
        } else if (message instanceof SignPressCraftC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(SIGN_PRESS_CRAFT, buf);
        } else if (message instanceof BlueprintUndoC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BLUEPRINT_UNDO, buf);
        } else if (message instanceof PlaceGridLetterC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(PLACE_GRID_LETTER, buf);
        } else if (message instanceof SyncGridModeC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(SYNC_GRID_MODE, buf);
        } else if (message instanceof RemoveGridLetterC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(REMOVE_GRID_LETTER, buf);
        } else if (message instanceof ApplyBackplateInkC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(APPLY_BACKPLATE_INK, buf);
        } else if (message instanceof UpdateLcdScreenC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(UPDATE_LCD_SCREEN, buf);
        } else if (message instanceof BlueprintDirectionC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(BLUEPRINT_DIRECTION, buf);
        } else if (message instanceof LocateStorageItemC2SPacket packet) {
            packet.toBytes(buf);
            NetworkManager.sendToServer(LOCATE_STORAGE_ITEM, buf);
        }
    }

    public static void sendToPlayer(ServerPlayer player, HighlightChestsS2CPacket packet) {
        if (player != null && packet != null) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            packet.toBytes(buf);
            NetworkManager.sendToPlayer(player, HIGHLIGHT_CHESTS, buf);
        }
    }
}
