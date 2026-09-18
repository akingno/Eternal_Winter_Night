package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;


public class ModConfiguredFeatures {
    // 1. 定义 Key
    // 这个 Key 就像是一个指针，指向我们将要生成的 JSON 数据
    public static final ResourceKey<ConfiguredFeature<?, ?>> SAKURA_KEY = registerKey("sakura");

    // 2. Bootstrap 方法
    // 这里是真正定义树长什么样的地方
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {

        // 将旧版的 BlobFoliagePlacer(FeatureSpread.create(2), FeatureSpread.create(0), 3) 翻译过来：
        // radius = 2, offset = 0, height = 3

        // 将旧版的 StraightTrunkPlacer(5,3,0) 翻译过来：
        // baseHeight = 5, heightRandA = 3, heightRandB = 0

        register(context, SAKURA_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.SAKURA_LOG.get()), // 原木
                new StraightTrunkPlacer(5, 3, 0), // 树干放置器

                BlockStateProvider.simple(ModBlocks.SAKURA_LEAVES.get()), // 树叶
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), // 树叶放置器

                new TwoLayersFeatureSize(1, 0, 1) // 树木的占地大小计算
        ).ignoreVines().build());
    }

    // 辅助方法：创建 Key
    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(WinterNight.MOD_ID, name));
    }

    // 辅助方法：注册 Feature
    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
