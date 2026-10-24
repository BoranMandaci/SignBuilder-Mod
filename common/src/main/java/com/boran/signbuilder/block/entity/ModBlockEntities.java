package com.boran.signbuilder.block.entity;

import com.boran.signbuilder.block.ModBlocks;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create("signbuilder", Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<LetterBlockEntity>> LETTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("letter_block_entity", () -> {
                List<Block> validBlocks = new ArrayList<>();
                for (RegistrySupplier<Block> supplier : ModBlocks.ALL_SIGN_BLOCKS) {
                    try {
                        if (supplier != null && supplier.isPresent()) {
                            validBlocks.add(supplier.get());
                        }
                    } catch (Exception ignored) {}
                }
                try {
                    if (ModBlocks.BACKPLATE.isPresent()) {
                        validBlocks.add(ModBlocks.BACKPLATE.get());
                    }
                } catch (Exception ignored) {}

                return new BlockEntityType<>(
                        (pos, state) -> new LetterBlockEntity(ModBlockEntities.LETTER_BLOCK_ENTITY.get(), pos, state),
                        Set.copyOf(validBlocks)
                );
            });

    public static final RegistrySupplier<BlockEntityType<SignPressBlockEntity>> SIGN_PRESS_BE =
            BLOCK_ENTITIES.register("sign_press_be", () ->
                    new BlockEntityType<>(SignPressBlockEntity::new, Set.of(ModBlocks.SIGN_PRESS.get())));

    public static final RegistrySupplier<BlockEntityType<com.boran.signbuilder.block.entity.GridSignBlockEntity>> GRID_SIGN_BLOCK_ENTITY = 
            BLOCK_ENTITIES.register("grid_sign_block_entity", () ->
                    new BlockEntityType<>(com.boran.signbuilder.block.entity.GridSignBlockEntity::new, Set.of(ModBlocks.GRID_SIGN.get())));

    public static final RegistrySupplier<BlockEntityType<LcdScreenBlockEntity>> LCD_SCREEN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("lcd_screen_block_entity", () ->
                    new BlockEntityType<>(LcdScreenBlockEntity::new, Set.of(ModBlocks.LCD_SCREEN.get())));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
