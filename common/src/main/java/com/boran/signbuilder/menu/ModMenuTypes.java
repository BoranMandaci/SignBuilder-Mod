package com.boran.signbuilder.menu;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create("signbuilder", Registries.MENU);

    public static final RegistrySupplier<MenuType<SignPressMenu>> SIGN_PRESS_MENU =
            MENUS.register("sign_press_menu", () -> MenuRegistry.ofExtended(SignPressMenu::new));

    public static void register() {
        MENUS.register();
    }
}