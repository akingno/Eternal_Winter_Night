package com.akingno.winternightak.client;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.client.particle.ModParticles;
import com.akingno.winternightak.world.PolarAdventureSettings;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID, value = Dist.CLIENT)
public final class BlizzardClient {
    private static boolean active;
    private static float strength;
    public static void setActive(boolean value) { active = value; }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { active = false; strength = 0; }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) { strength = 0; return; }
        if (mc.isPaused()) return;
        boolean polar = mc.level.dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE);
        if (!polar) { strength = 0; return; }
        strength = Mth.clamp(strength + (active && polar ? 0.01F : -0.025F), 0, 1);
        if (strength == 0) return;
        var camera = mc.gameRenderer.getMainCamera().getPosition();
        var random = mc.level.random;
        int count = switch (mc.options.particles().get()) { case ALL -> 96; case DECREASED -> 48; case MINIMAL -> 12; };
        for (int i = 0; i < count * strength; i++) {
            double x = camera.x + random.nextDouble() * 24 - 12;
            double y = camera.y + random.nextDouble() * 10 - 2;
            double z = camera.z + random.nextDouble() * 24 - 12;
            var pos = BlockPos.containing(x, y, z);
            if (mc.level.hasChunkAt(pos) && mc.level.canSeeSky(pos) && mc.level.getBlockState(pos).isAir())
                mc.level.addParticle(ModParticles.BLIZZARD_SNOW.get(), x, y, z, 0.27, -0.045, 0.10);
        }
    }
    @SubscribeEvent public static void fog(ViewportEvent.RenderFog event) {
        if (strength <= 0 || event.getType() != FogType.NONE) return;
        // Keep underwater/powder-snow fog untouched. Indoors no particles are spawned.
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), Mth.lerp(strength, event.getNearPlaneDistance(), 1)));
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), Mth.lerp(strength, event.getFarPlaneDistance(), PolarAdventureSettings.BLIZZARD_FOG_END)));
        event.setCanceled(true);
    }
    @SubscribeEvent public static void color(ViewportEvent.ComputeFogColor event) {
        if (strength <= 0 || event.getCamera().getFluidInCamera() != FogType.NONE) return;
        event.setRed(Mth.lerp(strength, event.getRed(), 0));
        event.setGreen(Mth.lerp(strength, event.getGreen(), 0));
        event.setBlue(Mth.lerp(strength, event.getBlue(), 0));
    }
}
