package com.akingno.winternightak.block.datagen;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.world.gen.ModConfiguredFeatures;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import com.akingno.winternightak.world.gen.PolarSnowFeature;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    // 建立注册表构建器，把我们的 ModConfiguredFeatures 加进去
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, context -> { ModConfiguredFeatures.bootstrap(context); PolarSnowFeature.configured(context); })
            .add(Registries.PLACED_FEATURE, PolarSnowFeature::placed)
            .add(Registries.BIOME, PolarWorldgen::bootstrapBiomes)
            .add(Registries.NOISE, PolarWorldgen::bootstrapNoises)
            .add(Registries.NOISE_SETTINGS, PolarWorldgen::bootstrapSettings)
            .add(Registries.DIMENSION_TYPE, PolarWorldgen::bootstrapDimensionTypes)
            .add(Registries.WORLD_PRESET, PolarWorldgen::bootstrapPresets);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(WinterNight.MOD_ID));
    }
}
