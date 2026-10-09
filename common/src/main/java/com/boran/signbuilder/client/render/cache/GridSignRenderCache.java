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

    public GridSignRenderCache(long version, Object modelManagerRef, AttachFace face, Direction facing, int rotationDegrees,
                               boolean hasBackplate, BlockState plateState, BakedModel finalPlateModel,
                               List<BakedQuad> plateQuads, float plateCenterLocalX, float plateCenterLocalZ,
                               boolean widenDiagonal, Direction plateFacing, SignMaterial fMat, SignMaterial bMat,
                               List<CachedCell> cells) {
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
            int rawIndex = entry.getKey();
            boolean isBack = rawIndex >= 16;
            int index = isBack ? rawIndex - 16 : rawIndex;
            GridSignBlockEntity.CellData data = entry.getValue();

            Block letterBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("signbuilder", data.character));
            if (letterBlock == null || letterBlock == Blocks.AIR) continue;

            Direction effectiveFacing = isBack ? facing.getOpposite() : facing;
            BlockState letterState = letterBlock.defaultBlockState();
            if (letterState.hasProperty(LetterBlock.FACING)) {
                letterState = letterState.setValue(LetterBlock.FACING, effectiveFacing);
            }
            if (letterState.hasProperty(LetterBlock.FACE)) {
                letterState = letterState.setValue(LetterBlock.FACE, AttachFace.WALL);
            }
            if (letterState.hasProperty(LetterBlock.MATERIAL)) {
                letterState = letterState.setValue(LetterBlock.MATERIAL, data.material);
            }
            if (letterState.hasProperty(LetterBlock.LIGHT_MODE)) {
                letterState = letterState.setValue(LetterBlock.LIGHT_MODE, data.lightMode);
            }

            int col = index % gridSize;
            int row = index / gridSize;
            float colShift = (gridSize - 1) / 2.0f - col;
            float rowShift = (gridSize - 1) / 2.0f - row;
            if (rotationDegrees != 0 && face != AttachFace.WALL) {
                colShift *= 1.4142135f;
            }

            Vector3f offsets = GridOffsetHelper.getOffset(face, effectiveFacing, colShift, rowShift, scale);
            Vector3f baseTrans = GridOffsetHelper.getBaseTranslation(face, effectiveFacing);
            Vector3f cl = hasBackplate ? GridOffsetHelper.getClearance(face, effectiveFacing, 1.05f / 16.0f) : new Vector3f(0, 0, 0);
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
                    scale, rawColor, data.rainbow, isMaterial
            ));
        }

        return new GridSignRenderCache(
                entity.getRenderVersion(), modelManagerRef, face, facing, rotationDegrees,
                hasBackplate, plateState, finalPlateModel, plateQuads,
                plateCenterLocalX, plateCenterLocalZ, widenDiagonal, plateFacing, fMat, bMat,
                cellList
        );
    }
}
