package com.boran.signbuilder.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreativeTabHelper {
    @ExpectPlatform
    public static CreativeModeTab create(ResourceLocation name, Supplier<ItemStack> icon, Consumer<NonNullList<ItemStack>> itemsAppender) {
        throw new AssertionError();
    }
}
