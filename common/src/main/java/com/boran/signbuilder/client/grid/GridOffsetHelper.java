package com.boran.signbuilder.client.grid;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Vector3f;

public class GridOffsetHelper {
    public static Vector3f getOffset(AttachFace face, Direction facing, float colShift, float rowShift, float scale) {
        float x = 0, y = 0, z = 0;
        y = rowShift * scale;
        switch (facing) {
            case NORTH -> x = colShift * scale;
            case SOUTH -> x = -colShift * scale;
            case WEST -> z = -colShift * scale;
            case EAST -> z = colShift * scale;
        }
        return new Vector3f(x, y, z);
    }
    
    public static Vector3f getCenter(AttachFace face, Direction facing) {
        float cx = 0.5f, cy = 0.5f, cz = 0.5f;
        switch (facing) {
            case NORTH -> cz = 1.0f;
            case SOUTH -> cz = 0.0f;
            case WEST -> cx = 1.0f;
            case EAST -> cx = 0.0f;
        }
        return new Vector3f(cx, cy, cz);
    }

    public static Vector3f getBaseTranslation(AttachFace face, Direction facing) {
        float dx = 0, dy = 0, dz = 0;
        if (face == AttachFace.FLOOR || face == AttachFace.CEILING) {
            float shift = 0.46875f;
            switch (facing) {
                case NORTH -> dz = -shift;
                case SOUTH -> dz = shift;
                case WEST -> dx = -shift;
                case EAST -> dx = shift;
            }
        }
        return new Vector3f(dx, dy, dz);
    }
    
    public static Vector3f getClearance(AttachFace face, Direction facing, float amount) {
        return switch (facing) {
            case NORTH -> new Vector3f(0, 0, -amount);
            case SOUTH -> new Vector3f(0, 0, amount);
            case WEST -> new Vector3f(-amount, 0, 0);
            case EAST -> new Vector3f(amount, 0, 0);
            default -> new Vector3f(0, 0, 0);
        };
    }
}
