package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

/** 原版噪声生成器的极地配置：先决定海洋/冰盖/岛屿形状，再替换表层材料；不添加洞穴。 */
public final class PolarWorldgen {
    public static final ResourceKey<Biome> ICE_CAP = key(Registries.BIOME, "polar_ice_cap");
    public static final ResourceKey<Biome> ICE_PLAIN = key(Registries.BIOME, "ice_plain");
    public static final ResourceKey<Biome> OCEAN = key(Registries.BIOME, "polar_ocean");
    public static final ResourceKey<NormalNoise.NoiseParameters> LAND_DISTRIBUTION = key(Registries.NOISE, "polar_distribution");
    public static final ResourceKey<NormalNoise.NoiseParameters> ISLAND_RELIEF = key(Registries.NOISE, "island_relief");
    public static final ResourceKey<NoiseGeneratorSettings> SETTINGS = key(Registries.NOISE_SETTINGS, "polar");
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = key(Registries.DIMENSION_TYPE, "polar");
    public static final ResourceKey<WorldPreset> PRESET = key(Registries.WORLD_PRESET, "winter_night");

    // 建造下限-64、总高度384，沿用主世界尺度；不要只改一项，否则地形和维度范围会错位。
    public static final int MIN_Y = -64;
    public static final int HEIGHT = 384;
    // 原版海平面参数63表示水面最高方块通常为Y=62；调高会抬升海水，须同步调整冰层公式。
    public static final int SEA_LEVEL = 63;
    // 目标约27%海洋/65%冰盖/8%冰原，不是逐区块精确抽签，种子不同实际面积会有波动。
    // 海洋阈值调高→海洋更多；岛屿阈值调高→冰原更少；两者之间为冰盖。
    public static final float OCEAN_THRESHOLD = -0.18F;
    public static final float ISLAND_THRESHOLD = 0.42F;

    public static <T> ResourceKey<T> key(ResourceKey<? extends net.minecraft.core.Registry<T>> registry, String path) {
        return ResourceKey.create(registry, ResourceLocation.fromNamespaceAndPath(WinterNight.MOD_ID, path));
    }

    public static void bootstrapBiomes(BootstapContext<Biome> context) {
        context.register(ICE_CAP, biome(context, -0.5F, true, 0xA3C9E0, false));
        context.register(ICE_PLAIN, biome(context, -0.4F, true, 0xB9D8E8, true));
        // Vanilla freezing is controlled separately from Cold Sweat's environmental temperatures.
        context.register(OCEAN, biome(context, 0.16F, false, 0x648CAA, false));
    }

