package com.boran.signbuilder.item;

import com.boran.signbuilder.SignBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SignBuilder.MODID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().tab(ModCreativeModeTabs.LETTERS_TAB).defaultDurability(256)));

    public static final RegistrySupplier<Item> SIGN_BLUEPRINT = ITEMS.register("sign_blueprint",
            () -> new SignBlueprintItem(new Item.Properties().tab(ModCreativeModeTabs.LETTERS_TAB).stacksTo(1)));

    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().tab(ModCreativeModeTabs.LETTERS_TAB).defaultDurability(256)));

    public static void register() {
        ITEMS.register();
    }
}
