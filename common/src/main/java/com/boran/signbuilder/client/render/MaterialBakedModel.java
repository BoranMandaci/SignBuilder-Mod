package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.SignMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MaterialBakedModel implements BakedModel {
    private final BakedModel originalModel;
    private final SignMaterial frontMat;
    private final SignMaterial backMat;
    private final boolean isLetter;

    private static final Map<String, List<BakedQuad>> GLOBAL_QUAD_CACHE = new ConcurrentHashMap<>();

    public MaterialBakedModel(BakedModel originalModel) {
        this(originalModel, SignMaterial.DEFAULT, SignMaterial.DEFAULT, false);
    }

    public MaterialBakedModel(BakedModel originalModel, SignMaterial frontMat, SignMaterial backMat) {
        this(originalModel, frontMat, backMat, false);
    }

    public MaterialBakedModel(BakedModel originalModel, SignMaterial frontMat, SignMaterial backMat, boolean isLetter) {
        BakedModel unwrapped = originalModel;
        while (unwrapped instanceof MaterialBakedModel mbm) {
            unwrapped = mbm.originalModel;
        }
        this.originalModel = unwrapped;
        this.frontMat = frontMat;
        this.backMat = backMat;
        this.isLetter = isLetter;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random) {
        List<BakedQuad> originalQuads = originalModel.getQuads(state, direction, random);

        if (state == null) {
            return originalQuads;
        }

        SignMaterial currentFMat = this.frontMat;
        SignMaterial currentBMat = this.backMat;

        if (currentFMat == SignMaterial.DEFAULT && currentBMat == SignMaterial.DEFAULT) {
            return originalQuads;
        }

        String cacheKey = System.identityHashCode(originalModel) + "_"
                + currentFMat.ordinal() + "_" + currentBMat.ordinal() + "_"
                + (direction != null ? direction.get3DDataValue() : -1);

        SignMaterial activeFront = currentFMat;
        return GLOBAL_QUAD_CACHE.computeIfAbsent(cacheKey, k -> remapQuads(originalQuads, activeFront, currentBMat));
    }

    private List<BakedQuad> remapQuads(List<BakedQuad> originalQuads, SignMaterial fMat, SignMaterial bMat) {
        List<BakedQuad> newQuads = new ArrayList<>(originalQuads.size());

        for (BakedQuad quad : originalQuads) {
            int tint = quad.getTintIndex();
            SignMaterial targetMat = SignMaterial.DEFAULT;

            if (isLetter) {
                targetMat = fMat;
                tint = (fMat == SignMaterial.DEFAULT) ? 0 : -1;
            } else {
                if (tint == 0) {
                    targetMat = fMat;
                    if (fMat != SignMaterial.DEFAULT) tint = -1;
                } else if (tint == 1) {
                    targetMat = bMat;
                    if (bMat != SignMaterial.DEFAULT) tint = -1;
                }
            }

            if (targetMat == SignMaterial.DEFAULT) {
                newQuads.add(quad);
                continue;
            }

            TextureAtlasSprite newSprite = getSpriteForMaterial(targetMat);
            TextureAtlasSprite oldSprite = quad.getSprite();

            int[] oldData = quad.getVertices();
            int[] newData = new int[32];
            System.arraycopy(oldData, 0, newData, 0, 32);

            float oldU0 = oldSprite.getU0(); float oldU1 = oldSprite.getU1();
            float oldV0 = oldSprite.getV0(); float oldV1 = oldSprite.getV1();
            float newU0 = newSprite.getU0(); float newU1 = newSprite.getU1();
            float newV0 = newSprite.getV0(); float newV1 = newSprite.getV1();

            for (int i = 0; i < 4; i++) {
                int vertexOffset = i * 8;
                int uIndex = vertexOffset + 4;
                int vIndex = vertexOffset + 5;
                float oldU = Float.intBitsToFloat(oldData[uIndex]);
                float oldV = Float.intBitsToFloat(oldData[vIndex]);
                float relU = (oldU1 - oldU0) == 0 ? 0 : (oldU - oldU0) / (oldU1 - oldU0);
                float relV = (oldV1 - oldV0) == 0 ? 0 : (oldV - oldV0) / (oldV1 - oldV0);
                newData[uIndex] = Float.floatToIntBits(newU0 + relU * (newU1 - newU0));
                newData[vIndex] = Float.floatToIntBits(newV0 + relV * (newV1 - newV0));
            }

            newQuads.add(new BakedQuad(newData, tint, quad.getDirection(), newSprite, quad.isShade()));
        }
        return newQuads;
    }

    public static TextureAtlasSprite getSpriteForMaterial(SignMaterial material) {
        String path = switch (material) {
            case OAK -> "block/oak_planks";
            case SPRUCE -> "block/spruce_planks";
            case BIRCH -> "block/birch_planks";
            case JUNGLE -> "block/jungle_planks";
            case ACACIA -> "block/acacia_planks";
            case DARK_OAK -> "block/dark_oak_planks";
            case MANGROVE -> "block/mangrove_planks";
            case CHERRY -> "block/cherry_planks";
            case BAMBOO -> "block/bamboo_planks";
            case IRON -> "block/iron_block";
            case ANDESITE -> "block/polished_andesite";
            case GOLD -> "block/gold_block";
            case DIAMOND -> "block/diamond_block";
            case LAPIS -> "block/lapis_block";
            case SMOOTH_STONE -> "block/smooth_stone";
            case POLISHED_DIORITE -> "block/polished_diorite";
            case BRICKS -> "block/bricks";
            case STONE_BRICKS -> "block/stone_bricks";
            default -> "block/white_concrete";
        };
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("minecraft", path));
    }

    @Override public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return originalModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }
    @Override public @NotNull TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
    @Override public @NotNull ItemTransforms getTransforms() { return originalModel.getTransforms(); }
    @Override public @NotNull ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}