    private static Biome biome(BootstapContext<Biome> context, float temperature, boolean precipitation, int fog, boolean icePlain) {
        // 0.05是新区块初始动物生成概率，调低更少；不直接等同于日后自然刷新的每tick概率。
        var spawns = new MobSpawnSettings.Builder().creatureGenerationProbability(0.05F);
        var generation = new BiomeGenerationSettings.Builder(context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER));
        if (precipitation) {
            // SpawnerData依次为种类、相对权重、每群最少、每群最多；权重不是百分比。
            // 兔子12、狐狸1：降低某种的权重会降低其相对占比；降低群数量会减少一次出现的只数。
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 12, 1, 3));
            spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 1, 1, 1));
            if (temperature > -0.5F)
                spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 1, 1, 2));
            // 仅雪原生成枯木，明确按群系用途区分，不依赖温度数值判断。
            if (icePlain) generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PolarDeadTreeFeature.PLACED);
            generation.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, PolarSnowFeature.PLACED);
        } else {
            // 原版冰山阶段；密度在placed_feature中为16/200区块一次尝试，调低更密集。
            generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, key(Registries.PLACED_FEATURE, "iceberg_packed"));
            generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, key(Registries.PLACED_FEATURE, "iceberg_blue"));
            spawns.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 10, 1, 3));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 10, 3, 5));
            spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, 10, 3, 5));
        }
        return new Biome.BiomeBuilder().hasPrecipitation(precipitation).temperature(temperature).downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x244E65).waterFogColor(0x102B3D)
                        .fogColor(fog).skyColor(0x7186A1).build())
                .mobSpawnSettings(spawns.build()).generationSettings(generation.build()).build();
    }

    public static void bootstrapNoises(BootstapContext<NormalNoise.NoiseParameters> context) {
        // 首八度-10控制大片地形尺度，越负通常越连片；两个1.0为噪声分量权重，不是群系比例。
        context.register(LAND_DISTRIBUTION, new NormalNoise.NoiseParameters(-10, 1.0, 1.0));
        // -5的尺度比群系噪声细，用来给岛内增加起伏；权重1.0为基准强度。
        context.register(ISLAND_RELIEF, new NormalNoise.NoiseParameters(-5, 1.0));
    }

    public static void bootstrapSettings(BootstapContext<NoiseGeneratorSettings> context) {
        var noises = context.lookup(Registries.NOISE);
        DensityFunction continent = DensityFunctions.cache2d(DensityFunctions.noise(noises.getOrThrow(LAND_DISTRIBUTION), 1.0, 0.0));
        DensityFunction relief = DensityFunctions.cache2d(DensityFunctions.noise(noises.getOrThrow(ISLAND_RELIEF), 1.0, 0.0));
        DensityFunction zero = DensityFunctions.zero();
        // 密度>0生成固体，<0为空气或海水。-53.5让海床顶格位于-54；-63..-54恰为10格沙砾。
        DensityFunction oceanFloor = below(-53.5);
        // 冰盖固定在Y=43..62，共20格；上界62.5抬高会加厚上部，下界42.5降低会加厚下部。
        DensityFunction iceSheet = DensityFunctions.min(below(62.5),
                DensityFunctions.yClampedGradient(MIN_Y, 320, MIN_Y - 42.5, 320 - 42.5));
        // 32控制离岸向内的升高速度，保持岸线不变；基础抬升上限由10提高为15，岛内高处约增高5格。
        // 最终还乘以下方起伏噪声，因此不是所有最高点都精确增加5格；调高15会放宽内陆高度上限。
        DensityFunction inland = DensityFunctions.mul(DensityFunctions.constant(32),
                DensityFunctions.add(continent, DensityFunctions.constant(-ISLAND_THRESHOLD))).clamp(0, 15);
        // 岸线基准63.5接近海面；0.2是岛内随机起伏比例，调高更崎岖，降低更平坦。
        DensityFunction island = DensityFunctions.add(below(63.5), DensityFunctions.mul(inland,
                DensityFunctions.add(DensityFunctions.constant(1), DensityFunctions.mul(DensityFunctions.constant(0.2), relief))));
        DensityFunction terrain = DensityFunctions.rangeChoice(continent, ISLAND_THRESHOLD, 1000000,
                island, DensityFunctions.rangeChoice(continent, -1000000, OCEAN_THRESHOLD,
                        oceanFloor, DensityFunctions.max(oceanFloor, iceSheet)));
        NoiseRouter router = new NoiseRouter(zero, zero, zero, zero, zero, zero, continent, zero, zero, zero,
                terrain, terrain, zero, zero, zero);

        // 岛屿表面3格冻结土壤，以下永久冻土；stoneDepthCheck的3调高会增厚可挖土层。
        SurfaceRules.RuleSource islandSurface = SurfaceRules.sequence(
                // yBlockCheck(9)表示Y>=9，取反覆盖Y<=8；调高9会抬高深板岩顶面。底部基岩规则优先。
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(9), 0)),
                        SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(3, false, CaveSurface.FLOOR),
                        SurfaceRules.state(ModBlocks.FROZEN_SOIL.get().defaultBlockState())),
                SurfaceRules.state(ModBlocks.PERMAFROST.get().defaultBlockState()));
        SurfaceRules.RuleSource surfaces = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(-63), 0)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())),
                // Use the same noise and thresholds as density, without biome-border zoom artifacts.
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(LAND_DISTRIBUTION, ISLAND_THRESHOLD), islandSurface),
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(-53), 0)),
                        SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())),
                SurfaceRules.state(ModBlocks.HARD_ICE.get().defaultBlockState()));
        context.register(SETTINGS, new NoiseGeneratorSettings(NoiseSettings.create(MIN_Y, HEIGHT, 1, 2),
                ModBlocks.PERMAFROST.get().defaultBlockState(), Blocks.WATER.defaultBlockState(), router, surfaces,
                List.of(parameters(OCEAN_THRESHOLD + 0.01F, ISLAND_THRESHOLD - 0.01F)), SEA_LEVEL, false, false, false, false));
    }

    private static DensityFunction below(double top) {
        return DensityFunctions.yClampedGradient(MIN_Y, 320, top - MIN_Y, top - 320);
    }

    public static Climate.ParameterPoint parameters(float min, float max) {
        var any = Climate.Parameter.span(-2.0F, 2.0F);
        return Climate.parameters(any, any, Climate.Parameter.span(min, max), any, any, any, 0.0F);
    }

    public static void bootstrapDimensionTypes(BootstapContext<DimensionType> context) {
        // 固定视觉时间18000为午夜，不冻结gameTime，因此暴雪计时与燃料仍正常推进。
        // 下列布尔项沿用当前维度天空、天花板、床等设置，不能当作生成概率调整。
        context.register(DIMENSION_TYPE, new DimensionType(OptionalLong.of(18000), true, false, false, true,
                1.0, true, false, MIN_Y, HEIGHT, HEIGHT, BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, 0.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapPresets(BootstapContext<WorldPreset> context) {
        var biomes = context.lookup(Registries.BIOME);
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> distribution = List.of(
                Pair.of(parameters(-2.0F, OCEAN_THRESHOLD), biomes.getOrThrow(OCEAN)),
                Pair.of(parameters(OCEAN_THRESHOLD, ISLAND_THRESHOLD), biomes.getOrThrow(ICE_CAP)),
                Pair.of(parameters(ISLAND_THRESHOLD, 2.0F), biomes.getOrThrow(ICE_PLAIN)));
        var source = MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(distribution));
        var generator = new NoiseBasedChunkGenerator(source, context.lookup(Registries.NOISE_SETTINGS).getOrThrow(SETTINGS));
        var overworld = new LevelStem(context.lookup(Registries.DIMENSION_TYPE).getOrThrow(DIMENSION_TYPE), generator);
        // This preset deliberately contains only its polar overworld.
        context.register(PRESET, new WorldPreset(Map.of(LevelStem.OVERWORLD, overworld)));
    }

    private PolarWorldgen() {}
}
