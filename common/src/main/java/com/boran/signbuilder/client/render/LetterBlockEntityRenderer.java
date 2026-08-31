package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BakedModel;

public class LetterBlockEntityRenderer implements BlockEntityRenderer<LetterBlockEntity> {

    public LetterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LetterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = entity.getBlockState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        BakedModel model = dispatcher.getBlockModel(state);

        int color = entity.getRgbColor();
        if (entity.isRainbow()) {
            float hue = (entity.getLevel().getGameTime() % 120) / 120f;
            color = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
        }

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        int light = entity.isActive() ? 15728880 : packedLight;

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()),
                state,
                model,
                r, g, b,
                light,
                packedOverlay
        );
    }
}
