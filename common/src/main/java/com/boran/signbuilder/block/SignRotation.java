package com.boran.signbuilder.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SignRotation {
    private static final int[] RIGHT_X = {-1, -1, 0, 1, 1, 1, 0, -1};
    private static final int[] RIGHT_Z = {0, -1, -1, -1, 0, 1, 1, 1};
    private static final int[] FACING_X = {0, 1, 1, 1, 0, -1, -1, -1};
    private static final int[] FACING_Z = {-1, -1, 0, 1, 1, 1, 0, -1};

    private SignRotation() {
    }

    public static int fromYaw(float yaw, int offset) {
        return Math.floorMod(Math.round(yaw / 45.0f) + 4 + offset, 8);
    }

    public static int fromPlacement(BlockState state, float yaw) {
        AttachFace face = state.hasProperty(BlockStateProperties.ATTACH_FACE)
                ? state.getValue(BlockStateProperties.ATTACH_FACE)
                : AttachFace.WALL;
        if (face == AttachFace.WALL && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return fromDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        return fromYaw(yaw, -2);
    }

    public static int fromBackplatePlacement(float yaw) {
        return fromYaw(yaw, 4);
    }

    public static int fromDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> 0;
            case EAST -> 2;
            case SOUTH -> 4;
            case WEST -> 6;
            default -> 0;
        };
    }

    public static Direction cardinalDirection(int rotation) {
        return switch (Math.floorMod(rotation, 8)) {
            case 0 -> Direction.NORTH;
            case 2 -> Direction.EAST;
            case 4 -> Direction.SOUTH;
            case 6 -> Direction.WEST;
            default -> null;
        };
    }

    public static int facingX(int rotation) {
        return FACING_X[Math.floorMod(rotation, 8)];
    }

    public static int facingZ(int rotation) {
        return FACING_Z[Math.floorMod(rotation, 8)];
    }

    public static int rightX(int rotation) {
        return RIGHT_X[Math.floorMod(rotation, 8)];
    }

    public static int rightZ(int rotation) {
        return RIGHT_Z[Math.floorMod(rotation, 8)];
    }

    public static int horizontalStepX(int rotation, boolean wall) {
        int normalized = Math.floorMod(rotation, 8);
        return wall ? RIGHT_X[normalized] : -FACING_X[normalized];
    }

    public static int horizontalStepZ(int rotation, boolean wall) {
        int normalized = Math.floorMod(rotation, 8);
        return wall ? RIGHT_Z[normalized] : -FACING_Z[normalized];
    }

    public static VoxelShape widenAlongTangent(VoxelShape shape, int rotation, double scale) {
        if ((Math.floorMod(rotation, 2) == 0) || scale == 1.0) return shape;

        double normalLength = Math.sqrt(facingX(rotation) * facingX(rotation) + facingZ(rotation) * facingZ(rotation));
        if (normalLength == 0.0) return shape;
        double tangentX = -facingZ(rotation) / normalLength;
        double tangentZ = facingX(rotation) / normalLength;
        VoxelShape result = Shapes.empty();

        for (AABB box : shape.toAabbs()) {
            double minX = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;

            for (int xIndex = 0; xIndex < 2; xIndex++) {
                double x = xIndex == 0 ? box.minX : box.maxX;
                for (int zIndex = 0; zIndex < 2; zIndex++) {
                    double z = zIndex == 0 ? box.minZ : box.maxZ;
                    double tangentOffset = (x - 0.5) * tangentX + (z - 0.5) * tangentZ;
                    double widenedX = x + (scale - 1.0) * tangentOffset * tangentX;
                    double widenedZ = z + (scale - 1.0) * tangentOffset * tangentZ;
                    minX = Math.min(minX, widenedX);
                    maxX = Math.max(maxX, widenedX);
                    minZ = Math.min(minZ, widenedZ);
                    maxZ = Math.max(maxZ, widenedZ);
                }
            }

            result = Shapes.or(result, Shapes.create(minX, box.minY, minZ, maxX, box.maxY, maxZ));
        }
        return result;
    }

    public static int deltaDegrees(int rotation, Direction facing) {
        int delta = Math.floorMod(rotation - fromDirection(facing) + 4, 8) - 4;
        return -delta * 45;
    }

    public static VoxelShape rotateAroundOrigin(VoxelShape shape, double degrees) {
        return rotate(shape, degrees, 0.0, 0.0);
    }

    public static VoxelShape rotateAroundBlockCenter(VoxelShape shape, double degrees) {
        return rotate(shape, degrees, 0.5, 0.5);
    }

    private static VoxelShape rotate(VoxelShape shape, double degrees, double centerX, double centerZ) {
        if (degrees == 0.0) {
            return shape;
        }

        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        VoxelShape result = Shapes.empty();

        for (AABB box : shape.toAabbs()) {
            double minX = Double.POSITIVE_INFINITY;
            double minZ = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxZ = Double.NEGATIVE_INFINITY;

            for (int xIndex = 0; xIndex < 2; xIndex++) {
                double x = (xIndex == 0 ? box.minX : box.maxX) - centerX;
                for (int zIndex = 0; zIndex < 2; zIndex++) {
                    double z = (zIndex == 0 ? box.minZ : box.maxZ) - centerZ;
                    double rotatedX = centerX + x * cos + z * sin;
                    double rotatedZ = centerZ - x * sin + z * cos;
                    minX = Math.min(minX, rotatedX);
                    maxX = Math.max(maxX, rotatedX);
                    minZ = Math.min(minZ, rotatedZ);
                    maxZ = Math.max(maxZ, rotatedZ);
                }
            }

            result = Shapes.or(result, Shapes.create(minX, box.minY, minZ, maxX, box.maxY, maxZ));
        }

        return result;
    }
}
