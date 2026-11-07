package com.boran.signbuilder.fabric;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.render.BlueprintPreviewRenderer;
import com.boran.signbuilder.client.render.GridSignBlockEntityRenderer;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.render.ModColorHandlers;
import com.boran.signbuilder.client.render.SignBuilderBakedModel;
import com.boran.signbuilder.client.render.StorageHighlightRenderer;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class SignBuilderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.boran.signbuilder.client.ClientModEvents.init();
        ModColorHandlers.register();

        net.minecraft.client.gui.screens.MenuScreens.register(com.boran.signbuilder.menu.ModMenuTypes.SIGN_PRESS_MENU.get(), com.boran.signbuilder.client.screen.SignPressScreen::new);

        BlockEntityRendererRegistry.register(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.GRID_SIGN_BLOCK_ENTITY.get(), GridSignBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.LCD_SCREEN_BLOCK_ENTITY.get(), com.boran.signbuilder.client.render.LcdScreenBlockEntityRenderer::new);

        for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            BlockRenderLayerMap.INSTANCE.putBlock(blockSupplier.get(), RenderType.cutout());
        }
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BACKPLATE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRID_SIGN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LCD_SCREEN.get(), RenderType.cutout());

        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.modifyModelAfterBake().register((model, context) -> {
                ModelResourceLocation topLevel = context.topLevelId();
                if (topLevel != null && "signbuilder".equals(topLevel.id().getNamespace()) && "inventory".equals(topLevel.variant())) {
                    String path = topLevel.id().getPath();
                    if ("backplate".equals(path)) {
                        return new SignBuilderBakedModel(model, true);
                    }
                    for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
                        if (path.equals(blockSupplier.getId().getPath())) {
                            return new SignBuilderBakedModel(model, false);
                        }
                    }
                }

                ResourceLocation resId = context.resourceId();
                if (resId != null && "signbuilder".equals(resId.getNamespace())) {
                    String path = resId.getPath();
                    if ("item/backplate".equals(path)) {
                        return new SignBuilderBakedModel(model, true);
                    }
                    if (path.startsWith("item/")) {
                        String sub = path.substring(5);
                        for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
                            if (sub.equals(blockSupplier.getId().getPath())) {
                                return new SignBuilderBakedModel(model, false);
                            }
                        }
                    }
                }
                return model;
            });
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            BlueprintPreviewRenderer.render(context.matrixStack());
            com.boran.signbuilder.client.render.GridPreviewRenderer.render(context.matrixStack());
            StorageHighlightRenderer.render(context.matrixStack());
        });
    }
}
