package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.client.render.cache.GridSignRenderCache;
import com.boran.signbuilder.util.RainbowColorHelper;
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

        int currentRainbow = RainbowColorHelper.getRainbowRgb();

        if (cache.hasBackplate && cache.plateState != null) {
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();
            int fColor = (cache.fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (cache.bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            if (cache.face != AttachFace.WALL) {
                poseStack.translate(cache.plateCenterLocalX, 0.0, cache.plateCenterLocalZ);
                if (cache.widenDiagonal) {
                    ModelCentering.widenDiagonalPose(poseStack, cache.plateFacing, entity.getFacingRotation());
                }
            }

            VertexConsumer plateBuffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(cache.plateState));
            int fLight = (entity.isBackplateFrontGlowInked() || entity.isActive()) ? 15728880 : plateLight;
            int bLight = (entity.isBackplateBackGlowInked() || entity.isActive()) ? 15728880 : plateLight;
            LetterBlockEntityRenderer.renderPlateQuads(poseStack.last(), plateBuffer, cache.plateQuads, fColor, bColor, fLight, bLight, packedOverlay);
            poseStack.popPose();
        }

        List<GridSignRenderCache.CachedCell> cells = cache.cells;
        int cellSize = cells.size();
        for (int i = 0; i < cellSize; i++) {
            GridSignRenderCache.CachedCell cell = cells.get(i);
            VertexConsumer buffer = bufferSource.getBuffer(cell.renderType);

            int color = cell.rawColor;
            if (cell.isRainbow) {
                color = currentRainbow;
            }
            if (cell.isMaterial) {
                color = 0xFFFFFF;
            }

            float lr = ((color >> 16) & 0xFF) / 255.0f;
            float lg = ((color >> 8) & 0xFF) / 255.0f;
            float lb = (color & 0xFF) / 255.0f;

            poseStack.pushPose();
            poseStack.translate(cell.prebakedTransX, cell.prebakedTransY, cell.prebakedTransZ);
            poseStack.scale(cell.scale, cell.scale, cell.scale);

            int cellLight = (cell.glowInked || entity.isActive()) ? 15728880 : plateLight;
            dispatcher.getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer,
                    cell.letterState,
                    cell.finalLetterModel,
                    lr, lg, lb,
                    cellLight,
                    packedOverlay
            );

            poseStack.popPose();
        }

        if (cache.hasBackGlyph && cache.finalBackModel != null) {
            VertexConsumer buffer = bufferSource.getBuffer(cache.backRenderType);
            int color = cache.backRawColor;
            if (cache.backRainbow) {
                color = currentRainbow;
            }
            if (cache.backIsMaterial) {
                color = 0xFFFFFF;
            }

            float lr = ((color >> 16) & 0xFF) / 255.0f;
            float lg = ((color >> 8) & 0xFF) / 255.0f;
            float lb = (color & 0xFF) / 255.0f;

            poseStack.pushPose();
            poseStack.translate(cache.backTransX, cache.backTransY, cache.backTransZ);
            int backLight = (cache.backGlowInked || entity.isActive()) ? 15728880 : plateLight;
            dispatcher.getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer,
                    cache.backState,
                    cache.finalBackModel,
                    lr, lg, lb,
                    backLight,
                    packedOverlay
            );
            poseStack.popPose();
        }

        if (cache.hasFrontGlyph && cache.finalFrontModel != null) {
            VertexConsumer buffer = bufferSource.getBuffer(cache.frontRenderType);
            int color = cache.frontRawColor;
            if (cache.frontRainbow) {
                color = currentRainbow;
            }
            if (cache.frontIsMaterial) {
                color = 0xFFFFFF;
            }

            float lr = ((color >> 16) & 0xFF) / 255.0f;
            float lg = ((color >> 8) & 0xFF) / 255.0f;
            float lb = (color & 0xFF) / 255.0f;

            poseStack.pushPose();
            poseStack.translate(cache.frontTransX, cache.frontTransY, cache.frontTransZ);
            int frontLight = (cache.frontGlowInked || entity.isActive()) ? 15728880 : plateLight;
            dispatcher.getModelRenderer().renderModel(
                    poseStack.last(),
                    buffer,
                    cache.frontState,
                    cache.finalFrontModel,
                    lr, lg, lb,
                    frontLight,
                    packedOverlay
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
