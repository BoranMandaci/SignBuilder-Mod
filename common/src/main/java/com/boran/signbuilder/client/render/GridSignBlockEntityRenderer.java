package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.client.render.cache.GridSignRenderCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.AttachFace;

import java.awt.Color;
import java.util.List;

public class GridSignBlockEntityRenderer implements BlockEntityRenderer<GridSignBlockEntity> {

    public GridSignBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox(GridSignBlockEntity blockEntity) {
        if (blockEntity.renderBoundingBox == null) {
            blockEntity.renderBoundingBox = new net.minecraft.world.phys.AABB(blockEntity.getBlockPos()).inflate(1.0);
        }
        return blockEntity.renderBoundingBox;
    }

    public net.minecraft.world.phys.AABB getViewBoundingBox(GridSignBlockEntity blockEntity) {
        return getRenderBoundingBox(blockEntity);
    }


    @Override
    public void render(GridSignBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (entity.getLevel() == null) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        GridSignRenderCache cache = GridSignRenderCache.getOrCreate(entity, dispatcher);
        if (cache == null) return;

        poseStack.pushPose();
        if (cache.rotationDegrees != 0) {
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(cache.rotationDegrees));
            poseStack.translate(-0.5, 0.0, -0.5);
        }

        int activeLight = (entity.getWrenchMode() == 10 || (entity.getWrenchMode() == 11 && entity.isCustomLightLowPower()))
                ? ((packedLight & 0xFFFF0000) | (6 << 4))
                : 15728880;
        int plateLight = entity.isActive() ? activeLight : packedLight;

        if (cache.hasBackplate && cache.plateState != null) {
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();
            int currentRainbow = 0xFFFFFF;
            if (entity.isBackplateFrontRainbow() || entity.isBackplateBackRainbow() || (cache.hasBackGlyph && cache.backRainbow) || (cache.hasFrontGlyph && cache.frontRainbow)) {
                float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                currentRainbow = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            }
            int fColor = (cache.fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (cache.bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            if (cache.face != AttachFace.WALL) {
                poseStack.translate(cache.plateCenterLocalX, 0.0, cache.plateCenterLocalZ);
                if (cache.widenDiagonal) {
                    ModelCentering.widenDiagonalPose(poseStack, cache.plateFacing, entity.getFacingRotation());
                }
            }

            VertexConsumer plateBuffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(cache.plateState, false));
            int fLight = (entity.isBackplateFrontGlowInked() || entity.isActive()) ? 15728880 : plateLight;
            int bLight = (entity.isBackplateBackGlowInked() || entity.isActive()) ? 15728880 : plateLight;
            LetterBlockEntityRenderer.renderPlateQuads(poseStack.last(), plateBuffer, cache.plateQuads, fColor, bColor, fLight, bLight, packedOverlay);
            poseStack.popPose();
        }

        int currentRainbow = 0;
        boolean hasRainbowCalculated = false;

        List<GridSignRenderCache.CachedCell> cells = cache.cells;
        int cellSize = cells.size();
        for (int i = 0; i < cellSize; i++) {
            GridSignRenderCache.CachedCell cell = cells.get(i);
            VertexConsumer buffer = bufferSource.getBuffer(cell.renderType);

            int color = cell.rawColor;
            if (cell.isRainbow) {
                if (!hasRainbowCalculated) {
                    float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                    currentRainbow = Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    hasRainbowCalculated = true;
                }
                color = currentRainbow;
            }
            if (cell.isMaterial) {
                color = 0xFFFFFF;
            }

            poseStack.pushPose();
            poseStack.translate(cell.totalTransX, cell.totalTransY, cell.totalTransZ);
            poseStack.translate(cell.pivotX, cell.pivotY, cell.pivotZ);
            poseStack.scale(cell.scale, cell.scale, cell.scale);
            poseStack.translate(-cell.pivotX, -cell.pivotY, -cell.pivotZ);

            int cellLight = (cell.glowInked || entity.isActive()) ? 15728880 : plateLight;
            LetterBlockEntityRenderer.renderPlateQuads(
                    poseStack.last(),
                    buffer,
                    cell.quads,
                    color, color,
                    cellLight, cellLight,
                    packedOverlay
            );

            poseStack.popPose();
        }

        if (cache.hasBackGlyph && cache.backQuads != null && !cache.backQuads.isEmpty()) {
            VertexConsumer buffer = bufferSource.getBuffer(cache.backRenderType);
            int color = cache.backRawColor;
            if (cache.backRainbow) {
                if (!hasRainbowCalculated) {
                    float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                    currentRainbow = Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    hasRainbowCalculated = true;
                }
                color = currentRainbow;
            }
            if (cache.backIsMaterial) {
                color = 0xFFFFFF;
            }

            poseStack.pushPose();
            poseStack.translate(cache.backTransX, cache.backTransY, cache.backTransZ);
            int backLight = (cache.backGlowInked || entity.isActive()) ? 15728880 : plateLight;
            LetterBlockEntityRenderer.renderPlateQuads(
                    poseStack.last(),
                    buffer,
                    cache.backQuads,
                    color, color,
                    backLight, backLight,
                    packedOverlay
            );
            poseStack.popPose();
        }

        if (cache.hasFrontGlyph && cache.frontQuads != null && !cache.frontQuads.isEmpty()) {
            VertexConsumer buffer = bufferSource.getBuffer(cache.frontRenderType);
            int color = cache.frontRawColor;
            if (cache.frontRainbow) {
                if (!hasRainbowCalculated) {
                    float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                    currentRainbow = Color.HSBtoRGB(hue, 1.0f, 1.0f);
                    hasRainbowCalculated = true;
                }
                color = currentRainbow;
            }
            if (cache.frontIsMaterial) {
                color = 0xFFFFFF;
            }

            poseStack.pushPose();
            poseStack.translate(cache.frontTransX, cache.frontTransY, cache.frontTransZ);
            int frontLight = (cache.frontGlowInked || entity.isActive()) ? 15728880 : plateLight;
            LetterBlockEntityRenderer.renderPlateQuads(
                    poseStack.last(),
                    buffer,
                    cache.frontQuads,
                    color, color,
                    frontLight, frontLight,
                    packedOverlay
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
