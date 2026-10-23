package com.boran.signbuilder.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;

import java.awt.Color;

public class LcdVisualAnimator {

    private static float clipX1;
    private static float clipY1;
    private static float clipX2;
    private static float clipY2;
    private static float currentZ;
    private static final int[][] TETRIS_STACK = new int[16][10];
    private static final float[] FLAPPY_GAP_CENTERS = new float[8];
    private static final float[] FLAPPY_PIPE_XS = new float[8];
    private static final float[] SUBWAY_SX = new float[8];
    private static final float[] SUBWAY_SY = new float[8];

    private static final float[][] RADAR_BLIPS = {
            { -0.45f, -0.32f, 0.85f },
            { 0.38f, -0.42f, 2.35f },
            { 0.52f, 0.36f, 3.72f },
            { -0.32f, 0.50f, 5.10f }
    };
    private static final float[][] BURGER_SEEDS = {
            { -0.45f, -0.55f }, { 0.0f, -0.72f }, { 0.45f, -0.52f }, { -0.22f, -0.32f }, { 0.25f, -0.35f }
    };
    private static final float[][] PIZZA_PEPS = {
            { -0.42f, -0.22f }, { -0.36f, 0.30f }, { 0.04f, 0.44f },
            { -0.12f, -0.45f }, { 0.38f, 0.28f }, { -0.15f, 0.05f }
    };
    private static final int[] GAS_BADGE_COLS = { 0x00DD55, 0xFFAA00, 0x00CCFF };
    private static final int[] GAS_LED_COLS = { 0x00FF66, 0xFFCC00, 0x33EEFF };
    private static final int[][] GAS_BASE_PRICES = { { 4, 2, 8, 5 }, { 4, 1, 5, 0 }, { 2, 4, 9, 0 } };
    private static final int[] TV_SMPTE = { 0xE6E6E6, 0xE6E614, 0x14E6E6, 0x14E614, 0xE614E6, 0xE61414, 0x1414E6 };
    private static final int[] TV_SUB_BARS = { 0x1414E6, 0x101014, 0xE614E6, 0x101014, 0x14E6E6, 0x101014, 0xE6E6E6 };
    private static final int[] SOLAR_P_COLORS = { 0xB8C0C8, 0x1E88FF, 0xFF4422, 0xE8C068 };
    private static final double[] SOLAR_P_SPEEDS = { 2.8, 1.65, 1.05, 0.62 };
    private static final float[] SOLAR_P_RADII = { 0.065f, 0.095f, 0.080f, 0.115f };
    private static final float[] AQUARIUM_KELP_X = { 0.10f, 0.42f, 0.60f, 0.90f };
    private static final int[] AQUARIUM_F_COLORS = { 0xFF6600, 0x00B8FF };
    private static final float[] AQUARIUM_F_DEPTHS = { 0.28f, 0.50f, 0.68f };
    private static final double[] AQUARIUM_F_SPEEDS = { 0.26, 0.34, 0.22 };
    private static final float[][] VET_PAW_COORDS = { { 0.84f, 0.72f }, { 0.86f, 0.30f } };
    private static final float[][] VET_TOES = { { -0.95f, -0.55f }, { -0.35f, -0.95f }, { 0.35f, -0.95f }, { 0.95f, -0.55f } };
    private static final float[][] POOL_POCKETS = {
            { 0.0f, 0.0f }, { 0.5f, 0.0f }, { 1.0f, 0.0f },
            { 0.0f, 1.0f }, { 0.5f, 1.0f }, { 1.0f, 1.0f }
    };
    private static final float[][] JEWELRY_FLARES = { { -1.15f, -0.15f }, { 1.15f, -0.15f }, { 0.0f, -0.75f } };


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

        currentZ = 0.002f;
        clipX1 = (visualAnim == 36) ? minX : x1;
        clipY1 = (visualAnim == 36) ? topY : y1;
        clipX2 = (visualAnim == 36) ? maxX : x2;
        clipY2 = (visualAnim == 36) ? bottomY : y2;

        double speedMult = switch (speed) {
            case 1 -> 0.55;
            case 3 -> 1.65;
            case 4 -> 2.50;
            default -> 1.00;
        };
        double t = (timeMs % 3600000L) * 0.001 * speedMult;

