package com.akingno.winternightak.entity;

import com.akingno.winternightak.WinterNight;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.*;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WinterNight.MOD_ID);
    // 尺寸0.5格、跟踪4区块、更新20 tick沿用原版三叉戟；无地形搜索或额外全局扫描。
    public static final RegistryObject<EntityType<StoneJavelinEntity>> STONE_JAVELIN = TYPES.register("stone_javelin",
            () -> EntityType.Builder.<StoneJavelinEntity>of(StoneJavelinEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("winternightak:stone_javelin"));
    private ModEntities() {}
}
