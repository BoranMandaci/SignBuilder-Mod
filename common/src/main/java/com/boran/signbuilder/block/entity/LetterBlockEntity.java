package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.SignMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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

    private int size = 1;
    private boolean isDummy = false;
    @Nullable
    private BlockPos masterPos = null;

    private boolean isAudioPlaying = false;

    private int buttonMode = 0;
    private boolean syncWord = false;
    private boolean isPressed = false;
    private int pressTicks = 0;
    private String pinCode = "";
    private String enteredBuffer = "";
    private int pinResetTicks = 0;
    private boolean isPinPowered = false;
    private final List<BlockPos> enteredPositions = new ArrayList<>();

    public LetterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getSize() { return this.size; }
    public void setSize(int size) { this.size = size; setChanged(); sync(); }

    public boolean isBig() { return this.size == 2; }
    public void setBig(boolean big) { this.size = big ? 2 : 1; setChanged(); sync(); }

    public boolean is3x3() { return this.size == 3; }
    public void set3x3(boolean is3x3) { this.size = is3x3 ? 3 : 1; setChanged(); sync(); }

    public int getBlockCount() { return this.size * this.size; }

    public boolean isDummy() { return this.isDummy; }
    public void setDummy(boolean dummy) { this.isDummy = dummy; setChanged(); sync(); }

    @Nullable
    public BlockPos getMasterPos() { return this.masterPos; }
    public void setMasterPos(@Nullable BlockPos pos) { this.masterPos = pos; setChanged(); sync(); }

    public LetterBlockEntity getEffectiveMaster() {
        if (this.isDummy && this.masterPos != null && this.level != null) {
            if (this.level.getBlockEntity(this.masterPos) instanceof LetterBlockEntity master) {
                return master;
            }
        }
        return this;
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

    public int getButtonMode() { return this.buttonMode; }
    public void setButtonMode(int mode) { this.buttonMode = mode; setChanged(); sync(); }

    public boolean isSyncWord() { return this.syncWord; }
    public void setSyncWord(boolean sync) { this.syncWord = sync; setChanged(); sync(); }

    public boolean isPressed() { return this.isPressed; }
    public void setPressed(boolean pressed) { this.isPressed = pressed; setChanged(); sync(); }

    public boolean isPinPowered() { return this.isPinPowered; }
    public void setPinPowered(boolean pinPowered) { this.isPinPowered = pinPowered; setChanged(); sync(); }

    public String getPinCode() { return this.pinCode; }
    public void setPinCode(String pin) { this.pinCode = pin != null ? pin : ""; setChanged(); sync(); }

    public boolean isWoodMaterial() {
        return switch (this.savedMaterial) {
            case OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, MANGROVE, CHERRY, BAMBOO, CRIMSON_PLANKS, WARPED_PLANKS -> true;
            default -> false;
        };
    }

    public boolean isMetalMaterial() {
        return switch (this.savedMaterial) {
            case IRON, GOLD, COPPER_BLOCK, NETHERITE_BLOCK -> true;
            default -> false;
        };
    }

    public void triggerPress(@Nullable Player player) {
        if (this.level == null) return;

        if (this.isDummy) {
            LetterBlockEntity master = getEffectiveMaster();
            if (master != this) {
                master.triggerPress(player);
                return;
            }
        }

        if (this.buttonMode == 0) return;

        if (this.buttonMode == 1) {
            if (this.isPressed) return;
            int duration = isWoodMaterial() ? 30 : 20;
            applyPressedState(true, duration);
            playPressSound(true);
        } else if (this.buttonMode == 2) {
            boolean nextState = !this.isPressed;
            applyPressedState(nextState, 0);
            playPressSound(nextState);
        } else if (this.buttonMode == 3) {
            handlePinInput(player);
        }
    }

    private void handlePinInput(@Nullable Player player) {
        if (this.level == null || this.level.isClientSide()) return;

        List<LetterBlockEntity> wordMembers = collectConnectedWord();
        if (wordMembers.isEmpty()) return;

        LetterBlockEntity controller = wordMembers.get(0);
        for (LetterBlockEntity member : wordMembers) {
            if (member.getBlockPos().compareTo(controller.getBlockPos()) < 0) {
                controller = member;
            }
        }

        if (controller.isPinPowered) {
            return;
        }

        String targetPin = controller.getPinCode();
        if (targetPin.isEmpty()) {
            for (LetterBlockEntity member : wordMembers) {
                if (!member.getPinCode().isEmpty()) {
                    targetPin = member.getPinCode();
                    controller.setPinCode(targetPin);
                    break;
                }
            }
        }
        if (targetPin.isEmpty()) {
            targetPin = this.pinCode;
            controller.setPinCode(targetPin);
        }
        if (targetPin.isEmpty()) {
            return;
        }

        String cleanTargetPin = targetPin.replace(" ", "");
        if (cleanTargetPin.isEmpty()) return;

        String myChar = LetterBlock.getCharacterFromBlock(getBlockState().getBlock());
        this.applyPressedState(true, 0);
        this.playPressSound(true);

        controller.enteredBuffer += myChar;
        controller.enteredPositions.add(this.worldPosition);
        controller.pinResetTicks = 120;

        if (controller.enteredBuffer.length() >= cleanTargetPin.length()) {
            boolean isMatch = controller.enteredBuffer.equalsIgnoreCase(cleanTargetPin);

            if (isMatch) {
                for (BlockPos p : controller.enteredPositions) {
                    BlockEntity be = this.level.getBlockEntity(p);
                    if (be instanceof LetterBlockEntity rawLbe) {
                        LetterBlockEntity member = LetterBlock.findMaster(this.level, p, rawLbe);
                        member.isPinPowered = true;
                        member.applyPressedState(true, 40);
                    }
                }
                this.level.playSound(null, this.worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8F, 1.2F);
            } else {
                for (BlockPos p : controller.enteredPositions) {
                    BlockEntity be = this.level.getBlockEntity(p);
                    if (be instanceof LetterBlockEntity rawLbe) {
                        LetterBlockEntity member = LetterBlock.findMaster(this.level, p, rawLbe);
                        member.isPinPowered = false;
                        member.applyPressedState(false, 0);
                    }
                }
                this.level.playSound(null, this.worldPosition, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            }
            controller.enteredBuffer = "";
            controller.enteredPositions.clear();
            controller.pinResetTicks = 0;
        }
    }

    private List<LetterBlockEntity> collectConnectedWord() {
        List<LetterBlockEntity> members = new ArrayList<>();
        if (this.level == null) return members;

        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> visitedMasters = new HashSet<>();

        queue.add(this.worldPosition);
        visited.add(this.worldPosition);

        while (!queue.isEmpty() && visited.size() <= 64) {
            BlockPos current = queue.poll();
            BlockEntity be = this.level.getBlockEntity(current);
            if (be instanceof LetterBlockEntity rawLbe) {
                LetterBlockEntity master = LetterBlock.findMaster(this.level, current, rawLbe);
                if (visitedMasters.add(master.getBlockPos())) {
                    members.add(master);
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (!visited.contains(neighbor)) {
                            BlockState ns = this.level.getBlockState(neighbor);
                            if (ns.getBlock() instanceof LetterBlock) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        return members;
    }

    public void applyPressedState(boolean pressed, int duration) {
        this.isPressed = pressed;
        this.pressTicks = duration;
        setChanged();
        sync();

        if (this.level != null && !this.level.isClientSide()) {
            notifyRedstoneNeighbors(this.worldPosition);

            if (this.size > 1) {
                BlockPos[] positions = (this.size == 3)
                        ? LetterBlock.get3x3BlockPositions(this.worldPosition, getBlockState())
                        : LetterBlock.getBigBlockPositions(this.worldPosition, getBlockState());

                for (BlockPos p : positions) {
                    if (!p.equals(this.worldPosition)) {
                        BlockEntity be = this.level.getBlockEntity(p);
                        if (be instanceof LetterBlockEntity dummy) {
                            dummy.isPressed = pressed;
                            dummy.pressTicks = duration;
                            dummy.setChanged();
                            dummy.sync();
                            notifyRedstoneNeighbors(p);
                        }
                    }
                }
            }
        }
    }

    private void releaseButton() {
        if (this.level == null) return;
        this.isPinPowered = false;
        applyPressedState(false, 0);
        playPressSound(false);
    }

    private void notifyRedstoneNeighbors(BlockPos pos) {
        if (this.level == null) return;
        this.level.updateNeighborsAt(pos, getBlockState().getBlock());
        for (Direction dir : Direction.values()) {
            this.level.updateNeighborsAt(pos.relative(dir), getBlockState().getBlock());
        }
    }

    public void playPressSound(boolean pressOn) {
        if (this.level == null) return;
        SoundEvent sound;

        if (isWoodMaterial()) {
            sound = pressOn ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
        } else if (isMetalMaterial()) {
            sound = pressOn ? SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON : SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF;
        } else {
            sound = pressOn ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
        }

        float pitch = pressOn ? 0.6F : 0.5F;
        this.level.playSound(null, this.worldPosition, sound, SoundSource.BLOCKS, 0.4F, pitch);
    }

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

        tag.putInt("Size", this.size);
        tag.putBoolean("IsBig", this.size == 2);
        tag.putBoolean("IsDummy", this.isDummy);
        if (this.masterPos != null) {
            tag.put("MasterPos", NbtUtils.writeBlockPos(this.masterPos));
        }

        if (this.buttonMode != 0) tag.putInt("ButtonMode", this.buttonMode);
        if (this.syncWord) tag.putBoolean("SyncWord", this.syncWord);
        if (this.isPressed) tag.putBoolean("IsPressed", this.isPressed);
        if (this.pressTicks > 0) tag.putInt("PressTicks", this.pressTicks);
        if (!this.pinCode.isEmpty()) tag.putString("PinCode", this.pinCode);
        tag.putBoolean("IsPinPowered", this.isPinPowered);
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

        if (tag.contains("Size")) {
            this.size = tag.getInt("Size");
        } else if (tag.getBoolean("IsBig")) {
            this.size = 2;
        } else {
            this.size = 1;
        }

        this.isDummy = tag.getBoolean("IsDummy");
        if (tag.contains("MasterPos")) {
            this.masterPos = NbtUtils.readBlockPos(tag.getCompound("MasterPos"));
        } else {
            this.masterPos = null;
        }

        this.buttonMode = tag.getInt("ButtonMode");
        this.syncWord = tag.getBoolean("SyncWord");
        this.isPressed = tag.getBoolean("IsPressed");
        this.pressTicks = tag.getInt("PressTicks");
        this.pinCode = tag.contains("PinCode") ? tag.getString("PinCode") : "";
        this.isPinPowered = tag.getBoolean("IsPinPowered");

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

        boolean isCustomized = (this.rgbColor != 0xFFFFFF)
                || this.isRainbow
                || (this.wrenchMode != 0)
                || this.isActive
                || !this.detectsMonsters
                || this.detectsAnimals
                || (this.savedMaterial != SignMaterial.DEFAULT)
                || this.hasBackplate
                || (this.buttonMode != 0)
                || this.syncWord
                || !this.pinCode.isEmpty();

        if (!isCustomized) {
            return stack;
        }

        CompoundTag beTag = new CompoundTag();

        if (this.rgbColor != 0xFFFFFF) beTag.putInt("RGBColor", this.rgbColor);
        if (this.isRainbow) beTag.putBoolean("IsRainbow", this.isRainbow);
        if (this.wrenchMode != 0) beTag.putInt("WrenchMode", this.wrenchMode);
        if (this.isActive) {
            beTag.putBoolean("IsActive", this.isActive);
            beTag.putBoolean("Glowing", this.isActive);
        }
        if (!this.detectsMonsters) beTag.putBoolean("DetectsMonsters", this.detectsMonsters);
        if (this.detectsAnimals) beTag.putBoolean("DetectsAnimals", this.detectsAnimals);
        if (this.savedMaterial != SignMaterial.DEFAULT) beTag.putString("SavedMaterial", this.savedMaterial.name());

        if (this.hasBackplate) {
            beTag.putBoolean("HasBackplate", this.hasBackplate);
            if (this.backplateFrontMaterial != SignMaterial.DEFAULT) beTag.putString("BPFrontMat", this.backplateFrontMaterial.name());
            if (this.backplateBackMaterial != SignMaterial.DEFAULT) beTag.putString("BPBackMat", this.backplateBackMaterial.name());
            if (this.backplateFrontColor != 0xFFFFFF) beTag.putInt("BPFrontColor", this.backplateFrontColor);
            if (this.backplateBackColor != 0xFFFFFF) beTag.putInt("BPBackColor", this.backplateBackColor);
            if (this.backplateFrontRainbow) beTag.putBoolean("BPFrontRainbow", this.backplateFrontRainbow);
            if (this.backplateBackRainbow) beTag.putBoolean("BPBackRainbow", this.backplateBackRainbow);
        }

        if (this.buttonMode != 0) beTag.putInt("ButtonMode", this.buttonMode);
        if (this.syncWord) beTag.putBoolean("SyncWord", this.syncWord);
        if (!this.pinCode.isEmpty()) beTag.putString("PinCode", this.pinCode);

        stack.getOrCreateTag().put("BlockEntityTag", beTag);

        if (this.savedMaterial != SignMaterial.DEFAULT) {
            CompoundTag stateTag = new CompoundTag();
            stateTag.putString("material", this.savedMaterial.name().toLowerCase());
            stack.getOrCreateTag().put("BlockStateTag", stateTag);
        }

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

    public static void tick(Level level, BlockPos pos, BlockState state, LetterBlockEntity entity) {
        if (!level.isClientSide()) {
            if (entity.pressTicks > 0) {
                entity.pressTicks--;
                if (entity.pressTicks == 0) {
                    entity.releaseButton();
                }
            }
            if (entity.pinResetTicks > 0) {
                entity.pinResetTicks--;
                if (entity.pinResetTicks == 0) {
                    for (BlockPos p : entity.enteredPositions) {
                        BlockEntity be = level.getBlockEntity(p);
                        if (be instanceof LetterBlockEntity rawLbe) {
                            LetterBlockEntity member = LetterBlock.findMaster(level, p, rawLbe);
                            member.isPinPowered = false;
                            member.applyPressedState(false, 0);
                        }
                    }
                    entity.enteredBuffer = "";
                    entity.enteredPositions.clear();
                }
            }
        }

        if (level.isClientSide) {
            boolean dirty = false;
            float hue = (level.getGameTime() % 120) / 120f;
            int rainbowRgb = Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;

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
            if (entity.getWrenchMode() == 0 || entity.getWrenchMode() == 10) {
                int expectedMode = entity.isActive() ? (entity.getWrenchMode() == 10 ? 1 : 2) : 0;
                if (state.hasProperty(LetterBlock.LIGHT_MODE) && state.getValue(LetterBlock.LIGHT_MODE) != expectedMode) {
                    LetterBlock.updateLightLevel(level, pos, state, entity);
                }
                return;
            }

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
                            AABB bounds = new AABB(pos).inflate(entity.getSize() == 3 ? 10.0 : (entity.getSize() == 2 ? 8.0 : 6.0));
                            shouldGlow = !level.getEntitiesOfClass(LivingEntity.class, bounds,
                                    t -> t instanceof Player ||
                                            (entity.doesDetectMonsters() && t instanceof Monster) ||
                                            (entity.doesDetectAnimals() && t instanceof Animal)).isEmpty();
                        } else shouldGlow = isCurrentlyGlowing; break;
                    case 6: shouldGlow = (time % 20 == 0) ? level.isNight() : isCurrentlyGlowing; break;
                    case 7:
                        if (time % 10 == 0) {
                            entity.isAudioPlaying = false;
                            for (BlockPos p : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 5, 5))) {
                                if (level.getBlockEntity(p) instanceof JukeboxBlockEntity jbe) {
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
                            if (entity.getSize() == 3) bounds = bounds.inflate(2.0);
                            else if (entity.getSize() == 2) bounds = bounds.inflate(1.0);
                            shouldGlow = false;
                            for (Player p : level.players()) {
                                if (p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 256) {
                                    Vec3 eye = p.getEyePosition();
                                    Vec3 look = p.getLookAngle();
                                    Vec3 end = eye.add(look.x * 16, look.y * 16, look.z * 16);
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
                LetterBlock.updateLightLevel(level, pos, state, entity);
            } else {
                int expectedMode = entity.isActive() ? (entity.getWrenchMode() == 10 ? 1 : 2) : 0;
                if (state.hasProperty(LetterBlock.LIGHT_MODE) && state.getValue(LetterBlock.LIGHT_MODE) != expectedMode) {
                    LetterBlock.updateLightLevel(level, pos, state, entity);
                }
            }
        }
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(this.size == 3 ? 12.0 : 8.0);
    }
}