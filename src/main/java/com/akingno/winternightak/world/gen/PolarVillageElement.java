package com.akingno.winternightak.world.gen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import java.util.List;

/** 极地村庄专用模板元素：放置时把土径换成沙砾，草方块和泥土换成冻结土壤。 */
public final class PolarVillageElement extends LegacySinglePoolElement {
    public static final Codec<PolarVillageElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PolarVillageElement.<PolarVillageElement>templateCodec(),
            PolarVillageElement.<PolarVillageElement>processorsCodec(),
            PolarVillageElement.<PolarVillageElement>projectionCodec()).apply(instance, PolarVillageElement::new));
    private static final RuleProcessor GRAVEL_PATH = new RuleProcessor(List.of(new ProcessorRule(
            new BlockMatchTest(Blocks.DIRT_PATH), AlwaysTrueTest.INSTANCE, Blocks.GRAVEL.defaultBlockState())));

    public PolarVillageElement(Either<ResourceLocation, StructureTemplate> template,
                               Holder<StructureProcessorList> processors, StructureTemplatePool.Projection projection) {
        super(template, processors, projection);
    }

    @Override protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox bounds, boolean keepJigsaws) {
        var settings = super.getSettings(rotation, bounds, keepJigsaws);
        // 第0项表示先转换原模板的土径，再执行原版随机积雪等处理，防止土径先被改成别的材料。
        settings.getProcessors().add(0, GRAVEL_PATH);
        // 拼接连接点的final_state也可能是土径，末尾再处理一次，避免路口残留土径。
        settings.addProcessor(GRAVEL_PATH);
        // 最后替换原模板或前面处理器留下的草方块/泥土，农田与其他建筑材料保留。
        // 在放置阶段读取模组方块，避免静态初始化先于方块注册完成。
        settings.addProcessor(new RuleProcessor(List.of(
                new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), AlwaysTrueTest.INSTANCE,
                        ModBlocks.FROZEN_SOIL.get().defaultBlockState()),
                new ProcessorRule(new BlockMatchTest(Blocks.DIRT), AlwaysTrueTest.INSTANCE,
                        ModBlocks.FROZEN_SOIL.get().defaultBlockState()))));
        return settings;
    }

    @Override public StructurePoolElementType<?> getType() { return PolarVillages.ELEMENT.get(); }
}
