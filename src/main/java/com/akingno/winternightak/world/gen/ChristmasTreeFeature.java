package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/** 树苗触发的固定树形；先检查全部空间，不扫描世界、不添加自然刷新。 */
public final class ChristmasTreeFeature extends Feature<NoneFeatureConfiguration> {
    // 9格总高，前8格中心为原木，最后1格南瓜灯；增加原木数量须重新核算16木换1铁的经济。
    public static final int HEIGHT = 9;
    public static final int LOG_COUNT = 8;
    // 各层叶冠半径：0表示该层仅中心轴；调大将需要更宽的空地。
    private static final int[] RADII = {0, 3, 2, 1, 2, 1, 0, 1};
    // 第3、5层每层最多3块玻璃；调高增加装饰及可回收玻璃数量。
    private static final int MAX_GLASS_PER_LAYER = 3;
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FEATURE =
            PolarSnowFeature.FEATURES.register("christmas_tree", ChristmasTreeFeature::new);
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED = PolarWorldgen.key(Registries.CONFIGURED_FEATURE, "christmas_tree");
    public ChristmasTreeFeature() { super(NoneFeatureConfiguration.CODEC); }
    public static void register() {}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        BlockPos base = context.origin();
        var soil = level.getBlockState(base.below());
        if (!soil.is(BlockTags.DIRT) && !soil.is(ModBlocks.THAWED_SOIL.get())) return false;
        var blocks = new LinkedHashMap<BlockPos, BlockState>();
        for (int y = 0; y < LOG_COUNT; y++) {
            int radius = RADII[y];
            var decoration = new ArrayList<BlockPos>();
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                // 圆形近似去掉方形四角；第8层仅保留十字。距离用于原版树叶消失计算。
                if (x == 0 && z == 0 || x*x + z*z > radius*radius + (radius > 1 ? 1 : 0)) continue;
                BlockPos pos = base.offset(x, y, z);
                blocks.put(pos, ModBlocks.CHRISTMAS_LEAVES.get().defaultBlockState()
                        .setValue(LeavesBlock.DISTANCE, Math.abs(x) + Math.abs(z)));
                if (Math.abs(x) + Math.abs(z) >= radius) decoration.add(pos);
            }
            if (y == 2 || y == 4) {
                int count = context.random().nextInt(MAX_GLASS_PER_LAYER + 1);
                for (int i = 0; i < count && !decoration.isEmpty(); i++) {
                    BlockPos pos = decoration.remove(context.random().nextInt(decoration.size()));
                    blocks.put(pos, (context.random().nextBoolean() ? Blocks.RED_STAINED_GLASS : Blocks.BLUE_STAINED_GLASS).defaultBlockState());
                }
            }
            blocks.put(base.above(y), Blocks.SPRUCE_LOG.defaultBlockState());
        }
        blocks.put(base.above(HEIGHT - 1), Blocks.JACK_O_LANTERN.defaultBlockState());
        for (BlockPos pos : blocks.keySet()) {
            if (level.isOutsideBuildHeight(pos) || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return false;
            var state = level.getBlockState(pos);
            // 仅替换空气、薄雪或触发的本棵树苗；碰到其他植物或建筑则完整保留树苗。
            if (!state.isAir() && !state.is(Blocks.SNOW)
                    && !(pos.equals(base) && state.is(ModBlocks.CHRISTMAS_TREE_SAPLING.get()))) return false;
        }
        blocks.forEach((pos, state) -> level.setBlock(pos, state, 3));
        return true;
    }
}
