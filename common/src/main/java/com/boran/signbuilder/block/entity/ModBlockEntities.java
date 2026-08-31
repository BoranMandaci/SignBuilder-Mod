package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.ModBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create("signbuilder", Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<LetterBlockEntity>> LETTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("letter_block_entity", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), pos, state),
                            ModBlocks.ALL_SIGN_BLOCKS.stream().map(RegistrySupplier::get).toArray(Block[]::new)
                    ).build(null));

    public static final RegistrySupplier<BlockEntityType<SignPressBlockEntity>> SIGN_PRESS_BE =
            BLOCK_ENTITIES.register("sign_press_be", () ->
                    BlockEntityType.Builder.of(SignPressBlockEntity::new, ModBlocks.SIGN_PRESS.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}