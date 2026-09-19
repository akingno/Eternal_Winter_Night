package com.akingno.winternightak.item;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.item.custom.MeshiItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.food.Foods;
import com.akingno.winternightak.item.custom.IcePickItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WinterNight.MOD_ID);

    // 第一、二阶段资源入口：油脂是燃料，兽肉复用生牛肉食物值，冰砖为物品而非可放方块。
    public static final RegistryObject<Item> ANIMAL_FAT = ITEMS.register("animal_fat",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FUR_WRAP = ITEMS.register("fur_wrap",
            com.akingno.winternightak.item.custom.FurWrapItem::new);
    public static final RegistryObject<Item> RAW_GAME_MEAT = ITEMS.register("raw_game_meat",
            () -> new Item(new Item.Properties().food(Foods.BEEF)));
    public static final RegistryObject<Item> ICE_BRICK = ITEMS.register("ice_brick",
            () -> new Item(new Item.Properties()));
    // 冰镐使用铁级耐久/速度；1与-2.8分别是额外攻击伤害和攻击速度修饰，不影响生成概率。
    public static final RegistryObject<Item> ICE_PICK = ITEMS.register("ice_pick",
            () -> new IcePickItem(Tiers.IRON, 1, -2.8F, new Item.Properties()));

    public static final RegistryObject<Item> BAMBOO_BUCKET = ITEMS.register("bamboo_bucket",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BAMBOO_SHOOT = ITEMS.register("bamboo_shoot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TUDURA = ITEMS.register("tudura",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> KATANA = ITEMS.register("katana",
            () -> new SwordItem(ModItemTier.KATANA,3,-1.5f, new Item.Properties()));

    
    public static final RegistryObject<Item> ICEGLASS = ITEMS.register("iceglass",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.ICEGLASS).stacksTo(1)));
    public static final RegistryObject<Item> MESHI = ITEMS.register("meshi",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.MESHI).stacksTo(1)));
    public static final RegistryObject<Item> GYUMESHI = ITEMS.register("gyumeshi",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.GYUMESHI).stacksTo(1)));
    public static final RegistryObject<Item> KATSUMESHI = ITEMS.register("katsumeshi",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.KATSUMESHI).stacksTo(1)));
    public static final RegistryObject<Item> KINOKOMESHI = ITEMS.register("kinokomeshi",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.KINOKOMESHI).stacksTo(1)));
    public static final RegistryObject<Item> EGGMESHI = ITEMS.register("eggmeshi",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.EGGMESHI).stacksTo(1)));
    public static final RegistryObject<Item> OYAKODON = ITEMS.register("oyakodon",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.OYAKODON).stacksTo(1)));
    public static final RegistryObject<Item> TEKKA = ITEMS.register("tekka",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.TEKKA).stacksTo(1)));
    public static final RegistryObject<Item> TAKEDON = ITEMS.register("takedon",
            () -> new MeshiItem(new Item.Properties().food(ModFoods.TAKEDON).stacksTo(1)));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
