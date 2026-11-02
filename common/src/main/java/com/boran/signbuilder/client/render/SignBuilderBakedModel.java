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
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
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

    private static volatile ModelBakery CURRENT_BAKERY = null;
    private static final ModelBakery DUMMY_BAKERY = createDummyBakery();

    public static void setCurrentBakery(ModelBakery bakery) {
        if (bakery != null) {
            CURRENT_BAKERY = bakery;
        }
    }

    private static ModelBakery getEffectiveBakery() {
        if (CURRENT_BAKERY != null) {
            return CURRENT_BAKERY;
        }
        try {
            ModelManager mm = Minecraft.getInstance().getModelManager();
            if (mm != null) {
                for (Field f : ModelManager.class.getDeclaredFields()) {
                    if (ModelBakery.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        Object val = f.get(mm);
                        if (val instanceof ModelBakery mb) {
                            CURRENT_BAKERY = mb;
                            return mb;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return DUMMY_BAKERY;
    }

    private static ModelBakery createDummyBakery() {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
            ModelBakery mb = (ModelBakery) unsafe.allocateInstance(ModelBakery.class);
            AtlasSet atlasSet = new AtlasSet(Collections.emptyList());
            for (Field field : ModelBakery.class.getDeclaredFields()) {
                if (AtlasSet.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    field.set(mb, atlasSet);
                    break;
                }
            }
            return mb;
        } catch (Throwable ignored) {
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
        this.overrides = new ItemOverrides(getEffectiveBakery(), null, r -> null, Collections.emptyList()) {
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

    public static void onModifyBakingResult(Map<?, ?> models) {
        if (models == null) return;
        wrapModelInMap(models, new ResourceLocation("signbuilder", "backplate"), true);
        for (var blockSupplier : ModBlocks.ALL_SIGN_BLOCKS) {
            wrapModelInMap(models, blockSupplier.getId(), false);
        }
    }

    public static void onModifyBakingResult(Map<?, ?> models, ModelBakery bakery) {
        setCurrentBakery(bakery);
        onModifyBakingResult(models);
    }

    @SuppressWarnings("unchecked")
    private static void wrapModelInMap(Map<?, ?> rawMap, ResourceLocation id, boolean isBackplate) {
        Map<Object, BakedModel> models = (Map<Object, BakedModel>) rawMap;
        ModelResourceLocation mrl = new ModelResourceLocation(id, "inventory");
        BakedModel existing = models.get(mrl);
        if (existing == null) {
            existing = models.get(id);
        }
        if (existing != null) {
            SignBuilderBakedModel wrapped = new SignBuilderBakedModel(existing, isBackplate);
            models.put(mrl, wrapped);
            models.put(id, wrapped);
        }
    }

    public static void checkAndWrap(Minecraft mc) {
        if (mc == null) return;
        ItemModelShaper shaper = mc.getItemRenderer() != null ? mc.getItemRenderer().getItemModelShaper() : null;
        ModelManager modelManager = mc.getModelManager();
        if (shaper == null || modelManager == null) return;

        Item bpItem = ModBlocks.BACKPLATE_ITEM.get();
        if (bpItem != null && shaper.getItemModel(bpItem) instanceof SignBuilderBakedModel) {
            return;
        }

        Map<ResourceLocation, BakedModel> registry = getBakedRegistry(modelManager);
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

        BakedModel current = shaper.getItemModel(item);
        if (current instanceof SignBuilderBakedModel) {
            return;
        }

        ResourceLocation id = new ResourceLocation("signbuilder", path);
        ModelResourceLocation mrl = new ModelResourceLocation(id, "inventory");
        BakedModel raw = null;
        if (registry != null) {
            raw = registry.get(mrl);
            if (raw == null) {
                raw = registry.get(id);
            }
        }
        if (raw == null && shapesCache != null) {
            raw = shapesCache.get(Item.getId(item));
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
            } catch (Exception ignored) {}
        }
        if (shapesCache != null) {
            shapesCache.put(Item.getId(item), wrapped);
        }
        if (registry != null) {
            registry.put(mrl, wrapped);
            registry.put(id, wrapped);
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
