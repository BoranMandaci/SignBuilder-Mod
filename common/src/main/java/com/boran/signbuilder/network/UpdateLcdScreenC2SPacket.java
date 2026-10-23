package com.boran.signbuilder.network;

import com.boran.signbuilder.block.LcdScreenBlock;
import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class UpdateLcdScreenC2SPacket {
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

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int fontStyle, int visualAnim) {
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
    }

    public UpdateLcdScreenC2SPacket(BlockPos pos, String text, int mode, int mode2, int align, int speed, int fontSize,
                                    int color, boolean rainbow, boolean glow,
                                    int theme, int widgetTL, int widgetTR, int widgetBL, int widgetBR,
                                    int visualAnim) {
        this(pos, text, mode, mode2, align, speed, fontSize, color, rainbow, glow, theme, widgetTL, widgetTR, widgetBL, widgetBR, 0, visualAnim);
    }

    public UpdateLcdScreenC2SPacket(FriendlyByteBuf buf) {
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
    }

    public void toBytes(FriendlyByteBuf buf) {
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
    }

    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (player == null) return;
        Level level = player.level;
        if (!level.isLoaded(this.pos)) return;

        BlockState masterState = level.getBlockState(this.pos);
        if (!(masterState.getBlock() instanceof LcdScreenBlock)) return;

        Direction facing = masterState.getValue(LcdScreenBlock.FACING);
        AttachFace face = masterState.getValue(LcdScreenBlock.FACE);
        Direction[] dirs = {
                LcdScreenBlock.getScreenUpDir(facing, face),
                LcdScreenBlock.getScreenDownDir(facing, face),
                LcdScreenBlock.getScreenLeftDir(facing, face),
                LcdScreenBlock.getScreenRightDir(facing, face)
        };

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
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

                BlockState st = level.getBlockState(cur);
                if (st.hasProperty(LcdScreenBlock.LIT) && st.getValue(LcdScreenBlock.LIT) != this.glow) {
                    level.setBlock(cur, st.setValue(LcdScreenBlock.LIT, this.glow), 3);
                }

                lcdBe.setChanged();
                lcdBe.sync();
            }

            for (Direction d : dirs) {
                if (LcdScreenBlock.isConnectedNeighbor(level, cur, d, facing, face)) {
                    BlockPos next = cur.relative(d);
                    if (visited.add(next)) {
                        queue.add(next);
                    }
                }
            }
        }
    }
}
