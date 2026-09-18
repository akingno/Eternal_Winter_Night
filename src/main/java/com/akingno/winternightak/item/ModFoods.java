package com.akingno.winternightak.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties ICEGLASS =
            (new FoodProperties.Builder())
                    .nutrition(2)
                    .saturationMod(0.1f)
                    .build();
    //ep8 2:20 饭
    public static final FoodProperties MESHI = new FoodProperties.Builder().nutrition(3)
            .saturationMod(0.1f).build();


    public static final FoodProperties GYUMESHI = new FoodProperties.Builder().
            nutrition(10).saturationMod(0.4f).build();

    public static final FoodProperties KATSUMESHI = new FoodProperties.Builder().
            nutrition(12).saturationMod(0.4f).build();

    public static final FoodProperties KINOKOMESHI = new FoodProperties.Builder().
            nutrition(5).saturationMod(0.3f)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION,
                    200, 0), 0.1f).build();

    public static final FoodProperties EGGMESHI = new FoodProperties.Builder()
            .nutrition(5).saturationMod(0.1f).build();

    public static final FoodProperties OYAKODON = new FoodProperties.Builder()
            .nutrition(8).saturationMod(0.2f).build();

    public static final FoodProperties TEKKA = new FoodProperties.Builder()
            .nutrition(7).saturationMod(0.1f).build();

    public static final FoodProperties TAKEDON = new FoodProperties.Builder().
            nutrition(5).saturationMod(0.1f).build();

}
