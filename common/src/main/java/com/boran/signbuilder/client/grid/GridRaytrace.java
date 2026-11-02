package com.boran.signbuilder.client.grid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;

public class GridRaytrace {

    public record GridHit(BlockPos pos, Direction face, int u, int v, int index, int gridSize) {}

    public static GridHit getGridHit(BlockHitResult hit, int gridSize) {
        if (hit == null || hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) return null;

        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        Vec3 loc = hit.getLocation();
        double rx = loc.x - pos.getX();
        double ry = loc.y - pos.getY();
        double rz = loc.z - pos.getZ();

        BlockState state = net.minecraft.client.Minecraft.getInstance().level.getBlockState(hit.getBlockPos());
        boolean isBackplate = state.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock;
        if (!isBackplate && state.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
            if (net.minecraft.client.Minecraft.getInstance().level.getBlockEntity(hit.getBlockPos()) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                isBackplate = lbe.getEffectiveMaster().hasBackplate();
            }
        }
        boolean isGridSign = state.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock;
        
        if (!isBackplate && !isGridSign) {
            if (face == Direction.UP || face == Direction.DOWN) return null;
        }

        Direction facing = face;
        if (isBackplate || isGridSign) {
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            }
        }

        int rotation = com.boran.signbuilder.block.SignRotation.fromDirection(facing);
        if (isGridSign && net.minecraft.client.Minecraft.getInstance().level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
            rotation = gbe.getFacingRotation();
        } else if (isBackplate && net.minecraft.client.Minecraft.getInstance().level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
            com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe.getEffectiveMaster();
            rotation = effective.getFacingRotation();
        } else if (isBackplate && state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            rotation = com.boran.signbuilder.block.SignRotation.fromDirection(state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING));
        }

        net.minecraft.world.level.block.state.properties.AttachFace aFace = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE) 
            ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE) 
            : net.minecraft.world.level.block.state.properties.AttachFace.WALL;

        if (state.getBlock() instanceof com.boran.signbuilder.block.LetterBlock && aFace != net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
            facing = facing.getCounterClockWise();
            rotation = Math.floorMod(rotation - 2, 8);
        }
            
        boolean isBack = false;
        net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (isBackplate || isGridSign) {
            if (aFace == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
                Direction blockFacing = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)
                        ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING) : face;
                if (face == blockFacing.getOpposite()) {
                    isBack = true;
                } else if (face == blockFacing) {
                    isBack = false;
                } else if (player != null) {
                    int rot = com.boran.signbuilder.block.SignRotation.fromDirection(blockFacing);
                    double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(rot)
                            + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(rot);
                    isBack = side < 0.0;
                }
            } else {
                Direction frontDirection = com.boran.signbuilder.block.SignRotation.cardinalDirection(rotation);
                if (frontDirection != null && face == frontDirection.getOpposite()) {
                    isBack = true;
                } else if (frontDirection != null && face == frontDirection) {
                    isBack = false;
                } else if (player != null) {
                    net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                    double dot = look.x * com.boran.signbuilder.block.SignRotation.facingX(rotation)
                            + look.z * com.boran.signbuilder.block.SignRotation.facingZ(rotation);
                    if (dot > 0.05) {
                        isBack = true;
                    } else if (dot < -0.05) {
                        isBack = false;
                    } else {
                        double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(rotation)
                                + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(rotation);
                        isBack = side < 0.0;
                    }
                }
            }
        }

        int rotationDegrees = com.boran.signbuilder.block.SignRotation.deltaDegrees(rotation, facing);

        if (rotationDegrees != 0 && aFace != net.minecraft.world.level.block.state.properties.AttachFace.WALL && player != null) {
            double nx = com.boran.signbuilder.block.SignRotation.facingX(rotation);
            double nz = com.boran.signbuilder.block.SignRotation.facingZ(rotation);
            net.minecraft.world.phys.Vec3 eye = player.getEyePosition();
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            double denom = look.x * nx + look.z * nz;
            if (Math.abs(denom) > 1e-5) {
                double t = ((pos.getX() + 0.5 - eye.x) * nx + (pos.getZ() + 0.5 - eye.z) * nz) / denom;
                if (t > 0.0 && t < 12.0) {
                    rx = eye.x + t * look.x - pos.getX();
                    ry = eye.y + t * look.y - pos.getY();
                    rz = eye.z + t * look.z - pos.getZ();
                }
            }
        }

        double radians = Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double dx = rx - 0.5;
        double dz = rz - 0.5;
        double localX = dx * cos - dz * sin;
        double localZ = dx * sin + dz * cos;

        double widthFactor = (rotationDegrees != 0 && aFace != net.minecraft.world.level.block.state.properties.AttachFace.WALL) ? Math.sqrt(2.0) : 1.0;

        Direction effectiveFacing = isBack ? facing.getOpposite() : facing;
        double u;
        switch (effectiveFacing) {
            case NORTH -> u = 0.5 - (localX / widthFactor);
            case SOUTH -> u = 0.5 + (localX / widthFactor);
            case EAST  -> u = 0.5 - (localZ / widthFactor);
            case WEST  -> u = 0.5 + (localZ / widthFactor);
            default    -> u = 0.5;
        }
        double v = 1.0 - ry;

        if (u < 0.0) u = 0.0;
        if (u >= 1.0) u = 0.9999;
        if (v < 0.0) v = 0.0;
        if (v >= 1.0) v = 0.9999;

        int cellU = (int) Math.floor(u * gridSize);
        int cellV = (int) Math.floor(v * gridSize);

        if (cellU < 0) cellU = 0;
        if (cellU >= gridSize) cellU = gridSize - 1;
        if (cellV < 0) cellV = 0;
        if (cellV >= gridSize) cellV = gridSize - 1;

        int index = cellV * gridSize + cellU;
        if (isBack) {
            index += com.boran.signbuilder.block.GridSignBlock.getBackOffset(gridSize);
        }

        return new GridHit(pos, face, cellU, cellV, index, gridSize);
    }

    public record GridRay(BlockPos pos, Direction face, double u, double v, boolean isBack) {}

    public static GridRay getRay(BlockHitResult hit) {
        if (hit == null || hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) return null;

        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        Vec3 loc = hit.getLocation();
        double rx = loc.x - pos.getX();
        double ry = loc.y - pos.getY();
        double rz = loc.z - pos.getZ();

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return null;
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock)) {
            if (mc.level.getBlockState(pos.above()).getBlock() instanceof com.boran.signbuilder.block.GridSignBlock) {
                pos = pos.above();
                state = mc.level.getBlockState(pos);
            } else if (mc.level.getBlockState(pos.below()).getBlock() instanceof com.boran.signbuilder.block.GridSignBlock) {
                pos = pos.below();
                state = mc.level.getBlockState(pos);
            } else {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbor = pos.relative(dir);
                    if (mc.level.getBlockState(neighbor).getBlock() instanceof com.boran.signbuilder.block.GridSignBlock) {
                        pos = neighbor;
                        state = mc.level.getBlockState(neighbor);
                        break;
                    }
                    BlockPos neighborAbove = neighbor.above();
                    if (mc.level.getBlockState(neighborAbove).getBlock() instanceof com.boran.signbuilder.block.GridSignBlock) {
                        pos = neighborAbove;
                        state = mc.level.getBlockState(neighborAbove);
                        break;
                    }
                    BlockPos neighborBelow = neighbor.below();
                    if (mc.level.getBlockState(neighborBelow).getBlock() instanceof com.boran.signbuilder.block.GridSignBlock) {
                        pos = neighborBelow;
                        state = mc.level.getBlockState(neighborBelow);
                        break;
                    }
                }
            }
            if (!(state.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock)) return null;
            rx = loc.x - pos.getX();
            ry = loc.y - pos.getY();
            rz = loc.z - pos.getZ();
        }

        Direction facing = face;
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        }

        int rotation = com.boran.signbuilder.block.SignRotation.fromDirection(facing);
        if (mc.level.getBlockEntity(pos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
            rotation = gbe.getFacingRotation();
        }

        net.minecraft.world.level.block.state.properties.AttachFace aFace = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE)
                ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE)
                : net.minecraft.world.level.block.state.properties.AttachFace.WALL;

        boolean isBack = false;
        net.minecraft.world.entity.player.Player player = mc.player;
        if (aFace == net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
            Direction blockFacing = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)
                    ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING) : face;
            if (face == blockFacing.getOpposite()) {
                isBack = true;
            } else if (face == blockFacing) {
                isBack = false;
            } else if (player != null) {
                int rot = com.boran.signbuilder.block.SignRotation.fromDirection(blockFacing);
                double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(rot)
                        + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(rot);
                isBack = side < 0.0;
            }
        } else {
            Direction frontDirection = com.boran.signbuilder.block.SignRotation.cardinalDirection(rotation);
            if (frontDirection != null && face == frontDirection.getOpposite()) {
                isBack = true;
            } else if (frontDirection != null && face == frontDirection) {
                isBack = false;
            } else if (player != null) {
                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                double dot = look.x * com.boran.signbuilder.block.SignRotation.facingX(rotation)
                        + look.z * com.boran.signbuilder.block.SignRotation.facingZ(rotation);
                if (dot > 0.05) {
                    isBack = true;
                } else if (dot < -0.05) {
                    isBack = false;
                } else {
                    double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(rotation)
                            + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(rotation);
                    isBack = side < 0.0;
                }
            }
        }

        int rotationDegrees = com.boran.signbuilder.block.SignRotation.deltaDegrees(rotation, facing);

        if (rotationDegrees != 0 && aFace != net.minecraft.world.level.block.state.properties.AttachFace.WALL && player != null) {
            double nx = com.boran.signbuilder.block.SignRotation.facingX(rotation);
            double nz = com.boran.signbuilder.block.SignRotation.facingZ(rotation);
            net.minecraft.world.phys.Vec3 eye = player.getEyePosition();
            net.minecraft.world.phys.Vec3 look = player.getLookAngle();
            double denom = look.x * nx + look.z * nz;
            if (Math.abs(denom) > 1e-5) {
                double t = ((pos.getX() + 0.5 - eye.x) * nx + (pos.getZ() + 0.5 - eye.z) * nz) / denom;
                if (t > 0.0 && t < 12.0) {
                    rx = eye.x + t * look.x - pos.getX();
                    ry = eye.y + t * look.y - pos.getY();
                    rz = eye.z + t * look.z - pos.getZ();
                }
            }
        }

        double radians = Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double dx = rx - 0.5;
        double dz = rz - 0.5;
        double localX = dx * cos - dz * sin;
        double localZ = dx * sin + dz * cos;

        double widthFactor = (rotationDegrees != 0 && aFace != net.minecraft.world.level.block.state.properties.AttachFace.WALL) ? Math.sqrt(2.0) : 1.0;

        Direction effectiveFacing = isBack ? facing.getOpposite() : facing;
        double u;
        switch (effectiveFacing) {
            case NORTH -> u = 0.5 - (localX / widthFactor);
            case SOUTH -> u = 0.5 + (localX / widthFactor);
            case EAST  -> u = 0.5 - (localZ / widthFactor);
            case WEST  -> u = 0.5 + (localZ / widthFactor);
            default    -> u = 0.5;
        }
        double v = 1.0 - ry;

        if (u < 0.0) u = 0.0;
        if (u >= 1.0) u = 0.9999;
        if (v < 0.0) v = 0.0;
        if (v >= 1.0) v = 0.9999;

        return new GridRay(pos, face, u, v, isBack);
    }
}
