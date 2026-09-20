package com.boran.signbuilder.forge;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.render.BlueprintPreviewRenderer;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.render.ModColorHandlers;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("signbuilder")
public class SignBuilderForge {
    public SignBuilderForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus("signbuilder", modEventBus);
        SignBuilder.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(this::registerRenderers);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderLevelStage);
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModColorHandlers::register);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            BlueprintPreviewRenderer.render(event.getPoseStack());
        }
    }
}