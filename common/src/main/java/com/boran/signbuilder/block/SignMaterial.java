package com.boran.signbuilder.block;

import net.minecraft.util.StringRepresentable;

public enum SignMaterial implements StringRepresentable {
    DEFAULT("default"),
    OAK("oak"),
    SPRUCE("spruce"),
    BIRCH("birch"),
    JUNGLE("jungle"),
    ACACIA("acacia"),
    DARK_OAK("dark_oak"),
    MANGROVE("mangrove"),
    CHERRY("cherry"),
    BAMBOO("bamboo"),
    IRON("iron"),
    ANDESITE("andesite"),
    GOLD("gold"),
    DIAMOND("diamond"),
    LAPIS("lapis"),
    SMOOTH_STONE("smooth_stone"),
    POLISHED_DIORITE("polished_diorite"),
    BRICKS("bricks"),
    STONE_BRICKS("stone_bricks"),
    REDSTONE_BLOCK("redstone_block"),
    NETHERITE_BLOCK("netherite_block"),
    QUARTZ_BLOCK("quartz_block"),
    POLISHED_GRANITE("polished_granite"),
    PURPUR_BLOCK("purpur_block"),
    STONE("stone"),
    EMERALD_BLOCK("emerald_block"),
    SMOOTH_SANDSTONE("smooth_sandstone"),
    SMOOTH_RED_SANDSTONE("smooth_red_sandstone"),
    COPPER_BLOCK("copper_block"),
    AMETHYST_BLOCK("amethyst_block"),
    COAL_BLOCK("coal_block"),
    END_STONE_BRICKS("end_stone_bricks"),
    NETHER_BRICKS("nether_bricks"),
    RED_NETHER_BRICKS("red_nether_bricks"),
    PRISMARINE_BRICKS("prismarine_bricks"),
    MUD_BRICKS("mud_bricks"),
    DEEPSLATE_BRICKS("deepslate_bricks"),
    CRIMSON_PLANKS("crimson_planks"),
    WARPED_PLANKS("warped_planks"),
    MOSSY_STONE_BRICKS("mossy_stone_bricks");

    private final String name;

    SignMaterial(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}