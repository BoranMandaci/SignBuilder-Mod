package com.boran.signbuilder.client.render.cache;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.SignRotation;
import com.boran.signbuilder.block.entity.LetterBlockEntity;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.render.ModelCentering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Collections;
import java.util.List;

public class LetterRenderCache {

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

    public final long version;
    public final Object modelManagerRef;

    public final boolean isBackplateBlock;
    public final BlockState state;
    public final RenderType renderType;

    public final BakedModel standalonePlateModel;
    public final List<BakedQuad> standalonePlateQuads;
    public final float standalonePlateCenterLocalX;
    public final float standalonePlateCenterLocalZ;
    public final boolean standaloneWidenDiagonal;
    public final Direction standaloneFacing;
    public final int standaloneRotationDegrees;

    public final AttachFace face;
    public final Direction facing;
    public final int size;
    public final boolean wall;
    public final double offsetX, offsetZ;
    public final int modelScale;
    public final int rotationDegrees;

    public final boolean hasBackplate;
    public final BlockState attachedPlateState;
    public final BakedModel attachedPlateModel;
    public final List<BakedQuad> attachedPlateQuads;
    public final float attachedPlateCenterLocalX;
    public final float attachedPlateCenterLocalZ;
    public final boolean attachedPlateWidenDiagonal;
    public final Direction attachedPlateFacing;

    public final BakedModel finalLetterModel;
    public final float letterCenterLocalX;
    public final float letterCenterLocalZ;
    public final float letterWallTransX;
    public final float letterWallTransZ;
    public final boolean isLetterWall;

    public final boolean hasBackGlyph;
    public final BlockState backState;
    public final BakedModel finalBackModel;
    public final float backCenterLocalX;
    public final float backCenterLocalZ;
    public final float backWallTransX;
    public final float backWallTransZ;
    public final int backRawColor;
    public final boolean backRainbow;
    public final boolean backIsMaterial;

    public LetterRenderCache(long version, Object modelManagerRef, boolean isBackplateBlock,
                             BlockState state, RenderType renderType,
                             BakedModel standalonePlateModel, List<BakedQuad> standalonePlateQuads,
                             float standalonePlateCenterLocalX, float standalonePlateCenterLocalZ,
                             boolean standaloneWidenDiagonal, Direction standaloneFacing, int standaloneRotationDegrees,
                             AttachFace face, Direction facing, int size, boolean wall,
                             double offsetX, double offsetZ, int modelScale, int rotationDegrees,
                             boolean hasBackplate, BlockState attachedPlateState, BakedModel attachedPlateModel,
                             List<BakedQuad> attachedPlateQuads, float attachedPlateCenterLocalX, float attachedPlateCenterLocalZ,
                             boolean attachedPlateWidenDiagonal, Direction attachedPlateFacing,
                             BakedModel finalLetterModel, float letterCenterLocalX, float letterCenterLocalZ,
                             float letterWallTransX, float letterWallTransZ, boolean isLetterWall,
                             boolean hasBackGlyph, BlockState backState, BakedModel finalBackModel,
                             float backCenterLocalX, float backCenterLocalZ,
                             float backWallTransX, float backWallTransZ,
                             int backRawColor, boolean backRainbow, boolean backIsMaterial) {
        this.version = version;
        this.modelManagerRef = modelManagerRef;
        this.isBackplateBlock = isBackplateBlock;
        this.state = state;
        this.renderType = renderType;
        this.standalonePlateModel = standalonePlateModel;
        this.standalonePlateQuads = standalonePlateQuads;
        this.standalonePlateCenterLocalX = standalonePlateCenterLocalX;
        this.standalonePlateCenterLocalZ = standalonePlateCenterLocalZ;
        this.standaloneWidenDiagonal = standaloneWidenDiagonal;
        this.standaloneFacing = standaloneFacing;
        this.standaloneRotationDegrees = standaloneRotationDegrees;
        this.face = face;
        this.facing = facing;
        this.size = size;
        this.wall = wall;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.modelScale = modelScale;
        this.rotationDegrees = rotationDegrees;
        this.hasBackplate = hasBackplate;
        this.attachedPlateState = attachedPlateState;
        this.attachedPlateModel = attachedPlateModel;
        this.attachedPlateQuads = attachedPlateQuads;
        this.attachedPlateCenterLocalX = attachedPlateCenterLocalX;
        this.attachedPlateCenterLocalZ = attachedPlateCenterLocalZ;
        this.attachedPlateWidenDiagonal = attachedPlateWidenDiagonal;
        this.attachedPlateFacing = attachedPlateFacing;
        this.finalLetterModel = finalLetterModel;
        this.letterCenterLocalX = letterCenterLocalX;
        this.letterCenterLocalZ = letterCenterLocalZ;
        this.letterWallTransX = letterWallTransX;
        this.letterWallTransZ = letterWallTransZ;
        this.isLetterWall = isLetterWall;
        this.hasBackGlyph = hasBackGlyph;
        this.backState = backState;
        this.finalBackModel = finalBackModel;
        this.backCenterLocalX = backCenterLocalX;
        this.backCenterLocalZ = backCenterLocalZ;
        this.backWallTransX = backWallTransX;
        this.backWallTransZ = backWallTransZ;
        this.backRawColor = backRawColor;
        this.backRainbow = backRainbow;
        this.backIsMaterial = backIsMaterial;
    }

