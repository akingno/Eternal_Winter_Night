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
import java.util.ArrayList;
import com.akingno.winternightak.block.entity.CampfireSettings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendStatic;

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

    /** 复用原版拼接接口，仅把雪村池引用转向模组自己的池；不改原版NBT或全局村庄。 */
    @Override public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
            StructureTemplateManager manager, net.minecraft.core.BlockPos pos, Rotation rotation,
            net.minecraft.util.RandomSource random) {
        var result = new ArrayList<StructureTemplate.StructureBlockInfo>();
        for (var info : super.getShuffledJigsawBlocks(manager, pos, rotation, random)) {
            var tag = info.nbt() == null ? null : info.nbt().copy();
            if (tag != null && tag.getString("pool").startsWith("minecraft:village/snowy/"))
                tag.putString("pool", tag.getString("pool").replace("minecraft:", "winternightak:"));
            result.add(new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), tag));
        }
        return result;
    }

    public static java.util.function.Function<StructureTemplatePool.Projection, net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement> template(String name) {
        return projection -> new PolarVillageElement(Either.left(new ResourceLocation(name)),
                Holder.direct(new StructureProcessorList(List.of())), projection);
    }

    @Override protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox bounds, boolean keepJigsaws) {
        var settings = super.getSettings(rotation, bounds, keepJigsaws);
        // 第0项表示先转换原模板的土径，再执行原版随机积雪等处理，防止土径先被改成别的材料。
        settings.getProcessors().add(0, GRAVEL_PATH);
        // 拼接连接点的final_state也可能是土径，末尾再处理一次，避免路口残留土径。
        settings.addProcessor(GRAVEL_PATH);
        // 最后替换原模板或前面处理器留下的草方块/泥土；耕地与作物由资源规则处理。
        // 在放置阶段读取模组方块，避免静态初始化先于方块注册完成。
        settings.addProcessor(new RuleProcessor(List.of(
                new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), AlwaysTrueTest.INSTANCE,
                        ModBlocks.FROZEN_SOIL.get().defaultBlockState()),
                new ProcessorRule(new BlockMatchTest(Blocks.DIRT), AlwaysTrueTest.INSTANCE,
                        ModBlocks.FROZEN_SOIL.get().defaultBlockState()))));
        settings.addProcessor(villageResources());
        return settings;
    }

    /** 沿用原版规则处理器，在单次模板放置中替换资源，不扫描世界或加载额外区块。 */
    private static RuleProcessor villageResources() {
        var rules = new ArrayList<ProcessorRule>();
        // 仅初始化新放置模板；之后燃料按正常规则消耗，不会自动补满。
        var torchFuel = new CompoundTag();
        torchFuel.putString("id", "winternightak:polar_torch");
        torchFuel.putInt("FuelTicks", com.akingno.winternightak.block.entity.TorchSettings.FUEL_TICKS);
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.TORCH), AlwaysTrueTest.INSTANCE,
                PosAlwaysTrueTest.INSTANCE, ModBlocks.POLAR_TORCH.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true),
                new AppendStatic(torchFuel)));
        for (var facing : Direction.Plane.HORIZONTAL) {
            // 保留墙上火把朝向；最终随模板旋转，不把墙上火把强行改成地面火把。
            rules.add(new ProcessorRule(new BlockStateMatchTest(Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, facing)),
                    AlwaysTrueTest.INSTANCE, PosAlwaysTrueTest.INSTANCE,
                    ModBlocks.POLAR_WALL_TORCH.get().defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, facing)
                            .setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true),
                    new AppendStatic(torchFuel)));
        }
        // 容量直接读取营火参数；修改容量后新村庄自动使用新值，不重复写燃料常量。
        var fuel = new CompoundTag();
        fuel.putInt("FuelTicks", CampfireSettings.MAX_FUEL_TICKS);
        fuel.putString("id", "winternightak:campfire");
        // 不继承原熔炉物品，防止熔炉库存误变成营火食物。
        fuel.put("Items", new ListTag());
        for (var direction : Direction.Plane.HORIZONTAL) {
            for (boolean lit : new boolean[]{false, true}) {
                // 两种点燃状态都替换，保留模板中的朝向，后续旋转由原版放置流程完成。
                rules.add(new ProcessorRule(new BlockStateMatchTest(Blocks.FURNACE.defaultBlockState()
                        .setValue(FurnaceBlock.FACING, direction).setValue(FurnaceBlock.LIT, lit)),
                        AlwaysTrueTest.INSTANCE, PosAlwaysTrueTest.INSTANCE,
                        ModBlocks.CAMPFIRE.get().defaultBlockState()
                                .setValue(CampfireBlock.FACING, direction).setValue(CampfireBlock.LIT, true),
                        new AppendStatic(fuel)));
            }
        }
        // 替换为空气而不是破坏方块，因此不会产生箱子、战利品或作物掉落。
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.CHEST), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()));
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.TRAPPED_CHEST), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()));
        // 仅雪村移除渔夫工作站；出生点营地用独立放置流程，木桶不受影响。
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.BARREL), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()));
        // 移除农民工作站；已有村民不会被强制删除，但新村庄不再提供堆肥桶转职点。
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.COMPOSTER), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()));
        // 制箭台不提供当前阶段需要的职业与资源，换成普通工作台供玩家使用。
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.FLETCHING_TABLE), AlwaysTrueTest.INSTANCE,
                Blocks.CRAFTING_TABLE.defaultBlockState()));
        // 木屋等模板也有少量装饰雪块，改为浮冰防止落雪块损坏建筑；薄雪保留。
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.SNOW_BLOCK), AlwaysTrueTest.INSTANCE, Blocks.PACKED_ICE.defaultBlockState()));
        rules.add(new ProcessorRule(new BlockMatchTest(Blocks.FARMLAND), AlwaysTrueTest.INSTANCE,
                ModBlocks.FROZEN_SOIL.get().defaultBlockState()));
        rules.add(new ProcessorRule(new TagMatchTest(BlockTags.CROPS), AlwaysTrueTest.INSTANCE, Blocks.AIR.defaultBlockState()));
        return new RuleProcessor(rules);
    }

    @Override public StructurePoolElementType<?> getType() { return PolarVillages.ELEMENT.get(); }
}
