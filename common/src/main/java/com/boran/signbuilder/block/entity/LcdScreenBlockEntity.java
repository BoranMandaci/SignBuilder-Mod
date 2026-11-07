package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.LcdScreenBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LcdScreenBlockEntity extends BlockEntity {

    private String text = "";
    private int mode = 0;
    private int mode2 = 0;
    private int align = 1;
    private int speed = 2;
    private int fontSize = 1;
    private int color = 0x00FF66;
    private boolean rainbow = false;
    private boolean glow = true;
    private int theme = 0;
    private int widgetTL = 0;
    private int widgetTR = 0;
    private int widgetBL = 0;
    private int widgetBR = 0;
    private int fontStyle = 0;
    private int visualAnim = 0;
    private boolean loopEnabled = false;
    private boolean loopShuffle = false;
    private int loopTransition = 1;
    private int[] loopAnims = new int[0];
    private int[] loopDurations = new int[0];
    private int[] loopColors = new int[0];
    private String[] loopTexts1 = new String[0];
    private String[] loopTexts2 = new String[0];
    private int[] loopModes1 = new int[0];
    private int[] loopModes2 = new int[0];
    private int[] loopAligns = new int[0];

    public static final int MAX_LOOP_STEPS = 64;
    public static final int MAX_LINKED_STORAGES = 32;
    public static final double MAX_LINK_DISTANCE = 48.0;
    public static final int MAX_SYNCED_ITEMS = 256;
    public static final int MAX_SYNCED_SLOTS = 216;
    public static final int MAX_WATCHLIST_ITEMS = 24;
    private static final int FLOW_RING_SIZE = 30;
    private static final ThreadLocal<int[]> LOOP_PERM_SCRATCH = ThreadLocal.withInitial(() -> new int[MAX_LOOP_STEPS * 2]);

    public record LocatedContainer(BlockPos minPos, BlockPos maxPos, int itemCount) {}
    public record LoopPlaybackState(int stepIndex, int animId, int stepColor, long elapsedMs, long stepDurationMs,
                                    float transitionIntensity, boolean transitionOutgoing, int effectiveTransition) {}

    private final List<BlockPos> linkedStoragePositions = new ArrayList<>();
    private final List<ItemStack> watchlistItems = new ArrayList<>();
    private int storageMonitorMode = 0;
    private int storageStyle = 0;
    private int storageFilterMode = 0;
    private int storageFlowMode = 0;

    private int storagePercent = 0;
    private int storageTotalItems = 0;
    private int storageMaxCapacity = 0;
    private int storageFilledSlots = 0;
    private int storageTotalSlots = 0;
    private int storageUniqueTypes = 0;
    private int storageNetRatePerHour = 0;
    private byte[] storageSlotFills = new byte[0];
    private int[] storageSlotPalette = new int[0];
    private int[] storageSlotCounts = new int[0];
    private ItemStack[] storageItemStacks = new ItemStack[0];
    private String[] storageItemNames = new String[0];
    private int[] storageItemCounts = new int[0];
    private int[] storageItemShares = new int[0];
    private int[] storageItemRatesPerHour = new int[0];
    private byte[] storageItemPinned = new byte[0];

    private transient final int[] flowTotalHistory = new int[FLOW_RING_SIZE];
    private transient final long[] flowTickHistory = new long[FLOW_RING_SIZE];
    private transient final Map<String, int[]> flowItemHistory = new HashMap<>();
    private transient int flowHead = 0;
    private transient int flowSampleCount = 0;
    private transient long lastFlowSampleTick = -100L;

    public long lastRenderNanoTime = 0L;
    public long lastRenderGameTime = -1L;
    public float lastRenderPartialTick = -1.0f;
    public BlockPos activeRenderPos = null;

    public BlockPos cachedMasterPos = null;
    public int cachedWidthBlocks = 1;
    public int cachedHeightBlocks = 1;
    public long lastCacheUpdateTick = -100L;

    public String cachedResolvedLine1 = "";
    public String cachedResolvedLine2 = "";
    public long lastResolvedTick = -100L;
    public String lastResolvedRawText = null;

    public String cachedWidgetTLText = "";
    public String cachedWidgetTRText = "";
    public String cachedWidgetBLText = "";
    public String cachedWidgetBRText = "";
    public long lastWidgetTick = -100L;

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
        this.lastResolvedRawText = null;
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
        this.widgetTL = Math.max(0, Math.min(11, widgetTL));
        setChanged();
        sync();
    }

    public int getWidgetTR() {
        return widgetTR;
    }

    public void setWidgetTR(int widgetTR) {
        this.widgetTR = Math.max(0, Math.min(11, widgetTR));
        setChanged();
        sync();
    }

    public int getWidgetBL() {
        return widgetBL;
    }

    public void setWidgetBL(int widgetBL) {
        this.widgetBL = Math.max(0, Math.min(11, widgetBL));
        setChanged();
        sync();
    }

    public int getWidgetBR() {
        return widgetBR;
    }

    public void setWidgetBR(int widgetBR) {
        this.widgetBR = Math.max(0, Math.min(11, widgetBR));
        setChanged();
        sync();
    }

    public boolean isShowTime() {
        return widgetTL == 1 || widgetTR == 1 || widgetBL == 1 || widgetBR == 1;
    }

    public boolean isShowWeather() {
        return widgetTL == 2 || widgetTR == 2 || widgetBL == 2 || widgetBR == 2;
    }

    public boolean isShowPlayer() {
        return widgetTL == 3 || widgetTR == 3 || widgetBL == 3 || widgetBR == 3;
    }

    public boolean isShowDay() {
        return widgetTL == 4 || widgetTR == 4 || widgetBL == 4 || widgetBR == 4;
    }

    public boolean hasWidgets() {
        return widgetTL > 0 || widgetTR > 0 || widgetBL > 0 || widgetBR > 0;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(int fontStyle) {
        this.fontStyle = Math.max(0, Math.min(3, fontStyle));
        setChanged();
        sync();
    }

    public int getVisualAnim() {
        return visualAnim;
    }

    public void setVisualAnim(int visualAnim) {
        this.visualAnim = Math.max(0, Math.min(60, visualAnim));
        setChanged();
        sync();
    }

    public boolean isLoopEnabled() {
        return loopEnabled;
    }

    public boolean isActiveVisualLoop() {
        return storageMonitorMode == 0 && loopEnabled && loopAnims != null && loopAnims.length > 0;
    }

    public boolean isLoopShuffle() {
        return loopShuffle;
    }

    public int getLoopTransition() {
        return loopTransition;
    }

    public int getLoopStepCount() {
        return loopAnims != null ? loopAnims.length : 0;
    }

    public int[] getLoopAnims() {
        return loopAnims != null ? loopAnims.clone() : new int[0];
    }

    public int[] getLoopDurations() {
        return loopDurations != null ? loopDurations.clone() : new int[0];
    }

    public int[] getLoopColors() {
        return loopColors != null ? loopColors.clone() : new int[0];
    }

    public String[] getLoopTexts1() {
        return loopTexts1 != null ? loopTexts1.clone() : new String[0];
    }

    public String[] getLoopTexts2() {
        return loopTexts2 != null ? loopTexts2.clone() : new String[0];
    }

    public int[] getLoopModes1() {
        return loopModes1 != null ? loopModes1.clone() : new int[0];
    }

    public int[] getLoopModes2() {
        return loopModes2 != null ? loopModes2.clone() : new int[0];
    }

    public int[] getLoopAligns() {
        return loopAligns != null ? loopAligns.clone() : new int[0];
    }

    public void setLoopConfig(boolean enabled, boolean shuffle, int transition,
                              int[] anims, int[] durations, int[] colors,
                              String[] texts1, String[] texts2, int[] modes1, int[] modes2, int[] aligns) {
        this.loopEnabled = enabled;
        this.loopShuffle = shuffle;
        this.loopTransition = Math.max(0, Math.min(9, transition));
        int count = anims != null ? Math.min(MAX_LOOP_STEPS, anims.length) : 0;
        this.loopAnims = new int[count];
        this.loopDurations = new int[count];
        this.loopColors = new int[count];
        this.loopTexts1 = new String[count];
        this.loopTexts2 = new String[count];
        this.loopModes1 = new int[count];
        this.loopModes2 = new int[count];
        this.loopAligns = new int[count];
        for (int i = 0; i < count; i++) {
            this.loopAnims[i] = Math.max(0, Math.min(60, anims[i]));
            int dur = (durations != null && i < durations.length) ? durations[i] : 6;
            this.loopDurations[i] = Math.max(1, Math.min(999, dur));
            int col = (colors != null && i < colors.length) ? colors[i] : -1;
            this.loopColors[i] = (col < 0) ? Math.max(-2, col) : (col & 0xFFFFFF);
            this.loopTexts1[i] = (texts1 != null && i < texts1.length && texts1[i] != null) ? texts1[i] : "";
            this.loopTexts2[i] = (texts2 != null && i < texts2.length && texts2[i] != null) ? texts2[i] : "";
            this.loopModes1[i] = (modes1 != null && i < modes1.length && modes1[i] >= 0 && modes1[i] <= 9) ? modes1[i] : -1;
            this.loopModes2[i] = (modes2 != null && i < modes2.length && modes2[i] >= 0 && modes2[i] <= 9) ? modes2[i] : -1;
            this.loopAligns[i] = (aligns != null && i < aligns.length && aligns[i] >= 0 && aligns[i] <= 2) ? aligns[i] : -1;
        }
        setChanged();
        sync();
    }

    public void setLoopConfig(boolean enabled, boolean shuffle, int transition, int[] anims, int[] durations, int[] colors) {
        setLoopConfig(enabled, shuffle, transition, anims, durations, colors, null, null, null, null, null);
    }

    private int lastLoopStepIdx = -1;
    private String lastLoopRaw1 = null;
    private String lastLoopRaw2 = null;
    private String cachedLoopLine1 = "";
    private String cachedLoopLine2 = "";
    private long lastLoopTick = -100L;

    public String getLoopResolvedLine(int stepIdx, int line, @Nullable Level level) {
        if (stepIdx >= 0 && stepIdx < (loopAnims != null ? loopAnims.length : 0) && loopAnims[stepIdx] == 0) {
            String raw1 = (loopTexts1 != null && stepIdx < loopTexts1.length && loopTexts1[stepIdx] != null) ? loopTexts1[stepIdx] : "";
            String raw2 = (loopTexts2 != null && stepIdx < loopTexts2.length && loopTexts2[stepIdx] != null) ? loopTexts2[stepIdx] : "";
            if (!raw1.isEmpty() || !raw2.isEmpty()) {
                long currentTick = level != null ? level.getGameTime() : 0L;
                boolean hasVars = raw1.indexOf('{') >= 0 || raw2.indexOf('{') >= 0;
                if (stepIdx != this.lastLoopStepIdx || !raw1.equals(this.lastLoopRaw1) || !raw2.equals(this.lastLoopRaw2) || (hasVars && currentTick - this.lastLoopTick >= 4L)) {
                    this.lastLoopStepIdx = stepIdx;
                    this.lastLoopRaw1 = raw1;
                    this.lastLoopRaw2 = raw2;
                    this.lastLoopTick = currentTick;
                    this.cachedLoopLine1 = !raw1.isEmpty() ? LcdTextFormatter.resolveAll(raw1, level, this.worldPosition) : "";
                    this.cachedLoopLine2 = !raw2.isEmpty() ? LcdTextFormatter.resolveAll(raw2, level, this.worldPosition) : "";
                }
                if (line == 0 && !raw1.isEmpty()) {
                    return this.cachedLoopLine1;
                }
                if (line == 1 && !raw2.isEmpty()) {
                    return this.cachedLoopLine2;
                }
            }
        }
        return getResolvedLine(line, level);
    }

    public int getLoopEffectiveMode(int stepIdx, int line) {
        if (stepIdx >= 0 && stepIdx < (loopAnims != null ? loopAnims.length : 0) && loopAnims[stepIdx] == 0) {
            if (line == 0 && loopModes1 != null && stepIdx < loopModes1.length && loopModes1[stepIdx] >= 0) {
                return loopModes1[stepIdx];
            }
            if (line == 1 && loopModes2 != null && stepIdx < loopModes2.length && loopModes2[stepIdx] >= 0) {
                return loopModes2[stepIdx];
            }
        }
        return line == 0 ? this.mode : this.mode2;
    }

    public int getLoopEffectiveAlign(int stepIdx) {
        if (stepIdx >= 0 && stepIdx < (loopAnims != null ? loopAnims.length : 0) && loopAnims[stepIdx] == 0) {
            if (loopAligns != null && stepIdx < loopAligns.length && loopAligns[stepIdx] >= 0) {
                return loopAligns[stepIdx];
            }
        }
        return this.align;
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static void fillRawPermutation(int[] out, int offset, int n, long epoch, long posSeed) {
        for (int i = 0; i < n; i++) {
            out[offset + i] = i;
        }
        long seed = mix64(epoch * 0x9E3779B97F4A7C15L ^ mix64(posSeed ^ 0xD1B54A32D192ED03L));
        for (int i = n - 1; i > 0; i--) {
            seed = mix64(seed + 0x9E3779B97F4A7C15L);
            int j = (int) Math.floorMod(seed >>> 1, i + 1);
            int tmp = out[offset + i];
            out[offset + i] = out[offset + j];
            out[offset + j] = tmp;
        }
    }

    public static LoopPlaybackState resolveLoopPlayback(int[] anims, int[] durations, int[] colors,
                                                        boolean shuffle, int transition, long posSeed, long timeMs) {
        int n = anims != null ? Math.min(MAX_LOOP_STEPS, anims.length) : 0;
        if (n <= 0) {
            return new LoopPlaybackState(0, 1, -1, 0L, 6000L, 0.0f, false, 0);
        }
        if (n == 1) {
            int anim = Math.max(0, Math.min(60, anims[0]));
            int col = (colors != null && colors.length > 0) ? colors[0] : -1;
            long stepMs = ((durations != null && durations.length > 0) ? Math.max(1, Math.min(999, durations[0])) : 6) * 1000L;
            long el = Math.floorMod(timeMs, stepMs);
            return new LoopPlaybackState(0, anim, col, el, stepMs, 0.0f, false, 0);
        }

        long totalMs = 0L;
        for (int i = 0; i < n; i++) {
            int d = (durations != null && i < durations.length) ? Math.max(1, Math.min(999, durations[i])) : 6;
            totalMs += d * 1000L;
        }
        if (totalMs <= 0L) totalMs = n * 6000L;

        long epoch = Math.floorDiv(timeMs, totalMs);
        long posMs = Math.floorMod(timeMs, totalMs);

        int[] scratch = LOOP_PERM_SCRATCH.get();
        if (shuffle) {
            if (n == 2) {
                int first = (int) (posSeed & 1L);
                scratch[0] = first;
                scratch[1] = 1 - first;
            } else {
                fillRawPermutation(scratch, 0, n, epoch, posSeed);
                fillRawPermutation(scratch, MAX_LOOP_STEPS, n, epoch - 1L, posSeed);
                int prevLast = scratch[MAX_LOOP_STEPS + n - 1];
                if (scratch[0] == prevLast) {
                    int tmp = scratch[0];
                    scratch[0] = scratch[1];
                    scratch[1] = tmp;
                }
            }
        }

        for (int slot = 0; slot < n; slot++) {
            int idx = shuffle ? scratch[slot] : slot;
            int d = (durations != null && idx < durations.length) ? Math.max(1, Math.min(999, durations[idx])) : 6;
            long stepMs = d * 1000L;
            if (posMs < stepMs || slot == n - 1) {
                long elapsedMs = Math.max(0L, Math.min(stepMs - 1L, posMs));
                long remainingMs = stepMs - elapsedMs;
                float intensity = 0.0f;
                boolean outgoing = false;
                int effTrans = 0;
                if (transition > 0) {
                    long transHalfMs = Math.min(260L, stepMs / 5L);
                    if (transHalfMs > 0L) {
                        if (elapsedMs < transHalfMs) {
                            intensity = 1.0f - ((float) elapsedMs / (float) transHalfMs);
                            outgoing = false;
                        } else if (remainingMs < transHalfMs) {
                            intensity = 1.0f - ((float) remainingMs / (float) transHalfMs);
                            outgoing = true;
                        }
                    }
                    if (intensity > 0.0f) {
                        if (transition == 9) {
                            long boundaryId = epoch * (long) n + slot + (outgoing ? 1L : 0L);
                            long h = mix64(boundaryId * 0x9E3779B97F4A7C15L ^ mix64(posSeed ^ 0x517CC1B727220A95L));
                            effTrans = 1 + (int) Math.floorMod(h >>> 1, 8);
                        } else {
                            effTrans = Math.min(8, transition);
                        }
                    }
                }
                int anim = Math.max(0, Math.min(60, anims[idx]));
                int col = (colors != null && idx < colors.length) ? colors[idx] : -1;
                return new LoopPlaybackState(idx, anim, col, elapsedMs, stepMs, intensity, outgoing, effTrans);
            }
            posMs -= stepMs;
        }
        return new LoopPlaybackState(0, Math.max(0, Math.min(60, anims[0])), -1, 0L, 6000L, 0.0f, false, 0);
    }

    public boolean hasLinkedStorage() {
        return !linkedStoragePositions.isEmpty();
    }

    public int getLinkedStorageCount() {
        return linkedStoragePositions.size();
    }

    public List<BlockPos> getLinkedStoragePositions() {
        return linkedStoragePositions;
    }

    @Nullable
    public BlockPos getLinkedStoragePos() {
        return linkedStoragePositions.isEmpty() ? null : linkedStoragePositions.get(0);
    }

    public void resetFlowHistory() {
        Arrays.fill(this.flowTotalHistory, 0);
        Arrays.fill(this.flowTickHistory, 0L);
        this.flowItemHistory.clear();
        this.flowHead = 0;
        this.flowSampleCount = 0;
        this.lastFlowSampleTick = -100L;
        this.storageNetRatePerHour = 0;
        this.storageItemRatesPerHour = new int[this.storageItemCounts != null ? this.storageItemCounts.length : 0];
    }

    public static String makeItemKey(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return "";
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            return id + "|" + stack.getHoverName().getString();
        }
        return id;
    }

    public static boolean matchesTargetItem(ItemStack candidate, ItemStack target) {
        if (candidate == null || candidate.isEmpty() || target == null || target.isEmpty()) return false;
        if (!ItemStack.isSameItem(candidate, target)) return false;
        if (target.has(DataComponents.CUSTOM_NAME) || candidate.has(DataComponents.CUSTOM_NAME)) {
            return candidate.getHoverName().getString().equals(target.getHoverName().getString());
        }
        return true;
    }

    public static ItemStack cleanWatchlistCopy(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = new ItemStack(stack.getItem(), 1);
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            copy.set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME));
        }
        return copy;
    }

    public void setLinkedStoragePos(@Nullable BlockPos pos) {
        this.linkedStoragePositions.clear();
        if (pos != null) {
            this.linkedStoragePositions.add(pos.immutable());
        }
        resetFlowHistory();
        this.lastResolvedRawText = null;
        setChanged();
        sync();
    }

    public void setLinkedStoragePositions(List<BlockPos> positions) {
        this.linkedStoragePositions.clear();
        if (positions != null) {
            for (BlockPos p : positions) {
                if (p != null && !this.linkedStoragePositions.contains(p)) {
                    this.linkedStoragePositions.add(p.immutable());
                    if (this.linkedStoragePositions.size() >= MAX_LINKED_STORAGES) {
                        break;
                    }
                }
            }
        }
        resetFlowHistory();
        this.lastResolvedRawText = null;
        setChanged();
        sync();
    }

    public int getStorageMonitorMode() {
        return storageMonitorMode;
    }

    public void setStorageMonitorMode(int mode) {
        this.storageMonitorMode = Math.max(0, Math.min(1, mode));
        setChanged();
        sync();
    }

    public int getStorageStyle() {
        return storageStyle;
    }

    public void setStorageStyle(int style) {
        this.storageStyle = Math.max(0, Math.min(5, style));
        setChanged();
        sync();
    }

    public int getStorageFilterMode() {
        return storageFilterMode;
    }

    public void setStorageFilterMode(int filterMode) {
        this.storageFilterMode = Math.max(0, Math.min(2, filterMode));
        setChanged();
        sync();
    }

    public int getStorageFlowMode() {
        return storageFlowMode;
    }

    public void setStorageFlowMode(int flowMode) {
        this.storageFlowMode = Math.max(0, Math.min(2, flowMode));
        setChanged();
        sync();
    }

    public List<ItemStack> getWatchlistItems() {
        List<ItemStack> out = new ArrayList<>(this.watchlistItems.size());
        for (ItemStack st : this.watchlistItems) {
            if (st != null && !st.isEmpty()) {
                out.add(st.copy());
            }
        }
        return out;
    }

    public void setWatchlistItems(List<ItemStack> items) {
        this.watchlistItems.clear();
        if (items != null) {
            for (ItemStack st : items) {
                if (st == null || st.isEmpty()) continue;
                ItemStack cleaned = cleanWatchlistCopy(st);
                boolean dup = false;
                for (ItemStack existing : this.watchlistItems) {
                    if (matchesTargetItem(existing, cleaned)) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    this.watchlistItems.add(cleaned);
                    if (this.watchlistItems.size() >= MAX_WATCHLIST_ITEMS) {
                        break;
                    }
                }
            }
        }
        setChanged();
        sync();
    }

    public int getStorageNetRatePerHour() {
        return storageNetRatePerHour;
    }

    public int getStorageTopRatePerHour(int index) {
        if (storageItemRatesPerHour == null || index < 0 || index >= storageItemRatesPerHour.length) return 0;
        return storageItemRatesPerHour[index];
    }

    public boolean isStorageTopPinned(int index) {
        if (storageItemPinned == null || index < 0 || index >= storageItemPinned.length) return false;
        return storageItemPinned[index] != 0;
    }

    public static String formatFlowRateText(int ratePerHour, int flowMode, boolean includeArrow, boolean showZero) {
        int effectiveMode = (flowMode <= 0) ? 1 : flowMode;
        int val = (effectiveMode == 1) ? Math.round(ratePerHour / 60.0f) : ratePerHour;
        if (val == 0 && !showZero) {
            return "";
        }
        String unit = Component.translatable(effectiveMode == 2
                ? "gui.signbuilder.lcd.storage.per_hour"
                : "gui.signbuilder.lcd.storage.per_min").getString();
        if (val > 0) {
            return (includeArrow ? "▲+" : "+") + formatCompactCount(val) + unit;
        }
        if (val < 0) {
            return (includeArrow ? "▼-" : "-") + formatCompactCount(Math.abs(val)) + unit;
        }
        return "0" + unit;
    }

    public int getStorageTotalItems() {
        return storageTotalItems;
    }

    public int getStorageMaxCapacity() {
        return storageMaxCapacity;
    }

    public int getStorageFilledSlots() {
        return storageFilledSlots;
    }

    public int getStorageTotalSlots() {
        return storageTotalSlots;
    }

    public int getStorageUniqueTypes() {
        return storageUniqueTypes;
    }

    public byte[] getStorageSlotFills() {
        return storageSlotFills != null ? storageSlotFills : new byte[0];
    }

    public int getStorageSlotFill(int slot) {
        if (storageSlotFills == null || slot < 0 || slot >= storageSlotFills.length) return 0;
        return storageSlotFills[slot] & 0xFF;
    }

    public int getStorageSlotCountVal(int slot) {
        if (storageSlotCounts == null || slot < 0 || slot >= storageSlotCounts.length) return 0;
        return storageSlotCounts[slot];
    }

    public ItemStack getStorageSlotStack(int slot) {
        if (storageSlotPalette == null || slot < 0 || slot >= storageSlotPalette.length) return ItemStack.EMPTY;
        int palIdx = storageSlotPalette[slot];
        if (palIdx < 0 || storageItemStacks == null || palIdx >= storageItemStacks.length) return ItemStack.EMPTY;
        ItemStack st = storageItemStacks[palIdx];
        return st != null ? st : ItemStack.EMPTY;
    }

    public int getStorageEntryCount() {
        return storageItemCounts != null ? storageItemCounts.length : 0;
    }

    public ItemStack getStorageTopStack(int index) {
        if (storageItemStacks == null || index < 0 || index >= storageItemStacks.length) return ItemStack.EMPTY;
        ItemStack st = storageItemStacks[index];
        return st != null ? st : ItemStack.EMPTY;
    }

    public int getStorageTopCount(int index) {
        if (storageItemCounts == null || index < 0 || index >= storageItemCounts.length) return 0;
        return storageItemCounts[index];
    }

    public int getStorageTopShare(int index) {
        if (storageItemShares == null || index < 0 || index >= storageItemShares.length) return 0;
        return storageItemShares[index];
    }

    public String getStorageTopDisplayName(int index) {
        if (index < 0 || index >= getStorageEntryCount()) return "";
        ItemStack st = getStorageTopStack(index);
        if (!st.isEmpty()) {
            return st.getHoverName().getString();
        }
        if (storageItemNames != null && index < storageItemNames.length && storageItemNames[index] != null) {
            return storageItemNames[index];
        }
        return "";
    }

    public String getStorageTopItem1Name() {
        return getStorageTopDisplayName(0);
    }

    public int getStorageTopItem1Count() {
        return getStorageTopCount(0);
    }

    public String getStorageTopItem2Name() {
        return getStorageTopDisplayName(1);
    }

    public int getStorageTopItem2Count() {
        return getStorageTopCount(1);
    }

    public String getStorageTopItem3Name() {
        return getStorageTopDisplayName(2);
    }

    public int getStorageTopItem3Count() {
        return getStorageTopCount(2);
    }

    public int getStoragePercent() {
        return Math.min(100, Math.max(0, storagePercent));
    }

    public String getStoragePercentString() {
        return getStoragePercent() + "%";
    }

    public String getStorageItemsString() {
        return formatCompactCount(storageTotalItems) + " (" + storageFilledSlots + "/" + storageTotalSlots + ")";
    }

    public static String formatCompactCount(int count) {
        if (count >= 1_000_000) {
            return String.format(java.util.Locale.ROOT, "%.1fM", count / 1_000_000.0);
        }
        if (count >= 10_000) {
            return String.format(java.util.Locale.ROOT, "%.1fK", count / 1_000.0);
        }
        return String.valueOf(count);
    }

    public String getStorageBarString(int barLength) {
        int percent = getStoragePercent();
        int filled = Math.min(barLength, Math.max(0, (percent * barLength + 50) / 100));
        if (filled == 0 && storageTotalItems > 0 && barLength > 0) {
            filled = 1;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        for (int i = filled; i < barLength; i++) {
            sb.append("░");
        }
        sb.append("]");
        return sb.toString();
    }

    public String getStorageTopItemString() {
        String n = getStorageTopDisplayName(0);
        int c = getStorageTopCount(0);
        if (!n.isEmpty() && c > 0) {
            return n + " x" + c;
        }
        return "";
    }

    private static final class AggregatedItem {
        final ItemStack repStack;
        final String name;
        final String key;
        int totalCount;
        double slotUnits;
        int pinnedOrder = -1;
        int ratePerHour = 0;
        int sortedIndex = -1;

        AggregatedItem(ItemStack repStack, String name) {
            this.repStack = repStack;
            this.name = name;
            this.key = makeItemKey(repStack);
        }
    }

    public List<LocatedContainer> locateItemInContainers(Level level, ItemStack target) {
        List<LocatedContainer> found = new ArrayList<>();
        if (level == null || level.isClientSide() || target == null || target.isEmpty() || this.linkedStoragePositions.isEmpty()) {
            return found;
        }

        double maxDistSq = MAX_LINK_DISTANCE * MAX_LINK_DISTANCE;
        Set<BlockPos> visitedHalves = new HashSet<>();

        for (BlockPos sPos : this.linkedStoragePositions) {
            if (sPos == null) continue;
            if (this.worldPosition.distSqr(sPos) > maxDistSq) continue;
            if (!level.isLoaded(sPos)) continue;
            if (!visitedHalves.add(sPos)) continue;

            BlockState targetState = level.getBlockState(sPos);
            Container container = null;
            BlockPos minPos = sPos.immutable();
            BlockPos maxPos = sPos.immutable();

            if (targetState.getBlock() instanceof ChestBlock chestBlock) {
                if (targetState.hasProperty(ChestBlock.TYPE) && targetState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    BlockPos otherHalf = sPos.relative(ChestBlock.getConnectedDirection(targetState));
                    visitedHalves.add(otherHalf);
                    minPos = new BlockPos(
                            Math.min(sPos.getX(), otherHalf.getX()),
                            Math.min(sPos.getY(), otherHalf.getY()),
                            Math.min(sPos.getZ(), otherHalf.getZ())
                    );
                    maxPos = new BlockPos(
                            Math.max(sPos.getX(), otherHalf.getX()),
                            Math.max(sPos.getY(), otherHalf.getY()),
                            Math.max(sPos.getZ(), otherHalf.getZ())
                    );
                }
                container = ChestBlock.getContainer(chestBlock, targetState, level, sPos, true);
            } else {
                BlockEntity targetBe = level.getBlockEntity(sPos);
                if (targetBe instanceof Container c) {
                    container = c;
                }
            }

            if (container != null) {
                int matchedCount = 0;
                int cSize = container.getContainerSize();
                for (int i = 0; i < cSize; i++) {
                    ItemStack slotStack = container.getItem(i);
                    if (!slotStack.isEmpty() && matchesTargetItem(slotStack, target)) {
                        matchedCount += slotStack.getCount();
                    }
                }
                if (matchedCount > 0) {
                    found.add(new LocatedContainer(minPos, maxPos, matchedCount));
                }
            }
        }

        found.sort((a, b) -> Integer.compare(b.itemCount(), a.itemCount()));
        return found;
    }

    private static int computeRatePerHour(int delta, long tickSpan) {
        if (delta == 0) return 0;
        double denomTicks = Math.max(200.0, (double) tickSpan);
        return (int) Math.round(delta * (72000.0 / denomTicks));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LcdScreenBlockEntity entity) {
        if (level.isClientSide()) return;
        if (entity.linkedStoragePositions.isEmpty()) return;
        if (level.getGameTime() % 10L != 0L) return;

        BlockPos masterPos = LcdScreenBlock.getMasterPos(level, pos, state);
        if (!pos.equals(masterPos)) return;

        entity.refreshStorageStats(level);
    }

    public void refreshStorageStats(Level level) {
        if (level == null || level.isClientSide() || this.linkedStoragePositions.isEmpty()) return;

        double maxDistSq = MAX_LINK_DISTANCE * MAX_LINK_DISTANCE;
        List<Container> containers = new ArrayList<>(this.linkedStoragePositions.size());
        Set<BlockPos> visitedHalves = new HashSet<>();

        for (BlockPos sPos : this.linkedStoragePositions) {
            if (sPos == null) continue;
            if (this.worldPosition.distSqr(sPos) > maxDistSq) continue;
            if (!level.isLoaded(sPos)) continue;
            if (!visitedHalves.add(sPos)) continue;

            BlockState targetState = level.getBlockState(sPos);
            Container container = null;
            if (targetState.getBlock() instanceof ChestBlock chestBlock) {
                if (targetState.hasProperty(ChestBlock.TYPE) && targetState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                    BlockPos otherHalf = sPos.relative(ChestBlock.getConnectedDirection(targetState));
                    visitedHalves.add(otherHalf);
                }
                container = ChestBlock.getContainer(chestBlock, targetState, level, sPos, true);
            } else {
                BlockEntity targetBe = level.getBlockEntity(sPos);
                if (targetBe instanceof Container c) {
                    container = c;
                }
            }

            if (container != null) {
                containers.add(container);
            }
        }

        if (containers.isEmpty()) return;

        int totalSlots = 0;
        for (Container c : containers) {
            totalSlots += c.getContainerSize();
        }

        int totalItems = 0;
        int maxCap = 0;
        int filledSlots = 0;
        double slotRatioSum = 0.0;
        boolean allFilledMaxed = true;
        int slotArrayLen = Math.min(totalSlots, MAX_SYNCED_SLOTS);
        byte[] slotFills = new byte[slotArrayLen];
        int[] rawSlotAggIdx = new int[slotArrayLen];
        Arrays.fill(rawSlotAggIdx, -1);
        int[] slotCounts = new int[slotArrayLen];

        List<AggregatedItem> rawAggList = new ArrayList<>();
        int globalSlotIdx = 0;

        for (Container container : containers) {
            int cSize = container.getContainerSize();
            int containerLimit = Math.max(1, container.getMaxStackSize());
            for (int i = 0; i < cSize; i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    int count = stack.getCount();
                    int maxStack = Math.max(1, Math.min(containerLimit, stack.getMaxStackSize()));
                    totalItems += count;
                    filledSlots++;
                    maxCap += maxStack;

                    double ratio = Math.min(1.0, (double) count / (double) maxStack);
                    slotRatioSum += ratio;
                    if (count < maxStack) {
                        allFilledMaxed = false;
                    }

                    int foundIdx = -1;
                    for (int k = 0; k < rawAggList.size(); k++) {
                        AggregatedItem cand = rawAggList.get(k);
                        if (matchesTargetItem(cand.repStack, stack)) {
                            foundIdx = k;
                            break;
                        }
                    }

                    AggregatedItem agg;
                    if (foundIdx >= 0) {
                        agg = rawAggList.get(foundIdx);
                    } else {
                        String hoverName = stack.getHoverName().getString();
                        ItemStack cleanCopy = stack.copyWithCount(1);
                        cleanCopy.remove(DataComponents.CONTAINER);
                        cleanCopy.remove(DataComponents.CONTAINER_LOOT);
                        cleanCopy.remove(DataComponents.BLOCK_ENTITY_DATA);
                        agg = new AggregatedItem(cleanCopy, hoverName);
                        foundIdx = rawAggList.size();
                        rawAggList.add(agg);
                    }
                    agg.totalCount += count;
                    agg.slotUnits += ratio;

                    if (globalSlotIdx < slotArrayLen) {
                        int cellPct = (count >= maxStack) ? 100 : Math.max(1, Math.min(99, (int) Math.round(ratio * 100.0)));
                        slotFills[globalSlotIdx] = (byte) cellPct;
                        slotCounts[globalSlotIdx] = count;
                        rawSlotAggIdx[globalSlotIdx] = foundIdx;
                    }
                } else {
                    maxCap += containerLimit;
                }
                globalSlotIdx++;
            }
        }

        int computedPercent;
        if (totalSlots <= 0 || totalItems <= 0) {
            computedPercent = 0;
        } else if (filledSlots >= totalSlots && allFilledMaxed) {
            computedPercent = 100;
        } else {
            computedPercent = (int) Math.round((slotRatioSum / (double) totalSlots) * 100.0);
            if (computedPercent <= 0) computedPercent = 1;
            if (computedPercent >= 100) computedPercent = 99;
        }

        long gameTime = level.getGameTime();
        if (gameTime < this.lastFlowSampleTick) {
            resetFlowHistory();
        }
        if (this.flowSampleCount == 0 || gameTime - this.lastFlowSampleTick >= 40L) {
            int ringIdx = this.flowHead;
            this.flowTotalHistory[ringIdx] = totalItems;
            this.flowTickHistory[ringIdx] = gameTime;

            for (int[] hist : this.flowItemHistory.values()) {
                hist[ringIdx] = 0;
            }
            for (AggregatedItem agg : rawAggList) {
                if (!agg.key.isEmpty()) {
                    int[] hist = this.flowItemHistory.computeIfAbsent(agg.key, k -> new int[FLOW_RING_SIZE]);
                    hist[ringIdx] = agg.totalCount;
                }
            }
            if (this.flowItemHistory.size() > 512) {
                this.flowItemHistory.entrySet().removeIf(e -> {
                    for (int v : e.getValue()) {
                        if (v != 0) return false;
                    }
                    return true;
                });
            }

            this.flowHead = (this.flowHead + 1) % FLOW_RING_SIZE;
            if (this.flowSampleCount < FLOW_RING_SIZE) {
                this.flowSampleCount++;
            }
            this.lastFlowSampleTick = gameTime;
        }

        int computedNetRateHr = 0;
        int newestRingIdx = -1;
        int oldestRingIdx = -1;
        long tickSpan = 40L;
        if (this.flowSampleCount >= 2) {
            newestRingIdx = (this.flowHead - 1 + FLOW_RING_SIZE) % FLOW_RING_SIZE;
            oldestRingIdx = (this.flowSampleCount < FLOW_RING_SIZE) ? 0 : this.flowHead;
            tickSpan = Math.max(20L, this.flowTickHistory[newestRingIdx] - this.flowTickHistory[oldestRingIdx]);
            int netDelta = this.flowTotalHistory[newestRingIdx] - this.flowTotalHistory[oldestRingIdx];
            computedNetRateHr = computeRatePerHour(netDelta, tickSpan);
        }

        int uniqueTypes = rawAggList.size();
        List<AggregatedItem> combinedItems = new ArrayList<>(rawAggList.size() + this.watchlistItems.size());
        combinedItems.addAll(rawAggList);

        for (int w = 0; w < this.watchlistItems.size(); w++) {
            ItemStack watchStack = this.watchlistItems.get(w);
            if (watchStack == null || watchStack.isEmpty()) continue;
            AggregatedItem matched = null;
            for (AggregatedItem cand : combinedItems) {
                if (cand.pinnedOrder < 0 && matchesTargetItem(cand.repStack, watchStack)) {
                    matched = cand;
                    break;
                }
            }
            if (matched != null) {
                matched.pinnedOrder = w;
            } else {
                ItemStack zeroCopy = watchStack.copyWithCount(1);
                AggregatedItem zeroAgg = new AggregatedItem(zeroCopy, zeroCopy.getHoverName().getString());
                zeroAgg.totalCount = 0;
                zeroAgg.slotUnits = 0.0;
                zeroAgg.pinnedOrder = w;
                combinedItems.add(zeroAgg);
            }
        }

        if (this.flowSampleCount >= 2 && newestRingIdx >= 0 && oldestRingIdx >= 0) {
            for (AggregatedItem item : combinedItems) {
                int[] hist = this.flowItemHistory.get(item.key);
                if (hist != null) {
                    int delta = item.totalCount - hist[oldestRingIdx];
                    item.ratePerHour = computeRatePerHour(delta, tickSpan);
                }
            }
        }

        final boolean prioritizePinned = (this.storageFilterMode >= 1);
        combinedItems.sort((a, b) -> {
            if (prioritizePinned) {
                boolean aPin = a.pinnedOrder >= 0;
                boolean bPin = b.pinnedOrder >= 0;
                if (aPin != bPin) {
                    return aPin ? -1 : 1;
                }
                if (aPin) {
                    return Integer.compare(a.pinnedOrder, b.pinnedOrder);
                }
            } else {
                boolean aZero = a.totalCount <= 0;
                boolean bZero = b.totalCount <= 0;
                if (aZero != bZero) {
                    return aZero ? 1 : -1;
                }
            }
            int cmp = Double.compare(b.slotUnits, a.slotUnits);
            if (cmp != 0) return cmp;
            int cntCmp = Integer.compare(b.totalCount, a.totalCount);
            if (cntCmp != 0) return cntCmp;
            return Integer.compare(a.pinnedOrder, b.pinnedOrder);
        });

        int itemCount = Math.min(MAX_SYNCED_ITEMS, combinedItems.size());
        ItemStack[] nextStacks = new ItemStack[itemCount];
        String[] nextNames = new String[itemCount];
        int[] nextCounts = new int[itemCount];
        int[] nextShares = new int[itemCount];
        int[] nextRatesHr = new int[itemCount];
        byte[] nextPinned = new byte[itemCount];

        for (int i = 0; i < itemCount; i++) {
            AggregatedItem item = combinedItems.get(i);
            item.sortedIndex = i;
            nextStacks[i] = item.repStack;
            nextNames[i] = item.name;
            nextCounts[i] = item.totalCount;
            int sh = totalSlots > 0 ? (int) Math.round((item.slotUnits / (double) totalSlots) * 100.0) : 0;
            if (sh <= 0 && item.totalCount > 0) sh = 1;
            nextShares[i] = Math.min(100, sh);
            nextRatesHr[i] = item.ratePerHour;
            nextPinned[i] = (byte) (item.pinnedOrder >= 0 ? 1 : 0);
        }

        int[] slotPalette = new int[slotArrayLen];
        for (int i = 0; i < slotArrayLen; i++) {
            int rawIdx = rawSlotAggIdx[i];
            slotPalette[i] = (rawIdx >= 0 && rawIdx < rawAggList.size()) ? rawAggList.get(rawIdx).sortedIndex : -1;
        }

        boolean changed = (this.storagePercent != computedPercent)
                || (this.storageTotalItems != totalItems)
                || (this.storageMaxCapacity != maxCap)
                || (this.storageFilledSlots != filledSlots)
                || (this.storageTotalSlots != totalSlots)
                || (this.storageUniqueTypes != uniqueTypes)
                || (this.storageNetRatePerHour != computedNetRateHr)
                || !Arrays.equals(this.storageSlotFills, slotFills)
                || !Arrays.equals(this.storageSlotPalette, slotPalette)
                || !Arrays.equals(this.storageSlotCounts, slotCounts)
                || !Arrays.equals(this.storageItemCounts, nextCounts)
                || !Arrays.equals(this.storageItemShares, nextShares)
                || !Arrays.equals(this.storageItemRatesPerHour, nextRatesHr)
                || !Arrays.equals(this.storageItemPinned, nextPinned);

        if (!changed) {
            if (this.storageItemStacks.length != nextStacks.length) {
                changed = true;
            } else {
                for (int i = 0; i < nextStacks.length; i++) {
                    if (!ItemStack.isSameItemSameComponents(this.getStorageTopStack(i), nextStacks[i])) {
                        changed = true;
                        break;
                    }
                }
            }
        }

        if (changed) {
            this.storagePercent = computedPercent;
            this.storageTotalItems = totalItems;
            this.storageMaxCapacity = maxCap;
            this.storageFilledSlots = filledSlots;
            this.storageTotalSlots = totalSlots;
            this.storageUniqueTypes = uniqueTypes;
            this.storageNetRatePerHour = computedNetRateHr;
            this.storageSlotFills = slotFills;
            this.storageSlotPalette = slotPalette;
            this.storageSlotCounts = slotCounts;
            this.storageItemStacks = nextStacks;
            this.storageItemNames = nextNames;
            this.storageItemCounts = nextCounts;
            this.storageItemShares = nextShares;
            this.storageItemRatesPerHour = nextRatesHr;
            this.storageItemPinned = nextPinned;
            this.lastResolvedRawText = null;
            this.setChanged();
            this.sync();
        }
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
        this.loopEnabled = other.loopEnabled;
        this.loopShuffle = other.loopShuffle;
        this.loopTransition = other.loopTransition;
        this.loopAnims = other.loopAnims != null ? other.loopAnims.clone() : new int[0];
        this.loopDurations = other.loopDurations != null ? other.loopDurations.clone() : new int[0];
        this.loopColors = other.loopColors != null ? other.loopColors.clone() : new int[0];
        this.loopTexts1 = other.loopTexts1 != null ? other.loopTexts1.clone() : new String[0];
        this.loopTexts2 = other.loopTexts2 != null ? other.loopTexts2.clone() : new String[0];
        this.loopModes1 = other.loopModes1 != null ? other.loopModes1.clone() : new int[0];
        this.loopModes2 = other.loopModes2 != null ? other.loopModes2.clone() : new int[0];
        this.loopAligns = other.loopAligns != null ? other.loopAligns.clone() : new int[0];
        this.linkedStoragePositions.clear();
        this.linkedStoragePositions.addAll(other.linkedStoragePositions);
        this.watchlistItems.clear();
        for (ItemStack st : other.watchlistItems) {
            if (st != null && !st.isEmpty()) {
                this.watchlistItems.add(st.copy());
            }
        }
        this.storageMonitorMode = other.storageMonitorMode;
        this.storageStyle = other.storageStyle;
        this.storageFilterMode = other.storageFilterMode;
        this.storageFlowMode = other.storageFlowMode;
        this.storagePercent = other.storagePercent;
        this.storageTotalItems = other.storageTotalItems;
        this.storageMaxCapacity = other.storageMaxCapacity;
        this.storageFilledSlots = other.storageFilledSlots;
        this.storageTotalSlots = other.storageTotalSlots;
        this.storageUniqueTypes = other.storageUniqueTypes;
        this.storageNetRatePerHour = other.storageNetRatePerHour;
        this.storageSlotFills = other.storageSlotFills != null ? other.storageSlotFills.clone() : new byte[0];
        this.storageSlotPalette = other.storageSlotPalette != null ? other.storageSlotPalette.clone() : new int[0];
        this.storageSlotCounts = other.storageSlotCounts != null ? other.storageSlotCounts.clone() : new int[0];
        int len = other.getStorageEntryCount();
        this.storageItemStacks = new ItemStack[len];
        this.storageItemNames = new String[len];
        this.storageItemCounts = new int[len];
        this.storageItemShares = new int[len];
        this.storageItemRatesPerHour = new int[len];
        this.storageItemPinned = new byte[len];
        for (int i = 0; i < len; i++) {
            ItemStack st = other.getStorageTopStack(i);
            this.storageItemStacks[i] = st.isEmpty() ? ItemStack.EMPTY : st.copy();
            this.storageItemNames[i] = other.storageItemNames[i];
            this.storageItemCounts[i] = other.storageItemCounts[i];
            this.storageItemShares[i] = other.storageItemShares[i];
            this.storageItemRatesPerHour[i] = other.getStorageTopRatePerHour(i);
            this.storageItemPinned[i] = (byte) (other.isStorageTopPinned(i) ? 1 : 0);
        }
        this.lastResolvedRawText = null;
        this.lastWidgetTick = -100L;
    }

    public String getResolvedLine(int line, @Nullable Level level) {
        String raw = getText();
        long currentTick = level != null ? level.getGameTime() : 0L;
        boolean hasVars = raw.indexOf('{') >= 0;
        if (lastResolvedRawText == null || !lastResolvedRawText.equals(raw) || (hasVars && currentTick - lastResolvedTick >= 4L)) {
            lastResolvedRawText = raw;
            lastResolvedTick = currentTick;
            String rawL1 = raw;
            String rawL2 = "";
            int nl = raw.indexOf('\n');
            if (nl >= 0) {
                rawL1 = raw.substring(0, nl);
                rawL2 = raw.substring(nl + 1);
            }
            cachedResolvedLine1 = LcdTextFormatter.resolveAll(rawL1, level, this.worldPosition);
            cachedResolvedLine2 = LcdTextFormatter.resolveAll(rawL2, level, this.worldPosition);
        }
        return line == 0 ? cachedResolvedLine1 : cachedResolvedLine2;
    }

    public String getResolvedWidgetText(int corner, @Nullable Level level) {
        long currentTick = level != null ? level.getGameTime() : 0L;
        if (currentTick - lastWidgetTick >= 4L) {
            lastWidgetTick = currentTick;
            cachedWidgetTLText = widgetTL > 0 ? LcdTextFormatter.getWidgetText(widgetTL, level, this.worldPosition) : "";
            cachedWidgetTRText = widgetTR > 0 ? LcdTextFormatter.getWidgetText(widgetTR, level, this.worldPosition) : "";
            cachedWidgetBLText = widgetBL > 0 ? LcdTextFormatter.getWidgetText(widgetBL, level, this.worldPosition) : "";
            cachedWidgetBRText = widgetBR > 0 ? LcdTextFormatter.getWidgetText(widgetBR, level, this.worldPosition) : "";
        }
        return switch (corner) {
            case 0 -> cachedWidgetTLText;
            case 1 -> cachedWidgetTRText;
            case 2 -> cachedWidgetBLText;
            case 3 -> cachedWidgetBRText;
            default -> "";
        };
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
        tag.putBoolean("LcdLoopEnabled", this.loopEnabled);
        tag.putBoolean("LcdLoopShuffle", this.loopShuffle);
        tag.putInt("LcdLoopTransition", this.loopTransition);
        if (this.loopAnims != null && this.loopAnims.length > 0) {
            tag.putIntArray("LcdLoopAnims", this.loopAnims);
            tag.putIntArray("LcdLoopDurations", this.loopDurations != null ? this.loopDurations : new int[0]);
            tag.putIntArray("LcdLoopColors", this.loopColors != null ? this.loopColors : new int[0]);
            ListTag loopStepList = new ListTag();
            int count = this.loopAnims.length;
            for (int i = 0; i < count; i++) {
                CompoundTag sTag = new CompoundTag();
                sTag.putInt("Anim", this.loopAnims[i]);
                sTag.putInt("Dur", (this.loopDurations != null && i < this.loopDurations.length) ? this.loopDurations[i] : 6);
                sTag.putInt("Col", (this.loopColors != null && i < this.loopColors.length) ? this.loopColors[i] : -1);
                if (this.loopTexts1 != null && i < this.loopTexts1.length && this.loopTexts1[i] != null && !this.loopTexts1[i].isEmpty()) {
                    sTag.putString("Text1", this.loopTexts1[i]);
                }
                if (this.loopTexts2 != null && i < this.loopTexts2.length && this.loopTexts2[i] != null && !this.loopTexts2[i].isEmpty()) {
                    sTag.putString("Text2", this.loopTexts2[i]);
                }
                if (this.loopModes1 != null && i < this.loopModes1.length && this.loopModes1[i] >= 0) {
                    sTag.putInt("Mode1", this.loopModes1[i]);
                }
                if (this.loopModes2 != null && i < this.loopModes2.length && this.loopModes2[i] >= 0) {
                    sTag.putInt("Mode2", this.loopModes2[i]);
                }
                if (this.loopAligns != null && i < this.loopAligns.length && this.loopAligns[i] >= 0) {
                    sTag.putInt("Align", this.loopAligns[i]);
                }
                loopStepList.add(sTag);
            }
            tag.put("LcdLoopStepList", loopStepList);
        }

        if (!this.linkedStoragePositions.isEmpty()) {
            long[] posLongs = new long[this.linkedStoragePositions.size()];
            for (int i = 0; i < this.linkedStoragePositions.size(); i++) {
                posLongs[i] = this.linkedStoragePositions.get(i).asLong();
            }
            tag.putLongArray("LinkedStorageList", posLongs);
            tag.putLong("LinkedStoragePos", posLongs[0]);
        }
        tag.putInt("StorageMonitorMode", this.storageMonitorMode);
        tag.putInt("StorageStyle", this.storageStyle);
        tag.putInt("StorageFilterMode", this.storageFilterMode);
        tag.putInt("StorageFlowMode", this.storageFlowMode);
        tag.putInt("StoragePct", this.storagePercent);
        tag.putInt("StorageTotalItems", this.storageTotalItems);
        tag.putInt("StorageMaxCap", this.storageMaxCapacity);
        tag.putInt("StorageFilledSlots", this.storageFilledSlots);
        tag.putInt("StorageTotalSlots", this.storageTotalSlots);
        tag.putInt("StorageUnique", this.storageUniqueTypes);
        tag.putInt("StorageNetRateHr", this.storageNetRatePerHour);

        ListTag watchTagList = new ListTag();
        for (ItemStack ws : this.watchlistItems) {
            if (ws != null && !ws.isEmpty()) {
                Tag saved = ws.saveOptional(registries);
                if (saved instanceof CompoundTag stTag) {
                    watchTagList.add(stTag);
                }
            }
        }
        tag.put("StorageWatchlist", watchTagList);

        if (this.storageSlotFills != null && this.storageSlotFills.length > 0) {
            tag.putByteArray("StorageSlots", this.storageSlotFills);
        }
        if (this.storageSlotPalette != null && this.storageSlotPalette.length > 0) {
            tag.putIntArray("StorageSlotPal", this.storageSlotPalette);
        }
        if (this.storageSlotCounts != null && this.storageSlotCounts.length > 0) {
            tag.putIntArray("StorageSlotCnt", this.storageSlotCounts);
        }
        ListTag topList = new ListTag();
        int entryLen = getStorageEntryCount();
        for (int i = 0; i < entryLen; i++) {
            ItemStack st = getStorageTopStack(i);
            int cnt = this.storageItemCounts[i];
            if (!st.isEmpty() || cnt > 0) {
                CompoundTag entry = new CompoundTag();
                if (!st.isEmpty()) {
                    Tag saved = st.saveOptional(registries);
                    if (saved instanceof CompoundTag stackTag) {
                        entry.put("Stack", stackTag);
                    }
                }
                entry.putString("Name", this.storageItemNames[i] != null ? this.storageItemNames[i] : "");
                entry.putInt("Count", cnt);
                entry.putInt("Share", this.storageItemShares[i]);
                entry.putInt("RateHr", getStorageTopRatePerHour(i));
                entry.putBoolean("Pin", isStorageTopPinned(i));
                topList.add(entry);
            }
        }
        tag.put("StorageTopList", topList);
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
        this.loopEnabled = tag.contains("LcdLoopEnabled") && tag.getBoolean("LcdLoopEnabled");
        this.loopShuffle = tag.contains("LcdLoopShuffle") && tag.getBoolean("LcdLoopShuffle");
        this.loopTransition = tag.contains("LcdLoopTransition") ? Math.max(0, Math.min(9, tag.getInt("LcdLoopTransition"))) : 1;
        if (tag.contains("LcdLoopStepList", Tag.TAG_LIST)) {
            ListTag list = tag.getList("LcdLoopStepList", Tag.TAG_COMPOUND);
            int count = Math.min(MAX_LOOP_STEPS, list.size());
            this.loopAnims = new int[count];
            this.loopDurations = new int[count];
            this.loopColors = new int[count];
            this.loopTexts1 = new String[count];
            this.loopTexts2 = new String[count];
            this.loopModes1 = new int[count];
            this.loopModes2 = new int[count];
            this.loopAligns = new int[count];
            for (int i = 0; i < count; i++) {
                CompoundTag sTag = list.getCompound(i);
                this.loopAnims[i] = Math.max(0, Math.min(60, sTag.getInt("Anim")));
                this.loopDurations[i] = Math.max(1, Math.min(999, sTag.contains("Dur") ? sTag.getInt("Dur") : 6));
                int col = sTag.contains("Col") ? sTag.getInt("Col") : -1;
                this.loopColors[i] = (col < 0) ? Math.max(-2, col) : (col & 0xFFFFFF);
                this.loopTexts1[i] = sTag.getString("Text1");
                this.loopTexts2[i] = sTag.getString("Text2");
                this.loopModes1[i] = sTag.contains("Mode1") ? Math.max(0, Math.min(9, sTag.getInt("Mode1"))) : -1;
                this.loopModes2[i] = sTag.contains("Mode2") ? Math.max(0, Math.min(9, sTag.getInt("Mode2"))) : -1;
                this.loopAligns[i] = sTag.contains("Align") ? Math.max(0, Math.min(2, sTag.getInt("Align"))) : -1;
            }
        } else if (tag.contains("LcdLoopAnims", Tag.TAG_INT_ARRAY)) {
            int[] rawAnims = tag.getIntArray("LcdLoopAnims");
            int[] rawDurs = tag.contains("LcdLoopDurations", Tag.TAG_INT_ARRAY) ? tag.getIntArray("LcdLoopDurations") : new int[0];
            int[] rawCols = tag.contains("LcdLoopColors", Tag.TAG_INT_ARRAY) ? tag.getIntArray("LcdLoopColors") : new int[0];
            int count = Math.min(MAX_LOOP_STEPS, rawAnims.length);
            this.loopAnims = new int[count];
            this.loopDurations = new int[count];
            this.loopColors = new int[count];
            this.loopTexts1 = new String[count];
            this.loopTexts2 = new String[count];
            this.loopModes1 = new int[count];
            this.loopModes2 = new int[count];
            this.loopAligns = new int[count];
            for (int i = 0; i < count; i++) {
                this.loopAnims[i] = Math.max(0, Math.min(60, rawAnims[i]));
                this.loopDurations[i] = Math.max(1, Math.min(999, i < rawDurs.length ? rawDurs[i] : 6));
                int col = i < rawCols.length ? rawCols[i] : -1;
                this.loopColors[i] = (col < 0) ? Math.max(-2, col) : (col & 0xFFFFFF);
                this.loopTexts1[i] = "";
                this.loopTexts2[i] = "";
                this.loopModes1[i] = -1;
                this.loopModes2[i] = -1;
                this.loopAligns[i] = -1;
            }
        } else {
            this.loopAnims = new int[0];
            this.loopDurations = new int[0];
            this.loopColors = new int[0];
            this.loopTexts1 = new String[0];
            this.loopTexts2 = new String[0];
            this.loopModes1 = new int[0];
            this.loopModes2 = new int[0];
            this.loopAligns = new int[0];
        }

        this.linkedStoragePositions.clear();
        if (tag.contains("LinkedStorageList", Tag.TAG_LONG_ARRAY)) {
            long[] posLongs = tag.getLongArray("LinkedStorageList");
            int lim = Math.min(MAX_LINKED_STORAGES, posLongs.length);
            for (int i = 0; i < lim; i++) {
                BlockPos p = BlockPos.of(posLongs[i]);
                if (!this.linkedStoragePositions.contains(p)) {
                    this.linkedStoragePositions.add(p);
                }
            }
        } else if (tag.contains("LinkedStoragePos")) {
            this.linkedStoragePositions.add(BlockPos.of(tag.getLong("LinkedStoragePos")));
        }
        if (tag.contains("StorageMonitorMode")) this.storageMonitorMode = tag.getInt("StorageMonitorMode");
        if (tag.contains("StorageStyle")) this.storageStyle = Math.max(0, Math.min(5, tag.getInt("StorageStyle")));
        if (tag.contains("StorageFilterMode")) this.storageFilterMode = Math.max(0, Math.min(2, tag.getInt("StorageFilterMode")));
        if (tag.contains("StorageFlowMode")) this.storageFlowMode = Math.max(0, Math.min(2, tag.getInt("StorageFlowMode")));
        if (tag.contains("StoragePct")) this.storagePercent = tag.getInt("StoragePct");
        if (tag.contains("StorageTotalItems")) this.storageTotalItems = tag.getInt("StorageTotalItems");
        if (tag.contains("StorageMaxCap")) this.storageMaxCapacity = tag.getInt("StorageMaxCap");
        if (tag.contains("StorageFilledSlots")) this.storageFilledSlots = tag.getInt("StorageFilledSlots");
        if (tag.contains("StorageTotalSlots")) this.storageTotalSlots = tag.getInt("StorageTotalSlots");
        if (tag.contains("StorageUnique")) this.storageUniqueTypes = tag.getInt("StorageUnique");
        this.storageNetRatePerHour = tag.contains("StorageNetRateHr") ? tag.getInt("StorageNetRateHr") : 0;

        this.watchlistItems.clear();
        if (tag.contains("StorageWatchlist", Tag.TAG_LIST)) {
            ListTag watchTagList = tag.getList("StorageWatchlist", Tag.TAG_COMPOUND);
            int wLim = Math.min(MAX_WATCHLIST_ITEMS, watchTagList.size());
            for (int i = 0; i < wLim; i++) {
                ItemStack parsed = ItemStack.parseOptional(registries, watchTagList.getCompound(i));
                if (!parsed.isEmpty()) {
                    this.watchlistItems.add(cleanWatchlistCopy(parsed));
                }
            }
        }

        if (tag.contains("StorageSlots", Tag.TAG_BYTE_ARRAY)) {
            this.storageSlotFills = tag.getByteArray("StorageSlots");
        } else {
            this.storageSlotFills = new byte[0];
        }
        if (tag.contains("StorageSlotPal", Tag.TAG_INT_ARRAY)) {
            this.storageSlotPalette = tag.getIntArray("StorageSlotPal");
        } else {
            this.storageSlotPalette = new int[0];
        }
        if (tag.contains("StorageSlotCnt", Tag.TAG_INT_ARRAY)) {
            this.storageSlotCounts = tag.getIntArray("StorageSlotCnt");
        } else {
            this.storageSlotCounts = new int[0];
        }
        if (tag.contains("StorageTopList", Tag.TAG_LIST)) {
            ListTag topList = tag.getList("StorageTopList", Tag.TAG_COMPOUND);
            int lim = Math.min(MAX_SYNCED_ITEMS, topList.size());
            this.storageItemStacks = new ItemStack[lim];
            this.storageItemNames = new String[lim];
            this.storageItemCounts = new int[lim];
            this.storageItemShares = new int[lim];
            this.storageItemRatesPerHour = new int[lim];
            this.storageItemPinned = new byte[lim];
            for (int i = 0; i < lim; i++) {
                CompoundTag entry = topList.getCompound(i);
                if (entry.contains("Stack", Tag.TAG_COMPOUND)) {
                    this.storageItemStacks[i] = ItemStack.parseOptional(registries, entry.getCompound("Stack"));
                } else {
                    this.storageItemStacks[i] = ItemStack.EMPTY;
                }
                this.storageItemNames[i] = entry.getString("Name");
                this.storageItemCounts[i] = entry.getInt("Count");
                this.storageItemShares[i] = entry.getInt("Share");
                this.storageItemRatesPerHour[i] = entry.contains("RateHr") ? entry.getInt("RateHr") : 0;
                this.storageItemPinned[i] = (byte) (entry.getBoolean("Pin") ? 1 : 0);
            }
        } else {
            this.storageItemStacks = new ItemStack[0];
            this.storageItemNames = new String[0];
            this.storageItemCounts = new int[0];
            this.storageItemShares = new int[0];
            this.storageItemRatesPerHour = new int[0];
            this.storageItemPinned = new byte[0];
        }
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

    public AABB cachedRenderBoundingBox = null;

    public AABB getRenderBoundingBox() {
        if (this.cachedRenderBoundingBox == null) {
            int w = Math.max(1, this.cachedWidthBlocks);
            int h = Math.max(1, this.cachedHeightBlocks);
            if (w == 1 && h == 1) {
                this.cachedRenderBoundingBox = new AABB(this.worldPosition);
            } else {
                this.cachedRenderBoundingBox = new AABB(this.worldPosition).inflate(Math.max(w, h));
            }
        }
        return this.cachedRenderBoundingBox;
    }
}
