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
    ANDESITE("andesite");

    private final String name;

    SignMaterial(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}