package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.client.render.cache.GridSignRenderCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
    public void render(GridSignBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (entity.getLevel() == null) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        GridSignRenderCache cache = GridSignRenderCache.getOrCreate(entity, dispatcher);
        if (cache == null) return;

        poseStack.pushPose();
        if (cache.rotationDegrees != 0) {
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(cache.rotationDegrees));
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
            if (entity.isBackplateFrontRainbow() || entity.isBackplateBackRainbow()) {
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
            LetterBlockEntityRenderer.renderPlateQuads(poseStack.last(), plateBuffer, cache.plateQuads, fColor, bColor, plateLight, packedOverlay);
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

            float lr = ((color >> 16) & 0xFF) / 255.0f;
            float lg = ((color >> 8) & 0xFF) / 255.0f;
            float lb = (color & 0xFF) / 255.0f;

            poseStack.pushPose();
            poseStack.translate(cell.totalTransX, cell.totalTransY, cell.totalTransZ);
            poseStack.translate(cell.pivotX, cell.pivotY, cell.pivotZ);
            poseStack.scale(cell.scale, cell.scale, cell.scale);
            poseStack.translate(-cell.pivotX, -cell.pivotY, -cell.pivotZ);

            dispatcher.getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer,
                    cell.letterState,
                    cell.finalLetterModel,
                    lr, lg, lb,
                    plateLight,
                    packedOverlay
            );

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
