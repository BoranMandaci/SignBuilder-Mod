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
    STONE_BRICKS("stone_bricks");

    private final String name;

    SignMaterial(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}