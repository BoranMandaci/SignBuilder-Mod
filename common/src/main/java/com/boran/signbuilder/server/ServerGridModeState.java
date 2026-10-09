package com.boran.signbuilder.server;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGridModeState {
    private static final ConcurrentHashMap<UUID, Integer> gridSizes = new ConcurrentHashMap<>();

    public static void setGridSize(UUID player, int size) {
        if (size <= 1) gridSizes.remove(player);
        else gridSizes.put(player, size);
    }

    public static int getGridSize(UUID player) {
        return gridSizes.getOrDefault(player, 1);
    }
}
