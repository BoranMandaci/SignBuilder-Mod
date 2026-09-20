package com.boran.signbuilder.client.render;

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

        boolean is2x2 = tag.getBoolean("Is2x2");
        boolean isVertical = tag.getBoolean("IsVertical");
        boolean withBackplate = tag.getBoolean("WithBackplate");

        Direction clickedFace = blockHit.getDirection();
        BlockPos clickedPos = blockHit.getBlockPos();
        int startX = clickedPos.getX() + clickedFace.getStepX();
        int startY = clickedPos.getY() + clickedFace.getStepY();
        int startZ = clickedPos.getZ() + clickedFace.getStepZ();

        Direction playerFacing = player.getDirection();
        Direction rightDir = playerFacing.getClockWise();
        int rightStepX = rightDir.getStepX();
        int rightStepZ = rightDir.getStepZ();

        int stepDirY = (clickedFace == Direction.UP) ? 1 : -1;

        AttachFace attachFace = (clickedFace.getAxis() == Direction.Axis.Y)
                ? (clickedFace == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING)
                : AttachFace.WALL;

        Direction modelFacing = (clickedFace.getAxis() == Direction.Axis.Y)
                ? playerFacing.getCounterClockWise()
                : clickedFace;

        double wallOffsetX = 0.0;
        double wallOffsetY = 0.0;
        double wallOffsetZ = 0.0;
        if (clickedFace == Direction.NORTH) wallOffsetZ = -1.0;
        else if (clickedFace == Direction.WEST) wallOffsetX = -1.0;
        else if (clickedFace == Direction.DOWN) wallOffsetY = -1.0;

        BakedModel bpModel = null;
        BlockState bpState = null;
        if (withBackplate) {
            Block bpBlock = ModBlocks.BACKPLATE.get();
            bpState = bpBlock.defaultBlockState();
            if (bpState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                bpState = bpState.setValue(BlockStateProperties.ATTACH_FACE, attachFace);
            }
            if (bpState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                bpState = bpState.setValue(BlockStateProperties.HORIZONTAL_FACING, modelFacing);
            }
            bpModel = mc.getBlockRenderer().getBlockModel(bpState);
        }

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

            BlockState stateToPlace = block.defaultBlockState();
            if (stateToPlace.hasProperty(BlockStateProperties.ATTACH_FACE)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.ATTACH_FACE, attachFace);
            }
            if (stateToPlace.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.HORIZONTAL_FACING, modelFacing);
            }

            BakedModel model = mc.getBlockRenderer().getBlockModel(stateToPlace);

            if (!isVertical) {
                if (!is2x2) {
                    int posX = startX + (rightStepX * effectiveIdx);
                    int posY = startY;
                    int posZ = startZ + (rightStepZ * effectiveIdx);

                    SCRATCH_POS.set(posX, posY, posZ);
                    boolean canPlace = level.getBlockState(SCRATCH_POS).canBeReplaced();

                    float r = canPlace ? 0.3F : 1.0F;
                    float g = canPlace ? 1.0F : 0.2F;
                    float b = canPlace ? 0.4F : 0.2F;
                    float a = 0.65F;

                    poseStack.pushPose();
                    poseStack.translate(posX - camX, posY - camY, posZ - camZ);
                    renderGhostModel(poseStack, consumer, model, stateToPlace, r, g, b, a, 15728880);

                    if (withBackplate && bpModel != null) {
                        renderGhostModel(poseStack, consumer, bpModel, bpState, r, g, b, a * 0.7F, 15728880);
                    }
                    poseStack.popPose();
                } else {
                    int baseX = startX + (rightStepX * (effectiveIdx * 2));
                    int baseY = startY;
                    int baseZ = startZ + (rightStepZ * (effectiveIdx * 2));

                    boolean canPlace = level.getBlockState(SCRATCH_POS.set(baseX, baseY, baseZ)).canBeReplaced()
                            && level.getBlockState(SCRATCH_POS.set(baseX + rightStepX, baseY, baseZ + rightStepZ)).canBeReplaced()
                            && level.getBlockState(SCRATCH_POS.set(baseX, baseY + 1, baseZ)).canBeReplaced()
                            && level.getBlockState(SCRATCH_POS.set(baseX + rightStepX, baseY + 1, baseZ + rightStepZ)).canBeReplaced();

                    float r = canPlace ? 0.3F : 1.0F;
                    float g = canPlace ? 1.0F : 0.2F;
                    float b = canPlace ? 0.4F : 0.2F;
                    float a = 0.65F;

                    double minX = Math.min(baseX, baseX + rightStepX);
                    double minY = baseY;
                    double minZ = Math.min(baseZ, baseZ + rightStepZ);

                    poseStack.pushPose();
                    poseStack.translate((minX + wallOffsetX) - camX, (minY + wallOffsetY) - camY, (minZ + wallOffsetZ) - camZ);
                    poseStack.scale(2.0F, 2.0F, 2.0F);
                    renderGhostModel(poseStack, consumer, model, stateToPlace, r, g, b, a, 15728880);

                    if (withBackplate && bpModel != null) {
                        renderGhostModel(poseStack, consumer, bpModel, bpState, r, g, b, a * 0.7F, 15728880);
                    }
                    poseStack.popPose();
                }
            } else {
                if (!is2x2) {
                    int posX = startX;
                    int posY = startY + (stepDirY * effectiveIdx);
                    int posZ = startZ;

                    SCRATCH_POS.set(posX, posY, posZ);
                    boolean canPlace = level.getBlockState(SCRATCH_POS).canBeReplaced();

                    float r = canPlace ? 0.3F : 1.0F;
                    float g = canPlace ? 1.0F : 0.2F;
                    float b = canPlace ? 0.4F : 0.2F;
                    float a = 0.65F;

                    poseStack.pushPose();
                    poseStack.translate(posX - camX, posY - camY, posZ - camZ);
                    renderGhostModel(poseStack, consumer, model, stateToPlace, r, g, b, a, 15728880);

                    if (withBackplate && bpModel != null) {
                        renderGhostModel(poseStack, consumer, bpModel, bpState, r, g, b, a * 0.7F, 15728880);
                    }
                    poseStack.popPose();
                } else {
                    int baseX = startX;
                    int baseY = (stepDirY == 1)
                            ? startY + (effectiveIdx * 2)
                            : startY - 1 - (effectiveIdx * 2);
                    int baseZ = startZ;

                    boolean canPlace = level.getBlockState(SCRATCH_POS.set(baseX, baseY, baseZ)).canBeReplaced()
                            && level.getBlockState(SCRATCH_POS.set(baseX + rightStepX, baseY, baseZ + rightStepZ)).canBeReplaced()
                            && level.getBlockState(SCRATCH_POS.set(baseX, baseY + 1, baseZ)).canBeReplaced()
                            && level.getBlockState(SCRATCH_POS.set(baseX + rightStepX, baseY + 1, baseZ + rightStepZ)).canBeReplaced();

                    float r = canPlace ? 0.3F : 1.0F;
                    float g = canPlace ? 1.0F : 0.2F;
                    float b = canPlace ? 0.4F : 0.2F;
                    float a = 0.65F;

                    double minX = Math.min(baseX, baseX + rightStepX);
                    double minY = baseY;
                    double minZ = Math.min(baseZ, baseZ + rightStepZ);

                    poseStack.pushPose();
                    poseStack.translate((minX + wallOffsetX) - camX, (minY + wallOffsetY) - camY, (minZ + wallOffsetZ) - camZ);
                    poseStack.scale(2.0F, 2.0F, 2.0F);
                    renderGhostModel(poseStack, consumer, model, stateToPlace, r, g, b, a, 15728880);

                    if (withBackplate && bpModel != null) {
                        renderGhostModel(poseStack, consumer, bpModel, bpState, r, g, b, a * 0.7F, 15728880);
                    }
                    poseStack.popPose();
                }
            }

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