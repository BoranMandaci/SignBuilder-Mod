package com.boran.signbuilder.network;

import com.boran.signbuilder.block.LcdScreenBlock;
import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class UpdateLcdScreenC2SPacket implements CustomPacketPayload {
    public static final Type<UpdateLcdScreenC2SPacket> TYPE = new Type<>(ModMessages.UPDATE_LCD_SCREEN);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLcdScreenC2SPacket> STREAM_CODEC =
            StreamCodec.ofMember(UpdateLcdScreenC2SPacket::toBytes, UpdateLcdScreenC2SPacket::new);

    private final BlockPos pos;
    private final String text;
    private final int mode;
    private final int mode2;
    private final int align;
    private final int speed;
    private final int fontSize;
    private final int color;
    private final boolean rainbow;
    private final boolean glow;
    private final int theme;
    private final int widgetTL;
    private final int widgetTR;
    private final int widgetBL;
    private final int widgetBR;
    private final int fontStyle;
    private final int visualAnim;
    private final int storageMonitorMode;
    private final int storageStyle;
    private final int storageFilterMode;
    private final int storageFlowMode;
    private final List<ItemStack> watchlistItems;
    private final boolean loopEnabled;
    private final boolean loopShuffle;
    private final int loopTransition;
    private final int[] loopAnims;
    private final int[] loopDurations;
    private final int[] loopColors;
    private final String[] loopTexts1;
    private final String[] loopTexts2;
    private final int[] loopModes1;
    private final int[] loopModes2;
    private final int[] loopAligns;

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int fontStyle, int visualAnim, int storageMonitorMode, int storageStyle,
                                    int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems,
                                    boolean loopEnabled, boolean loopShuffle, int loopTransition,
                                    int[] loopAnims, int[] loopDurations, int[] loopColors,
                                    String[] loopTexts1, String[] loopTexts2, int[] loopModes1, int[] loopModes2, int[] loopAligns) {
        this.pos = pos;
        this.text = text != null ? text : "";
        this.mode = mode;
        this.mode2 = mode2;
        this.align = align;
        this.speed = speed;
        this.fontSize = fontSize;
        this.color = color;
        this.rainbow = rainbow;
        this.glow = glow;
        this.theme = theme;
        this.widgetTL = widgetTL;
        this.widgetTR = widgetTR;
        this.widgetBL = widgetBL;
        this.widgetBR = widgetBR;
        this.fontStyle = fontStyle;
        this.visualAnim = visualAnim;
        this.storageMonitorMode = storageMonitorMode;
        this.storageStyle = storageStyle;
        this.storageFilterMode = storageFilterMode;
        this.storageFlowMode = storageFlowMode;
        this.watchlistItems = new ArrayList<>();
        if (watchlistItems != null) {
            for (ItemStack st : watchlistItems) {
                if (st != null && !st.isEmpty()) {
                    this.watchlistItems.add(LcdScreenBlockEntity.cleanWatchlistCopy(st));
                    if (this.watchlistItems.size() >= LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS) {
                        break;
                    }
                }
            }
        }
        this.loopEnabled = loopEnabled;
        this.loopShuffle = loopShuffle;
        this.loopTransition = Math.max(0, Math.min(9, loopTransition));
        int lLen = loopAnims != null ? Math.min(LcdScreenBlockEntity.MAX_LOOP_STEPS, loopAnims.length) : 0;
        this.loopAnims = new int[lLen];
        this.loopDurations = new int[lLen];
        this.loopColors = new int[lLen];
        this.loopTexts1 = new String[lLen];
        this.loopTexts2 = new String[lLen];
        this.loopModes1 = new int[lLen];
        this.loopModes2 = new int[lLen];
        this.loopAligns = new int[lLen];
        for (int i = 0; i < lLen; i++) {
            this.loopAnims[i] = Math.max(0, Math.min(60, loopAnims[i]));
            this.loopDurations[i] = Math.max(1, Math.min(999, (loopDurations != null && i < loopDurations.length) ? loopDurations[i] : 6));
            int c = (loopColors != null && i < loopColors.length) ? loopColors[i] : -1;
            this.loopColors[i] = (c < 0) ? Math.max(-2, c) : (c & 0xFFFFFF);
            this.loopTexts1[i] = (loopTexts1 != null && i < loopTexts1.length && loopTexts1[i] != null) ? loopTexts1[i] : "";
            this.loopTexts2[i] = (loopTexts2 != null && i < loopTexts2.length && loopTexts2[i] != null) ? loopTexts2[i] : "";
            this.loopModes1[i] = (loopModes1 != null && i < loopModes1.length && loopModes1[i] >= 0 && loopModes1[i] <= 9) ? loopModes1[i] : -1;
            this.loopModes2[i] = (loopModes2 != null && i < loopModes2.length && loopModes2[i] >= 0 && loopModes2[i] <= 9) ? loopModes2[i] : -1;
            this.loopAligns[i] = (loopAligns != null && i < loopAligns.length && loopAligns[i] >= 0 && loopAligns[i] <= 2) ? loopAligns[i] : -1;
        }
    }

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int fontStyle, int visualAnim, int storageMonitorMode, int storageStyle,
                                    int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems,
                                    boolean loopEnabled, boolean loopShuffle, int loopTransition,
                                    int[] loopAnims, int[] loopDurations, int[] loopColors) {
        this(pos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR,
                fontStyle, visualAnim, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems,
                loopEnabled, loopShuffle, loopTransition, loopAnims, loopDurations, loopColors,
                null, null, null, null, null);
    }

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int fontStyle, int visualAnim, int storageMonitorMode, int storageStyle,
                                    int storageFilterMode, int storageFlowMode, List<ItemStack> watchlistItems) {
        this(pos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR,
                fontStyle, visualAnim, storageMonitorMode, storageStyle, storageFilterMode, storageFlowMode, watchlistItems,
                false, false, 1, new int[0], new int[0], new int[0]);
    }

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int fontStyle, int visualAnim, int storageMonitorMode, int storageStyle) {
        this(pos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim, storageMonitorMode, storageStyle, 0, 0, List.of());
    }

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int fontStyle, int visualAnim) {
        this(pos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, fontStyle, visualAnim, 0, 0, 0, 0, List.of());
    }

    public UpdateLcdScreenC2SPacket(RegistryFriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.text = buf.readUtf();
        this.mode = buf.readInt();
        this.mode2 = buf.readInt();
        this.align = buf.readInt();
        this.speed = buf.readInt();
        this.fontSize = buf.readInt();
        this.color = buf.readInt();
        this.rainbow = buf.readBoolean();
        this.glow = buf.readBoolean();
        this.theme = buf.readInt();
        this.widgetTL = buf.readInt();
        this.widgetTR = buf.readInt();
        this.widgetBL = buf.readInt();
        this.widgetBR = buf.readInt();
        this.fontStyle = buf.readInt();
        this.visualAnim = buf.readInt();
        this.storageMonitorMode = buf.readInt();
        this.storageStyle = buf.readInt();
        this.storageFilterMode = buf.readInt();
        this.storageFlowMode = buf.readInt();
        int watchCount = Math.min(LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS, Math.max(0, buf.readVarInt()));
        this.watchlistItems = new ArrayList<>(watchCount);
        for (int i = 0; i < watchCount; i++) {
            ItemStack st = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            if (!st.isEmpty()) {
                this.watchlistItems.add(st);
            }
        }
        this.loopEnabled = buf.readBoolean();
        this.loopShuffle = buf.readBoolean();
        this.loopTransition = Math.max(0, Math.min(9, buf.readVarInt()));
        int stepCount = Math.min(LcdScreenBlockEntity.MAX_LOOP_STEPS, Math.max(0, buf.readVarInt()));
        this.loopAnims = new int[stepCount];
        this.loopDurations = new int[stepCount];
        this.loopColors = new int[stepCount];
        this.loopTexts1 = new String[stepCount];
        this.loopTexts2 = new String[stepCount];
        this.loopModes1 = new int[stepCount];
        this.loopModes2 = new int[stepCount];
        this.loopAligns = new int[stepCount];
        for (int i = 0; i < stepCount; i++) {
            this.loopAnims[i] = Math.max(0, Math.min(60, buf.readVarInt()));
            this.loopDurations[i] = Math.max(1, Math.min(999, buf.readVarInt()));
            this.loopColors[i] = buf.readInt();
            if (this.loopAnims[i] == 0) {
                this.loopTexts1[i] = buf.readUtf();
                this.loopTexts2[i] = buf.readUtf();
                this.loopModes1[i] = buf.readByte();
                this.loopModes2[i] = buf.readByte();
                this.loopAligns[i] = buf.readByte();
            } else {
                this.loopTexts1[i] = "";
                this.loopTexts2[i] = "";
                this.loopModes1[i] = -1;
                this.loopModes2[i] = -1;
                this.loopAligns[i] = -1;
            }
        }
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.text);
        buf.writeInt(this.mode);
        buf.writeInt(this.mode2);
        buf.writeInt(this.align);
        buf.writeInt(this.speed);
        buf.writeInt(this.fontSize);
        buf.writeInt(this.color);
        buf.writeBoolean(this.rainbow);
        buf.writeBoolean(this.glow);
        buf.writeInt(this.theme);
        buf.writeInt(this.widgetTL);
        buf.writeInt(this.widgetTR);
        buf.writeInt(this.widgetBL);
        buf.writeInt(this.widgetBR);
        buf.writeInt(this.fontStyle);
        buf.writeInt(this.visualAnim);
        buf.writeInt(this.storageMonitorMode);
        buf.writeInt(this.storageStyle);
        buf.writeInt(this.storageFilterMode);
        buf.writeInt(this.storageFlowMode);
        int watchCount = Math.min(LcdScreenBlockEntity.MAX_WATCHLIST_ITEMS, this.watchlistItems.size());
        buf.writeVarInt(watchCount);
        for (int i = 0; i < watchCount; i++) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.watchlistItems.get(i));
        }
        buf.writeBoolean(this.loopEnabled);
        buf.writeBoolean(this.loopShuffle);
        buf.writeVarInt(this.loopTransition);
        int stepCount = this.loopAnims != null ? Math.min(LcdScreenBlockEntity.MAX_LOOP_STEPS, this.loopAnims.length) : 0;
        buf.writeVarInt(stepCount);
        for (int i = 0; i < stepCount; i++) {
            buf.writeVarInt(this.loopAnims[i]);
            buf.writeVarInt(i < this.loopDurations.length ? this.loopDurations[i] : 6);
            buf.writeInt(i < this.loopColors.length ? this.loopColors[i] : -1);
            if (this.loopAnims[i] == 0) {
                buf.writeUtf((this.loopTexts1 != null && i < this.loopTexts1.length && this.loopTexts1[i] != null) ? this.loopTexts1[i] : "");
                buf.writeUtf((this.loopTexts2 != null && i < this.loopTexts2.length && this.loopTexts2[i] != null) ? this.loopTexts2[i] : "");
                buf.writeByte((this.loopModes1 != null && i < this.loopModes1.length) ? this.loopModes1[i] : -1);
                buf.writeByte((this.loopModes2 != null && i < this.loopModes2.length) ? this.loopModes2[i] : -1);
                buf.writeByte((this.loopAligns != null && i < this.loopAligns.length) ? this.loopAligns[i] : -1);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) return;
        Level level = player.level();
        if (!level.isLoaded(this.pos)) return;

        BlockState masterState = level.getBlockState(this.pos);
        if (!(masterState.getBlock() instanceof LcdScreenBlock)) return;

        net.minecraft.core.Direction facing = masterState.getValue(LcdScreenBlock.FACING);
        net.minecraft.world.level.block.state.properties.AttachFace face = masterState.getValue(LcdScreenBlock.FACE);
        net.minecraft.core.Direction[] dirs = {
                LcdScreenBlock.getScreenUpDir(facing, face),
                LcdScreenBlock.getScreenDownDir(facing, face),
                LcdScreenBlock.getScreenLeftDir(facing, face),
                LcdScreenBlock.getScreenRightDir(facing, face)
        };

        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
        BlockPos trueMaster = LcdScreenBlock.getMasterPos(level, this.pos, masterState);
        queue.add(trueMaster);
        visited.add(trueMaster);

        while (!queue.isEmpty() && visited.size() <= 128) {
            BlockPos cur = queue.poll();
            if (level.getBlockEntity(cur) instanceof LcdScreenBlockEntity lcdBe) {
                lcdBe.setText(this.text);
                lcdBe.setMode(this.mode);
                lcdBe.setMode2(this.mode2);
                lcdBe.setAlign(this.align);
                lcdBe.setSpeed(this.speed);
                lcdBe.setFontSize(this.fontSize);
                lcdBe.setColor(this.color);
                lcdBe.setRainbow(this.rainbow);
                lcdBe.setGlow(this.glow);
                lcdBe.setTheme(this.theme);
                lcdBe.setWidgetTL(this.widgetTL);
                lcdBe.setWidgetTR(this.widgetTR);
                lcdBe.setWidgetBL(this.widgetBL);
                lcdBe.setWidgetBR(this.widgetBR);
                lcdBe.setFontStyle(this.fontStyle);
                lcdBe.setVisualAnim(this.visualAnim);
                lcdBe.setStorageMonitorMode(this.storageMonitorMode);
                lcdBe.setStorageStyle(this.storageStyle);
                lcdBe.setStorageFilterMode(this.storageFilterMode);
                lcdBe.setStorageFlowMode(this.storageFlowMode);
                lcdBe.setWatchlistItems(this.watchlistItems);
                lcdBe.setLoopConfig(this.loopEnabled, this.loopShuffle, this.loopTransition, this.loopAnims, this.loopDurations, this.loopColors,
                        this.loopTexts1, this.loopTexts2, this.loopModes1, this.loopModes2, this.loopAligns);

                BlockState st = level.getBlockState(cur);
                if (st.hasProperty(LcdScreenBlock.LIT) && st.getValue(LcdScreenBlock.LIT) != this.glow) {
                    level.setBlock(cur, st.setValue(LcdScreenBlock.LIT, this.glow), 3);
                }

                lcdBe.setChanged();
                lcdBe.sync();
            }

            for (net.minecraft.core.Direction d : dirs) {
                if (LcdScreenBlock.isConnectedNeighbor(level, cur, d, facing, face)) {
                    BlockPos next = cur.relative(d);
                    if (visited.add(next)) {
                        queue.add(next);
                    }
                }
            }
        }

        if (level.getBlockEntity(trueMaster) instanceof LcdScreenBlockEntity masterBe) {
            masterBe.refreshStorageStats(level);
        }
    }
}
