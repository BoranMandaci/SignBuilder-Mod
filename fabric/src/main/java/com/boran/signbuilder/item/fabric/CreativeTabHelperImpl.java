package com.boran.signbuilder.item.fabric;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreativeTabHelperImpl {
    public static CreativeModeTab create(ResourceLocation name, Supplier<ItemStack> icon, Consumer<NonNullList<ItemStack>> itemsAppender) {
        return FabricItemGroupBuilder.create(name)
                .icon(icon)
                .appendItems(list -> {
                    NonNullList<ItemStack> nonNullList = NonNullList.create();
                    itemsAppender.accept(nonNullList);
                    list.addAll(nonNullList);
                })
                .build();
    }
}
