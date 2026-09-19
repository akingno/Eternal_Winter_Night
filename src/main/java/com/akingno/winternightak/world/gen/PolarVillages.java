package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.WinterNight;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 注册独立的雪村结构，避免改动原版世界的村庄配置。 */
public final class PolarVillages {
    public static final DeferredRegister<net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType<?>> ELEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, WinterNight.MOD_ID);
    public static final RegistryObject<net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType<PolarVillageElement>> ELEMENT =
            ELEMENT_TYPES.register("village_template", () -> () -> PolarVillageElement.CODEC);
    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, WinterNight.MOD_ID);
    public static final RegistryObject<StructureType<PolarVillageStructure>> TYPE = TYPES.register("snowy_village", () -> () -> PolarVillageStructure.CODEC);
    public static final ResourceKey<Structure> VILLAGE = ResourceKey.create(Registries.STRUCTURE, id("snowy_village"));
    public static final ResourceKey<StructureSet> SET = ResourceKey.create(Registries.STRUCTURE_SET, id("snowy_villages"));
    public static final ResourceKey<StructureTemplatePool> START_POOL = ResourceKey.create(Registries.TEMPLATE_POOL, id("village/snowy/town_centers"));

    private static ResourceLocation id(String path) { return new ResourceLocation(WinterNight.MOD_ID, path); }

    public static void pools(BootstapContext<StructureTemplatePool> context) {
        var empty = context.lookup(Registries.TEMPLATE_POOL).getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation("minecraft:empty")));
        // 只取正常雪村的三个广场，完全不加入僵尸版本；连接转向独立极地池，模板仍复用原版。
        // 权重2:1:3沿用原版正常广场比例，调高某项只增加该外观的出现比例，不增加村庄数量。
        context.register(START_POOL, new StructureTemplatePool(empty, List.of(
                Pair.of(PolarVillageElement.template("minecraft:village/snowy/town_centers/snowy_meeting_point_1"), 2),
                Pair.of(PolarVillageElement.template("minecraft:village/snowy/town_centers/snowy_meeting_point_2"), 1),
                Pair.of(PolarVillageElement.template("minecraft:village/snowy/town_centers/snowy_meeting_point_3"), 3)
        ), StructureTemplatePool.Projection.RIGID));
    }

    public static void structures(BootstapContext<Structure> context) {
        var plain = context.lookup(Registries.BIOME).getOrThrow(PolarWorldgen.ICE_PLAIN);
        // 不添加特殊刷怪表；现有PolarSpawning继续禁止猫、铁傀儡和怪物，允许村民。
        // NONE不做大范围地形抬升，防止把岛外冰盖拉成村庄地基。
        context.register(VILLAGE, new PolarVillageStructure(new Structure.StructureSettings(
                HolderSet.direct(plain), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
    }

    public static void sets(BootstapContext<StructureSet> context) {
        var village = context.lookup(Registries.STRUCTURE).getOrThrow(VILLAGE);
        // 唯一结构权重1表示只选本雪村，并非生成概率；概率由FREQUENCY单独控制。
        // ZERO不偏移定位坐标；DEFAULT按种子按FREQUENCY筛选候选；empty不额外排斥其他结构。
        context.register(SET, new StructureSet(List.of(StructureSet.entry(village, 1)),
                new RandomSpreadStructurePlacement(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT,
                        PolarVillageSettings.FREQUENCY, PolarVillageSettings.SALT, Optional.empty(),
                        PolarVillageSettings.SPACING, PolarVillageSettings.SEPARATION, RandomSpreadType.LINEAR)));
    }

    private PolarVillages() {}
}
