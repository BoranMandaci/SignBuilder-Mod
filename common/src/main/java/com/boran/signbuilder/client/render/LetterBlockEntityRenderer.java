package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignRotation;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
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
    private static final double[] OFFSET_ZERO = {0.0, 0.0};
    private static final double[] OFFSET_NEG_1_NEG_1 = {-1.0, -1.0};
    private static final double[] OFFSET_NEG_1_ZERO = {-1.0, 0.0};
    private static final double[] OFFSET_ZERO_NEG_1 = {0.0, -1.0};
    private static final double[] OFFSET_NEG_1_NEG_06875 = {-1.0, -0.6875};
    private static final double[] OFFSET_NEG_06875_ZERO = {-0.6875, 0.0};
    private static final double[] OFFSET_NEG_03125_NEG_1 = {-0.3125, -1.0};
    private static final double[] OFFSET_ZERO_NEG_03125 = {0.0, -0.3125};
    private static final double[] OFFSET_NEG_2_NEG_2 = {-2.0, -2.0};
    private static final double[] OFFSET_ZERO_NEG_2 = {0.0, -2.0};
    private static final double[] OFFSET_NEG_2_ZERO = {-2.0, 0.0};
    private static final double[] OFFSET_NEG_2_NEG_1375 = {-2.0, -1.375};
    private static final double[] OFFSET_NEG_1375_ZERO = {-1.375, 0.0};
    private static final double[] OFFSET_NEG_0625_NEG_2 = {-0.625, -2.0};
    private static final double[] OFFSET_ZERO_NEG_0625 = {0.0, -0.625};
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
        return blockEntity.getSize() > 1;
    }

    @Override
    public void render(LetterBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (entity.isLinkedMultiblockPart()) {
            return;
        }

        BlockState state = entity.getBlockState();
        if (entity.getLevel() == null) return;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, false);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        int light = entity.isActive() ? 15728880 : LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos());
        boolean usesRainbow = entity.isRainbow()
                || entity.isBackplateFrontRainbow()
                || entity.isBackplateBackRainbow()
                || isBackGlyphRainbow(entity.getBackGlyph());
        int currentRainbow = usesRainbow ? calculateRainbowColor() : 0;

        if (state.getBlock() instanceof BackplateBlock) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();

            int fColor = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            applyRotation(poseStack, state, entity, true);
            BakedModel baseModel = dispatcher.getBlockModel(state);
            BakedModel finalModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? getMaterialModel(baseModel, fMat, bMat, false)
                    : baseModel;

            int rotationDegrees = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    ? SignRotation.deltaDegrees(entity.getFacingRotation(), state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                    : 0;
            if (state.getValue(BackplateBlock.FACE) != AttachFace.WALL) {
                ModelCentering.centerPose(poseStack, finalModel, state, rotationDegrees, true, 1, 0.0, 0.0, 0.5, 0.5);
                ModelCentering.widenDiagonalPose(poseStack, state.getValue(BackplateBlock.FACING), entity.getFacingRotation());
            }

            renderPlateQuads(poseStack.last(), buffer, finalModel, state, fColor, bColor, light, packedOverlay);
            poseStack.popPose();
            return;
        }

        AttachFace face = state.hasProperty(BlockStateProperties.ATTACH_FACE) ? state.getValue(BlockStateProperties.ATTACH_FACE) : AttachFace.WALL;
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        int size = Math.max(1, entity.getSize());
        boolean wall = face == AttachFace.WALL;
        double[] offsets = getRenderOffsets(face, facing, size);
        double offsetX = offsets[0];
        double offsetZ = offsets[1];
        int modelScale = size;
        int rotationDegrees = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? SignRotation.deltaDegrees(entity.getFacingRotation(), facing)
                : 0;
        int normalRotation = wall ? entity.getFacingRotation() : Math.floorMod(entity.getFacingRotation() - 2, 8);
        double normalX = SignRotation.facingX(normalRotation);
        double normalZ = SignRotation.facingZ(normalRotation);
        double normalLength = Math.sqrt(normalX * normalX + normalZ * normalZ);
        if (normalLength == 0.0) normalLength = 1.0;
        normalX /= normalLength;
        normalZ /= normalLength;
        double stepX = SignRotation.horizontalStepX(entity.getFacingRotation(), wall);
        double stepZ = SignRotation.horizontalStepZ(entity.getFacingRotation(), wall);
        double groupCenterX = 0.5 + stepX * (size - 1) * 0.5;
        double groupCenterZ = 0.5 + stepZ * (size - 1) * 0.5;
        double plateClearance = (wall ? 1.0 : 2.0) * size / 16.0;

        poseStack.pushPose();
        applyRotation(poseStack, state, entity, entity.getSize() <= 1);
        poseStack.translate(offsetX, 0.0, offsetZ);
        if (size > 1) poseStack.scale((float) modelScale, (float) modelScale, (float) modelScale);

        if (entity.hasBackplate()) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            int rawFColor = entity.getBackplateFrontColor();
            int rawBColor = entity.getBackplateBackColor();

            int fColor = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateFrontRainbow() ? currentRainbow : (rawFColor == 0 ? 0xFFFFFF : rawFColor));
            int bColor = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isBackplateBackRainbow() ? currentRainbow : (rawBColor == 0 ? 0xFFFFFF : rawBColor));

            poseStack.pushPose();
            Direction plateFacing = (face != AttachFace.WALL) ? facing.getCounterClockWise() : facing;

            BlockState plateState = ModBlocks.BACKPLATE.get().defaultBlockState()
                    .setValue(BackplateBlock.FACING, plateFacing)
                    .setValue(BackplateBlock.FACE, face);

            BakedModel basePlateModel = dispatcher.getBlockModel(plateState);
            BakedModel finalPlateModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? getMaterialModel(basePlateModel, fMat, bMat, false)
                    : basePlateModel;

            if (!wall) {
                ModelCentering.centerPose(poseStack, finalPlateModel, plateState, rotationDegrees, size <= 1, modelScale, offsetX, offsetZ, groupCenterX, groupCenterZ);
                ModelCentering.widenDiagonalPose(poseStack, plateFacing, entity.getFacingRotation());
            }
            renderPlateQuads(poseStack.last(), buffer, finalPlateModel, plateState, fColor, bColor, light, packedOverlay);
            poseStack.popPose();
        }

        BakedModel letterModel = dispatcher.getBlockModel(state);
        SignMaterial letterMat = entity.getSavedMaterial();
        int rawLetterCol = entity.getRgbColor();
        int letterCol = (letterMat != SignMaterial.DEFAULT) ? 0xFFFFFF : (entity.isRainbow() ? currentRainbow : (rawLetterCol == 0 ? 0xFFFFFF : rawLetterCol));

        BakedModel finalLetterModel = (letterMat != SignMaterial.DEFAULT)
                ? getMaterialModel(letterModel, letterMat, SignMaterial.DEFAULT, true)
                : letterModel;

        ModelCentering.Center letterTarget = entity.hasBackplate() && !wall
                ? new ModelCentering.Center(groupCenterX + normalX * plateClearance, groupCenterZ + normalZ * plateClearance)
                : new ModelCentering.Center(groupCenterX, groupCenterZ);

        poseStack.pushPose();
        if (!wall) {
            ModelCentering.centerPose(poseStack, finalLetterModel, state, rotationDegrees, size <= 1, modelScale, offsetX, offsetZ, letterTarget.x(), letterTarget.z());
        } else if (entity.hasBackplate()) {
            poseStack.translate(normalX * plateClearance / modelScale, 0.0, normalZ * plateClearance / modelScale);
        }
        applyPressedTransform(poseStack, entity, face, facing);

        float lr = ((letterCol >> 16) & 0xFF) / 255.0f;
        float lg = ((letterCol >> 8) & 0xFF) / 255.0f;
        float lb = (letterCol & 0xFF) / 255.0f;

        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                state,
                finalLetterModel,
                lr, lg, lb,
                light,
                packedOverlay
        );

        poseStack.popPose();

        ItemStack backGlyph = entity.getBackGlyph();
        if (!wall && !backGlyph.isEmpty() && backGlyph.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LetterBlock) {
            Block backBlock = blockItem.getBlock();
            BlockState backState = backBlock.defaultBlockState()
                    .setValue(LetterBlock.FACE, face)
                    .setValue(LetterBlock.FACING, facing.getOpposite());
            CompoundTag backTag = backGlyph.getTagElement("BlockEntityTag");
            SignMaterial backMaterial = readMaterial(backTag, "SavedMaterial");
            if (backState.hasProperty(LetterBlock.MATERIAL)) {
                CompoundTag stateTag = backGlyph.getTagElement("BlockStateTag");
                if (stateTag != null && stateTag.contains("material")) {
                    try {
                        backState = backState.setValue(LetterBlock.MATERIAL, SignMaterial.valueOf(stateTag.getString("material").toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            BakedModel backBaseModel = dispatcher.getBlockModel(backState);
            BakedModel finalBackModel = backMaterial != SignMaterial.DEFAULT
                    ? getMaterialModel(backBaseModel, backMaterial, SignMaterial.DEFAULT, true)
                    : backBaseModel;
            int backColor = backMaterial != SignMaterial.DEFAULT ? 0xFFFFFF
                    : backTag != null && backTag.getBoolean("IsRainbow") ? currentRainbow
                    : backTag != null && backTag.contains("RGBColor") ? backTag.getInt("RGBColor") : 0xFFFFFF;
            int backRotation = Math.floorMod(entity.getFacingRotation() + 4, 8);
            int backRotationDegrees = SignRotation.deltaDegrees(backRotation, backState.getValue(LetterBlock.FACING));
            ModelCentering.Center backTarget = entity.hasBackplate() && !wall
                    ? new ModelCentering.Center(groupCenterX - normalX * plateClearance, groupCenterZ - normalZ * plateClearance)
                    : new ModelCentering.Center(groupCenterX - normalX * 2.0 * 1.0 / 16.0, groupCenterZ - normalZ * 2.0 * 1.0 / 16.0);

            poseStack.pushPose();
            if (!wall) {
                ModelCentering.centerPose(poseStack, finalBackModel, backState, backRotationDegrees, size <= 1, modelScale,
                        offsetX, offsetZ, backTarget.x(), backTarget.z());
            } else {
                poseStack.translate(-normalX * plateClearance / modelScale, 0.0, -normalZ * plateClearance / modelScale);
            }
            applyPressedTransform(poseStack, entity, face, facing.getOpposite());
            float br = ((backColor >> 16) & 0xFF) / 255.0f;
            float bg = ((backColor >> 8) & 0xFF) / 255.0f;
            float bb = (backColor & 0xFF) / 255.0f;
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer, backState, finalBackModel, br, bg, bb, light, packedOverlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static double[] getRenderOffsets(AttachFace face, Direction facing, int size) {
        if (size == 3) {
            if (face == AttachFace.WALL) {
                return switch (facing) {
                    case NORTH -> OFFSET_NEG_2_NEG_2;
                    case SOUTH -> OFFSET_ZERO;
                    case EAST -> OFFSET_ZERO_NEG_2;
                    case WEST -> OFFSET_NEG_2_ZERO;
                    default -> OFFSET_ZERO;
                };
            }
            return switch (facing) {
                case EAST -> OFFSET_NEG_2_NEG_1375;
                case NORTH -> OFFSET_NEG_1375_ZERO;
                case SOUTH -> OFFSET_NEG_0625_NEG_2;
                case WEST -> OFFSET_ZERO_NEG_0625;
                default -> OFFSET_ZERO;
            };
        }
        if (size == 2) {
            if (face == AttachFace.WALL) {
                return switch (facing) {
                    case NORTH -> OFFSET_NEG_1_NEG_1;
                    case SOUTH -> OFFSET_ZERO;
                    case EAST -> OFFSET_ZERO_NEG_1;
                    case WEST -> OFFSET_NEG_1_ZERO;
                    default -> OFFSET_ZERO;
                };
            }
            return switch (facing) {
                case EAST -> OFFSET_NEG_1_NEG_06875;
                case NORTH -> OFFSET_NEG_06875_ZERO;
                case SOUTH -> OFFSET_NEG_03125_NEG_1;
                case WEST -> OFFSET_ZERO_NEG_03125;
                default -> OFFSET_ZERO;
            };
        }
        return OFFSET_ZERO;
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

    private static boolean isBackGlyphRainbow(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        return tag != null && tag.getBoolean("IsRainbow");
    }

    private static SignMaterial readMaterial(@Nullable CompoundTag tag, String key) {
        if (tag == null || !tag.contains(key)) return SignMaterial.DEFAULT;
        try {
            return SignMaterial.valueOf(tag.getString(key).toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return SignMaterial.DEFAULT;
        }
    }

    private static BakedModel getMaterialModel(BakedModel model, SignMaterial front, SignMaterial back, boolean letter) {
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

    private void renderPlateQuads(PoseStack.Pose pose, VertexConsumer buffer, BakedModel model, BlockState state, int frontColor, int backColor, int light, int overlay) {
        for (int i = 0; i < 6; i++) {
            RANDOM.setSeed(42L);
            renderPlateQuadList(pose, buffer, model.getQuads(state, DIRECTIONS[i], RANDOM), frontColor, backColor, light, overlay);
        }
        RANDOM.setSeed(42L);
        renderPlateQuadList(pose, buffer, model.getQuads(state, null, RANDOM), frontColor, backColor, light, overlay);
    }

    private void renderPlateQuadList(PoseStack.Pose pose, VertexConsumer buffer, List<BakedQuad> quads, int frontColor, int backColor, int light, int overlay) {
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
            buffer.putBulkData(pose, quad, r, g, b, light, overlay);
        }
    }
}
