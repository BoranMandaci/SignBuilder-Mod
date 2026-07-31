package com.boran.signbuilder;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.item.ModCreativeModeTabs;
import com.boran.signbuilder.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SignBuilder.MODID)
public class SignBuilder {

    public static final String MODID = "signbuilder";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SignBuilder() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}