package com.boran.signbuilder.item.forge;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreativeTabHelperImpl {
    public static CreativeModeTab create(ResourceLocation name, Supplier<ItemStack> icon, Consumer<NonNullList<ItemStack>> itemsAppender) {
        return new CreativeModeTab(String.format("%s.%s", name.getNamespace(), name.getPath())) {
            @Override
            @NotNull
            public ItemStack makeIcon() {
                return icon.get();
            }

            @Override
            public void fillItemList(@NotNull NonNullList<ItemStack> items) {
                itemsAppender.accept(items);
            }
        };
    }
}
