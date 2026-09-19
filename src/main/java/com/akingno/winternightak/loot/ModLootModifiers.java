package com.akingno.winternightak.loot;

import com.akingno.winternightak.WinterNight;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 注册狩猎掉落修饰器的JSON解码器；影响的动物与追加掉落表由数据文件指定。 */
public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, WinterNight.MOD_ID);

    public static final RegistryObject<Codec<HuntingLootModifier>> HUNTING =
            SERIALIZERS.register("hunting", () -> HuntingLootModifier.CODEC);

    private ModLootModifiers() {}
}
