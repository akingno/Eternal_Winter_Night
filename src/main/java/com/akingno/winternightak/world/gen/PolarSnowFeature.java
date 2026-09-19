package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;

// 每个区块补薄雪的地物；仅覆盖硬冰/冻结土壤，不覆盖建筑或改成整块雪。
public class PolarSnowFeature extends Feature<NoneFeatureConfiguration> {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, WinterNight.MOD_ID);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SNOW = FEATURES.register("polar_snow", PolarSnowFeature::new);
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED = PolarWorldgen.key(Registries.CONFIGURED_FEATURE, "polar_snow");
    public static final ResourceKey<PlacedFeature> PLACED = PolarWorldgen.key(Registries.PLACED_FEATURE, "polar_snow");

    public PolarSnowFeature() { super(NoneFeatureConfiguration.CODEC); }

    public static void configured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CONFIGURED, new ConfiguredFeature<>(SNOW.get(), NoneFeatureConfiguration.INSTANCE));
    }

    public static void placed(BootstapContext<PlacedFeature> context) {
        context.register(PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CONFIGURED), List.of()));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        // 16是区块边长，不是密度参数；完整遍历本区块的256个地表列。
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, context.origin().offset(x, 0, z));
            var ground = level.getBlockState(top.below());
            if (level.isEmptyBlock(top) && (ground.is(ModBlocks.HARD_ICE.get()) || ground.is(ModBlocks.FROZEN_SOIL.get())))
                level.setBlock(top, Blocks.SNOW.defaultBlockState(), 2);
        }
        return true;
    }
}
