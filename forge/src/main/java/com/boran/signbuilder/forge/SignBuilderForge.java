package com.boran.signbuilder.forge;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.client.render.ModColorHandlers;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
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
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModColorHandlers::register);
    }
}