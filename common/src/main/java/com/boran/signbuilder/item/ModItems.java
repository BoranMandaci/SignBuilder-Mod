package com.boran.signbuilder.item;

import com.boran.signbuilder.SignBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(SignBuilder.MODID, Registries.ITEM);

    public static final RegistrySupplier<Item> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().durability(256).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SignBuilder.MODID, "paint_brush")))));

    public static final RegistrySupplier<Item> SIGN_BLUEPRINT = ITEMS.register("sign_blueprint",
            () -> new SignBlueprintItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SignBuilder.MODID, "sign_blueprint")))));

    public static final RegistrySupplier<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().durability(256).setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SignBuilder.MODID, "wrench")))));

    public static void register() {
        ITEMS.register();
    }
}
