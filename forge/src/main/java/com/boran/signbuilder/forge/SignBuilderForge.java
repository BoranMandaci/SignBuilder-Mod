package com.boran.signbuilder.forge;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.client.ModColorHandlers;
import com.boran.signbuilder.client.render.MaterialBakedModel;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("signbuilder")
public class SignBuilderForge {

    public SignBuilderForge() {
        EventBuses.registerModEventBus("signbuilder", FMLJavaModLoadingContext.get().getModEventBus());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(SignBuilderForge::onModelBake);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(SignBuilderForge::onRegisterItemColors);
        }

        SignBuilder.init();
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        for (ResourceLocation id : event.getModels().keySet()) {
            if (id.getNamespace().equals("signbuilder") &&
                    (id.getPath().startsWith("letter_") || id.getPath().startsWith("number_") ||
                            id.getPath().startsWith("symbol_") || id.getPath().startsWith("arrow_") ||
                            id.getPath().startsWith("backplate"))) {

                BakedModel original = event.getModels().get(id);
                event.getModels().put(id, new MaterialBakedModel(original));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        for (RegistrySupplier<Block> blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            event.register(ModColorHandlers::getItemColor, blockSupplier.get());
        }
        event.register(ModColorHandlers::getItemColor, ModBlocks.BACKPLATE_ITEM.get());
    }
}