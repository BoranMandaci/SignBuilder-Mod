package com.boran.signbuilder.forge;

import com.boran.signbuilder.SignBuilder;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("signbuilder")
public class SignBuilderForge {
    public SignBuilderForge() {
        EventBuses.registerModEventBus("signbuilder", FMLJavaModLoadingContext.get().getModEventBus());

        SignBuilder.init();
    }
}