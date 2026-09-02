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
    private final Map<String, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    public MaterialBakedModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random) {
        List<BakedQuad> originalQuads = originalModel.getQuads(state, direction, random);
        if (state != null && state.hasProperty(LetterBlock.MATERIAL)) {
            SignMaterial material = state.getValue(LetterBlock.MATERIAL);
            if (material != SignMaterial.DEFAULT) {
                String cacheKey = material.name() + "_" + (direction != null ? direction.getName() : "none");
                return quadCache.computeIfAbsent(cacheKey, k -> remapQuads(originalQuads, material));
            }
        }
        return originalQuads;
    }

    private List<BakedQuad> remapQuads(List<BakedQuad> originalQuads, SignMaterial material) {
        List<BakedQuad> newQuads = new ArrayList<>();
        TextureAtlasSprite newSprite = getSpriteForMaterial(material);

        for (BakedQuad quad : originalQuads) {
            if (quad.getTintIndex() == 0) {
                TextureAtlasSprite oldSprite = quad.getSprite();
                int[] oldData = quad.getVertices();
                int[] newData = new int[32];
                System.arraycopy(oldData, 0, newData, 0, 32);

                float oldU0 = oldSprite.getU0(); float oldU1 = oldSprite.getU1();
                float oldV0 = oldSprite.getV0(); float oldV1 = oldSprite.getV1();
                float newU0 = newSprite.getU0(); float newU1 = newSprite.getU1();
                float newV0 = newSprite.getV0(); float newV1 = newSprite.getV1();

                for (int i = 0; i < 4; i++) {
                    int uIndex = i * 8 + 4;
                    int vIndex = i * 8 + 5;
                    float oldU = Float.intBitsToFloat(oldData[uIndex]);
                    float oldV = Float.intBitsToFloat(oldData[vIndex]);
                    float relU = (oldU1 - oldU0) == 0 ? 0 : (oldU - oldU0) / (oldU1 - oldU0);
                    float relV = (oldV1 - oldV0) == 0 ? 0 : (oldV - oldV0) / (oldV1 - oldV0);
                    float newU = newU0 + relU * (newU1 - newU0);
                    float newV = newV0 + relV * (newV1 - newV0);
                    newData[uIndex] = Float.floatToIntBits(newU);
                    newData[vIndex] = Float.floatToIntBits(newV);
                }
                newQuads.add(new BakedQuad(newData, -1, quad.getDirection(), newSprite, quad.isShade()));
            } else {
                newQuads.add(quad);
            }
        }
        return newQuads;
    }

    private TextureAtlasSprite getSpriteForMaterial(SignMaterial material) {
        String textureLoc = switch (material) {
            case OAK -> "minecraft:block/oak_planks"; case SPRUCE -> "minecraft:block/spruce_planks"; case BIRCH -> "minecraft:block/birch_planks";
            case JUNGLE -> "minecraft:block/jungle_planks"; case ACACIA -> "minecraft:block/acacia_planks"; case DARK_OAK -> "minecraft:block/dark_oak_planks";
            case MANGROVE -> "minecraft:block/mangrove_planks"; case CHERRY -> "minecraft:block/cherry_planks"; case BAMBOO -> "minecraft:block/bamboo_planks";
            case IRON -> "minecraft:block/iron_block"; case ANDESITE -> "minecraft:block/polished_andesite"; default -> "minecraft:block/white_concrete";
        };
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(textureLoc));
    }

    @Override public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return originalModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }
    @Override public @NotNull TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
    @Override public @NotNull ItemTransforms getTransforms() { return originalModel.getTransforms(); }
    @Override public @NotNull ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}