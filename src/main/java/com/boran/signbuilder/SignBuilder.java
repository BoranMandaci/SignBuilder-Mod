package com.boran.signbuilder;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.item.ModCreativeModeTabs;
import com.boran.signbuilder.item.ModItems;
import com.boran.signbuilder.network.ModMessages;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
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

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::registerBlockColors);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
        });
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (RegistryObject<Block> block : ModBlocks.BLOCKS.getEntries()) {
            event.register((state, level, pos, tintIndex) -> {
                if (state.hasProperty(ModBlocks.COLOR)) {
                    int colorIndex = state.getValue(ModBlocks.COLOR);
                    return getColorHex(colorIndex);
                }
                return 0xFFFFFF;
            }, block.get());
        }
    }

    private int getColorHex(int index) {
        return switch (index) {
            case 0 -> 0xFFFFFF; case 1 -> 0xD87F33; case 2 -> 0xB24CD8; case 3 -> 0x6699D8;
            case 4 -> 0xE5E533; case 5 -> 0x7FCC19; case 6 -> 0xF27FA5; case 7 -> 0x4C4C4C;
            case 8 -> 0x999999; case 9 -> 0x4C7F99; case 10 -> 0x7F3FB2; case 11 -> 0x334CB2;
            case 12 -> 0x664C33; case 13 -> 0x667F33; case 14 -> 0x993333; case 15 -> 0x191919;
            default -> 0xFFFFFF;
        };
    }
}