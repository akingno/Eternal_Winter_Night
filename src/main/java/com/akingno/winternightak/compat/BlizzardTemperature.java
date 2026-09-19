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
/** Cold Sweat环境温度修饰器：暴雪时仅露天且不在水中的实体额外降温，不直接改玩家体温。 */
public class BlizzardTemperature extends TempModifier {
    @SubscribeEvent
    public static void register(TempModifierRegisterEvent event) {
        // 注册名写入Cold Sweat温度修饰器注册表；修改名称会破坏旧存档或其他配置对它的引用。
        event.register(ResourceLocation.fromNamespaceAndPath(WinterNight.MOD_ID, "blizzard"), BlizzardTemperature::new);
    }
    @Override protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        // 只有极地维度、头顶可见天空且不在水中的实体算暴露；室内与水下不追加暴雪降温。
        boolean exposed = entity.level().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)
                && entity.level().canSeeSky(entity.blockPosition().above()) && !entity.isInWaterOrBubble();
        return temperature -> exposed ? temperature - PolarAdventureSettings.BLIZZARD_COOLING : temperature;
    }
}