    public static LetterRenderCache getOrCreate(LetterBlockEntity entity, BlockRenderDispatcher dispatcher) {
        Object currentManager = Minecraft.getInstance().getModelManager();
        if (entity.clientRenderCache instanceof LetterRenderCache cache) {
            if (cache.version == entity.getRenderVersion() && cache.modelManagerRef == currentManager) {
                return cache;
            }
        }

        LetterRenderCache newCache = build(entity, dispatcher, currentManager);
        entity.clientRenderCache = newCache;
        return newCache;
    }

    private static LetterRenderCache build(LetterBlockEntity entity, BlockRenderDispatcher dispatcher, Object modelManagerRef) {
        BlockState state = entity.getBlockState();
        RenderType renderType = ItemBlockRenderTypes.getRenderType(state, false);

        if (state.getBlock() instanceof BackplateBlock) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            BakedModel baseModel = dispatcher.getBlockModel(state);
            BakedModel finalModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? LetterBlockEntityRenderer.getMaterialModel(baseModel, fMat, bMat, false)
                    : baseModel;
            List<BakedQuad> quads = LetterBlockEntityRenderer.getAllQuads(finalModel, state);

            int rotationDegrees = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    ? SignRotation.deltaDegrees(entity.getFacingRotation(), state.getValue(BlockStateProperties.HORIZONTAL_FACING))
                    : 0;

            float plateCenterLocalX = 0f;
            float plateCenterLocalZ = 0f;
            boolean widenDiagonal = false;
            Direction facing = state.getValue(BackplateBlock.FACING);

            if (state.getValue(BackplateBlock.FACE) != AttachFace.WALL) {
                double[] offsets = ModelCentering.getCenterOffset(finalModel, state, rotationDegrees, true, 1, 0.0, 0.0, 0.5, 0.5);
                plateCenterLocalX = (float) offsets[0];
                plateCenterLocalZ = (float) offsets[1];
                widenDiagonal = (entity.getFacingRotation() & 1) != 0;
            }

            return new LetterRenderCache(
                    entity.getRenderVersion(), modelManagerRef, true, state, renderType,
                    finalModel, quads, plateCenterLocalX, plateCenterLocalZ, widenDiagonal, facing, rotationDegrees,
                    AttachFace.WALL, Direction.NORTH, 1, true, 0.0, 0.0, 1, 0,
                    false, null, null, Collections.emptyList(), 0f, 0f, false, Direction.NORTH,
                    null, 0f, 0f, 0f, 0f, true,
                    false, null, null, 0f, 0f, 0f, 0f, 0xFFFFFF, false, false
            );
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

        boolean hasBackplate = entity.hasBackplate();
        BlockState attachedPlateState = null;
        BakedModel attachedPlateModel = null;
        List<BakedQuad> attachedPlateQuads = Collections.emptyList();
        float attachedPlateCenterLocalX = 0f;
        float attachedPlateCenterLocalZ = 0f;
        boolean attachedPlateWidenDiagonal = false;
        Direction plateFacing = (face != AttachFace.WALL) ? facing.getCounterClockWise() : facing;

        if (hasBackplate) {
            SignMaterial fMat = entity.getBackplateFrontMaterial();
            SignMaterial bMat = entity.getBackplateBackMaterial();
            attachedPlateState = ModBlocks.BACKPLATE.get().defaultBlockState()
                    .setValue(BackplateBlock.FACING, plateFacing)
                    .setValue(BackplateBlock.FACE, face);
            BakedModel basePlateModel = dispatcher.getBlockModel(attachedPlateState);
            attachedPlateModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? LetterBlockEntityRenderer.getMaterialModel(basePlateModel, fMat, bMat, false)
                    : basePlateModel;
            attachedPlateQuads = LetterBlockEntityRenderer.getAllQuads(attachedPlateModel, attachedPlateState);

            if (!wall) {
                double[] pOffsets = ModelCentering.getCenterOffset(attachedPlateModel, attachedPlateState, rotationDegrees, size <= 1, modelScale, offsetX, offsetZ, groupCenterX, groupCenterZ);
                attachedPlateCenterLocalX = (float) pOffsets[0];
                attachedPlateCenterLocalZ = (float) pOffsets[1];
                attachedPlateWidenDiagonal = (entity.getFacingRotation() & 1) != 0;
            }
        }

        BakedModel letterModel = dispatcher.getBlockModel(state);
        SignMaterial letterMat = entity.getSavedMaterial();
        BakedModel finalLetterModel = (letterMat != SignMaterial.DEFAULT)
                ? LetterBlockEntityRenderer.getMaterialModel(letterModel, letterMat, SignMaterial.DEFAULT, true)
                : letterModel;

        ModelCentering.Center letterTarget = hasBackplate && !wall
                ? new ModelCentering.Center(groupCenterX + normalX * plateClearance, groupCenterZ + normalZ * plateClearance)
                : new ModelCentering.Center(groupCenterX, groupCenterZ);
        if (!wall && LetterBlock.isLateralDot(state)) {
            ModelCentering.Center currentCenter = ModelCentering.transformedCenter(finalLetterModel, state, rotationDegrees,
                    size <= 1, modelScale, offsetX, offsetZ);
            letterTarget = ModelCentering.centerAlongNormal(currentCenter, letterTarget.x(), letterTarget.z(), normalX, normalZ);
        }

        float letterCenterLocalX = 0f;
        float letterCenterLocalZ = 0f;
        float letterWallTransX = 0f;
        float letterWallTransZ = 0f;

        if (!wall) {
            double[] lOffsets = ModelCentering.getCenterOffset(finalLetterModel, state, rotationDegrees, size <= 1, modelScale, offsetX, offsetZ, letterTarget.x(), letterTarget.z());
            letterCenterLocalX = (float) lOffsets[0];
            letterCenterLocalZ = (float) lOffsets[1];
        } else if (hasBackplate) {
            letterWallTransX = (float) (normalX * plateClearance / modelScale);
            letterWallTransZ = (float) (normalZ * plateClearance / modelScale);
        }

        ItemStack backGlyph = entity.getBackGlyph();
        boolean hasBackGlyph = false;
        BlockState backState = null;
        BakedModel finalBackModel = null;
        float backCenterLocalX = 0f;
        float backCenterLocalZ = 0f;
        float backWallTransX = 0f;
        float backWallTransZ = 0f;
        int backRawColor = 0xFFFFFF;
        boolean backRainbow = false;
        boolean backIsMaterial = false;

        if (!wall && !backGlyph.isEmpty() && backGlyph.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LetterBlock) {
            Block backBlock = blockItem.getBlock();
            backState = backBlock.defaultBlockState()
                    .setValue(LetterBlock.FACE, face)
                    .setValue(LetterBlock.FACING, facing.getOpposite());
            CompoundTag backTag = backGlyph.getTagElement("BlockEntityTag");
            SignMaterial backMaterial = readMaterial(backTag, "SavedMaterial");
            if (backState.hasProperty(LetterBlock.MATERIAL)) {
                CompoundTag stateTag = backGlyph.getTagElement("BlockStateTag");
                if (stateTag != null && stateTag.contains("material")) {
                    try {
                        backState = backState.setValue(LetterBlock.MATERIAL, SignMaterial.valueOf(stateTag.getString("material").toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            BakedModel backBaseModel = dispatcher.getBlockModel(backState);
            finalBackModel = backMaterial != SignMaterial.DEFAULT
                    ? LetterBlockEntityRenderer.getMaterialModel(backBaseModel, backMaterial, SignMaterial.DEFAULT, true)
                    : backBaseModel;

            backIsMaterial = backMaterial != SignMaterial.DEFAULT;
            backRainbow = backTag != null && backTag.getBoolean("IsRainbow");
            backRawColor = backMaterial != SignMaterial.DEFAULT ? 0xFFFFFF
                    : (backTag != null && backTag.contains("RGBColor") ? backTag.getInt("RGBColor") : 0xFFFFFF);

            int backRotation = Math.floorMod(entity.getFacingRotation() + 4, 8);
            int backRotationDegrees = SignRotation.deltaDegrees(backRotation, backState.getValue(LetterBlock.FACING));
            ModelCentering.Center backTarget = hasBackplate
                    ? new ModelCentering.Center(groupCenterX - normalX * plateClearance, groupCenterZ - normalZ * plateClearance)
                    : new ModelCentering.Center(groupCenterX - normalX * 2.0 * 1.0 / 16.0, groupCenterZ - normalZ * 2.0 * 1.0 / 16.0);
            if (LetterBlock.isLateralDot(backState)) {
                ModelCentering.Center currentCenter = ModelCentering.transformedCenter(finalBackModel, backState, backRotationDegrees,
                        size <= 1, modelScale, offsetX, offsetZ);
                backTarget = ModelCentering.centerAlongNormal(currentCenter, backTarget.x(), backTarget.z(), normalX, normalZ);
            }

            double[] bOffsets = ModelCentering.getCenterOffset(finalBackModel, backState, backRotationDegrees, size <= 1, modelScale, offsetX, offsetZ, backTarget.x(), backTarget.z());
            backCenterLocalX = (float) bOffsets[0];
            backCenterLocalZ = (float) bOffsets[1];
            hasBackGlyph = true;
        }

        return new LetterRenderCache(
                entity.getRenderVersion(), modelManagerRef, false, state, renderType,
                null, Collections.emptyList(), 0f, 0f, false, Direction.NORTH, 0,
                face, facing, size, wall, offsetX, offsetZ, modelScale, rotationDegrees,
                hasBackplate, attachedPlateState, attachedPlateModel, attachedPlateQuads,
                attachedPlateCenterLocalX, attachedPlateCenterLocalZ, attachedPlateWidenDiagonal, plateFacing,
                finalLetterModel, letterCenterLocalX, letterCenterLocalZ, letterWallTransX, letterWallTransZ, wall,
                hasBackGlyph, backState, finalBackModel, backCenterLocalX, backCenterLocalZ,
                backWallTransX, backWallTransZ, backRawColor, backRainbow, backIsMaterial
        );
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

    private static SignMaterial readMaterial(CompoundTag tag, String key) {
        if (tag == null || !tag.contains(key)) return SignMaterial.DEFAULT;
        try {
            return SignMaterial.valueOf(tag.getString(key).toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return SignMaterial.DEFAULT;
        }
    }
}
