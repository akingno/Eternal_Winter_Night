package com.akingno.winternightak.world.gen;

import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/** 完整复用原版冰山，包括雪块；允许雪块按本模组重力规则下落。保留注册名兼容已有配置。 */
public final class PolarIcebergFeature extends IcebergFeature {
    public PolarIcebergFeature() { super(BlockStateConfiguration.CODEC); }
    public static void register() {
        PolarSnowFeature.FEATURES.register("polar_iceberg", PolarIcebergFeature::new);
    }
}
