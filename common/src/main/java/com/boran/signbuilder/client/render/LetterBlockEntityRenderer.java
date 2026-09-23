package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class LetterBlockEntityRenderer implements BlockEntityRenderer<LetterBlockEntity> {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final RandomSource RANDOM = RandomSource.create();

    public LetterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    private int calculateRainbowColor() {
        float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
        return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRenderOffScreen(LetterBlockEntity blockEntity) {
        return blockEntity.getSize() > 1;
    }

    @Override
    public void render(LetterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (entity.isDummy()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (entity.getLevel() == null) return;

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, false);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

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

        poseStack.pushPose();

        if (entity.getSize() == 3) {
            if (face == AttachFace.WALL) {
                switch (facing) {
                    case NORTH -> poseStack.translate(-2.0, 0.0, -2.0);
                    case SOUTH -> poseStack.translate(0.0, 0.0, 0.0);
                    case EAST  -> poseStack.translate(0.0, 0.0, -2.0);
                    case WEST  -> poseStack.translate(-2.0, 0.0, 0.0);
                }
            } else {
                switch (facing) {
                    case EAST  -> poseStack.translate(0.0, 0.0, -1.375);
                    case NORTH -> poseStack.translate(-1.375, 0.0, 0.0);
                    case SOUTH -> poseStack.translate(-0.625, 0.0, 0.0);
                    case WEST  -> poseStack.translate(0.0, 0.0, -0.625);
                }
            }
            poseStack.scale(3.0F, 3.0F, 3.0F);
        } else if (entity.getSize() == 2) {
            if (face == AttachFace.WALL) {
                switch (facing) {
                    case NORTH -> poseStack.translate(-1.0, 0.0, -1.0);
                    case SOUTH -> poseStack.translate(0.0, 0.0, 0.0);
                    case EAST  -> poseStack.translate(0.0, 0.0, -1.0);
                    case WEST  -> poseStack.translate(-1.0, 0.0, 0.0);
                }
            } else {
                switch (facing) {
                    case EAST  -> poseStack.translate(0.0, 0.0, -0.6875);
                    case NORTH -> poseStack.translate(-0.6875, 0.0, 0.0);
                    case SOUTH -> poseStack.translate(-0.3125, 0.0, 0.0);
                    case WEST  -> poseStack.translate(0.0, 0.0, -0.3125);
                }
            }
            poseStack.scale(2.0F, 2.0F, 2.0F);
        }

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

        if (entity.isPressed() && face == AttachFace.WALL) {
            float wallPlane = (facing == Direction.NORTH || facing == Direction.WEST) ? 1.0f : 0.0f;
            float depthScale = 2.0f / 3.0f;
            if (facing.getAxis() == Direction.Axis.Z) {
                poseStack.translate(0, 0, wallPlane);
                poseStack.scale(1.0f, 1.0f, depthScale);
                poseStack.translate(0, 0, -wallPlane);
            } else {
                poseStack.translate(wallPlane, 0, 0);
                poseStack.scale(depthScale, 1.0f, 1.0f);
                poseStack.translate(-wallPlane, 0, 0);
            }
        }

        if (entity.hasBackplate() && face == AttachFace.WALL) {
            float onePixel = 0.0625f;
            poseStack.translate(facing.getStepX() * onePixel, 0, facing.getStepZ() * onePixel);
        }

        BakedModel letterModel = dispatcher.getBlockModel(state);
        SignMaterial letterMat = entity.getSavedMaterial();
        int rawLetterCol = entity.getRgbColor();
        int letterCol = (letterMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isRainbow() ? currentRainbow : (rawLetterCol == 0 ? 0xFFFFFF : rawLetterCol));

        BakedModel finalLetterModel = (letterMat != SignMaterial.DEFAULT)
                ? new MaterialBakedModel(letterModel, letterMat, SignMaterial.DEFAULT, true)
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
        poseStack.popPose();
    }

    private void renderPlateQuads(PoseStack.Pose pose, VertexConsumer buffer, BakedModel model, BlockState state, int frontColor, int backColor, int light, int overlay) {
        for (int i = 0; i < 6; i++) {
            RANDOM.setSeed(42L);
            renderPlateQuadList(pose, buffer, model.getQuads(state, DIRECTIONS[i], RANDOM), frontColor, backColor, light, overlay);
        }
        RANDOM.setSeed(42L);
        renderPlateQuadList(pose, buffer, model.getQuads(state, null, RANDOM), frontColor, backColor, light, overlay);
    }

    private void renderPlateQuadList(PoseStack.Pose pose, VertexConsumer buffer, List<BakedQuad> quads, int frontColor, int backColor, int light, int overlay) {
        int quadCount = quads.size();
        for (int i = 0; i < quadCount; i++) {
            BakedQuad quad = quads.get(i);
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