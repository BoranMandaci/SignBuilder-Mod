package com.boran.signbuilder.fabric;

import com.boran.signbuilder.client.render.ModColorHandlers;
import net.fabricmc.api.ClientModInitializer;

public class SignBuilderFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModColorHandlers.register();
    }
}