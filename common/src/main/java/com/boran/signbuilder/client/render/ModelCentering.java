package com.boran.signbuilder.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;

public final class ModelCentering {
    private static final Map<BakedModel, ModelCenters> MODEL_CENTERS = Collections.synchronizedMap(new WeakHashMap<>());

    private ModelCentering() {
    }

    public static Center transformedCenter(BakedModel model, BlockState state, int rotationDegrees, boolean blockCentered, int scale, double offsetX, double offsetZ) {
        BakedModel geometryModel = model instanceof MaterialBakedModel materialModel ? materialModel.originalModel() : model;
        ModelCenters centers = MODEL_CENTERS.computeIfAbsent(geometryModel, key -> findCenters(key, state));
        int rotationIndex = Math.floorMod(Math.round(rotationDegrees / 45.0f), 8);
        double radians = Math.toRadians(rotationIndex * 45.0);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        Center modelCenter = blockCentered ? centers.blockCentered[rotationIndex] : centers.originCentered[rotationIndex];
        if (blockCentered) {
            return new Center(modelCenter.x + offsetX * cos + offsetZ * sin,
                    modelCenter.z - offsetX * sin + offsetZ * cos);
        }
        return new Center(modelCenter.x * scale + offsetX * cos + offsetZ * sin,
                modelCenter.z * scale - offsetX * sin + offsetZ * cos);
    }

    public static double[] getCenterOffset(BakedModel model, BlockState state, int rotationDegrees, boolean blockCentered, int scale, double offsetX, double offsetZ, double targetX, double targetZ) {
        Center current = transformedCenter(model, state, rotationDegrees, blockCentered, scale, offsetX, offsetZ);
        double radians = Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double dx = targetX - current.x;
        double dz = targetZ - current.z;
        double localX = (cos * dx - sin * dz) / scale;
        double localZ = (sin * dx + cos * dz) / scale;
        return new double[]{localX, localZ};
    }

    public static void centerPose(PoseStack poseStack, BakedModel model, BlockState state, int rotationDegrees, boolean blockCentered, int scale, double offsetX, double offsetZ, double targetX, double targetZ) {
        double[] offset = getCenterOffset(model, state, rotationDegrees, blockCentered, scale, offsetX, offsetZ, targetX, targetZ);
        poseStack.translate(offset[0], 0.0, offset[1]);
    }

    public static void widenDiagonalPose(PoseStack poseStack, Direction modelFacing, int rotation) {
        if ((rotation & 1) == 0) return;
        float factor = (float) Math.sqrt(2.0);
        poseStack.translate(0.5, 0.0, 0.5);
        if (modelFacing.getAxis() == Direction.Axis.Z) {
            poseStack.scale(factor, 1.0F, 1.0F);
        } else {
            poseStack.scale(1.0F, 1.0F, factor);
        }
        poseStack.translate(-0.5, 0.0, -0.5);
    }

    public static Center centerAlongTangent(Center current, double targetX, double targetZ, double normalX, double normalZ) {
        double length = Math.sqrt(normalX * normalX + normalZ * normalZ);
        if (length == 0.0) return new Center(targetX, targetZ);
        double nx = normalX / length;
        double nz = normalZ / length;
        double tx = -nz;
        double tz = nx;
        double normalPosition = current.x() * nx + current.z() * nz;
        double tangentPosition = targetX * tx + targetZ * tz;
        return new Center(normalPosition * nx + tangentPosition * tx, normalPosition * nz + tangentPosition * tz);
    }

    public static Center centerAlongNormal(Center current, double targetX, double targetZ, double normalX, double normalZ) {
        double length = Math.sqrt(normalX * normalX + normalZ * normalZ);
        if (length == 0.0) return new Center(targetX, targetZ);
        double nx = normalX / length;
        double nz = normalZ / length;
        double tx = -nz;
        double tz = nx;
        double normalPosition = targetX * nx + targetZ * nz;
        double tangentPosition = current.x() * tx + current.z() * tz;
        return new Center(normalPosition * nx + tangentPosition * tx, normalPosition * nz + tangentPosition * tz);
    }

