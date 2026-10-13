package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.GridSignBlock;
import com.boran.signbuilder.block.SignMaterial;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class GridSignBlockEntity extends BlockEntity {

    private int gridSize = 3; 

    private final Map<Integer, CellData> cells = new HashMap<>();

    private boolean hasBackplate = false;
    private com.boran.signbuilder.block.SignMaterial backplateFrontMaterial = com.boran.signbuilder.block.SignMaterial.DEFAULT;
    private com.boran.signbuilder.block.SignMaterial backplateBackMaterial = com.boran.signbuilder.block.SignMaterial.DEFAULT;
    private int backplateFrontColor = 0xFFFFFF;
    private int backplateBackColor = 0xFFFFFF;
    private boolean backplateFrontRainbow = false;
    private boolean backplateBackRainbow = false;
    private int facingRotation = -1;

    public transient Object clientRenderCache = null;
    private long renderVersion = 0;
    private boolean suppressDrops = false;
    public long getRenderVersion() { return renderVersion; }
    public void markRenderDirty() { this.renderVersion++; }
    public boolean shouldSuppressDrops() { return suppressDrops; }
    public void setSuppressDrops(boolean suppress) { this.suppressDrops = suppress; }

    public boolean hasBackplate() { return hasBackplate; }
    public void setHasBackplate(boolean hasBackplate) { this.hasBackplate = hasBackplate; markRenderDirty(); setChanged(); sync(); }
    public com.boran.signbuilder.block.SignMaterial getBackplateFrontMaterial() { return backplateFrontMaterial; }
    public void setBackplateFrontMaterial(com.boran.signbuilder.block.SignMaterial mat) { this.backplateFrontMaterial = mat; markRenderDirty(); setChanged(); sync(); }
    public com.boran.signbuilder.block.SignMaterial getBackplateBackMaterial() { return backplateBackMaterial; }
    public void setBackplateBackMaterial(com.boran.signbuilder.block.SignMaterial mat) { this.backplateBackMaterial = mat; markRenderDirty(); setChanged(); sync(); }
    public int getBackplateFrontColor() { return backplateFrontColor; }
    public void setBackplateFrontColor(int color) { this.backplateFrontColor = color; markRenderDirty(); setChanged(); sync(); }
    public int getBackplateBackColor() { return backplateBackColor; }
    public void setBackplateBackColor(int color) { this.backplateBackColor = color; markRenderDirty(); setChanged(); sync(); }
    public boolean isBackplateFrontRainbow() { return backplateFrontRainbow; }
    public void setBackplateFrontRainbow(boolean rainbow) { this.backplateFrontRainbow = rainbow; markRenderDirty(); setChanged(); sync(); }
    public boolean isBackplateBackRainbow() { return backplateBackRainbow; }
    public void setBackplateBackRainbow(boolean rainbow) { this.backplateBackRainbow = rainbow; markRenderDirty(); setChanged(); sync(); }
    public int getFacingRotation() { 
        if (facingRotation >= 0) return facingRotation;
        BlockState state = getBlockState();
        if (state != null && state.hasProperty(com.boran.signbuilder.block.GridSignBlock.FACING)) {
            return com.boran.signbuilder.block.SignRotation.fromDirection(state.getValue(com.boran.signbuilder.block.GridSignBlock.FACING));
        }
        return 0; 
    }
    public void setFacingRotation(int rotation) { this.facingRotation = rotation; markRenderDirty(); setChanged(); sync(); }

    private net.minecraft.world.item.ItemStack backGlyph = net.minecraft.world.item.ItemStack.EMPTY;
    public net.minecraft.world.item.ItemStack getBackGlyph() { return this.backGlyph; }
    public void setBackGlyph(net.minecraft.world.item.ItemStack stack) {
        this.backGlyph = stack == null || stack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : stack.copyWithCount(1);
        markRenderDirty();
        setChanged();
        sync();
    }

    private net.minecraft.world.item.ItemStack frontGlyph = net.minecraft.world.item.ItemStack.EMPTY;
    public net.minecraft.world.item.ItemStack getFrontGlyph() { return this.frontGlyph; }
    public void setFrontGlyph(net.minecraft.world.item.ItemStack stack) {
        this.frontGlyph = stack == null || stack.isEmpty() ? net.minecraft.world.item.ItemStack.EMPTY : stack.copyWithCount(1);
        markRenderDirty();
        setChanged();
        sync();
    }

    private int wrenchMode = 0;
    private boolean isActive = false;
    private boolean detectsMonsters = true;
    private boolean detectsAnimals = false;
    private boolean isAudioPlaying = false;
    private int customLightOnTicks = 10;
    private int customLightOffTicks = 10;
    private int customLightType = 0;
    private int customLightRange = 8;
    private int customLightOffRange = 8;
    private int customLightCloseDelayTicks = 0;
    private int customLightCloseDelayRemaining = 0;
    private boolean customLightCloseDelayPending = false;
    private boolean customLightNightOnly = true;
    private boolean customLightPlayers = true;
    private boolean customLightLowPower = false;
    private boolean customLightLookOnly = true;

    public int getWrenchMode() { return wrenchMode; }
    public void setWrenchMode(int mode) { this.wrenchMode = mode; setChanged(); sync(); }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; setChanged(); sync(); }
    public boolean doesDetectMonsters() { return detectsMonsters; }
    public void setDetectsMonsters(boolean val) { this.detectsMonsters = val; setChanged(); sync(); }
    public boolean doesDetectAnimals() { return detectsAnimals; }
    public void setDetectsAnimals(boolean val) { this.detectsAnimals = val; setChanged(); sync(); }
    public int getCustomLightOnTicks() { return customLightOnTicks; }
    public int getCustomLightOffTicks() { return customLightOffTicks; }
    public int getCustomLightType() { return customLightType; }
    public int getCustomLightRange() { return customLightRange; }
    public int getCustomLightOffRange() { return customLightOffRange; }
    public int getCustomLightCloseDelayTicks() { return customLightCloseDelayTicks; }
    public boolean isCustomLightNightOnly() { return customLightNightOnly; }
    public boolean doesCustomLightDetectPlayers() { return customLightPlayers; }
    public boolean isCustomLightLowPower() { return customLightLowPower; }
    public boolean isCustomLightLookOnly() { return customLightLookOnly; }

    public void setCustomLightingConfig(int onTicks, int offTicks, int type, int range, int offRange, int closeDelay, boolean nightOnly, boolean players, boolean lowPower, boolean lookOnly) {
        this.customLightOnTicks = onTicks;
        this.customLightOffTicks = offTicks;
        this.customLightType = type;
        this.customLightRange = range;
        this.customLightOffRange = offRange;
        this.customLightCloseDelayTicks = closeDelay;
        this.customLightCloseDelayRemaining = 0;
        this.customLightCloseDelayPending = false;
        this.customLightNightOnly = nightOnly;
        this.customLightPlayers = players;
        this.customLightLowPower = lowPower;
        this.customLightLookOnly = lookOnly;
        setChanged();
        sync();
    }

    public void setLightConfiguration(int wrenchMode, boolean isActive, boolean detectsMonsters, boolean detectsAnimals,
                                      int onTicks, int offTicks, int customType, int customRange, int customOffRange,
                                      int closeDelayTicks, boolean customNightOnly, boolean customPlayers,
                                      boolean customLowPower, boolean customLookOnly) {
        this.wrenchMode = wrenchMode;
        this.isActive = isActive;
        this.detectsMonsters = detectsMonsters;
        this.detectsAnimals = detectsAnimals;
        this.customLightOnTicks = onTicks;
        this.customLightOffTicks = offTicks;
        this.customLightType = customType;
        this.customLightRange = customRange;
        this.customLightOffRange = customOffRange;
        this.customLightCloseDelayTicks = closeDelayTicks;
        this.customLightCloseDelayRemaining = 0;
        this.customLightCloseDelayPending = false;
        this.customLightNightOnly = customNightOnly;
        this.customLightPlayers = customPlayers;
        this.customLightLowPower = customLowPower;
        this.customLightLookOnly = customLookOnly;
        setChanged();
        sync();
    }

    public int getEmittedLightMode() {
        if (!isActive) return 0;
        if (wrenchMode == 10 || (wrenchMode == 11 && customLightLowPower)) return 1;
        return 2;
    }

    public GridSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRID_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int size) {
        this.gridSize = size;
        setChanged();
        sync();
    }

    public Map<Integer, CellData> getCells() {
        return cells;
    }

    public static int makeKey(boolean isBack, int gridSize, int col, int row) {
        return (isBack ? 10000 : 0) + gridSize * 100 + row * 10 + col;
    }

    public static boolean cellsOverlap(int g1, int col1, int row1, int g2, int col2, int row2) {
        float u1A = (float) col1 / g1;
        float u2A = (float) (col1 + 1) / g1;
        float v1A = (float) row1 / g1;
        float v2A = (float) (row1 + 1) / g1;
        float u1B = (float) col2 / g2;
        float u2B = (float) (col2 + 1) / g2;
        float v1B = (float) row2 / g2;
        float v2B = (float) (row2 + 1) / g2;
        float eps = 0.001f;
        return (u1A < u2B - eps) && (u2A > u1B + eps)
            && (v1A < v2B - eps) && (v2A > v1B + eps);
    }

    public boolean hasCollision(int candidateGridSize, int candidateCol, int candidateRow, boolean candidateIsBack) {
        for (CellData cell : cells.values()) {
            if (cell.isBack != candidateIsBack) continue;
            if (cell.gridSize == candidateGridSize && cell.col == candidateCol && cell.row == candidateRow) {
                continue;
            }
            if (cell.overlaps(candidateGridSize, candidateCol, candidateRow, candidateIsBack)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyCollision(int candidateGridSize, int candidateCol, int candidateRow, boolean candidateIsBack) {
        for (CellData cell : cells.values()) {
            if (cell.isBack != candidateIsBack) continue;
            if (cell.overlaps(candidateGridSize, candidateCol, candidateRow, candidateIsBack)) {
                return true;
            }
        }
        return false;
    }

    public CellData getCellAt(float u, float v, boolean isBack) {
        for (CellData cell : cells.values()) {
            if (cell.isBack == isBack && u >= cell.getU1() && u <= cell.getU2() && v >= cell.getV1() && v <= cell.getV2()) {
                return cell;
            }
        }
        return null;
    }

    public void setCell(int col, int row, int cellGridSize, boolean isBack, String character, SignMaterial material, int color, boolean rainbow, int lightMode) {
        int key = makeKey(isBack, cellGridSize, col, row);
        cells.put(key, new CellData(character, material, color, rainbow, lightMode, cellGridSize, col, row, isBack));
        markRenderDirty();
        setChanged();
        sync();
    }

    public void setCell(int index, String character, SignMaterial material, int color, boolean rainbow, int lightMode) {
        boolean isBack = com.boran.signbuilder.block.GridSignBlock.isBackIndex(index, this.gridSize);
        int localIndex = com.boran.signbuilder.block.GridSignBlock.getLocalIndex(index, this.gridSize);
        int col = localIndex % this.gridSize;
        int row = localIndex / this.gridSize;
        setCell(col, row, this.gridSize, isBack, character, material, color, rainbow, lightMode);
    }

    public void clearCell(int index) {
        cells.remove(index);
        markRenderDirty();
        setChanged();
        sync();
        if (cells.isEmpty() && level != null) {
            level.removeBlock(worldPosition, false);
        }
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("GridSize", gridSize);
        tag.putBoolean("HasBackplate", hasBackplate);
        tag.putString("BPFMat", backplateFrontMaterial.name());
        tag.putString("BPBMat", backplateBackMaterial.name());
        tag.putInt("BPFColor", backplateFrontColor);
        tag.putInt("BPBColor", backplateBackColor);
        tag.putBoolean("BPFRainbow", backplateFrontRainbow);
        tag.putBoolean("BPBRainbow", backplateBackRainbow);
        tag.putInt("FacingRotation", facingRotation);
        tag.putInt("WrenchMode", wrenchMode);
        tag.putBoolean("IsActive", isActive);
        tag.putBoolean("DetectsMonsters", detectsMonsters);
        tag.putBoolean("DetectsAnimals", detectsAnimals);
        tag.putInt("CustomLightOnTicks", customLightOnTicks);
        tag.putInt("CustomLightOffTicks", customLightOffTicks);
        tag.putInt("CustomLightType", customLightType);
        tag.putInt("CustomLightRange", customLightRange);
        tag.putInt("CustomLightOffRange", customLightOffRange);
        tag.putInt("CustomLightCloseDelayTicks", customLightCloseDelayTicks);
        tag.putBoolean("CustomLightNightOnly", customLightNightOnly);
        tag.putBoolean("CustomLightPlayers", customLightPlayers);
        tag.putBoolean("CustomLightLowPower", customLightLowPower);
        tag.putBoolean("CustomLightLookOnly", customLightLookOnly);
        if (!this.backGlyph.isEmpty()) {
            tag.put("BackGlyph", this.backGlyph.save(new CompoundTag()));
        }
        if (!this.frontGlyph.isEmpty()) {
            tag.put("FrontGlyph", this.frontGlyph.save(new CompoundTag()));
        }

        ListTag list = new ListTag();
        for (Map.Entry<Integer, CellData> entry : cells.entrySet()) {
            CompoundTag cellTag = new CompoundTag();
            cellTag.putInt("Index", entry.getKey());
            cellTag.putString("Char", entry.getValue().character);
            cellTag.putString("Material", entry.getValue().material.name());
            cellTag.putInt("Color", entry.getValue().color);
            cellTag.putBoolean("Rainbow", entry.getValue().rainbow);
            cellTag.putInt("LightMode", entry.getValue().lightMode);
            cellTag.putInt("GridSize", entry.getValue().gridSize);
            cellTag.putInt("Col", entry.getValue().col);
            cellTag.putInt("Row", entry.getValue().row);
            cellTag.putBoolean("IsBack", entry.getValue().isBack);
            list.add(cellTag);
        }
        tag.put("Cells", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.gridSize = tag.getInt("GridSize");
        if (this.gridSize < 2) this.gridSize = 3; // fallback
        
        this.hasBackplate = tag.getBoolean("HasBackplate");
        try { this.backplateFrontMaterial = com.boran.signbuilder.block.SignMaterial.valueOf(tag.getString("BPFMat")); } catch (Exception ignored) {}
        try { this.backplateBackMaterial = com.boran.signbuilder.block.SignMaterial.valueOf(tag.getString("BPBMat")); } catch (Exception ignored) {}
        this.backplateFrontColor = tag.contains("BPFColor") ? tag.getInt("BPFColor") : 0xFFFFFF;
        this.backplateBackColor = tag.contains("BPBColor") ? tag.getInt("BPBColor") : 0xFFFFFF;
        this.backplateFrontRainbow = tag.getBoolean("BPFRainbow");
        this.backplateBackRainbow = tag.getBoolean("BPBRainbow");
        this.facingRotation = tag.getInt("FacingRotation");
        this.wrenchMode = tag.getInt("WrenchMode");
        this.isActive = tag.getBoolean("IsActive");
        this.detectsMonsters = !tag.contains("DetectsMonsters") || tag.getBoolean("DetectsMonsters");
        this.detectsAnimals = tag.getBoolean("DetectsAnimals");
        this.customLightOnTicks = tag.contains("CustomLightOnTicks") ? tag.getInt("CustomLightOnTicks") : 10;
        this.backGlyph = tag.contains("BackGlyph", 10) ? net.minecraft.world.item.ItemStack.of(tag.getCompound("BackGlyph")) : net.minecraft.world.item.ItemStack.EMPTY;
        this.frontGlyph = tag.contains("FrontGlyph", 10) ? net.minecraft.world.item.ItemStack.of(tag.getCompound("FrontGlyph")) : net.minecraft.world.item.ItemStack.EMPTY;
        this.customLightOffTicks = tag.contains("CustomLightOffTicks") ? tag.getInt("CustomLightOffTicks") : 10;
        this.customLightType = tag.getInt("CustomLightType");
        this.customLightRange = tag.contains("CustomLightRange") ? tag.getInt("CustomLightRange") : 8;
        this.customLightOffRange = tag.contains("CustomLightOffRange") ? tag.getInt("CustomLightOffRange") : this.customLightRange;
        this.customLightCloseDelayTicks = tag.getInt("CustomLightCloseDelayTicks");
        this.customLightNightOnly = !tag.contains("CustomLightNightOnly") || tag.getBoolean("CustomLightNightOnly");
        this.customLightPlayers = !tag.contains("CustomLightPlayers") || tag.getBoolean("CustomLightPlayers");
        this.customLightLowPower = tag.getBoolean("CustomLightLowPower");
        this.customLightLookOnly = !tag.contains("CustomLightLookOnly") || tag.getBoolean("CustomLightLookOnly");

        this.cells.clear();
        
        ListTag list = tag.getList("Cells", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cellTag = list.getCompound(i);
            int index = cellTag.getInt("Index");
            String character = cellTag.getString("Char");
            SignMaterial material = SignMaterial.valueOf(cellTag.getString("Material"));
            int color = cellTag.getInt("Color");
            boolean rainbow = cellTag.getBoolean("Rainbow");
            int lightMode = cellTag.getInt("LightMode");

            int cellGridSize;
            int col;
            int row;
            boolean isBack;

            if (cellTag.contains("GridSize")) {
                cellGridSize = cellTag.getInt("GridSize");
                col = cellTag.getInt("Col");
                row = cellTag.getInt("Row");
                isBack = cellTag.getBoolean("IsBack");
            } else {
                cellGridSize = this.gridSize;
                isBack = com.boran.signbuilder.block.GridSignBlock.isBackIndex(index, this.gridSize);
                int localIndex = com.boran.signbuilder.block.GridSignBlock.getLocalIndex(index, this.gridSize);
                col = localIndex % this.gridSize;
                row = localIndex / this.gridSize;
            }

            cells.put(index, new CellData(character, material, color, rainbow, lightMode, cellGridSize, col, row, isBack));
        }
        markRenderDirty();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GridSignBlockEntity entity) {
        if (!level.isClientSide()) {
            if (entity.getWrenchMode() == 11 && entity.customLightType == 1 && entity.customLightCloseDelayPending) {
                if (entity.customLightCloseDelayRemaining > 0) {
                    entity.customLightCloseDelayRemaining--;
                }
            }
        }

        if (level.isClientSide) {
            boolean dirty = false;
            float hue = (level.getGameTime() % 120) / 120f;
            int rainbowRgb = Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;

            if (entity.isBackplateFrontRainbow()) {
                entity.setBackplateFrontColor(rainbowRgb);
                dirty = true;
            }
            if (entity.isBackplateBackRainbow()) {
                entity.setBackplateBackColor(rainbowRgb);
                dirty = true;
            }

            if (dirty && level.getGameTime() % 5 == 0) {
                EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
                    net.minecraft.client.Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
                });
            }
        }

        if (!level.isClientSide()) {
            if (entity.getWrenchMode() == 0 || entity.getWrenchMode() == 10) {
                int expectedMode = entity.getEmittedLightMode();
                if (state.hasProperty(GridSignBlock.LIGHT_MODE) && state.getValue(GridSignBlock.LIGHT_MODE) != expectedMode) {
                    GridSignBlock.updateLightLevel(level, pos, state, entity);
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
                    case 3: shouldGlow = com.boran.signbuilder.block.SignRotation.calculateWaveGlow(pos, state, entity.getFacingRotation(), time); break;
                    case 4: shouldGlow = (time % 60) < 30; break;
                    case 5:
                        if (Math.floorMod(time + pos.asLong(), 10L) == 0) {
                            AABB bounds = new AABB(pos).inflate(8.0);
                            shouldGlow = !level.getEntitiesOfClass(LivingEntity.class, bounds,
                                    t -> t instanceof Player ||
                                            (entity.doesDetectMonsters() && t instanceof Monster) ||
                                            (entity.doesDetectAnimals() && t instanceof Animal)).isEmpty();
                        } else shouldGlow = isCurrentlyGlowing; break;
                    case 6: shouldGlow = (time % 20 == 0) ? level.isNight() : isCurrentlyGlowing; break;
                    case 7:
                        if (Math.floorMod(time + pos.asLong(), 10L) == 0) {
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
                        if (Math.floorMod(time + pos.asLong(), 5L) == 0) {
                            AABB bounds = new AABB(pos);
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
                    case 11:
                        if (entity.customLightType == 1) {
                            if (Math.floorMod(time + pos.asLong(), 10L) == 0) {
                                boolean hasSelectedTargets = entity.customLightPlayers || entity.doesDetectMonsters() || entity.doesDetectAnimals();
                                if (!hasSelectedTargets) {
                                    entity.customLightCloseDelayPending = false;
                                    entity.customLightCloseDelayRemaining = 0;
                                    shouldGlow = false;
                                } else {
                                    int detectionRange = (entity.customLightCloseDelayPending && entity.customLightCloseDelayRemaining > 0)
                                            ? Math.max(entity.customLightRange, entity.customLightOffRange)
                                            : entity.customLightRange;
                                    AABB bounds = new AABB(pos).inflate(detectionRange);
                                    boolean entityInRange = !level.getEntitiesOfClass(LivingEntity.class, bounds,
                                            t -> (entity.customLightPlayers && t instanceof Player) ||
                                                    (entity.doesDetectMonsters() && t instanceof Monster) ||
                                                    (entity.doesDetectAnimals() && t instanceof Animal)).isEmpty();

                                    if (entity.customLightNightOnly && !level.isNight()) {
                                        entityInRange = false;
                                    }

                                    if (entityInRange) {
                                        entity.customLightCloseDelayPending = false;
                                        entity.customLightCloseDelayRemaining = 0;
                                        shouldGlow = true;
                                    } else if (isCurrentlyGlowing) {
                                        if (entity.customLightCloseDelayTicks > 0) {
                                            if (!entity.customLightCloseDelayPending) {
                                                entity.customLightCloseDelayPending = true;
                                                entity.customLightCloseDelayRemaining = entity.customLightCloseDelayTicks;
                                                shouldGlow = true;
                                            } else {
                                                shouldGlow = entity.customLightCloseDelayRemaining > 0;
                                                if (!shouldGlow) {
                                                    entity.customLightCloseDelayPending = false;
                                                }
                                            }
                                        } else {
                                            shouldGlow = false;
                                        }
                                    } else {
                                        shouldGlow = false;
                                    }
                                }
                            } else {
                                shouldGlow = isCurrentlyGlowing;
                            }
                        } else if (entity.customLightType == 3) {
                            if (Math.floorMod(time + pos.asLong(), 10L) == 0) {
                                AABB lookBounds = new AABB(pos);
                                boolean lookedAt = false;
                                for (Player player : level.players()) {
                                    if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 256.0) {
                                        Vec3 eye = player.getEyePosition();
                                        Vec3 look = player.getLookAngle();
                                        Vec3 end = eye.add(look.x * 16.0, look.y * 16.0, look.z * 16.0);
                                        if (lookBounds.clip(eye, end).isPresent()) {
                                            lookedAt = true;
                                            break;
                                        }
                                    }
                                }
                                shouldGlow = entity.customLightLookOnly == lookedAt;
                            } else {
                                shouldGlow = isCurrentlyGlowing;
                            }
                        } else {
                            int cycleLength = entity.customLightOnTicks + entity.customLightOffTicks;
                            shouldGlow = Math.floorMod(time, cycleLength) < entity.customLightOnTicks;
                        }
                        break;
                }
            }
            if (shouldGlow != isCurrentlyGlowing) {
                entity.setActive(shouldGlow);
                GridSignBlock.updateLightLevel(level, pos, state, entity);
            } else {
                int expectedMode = entity.getEmittedLightMode();
                if (state.hasProperty(GridSignBlock.LIGHT_MODE) && state.getValue(GridSignBlock.LIGHT_MODE) != expectedMode) {
                    GridSignBlock.updateLightLevel(level, pos, state, entity);
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static class CellData {
        public String character;
        public SignMaterial material;
        public int color;
        public boolean rainbow;
        public int lightMode;
        public int gridSize;
        public int col;
        public int row;
        public boolean isBack;

        public CellData(String character, SignMaterial material, int color, boolean rainbow, int lightMode,
                        int gridSize, int col, int row, boolean isBack) {
            this.character = character;
            this.material = material;
            this.color = color;
            this.rainbow = rainbow;
            this.lightMode = lightMode;
            this.gridSize = gridSize;
            this.col = col;
            this.row = row;
            this.isBack = isBack;
        }

        public CellData(String character, SignMaterial material, int color, boolean rainbow, int lightMode) {
            this(character, material, color, rainbow, lightMode, 3, 0, 0, false);
        }

        public float getU1() { return (float) col / gridSize; }
        public float getU2() { return (float) (col + 1) / gridSize; }
        public float getV1() { return (float) row / gridSize; }
        public float getV2() { return (float) (row + 1) / gridSize; }

        public boolean overlaps(int otherGridSize, int otherCol, int otherRow, boolean otherIsBack) {
            if (this.isBack != otherIsBack) return false;
            float oU1 = (float) otherCol / otherGridSize;
            float oU2 = (float) (otherCol + 1) / otherGridSize;
            float oV1 = (float) otherRow / otherGridSize;
            float oV2 = (float) (otherRow + 1) / otherGridSize;
            float eps = 0.001f;
            return (getU1() < oU2 - eps) && (getU2() > oU1 + eps)
                && (getV1() < oV2 - eps) && (getV2() > oV1 + eps);
        }
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(this.worldPosition).inflate(1000000.0);
    }
}
