package com.boran.signbuilder.fabric;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.render.ModColorHandlers;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class SignBuilderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModColorHandlers.register();

        BlockEntityRendererRegistry.register(
                ModBlockEntities.LETTER_BLOCK_ENTITY.get(),
                LetterBlockEntityRenderer::new
        );

        for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            BlockRenderLayerMap.INSTANCE.putBlock(blockSupplier.get(), RenderType.cutout());
        }
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BACKPLATE.get(), RenderType.cutout());
    }
}