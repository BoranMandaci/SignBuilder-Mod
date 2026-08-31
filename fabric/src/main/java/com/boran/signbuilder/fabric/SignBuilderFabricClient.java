package com.boran.signbuilder.fabric;

import com.boran.signbuilder.client.render.MaterialBakedModel;
import com.boran.signbuilder.client.render.ModColorHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

public class SignBuilderFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.modifyModelAfterBake().register((originalModel, context) -> {
                ResourceLocation id = context.id();
                if (id != null && id.getNamespace().equals("signbuilder")) {
                    String path = id.getPath();
                    if (path.startsWith("letter_") || path.startsWith("number_") ||
                            path.startsWith("symbol_") || path.startsWith("arrow_")) {
                        return new MaterialBakedModel(originalModel);
                    }
                }
                return originalModel;
            });
        });

        ModColorHandlers.register();

    }
}