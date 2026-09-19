package com.akingno.winternightak.compat;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.block.entity.CampfireSettings;
import com.akingno.winternightak.block.entity.LampSettings;
import com.akingno.winternightak.block.entity.TorchSettings;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.momosoftworks.coldsweat.api.event.core.registry.BlockTempRegisterEvent;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
/** 油灯、火把、营火共用一个热源累计账户；上限是环境增温上限，而非玩家最终体温。 */
public final class CampfireTemperature {
    @SubscribeEvent
    public static void register(BlockTempRegisterEvent event) {
        // First match wins in Cold Sweat: avoid also applying its generic campfire-tag heat.
        // One BlockTemp instance makes all three sources share Cold Sweat's total-effect cap.
        // 三种设备必须共用同一个BlockTemp对象；首个匹配优先，避免通用营火标签再次计温。
        event.registerFirst(new BlockTemp(ModBlocks.CAMPFIRE.get(), ModBlocks.PRIMITIVE_LAMP.get(), ModBlocks.POLAR_TORCH.get()) {
            @Override public boolean isValid(Level level, BlockPos pos, BlockState state) {
                return state.getValue(BlockStateProperties.LIT)
                        && (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !state.getValue(BlockStateProperties.WATERLOGGED));
            }
            @Override public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance) {
                if (!isValid(level, pos, state)) return 0;
                double heat = state.is(ModBlocks.CAMPFIRE.get()) ? CampfireSettings.HEAT
                        : state.is(ModBlocks.PRIMITIVE_LAMP.get()) ? LampSettings.HEAT : TorchSettings.HEAT;
                double range = state.is(ModBlocks.CAMPFIRE.get()) ? CampfireSettings.HEAT_RANGE
                        : state.is(ModBlocks.PRIMITIVE_LAMP.get()) ? LampSettings.HEAT_RANGE : TorchSettings.HEAT_RANGE;
                // Cold Sweat caches range per source instance, so apply each block's falloff here.
                // 0.5格内满强度，之后线性降低，到range为0；0/1钳制防止负热量或超额增温。
                return heat * Math.max(0, Math.min(1, (range - distance) / (range - 0.5)));
            }
            // 上面已手动按设备范围衰减，这里关闭框架二次衰减。
            @Override public boolean fades(LivingEntity entity, Level level, BlockPos pos, BlockState state) { return false; }
            @Override public double getRange(LivingEntity entity, Level level, BlockPos pos, BlockState state) {
                return Math.max(CampfireSettings.HEAT_RANGE, Math.max(LampSettings.HEAT_RANGE, TorchSettings.HEAT_RANGE));
            }
            @Override public double getMaxEffect(LivingEntity entity, Level level, BlockPos pos, BlockState state) { return CampfireSettings.MAX_HEAT; }
        });
    }
}
