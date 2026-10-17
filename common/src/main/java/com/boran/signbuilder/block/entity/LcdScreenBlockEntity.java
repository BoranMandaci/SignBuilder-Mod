package com.boran.signbuilder.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LcdScreenBlockEntity extends BlockEntity {

    private String text = "";
    private int mode = 0;
    private int mode2 = 0;
    private int align = 1;
    private int speed = 2;
    private int fontSize = 1;
    private int color = 0x00FF66; // Neon green
    private boolean rainbow = false;
    private boolean glow = true;
    private int theme = 0;
    private int widgetTL = 0;
    private int widgetTR = 0;
    private int widgetBL = 0;
    private int widgetBR = 0;
    private int fontStyle = 0;
    private int visualAnim = 0;
    public long lastRenderNanoTime = 0L;

    public BlockPos cachedMasterPos = null;
    public int cachedWidthBlocks = 1;
    public int cachedHeightBlocks = 1;
    public long lastCacheUpdateTick = -100L;

    public void invalidateGridCache() {
        this.cachedMasterPos = null;
        this.lastCacheUpdateTick = -100L;
    }

    public LcdScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LCD_SCREEN_BLOCK_ENTITY.get(), pos, state);
    }

    public String getText() {
        return text != null ? text : "";
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
        setChanged();
        sync();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = Math.max(0, Math.min(9, mode));
        setChanged();
        sync();
    }

    public int getMode2() {
        return mode2;
    }

    public void setMode2(int mode2) {
        this.mode2 = Math.max(0, Math.min(9, mode2));
        setChanged();
        sync();
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = Math.max(0, Math.min(2, align));
        setChanged();
        sync();
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = Math.max(1, Math.min(4, speed));
        setChanged();
        sync();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = Math.max(0, Math.min(3, fontSize));
        setChanged();
        sync();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        setChanged();
        sync();
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
        setChanged();
        sync();
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
        setChanged();
        sync();
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = Math.max(0, Math.min(14, theme));
        setChanged();
        sync();
    }

    public int getWidgetTL() {
        return widgetTL;
    }

    public void setWidgetTL(int widgetTL) {
        this.widgetTL = Math.max(0, Math.min(10, widgetTL));
        setChanged();
        sync();
    }

    public int getWidgetTR() {
        return widgetTR;
    }

    public void setWidgetTR(int widgetTR) {
        this.widgetTR = Math.max(0, Math.min(10, widgetTR));
        setChanged();
        sync();
    }

    public int getWidgetBL() {
        return widgetBL;
    }

    public void setWidgetBL(int widgetBL) {
        this.widgetBL = Math.max(0, Math.min(10, widgetBL));
        setChanged();
        sync();
    }

    public int getWidgetBR() {
        return widgetBR;
    }

    public void setWidgetBR(int widgetBR) {
        this.widgetBR = Math.max(0, Math.min(10, widgetBR));
        setChanged();
        sync();
    }

    public boolean hasWidgets() {
        return widgetTL > 0 || widgetTR > 0 || widgetBL > 0 || widgetBR > 0;
    }

    public boolean isShowTime() { return widgetTL == 1 || widgetTR == 1 || widgetBL == 1 || widgetBR == 1; }
    public void setShowTime(boolean b) { if (b) widgetTL = 1; else if (widgetTL == 1) widgetTL = 0; setChanged(); sync(); }
    public boolean isShowWeather() { return widgetTL == 2 || widgetTR == 2 || widgetBL == 2 || widgetBR == 2; }
    public void setShowWeather(boolean b) { if (b) widgetTR = 2; else if (widgetTR == 2) widgetTR = 0; setChanged(); sync(); }
    public boolean isShowPlayer() { return widgetTL == 3 || widgetTR == 3 || widgetBL == 3 || widgetBR == 3; }
    public void setShowPlayer(boolean b) { if (b) widgetBL = 3; else if (widgetBL == 3) widgetBL = 0; setChanged(); sync(); }
    public boolean isShowDay() { return widgetTL == 4 || widgetTR == 4 || widgetBL == 4 || widgetBR == 4; }
    public void setShowDay(boolean b) { if (b) widgetBR = 4; else if (widgetBR == 4) widgetBR = 0; setChanged(); sync(); }

    public int getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = Math.max(0, Math.min(4, fontStyle));
        setChanged();
        sync();
    }

    public int getVisualAnim() {
        return visualAnim;
    }

    public void setVisualAnim(int visualAnim) {
        this.visualAnim = Math.max(0, Math.min(24, visualAnim));
        setChanged();
        sync();
    }

    public void copyFrom(LcdScreenBlockEntity other) {
        if (other == null) return;
        this.text = other.text;
        this.mode = other.mode;
        this.mode2 = other.mode2;
        this.align = other.align;
        this.speed = other.speed;
        this.fontSize = other.fontSize;
        this.color = other.color;
        this.rainbow = other.rainbow;
        this.glow = other.glow;
        this.theme = other.theme;
        this.widgetTL = other.widgetTL;
        this.widgetTR = other.widgetTR;
        this.widgetBL = other.widgetBL;
        this.widgetBR = other.widgetBR;
        this.fontStyle = other.fontStyle;
        this.visualAnim = other.visualAnim;
    }

    public void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("LcdText", getText());
        tag.putInt("LcdMode", this.mode);
        tag.putInt("LcdMode2", this.mode2);
        tag.putInt("LcdAlign", this.align);
        tag.putInt("LcdSpeed", this.speed);
        tag.putInt("LcdFontSize", this.fontSize);
        tag.putInt("LcdColor", this.color);
        tag.putBoolean("LcdRainbow", this.rainbow);
        tag.putBoolean("LcdGlow", this.glow);
        tag.putInt("LcdTheme", this.theme);
        tag.putInt("LcdWidgetTL", this.widgetTL);
        tag.putInt("LcdWidgetTR", this.widgetTR);
        tag.putInt("LcdWidgetBL", this.widgetBL);
        tag.putInt("LcdWidgetBR", this.widgetBR);
        tag.putInt("LcdFontStyle", this.fontStyle);
        tag.putBoolean("LcdShowTime", isShowTime());
        tag.putBoolean("LcdShowWeather", isShowWeather());
        tag.putBoolean("LcdShowPlayer", isShowPlayer());
        tag.putBoolean("LcdShowDay", isShowDay());
        tag.putInt("LcdVisualAnim", this.visualAnim);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("LcdText")) this.text = tag.getString("LcdText");
        if (tag.contains("LcdMode")) this.mode = tag.getInt("LcdMode");
        if (tag.contains("LcdMode2")) this.mode2 = tag.getInt("LcdMode2");
        else this.mode2 = this.mode;
        if (tag.contains("LcdAlign")) this.align = tag.getInt("LcdAlign");
        if (tag.contains("LcdSpeed")) this.speed = tag.getInt("LcdSpeed");
        if (tag.contains("LcdFontSize")) this.fontSize = tag.getInt("LcdFontSize");
        if (tag.contains("LcdColor")) this.color = tag.getInt("LcdColor");
        if (tag.contains("LcdRainbow")) this.rainbow = tag.getBoolean("LcdRainbow");
        if (tag.contains("LcdGlow")) this.glow = tag.getBoolean("LcdGlow");
        if (tag.contains("LcdTheme")) this.theme = tag.getInt("LcdTheme");
        if (tag.contains("LcdWidgetTL")) this.widgetTL = tag.getInt("LcdWidgetTL");
        else if (tag.contains("LcdShowTime") && tag.getBoolean("LcdShowTime")) this.widgetTL = 1;
        if (tag.contains("LcdWidgetTR")) this.widgetTR = tag.getInt("LcdWidgetTR");
        else if (tag.contains("LcdShowWeather") && tag.getBoolean("LcdShowWeather")) this.widgetTR = 2;
        if (tag.contains("LcdWidgetBL")) this.widgetBL = tag.getInt("LcdWidgetBL");
        else if (tag.contains("LcdShowPlayer") && tag.getBoolean("LcdShowPlayer")) this.widgetBL = 3;
        if (tag.contains("LcdWidgetBR")) this.widgetBR = tag.getInt("LcdWidgetBR");
        else if (tag.contains("LcdShowDay") && tag.getBoolean("LcdShowDay")) this.widgetBR = 4;
        if (tag.contains("LcdFontStyle")) this.fontStyle = tag.getInt("LcdFontStyle");
        if (tag.contains("LcdVisualAnim")) this.visualAnim = tag.getInt("LcdVisualAnim");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(32.0);
    }
}
