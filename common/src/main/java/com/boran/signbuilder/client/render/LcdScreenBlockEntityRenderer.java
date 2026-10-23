package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.LcdScreenBlock;
import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import com.boran.signbuilder.block.entity.LcdTextFormatter;
import com.boran.signbuilder.client.LcdFontHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import com.mojang.math.Matrix4f;

import java.awt.Color;

public class LcdScreenBlockEntityRenderer implements BlockEntityRenderer<LcdScreenBlockEntity> {

    private static final class LcdRenderTypes extends net.minecraft.client.renderer.RenderStateShard {
        private static final RenderType CANVAS = RenderType.create(
                "signbuilder_lcd_canvas",
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                4096,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_LIGHTMAP_SHADER)
                        .setTextureState(NO_TEXTURE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );

        private LcdRenderTypes(String name, Runnable setupState, Runnable clearState) {
            super(name, setupState, clearState);
        }
    }

    private static final StringBuilder ACTIVE_STYLE_BUILDER = new StringBuilder();

    public LcdScreenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(LcdScreenBlockEntity blockEntity) {
        return true;
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox(LcdScreenBlockEntity blockEntity) {
        return new net.minecraft.world.phys.AABB(blockEntity.getBlockPos()).inflate(32.0);
    }

    public net.minecraft.world.phys.AABB getViewBoundingBox(LcdScreenBlockEntity blockEntity) {
        return new net.minecraft.world.phys.AABB(blockEntity.getBlockPos()).inflate(32.0);
    }

    private static boolean hasDisplayContent(LcdScreenBlockEntity be) {
        String t = be.getText();
        return (t != null && !t.trim().isEmpty())
                || be.isShowTime() || be.isShowWeather() || be.isShowPlayer() || be.isShowDay()
                || be.getTheme() != 0 || be.getVisualAnim() != 0;
    }

    @Override
    public void render(LcdScreenBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = entity.getLevel();
        if (level == null) return;

        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof LcdScreenBlock)) return;

        Direction facing = state.getValue(LcdScreenBlock.FACING);
        AttachFace face = state.getValue(LcdScreenBlock.FACE);

        long gameTime = level.getGameTime();
        BlockPos masterPos;
        int widthBlocks;
        int heightBlocks;

        if (entity.cachedMasterPos != null && gameTime - entity.lastCacheUpdateTick < 20L) {
            masterPos = entity.cachedMasterPos;
            widthBlocks = entity.cachedWidthBlocks;
            heightBlocks = entity.cachedHeightBlocks;
        } else {
            masterPos = LcdScreenBlock.getMasterPos(level, entity.getBlockPos(), state);
            Direction rightDir = LcdScreenBlock.getScreenRightDir(facing, face);
            Direction downDir = LcdScreenBlock.getScreenDownDir(facing, face);

            widthBlocks = 1;
            BlockPos checkRight = masterPos;
            for (int i = 1; i < 32; i++) {
                if (LcdScreenBlock.isConnectedNeighbor(level, checkRight, rightDir, facing, face)) {
                    widthBlocks++;
                    checkRight = checkRight.relative(rightDir);
                } else {
                    break;
                }
            }

            heightBlocks = 1;
            BlockPos checkDown = masterPos;
            for (int i = 1; i < 32; i++) {
                if (LcdScreenBlock.isConnectedNeighbor(level, checkDown, downDir, facing, face)) {
                    heightBlocks++;
                    checkDown = checkDown.relative(downDir);
                } else {
                    break;
                }
            }

            entity.cachedMasterPos = masterPos;
            entity.cachedWidthBlocks = widthBlocks;
            entity.cachedHeightBlocks = heightBlocks;
            entity.lastCacheUpdateTick = gameTime;
        }

        BlockPos myPos = entity.getBlockPos();
        boolean isMaster = masterPos.equals(myPos);
        LcdScreenBlockEntity master;
        if (isMaster) {
            master = entity;
        } else {
            BlockEntity be = level.getBlockEntity(masterPos);
            if (be instanceof LcdScreenBlockEntity masterBe) {
                master = masterBe;
            } else {
                master = entity;
            }
        }

