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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
                CompoundTag beTag = stack.getTagElement("BlockEntityTag");
                if (beTag == null || beTag.isEmpty()) {
                    beTag = stack.getTag();
                }

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
                    fGlow = beTag.getBoolean("BackplateFrontGlowInked") || beTag.getBoolean("BPFrontGlow") || beTag.getBoolean("BPFGlow");
                    bGlow = beTag.getBoolean("BackplateBackGlowInked") || beTag.getBoolean("BPBackGlow") || beTag.getBoolean("BPBGlow");
                } else {
                    String matStr = beTag.contains("SavedMaterial") ? beTag.getString("SavedMaterial")
                            : (beTag.contains("Material") ? beTag.getString("Material") : "");
                    if (!matStr.isEmpty()) {
                        try { fMat = SignMaterial.valueOf(matStr.toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
                    }
                    fGlow = beTag.getBoolean("GlowInked") || beTag.getBoolean("Glow");
                }

                if (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT || fGlow || bGlow) {
                    return LetterBlockEntityRenderer.getMaterialModel(originalModel, fMat, bMat, !isBackplate, fGlow || bGlow);
                }

                return originalModel.getOverrides().resolve(originalModel, stack, level, entity, seed);
            }
        };
    }

    public BakedModel getOriginalModel() {
        return originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return originalModel.getQuads(state, direction, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return originalModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return originalModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private static volatile Object lastWrappedRegistry = null;

    public static boolean isOrWrapsSignBuilderModel(@Nullable BakedModel model) {
        if (model == null) return false;
        if (model instanceof SignBuilderBakedModel) return true;
        BakedModel current = model;
        Set<BakedModel> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        while (current != null && visited.add(current)) {
            if (current instanceof SignBuilderBakedModel) return true;
            BakedModel next = null;
            Class<?> clazz = current.getClass();
            while (clazz != null && clazz != Object.class) {
                try {
                    for (Field f : clazz.getDeclaredFields()) {
                        if (BakedModel.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                            f.setAccessible(true);
                            Object val = f.get(current);
                            if (val instanceof BakedModel bm && !visited.contains(bm)) {
                                next = bm;
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
                if (next != null) break;
                clazz = clazz.getSuperclass();
            }
            current = next;
        }
        return false;
    }

    public static void onModifyBakingResult(Map<?, ?> models) {
        if (models == null) return;
        wrapModelInMap(models, new ResourceLocation("signbuilder", "backplate"), true);
        for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            wrapModelInMap(models, blockSupplier.getId(), false);
        }
    }

    @SuppressWarnings("unchecked")
    private static void wrapModelInMap(Map<?, ?> rawMap, ResourceLocation id, boolean isBackplate) {
        Map<Object, BakedModel> models = (Map<Object, BakedModel>) rawMap;
        ModelResourceLocation mrl = new ModelResourceLocation(id, "inventory");
        BakedModel existing = models.get(mrl);
        if (existing == null) {
            existing = models.get(id);
        }
        if (existing != null && !isOrWrapsSignBuilderModel(existing)) {
            SignBuilderBakedModel wrapped = new SignBuilderBakedModel(existing, isBackplate);
            try {
                models.put(mrl, wrapped);
                models.put(id, wrapped);
            } catch (Throwable ignored) {}
        }
    }

    public static void checkAndWrap(Minecraft mc) {
        if (mc == null) return;
        ItemModelShaper shaper = mc.getItemRenderer() != null ? mc.getItemRenderer().getItemModelShaper() : null;
        ModelManager modelManager = mc.getModelManager();
        if (shaper == null || modelManager == null) return;

        Map<ResourceLocation, BakedModel> registry = getBakedRegistry(modelManager);
        if (registry == null || registry.isEmpty()) return;
        if (registry == lastWrappedRegistry) return;

        Item bpItem = ModBlocks.BACKPLATE_ITEM.get();
        if (bpItem == null) return;

        ResourceLocation bpId = new ResourceLocation("signbuilder", "backplate");
        ModelResourceLocation bpMrl = new ModelResourceLocation(bpId, "inventory");
        BakedModel managerBpModel = null;
        try {
            managerBpModel = registry.get(bpMrl);
            if (managerBpModel == null) {
                managerBpModel = registry.get(bpId);
            }
        } catch (Throwable ignored) {}

        if (isOrWrapsSignBuilderModel(managerBpModel)) {
            lastWrappedRegistry = registry;
            return;
        }

        BakedModel currentBpModel = null;
        try {
            currentBpModel = shaper.getItemModel(bpItem);
        } catch (Throwable ignored) {}

        boolean needWrap = !isOrWrapsSignBuilderModel(currentBpModel);

        if (!needWrap && !ModBlocks.ALL_SIGN_BLOCKS.isEmpty()) {
            Block firstBlock = ModBlocks.ALL_SIGN_BLOCKS.get(0).get();
            if (firstBlock != null) {
                BakedModel firstModel = null;
                try {
                    firstModel = shaper.getItemModel(firstBlock.asItem());
                } catch (Throwable ignored) {}
                if (!isOrWrapsSignBuilderModel(firstModel)) {
                    needWrap = true;
                }
            }
        }

        lastWrappedRegistry = registry;
        if (!needWrap) {
            return;
        }

        Int2ObjectMap<BakedModel> shapesCache = getShapesCache(shaper);

        wrapItem(bpItem, "backplate", true, shaper, modelManager, registry, shapesCache);
        for (RegistrySupplier<Block> blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            Item item = blockSupplier.get().asItem();
            String path = blockSupplier.getId().getPath();
            wrapItem(item, path, false, shaper, modelManager, registry, shapesCache);
        }
    }

    private static void wrapItem(Item item, String path, boolean isBackplate,
                                 ItemModelShaper shaper, ModelManager modelManager,
                                 Map<ResourceLocation, BakedModel> registry,
                                 Int2ObjectMap<BakedModel> shapesCache) {
        if (item == null || item == net.minecraft.world.item.Items.AIR) return;

        BakedModel current = null;
        try {
            current = shaper.getItemModel(item);
        } catch (Throwable ignored) {}

        if (isOrWrapsSignBuilderModel(current)) {
            return;
        }

        ResourceLocation id = new ResourceLocation("signbuilder", path);
        ModelResourceLocation mrl = new ModelResourceLocation(id, "inventory");
        BakedModel raw = null;
        if (registry != null) {
            try {
                raw = registry.get(mrl);
                if (raw == null) {
                    raw = registry.get(id);
                }
            } catch (Throwable ignored) {}
        }
        if (raw == null && shapesCache != null) {
            try {
                raw = shapesCache.get(Item.getId(item));
            } catch (Throwable ignored) {}
        }
        if (raw == null) {
            raw = current;
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
        if (shaperModels != null && !(shaperModels instanceof Int2ObjectMap)) {
            try {
                shaperModels.put(item, wrapped);
            } catch (Throwable ignored) {}
        }
        if (shapesCache != null) {
            try {
                shapesCache.put(Item.getId(item), wrapped);
            } catch (Throwable ignored) {}
        }
        if (registry != null) {
            try {
                registry.put(mrl, wrapped);
                registry.put(id, wrapped);
            } catch (Throwable ignored) {}
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
                if (Map.class.isAssignableFrom(f.getType()) && !Int2ObjectMap.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(shaper);
                        if (val instanceof Map<?, ?> m && !(m instanceof Int2ObjectMap)) {
                            if ("models".equals(f.getName())) {
                                shaperModelsField = f;
                                return (Map<Item, BakedModel>) m;
                            }
                            if (!m.isEmpty()) {
                                Object firstKey = m.keySet().iterator().next();
                                if (firstKey instanceof Item && m.values().iterator().next() instanceof BakedModel) {
                                    shaperModelsField = f;
                                    return (Map<Item, BakedModel>) m;
                                }
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
    private static Map<ResourceLocation, BakedModel> getBakedRegistry(ModelManager modelManager) {
        if (bakedRegistryField != null) {
            try {
                return (Map<ResourceLocation, BakedModel>) bakedRegistryField.get(modelManager);
            } catch (Exception ignored) {}
        }

        String[] fieldNames = {"bakedRegistry", "f_119397_", "f_119400_", "field_4056"};
        for (String name : fieldNames) {
            try {
                Field f = ModelManager.class.getDeclaredField(name);
                f.setAccessible(true);
                Object val = f.get(modelManager);
                if (val instanceof Map<?, ?> m) {
                    bakedRegistryField = f;
                    return (Map<ResourceLocation, BakedModel>) m;
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
                        return (Map<ResourceLocation, BakedModel>) m;
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

        String[] fieldNames = {"shapesCache", "f_109389_", "f_110864_", "field_4013"};
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
