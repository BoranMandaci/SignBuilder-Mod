package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.SignMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
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
    private SignMaterial savedMaterial = SignMaterial.DEFAULT;

    private boolean hasBackplate = false;
    private SignMaterial backplateFrontMaterial = SignMaterial.DEFAULT;
    private SignMaterial backplateBackMaterial = SignMaterial.DEFAULT;
    private int backplateFrontColor = 0xFFFFFF;
    private int backplateBackColor = 0xFFFFFF;
    private boolean backplateFrontRainbow = false;
    private boolean backplateBackRainbow = false;

    private boolean isAudioPlaying = false;

    public LetterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setSavedMaterial(SignMaterial mat) { this.savedMaterial = mat; setChanged(); sync(); }
    public SignMaterial getSavedMaterial() { return this.savedMaterial; }
    public SignMaterial getMaterial() { return this.savedMaterial; }

    public void setActive(boolean active) { this.isActive = active; setChanged(); sync(); }
    public boolean isActive() { return this.isActive; }
    public boolean isGlowing() { return this.isActive; }
    public void setGlowing(boolean glowing) { this.isActive = glowing; setChanged(); sync(); }

    public void setWrenchMode(int mode) { this.wrenchMode = mode; setChanged(); sync(); }
    public int getWrenchMode() { return this.wrenchMode; }
    public void setDetectsMonsters(boolean detects) { this.detectsMonsters = detects; setChanged(); sync(); }
    public boolean doesDetectMonsters() { return this.detectsMonsters; }
    public void setDetectsAnimals(boolean detects) { this.detectsAnimals = detects; setChanged(); sync(); }
    public boolean doesDetectAnimals() { return this.detectsAnimals; }

    public void setRgbColor(int color) { this.rgbColor = color; this.isRainbow = false; setChanged(); sync(); }
    public int getRgbColor() { return rgbColor; }
    public int getColorIndex() { return 0; }
    public void setRainbow(boolean rainbow) { this.isRainbow = rainbow; setChanged(); sync(); }
    public boolean isRainbow() { return isRainbow; }

    public boolean hasBackplate() { return this.hasBackplate; }
    public void setHasBackplate(boolean hasBackplate) { this.hasBackplate = hasBackplate; setChanged(); sync(); }

    public SignMaterial getBackplateFrontMaterial() { return backplateFrontMaterial; }
    public void setBackplateFrontMaterial(SignMaterial mat) { this.backplateFrontMaterial = mat; setChanged(); sync(); }

    public SignMaterial getBackplateBackMaterial() { return backplateBackMaterial; }
    public void setBackplateBackMaterial(SignMaterial mat) { this.backplateBackMaterial = mat; setChanged(); sync(); }

    public int getBackplateFrontColor() { return backplateFrontColor; }
    public void setBackplateFrontColor(int color) { this.backplateFrontColor = color; this.backplateFrontRainbow = false; setChanged(); sync(); }

    public int getBackplateBackColor() { return backplateBackColor; }
    public void setBackplateBackColor(int color) { this.backplateBackColor = color; this.backplateBackRainbow = false; setChanged(); sync(); }

    public boolean isBackplateFrontRainbow() { return backplateFrontRainbow; }
    public void setBackplateFrontRainbow(boolean rainbow) { this.backplateFrontRainbow = rainbow; setChanged(); sync(); }

    public boolean isBackplateBackRainbow() { return backplateBackRainbow; }
    public void setBackplateBackRainbow(boolean rainbow) { this.backplateBackRainbow = rainbow; setChanged(); sync(); }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("RGBColor", this.rgbColor);
        tag.putBoolean("IsRainbow", this.isRainbow);
        tag.putInt("WrenchMode", this.wrenchMode);
        tag.putBoolean("IsActive", this.isActive);
        tag.putBoolean("Glowing", this.isActive);
        tag.putBoolean("DetectsMonsters", this.detectsMonsters);
        tag.putBoolean("DetectsAnimals", this.detectsAnimals);
        tag.putString("SavedMaterial", this.savedMaterial.name());

        tag.putBoolean("HasBackplate", this.hasBackplate);
        tag.putString("BPFrontMat", this.backplateFrontMaterial.name());
        tag.putString("BPBackMat", this.backplateBackMaterial.name());
        tag.putInt("BPFrontColor", this.backplateFrontColor);
        tag.putInt("BPBackColor", this.backplateBackColor);
        tag.putBoolean("BPFrontRainbow", this.backplateFrontRainbow);
        tag.putBoolean("BPBackRainbow", this.backplateBackRainbow);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("IsRainbow")) this.isRainbow = tag.getBoolean("IsRainbow");
        if (tag.contains("RGBColor") && !this.isRainbow) this.rgbColor = tag.getInt("RGBColor");
        if (tag.contains("SavedMaterial")) {
            try { this.savedMaterial = SignMaterial.valueOf(tag.getString("SavedMaterial").toUpperCase()); } catch (Exception ignored) {}
        }
        if (tag.contains("WrenchMode")) this.wrenchMode = tag.getInt("WrenchMode");
        if (tag.contains("IsActive")) this.isActive = tag.getBoolean("IsActive");
        else if (tag.contains("Glowing")) this.isActive = tag.getBoolean("Glowing");

        if (tag.contains("DetectsMonsters")) this.detectsMonsters = tag.getBoolean("DetectsMonsters");
        if (tag.contains("DetectsAnimals")) this.detectsAnimals = tag.getBoolean("DetectsAnimals");

        if (tag.contains("HasBackplate")) this.hasBackplate = tag.getBoolean("HasBackplate");
        if (tag.contains("BPFrontMat")) {
            try { this.backplateFrontMaterial = SignMaterial.valueOf(tag.getString("BPFrontMat").toUpperCase()); } catch (Exception ignored) {}
        }
        if (tag.contains("BPBackMat")) {
            try { this.backplateBackMaterial = SignMaterial.valueOf(tag.getString("BPBackMat").toUpperCase()); } catch (Exception ignored) {}
        }
        if (tag.contains("BPFrontColor")) this.backplateFrontColor = tag.getInt("BPFrontColor");
        if (tag.contains("BPBackColor")) this.backplateBackColor = tag.getInt("BPBackColor");
        if (tag.contains("BPFrontRainbow")) this.backplateFrontRainbow = tag.getBoolean("BPFrontRainbow");
        if (tag.contains("BPBackRainbow")) this.backplateBackRainbow = tag.getBoolean("BPBackRainbow");

        if (level != null && level.isClientSide) {
            dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () ->
                    com.boran.signbuilder.client.ClientHooks.setBlocksDirty(worldPosition)
            );
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getDroppedItemStack(BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock());
        CompoundTag beTag = new CompoundTag();

        beTag.putInt("RGBColor", this.rgbColor);
        beTag.putBoolean("IsRainbow", this.isRainbow);
        beTag.putInt("WrenchMode", this.wrenchMode);
        beTag.putBoolean("IsActive", this.isActive);
        beTag.putBoolean("Glowing", this.isActive);
        beTag.putBoolean("DetectsMonsters", this.detectsMonsters);
        beTag.putBoolean("DetectsAnimals", this.detectsAnimals);
        beTag.putString("SavedMaterial", this.savedMaterial.name());

        beTag.putBoolean("HasBackplate", this.hasBackplate);
        beTag.putString("BPFrontMat", this.backplateFrontMaterial.name());
        beTag.putString("BPBackMat", this.backplateBackMaterial.name());
        beTag.putInt("BPFrontColor", this.backplateFrontColor);
        beTag.putInt("BPBackColor", this.backplateBackColor);
        beTag.putBoolean("BPFrontRainbow", this.backplateFrontRainbow);
        beTag.putBoolean("BPBackRainbow", this.backplateBackRainbow);

        stack.getOrCreateTag().put("BlockEntityTag", beTag);

        CompoundTag stateTag = new CompoundTag();
        stateTag.putString("material", this.savedMaterial.name().toLowerCase());
        stack.getOrCreateTag().put("BlockStateTag", stateTag);

        return stack;
    }

    public static int getActualHexColor(int colorValue) {
        if (colorValue > 15 || colorValue < -1) return colorValue;
        return switch (colorValue) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8; case 4 -> 0xE5E533; case 5 -> 0x7FCC19;
            case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C; case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2;
            case 11 -> 0x334CB2; case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0xCF2323; case 15 -> 0x191919;
            default -> 0xFFFFFF;
        };
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, LetterBlockEntity entity) {
        if (level.isClientSide) {
            boolean dirty = false;
            float hue = (level.getGameTime() % 120) / 120f;
            int rainbowRgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;

            if (entity.isRainbow()) {
                entity.rgbColor = rainbowRgb;
                dirty = true;
            }
            if (entity.isBackplateFrontRainbow()) {
                entity.backplateFrontColor = rainbowRgb;
                dirty = true;
            }
            if (entity.isBackplateBackRainbow()) {
                entity.backplateBackColor = rainbowRgb;
                dirty = true;
            }

            if (dirty && level.getGameTime() % 5 == 0) {
                dev.architectury.utils.EnvExecutor.runInEnv(dev.architectury.utils.Env.CLIENT, () -> () -> {
                    net.minecraft.client.Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
                });
            }
        }

        if (!level.isClientSide()) {
            if (entity.getWrenchMode() == 0 || entity.getWrenchMode() == 10) return;

            boolean isCurrentlyGlowing = entity.isActive();
            boolean shouldGlow = false;
            long time = level.getGameTime();

            if (entity.isActive() || entity.getWrenchMode() != 0) {
                switch (entity.getWrenchMode()) {
                    case 1: shouldGlow = (time % 20) < 10; break;
                    case 2: shouldGlow = isCurrentlyGlowing; if (time % 4 == 0 && Math.random() > 0.7) shouldGlow = !isCurrentlyGlowing; break;
                    case 3: shouldGlow = Math.sin((time / 6.0) - ((pos.getX() + pos.getY() + pos.getZ()) * 0.8)) > 0.0; break;
                    case 4: shouldGlow = (time % 60) < 30; break;
                    case 5:
                        if (time % 10 == 0) {
                            AABB bounds = new AABB(pos).inflate(6.0);
                            shouldGlow = !level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, bounds,
                                    t -> t instanceof net.minecraft.world.entity.player.Player ||
                                            (entity.doesDetectMonsters() && t instanceof net.minecraft.world.entity.monster.Monster) ||
                                            (entity.doesDetectAnimals() && t instanceof net.minecraft.world.entity.animal.Animal)).isEmpty();
                        } else shouldGlow = isCurrentlyGlowing; break;
                    case 6: shouldGlow = (time % 20 == 0) ? level.isNight() : isCurrentlyGlowing; break;
                    case 7:
                        if (time % 10 == 0) {
                            entity.isAudioPlaying = false;
                            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 5, 5))) {
                                if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.JukeboxBlockEntity jbe) {
                                    if (jbe.isRecordPlaying()) { entity.isAudioPlaying = true; break; }
                                }
                            }
                        }
                        shouldGlow = entity.isAudioPlaying && Math.random() > 0.2;
                        break;
                    case 8: shouldGlow = (time % 6) < 3; break;
                    case 9:
                        if (time % 5 == 0) {
                            AABB bounds = new AABB(pos);
                            shouldGlow = false;
                            for (net.minecraft.world.entity.player.Player p : level.players()) {
                                if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 256) {
                                    net.minecraft.world.phys.Vec3 eye = p.getEyePosition();
                                    net.minecraft.world.phys.Vec3 look = p.getLookAngle();
                                    net.minecraft.world.phys.Vec3 end = eye.add(look.x * 16, look.y * 16, look.z * 16);
                                    if (bounds.clip(eye, end).isPresent()) {
                                        shouldGlow = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            shouldGlow = isCurrentlyGlowing;
                        }
                        break;
                }
            }
            if (shouldGlow != isCurrentlyGlowing) {
                entity.setActive(shouldGlow);
            }
        }
    }
}