        switch (visualAnim) {
            case 1 -> renderFastFoodBillboard(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 2 -> renderPizzaShop(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 3 -> renderCoffeeShop(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 4 -> renderIceCreamShop(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 5 -> renderJewelryShop(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 6 -> renderCinemaBoxOffice(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 7 -> renderVetClinic(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 8 -> renderCurrencyExchange(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 9 -> renderGasStation(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 10 -> renderSmartParking(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 11 -> renderAirportBoard(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 12 -> renderSubwayMap(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 13 -> renderSoccerMatch(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 14 -> renderBasketballMatch(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 15 -> renderFormulaOne(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 16 -> renderBoxingGym(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 17 -> renderGymFitness(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 18 -> renderPoolHall(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 19 -> renderBowlingAlley(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 20 -> renderConcertStage(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 21 -> renderBreakingNews(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 22 -> renderStockChart(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 23 -> renderSecurityCctv(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 24 -> renderTvStaticBars(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 25 -> renderPacman(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 26 -> renderTetris(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 27 -> renderRetroSnake(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 28 -> renderPong(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 29 -> renderSpaceInvaders(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 30 -> renderDinoRunner(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 31 -> renderFlappyBird(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 32 -> renderSlotMachine(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 33 -> renderCreeper(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 34 -> renderSlime(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 35 -> renderSynthwaveHighway(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 36 -> renderBouncingLogo(vc, mat, light, g, minX, topY, maxX - minX, bottomY - topY, baseColor, rainbow, t);
            case 37 -> renderEqualizer(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 38 -> renderTurntable(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 39 -> renderCassetteTape(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 40 -> renderDigitalClock(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 41 -> renderHourglass(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 42 -> renderBattery(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 43 -> renderHeartbeat(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 44 -> renderRadar(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 45 -> renderCyberTerminal(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 46 -> renderMatrixRain(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 47 -> renderSiren(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 48 -> renderBiohazard(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 49 -> renderHydraulicPress(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t);
            case 50 -> renderReactorSpinner(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 51 -> renderDnaHelix(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 52 -> renderSolarSystem(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 53 -> renderBlackHole(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 54 -> renderStarfield(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 55 -> renderUfo(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 56 -> renderWeatherStation(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 57 -> renderRainstorm(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 58 -> renderPixelFire(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 59 -> renderVolcano(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
            case 60 -> renderAquarium(vc, mat, light, g, x1, y1, w, h, baseColor, rainbow, t, lowDetail);
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
        int dimCol = blendColor(col, 0x001810, 0.72f);
        q(vc, mat, light, g, x, y, x + w, y + h, 0x030B08, 255);

        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float r = Math.min(w, h) * 0.45f;
        float lineT = Math.max(0.45f, Math.min(w, h) * 0.028f);

        drawFilledCircle(vc, mat, light, g, cx, cy, r, r, lowDetail ? 14 : 22, 0x061610, 255);

        q(vc, mat, light, g, cx - r, cy - lineT * 0.4f, cx + r, cy + lineT * 0.4f, dimCol, 150);
        q(vc, mat, light, g, cx - lineT * 0.4f, cy - r, cx + lineT * 0.4f, cy + r, dimCol, 150);

        int ringSteps = lowDetail ? 20 : 32;
        drawEllipseArc(vc, mat, light, g, cx, cy, r * 0.35f, r * 0.35f, lineT * 0.8f, 0, Math.PI * 2.0, ringSteps / 2, dimCol, 160);
        drawEllipseArc(vc, mat, light, g, cx, cy, r * 0.68f, r * 0.68f, lineT * 0.8f, 0, Math.PI * 2.0, ringSteps, dimCol, 180);
        drawEllipseArc(vc, mat, light, g, cx, cy, r, r, lineT * 1.2f, 0, Math.PI * 2.0, ringSteps, col, 240);

        double sweepAngle = (t * 2.4) % (Math.PI * 2.0);
        int trailRays = lowDetail ? 8 : 14;
        for (int i = 1; i <= trailRays; i++) {
            double ta = sweepAngle - i * 0.065;
            float tx = cx + (float) Math.cos(ta) * (r * 0.96f);
            float ty = cy + (float) Math.sin(ta) * (r * 0.96f);
            int tAlpha = (int) (135 * (1.0f - (float) i / (trailRays + 1)));
            drawLine(vc, mat, light, g, cx, cy, tx, ty, lineT * 1.25f, col, tAlpha);
        }

        float tipX = cx + (float) Math.cos(sweepAngle) * (r * 0.98f);
        float tipY = cy + (float) Math.sin(sweepAngle) * (r * 0.98f);
        drawLine(vc, mat, light, g, cx, cy, tipX, tipY, lineT * 1.15f, 0xFFFFFF, 255);
        drawFilledCircle(vc, mat, light, g, cx, cy, lineT * 1.8f, lineT * 1.8f, 4, 0xFFFFFF, 255);

        float[][] blips = RADAR_BLIPS;
        float blipR = Math.max(0.9f, r * 0.11f);
        for (float[] b : blips) {
            double bAngle = Math.atan2(b[1], b[0]);
            if (bAngle < 0) bAngle += Math.PI * 2.0;
            double diff = sweepAngle - bAngle;
            if (diff < 0) diff += Math.PI * 2.0;
            if (diff < Math.PI * 1.45) {
                float fade = 1.0f - (float) (diff / (Math.PI * 1.45));
                float bx = cx + b[0] * r;
                float by = cy + b[1] * r;
                int bCol = (diff < 0.25) ? 0xFFFFFF : (rainbow ? col : 0xFF3355);
                drawFilledCircle(vc, mat, light, g, bx, by, blipR, blipR, 4, bCol, (int) (fade * 255));
                if (diff < 0.45) {
                    float ringR = blipR * (1.2f + (float) (diff * 3.5));
                    drawEllipseArc(vc, mat, light, g, bx, by, ringR, ringR, 0.45f, 0, Math.PI * 2.0, 8, bCol, (int) (fade * 190));
                }
            }
        }
    }

    private static void renderBattery(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                      float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float bw = Math.min(w * 0.74f, h * 2.35f);
        float bh = Math.min(h * 0.68f, bw * 0.48f);
        float bx = x + (w - bw) * 0.5f;
        float by = y + (h - bh) * 0.5f;
        float border = Math.max(0.9f, bh * 0.095f);

        int frameCol = rainbow ? getRainbowColor(t * 0.2) : baseColor;
        q(vc, mat, light, g, bx, by, bx + bw, by + bh, 0x080E14, 240);
        drawOutlineBox(vc, mat, light, g, bx, by, bw, bh, border, frameCol, 245);
        q(vc, mat, light, g, bx + border, by + border, bx + bw - border, by + border * 1.7f, blendColor(frameCol, 0xFFFFFF, 0.45f), 90);

        float capW = Math.max(1.4f, border * 1.65f);
        float capH = bh * 0.44f;
        q(vc, mat, light, g, bx + bw, by + (bh - capH) * 0.5f, bx + bw + capW, by + (bh + capH) * 0.5f, frameCol, 245);

        int totalCells = 7;
        int filledCells = (int) (Math.floor(t * 2.2) % (totalCells + 2));
        boolean fullFlash = filledCells > totalCells;
        if (fullFlash) filledCells = totalCells;

        float innerX = bx + border * 1.75f;
        float innerY = by + border * 1.75f;
        float innerW = bw - border * 3.5f;
        float innerH = bh - border * 3.5f;
        float cellStep = innerW / totalCells;
        float cellW = Math.max(0.8f, cellStep * 0.76f);

        for (int i = 0; i < totalCells; i++) {
            float cx = innerX + i * cellStep + (cellStep - cellW) * 0.5f;
            if (i < filledCells) {
                int cellCol = frameCol;
                if (!rainbow) {
                    if (filledCells <= 2) cellCol = 0xFF3344;
                    else if (filledCells <= 4) cellCol = 0xFFAA00;
                }
                if (fullFlash && ((int) (t * 12) % 2 == 0)) {
                    cellCol = 0xFFFFFF;
                }
                q(vc, mat, light, g, cx, innerY, cx + cellW, innerY + innerH, cellCol, 245);
                q(vc, mat, light, g, cx, innerY, cx + cellW, innerY + innerH * 0.22f, blendColor(cellCol, 0xFFFFFF, 0.45f), 210);
            } else {
                q(vc, mat, light, g, cx, innerY, cx + cellW, innerY + innerH, frameCol, 28);
            }
        }

        float midX = bx + bw * 0.5f;
        float midY = by + bh * 0.5f;
        float boltH = innerH * 0.72f;
        float boltW = boltH * 0.55f;
        int boltAlpha = (int) (185 + 70 * Math.sin(t * 10.0));
        drawLine(vc, mat, light, g, midX + boltW * 0.25f, midY - boltH * 0.5f, midX - boltW * 0.35f, midY + boltH * 0.05f, Math.max(0.75f, border * 0.85f), 0xFFEE55, boltAlpha);
        drawLine(vc, mat, light, g, midX - boltW * 0.35f, midY + boltH * 0.05f, midX + boltW * 0.35f, midY - boltH * 0.05f, Math.max(0.75f, border * 0.85f), 0xFFFFFF, boltAlpha);
        drawLine(vc, mat, light, g, midX + boltW * 0.35f, midY - boltH * 0.05f, midX - boltW * 0.25f, midY + boltH * 0.5f, Math.max(0.75f, border * 0.85f), 0xFFEE55, boltAlpha);
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
        q(vc, mat, light, g, x, y, x + w, y + h, 0x080A12, 255);

        int phase = (int) (t * 9.0) % 6;
        boolean leftActive = (phase == 0 || phase == 2);
        boolean rightActive = (phase == 3 || phase == 5);

        int leftCol = rainbow ? getRainbowColor(t * 0.5) : 0xFF1830;
        int rightCol = rainbow ? getRainbowColor(t * 0.5 + 0.5) : (baseColor == 0x00FF66 ? 0x0088FF : baseColor);

        float barH = Math.max(1.8f, h * 0.12f);
        float lampR = Math.min(w * 0.20f, (h - barH * 2.4f) * 0.42f);
        float leftCx = x + w * 0.28f;
        float rightCx = x + w * 0.72f;
        float lampCy = y + h * 0.50f;

        if (leftActive) {
            drawFilledCircle(vc, mat, light, g, leftCx, lampCy, lampR * 1.38f, lampR * 1.25f, 10, leftCol, 110);
            for (int r = 0; r < 4; r++) {
                double ra = t * 10.0 + r * (Math.PI * 0.5);
                drawLine(vc, mat, light, g, leftCx, lampCy,
                         leftCx + (float) Math.cos(ra) * lampR * 1.35f,
                         lampCy + (float) Math.sin(ra) * lampR * 1.20f,
                         Math.max(0.8f, lampR * 0.18f), 0xFFFFFF, 180);
            }
        }
        drawFilledCircle(vc, mat, light, g, leftCx, lampCy, lampR, lampR, 10, leftCol, leftActive ? 255 : 70);
        if (leftActive) {
            drawFilledCircle(vc, mat, light, g, leftCx, lampCy, lampR * 0.52f, lampR * 0.52f, 6, 0xFFFFFF, 245);
        }

        if (rightActive) {
            drawFilledCircle(vc, mat, light, g, rightCx, lampCy, lampR * 1.38f, lampR * 1.25f, 10, rightCol, 110);
            for (int r = 0; r < 4; r++) {
                double ra = -t * 10.0 + r * (Math.PI * 0.5);
                drawLine(vc, mat, light, g, rightCx, lampCy,
                         rightCx + (float) Math.cos(ra) * lampR * 1.35f,
                         lampCy + (float) Math.sin(ra) * lampR * 1.20f,
                         Math.max(0.8f, lampR * 0.18f), 0xFFFFFF, 180);
            }
        }
        drawFilledCircle(vc, mat, light, g, rightCx, lampCy, lampR, lampR, 10, rightCol, rightActive ? 255 : 70);
        if (rightActive) {
            drawFilledCircle(vc, mat, light, g, rightCx, lampCy, lampR * 0.52f, lampR * 0.52f, 6, 0xFFFFFF, 245);
        }

        float stripeW = Math.max(3.0f, barH * 1.4f);
        float scroll = (float) ((t * 12.0) % (stripeW * 2.0));
        for (int b = 0; b < 2; b++) {
            float skew = b * (stripeW * 0.35f);
            float subH = barH * 0.5f;
            for (float sx = x - stripeW * 2.0f + scroll; sx < x + w + stripeW; sx += stripeW * 2.0f) {
                q(vc, mat, light, g, sx + skew, y + b * subH, sx + skew + stripeW, y + (b + 1) * subH + 0.1f, 0xFFCC00, 235);
                q(vc, mat, light, g, sx - skew, y + h - barH + b * subH, sx - skew + stripeW, y + h - barH + (b + 1) * subH + 0.1f, 0xFFCC00, 235);
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
        if (!on && Math.max(x2 - x1, y2 - y1) < 2.2f) return;
        int col = on ? activeColor : dimColor;
        q(vc, mat, light, g, x1, y1, x2, y2, col, 255);
    }

    private static void renderStarfield(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x02040A, 255);
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float maxR = (float) Math.sqrt(w * w + h * h) * 0.52f;
        int count = lowDetail ? 22 : 38;

        for (int i = 0; i < count; i++) {
            double angle = (i * 2.3999632) % (2.0 * Math.PI);
            double speedFactor = 0.65 + 0.7 * ((i * 31) % 10) / 10.0;
            double z = 1.0 - ((t * 0.52 * speedFactor + (double) i / count) % 1.0);
            if (z <= 0.02) continue;

            double invZ = 1.0 / (z * 3.6 + 0.14);
            double r = maxR * (invZ - 0.22);
            if (r <= 0.3) continue;

            float sx = cx + (float) (Math.cos(angle) * r);
            float sy = cy + (float) (Math.sin(angle) * r);

            double zTail = Math.min(1.0, z + 0.11);
            double invZTail = 1.0 / (zTail * 3.6 + 0.14);
            double rTail = Math.max(0.1, maxR * (invZTail - 0.22));
            float tx = cx + (float) (Math.cos(angle) * rTail);
            float ty = cy + (float) (Math.sin(angle) * rTail);

            float starThick = Math.max(0.6f, (float) (0.95 / (z + 0.35)));
            int alpha = (int) Math.min(255.0, (1.0 - z) * 320.0);
            int col = rainbow ? getRainbowColor(t * 0.3 + i * 0.05) : (z > 0.45 ? 0xCCF4FF : baseColor);

            drawLine(vc, mat, light, g, tx, ty, sx, sy, starThick, col, (int) (alpha * 0.75f));
            q(vc, mat, light, g, sx - starThick * 0.6f, sy - starThick * 0.6f, sx + starThick * 0.6f, sy + starThick * 0.6f, 0xFFFFFF, alpha);
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
        int dropCount = lowDetail ? 18 : 32;
        int rainCol = rainbow ? getRainbowColor(t * 0.3) : (baseColor == 0x00FF66 ? 0x77CCFF : baseColor);

        double strikePeriod = 4.2;
        double strikePhase = (t % strikePeriod);
        boolean isLightning = strikePhase > 3.82 && ((int) (t * 28.0) % 2 == 0);

        q(vc, mat, light, g, x, y, x + w, y + h, isLightning ? 0x2A3648 : 0x050912, 255);

        float cloudH = Math.max(2.2f, h * 0.16f);
        q(vc, mat, light, g, x, y, x + w, y + cloudH, isLightning ? 0x667788 : 0x121A26, 240);

        if (isLightning) {
            float boltX = x + w * 0.48f + (float) (Math.sin(Math.floor(t / strikePeriod) * 17.0) * (w * 0.18f));
            float stepY = (h - cloudH) / 6.0f;
            float curX = boltX;
            float curY = y + cloudH * 0.6f;

            for (int seg = 0; seg < 6; seg++) {
                float offset = ((seg & 1) == 0 ? 1.0f : -1.0f) * (w * 0.055f);
                float nextX = curX + offset;
                float nextY = curY + stepY;
                drawLine(vc, mat, light, g, curX, curY, nextX, nextY, Math.max(0.9f, w * 0.025f), 0xFFEEAA, 255);
                drawLine(vc, mat, light, g, curX, curY, nextX, nextY, Math.max(0.5f, w * 0.012f), 0xFFFFFF, 255);
                if (seg == 2) {
                    drawLine(vc, mat, light, g, curX, curY, curX - offset * 1.4f, curY + stepY * 1.6f, Math.max(0.6f, w * 0.015f), 0xCCEEFF, 210);
                }
                curX = nextX;
                curY = nextY;
            }
        }

        float slantX = Math.max(1.2f, w * 0.045f);
        float dropLenY = Math.max(2.2f, h * 0.15f);
        for (int i = 0; i < dropCount; i++) {
            int hash = (i * 265443576) ^ 0x61C88647;
            double dropSpeed = 1.25 + ((hash & 0x7F) / 127.0) * 1.05;
            double xFrac = ((hash >> 8) & 0xFF) / 255.0;
            double yPhase = ((hash >> 16) & 0xFF) / 255.0;

            double dropProgress = (t * dropSpeed + yPhase) % 1.0;
            float dx = x + (float) (xFrac * w) - (float) (dropProgress * slantX * 1.5f);
            float dy = y + cloudH * 0.7f + (float) (dropProgress * (h - cloudH * 0.7f));

            drawLine(vc, mat, light, g, dx, dy, dx - slantX * 0.35f, Math.min(y + h, dy + dropLenY), 0.65f, rainCol, 200);

            if (dy + dropLenY >= y + h - 1.5f) {
                float splashR = Math.max(1.2f, w * 0.035f);
                drawEllipseArc(vc, mat, light, g, dx - slantX * 0.35f, y + h - 0.8f, splashR, splashR * 0.4f, 0.45f, 0, Math.PI * 2.0, 6, 0xCCFFFF, 160);
            }
        }
    }

    private static void renderDnaHelix(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int pairs = lowDetail ? 12 : 18;
        float stepW = w / pairs;
        float cy = y + h * 0.5f;
        float amplitude = h * 0.36f;
        float baseR = Math.max(0.95f, Math.min(w, h) * 0.055f);

        for (int i = 0; i < pairs; i++) {
            double angle = t * 2.8 + i * 0.46;
            float sinVal = (float) Math.sin(angle);
            float cosVal = (float) Math.cos(angle);

            float strand1Y = cy + sinVal * amplitude;
            float strand2Y = cy - sinVal * amplitude;
            float curX = x + (i + 0.5f) * stepW;

            int col1 = rainbow ? getRainbowColor(t * 0.3 + i * 0.05) : baseColor;
            int col2 = rainbow ? getRainbowColor(t * 0.3 + i * 0.05 + 0.5) : blendColor(baseColor, 0x00E5FF, 0.65f);

            float midY = (strand1Y + strand2Y) * 0.5f;
            int rungAlpha = (int) (75 + Math.abs(cosVal) * 145);
            drawLine(vc, mat, light, g, curX, strand1Y, curX, midY, Math.max(0.55f, stepW * 0.22f), col1, rungAlpha);
            drawLine(vc, mat, light, g, curX, midY, curX, strand2Y, Math.max(0.55f, stepW * 0.22f), col2, rungAlpha);

            float r1 = baseR * (0.82f + cosVal * 0.30f);
            float r2 = baseR * (0.82f - cosVal * 0.30f);
            int a1 = (int) (135 + cosVal * 120);
            int a2 = (int) (135 - cosVal * 120);

            if (cosVal >= 0) {
                drawFilledCircle(vc, mat, light, g, curX, strand2Y, r2, r2, 5, col2, a2);
                drawFilledCircle(vc, mat, light, g, curX, strand1Y, r1, r1, 6, col1, a1);
                q(vc, mat, light, g, curX - r1 * 0.35f, strand1Y - r1 * 0.35f, curX + r1 * 0.15f, strand1Y + r1 * 0.15f, 0xFFFFFF, a1);
            } else {
                drawFilledCircle(vc, mat, light, g, curX, strand1Y, r1, r1, 5, col1, a1);
                drawFilledCircle(vc, mat, light, g, curX, strand2Y, r2, r2, 6, col2, a2);
                q(vc, mat, light, g, curX - r2 * 0.35f, strand2Y - r2 * 0.35f, curX + r2 * 0.15f, strand2Y + r2 * 0.15f, 0xFFFFFF, a2);
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
        int darkShell = blendColor(color, 0x0A0E14, 0.78f);
        int labelCol = blendColor(color, 0xFFFFFF, 0.22f);

        float cw = Math.min(w * 0.88f, h * 2.15f);
        float ch = Math.min(h * 0.82f, cw * 0.62f);
        float cx = x + (w - cw) * 0.5f;
        float cy = y + (h - ch) * 0.5f;
        float borderT = Math.max(0.75f, ch * 0.065f);

        q(vc, mat, light, g, cx, cy, cx + cw, cy + ch, darkShell, 250);
        drawOutlineBox(vc, mat, light, g, cx, cy, cw, ch, borderT, color, 245);

        float lblX = cx + cw * 0.08f;
        float lblY = cy + ch * 0.12f;
        float lblW = cw * 0.84f;
        float lblH = ch * 0.58f;
        q(vc, mat, light, g, lblX, lblY, lblX + lblW, lblY + lblH * 0.28f, labelCol, 235);

        float winW = cw * 0.68f;
        float winH = ch * 0.44f;
        float winX = cx + (cw - winW) * 0.5f;
        float winY = cy + ch * 0.25f;
        q(vc, mat, light, g, winX, winY, winX + winW, winY + winH, 0x06080C, 255);
        drawOutlineBox(vc, mat, light, g, winX, winY, winW, winH, Math.max(0.55f, borderT * 0.7f), color, 210);

        float wheelR = Math.max(2.0f, winH * 0.34f);
        float w1X = winX + winW * 0.26f;
        float w2X = winX + winW * 0.74f;
        float wY = winY + winH * 0.50f;

        double tapeProg = 0.5 + 0.5 * Math.sin(t * 0.35);
        float spool1R = wheelR * (float) (1.32 - tapeProg * 0.42);
        float spool2R = wheelR * (float) (0.90 + tapeProg * 0.42);

        drawFilledCircle(vc, mat, light, g, w1X, wY, spool1R, spool1R, 10, 0x2B1B14, 245);
        drawFilledCircle(vc, mat, light, g, w2X, wY, spool2R, spool2R, 10, 0x2B1B14, 245);

        drawFilledCircle(vc, mat, light, g, w1X, wY, wheelR * 0.72f, wheelR * 0.72f, 8, 0xE8EEF2, 255);
        drawFilledCircle(vc, mat, light, g, w2X, wY, wheelR * 0.72f, wheelR * 0.72f, 8, 0xE8EEF2, 255);
        drawFilledCircle(vc, mat, light, g, w1X, wY, wheelR * 0.42f, wheelR * 0.42f, 6, 0x06080C, 255);
        drawFilledCircle(vc, mat, light, g, w2X, wY, wheelR * 0.42f, wheelR * 0.42f, 6, 0x06080C, 255);

        for (int i = 0; i < 3; i++) {
            double rotAngle = -t * 4.2 + (i * Math.PI / 3.0);
            float dx = (float) Math.cos(rotAngle) * (wheelR * 0.68f);
            float dy = (float) Math.sin(rotAngle) * (wheelR * 0.68f);
            drawLine(vc, mat, light, g, w1X - dx, wY - dy, w1X + dx, wY + dy, Math.max(0.55f, wheelR * 0.18f), 0xFFFFFF, 245);
            drawLine(vc, mat, light, g, w2X - dx, wY - dy, w2X + dx, wY + dy, Math.max(0.55f, wheelR * 0.18f), 0xFFFFFF, 245);
        }

        float trapTopW = cw * 0.48f;
        float trapBotW = cw * 0.58f;
        float trapY1 = cy + ch * 0.76f;
        float trapY2 = cy + ch - borderT;
        drawLine(vc, mat, light, g, cx + (cw - trapTopW) * 0.5f, trapY1, cx + (cw + trapTopW) * 0.5f, trapY1, borderT * 0.75f, color, 200);
        drawLine(vc, mat, light, g, cx + (cw - trapTopW) * 0.5f, trapY1, cx + (cw - trapBotW) * 0.5f, trapY2, borderT * 0.75f, color, 200);
        drawLine(vc, mat, light, g, cx + (cw + trapTopW) * 0.5f, trapY1, cx + (cw + trapBotW) * 0.5f, trapY2, borderT * 0.75f, color, 200);
    }

    private static void renderReactorSpinner(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                             float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float maxR = Math.min(w, h) * 0.45f;
        int color = rainbow ? getRainbowColor(t * 0.35) : baseColor;
        int hotCol = blendColor(color, 0xFFFFFF, 0.65f);

        drawFilledCircle(vc, mat, light, g, cx, cy, maxR * 1.04f, maxR * 1.04f, lowDetail ? 12 : 20, 0x060C14, 240);

        float corePulse = (float) (0.88 + 0.12 * Math.sin(t * 6.5));
        float coreR = Math.max(1.5f, maxR * 0.28f * corePulse);
        drawFilledCircle(vc, mat, light, g, cx, cy, coreR * 1.45f, coreR * 1.45f, 8, color, 135);
        drawFilledCircle(vc, mat, light, g, cx, cy, coreR, coreR, 8, hotCol, 250);
        drawFilledCircle(vc, mat, light, g, cx, cy, coreR * 0.55f, coreR * 0.55f, 6, 0xFFFFFF, 255);

        int innerArcs = 3;
        float innerR = maxR * 0.58f;
        float innerThick = Math.max(0.8f, maxR * 0.11f);
        int arcSteps = lowDetail ? 6 : 10;
        for (int i = 0; i < innerArcs; i++) {
            double startA = -t * 3.0 + i * (Math.PI * 2.0 / innerArcs);
            drawEllipseArc(vc, mat, light, g, cx, cy, innerR, innerR, innerThick, startA, startA + 1.15, arcSteps, hotCol, 235);
        }

        int outerArcs = 4;
        float outerR = maxR * 0.88f;
        float outerThick = Math.max(0.95f, maxR * 0.13f);
        for (int i = 0; i < outerArcs; i++) {
            double startA = t * 2.1 + i * (Math.PI * 2.0 / outerArcs);
            drawEllipseArc(vc, mat, light, g, cx, cy, outerR, outerR, outerThick, startA, startA + 0.95, arcSteps, color, 250);
        }

        for (int s = 0; s < 6; s++) {
            double sa = s * (Math.PI / 3.0);
            float x1 = cx + (float) Math.cos(sa) * (maxR * 0.72f);
            float y1 = cy + (float) Math.sin(sa) * (maxR * 0.72f);
            float x2 = cx + (float) Math.cos(sa) * (maxR * 0.98f);
            float y2 = cy + (float) Math.sin(sa) * (maxR * 0.98f);
            drawLine(vc, mat, light, g, x1, y1, x2, y2, Math.max(0.6f, maxR * 0.06f), 0xFFFFFF, 190);
        }
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

        float domeW = ufoW * 0.48f;
        float domeH = ufoH * 0.58f;
        float ufoCx = ufoX + ufoW * 0.5f;
        float domeCy = ufoY + domeH * 0.55f;
        drawFilledCircle(vc, mat, light, g, ufoCx, domeCy, domeW * 0.5f, domeH * 0.55f, 8, 0x66EEFF, 230);
        drawFilledCircle(vc, mat, light, g, ufoCx - domeW * 0.14f, domeCy - domeH * 0.15f, domeW * 0.16f, domeH * 0.16f, 4, 0xFFFFFF, 250);

        float saucerMidY = ufoY + domeH * 0.68f;
        float saucerCy = saucerMidY + ufoH * 0.24f;
        drawFilledCircle(vc, mat, light, g, ufoCx, saucerCy + ufoH * 0.16f, ufoW * 0.36f, ufoH * 0.18f, 6, blendColor(ufoBodyCol, 0x000000, 0.45f), 255);
        drawFilledCircle(vc, mat, light, g, ufoCx, saucerCy, ufoW * 0.50f, ufoH * 0.24f, 8, ufoBodyCol, 255);

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

        int[][] stack = TETRIS_STACK;
        for (int r = 0; r < rows; r++) {
            java.util.Arrays.fill(stack[r], 0);
        }

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

        float[] gapCenters = FLAPPY_GAP_CENTERS;
        float[] pipeXs = FLAPPY_PIPE_XS;

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
        float h6 = hue * 6.0f;
        int i = (int) h6;
        float f = h6 - i;
        float q = 1.0f - 0.95f * f;
        float t = 1.0f - 0.95f * (1.0f - f);
        float p = 0.05f;
        float r, g, b;
        switch (i) {
            case 0 -> { r = 1.0f; g = t; b = p; }
            case 1 -> { r = q; g = 1.0f; b = p; }
            case 2 -> { r = p; g = 1.0f; b = t; }
            case 3 -> { r = p; g = q; b = 1.0f; }
            case 4 -> { r = t; g = p; b = 1.0f; }
            default -> { r = 1.0f; g = p; b = q; }
        }
        return ((int) (r * 255.0f) << 16) | ((int) (g * 255.0f) << 8) | (int) (b * 255.0f);
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
            float z = currentZ;
            currentZ += 0.0008f;
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

    private static void drawFilledCircle(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float cx, float cy, float rx, float ry, int slices, int colorRGB, int alpha) {
        if (rx <= 0.2f || ry <= 0.2f || alpha <= 0) return;
        int steps = Math.max(4, slices);
        float stepH = (ry * 2.0f) / steps;
        for (int i = 0; i < steps; i++) {
            float y1 = cy - ry + i * stepH;
            float y2 = y1 + stepH + 0.15f;
            float normY = ((i + 0.5f) / steps) * 2.0f - 1.0f;
            float span = (float) Math.sqrt(Math.max(0.0f, 1.0f - normY * normY));
            float halfW = rx * span;
            if (halfW > 0.15f) {
                q(vc, mat, light, g, cx - halfW, y1, cx + halfW, y2, colorRGB, alpha);
            }
        }
    }

    private static void drawEllipseArc(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float cx, float cy, float rx, float ry, float thick,
                                       double startAngle, double endAngle, int steps, int colorRGB, int alpha) {
        if (rx <= 0.2f || ry <= 0.2f || alpha <= 0 || steps <= 0) return;
        float halfT = Math.max(0.35f, thick * 0.5f);
        double span = endAngle - startAngle;
        for (int i = 0; i <= steps; i++) {
            double a = startAngle + (span * i) / steps;
            float px = cx + (float) Math.cos(a) * rx;
            float py = cy + (float) Math.sin(a) * ry;
            q(vc, mat, light, g, px - halfT, py - halfT, px + halfT, py + halfT, colorRGB, alpha);
        }
    }

    private static void drawLine(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                 float x1, float y1, float x2, float y2, float thick, int colorRGB, int alpha) {
        if (alpha <= 0) return;
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float stepSize = Math.max(0.45f, thick * 0.55f);
        int steps = Math.max(1, (int) Math.ceil(dist / stepSize));
        float halfT = Math.max(0.35f, thick * 0.5f);
        for (int i = 0; i <= steps; i++) {
            float f = (float) i / steps;
            float px = x1 + dx * f;
            float py = y1 + dy * f;
            q(vc, mat, light, g, px - halfT, py - halfT, px + halfT, py + halfT, colorRGB, alpha);
        }
    }

    private static void renderStockChart(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x050A12, 255);

        int gridCols = lowDetail ? 5 : 8;
        for (int i = 1; i < gridCols; i++) {
            float gx = x + (w * i) / gridCols;
            q(vc, mat, light, g, gx, y, gx + 0.4f, y + h, 0x102030, 140);
        }
        for (int j = 1; j < 4; j++) {
            float gy = y + (h * j) / 4.0f;
            q(vc, mat, light, g, x, gy, x + w, gy + 0.4f, 0x102030, 140);
        }

        int themeCol = rainbow ? getRainbowColor(t * 0.25) : baseColor;
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.6f, Math.min(w, h) * 0.03f), blendColor(themeCol, 0x1A2E44, 0.55f), 220);

        int candles = lowDetail ? 8 : Math.max(9, Math.min(16, (int) (w / 3.2f)));
        float padX = Math.max(1.5f, w * 0.05f);
        float chartW = w - padX * 2.0f;
        float chartTop = y + h * 0.16f;
        float chartBot = y + h * 0.80f;
        float chartH = chartBot - chartTop;
        float volTop = chartBot + 1.0f;
        float volBot = y + h - 1.2f;

        double scroll = t * 1.45;
        int baseIdx = (int) Math.floor(scroll);
        float shiftFrac = (float) (scroll - baseIdx);

        float stepX = chartW / candles;
        float bodyW = Math.max(1.2f, stepX * 0.64f);
        float wickW = Math.max(0.45f, bodyW * 0.26f);

        float prevMaX = 0.0f;
        float prevMaY = 0.0f;
        float lastCloseNorm = 0.5f;
        boolean lastBull = true;

        for (int i = 0; i < candles; i++) {
            int k = baseIdx + i;
            float openNorm = stockPriceAt(k);
            float closeNorm = stockPriceAt(k + 1);
            if (i == candles - 1) {
                float liveTick = (float) (Math.sin(t * 9.5) * 0.08 + Math.cos(t * 4.2) * 0.05);
                closeNorm = Math.max(0.08f, Math.min(0.92f, openNorm + (closeNorm - openNorm) * shiftFrac + liveTick));
                lastCloseNorm = closeNorm;
                lastBull = (closeNorm >= openNorm);
            }

            boolean bull = (closeNorm >= openNorm);
            int cCol = bull ? 0x00FF66 : 0xFF2244;
            int wCol = bull ? 0x66FFAA : 0xFF7788;

            float highNorm = Math.min(0.96f, Math.max(openNorm, closeNorm) + 0.05f + ((Math.abs(k * 37) % 10) * 0.008f));
            float lowNorm = Math.max(0.04f, Math.min(openNorm, closeNorm) - 0.05f - ((Math.abs(k * 53) % 10) * 0.008f));

            float cx = x + padX + (i + 0.5f - shiftFrac * 0.85f) * stepX;
            if (cx - bodyW * 0.5f < x + 1.0f || cx + bodyW * 0.5f > x + w - 1.0f) continue;

            float highY = chartBot - highNorm * chartH;
            float lowY = chartBot - lowNorm * chartH;
            float openY = chartBot - openNorm * chartH;
            float closeY = chartBot - closeNorm * chartH;
            float topBody = Math.min(openY, closeY);
            float botBody = Math.max(topBody + 0.9f, Math.max(openY, closeY));

            q(vc, mat, light, g, cx - wickW * 0.5f, highY, cx + wickW * 0.5f, lowY, wCol, 220);
            q(vc, mat, light, g, cx - bodyW * 0.5f, topBody, cx + bodyW * 0.5f, botBody, cCol, 250);
            q(vc, mat, light, g, cx - bodyW * 0.5f, topBody, cx - bodyW * 0.15f, botBody, blendColor(cCol, 0xFFFFFF, 0.35f), 210);

            float volFrac = 0.25f + 0.75f * Math.min(1.0f, Math.abs(closeNorm - openNorm) * 3.8f + ((Math.abs(k * 19) % 5) * 0.08f));
            float vTop = volBot - volFrac * (volBot - volTop);
            q(vc, mat, light, g, cx - bodyW * 0.45f, vTop, cx + bodyW * 0.45f, volBot, cCol, 125);

            float maNorm = (stockPriceAt(k - 1) + openNorm + closeNorm) / 3.0f;
            float maY = chartBot - maNorm * chartH;
            if (i > 0 && prevMaX > x) {
                drawLine(vc, mat, light, g, prevMaX, prevMaY, cx, maY, Math.max(0.55f, h * 0.032f), rainbow ? themeCol : 0x00E5FF, 215);
            }
            prevMaX = cx;
            prevMaY = maY;
        }

        float curPriceY = chartBot - lastCloseNorm * chartH;
        int lineCol = lastBull ? 0x00FF66 : 0xFF2244;
        q(vc, mat, light, g, x + 1.0f, curPriceY - 0.3f, x + w - 1.0f, curPriceY + 0.3f, lineCol, 140);

        if (w >= 24.0f && h >= 14.0f) {
            float dw = Math.max(1.6f, Math.min(3.2f, h * 0.105f));
            float dh = dw * 1.45f;
            float bx = x + 1.8f;
            float by = y + 1.4f;
            float bw = dw * 4.2f;
            float bh = dh + 1.6f;
            q(vc, mat, light, g, bx, by, bx + bw, by + bh, 0x08121E, 235);
            drawOutlineBox(vc, mat, light, g, bx, by, bw, bh, 0.5f, lineCol, 210);

            float triCx = bx + dw * 0.7f;
            float triCy = by + bh * 0.5f;
            float triR = dw * 0.45f;
            if (lastBull) {
                drawLine(vc, mat, light, g, triCx - triR, triCy + triR * 0.6f, triCx, triCy - triR * 0.7f, 0.65f, 0x00FF66, 255);
                drawLine(vc, mat, light, g, triCx, triCy - triR * 0.7f, triCx + triR, triCy + triR * 0.6f, 0.65f, 0x00FF66, 255);
            } else {
                drawLine(vc, mat, light, g, triCx - triR, triCy - triR * 0.6f, triCx, triCy + triR * 0.7f, 0.65f, 0xFF2244, 255);
                drawLine(vc, mat, light, g, triCx, triCy + triR * 0.7f, triCx + triR, triCy - triR * 0.6f, 0.65f, 0xFF2244, 255);
            }

            int priceVal = Math.max(10, Math.min(99, (int) (lastCloseNorm * 90.0f + 10.0f)));
            int dimCol = blendColor(lineCol, 0x000000, 0.82f);
            draw7SegmentDigit(vc, mat, light, g, bx + dw * 1.45f, by + 0.8f, dw, dh, (priceVal / 10) % 10, lineCol, dimCol);
            draw7SegmentDigit(vc, mat, light, g, bx + dw * 2.75f, by + 0.8f, dw, dh, priceVal % 10, 0xFFFFFF, dimCol);
        }
    }

    private static float stockPriceAt(int k) {
        double v = 0.50
                + 0.24 * Math.sin(k * 0.55)
                + 0.14 * Math.cos(k * 1.15 - 0.8)
                + 0.08 * Math.sin(k * 2.35 + 1.4);
        return (float) Math.max(0.10, Math.min(0.90, v));
    }

    private static void renderSlotMachine(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                          float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        double cycle = t % 5.2;
        boolean win = cycle >= 3.1;
        int themeCol = rainbow ? getRainbowColor(t * 0.4) : baseColor;
        int goldCol = rainbow ? getRainbowColor(t * 0.7) : 0xFFD700;

        q(vc, mat, light, g, x, y, x + w, y + h, 0x090612, 255);

        float leverSpace = Math.max(3.5f, w * 0.11f);
        float cabW = w - leverSpace - 1.5f;
        float cabX = x + 1.0f;
        float headerH = Math.max(2.5f, h * 0.18f);
        float bodyY = y + headerH;
        float bodyH = h - headerH - 1.0f;

        int cabRim = win && ((int) (t * 14.0) % 2 == 0) ? 0xFFFFFF : goldCol;
        q(vc, mat, light, g, cabX, y + 0.8f, cabX + cabW, bodyY, 0x260B36, 255);
        drawOutlineBox(vc, mat, light, g, cabX, y + 0.8f, cabW, headerH, Math.max(0.6f, h * 0.03f), cabRim, 255);

        int bulbs = Math.max(5, (int) (cabW / 4.0f));
        float bulbStep = (cabW - 2.0f) / bulbs;
        float bulbR = Math.max(0.6f, headerH * 0.22f);
        for (int b = 0; b < bulbs; b++) {
            boolean lit = win ? (((int) (t * 16.0) + b) % 2 == 0) : (((int) (t * 6.0) + b) % 3 == 0);
            float bx = cabX + 1.0f + (b + 0.5f) * bulbStep;
            float by = y + 0.8f + headerH * 0.5f;
            int bCol = lit ? (b % 2 == 0 ? 0xFFEE44 : 0xFF3366) : 0x442233;
            q(vc, mat, light, g, bx - bulbR, by - bulbR, bx + bulbR, by + bulbR, bCol, 255);
        }

        q(vc, mat, light, g, cabX, bodyY, cabX + cabW, bodyY + bodyH, 0x161224, 255);
        drawOutlineBox(vc, mat, light, g, cabX, bodyY, cabW, bodyH, Math.max(0.7f, h * 0.035f), themeCol, 240);

        float padX = Math.max(1.2f, cabW * 0.05f);
        float gap = Math.max(1.0f, cabW * 0.04f);
        float reelW = (cabW - padX * 2.0f - gap * 2.0f) / 3.0f;
        float reelH = bodyH * 0.82f;
        float reelY = bodyY + (bodyH - reelH) * 0.5f;

        int winSymbol = (((long) (t / 5.2)) % 2 == 0) ? 0 : 1;

        for (int i = 0; i < 3; i++) {
            float rx = cabX + padX + i * (reelW + gap);
            q(vc, mat, light, g, rx, reelY, rx + reelW, reelY + reelH, 0xF4F0E8, 255);
            q(vc, mat, light, g, rx, reelY, rx + reelW, reelY + reelH * 0.18f, 0xB8B2A6, 220);
            q(vc, mat, light, g, rx, reelY + reelH * 0.82f, rx + reelW, reelY + reelH, 0x989286, 220);

            double stopT = 1.65 + i * 0.65;
            boolean stopped = cycle >= stopT;
            float symW = reelW * 0.76f;
            float symH = Math.min(reelH * 0.62f, symW * 1.15f);
            float symX = rx + (reelW - symW) * 0.5f;
            float centerSymY = reelY + (reelH - symH) * 0.5f;

            if (stopped) {
                double dt = cycle - stopT;
                float bounce = (dt < 0.35) ? (float) (Math.sin(dt * 28.0) * Math.exp(-dt * 10.0) * (reelH * 0.16f)) : 0.0f;
                drawSlotSymbol(vc, mat, light, g, symX, centerSymY + bounce, symW, symH, winSymbol, rainbow, t);
            } else {
                double spinPos = t * (13.0 + i * 3.0);
                int baseIdx = (int) Math.floor(spinPos);
                float frac = (float) (spinPos - baseIdx);
                float stepY = reelH * 0.65f;
                for (int k = -1; k <= 1; k++) {
                    float sy = centerSymY + (k + frac - 0.5f) * stepY;
                    if (sy + symH > reelY + 1.0f && sy < reelY + reelH - 1.0f) {
                        float clipTop = Math.max(reelY + 1.0f, sy);
                        float clipBot = Math.min(reelY + reelH - 1.0f, sy + symH);
                        if (clipBot - clipTop > symH * 0.45f) {
                            int sType = Math.abs(baseIdx - k + i) % 4;
                            drawSlotSymbol(vc, mat, light, g, symX, sy, symW, symH, sType, rainbow, t);
                        }
                    }
                }
            }

            int reelFrame = stopped ? (win ? cabRim : 0xFFD700) : 0x556677;
            drawOutlineBox(vc, mat, light, g, rx, reelY, reelW, reelH, Math.max(0.6f, reelW * 0.07f), reelFrame, 255);
        }

        float paylineY = reelY + reelH * 0.5f;
        int payAlpha = win ? (((int) (t * 18.0) % 2 == 0) ? 255 : 90) : 110;
        q(vc, mat, light, g, cabX + padX * 0.5f, paylineY - 0.4f, cabX + cabW - padX * 0.5f, paylineY + 0.4f, 0xFF1133, payAlpha);

        float mountX = cabX + cabW;
        float mountY = bodyY + bodyH * 0.55f;
        float shaftX = mountX + leverSpace * 0.52f;
        q(vc, mat, light, g, mountX, mountY - 2.0f, shaftX, mountY + 2.0f, 0x778899, 255);

        float pullFrac = 0.0f;
        if (cycle < 0.35) {
            pullFrac = (float) (cycle / 0.35);
        } else if (cycle < 0.75) {
            pullFrac = 1.0f - (float) ((cycle - 0.35) / 0.40);
        }
        float topKnobY = y + h * 0.20f;
        float botKnobY = y + h * 0.80f;
        float curKnobY = topKnobY + pullFrac * (botKnobY - topKnobY);

        drawLine(vc, mat, light, g, shaftX, mountY, shaftX, curKnobY, Math.max(0.8f, leverSpace * 0.22f), 0xC0C8D0, 255);
        float knobR = Math.max(1.5f, leverSpace * 0.42f);
        drawFilledCircle(vc, mat, light, g, shaftX, curKnobY, knobR, knobR, 6, 0xFF2233, 255);
        q(vc, mat, light, g, shaftX - knobR * 0.4f, curKnobY - knobR * 0.4f, shaftX + knobR * 0.1f, curKnobY + knobR * 0.1f, 0xFFFFFF, 220);

        if (win) {
            int coins = 12;
            for (int k = 0; k < coins; k++) {
                double ct = ((cycle - 3.1) * 1.8 + k * 0.23) % 1.4;
                float cx = cabX + ((k * 37) % 100) * 0.01f * cabW;
                float cy = y + (float) (ct * h * 0.95);
                float cr = Math.max(1.0f, h * 0.055f);
                drawFilledCircle(vc, mat, light, g, cx, cy, cr, cr, 5, (k % 2 == 0) ? 0xFFD700 : 0xFFEE66, 245);
                q(vc, mat, light, g, cx - cr * 0.3f, cy - cr * 0.5f, cx + cr * 0.3f, cy + cr * 0.5f, 0xFFFFFF, 210);
            }
        }
    }

    private static void drawSlotSymbol(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float sx, float sy, float sw, float sh, int type, boolean rainbow, double t) {
        if (type == 0) {
            int col7 = rainbow ? getRainbowColor(t * 0.8) : 0xFF1E27;
            q(vc, mat, light, g, sx + sw * 0.10f, sy + sh * 0.10f, sx + sw * 0.90f, sy + sh * 0.28f, col7, 255);
            q(vc, mat, light, g, sx + sw * 0.10f, sy + sh * 0.28f, sx + sw * 0.28f, sy + sh * 0.42f, col7, 255);
            drawLine(vc, mat, light, g, sx + sw * 0.80f, sy + sh * 0.22f, sx + sw * 0.32f, sy + sh * 0.88f, Math.max(1.2f, sw * 0.24f), col7, 255);
            drawLine(vc, mat, light, g, sx + sw * 0.76f, sy + sh * 0.20f, sx + sw * 0.30f, sy + sh * 0.86f, Math.max(0.6f, sw * 0.08f), 0xFFD700, 240);
        } else if (type == 1) {
            int diaCol = rainbow ? getRainbowColor(t * 0.8 + 0.25) : 0x00E5FF;
            float cx = sx + sw * 0.5f;
            float cy = sy + sh * 0.5f;
            int slices = 7;
            for (int i = 0; i < slices; i++) {
                float ny = ((i + 0.5f) / slices) * 2.0f - 1.0f;
                float span = 1.0f - Math.abs(ny);
                float hw = (sw * 0.44f) * span;
                float y1 = sy + sh * 0.1f + i * (sh * 0.8f / slices);
                float y2 = y1 + (sh * 0.8f / slices) + 0.15f;
                q(vc, mat, light, g, cx - hw, y1, cx + hw, y2, i < 3 ? blendColor(diaCol, 0xFFFFFF, 0.45f) : diaCol, 255);
            }
        } else if (type == 2) {
            float r = Math.max(1.0f, Math.min(sw, sh) * 0.22f);
            float c1x = sx + sw * 0.32f;
            float c2x = sx + sw * 0.68f;
            float cy = sy + sh * 0.68f;
            float topX = sx + sw * 0.55f;
            float topY = sy + sh * 0.16f;
            drawLine(vc, mat, light, g, c1x, cy - r * 0.5f, topX, topY, Math.max(0.6f, sw * 0.08f), 0x22AA33, 255);
            drawLine(vc, mat, light, g, c2x, cy - r * 0.5f, topX, topY, Math.max(0.6f, sw * 0.08f), 0x22AA33, 255);
            drawFilledCircle(vc, mat, light, g, c1x, cy, r, r, 5, 0xEE1133, 255);
            drawFilledCircle(vc, mat, light, g, c2x, cy, r, r, 5, 0xEE1133, 255);
        } else {
            int crCol = 0xFFB800;
            q(vc, mat, light, g, sx + sw * 0.12f, sy + sh * 0.55f, sx + sw * 0.88f, sy + sh * 0.82f, crCol, 255);
            q(vc, mat, light, g, sx + sw * 0.12f, sy + sh * 0.25f, sx + sw * 0.30f, sy + sh * 0.55f, crCol, 255);
            q(vc, mat, light, g, sx + sw * 0.41f, sy + sh * 0.18f, sx + sw * 0.59f, sy + sh * 0.55f, crCol, 255);
            q(vc, mat, light, g, sx + sw * 0.70f, sy + sh * 0.25f, sx + sw * 0.88f, sy + sh * 0.55f, crCol, 255);
            q(vc, mat, light, g, sx + sw * 0.44f, sy + sh * 0.62f, sx + sw * 0.56f, sy + sh * 0.75f, 0xFF1144, 255);
        }
    }

    private static void renderSubwayMap(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x070E18, 255);

        int gridCols = lowDetail ? 6 : 10;
        for (int i = 1; i < gridCols; i++) {
            float gx = x + (w * i) / gridCols;
            q(vc, mat, light, g, gx, y, gx + 0.4f, y + h, 0x102035, 130);
        }
        for (int j = 1; j < 4; j++) {
            float gy = y + (h * j) / 4.0f;
            q(vc, mat, light, g, x, gy, x + w, gy + 0.4f, 0x102035, 130);
        }

        int mainLineCol = rainbow ? getRainbowColor(t * 0.2) : baseColor;
        int secLineCol = rainbow ? getRainbowColor(t * 0.2 + 0.45) : 0xFF8800;
        float trackThick = Math.max(1.2f, Math.min(w, h) * 0.075f);

        drawLine(vc, mat, light, g, x + w * 0.18f, y + h * 0.90f, x + w * 0.45f, y + h * 0.58f, trackThick * 0.75f, secLineCol, 130);
        drawLine(vc, mat, light, g, x + w * 0.45f, y + h * 0.58f, x + w * 0.82f, y + h * 0.16f, trackThick * 0.75f, secLineCol, 130);

        int stations = 5;
        float[] sx = SUBWAY_SX;
        float[] sy = SUBWAY_SY;
        float[] yNorms = { 0.66f, 0.66f, 0.42f, 0.42f, 0.26f };
        for (int s = 0; s < stations; s++) {
            sx[s] = x + w * (0.10f + s * 0.20f);
            sy[s] = y + h * yNorms[s];
        }

        for (int s = 0; s < stations - 1; s++) {
            drawLine(vc, mat, light, g, sx[s], sy[s], sx[s + 1], sy[s + 1], trackThick, mainLineCol, 235);
            drawLine(vc, mat, light, g, sx[s], sy[s], sx[s + 1], sy[s + 1], trackThick * 0.35f, blendColor(mainLineCol, 0xFFFFFF, 0.5f), 180);
        }

        double rawProg = (t * 0.55) % (stations - 1);
        int seg = Math.min(stations - 2, (int) Math.floor(rawProg));
        float segFrac = (float) (rawProg - seg);
        float pauseFrac;
        if (segFrac < 0.18f) {
            pauseFrac = 0.0f;
        } else if (segFrac > 0.88f) {
            pauseFrac = 1.0f;
        } else {
            float m = (segFrac - 0.18f) / 0.70f;
            pauseFrac = m * m * (3.0f - 2.0f * m);
        }

        int activeStation = (pauseFrac < 0.5f) ? seg : (seg + 1);
        float nodeR = Math.max(1.8f, Math.min(w, h) * 0.095f);

        for (int s = 0; s < stations; s++) {
            boolean isCurrent = (s == activeStation);
            if (isCurrent) {
                float pulseR = nodeR * (1.25f + 0.65f * (float) ((t * 2.2) % 1.0));
                int pulseAlpha = (int) (210 * (1.0 - ((t * 2.2) % 1.0)));
                drawEllipseArc(vc, mat, light, g, sx[s], sy[s], pulseR, pulseR, 0.7f, 0, Math.PI * 2.0, 12, 0x00FF88, pulseAlpha);
            }
            int outerCol = isCurrent ? 0x00FF88 : 0xFFFFFF;
            drawFilledCircle(vc, mat, light, g, sx[s], sy[s], nodeR, nodeR, 6, outerCol, 255);
            drawFilledCircle(vc, mat, light, g, sx[s], sy[s], nodeR * 0.52f, nodeR * 0.52f, 5, 0x070E18, 255);

            float pillW = Math.max(3.5f, nodeR * 2.3f);
            float pillH = Math.max(1.4f, nodeR * 0.85f);
            float pillY = (s % 2 == 0) ? (sy[s] - nodeR - pillH - 1.2f) : (sy[s] + nodeR + 1.2f);
            int pillCol = isCurrent ? 0x00FF88 : 0x3A5878;
            q(vc, mat, light, g, sx[s] - pillW * 0.5f, pillY, sx[s] + pillW * 0.5f, pillY + pillH, pillCol, 220);
            q(vc, mat, light, g, sx[s] - pillW * 0.35f, pillY + pillH * 0.3f, sx[s] + pillW * 0.35f, pillY + pillH * 0.7f, 0xFFFFFF, isCurrent ? 255 : 150);
        }

        float tx = sx[seg] + (sx[seg + 1] - sx[seg]) * pauseFrac;
        float ty = sy[seg] + (sy[seg + 1] - sy[seg]) * pauseFrac;
        float dx = sx[seg + 1] - sx[seg];
        float dy = sy[seg + 1] - sy[seg];
        float segLen = (float) Math.sqrt(dx * dx + dy * dy);
        float ux = dx / Math.max(0.01f, segLen);
        float uy = dy / Math.max(0.01f, segLen);

        float carLen = Math.max(3.5f, w * 0.085f);
        float carThick = Math.max(2.0f, trackThick * 1.45f);
        float frontX = tx + ux * carLen * 0.55f;
        float frontY = ty + uy * carLen * 0.55f;
        float rearX = tx - ux * carLen * 0.55f;
        float rearY = ty - uy * carLen * 0.55f;

        drawLine(vc, mat, light, g, rearX, rearY, frontX, frontY, carThick, 0xE8F0F8, 255);
        drawLine(vc, mat, light, g, rearX + ux * carLen * 0.2f, rearY + uy * carLen * 0.2f,
                 frontX - ux * carLen * 0.2f, frontY - uy * carLen * 0.2f, carThick * 0.52f, 0x00E5FF, 255);
        float beamX = frontX + ux * carLen * 0.35f;
        float beamY = frontY + uy * carLen * 0.35f;
        drawFilledCircle(vc, mat, light, g, frontX, frontY, carThick * 0.45f, carThick * 0.45f, 4, 0xFFEE44, 255);
        drawLine(vc, mat, light, g, frontX, frontY, beamX, beamY, carThick * 0.7f, 0xFFEE88, 120);

        if (w >= 24.0f && h >= 14.0f) {
            float dw = Math.max(1.8f, Math.min(3.4f, h * 0.11f));
            float dh = dw * 1.5f;
            float hx = x + 1.5f;
            float hy = y + 1.2f;
            q(vc, mat, light, g, hx, hy, hx + dw * 3.2f, hy + dh + 1.6f, 0x0D1B2A, 230);
            drawOutlineBox(vc, mat, light, g, hx, hy, dw * 3.2f, dh + 1.6f, 0.5f, mainLineCol, 200);
            int dimCol = blendColor(mainLineCol, 0x000000, 0.8f);
            draw7SegmentDigit(vc, mat, light, g, hx + 1.0f, hy + 0.8f, dw, dh, 0, mainLineCol, dimCol);
            draw7SegmentDigit(vc, mat, light, g, hx + 1.0f + dw * 1.35f, hy + 0.8f, dw, dh, activeStation + 1, 0x00FF88, dimCol);
        }
    }

    private static void renderBlackHole(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x020206, 255);

        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float bhR = Math.min(w * 0.21f, h * 0.30f);

        int mainCol = rainbow ? getRainbowColor(t * 0.25) : (baseColor == 0x00FF66 ? 0xFF7711 : baseColor);
        int hotCol = blendColor(mainCol, 0xFFFFDD, 0.78f);
        int midCol = blendColor(mainCol, 0xFFAA22, 0.45f);
        int outerCol = blendColor(mainCol, 0x660800, 0.65f);

        int stars = lowDetail ? 12 : 22;
        for (int s = 0; s < stars; s++) {
            int hash = (s * 92837111) ^ 0x4B3C2A1;
            float sx = x + ((hash & 0xFF) / 255.0f) * w;
            float sy = y + (((hash >> 8) & 0xFF) / 255.0f) * h;
            float dx = sx - cx;
            float dy = sy - cy;
            if (dx * dx + dy * dy < bhR * bhR * 2.2f) continue;
            int sa = 70 + (int) (140 * (0.5 + 0.5 * Math.sin(t * 2.2 + s)));
            q(vc, mat, light, g, sx, sy, sx + 0.8f, sy + 0.8f, 0xE8F0FF, sa);
        }

        float haloOuterR = bhR * 1.48f;
        int haloSlices = lowDetail ? 18 : 28;
        float sliceH = (haloOuterR * 2.0f) / haloSlices;
        for (int i = 0; i < haloSlices; i++) {
            float y1 = cy - haloOuterR + i * sliceH;
            float y2 = y1 + sliceH + 0.15f;
            float dy = (y1 + y2) * 0.5f - cy;
            float absDy = Math.abs(dy);
            if (absDy >= haloOuterR) continue;

            float outerSpan = (float) Math.sqrt(haloOuterR * haloOuterR - dy * dy);
            float radialNorm = absDy / haloOuterR;
            int hCol = radialNorm < 0.72f ? hotCol : (radialNorm < 0.86f ? midCol : outerCol);
            int hAlpha = (int) (245 * (1.0f - Math.max(0.0f, (radialNorm - 0.65f) / 0.35f) * 0.65f));

            if (absDy > bhR * 0.96f) {
                q(vc, mat, light, g, cx - outerSpan, y1, cx + outerSpan, y2, hCol, hAlpha);
            } else {
                float innerSpan = (float) Math.sqrt(bhR * bhR * 0.92f - dy * dy);
                q(vc, mat, light, g, cx - outerSpan, y1, cx - innerSpan, y2, hotCol, 220);
                q(vc, mat, light, g, cx + innerSpan, y1, cx + outerSpan, y2, outerCol, 175);
            }
        }

        float diskRx = Math.min(w * 0.45f, bhR * 2.55f);
        float diskRy = bhR * 0.38f;
        int diskSegs = lowDetail ? 14 : 22;
        int bands = lowDetail ? 4 : 6;

        for (int b = bands - 1; b >= 0; b--) {
            float bf = (float) b / Math.max(1, bands - 1);
            float rx = bhR * 1.15f + bf * (diskRx - bhR * 1.15f);
            float ry = bhR * 0.16f + bf * (diskRy - bhR * 0.16f);
            float bandT = Math.max(0.85f, (diskRx - bhR) / bands * 0.55f);
            int baseBandCol = (b == 0) ? 0xFFFFFF : ((b == 1) ? hotCol : ((b < bands - 1) ? midCol : outerCol));

            for (int s = 0; s < diskSegs; s++) {
                double a0 = Math.PI + (s * Math.PI) / diskSegs;
                double a1 = Math.PI + ((s + 1) * Math.PI) / diskSegs;
                float x0 = cx + (float) Math.cos(a0) * rx;
                float y0 = cy + (float) Math.sin(a0) * ry;
                float x1 = cx + (float) Math.cos(a1) * rx;
                float y1 = cy + (float) Math.sin(a1) * ry;
                float doppler = (float) (0.5 - 0.5 * Math.cos((a0 + a1) * 0.5));
                int segCol = blendColor(outerCol, baseBandCol, 0.35f + doppler * 0.65f);
                drawLine(vc, mat, light, g, x0, y0, x1, y1, bandT, segCol, 230 - b * 22);
            }
        }

        drawFilledCircle(vc, mat, light, g, cx, cy, bhR * 1.07f, bhR * 1.07f, lowDetail ? 14 : 22, 0xFFF6D6, 255);
        drawFilledCircle(vc, mat, light, g, cx, cy, bhR * 0.96f, bhR * 0.96f, lowDetail ? 14 : 22, 0x010104, 255);

        for (int b = bands - 1; b >= 0; b--) {
            float bf = (float) b / Math.max(1, bands - 1);
            float rx = bhR * 1.15f + bf * (diskRx - bhR * 1.15f);
            float ry = bhR * 0.16f + bf * (diskRy - bhR * 0.16f);
            float bandT = Math.max(0.95f, (diskRx - bhR) / bands * 0.62f);
            int baseBandCol = (b == 0) ? 0xFFFFFF : ((b == 1) ? hotCol : ((b < bands - 1) ? midCol : outerCol));

            for (int s = 0; s < diskSegs; s++) {
                double a0 = -0.08 + (s * (Math.PI + 0.16)) / diskSegs;
                double a1 = -0.08 + ((s + 1) * (Math.PI + 0.16)) / diskSegs;
                float x0 = cx + (float) Math.cos(a0) * rx;
                float y0 = cy + (float) Math.sin(a0) * ry;
                float x1 = cx + (float) Math.cos(a1) * rx;
                float y1 = cy + (float) Math.sin(a1) * ry;
                float doppler = (float) (0.5 - 0.5 * Math.cos((a0 + a1) * 0.5));
                double swirl = Math.sin(a0 * 4.0 - t * 4.5 + b * 0.9);
                int segCol = blendColor(outerCol, baseBandCol, Math.min(1.0f, 0.35f + doppler * 0.65f + (float) swirl * 0.15f));
                int alpha = Math.min(255, (int) ((245 - b * 20) * (0.75f + 0.25f * doppler)));
                drawLine(vc, mat, light, g, x0, y0, x1, y1, bandT, segCol, alpha);
            }
        }
    }

    private static void renderHourglass(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float hgH = Math.min(h * 0.88f, w * 1.15f);
        float hgW = Math.min(w * 0.68f, hgH * 0.72f);
        float halfH = hgH * 0.5f;
        float halfW = hgW * 0.5f;

        double cycle = t % 7.0;
        boolean flipping = cycle >= 6.0;
        float pourFrac = flipping ? 1.0f : (float) (cycle / 6.0);
        double flipAngle = 0.0;
        if (flipping) {
            float ft = (float) (cycle - 6.0);
            float smooth = ft * ft * (3.0f - 2.0f * ft);
            flipAngle = smooth * Math.PI;
        }
        float cosA = (float) Math.cos(flipAngle);
        float sinA = (float) Math.sin(flipAngle);

        int frameCol = rainbow ? getRainbowColor(t * 0.25) : 0xC8963E;
        int frameDark = blendColor(frameCol, 0x000000, 0.35f);
        int sandCol = rainbow ? getRainbowColor(t * 0.4 + 0.2) : (baseColor == 0x00FF66 ? 0xFFD700 : baseColor);
        int sandLight = blendColor(sandCol, 0xFFFFFF, 0.38f);
        int glassCol = 0x88DDFF;

        float capH = Math.max(1.2f, hgH * 0.08f);
        float innerHalfH = halfH - capH;
        int slices = 14;

        for (int s = -slices; s <= slices; s++) {
            if (s == 0) continue;
            float normY = (float) s / slices;
            float absY = Math.abs(normY);
            float profile = 0.10f + 0.90f * (float) Math.pow(absY, 0.75);
            float sliceHalfW = (halfW * 0.82f) * profile;
            float localY = normY * innerHalfH;
            float sliceThick = Math.max(0.8f, (innerHalfH / slices) * 1.15f);

            boolean hasSand = false;
            float sandSpan = 0.0f;
            if (s < 0) {
                float rem = 1.0f - pourFrac;
                float topSandMaxNorm = (float) Math.pow(rem, 0.65);
                if (absY <= topSandMaxNorm && rem > 0.02f) {
                    hasSand = true;
                    sandSpan = sliceHalfW * 0.84f;
                }
            } else {
                float botFillNorm = 1.0f - pourFrac * 0.88f;
                if (absY >= botFillNorm && pourFrac > 0.02f) {
                    hasSand = true;
                    float coneFactor = Math.min(1.0f, (absY - botFillNorm) / Math.max(0.12f, 1.0f - botFillNorm) + 0.25f);
                    sandSpan = sliceHalfW * 0.86f * coneFactor;
                }
            }

            if (hasSand && sandSpan > 0.3f) {
                float sx1 = cx + (-sandSpan) * cosA - localY * sinA;
                float sy1 = cy + (-sandSpan) * sinA + localY * cosA;
                float sx2 = cx + sandSpan * cosA - localY * sinA;
                float sy2 = cy + sandSpan * sinA + localY * cosA;
                drawLine(vc, mat, light, g, sx1, sy1, sx2, sy2, sliceThick, (s % 2 == 0) ? sandCol : sandLight, 240);
            }

            float gxL = cx + (-sliceHalfW) * cosA - localY * sinA;
            float gyL = cy + (-sliceHalfW) * sinA + localY * cosA;
            float gxR = cx + sliceHalfW * cosA - localY * sinA;
            float gyR = cy + sliceHalfW * sinA + localY * cosA;
            float wallT = Math.max(0.75f, hgW * 0.055f);
            q(vc, mat, light, g, gxL - wallT * 0.5f, gyL - wallT * 0.5f, gxL + wallT * 0.5f, gyL + wallT * 0.5f, glassCol, 220);
            q(vc, mat, light, g, gxR - wallT * 0.5f, gyR - wallT * 0.5f, gxR + wallT * 0.5f, gyR + wallT * 0.5f, glassCol, 220);

            if (absY > 0.35f && absY < 0.85f) {
                float sheenX = cx + (-sliceHalfW * 0.62f) * cosA - localY * sinA;
                float sheenY = cy + (-sliceHalfW * 0.62f) * sinA + localY * cosA;
                q(vc, mat, light, g, sheenX - 0.4f, sheenY - 0.4f, sheenX + 0.4f, sheenY + 0.4f, 0xFFFFFF, 160);
            }
        }

        if (!flipping && pourFrac < 0.98f) {
            int grains = 6;
            for (int k = 0; k < grains; k++) {
                float gf = (float) (((t * 5.5) + k * 0.17) % 1.0);
                float gy = cy + gf * innerHalfH * 0.92f;
                q(vc, mat, light, g, cx - 0.5f, gy - 0.6f, cx + 0.5f, gy + 0.6f, (k % 2 == 0) ? sandLight : sandCol, 245);
            }
        }

        float pLeft = -halfW * 0.92f;
        float pRight = halfW * 0.92f;
        drawLine(vc, mat, light, g,
                 cx + pLeft * cosA - (-innerHalfH) * sinA, cy + pLeft * sinA + (-innerHalfH) * cosA,
                 cx + pLeft * cosA - innerHalfH * sinA, cy + pLeft * sinA + innerHalfH * cosA,
                 Math.max(0.8f, hgW * 0.06f), frameDark, 235);
        drawLine(vc, mat, light, g,
                 cx + pRight * cosA - (-innerHalfH) * sinA, cy + pRight * sinA + (-innerHalfH) * cosA,
                 cx + pRight * cosA - innerHalfH * sinA, cy + pRight * sinA + innerHalfH * cosA,
                 Math.max(0.8f, hgW * 0.06f), frameDark, 235);

        float capYTop = -halfH + capH * 0.5f;
        float capYBot = halfH - capH * 0.5f;
        drawLine(vc, mat, light, g,
                 cx + (-halfW) * cosA - capYTop * sinA, cy + (-halfW) * sinA + capYTop * cosA,
                 cx + halfW * cosA - capYTop * sinA, cy + halfW * sinA + capYTop * cosA,
                 capH, frameCol, 255);
        drawLine(vc, mat, light, g,
                 cx + (-halfW) * cosA - capYBot * sinA, cy + (-halfW) * sinA + capYBot * cosA,
                 cx + halfW * cosA - capYBot * sinA, cy + halfW * sinA + capYBot * cosA,
                 capH, frameCol, 255);
    }

    private static void renderTurntable(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x12161F, 255);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.6f, h * 0.03f), 0x2A3242, 255);

        int accentCol = rainbow ? getRainbowColor(t * 0.35) : baseColor;
        float platterR = Math.min(w * 0.36f, h * 0.43f);
        float diskCx = x + platterR + Math.max(2.0f, w * 0.06f);
        float diskCy = y + h * 0.50f;

        int slices = lowDetail ? 14 : 22;
        drawFilledCircle(vc, mat, light, g, diskCx, diskCy, platterR * 1.06f, platterR * 1.06f, slices, 0x556070, 255);
        drawFilledCircle(vc, mat, light, g, diskCx, diskCy, platterR, platterR, slices, 0x0B0D12, 255);

        double rot = t * 3.8;
        int arcSteps = lowDetail ? 8 : 14;
        for (int s = 0; s < 2; s++) {
            double sheenA = rot + s * Math.PI;
            drawEllipseArc(vc, mat, light, g, diskCx, diskCy, platterR * 0.78f, platterR * 0.78f, platterR * 0.16f, sheenA - 0.35, sheenA + 0.35, arcSteps, 0x354254, 190);
            drawEllipseArc(vc, mat, light, g, diskCx, diskCy, platterR * 0.56f, platterR * 0.56f, platterR * 0.14f, sheenA - 0.30, sheenA + 0.30, arcSteps, 0x4A5B70, 190);
        }

        int grooves = lowDetail ? 2 : 3;
        for (int k = 1; k <= grooves; k++) {
            float gr = platterR * (0.42f + k * 0.16f);
            drawEllipseArc(vc, mat, light, g, diskCx, diskCy, gr, gr, 0.55f, 0, Math.PI * 2.0, lowDetail ? 16 : 24, 0x222834, 210);
        }

        float labelR = platterR * 0.34f;
        drawFilledCircle(vc, mat, light, g, diskCx, diskCy, labelR, labelR, 10, accentCol, 255);
        float dotAngle = (float) rot;
        float markerX = diskCx + (float) Math.cos(dotAngle) * (labelR * 0.58f);
        float markerY = diskCy + (float) Math.sin(dotAngle) * (labelR * 0.58f);
        q(vc, mat, light, g, markerX - 0.7f, markerY - 0.7f, markerX + 0.7f, markerY + 0.7f, 0xFFFFFF, 240);
        drawFilledCircle(vc, mat, light, g, diskCx, diskCy, Math.max(0.8f, labelR * 0.24f), Math.max(0.8f, labelR * 0.24f), 5, 0xE0E6ED, 255);

        float strobeR = Math.max(1.0f, platterR * 0.12f);
        drawFilledCircle(vc, mat, light, g, diskCx - platterR * 0.88f, diskCy + platterR * 0.82f, strobeR, strobeR, 5, 0xFF2244, 255);

        float pivotX = x + w * 0.82f;
        float pivotY = y + h * 0.22f;
        float pivotR = Math.max(1.6f, platterR * 0.22f);
        drawFilledCircle(vc, mat, light, g, pivotX, pivotY, pivotR, pivotR, 6, 0x3A4454, 255);
        drawFilledCircle(vc, mat, light, g, pivotX, pivotY, pivotR * 0.55f, pivotR * 0.55f, 5, 0xAEB8C4, 255);

        float sway = (float) Math.sin(t * 6.0) * 0.45f;
        float elbowX = pivotX - platterR * 0.08f;
        float elbowY = diskCy + platterR * 0.22f;
        float stylusX = diskCx + platterR * 0.62f + sway;
        float stylusY = diskCy + platterR * 0.36f;

        float armThick = Math.max(0.85f, platterR * 0.09f);
        drawLine(vc, mat, light, g, pivotX, pivotY - pivotR * 0.7f, pivotX, pivotY, armThick * 1.4f, 0x778290, 255);
        drawLine(vc, mat, light, g, pivotX, pivotY, elbowX, elbowY, armThick, 0xD0D8E2, 255);
        drawLine(vc, mat, light, g, elbowX, elbowY, stylusX, stylusY, armThick, 0xD0D8E2, 255);
        drawLine(vc, mat, light, g, stylusX, stylusY, stylusX - armThick * 1.6f, stylusY - armThick * 0.9f, armThick * 1.55f, 0xFF2233, 255);

        if (w > h * 1.15f) {
            float vuX = x + w - Math.max(2.8f, w * 0.09f);
            float vuTop = y + h * 0.16f;
            float vuBot = y + h * 0.84f;
            int bars = 7;
            float barStep = (vuBot - vuTop) / bars;
            double level = 0.55 + 0.42 * Math.abs(Math.sin(t * 7.5));
            int activeBars = (int) Math.round(level * bars);
            for (int b = 0; b < bars; b++) {
                float by = vuBot - (b + 1) * barStep;
                boolean on = b < activeBars;
                int bCol = (b >= bars - 1) ? 0xFF2233 : ((b >= bars - 2) ? 0xFFCC00 : accentCol);
                q(vc, mat, light, g, vuX, by + 0.3f, vuX + Math.max(1.4f, w * 0.05f), by + barStep - 0.3f, bCol, on ? 245 : 55);
            }
        }
    }

    private static void renderAquarium(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x041528, 255);
        q(vc, mat, light, g, x, y, x + w, y + h * 0.55f, 0x072644, 180);
        q(vc, mat, light, g, x, y, x + w, y + h * 0.25f, 0x0D3B66, 170);

        int waveSegs = lowDetail ? 8 : 14;
        float waveW = w / waveSegs;
        for (int i = 0; i < waveSegs; i++) {
            float wx = x + i * waveW;
            float wy = y + 0.8f + (float) Math.sin(t * 3.5 + i * 0.8) * 0.7f;
            q(vc, mat, light, g, wx, y, wx + waveW, wy + 1.0f, 0x55DDFF, 210);
        }

        float sandH = Math.max(2.2f, h * 0.14f);
        float sandY = y + h - sandH;
        q(vc, mat, light, g, x, sandY, x + w, y + h, 0xC8A258, 255);
        q(vc, mat, light, g, x, sandY, x + w, sandY + sandH * 0.35f, 0xE2C074, 255);

        float coralX = x + w * 0.22f;
        float coralH = h * 0.24f;
        int coralCol = rainbow ? getRainbowColor(t * 0.2 + 0.6) : 0xFF4488;
        drawLine(vc, mat, light, g, coralX, sandY, coralX, sandY - coralH, Math.max(1.0f, w * 0.03f), coralCol, 240);
        drawLine(vc, mat, light, g, coralX, sandY - coralH * 0.45f, coralX - coralH * 0.4f, sandY - coralH * 0.85f, Math.max(0.8f, w * 0.025f), coralCol, 240);
        drawLine(vc, mat, light, g, coralX, sandY - coralH * 0.55f, coralX + coralH * 0.4f, sandY - coralH * 0.90f, Math.max(0.8f, w * 0.025f), coralCol, 240);

        float chestW = Math.max(3.5f, w * 0.12f);
        float chestH = Math.max(2.5f, h * 0.14f);
        float chestX = x + w * 0.74f;
        float chestY = sandY - chestH * 0.75f;
        boolean chestOpen = ((t % 4.0) > 1.8 && (t % 4.0) < 3.4);
        q(vc, mat, light, g, chestX, chestY + chestH * 0.4f, chestX + chestW, chestY + chestH, 0x6E3B16, 255);
        if (chestOpen) {
            q(vc, mat, light, g, chestX + chestW * 0.15f, chestY + chestH * 0.15f, chestX + chestW * 0.85f, chestY + chestH * 0.45f, 0xFFD700, 255);
            q(vc, mat, light, g, chestX, chestY - chestH * 0.2f, chestX + chestW, chestY + chestH * 0.15f, 0x8A4B1C, 255);
        } else {
            q(vc, mat, light, g, chestX, chestY, chestX + chestW, chestY + chestH * 0.4f, 0x8A4B1C, 255);
        }
        q(vc, mat, light, g, chestX + chestW * 0.4f, chestY + chestH * 0.35f, chestX + chestW * 0.6f, chestY + chestH * 0.55f, 0xFFD700, 255);

        int kelps = lowDetail ? 3 : 4;
        float[] kelpNormX = AQUARIUM_KELP_X;
        for (int k = 0; k < kelps; k++) {
            float kx = x + w * kelpNormX[k];
            int segs = 6;
            float segH = (h * 0.52f) / segs;
            int kCol = (k % 2 == 0) ? 0x1EA846 : 0x32C85A;
            for (int s = 0; s < segs; s++) {
                float sy = sandY - (s + 1) * segH;
                float sway = (float) Math.sin(t * 2.4 + k * 1.3 + s * 0.55) * (s * 0.55f);
                float kw = Math.max(0.9f, w * 0.028f);
                q(vc, mat, light, g, kx + sway - kw * 0.5f, sy, kx + sway + kw * 0.5f, sy + segH + 0.2f, kCol, 235);
            }
        }

        int bubbles = lowDetail ? 5 : 8;
        for (int b = 0; b < bubbles; b++) {
            double bt = (t * 0.75 + b * 0.28) % 1.6;
            float by = sandY - (float) ((bt / 1.6) * (h * 0.82f));
            float bx = (b % 2 == 0 ? (chestX + chestW * 0.5f) : (x + ((b * 29) % 100) * 0.01f * w)) + (float) Math.sin(t * 3.5 + b) * 1.2f;
            float br = Math.max(0.6f, h * 0.032f);
            drawEllipseArc(vc, mat, light, g, bx, by, br, br, 0.45f, 0, Math.PI * 2.0, 6, 0xAAFFFF, 200);
        }

        int fishCount = lowDetail ? 2 : 3;
        int[] fColors = AQUARIUM_F_COLORS;
        float[] fDepths = AQUARIUM_F_DEPTHS;
        double[] fSpeeds = AQUARIUM_F_SPEEDS;

        for (int f = 0; f < fishCount; f++) {
            double phase = (t * fSpeeds[f] + f * 0.65) % 2.0;
            boolean right = (phase < 1.0);
            float prog = right ? (float) phase : (float) (2.0 - phase);
            float fw = Math.max(4.5f, w * 0.14f);
            float fh = Math.max(2.6f, h * 0.16f);
            float fx = x + fw * 0.4f + prog * (w - fw * 1.8f);
            float fy = y + h * fDepths[f] + (float) Math.sin(t * 3.2 + f * 1.7) * (h * 0.04f);
            int fCol = (f == 2) ? (rainbow ? getRainbowColor(t * 0.4) : baseColor) : (rainbow ? getRainbowColor(t * 0.4 + f * 0.3) : fColors[f]);
            drawAquariumFish(vc, mat, light, g, fx, fy, fw, fh, fCol, right, f == 0, t);
        }
    }

    private static void drawAquariumFish(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float fx, float fy, float fw, float fh, int fCol, boolean right, boolean clownStripes, double t) {
        float dir = right ? 1.0f : -1.0f;
        float cx = fx + fw * 0.5f;
        float cy = fy + fh * 0.5f;

        float wag = (float) Math.sin(t * 11.0 + fy) * (fh * 0.18f);
        float tailX = cx - dir * (fw * 0.52f);
        drawLine(vc, mat, light, g, cx - dir * (fw * 0.25f), cy, tailX, cy - fh * 0.38f + wag, Math.max(0.8f, fh * 0.22f), fCol, 240);
        drawLine(vc, mat, light, g, cx - dir * (fw * 0.25f), cy, tailX, cy + fh * 0.38f + wag, Math.max(0.8f, fh * 0.22f), fCol, 240);

        q(vc, mat, light, g, cx - fw * 0.15f, cy - fh * 0.58f, cx + fw * 0.15f, cy - fh * 0.30f, blendColor(fCol, 0xFFFFFF, 0.25f), 235);
        drawFilledCircle(vc, mat, light, g, cx, cy, fw * 0.40f, fh * 0.38f, 6, fCol, 255);

        if (clownStripes) {
            q(vc, mat, light, g, cx - fw * 0.06f, cy - fh * 0.35f, cx + fw * 0.06f, cy + fh * 0.35f, 0xFFFFFF, 250);
        } else {
            q(vc, mat, light, g, cx - fw * 0.22f, cy - fh * 0.08f, cx + fw * 0.22f, cy + fh * 0.12f, blendColor(fCol, 0xFFFFFF, 0.45f), 220);
        }

        float eyeX = cx + dir * (fw * 0.20f);
        float eyeY = cy - fh * 0.10f;
        float eyeSz = Math.max(0.55f, fh * 0.16f);
        q(vc, mat, light, g, eyeX - eyeSz, eyeY - eyeSz, eyeX + eyeSz, eyeY + eyeSz, 0xFFFFFF, 255);
        q(vc, mat, light, g, eyeX - eyeSz * 0.5f, eyeY - eyeSz * 0.5f, eyeX + eyeSz * 0.5f, eyeY + eyeSz * 0.5f, 0x000000, 255);
    }

    private static void renderCyberTerminal(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                            float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        int termCol = rainbow ? getRainbowColor(t * 0.25) : baseColor;
        int dimCol = blendColor(termCol, 0x000000, 0.60f);
        int darkBg = blendColor(termCol, 0x030805, 0.92f);
        int brightCol = blendColor(termCol, 0xFFFFFF, 0.68f);

        q(vc, mat, light, g, x, y, x + w, y + h, darkBg, 255);

        int scanlines = Math.max(5, (int) (h / 2.0f));
        for (int s = 0; s < scanlines; s++) {
            float sy = y + s * 2.0f;
            q(vc, mat, light, g, x, sy, x + w, sy + 0.65f, 0x000000, 85);
        }

        float sweepY = y + (float) ((t * 0.38) % 1.0) * h;
        q(vc, mat, light, g, x, sweepY, x + w, Math.min(y + h, sweepY + h * 0.12f), termCol, 28);

        float borderT = Math.max(0.6f, Math.min(w, h) * 0.03f);
        float headerH = Math.max(2.4f, h * 0.16f);
        q(vc, mat, light, g, x, y, x + w, y + headerH, blendColor(termCol, 0x06120B, 0.68f), 255);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, borderT, termCol, 240);

        float dotR = Math.max(0.55f, headerH * 0.22f);
        float dotY = y + headerH * 0.5f;
        q(vc, mat, light, g, x + 1.4f, dotY - dotR, x + 1.4f + dotR * 2.0f, dotY + dotR, 0xFF3344, 255);
        q(vc, mat, light, g, x + 1.4f + dotR * 2.8f, dotY - dotR, x + 1.4f + dotR * 4.8f, dotY + dotR, 0xFFCC00, 255);
        q(vc, mat, light, g, x + 1.4f + dotR * 5.6f, dotY - dotR, x + 1.4f + dotR * 7.6f, dotY + dotR, 0x00FF66, 255);

        boolean hasSidebar = (w > h * 1.32f);
        float mainW = hasSidebar ? (w * 0.66f) : (w - borderT * 2.0f);

        if (hasSidebar) {
            float sideX = x + mainW;
            float sideW = w - mainW - borderT;
            q(vc, mat, light, g, sideX, y + headerH, sideX + borderT, y + h, termCol, 200);

            float waveTop = y + headerH + 1.5f;
            float waveH = (h - headerH) * 0.42f;
            float waveMidY = waveTop + waveH * 0.5f;
            q(vc, mat, light, g, sideX + borderT + 1.0f, waveTop, sideX + sideW - 1.0f, waveTop + waveH, 0x020604, 220);
            drawOutlineBox(vc, mat, light, g, sideX + borderT + 1.0f, waveTop, sideW - 2.0f, waveH, 0.45f, dimCol, 190);

            int waveSteps = 10;
            float wStepX = (sideW - 3.2f) / waveSteps;
            float prevWx = sideX + borderT + 1.6f;
            float prevWy = waveMidY;
            for (int ws = 0; ws <= waveSteps; ws++) {
                float wx = sideX + borderT + 1.6f + ws * wStepX;
                float wy = waveMidY + (float) (Math.sin(t * 7.5 + ws * 0.9) * Math.cos(t * 3.2 - ws * 0.4)) * (waveH * 0.38f);
                if (ws > 0) {
                    drawLine(vc, mat, light, g, prevWx, prevWy, wx, wy, 0.65f, brightCol, 245);
                }
                prevWx = wx;
                prevWy = wy;
            }

            float matTop = waveTop + waveH + 1.5f;
            float matH = (y + h - borderT - 1.0f) - matTop;
            int mCols = 4;
            int mRows = 3;
            float cw = (sideW - 2.4f) / mCols;
            float ch = matH / mRows;
            int tick = (int) (t * 8.0);
            for (int r = 0; r < mRows; r++) {
                for (int c = 0; c < mCols; c++) {
                    int hVal = ((r * 7 + c * 13 + tick) * 1664525) ^ 0x5DEECE66;
                    boolean active = (hVal & 3) != 0;
                    int cellCol = ((hVal & 7) == 0) ? 0xFFFFFF : (active ? termCol : dimCol);
                    drawTermGlyph(vc, mat, light, g, sideX + borderT + 1.2f + c * cw, matTop + r * ch, cw * 0.78f, ch * 0.78f, Math.abs(hVal >> 4) % 8, cellCol, active ? 235 : 90);
                }
            }
        }

        float bodyTop = y + headerH + 1.4f;
        float progH = Math.max(2.4f, h * 0.17f);
        float linesAreaH = (y + h - progH - 1.8f) - bodyTop;
        int numLines = 4;
        float lineStep = linesAreaH / numLines;
        float glyphH = Math.max(1.8f, lineStep * 0.68f);
        float glyphW = Math.max(1.2f, glyphH * 0.68f);
        float glyphGap = Math.max(0.45f, glyphW * 0.35f);

        double cycle = (t * 1.4) % 6.4;
        int maxSlots = Math.max(5, Math.min(14, (int) ((mainW - 5.0f) / (glyphW + glyphGap))));

        for (int l = 0; l < numLines; l++) {
            float ly = bodyTop + l * lineStep;
            float startX = x + borderT + 1.5f;

            drawLine(vc, mat, light, g, startX, ly + glyphH * 0.15f, startX + glyphW * 0.75f, ly + glyphH * 0.5f, 0.55f, brightCol, 255);
            drawLine(vc, mat, light, g, startX + glyphW * 0.75f, ly + glyphH * 0.5f, startX, ly + glyphH * 0.85f, 0.55f, brightCol, 255);

            double lineProg = Math.max(0.0, Math.min(1.0, (cycle - l * 1.05) / 1.15));
            int lineTargetChars = maxSlots - 2 - (l % 2);
            int typedChars = (int) Math.round(lineProg * lineTargetChars);

            float curX = startX + glyphW + glyphGap * 1.4f;
            for (int c = 0; c < typedChars; c++) {
                if (c == 3 || c == 7) {
                    curX += glyphW * 0.55f;
                    continue;
                }
                boolean scrambling = (lineProg < 0.98 && c >= typedChars - 1);
                int glyphId = scrambling ? ((int) (t * 24.0 + c) % 8) : ((l * 5 + c * 3) % 8);
                int gCol = scrambling ? 0xFFFFFF : ((c < 3) ? brightCol : ((c < 7) ? termCol : (!rainbow && l % 2 == 1 ? 0x00E5FF : dimCol)));
                drawTermGlyph(vc, mat, light, g, curX, ly, glyphW, glyphH, glyphId, gCol, 245);
                curX += glyphW + glyphGap;
            }

            if (lineProg >= 0.99) {
                float badgeW = Math.max(2.6f, glyphW * 2.1f);
                float badgeX = x + mainW - badgeW - 1.5f;
                q(vc, mat, light, g, badgeX, ly, badgeX + badgeW, ly + glyphH, 0x00FF66, 235);
                q(vc, mat, light, g, badgeX + 0.5f, ly + 0.5f, badgeX + badgeW - 0.5f, ly + glyphH - 0.5f, 0xFFFFFF, 190);
            } else if (lineProg > 0.0 && ((int) (t * 9.0) % 2 == 0)) {
                q(vc, mat, light, g, curX, ly, curX + glyphW * 0.85f, ly + glyphH, 0xFFFFFF, 255);
            }
        }

        float progY = y + h - progH - 1.0f;
        float progX = x + borderT + 1.5f;
        float progBarW = (mainW >= 22.0f) ? (mainW * 0.66f) : (mainW - 3.0f);
        q(vc, mat, light, g, progX, progY, progX + progBarW, progY + progH - 0.5f, 0x020604, 240);
        drawOutlineBox(vc, mat, light, g, progX, progY, progBarW, progH - 0.5f, 0.5f, termCol, 220);

        float fillFrac = (float) Math.min(1.0, cycle / 5.0);
        int segs = 10;
        int activeSegs = (int) Math.round(fillFrac * segs);
        float segStep = (progBarW - 1.6f) / segs;
        for (int s = 0; s < activeSegs; s++) {
            float sx = progX + 0.8f + s * segStep;
            q(vc, mat, light, g, sx, progY + 0.7f, sx + segStep * 0.78f, progY + progH - 1.2f, (s == activeSegs - 1) ? 0xFFFFFF : termCol, 245);
        }

        if (mainW >= 22.0f) {
            int pct = Math.min(99, (int) (fillFrac * 99));
            float dw = Math.max(1.4f, progH * 0.52f);
            float dh = progH - 0.4f;
            float dx = progX + progBarW + 1.6f;
            draw7SegmentDigit(vc, mat, light, g, dx, progY, dw, dh, (pct / 10) % 10, brightCol, blendColor(termCol, 0x000000, 0.85f));
            draw7SegmentDigit(vc, mat, light, g, dx + dw * 1.3f, progY, dw, dh, pct % 10, brightCol, blendColor(termCol, 0x000000, 0.85f));
        }
    }

    private static void drawTermGlyph(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                      float gx, float gy, float gw, float gh, int glyphId, int col, int alpha) {
        float t = Math.max(0.35f, Math.min(gw, gh) * 0.26f);
        switch (glyphId & 7) {
            case 0 -> {
                q(vc, mat, light, g, gx, gy, gx + gw, gy + t, col, alpha);
                q(vc, mat, light, g, gx, gy, gx + t, gy + gh, col, alpha);
                q(vc, mat, light, g, gx, gy + gh * 0.45f, gx + gw * 0.8f, gy + gh * 0.45f + t, col, alpha);
            }
            case 1 -> {
                q(vc, mat, light, g, gx, gy, gx + gw, gy + t, col, alpha);
                q(vc, mat, light, g, gx + gw - t, gy, gx + gw, gy + gh, col, alpha);
                q(vc, mat, light, g, gx, gy + gh - t, gx + gw, gy + gh, col, alpha);
            }
            case 2 -> {
                q(vc, mat, light, g, gx, gy, gx + t, gy + gh, col, alpha);
                q(vc, mat, light, g, gx, gy + gh - t, gx + gw, gy + gh, col, alpha);
                q(vc, mat, light, g, gx + gw - t, gy + gh * 0.35f, gx + gw, gy + gh, col, alpha);
            }
            case 3 -> {
                q(vc, mat, light, g, gx, gy, gx + gw, gy + t, col, alpha);
                q(vc, mat, light, g, gx, gy + gh * 0.45f, gx + gw, gy + gh * 0.45f + t, col, alpha);
                q(vc, mat, light, g, gx, gy + gh - t, gx + gw, gy + gh, col, alpha);
            }
            case 4 -> {
                q(vc, mat, light, g, gx, gy, gx + t, gy + gh * 0.55f, col, alpha);
                q(vc, mat, light, g, gx, gy + gh * 0.45f, gx + gw, gy + gh * 0.45f + t, col, alpha);
                q(vc, mat, light, g, gx + gw - t, gy, gx + gw, gy + gh, col, alpha);
            }
            case 5 -> {
                drawOutlineBox(vc, mat, light, g, gx, gy, gw, gh, t, col, alpha);
            }
            case 6 -> {
                q(vc, mat, light, g, gx + (gw - t) * 0.5f, gy, gx + (gw + t) * 0.5f, gy + gh, col, alpha);
                q(vc, mat, light, g, gx, gy, gx + gw, gy + t, col, alpha);
            }
            default -> {
                q(vc, mat, light, g, gx, gy, gx + t, gy + gh, col, alpha);
                q(vc, mat, light, g, gx + gw - t, gy, gx + gw, gy + gh, col, alpha);
                q(vc, mat, light, g, gx, gy + gh * 0.45f, gx + gw, gy + gh * 0.45f + t, col, alpha);
            }
        }
    }

    private static void renderSolarSystem(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                          float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x03050C, 255);

        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float maxRx = w * 0.44f;
        float maxRy = h * 0.42f;
        float tilt = 0.52f;

        int stars = lowDetail ? 10 : 18;
        for (int s = 0; s < stars; s++) {
            int hash = (s * 92837111) ^ 0x4B3C2A1;
            float sx = x + ((hash & 0xFF) / 255.0f) * w;
            float sy = y + (((hash >> 8) & 0xFF) / 255.0f) * h;
            int sa = 90 + (int) (130 * (0.5 + 0.5 * Math.sin(t * 2.5 + s)));
            q(vc, mat, light, g, sx, sy, sx + 0.8f, sy + 0.8f, 0xFFFFFF, sa);
        }

        int planets = 4;
        float sunR = Math.max(2.2f, Math.min(maxRx, maxRy) * 0.25f);
        int orbitSteps = lowDetail ? 20 : 32;

        for (int p = 0; p < planets; p++) {
            float frac = (float) (p + 1) / planets;
            float rx = sunR * 1.35f + frac * (maxRx - sunR * 1.35f);
            float ry = Math.min(maxRy * frac, rx * tilt);
            drawEllipseArc(vc, mat, light, g, cx, cy, rx, ry, 0.45f, 0, Math.PI * 2.0, orbitSteps, 0x22334D, 145);
        }

        int[] pColors = SOLAR_P_COLORS;
        double[] pSpeeds = SOLAR_P_SPEEDS;
        float[] pRadii = SOLAR_P_RADII;

        for (int pass = 0; pass < 2; pass++) {
            if (pass == 1) {
                int sunCol = rainbow ? getRainbowColor(t * 0.25) : 0xFFAA00;
                float pulse = 1.0f + 0.08f * (float) Math.sin(t * 4.5);
                drawFilledCircle(vc, mat, light, g, cx, cy, sunR * 1.32f * pulse, sunR * 1.32f * pulse, 10, 0xFF5500, 125);
                drawFilledCircle(vc, mat, light, g, cx, cy, sunR * pulse, sunR * pulse, 10, sunCol, 255);
                drawFilledCircle(vc, mat, light, g, cx, cy, sunR * 0.55f, sunR * 0.55f, 6, 0xFFFFCC, 250);
            }

            for (int p = 0; p < planets; p++) {
                double angle = t * pSpeeds[p] + p * 1.7;
                boolean inFront = Math.sin(angle) >= 0.0;
                if ((pass == 0 && inFront) || (pass == 1 && !inFront)) continue;

                float frac = (float) (p + 1) / planets;
                float rx = sunR * 1.35f + frac * (maxRx - sunR * 1.35f);
                float ry = Math.min(maxRy * frac, rx * tilt);
                float px = cx + (float) Math.cos(angle) * rx;
                float py = cy + (float) Math.sin(angle) * ry;
                float pr = Math.max(1.1f, Math.min(maxRx, maxRy) * pRadii[p]);
                int pCol = rainbow ? getRainbowColor(t * 0.35 + p * 0.22) : pColors[p];

                if (p == 3) {
                    drawEllipseArc(vc, mat, light, g, px, py, pr * 2.2f, pr * 0.85f, Math.max(0.6f, pr * 0.35f), Math.PI, Math.PI * 2.0, 12, 0xD8B888, 210);
                }

                drawFilledCircle(vc, mat, light, g, px, py, pr, pr, 7, pCol, 255);
                float sunDirX = (cx - px);
                float sunDirY = (cy - py);
                float sLen = (float) Math.sqrt(sunDirX * sunDirX + sunDirY * sunDirY);
                if (sLen > 0.1f) {
                    float hx = px + (sunDirX / sLen) * (pr * 0.35f);
                    float hy = py + (sunDirY / sLen) * (pr * 0.35f);
                    drawFilledCircle(vc, mat, light, g, hx, hy, pr * 0.45f, pr * 0.45f, 4, blendColor(pCol, 0xFFFFFF, 0.55f), 220);
                }

                if (p == 1) {
                    q(vc, mat, light, g, px - pr * 0.3f, py - pr * 0.2f, px + pr * 0.2f, py + pr * 0.3f, 0x22CC55, 220);
                    double mAngle = t * 7.5;
                    float mx = px + (float) Math.cos(mAngle) * (pr * 2.1f);
                    float my = py + (float) Math.sin(mAngle) * (pr * 1.2f);
                    drawFilledCircle(vc, mat, light, g, mx, my, Math.max(0.55f, pr * 0.35f), Math.max(0.55f, pr * 0.35f), 4, 0xE8EEF5, 245);
                } else if (p == 3) {
                    drawEllipseArc(vc, mat, light, g, px, py, pr * 2.2f, pr * 0.85f, Math.max(0.6f, pr * 0.35f), 0.0, Math.PI, 12, 0xF0D8A8, 240);
                }
            }
        }
    }

    private static void renderBiohazard(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x0D0E0A, 255);

        boolean alarmPulse = ((int) (t * 4.5) % 2 == 0);
        int hazCol = rainbow ? getRainbowColor(t * 0.4) : (baseColor == 0x00FF66 ? 0xFFCC00 : baseColor);
        int alertCol = alarmPulse ? 0xFF2200 : hazCol;

        float stripeH = Math.max(2.0f, h * 0.13f);
        float stripeW = Math.max(3.0f, stripeH * 1.35f);
        float scroll = (float) ((t * 9.0) % (stripeW * 2.0));
        int subBands = 3;
        float bandH = stripeH / subBands;

        for (int b = 0; b < subBands; b++) {
            float skew = b * (stripeW * 0.32f);
            float topBandY = y + b * bandH;
            float botBandY = y + h - stripeH + b * bandH;
            for (float sx = x - stripeW * 3.0f + scroll; sx < x + w + stripeW * 2.0f; sx += stripeW * 2.0f) {
                q(vc, mat, light, g, sx + skew, topBandY, sx + skew + stripeW, topBandY + bandH + 0.15f, hazCol, 255);
                q(vc, mat, light, g, sx + skew + stripeW, topBandY, sx + skew + stripeW * 2.0f, topBandY + bandH + 0.15f, 0x141414, 255);
                q(vc, mat, light, g, sx - skew, botBandY, sx - skew + stripeW, botBandY + bandH + 0.15f, hazCol, 255);
                q(vc, mat, light, g, sx - skew + stripeW, botBandY, sx - skew + stripeW * 2.0f, botBandY + bandH + 0.15f, 0x141414, 255);
            }
        }

        boolean wide = (w > h * 1.3f);
        float symCx = wide ? (x + w * 0.36f) : (x + w * 0.50f);
        float symCy = y + h * 0.50f;
        float symR = Math.min(w * (wide ? 0.26f : 0.34f), (h - stripeH * 2.2f) * 0.44f);

        drawFilledCircle(vc, mat, light, g, symCx, symCy, symR * 1.08f, symR * 1.08f, lowDetail ? 10 : 16, 0x181A14, 255);
        drawEllipseArc(vc, mat, light, g, symCx, symCy, symR * 1.04f, symR * 1.04f, Math.max(0.65f, symR * 0.09f), 0, Math.PI * 2.0, lowDetail ? 16 : 24, alertCol, 245);

        double rot = t * 1.1;
        int bladeRings = lowDetail ? 3 : 5;
        int bladeSteps = lowDetail ? 5 : 8;
        for (int b = 0; b < 3; b++) {
            double centerA = rot + b * (Math.PI * 2.0 / 3.0);
            double span = Math.PI / 3.2;
            for (int r = 0; r < bladeRings; r++) {
                float rf = (float) r / Math.max(1, bladeRings - 1);
                float br = symR * (0.34f + rf * 0.52f);
                drawEllipseArc(vc, mat, light, g, symCx, symCy, br, br, Math.max(0.75f, symR * 0.16f),
                               centerA - span * 0.5, centerA + span * 0.5, bladeSteps, hazCol, 250);
            }
        }
        drawFilledCircle(vc, mat, light, g, symCx, symCy, symR * 0.25f, symR * 0.25f, 5, 0x181A14, 255);
        drawFilledCircle(vc, mat, light, g, symCx, symCy, symR * 0.15f, symR * 0.15f, 5, alertCol, 255);

        if (wide) {
            float gx = x + w * 0.65f;
            float gw = w * 0.30f;
            float gy = y + stripeH + 2.0f;
            float gh = h - stripeH * 2.0f - 4.0f;
            q(vc, mat, light, g, gx, gy, gx + gw, gy + gh, 0x14181C, 255);
            drawOutlineBox(vc, mat, light, g, gx, gy, gw, gh, 0.7f, 0x4A5868, 240);

            float dialCx = gx + gw * 0.50f;
            float dialCy = gy + gh * 0.78f;
            float dialR = Math.min(gw * 0.42f, gh * 0.58f);

            drawEllipseArc(vc, mat, light, g, dialCx, dialCy, dialR, dialR, 0.8f, Math.PI * 1.10, Math.PI * 1.55, 6, 0x00FF66, 220);
            drawEllipseArc(vc, mat, light, g, dialCx, dialCy, dialR, dialR, 0.8f, Math.PI * 1.55, Math.PI * 1.78, 4, 0xFFCC00, 220);
            drawEllipseArc(vc, mat, light, g, dialCx, dialCy, dialR, dialR, 0.8f, Math.PI * 1.78, Math.PI * 1.92, 4, 0xFF2200, 245);

            double jitter = 0.52 + 0.32 * Math.sin(t * 6.5) + 0.14 * Math.cos(t * 19.0);
            double needleA = Math.PI * (1.12 + jitter * 0.78);
            float nx = dialCx + (float) Math.cos(needleA) * (dialR * 0.92f);
            float ny = dialCy + (float) Math.sin(needleA) * (dialR * 0.92f);
            drawLine(vc, mat, light, g, dialCx, dialCy, nx, ny, Math.max(0.7f, dialR * 0.09f), 0xFF3333, 255);
            drawFilledCircle(vc, mat, light, g, dialCx, dialCy, Math.max(0.9f, dialR * 0.16f), Math.max(0.9f, dialR * 0.16f), 4, 0xCCD4DD, 255);

            float ledR = Math.max(0.8f, gw * 0.07f);
            drawFilledCircle(vc, mat, light, g, gx + gw - ledR * 2.2f, gy + ledR * 2.2f, ledR, ledR, 4, alarmPulse ? 0xFF1100 : 0x441111, 255);
        }
    }


    private static void renderRetroSnake(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x050D07, 255);

        int snakeCol = rainbow ? getRainbowColor(t * 0.35) : baseColor;
        float wallT = Math.max(0.7f, Math.min(w, h) * 0.038f);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, wallT, snakeCol, 235);

        int cols = 14;
        int rows = 9;
        float pad = wallT + 0.8f;
        float gw = w - pad * 2.0f;
        float gh = h - pad * 2.0f;
        float cw = gw / cols;
        float ch = gh / rows;
        float gx = x + pad;
        float gy = y + pad;

        if (!lowDetail) {
            for (int r = 1; r < rows; r++) {
                q(vc, mat, light, g, gx, gy + r * ch, gx + gw, gy + r * ch + 0.3f, 0x0B1B10, 120);
            }
            for (int c = 1; c < cols; c++) {
                q(vc, mat, light, g, gx + c * cw, gy, gx + c * cw + 0.3f, gy + gh, 0x0B1B10, 120);
            }
        }

        int[][] loopPath = {
                {1, 1}, {2, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1}, {7, 1}, {8, 1}, {9, 1}, {10, 1}, {11, 1}, {12, 1},
                {12, 2}, {12, 3}, {12, 4}, {11, 4}, {10, 4}, {9, 4}, {8, 4}, {7, 4}, {6, 4}, {5, 4},
                {5, 5}, {5, 6}, {6, 6}, {7, 6}, {8, 6}, {9, 6}, {10, 6}, {11, 6}, {12, 6}, {12, 7},
                {11, 7}, {10, 7}, {9, 7}, {8, 7}, {7, 7}, {6, 7}, {5, 7}, {4, 7}, {3, 7}, {2, 7}, {1, 7},
                {1, 6}, {1, 5}, {1, 4}, {1, 3}, {1, 2}
        };
        int pathLen = loopPath.length;
        int appleInterval = 10;
        double movePos = t * 7.2;
        long totalStep = (long) Math.floor(movePos);
        float subFrac = (float) (movePos - totalStep);

        long appleEpoch = (long) Math.floor((movePos + 0.32) / appleInterval);
        long targetStep = (appleEpoch + 1L) * appleInterval;
        double distSinceEat = (movePos + 0.32) - (appleEpoch * appleInterval);

        int applesInRound = (int) (appleEpoch % 5L);
        int snakeLen = 4 + applesInRound * 2;

        if (distSinceEat < 1.2 && appleEpoch > 0) {
            int eatenIdx = (int) ((appleEpoch * appleInterval) % pathLen);
            float ex = gx + (loopPath[eatenIdx][0] + 0.5f) * cw;
            float ey = gy + (loopPath[eatenIdx][1] + 0.5f) * ch;
            float popProg = (float) (distSinceEat / 1.2);
            float popR = Math.max(1.2f, Math.min(cw, ch) * (0.5f + popProg * 0.9f));
            drawEllipseArc(vc, mat, light, g, ex, ey, popR, popR, 0.6f, 0, Math.PI * 2.0, 8, 0xFFFF88, (int) (240 * (1.0f - popProg)));
        }

        int appleIdx = (int) (targetStep % pathLen);
        float spawnScale = (distSinceEat < 0.8) ? (float) (distSinceEat / 0.8) : 1.0f;
        float appleX = gx + (loopPath[appleIdx][0] + 0.5f) * cw;
        float appleY = gy + (loopPath[appleIdx][1] + 0.5f) * ch;
        float appleR = Math.max(1.0f, Math.min(cw, ch) * 0.46f) * (0.90f + 0.12f * (float) Math.sin(t * 8.5)) * spawnScale;

        if (appleR > 0.35f) {
            drawFilledCircle(vc, mat, light, g, appleX, appleY, appleR, appleR, 6, 0xFF1E27, 255);
            q(vc, mat, light, g, appleX - appleR * 0.35f, appleY - appleR * 0.35f, appleX + appleR * 0.1f, appleY + appleR * 0.1f, 0xFF99AA, 230);
            q(vc, mat, light, g, appleX - appleR * 0.15f, appleY - appleR * 1.28f, appleX + appleR * 0.45f, appleY - appleR * 0.72f, 0x33FF55, 255);
        }

        for (int s = snakeLen - 1; s >= 0; s--) {
            double segPos = movePos - s * 0.82;
            long sStep = (long) Math.floor(segPos);
            float sFrac = (float) (segPos - sStep);
            int p0 = (int) ((sStep % pathLen + pathLen) % pathLen);
            int p1 = (p0 + 1) % pathLen;

            float cellX = loopPath[p0][0] + (loopPath[p1][0] - loopPath[p0][0]) * sFrac;
            float cellY = loopPath[p0][1] + (loopPath[p1][1] - loopPath[p0][1]) * sFrac;
            float sx = gx + (cellX + 0.5f) * cw;
            float sy = gy + (cellY + 0.5f) * ch;

            int segCol = rainbow ? getRainbowColor(t * 0.35 - s * 0.04) : ((s == 0) ? blendColor(snakeCol, 0xFFFFFF, 0.42f) : ((s % 2 == 0) ? snakeCol : blendColor(snakeCol, 0x000000, 0.28f)));
            float taper = (s > snakeLen - 3) ? (0.72f + 0.12f * (snakeLen - s)) : 1.0f;
            float hw = cw * (s == 0 ? 0.50f : 0.43f * taper);
            float hh = ch * (s == 0 ? 0.50f : 0.43f * taper);
            drawFilledCircle(vc, mat, light, g, sx, sy, hw, hh, 5, segCol, 255);

            if (s == 0) {
                float dx = loopPath[p1][0] - loopPath[p0][0];
                float dy = loopPath[p1][1] - loopPath[p0][1];
                float eyeSz = Math.max(0.45f, Math.min(cw, ch) * 0.18f);
                float ex = sx + dx * hw * 0.35f;
                float ey = sy + dy * hh * 0.35f;
                q(vc, mat, light, g, ex - eyeSz, ey - eyeSz, ex + eyeSz, ey + eyeSz, 0x000000, 255);
                if (((int) (t * 7.0) % 2) == 0) {
                    drawLine(vc, mat, light, g, sx + dx * hw * 0.9f, sy + dy * hh * 0.9f, sx + dx * hw * 1.75f, sy + dy * hh * 1.75f, 0.55f, 0xFF2244, 245);
                }
            }
        }
    }

    private static void renderWeatherStation(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                             float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        int state = ((int) (t * 0.25)) % 4;
        boolean isNight = false;

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            long dayTime = level.getDayTime() % 24000L;
            isNight = (dayTime >= 12500L && dayTime < 23500L);
            if (level.isThundering()) {
                state = 2;
            } else if (level.isRaining()) {
                boolean cold = false;
                if (Minecraft.getInstance().player != null) {
                    BlockPos pos = Minecraft.getInstance().player.blockPosition();
                    cold = level.getBiome(pos).value().coldEnoughToSnow(pos);
                }
                state = cold ? 3 : 1;
            } else {
                state = 0;
            }
        }

        int skyTop;
        if (state == 0) {
            skyTop = isNight ? 0x050B18 : 0x0E3A6C;
        } else if (state == 1) {
            skyTop = isNight ? 0x060E1C : 0x0B2038;
        } else if (state == 2) {
            skyTop = 0x070914;
        } else {
            skyTop = isNight ? 0x071222 : 0x0D2238;
        }
        q(vc, mat, light, g, x, y, x + w, y + h, skyTop, 255);

        int themeCol = rainbow ? getRainbowColor(t * 0.3) : baseColor;
        float borderT = Math.max(0.6f, Math.min(w, h) * 0.032f);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, borderT, themeCol, 235);

        boolean wide = (w > h * 1.25f);
        float skyW = wide ? (w * 0.62f) : (w * 0.74f);
        float skyCx = x + skyW * 0.5f;
        float skyCy = y + h * 0.48f;
        float iconScale = Math.min(skyW, h) * 0.34f;

        if (isNight) {
            int stars = lowDetail ? 6 : 12;
            for (int s = 0; s < stars; s++) {
                int hash = (s * 92837111) ^ 0x4B3C2A1;
                float sx = x + borderT + ((hash & 0xFF) / 255.0f) * (skyW - borderT * 2.0f);
                float sy = y + borderT + (((hash >> 8) & 0xFF) / 255.0f) * (h * 0.55f);
                int sa = 90 + (int) (150 * (0.5 + 0.5 * Math.sin(t * 2.8 + s)));
                q(vc, mat, light, g, sx, sy, sx + 0.8f, sy + 0.8f, 0xFFFFFF, sa);
            }
        }

        if (state == 0) {
            float bodyR = iconScale * 0.68f;
            if (isNight) {
                drawFilledCircle(vc, mat, light, g, skyCx, skyCy, bodyR * 1.25f, bodyR * 1.25f, 10, 0x335588, 95);
                drawFilledCircle(vc, mat, light, g, skyCx, skyCy, bodyR, bodyR, 12, 0xFFF2B2, 255);
                drawFilledCircle(vc, mat, light, g, skyCx + bodyR * 0.38f, skyCy - bodyR * 0.18f, bodyR * 0.82f, bodyR * 0.82f, 10, skyTop, 255);
            } else {
                for (int r = 0; r < 8; r++) {
                    double ra = t * 1.4 + r * (Math.PI * 0.25);
                    float x1 = skyCx + (float) Math.cos(ra) * (bodyR * 1.22f);
                    float y1 = skyCy + (float) Math.sin(ra) * (bodyR * 1.22f);
                    float x2 = skyCx + (float) Math.cos(ra) * (bodyR * 1.68f);
                    float y2 = skyCy + (float) Math.sin(ra) * (bodyR * 1.68f);
                    drawLine(vc, mat, light, g, x1, y1, x2, y2, Math.max(0.75f, bodyR * 0.18f), 0xFFEE44, 240);
                }
                drawFilledCircle(vc, mat, light, g, skyCx, skyCy, bodyR * 1.18f, bodyR * 1.18f, 10, 0xFF8800, 120);
                drawFilledCircle(vc, mat, light, g, skyCx, skyCy, bodyR, bodyR, 10, 0xFFDD00, 255);
                drawFilledCircle(vc, mat, light, g, skyCx - bodyR * 0.2f, skyCy - bodyR * 0.2f, bodyR * 0.45f, bodyR * 0.45f, 6, 0xFFFFCC, 240);
            }
        } else {
            float cloudY = y + h * 0.34f;
            int cloudCol = (state == 2) ? 0x556274 : ((state == 3) ? 0xD8E8F8 : (isNight ? 0x7A8C9E : 0xB8C8DC));
            int cloudShade = blendColor(cloudCol, 0x101824, 0.35f);
            float cr = iconScale * 0.48f;
            drawFilledCircle(vc, mat, light, g, skyCx - cr * 0.85f, cloudY + cr * 0.2f, cr * 0.85f, cr * 0.72f, 7, cloudShade, 255);
            drawFilledCircle(vc, mat, light, g, skyCx + cr * 0.85f, cloudY + cr * 0.25f, cr * 0.80f, cr * 0.68f, 7, cloudShade, 255);
            drawFilledCircle(vc, mat, light, g, skyCx, cloudY - cr * 0.15f, cr * 1.05f, cr * 0.90f, 8, cloudCol, 255);
            q(vc, mat, light, g, skyCx - cr * 1.2f, cloudY + cr * 0.15f, skyCx + cr * 1.2f, cloudY + cr * 0.75f, cloudCol, 255);

            float precipTop = cloudY + cr * 0.80f;
            float precipBot = y + h - borderT - 1.2f;
            if (state == 1 || state == 2) {
                if (state == 2 && ((int) (t * 14.0) % 5 == 0)) {
                    drawLine(vc, mat, light, g, skyCx + cr * 0.2f, precipTop, skyCx - cr * 0.3f, (precipTop + precipBot) * 0.55f, 1.0f, 0xFFEE55, 255);
                    drawLine(vc, mat, light, g, skyCx - cr * 0.3f, (precipTop + precipBot) * 0.55f, skyCx + cr * 0.15f, precipBot, 1.0f, 0xFFFFFF, 255);
                }
                int drops = 5;
                for (int d = 0; d < drops; d++) {
                    float df = (float) ((t * 2.2 + d * 0.23) % 1.0);
                    float dx = skyCx - cr * 1.0f + d * (cr * 0.5f) - df * 1.5f;
                    float dy = precipTop + df * (precipBot - precipTop - 2.0f);
                    drawLine(vc, mat, light, g, dx, dy, dx - 0.8f, dy + 2.2f, 0.65f, 0x55DDFF, 230);
                }
            } else {
                int flakes = 6;
                for (int f = 0; f < flakes; f++) {
                    float ff = (float) ((t * 0.95 + f * 0.19) % 1.0);
                    float fx = skyCx - cr * 1.1f + f * (cr * 0.44f) + (float) Math.sin(t * 3.0 + f) * 1.0f;
                    float fy = precipTop + ff * (precipBot - precipTop - 1.5f);
                    float fSz = Math.max(0.75f, cr * 0.18f);
                    q(vc, mat, light, g, fx - fSz, fy - fSz * 0.35f, fx + fSz, fy + fSz * 0.35f, 0xFFFFFF, 240);
                    q(vc, mat, light, g, fx - fSz * 0.35f, fy - fSz, fx + fSz * 0.35f, fy + fSz, 0xFFFFFF, 240);
                }
            }
        }

        float thermX = wide ? (x + skyW + (w - skyW) * 0.25f) : (x + w - Math.max(3.2f, w * 0.16f));
        float thermTop = y + h * 0.18f;
        float thermBot = y + h * 0.78f;
        float bulbR = Math.max(1.4f, Math.min(w, h) * 0.095f);
        float tubeW = Math.max(1.2f, bulbR * 0.95f);

        float targetFill = switch (state) {
            case 0 -> isNight ? 0.52f : 0.88f;
            case 1 -> 0.48f;
            case 2 -> 0.38f;
            default -> 0.18f;
        };
        int mercCol = switch (state) {
            case 0 -> isNight ? 0x00FFAA : 0xFF3322;
            case 1 -> 0xFFAA00;
            case 2 -> 0x00E5FF;
            default -> 0x66CCFF;
        };

        q(vc, mat, light, g, thermX - tubeW * 0.5f, thermTop, thermX + tubeW * 0.5f, thermBot, 0x142234, 245);
        drawOutlineBox(vc, mat, light, g, thermX - tubeW * 0.5f, thermTop, tubeW, thermBot - thermTop, 0.45f, 0x88AABB, 220);

        float fillTop = thermBot - targetFill * (thermBot - thermTop - 1.0f);
        q(vc, mat, light, g, thermX - tubeW * 0.32f, fillTop, thermX + tubeW * 0.32f, thermBot, mercCol, 255);
        drawFilledCircle(vc, mat, light, g, thermX, thermBot + bulbR * 0.35f, bulbR, bulbR, 6, mercCol, 255);
        q(vc, mat, light, g, thermX - bulbR * 0.35f, thermBot, thermX + bulbR * 0.15f, thermBot + bulbR * 0.5f, 0xFFFFFF, 210);

        if (wide && w >= 28.0f) {
            int tempVal = switch (state) {
                case 0 -> isNight ? 15 : 28;
                case 1 -> 17;
                case 2 -> 12;
                default -> 2;
            };
            float dw = Math.max(1.6f, Math.min(3.2f, (w - skyW) * 0.24f));
            float dh = dw * 1.5f;
            float dx = thermX + bulbR + 1.6f;
            float dy = y + (h - dh) * 0.5f;
            int dimCol = blendColor(mercCol, 0x000000, 0.82f);
            draw7SegmentDigit(vc, mat, light, g, dx, dy, dw, dh, (tempVal / 10) % 10, mercCol, dimCol);
            draw7SegmentDigit(vc, mat, light, g, dx + dw * 1.3f, dy, dw, dh, tempVal % 10, 0xFFFFFF, dimCol);
            q(vc, mat, light, g, dx + dw * 2.55f, dy, dx + dw * 2.55f + 1.0f, dy + 1.0f, 0xFFFFFF, 245);
        }
    }

    private static void renderBowlingAlley(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x080612, 255);

        float cx = x + w * 0.5f;
        float deckY = y + h * 0.24f;
        float botY = y + h;
        float laneH = botY - deckY;
        float topHalfW = w * 0.22f;
        float botHalfW = w * 0.44f;

        q(vc, mat, light, g, cx - topHalfW * 1.18f, y + 0.8f, cx + topHalfW * 1.18f, deckY, 0x040308, 255);
        int themeCol = rainbow ? getRainbowColor(t * 0.35) : 0x00E5FF;
        drawOutlineBox(vc, mat, light, g, cx - topHalfW * 1.18f, y + 0.8f, topHalfW * 2.36f, deckY - y - 0.8f, 0.55f, themeCol, 255);

        int slices = lowDetail ? 14 : 22;
        float sliceH = laneH / slices;
        for (int i = 0; i < slices; i++) {
            float f1 = (float) i / slices;
            float f2 = (float) (i + 1) / slices;
            float fm = (f1 + f2) * 0.5f;
            float y1 = deckY + f1 * laneH;
            float y2 = deckY + f2 * laneH + 0.15f;
            float hw = topHalfW + fm * (botHalfW - topHalfW);
            float gutW = Math.max(1.0f, hw * 0.16f);

            q(vc, mat, light, g, cx - hw - gutW, y1, cx - hw, y2, 0x141822, 255);
            q(vc, mat, light, g, cx + hw, y1, cx + hw + gutW, y2, 0x141822, 255);

            int woodBase = (i % 2 == 0) ? 0xD89B52 : 0xC88A42;
            q(vc, mat, light, g, cx - hw, y1, cx + hw, y2, woodBase, 255);
            q(vc, mat, light, g, cx - hw * 0.36f, y1, cx + hw * 0.36f, y2, blendColor(woodBase, 0xFFE4A8, 0.25f), 255);
        }

        for (int p = -2; p <= 2; p++) {
            if (p == 0) continue;
            float f = p * 0.38f;
            drawLine(vc, mat, light, g, cx + topHalfW * f, deckY, cx + botHalfW * f, botY, 0.5f, 0x9E6424, 220);
        }

        for (int a = -1; a <= 1; a++) {
            float ay = deckY + laneH * (0.48f + Math.abs(a) * 0.06f);
            float ahw = topHalfW + 0.50f * (botHalfW - topHalfW);
            float ax = cx + a * (ahw * 0.38f);
            drawLine(vc, mat, light, g, ax, ay - 1.2f, ax - 0.9f, ay + 0.8f, 0.55f, 0xB82818, 255);
            drawLine(vc, mat, light, g, ax, ay - 1.2f, ax + 0.9f, ay + 0.8f, 0.55f, 0xB82818, 255);
        }

        double cycle = (t * 1.10) % 4.2;
        boolean strike = cycle >= 1.95;
        float strikeAge = strike ? (float) (cycle - 1.95) : 0.0f;

        float[][] pinLayout = {
                { -0.75f, -0.22f }, { -0.25f, -0.22f }, { 0.25f, -0.22f }, { 0.75f, -0.22f },
                { -0.50f, -0.10f }, { 0.0f, -0.10f }, { 0.50f, -0.10f },
                { -0.25f, 0.02f }, { 0.25f, 0.02f },
                { 0.0f, 0.14f }
        };

        float pinW = Math.max(1.3f, topHalfW * 0.26f);
        float pinH = Math.max(2.6f, h * 0.17f);

        for (int p = 0; p < pinLayout.length; p++) {
            float px = cx + pinLayout[p][0] * (topHalfW * 0.88f);
            float py = deckY + pinLayout[p][1] * (h * 0.22f);

            if (strike) {
                if (strikeAge > 1.4f) continue;
                float dirX = (pinLayout[p][0] == 0.0f) ? ((p % 2 == 0) ? -0.45f : 0.45f) : (pinLayout[p][0] * 1.5f);
                float dirY = -0.35f - (p % 3) * 0.22f;
                px += dirX * (w * 0.32f) * strikeAge;
                py += dirY * (h * 0.42f) * strikeAge;
            }

            drawFilledCircle(vc, mat, light, g, px, py, pinW * 0.55f, pinH * 0.34f, 4, 0xE8EEF5, 255);
            q(vc, mat, light, g, px - pinW * 0.32f, py - pinH * 0.48f, px + pinW * 0.32f, py, 0xFFFFFF, 255);
            drawFilledCircle(vc, mat, light, g, px, py - pinH * 0.52f, pinW * 0.36f, pinW * 0.36f, 4, 0xFFFFFF, 255);
            q(vc, mat, light, g, px - pinW * 0.34f, py - pinH * 0.32f, px + pinW * 0.34f, py - pinH * 0.16f, 0xFF1E27, 255);
        }

        if (!strike) {
            float prog = (float) (cycle / 1.95);
            float ballY = (botY - 1.5f) - prog * (laneH - 1.5f);
            float hook = (float) Math.sin(prog * Math.PI) * (topHalfW * 0.55f);
            float ballX = cx + (1.0f - prog) * (botHalfW * 0.25f) + hook;
            float ballR = Math.max(1.4f, Math.min(w, h) * (0.14f - prog * 0.06f));
            int ballCol = rainbow ? getRainbowColor(t * 0.4) : (baseColor == 0x00FF66 ? 0x1E66FF : baseColor);

            drawFilledCircle(vc, mat, light, g, ballX, ballY + ballR * 0.4f, ballR * 0.9f, ballR * 0.5f, 4, 0x5A3814, 255);
            drawFilledCircle(vc, mat, light, g, ballX, ballY, ballR, ballR, 7, ballCol, 255);
            drawFilledCircle(vc, mat, light, g, ballX - ballR * 0.28f, ballY - ballR * 0.28f, ballR * 0.32f, ballR * 0.32f, 4, blendColor(ballCol, 0xFFFFFF, 0.65f), 255);
        } else if (strikeAge < 1.55f) {
            float xSz = Math.min(w, h) * 0.20f;
            float xCy = y + h * 0.56f;
            q(vc, mat, light, g, cx - xSz * 1.45f, xCy - xSz * 1.1f, cx + xSz * 1.45f, xCy + xSz * 1.1f, 0x100822, 255);
            int rimCol = (((int) (t * 15.0) % 2) == 0) ? 0xFFD700 : 0x00E5FF;
            drawOutlineBox(vc, mat, light, g, cx - xSz * 1.45f, xCy - xSz * 1.1f, xSz * 2.9f, xSz * 2.2f, 0.75f, rimCol, 255);
            int xCol = (((int) (t * 15.0) % 2) == 0) ? 0xFFFFFF : 0xFF2244;
            drawLine(vc, mat, light, g, cx - xSz * 0.75f, xCy - xSz * 0.70f, cx + xSz * 0.75f, xCy + xSz * 0.70f, Math.max(1.2f, xSz * 0.32f), xCol, 255);
            drawLine(vc, mat, light, g, cx + xSz * 0.75f, xCy - xSz * 0.70f, cx - xSz * 0.75f, xCy + xSz * 0.70f, Math.max(1.2f, xSz * 0.32f), xCol, 255);
        }
    }

    private static void renderVolcano(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                      float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        double pulse = 0.5 + 0.5 * Math.sin(t * 2.8);
        q(vc, mat, light, g, x, y, x + w, y + h, 0x0C0510, 255);
        q(vc, mat, light, g, x, y + h * 0.35f, x + w, y + h, blendColor(0x1E0812, 0x4A1208, (float) pulse), 220);

        float cx = x + w * 0.5f;
        float craterY = y + h * 0.42f;
        float baseY = y + h;
        float craterHalfW = w * 0.15f;
        float baseHalfW = w * 0.46f;

        int lavaCol = rainbow ? getRainbowColor(t * 0.35) : 0xFF5500;
        int hotLava = blendColor(lavaCol, 0xFFFF99, 0.72f);

        int smokePuffs = lowDetail ? 5 : 8;
        for (int s = 0; s < smokePuffs; s++) {
            float sf = (float) ((t * 0.52 + s * (1.0 / smokePuffs)) % 1.0);
            float sy = craterY - sf * (craterY - y + 1.5f);
            float sx = cx + (float) Math.sin(t * 2.2 + s * 1.6) * (craterHalfW * (0.4f + sf * 1.8f));
            float sr = craterHalfW * (0.60f + sf * 0.95f);
            int topAsh = 0x262028;
            int litAsh = blendColor(lavaCol, 0x382428, 0.55f);
            int sAlpha = (int) (225 * (1.0f - sf * 0.72f));
            drawFilledCircle(vc, mat, light, g, sx, sy, sr, sr * 0.72f, 6, topAsh, sAlpha);
            if (sf < 0.55f) {
                drawFilledCircle(vc, mat, light, g, sx, sy + sr * 0.25f, sr * 0.78f, sr * 0.42f, 5, litAsh, sAlpha);
            }
        }

        if (((int) (t * 14.0) % 6) == 0) {
            float lx = cx + (float) Math.sin(t * 33.0) * (craterHalfW * 0.7f);
            drawLine(vc, mat, light, g, lx, craterY - 1.0f, lx - craterHalfW * 0.5f, y + h * 0.20f, 0.75f, 0xFFFFAA, 255);
            drawLine(vc, mat, light, g, lx - craterHalfW * 0.5f, y + h * 0.20f, lx + craterHalfW * 0.4f, y + h * 0.08f, 0.65f, 0xFFFFFF, 240);
        }

        float jetH = h * (0.16f + 0.12f * (float) Math.abs(Math.sin(t * 5.5)));
        drawFilledCircle(vc, mat, light, g, cx, craterY - jetH * 0.45f, craterHalfW * 0.65f, jetH * 0.65f, 7, lavaCol, 245);
        drawFilledCircle(vc, mat, light, g, cx, craterY - jetH * 0.35f, craterHalfW * 0.38f, jetH * 0.45f, 5, 0xFFFFCC, 255);

        int bombs = lowDetail ? 6 : 9;
        for (int b = 0; b < bombs; b++) {
            float bf = (float) ((t * (1.05 + (b % 3) * 0.22) + b * 0.29) % 1.0);
            float prevBf = Math.max(0.0f, bf - 0.10f);
            float vx = ((b % 2 == 0) ? 1.0f : -1.0f) * (0.28f + (b % 4) * 0.20f) * (w * 0.40f);
            float arcH = h * (0.34f + (b % 3) * 0.07f);

            float bx = cx + vx * bf;
            float by = craterY - (4.0f * bf * (1.0f - bf)) * arcH + bf * bf * (h * 0.38f);
            float tx = cx + vx * prevBf;
            float ty = craterY - (4.0f * prevBf * (1.0f - prevBf)) * arcH + prevBf * prevBf * (h * 0.38f);

            float br = Math.max(0.85f, Math.min(w, h) * 0.048f * (1.0f - bf * 0.35f));
            drawLine(vc, mat, light, g, tx, ty, bx, by, br * 1.1f, lavaCol, 180);
            drawFilledCircle(vc, mat, light, g, bx, by, br, br, 4, (bf < 0.45f) ? 0xFFFFCC : hotLava, 255);
        }

        int mSlices = lowDetail ? 14 : 20;
        float mSliceH = (baseY - craterY) / mSlices;
        for (int i = 0; i < mSlices; i++) {
            float f = (float) i / mSlices;
            float slope = (float) Math.pow(f, 0.80);
            float hw = craterHalfW + slope * (baseHalfW - craterHalfW);
            float y1 = craterY + i * mSliceH;
            float y2 = y1 + mSliceH + 0.15f;

            q(vc, mat, light, g, cx - hw, y1, cx + hw, y2, 0x241A22, 255);
            q(vc, mat, light, g, cx - hw, y1, cx - hw * 0.35f, y2, 0x3A2832, 255);
            q(vc, mat, light, g, cx + hw * 0.45f, y1, cx + hw, y2, 0x161016, 255);

            float lavaProgress = (float) ((t * 1.6 + f * 2.0) % 1.0);
            float l1x = cx - hw * 0.38f + (float) Math.sin(f * 5.0 + 0.5) * (hw * 0.12f);
            float l2x = cx + hw * 0.08f + (float) Math.cos(f * 6.0) * (hw * 0.10f);
            float l3x = cx + hw * 0.42f + (float) Math.sin(f * 4.5 - 0.8) * (hw * 0.12f);
            float lw = Math.max(0.9f, hw * (0.14f - f * 0.04f));

            q(vc, mat, light, g, l1x - lw, y1, l1x + lw, y2, lavaCol, 255);
            q(vc, mat, light, g, l1x - lw * 0.45f, y1, l1x + lw * 0.45f, y2, (lavaProgress < 0.5f) ? 0xFFFFAA : hotLava, 255);

            q(vc, mat, light, g, l2x - lw * 0.9f, y1, l2x + lw * 0.9f, y2, lavaCol, 255);
            q(vc, mat, light, g, l2x - lw * 0.4f, y1, l2x + lw * 0.4f, y2, hotLava, 255);

            if (f < 0.78f) {
                q(vc, mat, light, g, l3x - lw * 0.8f, y1, l3x + lw * 0.8f, y2, lavaCol, 255);
            }
        }

        drawFilledCircle(vc, mat, light, g, cx, craterY, craterHalfW * 0.95f, Math.max(1.3f, h * 0.065f), 6, 0xFFFFCC, 255);
    }

    private static void renderHydraulicPress(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                             float x, float y, float w, float h, int baseColor, boolean rainbow, double t) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x0B0E14, 255);

        float cx = x + w * 0.5f;
        float pillarW = Math.max(1.2f, w * 0.06f);
        q(vc, mat, light, g, x + w * 0.12f, y, x + w * 0.12f + pillarW, y + h, 0x262E38, 255);
        q(vc, mat, light, g, x + w * 0.88f - pillarW, y, x + w * 0.88f, y + h, 0x262E38, 255);

        float anvilH = Math.max(2.5f, h * 0.18f);
        float anvilY = y + h - anvilH;
        float anvilW = w * 0.72f;
        q(vc, mat, light, g, cx - anvilW * 0.5f, anvilY, cx + anvilW * 0.5f, y + h, 0x384250, 255);

        float stripeW = Math.max(2.5f, anvilW * 0.10f);
        for (float sx = cx - anvilW * 0.48f; sx < cx + anvilW * 0.48f - stripeW; sx += stripeW * 2.0f) {
            q(vc, mat, light, g, sx, anvilY + anvilH * 0.35f, sx + stripeW, y + h - 0.5f, 0xFFCC00, 245);
        }

        double cycle = t % 4.8;
        float pressFrac;
        boolean crushing = false;
        if (cycle < 0.8) {
            pressFrac = (float) (cycle / 0.8) * 0.35f;
        } else if (cycle < 2.5) {
            crushing = true;
            float u = (float) ((cycle - 0.8) / 1.7);
            pressFrac = 0.35f + u * 0.65f;
        } else if (cycle < 3.2) {
            crushing = true;
            pressFrac = 1.0f;
        } else {
            float u = (float) ((cycle - 3.2) / 1.6);
            pressFrac = 1.0f - u * u * (3.0f - 2.0f * u);
        }

        float shakeX = (crushing && pressFrac > 0.65f) ? ((float) Math.sin(t * 55.0) * 0.6f) : 0.0f;

        float maxItemH = (anvilY - y) * 0.42f;
        float minItemH = Math.max(1.2f, maxItemH * 0.18f);
        float comp = Math.max(0.0f, (pressFrac - 0.35f) / 0.65f);
        float curItemH = maxItemH - comp * (maxItemH - minItemH);
        float baseItemW = Math.min(w * 0.24f, maxItemH * 1.05f);
        float curItemW = baseItemW * (1.0f + comp * 0.85f);

        int itemCol = rainbow ? getRainbowColor(t * 0.4) : baseColor;
        int stressedCol = blendColor(itemCol, 0xFF3300, comp * 0.75f);
        if (comp > 0.85f && ((int) (t * 18.0) % 2 == 0)) {
            stressedCol = 0xFFFFAA;
        }

        float itemTopY = anvilY - curItemH;
        drawFilledCircle(vc, mat, light, g, cx + shakeX, itemTopY + curItemH * 0.5f, curItemW * 0.52f, curItemH * 0.52f, 7, stressedCol, 255);
        drawFilledCircle(vc, mat, light, g, cx + shakeX, itemTopY + curItemH * 0.45f, curItemW * 0.28f, curItemH * 0.28f, 4, blendColor(stressedCol, 0xFFFFFF, 0.55f), 230);

        float headH = Math.max(2.4f, h * 0.16f);
        float headW = anvilW * 0.86f;
        float startHeadBot = anvilY - maxItemH - (anvilY - y - maxItemH - headH) * 0.65f;
        float curHeadBot = (comp > 0.0f) ? itemTopY : (startHeadBot + (pressFrac / 0.35f) * (itemTopY - startHeadBot));
        float curHeadTop = curHeadBot - headH;

        float shaftW = headW * 0.36f;
        q(vc, mat, light, g, cx - shaftW * 0.5f + shakeX, y, cx + shaftW * 0.5f + shakeX, curHeadTop, 0xAEB8C4, 255);
        q(vc, mat, light, g, cx - shaftW * 0.25f + shakeX, y, cx - shaftW * 0.05f + shakeX, curHeadTop, 0xFFFFFF, 220);

        q(vc, mat, light, g, cx - headW * 0.5f + shakeX, curHeadTop, cx + headW * 0.5f + shakeX, curHeadBot, 0x465262, 255);
        drawOutlineBox(vc, mat, light, g, cx - headW * 0.5f + shakeX, curHeadTop, headW, headH, 0.65f, 0x7A889B, 255);

        if (comp > 0.55f && cycle < 3.2) {
            for (int sp = 0; sp < 6; sp++) {
                float dir = (sp % 2 == 0) ? 1.0f : -1.0f;
                float sf = (float) ((t * 5.5 + sp * 0.19) % 1.0);
                float sx1 = cx + dir * (curItemW * 0.45f);
                float sy1 = anvilY - curItemH * 0.35f;
                float sx2 = sx1 + dir * sf * (w * 0.26f);
                float sy2 = sy1 - (sf * (1.0f - sf)) * (h * 0.22f) + (sp % 3 - 1) * 1.5f;
                drawLine(vc, mat, light, g, sx1, sy1, sx2, sy2, 0.7f, (sp % 2 == 0) ? 0xFFEE44 : 0xFF6600, 245);
            }
        } else if (cycle >= 3.2 && cycle < 4.1) {
            float steamF = (float) ((cycle - 3.2) / 0.9);
            float sr = Math.max(1.2f, h * 0.09f * (0.5f + steamF));
            int sAlpha = (int) (190 * (1.0f - steamF));
            drawFilledCircle(vc, mat, light, g, cx - headW * 0.55f - steamF * 3.0f, curHeadTop + 1.0f, sr, sr, 5, 0xE8F4FF, sAlpha);
            drawFilledCircle(vc, mat, light, g, cx + headW * 0.55f + steamF * 3.0f, curHeadTop + 1.0f, sr, sr, 5, 0xE8F4FF, sAlpha);
        }
    }

    private static void renderTvStaticBars(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        double cycle = t % 4.6;
        boolean fullStatic = (cycle > 1.85 && cycle < 2.55) || (cycle > 3.85);
        boolean glitchBurst = (cycle > 1.60 && cycle <= 1.85) || (cycle > 3.60 && cycle <= 3.85);

        if (!fullStatic) {
            int[] smpte = TV_SMPTE;
            int[] subBars = TV_SUB_BARS;
            float mainH = h * 0.68f;
            float midH = h * 0.10f;
            float botY = y + mainH + midH;
            float barW = w / 7.0f;

            float jitterX = glitchBurst ? ((float) Math.sin(t * 95.0) * (w * 0.04f)) : 0.0f;

            for (int i = 0; i < 7; i++) {
                float bx1 = x + i * barW + jitterX;
                float bx2 = bx1 + barW + 0.25f;
                int cTop = rainbow ? getRainbowColor(t * 0.35 + i * 0.14) : smpte[i];
                q(vc, mat, light, g, bx1, y, bx2, y + mainH + 0.2f, cTop, 255);
                q(vc, mat, light, g, bx1, y + mainH, bx2, botY + 0.2f, subBars[i], 255);
            }

            q(vc, mat, light, g, x, botY, x + w * 0.18f, y + h, 0x081D3A, 255);
            q(vc, mat, light, g, x + w * 0.18f, botY, x + w * 0.36f, y + h, 0xFFFFFF, 255);
            q(vc, mat, light, g, x + w * 0.36f, botY, x + w * 0.54f, y + h, 0x260A4A, 255);
            q(vc, mat, light, g, x + w * 0.54f, botY, x + w, y + h, 0x0E0E12, 255);

            float cx = x + w * 0.5f;
            float cy = y + h * 0.42f;
            float r = Math.min(w, h) * 0.24f;
            drawFilledCircle(vc, mat, light, g, cx, cy, r * 0.72f, r * 0.72f, 6, 0x0C1018, 210);
            drawEllipseArc(vc, mat, light, g, cx, cy, r, r, 0.7f, 0, Math.PI * 2.0, lowDetail ? 12 : 18, 0xFFFFFF, 235);
            q(vc, mat, light, g, cx - r * 1.15f, cy - 0.35f, cx + r * 1.15f, cy + 0.35f, 0xFFFFFF, 220);
            q(vc, mat, light, g, cx - 0.35f, cy - r * 1.15f, cx + 0.35f, cy + r * 1.15f, 0xFFFFFF, 220);
        }

        if (fullStatic || glitchBurst) {
            q(vc, mat, light, g, x, y, x + w, y + h, 0x0A0C10, fullStatic ? 255 : 145);
            int cols = lowDetail ? 14 : 22;
            int rows = lowDetail ? 9 : 14;
            float cw = w / cols;
            float ch = h / rows;
            long frameSeed = (long) (t * 28.0);

            for (int r = 0; r < rows; r++) {
                if (glitchBurst && ((r + (int) frameSeed) % 3 != 0)) continue;
                for (int c = 0; c < cols; c++) {
                    long hash = (frameSeed * 1664525L + r * 374761393L + c * 668265263L) ^ 0x5DEECE66DL;
                    int v = (int) ((hash >>> 16) & 0xFF);
                    if (v < 45) continue;
                    int gray = (v > 200) ? 0xFFFFFF : ((v > 135) ? 0xB8C0C8 : 0x485058);
                    if (rainbow && v > 180) {
                        gray = getRainbowColor((c * 0.08) + t * 0.5);
                    }
                    float sx = x + c * cw;
                    float sy = y + r * ch;
                    q(vc, mat, light, g, sx, sy, sx + cw + 0.15f, sy + ch + 0.15f, gray, fullStatic ? 245 : 190);
                }
            }
        }

        float vhsY = y + (float) (((t * 0.55) % 1.0) * h);
        float vhsH = Math.max(1.8f, h * 0.11f);
        q(vc, mat, light, g, x, vhsY, x + w, vhsY + vhsH, 0xFFFFFF, 85);
        q(vc, mat, light, g, x, vhsY + vhsH * 0.35f, x + w, vhsY + vhsH * 0.65f, 0x000000, 120);
    }

    private static void renderBreakingNews(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x061024, 255);
        q(vc, mat, light, g, x, y, x + w, y + h * 0.64f, 0x0B1E3D, 220);

        float globeCx = x + w * 0.25f;
        float globeCy = y + h * 0.34f;
        float globeR = Math.min(w, h) * 0.23f;
        int globeCol = rainbow ? getRainbowColor(t * 0.25) : 0x00B8FF;
        drawFilledCircle(vc, mat, light, g, globeCx, globeCy, globeR, globeR, 8, 0x082A52, 235);
        drawEllipseArc(vc, mat, light, g, globeCx, globeCy, globeR, globeR, 0.55f, 0, Math.PI * 2.0, lowDetail ? 12 : 18, globeCol, 230);
        q(vc, mat, light, g, globeCx - globeR, globeCy - 0.3f, globeCx + globeR, globeCy + 0.3f, globeCol, 180);
        float lonRx = globeR * (float) Math.abs(Math.cos(t * 1.6));
        if (lonRx > 0.4f) {
            drawEllipseArc(vc, mat, light, g, globeCx, globeCy, lonRx, globeR, 0.5f, 0, Math.PI * 2.0, lowDetail ? 10 : 14, globeCol, 190);
        }

        float liveW = Math.max(4.5f, w * 0.22f);
        float liveH = Math.max(2.0f, h * 0.14f);
        float liveX = x + w - liveW - 1.2f;
        float liveY = y + 1.0f;
        q(vc, mat, light, g, liveX, liveY, liveX + liveW, liveY + liveH, 0xCC1122, 250);
        if (((int) (t * 5.0) % 2) == 0) {
            float dotR = Math.max(0.6f, liveH * 0.26f);
            drawFilledCircle(vc, mat, light, g, liveX + dotR * 1.8f, liveY + liveH * 0.5f, dotR, dotR, 4, 0xFFFFFF, 255);
        }
        q(vc, mat, light, g, liveX + liveH * 0.95f, liveY + liveH * 0.32f, liveX + liveW - 1.0f, liveY + liveH * 0.68f, 0xFFFFFF, 240);

        float ancCx = x + w * 0.62f;
        float deskTop = y + h * 0.54f;
        float headR = Math.min(w, h) * 0.105f;
        float headBob = (float) Math.sin(t * 7.5) * 0.35f;
        float headCy = deskTop - headR * 2.15f + headBob;
        float shW = headR * 2.9f;
        float shH = deskTop - (headCy + headR * 0.85f);

        drawFilledCircle(vc, mat, light, g, ancCx, deskTop, shW * 0.55f, shH, 6, 0x1C2838, 255);
        q(vc, mat, light, g, ancCx - headR * 0.45f, headCy + headR * 0.85f, ancCx + headR * 0.45f, deskTop, 0xF0F4F8, 255);
        q(vc, mat, light, g, ancCx - headR * 0.18f, headCy + headR * 0.95f, ancCx + headR * 0.18f, deskTop, 0xE01B24, 255);

        drawFilledCircle(vc, mat, light, g, ancCx, headCy, headR, headR * 1.08f, 6, 0xF2C29B, 255);
        drawFilledCircle(vc, mat, light, g, ancCx, headCy - headR * 0.55f, headR * 1.05f, headR * 0.60f, 5, 0x221A14, 255);

        q(vc, mat, light, g, ancCx - shW * 0.72f, deskTop, ancCx + shW * 0.72f, y + h * 0.68f, 0x102242, 255);
        q(vc, mat, light, g, ancCx - shW * 0.72f, deskTop, ancCx + shW * 0.72f, deskTop + 0.8f, 0x00E5FF, 255);

        float banTop = y + h * 0.66f;
        float tickTop = y + h * 0.83f;
        int banCol = (((int) (t * 4.0) % 2) == 0) ? 0xD81420 : 0xB80E18;
        q(vc, mat, light, g, x, banTop, x + w, tickTop, banCol, 255);
        q(vc, mat, light, g, x, banTop, x + w, banTop + 0.7f, 0xFFD700, 255);

        float badgeW = w * 0.28f;
        q(vc, mat, light, g, x + 1.0f, banTop + 1.1f, x + 1.0f + badgeW, tickTop - 0.8f, 0xFFD700, 255);
        q(vc, mat, light, g, x + 2.2f + badgeW, banTop + (tickTop - banTop) * 0.34f, x + w - 2.0f, banTop + (tickTop - banTop) * 0.68f, 0xFFFFFF, 245);

        q(vc, mat, light, g, x, tickTop, x + w, y + h, 0xF0F4F8, 255);
        float scroll = (float) ((t * 14.0) % (w * 0.45f));
        float segStep = Math.max(3.5f, w * 0.15f);
        for (float tx = x - segStep + (segStep - (scroll % segStep)); tx < x + w; tx += segStep) {
            float tw = segStep * 0.68f;
            q(vc, mat, light, g, tx, tickTop + (y + h - tickTop) * 0.30f, tx + tw, tickTop + (y + h - tickTop) * 0.72f, 0x121824, 245);
            q(vc, mat, light, g, tx + tw + 0.8f, tickTop + (y + h - tickTop) * 0.38f, tx + tw + 1.6f, tickTop + (y + h - tickTop) * 0.64f, 0xD81420, 245);
        }
    }

    private static void renderSoccerMatch(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                          float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        double cycle = (t * 0.85) % 4.8;
        boolean isGoal = cycle >= 2.85;

        float standsH = Math.max(2.4f, h * 0.18f);
        float pitchY = y + standsH;
        float pitchH = h - standsH;

        q(vc, mat, light, g, x, y, x + w, pitchY, 0x101622, 255);
        int flashes = isGoal ? 9 : 3;
        long flashSeed = (long) (t * (isGoal ? 20.0 : 6.0));
        for (int f = 0; f < flashes; f++) {
            long fh = (flashSeed * 2654435761L + f * 97531L) & 0xFFFFL;
            float fx = x + 1.0f + (fh % 100) * 0.01f * (w - 2.0f);
            float fy = y + 0.3f + ((fh >> 8) % 100) * 0.01f * (standsH - 0.8f);
            q(vc, mat, light, g, fx, fy, fx + 1.0f, fy + 0.9f, (isGoal && f % 2 == 0) ? 0xFFEE55 : 0xFFFFFF, 245);
        }

        int stripes = 10;
        float stripeW = w / stripes;
        for (int s = 0; s < stripes; s++) {
            float sx = x + s * stripeW;
            int turfCol = (s % 2 == 0) ? 0x167C2A : 0x126622;
            q(vc, mat, light, g, sx, pitchY, sx + stripeW + 0.2f, y + h, turfCol, 255);
        }

        float pad = Math.max(0.8f, Math.min(w, pitchH) * 0.05f);
        drawOutlineBox(vc, mat, light, g, x + pad, pitchY + pad, w - pad * 2.0f, pitchH - pad * 2.0f, 0.45f, 0xE8F8E8, 210);
        float midX = x + w * 0.5f;
        float midY = pitchY + pitchH * 0.5f;
        q(vc, mat, light, g, midX - 0.25f, pitchY + pad, midX + 0.25f, y + h - pad, 0xE8F8E8, 195);
        float cirR = Math.min(w, pitchH) * 0.18f;
        drawEllipseArc(vc, mat, light, g, midX, midY, cirR, cirR, 0.45f, 0, Math.PI * 2.0, lowDetail ? 10 : 14, 0xE8F8E8, 195);
        q(vc, mat, light, g, midX - 0.5f, midY - 0.5f, midX + 0.5f, midY + 0.5f, 0xFFFFFF, 220);

        float boxW = w * 0.16f;
        float boxH = pitchH * 0.56f;
        drawOutlineBox(vc, mat, light, g, x + pad, midY - boxH * 0.5f, boxW, boxH, 0.45f, 0xE8F8E8, 200);
        drawOutlineBox(vc, mat, light, g, x + w - pad - boxW, midY - boxH * 0.5f, boxW, boxH, 0.45f, 0xE8F8E8, 200);

        float goalW = Math.max(1.5f, w * 0.055f);
        float goalH = pitchH * 0.34f;
        float goalX = x + w - pad - goalW * 0.2f;
        float goalTop = midY - goalH * 0.5f;
        float netStretch = (isGoal && cycle < 3.45) ? ((float) Math.sin((cycle - 2.85) * 18.0) * 0.9f) : 0.0f;
        q(vc, mat, light, g, x + pad - goalW * 0.8f, goalTop, x + pad, goalTop + goalH, 0xD8E8F0, 110);
        drawOutlineBox(vc, mat, light, g, x + pad - goalW * 0.8f, goalTop, goalW * 0.8f, goalH, 0.45f, 0xFFFFFF, 240);
        q(vc, mat, light, g, goalX, goalTop, goalX + goalW + netStretch, goalTop + goalH, 0xD8E8F0, 125);
        drawOutlineBox(vc, mat, light, g, goalX, goalTop, goalW + netStretch, goalH, 0.55f, 0xFFFFFF, 255);

        float sbTopW = w * 0.38f;
        float sbTopH = Math.max(1.8f, standsH * 0.78f);
        q(vc, mat, light, g, x + 1.0f, y + 0.3f, x + 1.0f + sbTopW, y + 0.3f + sbTopH, 0x0A101C, 240);
        q(vc, mat, light, g, x + 1.2f, y + 0.5f, x + 1.2f + sbTopW * 0.28f, y + 0.1f + sbTopH, 0x0088FF, 255);
        q(vc, mat, light, g, x + 1.0f + sbTopW * 0.72f, y + 0.5f, x + 0.8f + sbTopW, y + 0.1f + sbTopH, 0xEE2233, 255);
        q(vc, mat, light, g, x + 1.0f + sbTopW * 0.34f, y + 0.5f, x + 1.0f + sbTopW * 0.66f, y + 0.1f + sbTopH, isGoal ? 0xFFD700 : 0xFFFFFF, 255);

        float pR = Math.max(0.95f, Math.min(w, pitchH) * 0.065f);
        float a1x = x + w * 0.22f + (float) Math.sin(t * 2.0) * (w * 0.03f);
        float a1y = pitchY + pitchH * 0.52f;
        float a2x = x + w * (0.38f + 0.08f * (float) Math.min(1.0, cycle / 1.2));
        float a2y = pitchY + pitchH * 0.26f;
        float a3x = x + w * (0.54f + 0.10f * (float) Math.min(1.0, cycle / 2.0));
        float a3y = pitchY + pitchH * 0.74f;
        float a4x = x + w * (0.66f + 0.08f * (float) Math.min(1.0, cycle / 2.6));
        float a4y = pitchY + pitchH * 0.44f;

        float d1x = x + w * 0.48f;
        float d1y = pitchY + pitchH * (0.46f + 0.08f * (float) Math.cos(t * 2.6));
        float d2x = x + w * 0.68f;
        float d2y = pitchY + pitchH * (0.62f + 0.07f * (float) Math.sin(t * 2.4));
        float d3x = x + w * 0.74f;
        float d3y = pitchY + pitchH * (0.30f + 0.06f * (float) Math.cos(t * 3.0));
        float gkX = goalX - pR * 0.8f;
        float gkY = midY + (cycle > 2.0 ? (pitchH * 0.15f) : ((float) Math.sin(t * 4.5) * (goalH * 0.22f)));

        float[][] blueTeam = { { a1x, a1y }, { a2x, a2y }, { a3x, a3y }, { a4x, a4y } };
        for (float[] p : blueTeam) {
            q(vc, mat, light, g, p[0] - pR * 0.7f, p[1] + pR * 0.5f, p[0] + pR * 0.7f, p[1] + pR * 1.05f, 0x093A12, 180);
            drawFilledCircle(vc, mat, light, g, p[0], p[1], pR, pR * 1.1f, 5, 0x0088FF, 255);
            drawFilledCircle(vc, mat, light, g, p[0], p[1] - pR * 0.65f, pR * 0.58f, pR * 0.58f, 4, 0xF5CBA7, 255);
        }
        float[][] redTeam = { { d1x, d1y }, { d2x, d2y }, { d3x, d3y } };
        for (float[] p : redTeam) {
            q(vc, mat, light, g, p[0] - pR * 0.7f, p[1] + pR * 0.5f, p[0] + pR * 0.7f, p[1] + pR * 1.05f, 0x093A12, 180);
            drawFilledCircle(vc, mat, light, g, p[0], p[1], pR, pR * 1.1f, 5, 0xEE2233, 255);
            drawFilledCircle(vc, mat, light, g, p[0], p[1] - pR * 0.65f, pR * 0.58f, pR * 0.58f, 4, 0xF5CBA7, 255);
        }
        drawFilledCircle(vc, mat, light, g, gkX, gkY, pR, pR * 1.1f, 5, 0xFFCC00, 255);
        drawFilledCircle(vc, mat, light, g, gkX, gkY - pR * 0.65f, pR * 0.58f, pR * 0.58f, 4, 0xF5CBA7, 255);

        float ballX;
        float ballY;
        if (cycle < 0.70) {
            float u = (float) (cycle / 0.70);
            ballX = a1x + (a2x - a1x) * u;
            ballY = a1y + (a2y - a1y) * u;
        } else if (cycle < 1.45) {
            float u = (float) ((cycle - 0.70) / 0.75);
            ballX = a2x + (a3x - a2x) * u;
            ballY = a2y + (a3y - a2y) * u;
        } else if (cycle < 2.10) {
            float u = (float) ((cycle - 1.45) / 0.65);
            ballX = a3x + (a4x - a3x) * u;
            ballY = a3y + (a4y - a3y) * u;
        } else if (cycle < 2.85) {
            float u = (float) ((cycle - 2.10) / 0.75);
            float targetX = goalX + goalW * 0.58f;
            float targetY = goalTop + goalH * 0.24f;
            ballX = a4x + (targetX - a4x) * u;
            ballY = a4y + (targetY - a4y) * u;
            float trailU = Math.max(0.0f, u - 0.25f);
            drawLine(vc, mat, light, g, a4x + (targetX - a4x) * trailU, a4y + (targetY - a4y) * trailU, ballX, ballY, 0.7f, 0xFFFFFF, 190);
        } else {
            ballX = goalX + goalW * 0.58f;
            ballY = goalTop + goalH * 0.24f;
        }
        float ballR = Math.max(0.8f, pR * 0.70f);
        drawFilledCircle(vc, mat, light, g, ballX, ballY, ballR, ballR, 5, 0xFFFFFF, 255);
        q(vc, mat, light, g, ballX - ballR * 0.35f, ballY - ballR * 0.35f, ballX + ballR * 0.35f, ballY + ballR * 0.35f, 0x182028, 240);

        if (isGoal) {
            float sbW = w * 0.58f;
            float sbH = pitchH * 0.52f;
            float sbX = x + (w - sbW) * 0.5f;
            float sbY = pitchY + (pitchH - sbH) * 0.5f;
            int rimCol = (((int) (t * 16.0) % 2) == 0) ? 0xFFD700 : 0x00E5FF;
            q(vc, mat, light, g, sbX, sbY, sbX + sbW, sbY + sbH, 0x081224, 245);
            drawOutlineBox(vc, mat, light, g, sbX, sbY, sbW, sbH, 0.75f, rimCol, 255);

            float dw = Math.max(1.6f, Math.min(sbW * 0.20f, sbH * 0.44f));
            float dh = dw * 1.45f;
            float dy = sbY + (sbH - dh) * 0.5f;
            draw7SegmentDigit(vc, mat, light, g, sbX + sbW * 0.18f, dy, dw, dh, 2, 0x00E5FF, 0x102438);
            q(vc, mat, light, g, sbX + sbW * 0.45f, sbY + sbH * 0.45f, sbX + sbW * 0.55f, sbY + sbH * 0.55f, 0xFFD700, 255);
            draw7SegmentDigit(vc, mat, light, g, sbX + sbW * 0.82f - dw, dy, dw, dh, 1, 0xFF3344, 0x281018);
        }
    }

    private static void renderSecurityCctv(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x040A08, 255);

        float midX = x + w * 0.5f;
        float midY = y + h * 0.5f;
        float qw = w * 0.5f;
        float qh = h * 0.5f;

        q(vc, mat, light, g, x, y, midX, midY, 0x05160E, 255);
        float c1WallTop = y + qh * 0.20f;
        float c1WallBot = y + qh * 0.64f;
        drawLine(vc, mat, light, g, x, y, x + qw * 0.25f, c1WallTop, 0.45f, 0x164A2E, 210);
        drawLine(vc, mat, light, g, midX, y, x + qw * 0.75f, c1WallTop, 0.45f, 0x164A2E, 210);
        drawLine(vc, mat, light, g, x, midY, x + qw * 0.25f, c1WallBot, 0.45f, 0x164A2E, 210);
        drawLine(vc, mat, light, g, midX, midY, x + qw * 0.75f, c1WallBot, 0.45f, 0x164A2E, 210);
        drawOutlineBox(vc, mat, light, g, x + qw * 0.25f, c1WallTop, qw * 0.50f, c1WallBot - c1WallTop, 0.45f, 0x1E603C, 225);

        float walkProg = (float) ((t * 0.35) % 1.0);
        float p1x = x + qw * (0.20f + walkProg * 0.60f);
        float p1y = y + qh * 0.82f;
        float p1h = qh * 0.48f;
        float p1w = p1h * 0.48f;
        drawFilledCircle(vc, mat, light, g, p1x, p1y - p1h * 0.82f, p1w * 0.42f, p1w * 0.42f, 4, 0x44FF99, 245);
        q(vc, mat, light, g, p1x - p1w * 0.35f, p1y - p1h * 0.62f, p1x + p1w * 0.35f, p1y - p1h * 0.22f, 0x44FF99, 235);
        float stride = (float) Math.sin(t * 8.0) * (p1w * 0.42f);
        drawLine(vc, mat, light, g, p1x, p1y - p1h * 0.24f, p1x - stride, p1y, 0.55f, 0x44FF99, 235);
        drawLine(vc, mat, light, g, p1x, p1y - p1h * 0.24f, p1x + stride, p1y, 0.55f, 0x44FF99, 235);
        int boxCol = (((int) (t * 8.0) % 2) == 0) ? 0xFFEE00 : 0xFF2233;
        drawOutlineBox(vc, mat, light, g, p1x - p1w * 0.80f, p1y - p1h * 1.15f, p1w * 1.60f, p1h * 1.25f, 0.45f, boxCol, 250);

        q(vc, mat, light, g, midX, y, x + w, midY, 0x08121E, 255);
        q(vc, mat, light, g, midX, y + qh * 0.78f, x + w, midY, 0x122236, 255);
        float vCx = midX + qw * 0.44f;
        float vCy = y + qh * 0.48f;
        float vR = Math.min(qw, qh) * 0.34f;
        drawFilledCircle(vc, mat, light, g, vCx, vCy, vR, vR, 7, 0x28384C, 255);
        drawEllipseArc(vc, mat, light, g, vCx, vCy, vR, vR, 0.65f, 0, Math.PI * 2.0, lowDetail ? 10 : 14, 0x7A92AC, 255);
        drawFilledCircle(vc, mat, light, g, vCx, vCy, vR * 0.72f, vR * 0.72f, 6, 0x1A2636, 255);
        for (int sp = 0; sp < 4; sp++) {
            double sa = t * 1.8 + sp * (Math.PI * 0.5);
            drawLine(vc, mat, light, g, vCx - (float) Math.cos(sa) * vR * 0.58f, vCy - (float) Math.sin(sa) * vR * 0.58f,
                    vCx + (float) Math.cos(sa) * vR * 0.58f, vCy + (float) Math.sin(sa) * vR * 0.58f, 0.55f, 0xC0D2E4, 245);
        }
        drawFilledCircle(vc, mat, light, g, vCx, vCy, vR * 0.24f, vR * 0.24f, 4, 0x00E5FF, 255);

        float kpX = midX + qw * 0.78f;
        float kpY = y + qh * 0.32f;
        float kpW = qw * 0.14f;
        float kpH = qh * 0.34f;
        q(vc, mat, light, g, kpX, kpY, kpX + kpW, kpY + kpH, 0x182432, 255);
        drawOutlineBox(vc, mat, light, g, kpX, kpY, kpW, kpH, 0.4f, 0x506882, 240);
        int kpLight = (((int) (t * 4.0) % 2) == 0) ? 0x00FF66 : 0xFF2233;
        q(vc, mat, light, g, kpX + kpW * 0.2f, kpY + kpH * 0.18f, kpX + kpW * 0.8f, kpY + kpH * 0.45f, kpLight, 255);

        q(vc, mat, light, g, x, midY, midX, y + h, 0x0C0614, 255);
        q(vc, mat, light, g, x, midY + qh * 0.80f, midX, y + h, 0x180E28, 255);
        float pedX = x + qw * 0.50f;
        float pedY = midY + qh * 0.82f;
        q(vc, mat, light, g, pedX - qw * 0.18f, pedY - qh * 0.10f, pedX + qw * 0.18f, pedY, 0x384458, 255);
        q(vc, mat, light, g, pedX - qw * 0.12f, pedY - qh * 0.32f, pedX + qw * 0.12f, pedY - qh * 0.10f, 0x283244, 255);
        float gemY = pedY - qh * 0.48f + (float) Math.sin(t * 4.0) * 0.4f;
        drawFilledCircle(vc, mat, light, g, pedX, gemY, qw * 0.12f, qh * 0.16f, 5, 0x00FFFF, 255);
        drawFilledCircle(vc, mat, light, g, pedX - qw * 0.03f, gemY - qh * 0.04f, qw * 0.05f, qh * 0.06f, 4, 0xFFFFFF, 240);
        float lasY1 = midY + qh * (0.32f + 0.24f * (float) Math.sin(t * 3.2));
        float lasY2 = midY + qh * (0.58f - 0.24f * (float) Math.cos(t * 2.7));
        drawLine(vc, mat, light, g, x + 0.6f, midY + qh * 0.18f, midX - 0.6f, lasY1, 0.5f, 0xFF1133, 240);
        drawLine(vc, mat, light, g, x + 0.6f, lasY2, midX - 0.6f, midY + qh * 0.22f, 0.5f, 0xFF1133, 240);
        drawLine(vc, mat, light, g, x + 0.6f, midY + qh * 0.70f, midX - 0.6f, midY + qh * 0.70f, 0.5f, 0xFF1133, 220);

        q(vc, mat, light, g, midX, midY, x + w, y + h, 0x060C10, 255);
        boolean cam4Static = ((t % 4.2) > 3.35);
        if (cam4Static) {
            int scols = lowDetail ? 6 : 9;
            int srows = lowDetail ? 4 : 6;
            float scw = qw / scols;
            float sch = qh / srows;
            long seed = (long) (t * 26.0);
            for (int r = 0; r < srows; r++) {
                for (int c = 0; c < scols; c++) {
                    long hsh = (seed * 1664525L + r * 374761393L + c * 668265263L) & 0xFFL;
                    if (hsh < 60) continue;
                    int gCol = (hsh > 180) ? 0xE0E8F0 : 0x506070;
                    q(vc, mat, light, g, midX + c * scw, midY + r * sch, midX + (c + 1) * scw, midY + (r + 1) * sch, gCol, 235);
                }
            }
        } else {
            for (int rk = 0; rk < 3; rk++) {
                float rx = midX + qw * (0.10f + rk * 0.28f);
                float rw = qw * 0.22f;
                float ry = midY + qh * 0.16f;
                float rh = qh * 0.74f;
                q(vc, mat, light, g, rx, ry, rx + rw, ry + rh, 0x14222E, 255);
                drawOutlineBox(vc, mat, light, g, rx, ry, rw, rh, 0.35f, 0x2A4054, 230);
                for (int ld = 0; ld < 4; ld++) {
                    float ly = ry + rh * (0.15f + ld * 0.20f);
                    boolean on = (((int) (t * 6.0) + rk * 3 + ld) % 2) == 0;
                    int lCol = on ? ((ld == 0 && rk == 1) ? 0xFFAA00 : 0x00FF66) : 0x0A1814;
                    q(vc, mat, light, g, rx + rw * 0.22f, ly, rx + rw * 0.78f, ly + 0.65f, lCol, 245);
                }
            }
        }

        int activeCam = ((int) (t * 0.65)) % 4;
        float ax = (activeCam % 2 == 0) ? x : midX;
        float ay = (activeCam < 2) ? y : midY;
        int activeRim = rainbow ? getRainbowColor(t * 0.3) : 0x00FF88;

        q(vc, mat, light, g, midX - 0.35f, y, midX + 0.35f, y + h, 0x283830, 255);
        q(vc, mat, light, g, x, midY - 0.35f, x + w, midY + 0.35f, 0x283830, 255);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, 0.55f, 0x384E42, 255);
        drawOutlineBox(vc, mat, light, g, ax + 0.3f, ay + 0.3f, qw - 0.6f, qh - 0.6f, 0.55f, activeRim, 250);

        if (((int) (t * 3.0) % 2) == 0) {
            float dotR = Math.max(0.55f, Math.min(qw, qh) * 0.06f);
            drawFilledCircle(vc, mat, light, g, x + 1.4f, y + 1.4f, dotR, dotR, 4, 0xFF1E27, 255);
            drawFilledCircle(vc, mat, light, g, midX + 1.4f, y + 1.4f, dotR, dotR, 4, 0xFF1E27, 255);
            drawFilledCircle(vc, mat, light, g, x + 1.4f, midY + 1.4f, dotR, dotR, 4, 0xFF1E27, 255);
            drawFilledCircle(vc, mat, light, g, midX + 1.4f, midY + 1.4f, dotR, dotR, 4, 0xFF1E27, 255);
        }
    }

    private static void renderConcertStage(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x070412, 255);

        double cycle = (t * 0.75) % 4.5;
        float stageY = y + h * 0.54f;
        q(vc, mat, light, g, x, stageY, x + w, y + h, 0x120A24, 255);
        q(vc, mat, light, g, x, stageY, x + w, stageY + 0.7f, 0x00E5FF, 245);

        int[] laserCols = { 0xFF00AA, 0x00E5FF, 0xFFEE00, 0x00FF88 };
        int numLasers = lowDetail ? 3 : 4;
        int beamSlices = lowDetail ? 5 : 8;
        for (int l = 0; l < numLasers; l++) {
            float srcX = x + w * (0.16f + l * (0.68f / (numLasers - 1)));
            float sweep = (float) Math.sin(t * (2.2 + l * 0.45) + l * 1.3) * (w * 0.26f);
            float dstX = srcX + sweep;
            int lCol = rainbow ? getRainbowColor(t * 0.35 + l * 0.22) : laserCols[l % laserCols.length];

            for (int s = 0; s < beamSlices; s++) {
                float f1 = (float) s / beamSlices;
                float f2 = (float) (s + 1) / beamSlices;
                float sy1 = y + 1.0f + f1 * (stageY - y - 1.0f);
                float sy2 = y + 1.0f + f2 * (stageY - y - 1.0f) + 0.15f;
                float bx = srcX + f1 * (dstX - srcX);
                float halfW = 0.35f + f1 * (w * 0.08f);
                q(vc, mat, light, g, bx - halfW, sy1, bx + halfW, sy2, lCol, (int) (105 * (1.0f - f1 * 0.35f)));
            }
            drawFilledCircle(vc, mat, light, g, srcX, y + 1.0f, 0.85f, 0.85f, 4, 0xFFFFFF, 255);
        }
        q(vc, mat, light, g, x, y, x + w, y + 1.0f, 0x2A3040, 255);

        int eqRows = 5;
        float eqW = Math.max(1.4f, w * 0.075f);
        float eqStepY = (stageY - y - 2.2f) / eqRows;
        for (int side = 0; side < 2; side++) {
            float ex = (side == 0) ? (x + 1.0f) : (x + w - 1.0f - eqW);
            int active = 1 + (int) ((0.5 + 0.5 * Math.sin(t * 8.5 + side * 1.7)) * (eqRows - 1));
            for (int r = 0; r < active; r++) {
                float ey = stageY - 0.8f - (r + 1) * eqStepY;
                int eCol = (r >= eqRows - 2) ? 0xFF2255 : ((r >= eqRows - 3) ? 0xFFEE00 : 0x00FF88);
                q(vc, mat, light, g, ex, ey, ex + eqW, ey + eqStepY * 0.72f, eCol, 240);
            }
        }

        float cx = x + w * 0.5f;
        float headR = Math.min(w, h) * 0.068f;
        float headY = stageY - h * 0.28f + (float) Math.abs(Math.sin(t * 5.8)) * 0.45f;

        for (int wv = 0; wv < 2; wv++) {
            float wf = (float) ((t * 1.7 + wv * 0.5) % 1.0);
            float wr = headR * (1.3f + wf * 2.8f);
            drawEllipseArc(vc, mat, light, g, cx, headY, wr, wr * 0.82f, 0.5f, 0, Math.PI * 2.0, lowDetail ? 8 : 12, 0x00FFFF, (int) (205 * (1.0f - wf)));
        }

        drawFilledCircle(vc, mat, light, g, cx, stageY - h * 0.03f, headR * 1.7f, h * 0.045f, 5, 0xFF00AA, 190);
        q(vc, mat, light, g, cx - headR * 0.8f, headY + headR * 0.85f, cx + headR * 0.8f, stageY, 0x0C0818, 255);
        drawFilledCircle(vc, mat, light, g, cx, headY, headR, headR, 5, 0xFFFFFF, 255);
        q(vc, mat, light, g, cx + headR * 1.05f, headY + headR * 0.15f, cx + headR * 1.4f, stageY, 0xC0CCD8, 245);
        drawFilledCircle(vc, mat, light, g, cx + headR * 1.25f, headY + headR * 0.08f, headR * 0.35f, headR * 0.35f, 4, 0x00E5FF, 255);

        double[] turnTimes = { 1.05, 1.85, 2.55 };
        float chairW = w * 0.16f;
        float chairH = h * 0.22f;
        float chairBot = y + h * 0.80f;

        for (int j = 0; j < 3; j++) {
            float jx = x + w * (0.23f + j * 0.27f);
            boolean turned = cycle >= turnTimes[j];
            double dt = turned ? (cycle - turnTimes[j]) : 0.0;
            float spinScale = (turned && dt < 0.35) ? (float) Math.max(0.22, Math.abs(Math.cos((dt / 0.35) * Math.PI))) : 1.0f;
            float curW = chairW * spinScale;

            int floorCol = turned ? 0xFFFFFF : 0x281838;
            q(vc, mat, light, g, jx - chairW * 0.58f, chairBot - 0.9f, jx + chairW * 0.58f, chairBot, floorCol, 255);

            float cTop = chairBot - chairH;
            int backCol = turned ? 0xE61E2A : 0x98121C;
            int rimCol = turned ? 0xFFD700 : 0x551822;
            q(vc, mat, light, g, jx - curW * 0.5f, cTop, jx + curW * 0.5f, chairBot - 0.9f, backCol, 255);
            drawOutlineBox(vc, mat, light, g, jx - curW * 0.5f, cTop, curW, chairH - 0.9f, 0.45f, rimCol, 255);

            if (turned && dt >= 0.18) {
                float jr = Math.min(curW * 0.32f, chairH * 0.24f);
                drawFilledCircle(vc, mat, light, g, jx, cTop + chairH * 0.38f, jr, jr, 4, 0xFFE0C2, 255);
                drawFilledCircle(vc, mat, light, g, jx, chairBot - 1.5f, jr * 0.65f, jr * 0.45f, 4, 0xFF2233, 255);
            } else {
                q(vc, mat, light, g, jx - curW * 0.24f, cTop + chairH * 0.25f, jx + curW * 0.24f, cTop + chairH * 0.62f, 0xFFFFFF, 180);
            }
        }

        int fans = Math.max(6, (int) (w / 3.6f));
        float fanStep = w / fans;
        for (int f = 0; f < fans; f++) {
            float fx = x + (f + 0.5f) * fanStep;
            float bounce = (float) Math.abs(Math.sin(t * 7.5 + f * 1.3)) * (h * 0.055f);
            float fy = y + h - h * 0.06f - bounce;
            float fr = Math.max(0.95f, fanStep * 0.42f);
            drawFilledCircle(vc, mat, light, g, fx, fy, fr, fr, 4, 0x05020A, 255);
            if (f % 2 == 0) {
                float stickX = fx + fr * 0.75f;
                float stickY = fy - fr * 1.65f;
                int sCol = (f % 4 == 0) ? 0xFF22AA : 0x00E5FF;
                drawLine(vc, mat, light, g, fx + fr * 0.35f, fy - fr * 0.35f, stickX, stickY, 0.65f, sCol, 250);
            }
        }
    }

    private static void renderFormulaOne(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x080B12, 255);

        int rpmLeds = 12;
        float ledBarH = Math.max(1.4f, h * 0.095f);
        double throttlePhase = (t * 0.34) % 1.0;
        int activeLeds = 2 + (int) (throttlePhase * (rpmLeds - 1));
        float ledStep = (w - 2.0f) / rpmLeds;
        for (int i = 0; i < rpmLeds; i++) {
            float lx = x + 1.0f + i * ledStep;
            boolean lit = i < activeLeds;
            if (activeLeds >= rpmLeds && ((int) (t * 14.0) % 2 == 0)) {
                lit = true;
            }
            int lCol = (i < 4) ? 0x00FF66 : ((i < 8) ? 0xFF2233 : 0xAA33FF);
            if (!lit) lCol = blendColor(lCol, 0x080B12, 0.82f);
            q(vc, mat, light, g, lx, y + 0.8f, lx + ledStep * 0.78f, y + 0.8f + ledBarH, lCol, 255);
        }

        float bodyTop = y + ledBarH + 1.6f;
        float bodyH = (y + h) - bodyTop;
        float trackAreaW = w * 0.58f;

        float[][] pts = {
                { 0.16f, 0.78f },
                { 0.14f, 0.26f },
                { 0.42f, 0.16f },
                { 0.56f, 0.42f },
                { 0.84f, 0.28f },
                { 0.86f, 0.78f },
                { 0.50f, 0.84f }
        };

        float trackThick = Math.max(1.7f, Math.min(trackAreaW, bodyH) * 0.12f);
        for (int i = 0; i < pts.length; i++) {
            int nxt = (i + 1) % pts.length;
            float x1 = x + pts[i][0] * trackAreaW;
            float y1 = bodyTop + pts[i][1] * bodyH;
            float x2 = x + pts[nxt][0] * trackAreaW;
            float y2 = bodyTop + pts[nxt][1] * bodyH;
            drawLine(vc, mat, light, g, x1, y1, x2, y2, trackThick, 0x262E3C, 255);
            drawLine(vc, mat, light, g, x1, y1, x2, y2, trackThick * 0.45f, 0x425066, 255);
            q(vc, mat, light, g, x1 - 0.85f, y1 - 0.85f, x1 + 0.85f, y1 + 0.85f, (i % 2 == 0) ? 0xFF2233 : 0xFFFFFF, 235);
        }

        float sfX = x + (pts[0][0] + pts[6][0]) * 0.5f * trackAreaW;
        float sfY = bodyTop + (pts[0][1] + pts[6][1]) * 0.5f * bodyH;
        q(vc, mat, light, g, sfX - 0.45f, sfY - trackThick * 0.65f, sfX + 0.45f, sfY + trackThick * 0.65f, 0xFFFFFF, 245);

        int[] carCols = { 0xFF1E27, 0xFF8800, 0x00E5FF };
        double[] speeds = { 0.38, 0.36, 0.34 };
        double[] offsets = { 0.0, -0.14, -0.29 };
        float carR = Math.max(1.05f, trackThick * 0.62f);

        for (int c = 2; c >= 0; c--) {
            double pos = ((t * speeds[c] + offsets[c]) % 1.0 + 1.0) % 1.0;
            double segPos = pos * pts.length;
            int idx = (int) Math.floor(segPos) % pts.length;
            int nxt = (idx + 1) % pts.length;
            float frac = (float) (segPos - Math.floor(segPos));

            float cx = x + (pts[idx][0] + (pts[nxt][0] - pts[idx][0]) * frac) * trackAreaW;
            float cy = bodyTop + (pts[idx][1] + (pts[nxt][1] - pts[idx][1]) * frac) * bodyH;
            int col = (c == 0 && rainbow) ? getRainbowColor(t * 0.35) : carCols[c];
            drawFilledCircle(vc, mat, light, g, cx, cy, carR * 1.25f, carR * 1.25f, 4, col, 120);
            drawFilledCircle(vc, mat, light, g, cx, cy, carR, carR, 5, col, 255);
            q(vc, mat, light, g, cx - carR * 0.35f, cy - carR * 0.35f, cx + carR * 0.35f, cy + carR * 0.35f, 0xFFFFFF, 240);
        }

        float hudX = x + trackAreaW + 1.0f;
        float hudW = (x + w) - hudX - 1.0f;
        q(vc, mat, light, g, hudX, bodyTop + 0.6f, hudX + hudW, y + h - 0.8f, 0x101622, 245);
        drawOutlineBox(vc, mat, light, g, hudX, bodyTop + 0.6f, hudW, bodyH - 1.4f, 0.5f, 0x28384E, 235);

        int gear = 2 + (int) (throttlePhase * 6.9);
        boolean drsOn = gear >= 7;
        q(vc, mat, light, g, hudX + 1.0f, bodyTop + 1.4f, hudX + hudW * 0.52f, bodyTop + 1.4f + Math.max(1.2f, bodyH * 0.12f), drsOn ? 0x00FF66 : 0x1E2836, 250);

        float dw = Math.max(1.8f, Math.min(hudW * 0.46f, bodyH * 0.36f));
        float dh = dw * 1.45f;
        float gx = hudX + (hudW - dw) * 0.26f;
        float gy = bodyTop + (bodyH - dh) * 0.56f;
        int gearCol = (gear >= 7) ? 0x00E5FF : 0xFFEE00;
        draw7SegmentDigit(vc, mat, light, g, gx, gy, dw, dh, gear, gearCol, 0x182230);

        float barW = Math.max(1.1f, hudW * 0.14f);
        float barBot = y + h - 1.6f;
        float barMaxH = bodyH * 0.72f;
        float thrH = barMaxH * (0.25f + 0.75f * (float) throttlePhase);
        float brkH = (throttlePhase < 0.18) ? (barMaxH * 0.85f) : (barMaxH * 0.10f);

        float tx = hudX + hudW - barW * 2.5f - 0.8f;
        float bx = hudX + hudW - barW * 1.1f - 0.4f;
        q(vc, mat, light, g, tx, barBot - thrH, tx + barW, barBot, 0x00FF66, 250);
        q(vc, mat, light, g, bx, barBot - brkH, bx + barW, barBot, 0xFF2233, 250);
    }

    private static void renderAirportBoard(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x0A0E16, 255);

        float headH = Math.max(2.6f, h * 0.20f);
        int headerCol = rainbow ? getRainbowColor(t * 0.25) : 0xFFCC00;
        q(vc, mat, light, g, x, y, x + w, y + headH, headerCol, 255);

        float px = x + headH * 0.85f;
        float py = y + headH * 0.50f;
        float ps = headH * 0.38f;
        drawLine(vc, mat, light, g, px - ps, py + ps * 0.25f, px + ps, py - ps * 0.25f, Math.max(0.75f, ps * 0.45f), 0x0A0E16, 255);
        drawLine(vc, mat, light, g, px - ps * 0.15f, py - ps * 0.85f, px + ps * 0.15f, py + ps * 0.85f, Math.max(0.65f, ps * 0.38f), 0x0A0E16, 255);
        q(vc, mat, light, g, px + ps * 1.6f, y + headH * 0.28f, x + w * 0.55f, y + headH * 0.72f, 0x0A0E16, 235);
        q(vc, mat, light, g, x + w * 0.62f, y + headH * 0.28f, x + w - 1.2f, y + headH * 0.72f, 0x0A0E16, 200);

        float runwayH = Math.max(1.8f, h * 0.13f);
        float runwayY = y + h - runwayH;
        q(vc, mat, light, g, x, runwayY, x + w, y + h, 0x141A24, 255);
        for (float rx = x + 1.0f; rx < x + w - 1.5f; rx += 3.2f) {
            q(vc, mat, light, g, rx, runwayY + runwayH * 0.45f, rx + 1.5f, runwayY + runwayH * 0.58f, 0xFFEE88, 190);
        }
        float planeProg = (float) ((t * 0.38) % 1.0);
        float rpx = x + 1.5f + planeProg * (w - 3.5f);
        float rpy = runwayY + runwayH * 0.5f - (planeProg > 0.55f ? ((planeProg - 0.55f) * runwayH * 1.4f) : 0.0f);
        drawLine(vc, mat, light, g, rpx - 1.4f, rpy, rpx + 1.4f, rpy - 0.4f, 0.75f, 0xFFFFFF, 255);
        drawFilledCircle(vc, mat, light, g, rpx - 1.6f, rpy + 0.1f, 0.6f, 0.5f, 4, 0x00E5FF, 220);

        int rows = 3;
        int cols = lowDetail ? 7 : 10;
        float bodyY = y + headH + 0.6f;
        float bodyH = runwayY - bodyY - 0.4f;
        float rowStep = bodyH / rows;
        float rowH = rowStep * 0.82f;

        float logoW = Math.max(2.0f, w * 0.09f);
        float statusW = Math.max(3.8f, w * 0.22f);
        float flapsX = x + logoW + 1.6f;
        float flapsW = w - logoW - statusW - 3.2f;
        float flapStepX = flapsW / cols;
        float flapW = flapStepX * 0.84f;

        int[] airlineCols = { 0xE01B24, 0x00B8FF, 0xFF9900 };
        double wavePos = (t * 1.65) % (rows + 1.5);

        for (int r = 0; r < rows; r++) {
            float ry = bodyY + r * rowStep;
            q(vc, mat, light, g, x + 0.8f, ry + rowH * 0.12f, x + 0.8f + logoW, ry + rowH * 0.88f, airlineCols[r % airlineCols.length], 255);
            q(vc, mat, light, g, x + 0.8f + logoW * 0.25f, ry + rowH * 0.30f, x + 0.8f + logoW * 0.75f, ry + rowH * 0.70f, 0xFFFFFF, 240);

            double rowWave = (wavePos - r) * cols;
            for (int c = 0; c < cols; c++) {
                float fx = flapsX + c * flapStepX;
                double dist = Math.abs(rowWave - c);
                boolean flipping = dist < 1.25;
                float fold = flipping ? (float) Math.max(0.18, Math.abs(Math.cos(dist * Math.PI))) : 1.0f;
                float fh = rowH * fold;
                float fy = ry + (rowH - fh) * 0.5f;

                q(vc, mat, light, g, fx, fy, fx + flapW, fy + fh, flipping ? 0x344054 : 0x1A2230, 255);
                int charCol = flipping ? 0xFFFFFF : ((c < 2) ? 0xFFCC00 : ((c >= cols - 2) ? 0x00E5FF : 0xF0F4F8));
                if (((r * 7 + c * 3 + (int) (t * 0.45)) % 5) != 0) {
                    q(vc, mat, light, g, fx + flapW * 0.18f, fy + fh * 0.24f, fx + flapW * 0.82f, fy + fh * 0.76f, charCol, 245);
                }
                q(vc, mat, light, g, fx, ry + rowH * 0.48f, fx + flapW, ry + rowH * 0.54f, 0x080A0E, 255);
            }

            float sx = x + w - statusW - 0.8f;
            int stCol = (r == 0) ? 0x00FF66 : ((r == 1) ? ((((int) (t * 5.0) % 2) == 0) ? 0xFFAA00 : 0x553800) : 0xFF2244);
            q(vc, mat, light, g, sx, ry + rowH * 0.12f, sx + statusW, ry + rowH * 0.88f, stCol, 250);
            q(vc, mat, light, g, sx + statusW * 0.15f, ry + rowH * 0.36f, sx + statusW * 0.85f, ry + rowH * 0.64f, 0x0A0E16, 210);
        }
    }

    private static void renderFastFoodBillboard(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                                float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x140618, 255);

        int neonRim = rainbow ? getRainbowColor(t * 0.35) : (baseColor == 0x00FF66 ? 0xFF2288 : baseColor);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.6f, Math.min(w, h) * 0.035f), neonRim, 245);

        float bounce = (float) Math.max(0.0, Math.sin(t * 4.2)) * (h * 0.055f);
        float burgCx = x + w * 0.34f;
        float burgBaseY = y + h * 0.84f;
        float burgW = Math.min(w * 0.46f, h * 0.82f);
        float halfB = burgW * 0.5f;

        float bunBotH = h * 0.11f;
        q(vc, mat, light, g, burgCx - halfB * 0.92f, burgBaseY - bunBotH, burgCx + halfB * 0.92f, burgBaseY, 0xD88A38, 255);

        float pattyY = burgBaseY - bunBotH - bounce * 0.25f;
        float pattyH = h * 0.11f;
        drawFilledCircle(vc, mat, light, g, burgCx, pattyY - pattyH * 0.5f, halfB * 1.02f, pattyH * 0.65f, 5, 0x5A2614, 255);

        float cheeseY = pattyY - pattyH - bounce * 0.45f;
        q(vc, mat, light, g, burgCx - halfB * 0.98f, cheeseY - h * 0.045f, burgCx + halfB * 0.98f, cheeseY + h * 0.02f, 0xFFCC00, 255);
        q(vc, mat, light, g, burgCx - halfB * 0.45f, cheeseY, burgCx - halfB * 0.15f, cheeseY + h * 0.07f, 0xFFCC00, 255);
        q(vc, mat, light, g, burgCx + halfB * 0.25f, cheeseY, burgCx + halfB * 0.55f, cheeseY + h * 0.06f, 0xFFCC00, 255);

        float tomY = cheeseY - h * 0.045f - bounce * 0.65f;
        float tomH = h * 0.065f;
        q(vc, mat, light, g, burgCx - halfB * 0.86f, tomY - tomH, burgCx + halfB * 0.86f, tomY, 0xFF2233, 255);

        float letY = tomY - tomH - bounce * 0.80f;
        for (int l = -2; l <= 2; l++) {
            drawFilledCircle(vc, mat, light, g, burgCx + l * (halfB * 0.38f), letY - h * 0.02f, halfB * 0.28f, h * 0.045f, 4, 0x33DD44, 255);
        }

        float topBunY = letY - h * 0.03f - bounce;
        float topBunH = h * 0.22f;
        drawFilledCircle(vc, mat, light, g, burgCx, topBunY - topBunH * 0.35f, halfB * 0.96f, topBunH * 0.75f, 7, 0xE59842, 255);
        q(vc, mat, light, g, burgCx - halfB * 0.96f, topBunY - topBunH * 0.35f, burgCx + halfB * 0.96f, topBunY, 0xE59842, 255);

        float[][] seeds = BURGER_SEEDS;
        for (float[] sd : seeds) {
            float sx = burgCx + sd[0] * halfB;
            float sy = topBunY + sd[1] * topBunH;
            q(vc, mat, light, g, sx - 0.45f, sy - 0.35f, sx + 0.45f, sy + 0.35f, 0xFFF4D0, 245);
        }

        float cupCx = x + w * 0.77f;
        float cupBot = y + h * 0.84f;
        float cupTop = y + h * 0.38f;
        float cupW = w * 0.24f;

        float strawTopY = y + h * 0.16f;
        drawLine(vc, mat, light, g, cupCx, cupTop, cupCx + cupW * 0.28f, strawTopY, Math.max(0.75f, cupW * 0.14f), 0xFFFFFF, 255);
        drawLine(vc, mat, light, g, cupCx + cupW * 0.28f, strawTopY, cupCx + cupW * 0.52f, strawTopY + h * 0.04f, Math.max(0.75f, cupW * 0.14f), 0xFF2233, 255);

        int cupSlices = 8;
        float cupSliceH = (cupBot - cupTop) / cupSlices;
        for (int s = 0; s < cupSlices; s++) {
            float f = (float) s / cupSlices;
            float hw = (cupW * 0.52f) * (1.0f - f * 0.22f);
            float cy1 = cupTop + s * cupSliceH;
            float cy2 = cy1 + cupSliceH + 0.15f;
            int cCol = (s == 2 || s == 3) ? 0xFFFFFF : 0xE01B24;
            q(vc, mat, light, g, cupCx - hw, cy1, cupCx + hw, cy2, cCol, 255);
        }
        q(vc, mat, light, g, cupCx - cupW * 0.58f, cupTop - h * 0.05f, cupCx + cupW * 0.58f, cupTop + h * 0.02f, 0xF0F4F8, 255);

        for (int sp = 0; sp < 3; sp++) {
            if (((int) (t * 6.0 + sp) % 2) == 0) {
                float sx = x + w * (0.12f + sp * 0.38f);
                float sy = y + h * (0.18f + (sp % 2) * 0.08f);
                q(vc, mat, light, g, sx - 1.1f, sy - 0.3f, sx + 1.1f, sy + 0.3f, 0xFFEE44, 250);
                q(vc, mat, light, g, sx - 0.3f, sy - 1.1f, sx + 0.3f, sy + 1.1f, 0xFFEE44, 250);
            }
        }
    }

    private static void renderGasStation(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x060C14, 255);

        int themeCol = rainbow ? getRainbowColor(t * 0.3) : baseColor;
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.036f), blendColor(themeCol, 0x243B54, 0.45f), 255);

        float padX = 1.0f;
        float boardW = w - padX * 2.0f;
        float headH = Math.max(2.4f, h * 0.17f);
        float stripeH = Math.max(0.8f, headH * 0.22f);
        q(vc, mat, light, g, x + padX, y + 0.8f, x + padX + boardW, y + 0.8f + headH, 0xD81E28, 255);
        q(vc, mat, light, g, x + padX, y + 0.8f + headH - stripeH, x + padX + boardW, y + 0.8f + headH, 0xFFCC00, 255);

        float dropCx = x + padX + headH * 0.75f;
        float dropCy = y + 0.8f + (headH - stripeH) * 0.52f;
        drawFilledCircle(vc, mat, light, g, dropCx, dropCy, headH * 0.28f, headH * 0.30f, 4, 0xFFDD44, 255);
        q(vc, mat, light, g, dropCx + headH * 0.55f, y + 0.8f + headH * 0.22f, x + padX + boardW - 1.5f, y + 0.8f + headH * 0.62f, 0xFFFFFF, 255);

        int[] badgeCols = GAS_BADGE_COLS;
        int[] ledCols = GAS_LED_COLS;
        int[][] basePrices = GAS_BASE_PRICES;

        float rowsTop = y + headH + 1.5f;
        float rowsH = (y + h - 1.0f) - rowsTop;
        float rowStep = rowsH / 3.0f;
        float rowH = rowStep * 0.85f;

        int activeRow = ((int) (t * 0.45)) % 3;

        for (int r = 0; r < 3; r++) {
            float ry = rowsTop + r * rowStep;
            float badgeW = Math.max(3.2f, boardW * 0.24f);
            q(vc, mat, light, g, x + padX, ry, x + padX + badgeW, ry + rowH, badgeCols[r], 255);
            q(vc, mat, light, g, x + padX + badgeW * 0.18f, ry + rowH * 0.30f, x + padX + badgeW * 0.82f, ry + rowH * 0.70f, 0x060C14, 255);

            float lcdX = x + padX + badgeW + 0.8f;
            float lcdW = (x + padX + boardW) - lcdX;
            q(vc, mat, light, g, lcdX, ry, lcdX + lcdW, ry + rowH, 0x03070C, 255);
            int rimC = (r == activeRow) ? badgeCols[r] : blendColor(badgeCols[r], 0x182636, 0.65f);
            drawOutlineBox(vc, mat, light, g, lcdX, ry, lcdW, rowH, Math.max(0.75f, rowH * 0.10f), rimC, 255);

            float dw = Math.max(1.2f, Math.min(lcdW * 0.17f, rowH * 0.56f));
            float dh = Math.min(rowH * 0.78f, dw * 1.42f);
            float dy = ry + (rowH - dh) * 0.5f;
            float totalDigitsW = dw * 4.95f;
            float startDx = lcdX + (lcdW - totalDigitsW) * 0.5f;

            int d0 = basePrices[r][0];
            int d1 = basePrices[r][1];
            int d2 = basePrices[r][2];
            int d3 = basePrices[r][3];
            int pCol = (r == activeRow) ? blendColor(ledCols[r], 0xFFFFFF, 0.25f) : ledCols[r];
            int dimCol = blendColor(pCol, 0x000000, 0.85f);
            float dotSz = Math.max(0.85f, dw * 0.28f);

            draw7SegmentDigit(vc, mat, light, g, startDx, dy, dw, dh, d0, pCol, dimCol);
            draw7SegmentDigit(vc, mat, light, g, startDx + dw * 1.22f, dy, dw, dh, d1, pCol, dimCol);
            q(vc, mat, light, g, startDx + dw * 2.32f, dy + dh - dotSz, startDx + dw * 2.32f + dotSz, dy + dh, 0xFFFFFF, 255);
            draw7SegmentDigit(vc, mat, light, g, startDx + dw * 2.65f, dy, dw, dh, d2, pCol, dimCol);
            draw7SegmentDigit(vc, mat, light, g, startDx + dw * 3.87f, dy, dw, dh, d3, 0xFFFFFF, dimCol);
        }
    }

    private static void renderSmartParking(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x060C16, 255);

        int rimCol = rainbow ? getRainbowColor(t * 0.3) : 0x0088FF;
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.035f), rimCol, 255);

        float padX = 1.0f;
        float boardW = w - padX * 2.0f;
        float headH = Math.max(2.6f, h * 0.20f);
        q(vc, mat, light, g, x + padX, y + 0.8f, x + padX + boardW, y + 0.8f + headH, 0x0055CC, 255);

        float pBoxX = x + padX + 0.8f;
        float pBoxY = y + 1.2f;
        float pBoxH = headH - 0.8f;
        float pBoxW = pBoxH * 0.85f;
        q(vc, mat, light, g, pBoxX, pBoxY, pBoxX + pBoxW * 0.35f, pBoxY + pBoxH, 0xFFFFFF, 255);
        q(vc, mat, light, g, pBoxX, pBoxY, pBoxX + pBoxW, pBoxY + pBoxH * 0.28f, 0xFFFFFF, 255);
        q(vc, mat, light, g, pBoxX, pBoxY + pBoxH * 0.48f, pBoxX + pBoxW, pBoxY + pBoxH * 0.72f, 0xFFFFFF, 255);
        q(vc, mat, light, g, pBoxX + pBoxW * 0.68f, pBoxY, pBoxX + pBoxW, pBoxY + pBoxH * 0.72f, 0xFFFFFF, 255);

        float glide = (float) Math.sin(t * 2.2) * (boardW * 0.025f);
        int activeChev = ((int) (t * 2.4)) % 3;
        float chevStart = x + padX + boardW * 0.46f + glide;
        float chevStep = boardW * 0.14f;
        float ay = y + 0.8f + headH * 0.50f;
        float asz = headH * 0.28f;
        for (int c = 0; c < 3; c++) {
            float cx = chevStart + c * chevStep;
            int cCol = (c == activeChev) ? 0x88FFBB : 0x00FF66;
            drawLine(vc, mat, light, g, cx - asz * 0.55f, ay - asz * 0.80f, cx + asz * 0.45f, ay, Math.max(0.75f, asz * 0.42f), cCol, 255);
            drawLine(vc, mat, light, g, cx - asz * 0.55f, ay + asz * 0.80f, cx + asz * 0.45f, ay, Math.max(0.75f, asz * 0.42f), cCol, 255);
        }

        float rowsTop = y + headH + 1.5f;
        float rowsH = (y + h - 1.0f) - rowsTop;
        float rowStep = rowsH / 3.0f;
        float rowH = rowStep * 0.84f;

        int[] counts = { 48, 0, 14 };
        int[] rowCols = { 0x00FF66, 0xFF2233, 0x00E5FF };

        for (int r = 0; r < 3; r++) {
            float ry = rowsTop + r * rowStep;
            float badgeW = Math.max(3.4f, boardW * 0.26f);
            int bCol = (r == 2) ? 0xD89614 : 0x1A2C44;
            q(vc, mat, light, g, x + padX, ry, x + padX + badgeW, ry + rowH, bCol, 255);
            q(vc, mat, light, g, x + padX + badgeW * 0.20f, ry + rowH * 0.30f, x + padX + badgeW * 0.80f, ry + rowH * 0.70f, 0xFFFFFF, 255);

            float stX = x + padX + badgeW + 0.8f;
            float stW = boardW * 0.26f;
            q(vc, mat, light, g, stX, ry, stX + stW, ry + rowH, 0x0C1624, 255);
            boolean blinkOn = (r != 1) || (((int) (t * 3.0) % 2) == 0);
            int stCol = blinkOn ? rowCols[r] : 0x441018;
            float dotR = Math.max(0.8f, rowH * 0.26f);
            drawFilledCircle(vc, mat, light, g, stX + stW * 0.5f, ry + rowH * 0.5f, dotR, dotR, 4, stCol, 255);

            float lcdX = stX + stW + 0.8f;
            float lcdW = (x + padX + boardW) - lcdX;
            q(vc, mat, light, g, lcdX, ry, lcdX + lcdW, ry + rowH, 0x03060C, 255);
            drawOutlineBox(vc, mat, light, g, lcdX, ry, lcdW, rowH, Math.max(0.7f, rowH * 0.10f), rowCols[r], 255);

            float dw = Math.max(1.2f, Math.min(lcdW * 0.34f, rowH * 0.55f));
            float dh = Math.min(rowH * 0.78f, dw * 1.42f);
            float dy = ry + (rowH - dh) * 0.5f;
            float startDx = lcdX + (lcdW - dw * 2.3f) * 0.5f;
            int val = counts[r];
            int dimCol = blendColor(rowCols[r], 0x000000, 0.85f);
            draw7SegmentDigit(vc, mat, light, g, startDx, dy, dw, dh, (val / 10) % 10, rowCols[r], dimCol);
            draw7SegmentDigit(vc, mat, light, g, startDx + dw * 1.3f, dy, dw, dh, val % 10, rowCols[r], dimCol);
        }
    }

    private static void renderCurrencyExchange(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                               float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x05080E, 255);

        int rimCol = rainbow ? getRainbowColor(t * 0.3) : 0xFFB800;
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.035f), rimCol, 255);

        float padX = 1.0f;
        float boardW = w - padX * 2.0f;
        float headH = Math.max(2.2f, h * 0.16f);
        q(vc, mat, light, g, x + padX, y + 0.8f, x + padX + boardW, y + 0.8f + headH, 0x162234, 255);
        q(vc, mat, light, g, x + padX + boardW * 0.28f, y + 0.8f + headH * 0.28f, x + padX + boardW * 0.54f, y + 0.8f + headH * 0.72f, 0xFFAA00, 255);
        q(vc, mat, light, g, x + padX + boardW * 0.60f, y + 0.8f + headH * 0.28f, x + padX + boardW * 0.86f, y + 0.8f + headH * 0.72f, 0x00FF66, 255);

        float rowsTop = y + headH + 1.4f;
        float rowsH = (y + h - 1.0f) - rowsTop;
        float rowStep = rowsH / 3.0f;
        float rowH = rowStep * 0.85f;

        int[] badgeCols = { 0x00AA44, 0x0066DD, 0xFFD700 };
        int[][] buyRates = { { 3, 4 }, { 3, 7 }, { 2, 9 } };
        int[][] sellRates = { { 3, 5 }, { 3, 8 }, { 3, 0 } };
        boolean[] upTrend = { true, false, true };

        for (int r = 0; r < 3; r++) {
            float ry = rowsTop + r * rowStep;
            float badgeW = Math.max(3.0f, boardW * 0.20f);
            q(vc, mat, light, g, x + padX, ry, x + padX + badgeW, ry + rowH, badgeCols[r], 255);
            int symCol = (r == 2) ? 0x221600 : 0xFFFFFF;
            q(vc, mat, light, g, x + padX + badgeW * 0.22f, ry + rowH * 0.28f, x + padX + badgeW * 0.78f, ry + rowH * 0.72f, symCol, 255);

            float rateAreaX = x + padX + badgeW + 0.6f;
            float arrowW = Math.max(2.2f, boardW * 0.14f);
            float rateAreaW = (x + padX + boardW - arrowW - 0.6f) - rateAreaX;
            q(vc, mat, light, g, rateAreaX, ry, rateAreaX + rateAreaW, ry + rowH, 0x080E18, 255);

            float dw = Math.max(1.1f, Math.min(rateAreaW * 0.18f, rowH * 0.56f));
            float dh = Math.min(rowH * 0.78f, dw * 1.42f);
            float dy = ry + (rowH - dh) * 0.5f;

            float buyX = rateAreaX + rateAreaW * 0.06f;
            draw7SegmentDigit(vc, mat, light, g, buyX, dy, dw, dh, buyRates[r][0], 0xFFAA00, 0x221804);
            draw7SegmentDigit(vc, mat, light, g, buyX + dw * 1.25f, dy, dw, dh, buyRates[r][1], 0xFFAA00, 0x221804);

            float sellX = rateAreaX + rateAreaW * 0.54f;
            draw7SegmentDigit(vc, mat, light, g, sellX, dy, dw, dh, sellRates[r][0], 0x00FF66, 0x042210);
            draw7SegmentDigit(vc, mat, light, g, sellX + dw * 1.25f, dy, dw, dh, sellRates[r][1], 0x00FF66, 0x042210);

            float ax = x + padX + boardW - arrowW * 0.5f;
            float ay = ry + rowH * 0.5f + (float) Math.sin(t * 4.5 + r) * 0.35f;
            float ar = Math.min(arrowW, rowH) * 0.32f;
            if (upTrend[r]) {
                drawLine(vc, mat, light, g, ax - ar, ay + ar * 0.6f, ax, ay - ar * 0.7f, 0.75f, 0x00FF66, 255);
                drawLine(vc, mat, light, g, ax + ar, ay + ar * 0.6f, ax, ay - ar * 0.7f, 0.75f, 0x00FF66, 255);
            } else {
                drawLine(vc, mat, light, g, ax - ar, ay - ar * 0.6f, ax, ay + ar * 0.7f, 0.75f, 0xFF2233, 255);
                drawLine(vc, mat, light, g, ax + ar, ay - ar * 0.6f, ax, ay + ar * 0.7f, 0.75f, 0xFF2233, 255);
            }
        }
    }

    private static void renderCoffeeShop(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x120906, 255);

        int neonCol = rainbow ? getRainbowColor(t * 0.3) : (baseColor == 0x00FF66 ? 0xFF8822 : baseColor);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.038f), neonCol, 255);

        float cupCx = x + w * 0.42f;
        float saucerY = y + h * 0.82f;
        float cupW = Math.min(w * 0.44f, h * 0.62f);
        float cupH = h * 0.38f;
        float cupTop = saucerY - cupH;

        drawEllipseArc(vc, mat, light, g, cupCx + cupW * 0.48f, cupTop + cupH * 0.46f, cupW * 0.22f, cupH * 0.28f, Math.max(0.9f, cupW * 0.11f), -Math.PI * 0.5, Math.PI * 0.5, 8, 0xFFF2E2, 255);

        drawFilledCircle(vc, mat, light, g, cupCx, cupTop + cupH * 0.45f, cupW * 0.50f, cupH * 0.55f, 8, 0xFFF2E2, 255);
        q(vc, mat, light, g, cupCx - cupW * 0.50f, cupTop, cupCx + cupW * 0.50f, cupTop + cupH * 0.50f, 0xFFF2E2, 255);
        q(vc, mat, light, g, cupCx - cupW * 0.48f, cupTop + cupH * 0.28f, cupCx + cupW * 0.48f, cupTop + cupH * 0.54f, 0xC86428, 255);
        drawFilledCircle(vc, mat, light, g, cupCx, cupTop + 0.4f, cupW * 0.44f, Math.max(0.9f, cupH * 0.14f), 5, 0x3A1A08, 255);

        q(vc, mat, light, g, cupCx - cupW * 0.68f, saucerY - h * 0.05f, cupCx + cupW * 0.68f, saucerY + h * 0.03f, 0xE6D4C0, 255);

        for (int s = -1; s <= 1; s++) {
            float sxBase = cupCx + s * (cupW * 0.24f);
            int segs = 5;
            float steamH = (cupTop - y - 2.2f);
            float segStep = steamH / segs;
            for (int k = 0; k < segs; k++) {
                float f1 = (float) k / segs;
                float f2 = (float) (k + 1) / segs;
                float sy1 = cupTop - 1.0f - f1 * steamH;
                float sy2 = cupTop - 1.0f - f2 * steamH;
                float sw1 = sxBase + (float) Math.sin(t * 3.8 + s * 1.4 + f1 * 4.5) * (cupW * 0.11f);
                float sw2 = sxBase + (float) Math.sin(t * 3.8 + s * 1.4 + f2 * 4.5) * (cupW * 0.11f);
                drawLine(vc, mat, light, g, sw1, sy1, sw2, sy2, Math.max(0.75f, cupW * 0.08f), 0xFFE6C8, 255);
            }
        }

        float beanCx = x + w * 0.80f;
        float beanCy = y + h * 0.56f + (float) Math.sin(t * 3.2) * 0.6f;
        float beanR = Math.min(w, h) * 0.14f;
        drawFilledCircle(vc, mat, light, g, beanCx, beanCy, beanR * 0.85f, beanR * 1.15f, 6, 0x6E3514, 255);
        drawLine(vc, mat, light, g, beanCx - beanR * 0.2f, beanCy - beanR * 0.85f, beanCx + beanR * 0.2f, beanCy + beanR * 0.85f, 0.7f, 0x240E04, 255);
    }

    private static void renderGymFitness(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                         float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x080A12, 255);

        int neonCol = rainbow ? getRainbowColor(t * 0.35) : (baseColor == 0x00FF66 ? 0xFF2255 : baseColor);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.036f), neonCol, 255);

        float liftPhase = 0.5f + 0.5f * (float) Math.sin(t * 3.8);
        float cx = x + w * 0.42f;
        float botY = y + h * 0.88f;
        float headR = Math.min(w, h) * 0.10f;
        float headY = botY - h * 0.36f;

        q(vc, mat, light, g, cx - headR * 1.35f, headY + headR * 0.9f, cx + headR * 1.35f, botY, 0x1E2C42, 255);
        q(vc, mat, light, g, cx - headR * 0.95f, headY + headR * 0.9f, cx + headR * 0.95f, botY, 0xFF2255, 255);
        drawFilledCircle(vc, mat, light, g, cx, headY, headR, headR, 5, 0xF2C29B, 255);
        q(vc, mat, light, g, cx - headR * 1.02f, headY - headR * 0.55f, cx + headR * 1.02f, headY - headR * 0.15f, 0x00E5FF, 255);

        float barLowY = headY + headR * 0.35f;
        float barHighY = y + h * 0.24f;
        float barY = barLowY - liftPhase * (barLowY - barHighY);
        float shY = headY + headR * 1.15f;
        float armThick = Math.max(1.0f, headR * 0.55f);

        drawLine(vc, mat, light, g, cx - headR * 1.2f, shY, cx - headR * 1.85f, (shY + barY) * 0.5f + headR * 0.4f, armThick, 0xF2C29B, 255);
        drawLine(vc, mat, light, g, cx - headR * 1.85f, (shY + barY) * 0.5f + headR * 0.4f, cx - headR * 1.55f, barY, armThick, 0xF2C29B, 255);
        drawLine(vc, mat, light, g, cx + headR * 1.2f, shY, cx + headR * 1.85f, (shY + barY) * 0.5f + headR * 0.4f, armThick, 0xF2C29B, 255);
        drawLine(vc, mat, light, g, cx + headR * 1.85f, (shY + barY) * 0.5f + headR * 0.4f, cx + headR * 1.55f, barY, armThick, 0xF2C29B, 255);

        float barHalfW = w * 0.34f;
        q(vc, mat, light, g, cx - barHalfW, barY - 0.55f, cx + barHalfW, barY + 0.55f, 0xD0DAE5, 255);

        float p1H = h * 0.28f;
        float p2H = h * 0.20f;
        float pw = Math.max(1.1f, w * 0.042f);
        for (int side = -1; side <= 1; side += 2) {
            float px1 = cx + side * (barHalfW * 0.72f);
            float px2 = cx + side * (barHalfW * 0.88f);
            q(vc, mat, light, g, px1 - pw * 0.5f, barY - p1H * 0.5f, px1 + pw * 0.5f, barY + p1H * 0.5f, 0xFF1E27, 255);
            q(vc, mat, light, g, px2 - pw * 0.5f, barY - p2H * 0.5f, px2 + pw * 0.5f, barY + p2H * 0.5f, 0x00E5FF, 255);
        }

        float mtrX = x + w * 0.82f;
        float mtrW = w * 0.11f;
        int segs = 6;
        float mtrTop = y + h * 0.18f;
        float mtrBot = y + h * 0.84f;
        float segStep = (mtrBot - mtrTop) / segs;
        int litSegs = 1 + Math.round(liftPhase * (segs - 1));
        for (int s = 0; s < segs; s++) {
            float sy = mtrBot - (s + 1) * segStep;
            boolean on = s < litSegs;
            int sCol = (s >= 4) ? 0xFF2255 : ((s >= 2) ? 0xFFCC00 : 0x00FF66);
            if (!on) sCol = 0x161E2A;
            q(vc, mat, light, g, mtrX, sy + 0.3f, mtrX + mtrW, sy + segStep - 0.3f, sCol, 255);
        }
    }

    private static void renderPizzaShop(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x140806, 255);

        float banH = Math.max(1.5f, h * 0.09f);
        q(vc, mat, light, g, x, y, x + w * 0.33f, y + banH, 0x00AA44, 255);
        q(vc, mat, light, g, x + w * 0.33f, y, x + w * 0.67f, y + banH, 0xFFFFFF, 255);
        q(vc, mat, light, g, x + w * 0.67f, y, x + w, y + banH, 0xEE2222, 255);

        int flames = lowDetail ? 7 : 11;
        float fw = w / flames;
        for (int f = 0; f < flames; f++) {
            float fx = x + f * fw;
            float fh = h * (0.08f + 0.05f * (float) Math.abs(Math.sin(t * 6.5 + f * 1.1)));
            q(vc, mat, light, g, fx, y + h - fh, fx + fw + 0.2f, y + h, (f % 2 == 0) ? 0xFF5500 : 0xFFAA00, 255);
        }

        float pCx = x + w * 0.42f;
        float pCy = y + h * 0.47f;
        float pR = Math.min(w, h) * 0.30f;

        drawFilledCircle(vc, mat, light, g, pCx, pCy, pR, pR, lowDetail ? 12 : 16, 0xD88A32, 255);
        drawFilledCircle(vc, mat, light, g, pCx, pCy, pR * 0.84f, pR * 0.84f, lowDetail ? 10 : 14, 0xD82218, 255);
        drawFilledCircle(vc, mat, light, g, pCx, pCy, pR * 0.76f, pR * 0.76f, lowDetail ? 10 : 14, 0xFFDD55, 255);

        float[][] peps = PIZZA_PEPS;
        float pepR = Math.max(0.85f, pR * 0.15f);
        for (float[] pp : peps) {
            drawFilledCircle(vc, mat, light, g, pCx + pp[0] * pR, pCy + pp[1] * pR, pepR, pepR, 4, 0xB81414, 255);
        }

        double a1 = -0.76;
        double a2 = -0.12;
        float dirX1 = (float) Math.cos(a1);
        float dirY1 = (float) Math.sin(a1);
        float dirX2 = (float) Math.cos(a2);
        float dirY2 = (float) Math.sin(a2);
        float midA = (float) ((a1 + a2) * 0.5);
        float pullDirX = (float) Math.cos(midA);
        float pullDirY = (float) Math.sin(midA);

        int wedgeSteps = 12;
        for (int s = 0; s <= wedgeSteps; s++) {
            float r = (pR * 1.08f) * ((float) s / wedgeSteps);
            drawLine(vc, mat, light, g, pCx + dirX1 * r, pCy + dirY1 * r, pCx + dirX2 * r, pCy + dirY2 * r, Math.max(0.95f, pR * 0.15f), 0x140806, 255);
        }

        float pull = (0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(t * 2.8))) * (pR * 0.50f);
        float crustOriginX = pCx + pullDirX * pull;
        float crustOriginY = pCy + pullDirY * pull;
        float sharpTipX = pCx + pullDirX * (pull * 0.42f);
        float sharpTipY = pCy + pullDirY * (pull * 0.42f);
        float c1X = crustOriginX + dirX1 * pR;
        float c1Y = crustOriginY + dirY1 * pR;
        float c2X = crustOriginX + dirX2 * pR;
        float c2Y = crustOriginY + dirY2 * pR;

        float in1X = (dirX1 * 0.72f + pullDirX * 0.28f);
        float in1Y = (dirY1 * 0.72f + pullDirY * 0.28f);
        float in2X = (dirX2 * 0.72f + pullDirX * 0.28f);
        float in2Y = (dirY2 * 0.72f + pullDirY * 0.28f);
        drawLine(vc, mat, light, g, pCx + in1X * (pR * 0.42f), pCy + in1Y * (pR * 0.42f),
                sharpTipX + (c1X - sharpTipX) * 0.44f, sharpTipY + (c1Y - sharpTipY) * 0.44f, 0.6f, 0xFFE877, 255);
        drawLine(vc, mat, light, g, pCx + in2X * (pR * 0.52f), pCy + in2Y * (pR * 0.52f),
                sharpTipX + (c2X - sharpTipX) * 0.54f, sharpTipY + (c2Y - sharpTipY) * 0.54f, 0.6f, 0xFFE877, 255);

        int sliceSteps = 14;
        for (int s = 1; s <= sliceSteps; s++) {
            float u = (float) s / sliceSteps;
            float sx1 = sharpTipX + (c1X - sharpTipX) * u;
            float sy1 = sharpTipY + (c1Y - sharpTipY) * u;
            float sx2 = sharpTipX + (c2X - sharpTipX) * u;
            float sy2 = sharpTipY + (c2Y - sharpTipY) * u;
            int sCol = (u > 0.84f) ? 0xD88A32 : ((u > 0.77f) ? 0xD82218 : 0xFFDD55);
            float lineT = Math.max(0.75f, pR * (0.08f + u * 0.10f));
            drawLine(vc, mat, light, g, sx1, sy1, sx2, sy2, lineT, sCol, 255);
        }
        drawFilledCircle(vc, mat, light, g, sharpTipX + (crustOriginX + pullDirX * pR - sharpTipX) * 0.56f,
                sharpTipY + (crustOriginY + pullDirY * pR - sharpTipY) * 0.56f, pepR * 0.9f, pepR * 0.9f, 4, 0xB81414, 255);
    }

    private static void renderIceCreamShop(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                           float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x12081A, 255);

        int neonRim = rainbow ? getRainbowColor(t * 0.35) : 0xFF55AA;
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.036f), neonRim, 255);

        float cx = x + w * 0.50f;
        float coneTopY = y + h * 0.54f;
        float coneBotY = y + h * 0.90f;
        float coneHalfW = Math.min(w * 0.22f, h * 0.26f);

        int coneSlices = 8;
        float cSliceH = (coneBotY - coneTopY) / coneSlices;
        for (int i = 0; i < coneSlices; i++) {
            float f = (float) i / coneSlices;
            float hw = coneHalfW * (1.0f - f * 0.82f);
            float y1 = coneTopY + i * cSliceH;
            float y2 = y1 + cSliceH + 0.15f;
            int wCol = (i % 2 == 0) ? 0xD89442 : 0xB87828;
            q(vc, mat, light, g, cx - hw, y1, cx + hw, y2, wCol, 255);
        }

        float bounce = (float) Math.abs(Math.sin(t * 3.8)) * (h * 0.035f);
        float scR = coneHalfW * 0.68f;
        drawFilledCircle(vc, mat, light, g, cx - coneHalfW * 0.48f, coneTopY - scR * 0.35f - bounce * 0.5f, scR, scR * 0.92f, 6, 0x55EE88, 255);
        drawFilledCircle(vc, mat, light, g, cx + coneHalfW * 0.48f, coneTopY - scR * 0.35f - bounce * 0.5f, scR, scR * 0.92f, 6, 0xFFF6D6, 255);

        float topScY = coneTopY - scR * 1.15f - bounce;
        drawFilledCircle(vc, mat, light, g, cx, topScY, scR * 1.08f, scR * 0.98f, 7, 0xFF66AA, 255);
        q(vc, mat, light, g, cx - scR * 0.65f, topScY - scR * 0.85f, cx + scR * 0.65f, topScY - scR * 0.25f, 0x4A2010, 255);
        q(vc, mat, light, g, cx - scR * 0.25f, topScY - scR * 0.30f, cx + scR * 0.15f, topScY + scR * 0.25f, 0x4A2010, 255);

        float chY = topScY - scR * 1.05f;
        float chR = Math.max(0.95f, scR * 0.38f);
        drawFilledCircle(vc, mat, light, g, cx, chY, chR, chR, 4, 0xFF1122, 255);
        drawLine(vc, mat, light, g, cx, chY - chR * 0.6f, cx + chR * 1.2f, chY - chR * 1.6f, 0.65f, 0x33CC44, 255);

        for (int sp = 0; sp < 4; sp++) {
            if (((int) (t * 5.0 + sp) % 2) == 0) {
                float sx = (sp < 2) ? (x + w * (0.16f + sp * 0.08f)) : (x + w * (0.76f + (sp - 2) * 0.08f));
                float sy = y + h * (0.24f + (sp % 2) * 0.32f);
                q(vc, mat, light, g, sx - 0.9f, sy - 0.3f, sx + 0.9f, sy + 0.3f, 0x00F0FF, 255);
                q(vc, mat, light, g, sx - 0.3f, sy - 0.9f, sx + 0.3f, sy + 0.9f, 0x00F0FF, 255);
            }
        }
    }

    private static void renderVetClinic(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x061418, 255);

        int medCol = rainbow ? getRainbowColor(t * 0.3) : 0x00E676;
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.036f), medCol, 255);

        float crCx = x + w * 0.21f;
        float crCy = y + h * 0.36f;
        float crArm = Math.min(w, h) * 0.18f;
        float crT = Math.max(1.2f, crArm * 0.38f);
        q(vc, mat, light, g, crCx - crArm, crCy - crT, crCx + crArm, crCy + crT, 0xFFFFFF, 255);
        q(vc, mat, light, g, crCx - crT, crCy - crArm, crCx + crT, crCy + crArm, 0xFFFFFF, 255);
        q(vc, mat, light, g, crCx - crArm * 0.82f, crCy - crT * 0.72f, crCx + crArm * 0.82f, crCy + crT * 0.72f, medCol, 255);
        q(vc, mat, light, g, crCx - crT * 0.72f, crCy - crArm * 0.82f, crCx + crT * 0.72f, crCy + crArm * 0.82f, medCol, 255);

        float hCx = x + w * 0.21f;
        float hCy = y + h * 0.74f + (float) Math.sin(t * 5.0) * 0.45f;
        float hR = Math.min(w, h) * 0.085f;
        drawFilledCircle(vc, mat, light, g, hCx - hR * 0.55f, hCy - hR * 0.25f, hR * 0.65f, hR * 0.65f, 4, 0xFF3366, 255);
        drawFilledCircle(vc, mat, light, g, hCx + hR * 0.55f, hCy - hR * 0.25f, hR * 0.65f, hR * 0.65f, 4, 0xFF3366, 255);
        drawFilledCircle(vc, mat, light, g, hCx, hCy + hR * 0.25f, hR * 0.85f, hR * 0.75f, 4, 0xFF3366, 255);

        float dogCx = x + w * 0.54f;
        float dogCy = y + h * 0.54f;
        float headR = Math.min(w, h) * 0.22f;

        float earDrop = (float) Math.sin(t * 4.5) * (headR * 0.08f);
        drawFilledCircle(vc, mat, light, g, dogCx - headR * 0.95f, dogCy - headR * 0.15f + earDrop, headR * 0.38f, headR * 0.68f, 5, 0x8A5228, 255);
        drawFilledCircle(vc, mat, light, g, dogCx + headR * 0.95f, dogCy - headR * 0.15f - earDrop, headR * 0.38f, headR * 0.68f, 5, 0x8A5228, 255);

        q(vc, mat, light, g, dogCx - headR * 0.75f, dogCy + headR * 0.78f, dogCx + headR * 0.75f, dogCy + headR * 1.12f, 0xE62233, 255);
        drawFilledCircle(vc, mat, light, g, dogCx, dogCy + headR * 1.15f, headR * 0.24f, headR * 0.24f, 4, 0xFFD700, 255);

        drawFilledCircle(vc, mat, light, g, dogCx, dogCy, headR, headR * 0.92f, 8, 0xE8B478, 255);
        drawFilledCircle(vc, mat, light, g, dogCx, dogCy + headR * 0.22f, headR * 0.58f, headR * 0.46f, 5, 0xFFF2DF, 255);

        float eyeR = Math.max(0.75f, headR * 0.16f);
        q(vc, mat, light, g, dogCx - headR * 0.48f - eyeR * 0.5f, dogCy - headR * 0.25f - eyeR * 0.5f,
                dogCx - headR * 0.48f + eyeR * 0.5f, dogCy - headR * 0.25f + eyeR * 0.5f, 0x101418, 255);
        q(vc, mat, light, g, dogCx + headR * 0.48f - eyeR * 0.5f, dogCy - headR * 0.25f - eyeR * 0.5f,
                dogCx + headR * 0.48f + eyeR * 0.5f, dogCy - headR * 0.25f + eyeR * 0.5f, 0x101418, 255);

        drawFilledCircle(vc, mat, light, g, dogCx, dogCy + headR * 0.10f, headR * 0.24f, headR * 0.18f, 4, 0x181210, 255);
        float tongueH = headR * (0.24f + 0.08f * (float) Math.abs(Math.sin(t * 6.0)));
        q(vc, mat, light, g, dogCx - headR * 0.16f, dogCy + headR * 0.42f, dogCx + headR * 0.16f, dogCy + headR * 0.42f + tongueH, 0xFF5577, 255);

        int activePaw = ((int) (t * 2.0)) % 2;
        float[][] pawCoords = VET_PAW_COORDS;
        float pawR = Math.min(w, h) * 0.085f;
        for (int p = 0; p < 2; p++) {
            float px = x + pawCoords[p][0] * w;
            float py = y + pawCoords[p][1] * h;
            int pCol = (p == activePaw) ? 0x00FFAA : 0x1A5244;
            drawFilledCircle(vc, mat, light, g, px, py + pawR * 0.25f, pawR, pawR * 0.82f, 4, pCol, 255);
            float toeR = Math.max(0.55f, pawR * 0.38f);
            float[][] toes = VET_TOES;
            for (float[] td : toes) {
                drawFilledCircle(vc, mat, light, g, px + td[0] * pawR, py + td[1] * pawR, toeR, toeR, 3, pCol, 255);
            }
        }
    }

    private static void renderCinemaBoxOffice(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                              float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x120614, 255);

        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.9f, Math.min(w, h) * 0.045f), 0x7A1828, 255);
        int bulbs = lowDetail ? 7 : 10;
        float bStep = (w - 2.0f) / bulbs;
        for (int b = 0; b < bulbs; b++) {
            boolean lit = (((int) (t * 6.0) + b) % 2) == 0;
            int bCol = lit ? 0xFFEE55 : 0x553310;
            float bx = x + 1.0f + (b + 0.5f) * bStep;
            q(vc, mat, light, g, bx - 0.55f, y + 0.3f, bx + 0.55f, y + 1.3f, bCol, 255);
            q(vc, mat, light, g, bx - 0.55f, y + h - 1.3f, bx + 0.55f, y + h - 0.3f, bCol, 255);
        }

        float buckCx = x + w * 0.29f;
        float buckTop = y + h * 0.44f;
        float buckBot = y + h * 0.86f;
        float buckTopW = Math.min(w * 0.34f, h * 0.52f);
        float buckBotW = buckTopW * 0.72f;

        float popR = Math.max(1.0f, buckTopW * 0.22f);
        for (int p = -2; p <= 2; p++) {
            float px = buckCx + p * (buckTopW * 0.19f);
            float py = buckTop - popR * (0.30f + (Math.abs(p) % 2 == 0 ? 0.35f : 0.05f));
            drawFilledCircle(vc, mat, light, g, px, py, popR, popR, 4, (p % 2 == 0) ? 0xFFDD44 : 0xFFF2AA, 255);
        }

        for (int k = 0; k < 3; k++) {
            float kf = (float) ((t * 1.35 + k * 0.33) % 1.0);
            float kx = buckCx + (k - 1) * (buckTopW * 0.30f) * kf;
            float ky = buckTop - (4.0f * kf * (1.0f - kf)) * (h * 0.26f);
            drawFilledCircle(vc, mat, light, g, kx, ky, popR * 0.70f, popR * 0.70f, 4, 0xFFEE66, 255);
        }

        int bRows = 7;
        float rStep = (buckBot - buckTop) / bRows;
        for (int r = 0; r < bRows; r++) {
            float f = (float) r / bRows;
            float curW = buckTopW - f * (buckTopW - buckBotW);
            float ry1 = buckTop + r * rStep;
            float ry2 = ry1 + rStep + 0.15f;
            float sw = curW / 5.0f;
            for (int s = 0; s < 5; s++) {
                float sx1 = buckCx - curW * 0.5f + s * sw;
                q(vc, mat, light, g, sx1, ry1, sx1 + sw + 0.12f, ry2, (s % 2 == 0) ? 0xE61E28 : 0xFFFFFF, 255);
            }
        }
        q(vc, mat, light, g, buckCx - buckTopW * 0.52f, buckTop, buckCx + buckTopW * 0.52f, buckTop + 0.8f, 0xFFD700, 255);

        float clX = x + w * 0.55f;
        float clY = y + h * 0.42f;
        float clW = w * 0.36f;
        float clH = h * 0.42f;

        q(vc, mat, light, g, clX, clY, clX + clW, clY + clH, 0x1A202C, 255);
        drawOutlineBox(vc, mat, light, g, clX, clY, clW, clH, 0.6f, 0xDCE4EE, 255);
        q(vc, mat, light, g, clX + clW * 0.14f, clY + clH * 0.28f, clX + clW * 0.86f, clY + clH * 0.42f, 0xFFFFFF, 255);
        q(vc, mat, light, g, clX + clW * 0.14f, clY + clH * 0.58f, clX + clW * 0.62f, clY + clH * 0.72f, 0x00E5FF, 255);

        float barH = Math.max(1.4f, clH * 0.22f);
        q(vc, mat, light, g, clX, clY, clX + clW, clY + barH, 0xF0F4F8, 255);
        for (int s = 0; s < 4; s++) {
            float sx = clX + (s + 0.2f) * (clW / 4.0f);
            q(vc, mat, light, g, sx, clY, sx + clW * 0.12f, clY + barH, 0x12161E, 255);
        }

        float clapOpen = (float) Math.abs(Math.sin(t * 2.8)) * 0.42f;
        float tipX = clX + (float) Math.cos(-clapOpen) * clW;
        float tipY = clY + (float) Math.sin(-clapOpen) * clW;
        drawLine(vc, mat, light, g, clX, clY, tipX, tipY, barH, 0xF0F4F8, 255);
        for (int s = 0; s < 4; s++) {
            float u = (s + 0.35f) / 4.0f;
            float bx = clX + (tipX - clX) * u;
            float by = clY + (tipY - clY) * u;
            q(vc, mat, light, g, bx - barH * 0.45f, by - barH * 0.45f, bx + barH * 0.45f, by + barH * 0.45f, 0x12161E, 255);
        }
    }

    private static void renderPoolHall(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                       float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x5C2C16, 255);

        float rail = Math.max(1.5f, Math.min(w, h) * 0.11f);
        float feltX = x + rail;
        float feltY = y + rail;
        float feltW = w - rail * 2.0f;
        float feltH = h - rail * 2.0f;
        q(vc, mat, light, g, feltX, feltY, feltX + feltW, feltY + feltH, 0x0D7A38, 255);
        drawOutlineBox(vc, mat, light, g, feltX, feltY, feltW, feltH, 0.55f, 0x085224, 255);

        float pockR = Math.max(1.1f, rail * 0.72f);
        for (float[] pk : POOL_POCKETS) {
            drawFilledCircle(vc, mat, light, g, feltX + feltW * pk[0], feltY + feltH * pk[1], pockR, pockR, 4, 0x050806, 255);
        }

        double cycle = (t * 0.90) % 4.4;
        float bR = Math.max(1.2f, Math.min(feltW, feltH) * 0.12f);

        float cueStartX = feltX + feltW * 0.22f;
        float cueStartY = feltY + feltH * 0.62f;
        float eightStartX = feltX + feltW * 0.54f;
        float eightStartY = feltY + feltH * 0.42f;
        float hitCueX = eightStartX - bR * 1.35f;
        float hitCueY = eightStartY + bR * 0.75f;

        float redStartX = feltX + feltW * 0.66f;
        float redStartY = feltY + feltH * 0.68f;
        float hitRedCueX = redStartX - bR * 1.35f;
        float hitRedCueY = redStartY - bR * 0.55f;

        float topRightPockX = feltX + feltW - bR * 0.25f;
        float topRightPockY = feltY + bR * 0.25f;
        float botRightPockX = feltX + feltW - bR * 0.25f;
        float botRightPockY = feltY + feltH - bR * 0.25f;

        float cueX = cueStartX;
        float cueY = cueStartY;
        float eightX = eightStartX;
        float eightY = eightStartY;
        float redX = redStartX;
        float redY = redStartY;
        boolean eightPocketed = false;
        boolean redPocketed = false;

        if (cycle < 1.1) {
            float pull = (float) Math.sin((cycle / 1.1) * Math.PI) * (feltW * 0.12f);
            float stickX1 = cueStartX - bR * 1.4f - pull;
            float stickY1 = cueStartY + bR * 0.85f + pull * 0.55f;
            float stickX2 = stickX1 - feltW * 0.28f;
            float stickY2 = stickY1 + feltH * 0.18f;
            drawLine(vc, mat, light, g, stickX2, stickY2, stickX1, stickY1, Math.max(0.85f, bR * 0.55f), 0xE2A858, 255);
            q(vc, mat, light, g, stickX1 - 0.6f, stickY1 - 0.6f, stickX1 + 0.6f, stickY1 + 0.6f, 0x00B8FF, 255);
        } else if (cycle < 1.80) {
            float u = (float) ((cycle - 1.1) / 0.70);
            cueX = cueStartX + (hitCueX - cueStartX) * u;
            cueY = cueStartY + (hitCueY - cueStartY) * u;
        } else if (cycle < 2.35) {
            float u = (float) ((cycle - 1.80) / 0.55);
            cueX = hitCueX + (hitRedCueX - hitCueX) * u;
            cueY = hitCueY + (hitRedCueY - hitCueY) * u;
            float u8 = (float) ((cycle - 1.80) / 1.15);
            eightX = eightStartX + (topRightPockX - eightStartX) * u8;
            eightY = eightStartY + (topRightPockY - eightStartY) * u8;
        } else if (cycle < 3.40) {
            float u8 = (float) Math.min(1.0, (cycle - 1.80) / 1.15);
            eightX = eightStartX + (topRightPockX - eightStartX) * u8;
            eightY = eightStartY + (topRightPockY - eightStartY) * u8;
            if (u8 >= 1.0f) eightPocketed = true;

            float uRed = (float) ((cycle - 2.35) / 1.05);
            cueX = hitRedCueX - uRed * (feltW * 0.06f);
            cueY = hitRedCueY + uRed * (feltH * 0.08f);
            redX = redStartX + (botRightPockX - redStartX) * uRed;
            redY = redStartY + (botRightPockY - redStartY) * uRed;
        } else {
            cueX = hitRedCueX - feltW * 0.06f;
            cueY = hitRedCueY + feltH * 0.08f;
            eightPocketed = true;
            redPocketed = true;
        }

        if (!redPocketed) {
            drawFilledCircle(vc, mat, light, g, redX, redY, bR, bR, 5, 0xEE2233, 255);
            q(vc, mat, light, g, redX - bR * 0.35f, redY - bR * 0.35f, redX + bR * 0.25f, redY + bR * 0.25f, 0xFFFFFF, 255);
        }

        drawFilledCircle(vc, mat, light, g, cueX, cueY, bR, bR, 5, 0xFFFFFF, 255);

        if (!eightPocketed) {
            drawFilledCircle(vc, mat, light, g, eightX, eightY, bR, bR, 5, 0x101216, 255);
            q(vc, mat, light, g, eightX - bR * 0.42f, eightY - bR * 0.42f, eightX + bR * 0.42f, eightY + bR * 0.42f, 0xFFFFFF, 255);
        }

        if (eightPocketed && (((int) (t * 14.0) % 2) == 0)) {
            drawEllipseArc(vc, mat, light, g, feltX + feltW, feltY, pockR * 1.45f, pockR * 1.45f, 0.8f, Math.PI * 0.5, Math.PI, 6, 0xFFD700, 255);
        }
        if (redPocketed && (((int) (t * 14.0) % 2) == 0)) {
            drawEllipseArc(vc, mat, light, g, feltX + feltW, feltY + feltH, pockR * 1.45f, pockR * 1.45f, 0.8f, Math.PI, Math.PI * 1.5, 6, 0xFFD700, 255);
        }
    }

    private static void renderJewelryShop(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                          float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x080514, 255);

        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.038f), 0xFFD700, 255);

        float cx = x + w * 0.50f;
        float pedY = y + h * 0.88f;
        float pedW = w * 0.46f;

        float ringCy = pedY - h * 0.28f;
        float ringRx = Math.min(w, h) * 0.21f;
        float ringRy = Math.min(w, h) * 0.17f;

        drawFilledCircle(vc, mat, light, g, cx, ringCy, ringRx, ringRy, lowDetail ? 14 : 22, 0xE5A812, 255);
        drawFilledCircle(vc, mat, light, g, cx, ringCy - ringRy * 0.04f, ringRx * 0.92f, ringRy * 0.92f, lowDetail ? 14 : 20, 0xFFD700, 255);
        drawFilledCircle(vc, mat, light, g, cx - ringRx * 0.06f, ringCy - ringRy * 0.08f, ringRx * 0.82f, ringRy * 0.80f, lowDetail ? 12 : 18, 0xFFF2AA, 255);
        drawFilledCircle(vc, mat, light, g, cx, ringCy + ringRy * 0.04f, ringRx * 0.65f, ringRy * 0.62f, lowDetail ? 12 : 18, 0x080514, 255);

        q(vc, mat, light, g, cx - pedW * 0.5f, pedY - h * 0.11f, cx + pedW * 0.5f, pedY, 0x280E42, 255);
        q(vc, mat, light, g, cx - pedW * 0.52f, pedY - h * 0.13f, cx + pedW * 0.52f, pedY - h * 0.10f, 0xFFD700, 255);

        float gemBotY = ringCy - ringRy * 0.72f;
        float gemMidY = gemBotY - h * 0.18f;
        float gemTopY = gemMidY - h * 0.10f;
        float gemHalfW = Math.min(w, h) * 0.22f;

        q(vc, mat, light, g, cx - gemHalfW * 0.35f, gemBotY - 0.8f, cx + gemHalfW * 0.35f, gemBotY + 0.8f, 0xFFD700, 255);

        int shimmer = ((int) (t * 4.5)) % 3;
        int cLeft = (shimmer == 0) ? 0xFFFFFF : 0x55EEFF;
        int cCenter = (shimmer == 1) ? 0xFFFFFF : 0x00C8FF;
        int cRight = (shimmer == 2) ? 0xFFFFFF : 0x88F8FF;

        int crownSteps = 4;
        for (int i = 0; i < crownSteps; i++) {
            float f = (float) i / crownSteps;
            float hw = gemHalfW * (0.68f + f * 0.32f);
            float y1 = gemTopY + f * (gemMidY - gemTopY);
            float y2 = y1 + (gemMidY - gemTopY) / crownSteps + 0.15f;
            q(vc, mat, light, g, cx - hw, y1, cx + hw, y2, cRight, 255);
            q(vc, mat, light, g, cx - hw * 0.38f, y1, cx + hw * 0.38f, y2, cLeft, 255);
        }

        int pavSteps = 7;
        for (int i = 0; i < pavSteps; i++) {
            float f = (float) i / pavSteps;
            float hw = gemHalfW * (1.0f - f * 0.90f);
            float y1 = gemMidY + f * (gemBotY - gemMidY);
            float y2 = y1 + (gemBotY - gemMidY) / pavSteps + 0.15f;
            q(vc, mat, light, g, cx - hw, y1, cx + hw, y2, cCenter, 255);
            q(vc, mat, light, g, cx - hw, y1, cx - hw * 0.25f, y2, cLeft, 255);
        }

        float[][] flares = JEWELRY_FLARES;
        for (int f = 0; f < flares.length; f++) {
            float pulse = (float) Math.abs(Math.sin(t * 5.0 + f * 1.1));
            float fx = cx + flares[f][0] * gemHalfW;
            float fy = gemMidY + flares[f][1] * (gemBotY - gemTopY);
            float fSz = Math.max(1.0f, gemHalfW * 0.38f * pulse);
            q(vc, mat, light, g, fx - fSz, fy - 0.35f, fx + fSz, fy + 0.35f, 0xFFFFFF, 255);
            q(vc, mat, light, g, fx - 0.35f, fy - fSz, fx + 0.35f, fy + fSz, 0xFFFFFF, 255);
        }
    }

    private static void renderBasketballMatch(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                              float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        double cycle = (t * 0.95) % 4.6;
        boolean scored = cycle >= 2.55;

        float standsH = Math.max(2.6f, h * 0.22f);
        float courtY = y + standsH;
        float courtH = h - standsH;

        q(vc, mat, light, g, x, y, x + w, courtY, 0x0E1420, 255);

        float clkW = Math.max(4.2f, w * 0.22f);
        float clkX = x + (w - clkW) * 0.5f;
        q(vc, mat, light, g, clkX, y + 0.4f, clkX + clkW, courtY - 0.4f, 0x05080E, 255);
        drawOutlineBox(vc, mat, light, g, clkX, y + 0.4f, clkW, standsH - 0.8f, 0.45f, scored ? 0xFF2233 : 0xFFAA00, 255);
        int secRem = scored ? 0 : Math.max(1, 3 - (int) (cycle * 1.15));
        float dw = Math.max(1.0f, Math.min(clkW * 0.32f, (standsH - 1.2f) * 0.58f));
        float dh = Math.min(standsH - 1.2f, dw * 1.4f);
        draw7SegmentDigit(vc, mat, light, g, clkX + (clkW - dw * 2.3f) * 0.5f, y + (standsH - dh) * 0.5f, dw, dh, 0, 0xFF2233, 0x24080C);
        draw7SegmentDigit(vc, mat, light, g, clkX + (clkW - dw * 2.3f) * 0.5f + dw * 1.3f, y + (standsH - dh) * 0.5f, dw, dh, secRem, 0xFF2233, 0x24080C);

        int planks = 6;
        float plankH = courtH / planks;
        for (int p = 0; p < planks; p++) {
            float py1 = courtY + p * plankH;
            int wCol = (p % 2 == 0) ? 0xD88E42 : 0xC87E34;
            q(vc, mat, light, g, x, py1, x + w, py1 + plankH + 0.15f, wCol, 255);
        }

        float keyW = w * 0.28f;
        float keyH = courtH * 0.52f;
        float keyY = courtY + (courtH - keyH) * 0.5f;
        q(vc, mat, light, g, x + w - keyW, keyY, x + w - 1.0f, keyY + keyH, 0xC82222, 255);
        drawOutlineBox(vc, mat, light, g, x + w - keyW, keyY, keyW - 1.0f, keyH, 0.55f, 0xFFFFFF, 255);
        drawEllipseArc(vc, mat, light, g, x + w - 1.5f, courtY + courtH * 0.5f, w * 0.44f, courtH * 0.44f, 0.55f, Math.PI * 0.55, Math.PI * 1.45, lowDetail ? 10 : 14, 0xFFFFFF, 255);

        float hoopX = x + w - Math.max(2.6f, w * 0.10f);
        float hoopY = courtY + courtH * 0.34f;
        q(vc, mat, light, g, hoopX + 1.2f, hoopY - courtH * 0.20f, hoopX + 2.0f, hoopY + courtH * 0.08f, 0xE8F4FF, 255);
        q(vc, mat, light, g, hoopX + 1.5f, hoopY, x + w, hoopY + 0.9f, 0x384454, 255);

        float rimW = Math.max(2.2f, w * 0.08f);
        float rimCx = hoopX - rimW * 0.45f;
        float netSway = (scored && cycle < 3.25) ? ((float) Math.sin((cycle - 2.55) * 18.0) * 0.85f) : 0.0f;
        q(vc, mat, light, g, hoopX - rimW + 0.4f + netSway * 0.5f, hoopY + 0.6f, hoopX + 0.8f + netSway, hoopY + courtH * 0.18f, 0xF0F4F8, 255);
        q(vc, mat, light, g, hoopX - rimW, hoopY - 0.3f, hoopX + 1.2f, hoopY + 0.7f, 0xFF5500, 255);

        float pR = Math.max(1.0f, Math.min(w, courtH) * 0.075f);
        float pgX = x + w * 0.16f;
        float pgY = courtY + courtH * 0.64f;
        q(vc, mat, light, g, pgX - pR * 0.5f, pgY, pgX + pR * 0.5f, pgY + pR * 1.4f, 0x1E2838, 255);
        drawFilledCircle(vc, mat, light, g, pgX, pgY - pR * 0.2f, pR, pR * 1.1f, 5, 0x6622CC, 255);
        drawFilledCircle(vc, mat, light, g, pgX, pgY - pR * 1.05f, pR * 0.65f, pR * 0.65f, 4, 0x8D5524, 255);

        float shX = x + w * 0.42f;
        float shBaseY = courtY + courtH * 0.68f;
        float jumpU = (cycle >= 0.80 && cycle < 1.70) ? (float) ((cycle - 0.80) / 0.90) : 0.0f;
        float shJump = (float) Math.sin(jumpU * Math.PI) * (courtH * 0.22f);
        float shY = shBaseY - shJump;
        q(vc, mat, light, g, shX - pR * 0.5f, shY, shX + pR * 0.5f, shY + pR * 1.4f, 0x1E2838, 255);
        drawFilledCircle(vc, mat, light, g, shX, shY - pR * 0.2f, pR, pR * 1.1f, 5, 0xFFCC00, 255);
        drawFilledCircle(vc, mat, light, g, shX, shY - pR * 1.05f, pR * 0.65f, pR * 0.65f, 4, 0x8D5524, 255);
        if (jumpU > 0.1f) {
            drawLine(vc, mat, light, g, shX + pR * 0.4f, shY - pR * 0.5f, shX + pR * 1.1f, shY - pR * 1.65f, 0.75f, 0x8D5524, 255);
        }

        float defX = x + w * 0.62f;
        float defBaseY = courtY + courtH * 0.62f;
        float defJumpU = (cycle >= 1.00 && cycle < 1.85) ? (float) ((cycle - 1.00) / 0.85) : 0.0f;
        float defY = defBaseY - (float) Math.sin(defJumpU * Math.PI) * (courtH * 0.16f);
        q(vc, mat, light, g, defX - pR * 0.5f, defY, defX + pR * 0.5f, defY + pR * 1.4f, 0x1E2838, 255);
        drawFilledCircle(vc, mat, light, g, defX, defY - pR * 0.2f, pR, pR * 1.1f, 5, 0x00AA55, 255);
        drawFilledCircle(vc, mat, light, g, defX, defY - pR * 1.05f, pR * 0.65f, pR * 0.65f, 4, 0xF2C29B, 255);
        if (defJumpU > 0.1f) {
            drawLine(vc, mat, light, g, defX - pR * 0.4f, defY - pR * 0.5f, defX - pR * 1.0f, defY - pR * 1.6f, 0.75f, 0xF2C29B, 255);
        }

        float releaseX = shX + pR * 0.85f;
        float releaseY = shBaseY - courtH * 0.22f - pR * 1.55f;
        float ballX;
        float ballY;
        if (cycle < 0.80) {
            float u = (float) (cycle / 0.80);
            ballX = pgX + (shX - pgX) * u;
            ballY = (pgY - pR * 0.4f) + (shBaseY - pR * 0.4f - (pgY - pR * 0.4f)) * u - (float) Math.abs(Math.sin(u * Math.PI * 2.0)) * (courtH * 0.12f);
        } else if (cycle < 1.15) {
            ballX = shX + pR * 0.85f;
            ballY = shY - pR * 1.55f;
        } else if (cycle < 2.55) {
            float u = (float) ((cycle - 1.15) / 1.40);
            ballX = releaseX + (rimCx - releaseX) * u;
            float linearY = releaseY + (hoopY - releaseY) * u;
            float arcH = courtH * 0.40f;
            ballY = linearY - (4.0f * u * (1.0f - u)) * arcH;
        } else {
            float dropU = (float) Math.min(1.0, (cycle - 2.55) / 0.85);
            ballX = rimCx;
            ballY = hoopY + dropU * (courtH * 0.44f) - (dropU > 0.65f ? (float) Math.abs(Math.sin((dropU - 0.65f) * 9.0f)) * (courtH * 0.10f) : 0.0f);
        }
        float bSz = Math.max(0.95f, pR * 0.78f);
        drawFilledCircle(vc, mat, light, g, ballX, ballY, bSz, bSz, 5, 0xFF6600, 255);
        q(vc, mat, light, g, ballX - bSz * 0.8f, ballY - 0.25f, ballX + bSz * 0.8f, ballY + 0.25f, 0x221004, 255);

        if (scored && cycle > 2.95) {
            float sbW = w * 0.56f;
            float sbH = courtH * 0.48f;
            float sbX = x + (w - sbW) * 0.5f;
            float sbY = courtY + (courtH - sbH) * 0.5f;
            q(vc, mat, light, g, sbX, sbY, sbX + sbW, sbY + sbH, 0x0E1420, 255);
            drawOutlineBox(vc, mat, light, g, sbX, sbY, sbW, sbH, 0.75f, (((int) (t * 14.0) % 2) == 0) ? 0xFFD700 : 0xFF5500, 255);
            float sdw = Math.max(1.5f, Math.min(sbW * 0.22f, sbH * 0.45f));
            float sdh = sdw * 1.42f;
            float sdy = sbY + (sbH - sdh) * 0.5f;
            q(vc, mat, light, g, sbX + sbW * 0.20f, sbY + sbH * 0.44f, sbX + sbW * 0.42f, sbY + sbH * 0.56f, 0x00FF66, 255);
            q(vc, mat, light, g, sbX + sbW * 0.28f, sbY + sbH * 0.28f, sbX + sbW * 0.34f, sbY + sbH * 0.72f, 0x00FF66, 255);
            draw7SegmentDigit(vc, mat, light, g, sbX + sbW * 0.56f, sdy, sdw, sdh, 3, 0xFFD700, 0x241C04);
        }
    }

    private static void renderBoxingGym(VertexConsumer vc, Matrix4f mat, int light, GuiGraphics g,
                                        float x, float y, float w, float h, int baseColor, boolean rainbow, double t, boolean lowDetail) {
        q(vc, mat, light, g, x, y, x + w, y + h, 0x0A060C, 255);

        q(vc, mat, light, g, x + 1.0f, y + h * 0.34f, x + w - 1.0f, y + h * 0.38f, 0x881420, 255);
        q(vc, mat, light, g, x + 1.0f, y + h * 0.54f, x + w - 1.0f, y + h * 0.58f, 0xB8C4D0, 255);
        q(vc, mat, light, g, x + 1.0f, y + h * 0.74f, x + w - 1.0f, y + h * 0.78f, 0x144488, 255);

        int neonRim = rainbow ? getRainbowColor(t * 0.35) : (baseColor == 0x00FF66 ? 0xFF2233 : baseColor);
        drawOutlineBox(vc, mat, light, g, x, y, w, h, Math.max(0.8f, Math.min(w, h) * 0.038f), neonRim, 255);

        double cycle = (t * 1.35) % 2.4;
        float punchFrac = 0.0f;
        boolean impact = false;
        float bagSwing = 0.0f;

        if (cycle < 0.25) {
            punchFrac = (float) (cycle / 0.25);
        } else if (cycle < 0.55) {
            float u = (float) ((cycle - 0.25) / 0.30);
            punchFrac = 1.0f - u * 0.65f;
            impact = (cycle < 0.42);
            bagSwing = (float) Math.sin(u * Math.PI * 0.5) * 0.75f;
        } else if (cycle < 0.80) {
            float u = (float) ((cycle - 0.55) / 0.25);
            punchFrac = 0.35f + u * 0.65f;
            bagSwing = 0.75f * (1.0f - u * 0.35f);
        } else if (cycle < 1.15) {
            float u = (float) ((cycle - 0.80) / 0.35);
            punchFrac = 1.0f - u;
            impact = (cycle < 1.02);
            bagSwing = 1.0f;
        } else {
            double dt = cycle - 1.15;
            bagSwing = (float) (Math.cos(dt * 7.5) * Math.exp(-dt * 2.2));
        }

        float pivotX = x + w * 0.64f;
        float pivotY = y + 1.0f;
        q(vc, mat, light, g, pivotX - 1.6f, pivotY, pivotX + 1.6f, pivotY + 1.2f, 0x90A0B0, 255);

        float bagW = Math.min(w * 0.24f, h * 0.34f);
        float bagH = h * 0.58f;
        float chainH = h * 0.16f;
        float swingX = bagSwing * (w * 0.11f);
        float swingY = -Math.abs(bagSwing) * (h * 0.04f);

        float bagTopX = pivotX + swingX * 0.55f;
        float bagTopY = pivotY + chainH + swingY * 0.5f;
        float bagBotX = pivotX + swingX * 1.15f;
        float bagBotY = bagTopY + bagH + swingY;

        drawLine(vc, mat, light, g, pivotX, pivotY + 0.8f, bagTopX - bagW * 0.35f, bagTopY, 0.75f, 0xC0CCD8, 255);
        drawLine(vc, mat, light, g, pivotX, pivotY + 0.8f, bagTopX + bagW * 0.35f, bagTopY, 0.75f, 0xC0CCD8, 255);

        int bagSlices = 9;
        for (int s = 0; s < bagSlices; s++) {
            float f1 = (float) s / bagSlices;
            float f2 = (float) (s + 1) / bagSlices;
            float sx = bagTopX + (bagBotX - bagTopX) * f1;
            float sy1 = bagTopY + (bagBotY - bagTopY) * f1;
            float sy2 = bagTopY + (bagBotY - bagTopY) * f2 + 0.2f;
            boolean isStrap = (s == 0 || s == bagSlices - 1);
            int bCol = isStrap ? 0x1A2028 : 0xD81E28;
            int shadeCol = isStrap ? 0x10141A : 0x981018;
            q(vc, mat, light, g, sx - bagW * 0.5f, sy1, sx + bagW * 0.5f, sy2, bCol, 255);
            q(vc, mat, light, g, sx + bagW * 0.18f, sy1, sx + bagW * 0.5f, sy2, shadeCol, 255);
            if (!isStrap) {
                q(vc, mat, light, g, sx - bagW * 0.38f, sy1, sx - bagW * 0.18f, sy2, 0xFF5560, 255);
            }
        }

        float hitTargetX = (bagTopX + bagBotX) * 0.5f - bagW * 0.5f;
        float gloveRestX = x + w * 0.20f;
        float gloveMaxX = hitTargetX - bagW * 0.18f;
        float gloveCx = gloveRestX + punchFrac * (gloveMaxX - gloveRestX);
        float gloveCy = y + h * 0.52f;
        float gloveR = Math.min(w, h) * 0.15f;

        q(vc, mat, light, g, x + 0.8f, gloveCy - gloveR * 0.52f, gloveCx - gloveR * 0.75f, gloveCy + gloveR * 0.52f, 0xE8B488, 255);
        q(vc, mat, light, g, gloveCx - gloveR * 1.15f, gloveCy - gloveR * 0.65f, gloveCx - gloveR * 0.48f, gloveCy + gloveR * 0.65f, 0xF0F4F8, 255);
        q(vc, mat, light, g, gloveCx - gloveR * 0.95f, gloveCy - gloveR * 0.55f, gloveCx - gloveR * 0.70f, gloveCy + gloveR * 0.55f, 0xFF1E27, 255);

        drawFilledCircle(vc, mat, light, g, gloveCx, gloveCy, gloveR * 1.05f, gloveR * 0.92f, 7, 0xFF1E27, 255);
        drawFilledCircle(vc, mat, light, g, gloveCx - gloveR * 0.15f, gloveCy - gloveR * 0.68f, gloveR * 0.52f, gloveR * 0.42f, 5, 0xD8141E, 255);
        drawFilledCircle(vc, mat, light, g, gloveCx + gloveR * 0.35f, gloveCy - gloveR * 0.25f, gloveR * 0.35f, gloveR * 0.35f, 4, 0xFF8888, 255);

        if (impact) {
            float impX = hitTargetX;
            float impY = gloveCy;
            float impR = gloveR * 1.25f;
            drawEllipseArc(vc, mat, light, g, impX, impY, impR, impR * 1.15f, 0.85f, Math.PI * 0.55, Math.PI * 1.45, 7, 0xFFEE44, 255);
            drawLine(vc, mat, light, g, impX - 0.5f, impY - impR * 0.6f, impX - impR * 0.9f, impY - impR * 1.15f, 0.8f, 0xFFFFFF, 255);
            drawLine(vc, mat, light, g, impX - 0.5f, impY + impR * 0.6f, impX - impR * 0.9f, impY + impR * 1.15f, 0.8f, 0xFFFFFF, 255);
            drawLine(vc, mat, light, g, impX - 0.8f, impY, impX - impR * 1.1f, impY, 0.85f, 0xFFEE44, 255);
        }
    }
}
