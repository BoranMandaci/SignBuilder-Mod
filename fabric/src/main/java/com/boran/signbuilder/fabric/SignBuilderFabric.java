package com.boran.signbuilder.fabric;

import com.boran.signbuilder.SignBuilder;
import net.fabricmc.api.ModInitializer;

public class SignBuilderFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SignBuilder.init();
    }
}
