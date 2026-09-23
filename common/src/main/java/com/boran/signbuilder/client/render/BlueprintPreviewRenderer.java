package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.item.SignBlueprintItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class BlueprintPreviewRenderer {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final BlockPos.MutableBlockPos SCRATCH_POS = new BlockPos.MutableBlockPos();

    public static void render(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        Camera camera = mc.gameRenderer.getMainCamera();
        if (player == null || level == null || camera == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SignBlueprintItem)) {
            stack = player.getOffhandItem();
        }
        if (!(stack.getItem() instanceof SignBlueprintItem)) return;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlueprintText")) return;

        String text = tag.getString("BlueprintText");
        if (text.isEmpty()) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        int size = 1;
        if (tag.contains("Size")) {
            size = tag.getInt("Size");
        } else if (tag.getBoolean("Is2x2")) {
            size = 2;
        }

        boolean isVertical = tag.getBoolean("IsVertical");
        boolean withBackplate = tag.getBoolean("WithBackplate");

        Direction clickedFace = blockHit.getDirection();
        BlockPos startPos = blockHit.getBlockPos().relative(clickedFace);

        Direction playerFacing = player.getDirection();
        Direction rightDir = playerFacing.getClockWise();

        int stepDirY = (clickedFace == Direction.UP) ? 1 : -1;

        boolean isFloor = (clickedFace.getAxis() == Direction.Axis.Y);
        AttachFace attachFace = isFloor
                ? (clickedFace == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING)
                : AttachFace.WALL;

        Direction facing = isFloor
                ? playerFacing.getCounterClockWise()
                : clickedFace;

        Vec3 camPos = camera.getPosition();
        double camX = camPos.x;
        double camY = camPos.y;
        double camZ = camPos.z;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        int effectiveIdx = 0;
        int textLen = text.length();

        for (int i = 0; i < textLen; i++) {
            char c = text.charAt(i);
            if (c == ' ') continue;

            Block block = SignBlueprintItem.getBlockForChar(c);
            if (block == null) {
                effectiveIdx++;
                continue;
            }

            BlockPos basePos;
            if (!isVertical) {
                basePos = startPos.relative(rightDir, effectiveIdx * size);
            } else {
                int baseY = (stepDirY == 1)
                        ? startPos.getY() + (effectiveIdx * size)
                        : startPos.getY() - (size - 1) - (effectiveIdx * size);
                basePos = new BlockPos(startPos.getX(), baseY, startPos.getZ());
            }

            boolean canPlace = true;
            for (int dy = 0; dy < size; dy++) {
                for (int dx = 0; dx < size; dx++) {
                    SCRATCH_POS.set(basePos).move(rightDir, dx).move(Direction.UP, dy);
                    if (!level.getBlockState(SCRATCH_POS).canBeReplaced()) {
                        canPlace = false;
                        break;
                    }
                }
                if (!canPlace) break;
            }

            float r = canPlace ? 0.3F : 1.0F;
            float g = canPlace ? 1.0F : 0.2F;
            float b = canPlace ? 0.4F : 0.2F;
            float a = 0.65F;

            BlockState stateToPlace = block.defaultBlockState();
            if (stateToPlace.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.ATTACH_FACE, attachFace);
            }
            if (stateToPlace.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
            }

            BakedModel model = mc.getBlockRenderer().getBlockModel(stateToPlace);

            poseStack.pushPose();

            double posX = basePos.getX() - camX;
            double posY = basePos.getY() - camY;
            double posZ = basePos.getZ() - camZ;

            if (isFloor) {
                if (playerFacing == Direction.SOUTH) {
                    posX -= (size - 1);
                } else if (playerFacing == Direction.WEST) {
                    posZ -= (size - 1);
                }
            }

            poseStack.translate(posX, posY, posZ);

            if (size == 3) {
                if (attachFace == AttachFace.WALL) {
                    switch (facing) {
                        case NORTH -> poseStack.translate(-2.0, 0.0, -2.0);
                        case SOUTH -> poseStack.translate(0.0, 0.0, 0.0);
                        case EAST  -> poseStack.translate(0.0, 0.0, -2.0);
                        case WEST  -> poseStack.translate(-2.0, 0.0, 0.0);
                    }
                } else {
                    switch (facing) {
                        case EAST  -> poseStack.translate(0.0, 0.0, -1.375);
                        case NORTH -> poseStack.translate(-1.375, 0.0, 0.0);
                        case SOUTH -> poseStack.translate(-0.625, 0.0, 0.0);
                        case WEST  -> poseStack.translate(0.0, 0.0, -0.625);
                    }
                }
                poseStack.scale(3.0F, 3.0F, 3.0F);
            } else if (size == 2) {
                if (attachFace == AttachFace.WALL) {
                    switch (facing) {
                        case NORTH -> poseStack.translate(-1.0, 0.0, -1.0);
                        case SOUTH -> poseStack.translate(0.0, 0.0, 0.0);
                        case EAST  -> poseStack.translate(0.0, 0.0, -1.0);
                        case WEST  -> poseStack.translate(-1.0, 0.0, 0.0);
                    }
                } else {
                    switch (facing) {
                        case EAST  -> poseStack.translate(0.0, 0.0, -0.6875);
                        case NORTH -> poseStack.translate(-0.6875, 0.0, 0.0);
                        case SOUTH -> poseStack.translate(-0.3125, 0.0, 0.0);
                        case WEST  -> poseStack.translate(0.0, 0.0, -0.3125);
                    }
                }
                poseStack.scale(2.0F, 2.0F, 2.0F);
            }

            if (withBackplate) {
                Direction plateFacing = (attachFace == AttachFace.FLOOR) ? facing.getCounterClockWise() : facing;
                BlockState bpState = ModBlocks.BACKPLATE.get().defaultBlockState()
                        .setValue(BackplateBlock.FACING, plateFacing)
                        .setValue(BackplateBlock.FACE, attachFace);

                BakedModel bpModel = mc.getBlockRenderer().getBlockModel(bpState);
                renderGhostModel(poseStack, consumer, bpModel, bpState, r, g, b, a * 0.7F, 15728880);
            }

            if (withBackplate && attachFace == AttachFace.WALL) {
                float onePixel = 0.0625f;
                poseStack.translate(facing.getStepX() * onePixel, 0, facing.getStepZ() * onePixel);
            }

            renderGhostModel(poseStack, consumer, model, stateToPlace, r, g, b, a, 15728880);

            poseStack.popPose();
            effectiveIdx++;
        }

        bufferSource.endBatch(RenderType.translucent());
    }

    private static void renderGhostModel(PoseStack poseStack, VertexConsumer consumer, BakedModel model, BlockState state, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        int red = (int) (r * 255.0F);
        int green = (int) (g * 255.0F);
        int blue = (int) (b * 255.0F);
        int alpha = (int) (a * 255.0F);

        for (int d = 0; d < 6; d++) {
            RANDOM.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(state, DIRECTIONS[d], RANDOM);
            int qSize = quads.size();
            for (int i = 0; i < qSize; i++) {
                renderGhostQuad(poseMatrix, normalMatrix, consumer, quads.get(i), red, green, blue, alpha, light);
            }
        }

        RANDOM.setSeed(42L);
        List<BakedQuad> unculledQuads = model.getQuads(state, null, RANDOM);
        int unculledSize = unculledQuads.size();
        for (int i = 0; i < unculledSize; i++) {
            renderGhostQuad(poseMatrix, normalMatrix, consumer, unculledQuads.get(i), red, green, blue, alpha, light);
        }
    }

    private static void renderGhostQuad(Matrix4f poseMatrix, Matrix3f normalMatrix, VertexConsumer consumer, BakedQuad quad, int red, int green, int blue, int alpha, int light) {
        int[] vertices = quad.getVertices();
        int vertexCount = vertices.length / 8;

        for (int i = 0; i < vertexCount; i++) {
            int offset = i * 8;
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            float u = Float.intBitsToFloat(vertices[offset + 4]);
            float v = Float.intBitsToFloat(vertices[offset + 5]);

            int packedNormal = vertices[offset + 7];
            float nx, ny, nz;
            if (packedNormal != 0) {
                nx = ((byte) (packedNormal & 0xFF)) / 127.0F;
                ny = ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0F;
                nz = ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0F;
            } else {
                Direction dir = quad.getDirection();
                nx = dir.getStepX();
                ny = dir.getStepY();
                nz = dir.getStepZ();
            }

            consumer.vertex(poseMatrix, x, y, z)
                    .color(red, green, blue, alpha)
                    .uv(u, v)
                    .uv2(light)
                    .normal(normalMatrix, nx, ny, nz)
                    .endVertex();
        }
    }
}