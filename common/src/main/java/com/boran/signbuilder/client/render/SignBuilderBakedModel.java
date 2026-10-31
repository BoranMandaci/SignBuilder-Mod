package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;

public class SignBuilderBakedModel implements BakedModel {
    private final BakedModel originalModel;
    private final boolean isBackplate;
    private final ItemOverrides overrides;

    private static volatile Field bakedRegistryField = null;
    private static volatile Field shapesCacheField = null;
    private static volatile Field shaperModelsField = null;

    private static final ModelBaker DUMMY_BAKER = createDummyBaker();

    @SuppressWarnings("unchecked")
    private static ModelBaker createDummyBaker() {
        try {
            return (ModelBaker) Proxy.newProxyInstance(
                    ModelBaker.class.getClassLoader(),
                    new Class<?>[]{ModelBaker.class},
                    (proxy, method, args) -> {
                        if ("getModelTextureGetter".equals(method.getName())) {
                            return (Function<Object, Object>) mat -> null;
                        }
                        Class<?> returnType = method.getReturnType();
                        if (returnType == boolean.class) return false;
                        if (returnType == int.class) return 0;
                        if (returnType == long.class) return 0L;
                        if (returnType == float.class) return 0f;
                        if (returnType == double.class) return 0.0;
                        return null;
                    }
            );
        } catch (Exception e) {
            return null;
        }
    }

