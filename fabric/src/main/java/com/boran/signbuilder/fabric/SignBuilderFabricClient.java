package com.boran.signbuilder.fabric;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.client.render.BlueprintPreviewRenderer;
import com.boran.signbuilder.client.render.GridPreviewRenderer;
import com.boran.signbuilder.client.render.ModColorHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.RenderType;

public class SignBuilderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModColorHandlers.register();

        for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            BlockRenderLayerMap.INSTANCE.putBlock(blockSupplier.get(), RenderType.cutout());
        }
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BACKPLATE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRID_SIGN.get(), RenderType.cutout());

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            BlueprintPreviewRenderer.render(context.matrixStack());
            GridPreviewRenderer.render(context.matrixStack());
        });
    }
}