    private static ModelCenters findCenters(BakedModel model, BlockState state) {
        double[] minOriginX = new double[8];
        double[] minOriginZ = new double[8];
        double[] maxOriginX = new double[8];
        double[] maxOriginZ = new double[8];
        double[] minCenteredX = new double[8];
        double[] minCenteredZ = new double[8];
        double[] maxCenteredX = new double[8];
        double[] maxCenteredZ = new double[8];
        java.util.Arrays.fill(minOriginX, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(minOriginZ, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(maxOriginX, Double.NEGATIVE_INFINITY);
        java.util.Arrays.fill(maxOriginZ, Double.NEGATIVE_INFINITY);
        java.util.Arrays.fill(minCenteredX, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(minCenteredZ, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(maxCenteredX, Double.NEGATIVE_INFINITY);
        java.util.Arrays.fill(maxCenteredZ, Double.NEGATIVE_INFINITY);
        RandomSource random = RandomSource.create(42L);

        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(state, direction, random)) {
                int[] vertices = quad.getVertices();
                int stride = vertices.length / 4;
                for (int i = 0; i < vertices.length; i += stride) {
                    double x = Float.intBitsToFloat(vertices[i]);
                    double z = Float.intBitsToFloat(vertices[i + 2]);
                    addVertex(x, z, minOriginX, minOriginZ, maxOriginX, maxOriginZ, minCenteredX, minCenteredZ, maxCenteredX, maxCenteredZ);
                }
            }
        }

        random.setSeed(42L);
        for (BakedQuad quad : model.getQuads(state, null, random)) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            for (int i = 0; i < vertices.length; i += stride) {
                double x = Float.intBitsToFloat(vertices[i]);
                double z = Float.intBitsToFloat(vertices[i + 2]);
                addVertex(x, z, minOriginX, minOriginZ, maxOriginX, maxOriginZ, minCenteredX, minCenteredZ, maxCenteredX, maxCenteredZ);
            }
        }

        Center[] originCenters = new Center[8];
        Center[] blockCenters = new Center[8];
        for (int i = 0; i < 8; i++) {
            if (!Double.isFinite(minOriginX[i]) || !Double.isFinite(minOriginZ[i])) {
                originCenters[i] = new Center(0.5, 0.5);
                blockCenters[i] = new Center(0.5, 0.5);
            } else {
                originCenters[i] = new Center((minOriginX[i] + maxOriginX[i]) * 0.5, (minOriginZ[i] + maxOriginZ[i]) * 0.5);
                blockCenters[i] = new Center((minCenteredX[i] + maxCenteredX[i]) * 0.5, (minCenteredZ[i] + maxCenteredZ[i]) * 0.5);
            }
        }
        return new ModelCenters(originCenters, blockCenters);
    }

    private static void addVertex(double x, double z, double[] minOriginX, double[] minOriginZ, double[] maxOriginX, double[] maxOriginZ,
                                  double[] minCenteredX, double[] minCenteredZ, double[] maxCenteredX, double[] maxCenteredZ) {
        for (int rotationIndex = 0; rotationIndex < 8; rotationIndex++) {
            double radians = Math.toRadians(rotationIndex * 45.0);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double rotatedX = x * cos + z * sin;
            double rotatedZ = -x * sin + z * cos;
            minOriginX[rotationIndex] = Math.min(minOriginX[rotationIndex], rotatedX);
            minOriginZ[rotationIndex] = Math.min(minOriginZ[rotationIndex], rotatedZ);
            maxOriginX[rotationIndex] = Math.max(maxOriginX[rotationIndex], rotatedX);
            maxOriginZ[rotationIndex] = Math.max(maxOriginZ[rotationIndex], rotatedZ);

            double centeredX = 0.5 + (x - 0.5) * cos + (z - 0.5) * sin;
            double centeredZ = 0.5 - (x - 0.5) * sin + (z - 0.5) * cos;
            minCenteredX[rotationIndex] = Math.min(minCenteredX[rotationIndex], centeredX);
            minCenteredZ[rotationIndex] = Math.min(minCenteredZ[rotationIndex], centeredZ);
            maxCenteredX[rotationIndex] = Math.max(maxCenteredX[rotationIndex], centeredX);
            maxCenteredZ[rotationIndex] = Math.max(maxCenteredZ[rotationIndex], centeredZ);
        }
    }

    private record ModelCenters(Center[] originCentered, Center[] blockCentered) {}

    public record Center(double x, double z) {
    }
}
