package com.boran.signbuilder.neoforge;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.entity.ModBlockEntities;
import com.boran.signbuilder.client.render.BlueprintPreviewRenderer;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.render.ModColorHandlers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("signbuilder")
public class SignBuilderNeoForge {
    public SignBuilderNeoForge(IEventBus modEventBus) {
        SignBuilder.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.boran.signbuilder.client.ClientModEvents.init();
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(this::registerRenderers);
            modEventBus.addListener(this::registerScreens);
            NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModColorHandlers::register);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), LetterBlockEntityRenderer::new);
    }

    private void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.boran.signbuilder.menu.ModMenuTypes.SIGN_PRESS_MENU.get(), com.boran.signbuilder.client.screen.SignPressScreen::new);
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            BlueprintPreviewRenderer.render(event.getPoseStack());
            com.boran.signbuilder.client.render.GridPreviewRenderer.render(event.getPoseStack());
        }
    }
}
