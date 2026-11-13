package com.boran.signbuilder.client.render;

import com.boran.signbuilder.block.ModBlocks;
import com.boran.signbuilder.block.SignMaterial;
import com.boran.signbuilder.util.RainbowColorHelper;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class SignBuilderItemModel implements ItemModel {
    private final ItemModel originalModel;
    private final BakedModel baseModel;
    private final boolean isBackplate;

    private static final Set<ResourceLocation> TARGET_IDS = new HashSet<>();
    private static volatile Field itemStackModelsField = null;

    public SignBuilderItemModel(ItemModel originalModel, ResourceLocation id, boolean isBackplate) {
        this.originalModel = originalModel;
        this.isBackplate = isBackplate;
        this.baseModel = resolveBakedModel(originalModel, id);
    }

    private static BakedModel resolveBakedModel(ItemModel itemModel, ResourceLocation id) {
        if (itemModel != null) {
            BakedModel bm = extractBakedModelFromItemModel(itemModel);
            if (bm != null) return bm;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getModelManager() != null) {
                BakedModel bm = mc.getModelManager().getModel(new ModelResourceLocation(id, "inventory"));
                if (bm != null && bm != mc.getModelManager().getMissingModel()) {
                    return bm;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static BakedModel extractBakedModelFromItemModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        Class<?> clazz = itemModel.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (BakedModel.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(itemModel);
                        if (val instanceof BakedModel bm) {
                            return bm;
                        }
                    } catch (Exception ignored) {}
                } else if (ItemModel.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(itemModel);
                        if (val instanceof ItemModel subModel && val != itemModel) {
                            BakedModel bm = extractBakedModelFromItemModel(subModel);
                            if (bm != null) return bm;
                        }
                    } catch (Exception ignored) {}
                } else if (List.class.isAssignableFrom(f.getType())) {
                    try {
                        f.setAccessible(true);
                        Object val = f.get(itemModel);
                        if (val instanceof List<?> list) {
                            for (Object o : list) {
                                if (o instanceof ItemModel subModel) {
                                    BakedModel bm = extractBakedModelFromItemModel(subModel);
                                    if (bm != null) return bm;
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

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, LivingEntity entity, int seed) {
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData == null || customData.isEmpty() || this.baseModel == null) {
            this.originalModel.update(state, stack, resolver, displayContext, level, entity, seed);
            return;
        }

        CompoundTag beTag = customData.copyTag();

        SignMaterial fMat = SignMaterial.DEFAULT;
        SignMaterial bMat = SignMaterial.DEFAULT;
        int frontColor = 0xFFFFFF;
        int backColor = 0xFFFFFF;
        boolean fRainbow = false;
        boolean bRainbow = false;
        boolean fGlow = false;
        boolean bGlow = false;

        if (this.isBackplate) {
            String fMatStr = beTag.contains("BackplateFrontMaterial") ? beTag.getString("BackplateFrontMaterial")
                    : (beTag.contains("BPFrontMat") ? beTag.getString("BPFrontMat")
                    : (beTag.contains("BPFMat") ? beTag.getString("BPFMat") : ""));
            if (!fMatStr.isEmpty()) {
                try { fMat = SignMaterial.valueOf(fMatStr); } catch (Exception ignored) {}
            }

            String bMatStr = beTag.contains("BackplateBackMaterial") ? beTag.getString("BackplateBackMaterial")
                    : (beTag.contains("BPBackMat") ? beTag.getString("BPBackMat")
                    : (beTag.contains("BPBMat") ? beTag.getString("BPBMat") : ""));
            if (!bMatStr.isEmpty()) {
                try { bMat = SignMaterial.valueOf(bMatStr); } catch (Exception ignored) {}
            }

            if (beTag.contains("BackplateFrontColor")) frontColor = beTag.getInt("BackplateFrontColor");
            else if (beTag.contains("BPFrontColor")) frontColor = beTag.getInt("BPFrontColor");
            else if (beTag.contains("BPFColor")) frontColor = beTag.getInt("BPFColor");

            if (beTag.contains("BackplateBackColor")) backColor = beTag.getInt("BackplateBackColor");
            else if (beTag.contains("BPBackColor")) backColor = beTag.getInt("BPBackColor");
            else if (beTag.contains("BPBColor")) backColor = beTag.getInt("BPBColor");

            fRainbow = beTag.getBoolean("BackplateFrontRainbow") || beTag.getBoolean("BPFrontRainbow") || beTag.getBoolean("BPFRainbow");
            bRainbow = beTag.getBoolean("BackplateBackRainbow") || beTag.getBoolean("BPBackRainbow") || beTag.getBoolean("BPBRainbow");
            fGlow = beTag.getBoolean("BackplateFrontGlowInked") || beTag.getBoolean("BPFrontGlowInked");
            bGlow = beTag.getBoolean("BackplateBackGlowInked") || beTag.getBoolean("BPBackGlowInked");
        } else {
            if (beTag.contains("SavedMaterial")) {
                try { fMat = SignMaterial.valueOf(beTag.getString("SavedMaterial")); } catch (Exception ignored) {}
            } else if (beTag.contains("savedMaterial")) {
                try { fMat = SignMaterial.valueOf(beTag.getString("savedMaterial")); } catch (Exception ignored) {}
            } else if (beTag.contains("material")) {
                try { fMat = SignMaterial.valueOf(beTag.getString("material").toUpperCase(Locale.ROOT)); } catch (Exception ignored) {}
            }

            if (beTag.contains("RGBColor")) frontColor = beTag.getInt("RGBColor");
            else if (beTag.contains("rgbColor")) frontColor = beTag.getInt("rgbColor");

            backColor = frontColor;
            fRainbow = beTag.getBoolean("IsRainbow") || beTag.getBoolean("isRainbow");
            bRainbow = fRainbow;
            fGlow = beTag.getBoolean("IsGlowInked") || beTag.getBoolean("isGlowInked");
            bGlow = fGlow;
        }

        boolean hasCustomization = (fMat != SignMaterial.DEFAULT)
                || (bMat != SignMaterial.DEFAULT)
                || (frontColor != 0xFFFFFF && frontColor != 0)
                || (backColor != 0xFFFFFF && backColor != 0)
                || fRainbow || bRainbow || fGlow || bGlow;

        if (!hasCustomization) {
            this.originalModel.update(state, stack, resolver, displayContext, level, entity, seed);
            return;
        }

        if (fRainbow) frontColor = RainbowColorHelper.getRainbowRgb();
        if (bRainbow) backColor = RainbowColorHelper.getRainbowRgb();

        BakedModel finalModel;
        if (fMat != SignMaterial.DEFAULT || bMat != SignMaterial.DEFAULT || fGlow || bGlow) {
            finalModel = LetterBlockEntityRenderer.getMaterialModel(this.baseModel, fMat, bMat, !this.isBackplate, fGlow || bGlow);
        } else {
            finalModel = this.baseModel;
        }

        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
        }

        int[] tintLayers = layer.prepareTintLayers(2);
        if (this.isBackplate) {
            tintLayers[0] = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFFFF : (0xFF000000 | (frontColor & 0xFFFFFF));
            tintLayers[1] = (bMat != SignMaterial.DEFAULT) ? 0xFFFFFFFF : (0xFF000000 | (backColor & 0xFFFFFF));
        } else {
            int c = (fMat != SignMaterial.DEFAULT) ? 0xFFFFFFFF : (0xFF000000 | (frontColor & 0xFFFFFF));
            tintLayers[0] = c;
            tintLayers[1] = c;
        }

        RenderType renderType = ItemBlockRenderTypes.getRenderType(stack);
        layer.setupBlockModel(finalModel, renderType);
    }

    private static volatile Object lastWrappedMap = null;

    public static boolean isOrWrapsSignBuilderItemModel(ItemModel model) {
        if (model == null) return false;
        if (model instanceof SignBuilderItemModel) return true;
        ItemModel current = model;
        Set<ItemModel> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        while (current != null && visited.add(current)) {
            if (current instanceof SignBuilderItemModel) return true;
            ItemModel next = null;
            Class<?> clazz = current.getClass();
            while (clazz != null && clazz != Object.class) {
                try {
                    for (Field f : clazz.getDeclaredFields()) {
                        if (ItemModel.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                            f.setAccessible(true);
                            Object val = f.get(current);
                            if (val instanceof ItemModel im && !visited.contains(im)) {
                                next = im;
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

    @SuppressWarnings("unchecked")
    public static void checkAndWrap(Minecraft client) {
        if (client == null) return;
        ModelManager modelManager = client.getModelManager();
        if (modelManager == null) return;

        Map<ResourceLocation, ItemModel> map = getItemStackModels(modelManager);
        if (map == null || map.isEmpty()) return;
        if (map == lastWrappedMap) return;

        ItemModel backplateModel = null;
        try {
            backplateModel = map.get(ModBlocks.BACKPLATE_ITEM.getId());
        } catch (Throwable ignored) {}

        if (isOrWrapsSignBuilderItemModel(backplateModel)) {
            lastWrappedMap = map;
            return;
        }

        lastWrappedMap = map;
        wrapAll(modelManager, map);
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, ItemModel> getItemStackModels(ModelManager modelManager) {
        try {
            if (itemStackModelsField != null) {
                return (Map<ResourceLocation, ItemModel>) itemStackModelsField.get(modelManager);
            }
            for (Field f : ModelManager.class.getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Object obj = f.get(modelManager);
                    if (obj instanceof Map<?, ?> m && !m.isEmpty()) {
                        Object sample = m.values().iterator().next();
                        if (sample instanceof ItemModel) {
                            itemStackModelsField = f;
                            return (Map<ResourceLocation, ItemModel>) obj;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static synchronized void ensureTargetIds() {
        if (TARGET_IDS.isEmpty()) {
            TARGET_IDS.add(ResourceLocation.fromNamespaceAndPath("signbuilder", "backplate"));
            for (RegistrySupplier<Block> b : ModBlocks.ALL_SIGN_BLOCKS) {
                TARGET_IDS.add(b.getId());
            }
        }
    }

    private static synchronized void wrapAll(ModelManager modelManager, Map<ResourceLocation, ItemModel> map) {
        ensureTargetIds();
        LetterBlockEntityRenderer.clearModelCache();
        MaterialBakedModel.clearQuadCache();

        boolean mapModified = false;
        Map<ResourceLocation, ItemModel> targetMap = map;

        for (ResourceLocation id : TARGET_IDS) {
            ItemModel original = null;
            try {
                original = targetMap.get(id);
            } catch (Throwable ignored) {}
            if (original != null && !isOrWrapsSignBuilderItemModel(original)) {
                boolean isBackplate = id.getPath().equals("backplate");
                SignBuilderItemModel wrapped = new SignBuilderItemModel(original, id, isBackplate);
                try {
                    targetMap.put(id, wrapped);
                } catch (UnsupportedOperationException e) {
                    if (!mapModified) {
                        try {
                            targetMap = new HashMap<>(map);
                            if (itemStackModelsField != null) {
                                itemStackModelsField.set(modelManager, targetMap);
                            }
                            mapModified = true;
                        } catch (Throwable ignored) {}
                    }
                    try {
                        targetMap.put(id, wrapped);
                    } catch (Throwable ignored) {}
                } catch (Throwable ignored) {}
            }
        }
    }
}
