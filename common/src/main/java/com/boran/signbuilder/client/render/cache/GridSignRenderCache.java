package com.boran.signbuilder.client.render.cache;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.GridSignBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.block.SignRotation;
import com.boran.signbuilder.block.entity.GridSignBlockEntity;
import com.boran.signbuilder.client.grid.GridOffsetHelper;
import com.boran.signbuilder.client.render.LetterBlockEntityRenderer;
import com.boran.signbuilder.client.render.ModelCentering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GridSignRenderCache {

    public static class CachedCell {
        public final BlockState letterState;
        public final BakedModel finalLetterModel;
        public final RenderType renderType;
        public final float totalTransX, totalTransY, totalTransZ;
        public final float pivotX, pivotY, pivotZ;
        public final float scale;
        public final int rawColor;
        public final boolean isRainbow;
        public final boolean isMaterial;

        public CachedCell(BlockState letterState, BakedModel finalLetterModel, RenderType renderType,
                          float totalTransX, float totalTransY, float totalTransZ,
                          float pivotX, float pivotY, float pivotZ,
                          float scale, int rawColor, boolean isRainbow, boolean isMaterial) {
            this.letterState = letterState;
            this.finalLetterModel = finalLetterModel;
            this.renderType = renderType;
            this.totalTransX = totalTransX;
            this.totalTransY = totalTransY;
            this.totalTransZ = totalTransZ;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.pivotZ = pivotZ;
            this.scale = scale;
            this.rawColor = rawColor;
            this.isRainbow = isRainbow;
            this.isMaterial = isMaterial;
        }
    }

    public final long version;
    public final Object modelManagerRef;
    public final AttachFace face;
    public final Direction facing;
    public final int rotationDegrees;

    public final boolean hasBackplate;
    public final BlockState plateState;
    public final BakedModel finalPlateModel;
    public final List<BakedQuad> plateQuads;
    public final float plateCenterLocalX, plateCenterLocalZ;
    public final boolean widenDiagonal;
    public final Direction plateFacing;
    public final SignMaterial fMat;
    public final SignMaterial bMat;

    public final List<CachedCell> cells;

    public final boolean hasBackGlyph;
    public final BlockState backState;
    public final BakedModel finalBackModel;
    public final RenderType backRenderType;
    public final float backTransX, backTransY, backTransZ;
    public final int backRawColor;
    public final boolean backRainbow;
    public final boolean backIsMaterial;

    public final boolean hasFrontGlyph;
    public final BlockState frontState;
    public final BakedModel finalFrontModel;
    public final RenderType frontRenderType;
    public final float frontTransX, frontTransY, frontTransZ;
    public final int frontRawColor;
    public final boolean frontRainbow;
    public final boolean frontIsMaterial;

    public GridSignRenderCache(long version, Object modelManagerRef, AttachFace face, Direction facing, int rotationDegrees,
                               boolean hasBackplate, BlockState plateState, BakedModel finalPlateModel,
                               List<BakedQuad> plateQuads, float plateCenterLocalX, float plateCenterLocalZ,
                               boolean widenDiagonal, Direction plateFacing, SignMaterial fMat, SignMaterial bMat,
                               List<CachedCell> cells,
                               boolean hasBackGlyph, BlockState backState, BakedModel finalBackModel,
                               RenderType backRenderType, float backTransX, float backTransY, float backTransZ,
                               int backRawColor, boolean backRainbow, boolean backIsMaterial,
                               boolean hasFrontGlyph, BlockState frontState, BakedModel finalFrontModel,
                               RenderType frontRenderType, float frontTransX, float frontTransY, float frontTransZ,
                               int frontRawColor, boolean frontRainbow, boolean frontIsMaterial) {
        this.version = version;
        this.modelManagerRef = modelManagerRef;
        this.face = face;
        this.facing = facing;
        this.rotationDegrees = rotationDegrees;
        this.hasBackplate = hasBackplate;
        this.plateState = plateState;
        this.finalPlateModel = finalPlateModel;
        this.plateQuads = plateQuads;
        this.plateCenterLocalX = plateCenterLocalX;
        this.plateCenterLocalZ = plateCenterLocalZ;
        this.widenDiagonal = widenDiagonal;
        this.plateFacing = plateFacing;
        this.fMat = fMat;
        this.bMat = bMat;
        this.cells = cells;
        this.hasBackGlyph = hasBackGlyph;
        this.backState = backState;
        this.finalBackModel = finalBackModel;
        this.backRenderType = backRenderType;
        this.backTransX = backTransX;
        this.backTransY = backTransY;
        this.backTransZ = backTransZ;
        this.backRawColor = backRawColor;
        this.backRainbow = backRainbow;
        this.backIsMaterial = backIsMaterial;
        this.hasFrontGlyph = hasFrontGlyph;
        this.frontState = frontState;
        this.finalFrontModel = finalFrontModel;
        this.frontRenderType = frontRenderType;
        this.frontTransX = frontTransX;
        this.frontTransY = frontTransY;
        this.frontTransZ = frontTransZ;
        this.frontRawColor = frontRawColor;
        this.frontRainbow = frontRainbow;
        this.frontIsMaterial = frontIsMaterial;
    }

    public static GridSignRenderCache getOrCreate(GridSignBlockEntity entity, BlockRenderDispatcher dispatcher) {
        Object currentManager = Minecraft.getInstance().getModelManager();
        if (entity.clientRenderCache instanceof GridSignRenderCache cache) {
            if (cache.version == entity.getRenderVersion() && cache.modelManagerRef == currentManager) {
                return cache;
            }
        }

        GridSignRenderCache newCache = build(entity, dispatcher, currentManager);
        entity.clientRenderCache = newCache;
        return newCache;
    }

    private static GridSignRenderCache build(GridSignBlockEntity entity, BlockRenderDispatcher dispatcher, Object modelManagerRef) {
        BlockState state = entity.getBlockState();
        AttachFace face = state.hasProperty(GridSignBlock.FACE) ? state.getValue(GridSignBlock.FACE) : AttachFace.WALL;
        Direction facing = state.hasProperty(GridSignBlock.FACING) ? state.getValue(GridSignBlock.FACING) : Direction.NORTH;
        int rotationDegrees = SignRotation.deltaDegrees(entity.getFacingRotation(), facing);
        int gridSize = Math.max(1, entity.getGridSize());
        float scale = 1.0f / gridSize;

        boolean hasBackplate = entity.hasBackplate();
        BlockState plateState = null;
        BakedModel finalPlateModel = null;
        List<BakedQuad> plateQuads = Collections.emptyList();
        float plateCenterLocalX = 0f;
        float plateCenterLocalZ = 0f;
        boolean widenDiagonal = false;
        Direction plateFacing = facing;
        SignMaterial fMat = entity.getBackplateFrontMaterial();
        SignMaterial bMat = entity.getBackplateBackMaterial();

        if (hasBackplate) {
            plateState = ModBlocks.BACKPLATE.get().defaultBlockState()
                    .setValue(BackplateBlock.FACING, facing)
                    .setValue(BackplateBlock.FACE, face);
            BakedModel basePlateModel = dispatcher.getBlockModel(plateState);
            finalPlateModel = (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT)
                    ? LetterBlockEntityRenderer.getMaterialModel(basePlateModel, fMat, bMat, false)
                    : basePlateModel;
            plateQuads = LetterBlockEntityRenderer.getAllQuads(finalPlateModel, plateState);

            if (face != AttachFace.WALL) {
                double[] centerOffsets = ModelCentering.getCenterOffset(finalPlateModel, plateState, rotationDegrees, true, 1, 0.0, 0.0, 0.5, 0.5);
                plateCenterLocalX = (float) centerOffsets[0];
                plateCenterLocalZ = (float) centerOffsets[1];
                widenDiagonal = (entity.getFacingRotation() & 1) != 0;
                plateFacing = plateState.getValue(BackplateBlock.FACING);
            }
        }

        List<CachedCell> cellList = new ArrayList<>();
        for (Map.Entry<Integer, GridSignBlockEntity.CellData> entry : entity.getCells().entrySet()) {
            GridSignBlockEntity.CellData data = entry.getValue();

            Block letterBlock = BuiltInRegistries.BLOCK.get(new ResourceLocation("signbuilder", data.character));
            if (letterBlock == null || letterBlock == Blocks.AIR) continue;

            boolean isBack = data.isBack;
            int cellGridSize = data.gridSize > 0 ? data.gridSize : gridSize;
            int col = data.col;
            int row = data.row;
            float cellScale = 1.0f / cellGridSize;

            Direction effectiveFacing = isBack ? facing.getOpposite() : facing;
            Direction cellFacing = (face != AttachFace.WALL) ? effectiveFacing.getClockWise() : effectiveFacing;
            AttachFace cellFace = (face != AttachFace.WALL) ? face : AttachFace.WALL;

            BlockState letterState = letterBlock.defaultBlockState();
            if (letterState.hasProperty(LetterBlock.FACING)) {
                letterState = letterState.setValue(LetterBlock.FACING, cellFacing);
            }
            if (letterState.hasProperty(LetterBlock.FACE)) {
                letterState = letterState.setValue(LetterBlock.FACE, cellFace);
            }
            if (letterState.hasProperty(LetterBlock.MATERIAL)) {
                letterState = letterState.setValue(LetterBlock.MATERIAL, data.material);
            }
            if (letterState.hasProperty(LetterBlock.LIGHT_MODE)) {
                letterState = letterState.setValue(LetterBlock.LIGHT_MODE, data.lightMode);
            }

            float colShift = (cellGridSize - 1) / 2.0f - col;
            float rowShift = (cellGridSize - 1) / 2.0f - row;
            if (rotationDegrees != 0 && face != AttachFace.WALL) {
                colShift *= 1.4142135f;
            }

            Vector3f offsets = GridOffsetHelper.getOffset(face, effectiveFacing, colShift, rowShift, cellScale);
            Vector3f baseTrans = GridOffsetHelper.getBaseTranslation(face, effectiveFacing, isBack);
            Vector3f cl = GridOffsetHelper.getClearance(face, effectiveFacing, hasBackplate);
            Vector3f c = GridOffsetHelper.getCenter(face, effectiveFacing);

            float totalTransX = baseTrans.x + offsets.x + cl.x;
            float totalTransY = baseTrans.y + offsets.y + cl.y;
            float totalTransZ = baseTrans.z + offsets.z + cl.z;

            RenderType renderType = ItemBlockRenderTypes.getRenderType(letterState, false);
            BakedModel baseModel = dispatcher.getBlockModel(letterState);
            BakedModel finalLetterModel = (data.material != SignMaterial.DEFAULT)
                    ? LetterBlockEntityRenderer.getMaterialModel(baseModel, data.material, SignMaterial.DEFAULT, true)
                    : baseModel;

            boolean isMaterial = data.material != SignMaterial.DEFAULT;
            int rawColor = data.color == 0 ? 0xFFFFFF : data.color;

            cellList.add(new CachedCell(
                    letterState, finalLetterModel, renderType,
                    totalTransX, totalTransY, totalTransZ,
                    c.x, c.y, c.z,
                    cellScale, rawColor, data.rainbow, isMaterial
            ));
        }

        ItemStack backGlyph = entity.getBackGlyph();
        boolean hasBackGlyph = false;
        BlockState backState = null;
        BakedModel finalBackModel = null;
        RenderType backRenderType = null;
        float backTransX = 0f, backTransY = 0f, backTransZ = 0f;
        int backRawColor = 0xFFFFFF;
        boolean backRainbow = false;
        boolean backIsMaterial = false;

        if (!backGlyph.isEmpty() && backGlyph.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
            Direction effectiveFacing = facing.getOpposite();
            Direction cellFacing = (face != AttachFace.WALL) ? effectiveFacing.getClockWise() : effectiveFacing;
            AttachFace cellFace = face;

            backState = letterBlock.defaultBlockState();
            if (backState.hasProperty(LetterBlock.FACING)) {
                backState = backState.setValue(LetterBlock.FACING, cellFacing);
            }
            if (backState.hasProperty(LetterBlock.FACE)) {
                backState = backState.setValue(LetterBlock.FACE, cellFace);
            }

            CompoundTag backTag = backGlyph.getTagElement("BlockEntityTag");
            SignMaterial backMaterial = SignMaterial.DEFAULT;
            if (backTag != null && backTag.contains("SavedMaterial")) {
                try {
                    backMaterial = SignMaterial.valueOf(backTag.getString("SavedMaterial"));
                } catch (IllegalArgumentException ignored) {}
            }
            if (backState.hasProperty(LetterBlock.MATERIAL)) {
                CompoundTag stateTag = backGlyph.getTagElement("BlockStateTag");
                if (stateTag != null && stateTag.contains("material")) {
                    try {
                        backState = backState.setValue(LetterBlock.MATERIAL, SignMaterial.valueOf(stateTag.getString("material").toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                } else if (backMaterial != SignMaterial.DEFAULT) {
                    backState = backState.setValue(LetterBlock.MATERIAL, backMaterial);
                }
            }

            BakedModel baseBackModel = dispatcher.getBlockModel(backState);
            finalBackModel = (backMaterial != SignMaterial.DEFAULT)
                    ? LetterBlockEntityRenderer.getMaterialModel(baseBackModel, backMaterial, SignMaterial.DEFAULT, true)
                    : baseBackModel;

            int plateRotation = entity.getFacingRotation();
            double normalLength = Math.sqrt(
                    SignRotation.facingX(plateRotation) * SignRotation.facingX(plateRotation)
                            + SignRotation.facingZ(plateRotation) * SignRotation.facingZ(plateRotation));
            if (normalLength == 0.0) normalLength = 1.0;
            double normalX = SignRotation.facingX(plateRotation) / normalLength;
            double normalZ = SignRotation.facingZ(plateRotation) / normalLength;
            double plateClearance = (face == AttachFace.WALL ? 1.0 : 2.0) / 16.0;

            if (face != AttachFace.WALL) {
                ModelCentering.Center backTarget = hasBackplate
                        ? new ModelCentering.Center(0.5 - normalX * plateClearance, 0.5 - normalZ * plateClearance)
                        : new ModelCentering.Center(0.5 - normalX * 2.0 / 16.0, 0.5 - normalZ * 2.0 / 16.0);
                if (LetterBlock.isLateralDot(backState)) {
                    ModelCentering.Center currentCenter = ModelCentering.transformedCenter(finalBackModel, backState, rotationDegrees,
                            true, 1, 0.0, 0.0);
                    backTarget = ModelCentering.centerAlongNormal(currentCenter, backTarget.x(), backTarget.z(), normalX, normalZ);
                }

                double[] bOffsets = ModelCentering.getCenterOffset(finalBackModel, backState, rotationDegrees, true, 1, 0.0, 0.0, backTarget.x(), backTarget.z());
                backTransX = (float) bOffsets[0];
                backTransY = 0f;
                backTransZ = (float) bOffsets[1];
            } else {
                float shift = 1.0f;
                float clearance = hasBackplate ? (1.05f / 16.0f) : 0f;
                switch (effectiveFacing) {
                    case NORTH -> backTransZ = -shift - clearance;
                    case SOUTH -> backTransZ = shift + clearance;
                    case WEST  -> backTransX = -shift - clearance;
                    case EAST  -> backTransX = shift + clearance;
                }
            }

            backRenderType = ItemBlockRenderTypes.getRenderType(backState, false);
            backIsMaterial = backMaterial != SignMaterial.DEFAULT;
            backRainbow = backTag != null && backTag.getBoolean("IsRainbow");
            backRawColor = backMaterial != SignMaterial.DEFAULT ? 0xFFFFFF
                    : (backTag != null && backTag.contains("RGBColor") ? backTag.getInt("RGBColor") : 0xFFFFFF);
            hasBackGlyph = true;
        }

        ItemStack frontGlyph = entity.getFrontGlyph();
        boolean hasFrontGlyph = false;
        BlockState frontState = null;
        BakedModel finalFrontModel = null;
        RenderType frontRenderType = null;
        float frontTransX = 0f, frontTransY = 0f, frontTransZ = 0f;
        int frontRawColor = 0xFFFFFF;
        boolean frontRainbow = false;
        boolean frontIsMaterial = false;

        if (!frontGlyph.isEmpty() && frontGlyph.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock letterBlock) {
            Direction cellFacing = (face != AttachFace.WALL) ? facing.getClockWise() : facing;
            AttachFace cellFace = face;

            frontState = letterBlock.defaultBlockState();
            if (frontState.hasProperty(LetterBlock.FACING)) {
                frontState = frontState.setValue(LetterBlock.FACING, cellFacing);
            }
            if (frontState.hasProperty(LetterBlock.FACE)) {
                frontState = frontState.setValue(LetterBlock.FACE, cellFace);
            }

            CompoundTag frontTag = frontGlyph.getTagElement("BlockEntityTag");
            SignMaterial frontMaterial = SignMaterial.DEFAULT;
            if (frontTag != null && frontTag.contains("SavedMaterial")) {
                try {
                    frontMaterial = SignMaterial.valueOf(frontTag.getString("SavedMaterial"));
                } catch (IllegalArgumentException ignored) {}
            }
            if (frontState.hasProperty(LetterBlock.MATERIAL)) {
                CompoundTag stateTag = frontGlyph.getTagElement("BlockStateTag");
                if (stateTag != null && stateTag.contains("material")) {
                    try {
                        frontState = frontState.setValue(LetterBlock.MATERIAL, SignMaterial.valueOf(stateTag.getString("material").toUpperCase()));
                    } catch (IllegalArgumentException ignored) {}
                } else if (frontMaterial != SignMaterial.DEFAULT) {
                    frontState = frontState.setValue(LetterBlock.MATERIAL, frontMaterial);
                }
            }

            BakedModel baseFrontModel = dispatcher.getBlockModel(frontState);
            finalFrontModel = (frontMaterial != SignMaterial.DEFAULT)
                    ? LetterBlockEntityRenderer.getMaterialModel(baseFrontModel, frontMaterial, SignMaterial.DEFAULT, true)
                    : baseFrontModel;

            int plateRotation = entity.getFacingRotation();
            double normalLength = Math.sqrt(
                    SignRotation.facingX(plateRotation) * SignRotation.facingX(plateRotation)
                            + SignRotation.facingZ(plateRotation) * SignRotation.facingZ(plateRotation));
            if (normalLength == 0.0) normalLength = 1.0;
            double normalX = SignRotation.facingX(plateRotation) / normalLength;
            double normalZ = SignRotation.facingZ(plateRotation) / normalLength;
            double plateClearance = (face == AttachFace.WALL ? 1.0 : 2.0) / 16.0;

            if (face != AttachFace.WALL) {
                ModelCentering.Center letterTarget = hasBackplate
                        ? new ModelCentering.Center(0.5 + normalX * plateClearance, 0.5 + normalZ * plateClearance)
                        : new ModelCentering.Center(0.5, 0.5);
                if (LetterBlock.isLateralDot(frontState)) {
                    ModelCentering.Center currentCenter = ModelCentering.transformedCenter(finalFrontModel, frontState, rotationDegrees,
                            true, 1, 0.0, 0.0);
                    letterTarget = ModelCentering.centerAlongNormal(currentCenter, letterTarget.x(), letterTarget.z(), normalX, normalZ);
                }
                double[] lOffsets = ModelCentering.getCenterOffset(finalFrontModel, frontState, rotationDegrees, true, 1, 0.0, 0.0, letterTarget.x(), letterTarget.z());
                frontTransX = (float) lOffsets[0];
                frontTransY = 0f;
                frontTransZ = (float) lOffsets[1];
            } else if (hasBackplate) {
                frontTransX = (float) (normalX * plateClearance);
                frontTransY = 0f;
                frontTransZ = (float) (normalZ * plateClearance);
            }

            frontRenderType = ItemBlockRenderTypes.getRenderType(frontState, false);
            frontIsMaterial = frontMaterial != SignMaterial.DEFAULT;
            frontRainbow = frontTag != null && frontTag.getBoolean("IsRainbow");
            frontRawColor = frontMaterial != SignMaterial.DEFAULT ? 0xFFFFFF
                    : (frontTag != null && frontTag.contains("RGBColor") ? frontTag.getInt("RGBColor") : 0xFFFFFF);
            hasFrontGlyph = true;
        }

        return new GridSignRenderCache(
                entity.getRenderVersion(), modelManagerRef, face, facing, rotationDegrees,
                hasBackplate, plateState, finalPlateModel, plateQuads,
                plateCenterLocalX, plateCenterLocalZ, widenDiagonal, plateFacing, fMat, bMat,
                cellList,
                hasBackGlyph, backState, finalBackModel,
                backRenderType, backTransX, backTransY, backTransZ,
                backRawColor, backRainbow, backIsMaterial,
                hasFrontGlyph, frontState, finalFrontModel,
                frontRenderType, frontTransX, frontTransY, frontTransZ,
                frontRawColor, frontRainbow, frontIsMaterial
        );
    }
}
