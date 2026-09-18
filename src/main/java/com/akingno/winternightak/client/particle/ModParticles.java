package com.akingno.winternightak.client.particle;

import net.minecraft.core.particles.ParticleType;
import com.akingno.winternightak.WinterNight;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, WinterNight.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SAKURA_LEAF = PARTICLE_TYPES.register("sakura_leaf",
                    () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
