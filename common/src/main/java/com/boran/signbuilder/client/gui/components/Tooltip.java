package com.boran.signbuilder.client.gui.components;

import net.minecraft.network.chat.Component;

public class Tooltip {
    private final Component message;

    private Tooltip(Component message) {
        this.message = message;
    }

    public static Tooltip create(Component message) {
        return new Tooltip(message);
    }

    public Component getMessage() {
        return message;
    }
}
