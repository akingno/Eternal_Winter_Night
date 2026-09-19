package com.akingno.winternightak.compat;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.world.PolarAdventureSettings;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
public class BlizzardTemperature extends TempModifier {
    @SubscribeEvent
    public static void register(TempModifierRegisterEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(WinterNight.MOD_ID, "blizzard"), BlizzardTemperature::new);
    }
    @Override protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        boolean exposed = entity.level().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)
                && entity.level().canSeeSky(entity.blockPosition().above()) && !entity.isInWaterOrBubble();
        return temperature -> exposed ? temperature - PolarAdventureSettings.BLIZZARD_COOLING : temperature;
    }
}
