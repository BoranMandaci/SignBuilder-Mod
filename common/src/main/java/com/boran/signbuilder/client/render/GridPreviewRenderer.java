package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.GridSignBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.client.grid.GridModeManager;
import com.boran.signbuilder.client.grid.GridRaytrace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class GridPreviewRenderer {
    
    private static final RandomSource RANDOM = RandomSource.create();
    
    public static void render(PoseStack poseStack) {
        if (!GridModeManager.isGridModeActive()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack item = player.getMainHandItem();
        LetterBlock letterBlock = null;
        if (item.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock lb) {
            letterBlock = lb;
        } else {
            item = player.getOffhandItem();
            if (item.getItem() instanceof BlockItem bi2 && bi2.getBlock() instanceof LetterBlock lb2) {
                letterBlock = lb2;
            } else {
                return;
            }
        }

        HitResult rawHit = mc.hitResult;
        if (!(rawHit instanceof BlockHitResult hit) || hit.getType() == HitResult.Type.MISS) return;

        int gridSize = GridModeManager.getCurrentGridSize();
        GridRaytrace.GridHit gridHit = GridRaytrace.getGridHit(hit, gridSize);
        if (gridHit == null) return;

        net.minecraft.core.BlockPos basePos = gridHit.pos();
        BlockState targetState = mc.level.getBlockState(basePos);
        boolean isBackplate = targetState.getBlock() instanceof com.boran.signbuilder.block.BackplateBlock || targetState.getBlock() instanceof com.boran.signbuilder.block.GridSignBlock;
        if (!isBackplate && targetState.getBlock() instanceof com.boran.signbuilder.block.LetterBlock) {
            if (mc.level.getBlockEntity(basePos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
                isBackplate = lbe.hasBackplate();
            }
        }

        Direction facing = gridHit.face();
        if (isBackplate && targetState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = targetState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else if (facing == Direction.UP || facing == Direction.DOWN) {
            facing = player.getDirection().getOpposite();
        }
        
        int facingRotation = com.boran.signbuilder.block.SignRotation.fromDirection(facing);
        if (mc.level.getBlockEntity(basePos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe) {
            facingRotation = gbe.getFacingRotation();
        } else if (isBackplate && mc.level.getBlockEntity(basePos) instanceof com.boran.signbuilder.block.entity.LetterBlockEntity lbe) {
            com.boran.signbuilder.block.entity.LetterBlockEntity effective = lbe;
            if (lbe.isDummy()) {
                net.minecraft.world.level.block.entity.BlockEntity me = mc.level.getBlockEntity(lbe.getMasterPos());
                if (me instanceof com.boran.signbuilder.block.entity.LetterBlockEntity masterBe) {
                    effective = masterBe;
                }
            }
            facingRotation = effective.getFacingRotation();
        } else if (isBackplate && targetState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facingRotation = com.boran.signbuilder.block.SignRotation.fromDirection(targetState.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        int index = gridHit.index();
        boolean isBack = index >= 16;
        int rawIndex = isBack ? index - 16 : index;
        int col = rawIndex % gridSize;
        int row = rawIndex / gridSize;

        Direction effectiveFacing = isBack ? facing.getOpposite() : facing;

        BlockState letterState = letterBlock.defaultBlockState();
        if (letterState.hasProperty(LetterBlock.FACING)) {
            letterState = letterState.setValue(LetterBlock.FACING, effectiveFacing);
        }
        AttachFace attachFace = AttachFace.WALL;
        if (isBackplate && targetState.hasProperty(BlockStateProperties.ATTACH_FACE)) {
            attachFace = targetState.getValue(BlockStateProperties.ATTACH_FACE);
        }
        if (letterState.hasProperty(LetterBlock.FACE)) {
            letterState = letterState.setValue(LetterBlock.FACE, AttachFace.WALL);
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        poseStack.pushPose();
        
        net.minecraft.core.BlockPos pos = basePos;
        if (!(targetState.getBlock() instanceof GridSignBlock) && !isBackplate) {
            pos = pos.relative(gridHit.face());
        }

        poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);
        
        int rotationDegrees = com.boran.signbuilder.block.SignRotation.deltaDegrees(facingRotation, facing);
        if (rotationDegrees != 0) {
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationDegrees));
            poseStack.translate(-0.5, 0.0, -0.5);
        }

        org.joml.Vector3f baseTrans = com.boran.signbuilder.client.grid.GridOffsetHelper.getBaseTranslation(attachFace, effectiveFacing);
        poseStack.translate(baseTrans.x, baseTrans.y, baseTrans.z);

        float scale = 1.0f / gridSize;

        float colShift = (gridSize - 1) / 2.0f - col;
        float rowShift = (gridSize - 1) / 2.0f - row;
        
        if (rotationDegrees != 0 && attachFace != AttachFace.WALL) {
            colShift *= 1.4142135f;
        }

        org.joml.Vector3f offsets = com.boran.signbuilder.client.grid.GridOffsetHelper.getOffset(attachFace, effectiveFacing, colShift, rowShift, scale);

        poseStack.translate(offsets.x, offsets.y, offsets.z);

        if (isBackplate || (targetState.getBlock() instanceof GridSignBlock && mc.level.getBlockEntity(basePos) instanceof com.boran.signbuilder.block.entity.GridSignBlockEntity gbe && gbe.hasBackplate())) {
            float clearance = 1.05f / 16.0f;
            org.joml.Vector3f cl = com.boran.signbuilder.client.grid.GridOffsetHelper.getClearance(attachFace, effectiveFacing, clearance);
            poseStack.translate(cl.x, cl.y, cl.z);
        }

        org.joml.Vector3f c = com.boran.signbuilder.client.grid.GridOffsetHelper.getCenter(attachFace, effectiveFacing);

        poseStack.translate(c.x, c.y, c.z);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-c.x, -c.y, -c.z);

        BakedModel model = mc.getBlockRenderer().getBlockModel(letterState);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());

        Matrix4f poseMatrix = poseStack.last().pose();
        Matrix3f normalMatrix = poseStack.last().normal();
        
        int light = 0xF000F0;
        int red = 255, green = 255, blue = 255, alpha = 127; // Semi-transparent

        for (Direction d : Direction.values()) {
            RANDOM.setSeed(42L);
            List<BakedQuad> quads = model.getQuads(letterState, d, RANDOM);
            for (BakedQuad quad : quads) {
                renderGhostQuad(poseMatrix, normalMatrix, consumer, quad, red, green, blue, alpha, light);
            }
        }
        RANDOM.setSeed(42L);
        List<BakedQuad> unculledQuads = model.getQuads(letterState, null, RANDOM);
        for (BakedQuad quad : unculledQuads) {
            renderGhostQuad(poseMatrix, normalMatrix, consumer, quad, red, green, blue, alpha, light);
        }
        
        bufferSource.endBatch(RenderType.translucent());
        poseStack.popPose();
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
