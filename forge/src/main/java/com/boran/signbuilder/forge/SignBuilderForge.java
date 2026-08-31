package com.boran.signbuilder.forge;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.client.render.ModColorHandlers;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("signbuilder")
public class SignBuilderForge {

    public SignBuilderForge() {
        EventBuses.registerModEventBus("signbuilder", FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(SignBuilderForge::onModelBake);

        SignBuilder.init();

        ModColorHandlers.register();
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
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