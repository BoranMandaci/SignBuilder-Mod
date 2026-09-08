package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

public class LetterBlockEntityRenderer implements BlockEntityRenderer<LetterBlockEntity> {

    public LetterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    private int calculateRainbowColor() {
        float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
        return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
    }

    @Override
    public void render(LetterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        if (entity.getLevel() == null) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        int light = entity.isActive() ? 15728880 : LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos());
        int currentRainbow = calculateRainbowColor();

        if (state.getBlock() instanceof BackplateBlock) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();

            int fColor = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            BakedModel baseModel = dispatcher.getBlockModel(state);
            BakedModel finalModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? new MaterialBakedModel(baseModel, fMat, bMat)
                    : baseModel;

            renderPlateQuads(poseStack.last(), buffer, finalModel, state, fColor, bColor, light, packedOverlay);
            poseStack.popPose();
            return;
        }

        AttachFace face = state.hasProperty(BlockStateProperties.ATTACH_FACE) ? state.getValue(BlockStateProperties.ATTACH_FACE) : AttachFace.WALL;
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;

        if (entity.hasBackplate()) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();

            int fColor = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            Direction plateFacing = (face == AttachFace.FLOOR) ? facing.getCounterClockWise() : facing;

            BlockState plateState = ModBlocks.BACKPLATE.get().defaultBlockState()
                    .setValue(BackplateBlock.FACING, plateFacing)
                    .setValue(BackplateBlock.FACE, face);

            BakedModel basePlateModel = dispatcher.getBlockModel(plateState);
            BakedModel finalPlateModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? new MaterialBakedModel(basePlateModel, fMat, bMat)
                    : basePlateModel;

            renderPlateQuads(poseStack.last(), buffer, finalPlateModel, plateState, fColor, bColor, light, packedOverlay);
            poseStack.popPose();
        }

        poseStack.pushPose();
        if (entity.hasBackplate() && face == AttachFace.WALL) {
            float onePixel = 0.0625f;
            poseStack.translate(facing.getStepX() * onePixel, 0, facing.getStepZ() * onePixel);
        }

        BakedModel letterModel = dispatcher.getBlockModel(state);
        SignMaterial letterMat = state.hasProperty(LetterBlock.MATERIAL) ? state.getValue(LetterBlock.MATERIAL) : SignMaterial.DEFAULT;
        int rawLetterCol = entity.getRgbColor();
        int letterCol = (letterMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isRainbow() ? currentRainbow : (rawLetterCol == 0 ? 0xFFFFFF : rawLetterCol));

        BakedModel finalLetterModel = (letterMat != SignMaterial.DEFAULT)
                ? new MaterialBakedModel(letterModel, letterMat, SignMaterial.DEFAULT)
                : letterModel;

        float lr = ((letterCol >> 16) & 0xFF) / 255.0f;
        float lg = ((letterCol >> 8) & 0xFF) / 255.0f;
        float lb = (letterCol & 0xFF) / 255.0f;

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                state,
                finalLetterModel,
                lr, lg, lb,
                light,
                packedOverlay
        );
        poseStack.popPose();
    }

    private void renderPlateQuads(PoseStack.Pose pose, VertexConsumer buffer, BakedModel model, BlockState state, int frontColor, int backColor, int light, int overlay) {
        RandomSource random = RandomSource.create(42L);
        for (Direction dir : Direction.values()) {
            renderPlateQuadList(pose, buffer, model.getQuads(state, dir, random), frontColor, backColor, light, overlay);
        }
        renderPlateQuadList(pose, buffer, model.getQuads(state, null, random), frontColor, backColor, light, overlay);
    }

    private void renderPlateQuadList(PoseStack.Pose pose, VertexConsumer buffer, List<BakedQuad> quads, int frontColor, int backColor, int light, int overlay) {
        for (BakedQuad quad : quads) {
            int color = 0xFFFFFF;
            if (quad.getTintIndex() == 0) {
                color = frontColor;
            } else if (quad.getTintIndex() == 1) {
                color = backColor;
            }

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            buffer.putBulkData(pose, quad, r, g, b, light, overlay);
        }
    }
}