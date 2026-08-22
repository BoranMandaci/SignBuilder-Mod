package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SignBuilder.MODID);

    public static final RegistryObject<BlockEntityType<LetterBlockEntity>> LETTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("letter_block_entity", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), pos, state),
                            ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).toArray(Block[]::new)
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<BlockEntityType<SignPressBlockEntity>> SIGN_PRESS_BE =
            BLOCK_ENTITIES.register("sign_press_be", () ->
                    BlockEntityType.Builder.of(SignPressBlockEntity::new, ModBlocks.SIGN_PRESS.get()).build(null));
}