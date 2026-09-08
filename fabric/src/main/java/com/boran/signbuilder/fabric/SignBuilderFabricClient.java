package com.boran.signbuilder.fabric;

import com.boran.signbuilder.client.ModColorHandlers;
import com.boran.signbuilder.client.render.MaterialBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.resources.ResourceLocation;

public class SignBuilderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModColorHandlers.register();

        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (originalModel, context) -> {
                ResourceLocation id = context.id();
                if (id != null && id.getNamespace().equals("signbuilder")) {
                    return new MaterialBakedModel(originalModel);
                }
                return originalModel;
            });
        });
    }
}