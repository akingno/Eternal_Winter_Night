package com.akingno.winternightak.client;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.client.particle.FallenLeafParticle;
import com.akingno.winternightak.client.particle.ModParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        // 1.20.1 不再使用 Minecraft.getInstance().particles
        // 而是直接使用 event.registerSpriteSet 或者 event.registerSpecial

        // 这里的 registerSpriteSet 适用于需要贴图的粒子（比如落叶）
        // 它会自动把贴图集合传给你的 Factory 构造函数
        event.registerSpriteSet(
                ModParticles.SAKURA_LEAF.get(),
                FallenLeafParticle.Factory::new
        );
    }
}

