package com.akingno.winternightak.client.particle;

import net.minecraft.core.particles.ParticleType;
import com.akingno.winternightak.WinterNight;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    // false表示尊重玩家“全部/减少/最少”粒子设置，不强制显示每一片粒子。
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, WinterNight.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SAKURA_LEAF = PARTICLE_TYPES.register("sakura_leaf",
                    () -> new SimpleParticleType(false));
    // false表示尊重玩家粒子限制，不强制显示每一片雪；具体形状由客户端工厂创建。
    public static final RegistryObject<SimpleParticleType> BLIZZARD_SNOW = PARTICLE_TYPES.register("blizzard_snow",
                    () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
