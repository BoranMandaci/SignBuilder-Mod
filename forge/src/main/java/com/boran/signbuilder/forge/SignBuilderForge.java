package com.boran.signbuilder.forge;

import com.boran.signbuilder.SignBuilder;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("signbuilder")
public class SignBuilderForge {

    public SignBuilderForge() {
        EventBuses.registerModEventBus("signbuilder", FMLJavaModLoadingContext.get().getModEventBus());

        SignBuilder.init();

        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientEvents::onModelBake);

            com.boran.signbuilder.client.render.ModColorHandlers.register();
        });
    }

    public static class ClientEvents {
        public static void onModelBake(net.minecraftforge.client.event.ModelEvent.ModifyBakingResult event) {
            for (net.minecraft.resources.ResourceLocation id : event.getModels().keySet()) {
                if (id.getNamespace().equals("signbuilder") &&
                        (id.getPath().startsWith("letter_") || id.getPath().startsWith("number_") ||
                                id.getPath().startsWith("symbol_") || id.getPath().startsWith("arrow_"))) {

                    net.minecraft.client.resources.model.BakedModel original = event.getModels().get(id);
                    event.getModels().put(id, new com.boran.signbuilder.client.render.MaterialBakedModel(original));
                }
            }
        }
    }
}