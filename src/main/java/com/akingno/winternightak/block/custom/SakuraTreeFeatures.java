package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class SakuraTreeFeatures {

    public static List<Pair<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>>> ENTRY = new ArrayList<>();

    public static final ResourceKey<ConfiguredFeature<?, ?>> SAKURA_KEY = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(WinterNight.MOD_ID, "sakura"));
    public static final ConfiguredFeature<?, ?> SAKURA = registryTree(SAKURA_KEY, createSimpleBlobTree(ModBlocks.SAKURA_LOG.get(), ModBlocks.SAKURA_LEAVES.get()).ignoreVines());

    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_SAKURA_KEY = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(WinterNight.MOD_ID, "fancy_sakura"));
    public static final ConfiguredFeature<?, ?> FANCY_SAKURA = registryTree(FANCY_SAKURA_KEY, createFancyTree(ModBlocks.SAKURA_LOG.get(), ModBlocks.SAKURA_LEAVES.get()));

    private static ConfiguredFeature<?, ?> registryTree(ResourceKey<ConfiguredFeature<?, ?>> key, TreeConfiguration.TreeConfigurationBuilder tree) {
        ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>> feature = new ConfiguredFeature<>(Feature.TREE, tree.build());
        ENTRY.add(new Pair<>(key, feature));
        return feature;
    }

    private static TreeConfiguration.TreeConfigurationBuilder createSimpleBlobTree(Block log, Block leaves) {
        return createStraightBlobTree(log, leaves, 4, 2, 0, 2);
    }

    private static TreeConfiguration.TreeConfigurationBuilder createStraightBlobTree(Block log, Block leaves,
                                                                                     int baseHeight, int heightRandA, int heightRandB, int leaves_radius) {
        return new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(log),
                new StraightTrunkPlacer(baseHeight, heightRandA, heightRandB), BlockStateProvider.simple(leaves),
                new BlobFoliagePlacer(ConstantInt.of(leaves_radius), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1));
    }

    private static TreeConfiguration.TreeConfigurationBuilder createFancyTree(Block log, Block leaves) {
        return (new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(log),
                new FancyTrunkPlacer(3, 11, 0), BlockStateProvider.simple(leaves),
                new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)))).ignoreVines();
    }
}
