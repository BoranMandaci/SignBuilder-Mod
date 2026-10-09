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
                isBackplate = lbe.hasBackplate();
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
            com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe;
            if (lbe.isDummy()) {
                net.minecraft.world.level.block.entity.BlockEntity me = net.minecraft.client.Minecraft.getInstance().level.getBlockEntity(lbe.getMasterPos());
                if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity masterBe) {
                    effective = masterBe;
                }
            }
            rotation = effective.getFacingRotation();
        } else if (isBackplate && state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            rotation = com.boran.signbuilder.block.SignRotation.fromDirection(state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING));
        }

        net.minecraft.world.level.block.state.properties.AttachFace aFace = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE) 
            ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ATTACH_FACE) 
            : net.minecraft.world.level.block.state.properties.AttachFace.WALL;
            
        boolean isBack = false;
        net.minecraft.world.entity.player.Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null && (isBackplate || isGridSign) && aFace != net.minecraft.world.level.block.state.properties.AttachFace.WALL) {
            double side = (player.getX() - (pos.getX() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingX(rotation)
                    + (player.getZ() - (pos.getZ() + 0.5)) * com.boran.signbuilder.block.SignRotation.facingZ(rotation);
            if (side < 0.0) {
                isBack = true;
            }
        }

        int rotationDegrees = com.boran.signbuilder.block.SignRotation.deltaDegrees(rotation, facing);
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
            index += 16;
        }

        return new GridHit(pos, face, cellU, cellV, index, gridSize);
    }
}