    public SignBuilderBakedModel(BakedModel originalModel, boolean isBackplate) {
        BakedModel unwrapped = originalModel;
        while (unwrapped instanceof SignBuilderBakedModel sbbm) {
            unwrapped = sbbm.originalModel;
        }
        this.originalModel = unwrapped;
        this.isBackplate = isBackplate;
        this.overrides = new ItemOverrides(DUMMY_BAKER, null, Collections.emptyList()) {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                CompoundTag beTag = (blockEntityData != null && !blockEntityData.isEmpty()) ? blockEntityData.copyTag()
                        : ((customData != null && !customData.isEmpty()) ? customData.copyTag() : null);

                if (beTag == null || beTag.isEmpty()) {
                    return originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
                }

                SignMaterial fMat = SignMaterial.DEFAULT;
                SignMaterial bMat = SignMaterial.DEFAULT;
                boolean fGlow = false;
                boolean bGlow = false;

                if (isBackplate) {
                    String fMatStr = beTag.contains("BackplateFrontMaterial") ? beTag.getString("BackplateFrontMaterial")
                            : (beTag.contains("BPFrontMat") ? beTag.getString("BPFrontMat")
                            : (beTag.contains("BPFMat") ? beTag.getString("BPFMat") : ""));
                    if (!fMatStr.isEmpty()) {
                        try { fMat = SignMaterial.valueOf(fMatStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                    }

                    String bMatStr = beTag.contains("BackplateBackMaterial") ? beTag.getString("BackplateBackMaterial")
                            : (beTag.contains("BPBackMat") ? beTag.getString("BPBackMat")
                            : (beTag.contains("BPBMat") ? beTag.getString("BPBMat") : ""));
                    if (!bMatStr.isEmpty()) {
                        try { bMat = SignMaterial.valueOf(bMatStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                    }

                    fGlow = beTag.getBoolean("BackplateFrontGlowInked") || beTag.getBoolean("BPFrontGlowInked");
                    bGlow = beTag.getBoolean("BackplateBackGlowInked") || beTag.getBoolean("BPBackGlowInked");
                } else {
                    String matStr = "";
                    if (beTag.contains("SavedMaterial")) matStr = beTag.getString("SavedMaterial");
                    else if (beTag.contains("savedMaterial")) matStr = beTag.getString("savedMaterial");
                    else if (beTag.contains("material")) matStr = beTag.getString("material");
                    else if (beTag.contains("Material")) matStr = beTag.getString("Material");

                    if (!matStr.isEmpty()) {
                        try { fMat = SignMaterial.valueOf(matStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                    }
                    fGlow = beTag.getBoolean("IsGlowInked") || beTag.getBoolean("isGlowInked");
                }

                if (fMat == SignMaterial.DEFAULT) {
                    net.minecraft.world.item.component.BlockItemStateProperties stateProps = stack.get(DataComponents.BLOCK_STATE);
                    if (stateProps != null && !stateProps.isEmpty()) {
                        String stMat = stateProps.properties().get("material");
                        if (stMat != null && !stMat.isEmpty()) {
                            try { fMat = SignMaterial.valueOf(stMat.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                        }
                    }
                }

                if (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT || fGlow || bGlow) {
                    BakedModel base = originalModel;
                    while (base instanceof SignBuilderBakedModel sbbm) {
                        base = sbbm.originalModel;
                    }
                    return LetterBlockEntityRenderer.getMaterialModel(base, fMat, bMat, !isBackplate, fGlow || bGlow);
                }

                return originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
            }
        };
    }

    public BakedModel getOriginalModel() {
        return originalModel;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return originalModel.getQuads(state, direction, random);
    }

    @Override public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return originalModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return originalModel.getTransforms(); }

    public static void checkAndWrap(Minecraft client) {
        if (client == null) return;
        ModelManager modelManager = client.getModelManager();
        if (modelManager == null) return;
        if (client.getItemRenderer() == null) return;
        ItemModelShaper shaper = client.getItemRenderer().getItemModelShaper();
        if (shaper == null) return;

        Item backplateItem = ModBlocks.BACKPLATE_ITEM.get();
        if (backplateItem == null) return;

        BakedModel currentBpModel = shaper.getItemModel(backplateItem);
        boolean needWrap = !(currentBpModel instanceof SignBuilderBakedModel);

        if (!needWrap && !ModBlocks.ALL_SIGN_BLOCKS.isEmpty()) {
            Block firstBlock = ModBlocks.ALL_SIGN_BLOCKS.get(0).get();
            if (firstBlock != null) {
                BakedModel firstModel = shaper.getItemModel(firstBlock.asItem());
                if (!(firstModel instanceof SignBuilderBakedModel)) {
                    needWrap = true;
                }
            }
        }

        if (!needWrap) {
            return;
        }

        wrapAll(modelManager, shaper);
    }

    public static void onModifyBakingResult(Map<ModelResourceLocation, BakedModel> models) {
        if (models == null || models.isEmpty()) return;

        Item backplateItem = ModBlocks.BACKPLATE_ITEM.get();
        if (backplateItem != null) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("signbuilder", "backplate");
            ModelResourceLocation mrl = ModelResourceLocation.inventory(id);
            BakedModel original = models.get(mrl);
            if (original != null && !(original instanceof SignBuilderBakedModel)) {
                models.put(mrl, new SignBuilderBakedModel(original, true));
            }
        }

        for (RegistrySupplier<Block> blockReg : ModBlocks.ALL_SIGN_BLOCKS) {
            Block block = blockReg.get();
            if (block != null) {
                ResourceLocation id = blockReg.getId();
                ModelResourceLocation mrl = ModelResourceLocation.inventory(id);
                BakedModel original = models.get(mrl);
                if (original != null && !(original instanceof SignBuilderBakedModel)) {
                    models.put(mrl, new SignBuilderBakedModel(original, false));
                }
            }
        }
    }

    private static synchronized void wrapAll(ModelManager modelManager, ItemModelShaper shaper) {
        LetterBlockEntityRenderer.clearModelCache();
        MaterialBakedModel.clearQuadCache();

        Map<ModelResourceLocation, BakedModel> registry = getBakedRegistry(modelManager);
        Int2ObjectMap<BakedModel> shapesCache = getShapesCache(shaper);

        wrapItem(ModBlocks.BACKPLATE_ITEM.get(), ResourceLocation.fromNamespaceAndPath("signbuilder", "backplate"), true, registry, shapesCache, shaper, modelManager);

        for (RegistrySupplier<Block> blockReg : ModBlocks.ALL_SIGN_BLOCKS) {
            Block block = blockReg.get();
            if (block != null) {
                Item item = block.asItem();
                ResourceLocation id = blockReg.getId();
                wrapItem(item, id, false, registry, shapesCache, shaper, modelManager);
            }
        }
    }

    private static void wrapItem(Item item, ResourceLocation id, boolean isBackplate,
                                 Map<ModelResourceLocation, BakedModel> registry,
                                 Int2ObjectMap<BakedModel> shapesCache,
                                 ItemModelShaper shaper,
                                 ModelManager modelManager) {
        if (item == null || item == net.minecraft.world.item.Items.AIR) return;

        ModelResourceLocation mrl = null;
        if (shaper.shapes != null) {
            mrl = shaper.shapes.get(Item.getId(item));
        }
        if (mrl == null) {
            mrl = ModelResourceLocation.inventory(id);
        }

        BakedModel raw = (shapesCache != null) ? shapesCache.get(Item.getId(item)) : null;
        if (raw == null) {
            raw = shaper.getItemModel(item);
        }
        if (raw == null || raw == modelManager.getMissingModel()) {
            if (registry != null) {
                raw = registry.get(mrl);
            }
        }
        if (raw == null || raw == modelManager.getMissingModel()) {
            raw = modelManager.getModel(mrl);
        }
        if (raw == null || raw == modelManager.getMissingModel()) {
            return;
        }

        BakedModel original = raw;
        while (original instanceof SignBuilderBakedModel sbbm) {
            original = sbbm.originalModel;
        }

        SignBuilderBakedModel wrapped = new SignBuilderBakedModel(original, isBackplate);

        Map<Item, BakedModel> shaperModels = getShaperModelsMap(shaper);
        if (shaperModels != null) {
            shaperModels.put(item, wrapped);
        }
        if (shapesCache != null) {
            shapesCache.put(Item.getId(item), wrapped);
        }
        if (registry != null) {
            registry.put(mrl, wrapped);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Item, BakedModel> getShaperModelsMap(ItemModelShaper shaper) {
        if (shaper == null) return null;
        if (shaperModelsField != null) {
            try {
                return (Map<Item, BakedModel>) shaperModelsField.get(shaper);
            } catch (Exception ignored) {}
        }

        Class<?> clazz = shaper.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(shaper);
                        if (val instanceof Map<?, ?> m) {
                            if ("models".equals(f.getName())) {
                                shaperModelsField = f;
                                return (Map<Item, BakedModel>) m;
                            }
                            if (!m.isEmpty() && m.values().iterator().next() instanceof BakedModel) {
                                shaperModelsField = f;
                                return (Map<Item, BakedModel>) m;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<ModelResourceLocation, BakedModel> getBakedRegistry(ModelManager modelManager) {
        if (bakedRegistryField != null) {
            try {
                return (Map<ModelResourceLocation, BakedModel>) bakedRegistryField.get(modelManager);
            } catch (Exception ignored) {}
        }

        String[] fieldNames = {"bakedRegistry", "f_119400_", "field_4056"};
        for (String name : fieldNames) {
            try {
                Field f = ModelManager.class.getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(modelManager);
                if (val instanceof Map<?, ?> m) {
                    bakedRegistryField = f;
                    return (Map<ModelResourceLocation, BakedModel>) m;
                }
            } catch (Exception ignored) {}
        }

        for (Field f : ModelManager.class.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    Object val = f.get(modelManager);
                    if (val instanceof Map<?, ?> m) {
                        bakedRegistryField = f;
                        return (Map<ModelResourceLocation, BakedModel>) m;
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Int2ObjectMap<BakedModel> getShapesCache(ItemModelShaper shaper) {
        if (shapesCacheField != null) {
            try {
                return (Int2ObjectMap<BakedModel>) shapesCacheField.get(shaper);
            } catch (Exception ignored) {}
        }

        String[] fieldNames = {"shapesCache", "f_110864_", "field_4013"};
        for (String name : fieldNames) {
            try {
                Field f = ItemModelShaper.class.getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(shaper);
                if (val instanceof Int2ObjectMap<?> m) {
                    shapesCacheField = f;
                    return (Int2ObjectMap<BakedModel>) m;
                }
            } catch (Exception ignored) {}
        }

        for (Field f : ItemModelShaper.class.getDeclaredFields()) {
            if (Int2ObjectMap.class.isAssignableFrom(f.getType()) && !Modifier.isPublic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    Object val = f.get(shaper);
                    if (val instanceof Int2ObjectMap<?> m) {
                        shapesCacheField = f;
                        return (Int2ObjectMap<BakedModel>) m;
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
