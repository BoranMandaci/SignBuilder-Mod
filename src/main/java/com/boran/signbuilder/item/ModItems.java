package com.boran.signbuilder.item;

import com.boran.signbuilder.SignBuilder;
import com.boran.signbuilder.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {


    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "signbuilder");

    public static final RegistryObject<Item> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HARF_A_ITEM = ITEMS.register("harf_a",
            () -> new BlockItem(ModBlocks.HARF_A.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_B_ITEM = ITEMS.register("harf_b",
            () -> new BlockItem(ModBlocks.HARF_B.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_C_ITEM = ITEMS.register("harf_c",
            () -> new BlockItem(ModBlocks.HARF_C.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_D_ITEM = ITEMS.register("harf_d",
            () -> new BlockItem(ModBlocks.HARF_D.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_E_ITEM = ITEMS.register("harf_e",
            () -> new BlockItem(ModBlocks.HARF_E.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_F_ITEM = ITEMS.register("harf_f",
            () -> new BlockItem(ModBlocks.HARF_F.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_G_ITEM = ITEMS.register("harf_g",
            () -> new BlockItem(ModBlocks.HARF_G.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_H_ITEM = ITEMS.register("harf_h",
            () -> new BlockItem(ModBlocks.HARF_H.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_I_ITEM = ITEMS.register("harf_i",
            () -> new BlockItem(ModBlocks.HARF_I.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_J_ITEM = ITEMS.register("harf_j",
            () -> new BlockItem(ModBlocks.HARF_J.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_K_ITEM = ITEMS.register("harf_k",
            () -> new BlockItem(ModBlocks.HARF_K.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_L_ITEM = ITEMS.register("harf_l",
            () -> new BlockItem(ModBlocks.HARF_L.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_M_ITEM = ITEMS.register("harf_m",
            () -> new BlockItem(ModBlocks.HARF_M.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_N_ITEM = ITEMS.register("harf_n",
            () -> new BlockItem(ModBlocks.HARF_N.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_O_ITEM = ITEMS.register("harf_o",
            () -> new BlockItem(ModBlocks.HARF_O.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_P_ITEM = ITEMS.register("harf_p",
            () -> new BlockItem(ModBlocks.HARF_P.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_R_ITEM = ITEMS.register("harf_r",
            () -> new BlockItem(ModBlocks.HARF_R.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_S_ITEM = ITEMS.register("harf_s",
            () -> new BlockItem(ModBlocks.HARF_S.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_T_ITEM = ITEMS.register("harf_t",
            () -> new BlockItem(ModBlocks.HARF_T.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_U_ITEM = ITEMS.register("harf_u",
            () -> new BlockItem(ModBlocks.HARF_U.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_V_ITEM = ITEMS.register("harf_v",
            () -> new BlockItem(ModBlocks.HARF_V.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_Y_ITEM = ITEMS.register("harf_y",
            () -> new BlockItem(ModBlocks.HARF_Y.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_Z_ITEM = ITEMS.register("harf_z",
            () -> new BlockItem(ModBlocks.HARF_Z.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_W_ITEM = ITEMS.register("harf_w",
            () -> new BlockItem(ModBlocks.HARF_W.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARF_X_ITEM = ITEMS.register("harf_x",
            () -> new BlockItem(ModBlocks.HARF_X.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
