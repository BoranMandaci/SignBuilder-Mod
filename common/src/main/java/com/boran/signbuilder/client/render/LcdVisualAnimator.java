package com.boran.signbuilder.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

import java.awt.Color;

public class LcdVisualAnimator {

    private static float clipX1;
    private static float clipY1;
    private static float clipX2;
    private static float clipY2;
    private static float currentZ;

    public static void render3D(VertexConsumer vc, Matrix4f mat, int light,
                                float minX, float maxX, float topY, float bottomY,
                                int visualAnim, int speed, int baseColor, boolean rainbow, long timeMs, boolean lowDetail) {
        renderInternal(vc, mat, light, null, minX, maxX, topY, bottomY, visualAnim, speed, baseColor, rainbow, timeMs, lowDetail);
    }

    public static void render2D(GuiGraphics g, float minX, float maxX, float topY, float bottomY,
                                int visualAnim, int speed, int baseColor, boolean rainbow, long timeMs) {
        renderInternal(null, null, 0, g, minX, maxX, topY, bottomY, visualAnim, speed, baseColor, rainbow, timeMs, false);
    }

    private static void renderInternal(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float minX, float maxX, float topY, float bottomY,
                                       int visualAnim, int speed, int baseColor, boolean rainbow, long timeMs, boolean lowDetail) {
        float pad = Math.max(1.5f, (bottomY - topY) * 0.08f);
        float x1 = minX + pad;
        float x2 = maxX - pad;
        float y1 = topY + pad;
        float y2 = bottomY - pad;
        float w = x2 - x1;
        float h = y2 - y1;
        if (w <= 2.0f || h <= 2.0f) return;

        currentZ = 0.001f;
        clipX1 = (visualAnim == 12) ? minX : x1;
        clipY1 = (visualAnim == 12) ? topY : y1;
        clipX2 = (visualAnim == 12) ? maxX : x2;
        clipY2 = (visualAnim == 12) ? bottomY : y2;

        double speedMult = switch (speed) {
            case 1 -> 0.55;
            case 3 -> 1.65;
            case 4 -> 2.50;
            default -> 1.00;
        };
        double t = (timeMs % 3600000L) * 0.001 * speedMult;

        switch (visualAnim) {
            case 1 -> renderEqualizer(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 2 -> renderMatrixRain(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 3 -> renderHeartbeat(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 4 -> renderPixelFire(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 5 -> renderRadar(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 6 -> renderBattery(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 7 -> renderCreeper(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 8 -> renderSiren(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 9 -> renderPacman(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 10 -> renderDigitalClock(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 11 -> renderStarfield(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 12 -> renderBouncingLogo(vc, mat, light, g, minX, topY, maxX - minX, bottomY - topY, baseColor, rainbow, t);
            case 13 -> renderSlime(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 14 -> renderPong(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 15 -> renderRainstorm(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 16 -> renderDnaHelix(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 17 -> renderSpaceInvaders(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 18 -> renderCassetteTape(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 19 -> renderReactorSpinner(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 20 -> renderUfo(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 21 -> renderDinoRunner(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 22 -> renderSynthwaveHighway(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 23 -> renderTetris(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 24 -> renderFlappyBird(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
        }
    }

    private static void renderEqualizer(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int bars = Math.max(10, Math.min(lowDetail ? 14 : 28, Math.round(w / Math.max(2.2f, h * 0.14f))));
        int segments = lowDetail ? 5 : 8;
        float stepX = w / bars;
        float barW = Math.max(1.0f, stepX * 0.72f);
        float segStepY = h / segments;
        float segH = Math.max(0.8f, segStepY * 0.74f);

        for (int i = 0; i < bars; i++) {
            double wave = 0.48
                    + 0.26 * Math.sin(t * 4.5 + i * 0.62)
                    + 0.16 * Math.cos(t * 7.9 - i * 1.15)
                    + 0.10 * Math.sin(t * 12.3 + i * 2.05);
            int activeSegs = Math.max(1, Math.min(segments, (int) Math.round(wave * segments)));

            int colBase = rainbow ? getRainbowColor(t * 0.35 + i * (1.0 / bars)) : baseColor;
            float bx = x + i * stepX + (stepX - barW) * 0.5f;

            for (int s = 0; s < activeSegs; s++) {
                float sy = y + h - (s + 1) * segStepY + (segStepY - segH) * 0.5f;
                int segColor = colBase;
                if (!rainbow) {
                    if (s == segments - 1) segColor = 0xFF3355;
                    else if (s >= segments - 2) segColor = blendColor(colBase, 0xFFEE44, 0.65f);
                }
                q(vc, mat, light, g, bx, sy, bx + barW, sy + segH, segColor, 245);
            }

            double peakWave = Math.max(wave, 0.55 + 0.35 * Math.sin(t * 2.2 + i * 0.62));
            int peakSeg = Math.min(segments - 1, (int) Math.floor(peakWave * segments));
            if (peakSeg >= activeSegs) {
                float py = y + h - (peakSeg + 1) * segStepY;
                q(vc, mat, light, g, bx, py, bx + barW, py + Math.max(0.7f, segH * 0.45f), 0xFFFFFF, 220);
            }
        }
    }

    private static void renderMatrixRain(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int cols = Math.max(12, Math.min(lowDetail ? 16 : 32, Math.round(w / Math.max(2.0f, h * 0.12f))));
        int tailLen = lowDetail ? 4 : 6;
        float colW = w / cols;
        float dropW = Math.max(0.9f, colW * 0.58f);
        float cellH = h / 8.5f;

        for (int c = 0; c < cols; c++) {
            int hash = (c * 73856093) ^ 0x5DEECE66;
            double colSpeed = 0.75 + ((hash & 0xFF) / 255.0) * 0.85;
            double phase = ((hash >> 8) & 0xFF) / 255.0;
            double cycle = (t * colSpeed + phase * 4.0) % 1.45;

            int headCell = (int) Math.floor(cycle * 11.0);
            int colColor = rainbow ? getRainbowColor(t * 0.25 + c * 0.05) : baseColor;
            int headColor = blendColor(colColor, 0xFFFFFF, 0.75f);
            float cx = x + c * colW + (colW - dropW) * 0.5f;

            for (int k = 0; k < tailLen; k++) {
                int cellY = headCell - k;
                if (cellY < 0 || cellY > 8) continue;
                float dy = y + cellY * cellH;
                if (dy + cellH * 0.8f > y + h) continue;

                int cRgb = (k == 0) ? headColor : colColor;
                int alpha = (k == 0) ? 255 : Math.max(35, 225 - k * 38);
                q(vc, mat, light, g, cx, dy, cx + dropW, dy + cellH * 0.78f, cRgb, alpha);
            }
        }
    }

    private static void renderHeartbeat(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int col = rainbow ? getRainbowColor(t * 0.3) : baseColor;

        float midY = y + h * 0.52f;
        float gridThick = Math.max(0.45f, h * 0.025f);
        q(vc, mat, light, g, x, midY - gridThick * 0.5f, x + w, midY + gridThick * 0.5f, col, 45);
        q(vc, mat, light, g, x + w * 0.25f, y, x + w * 0.25f + gridThick, y + h, col, 35);
        q(vc, mat, light, g, x + w * 0.50f, y, x + w * 0.50f + gridThick, y + h, col, 35);
        q(vc, mat, light, g, x + w * 0.75f, y, x + w * 0.75f + gridThick, y + h, col, 35);

        int steps = lowDetail ? 24 : 44;
        float stepW = w / steps;
        double sweepPos = (t * 0.55) % 1.0;
        float dotH = Math.max(1.2f, h * 0.10f);

        float prevY = midY;
        for (int i = 0; i < steps; i++) {
            double normX = (double) i / steps;
            double beatPhase = ((normX * (w > h * 2.2f ? 2.0 : 1.2)) % 1.0);
            float offset = ekgWaveform(beatPhase) * (h * 0.42f);
            float curY = midY - offset;

            if (i == 0) prevY = curY;

            double distBehind = sweepPos - normX;
            if (distBehind < 0) distBehind += 1.0;
            int alpha = (int) Math.max(40, 255 * (1.0 - distBehind * 0.82));

            float sx = x + i * stepW;
            float topSeg = Math.min(prevY, curY) - dotH * 0.45f;
            float botSeg = Math.max(prevY, curY) + dotH * 0.45f;
            q(vc, mat, light, g, sx, topSeg, sx + stepW * 0.92f, botSeg, col, alpha);

            if (distBehind < (1.5 / steps)) {
                q(vc, mat, light, g, sx - dotH * 0.3f, curY - dotH * 0.8f, sx + stepW + dotH * 0.3f, curY + dotH * 0.8f, 0xFFFFFF, 255);
            }
            prevY = curY;
        }

        boolean beatNow = ((t * 1.8) % 1.0) < 0.28;
        float hs = Math.max(1.0f, h * (beatNow ? 0.065f : 0.052f));
        float hx = x + w - hs * 7.5f;
        float hy = y + hs * 1.2f;
        int heartCol = rainbow ? col : 0xFF2255;
        q(vc, mat, light, g, hx + hs, hy, hx + hs * 2.5f, hy + hs, heartCol, 245);
        q(vc, mat, light, g, hx + hs * 3.5f, hy, hx + hs * 5.0f, hy + hs, heartCol, 245);
        q(vc, mat, light, g, hx, hy + hs, hx + hs * 6.0f, hy + hs * 2.6f, heartCol, 245);
        q(vc, mat, light, g, hx + hs, hy + hs * 2.6f, hx + hs * 5.0f, hy + hs * 3.8f, heartCol, 245);
        q(vc, mat, light, g, hx + hs * 2.2f, hy + hs * 3.8f, hx + hs * 3.8f, hy + hs * 4.8f, heartCol, 245);
    }

    private static float ekgWaveform(double p) {
        if (p > 0.20 && p < 0.28) {
            return (float) (0.22 * Math.sin((p - 0.20) / 0.08 * Math.PI));
        } else if (p >= 0.32 && p < 0.36) {
            return -0.25f;
        } else if (p >= 0.36 && p < 0.43) {
            return 1.0f;
        } else if (p >= 0.43 && p < 0.48) {
            return -0.55f;
        } else if (p >= 0.56 && p < 0.68) {
            return (float) (0.32 * Math.sin((p - 0.56) / 0.12 * Math.PI));
        }
        return 0.0f;
    }

    private static void renderPixelFire(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int cols = Math.max(12, Math.min(lowDetail ? 16 : 28, Math.round(w / Math.max(2.0f, h * 0.13f))));
        int rows = lowDetail ? 6 : 9;
        float cellW = w / cols;
        float cellH = h / rows;
        float padW = cellW * 0.86f;
        float padH = cellH * 0.88f;

        for (int c = 0; c < cols; c++) {
            double flameHeight = 0.52
                    + 0.25 * Math.sin(t * 5.2 + c * 0.75)
                    + 0.18 * Math.cos(t * 8.7 - c * 1.35)
                    + 0.12 * Math.sin(t * 13.0 + c * 2.1);
            int maxRow = Math.max(2, Math.min(rows, (int) Math.round(flameHeight * rows)));
            float cx = x + c * cellW + (cellW - padW) * 0.5f;

            for (int r = 0; r < maxRow; r++) {
                float cy = y + h - (r + 1) * cellH + (cellH - padH) * 0.5f;
                float heat = 1.0f - ((float) r / rows);
                int fireColor;
                if (rainbow) {
                    fireColor = getRainbowColor(t * 0.3 + c * 0.04 + r * 0.06);
                } else if (heat > 0.78f) {
                    fireColor = 0xFFF688;
                } else if (heat > 0.52f) {
                    fireColor = 0xFF9900;
                } else if (heat > 0.28f) {
                    fireColor = 0xFF3300;
                } else {
                    fireColor = 0xAA1100;
                }
                int alpha = Math.min(255, (int) (130 + heat * 125));
                q(vc, mat, light, g, cx, cy, cx + padW, cy + padH, fireColor, alpha);
            }
        }
    }

    private static void renderRadar(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                    float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int col = rainbow ? getRainbowColor(t * 0.25) : baseColor;
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float lineT = Math.max(0.5f, h * 0.03f);

        q(vc, mat, light, g, x, cy - lineT * 0.5f, x + w, cy + lineT * 0.5f, col, 65);
        q(vc, mat, light, g, cx - lineT * 0.5f, y, cx + lineT * 0.5f, y + h, col, 65);

        float rx = w * 0.28f;
        float ry = h * 0.32f;
        drawOutlineBox(vc, mat, light, g, cx - rx, cy - ry, rx * 2, ry * 2, lineT, col, 80);

        double sweep = (t * 0.48) % 1.0;
        float sx = x + (float) sweep * w;
        float trailW = w * 0.14f;
        int trailSlices = lowDetail ? 4 : 7;
        for (int i = 0; i < trailSlices; i++) {
            float tx = sx - (i + 1) * (trailW / trailSlices);
            if (tx >= x) {
                q(vc, mat, light, g, tx, y, tx + (trailW / trailSlices), y + h, col, Math.max(15, 110 - i * 15));
            }
        }
        q(vc, mat, light, g, sx - lineT, y, sx + lineT, y + h, 0xFFFFFF, 245);

        float[][] blips = {
                {0.22f, 0.30f},
                {0.44f, 0.72f},
                {0.68f, 0.26f},
                {0.82f, 0.64f}
        };
        float blipSize = Math.max(1.3f, h * 0.09f);
        for (float[] b : blips) {
            double dist = sweep - b[0];
            if (dist < 0) dist += 1.0;
            if (dist < 0.65) {
                int bAlpha = (int) (255 * (1.0 - dist / 0.65));
                float bx = x + b[0] * w;
                float by = y + b[1] * h;
                int blipCol = dist < 0.08 ? 0xFFFFFF : (rainbow ? col : 0xFF3355);
                q(vc, mat, light, g, bx - blipSize, by - blipSize, bx + blipSize, by + blipSize, blipCol, bAlpha);
            }
        }
    }

    private static void renderBattery(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                      float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float bw = Math.min(w * 0.74f, h * 2.4f);
        float bh = Math.min(h * 0.68f, bw * 0.48f);
        float bx = x + (w - bw) * 0.5f;
        float by = y + (h - bh) * 0.5f;
        float border = Math.max(1.0f, bh * 0.10f);

        int frameCol = rainbow ? getRainbowColor(t * 0.2) : baseColor;
        drawOutlineBox(vc, mat, light, g, bx, by, bw, bh, border, frameCol, 235);

        float capW = Math.max(1.5f, border * 1.6f);
        float capH = bh * 0.46f;
        q(vc, mat, light, g, bx + bw, by + (bh - capH) * 0.5f, bx + bw + capW, by + (bh + capH) * 0.5f, frameCol, 235);

        int totalCells = 7;
        int filledCells = (int) (Math.floor(t * 2.2) % (totalCells + 2));
        boolean fullFlash = filledCells > totalCells;
        if (fullFlash) filledCells = totalCells;

        float innerX = bx + border * 1.8f;
        float innerY = by + border * 1.8f;
        float innerW = bw - border * 3.6f;
        float innerH = bh - border * 3.6f;
        float cellStep = innerW / totalCells;
        float cellW = Math.max(0.8f, cellStep * 0.76f);

        for (int i = 0; i < filledCells; i++) {
            float cx = innerX + i * cellStep + (cellStep - cellW) * 0.5f;
            int cellCol = frameCol;
            if (!rainbow) {
                if (filledCells <= 2) cellCol = 0xFF3344;
                else if (filledCells <= 4) cellCol = 0xFFAA00;
            }
            if (fullFlash && ((int) (t * 12) % 2 == 0)) {
                cellCol = 0xFFFFFF;
            }
            q(vc, mat, light, g, cx, innerY, cx + cellW, innerY + innerH, cellCol, 245);
        }
    }

    private static void renderCreeper(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                      float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int col = rainbow ? getRainbowColor(t * 0.3) : baseColor;
        boolean hissFlash = ((t * 0.45) % 1.0) > 0.86 && ((int) (t * 14) % 2 == 0);
        if (hissFlash) col = 0xFFFFFF;

        float bobY = (float) Math.sin(t * 3.2) * (h * 0.04f);
        float faceSize = Math.min(w * 0.65f, h * 0.82f);
        float px = faceSize / 8.0f;
        float fx = x + (w - faceSize) * 0.5f;
        float fy = y + (h - faceSize) * 0.5f + bobY;

        boolean blink = ((t * 0.65) % 1.0) > 0.92;
        float eyeTop = blink ? 2.6f : 1.5f;

        q(vc, mat, light, g, fx + px * 1.0f, fy + px * eyeTop, fx + px * 3.0f, fy + px * 3.5f, col, 250);
        q(vc, mat, light, g, fx + px * 5.0f, fy + px * eyeTop, fx + px * 7.0f, fy + px * 3.5f, col, 250);

        q(vc, mat, light, g, fx + px * 3.0f, fy + px * 3.5f, fx + px * 5.0f, fy + px * 6.0f, col, 250);

        q(vc, mat, light, g, fx + px * 2.0f, fy + px * 4.5f, fx + px * 3.0f, fy + px * 7.2f, col, 250);
        q(vc, mat, light, g, fx + px * 5.0f, fy + px * 4.5f, fx + px * 6.0f, fy + px * 7.2f, col, 250);

        float bLen = Math.min(w, h) * 0.18f;
        float bThick = Math.max(0.8f, h * 0.04f);
        q(vc, mat, light, g, x, y, x + bLen, y + bThick, col, 160);
        q(vc, mat, light, g, x, y, x + bThick, y + bLen, col, 160);
        q(vc, mat, light, g, x + w - bLen, y, x + w, y + bThick, col, 160);
        q(vc, mat, light, g, x + w - bThick, y, x + w, y + bLen, col, 160);
        q(vc, mat, light, g, x, y + h - bThick, x + bLen, y + h, col, 160);
        q(vc, mat, light, g, x, y + h - bLen, x + bThick, y + h, col, 160);
        q(vc, mat, light, g, x + w - bLen, y + h - bThick, x + w, y + h, col, 160);
        q(vc, mat, light, g, x + w - bThick, y + h - bLen, x + w, y + h, col, 160);
    }

    private static void renderSiren(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                    float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int phase = (int) (t * 8.0) % 6;
        boolean leftActive = (phase == 0 || phase == 2);
        boolean rightActive = (phase == 3 || phase == 5);

        int leftCol = rainbow ? getRainbowColor(t * 0.5) : 0xFF1E38;
        int rightCol = rainbow ? getRainbowColor(t * 0.5 + 0.5) : (baseColor == 0x00FF66 ? 0x0088FF : baseColor);

        float halfW = w * 0.46f;
        float lampH = h * 0.64f;
        float lampY = y + (h - lampH) * 0.5f;

        q(vc, mat, light, g, x + w * 0.02f, lampY, x + w * 0.02f + halfW, lampY + lampH, leftCol, leftActive ? 250 : 45);
        if (leftActive) {
            q(vc, mat, light, g, x + w * 0.10f, lampY + lampH * 0.22f, x + w * 0.02f + halfW - w * 0.08f, lampY + lampH * 0.78f, 0xFFFFFF, 220);
        }

        float rx = x + w * 0.52f;
        q(vc, mat, light, g, rx, lampY, rx + halfW, lampY + lampH, rightCol, rightActive ? 250 : 45);
        if (rightActive) {
            q(vc, mat, light, g, rx + w * 0.08f, lampY + lampH * 0.22f, rx + halfW - w * 0.08f, lampY + lampH * 0.78f, 0xFFFFFF, 220);
        }

        int stripes = 10;
        float stripeW = w / stripes;
        float barH = h * 0.11f;
        int scrollOffset = (int) (t * 6.0);
        for (int i = 0; i < stripes; i++) {
            if (((i + scrollOffset) & 1) == 0) {
                q(vc, mat, light, g, x + i * stripeW, y, x + (i + 0.7f) * stripeW, y + barH, 0xFFCC00, 210);
                q(vc, mat, light, g, x + i * stripeW, y + h - barH, x + (i + 0.7f) * stripeW, y + h, 0xFFCC00, 210);
            }
        }
    }

    private static void renderPacman(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                     float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float pacSize = Math.max(5.0f, Math.min(11.0f, h * 0.36f));
        float pxStep = pacSize / 7.0f;

        float padX = Math.max(4.0f, w * 0.05f);
        float padY = Math.max(3.0f, h * 0.08f);

        float xLeft = x + padX;
        float xRight = x + w - padX - pacSize;
        float yTop = y + padY;
        float yBot = y + h - padY - pacSize;
        float yMid = (yTop + yBot) * 0.5f;

        boolean threeLanes = (yBot - yTop) >= (pacSize * 2.2f);
        if (!threeLanes) {
            yMid = (yTop + yBot) * 0.5f;
        }

        if (threeLanes && w >= 30.0f) {
            int wallCol = 0x1A3580;
            float wallH = 1.0f;
            float wallPad = padX + pacSize * 1.2f;
            q(vc, mat, light, g, x + wallPad, (yTop + yMid) * 0.5f + pacSize * 0.5f - 0.5f,
                    x + w - wallPad, (yTop + yMid) * 0.5f + pacSize * 0.5f + wallH, wallCol, 130);
            q(vc, mat, light, g, x + wallPad, (yMid + yBot) * 0.5f + pacSize * 0.5f - 0.5f,
                    x + w - wallPad, (yMid + yBot) * 0.5f + pacSize * 0.5f + wallH, wallCol, 130);
        }

        double cycle = (t * 1.1) % 12.0;

        float pacX, pacY;
        int pacDir;
        if (cycle < 2.7) {
            float frac = (float) (cycle / 2.7);
            pacX = xLeft + frac * (xRight - xLeft);
            pacY = yTop;
            pacDir = 0;
        } else if (cycle < 3.8) {
            float frac = (float) ((cycle - 2.7) / 1.1);
            pacX = xRight;
            pacY = yTop + frac * (threeLanes ? (yMid - yTop) : (yBot - yTop));
            pacDir = 1;
        } else if (cycle < 6.5) {
            float frac = (float) ((cycle - 3.8) / 2.7);
            pacX = xRight - frac * (xRight - xLeft);
            pacY = threeLanes ? yMid : yBot;
            pacDir = 2;
        } else if (cycle < 7.6) {
            float frac = (float) ((cycle - 6.5) / 1.1);
            pacX = xLeft;
            pacY = threeLanes ? (yMid + frac * (yBot - yMid)) : (yBot - frac * (yBot - yTop));
            pacDir = threeLanes ? 1 : 3;
        } else if (cycle < 10.3) {
            float frac = (float) ((cycle - 7.6) / 2.7);
            pacX = xLeft + frac * (xRight - xLeft);
            pacY = yBot;
            pacDir = 0;
        } else {
            float frac = (float) ((cycle - 10.3) / 1.7);
            pacX = xRight;
            pacY = yBot - frac * (yBot - yTop);
            pacDir = 3;
        }

        double ghost1Cycle = (cycle - 0.95 + 12.0) % 12.0;
        float g1X, g1Y;
        int g1Dir;
        if (ghost1Cycle < 2.7) {
            float frac = (float) (ghost1Cycle / 2.7);
            g1X = xLeft + frac * (xRight - xLeft);
            g1Y = yTop;
            g1Dir = 0;
        } else if (ghost1Cycle < 3.8) {
            float frac = (float) ((ghost1Cycle - 2.7) / 1.1);
            g1X = xRight;
            g1Y = yTop + frac * (threeLanes ? (yMid - yTop) : (yBot - yTop));
            g1Dir = 1;
        } else if (ghost1Cycle < 6.5) {
            float frac = (float) ((ghost1Cycle - 3.8) / 2.7);
            g1X = xRight - frac * (xRight - xLeft);
            g1Y = threeLanes ? yMid : yBot;
            g1Dir = 2;
        } else if (ghost1Cycle < 7.6) {
            float frac = (float) ((ghost1Cycle - 6.5) / 1.1);
            g1X = xLeft;
            g1Y = threeLanes ? (yMid + frac * (yBot - yMid)) : (yBot - frac * (yBot - yTop));
            g1Dir = threeLanes ? 1 : 3;
        } else if (ghost1Cycle < 10.3) {
            float frac = (float) ((ghost1Cycle - 7.6) / 2.7);
            g1X = xLeft + frac * (xRight - xLeft);
            g1Y = yBot;
            g1Dir = 0;
        } else {
            float frac = (float) ((ghost1Cycle - 10.3) / 1.7);
            g1X = xRight;
            g1Y = yBot - frac * (yBot - yTop);
            g1Dir = 3;
        }

        int numDots = Math.max(3, (int) ((xRight - xLeft) / Math.max(8.0f, pacSize * 1.6f)));
        float dotSpacing = (xRight - xLeft) / Math.max(1, numDots - 1);
        float dotSize = Math.max(1.2f, pacSize * 0.22f);

        for (int i = 0; i < numDots; i++) {
            float dx = xLeft + i * dotSpacing;
            boolean eaten = (cycle >= 2.7) || (cycle < 2.7 && pacX >= dx - pacSize * 0.3f);
            if (!eaten) {
                int dotCol = rainbow ? getRainbowColor(t * 0.3 + i * 0.1) : 0xFFFFAA;
                q(vc, mat, light, g, dx + pacSize * 0.5f - dotSize * 0.5f, yTop + pacSize * 0.5f - dotSize * 0.5f,
                        dx + pacSize * 0.5f + dotSize * 0.5f, yTop + pacSize * 0.5f + dotSize * 0.5f, dotCol, 220);
            }
        }

        if (threeLanes) {
            for (int i = 0; i < numDots; i++) {
                float dx = xLeft + i * dotSpacing;
                boolean eaten = (cycle >= 6.5) || (cycle < 3.8) || (cycle >= 3.8 && pacX <= dx + pacSize * 0.3f);
                if (!eaten) {
                    int dotCol = rainbow ? getRainbowColor(t * 0.3 + i * 0.1) : 0xFFFFAA;
                    q(vc, mat, light, g, dx + pacSize * 0.5f - dotSize * 0.5f, yMid + pacSize * 0.5f - dotSize * 0.5f,
                            dx + pacSize * 0.5f + dotSize * 0.5f, yMid + pacSize * 0.5f + dotSize * 0.5f, dotCol, 220);
                }
            }
        }

        for (int i = 0; i < numDots; i++) {
            float dx = xLeft + i * dotSpacing;
            boolean eaten = (cycle >= 10.3) || (cycle < 7.6) || (cycle >= 7.6 && pacX >= dx - pacSize * 0.3f);
            if (!eaten) {
                int dotCol = rainbow ? getRainbowColor(t * 0.3 + i * 0.1) : 0xFFFFAA;
                q(vc, mat, light, g, dx + pacSize * 0.5f - dotSize * 0.5f, yBot + pacSize * 0.5f - dotSize * 0.5f,
                        dx + pacSize * 0.5f + dotSize * 0.5f, yBot + pacSize * 0.5f + dotSize * 0.5f, dotCol, 220);
            }
        }

        double chomp = Math.abs(Math.sin(t * 14.0));
        int pacCol = rainbow ? getRainbowColor(t * 0.5) : 0xFFDD00;

        int[][] pacPattern = {
                {0, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 0}
        };

        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                boolean fill = pacPattern[r][c] == 1;
                if (!fill && chomp < 0.30 && (r == 2 || r == 3 || r == 4) && c >= 4) {
                    fill = true;
                }
                if (fill) {
                    float bx, by;
                    if (pacDir == 0) {
                        bx = pacX + c * pxStep;
                        by = pacY + r * pxStep;
                    } else if (pacDir == 1) {
                        bx = pacX + (6 - r) * pxStep;
                        by = pacY + c * pxStep;
                    } else if (pacDir == 2) {
                        bx = pacX + (6 - c) * pxStep;
                        by = pacY + r * pxStep;
                    } else {
                        bx = pacX + r * pxStep;
                        by = pacY + (6 - c) * pxStep;
                    }
                    q(vc, mat, light, g, bx, by, bx + pxStep, by + pxStep, pacCol, 255);
                }
            }
        }

        int g1Col = rainbow ? getRainbowColor(t * 0.5 + 0.3) : 0xFF2233;
        boolean ghostFlap = ((int) (t * 8.0) % 2 == 0);
        drawPacGhost(vc, mat, light, g, g1X, g1Y, pxStep, g1Col, g1Dir, ghostFlap);

        if (w >= 36.0f) {
            double ghost2Cycle = (cycle - 1.85 + 12.0) % 12.0;
            float g2X, g2Y;
            int g2Dir;
            if (ghost2Cycle < 2.7) {
                float frac = (float) (ghost2Cycle / 2.7);
                g2X = xLeft + frac * (xRight - xLeft);
                g2Y = yTop;
                g2Dir = 0;
            } else if (ghost2Cycle < 3.8) {
                float frac = (float) ((ghost2Cycle - 2.7) / 1.1);
                g2X = xRight;
                g2Y = yTop + frac * (threeLanes ? (yMid - yTop) : (yBot - yTop));
                g2Dir = 1;
            } else if (ghost2Cycle < 6.5) {
                float frac = (float) ((ghost2Cycle - 3.8) / 2.7);
                g2X = xRight - frac * (xRight - xLeft);
                g2Y = threeLanes ? yMid : yBot;
                g2Dir = 2;
            } else if (ghost2Cycle < 7.6) {
                float frac = (float) ((ghost2Cycle - 6.5) / 1.1);
                g2X = xLeft;
                g2Y = threeLanes ? (yMid + frac * (yBot - yMid)) : (yBot - frac * (yBot - yTop));
                g2Dir = threeLanes ? 1 : 3;
            } else if (ghost2Cycle < 10.3) {
                float frac = (float) ((ghost2Cycle - 7.6) / 2.7);
                g2X = xLeft + frac * (xRight - xLeft);
                g2Y = yBot;
                g2Dir = 0;
            } else {
                float frac = (float) ((ghost2Cycle - 10.3) / 1.7);
                g2X = xRight;
                g2Y = yBot - frac * (yBot - yTop);
                g2Dir = 3;
            }
            int g2Col = rainbow ? getRainbowColor(t * 0.5 + 0.6) : 0xFFB8FF;
            drawPacGhost(vc, mat, light, g, g2X, g2Y, pxStep, g2Col, g2Dir, !ghostFlap);
        }
    }

    private static void drawPacGhost(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                     float gx, float gy, float pxStep, int ghostCol, int dir, boolean flap) {
        int[][] ghostPattern = {
                {0, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1},
                {1, 0, 1, 0, 1, 0, 1}
        };
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                boolean fill = ghostPattern[r][c] == 1;
                if (r == 6) {
                    fill = flap ? (c == 0 || c == 2 || c == 4 || c == 6) : (c == 1 || c == 3 || c == 5);
                }
                if (fill) {
                    float bx = gx + c * pxStep;
                    float by = gy + r * pxStep;
                    if (r == 2 && (c == 2 || c == 5)) {
                        q(vc, mat, light, g, bx, by, bx + pxStep, by + pxStep, 0xFFFFFF, 255);
                    } else if (r == 2 && (dir == 2 ? (c == 1 || c == 4) : (c == 3 || c == 6))) {
                        q(vc, mat, light, g, bx, by, bx + pxStep, by + pxStep, 0x0022FF, 255);
                    } else {
                        q(vc, mat, light, g, bx, by, bx + pxStep, by + pxStep, ghostCol, 245);
                    }
                }
            }
        }
    }

    private static void renderDigitalClock(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        java.time.LocalTime now = java.time.LocalTime.now();
        int hour = now.getHour();
        int min = now.getMinute();
        int sec = now.getSecond();

        boolean showSeconds = w >= 65.0f;
        int numDigits = showSeconds ? 6 : 4;
        int numColons = showSeconds ? 2 : 1;

        float unitSlots = showSeconds ? 7.6f : 4.8f;
        float maxDW_W = (w * 0.88f) / unitSlots;
        float maxDW_H = (h * 0.70f) / 1.7f;
        float digitW = Math.max(2.5f, Math.min(maxDW_W, maxDW_H));
        float digitH = digitW * 1.7f;
        float spacing = digitW * 0.20f;
        float colonW = digitW * 0.35f;

        int totalElements = numDigits + numColons;
        float totalW = numDigits * digitW + numColons * colonW + (totalElements - 1) * spacing;
        float startX = x + (w - totalW) * 0.5f;
        float digitY = y + (h - digitH) * 0.5f;

        int activeColor = rainbow ? getRainbowColor(t * 0.3) : baseColor;
        int dimColor = blendColor(activeColor, 0x000000, 0.88f);
        boolean colonBlink = ((int) (t * 2.0) & 1) == 0;

        float curX = startX;

        draw7SegmentDigit(vc, mat, light, g, curX, digitY, digitW, digitH, hour / 10, activeColor, dimColor);
        curX += digitW + spacing;
        draw7SegmentDigit(vc, mat, light, g, curX, digitY, digitW, digitH, hour % 10, activeColor, dimColor);
        curX += digitW + spacing;

        drawColon(vc, mat, light, g, curX, digitY, colonW, digitH, activeColor, dimColor, colonBlink);
        curX += colonW + spacing;

        draw7SegmentDigit(vc, mat, light, g, curX, digitY, digitW, digitH, min / 10, activeColor, dimColor);
        curX += digitW + spacing;
        draw7SegmentDigit(vc, mat, light, g, curX, digitY, digitW, digitH, min % 10, activeColor, dimColor);

        if (showSeconds) {
            curX += digitW + spacing;
            drawColon(vc, mat, light, g, curX, digitY, colonW, digitH, activeColor, dimColor, colonBlink);
            curX += colonW + spacing;

            draw7SegmentDigit(vc, mat, light, g, curX, digitY, digitW, digitH, sec / 10, activeColor, dimColor);
            curX += digitW + spacing;
            draw7SegmentDigit(vc, mat, light, g, curX, digitY, digitW, digitH, sec % 10, activeColor, dimColor);
        }
    }

    private static void drawColon(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                  float x, float y, float w, float h, int activeColor, int dimColor, boolean active) {
        float dotSize = Math.max(1.0f, w * 0.70f);
        float cx = x + (w - dotSize) * 0.5f;
        float dot1Y = y + h * 0.28f;
        float dot2Y = y + h * 0.65f;
        int col = active ? activeColor : dimColor;
        int alpha = active ? 255 : 35;
        q(vc, mat, light, g, cx, dot1Y, cx + dotSize, dot1Y + dotSize, col, alpha);
        q(vc, mat, light, g, cx, dot2Y, cx + dotSize, dot2Y + dotSize, col, alpha);
    }

    private static void draw7SegmentDigit(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int digit, int activeColor, int dimColor) {
        int[] patterns = { 0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F };
        int mask = (digit >= 0 && digit <= 9) ? patterns[digit] : 0;

        float segT = Math.max(0.8f, Math.min(2.8f, w * 0.16f));
        float halfH = h * 0.5f;

        renderSeg(vc, mat, light, g, x + segT, y, x + w - segT, y + segT, (mask & 0x01) != 0, activeColor, dimColor);
        renderSeg(vc, mat, light, g, x + w - segT, y + segT, x + w, y + halfH - segT * 0.5f, (mask & 0x02) != 0, activeColor, dimColor);
        renderSeg(vc, mat, light, g, x + w - segT, y + halfH + segT * 0.5f, x + w, y + h - segT, (mask & 0x04) != 0, activeColor, dimColor);
        renderSeg(vc, mat, light, g, x + segT, y + h - segT, x + w - segT, y + h, (mask & 0x08) != 0, activeColor, dimColor);
        renderSeg(vc, mat, light, g, x, y + halfH + segT * 0.5f, x + segT, y + h - segT, (mask & 0x10) != 0, activeColor, dimColor);
        renderSeg(vc, mat, light, g, x, y + segT, x + segT, y + halfH - segT * 0.5f, (mask & 0x20) != 0, activeColor, dimColor);
        renderSeg(vc, mat, light, g, x + segT, y + halfH - segT * 0.5f, x + w - segT, y + halfH + segT * 0.5f, (mask & 0x40) != 0, activeColor, dimColor);
    }

    private static void renderSeg(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                  float x1, float y1, float x2, float y2, boolean on, int activeColor, int dimColor) {
        int col = on ? activeColor : dimColor;
        int alpha = on ? 255 : 35;
        q(vc, mat, light, g, x1, y1, x2, y2, col, alpha);
    }

    private static void renderStarfield(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float maxR = Math.min(w, h) * 0.52f;
        int count = lowDetail ? 24 : 44;

        for (int i = 0; i < count; i++) {
            double angle = (i * 2.3999632) % (2.0 * Math.PI);
            double speedFactor = 0.65 + 0.7 * ((i * 31) % 10) / 10.0;
            double z = 1.0 - ((t * 0.45 * speedFactor + (double) i / count) % 1.0);
            if (z <= 0.01) continue;

            double invZ = 1.0 / (z * 3.8 + 0.12);
            double r = maxR * (invZ - 0.22);
            if (r <= 0) continue;
            if (r > maxR) r = maxR;

            float sx = cx + (float) (Math.cos(angle) * r);
            float sy = cy + (float) (Math.sin(angle) * r);

            double zTail = Math.min(1.0, z + 0.09);
            double invZTail = 1.0 / (zTail * 3.8 + 0.12);
            double rTail = maxR * (invZTail - 0.22);
            if (rTail > maxR) rTail = maxR;
            float tx = cx + (float) (Math.cos(angle) * rTail);
            float ty = cy + (float) (Math.sin(angle) * rTail);

            float starSize = Math.max(0.9f, (float) (1.2 / (z + 0.3)));
            int alpha = (int) Math.min(255.0, (1.0 - z) * 320.0);
            int col = rainbow ? getRainbowColor(t * 0.3 + i * 0.05) : (z > 0.4 ? 0xDDFFFF : baseColor);

            float minPx = Math.min(sx, tx);
            float maxPx = Math.max(sx, tx) + starSize;
            float minPy = Math.min(sy, ty);
            float maxPy = Math.max(sy, ty) + starSize;
            q(vc, mat, light, g, minPx, minPy, maxPx, maxPy, col, alpha);
        }
    }

    private static void renderBouncingLogo(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                          float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float lw = Math.max(8.0f, Math.min(24.0f, w * 0.28f));
        float lh = Math.max(6.0f, Math.min(15.0f, h * 0.32f));
        float rangeX = Math.max(0.0f, w - lw);
        float rangeY = Math.max(0.0f, h - lh);

        if (rangeX <= 1.0f || rangeY <= 1.0f) {
            float bx = x + (w - lw) * 0.5f;
            float by = y + (h - lh) * 0.5f;
            drawLogoEmblem(vc, mat, light, g, bx, by, lw, lh, baseColor, 255);
            return;
        }

        double periodX = rangeX * 2.0;
        double periodY = rangeY * 2.0;
        double rawX = (t * 26.0) % periodX;
        double rawY = (t * 19.0) % periodY;
        float lx = x + (float) (rawX > rangeX ? (periodX - rawX) : rawX);
        float ly = y + (float) (rawY > rangeY ? (periodY - rawY) : rawY);

        int bounceCount = (int) ((t * 26.0) / rangeX) + (int) ((t * 19.0) / rangeY);
        int col = rainbow ? getRainbowColor(t * 0.4) : getBounceColor(bounceCount, baseColor);

        double rawGhostX = (Math.max(0.0, t - 0.08) * 26.0) % periodX;
        double rawGhostY = (Math.max(0.0, t - 0.08) * 19.0) % periodY;
        float gx = x + (float) (rawGhostX > rangeX ? (periodX - rawGhostX) : rawGhostX);
        float gy = y + (float) (rawGhostY > rangeY ? (periodY - rawGhostY) : rawGhostY);
        drawLogoEmblem(vc, mat, light, g, gx, gy, lw, lh, col, 65);

        drawLogoEmblem(vc, mat, light, g, lx, ly, lw, lh, col, 250);
    }

    private static int getBounceColor(int bounceCount, int fallback) {
        int[] palette = { 0x00FFCC, 0xFF00AA, 0xFFCC00, 0x00E5FF, 0xFF3355, 0x00FF66, 0x9955FF };
        return palette[Math.abs(bounceCount) % palette.length];
    }

    private static void renderSlime(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                    float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float barH = Math.max(2.5f, h * 0.16f);
        float barY = y + h - barH - 1.0f;
        float barW = w * 0.88f;
        float barX = x + (w - barW) * 0.5f;

        int activeColor = rainbow ? getRainbowColor(t * 0.35) : (baseColor == 0x00FF66 ? 0x66FF44 : baseColor);
        int trackCol = blendColor(activeColor, 0x000000, 0.85f);

        float borderT = Math.max(0.6f, barH * 0.20f);

        drawOutlineBox(vc, mat, light, g, barX, barY, barW, barH, borderT, activeColor, 180);

        float inX = barX + borderT;
        float inY = barY + borderT;
        float inW = Math.max(1.0f, barW - borderT * 2.0f);
        float inH = Math.max(1.0f, barH - borderT * 2.0f);

        double prog = (t * 0.35) % 1.0;
        float fillW = Math.max(0.0f, Math.min(inW, (float) (inW * prog)));

        if (fillW > 0.5f) {
            q(vc, mat, light, g, inX, inY, inX + fillW, inY + inH, activeColor, 255);
        }
        if (fillW < inW - 0.5f) {
            q(vc, mat, light, g, inX + fillW, inY, inX + inW, inY + inH, trackCol, 140);
        }

        float slimeSize = Math.max(6.0f, Math.min(16.0f, (barY - y) * 0.65f));
        float slimeX = barX + fillW - slimeSize * 0.5f;
        slimeX = Math.max(barX, Math.min(barX + barW - slimeSize, slimeX));

        double jumpCycle = (t * 2.8) % 1.0;
        float jumpY = (float) Math.sin(jumpCycle * Math.PI);
        float squashX = jumpY < 0.2f ? 1.25f : (1.0f - jumpY * 0.15f);
        float squashY = jumpY < 0.2f ? 0.75f : (1.0f + jumpY * 0.20f);

        float curSlimeW = slimeSize * squashX;
        float curSlimeH = slimeSize * squashY;
        float maxJumpHeight = (barY - y - slimeSize) * 0.85f;
        float curSlimeY = barY - curSlimeH - (jumpY * maxJumpHeight);

        int slimeBodyCol = activeColor;
        q(vc, mat, light, g, slimeX, curSlimeY, slimeX + curSlimeW, curSlimeY + curSlimeH, slimeBodyCol, 190);
        drawOutlineBox(vc, mat, light, g, slimeX, curSlimeY, curSlimeW, curSlimeH, Math.max(0.6f, slimeSize * 0.1f), slimeBodyCol, 255);

        float corePadX = curSlimeW * 0.22f;
        float corePadY = curSlimeH * 0.22f;
        int coreCol = blendColor(activeColor, 0x000000, 0.40f);
        q(vc, mat, light, g, slimeX + corePadX, curSlimeY + corePadY,
                slimeX + curSlimeW - corePadX, curSlimeY + curSlimeH - corePadY, coreCol, 230);

        float eyeSize = Math.max(1.0f, slimeSize * 0.18f);
        float eyeY = curSlimeY + curSlimeH * 0.28f;
        float eye1X = slimeX + curSlimeW * 0.22f;
        float eye2X = slimeX + curSlimeW * 0.58f;
        q(vc, mat, light, g, eye1X, eyeY, eye1X + eyeSize, eyeY + eyeSize, 0x000000, 255);
        q(vc, mat, light, g, eye2X, eyeY, eye2X + eyeSize, eyeY + eyeSize, 0x000000, 255);
    }

    private static float smoothStep(float t, float t0, float t1, float v0, float v1) {
        if (t <= t0) return v0;
        if (t >= t1) return v1;
        float u = (t - t0) / (t1 - t0);
        float s = u * u * (3.0f - 2.0f * u);
        return v0 + (v1 - v0) * s;
    }

    private static float interpKeyframes(float t, float[] times, float[] values) {
        if (t <= times[0]) return values[0];
        int n = times.length;
        if (t >= times[n - 1]) return values[n - 1];
        for (int i = 0; i < n - 1; i++) {
            if (t < times[i + 1]) {
                return smoothStep(t, times[i], times[i + 1], values[i], values[i + 1]);
            }
        }
        return values[n - 1];
    }

    private static void renderPong(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                   float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int color = rainbow ? getRainbowColor(t * 0.3) : baseColor;

        float midX = x + w * 0.5f;
        int dashCount = Math.max(4, (int) (h / 5.0f));
        float dashH = h / (dashCount * 2 - 1);
        for (int i = 0; i < dashCount; i++) {
            float dy = y + i * 2 * dashH;
            q(vc, mat, light, g, midX - 0.5f, dy, midX + 0.5f, dy + dashH, color, 120);
        }

        float ballSize = Math.max(1.8f, Math.min(3.5f, h * 0.10f));
        float paddleW = Math.max(1.5f, w * 0.024f);
        float paddleH = Math.max(6.0f, h * 0.34f);

        float courtLeft = x + 3.0f + paddleW;
        float courtRight = x + w - 3.0f - paddleW - ballSize;
        float courtW = Math.max(10.0f, courtRight - courtLeft);
        float courtTop = y + 1.0f;
        float courtBot = y + h - ballSize - 1.0f;
        float courtH = Math.max(4.0f, courtBot - courtTop);

        double matchCycle = (t * 1.0) % 45.0;
        int roundNum = (int) (matchCycle / 7.5);
        float roundT = (float) (matchCycle - roundNum * 7.5);

        boolean missP2 = (roundNum != 1 && roundNum != 4);

        int baseScore1 = switch (roundNum) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 1;
            case 3 -> 2;
            case 4 -> 3;
            case 5 -> 3;
            default -> 0;
        };
        int baseScore2 = switch (roundNum) {
            case 0 -> 0;
            case 1 -> 0;
            case 2 -> 1;
            case 3 -> 1;
            case 4 -> 1;
            case 5 -> 2;
            default -> 0;
        };

        boolean goalScored = (roundT >= 6.35f);
        int score1 = baseScore1 + (goalScored && missP2 ? 1 : 0);
        int score2 = baseScore2 + (goalScored && !missP2 ? 1 : 0);

        float ballX, ballY;
        float p1TargetY, p2TargetY;

        if (missP2) {
            float[] p1Times = { 0.0f, 1.3f, 2.6f, 3.9f, 5.0f, 6.35f, 7.5f };
            float[] p1Vals  = { 0.35f, 0.45f, 0.55f, 0.65f, 0.80f, 0.80f, 0.35f };
            float[] p2Times = { 0.0f, 1.3f, 2.6f, 3.9f, 5.0f, 5.9f, 6.35f, 7.5f };
            float[] p2Vals  = { 0.40f, 0.80f, 0.60f, 0.35f, 0.45f, 0.75f, 0.75f, 0.40f };

            p1TargetY = courtTop + courtH * interpKeyframes(roundT, p1Times, p1Vals);
            p2TargetY = courtTop + courtH * interpKeyframes(roundT, p2Times, p2Vals);

            float goalRight = x + w - paddleW - ballSize - 1.0f;

            if (roundT < 1.3f) {
                float u = roundT / 1.3f;
                ballX = courtLeft + u * courtW;
                ballY = courtTop + courtH * (0.35f + 0.45f * u);
            } else if (roundT < 2.6f) {
                float u = (roundT - 1.3f) / 1.3f;
                ballX = courtRight - u * courtW;
                ballY = (u < 0.5f)
                        ? (courtTop + courtH * (0.80f - 1.60f * u))
                        : (courtTop + courtH * (1.10f * (u - 0.5f)));
            } else if (roundT < 3.9f) {
                float u = (roundT - 2.6f) / 1.3f;
                ballX = courtLeft + u * courtW;
                ballY = (u < 0.5f)
                        ? (courtTop + courtH * (0.55f + 0.90f * u))
                        : (courtTop + courtH * (1.0f - 1.30f * (u - 0.5f)));
            } else if (roundT < 5.0f) {
                float u = (roundT - 3.9f) / 1.1f;
                ballX = courtRight - u * courtW;
                ballY = courtTop + courtH * (0.35f + 0.45f * u);
            } else if (roundT < 6.35f) {
                float u = (roundT - 5.0f) / 1.35f;
                ballX = courtLeft + u * (goalRight - courtLeft);
                ballY = courtTop + courtH * (0.80f - 0.70f * u);
            } else if (roundT < 6.65f) {
                ballX = goalRight;
                ballY = courtTop + courtH * 0.10f;
            } else {
                ballX = midX - ballSize * 0.5f;
                ballY = courtTop + courtH * 0.5f;
            }
        } else {
            float[] p1Times = { 0.0f, 1.3f, 2.6f, 3.9f, 5.0f, 5.9f, 6.35f, 7.5f };
            float[] p1Vals  = { 0.50f, 0.20f, 0.35f, 0.65f, 0.50f, 0.25f, 0.25f, 0.50f };
            float[] p2Times = { 0.0f, 1.3f, 2.6f, 3.9f, 5.0f, 6.35f, 7.5f };
            float[] p2Vals  = { 0.65f, 0.55f, 0.45f, 0.35f, 0.20f, 0.20f, 0.65f };

            p1TargetY = courtTop + courtH * interpKeyframes(roundT, p1Times, p1Vals);
            p2TargetY = courtTop + courtH * interpKeyframes(roundT, p2Times, p2Vals);

            float goalLeft = x + 1.0f;

            if (roundT < 1.3f) {
                float u = roundT / 1.3f;
                ballX = courtRight - u * courtW;
                ballY = courtTop + courtH * (0.65f - 0.45f * u);
            } else if (roundT < 2.6f) {
                float u = (roundT - 1.3f) / 1.3f;
                ballX = courtLeft + u * courtW;
                ballY = (u < 0.5f)
                        ? (courtTop + courtH * (0.20f + 1.60f * u))
                        : (courtTop + courtH * (1.0f - 1.10f * (u - 0.5f)));
            } else if (roundT < 3.9f) {
                float u = (roundT - 2.6f) / 1.3f;
                ballX = courtRight - u * courtW;
                ballY = (u < 0.5f)
                        ? (courtTop + courtH * (0.45f - 0.90f * u))
                        : (courtTop + courtH * (1.30f * (u - 0.5f)));
            } else if (roundT < 5.0f) {
                float u = (roundT - 3.9f) / 1.1f;
                ballX = courtLeft + u * courtW;
                ballY = courtTop + courtH * (0.65f - 0.45f * u);
            } else if (roundT < 6.35f) {
                float u = (roundT - 5.0f) / 1.35f;
                ballX = courtRight - u * (courtRight - goalLeft);
                ballY = courtTop + courtH * (0.20f + 0.70f * u);
            } else if (roundT < 6.65f) {
                ballX = goalLeft;
                ballY = courtTop + courtH * 0.90f;
            } else {
                ballX = midX - ballSize * 0.5f;
                ballY = courtTop + courtH * 0.5f;
            }
        }

        float p1Y = Math.max(courtTop, Math.min(courtBot + ballSize - paddleH, p1TargetY - paddleH * 0.5f));
        float p2Y = Math.max(courtTop, Math.min(courtBot + ballSize - paddleH, p2TargetY - paddleH * 0.5f));

        q(vc, mat, light, g, x + 3.0f, p1Y, x + 3.0f + paddleW, p1Y + paddleH, color, 245);
        q(vc, mat, light, g, x + w - 3.0f - paddleW, p2Y, x + w - 3.0f, p2Y + paddleH, color, 245);

        float safeBallX = Math.max(x + 1.0f, Math.min(x + w - ballSize - 1.0f, ballX));
        float safeBallY = Math.max(courtTop, Math.min(courtBot, ballY));
        if (roundT < 6.65f) {
            q(vc, mat, light, g, safeBallX, safeBallY, safeBallX + ballSize, safeBallY + ballSize, 0xFFFFFF, 255);
        } else if (((int) (t * 8.0) % 2) == 0) {
            q(vc, mat, light, g, midX - 4.0f, y + 1.0f, midX + 4.0f, y + h - 1.0f, 0xFFFFAA, 60);
        }

        float scoreSize = Math.max(2.2f, h * 0.15f);
        float scoreY = y + 2.0f;
        int activeScoreCol = goalScored ? 0xFFFFAA : color;
        draw7SegmentDigit(vc, mat, light, g, midX - scoreSize * 2.2f, scoreY, scoreSize, scoreSize * 1.5f, score1, activeScoreCol, blendColor(color, 0x000000, 0.85f));
        draw7SegmentDigit(vc, mat, light, g, midX + scoreSize * 1.2f, scoreY, scoreSize, scoreSize * 1.5f, score2, activeScoreCol, blendColor(color, 0x000000, 0.85f));
    }

    private static void renderRainstorm(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int dropCount = lowDetail ? 18 : 34;
        int rainCol = rainbow ? getRainbowColor(t * 0.3) : (baseColor == 0x00FF66 ? 0x88DDFF : baseColor);

        double strikePeriod = 4.5;
        double strikePhase = (t % strikePeriod);
        boolean isLightning = strikePhase > 4.15 && ((int) (t * 30.0) % 2 == 0);

        if (isLightning) {
            q(vc, mat, light, g, x, y, x + w, y + h, 0xFFFFFF, 75);
        }

        float slant = Math.max(1.5f, w * 0.04f);
        for (int i = 0; i < dropCount; i++) {
            int hash = (i * 265443576) ^ 0x61C88647;
            double dropSpeed = 1.2 + ((hash & 0x7F) / 127.0) * 1.1;
            double xFrac = ((hash >> 8) & 0xFF) / 255.0;
            double yPhase = ((hash >> 16) & 0xFF) / 255.0;

            double dropProgress = (t * dropSpeed + yPhase) % 1.0;
            float dx = x + (float) (xFrac * (w - slant)) + (float) (dropProgress * slant);
            float dy = y + (float) (dropProgress * h);
            float dropLen = Math.max(2.0f, h * 0.16f);

            q(vc, mat, light, g, dx, dy, dx + 1.0f, Math.min(y + h, dy + dropLen), rainCol, 185);

            if (dy + dropLen >= y + h - 1.5f) {
                float splashW = Math.max(1.8f, h * 0.06f);
                q(vc, mat, light, g, dx - splashW * 0.5f, y + h - 1.0f, dx + splashW * 0.5f, y + h, rainCol, 140);
            }
        }

        if (isLightning) {
            float boltX = x + w * 0.45f + (float) (Math.sin(t * 88.0) * (w * 0.20f));
            float boltY = y;
            float stepY = h / 6.0f;
            float curX = boltX;

            for (int seg = 0; seg < 6; seg++) {
                float nextX = curX + (float) ((Math.sin(seg * 7.5 + t * 40.0)) * (w * 0.08f));
                float nextY = boltY + (seg + 1) * stepY;
                float minBx = Math.min(curX, nextX);
                float maxBx = Math.max(curX, nextX) + 1.2f;
                q(vc, mat, light, g, minBx, boltY + seg * stepY, maxBx, nextY, 0xFFFFFF, 255);
                curX = nextX;
            }
        }
    }

    private static void renderDnaHelix(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int steps = lowDetail ? 16 : 28;
        float stepW = w / steps;
        float cy = y + h * 0.5f;
        float amplitude = h * 0.36f;
        float nodeSize = Math.max(1.2f, h * 0.08f);

        for (int i = 0; i < steps; i++) {
            double angle = t * 2.8 + i * 0.42;
            float sinVal = (float) Math.sin(angle);
            float cosVal = (float) Math.cos(angle);

            float strand1Y = cy + sinVal * amplitude;
            float strand2Y = cy - sinVal * amplitude;
            float curX = x + i * stepW + stepW * 0.5f;

            if (i % 2 == 0) {
                float topRung = Math.min(strand1Y, strand2Y);
                float botRung = Math.max(strand1Y, strand2Y);
                int rungCol = rainbow ? getRainbowColor(t * 0.2 + i * 0.05) : blendColor(baseColor, 0x00E5FF, 0.4f);
                int rungAlpha = (int) (60 + Math.abs(cosVal) * 140);
                q(vc, mat, light, g, curX - 0.5f, topRung, curX + 0.5f, botRung, rungCol, rungAlpha);
            }

            int col1 = rainbow ? getRainbowColor(t * 0.3 + i * 0.05) : baseColor;
            int col2 = rainbow ? getRainbowColor(t * 0.3 + i * 0.05 + 0.5) : blendColor(baseColor, 0xFF33AA, 0.55f);

            float sz1 = nodeSize * (0.8f + cosVal * 0.35f);
            float sz2 = nodeSize * (0.8f - cosVal * 0.35f);
            int a1 = (int) (120 + cosVal * 135);
            int a2 = (int) (120 - cosVal * 135);

            if (cosVal >= 0) {
                q(vc, mat, light, g, curX - sz2 * 0.5f, strand2Y - sz2 * 0.5f, curX + sz2 * 0.5f, strand2Y + sz2 * 0.5f, col2, a2);
                q(vc, mat, light, g, curX - sz1 * 0.5f, strand1Y - sz1 * 0.5f, curX + sz1 * 0.5f, strand1Y + sz1 * 0.5f, col1, a1);
            } else {
                q(vc, mat, light, g, curX - sz1 * 0.5f, strand1Y - sz1 * 0.5f, curX + sz1 * 0.5f, strand1Y + sz1 * 0.5f, col1, a1);
                q(vc, mat, light, g, curX - sz2 * 0.5f, strand2Y - sz2 * 0.5f, curX + sz2 * 0.5f, strand2Y + sz2 * 0.5f, col2, a2);
            }
        }
    }

    private static void renderSpaceInvaders(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                            float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int shipCol = rainbow ? getRainbowColor(t * 0.4) : 0x00FF88;
        int invaderCol = rainbow ? getRainbowColor(t * 0.4 + 0.3) : (baseColor == 0x00FF66 ? 0xFF3366 : baseColor);
        int invaderCol2 = rainbow ? getRainbowColor(t * 0.4 + 0.6) : 0x00E5FF;

        float shipW = Math.max(8.0f, Math.min(18.0f, w * 0.16f));
        float shipH = Math.max(4.0f, Math.min(8.0f, h * 0.18f));
        float shipY = y + h - shipH - 1.0f;

        double cycle = (t * 1.0) % 14.0;

        int cols = 4;
        float alienW = Math.max(5.0f, Math.min(12.0f, w * 0.12f));
        float alienH = alienW * 0.72f;
        float alienStepX = (w * 0.70f) / cols;

        double marchOffset = Math.sin(t * 1.8) * (w * 0.09f);
        float marchY = (float) (Math.abs(Math.sin(t * 0.3)) * (h * 0.06f));
        float alienStartX = x + w * 0.12f + (float) marchOffset;
        float row0Y = y + h * 0.12f + marchY;
        float row1Y = row0Y + alienH * 1.35f;

        int[] targetAliens = { 5, 6, 4, 7, 1, 2 };

        int curShot = Math.min(targetAliens.length - 1, (int) (cycle / 2.0));
        double relT = cycle - curShot * 2.0;

        int targetAlien = targetAliens[curShot];
        int targetCol = targetAlien % cols;
        float targetAx = alienStartX + targetCol * alienStepX + (alienW - shipW) * 0.5f;

        float prevAx;
        if (curShot > 0) {
            int prevCol = targetAliens[curShot - 1] % cols;
            prevAx = alienStartX + prevCol * alienStepX + (alienW - shipW) * 0.5f;
        } else {
            prevAx = x + (w - shipW) * 0.5f;
        }

        float shipX;
        if (cycle >= 12.0) {
            shipX = x + (float) ((0.5 + 0.38 * Math.sin(t * 2.5)) * (w - shipW));
        } else if (relT < 0.6) {
            float moveFrac = (float) (0.5 - 0.5 * Math.cos(relT / 0.6 * Math.PI));
            shipX = prevAx + (targetAx - prevAx) * moveFrac;
        } else {
            shipX = targetAx;
        }

        shipX = Math.max(x, Math.min(x + w - shipW, shipX));

        q(vc, mat, light, g, shipX + shipW * 0.42f, shipY, shipX + shipW * 0.58f, shipY + shipH * 0.4f, 0xFFFFFF, 255);
        q(vc, mat, light, g, shipX + shipW * 0.20f, shipY + shipH * 0.35f, shipX + shipW * 0.80f, shipY + shipH * 0.75f, shipCol, 245);
        q(vc, mat, light, g, shipX, shipY + shipH * 0.65f, shipX + shipW, shipY + shipH, shipCol, 245);

        if (cycle < 12.0 && relT >= 0.7 && relT < 1.0) {
            float lProg = (float) ((relT - 0.7) / 0.3);
            int tRow = targetAlien / cols;
            float targetRowY = (tRow == 0 ? row0Y : row1Y) + alienH * 0.5f;
            float laserX = shipX + shipW * 0.5f;
            float curLaserY = shipY - lProg * (shipY - targetRowY);
            float laserLen = Math.max(2.5f, h * 0.12f);
            q(vc, mat, light, g, laserX - 0.7f, curLaserY, laserX + 0.7f, Math.min(shipY, curLaserY + laserLen), 0xFFFFAA, 255);
        }

        boolean flapLegs = ((int) (t * 4.0) % 2 == 0);

        for (int r = 0; r < 2; r++) {
            float curRowY = (r == 0) ? row0Y : row1Y;
            int curCol = (r == 0) ? invaderCol2 : invaderCol;

            for (int c = 0; c < cols; c++) {
                int alienIdx = r * cols + c;
                float ax = alienStartX + c * alienStepX;

                boolean isDead = false;
                boolean isExploding = false;
                float explodeAge = 0.0f;

                for (int k = 0; k < targetAliens.length; k++) {
                    if (targetAliens[k] == alienIdx) {
                        double hitT = k * 2.0 + 1.0;
                        if (cycle >= hitT && cycle < 13.2) {
                            if (cycle < hitT + 0.35) {
                                isExploding = true;
                                explodeAge = (float) (cycle - hitT);
                            } else {
                                isDead = true;
                            }
                        }
                    }
                }

                if (isDead) {
                    continue;
                }

                if (isExploding) {
                    float expDist = explodeAge * 15.0f;
                    float ecx = ax + alienW * 0.5f;
                    float ecy = curRowY + alienH * 0.5f;
                    q(vc, mat, light, g, ecx - expDist - 1.0f, ecy - 1.0f, ecx - expDist + 1.0f, ecy + 1.0f, 0xFFFFAA, 255);
                    q(vc, mat, light, g, ecx + expDist - 1.0f, ecy - 1.0f, ecx + expDist + 1.0f, ecy + 1.0f, 0xFFFFAA, 255);
                    q(vc, mat, light, g, ecx - 1.0f, ecy - expDist - 1.0f, ecx + 1.0f, ecy - expDist + 1.0f, 0xFFFFAA, 255);
                    q(vc, mat, light, g, ecx - 1.0f, ecy + expDist - 1.0f, ecx + 1.0f, ecy + expDist + 1.0f, 0xFFFFAA, 255);
                    continue;
                }

                if (cycle >= 13.2 && ((int) (t * 12.0) % 2 == 0)) {
                    curCol = 0xFFFFFF;
                }

                if (r == 0) {
                    q(vc, mat, light, g, ax + alienW * 0.15f, curRowY, ax + alienW * 0.3f, curRowY + alienH * 0.3f, curCol, 245);
                    q(vc, mat, light, g, ax + alienW * 0.70f, curRowY, ax + alienW * 0.85f, curRowY + alienH * 0.3f, curCol, 245);
                    q(vc, mat, light, g, ax + alienW * 0.25f, curRowY + alienH * 0.2f, ax + alienW * 0.75f, curRowY + alienH * 0.6f, curCol, 245);
                    q(vc, mat, light, g, ax, curRowY + alienH * 0.4f, ax + alienW, curRowY + alienH * 0.75f, curCol, 245);
                    if (flapLegs) {
                        q(vc, mat, light, g, ax + alienW * 0.1f, curRowY + alienH * 0.75f, ax + alienW * 0.35f, curRowY + alienH, curCol, 245);
                        q(vc, mat, light, g, ax + alienW * 0.65f, curRowY + alienH * 0.75f, ax + alienW * 0.90f, curRowY + alienH, curCol, 245);
                    } else {
                        q(vc, mat, light, g, ax + alienW * 0.25f, curRowY + alienH * 0.75f, ax + alienW * 0.50f, curRowY + alienH, curCol, 245);
                        q(vc, mat, light, g, ax + alienW * 0.50f, curRowY + alienH * 0.75f, ax + alienW * 0.75f, curRowY + alienH, curCol, 245);
                    }
                    float eyeSz = Math.max(0.8f, alienW * 0.14f);
                    q(vc, mat, light, g, ax + alienW * 0.32f, curRowY + alienH * 0.35f, ax + alienW * 0.32f + eyeSz, curRowY + alienH * 0.35f + eyeSz, 0x000000, 255);
                    q(vc, mat, light, g, ax + alienW * 0.56f, curRowY + alienH * 0.35f, ax + alienW * 0.56f + eyeSz, curRowY + alienH * 0.35f + eyeSz, 0x000000, 255);
                } else {
                    q(vc, mat, light, g, ax + alienW * 0.2f, curRowY, ax + alienW * 0.8f, curRowY + alienH * 0.35f, curCol, 240);
                    q(vc, mat, light, g, ax, curRowY + alienH * 0.35f, ax + alienW, curRowY + alienH * 0.70f, curCol, 240);
                    if (flapLegs) {
                        q(vc, mat, light, g, ax + alienW * 0.1f, curRowY + alienH * 0.70f, ax + alienW * 0.35f, curRowY + alienH, curCol, 240);
                        q(vc, mat, light, g, ax + alienW * 0.65f, curRowY + alienH * 0.70f, ax + alienW * 0.90f, curRowY + alienH, curCol, 240);
                    } else {
                        q(vc, mat, light, g, ax + alienW * 0.3f, curRowY + alienH * 0.70f, ax + alienW * 0.5f, curRowY + alienH, curCol, 240);
                        q(vc, mat, light, g, ax + alienW * 0.5f, curRowY + alienH * 0.70f, ax + alienW * 0.7f, curRowY + alienH, curCol, 240);
                    }
                    float eyeSz = Math.max(0.8f, alienW * 0.15f);
                    q(vc, mat, light, g, ax + alienW * 0.25f, curRowY + alienH * 0.2f, ax + alienW * 0.25f + eyeSz, curRowY + alienH * 0.2f + eyeSz, 0x000000, 255);
                    q(vc, mat, light, g, ax + alienW * 0.65f, curRowY + alienH * 0.2f, ax + alienW * 0.65f + eyeSz, curRowY + alienH * 0.2f + eyeSz, 0x000000, 255);
                }
            }
        }

        if (cycle < 12.0 && relT >= 0.3 && relT < 1.6) {
            int bombCol = (targetCol <= 1) ? 3 : 0;
            float bAlienX = alienStartX + bombCol * alienStepX + alienW * 0.5f;
            float bProg = (float) ((relT - 0.3) / 1.3);
            float bombY = row1Y + alienH + bProg * (shipY + shipH - row1Y - alienH);
            q(vc, mat, light, g, bAlienX - 0.8f, bombY, bAlienX + 0.8f, bombY + 2.5f, 0xFF3333, 240);
            if (bProg >= 0.92f) {
                q(vc, mat, light, g, bAlienX - 1.5f, shipY + shipH - 0.8f, bAlienX + 1.5f, shipY + shipH, 0xFF7744, 220);
            }
        }
    }

    private static void renderCassetteTape(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                          float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int color = rainbow ? getRainbowColor(t * 0.35) : baseColor;

        float cw = Math.min(w * 0.84f, h * 2.2f);
        float ch = Math.min(h * 0.72f, cw * 0.58f);
        float cx = x + (w - cw) * 0.5f;
        float cy = y + (h - ch) * 0.5f;
        float borderT = Math.max(0.8f, ch * 0.08f);

        drawOutlineBox(vc, mat, light, g, cx, cy, cw, ch, borderT, color, 220);

        float winW = cw * 0.65f;
        float winH = ch * 0.52f;
        float winX = cx + (cw - winW) * 0.5f;
        float winY = cy + (ch - winH) * 0.5f;
        drawOutlineBox(vc, mat, light, g, winX, winY, winW, winH, Math.max(0.5f, borderT * 0.7f), color, 180);

        float ribbonY = winY + winH * 0.5f;
        q(vc, mat, light, g, winX + winW * 0.25f, ribbonY - 0.5f, winX + winW * 0.75f, ribbonY + 0.5f, blendColor(color, 0x000000, 0.45f), 200);

        float wheelR = Math.max(2.5f, winH * 0.38f);
        float w1X = winX + winW * 0.25f;
        float w2X = winX + winW * 0.75f;
        float wY = winY + winH * 0.5f;

        double tapeProg = (t * 0.12) % 1.0;
        float spool1R = wheelR * (float) (1.25 - tapeProg * 0.45);
        float spool2R = wheelR * (float) (0.80 + tapeProg * 0.45);

        q(vc, mat, light, g, w1X - spool1R, wY - spool1R, w1X + spool1R, wY + spool1R, blendColor(color, 0x000000, 0.55f), 190);
        q(vc, mat, light, g, w2X - spool2R, wY - spool2R, w2X + spool2R, wY + spool2R, blendColor(color, 0x000000, 0.55f), 190);

        for (int i = 0; i < 4; i++) {
            double rotAngle = t * 4.5 + (i * Math.PI / 4.0);
            float cos = (float) Math.cos(rotAngle);
            float sin = (float) Math.sin(rotAngle);
            float tooth = wheelR * 0.65f;

            q(vc, mat, light, g, w1X - cos * tooth - 0.5f, wY - sin * tooth - 0.5f,
                    w1X + cos * tooth + 0.5f, wY + sin * tooth + 0.5f, 0xFFFFFF, 240);
            q(vc, mat, light, g, w2X - cos * tooth - 0.5f, wY - sin * tooth - 0.5f,
                    w2X + cos * tooth + 0.5f, wY + sin * tooth + 0.5f, 0xFFFFFF, 240);
        }
    }

    private static void renderReactorSpinner(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                             float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float maxR = Math.min(w, h) * 0.46f;
        int color = rainbow ? getRainbowColor(t * 0.35) : baseColor;

        float corePulse = (float) (0.85 + 0.15 * Math.sin(t * 6.0));
        float coreR = Math.max(1.5f, maxR * 0.22f * corePulse);
        q(vc, mat, light, g, cx - coreR, cy - coreR, cx + coreR, cy + coreR, 0xFFFFFF, 255);
        q(vc, mat, light, g, cx - coreR * 1.6f, cy - coreR * 1.6f, cx + coreR * 1.6f, cy + coreR * 1.6f, color, 140);

        int innerSectors = 6;
        float innerR = maxR * 0.55f;
        float innerThick = Math.max(0.8f, maxR * 0.08f);
        for (int i = 0; i < innerSectors; i++) {
            if ((i & 1) == 0) continue;
            double a = -t * 3.2 + (i * 2.0 * Math.PI / innerSectors);
            float px = cx + (float) (Math.cos(a) * innerR);
            float py = cy + (float) (Math.sin(a) * innerR);
            float segSz = innerR * 0.42f;
            q(vc, mat, light, g, px - segSz * 0.5f, py - innerThick * 0.5f, px + segSz * 0.5f, py + innerThick * 0.5f, color, 210);
        }

        int outerSectors = lowDetail ? 6 : 8;
        float outerR = maxR * 0.88f;
        float outerThick = Math.max(1.0f, maxR * 0.10f);
        for (int i = 0; i < outerSectors; i++) {
            if ((i % 3) == 0) continue;
            double a = t * 2.2 + (i * 2.0 * Math.PI / outerSectors);
            float px = cx + (float) (Math.cos(a) * outerR);
            float py = cy + (float) (Math.sin(a) * outerR);
            float segW = outerR * 0.32f;
            q(vc, mat, light, g, px - segW * 0.5f, py - outerThick * 0.5f, px + segW * 0.5f, py + outerThick * 0.5f, color, 245);
        }

        float tickLen = maxR * 0.14f;
        q(vc, mat, light, g, cx - 0.5f, cy - maxR - tickLen, cx + 0.5f, cy - maxR, color, 180);
        q(vc, mat, light, g, cx - 0.5f, cy + maxR, cx + 0.5f, cy + maxR + tickLen, color, 180);
        q(vc, mat, light, g, cx - maxR - tickLen, cy - 0.5f, cx - maxR, cy + 0.5f, color, 180);
        q(vc, mat, light, g, cx + maxR, cy - 0.5f, cx + maxR + tickLen, cy + 0.5f, color, 180);
    }

    private static void drawLogoEmblem(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float x, float y, float w, float h, int colorRGB, int alpha) {
        float borderT = Math.max(0.8f, h * 0.12f);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, borderT, colorRGB, alpha);

        float pad = borderT * 2.0f;
        float innerW = w - pad * 2.0f;
        float innerH = h - pad * 2.0f;
        if (innerW > 2.0f && innerH > 2.0f) {
            float cx = x + w * 0.5f;
            float cy = y + h * 0.5f;
            float dw = innerW * 0.45f;
            float dh = innerH * 0.45f;
            q(vc, mat, light, g, cx - dw, cy - dh * 0.35f, cx + dw, cy + dh * 0.35f, 0xFFFFFF, (int) (alpha * 0.85f));
            q(vc, mat, light, g, cx - dw * 0.35f, cy - dh, cx + dw * 0.35f, cy + dh, 0xFFFFFF, (int) (alpha * 0.85f));
            q(vc, mat, light, g, cx - dw * 0.6f, cy - dh * 0.6f, cx + dw * 0.6f, cy + dh * 0.6f, colorRGB, alpha);
        }
    }

    private static void renderUfo(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                  float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int ufoBodyCol = rainbow ? getRainbowColor(t * 0.3) : baseColor;
        int beamCol = rainbow ? getRainbowColor(t * 0.3 + 0.3) : (baseColor == 0x00FF66 ? 0x00FFCC : 0x00E5FF);

        int starCount = lowDetail ? 10 : 20;
        for (int i = 0; i < starCount; i++) {
            int hash = (i * 92837111) ^ 0x4B3C2A1;
            float sx = x + ((hash & 0xFF) / 255.0f) * w;
            float sy = y + (((hash >> 8) & 0xFF) / 255.0f) * h * 0.65f;
            float twinkle = 0.5f + 0.5f * (float) Math.sin(t * 2.5 + i * 1.3);
            int sa = (int) (twinkle * 180 + 35);
            q(vc, mat, light, g, sx, sy, sx + 1.0f, sy + 1.0f, 0xFFFFFF, sa);
        }

        float groundH = Math.max(3.0f, h * 0.10f);
        float groundY = y + h - groundH;
        q(vc, mat, light, g, x, groundY, x + w, y + h, 0x0D2416, 245);
        q(vc, mat, light, g, x, groundY, x + w, groundY + 1.0f, 0x1E6030, 255);

        float ufoW = Math.max(14.0f, Math.min(30.0f, w * 0.26f));
        float ufoH = ufoW * 0.38f;
        double ufoSwayX = Math.sin(t * 1.4) * (w * 0.15f);
        double ufoSwayY = Math.cos(t * 2.0) * (h * 0.035f);
        float ufoX = x + (w - ufoW) * 0.5f + (float) ufoSwayX;
        float ufoY = y + h * 0.14f + (float) ufoSwayY;

        double abductCycle = (t * 0.7) % 6.0;
        boolean beamActive = (abductCycle >= 0.8 && abductCycle < 5.4);

        float topBeamW = ufoW * 0.35f;
        float topBeamX = ufoX + (ufoW - topBeamW) * 0.5f;
        float botBeamW = ufoW * 1.55f;
        float botBeamX = ufoX + (ufoW - botBeamW) * 0.5f;

        if (beamActive) {
            float beamAlpha = 75.0f + 25.0f * (float) Math.sin(t * 4.0);
            int steps = 8;
            float stepH = (groundY - (ufoY + ufoH)) / steps;
            for (int s = 0; s < steps; s++) {
                float f1 = (float) s / steps;
                float bx1 = topBeamX + f1 * (botBeamX - topBeamX);
                float bw1 = topBeamW + f1 * (botBeamW - topBeamW);
                float by1 = ufoY + ufoH + s * stepH;
                float by2 = by1 + stepH;
                q(vc, mat, light, g, bx1, by1, bx1 + bw1, by2, beamCol, (int) beamAlpha);
            }

            for (int r = 0; r < 3; r++) {
                double ringPhase = (t * 1.2 + r * 0.33) % 1.0;
                float ry = groundY - (float) (ringPhase * (groundY - (ufoY + ufoH)));
                float rf = 1.0f - (float) ringPhase;
                float rx = topBeamX + rf * (botBeamX - topBeamX);
                float rw = topBeamW + rf * (botBeamW - topBeamW);
                int ringAlpha = (int) ((1.0 - ringPhase) * 160);
                q(vc, mat, light, g, rx, ry, rx + rw, ry + 1.0f, 0xFFFFFF, ringAlpha);
            }
        }

        float cowW = Math.max(6.0f, Math.min(11.0f, ufoW * 0.40f));
        float cowH = cowW * 0.65f;
        float cowFloorY = groundY - cowH;
        float cowTargetY = ufoY + ufoH * 0.55f;

        float cowX = ufoX + (ufoW - cowW) * 0.5f;
        float cowY = cowFloorY;
        boolean cowVisible = true;

        if (abductCycle < 1.0) {
            cowY = cowFloorY;
            cowX += (float) (Math.sin(t * 3.0) * 0.5f);
        } else if (abductCycle < 4.2) {
            float liftFrac = (float) ((abductCycle - 1.0) / 3.2);
            cowY = cowFloorY - liftFrac * (cowFloorY - cowTargetY);
            cowX += (float) Math.sin(t * 6.0) * 1.2f;
        } else if (abductCycle < 4.8) {
            cowVisible = false;
            q(vc, mat, light, g, ufoX + ufoW * 0.38f, ufoY + ufoH * 0.6f,
                    ufoX + ufoW * 0.62f, ufoY + ufoH + 1.5f, 0xFFFFFF, 220);
        } else if (abductCycle < 5.6) {
            float landFrac = (float) ((abductCycle - 4.8) / 0.8);
            cowY = cowTargetY + landFrac * (cowFloorY - cowTargetY);
        } else {
            cowY = cowFloorY;
        }

        if (cowVisible) {
            float bY1 = cowY + cowH * 0.25f;
            float bY2 = cowY + cowH * 0.75f;
            float spot1Y = cowY + cowH * 0.65f;
            float spot2Y = cowY + cowH * 0.40f;

            q(vc, mat, light, g, cowX, bY1, cowX + cowW * 0.25f, bY2, 0xFFFFFF, 255);

            q(vc, mat, light, g, cowX + cowW * 0.25f, bY1, cowX + cowW * 0.55f, spot1Y, 0x111111, 255);
            q(vc, mat, light, g, cowX + cowW * 0.25f, spot1Y, cowX + cowW * 0.55f, bY2, 0xFFFFFF, 255);
            q(vc, mat, light, g, cowX + cowW * 0.55f, bY1, cowX + cowW * 0.70f, bY2, 0xFFFFFF, 255);
            q(vc, mat, light, g, cowX + cowW * 0.70f, bY1, cowX + cowW * 0.90f, spot2Y, 0xFFFFFF, 255);
            q(vc, mat, light, g, cowX + cowW * 0.70f, spot2Y, cowX + cowW * 0.90f, bY2, 0x111111, 255);
            q(vc, mat, light, g, cowX + cowW * 0.90f, bY1, cowX + cowW, bY2, 0xFFFFFF, 255);
            q(vc, mat, light, g, cowX, cowY, cowX + cowW * 0.35f, cowY + cowH * 0.35f, 0xFFFFFF, 255);
            q(vc, mat, light, g, cowX, cowY + cowH * 0.35f, cowX + cowW * 0.20f, cowY + cowH * 0.55f, 0xFF99AA, 255);
            q(vc, mat, light, g, cowX + cowW * 0.20f, cowY + cowH * 0.35f, cowX + cowW * 0.35f, cowY + cowH * 0.55f, 0xFFFFFF, 255);

            float legW = Math.max(0.8f, cowW * 0.14f);
            q(vc, mat, light, g, cowX + cowW * 0.1f, cowY + cowH * 0.75f, cowX + cowW * 0.1f + legW, cowY + cowH, 0x111111, 255);
            q(vc, mat, light, g, cowX + cowW * 0.3f, cowY + cowH * 0.75f, cowX + cowW * 0.3f + legW, cowY + cowH, 0x111111, 255);
            q(vc, mat, light, g, cowX + cowW * 0.6f, cowY + cowH * 0.75f, cowX + cowW * 0.6f + legW, cowY + cowH, 0x111111, 255);
            q(vc, mat, light, g, cowX + cowW * 0.8f, cowY + cowH * 0.75f, cowX + cowW * 0.8f + legW, cowY + cowH, 0x111111, 255);
        }

        float domeW = ufoW * 0.45f;
        float domeH = ufoH * 0.55f;
        float domeX = ufoX + (ufoW - domeW) * 0.5f;
        float domeY = ufoY;
        q(vc, mat, light, g, domeX, domeY, domeX + domeW, domeY + domeH, 0x66EEFF, 220);
        q(vc, mat, light, g, domeX + domeW * 0.2f, domeY + domeH * 0.2f, domeX + domeW * 0.5f, domeY + domeH * 0.45f, 0xFFFFFF, 250);

        float saucerMidY = domeY + domeH * 0.7f;
        q(vc, mat, light, g, ufoX, saucerMidY, ufoX + ufoW, saucerMidY + ufoH * 0.45f, ufoBodyCol, 255);
        q(vc, mat, light, g, ufoX + ufoW * 0.15f, saucerMidY + ufoH * 0.45f, ufoX + ufoW * 0.85f, saucerMidY + ufoH * 0.65f, blendColor(ufoBodyCol, 0x000000, 0.4f), 255);

        int lightCount = 5;
        float lightStep = ufoW / (lightCount + 1);
        int lightCycle = (int) (t * 6.0);
        for (int l = 0; l < lightCount; l++) {
            float lx = ufoX + (l + 1) * lightStep;
            float ly = saucerMidY + ufoH * 0.18f;
            float lSize = Math.max(1.0f, ufoW * 0.05f);
            boolean on = ((lightCycle + l) % lightCount) == 0;
            int lCol = on ? 0xFFEE00 : (rainbow ? getRainbowColor(t * 0.5 + l * 0.2) : 0x00FF88);
            q(vc, mat, light, g, lx - lSize * 0.5f, ly - lSize * 0.5f, lx + lSize * 0.5f, ly + lSize * 0.5f, lCol, on ? 255 : 120);
        }
    }

    private static void renderDinoRunner(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int color = rainbow ? getRainbowColor(t * 0.3) : baseColor;

        float groundY = y + h * 0.78f;
        q(vc, mat, light, g, x, groundY, x + w, groundY + 1.0f, color, 240);

        float speed = Math.max(36.0f, w * 0.40f);
        float groundScroll = (float) ((t * speed) % 20.0);
        for (float gx = x - groundScroll; gx < x + w; gx += 20.0f) {
            q(vc, mat, light, g, gx, groundY + 2.0f, gx + 2.0f, groundY + 3.0f, color, 140);
            q(vc, mat, light, g, gx + 9.0f, groundY + 3.0f, gx + 13.0f, groundY + 4.0f, color, 100);
        }

        float trackLen = w + 35.0f;
        double obsProgress = ((t * speed) % trackLen) / trackLen;
        float obsX = (x + w + 15.0f) - (float) (obsProgress * trackLen);

        float dinoW = Math.max(8.0f, Math.min(14.0f, h * 0.34f));
        float dinoH = dinoW * 1.15f;
        float dinoX = x + Math.max(8.0f, w * 0.16f);

        float relDist = obsX - dinoX;
        float jumpSpan = 34.0f;
        float jumpStartDist = 22.0f;
        boolean inJump = (relDist >= -12.0f && relDist <= jumpStartDist);
        float jumpY = 0.0f;
        if (inJump) {
            float jFrac = (jumpStartDist - relDist) / jumpSpan;
            jumpY = (float) (Math.sin(jFrac * Math.PI) * Math.max(8.0f, h * 0.38f));
        }

        float curDinoY = groundY - dinoH - jumpY;
        float px = dinoW / 10.0f;

        q(vc, mat, light, g, dinoX + px * 4.0f, curDinoY, dinoX + px * 10.0f, curDinoY + px * 4.0f, color, 255);
        q(vc, mat, light, g, dinoX + px * 6.5f, curDinoY + px * 1.0f, dinoX + px * 7.5f, curDinoY + px * 2.0f, 0x000000, 255);
        q(vc, mat, light, g, dinoX + px * 2.0f, curDinoY + px * 4.0f, dinoX + px * 8.0f, curDinoY + px * 8.0f, color, 255);
        q(vc, mat, light, g, dinoX, curDinoY + px * 5.0f, dinoX + px * 2.0f, curDinoY + px * 7.0f, color, 255);
        q(vc, mat, light, g, dinoX + px * 8.0f, curDinoY + px * 5.5f, dinoX + px * 9.5f, curDinoY + px * 6.5f, color, 255);

        boolean legToggle = ((int) (t * 12.0) % 2 == 0);
        if (inJump) {
            q(vc, mat, light, g, dinoX + px * 3.5f, curDinoY + px * 8.0f, dinoX + px * 5.0f, curDinoY + px * 9.5f, color, 255);
            q(vc, mat, light, g, dinoX + px * 6.0f, curDinoY + px * 8.0f, dinoX + px * 7.5f, curDinoY + px * 9.5f, color, 255);
        } else {
            if (legToggle) {
                q(vc, mat, light, g, dinoX + px * 3.5f, curDinoY + px * 8.0f, dinoX + px * 5.0f, curDinoY + dinoH, color, 255);
                q(vc, mat, light, g, dinoX + px * 6.0f, curDinoY + px * 8.0f, dinoX + px * 7.5f, curDinoY + px * 9.5f, color, 255);
            } else {
                q(vc, mat, light, g, dinoX + px * 3.5f, curDinoY + px * 8.0f, dinoX + px * 5.0f, curDinoY + px * 9.5f, color, 255);
                q(vc, mat, light, g, dinoX + px * 6.0f, curDinoY + px * 8.0f, dinoX + px * 7.5f, curDinoY + dinoH, color, 255);
            }
        }

        if (obsX >= x - 12.0f && obsX <= x + w + 12.0f) {
            float cactW = Math.max(3.5f, dinoW * 0.45f);
            float cactH = dinoH * 0.85f;
            float cactY = groundY - cactH;
            q(vc, mat, light, g, obsX + cactW * 0.35f, cactY, obsX + cactW * 0.65f, groundY, color, 250);
            q(vc, mat, light, g, obsX, cactY + cactH * 0.30f, obsX + cactW * 0.35f, cactY + cactH * 0.45f, color, 250);
            q(vc, mat, light, g, obsX, cactY + cactH * 0.15f, obsX + cactW * 0.15f, cactY + cactH * 0.45f, color, 250);
            q(vc, mat, light, g, obsX + cactW * 0.65f, cactY + cactH * 0.45f, obsX + cactW, cactY + cactH * 0.60f, color, 250);
            q(vc, mat, light, g, obsX + cactW * 0.85f, cactY + cactH * 0.30f, obsX + cactW, cactY + cactH * 0.60f, color, 250);
        }

        float cloudSpeed = speed * 0.25f;
        float cloudTrack = w + 45.0f;
        float cloudX = (x + w + 20.0f) - (float) (((t * cloudSpeed) % cloudTrack));
        float cloudY = y + h * 0.20f;
        float cloudW = dinoW * 1.5f;
        float cloudH = cloudW * 0.45f;
        q(vc, mat, light, g, cloudX, cloudY + cloudH * 0.35f, cloudX + cloudW, cloudY + cloudH, color, 140);
        q(vc, mat, light, g, cloudX + cloudW * 0.25f, cloudY, cloudX + cloudW * 0.75f, cloudY + cloudH * 0.4f, color, 140);

        int scoreVal = (int) (t * 8.0) % 9999;
        float scoreSize = Math.max(1.8f, h * 0.12f);
        float scoreY = y + 2.0f;
        float curSX = x + w - scoreSize * 6.5f - 2.0f;
        draw7SegmentDigit(vc, mat, light, g, curSX, scoreY, scoreSize, scoreSize * 1.5f, (scoreVal / 1000) % 10, color, blendColor(color, 0x000000, 0.85f));
        curSX += scoreSize * 1.3f;
        draw7SegmentDigit(vc, mat, light, g, curSX, scoreY, scoreSize, scoreSize * 1.5f, (scoreVal / 100) % 10, color, blendColor(color, 0x000000, 0.85f));
        curSX += scoreSize * 1.3f;
        draw7SegmentDigit(vc, mat, light, g, curSX, scoreY, scoreSize, scoreSize * 1.5f, (scoreVal / 10) % 10, color, blendColor(color, 0x000000, 0.85f));
        curSX += scoreSize * 1.3f;
        draw7SegmentDigit(vc, mat, light, g, curSX, scoreY, scoreSize, scoreSize * 1.5f, scoreVal % 10, color, blendColor(color, 0x000000, 0.85f));
    }

    private static void renderSynthwaveHighway(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                               float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        float horizonY = y + h * 0.44f;
        float midX = x + w * 0.5f;

        q(vc, mat, light, g, x, y, x + w, horizonY, 0x120422, 255);

        float sunR = Math.max(9.0f, Math.min(22.0f, h * 0.32f));
        float sunY = horizonY - sunR * 0.45f;
        float sunTop = sunY - sunR;

        int starCount = lowDetail ? 8 : 16;
        for (int i = 0; i < starCount; i++) {
            int hash = (i * 92837111) ^ 0x4B3C2A1;
            float sx = x + ((hash & 0xFF) / 255.0f) * w;
            float sy = y + (((hash >> 8) & 0xFF) / 255.0f) * (horizonY - y) * 0.55f;
            if (sx >= midX - sunR * 1.15f && sx <= midX + sunR * 1.15f && sy >= sunTop && sy <= horizonY) {
                continue;
            }
            float twinkle = 0.5f + 0.5f * (float) Math.sin(t * 2.5 + i);
            int sa = (int) (twinkle * 180 + 40);
            q(vc, mat, light, g, sx, sy, sx + 1.0f, sy + 1.0f, 0xFFFFFF, sa);
        }

        int sunSteps = 10;
        float sunStepH = (sunR * 2.0f) / sunSteps;

        for (int i = 0; i < sunSteps; i++) {
            float sy1 = sunY - sunR + i * sunStepH;
            float sy2 = sy1 + sunStepH;
            if (sy1 >= horizonY - 0.5f) continue;
            if (sy2 > horizonY - 0.5f) sy2 = horizonY - 0.5f;

            float dy = (sy1 + sy2) * 0.5f - sunY;
            if (Math.abs(dy) >= sunR) continue;

            float sw = (float) Math.sqrt(sunR * sunR - dy * dy) * 1.85f;
            float gap = (i >= 5) ? (0.8f + (i - 5) * 0.4f) : 0.0f;
            float drawY2 = Math.max(sy1 + 0.5f, sy2 - gap);
            if (drawY2 <= sy1) continue;

            float frac = (float) i / sunSteps;
            int sCol = blendColor(0xFFEE22, 0xFF0066, frac);
            q(vc, mat, light, g, midX - sw * 0.5f, sy1, midX + sw * 0.5f, drawY2, sCol, 255);
        }

        q(vc, mat, light, g, x, horizonY - 0.5f, x + w, horizonY + 0.5f, 0xFF00AA, 255);

        float roadH = (y + h) - horizonY;
        float topRoadW = Math.max(6.0f, w * 0.12f);
        float botRoadW = w * 0.82f;
        int gridCol = rainbow ? getRainbowColor(t * 0.3) : (baseColor == 0x00FF66 ? 0x00E5FF : baseColor);
        int curbCol = 0xFF00D4;

        int slices = lowDetail ? 16 : 24;
        for (int i = 0; i < slices; i++) {
            float yA = horizonY + (float) i / slices * roadH;
            float yB = horizonY + (float) (i + 1) / slices * roadH;
            float prog = (float) (i + 0.5f) / slices;
            float curRoadW = topRoadW + prog * (botRoadW - topRoadW);

            float leftEdge = midX - curRoadW * 0.5f;
            float rightEdge = midX + curRoadW * 0.5f;

            q(vc, mat, light, g, x, yA, leftEdge, yB, 0x07020E, 255);
            q(vc, mat, light, g, rightEdge, yA, x + w, yB, 0x07020E, 255);
            q(vc, mat, light, g, leftEdge, yA, rightEdge, yB, 0x10081E, 255);

            float curbThick = Math.max(0.8f, 1.6f * prog);
            q(vc, mat, light, g, leftEdge - curbThick, yA, leftEdge, yB, curbCol, 255);
            q(vc, mat, light, g, rightEdge, yA, rightEdge + curbThick, yB, curbCol, 255);
        }

        float carW = Math.round(Math.max(14.0f, Math.min(26.0f, w * 0.22f)));
        float carH = Math.round(carW * 0.52f);
        float carBaseY = Math.round(y + h - carH - 1.5f);
        double carSteer = Math.sin(t * 1.5) * (botRoadW * 0.16f);
        float carX = Math.round(midX + (float) carSteer - carW * 0.5f);

        double speedPhase = (t * 2.2) % 1.0;
        int rungs = lowDetail ? 6 : 9;
        for (int r = 0; r < rungs; r++) {
            double prog = (r + speedPhase) / rungs;
            double curve = prog * prog;
            float ry = horizonY + (float) (curve * roadH);
            if (ry >= carBaseY) continue;

            float curRoadW = topRoadW + (float) (curve * (botRoadW - topRoadW));
            float thick = Math.max(0.8f, (float) (curve * 2.4f));
            q(vc, mat, light, g, midX - curRoadW * 0.5f, ry, midX + curRoadW * 0.5f, ry + thick, gridCol, (int) (curve * 190 + 55));

            if ((r & 1) != 0) {
                float dashW = Math.max(0.8f, (float) (curve * 2.2f));
                float dashLen = Math.max(1.4f, (float) (curve * 4.2f));
                q(vc, mat, light, g, midX - dashW * 0.5f, ry, midX + dashW * 0.5f, ry + dashLen, 0xFFEE00, 240);
            }
        }

        q(vc, mat, light, g, carX + 1.0f, carBaseY + carH, carX + carW - 1.0f, carBaseY + carH + 1.5f, 0x000000, 255);

        float cabY1 = carBaseY + carH * 0.08f;
        float cabY2 = carBaseY + carH * 0.44f;
        float winY1 = carBaseY + carH * 0.15f;
        float winY2 = carBaseY + carH * 0.38f;
        float winX1 = carX + carW * 0.22f;
        float winX2 = carX + carW * 0.78f;

        q(vc, mat, light, g, carX + carW * 0.16f, cabY1, carX + carW * 0.84f, winY1, 0x2A1438, 255);
        q(vc, mat, light, g, carX + carW * 0.16f, winY1, winX1, cabY2, 0x2A1438, 255);
        q(vc, mat, light, g, winX1, winY1, winX2, winY2, 0x00D9F5, 255);
        q(vc, mat, light, g, winX1, winY2, winX2, cabY2, 0x2A1438, 255);
        q(vc, mat, light, g, winX2, winY1, carX + carW * 0.84f, cabY2, 0x2A1438, 255);

        float spY1 = carBaseY + carH * 0.36f;
        float spY2 = carBaseY + carH * 0.44f;
        q(vc, mat, light, g, carX - 1.0f, spY1, carX + carW + 1.0f, spY2, 0xFF0055, 255);

        float tlY1 = carBaseY + carH * 0.54f;
        float tlY2 = carBaseY + carH * 0.74f;

        q(vc, mat, light, g, carX, cabY2, carX + carW, tlY1, 0x1A0E24, 255);

        float tl1X1 = carX + carW * 0.08f;
        float tl1X2 = carX + carW * 0.38f;
        float tl2X1 = carX + carW * 0.62f;
        float tl2X2 = carX + carW * 0.92f;

        q(vc, mat, light, g, carX, tlY1, tl1X1, tlY2, 0x1A0E24, 255);
        q(vc, mat, light, g, tl1X1, tlY1, tl1X2, tlY2, 0xFF0033, 255);
        q(vc, mat, light, g, tl1X2, tlY1, tl2X1, tlY2, 0x120818, 255);
        q(vc, mat, light, g, tl2X1, tlY1, tl2X2, tlY2, 0xFF0033, 255);
        q(vc, mat, light, g, tl2X2, tlY1, carX + carW, tlY2, 0x1A0E24, 255);

        q(vc, mat, light, g, carX, tlY2, carX + carW, carBaseY + carH, 0x100618, 255);

        boolean boostFlame = ((int) (t * 10.0) % 3) != 0;
        if (boostFlame) {
            float flH = Math.max(1.0f, carH * 0.22f);
            q(vc, mat, light, g, carX + carW * 0.20f, carBaseY + carH, carX + carW * 0.27f, carBaseY + carH + flH, 0x00E5FF, 255);
            q(vc, mat, light, g, carX + carW * 0.73f, carBaseY + carH, carX + carW * 0.80f, carBaseY + carH + flH, 0x00E5FF, 255);
        }
    }

    private static void renderTetris(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                     float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int cols = 10;
        int rows = 16;
        float maxCellW = (w * 0.58f) / cols;
        float maxCellH = (h * 0.90f) / rows;
        float cellSize = Math.max(1.5f, Math.min(maxCellW, maxCellH));
        float gridW = cols * cellSize;
        float gridH = rows * cellSize;

        boolean wide = w >= gridW + 28.0f;
        float panelW = wide ? (gridW + 26.0f) : gridW;
        float startX = x + (w - panelW) * 0.5f;
        float startY = y + (h - gridH) * 0.5f;

        q(vc, mat, light, g, startX - 1.0f, startY - 1.0f, startX + gridW + 1.0f, startY + gridH + 1.0f, 0x1A2234, 255);
        q(vc, mat, light, g, startX, startY, startX + gridW, startY + gridH, 0x070B14, 255);

        double cycle = (t * 1.35) % 16.0;

        int[][] stack = new int[rows][cols];

        int[] r15 = { 6, 6, 7, 7, 2, 2, 5, 5, 4, 0 };
        int[] r14 = { 6, 7, 7, 5, 2, 2, 5, 4, 4, 0 };
        int[] r13 = { 6, 0, 0, 0, 0, 0, 7, 7, 5, 0 };

        for (int c = 0; c < cols; c++) {
            stack[15][c] = r15[c];
            stack[14][c] = r14[c];
            stack[13][c] = r13[c];
        }

        if (cycle >= 3.2) {
            stack[13][3] = 3; stack[13][4] = 3; stack[13][5] = 3;
            stack[12][4] = 3;
        }

        if (cycle >= 6.7) {
            stack[13][1] = 2; stack[13][2] = 2;
            stack[12][1] = 2; stack[12][2] = 2;
        }

        if (cycle >= 9.6 && cycle < 10.8) {
            stack[12][9] = 1; stack[13][9] = 1; stack[14][9] = 1; stack[15][9] = 1;
        }

        boolean lineClearFlash = (cycle >= 9.8 && cycle < 10.6);
        boolean rowsCleared = (cycle >= 10.6);

        if (rowsCleared) {
            for (int r = 15; r >= 3; r--) {
                for (int c = 0; c < cols; c++) {
                    stack[r][c] = stack[r - 3][c];
                }
            }
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < cols; c++) {
                    stack[r][c] = 0;
                }
            }
        }

        if (cycle >= 14.2) {
            stack[14][6] = 4; stack[14][7] = 4;
            stack[15][5] = 4; stack[15][6] = 4;
        }

        if (cycle < 3.2) {
            float prog = (float) (cycle / 3.2);
            int curRow = Math.min(12, (int) (prog * 13));
            int curCol = 3;
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow + 1, 3, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol + 1, curRow + 1, 3, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol + 2, curRow + 1, 3, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol + 1, curRow, 3, rainbow, t);
        } else if (cycle >= 3.5 && cycle < 6.7) {
            float prog = (float) ((cycle - 3.5) / 3.2);
            int curRow = Math.min(12, (int) (prog * 13));
            int curCol = 1;
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow, 2, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol + 1, curRow, 2, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow + 1, 2, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol + 1, curRow + 1, 2, rainbow, t);
        } else if (cycle >= 7.0 && cycle < 9.6) {
            float prog = (float) ((cycle - 7.0) / 2.6);
            int curRow = Math.min(12, (int) (prog * 13));
            int curCol = 9;
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow, 1, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow + 1, 1, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow + 2, 1, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow + 3, 1, rainbow, t);
        } else if (cycle >= 11.0 && cycle < 14.2) {
            float prog = (float) ((cycle - 11.0) / 3.2);
            int curRow = Math.min(14, (int) (prog * 15));
            int curCol = 6;
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow, 4, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol + 1, curRow, 4, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol - 1, curRow + 1, 4, rainbow, t);
            drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, curCol, curRow + 1, 4, rainbow, t);
        }

        for (int r = 0; r < rows; r++) {
            boolean isClearingRow = lineClearFlash && (r >= 13 && r <= 15);
            for (int c = 0; c < cols; c++) {
                int code = stack[r][c];
                if (code > 0) {
                    if (isClearingRow) {
                        boolean flashOn = ((int) (t * 16.0) % 2 == 0);
                        int flashCol = flashOn ? 0xFFFFFF : 0xFFEE44;
                        float bx = startX + c * cellSize;
                        float by = startY + r * cellSize;
                        q(vc, mat, light, g, bx, by, bx + cellSize - 0.5f, by + cellSize - 0.5f, flashCol, 255);
                    } else {
                        drawTetrisBlock(vc, mat, light, g, startX, startY, cellSize, c, r, code, rainbow, t);
                    }
                }
            }
        }

        if (wide) {
            float sideX = startX + gridW + 4.0f;
            float sideW = 22.0f;

            int score = (cycle < 10.0) ? 450 : 2250;
            float scoreDigitW = Math.max(1.8f, cellSize * 0.75f);
            float scoreY = startY + 2.0f;
            draw7SegmentDigit(vc, mat, light, g, sideX, scoreY, scoreDigitW, scoreDigitW * 1.5f, (score / 1000) % 10, 0xFFFFAA, 0x332211);
            draw7SegmentDigit(vc, mat, light, g, sideX + scoreDigitW * 1.3f, scoreY, scoreDigitW, scoreDigitW * 1.5f, (score / 100) % 10, 0xFFFFAA, 0x332211);
            draw7SegmentDigit(vc, mat, light, g, sideX + scoreDigitW * 2.6f, scoreY, scoreDigitW, scoreDigitW * 1.5f, (score / 10) % 10, 0xFFFFAA, 0x332211);
            draw7SegmentDigit(vc, mat, light, g, sideX + scoreDigitW * 3.9f, scoreY, scoreDigitW, scoreDigitW * 1.5f, score % 10, 0xFFFFAA, 0x332211);

            float nextBoxY = startY + gridH * 0.35f;
            float nextBoxH = gridH * 0.40f;
            drawOutlineBox(vc, mat, light, g, sideX, nextBoxY, sideW, nextBoxH, 0.8f, 0x335577, 200);

            int nextPiece = (cycle < 3.5) ? 2 : ((cycle < 7.0) ? 1 : ((cycle < 11.0) ? 4 : 3));
            float nCell = Math.max(1.6f, cellSize * 0.7f);
            float nMidX = sideX + (sideW - nCell * 3.0f) * 0.5f;
            float nMidY = nextBoxY + (nextBoxH - nCell * 2.0f) * 0.5f;

            if (nextPiece == 1) {
                for (int i = 0; i < 4; i++) q(vc, mat, light, g, nMidX + i * nCell, nMidY, nMidX + (i + 1) * nCell - 0.5f, nMidY + nCell - 0.5f, 0x00E5FF, 255);
            } else if (nextPiece == 2) {
                q(vc, mat, light, g, nMidX + 0.5f * nCell, nMidY, nMidX + 2.5f * nCell - 0.5f, nMidY + 2.0f * nCell - 0.5f, 0xFFDD00, 255);
            } else if (nextPiece == 3) {
                q(vc, mat, light, g, nMidX, nMidY + nCell, nMidX + 3.0f * nCell - 0.5f, nMidY + 2.0f * nCell - 0.5f, 0xBB00FF, 255);
                q(vc, mat, light, g, nMidX + nCell, nMidY, nMidX + 2.0f * nCell - 0.5f, nMidY + nCell - 0.5f, 0xBB00FF, 255);
            } else {
                q(vc, mat, light, g, nMidX + nCell, nMidY, nMidX + 3.0f * nCell - 0.5f, nMidY + nCell - 0.5f, 0x00FF44, 255);
                q(vc, mat, light, g, nMidX, nMidY + nCell, nMidX + 2.0f * nCell - 0.5f, nMidY + 2.0f * nCell - 0.5f, 0x00FF44, 255);
            }
        }
    }

    private static void drawTetrisBlock(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float gridX, float gridY, float cellSize, int col, int row, int colorCode,
                                       boolean rainbow, double t) {
        float bx = gridX + col * cellSize;
        float by = gridY + row * cellSize;
        int color = switch (colorCode) {
            case 1 -> 0x00E5FF;
            case 2 -> 0xFFDD00;
            case 3 -> 0xBB00FF;
            case 4 -> 0x00FF44;
            case 5 -> 0xFF2233;
            case 6 -> 0x2255FF;
            case 7 -> 0xFF8800;
            default -> 0xFFFFFF;
        };
        if (rainbow) color = getRainbowColor(t * 0.3 + colorCode * 0.15);

        float bw = cellSize - 0.5f;
        float bh = cellSize - 0.5f;
        float inset = Math.max(0.4f, cellSize * 0.20f);

        int highlightCol = blendColor(color, 0xFFFFFF, 0.40f);
        int shadowCol = blendColor(color, 0x000000, 0.35f);

        q(vc, mat, light, g, bx, by, bx + bw, by + inset, highlightCol, 255);
        q(vc, mat, light, g, bx, by + inset, bx + inset, by + bh - inset, highlightCol, 255);
        q(vc, mat, light, g, bx + bw - inset, by + inset, bx + bw, by + bh - inset, shadowCol, 255);
        q(vc, mat, light, g, bx, by + bh - inset, bx + bw, by + bh, shadowCol, 255);
        q(vc, mat, light, g, bx + inset, by + inset, bx + bw - inset, by + bh - inset, color, 255);
    }

    private static void renderFlappyBird(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float groundH = Math.max(6.0f, h * 0.18f);
        float groundY = y + h - groundH;
        float playableH = groundY - y;

        int skyCol = rainbow ? getRainbowColor(t * 0.2) : (baseColor == 0x00FF66 ? 0x4EC0CA : blendColor(baseColor, 0x4EC0CA, 0.6f));
        q(vc, mat, light, g, x, y, x + w, groundY, skyCol, 255);

        float cW = Math.max(14.0f, Math.min(32.0f, w * 0.22f));
        float cH = Math.max(6.0f, cW * 0.42f);
        float totalCloudTrack = w + cW * 2.5f + 30.0f;
        double cloudSpeed = 7.0;

        float c1X = (x + w + cW) - (float) (((t * cloudSpeed) % totalCloudTrack));
        float c2X = (x + w + cW) - (float) (((t * cloudSpeed + totalCloudTrack * 0.5) % totalCloudTrack));
        float c1Y = y + h * 0.10f;
        float c2Y = y + h * 0.20f;

        drawFlappyCloud(vc, mat, light, g, c1X, c1Y, cW, cH, skyCol);
        drawFlappyCloud(vc, mat, light, g, c2X, c2Y, cW * 1.25f, cH * 1.15f, skyCol);

        float pipeW = Math.max(8.0f, Math.min(18.0f, w * 0.13f));
        float pipeGap = Math.max(16.0f, playableH * 0.46f);
        float pipeSpacing = Math.max(34.0f, (w + 40.0f) / 2.5f);
        int numPipes = 3;
        float totalTrack = numPipes * pipeSpacing;
        double scrollSpeed = 24.0;

        int pipeCol = 0x73BF2E;
        int pipeShadow = 0x558022;
        int pipeRimCol = 0x86D438;

        float[] gapCenters = new float[numPipes];
        float[] pipeXs = new float[numPipes];

        for (int p = 0; p < numPipes; p++) {
            float px = (x + w + 10.0f) - (float) (((t * scrollSpeed + p * pipeSpacing) % totalTrack));
            pipeXs[p] = px;

            float gapFrac = (p == 0) ? 0.35f : ((p == 1) ? 0.65f : 0.48f);
            float gapY = y + gapFrac * (playableH - pipeGap);
            gapCenters[p] = gapY + pipeGap * 0.5f;

            if (px < x - pipeW || px > x + w + 4.0f) continue;

            float upRimH = 4.0f;
            float upShaftBot = gapY;
            float upRimTop = upShaftBot - upRimH;

            q(vc, mat, light, g, px + 1.0f, y, px + pipeW - 3.0f, upRimTop, pipeCol, 255);
            q(vc, mat, light, g, px + pipeW - 3.0f, y, px + pipeW - 1.0f, upRimTop, pipeShadow, 255);

            q(vc, mat, light, g, px, upRimTop, px + pipeW - 2.5f, upShaftBot, pipeRimCol, 255);
            q(vc, mat, light, g, px + pipeW - 2.5f, upRimTop, px + pipeW, upShaftBot, pipeShadow, 255);

            float lowRimTop = gapY + pipeGap;
            float lowRimBot = lowRimTop + upRimH;

            q(vc, mat, light, g, px, lowRimTop, px + pipeW - 2.5f, lowRimBot, pipeRimCol, 255);
            q(vc, mat, light, g, px + pipeW - 2.5f, lowRimTop, px + pipeW, lowRimBot, pipeShadow, 255);

            q(vc, mat, light, g, px + 1.0f, lowRimBot, px + pipeW - 3.0f, groundY, pipeCol, 255);
            q(vc, mat, light, g, px + pipeW - 3.0f, lowRimBot, px + pipeW - 1.0f, groundY, pipeShadow, 255);
        }

        q(vc, mat, light, g, x, groundY, x + w, groundY + 2.5f, 0x5EE23A, 255);
        q(vc, mat, light, g, x, groundY + 2.5f, x + w, y + h, 0xDDD088, 255);

        float dashStep = 8.0f;
        float dashPhase = (float) ((t * scrollSpeed) % dashStep);
        for (float dx = x - dashStep + dashPhase; dx < x + w; dx += dashStep) {
            q(vc, mat, light, g, dx, groundY + 3.0f, dx + 3.0f, groundY + 4.5f, 0xBCAB66, 255);
            q(vc, mat, light, g, dx + 2.0f, groundY + 5.5f, dx + 5.0f, y + h - 1.0f, 0xBCAB66, 255);
        }

        float birdW = Math.max(7.0f, Math.min(12.0f, w * 0.09f));
        float birdH = birdW * 0.72f;
        float birdX = x + w * 0.22f;

        double trackK = (t * scrollSpeed + (x + w + 10.0f + pipeW * 0.5f - birdX)) / pipeSpacing;
        int pipeK = (int) Math.floor(trackK);
        float pipeF = (float) (trackK - pipeK);
        int p0 = (pipeK % numPipes + numPipes) % numPipes;
        int p1 = ((pipeK + 1) % numPipes + numPipes) % numPipes;

        float transU = Math.max(0.0f, Math.min(1.0f, (pipeF - 0.18f) / 0.64f));
        float transS = transU * transU * (3.0f - 2.0f * transU);
        float baseCenterY = gapCenters[p0] + (gapCenters[p1] - gapCenters[p0]) * transS;

        double hopCycle = (t * 2.2) % 1.0;
        float hopOffset = (float) (Math.sin(hopCycle * Math.PI * 2.0) * -2.4f - Math.sin(hopCycle * Math.PI * 4.0) * 0.6f);
        float birdY = baseCenterY - birdH * 0.5f + hopOffset;
        birdY = Math.max(y + 2.0f, Math.min(groundY - birdH - 1.0f, birdY));

        boolean wingUp = (hopCycle < 0.35);
        float wingY = wingUp ? (birdY + birdH * 0.10f) : (birdY + birdH * 0.44f);

        q(vc, mat, light, g, birdX + birdW * 0.15f, birdY, birdX + birdW * 0.85f, birdY + birdH, 0xF8E800, 255);
        q(vc, mat, light, g, birdX + birdW * 0.85f, birdY + birdH * 0.45f, birdX + birdW + 1.5f, birdY + birdH * 0.80f, 0xE86000, 255);
        q(vc, mat, light, g, birdX + birdW * 0.55f, birdY + birdH * 0.12f, birdX + birdW * 0.82f, birdY + birdH * 0.48f, 0xFFFFFF, 255);
        q(vc, mat, light, g, birdX + birdW * 0.68f, birdY + birdH * 0.20f, birdX + birdW * 0.80f, birdY + birdH * 0.42f, 0x000000, 255);
        q(vc, mat, light, g, birdX + birdW * 0.20f, birdY + birdH * 0.60f, birdX + birdW * 0.65f, birdY + birdH * 0.95f, 0xFFFEE8, 255);
        q(vc, mat, light, g, birdX, wingY, birdX + birdW * 0.45f, wingY + birdH * 0.38f, 0xFFFFFF, 255);

        double firstPipeDelay = (w + 10.0f + pipeW * 0.5f - (birdX - x)) / scrollSpeed;
        int score = (t < firstPipeDelay) ? 0 : (int) (((t - firstPipeDelay) * scrollSpeed / pipeSpacing) + 1) % 100;
        float scoreSize = Math.max(2.2f, h * 0.14f);
        float scoreY = y + 2.0f;
        float midX = x + w * 0.5f;
        draw7SegmentDigit(vc, mat, light, g, midX - scoreSize * 1.15f, scoreY, scoreSize, scoreSize * 1.5f, (score / 10) % 10, 0xFFFFFF, 0x335566);
        draw7SegmentDigit(vc, mat, light, g, midX + scoreSize * 0.15f, scoreY, scoreSize, scoreSize * 1.5f, score % 10, 0xFFFFFF, 0x335566);
    }

    private static void drawFlappyCloud(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float cx, float cy, float cw, float ch, int skyCol) {
        if (cx < clipX1 - cw || cx > clipX2 + 4.0f) return;
        float shadeH = Math.max(1.0f, ch * 0.25f);
        int shadeCol = blendColor(skyCol, 0xFFFFFF, 0.35f);

        q(vc, mat, light, g, cx + cw * 0.18f, cy, cx + cw * 0.52f, cy + ch * 0.45f, 0xFFFFFF, 255);
        q(vc, mat, light, g, cx + cw * 0.48f, cy + ch * 0.12f, cx + cw * 0.82f, cy + ch * 0.45f, 0xFFFFFF, 255);
        q(vc, mat, light, g, cx, cy + ch * 0.45f, cx + cw, cy + ch - shadeH, 0xFFFFFF, 255);
        q(vc, mat, light, g, cx + 1.0f, cy + ch - shadeH, cx + cw - 1.0f, cy + ch, shadeCol, 255);
    }

    private static void drawOutlineBox(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float x, float y, float w, float h, float t, int colorRGB, int alpha) {
        q(vc, mat, light, g, x, y, x + w, y + t, colorRGB, alpha);
        q(vc, mat, light, g, x, y + h - t, x + w, y + h, colorRGB, alpha);
        q(vc, mat, light, g, x, y + t, x + t, y + h - t, colorRGB, alpha);
        q(vc, mat, light, g, x + w - t, y + t, x + w, y + h - t, colorRGB, alpha);
    }

    private static int getRainbowColor(double huePhase) {
        float hue = (float) (huePhase - Math.floor(huePhase));
        return Color.HSBtoRGB(hue, 0.95f, 1.0f) & 0xFFFFFF;
    }

    private static int blendColor(int c1, int c2, float ratio) {
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    private static void q(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                          float x1, float y1, float x2, float y2, int colorRGB, int alpha) {
        float minValX = Math.min(x1, x2);
        float maxValX = Math.max(x1, x2);
        float minValY = Math.min(y1, y2);
        float maxValY = Math.max(y1, y2);
        float cx1 = Math.max(clipX1, minValX);
        float cy1 = Math.max(clipY1, minValY);
        float cx2 = Math.min(clipX2, maxValX);
        float cy2 = Math.min(clipY2, maxValY);
        if (cx2 <= cx1 || cy2 <= cy1 || alpha <= 0) return;
        if (vc != null && mat != null) {
            int r = (colorRGB >> 16) & 0xFF;
            int gr = (colorRGB >> 8) & 0xFF;
            int b = colorRGB & 0xFF;
            currentZ += 0.0005f;
            float z = currentZ;
            vc.vertex(mat, cx1, cy2, z).color(r, gr, b, alpha).uv2(light).endVertex();
            vc.vertex(mat, cx2, cy2, z).color(r, gr, b, alpha).uv2(light).endVertex();
            vc.vertex(mat, cx2, cy1, z).color(r, gr, b, alpha).uv2(light).endVertex();
            vc.vertex(mat, cx1, cy1, z).color(r, gr, b, alpha).uv2(light).endVertex();
        } else if (g != null) {
            int ix1 = Math.round(cx1);
            int iy1 = Math.round(cy1);
            int ix2 = Math.max(ix1 + 1, Math.round(cx2));
            int iy2 = Math.max(iy1 + 1, Math.round(cy2));
            int argb = ((alpha & 0xFF) << 24) | (colorRGB & 0xFFFFFF);
            g.fill(ix1, iy1, ix2, iy2, argb);
        }
    }
}
