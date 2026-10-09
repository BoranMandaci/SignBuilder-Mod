package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignRotation;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.render.cache.LetterRenderCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

public class LetterBlockEntityRenderer implements BlockEntityRenderer<LetterBlockEntity> {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<MaterialModelKey, BakedModel> MATERIAL_MODELS = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<MaterialModelKey, BakedModel> eldest) {
            return size() > 256;
        }
    });
    private static long rainbowColorSlot = Long.MIN_VALUE;
    private static int rainbowColor;

    private record MaterialModelKey(BakedModel model, SignMaterial front, SignMaterial back, boolean letter) {}

    public LetterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    private int calculateRainbowColor() {
        long currentTime = System.currentTimeMillis();
        long slot = currentTime / 33L;
        if (slot != rainbowColorSlot) {
            float hue = (currentTime % 3000L) / 3000.0f;
            rainbowColor = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
            rainbowColorSlot = slot;
        }
        return rainbowColor;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRenderOffScreen(LetterBlockEntity blockEntity) {
        return true;
    }

    public net.minecraft.world.phys.AABB getRenderBoundingBox(LetterBlockEntity blockEntity) {
        return new net.minecraft.world.phys.AABB(blockEntity.getBlockPos()).inflate(256.0);
    }

    public net.minecraft.world.phys.AABB getViewBoundingBox(LetterBlockEntity blockEntity) {
        return new net.minecraft.world.phys.AABB(blockEntity.getBlockPos()).inflate(256.0);
    }


    @Override
    public void render(LetterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (entity.isLinkedMultiblockPart()) {
            LetterBlockEntity master = entity.getEffectiveMaster();
            if (master != null && master != entity) {
                
                net.minecraft.core.BlockPos offset = master.getBlockPos().subtract(entity.getBlockPos());
                poseStack.pushPose();
                poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
                render(master, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
                poseStack.popPose();
            }
            return;
        }

        if (entity.getLevel() == null) return;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        LetterRenderCache cache = LetterRenderCache.getOrCreate(entity, dispatcher);
        if (cache == null) return;

        VertexConsumer buffer = bufferSource.getBuffer(cache.renderType);
        int light = entity.isActive() ? 15728880 : LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos());

        boolean usesRainbow = entity.isRainbow()
                || entity.isBackplateFrontRainbow()
                || entity.isBackplateBackRainbow()
                || (cache.hasBackGlyph && cache.backRainbow);
        int currentRainbow = usesRainbow ? calculateRainbowColor() : 0;

        if (cache.isBackplateBlock) {
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();

            int fColor = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            applyRotation(poseStack, cache.state, entity, true);
            if (cache.standalonePlateCenterLocalX != 0f || cache.standalonePlateCenterLocalZ != 0f) {
                poseStack.translate(cache.standalonePlateCenterLocalX, 0.0, cache.standalonePlateCenterLocalZ);
            }
            if (cache.standaloneWidenDiagonal) {
                ModelCentering.widenDiagonalPose(poseStack, cache.standaloneFacing, entity.getFacingRotation());
            }

            renderPlateQuads(poseStack.last(), buffer, cache.standalonePlateQuads, fColor, bColor, light, packedOverlay);
            poseStack.popPose();
            return;
        }

        poseStack.pushPose();
        applyRotation(poseStack, cache.state, entity, cache.size <= 1);
        poseStack.translate(cache.offsetX, 0.0, cache.offsetZ);
        if (cache.size > 1) poseStack.scale((float) cache.modelScale, (float) cache.modelScale, (float) cache.modelScale);

        if (cache.hasBackplate) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();

            int fColor = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            if (!cache.wall) {
                poseStack.translate(cache.attachedPlateCenterLocalX, 0.0, cache.attachedPlateCenterLocalZ);
                if (cache.attachedPlateWidenDiagonal) {
                    ModelCentering.widenDiagonalPose(poseStack, cache.attachedPlateFacing, entity.getFacingRotation());
                }
            }
            renderPlateQuads(poseStack.last(), buffer, cache.attachedPlateQuads, fColor, bColor, light, packedOverlay);
            poseStack.popPose();
        }

        SignMaterial letterMat = entity.getSavedMaterial();
        int rawLetterCol = entity.getRgbColor();
        int letterCol = (letterMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isRainbow() ? currentRainbow : (rawLetterCol == 0 ? 0xFFFFFF : rawLetterCol));

        poseStack.pushPose();
        if (!cache.wall) {
            poseStack.translate(cache.letterCenterLocalX, 0.0, cache.letterCenterLocalZ);
        } else if (cache.letterWallTransX != 0f || cache.letterWallTransZ != 0f) {
            poseStack.translate(cache.letterWallTransX, 0.0, cache.letterWallTransZ);
        }
        applyPressedTransform(poseStack, entity, cache.face, cache.facing);

        float lr = ((letterCol >> 16) & 0xFF) / 255.0f;
        float lg = ((letterCol >> 8) & 0xFF) / 255.0f;
        float lb = (letterCol & 0xFF) / 255.0f;

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                cache.state,
                cache.finalLetterModel,
                lr, lg, lb,
                light,
                packedOverlay
        );
        poseStack.popPose();

        if (cache.hasBackGlyph) {
            int backColor = cache.backIsMaterial ? 0xFFFFFF : (cache.backRainbow ? currentRainbow : cache.backRawColor);
            poseStack.pushPose();
            if (!cache.wall) {
                poseStack.translate(cache.backCenterLocalX, 0.0, cache.backCenterLocalZ);
            } else if (cache.backWallTransX != 0f || cache.backWallTransZ != 0f) {
                poseStack.translate(cache.backWallTransX, 0.0, cache.backWallTransZ);
            }
            applyPressedTransform(poseStack, entity, cache.face, cache.facing.getOpposite());
            float br = ((backColor >> 16) & 0xFF) / 255.0f;
            float bg = ((backColor >> 8) & 0xFF) / 255.0f;
            float bb = (backColor & 0xFF) / 255.0f;
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer, cache.backState, cache.finalBackModel, br, bg, bb, light, packedOverlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void applyPressedTransform(PoseStack poseStack, LetterBlockEntity entity, AttachFace face, Direction facing) {
        if (!entity.isPressed() || face != AttachFace.WALL) return;
        float wallPlane = (facing == Direction.NORTH || facing == Direction.WEST) ? 1.0f : 0.0f;
        float depthScale = 2.0f / 3.0f;
        if (facing.getAxis() == Direction.Axis.Z) {
            poseStack.translate(0, 0, wallPlane);
            poseStack.scale(1.0f, 1.0f, depthScale);
            poseStack.translate(0, 0, -wallPlane);
        } else {
            poseStack.translate(wallPlane, 0, 0);
            poseStack.scale(depthScale, 1.0f, 1.0f);
            poseStack.translate(-wallPlane, 0, 0);
        }
    }

    public static BakedModel getMaterialModel(BakedModel model, SignMaterial front, SignMaterial back, boolean letter) {
        return MATERIAL_MODELS.computeIfAbsent(new MaterialModelKey(model, front, back, letter),
                key -> new MaterialBakedModel(key.model(), key.front(), key.back(), key.letter()));
    }

    private static void applyRotation(PoseStack poseStack, BlockState state, LetterBlockEntity entity, boolean blockCentered) {
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return;
        }
        int degrees = SignRotation.deltaDegrees(entity.getFacingRotation(), state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        if (degrees == 0) {
            return;
        }
        if (blockCentered) {
            poseStack.translate(0.5, 0.0, 0.5);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        if (blockCentered) {
            poseStack.translate(-0.5, 0.0, -0.5);
        }
    }

    public static List<BakedQuad> getAllQuads(BakedModel model, BlockState state) {
        if (model == null) return Collections.emptyList();
        RandomSource random = RandomSource.create(42L);
        List<BakedQuad> quads = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            random.setSeed(42L);
            quads.addAll(model.getQuads(state, DIRECTIONS[i], random));
        }
        random.setSeed(42L);
        quads.addAll(model.getQuads(state, null, random));
        return quads;
    }

    public static void renderPlateQuads(PoseStack.Pose pose, VertexConsumer buffer, List<BakedQuad> quads, int frontColor, int backColor, int light, int overlay) {
        renderPlateQuadList(pose, buffer, quads, frontColor, backColor, light, overlay);
    }

    public static void renderPlateQuads(PoseStack.Pose pose, VertexConsumer buffer, BakedModel model, BlockState state, int frontColor, int backColor, int light, int overlay) {
        for (int i = 0; i < 6; i++) {
            RANDOM.setSeed(42L);
            renderPlateQuadList(pose, buffer, model.getQuads(state, DIRECTIONS[i], RANDOM), frontColor, backColor, light, overlay);
        }
        RANDOM.setSeed(42L);
        renderPlateQuadList(pose, buffer, model.getQuads(state, null, RANDOM), frontColor, backColor, light, overlay);
    }

    private static void renderPlateQuadList(PoseStack.Pose pose, VertexConsumer buffer, List<BakedQuad> quads, int frontColor, int backColor, int light, int overlay) {
        int quadCount = quads.size();
        for (int i = 0; i < quadCount; i++) {
            BakedQuad quad = quads.get(i);
            int color = 0xFFFFFF;
            if (quad.getTintIndex() == 0) {
                color = frontColor;
            } else if (quad.getTintIndex() == 1) {
                color = backColor;
            }

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            buffer.putBulkData(pose, quad, r, g, b, 1.0F, light, overlay);
        }
    }
}
