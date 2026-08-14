package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LetterBlockEntity extends BlockEntity {
    private int rgbColor = 0xFFFFFF;
    private boolean isRainbow = false;
    private int wrenchMode = 0;
    private boolean isActive = false;
    private boolean detectsMonsters = true;
    private boolean detectsAnimals = false; 

    public LetterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        if (state.hasProperty(ModBlocks.COLOR)) {
            this.rgbColor = getActualHexColor(state.getValue(ModBlocks.COLOR));
        }
        if (state.hasProperty(ModBlocks.GLOWING)) {
            this.isActive = state.getValue(ModBlocks.GLOWING);
        }
    }

    public void setActive(boolean active) { this.isActive = active; setChanged(); }
    public boolean isActive() { return this.isActive; }

    public void setWrenchMode(int mode) {
        this.wrenchMode = mode;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public int getWrenchMode() { return this.wrenchMode; }

    public void setDetectsMonsters(boolean detects) { this.detectsMonsters = detects; setChanged(); }
    public boolean doesDetectMonsters() { return this.detectsMonsters; }

    public void setDetectsAnimals(boolean detects) { this.detectsAnimals = detects; setChanged(); }
    public boolean doesDetectAnimals() { return this.detectsAnimals; }

    public void setRgbColor(int color) {
        this.rgbColor = color;
        this.isRainbow = false;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public int getRgbColor() { return rgbColor; }

    public void setRainbow(boolean rainbow) {
        this.isRainbow = rainbow;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public boolean isRainbow() { return isRainbow; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RGBColor", this.rgbColor);
        tag.putBoolean("IsRainbow", this.isRainbow);
        tag.putInt("WrenchMode", this.wrenchMode);
        tag.putBoolean("IsActive", this.isActive);
        tag.putBoolean("DetectsMonsters", this.detectsMonsters);
        tag.putBoolean("DetectsAnimals", this.detectsAnimals); // Kaydet
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("IsRainbow")) this.isRainbow = tag.getBoolean("IsRainbow");
        if (tag.contains("RGBColor")) {
            if (!this.isRainbow) this.rgbColor = tag.getInt("RGBColor");
        }
        if (tag.contains("WrenchMode")) this.wrenchMode = tag.getInt("WrenchMode");
        if (tag.contains("IsActive")) this.isActive = tag.getBoolean("IsActive");
        if (tag.contains("DetectsMonsters")) this.detectsMonsters = tag.getBoolean("DetectsMonsters");
        if (tag.contains("DetectsAnimals")) this.detectsAnimals = tag.getBoolean("DetectsAnimals"); // Yükle
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static int getActualHexColor(int colorValue) {
        if (colorValue > 15) return colorValue;
        return switch (colorValue) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8;
            case 4 -> 0xE5E533; case 5 -> 0x7FCC19; case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C;
            case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2; case 11 -> 0x334CB2;
            case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0xCF2323; case 15 -> 0x191919;
            default -> 0xFFFFFF;
        };
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, LetterBlockEntity entity) {
        if (level.isClientSide && entity.isRainbow()) {
            float hue = (level.getGameTime() % 120) / 120f;
            entity.rgbColor = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
            if (level.getGameTime() % 5 == 0) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        if (!level.isClientSide() && state.hasProperty(ModBlocks.GLOWING)) {
            if (entity.getWrenchMode() == 0) return;

            boolean isCurrentlyGlowing = state.getValue(ModBlocks.GLOWING);
            boolean shouldGlow = false;
            long time = level.getGameTime();

            if (entity.isActive()) {
                switch (entity.getWrenchMode()) {
                    case 1:
                        shouldGlow = (time % 20) < 10;
                        break;
                    case 2:
                        shouldGlow = isCurrentlyGlowing;
                        if (time % 4 == 0) {
                            if (Math.random() > 0.7) shouldGlow = !isCurrentlyGlowing;
                        }
                        break;
                    case 3:
                        double spatialPosition = pos.getX() + pos.getY() + pos.getZ();
                        double sineWave = Math.sin((time / 6.0) - (spatialPosition * 0.8));
                        shouldGlow = sineWave > 0.0;
                        break;
                    case 4:
                        shouldGlow = (time % 60) < 30;
                        break;
                    case 5:
                        if (time % 10 == 0) {
                            AABB bounds = new AABB(pos).inflate(6.0);
                            shouldGlow = !level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, bounds,
                                    target -> target instanceof net.minecraft.world.entity.player.Player ||
                                            (entity.doesDetectMonsters() && target instanceof net.minecraft.world.entity.monster.Monster) ||
                                            (entity.doesDetectAnimals() && target instanceof net.minecraft.world.entity.animal.Animal)
                            ).isEmpty();
                        } else {
                            shouldGlow = isCurrentlyGlowing;
                        }
                        break;
                    case 6:
                        if (time % 20 == 0) {
                            shouldGlow = level.isNight();
                        } else {
                            shouldGlow = isCurrentlyGlowing;
                        }
                        break;
                }
            }

            if (shouldGlow != isCurrentlyGlowing) {
                level.setBlock(pos, state.setValue(ModBlocks.GLOWING, shouldGlow), 3);
            }
        }
    }
}