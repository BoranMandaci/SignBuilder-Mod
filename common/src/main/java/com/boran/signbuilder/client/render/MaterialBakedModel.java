package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.BackplateBlock;
import com.boran.signbuilder.block.LetterBlock;
import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.item.PaintBrushItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MaterialBakedModel implements BakedModel {
    private final BakedModel originalModel;
    private final SignMaterial frontMat;
    private final SignMaterial backMat;
    private final int frontColor;
    private final int backColor;
    private final boolean isLetter;
    private final Map<String, List<BakedQuad>> quadCache = new ConcurrentHashMap<>();

    private static final ModelBaker DUMMY_BAKER = (ModelBaker) Proxy.newProxyInstance(
            MaterialBakedModel.class.getClassLoader(),
            new Class<?>[]{ModelBaker.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getModelTextureGetter") || method.getReturnType().equals(Function.class)) {
                    return (Function<Material, TextureAtlasSprite>) mat ->
                            mat != null ? Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(mat.texture()) : null;
                }
                return null;
            }
    );

    private final ItemOverrides itemOverrides = new ItemOverrides(
            DUMMY_BAKER,
            (BlockModel) null,
            Collections.emptyList()
    ) {
        @Override
        public BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            CompoundTag beTag = stack.getTagElement("BlockEntityTag");
            if (beTag != null) {
                boolean isLetterItem = stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof LetterBlock;
                boolean isBackplateItem = stack.is(ModBlocks.BACKPLATE_ITEM.get());

                SignMaterial fMat = SignMaterial.DEFAULT;
                SignMaterial bMat = SignMaterial.DEFAULT;
                int fColor = 0xFFFFFF;
                int bColor = 0xFFFFFF;

                if (isLetterItem) {
                    if (beTag.contains("SavedMaterial")) {
                        try { fMat = SignMaterial.valueOf(beTag.getString("SavedMaterial")); } catch (Exception ignored) {}
                    } else if (beTag.contains("savedMaterial")) {
                        try { fMat = SignMaterial.valueOf(beTag.getString("savedMaterial")); } catch (Exception ignored) {}
                    } else if (beTag.contains("Material")) {
                        try { fMat = SignMaterial.valueOf(beTag.getString("Material")); } catch (Exception ignored) {}
                    }

                    if (beTag.contains("RgbColor")) {
                        fColor = beTag.getInt("RgbColor");
                    } else if (beTag.contains("rgbColor")) {
                        fColor = beTag.getInt("rgbColor");
                    } else if (beTag.contains("Color")) {
                        int c = beTag.getInt("Color");
                        fColor = (c >= 0 && c <= 15) ? PaintBrushItem.getActualHexColor(c) : c;
                    } else if (beTag.contains("color")) {
                        int c = beTag.getInt("color");
                        fColor = (c >= 0 && c <= 15) ? PaintBrushItem.getActualHexColor(c) : c;
                    } else if (beTag.contains("ColorIndex")) {
                        fColor = PaintBrushItem.getActualHexColor(beTag.getInt("ColorIndex"));
                    }

                    boolean fRainbow = beTag.getBoolean("IsRainbow") || beTag.getBoolean("isRainbow") || beTag.getBoolean("Rainbow");
                    if (fRainbow) {
                        float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                        fColor = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
                    }

                    if (fColor == 0) fColor = 0xFFFFFF;
                    bMat = fMat;
                    bColor = fColor;

                    if (fMat != SignMaterial.DEFAULT || fColor != 0xFFFFFF) {
                        return new MaterialBakedModel(originalModel, fMat, bMat, fColor, bColor, true);
                    }
                } else if (isBackplateItem) {
                    if (beTag.contains("BackplateFrontMaterial")) {
                        try { fMat = SignMaterial.valueOf(beTag.getString("BackplateFrontMaterial")); } catch (Exception ignored) {}
                    } else if (beTag.contains("backplateFrontMaterial")) {
                        try { fMat = SignMaterial.valueOf(beTag.getString("backplateFrontMaterial")); } catch (Exception ignored) {}
                    }

                    if (beTag.contains("BackplateBackMaterial")) {
                        try { bMat = SignMaterial.valueOf(beTag.getString("BackplateBackMaterial")); } catch (Exception ignored) {}
                    } else if (beTag.contains("backplateBackMaterial")) {
                        try { bMat = SignMaterial.valueOf(beTag.getString("backplateBackMaterial")); } catch (Exception ignored) {}
                    }

                    if (beTag.contains("BackplateFrontColor")) {
                        fColor = beTag.getInt("BackplateFrontColor");
                    } else if (beTag.contains("backplateFrontColor")) {
                        fColor = beTag.getInt("backplateFrontColor");
                    }

                    if (beTag.contains("BackplateBackColor")) {
                        bColor = beTag.getInt("BackplateBackColor");
                    } else if (beTag.contains("backplateBackColor")) {
                        bColor = beTag.getInt("backplateBackColor");
                    }

                    boolean fRainbow = beTag.getBoolean("BackplateFrontRainbow") || beTag.getBoolean("backplateFrontRainbow");
                    boolean bRainbow = beTag.getBoolean("BackplateBackRainbow") || beTag.getBoolean("backplateBackRainbow");

                    if (fRainbow || bRainbow) {
                        float hue = (System.currentTimeMillis() % 3000L) / 3000.0f;
                        int rainbowCol = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
                        if (fRainbow) fColor = rainbowCol;
                        if (bRainbow) bColor = rainbowCol;
                    }

                    if (fColor == 0) fColor = 0xFFFFFF;
                    if (bColor == 0) bColor = 0xFFFFFF;

                    if (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT || fColor != 0xFFFFFF || bColor != 0xFFFFFF) {
                        return new MaterialBakedModel(originalModel, fMat, bMat, fColor, bColor, false);
                    }
                }
            }
            return model;
        }
    };

    public MaterialBakedModel(BakedModel originalModel) {
        this(originalModel, SignMaterial.DEFAULT, SignMaterial.DEFAULT, 0xFFFFFF, 0xFFFFFF, false);
    }

    public MaterialBakedModel(BakedModel originalModel, SignMaterial frontMat, SignMaterial backMat) {
        this(originalModel, frontMat, backMat, 0xFFFFFF, 0xFFFFFF, false);
    }

    public MaterialBakedModel(BakedModel originalModel, SignMaterial frontMat, SignMaterial backMat, int frontColor, int backColor) {
        this(originalModel, frontMat, backMat, frontColor, backColor, false);
    }

    public MaterialBakedModel(BakedModel originalModel, SignMaterial frontMat, SignMaterial backMat, int frontColor, int backColor, boolean isLetter) {
        BakedModel unwrapped = originalModel;
        while (unwrapped instanceof MaterialBakedModel mbm) {
            unwrapped = mbm.originalModel;
        }
        this.originalModel = unwrapped;
        this.frontMat = frontMat;
        this.backMat = backMat;
        this.frontColor = frontColor;
        this.backColor = backColor;
        this.isLetter = isLetter;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random) {
        List<BakedQuad> originalQuads = originalModel.getQuads(state, direction, random);

        SignMaterial currentFMat = this.frontMat;
        if (state != null) {
            if (currentFMat == SignMaterial.DEFAULT && state.hasProperty(LetterBlock.MATERIAL)) {
                currentFMat = state.getValue(LetterBlock.MATERIAL);
            }
            if (currentFMat == SignMaterial.DEFAULT && state.hasProperty(BackplateBlock.MATERIAL)) {
                currentFMat = state.getValue(BackplateBlock.MATERIAL);
            }
        }
        SignMaterial currentBMat = this.backMat;

        if (currentFMat == SignMaterial.DEFAULT && currentBMat == SignMaterial.DEFAULT && frontColor == 0xFFFFFF && backColor == 0xFFFFFF) {
            return originalQuads;
        }

        String cacheKey = (isLetter ? "letter_" : "plate_")
                + currentFMat.getSerializedName() + "_" + currentBMat.getSerializedName() + "_"
                + Integer.toHexString(frontColor) + "_" + Integer.toHexString(backColor) + "_"
                + (direction != null ? direction.getName() : "none");

        SignMaterial activeFront = currentFMat;
        return quadCache.computeIfAbsent(cacheKey, k -> remapQuads(originalQuads, activeFront, currentBMat));
    }

    private List<BakedQuad> remapQuads(List<BakedQuad> originalQuads, SignMaterial fMat, SignMaterial bMat) {
        List<BakedQuad> newQuads = new ArrayList<>();

        for (BakedQuad quad : originalQuads) {
            int tint = quad.getTintIndex();
            SignMaterial targetMat = SignMaterial.DEFAULT;
            int applyColor = 0xFFFFFF;

            if (isLetter) {
                targetMat = fMat;
                applyColor = (fMat == SignMaterial.DEFAULT) ? frontColor : 0xFFFFFF;
                tint = (fMat == SignMaterial.DEFAULT && frontColor != 0xFFFFFF) ? 0 : -1;
            } else {
                if (tint == 0) {
                    targetMat = fMat;
                    applyColor = (fMat == SignMaterial.DEFAULT) ? frontColor : 0xFFFFFF;
                    if (fMat != SignMaterial.DEFAULT) tint = -1;
                } else if (tint == 1) {
                    targetMat = bMat;
                    applyColor = (bMat == SignMaterial.DEFAULT) ? backColor : 0xFFFFFF;
                    if (bMat != SignMaterial.DEFAULT) tint = -1;
                }
            }

            if (targetMat == SignMaterial.DEFAULT && applyColor == 0xFFFFFF) {
                newQuads.add(quad);
                continue;
            }

            TextureAtlasSprite newSprite = (targetMat != SignMaterial.DEFAULT) ? getSpriteForMaterial(targetMat) : quad.getSprite();
            TextureAtlasSprite oldSprite = quad.getSprite();

            int[] oldData = quad.getVertices();
            int[] newData = new int[32];
            System.arraycopy(oldData, 0, newData, 0, 32);

            float oldU0 = oldSprite.getU0(); float oldU1 = oldSprite.getU1();
            float oldV0 = oldSprite.getV0(); float oldV1 = oldSprite.getV1();
            float newU0 = newSprite.getU0(); float newU1 = newSprite.getU1();
            float newV0 = newSprite.getV0(); float newV1 = newSprite.getV1();

            int r = (applyColor >> 16) & 0xFF;
            int g = (applyColor >> 8) & 0xFF;
            int b = applyColor & 0xFF;
            int packedColor = (0xFF << 24) | (b << 16) | (g << 8) | r;

            for (int i = 0; i < 4; i++) {
                int vertexOffset = i * 8;
                newData[vertexOffset + 3] = packedColor;

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
    @Override public @NotNull ItemOverrides getOverrides() { return itemOverrides; }
}