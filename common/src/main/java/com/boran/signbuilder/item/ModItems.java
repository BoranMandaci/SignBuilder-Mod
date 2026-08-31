package com.boran.signbuilder.item;

import com.boran.signbuilder.SignBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SignBuilder.MODID, Registries.ITEM);

    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().defaultDurability(256)));

    public static final RegistrySupplier<Item> SIGN_BLUEPRINT = ITEMS.register("sign_blueprint",
            () -> new SignBlueprintItem(new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().defaultDurability(256)));

    public static void register() {
        ITEMS.register();
    }
}