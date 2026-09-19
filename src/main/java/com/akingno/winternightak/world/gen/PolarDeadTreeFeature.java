package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;

/** 雪原枯木：云杉原木短树干与短枝，不生成树叶、泥土或树苗。 */
public final class PolarDeadTreeFeature extends Feature<NoneFeatureConfiguration> {
    // 平均8个区块尝试一次；调低更密集，调高更稀疏。落点不合适会跳过。
    public static final int RARITY = 8;
    // 树干5～7格，接近普通白桦高度；调高更高，调低更矮。
    public static final int MIN_HEIGHT = 5;
    public static final int HEIGHT_VARIATION = 3;
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FEATURE =
            PolarSnowFeature.FEATURES.register("dead_spruce", PolarDeadTreeFeature::new);
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED = PolarWorldgen.key(Registries.CONFIGURED_FEATURE, "dead_spruce");
    public static final ResourceKey<PlacedFeature> PLACED = PolarWorldgen.key(Registries.PLACED_FEATURE, "dead_spruce");

    public PolarDeadTreeFeature() { super(NoneFeatureConfiguration.CODEC); }
    // 在Feature注册事件前触发静态字段初始化。
    public static void register() {}
    public static void configured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CONFIGURED, new ConfiguredFeature<>(FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
    }
    public static void placed(BootstapContext<PlacedFeature> context) {
        context.register(PLACED, new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CONFIGURED),
                List.of(RarityFilter.onAverageOnceEvery(RARITY), InSquarePlacement.spread(),
                        HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG), BiomeFilter.biome())));
    }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        BlockPos base = context.origin();
        if (level.getBlockState(base.below()).is(Blocks.SNOW)) base = base.below();
        if (!level.getBiome(base).is(PolarWorldgen.ICE_PLAIN)
                || !level.getBlockState(base.below()).is(ModBlocks.FROZEN_SOIL.get())) return false;
        // 不在雪村占地内生成野外枯木，避免刚移除装饰树又长出新树。
        var village = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(PolarVillages.VILLAGE).value();
        var manager = level.getLevel().structureManager();
        // 使用当前生成区域的缓存，不能在异步生成中要求主世界加载区块。
        if (level instanceof net.minecraft.server.level.WorldGenRegion region) manager = manager.forWorldGenRegion(region);
        for (var start : manager.startsForStructure(net.minecraft.core.SectionPos.of(base), village)) {
            var bounds = start.getBoundingBox();
            // 按水平占地判断，并为1格长的短枝留出余量，不受地面高低影响。
            if (base.getX() >= bounds.minX() - 1 && base.getX() <= bounds.maxX() + 1
                    && base.getZ() >= bounds.minZ() - 1 && base.getZ() <= bounds.maxZ() + 1) return false;
        }
        int height = MIN_HEIGHT + context.random().nextInt(HEIGHT_VARIATION);
        // 周围1格是短枝所需空间；先检查后放置，避免截断树干或覆盖房屋。
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-1, 0, -1), base.offset(1, height - 1, 1))) {
            var state = level.getBlockState(pos);
            if (!state.isAir() && !state.is(Blocks.SNOW)) return false;
        }
        for (int y = 0; y < height; y++) level.setBlock(base.above(y), Blocks.SPRUCE_LOG.defaultBlockState(), 2);
        Direction branch = Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
        // 两根相反方向的短枝，分别位于树顶下2、3格；改小偏移更靠近树顶。
        level.setBlock(base.above(height - 2).relative(branch), Blocks.SPRUCE_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, branch.getAxis()), 2);
        level.setBlock(base.above(height - 3).relative(branch.getOpposite()), Blocks.SPRUCE_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, branch.getAxis()), 2);
        // 更新标记2仅通知客户端，避免世界生成时触发不必要的邻居连锁更新。
        return true;
    }
}
