package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.entity.LcdScreenBlockEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class StorageHighlightRenderer {

    private static final class HighlightRenderTypes extends RenderStateShard {
        private static final RenderType XRAY_QUADS = RenderType.create(
                "signbuilder_storage_highlight_xray",
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS,
                4096,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setTextureState(NO_TEXTURE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );

        private HighlightRenderTypes(String name, Runnable setupState, Runnable clearState) {
            super(name, setupState, clearState);
        }
    }

    private static final class HighlightEntry {
        final BlockPos minPos;
        final BlockPos maxPos;
        final int count;

        HighlightEntry(BlockPos minPos, BlockPos maxPos, int count) {
            this.minPos = minPos;
            this.maxPos = maxPos;
            this.count = count;
        }

        boolean contains(BlockPos pos) {
            return pos.getX() >= minPos.getX() && pos.getX() <= maxPos.getX()
                    && pos.getY() >= minPos.getY() && pos.getY() <= maxPos.getY()
                    && pos.getZ() >= minPos.getZ() && pos.getZ() <= maxPos.getZ();
        }
    }

    private static final List<HighlightEntry> ACTIVE_ENTRIES = new ArrayList<>();
    private static ItemStack activeStack = ItemStack.EMPTY;
    private static long expireAtMs = 0L;
    private static ResourceKey<Level> activeDimension = null;

    public static synchronized void setHighlights(ItemStack stack, List<BlockPos> minPositions, List<BlockPos> maxPositions, int[] counts, int durationSeconds) {
        ACTIVE_ENTRIES.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || stack == null || stack.isEmpty() || durationSeconds <= 0
                || minPositions == null || maxPositions == null || counts == null || minPositions.isEmpty()) {
            activeStack = ItemStack.EMPTY;
            expireAtMs = 0L;
            activeDimension = null;
            return;
        }

        int n = Math.min(minPositions.size(), Math.min(maxPositions.size(), counts.length));
        for (int i = 0; i < n; i++) {
            ACTIVE_ENTRIES.add(new HighlightEntry(minPositions.get(i).immutable(), maxPositions.get(i).immutable(), counts[i]));
        }
        activeStack = stack.copyWithCount(1);
        expireAtMs = System.currentTimeMillis() + durationSeconds * 1000L;
        activeDimension = mc.level.dimension();
    }

    public static synchronized void onBlockInteracted(BlockPos clickedPos) {
        if (clickedPos == null || ACTIVE_ENTRIES.isEmpty()) return;
        ACTIVE_ENTRIES.removeIf(entry -> entry.contains(clickedPos));
        if (ACTIVE_ENTRIES.isEmpty()) {
            activeStack = ItemStack.EMPTY;
            expireAtMs = 0L;
        }
    }

    public static synchronized void clear() {
        ACTIVE_ENTRIES.clear();
        activeStack = ItemStack.EMPTY;
        expireAtMs = 0L;
        activeDimension = null;
    }

    public static void render(PoseStack poseStack) {
        if (poseStack == null || ACTIVE_ENTRIES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clear();
            return;
        }

        long now = System.currentTimeMillis();
        if (now >= expireAtMs || (activeDimension != null && !mc.level.dimension().equals(activeDimension))) {
            clear();
            return;
        }

        List<HighlightEntry> snapshot;
        ItemStack iconStack;
        long remainingMs;
        synchronized (StorageHighlightRenderer.class) {
            if (ACTIVE_ENTRIES.isEmpty() || activeStack.isEmpty()) return;
            snapshot = new ArrayList<>(ACTIVE_ENTRIES);
            iconStack = activeStack;
            remainingMs = expireAtMs - now;
        }

        float fade = remainingMs < 2500L ? Math.max(0.08f, remainingMs / 2500.0f) : 1.0f;
        float pulse = 0.5f + 0.5f * (float) Math.sin((now % 4000L) * (Math.PI * 2.0 / 1200.0));

        int edgeR = (int) (0 + 60 * pulse);
        int edgeG = (int) (215 + 40 * pulse);
        int edgeB = 255;
        int edgeA = Math.min(255, Math.max(12, Math.round((195 + 55 * pulse) * fade)));
        int fillA = Math.min(255, Math.max(4, Math.round((20 + 18 * pulse) * fade)));

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = mc.getEntityRenderDispatcher().cameraOrientation();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Font font = mc.font;

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer xrayVc = bufferSource.getBuffer(HighlightRenderTypes.XRAY_QUADS);
        Matrix4f worldMat = poseStack.last().pose();

        for (HighlightEntry entry : snapshot) {
            float x0 = entry.minPos.getX() + 0.035f;
            float y0 = entry.minPos.getY() + 0.008f;
            float z0 = entry.minPos.getZ() + 0.035f;
            float x1 = entry.maxPos.getX() + 0.965f;
            float y1 = entry.maxPos.getY() + 0.955f;
            float z1 = entry.maxPos.getZ() + 0.965f;

            drawTranslucentBox(xrayVc, worldMat, x0, y0, z0, x1, y1, z1, edgeR, edgeG, edgeB, fillA);
            drawWireframeBeams(xrayVc, worldMat, x0, y0, z0, x1, y1, z1, 0.018f + 0.005f * pulse, edgeR, edgeG, edgeB, edgeA);
        }

        bufferSource.endBatch(HighlightRenderTypes.XRAY_QUADS);
        poseStack.popPose();

        snapshot.sort((a, b) -> {
            double da = distSqToCenter(camPos, a);
            double db = distSqToCenter(camPos, b);
            return Double.compare(db, da);
        });

        int pillAlpha = Math.min(235, Math.max(20, Math.round(218 * fade)));
        int pillBorderAlpha = Math.min(255, Math.max(25, Math.round(240 * fade)));
        int textAlpha = Math.min(255, Math.max(30, Math.round(255 * fade)));
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        var itemRenderer = mc.getItemRenderer();

        for (int idx = 0; idx < snapshot.size(); idx++) {
            HighlightEntry entry = snapshot.get(idx);
            double worldCx = (entry.minPos.getX() + entry.maxPos.getX() + 1.0) * 0.5;
            double worldCy = entry.maxPos.getY() + 1.30;
            double worldCz = (entry.minPos.getZ() + entry.maxPos.getZ() + 1.0) * 0.5;

            double dx = worldCx - camPos.x;
            double dy = worldCy - camPos.y;
            double dz = worldCz - camPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.35 || dist > 72.0) continue;

            float worldScale = (dist <= 9.0)
                    ? 0.026f
                    : (float) Math.min(0.11, 0.026 * (1.0 + (dist - 9.0) * 0.075));

            String countLabel = "x" + (entry.count >= 10000
                    ? LcdScreenBlockEntity.formatCompactCount(entry.count)
                    : String.valueOf(entry.count));
            int textW = font.width(countLabel);
            float pillW = 20.0f + textW + 7.0f;
            float pillH = 17.0f;
            float left = -pillW * 0.5f;
            float top = -pillH * 0.5f;

            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            poseStack.mulPose(camRot);
            poseStack.scale(-worldScale, -worldScale, worldScale);

            VertexConsumer pillVc = bufferSource.getBuffer(HighlightRenderTypes.XRAY_QUADS);
            Matrix4f pillMat = poseStack.last().pose();

            float t = 1.0f;
            fillRect2D(pillVc, pillMat, left + t, top + t, left + pillW - t, top + pillH - t, 0.0f, 8, 16, 26, pillAlpha);
            drawRectOutline2D(pillVc, pillMat, left, top, pillW, pillH, t, 0.02f, edgeR, edgeG, edgeB, pillBorderAlpha);
            fillRect2D(pillVc, pillMat, -2.5f, top + pillH, 2.5f, top + pillH + 2.5f, 0.02f, edgeR, edgeG, edgeB, pillBorderAlpha);
            bufferSource.endBatch(HighlightRenderTypes.XRAY_QUADS);

            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.08f);
            font.drawInBatch(
                    countLabel,
                    left + 19.5f,
                    top + 4.5f,
                    textColor,
                    false,
                    poseStack.last().pose(),
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    15728880
            );
            bufferSource.endBatch();
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(left + 10.0f, top + pillH * 0.5f, 0.35f);
            poseStack.last().pose().scale(12.5f, -12.5f, 4.0f);
            poseStack.last().normal().scale(1.0f, -1.0f, 1.0f);
            itemRenderer.renderStatic(
                    iconStack,
                    ItemDisplayContext.GUI,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    mc.level,
                    idx
            );
            poseStack.popPose();
            try {
                GL11.glDepthRange(0.01, 0.45);
                bufferSource.endBatch();
            } finally {
                GL11.glDepthRange(0.0, 1.0);
            }

            poseStack.popPose();
        }
    }

    private static double distSqToCenter(Vec3 camPos, HighlightEntry entry) {
        double cx = (entry.minPos.getX() + entry.maxPos.getX() + 1.0) * 0.5 - camPos.x;
        double cy = (entry.minPos.getY() + entry.maxPos.getY() + 1.0) * 0.5 - camPos.y;
        double cz = (entry.minPos.getZ() + entry.maxPos.getZ() + 1.0) * 0.5 - camPos.z;
        return cx * cx + cy * cy + cz * cz;
    }

    private static void fillRect2D(VertexConsumer vc, Matrix4f mat, float x0, float y0, float x1, float y1, float z, int r, int g, int b, int a) {
        vc.vertex(mat, x0, y1, z).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, z).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y0, z).color(r, g, b, a).endVertex();
    }

    private static void drawRectOutline2D(VertexConsumer vc, Matrix4f mat, float x, float y, float w, float h, float t, float z, int r, int g, int b, int a) {
        fillRect2D(vc, mat, x, y, x + w, y + t, z, r, g, b, a);
        fillRect2D(vc, mat, x, y + h - t, x + w, y + h, z, r, g, b, a);
        fillRect2D(vc, mat, x, y + t, x + t, y + h - t, z, r, g, b, a);
        fillRect2D(vc, mat, x + w - t, y + t, x + w, y + h - t, z, r, g, b, a);
    }

    private static void drawTranslucentBox(VertexConsumer vc, Matrix4f mat,
                                           float x0, float y0, float z0, float x1, float y1, float z1,
                                           int r, int g, int b, int a) {
        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();

        vc.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();

        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();

        vc.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();

        vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();

        vc.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
    }

    private static void drawWireframeBeams(VertexConsumer vc, Matrix4f mat,
                                           float x0, float y0, float z0, float x1, float y1, float z1,
                                           float t, int r, int g, int b, int a) {
        drawTranslucentBox(vc, mat, x0 - t, y0 - t, z0 - t, x1 + t, y0 + t, z0 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x0 - t, y0 - t, z1 - t, x1 + t, y0 + t, z1 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x0 - t, y1 - t, z0 - t, x1 + t, y1 + t, z0 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x0 - t, y1 - t, z1 - t, x1 + t, y1 + t, z1 + t, r, g, b, a);

        drawTranslucentBox(vc, mat, x0 - t, y0 - t, z0 - t, x0 + t, y0 + t, z1 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x1 - t, y0 - t, z0 - t, x1 + t, y0 + t, z1 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x0 - t, y1 - t, z0 - t, x0 + t, y1 + t, z1 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x1 - t, y1 - t, z0 - t, x1 + t, y1 + t, z1 + t, r, g, b, a);

        drawTranslucentBox(vc, mat, x0 - t, y0 - t, z0 - t, x0 + t, y1 + t, z0 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x1 - t, y0 - t, z0 - t, x1 + t, y1 + t, z0 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x0 - t, y0 - t, z1 - t, x0 + t, y1 + t, z1 + t, r, g, b, a);
        drawTranslucentBox(vc, mat, x1 - t, y0 - t, z1 - t, x1 + t, y1 + t, z1 + t, r, g, b, a);
    }
}
