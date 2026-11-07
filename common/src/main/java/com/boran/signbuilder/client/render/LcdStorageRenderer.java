package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import com.boran.signbuilder.client.LcdFontHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class LcdStorageRenderer {

    private static final class IconCmd {
        final ItemStack stack;
        final float cx;
        final float cy;
        final float size;

        IconCmd(ItemStack stack, float cx, float cy, float size) {
            this.stack = stack;
            this.cx = cx;
            this.cy = cy;
            this.size = size;
        }
    }

    private static final class TextCmd {
        final String text;
        final float x;
        final float y;
        final float scale;
        final int color;
        final boolean shadow;
        final int bgColor;

        TextCmd(String text, float x, float y, float scale, int color, boolean shadow, int bgColor) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.color = color;
            this.shadow = shadow;
            this.bgColor = bgColor;
        }
    }

    private static final class DisplayEntry {
        final ItemStack stack;
        final String name;
        final int count;
        final int share;
        final int rateHr;
        final boolean pinned;

        DisplayEntry(ItemStack stack, String name, int count, int share, int rateHr, boolean pinned) {
            this.stack = stack;
            this.name = name;
            this.count = count;
            this.share = share;
            this.rateHr = rateHr;
            this.pinned = pinned;
        }
    }

    private static final List<IconCmd> ICON_QUEUE = new ArrayList<>(128);
    private static final List<TextCmd> TEXT_QUEUE = new ArrayList<>(180);
    private static final List<DisplayEntry> ACTIVE_ITEMS = new ArrayList<>(128);

    private static float clipMinX;
    private static float clipMaxX;
    private static float clipMinY;
    private static float clipMaxY;
    private static float curZ;
    private static boolean isEInk;
    private static int activeFilterMode;
    private static int activeFlowMode;

    public static void render3D(Font font, PoseStack poseStack, MultiBufferSource bufferSource, RenderType canvasType,
                                LcdScreenBlockEntity master, Level level,
                                float minX, float maxX, float topY, float bottomY,
                                int widthBlocks, int heightBlocks,
                                int theme, int light, int accentRgb, boolean glow, int fontStyle, long timeMs) {
        ICON_QUEUE.clear();
        TEXT_QUEUE.clear();
        isEInk = (theme == 12);

        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.03f);

        VertexConsumer vc = bufferSource.getBuffer(canvasType);
        Matrix4f mat = poseStack.last().pose();

        buildScene(font, vc, mat, light, master, minX, maxX, topY, bottomY, widthBlocks, heightBlocks, theme, accentRgb, glow, fontStyle, timeMs);

        if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(canvasType);
        }

        double camDistSq = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .distanceToSqr(master.getBlockPos().getX() + 0.5, master.getBlockPos().getY() + 0.5, master.getBlockPos().getZ() + 0.5);
        if (camDistSq <= 36.0 * 36.0) {
            int itemLight = glow ? 15728880 : light;
            var itemRenderer = Minecraft.getInstance().getItemRenderer();
            for (int i = 0; i < ICON_QUEUE.size(); i++) {
                IconCmd ic = ICON_QUEUE.get(i);
                float half = ic.size * 0.45f;
                if (ic.cx - half < minX - 1.5f || ic.cx + half > maxX + 1.5f || ic.cy - half < topY - 1.5f || ic.cy + half > bottomY + 1.5f) {
                    continue;
                }
                poseStack.pushPose();
                poseStack.translate(ic.cx, ic.cy, 0.095f);
                poseStack.last().pose().scale(ic.size, -ic.size, 0.06f);
                poseStack.last().normal().scale(1.0f, -1.0f, 1.0f);
                itemRenderer.renderStatic(ic.stack, ItemDisplayContext.GUI, itemLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, i);
                poseStack.popPose();
            }
        }

        for (int i = 0; i < TEXT_QUEUE.size(); i++) {
            TextCmd tc = TEXT_QUEUE.get(i);
            if (tc.text == null || tc.text.isEmpty()) continue;
            if (tc.y < topY - 2.0f || tc.y + font.lineHeight * tc.scale > bottomY + 3.0f) continue;
            poseStack.pushPose();
            poseStack.translate(tc.x, tc.y, 0.145f);
            poseStack.scale(tc.scale, tc.scale, 0.15f);
            FormattedCharSequence seq = LcdFontHelper.toFormatted(tc.text, fontStyle);
            boolean drawShadow = tc.shadow && glow && (!isEInk || tc.bgColor != 0);
            font.drawInBatch(seq, 0.0f, 0.0f, tc.color, drawShadow, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, tc.bgColor, light);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    public static void render2D(Font font, GuiGraphics g, LcdScreenBlockEntity master,
                                int px, int py, int pw, int ph,
                                int widthBlocks, int heightBlocks,
                                int styleOverride, int speedOverride,
                                int theme, int accentRgb, boolean glow, int fontStyle, long timeMs) {
        int fm = master != null ? master.getStorageFilterMode() : 0;
        int flm = master != null ? master.getStorageFlowMode() : 0;
        render2D(font, g, master, px, py, pw, ph, widthBlocks, heightBlocks, styleOverride, speedOverride, fm, flm, null, theme, accentRgb, glow, fontStyle, timeMs);
    }

    public static void render2D(Font font, GuiGraphics g, LcdScreenBlockEntity master,
                                int px, int py, int pw, int ph,
                                int widthBlocks, int heightBlocks,
                                int styleOverride, int speedOverride,
                                int filterModeOverride, int flowModeOverride, @Nullable List<ItemStack> watchlistOverride,
                                int theme, int accentRgb, boolean glow, int fontStyle, long timeMs) {
        ICON_QUEUE.clear();
        TEXT_QUEUE.clear();
        isEInk = (theme == 12);

        float baseScale = 0.022f;
        float worldFontScale = (heightBlocks <= 1) ? baseScale : (baseScale * (1.0f + (heightBlocks - 1) * 0.45f));
        float margin = (0.055f / worldFontScale) * 2.0f;
        float virtW = Math.max(36.0f, (widthBlocks / worldFontScale) - margin);
        float virtH = Math.max(36.0f, (heightBlocks / worldFontScale) - margin);

        float scaleX = (pw - 4.0f) / virtW;
        float scaleY = (ph - 4.0f) / virtH;
        float uniScale = Math.min(scaleX, scaleY);
        float drawW = (pw - 4.0f) / uniScale;
        float drawH = (ph - 4.0f) / uniScale;

        g.pose().pushPose();
        g.pose().translate(px + 2.0f, py + 2.0f, 0.0f);
        g.pose().scale(uniScale, uniScale, 1.0f);

        Matrix4f guiMat = new Matrix4f(g.pose().last().pose());
        VertexConsumer gvc = g.bufferSource().getBuffer(RenderType.gui());
        buildSceneWithOverrides(font, gvc, guiMat, -1, master, 0.0f, drawW, 0.0f, drawH,
                widthBlocks, heightBlocks, styleOverride, speedOverride,
                filterModeOverride, flowModeOverride, watchlistOverride,
                theme, accentRgb, glow, fontStyle, timeMs);
        g.flush();
        g.pose().popPose();

        for (int i = 0; i < ICON_QUEUE.size(); i++) {
            IconCmd ic = ICON_QUEUE.get(i);
            float sx = px + 2.0f + ic.cx * uniScale;
            float sy = py + 2.0f + ic.cy * uniScale;
            float sScale = (ic.size * uniScale) / 16.0f;
            if (sScale <= 0.05f) continue;
            g.pose().pushPose();
            g.pose().translate(sx, sy, 0.0f);
            g.pose().scale(sScale, sScale, sScale);
            g.renderFakeItem(ic.stack, -8, -8);
            g.pose().popPose();
        }

        for (int i = 0; i < TEXT_QUEUE.size(); i++) {
            TextCmd tc = TEXT_QUEUE.get(i);
            if (tc.text == null || tc.text.isEmpty()) continue;
            float tx = px + 2.0f + tc.x * uniScale;
            float ty = py + 2.0f + tc.y * uniScale;
            float ts = tc.scale * uniScale;
            if (ts <= 0.05f) continue;
            g.pose().pushPose();
            g.pose().translate(tx, ty, 350.0f);
            g.pose().scale(ts, ts, 1.0f);
            FormattedCharSequence seq = LcdFontHelper.toFormatted(tc.text, fontStyle);
            if (tc.bgColor != 0) {
                int tw = LcdFontHelper.getWidth(font, tc.text, fontStyle);
                g.fill(-1, -1, tw + 1, font.lineHeight, tc.bgColor);
            }
            boolean drawShadow = tc.shadow && glow && (!isEInk || tc.bgColor != 0);
            g.drawString(font, seq, 0, 0, tc.color, drawShadow);
            g.pose().popPose();
        }
    }

    private static void buildScene(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                   LcdScreenBlockEntity master,
                                   float minX, float maxX, float topY, float bottomY,
                                   int widthBlocks, int heightBlocks,
                                   int theme, int accentRgb, boolean glow, int fontStyle, long timeMs) {
        int style = master != null ? master.getStorageStyle() : 0;
        int speed = master != null ? master.getSpeed() : 2;
        int filterMode = master != null ? master.getStorageFilterMode() : 0;
        int flowMode = master != null ? master.getStorageFlowMode() : 0;
        buildSceneWithOverrides(font, vc, mat, light, master, minX, maxX, topY, bottomY,
                widthBlocks, heightBlocks, style, speed, filterMode, flowMode, null, theme, accentRgb, glow, fontStyle, timeMs);
    }

    private static void buildActiveItems(LcdScreenBlockEntity master, int filterMode, @Nullable List<ItemStack> watchlistOverride) {
        ACTIVE_ITEMS.clear();
        if (master == null) return;
        int rawCount = master.getStorageEntryCount();

        if (watchlistOverride == null) {
            for (int i = 0; i < rawCount; i++) {
                int cnt = master.getStorageTopCount(i);
                boolean pin = master.isStorageTopPinned(i);
                if (filterMode == 0 && cnt <= 0) continue;
                if (filterMode == 2 && !pin) continue;
                ACTIVE_ITEMS.add(new DisplayEntry(
                        master.getStorageTopStack(i),
                        master.getStorageTopDisplayName(i),
                        cnt,
                        master.getStorageTopShare(i),
                        master.getStorageTopRatePerHour(i),
                        pin
                ));
            }
            return;
        }

        boolean[] matchedRaw = new boolean[rawCount];
        if (filterMode >= 1) {
            for (ItemStack ws : watchlistOverride) {
                if (ws == null || ws.isEmpty()) continue;
                int found = -1;
                for (int i = 0; i < rawCount; i++) {
                    if (!matchedRaw[i] && LcdScreenBlockEntity.matchesTargetItem(master.getStorageTopStack(i), ws)) {
                        found = i;
                        break;
                    }
                }
                if (found >= 0) {
                    matchedRaw[found] = true;
                    ACTIVE_ITEMS.add(new DisplayEntry(
                            master.getStorageTopStack(found),
                            master.getStorageTopDisplayName(found),
                            master.getStorageTopCount(found),
                            master.getStorageTopShare(found),
                            master.getStorageTopRatePerHour(found),
                            true
                    ));
                } else {
                    ItemStack clean = ws.copyWithCount(1);
                    ACTIVE_ITEMS.add(new DisplayEntry(clean, clean.getHoverName().getString(), 0, 0, 0, true));
                }
            }
            if (filterMode == 1) {
                for (int i = 0; i < rawCount; i++) {
                    if (matchedRaw[i]) continue;
                    int cnt = master.getStorageTopCount(i);
                    if (cnt <= 0) continue;
                    ACTIVE_ITEMS.add(new DisplayEntry(
                            master.getStorageTopStack(i),
                            master.getStorageTopDisplayName(i),
                            cnt,
                            master.getStorageTopShare(i),
                            master.getStorageTopRatePerHour(i),
                            false
                    ));
                }
            }
        } else {
            for (int i = 0; i < rawCount; i++) {
                int cnt = master.getStorageTopCount(i);
                if (cnt <= 0) continue;
                ItemStack st = master.getStorageTopStack(i);
                boolean pin = false;
                for (ItemStack ws : watchlistOverride) {
                    if (LcdScreenBlockEntity.matchesTargetItem(st, ws)) {
                        pin = true;
                        break;
                    }
                }
                ACTIVE_ITEMS.add(new DisplayEntry(
                        st,
                        master.getStorageTopDisplayName(i),
                        cnt,
                        master.getStorageTopShare(i),
                        master.getStorageTopRatePerHour(i),
                        pin
                ));
            }
        }
    }

    private static void buildSceneWithOverrides(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                                LcdScreenBlockEntity master,
                                                float minX, float maxX, float topY, float bottomY,
                                                int widthBlocks, int heightBlocks,
                                                int style, int speed,
                                                int filterMode, int flowMode, @Nullable List<ItemStack> watchlistOverride,
                                                int theme, int accentRgb, boolean glow, int fontStyle, long timeMs) {
        clipMinX = minX;
        clipMaxX = maxX;
        clipMinY = topY;
        clipMaxY = bottomY;
        curZ = 0.002f;
        isEInk = (theme == 12);
        activeFilterMode = filterMode;
        activeFlowMode = flowMode;

        float w = maxX - minX;
        float h = bottomY - topY;
        if (w <= 6.0f || h <= 6.0f) return;

        if (master == null || !master.hasLinkedStorage()) {
            renderUnlinkedState(font, vc, mat, light, minX, topY, w, h, fontStyle, timeMs);
            return;
        }

        buildActiveItems(master, filterMode, watchlistOverride);

        int effAccent = resolveThemeAccent(accentRgb);
        int pct = master.getStoragePercent();
        int statusRgb = resolveStatusColor(pct, effAccent, timeMs);

        if (widthBlocks <= 1 && heightBlocks <= 1) {
            renderSingleSpotlightSlide(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            return;
        }
        if ((widthBlocks == 2 && heightBlocks == 1) || (widthBlocks == 1 && heightBlocks == 2)) {
            renderDualSpotlightSlide(font, vc, mat, light, master, minX, topY, w, h, widthBlocks == 2, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            return;
        }

        switch (style) {
            case 1 -> renderShowcaseCardsStyle(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            case 2 -> renderManifestTableStyle(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            case 3 -> renderLiveTickerStyle(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            case 4 -> renderCyberRingStyle(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            case 5 -> renderLiquidSiloStyle(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
            default -> renderLiveChestGridStyle(font, vc, mat, light, master, minX, topY, w, h, pct, statusRgb, effAccent, speed, fontStyle, timeMs);
        }
    }

    private static int resolveThemeAccent(int accentRgb) {
        int rgb = accentRgb & 0x00FFFFFF;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int maxC = Math.max(r, Math.max(g, b));

        if (isEInk) {
            if (maxC < 55 || (r > 200 && g > 200 && b > 200)) {
                return 0x1E3A5F;
            }
            int lum = (r * 299 + g * 587 + b * 114) / 1000;
            if (lum > 130) {
                float factor = 112.0f / lum;
                r = Math.min(255, Math.round(r * factor));
                g = Math.min(255, Math.round(g * factor));
                b = Math.min(255, Math.round(b * factor));
                return (r << 16) | (g << 8) | b;
            }
            return rgb;
        }

        if (maxC < 50) {
            return 0x00E5FF;
        }
        if (maxC < 120) {
            float factor = 140.0f / maxC;
            r = Math.min(255, Math.round(r * factor));
            g = Math.min(255, Math.round(g * factor));
            b = Math.min(255, Math.round(b * factor));
            return (r << 16) | (g << 8) | b;
        }
        return rgb;
    }

    private static int colHudBg() {
        return isEInk ? 0xE8DCE1DA : 0xAA08101A;
    }

    private static int colCardBg() {
        return isEInk ? 0xF0F5F7F3 : 0xB40C1522;
    }

    private static int colOutCardBg() {
        return isEInk ? 0xF2FCE8E8 : 0xCC240B10;
    }

    private static int colOutCardBorder(long timeMs) {
        boolean blink = (timeMs / 420L) % 2L == 0L;
        return isEInk ? (blink ? 0xEEB91C1C : 0xAA991B1B) : (blink ? 0xFFFF3B3B : 0xBBCC2233);
    }

    private static int colOutText() {
        return isEInk ? 0xFFB91C1C : 0xFFFF4D4D;
    }

    private static int colPinAccent() {
        return isEInk ? 0xFFB45309 : 0xFFFFCC22;
    }

    private static int colFlowColor(int rateHr) {
        if (rateHr >= 0) {
            return isEInk ? 0xFF15803D : 0xFF22FF88;
        }
        return isEInk ? 0xFFC2410C : 0xFFFF6644;
    }

    private static String getFlowLabel(int rateHr) {
        if (activeFlowMode <= 0 || rateHr == 0) return "";
        return LcdScreenBlockEntity.formatFlowRateText(rateHr, activeFlowMode, true, false);
    }

    private static String getCountOrOutLabel(DisplayEntry e, boolean allowLongOutText) {
        if (e.count <= 0 && e.pinned) {
            if (allowLongOutText) {
                return "⚠ " + Component.translatable("gui.signbuilder.lcd.storage.out_of_stock").getString();
            }
            return "⚠ x0";
        }
        return "x" + LcdScreenBlockEntity.formatCompactCount(e.count);
    }

    private static String getDisplayNameWithPin(DisplayEntry e) {
        if (e.pinned && activeFilterMode > 0) {
            return "★ " + e.name;
        }
        return e.name;
    }

    private static int colPanelBg() {
        return isEInk ? 0xEAE0E5DE : 0xAA060B12;
    }

    private static int colWellBg() {
        return isEInk ? 0xDDD4DAD2 : 0x66122234;
    }

    private static int colTrackBg() {
        return isEInk ? 0xCCC4CCD4 : 0x88081018;
    }

    private static int colMutedBorder() {
        return isEInk ? 0x99788796 : 0x77263B52;
    }

    private static int colPrimaryText() {
        return isEInk ? 0xFF1E293B : 0xFFE8F2FC;
    }

    private static int colCountText() {
        return isEInk ? 0xFFB45309 : 0xFFFFCC44;
    }

    private static int colSecondaryText() {
        return isEInk ? 0xFF475569 : 0xFF94ACC4;
    }

    private static int colPillOverlayBg() {
        return isEInk ? 0xDF1E293B : 0xB8060A10;
    }

    private static long getSlideIntervalMs(int speed) {
        return switch (speed) {
            case 1 -> 2800L;
            case 3 -> 1250L;
            case 4 -> 800L;
            default -> 1900L;
        };
    }

    private static void renderSingleSpotlightSlide(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                                   LcdScreenBlockEntity master,
                                                   float x, float y, float w, float h,
                                                   int pct, int statusRgb, int accentRgb,
                                                   int speed, int fontStyle, long timeMs) {
        int fullStatus = 0xFF000000 | statusRgb;
        int fullAccent = 0xFF000000 | (accentRgb & 0x00FFFFFF);

        int entryCount = ACTIVE_ITEMS.size();
        int curIdx = (entryCount > 1) ? (int) ((timeMs / getSlideIntervalMs(speed)) % entryCount) : 0;
        DisplayEntry curEntry = (entryCount > 0) ? ACTIVE_ITEMS.get(curIdx) : null;
        boolean isOut = curEntry != null && curEntry.pinned && curEntry.count <= 0;

        fillRect(vc, mat, light, x + 0.8f, y + 0.8f, w - 1.6f, h - 1.6f, isOut ? colOutCardBg() : colCardBg());
        drawCornerBrackets(vc, mat, light, x + 1.2f, y + 1.2f, w - 2.4f, h - 2.4f,
                Math.min(4.2f, w * 0.14f), 0.7f, isOut ? colOutCardBorder(timeMs) : fullAccent);

        String pctStr = pct + "%";
        float topSc = 0.46f;
        queueText(pctStr, x + 2.6f, y + 2.2f, topSc, fullStatus, true);

        if (curEntry != null) {
            String flowStr = getFlowLabel(curEntry.rateHr);
            if (!flowStr.isEmpty()) {
                float fSc = fitScale(font, flowStr, fontStyle, w * 0.56f, 0.38f, 0.28f);
                float fw = LcdFontHelper.getWidth(font, flowStr, fontStyle) * fSc;
                queueText(flowStr, x + w - fw - 2.4f, y + 2.5f, fSc, colFlowColor(curEntry.rateHr), false);
            } else if (entryCount > 1) {
                String idxStr = (curIdx + 1) + "/" + entryCount;
                float iw = LcdFontHelper.getWidth(font, idxStr, fontStyle) * 0.40f;
                queueText(idxStr, x + w - iw - 2.6f, y + 2.5f, 0.40f, colSecondaryText(), false);
            } else {
                int linkedCnt = master.getLinkedStorageCount();
                if (linkedCnt > 1) {
                    String cBadge = "×" + linkedCnt;
                    float bw = LcdFontHelper.getWidth(font, cBadge, fontStyle) * 0.40f;
                    queueText(cBadge, x + w - bw - 2.6f, y + 2.5f, 0.40f, colSecondaryText(), false);
                }
            }

            float iconSz = Math.max(10.0f, Math.min(w, h) * 0.47f);
            if (!curEntry.stack.isEmpty()) {
                queueItem(curEntry.stack, x + w * 0.5f, y + h * 0.44f, iconSz);
            }

            String cntStr = getCountOrOutLabel(curEntry, false);
            float cScale = fitScale(font, cntStr, fontStyle, w - 5.0f, 0.72f, 0.36f);
            float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cScale;
            queueText(cntStr, x + (w - cw) * 0.5f, y + h - font.lineHeight * cScale - 4.4f,
                    cScale, isOut ? colOutText() : colCountText(), true);
        } else {
            String emptyKey = activeFilterMode == 2 ? "gui.signbuilder.lcd.storage.no_pinned" : "gui.signbuilder.lcd.storage.empty";
            String emptyTxt = Component.translatable(emptyKey).getString();
            float eScale = fitScale(font, emptyTxt, fontStyle, w - 6.0f, 0.56f, 0.30f);
            float ew = LcdFontHelper.getWidth(font, emptyTxt, fontStyle) * eScale;
            queueText(emptyTxt, x + (w - ew) * 0.5f, y + (h - font.lineHeight * eScale) * 0.5f, eScale, colSecondaryText(), false);
        }

        float barW = w - 5.0f;
        float barY = y + h - 2.8f;
        drawSegmentedBar(vc, mat, light, x + 2.5f, barY, barW, 1.6f, 10, pct, statusRgb);
    }

    private static void renderDualSpotlightSlide(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                                 LcdScreenBlockEntity master,
                                                 float x, float y, float w, float h, boolean horizontal,
                                                 int pct, int statusRgb, int accentRgb,
                                                 int speed, int fontStyle, long timeMs) {
        int entryCount = ACTIVE_ITEMS.size();
        int totalPages = Math.max(1, (entryCount + 1) / 2);
        int curPage = (totalPages > 1) ? (int) ((timeMs / getSlideIntervalMs(speed)) % totalPages) : 0;

        float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, curPage, totalPages, fontStyle);

        float bx = x + 2.0f;
        float by = y + topUsed;
        float bw = w - 4.0f;
        float bh = h - topUsed - 1.8f;

        if (entryCount <= 0) {
            renderEmptyBadge(font, vc, mat, light, bx, by, bw, bh, accentRgb, fontStyle);
            return;
        }

        if (entryCount == 1) {
            renderSingleHeroCard(font, vc, mat, light, ACTIVE_ITEMS.get(0), bx, by, bw, bh, statusRgb, accentRgb, fontStyle, timeMs);
            return;
        }

        int idx1 = curPage * 2;
        int idx2 = idx1 + 1;
        float gap = 2.0f;

        if (horizontal) {
            float cw = (bw - gap) * 0.5f;
            drawSpotlightPairCard(font, vc, mat, light, ACTIVE_ITEMS.get(idx1), bx, by, cw, bh, statusRgb, accentRgb, fontStyle, timeMs);
            if (idx2 < entryCount) {
                drawSpotlightPairCard(font, vc, mat, light, ACTIVE_ITEMS.get(idx2), bx + cw + gap, by, cw, bh, statusRgb, accentRgb, fontStyle, timeMs);
            }
        } else {
            float ch = (bh - gap) * 0.5f;
            drawSpotlightPairCard(font, vc, mat, light, ACTIVE_ITEMS.get(idx1), bx, by, bw, ch, statusRgb, accentRgb, fontStyle, timeMs);
            if (idx2 < entryCount) {
                drawSpotlightPairCard(font, vc, mat, light, ACTIVE_ITEMS.get(idx2), bx, by + ch + gap, bw, ch, statusRgb, accentRgb, fontStyle, timeMs);
            }
        }
    }

    private static void drawSpotlightPairCard(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                              DisplayEntry entry,
                                              float x, float y, float w, float h,
                                              int statusRgb, int accentRgb, int fontStyle, long timeMs) {
        boolean isOut = entry.pinned && entry.count <= 0;
        int borderCol = isOut
                ? colOutCardBorder(timeMs)
                : (entry.pinned && activeFilterMode > 0 ? colPinAccent() : ((isEInk ? 0xBB000000 : 0xAA000000) | (accentRgb & 0x00FFFFFF)));

        fillRect(vc, mat, light, x, y, w, h, isOut ? colOutCardBg() : colCardBg());
        drawBorder(vc, mat, light, x, y, w, h, isOut ? 0.7f : 0.55f, borderCol);

        String cntStr = getCountOrOutLabel(entry, w >= 52.0f);
        String flowStr = getFlowLabel(entry.rateHr);
        int cntCol = isOut ? colOutText() : colCountText();

        if (w >= h * 1.15f) {
            float iconSz = Math.max(9.0f, Math.min(h - 4.5f, w * 0.40f));
            float icx = x + 2.5f + iconSz * 0.5f;
            float icy = y + (h - 2.0f) * 0.5f;
            if (!entry.stack.isEmpty()) {
                queueItem(entry.stack, icx, icy, iconSz);
            }
            float tx = icx + iconSz * 0.5f + 2.5f;
            float availW = (x + w - 2.2f) - tx;
            float nSc = Math.min(0.52f, Math.max(0.32f, (h * 0.25f) / font.lineHeight));
            String trimName = trimToWidth(font, getDisplayNameWithPin(entry), fontStyle, availW / nSc);
            queueText(trimName, tx, y + h * 0.16f, nSc, colPrimaryText(), false);

            float cSc = fitScale(font, cntStr, fontStyle, availW, 0.64f, 0.34f);
            queueText(cntStr, tx, y + h * 0.48f, cSc, cntCol, true);
            if (!flowStr.isEmpty()) {
                float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cSc;
                float fAvail = availW - cw - 2.5f;
                if (fAvail >= 12.0f) {
                    float fSc = fitScale(font, flowStr, fontStyle, fAvail, 0.44f, 0.28f);
                    queueText(flowStr, tx + cw + 2.5f, y + h * 0.52f, fSc, colFlowColor(entry.rateHr), false);
                }
            }
        } else {
            float iconSz = Math.max(9.0f, Math.min(w * 0.56f, h * 0.50f));
            if (!entry.stack.isEmpty()) {
                queueItem(entry.stack, x + w * 0.5f, y + h * 0.36f, iconSz);
            }
            float cSc = fitScale(font, cntStr, fontStyle, w - 4.0f, 0.60f, 0.32f);
            float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cSc;
            queueText(cntStr, x + (w - cw) * 0.5f, y + h - font.lineHeight * cSc - 3.2f, cSc, cntCol, true);
        }

        float barY = y + h - 1.8f;
        fillRect(vc, mat, light, x + 1.4f, barY, w - 2.8f, 1.1f, colTrackBg());
        if (!isOut) {
            float fw = Math.max(1.0f, (w - 2.8f) * (Math.min(100, entry.share) / 100.0f));
            fillRect(vc, mat, light, x + 1.4f, barY, fw, 1.1f, 0xFF000000 | statusRgb);
        }
    }

    private static int resolveStatusColor(int pct, int accentRgb, long timeMs) {
        if (pct >= 95) {
            float pulse = 0.65f + 0.35f * (float) Math.sin(timeMs * 0.008);
            if (isEInk) {
                int r = (int) (180 + 40 * pulse);
                return (r << 16) | (24 << 8) | 24;
            }
            int r = 255;
            int gr = (int) (36 + 44 * (1.0f - pulse));
            int b = (int) (48 + 32 * (1.0f - pulse));
            return (r << 16) | (gr << 8) | b;
        }
        if (pct >= 80) {
            return isEInk ? 0xC2410C : 0xFFAA18;
        }
        return accentRgb & 0x00FFFFFF;
    }

    private static long getPageIntervalMs(int speed) {
        return switch (speed) {
            case 1 -> 4400L;
            case 3 -> 2000L;
            case 4 -> 1250L;
            default -> 3000L;
        };
    }

    private static void renderUnlinkedState(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                            float x, float y, float w, float h, int fontStyle, long timeMs) {
        float pad = Math.min(4.0f, h * 0.08f);
        float bx = x + pad;
        float by = y + pad;
        float bw = w - pad * 2.0f;
        float bh = h - pad * 2.0f;

        boolean blink = (timeMs / 480L) % 2L == 0L;
        int borderCol = isEInk ? (blink ? 0xDDB91C1C : 0xAAC2410C) : (blink ? 0xDDFF4433 : 0xAAFF9900);
        int bgCol = isEInk ? 0xEEF5F1EC : 0x88140A0E;
        int bracketCol = isEInk ? 0xFF9A3412 : 0xFFFFAA22;
        int titleCol = isEInk ? (blink ? 0xFFB91C1C : 0xFFC2410C) : (blink ? 0xFFFF5544 : 0xFFFFBB33);
        int descCol = isEInk ? 0xFF475569 : 0xFFAEC0D4;

        fillRect(vc, mat, light, bx, by, bw, bh, bgCol);
        drawBorder(vc, mat, light, bx, by, bw, bh, 0.7f, borderCol);

        float cornerLen = Math.min(5.0f, Math.min(bw, bh) * 0.2f);
        drawCornerBrackets(vc, mat, light, bx + 1.2f, by + 1.2f, bw - 2.4f, bh - 2.4f, cornerLen, 0.8f, bracketCol);

        String t1 = "⚠ " + Component.translatable("gui.signbuilder.lcd.storage.no_linked").getString();
        String t2 = Component.translatable("gui.signbuilder.lcd.storage.no_linked_desc").getString();

        float s1 = fitScale(font, t1, fontStyle, bw - 6.0f, 0.85f, 0.42f);
        float w1 = LcdFontHelper.getWidth(font, t1, fontStyle) * s1;
        float cy = by + bh * 0.5f;

        if (bh >= 26.0f) {
            queueText(t1, bx + (bw - w1) * 0.5f, cy - font.lineHeight * s1 - 1.0f, s1, titleCol, true);
            float s2 = fitScale(font, t2, fontStyle, bw - 8.0f, 0.62f, 0.36f);
            float w2 = LcdFontHelper.getWidth(font, t2, fontStyle) * s2;
            queueText(t2, bx + (bw - w2) * 0.5f, cy + 2.0f, s2, descCol, false);
        } else {
            queueText(t1, bx + (bw - w1) * 0.5f, cy - (font.lineHeight * s1) * 0.5f, s1, titleCol, true);
        }
    }

    private static void renderEmptyBadge(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                         float x, float y, float w, float h, int accentRgb, int fontStyle) {
        String emptyKey = activeFilterMode == 2 ? "gui.signbuilder.lcd.storage.no_pinned" : "gui.signbuilder.lcd.storage.empty";
        String emptyTxt = Component.translatable(emptyKey).getString();
        float bw = Math.min(w - 6.0f, 112.0f);
        float bh = Math.min(h - 4.0f, 16.0f);
        float bx = x + (w - bw) * 0.5f;
        float by = y + (h - bh) * 0.5f;
        fillRect(vc, mat, light, bx, by, bw, bh, colCardBg());
        drawBorder(vc, mat, light, bx, by, bw, bh, 0.5f, ((isEInk ? 0x88000000 : 0x66000000) | (accentRgb & 0x00FFFFFF)));
        float sc = fitScale(font, emptyTxt, fontStyle, bw - 6.0f, 0.66f, 0.34f);
        float tw = LcdFontHelper.getWidth(font, emptyTxt, fontStyle) * sc;
        queueText(emptyTxt, bx + (bw - tw) * 0.5f, by + (bh - font.lineHeight * sc) * 0.5f, sc, colSecondaryText(), false);
    }

    private static float renderCompactTopHud(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                             LcdScreenBlockEntity master,
                                             float x, float y, float w, float h,
                                             int pct, int statusRgb, int accentRgb,
                                             int curPage, int totalPages, int fontStyle) {
        float headerH = Math.max(7.5f, Math.min(12.5f, h * 0.17f));
        int fullStatus = 0xFF000000 | statusRgb;

        fillRect(vc, mat, light, x + 1.0f, y + 1.0f, w - 2.0f, headerH, colHudBg());
        fillRect(vc, mat, light, x + 1.0f, y + 1.0f + headerH - 0.6f, w - 2.0f, 0.6f, (0x88000000 | statusRgb));

        String leftTxt = "▣ " + pct + "%";
        int linkedCnt = master.getLinkedStorageCount();
        if (linkedCnt > 1 && w >= 55.0f) {
            leftTxt += " [×" + linkedCnt + "]";
        }
        if (w >= 72.0f) {
            leftTxt += " • " + master.getStorageFilledSlots() + "/" + master.getStorageTotalSlots();
        }
        String netFlowStr = getFlowLabel(master.getStorageNetRatePerHour());
        if (netFlowStr.isEmpty() && w >= 125.0f) {
            leftTxt += " (" + LcdScreenBlockEntity.formatCompactCount(master.getStorageTotalItems()) + ")";
        }

        float sc = Math.min(0.66f, Math.max(0.36f, (headerH - 2.2f) / font.lineHeight));
        float textY = y + 1.0f + (headerH - font.lineHeight * sc) * 0.5f;
        queueText(leftTxt, x + 3.0f, textY, sc, fullStatus, true);
        float leftW = LcdFontHelper.getWidth(font, leftTxt, fontStyle) * sc;

        if (!netFlowStr.isEmpty() && w >= 56.0f) {
            float flowW = LcdFontHelper.getWidth(font, netFlowStr, fontStyle) * sc;
            if (leftW + flowW + 10.0f <= w - 14.0f) {
                queueText(netFlowStr, x + 5.5f + leftW, textY, sc, colFlowColor(master.getStorageNetRatePerHour()), true);
                leftW += flowW + 3.5f;
            }
        }

        float rightReserved = 2.0f;
        if (totalPages > 1) {
            String pgStr = (curPage + 1) + "/" + totalPages;
            float pgW = LcdFontHelper.getWidth(font, pgStr, fontStyle) * sc;
            queueText(pgStr, x + w - pgW - 3.0f, textY, sc, colCountText(), false);
            rightReserved = pgW + 5.0f;

            int maxDots = Math.min(totalPages, 6);
            if (w - leftW - rightReserved > 28.0f) {
                float dotSize = 1.8f;
                float dotGap = 1.4f;
                float dotsW = maxDots * dotSize + (maxDots - 1) * dotGap;
                float dx = x + w - rightReserved - dotsW - 2.0f;
                float dy = y + 1.0f + (headerH - dotSize) * 0.5f;
                int activeDot = curPage % maxDots;
                int dotOff = isEInk ? 0x8894A3B8 : 0x66344B63;
                for (int d = 0; d < maxDots; d++) {
                    fillRect(vc, mat, light, dx + d * (dotSize + dotGap), dy, dotSize, dotSize,
                            d == activeDot ? fullStatus : dotOff);
                }
                rightReserved += dotsW + 4.0f;
            }
        }

        float barX = x + 5.0f + leftW;
        float barW = (x + w - rightReserved - 2.0f) - barX;
        if (barW >= 14.0f) {
            float barH = Math.max(2.6f, headerH * 0.36f);
            float barY = y + 1.0f + (headerH - barH) * 0.5f;
            int segs = Math.max(6, Math.min(24, (int) (barW / 3.4f)));
            drawSegmentedBar(vc, mat, light, barX, barY, barW, barH, segs, pct, statusRgb);
        }

        return headerH + 2.4f;
    }

    private static void renderLiveChestGridStyle(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                                 LcdScreenBlockEntity master,
                                                 float x, float y, float w, float h,
                                                 int pct, int statusRgb, int accentRgb,
                                                 int speed, int fontStyle, long timeMs) {
        if (activeFilterMode == 2 && ACTIVE_ITEMS.isEmpty()) {
            float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, 0, 1, fontStyle);
            renderEmptyBadge(font, vc, mat, light, x + 2.0f, y + topUsed, w - 4.0f, h - topUsed - 2.0f, accentRgb, fontStyle);
            return;
        }

        int rawTotalSlots = Math.max(1, master.getStorageTotalSlots());
        boolean watchlistGridMode = (activeFilterMode == 2);
        boolean aggregatedSlotMode = watchlistGridMode || rawTotalSlots > LcdScreenBlockEntity.MAX_SYNCED_SLOTS;
        int totalCells = watchlistGridMode
                ? Math.max(9, ACTIVE_ITEMS.size())
                : (aggregatedSlotMode ? Math.max(27, ACTIVE_ITEMS.size()) : rawTotalSlots);

        int cols;
        if (!aggregatedSlotMode && totalCells == 5) {
            cols = 5;
        } else if (!aggregatedSlotMode && totalCells == 9) {
            cols = 3;
        } else if (!aggregatedSlotMode && totalCells > 54 && totalCells % 12 == 0) {
            cols = 12;
        } else {
            cols = Math.min(9, totalCells);
        }
        int totalRows = Math.max(1, (totalCells + cols - 1) / cols);

        float estHeader = Math.max(7.5f, Math.min(12.5f, h * 0.17f)) + 2.4f;
        float availW = w - 4.0f;
        float availH = h - estHeader - 2.0f;

        float testGap = 0.9f;
        float cellIfAll = Math.min((availW - (cols - 1) * testGap) / cols, (availH - (totalRows - 1) * testGap) / totalRows);

        int rowsPerPage = totalRows;
        if (totalRows > 6 || (totalRows > 3 && cellIfAll < 8.5f)) {
            float cellIf6 = Math.min((availW - (cols - 1) * testGap) / cols, (availH - 5 * testGap) / 6.0f);
            rowsPerPage = (cellIf6 >= 8.5f && totalRows >= 6) ? 6 : 3;
        }
        int totalPages = Math.max(1, (totalRows + rowsPerPage - 1) / rowsPerPage);
        int curPage = (totalPages > 1) ? (int) ((timeMs / getPageIntervalMs(speed)) % totalPages) : 0;

        float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, curPage, totalPages, fontStyle);

        float gxArea = x + 2.0f;
        float gyArea = y + topUsed;
        float gwArea = w - 4.0f;
        float ghArea = h - topUsed - 1.8f;
        if (gwArea <= 4.0f || ghArea <= 4.0f) return;

        int startRow = curPage * rowsPerPage;
        int visRows = Math.min(rowsPerPage, totalRows - startRow);
        int layoutRows = (totalPages > 1) ? rowsPerPage : visRows;

        float gap = Math.max(0.6f, Math.min(1.6f, Math.min(gwArea / (cols * 10.0f), ghArea / (layoutRows * 10.0f))));
        float cellW = (gwArea - (cols - 1) * gap) / cols;
        float cellH = (ghArea - (layoutRows - 1) * gap) / layoutRows;
        float cell = Math.min(cellW, cellH);

        float totalGridW = cols * cell + (cols - 1) * gap;
        float visGridH = visRows * cell + (visRows - 1) * gap;
        float fullPageGridH = layoutRows * cell + (layoutRows - 1) * gap;
        float startX = gxArea + (gwArea - totalGridW) * 0.5f;
        float startY = gyArea + (ghArea - fullPageGridH) * 0.5f;

        fillRect(vc, mat, light, startX - 1.2f, startY - 1.2f, totalGridW + 2.4f, visGridH + 2.4f, colPanelBg());
        drawBorder(vc, mat, light, startX - 1.2f, startY - 1.2f, totalGridW + 2.4f, visGridH + 2.4f, 0.5f, ((isEInk ? 0x99000000 : 0x77000000) | (accentRgb & 0x00FFFFFF)));

        for (int r = 0; r < visRows; r++) {
            int actualRow = startRow + r;
            for (int c = 0; c < cols; c++) {
                int slotIdx = actualRow * cols + c;
                if (slotIdx >= totalCells) break;

                float cx = startX + c * (cell + gap);
                float cy = startY + r * (cell + gap);

                int fillPct;
                ItemStack slotStack;
                int slotCount;
                boolean isPinnedOut = false;
                boolean isPinnedCell = false;

                if (aggregatedSlotMode) {
                    if (slotIdx < ACTIVE_ITEMS.size()) {
                        DisplayEntry de = ACTIVE_ITEMS.get(slotIdx);
                        slotStack = de.stack;
                        slotCount = de.count;
                        isPinnedCell = de.pinned;
                        isPinnedOut = de.pinned && de.count <= 0;
                        fillPct = isPinnedOut ? 0 : Math.max(20, de.share);
                    } else {
                        slotStack = ItemStack.EMPTY;
                        slotCount = 0;
                        fillPct = 0;
                    }
                } else {
                    fillPct = master.getStorageSlotFill(slotIdx);
                    slotStack = master.getStorageSlotStack(slotIdx);
                    slotCount = master.getStorageSlotCountVal(slotIdx);
                    if (activeFilterMode == 1 && !slotStack.isEmpty()) {
                        for (DisplayEntry de : ACTIVE_ITEMS) {
                            if (!de.pinned) break;
                            if (LcdScreenBlockEntity.matchesTargetItem(slotStack, de.stack)) {
                                isPinnedCell = true;
                                break;
                            }
                        }
                    }
                }

                int cellBg;
                int borderCol;
                if (isPinnedOut) {
                    cellBg = colOutCardBg();
                    borderCol = colOutCardBorder(timeMs);
                } else if (slotCount <= 0 && fillPct <= 0) {
                    cellBg = isEInk ? 0xCCD8DED6 : 0xBB0C141E;
                    borderCol = isEInk ? 0x7794A3B8 : 0x55223448;
                } else if (isPinnedCell && activeFilterMode > 0) {
                    cellBg = isEInk ? 0xF2FAF3DC : 0xCC221B0C;
                    borderCol = colPinAccent();
                } else if (fillPct >= 100) {
                    cellBg = isEInk ? 0xE6DCF2E6 : 0xCC102624;
                    borderCol = isEInk ? 0xCC15803D : 0xCC00FF99;
                } else if (fillPct >= 75) {
                    cellBg = isEInk ? 0xE6F7EBD6 : 0xCC241D10;
                    borderCol = isEInk ? 0xCCC2410C : 0xCCFFAA22;
                } else {
                    cellBg = isEInk ? 0xF0F5F7F3 : 0xCC0E1E2C;
                    borderCol = ((isEInk ? 0xBB000000 : 0xAA000000) | (accentRgb & 0x00FFFFFF));
                }

                fillRect(vc, mat, light, cx, cy, cell, cell, cellBg);
                if (cell >= 5.0f) {
                    drawBorder(vc, mat, light, cx, cy, cell, cell, isPinnedOut ? 0.65f : 0.45f, borderCol);
                }

                if (!slotStack.isEmpty()) {
                    float iconSz = Math.max(3.2f, cell * 0.78f);
                    queueItem(slotStack, cx + cell * 0.5f, cy + cell * 0.48f, iconSz);

                    if (isPinnedOut && cell >= 6.0f) {
                        String cStr = "x0";
                        float cScale = Math.min(0.52f, Math.max(0.24f, (cell * 0.34f) / font.lineHeight));
                        float cw = LcdFontHelper.getWidth(font, cStr, fontStyle) * cScale;
                        queueTextBg(cStr, cx + cell - cw - 0.5f, cy + cell - font.lineHeight * cScale - 0.3f, cScale, 0xFFFF5555, false, colPillOverlayBg());
                    } else if (slotCount > 1 && cell >= 6.2f) {
                        String cStr = (slotCount >= 1000) ? LcdScreenBlockEntity.formatCompactCount(slotCount) : String.valueOf(slotCount);
                        float cScale = Math.min(0.52f, Math.max(0.24f, (cell * 0.34f) / font.lineHeight));
                        float cw = LcdFontHelper.getWidth(font, cStr, fontStyle) * cScale;
                        float tx = cx + cell - cw - 0.5f;
                        float ty = cy + cell - font.lineHeight * cScale - 0.3f;
                        queueTextBg(cStr, tx, ty, cScale, 0xFFFFFFFF, false, colPillOverlayBg());
                    }
                }
            }
        }
    }

    private static void renderShowcaseCardsStyle(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                                 LcdScreenBlockEntity master,
                                                 float x, float y, float w, float h,
                                                 int pct, int statusRgb, int accentRgb,
                                                 int speed, int fontStyle, long timeMs) {
        int entryCount = ACTIVE_ITEMS.size();
        if (entryCount <= 0) {
            float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, 0, 1, fontStyle);
            renderEmptyBadge(font, vc, mat, light, x + 2.0f, y + topUsed, w - 4.0f, h - topUsed - 2.0f, accentRgb, fontStyle);
            return;
        }

        float estHeader = Math.max(7.5f, Math.min(12.5f, h * 0.17f)) + 2.4f;
        float bodyW = w - 4.0f;
        float bodyH = h - estHeader - 2.0f;

        if (entryCount == 1) {
            float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, 0, 1, fontStyle);
            renderSingleHeroCard(font, vc, mat, light, ACTIVE_ITEMS.get(0), x + 2.0f, y + topUsed, w - 4.0f, h - topUsed - 2.0f, statusRgb, accentRgb, fontStyle, timeMs);
            return;
        }

        int maxCols = Math.max(1, Math.min(5, (int) (bodyW / 28.0f)));
        int maxRows = Math.max(1, Math.min(5, (int) (bodyH / 13.5f)));

        int cols = Math.min(maxCols, entryCount);
        int rows = Math.min(maxRows, (entryCount + cols - 1) / cols);
        while (cols * rows < entryCount && cols < maxCols) {
            cols++;
            rows = Math.min(maxRows, (entryCount + cols - 1) / cols);
        }

        int perPage = Math.max(1, cols * rows);
        int totalPages = (entryCount + perPage - 1) / perPage;
        int curPage = (totalPages > 1) ? (int) ((timeMs / getPageIntervalMs(speed)) % totalPages) : 0;

        float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, curPage, totalPages, fontStyle);

        float gx = x + 2.0f;
        float gy = y + topUsed;
        float gw = w - 4.0f;
        float gh = h - topUsed - 1.8f;

        int pageStart = curPage * perPage;
        int pageItems = Math.min(perPage, entryCount - pageStart);

        float gap = 1.6f;
        float cardW = (gw - (cols - 1) * gap) / cols;
        float cardH = Math.min(21.0f, (gh - (rows - 1) * gap) / rows);

        for (int idx = 0; idx < pageItems; idx++) {
            int itemIdx = pageStart + idx;
            int r = idx / cols;
            int c = idx % cols;
            float cx = gx + c * (cardW + gap);
            float cy = gy + r * (cardH + gap);
            drawItemCard(font, vc, mat, light, ACTIVE_ITEMS.get(itemIdx), itemIdx, cx, cy, cardW, cardH, statusRgb, accentRgb, fontStyle, timeMs);
        }
    }

    private static void renderSingleHeroCard(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                             DisplayEntry entry,
                                             float x, float y, float w, float h,
                                             int statusRgb, int accentRgb, int fontStyle, long timeMs) {
        float ch = Math.min(h, 36.0f);
        boolean isOut = entry.pinned && entry.count <= 0;
        String cntStr = getCountOrOutLabel(entry, w >= 56.0f);
        String flowStr = getFlowLabel(entry.rateHr);
        int fullAccent = isOut ? colOutCardBorder(timeMs) : (0xFF000000 | (accentRgb & 0x00FFFFFF));
        int cntCol = isOut ? colOutText() : colCountText();

        fillRect(vc, mat, light, x, y, w, ch, isOut ? colOutCardBg() : colCardBg());
        drawBorder(vc, mat, light, x, y, w, ch, 0.6f, isOut ? fullAccent : ((isEInk ? 0xAA000000 : 0x88000000) | (accentRgb & 0x00FFFFFF)));
        drawCornerBrackets(vc, mat, light, x + 1.0f, y + 1.0f, w - 2.0f, ch - 2.0f, Math.min(4.5f, ch * 0.22f), 0.7f, fullAccent);

        float iconSz = Math.max(8.0f, Math.min(ch * 0.68f, Math.min(w * 0.34f, 24.0f)));
        if (w >= ch * 1.25f) {
            float icx = x + 4.0f + iconSz * 0.55f;
            float icy = y + ch * 0.5f;
            fillRect(vc, mat, light, icx - iconSz * 0.54f, icy - iconSz * 0.54f, iconSz * 1.08f, iconSz * 1.08f, colWellBg());
            if (!entry.stack.isEmpty()) queueItem(entry.stack, icx, icy, iconSz);

            float tx = icx + iconSz * 0.62f + 3.0f;
            float availW = (x + w - 4.0f) - tx;
            float nScale = Math.min(0.76f, Math.max(0.40f, (ch * 0.26f) / font.lineHeight));
            String trimName = trimToWidth(font, getDisplayNameWithPin(entry), fontStyle, availW / nScale);
            queueText(trimName, tx, y + ch * 0.20f, nScale, colPrimaryText(), false);

            float cScale = fitScale(font, cntStr, fontStyle, availW, Math.min(0.92f, (ch * 0.34f) / font.lineHeight), 0.38f);
            queueText(cntStr, tx, y + ch * 0.54f, cScale, cntCol, true);
            if (!flowStr.isEmpty()) {
                float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cScale;
                float fAvail = availW - cw - 3.5f;
                if (fAvail >= 16.0f) {
                    float fScale = fitScale(font, flowStr, fontStyle, fAvail, Math.min(0.64f, cScale * 0.85f), 0.34f);
                    queueText(flowStr, tx + cw + 3.5f, y + ch * 0.58f, fScale, colFlowColor(entry.rateHr), true);
                }
            }
        } else {
            float icx = x + w * 0.5f;
            float icy = y + ch * 0.38f;
            if (!entry.stack.isEmpty()) queueItem(entry.stack, icx, icy, iconSz);
            float cScale = fitScale(font, cntStr, fontStyle, w - 6.0f, 0.72f, 0.36f);
            float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cScale;
            queueText(cntStr, x + (w - cw) * 0.5f, y + ch - font.lineHeight * cScale - 2.0f, cScale, cntCol, true);
        }
    }

    private static void drawItemCard(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                     DisplayEntry entry, int index,
                                     float x, float y, float w, float h,
                                     int statusRgb, int accentRgb, int fontStyle, long timeMs) {
        boolean isOut = entry.pinned && entry.count <= 0;
        int borderCol;
        if (isOut) {
            borderCol = colOutCardBorder(timeMs);
        } else if (entry.pinned && activeFilterMode > 0) {
            borderCol = colPinAccent();
        } else if (index == 0) {
            borderCol = 0xBB000000 | (accentRgb & 0x00FFFFFF);
        } else {
            borderCol = colMutedBorder();
        }

        fillRect(vc, mat, light, x, y, w, h, isOut ? colOutCardBg() : colCardBg());
        drawBorder(vc, mat, light, x, y, w, h, isOut ? 0.65f : 0.5f, borderCol);

        if (w < 26.0f || h < 11.5f) {
            String cntStr = getCountOrOutLabel(entry, false);
            float iconSz = Math.max(4.5f, Math.min(h * 0.72f, w * 0.55f));
            if (!entry.stack.isEmpty()) {
                queueItem(entry.stack, x + w * 0.5f, y + h * 0.42f, iconSz);
            }
            float cs = fitScale(font, cntStr, fontStyle, w - 2.0f, 0.44f, 0.26f);
            float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cs;
            queueTextBg(cntStr, x + w - cw - 1.0f, y + h - font.lineHeight * cs - 0.5f, cs, isOut ? 0xFFFF6666 : 0xFFFFDD66, false, colPillOverlayBg());
            return;
        }

        float iconSz = Math.max(6.0f, Math.min(h - 4.0f, Math.min(w * 0.32f, 14.5f)));
        float icx = x + 2.2f + iconSz * 0.5f;
        float icy = y + (h - 2.0f) * 0.5f;
        fillRect(vc, mat, light, icx - iconSz * 0.5f, icy - iconSz * 0.5f, iconSz, iconSz, colWellBg());
        if (!entry.stack.isEmpty()) {
            queueItem(entry.stack, icx, icy, iconSz * 0.90f);
        }

        float tx = icx + iconSz * 0.5f + 2.2f;
        float availW = (x + w - 2.0f) - tx;
        String cntStr = getCountOrOutLabel(entry, availW >= 26.0f);
        String flowStr = getFlowLabel(entry.rateHr);
        int cntCol = isOut ? colOutText() : colCountText();

        if (availW >= 8.0f) {
            if (h >= 12.8f) {
                float nScale = Math.min(0.54f, Math.max(0.30f, (h * 0.28f) / font.lineHeight));
                String trimName = trimToWidth(font, getDisplayNameWithPin(entry), fontStyle, availW / nScale);
                queueText(trimName, tx, y + h * 0.14f, nScale, colPrimaryText(), false);

                float cScale = fitScale(font, cntStr, fontStyle, availW, Math.min(0.58f, Math.max(0.32f, (h * 0.30f) / font.lineHeight)), 0.28f);
                queueText(cntStr, tx, y + h * 0.49f, cScale, cntCol, false);

                if (!flowStr.isEmpty()) {
                    float cw = LcdFontHelper.getWidth(font, cntStr, fontStyle) * cScale;
                    float remW = availW - cw - 2.2f;
                    if (remW >= 12.0f) {
                        float fScale = fitScale(font, flowStr, fontStyle, remW, Math.min(0.46f, cScale * 0.90f), 0.26f);
                        float fw = LcdFontHelper.getWidth(font, flowStr, fontStyle) * fScale;
                        queueText(flowStr, x + w - fw - 1.8f, y + h * 0.51f, fScale, colFlowColor(entry.rateHr), false);
                    }
                }
            } else {
                float cScale = fitScale(font, cntStr, fontStyle, availW, 0.54f, 0.28f);
                queueText(cntStr, tx, y + (h - 2.0f - font.lineHeight * cScale) * 0.5f, cScale, cntCol, false);
            }
        }

        float barY = y + h - 1.7f;
        float barW = w - 2.4f;
        fillRect(vc, mat, light, x + 1.2f, barY, barW, 1.1f, colTrackBg());
        if (!isOut) {
            float fillW = Math.max(1.0f, barW * (Math.min(100, entry.share) / 100.0f));
            fillRect(vc, mat, light, x + 1.2f, barY, fillW, 1.1f, 0xFF000000 | statusRgb);
        }
    }

    private static void renderManifestTableStyle(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                                 LcdScreenBlockEntity master,
                                                 float x, float y, float w, float h,
                                                 int pct, int statusRgb, int accentRgb,
                                                 int speed, int fontStyle, long timeMs) {
        int entryCount = ACTIVE_ITEMS.size();
        float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, 0, 1, fontStyle);

        float lx = x + 2.0f;
        float ly = y + topUsed;
        float lw = w - 4.0f;
        float lh = h - topUsed - 1.8f;
        if (lw <= 6.0f || lh <= 6.0f) return;

        if (entryCount <= 0) {
            renderEmptyBadge(font, vc, mat, light, lx, ly, lw, lh, accentRgb, fontStyle);
            return;
        }

        fillRect(vc, mat, light, lx, ly, lw, lh, colPanelBg());
        drawBorder(vc, mat, light, lx, ly, lw, lh, 0.5f, colMutedBorder());

        float targetRowH = Math.max(8.5f, Math.min(12.5f, lh / 4.0f));
        int fitRows = Math.max(2, (int) (lh / targetRowH));
        boolean needsScroll = entryCount > fitRows;

        float savedMinY = clipMinY;
        float savedMaxY = clipMaxY;
        clipMinY = Math.max(savedMinY, ly + 0.6f);
        clipMaxY = Math.min(savedMaxY, ly + lh - 0.6f);

        if (!needsScroll) {
            float rowH = Math.min(13.5f, (lh - 1.2f) / Math.max(1, entryCount));
            for (int i = 0; i < entryCount; i++) {
                float ry = ly + 0.6f + i * rowH;
                drawManifestRow(font, vc, mat, light, ACTIVE_ITEMS.get(i), i, lx + 0.8f, ry, lw - 1.6f, rowH - 0.6f,
                        ly + 0.6f, ly + lh - 0.6f, statusRgb, accentRgb, fontStyle, timeMs);
            }
        } else {
            float rowStep = targetRowH;
            float speedPxPerSec = switch (speed) {
                case 1 -> 8.0f;
                case 3 -> 22.0f;
                case 4 -> 36.0f;
                default -> 14.0f;
            };
            float loopH = entryCount * rowStep;
            float scrollY = (float) (((timeMs / 1000.0) * speedPxPerSec) % loopH);
            int firstIdx = (int) Math.floor(scrollY / rowStep);
            float subOffset = scrollY - (firstIdx * rowStep);

            int drawCount = (int) Math.ceil(lh / rowStep) + 2;
            for (int k = 0; k < drawCount; k++) {
                int itemIdx = (firstIdx + k) % entryCount;
                float ry = ly + 0.6f + k * rowStep - subOffset;
                if (ry + rowStep < ly + 0.6f || ry > ly + lh - 0.6f) continue;

                drawManifestRow(font, vc, mat, light, ACTIVE_ITEMS.get(itemIdx), itemIdx, lx + 0.8f, ry, lw - 1.6f, rowStep - 0.7f,
                        ly + 0.6f, ly + lh - 0.6f, statusRgb, accentRgb, fontStyle, timeMs);
            }
        }

        clipMinY = savedMinY;
        clipMaxY = savedMaxY;
    }

    private static void drawManifestRow(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                        DisplayEntry entry, int idx,
                                        float rx, float ry, float rw, float rh,
                                        float visTop, float visBot,
                                        int statusRgb, int accentRgb, int fontStyle, long timeMs) {
        boolean isOut = entry.pinned && entry.count <= 0;
        int bgCol = isOut
                ? colOutCardBg()
                : (isEInk ? ((idx % 2 == 0) ? 0xF0F5F7F3 : 0xEAE6EAE3) : ((idx % 2 == 0) ? 0xAA0D1724 : 0xAA0A121C));
        int stripeCol;
        if (isOut) {
            stripeCol = colOutCardBorder(timeMs);
        } else if (entry.pinned && activeFilterMode > 0) {
            stripeCol = colPinAccent();
        } else if (idx == 0) {
            stripeCol = 0xFF000000 | (accentRgb & 0x00FFFFFF);
        } else {
            stripeCol = isEInk ? 0x99788796 : 0x882E4966;
        }
        fillRect(vc, mat, light, rx, ry, rw, rh, bgCol);
        fillRect(vc, mat, light, rx, ry, 1.3f, rh, stripeCol);

        float cy = ry + rh * 0.5f;
        float iconSz = Math.max(5.0f, Math.min(rh - 1.4f, 10.5f));
        if (cy - iconSz * 0.42f < visTop || cy + iconSz * 0.42f > visBot) {
            return;
        }

        float curX = rx + 2.6f;
        float tScale = Math.min(0.56f, Math.max(0.32f, (rh - 2.0f) / font.lineHeight));
        float ty = ry + (rh - font.lineHeight * tScale) * 0.5f;

        if (rw >= 72.0f) {
            String rank = String.format(java.util.Locale.ROOT, "%02d", idx + 1);
            queueText(rank, curX, ty, tScale * 0.90f, colSecondaryText(), false);
            curX += LcdFontHelper.getWidth(font, rank, fontStyle) * tScale * 0.90f + 2.2f;
        }

        if (!entry.stack.isEmpty()) {
            queueItem(entry.stack, curX + iconSz * 0.5f, cy, iconSz);
        }
        curX += iconSz + 2.4f;

        String cntStr = getCountOrOutLabel(entry, rw >= 90.0f);
        float cntW = LcdFontHelper.getWidth(font, cntStr, fontStyle) * tScale;
        float cntX = rx + rw - cntW - 2.2f;
        queueText(cntStr, cntX, ty, tScale, isOut ? colOutText() : colCountText(), false);

        float shareRight = cntX - 2.5f;
        String flowStr = getFlowLabel(entry.rateHr);
        if (!flowStr.isEmpty() && rw >= 80.0f) {
            float fScale = tScale * 0.92f;
            float fw = LcdFontHelper.getWidth(font, flowStr, fontStyle) * fScale;
            if (shareRight - fw - curX >= 18.0f) {
                float fx = shareRight - fw;
                queueText(flowStr, fx, ty, fScale, colFlowColor(entry.rateHr), false);
                shareRight = fx - 2.5f;
            }
        }

        if (!isOut && rw >= 105.0f) {
            float barW = Math.min(26.0f, rw * 0.18f);
            if (shareRight - barW - curX >= 20.0f) {
                float barX = shareRight - barW;
                float barH = Math.max(2.0f, rh * 0.26f);
                float barY = ry + (rh - barH) * 0.5f;
                fillRect(vc, mat, light, barX, barY, barW, barH, colTrackBg());
                float fw = Math.max(1.0f, barW * (Math.min(100, entry.share) / 100.0f));
                fillRect(vc, mat, light, barX, barY, fw, barH, 0xFF000000 | statusRgb);
                shareRight = barX - 2.5f;
            }
        }

        float nameAvail = shareRight - curX;
        if (nameAvail >= 8.0f) {
            String name = trimToWidth(font, getDisplayNameWithPin(entry), fontStyle, nameAvail / tScale);
            queueText(name, curX, ty, tScale, colPrimaryText(), false);
        }
    }

    private static void renderLiveTickerStyle(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                              LcdScreenBlockEntity master,
                                              float x, float y, float w, float h,
                                              int pct, int statusRgb, int accentRgb,
                                              int speed, int fontStyle, long timeMs) {
        int entryCount = ACTIVE_ITEMS.size();
        float topUsed = renderCompactTopHud(font, vc, mat, light, master, x, y, w, h, pct, statusRgb, accentRgb, 0, 1, fontStyle);

        float footerH = Math.max(3.5f, Math.min(6.5f, h * 0.11f));
        float bodyY = y + topUsed;
        float bodyH = h - topUsed - footerH - 2.2f;

        if (entryCount <= 0) {
            renderEmptyBadge(font, vc, mat, light, x + 2.0f, bodyY, w - 4.0f, bodyH, accentRgb, fontStyle);
        } else {
            boolean dualLane = (bodyH >= 30.0f && entryCount >= 4);
            if (!dualLane) {
                List<Integer> allIndices = new ArrayList<>(entryCount);
                for (int i = 0; i < entryCount; i++) allIndices.add(i);
                renderConveyorBeltLane(font, vc, mat, light, allIndices, x + 1.5f, bodyY + bodyH * 0.08f, w - 3.0f, bodyH * 0.84f,
                        accentRgb, speed, false, fontStyle, timeMs);
            } else {
                List<Integer> laneA = new ArrayList<>();
                List<Integer> laneB = new ArrayList<>();
                for (int i = 0; i < entryCount; i++) {
                    if (i % 2 == 0) laneA.add(i);
                    else laneB.add(i);
                }
                float laneH = (bodyH - 2.0f) * 0.5f;
                renderConveyorBeltLane(font, vc, mat, light, laneA, x + 1.5f, bodyY, w - 3.0f, laneH,
                        accentRgb, speed, false, fontStyle, timeMs);
                renderConveyorBeltLane(font, vc, mat, light, laneB, x + 1.5f, bodyY + laneH + 2.0f, w - 3.0f, laneH,
                        accentRgb, speed, true, fontStyle, timeMs);
            }
        }

        int segs = Math.max(8, Math.min(30, (int) ((w - 4.0f) / 4.0f)));
        drawSegmentedBar(vc, mat, light, x + 2.0f, y + h - footerH - 1.0f, w - 4.0f, footerH, segs, pct, statusRgb);
    }

    private static void renderConveyorBeltLane(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                               List<Integer> indices,
                                               float tx, float ty, float tw, float th,
                                               int accentRgb, int speed, boolean reverse, int fontStyle, long timeMs) {
        if (indices.isEmpty() || tw <= 6.0f || th <= 6.0f) return;

        int beltBg = isEInk ? 0xE2DCE1DA : 0x8808111B;
        int railCol = isEInk ? 0x99788796 : 0x7726425E;
        fillRect(vc, mat, light, tx, ty, tw, th, beltBg);
        fillRect(vc, mat, light, tx, ty, tw, 0.5f, railCol);
        fillRect(vc, mat, light, tx, ty + th - 0.5f, tw, 0.5f, railCol);

        float savedMinX = clipMinX;
        float savedMaxX = clipMaxX;
        clipMinX = Math.max(savedMinX, tx + 0.5f);
        clipMaxX = Math.min(savedMaxX, tx + tw - 0.5f);

        float capH = Math.max(7.5f, th - 2.4f);
        float capY = ty + (th - capH) * 0.5f;
        float iconSz = Math.max(5.5f, Math.min(capH - 1.8f, 13.5f));
        float tScale = Math.min(0.62f, Math.max(0.34f, (capH * 0.42f) / font.lineHeight));
        float gap = 5.0f;

        int n = indices.size();
        float[] widths = new float[n];
        String[] labels = new String[n];
        String[] counts = new String[n];
        String[] flows = new String[n];
        float loopLen = 0.0f;

        for (int k = 0; k < n; k++) {
            DisplayEntry entry = ACTIVE_ITEMS.get(indices.get(k));
            String nm = trimToWidth(font, getDisplayNameWithPin(entry), fontStyle, 68.0f);
            String ct = getCountOrOutLabel(entry, true);
            String fl = getFlowLabel(entry.rateHr);
            labels[k] = nm;
            counts[k] = ct;
            flows[k] = fl;
            int rawTextW = LcdFontHelper.getWidth(font, nm, fontStyle) + LcdFontHelper.getWidth(font, ct, fontStyle) + 6;
            if (!fl.isEmpty()) {
                rawTextW += LcdFontHelper.getWidth(font, fl, fontStyle) + 5;
            }
            widths[k] = 3.0f + iconSz + 2.5f + rawTextW * tScale + 3.5f;
            loopLen += widths[k] + gap;
        }

        if (loopLen < 12.0f) loopLen = 12.0f;

        float pxPerSec = switch (speed) {
            case 1 -> 15.0f;
            case 3 -> 42.0f;
            case 4 -> 68.0f;
            default -> 26.0f;
        };
        float rawOffset = (float) (((timeMs / 1000.0) * pxPerSec) % loopLen);
        float startCursor = reverse ? (tx - loopLen + rawOffset) : (tx - rawOffset);

        int normalCapBg = isEInk ? 0xF2F6F8F4 : 0xCC0E1A2A;
        int normalCapBorder = ((isEInk ? 0xBB000000 : 0x99000000) | (accentRgb & 0x00FFFFFF));

        int repeats = Math.max(2, (int) Math.ceil((tw + loopLen) / loopLen) + 1);
        for (int rep = 0; rep < repeats; rep++) {
            float curX = startCursor + rep * loopLen;
            for (int k = 0; k < n; k++) {
                float cw = widths[k];
                if (curX + cw > tx + 0.5f && curX < tx + tw - 0.5f) {
                    DisplayEntry entry = ACTIVE_ITEMS.get(indices.get(k));
                    boolean isOut = entry.pinned && entry.count <= 0;
                    int capBg = isOut ? colOutCardBg() : normalCapBg;
                    int capBorder = isOut ? colOutCardBorder(timeMs) : (entry.pinned && activeFilterMode > 0 ? colPinAccent() : normalCapBorder);

                    fillRect(vc, mat, light, curX, capY, cw, capH, capBg);
                    drawBorder(vc, mat, light, curX, capY, cw, capH, isOut ? 0.65f : 0.5f, capBorder);

                    float icx = curX + 2.2f + iconSz * 0.5f;
                    float icy = capY + capH * 0.5f;
                    if (!entry.stack.isEmpty() && icx - iconSz * 0.45f >= tx && icx + iconSz * 0.45f <= tx + tw) {
                        queueItem(entry.stack, icx, icy, iconSz);
                    }

                    float lx = curX + 2.2f + iconSz + 2.2f;
                    float ly = capY + (capH - font.lineHeight * tScale) * 0.5f;
                    float nmW = LcdFontHelper.getWidth(font, labels[k], fontStyle) * tScale;
                    if (lx >= tx + 1.0f && lx + nmW <= tx + tw - 1.0f) {
                        queueText(labels[k], lx, ly, tScale, colPrimaryText(), false);
                    }

                    float cntX = lx + nmW + 3.0f * tScale;
                    float cntW = LcdFontHelper.getWidth(font, counts[k], fontStyle) * tScale;
                    if (cntX >= tx + 1.0f && cntX + cntW <= tx + tw - 1.0f) {
                        queueText(counts[k], cntX, ly, tScale, isOut ? colOutText() : colCountText(), false);
                    }

                    if (!flows[k].isEmpty()) {
                        float flX = cntX + cntW + 3.0f * tScale;
                        float flW = LcdFontHelper.getWidth(font, flows[k], fontStyle) * tScale;
                        if (flX >= tx + 1.0f && flX + flW <= tx + tw - 1.0f) {
                            queueText(flows[k], flX, ly, tScale, colFlowColor(entry.rateHr), false);
                        }
                    }
                }
                curX += cw + gap;
            }
        }

        clipMinX = savedMinX;
        clipMaxX = savedMaxX;
    }

    private static void renderCyberRingStyle(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                             LcdScreenBlockEntity master,
                                             float x, float y, float w, float h,
                                             int pct, int statusRgb, int accentRgb,
                                             int speed, int fontStyle, long timeMs) {
        int entryCount = ACTIVE_ITEMS.size();
        boolean wide = w >= h * 1.25f && w >= 62.0f;

        float ringCx;
        float ringCy;
        float outerR;

        if (wide) {
            float leftW = Math.min(h * 1.02f, w * 0.44f);
            ringCx = x + leftW * 0.5f + 1.5f;
            ringCy = y + h * 0.5f;
            outerR = Math.min(leftW, h) * 0.43f;
        } else {
            ringCx = x + w * 0.5f;
            ringCy = y + (entryCount > 0 ? h * 0.40f : h * 0.5f);
            outerR = Math.min(w, h * (entryCount > 0 ? 0.74f : 0.90f)) * 0.42f;
        }

        float innerR = outerR * 0.72f;
        int segments = 24;
        float startDeg = 135.0f;
        float sweepDeg = 270.0f;
        int litSegs = Math.round((pct / 100.0f) * segments);
        if (litSegs == 0 && master.getStorageTotalItems() > 0) litSegs = 1;
        int offSegCol = isEInk ? 0x88BCC5CE : 0x551B2A3A;

        for (int s = 0; s < segments; s++) {
            float a0 = (float) Math.toRadians(startDeg + (s * sweepDeg / segments) + 1.4f);
            float a1 = (float) Math.toRadians(startDeg + ((s + 1) * sweepDeg / segments) - 1.4f);
            int segCol = (s < litSegs) ? (0xFF000000 | statusRgb) : offSegCol;
            float x1 = ringCx + (float) Math.cos(a0) * innerR;
            float y1 = ringCy + (float) Math.sin(a0) * innerR;
            float x2 = ringCx + (float) Math.cos(a0) * outerR;
            float y2 = ringCy + (float) Math.sin(a0) * outerR;
            float x3 = ringCx + (float) Math.cos(a1) * outerR;
            float y3 = ringCy + (float) Math.sin(a1) * outerR;
            float x4 = ringCx + (float) Math.cos(a1) * innerR;
            float y4 = ringCy + (float) Math.sin(a1) * innerR;
            fillQuad(vc, mat, light, x1, y1, x2, y2, x3, y3, x4, y4, segCol);
        }

        float orbitAngle = (float) ((timeMs % 3600L) / 3600.0 * Math.PI * 2.0);
        float pipR = (innerR + outerR) * 0.5f;
        float px = ringCx + (float) Math.cos(orbitAngle) * pipR;
        float py = ringCy + (float) Math.sin(orbitAngle) * pipR;
        fillRect(vc, mat, light, px - 0.9f, py - 0.9f, 1.8f, 1.8f, isEInk ? 0xDD1E293B : 0xCCFFFFFF);

        String pctStr = pct + "%";
        float pScale = fitScale(font, pctStr, fontStyle, innerR * 1.55f, 0.85f, 0.36f);
        float pw = LcdFontHelper.getWidth(font, pctStr, fontStyle) * pScale;
        queueText(pctStr, ringCx - pw * 0.5f, ringCy - font.lineHeight * pScale * 0.60f, pScale, 0xFF000000 | statusRgb, true);

        String netFlow = getFlowLabel(master.getStorageNetRatePerHour());
        String subRingStr = !netFlow.isEmpty() ? netFlow : (master.getStorageFilledSlots() + "/" + master.getStorageTotalSlots());
        int subRingCol = !netFlow.isEmpty() ? colFlowColor(master.getStorageNetRatePerHour()) : colSecondaryText();
        float sScale = fitScale(font, subRingStr, fontStyle, innerR * 1.55f, 0.45f, 0.26f);
        float sw = LcdFontHelper.getWidth(font, subRingStr, fontStyle) * sScale;
        queueText(subRingStr, ringCx - sw * 0.5f, ringCy + font.lineHeight * pScale * 0.38f, sScale, subRingCol, false);

        if (wide) {
            float rx = ringCx + outerR + 4.5f;
            float rw = (x + w - 2.5f) - rx;
            float ry = y + 2.5f;
            float rh = h - 5.0f;

            if (entryCount <= 0) {
                renderEmptyBadge(font, vc, mat, light, rx, ry, rw, rh, accentRgb, fontStyle);
                return;
            }

            float topBarH = 7.5f;
            int rowsPerPage = Math.max(2, Math.min(5, (int) ((rh - topBarH) / 11.5f)));
            int totalPages = (entryCount + rowsPerPage - 1) / rowsPerPage;
            int curPage = (totalPages > 1) ? (int) ((timeMs / getPageIntervalMs(speed)) % totalPages) : 0;

            String deckHdr = (curPage * rowsPerPage + 1) + "-" + Math.min(entryCount, (curPage + 1) * rowsPerPage) + " / " + entryCount;
            float hSc = 0.44f;
            queueText(deckHdr, rx + 1.0f, ry + 0.8f, hSc, colSecondaryText(), false);
            if (totalPages > 1) {
                String pBadge = (curPage + 1) + "/" + totalPages;
                float pbW = LcdFontHelper.getWidth(font, pBadge, fontStyle) * hSc;
                queueText(pBadge, rx + rw - pbW - 1.0f, ry + 0.8f, hSc, colCountText(), false);
            }

            int startIdx = curPage * rowsPerPage;
            int visCount = Math.min(rowsPerPage, entryCount - startIdx);
            float cardsY = ry + topBarH;
            float cardsH = rh - topBarH;
            float gap = 1.4f;
            float cardH = Math.min(21.0f, (cardsH - (rowsPerPage - 1) * gap) / rowsPerPage);

            for (int i = 0; i < visCount; i++) {
                int idx = startIdx + i;
                drawItemCard(font, vc, mat, light, ACTIVE_ITEMS.get(idx), idx, rx, cardsY + i * (cardH + gap), rw, cardH, statusRgb, accentRgb, fontStyle, timeMs);
            }
        } else if (entryCount > 0) {
            int curIdx = (int) ((timeMs / (getPageIntervalMs(speed) / 2L + 400L)) % entryCount);
            float cy = y + h * 0.77f;
            float ch = Math.max(8.5f, h * 0.20f);
            float cw = w - 5.0f;
            float cx = x + 2.5f;
            drawItemCard(font, vc, mat, light, ACTIVE_ITEMS.get(curIdx), curIdx, cx, cy, cw, ch, statusRgb, accentRgb, fontStyle, timeMs);
        }
    }

    private static void renderLiquidSiloStyle(Font font, VertexConsumer vc, Matrix4f mat, int light,
                                              LcdScreenBlockEntity master,
                                              float x, float y, float w, float h,
                                              int pct, int statusRgb, int accentRgb,
                                              int speed, int fontStyle, long timeMs) {
        int entryCount = ACTIVE_ITEMS.size();
        boolean splitRight = w >= h * 1.15f && w >= 60.0f;

        float tankX = x + 2.5f;
        float tankY = y + 2.5f;
        float tankW = splitRight ? Math.max(22.0f, Math.min(w * 0.34f, 44.0f)) : (w - 5.0f);
        float tankH = h - 5.0f;

        fillRect(vc, mat, light, tankX, tankY, tankW, tankH, isEInk ? 0xEADCE2DC : 0xBB070E17);
        drawBorder(vc, mat, light, tankX, tankY, tankW, tankH, 0.7f, isEInk ? 0xCC334155 : 0xAA36526E);

        float innerX = tankX + 1.4f;
        float innerY = tankY + 1.4f;
        float innerW = tankW - 2.8f;
        float innerH = tankH - 2.8f;

        if (pct > 0 && innerW > 2.0f && innerH > 2.0f) {
            float baseFillH = innerH * (pct / 100.0f);
            int cols = Math.max(6, Math.min(20, (int) (innerW / 2.2f)));
            float colW = innerW / cols;
            double phase = timeMs * 0.0045;
            int liqBody = ((isEInk ? 0xC8000000 : 0xAA000000) | statusRgb);
            int liqCrest = (0xEE000000 | blendWhite(statusRgb, isEInk ? 0.25f : 0.35f));

            for (int c = 0; c < cols; c++) {
                float wave = (pct >= 100) ? 0.0f : (float) (Math.sin(phase + c * 0.55) * Math.min(1.6f, innerH * 0.04f));
                float hCol = Math.max(1.0f, Math.min(innerH, baseFillH + wave));
                float sx = innerX + c * colW;
                float sy = innerY + innerH - hCol;
                fillRect(vc, mat, light, sx, sy, colW + 0.15f, hCol, liqBody);
                fillRect(vc, mat, light, sx, sy, colW + 0.15f, Math.min(1.1f, hCol), liqCrest);
            }

            for (int b = 0; b < 4; b++) {
                float bProg = (float) (((timeMs * 0.00045 + b * 0.27) % 1.0));
                float bx = innerX + innerW * (0.18f + 0.21f * b);
                float by = innerY + innerH - (bProg * Math.max(2.0f, baseFillH - 2.0f)) - 1.5f;
                if (by > innerY + innerH - baseFillH + 1.5f) {
                    fillRect(vc, mat, light, bx, by, 1.2f, 1.2f, 0x88FFFFFF);
                }
            }
        }

        int tickCol = isEInk ? 0xAA334155 : 0x8898B4D0;
        for (int tick = 1; tick <= 3; tick++) {
            float ty = innerY + innerH * (1.0f - tick * 0.25f);
            fillRect(vc, mat, light, innerX, ty, Math.min(3.5f, innerW * 0.22f), 0.5f, tickCol);
        }

        String pctStr = pct + "%";
        float pScale = fitScale(font, pctStr, fontStyle, innerW - 2.0f, 0.72f, 0.36f);
        float pw = LcdFontHelper.getWidth(font, pctStr, fontStyle) * pScale;
        if (isEInk) {
            queueTextBg(pctStr, tankX + (tankW - pw) * 0.5f, tankY + 2.5f, pScale, 0xFF1E293B, false, 0xDCF5F7F3);
        } else {
            queueTextBg(pctStr, tankX + (tankW - pw) * 0.5f, tankY + 2.5f, pScale, 0xFFFFFFFF, true, 0x88060B12);
        }

        if (splitRight) {
            float rx = tankX + tankW + 3.0f;
            float rw = (x + w - 2.5f) - rx;
            float ry = y + 2.5f;
            float rh = h - 5.0f;

            if (entryCount <= 0) {
                renderEmptyBadge(font, vc, mat, light, rx, ry, rw, rh, accentRgb, fontStyle);
                return;
            }

            float hdrH = 7.5f;
            int cols = Math.max(1, Math.min(3, (int) (rw / 30.0f)));
            int rows = Math.max(2, Math.min(4, (int) ((rh - hdrH) / 12.5f)));
            int perPage = cols * rows;
            int totalPages = (entryCount + perPage - 1) / perPage;
            int curPage = (totalPages > 1) ? (int) ((timeMs / getPageIntervalMs(speed)) % totalPages) : 0;

            String netFlow = getFlowLabel(master.getStorageNetRatePerHour());
            String info = master.getStorageFilledSlots() + "/" + master.getStorageTotalSlots() + " • " + entryCount;
            float hSc = 0.44f;
            queueText(info, rx + 1.0f, ry + 0.8f, hSc, colSecondaryText(), false);
            if (!netFlow.isEmpty()) {
                float iw = LcdFontHelper.getWidth(font, info, fontStyle) * hSc;
                float fw = LcdFontHelper.getWidth(font, netFlow, fontStyle) * hSc;
                if (iw + fw + 8.0f < rw - 18.0f) {
                    queueText(netFlow, rx + 3.5f + iw, ry + 0.8f, hSc, colFlowColor(master.getStorageNetRatePerHour()), false);
                }
            }
            if (totalPages > 1) {
                String pg = (curPage + 1) + "/" + totalPages;
                float pgW = LcdFontHelper.getWidth(font, pg, fontStyle) * hSc;
                queueText(pg, rx + rw - pgW - 1.0f, ry + 0.8f, hSc, colCountText(), false);
            }

            int pageStart = curPage * perPage;
            int pageItems = Math.min(perPage, entryCount - pageStart);
            float gap = 1.4f;
            float cw = (rw - (cols - 1) * gap) / cols;
            float ch = Math.min(21.0f, (rh - hdrH - (rows - 1) * gap) / rows);

            for (int i = 0; i < pageItems; i++) {
                int r = i / cols;
                int c = i % cols;
                int idx = pageStart + i;
                drawItemCard(font, vc, mat, light, ACTIVE_ITEMS.get(idx), idx,
                        rx + c * (cw + gap), ry + hdrH + r * (ch + gap), cw, ch, statusRgb, accentRgb, fontStyle, timeMs);
            }
        } else if (entryCount > 0) {
            int curIdx = (int) ((timeMs / (getPageIntervalMs(speed) / 2L + 400L)) % entryCount);
            DisplayEntry entry = ACTIVE_ITEMS.get(curIdx);
            float iconSz = Math.max(7.0f, Math.min(innerW * 0.50f, innerH * 0.36f));
            if (!entry.stack.isEmpty()) {
                queueItem(entry.stack, tankX + tankW * 0.5f, tankY + tankH * 0.54f, iconSz);
            }
            String cnt = getCountOrOutLabel(entry, false);
            if (entryCount > 1) {
                cnt += " (" + (curIdx + 1) + "/" + entryCount + ")";
            }
            float cs = fitScale(font, cnt, fontStyle, innerW - 2.0f, 0.48f, 0.28f);
            float cw = LcdFontHelper.getWidth(font, cnt, fontStyle) * cs;
            boolean isOut = entry.pinned && entry.count <= 0;
            queueTextBg(cnt, tankX + (tankW - cw) * 0.5f, tankY + tankH - font.lineHeight * cs - 2.2f, cs,
                    isOut ? 0xFFFF6666 : 0xFFFFDD66, false, colPillOverlayBg());
        }
    }

    private static int blendWhite(int rgb, float ratio) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = Math.min(255, (int) (r + (255 - r) * ratio));
        g = Math.min(255, (int) (g + (255 - g) * ratio));
        b = Math.min(255, (int) (b + (255 - b) * ratio));
        return (r << 16) | (g << 8) | b;
    }

    private static void drawSegmentedBar(VertexConsumer vc, Matrix4f mat, int light,
                                         float x, float y, float w, float h, int segs, int pct, int statusRgb) {
        float gap = 0.8f;
        float segW = (w - (segs - 1) * gap) / segs;
        int offCol = isEInk ? 0x88BCC5CE : 0x55182838;
        if (segW <= 0.4f) {
            fillRect(vc, mat, light, x, y, w, h, offCol);
            fillRect(vc, mat, light, x, y, w * (pct / 100.0f), h, 0xFF000000 | statusRgb);
            return;
        }
        int lit = Math.round((pct / 100.0f) * segs);
        if (lit == 0 && pct > 0) lit = 1;
        for (int i = 0; i < segs; i++) {
            float sx = x + i * (segW + gap);
            int col = (i < lit) ? (0xFF000000 | statusRgb) : offCol;
            fillRect(vc, mat, light, sx, y, segW, h, col);
        }
    }

    private static void drawCornerBrackets(VertexConsumer vc, Matrix4f mat, int light,
                                           float x, float y, float w, float h, float len, float thick, int argb) {
        fillRect(vc, mat, light, x, y, len, thick, argb);
        fillRect(vc, mat, light, x, y, thick, len, argb);
        fillRect(vc, mat, light, x + w - len, y, len, thick, argb);
        fillRect(vc, mat, light, x + w - thick, y, thick, len, argb);
        fillRect(vc, mat, light, x, y + h - thick, len, thick, argb);
        fillRect(vc, mat, light, x, y + h - len, thick, len, argb);
        fillRect(vc, mat, light, x + w - len, y + h - thick, len, thick, argb);
        fillRect(vc, mat, light, x + w - thick, y + h - len, thick, len, argb);
    }

    private static float fitScale(Font font, String text, int fontStyle, float maxW, float maxScale, float minScale) {
        int rawW = Math.max(1, LcdFontHelper.getWidth(font, text, fontStyle));
        float ideal = maxW / (float) rawW;
        return Math.max(minScale, Math.min(maxScale, ideal));
    }

    private static String trimToWidth(Font font, String text, int fontStyle, float maxUnscaledWidth) {
        if (text == null || text.isEmpty()) return "";
        if (LcdFontHelper.getWidth(font, text, fontStyle) <= maxUnscaledWidth) return text;
        int dotsW = LcdFontHelper.getWidth(font, "..", fontStyle);
        if (maxUnscaledWidth <= dotsW + 2) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int cc = Character.charCount(cp);
            String next = sb.toString() + text.substring(i, i + cc);
            if (LcdFontHelper.getWidth(font, next, fontStyle) + dotsW > maxUnscaledWidth) {
                break;
            }
            sb.appendCodePoint(cp);
            i += cc;
        }
        return sb.length() > 0 ? sb.append("..").toString() : "";
    }

    private static void queueItem(ItemStack stack, float cx, float cy, float size) {
        ICON_QUEUE.add(new IconCmd(stack, cx, cy, size));
    }

    private static void queueText(String text, float x, float y, float scale, int color, boolean shadow) {
        TEXT_QUEUE.add(new TextCmd(text, x, y, scale, color, shadow, 0));
    }

    private static void queueTextBg(String text, float x, float y, float scale, int color, boolean shadow, int bgColor) {
        TEXT_QUEUE.add(new TextCmd(text, x, y, scale, color, shadow, bgColor));
    }

    private static void drawBorder(VertexConsumer vc, Matrix4f mat, int light,
                                   float x, float y, float w, float h, float t, int argb) {
        fillRect(vc, mat, light, x, y, w, t, argb);
        fillRect(vc, mat, light, x, y + h - t, w, t, argb);
        fillRect(vc, mat, x, y + t, t, Math.max(0.0f, h - t * 2.0f), argb, light);
        fillRect(vc, mat, x + w - t, y + t, t, Math.max(0.0f, h - t * 2.0f), argb, light);
    }

    private static void fillRect(VertexConsumer vc, Matrix4f mat, float x, float y, float w, float h, int argb, int light) {
        fillRect(vc, mat, light, x, y, w, h, argb);
    }

    private static void fillRect(VertexConsumer vc, Matrix4f mat, int light,
                                 float x, float y, float w, float h, int argb) {
        float x1 = Math.max(clipMinX, x);
        float y1 = Math.max(clipMinY, y);
        float x2 = Math.min(clipMaxX, x + w);
        float y2 = Math.min(clipMaxY, y + h);
        if (x2 <= x1 || y2 <= y1) return;

        if (vc != null && mat != null) {
            int a = (argb >>> 24) & 0xFF;
            int r = (argb >>> 16) & 0xFF;
            int gr = (argb >>> 8) & 0xFF;
            int b = argb & 0xFF;
            if (light < 0) {
                vc.addVertex(mat, x1, y2, 0.0f).setColor(r, gr, b, a);
                vc.addVertex(mat, x2, y2, 0.0f).setColor(r, gr, b, a);
                vc.addVertex(mat, x2, y1, 0.0f).setColor(r, gr, b, a);
                vc.addVertex(mat, x1, y1, 0.0f).setColor(r, gr, b, a);
            } else {
                curZ += 0.00003f;
                vc.addVertex(mat, x1, y2, curZ).setColor(r, gr, b, a).setLight(light);
                vc.addVertex(mat, x2, y2, curZ).setColor(r, gr, b, a).setLight(light);
                vc.addVertex(mat, x2, y1, curZ).setColor(r, gr, b, a).setLight(light);
                vc.addVertex(mat, x1, y1, curZ).setColor(r, gr, b, a).setLight(light);
            }
        }
    }

    private static void fillQuad(VertexConsumer vc, Matrix4f mat, int light,
                                 float x1, float y1, float x2, float y2,
                                 float x3, float y3, float x4, float y4, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int gr = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        if (vc != null && mat != null) {
            if (light < 0) {
                vc.addVertex(mat, x1, y1, 0.0f).setColor(r, gr, b, a);
                vc.addVertex(mat, x4, y4, 0.0f).setColor(r, gr, b, a);
                vc.addVertex(mat, x3, y3, 0.0f).setColor(r, gr, b, a);
                vc.addVertex(mat, x2, y2, 0.0f).setColor(r, gr, b, a);
            } else {
                curZ += 0.00003f;
                vc.addVertex(mat, x1, y1, curZ).setColor(r, gr, b, a).setLight(light);
                vc.addVertex(mat, x4, y4, curZ).setColor(r, gr, b, a).setLight(light);
                vc.addVertex(mat, x3, y3, curZ).setColor(r, gr, b, a).setLight(light);
                vc.addVertex(mat, x2, y2, curZ).setColor(r, gr, b, a).setLight(light);
            }
        }
    }
}