        long now = System.nanoTime();
        boolean newFrame = (gameTime != master.lastRenderGameTime)
                || (partialTick != master.lastRenderPartialTick)
                || (now - master.lastRenderNanoTime > 2_500_000L)
                || myPos.equals(master.activeRenderPos);
        if (!newFrame) {
            return;
        }
        master.activeRenderPos = myPos;
        master.lastRenderGameTime = gameTime;
        master.lastRenderPartialTick = partialTick;
        master.lastRenderNanoTime = now;

        String text = master.getText();
        boolean hasText = text != null && !text.trim().isEmpty();
        boolean hasWidgets = master.hasWidgets();
        int visualAnim = master.getVisualAnim();
        if (!hasText && !hasWidgets && master.getTheme() == 0 && visualAnim == 0) return;

        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        if (!isMaster) {
            BlockPos offset = masterPos.subtract(myPos);
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
        }
        poseStack.translate(0.5, 0.5, 0.5);

        if (face == AttachFace.WALL) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
            poseStack.translate(0.0, 0.0, -0.402);
        } else if (face == AttachFace.FLOOR) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0f));
            poseStack.translate(0.0, 0.0, -0.402);
        } else {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-facing.toYRot()));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
            poseStack.translate(0.0, 0.0, -0.402);
        }

        float centerX = (widthBlocks - 1.0f) * 0.5f;
        float centerY = -(heightBlocks - 1.0f) * 0.5f;
        poseStack.translate(centerX, centerY, 0.0);

        float baseScale = 0.022f;
        float fontScale = (heightBlocks == 1) ? baseScale : (baseScale * (1.0f + (heightBlocks - 1) * 0.45f));
        poseStack.scale(fontScale, -fontScale, fontScale);

        int color = master.getColor();
        if (master.isRainbow()) {
            float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
            color = Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
        }

        int light = master.isGlow() ? 15728880 : packedLight;

        long timeMs = System.currentTimeMillis();
        int align = master.getAlign();
        int mode1 = master.getMode();
        int mode2 = master.getMode2();
        int speed = Math.max(1, Math.min(4, master.getSpeed()));
        double pixelsPerSec = switch (speed) {
            case 1 -> 30.0;
            case 2 -> 60.0;
            case 3 -> 100.0;
            case 4 -> 160.0;
            default -> 60.0;
        };

        float halfScreenWidth = (widthBlocks * 0.5f) / fontScale;
        float halfScreenHeight = (heightBlocks * 0.5f) / fontScale;
        float margin = 0.055f / fontScale;
        float minX = -halfScreenWidth + margin;
        float maxX = halfScreenWidth - margin;
        float topY = -halfScreenHeight + margin;
        float bottomY = halfScreenHeight - margin;

        renderScreenBackground(poseStack, bufferSource, master.getTheme(), master.getColor(), master.isRainbow(), minX, maxX, topY, bottomY, 15728880);
        if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(LcdRenderTypes.CANVAS);
        }

        if (hasWidgets && visualAnim == 0) {
            renderCornerWidgets(font, poseStack, bufferSource, master, level, minX, maxX, topY, bottomY, light, color, master.getFontStyle());
        }

        if (visualAnim > 0) {
            double distSq = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                    .distanceToSqr(masterPos.getX() + 0.5, masterPos.getY() + 0.5, masterPos.getZ() + 0.5);
            if (distSq <= 64.0 * 64.0) {
                boolean lowDetail = distSq > 24.0 * 24.0;
                poseStack.pushPose();
                poseStack.translate(0.0, 0.0, 0.06f);
                VertexConsumer vc = bufferSource.getBuffer(LcdRenderTypes.CANVAS);
                LcdVisualAnimator.render3D(vc, poseStack.last().pose(), light,
                        minX, maxX, topY, bottomY,
                        visualAnim, speed, color, master.isRainbow(), timeMs, lowDetail);
                if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
                    bs.endBatch(LcdRenderTypes.CANVAS);
                }
                poseStack.popPose();
            }
        } else {
            String line1 = master.getResolvedLine(0, level);
            String line2 = master.getResolvedLine(1, level);
            boolean hasL1 = !line1.trim().isEmpty();
            boolean hasL2 = !line2.trim().isEmpty();

            float sizeMult = switch (master.getFontSize()) {
                case 0 -> 0.75f;
                case 2 -> 1.35f;
                case 3 -> 1.75f;
                default -> 1.0f;
            };

            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 0.04f);
            poseStack.scale(sizeMult, sizeMult, 1.0f);
            float scaledMinX = minX / sizeMult;
            float scaledMaxX = maxX / sizeMult;
            float scaledHalfH = halfScreenHeight / sizeMult;
            double scaledPixelsPerSec = pixelsPerSec / sizeMult;
            int fontStyle = master.getFontStyle();

            if (hasL1 && !hasL2) {
                renderSingleLine(font, poseStack, bufferSource, line1, -font.lineHeight / 2.0f, scaledMinX, scaledMaxX,
                        align, mode1, speed, scaledPixelsPerSec, timeMs, color, master.isGlow(), light, fontStyle);
            } else if (!hasL1 && hasL2) {
                renderSingleLine(font, poseStack, bufferSource, line2, -font.lineHeight / 2.0f, scaledMinX, scaledMaxX,
                        align, mode2, speed, scaledPixelsPerSec, timeMs, color, master.isGlow(), light, fontStyle);
            } else if (hasL1 && hasL2) {
                float lineOffset = Math.max(font.lineHeight * 0.65f, scaledHalfH * 0.32f);
                renderSingleLine(font, poseStack, bufferSource, line1, -lineOffset - font.lineHeight / 2.0f, scaledMinX, scaledMaxX,
                        align, mode1, speed, scaledPixelsPerSec, timeMs, color, master.isGlow(), light, fontStyle);
                renderSingleLine(font, poseStack, bufferSource, line2, lineOffset - font.lineHeight / 2.0f, scaledMinX, scaledMaxX,
                        align, mode2, speed, scaledPixelsPerSec, timeMs, color, master.isGlow(), light, fontStyle);
            }
            poseStack.popPose();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.gameMode.isDestroying() && mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bhr) {
            BlockPos hitPos = bhr.getBlockPos();
            Direction rightDir = LcdScreenBlock.getScreenRightDir(facing, face);
            Direction downDir = LcdScreenBlock.getScreenDownDir(facing, face);
            int hitCol = -1;
            int hitRow = -1;
            for (int x = 0; x < widthBlocks; x++) {
                for (int y = 0; y < heightBlocks; y++) {
                    if (masterPos.relative(rightDir, x).relative(downDir, y).equals(hitPos)) {
                        hitCol = x;
                        hitRow = y;
                        break;
                    }
                }
                if (hitCol >= 0) break;
            }
            if (hitCol >= 0) {
                int stage = -1;
                if (stage >= 0 && stage <= 9) {
                    float colWidth = (maxX - minX) / widthBlocks;
                    float rowHeight = (bottomY - topY) / heightBlocks;
                    float crackX1 = minX + hitCol * colWidth;
                    float crackX2 = minX + (hitCol + 1) * colWidth;
                    float crackY1 = topY + hitRow * rowHeight;
                    float crackY2 = topY + (hitRow + 1) * rowHeight;
                    float crackZ = 0.50f;

                    net.minecraft.resources.ResourceLocation crackTex = new net.minecraft.resources.ResourceLocation("minecraft", "textures/block/destroy_stage_" + stage + ".png");
                    VertexConsumer crackConsumer = bufferSource.getBuffer(RenderType.text(crackTex));
                    Matrix4f mat = poseStack.last().pose();

                    crackConsumer.vertex(mat, crackX1, crackY2, crackZ).color(255, 255, 255, 255).uv(0.0f, 1.0f).uv2(15728880).endVertex();
                    crackConsumer.vertex(mat, crackX2, crackY2, crackZ).color(255, 255, 255, 255).uv(1.0f, 1.0f).uv2(15728880).endVertex();
                    crackConsumer.vertex(mat, crackX2, crackY1, crackZ).color(255, 255, 255, 255).uv(1.0f, 0.0f).uv2(15728880).endVertex();
                    crackConsumer.vertex(mat, crackX1, crackY1, crackZ).color(255, 255, 255, 255).uv(0.0f, 0.0f).uv2(15728880).endVertex();
                }
            }
        }

        poseStack.popPose();
    }

    private void renderCornerWidgets(Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                     LcdScreenBlockEntity entity, Level level,
                                     float minX, float maxX, float topY, float bottomY, int light, int color, int fontStyle) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.08f);
        float widgetScale = 0.60f;
        poseStack.scale(widgetScale, widgetScale, widgetScale);
        Matrix4f matrix = poseStack.last().pose();

        float scaledMinX = (minX + 3.0f) / widgetScale;
        float scaledMaxX = (maxX - 3.0f) / widgetScale;
        float scaledTopY = (topY + 3.0f) / widgetScale;
        float scaledBottomY = (bottomY - font.lineHeight - 2.0f) / widgetScale;

        int widgetColor = 0xDD000000 | (color & 0x00FFFFFF);

        if (entity.getWidgetTL() > 0) {
            String str = entity.getResolvedWidgetText(0, level);
            if (!str.isEmpty()) {
                FormattedCharSequence seq = LcdFontHelper.toFormatted(str, fontStyle);
                font.drawInBatch(seq, scaledMinX, scaledTopY, widgetColor, false, matrix, bufferSource, false, 0, light);
            }
        }
        if (entity.getWidgetTR() > 0) {
            String str = entity.getResolvedWidgetText(1, level);
            if (!str.isEmpty()) {
                float w = LcdFontHelper.getWidth(font, str, fontStyle);
                FormattedCharSequence seq = LcdFontHelper.toFormatted(str, fontStyle);
                font.drawInBatch(seq, scaledMaxX - w, scaledTopY, widgetColor, false, matrix, bufferSource, false, 0, light);
            }
        }
        if (entity.getWidgetBL() > 0) {
            String str = entity.getResolvedWidgetText(2, level);
            if (!str.isEmpty()) {
                FormattedCharSequence seq = LcdFontHelper.toFormatted(str, fontStyle);
                font.drawInBatch(seq, scaledMinX, scaledBottomY, widgetColor, false, matrix, bufferSource, false, 0, light);
            }
        }
        if (entity.getWidgetBR() > 0) {
            String str = entity.getResolvedWidgetText(3, level);
            if (!str.isEmpty()) {
                float w = LcdFontHelper.getWidth(font, str, fontStyle);
                FormattedCharSequence seq = LcdFontHelper.toFormatted(str, fontStyle);
                font.drawInBatch(seq, scaledMaxX - w, scaledBottomY, widgetColor, false, matrix, bufferSource, false, 0, light);
            }
        }

        poseStack.popPose();
    }

    private void renderSingleLine(Font font, PoseStack poseStack, MultiBufferSource bufferSource,
                                  String lineText, float textY, float minX, float maxX,
                                  int align, int mode, int speed, double pixelsPerSec, long timeMs,
                                  int color, boolean glow, int light, int fontStyle) {
        if (lineText == null || lineText.trim().isEmpty()) return;

        int textWidth = LcdFontHelper.getWidth(font, lineText, fontStyle);
        float alignedBaseX = switch (align) {
            case 0 -> minX + 2.0f;
            case 2 -> maxX - textWidth - 2.0f;
            default -> -textWidth / 2.0f;
        };

        float textStartX = alignedBaseX;
        String textToDraw = lineText;

        if (mode == 1) {
            double track = (maxX - minX) + textWidth + 12.0;
            double cycleMs = Math.max(100.0, (track / pixelsPerSec) * 1000.0);
            double progress = (timeMs % (long) cycleMs) / cycleMs;
            textStartX = (float) (maxX - (progress * track));
        } else if (mode == 2) {
            double track = (maxX - minX) + textWidth + 12.0;
            double cycleMs = Math.max(100.0, (track / pixelsPerSec) * 1000.0);
            double progress = (timeMs % (long) cycleMs) / cycleMs;
            textStartX = (float) (minX - textWidth + (progress * track));
        } else if (mode == 3) {
            textStartX = alignedBaseX;
            int blinkInterval = switch (speed) {
                case 1 -> 800;
                case 2 -> 500;
                case 3 -> 300;
                case 4 -> 150;
                default -> 500;
            };
            boolean visible = ((timeMs / blinkInterval) % 2) == 0;
            if (!visible) return;
        } else if (mode == 4) {
            textStartX = alignedBaseX;
            int charDelay = switch (speed) {
                case 1 -> 250;
                case 2 -> 150;
                case 3 -> 90;
                case 4 -> 50;
                default -> 150;
            };
            int visLen = LcdTextFormatter.countVisibleChars(lineText);
            int cycle = (int) ((timeMs / charDelay) % (visLen + 10));
            int showChars = Math.min(visLen, cycle);
            textToDraw = LcdTextFormatter.substringVisibleChars(lineText, showChars);
        } else if (mode == 5) {
            textStartX = alignedBaseX;
            double freq = switch (speed) {
                case 1 -> 0.002;
                case 2 -> 0.004;
                case 3 -> 0.007;
                case 4 -> 0.012;
                default -> 0.004;
            };
            double timeMod = (timeMs % 10000000L);
            float factor = (float) (0.625 + 0.375 * Math.sin(timeMod * freq));
            int r = (int) (((color >> 16) & 0xFF) * factor);
            int g = (int) (((color >> 8) & 0xFF) * factor);
            int b = (int) ((color & 0xFF) * factor);
            color = (r << 16) | (g << 8) | b;
        } else if (mode == 6) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 0.06f);
            Matrix4f matrix = poseStack.last().pose();
            drawWaveText(font, lineText, alignedBaseX, textY, minX, maxX, 0xFF000000 | color, glow, matrix, bufferSource, light, speed, timeMs, fontStyle);
            poseStack.popPose();
            return;
        } else if (mode == 7) {
            textStartX = alignedBaseX;
            textToDraw = LcdTextFormatter.buildGlitchText(lineText, speed, timeMs);
        } else if (mode == 8) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 0.06f);
            Matrix4f matrix = poseStack.last().pose();
            drawRainbowFlowText(font, lineText, alignedBaseX, textY, minX, maxX, glow, matrix, bufferSource, light, speed, timeMs, fontStyle);
            poseStack.popPose();
            return;
        } else if (mode == 9) {
            if (textWidth <= (maxX - minX)) {
                textStartX = alignedBaseX;
            } else {
                double track = (maxX - minX) + textWidth + 12.0;
                double cycleMs = Math.max(100.0, (track / pixelsPerSec) * 1000.0);
                double progress = (timeMs % (long) cycleMs) / cycleMs;
                textStartX = (float) (maxX - (progress * track));
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.06f);
        Matrix4f matrix = poseStack.last().pose();
        drawClippedText(font, textToDraw, textStartX, textY, minX, maxX, 0xFF000000 | color, glow, matrix, bufferSource, light, fontStyle);
        poseStack.popPose();
    }

    private void drawWaveText(Font font, String text, float startX, float textY, float minX, float maxX,
                              int color, boolean glow, Matrix4f matrix, MultiBufferSource bufferSource, int light,
                              int speed, long timeMs, int fontStyle) {
        double freq = switch (speed) {
            case 1 -> 0.003;
            case 2 -> 0.006;
            case 3 -> 0.010;
            case 4 -> 0.016;
            default -> 0.006;
        };

        float curX = startX;
        StringBuilder activeStyle = ACTIVE_STYLE_BUILDER;
        activeStyle.setLength(0);
        int charIdx = 0;

        for (int i = 0; i < text.length(); ) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (code == 'r' || code == 'R') {
                    activeStyle.setLength(0);
                } else {
                    activeStyle.append('§').append(code);
                }
                i += 2;
                continue;
            }

            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String chStr = text.substring(i, i + charCount);
            int chWidth = LcdFontHelper.getWidth(font, chStr, fontStyle);

            float waveY = (float) (Math.sin((timeMs * freq) + (charIdx * 0.45)) * 2.2f);

            if (curX >= minX && (curX + chWidth) <= maxX) {
                String toRender = activeStyle.isEmpty() ? chStr : (activeStyle + chStr);
                FormattedCharSequence seq = LcdFontHelper.toFormatted(toRender, fontStyle);
                font.drawInBatch(seq, curX, textY + waveY, color, glow, matrix, bufferSource, false, 0, light);
            }

            curX += chWidth;
            charIdx++;
            i += charCount;
        }
    }

    private void drawRainbowFlowText(Font font, String text, float startX, float textY, float minX, float maxX,
                                     boolean glow, Matrix4f matrix, MultiBufferSource bufferSource, int light,
                                     int speed, long timeMs, int fontStyle) {
        double flowSpeed = switch (speed) {
            case 1 -> 0.0008;
            case 2 -> 0.0016;
            case 3 -> 0.0028;
            case 4 -> 0.0045;
            default -> 0.0016;
        };

        float curX = startX;
        StringBuilder activeStyle = ACTIVE_STYLE_BUILDER;
        activeStyle.setLength(0);
        int charIdx = 0;

        for (int i = 0; i < text.length(); ) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (code == 'r' || code == 'R') {
                    activeStyle.setLength(0);
                } else {
                    activeStyle.append('§').append(code);
                }
                i += 2;
                continue;
            }

            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String chStr = text.substring(i, i + charCount);
            int chWidth = LcdFontHelper.getWidth(font, chStr, fontStyle);

            float hue = (float) ((timeMs * flowSpeed + charIdx * 0.065) % 1.0);
            if (hue < 0) hue += 1.0f;
            int charColor = 0xFF000000 | (Color.HSBtoRGB(hue, 0.88f, 1.0f) & 0xFFFFFF);

            if (curX >= minX && (curX + chWidth) <= maxX) {
                String toRender = activeStyle.isEmpty() ? chStr : (activeStyle + chStr);
                FormattedCharSequence seq = LcdFontHelper.toFormatted(toRender, fontStyle);
                font.drawInBatch(seq, curX, textY, charColor, glow, matrix, bufferSource, false, 0, light);
            }

            curX += chWidth;
            charIdx++;
            i += charCount;
        }
    }

    private void drawClippedText(Font font, String text, float startX, float textY, float minX, float maxX,
                                 int color, boolean glow, Matrix4f matrix, MultiBufferSource bufferSource, int light, int fontStyle) {
        int totalWidth = LcdFontHelper.getWidth(font, text, fontStyle);
        if (startX >= minX && (startX + totalWidth) <= maxX) {
            FormattedCharSequence seq = LcdFontHelper.toFormatted(text, fontStyle);
            font.drawInBatch(seq, startX, textY, color, glow, matrix, bufferSource, false, 0, light);
            return;
        }

        float curX = startX;
        StringBuilder activeStyle = ACTIVE_STYLE_BUILDER;
        activeStyle.setLength(0);
        for (int i = 0; i < text.length(); ) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (code == 'r' || code == 'R') {
                    activeStyle.setLength(0);
                } else {
                    activeStyle.append('§').append(code);
                }
                i += 2;
                continue;
            }

            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String chStr = text.substring(i, i + charCount);
            int chWidth = LcdFontHelper.getWidth(font, chStr, fontStyle);

            if (curX >= minX && (curX + chWidth) <= maxX) {
                String toRender = activeStyle.isEmpty() ? chStr : (activeStyle + chStr);
                FormattedCharSequence seq = LcdFontHelper.toFormatted(toRender, fontStyle);
                font.drawInBatch(seq, curX, textY, color, glow, matrix, bufferSource, false, 0, light);
            }

            curX += chWidth;
            i += charCount;
        }
    }

    private void renderScreenBackground(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int theme, int customColor, boolean rainbow,
                                        float minX, float maxX, float topY, float bottomY, int light) {
        int r, g, b, a;
        int br, bg, bb, ba;
        switch (theme) {
            case 1 -> {
                r = 2; g = 38; b = 14; a = 220;
                br = 0; bg = 255; bb = 68; ba = 240;
            }
            case 2 -> {
                r = 46; g = 24; b = 3; a = 225;
                br = 255; bg = 170; bb = 0; ba = 240;
            }
            case 3 -> {
                r = 6; g = 28; b = 58; a = 225;
                br = 0; bg = 240; bb = 255; ba = 240;
            }
            case 4 -> {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 250.0);
                r = (int) (40 + pulse * 45);
                g = 4;
                b = 8;
                a = (int) (200 + pulse * 45);
                br = 255; bg = (int) (20 + pulse * 30); bb = 40; ba = 255;
            }
            case 5 -> {
                r = 28; g = 8; b = 38; a = 225;
                br = 255; bg = 20; bb = 160; ba = 245;
            }
            case 6 -> {
                r = 4; g = 18; b = 48; a = 230;
                br = 0; bg = 210; bb = 255; ba = 245;
            }
            case 7 -> {
                r = 30; g = 40; b = 14; a = 230;
                br = 140; bg = 165; bb = 35; ba = 240;
            }
            case 8 -> {
                r = 6; g = 26; b = 10; a = 230;
                br = 34; bg = 238; bb = 85; ba = 245;
            }
            case 9 -> {
                r = 4; g = 26; b = 42; a = 230;
                br = 130; bg = 235; bb = 255; ba = 250;
            }
            case 10 -> {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 280.0);
                r = (int) (35 + pulse * 25);
                g = (int) (10 + pulse * 12);
                b = 4;
                a = 235;
                br = 255; bg = (int) (70 + pulse * 50); bb = 0; ba = 255;
            }
            case 11 -> {
                r = 24; g = 6; b = 30; a = 235;
                br = 255; bg = 0; bb = 110; ba = 255;
            }
            case 12 -> {
                r = 232; g = 235; b = 230; a = 245;
                br = 50; bg = 56; bb = 64; ba = 230;
            }
            case 13 -> {
                r = 10; g = 20; b = 14; a = 235;
                br = 255; bg = 215; bb = 0; ba = 255;
            }
            case 14 -> {
                r = 28; g = 18; b = 10; a = 235;
                br = 205; bg = 135; bb = 45; ba = 250;
            }
            default -> {
                r = 8; g = 11; b = 16; a = 190;
                br = 35; bg = 55; bb = 75; ba = 180;
            }
        }

        VertexConsumer consumer = bufferSource.getBuffer(LcdRenderTypes.CANVAS);
        Matrix4f mat = poseStack.last().pose();

        float bgZ = 0.0f;
        float borderZ = 0.01f;
        consumer.vertex(mat, minX, bottomY, bgZ).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(mat, maxX, bottomY, bgZ).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(mat, maxX, topY, bgZ).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(mat, minX, topY, bgZ).color(r, g, b, a).uv2(light).endVertex();

        float bw = 0.65f;
        consumer.vertex(mat, minX + bw, topY + bw, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX - bw, topY + bw, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX - bw, topY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, minX + bw, topY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();

        consumer.vertex(mat, minX + bw, bottomY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX - bw, bottomY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX - bw, bottomY - bw, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, minX + bw, bottomY - bw, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();

        consumer.vertex(mat, minX, bottomY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, minX + bw, bottomY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, minX + bw, topY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, minX, topY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();

        consumer.vertex(mat, maxX - bw, bottomY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX, bottomY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX, topY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
        consumer.vertex(mat, maxX - bw, topY, borderZ).color(br, bg, bb, ba).uv2(light).endVertex();
    }
}
