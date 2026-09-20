package com.akingno.winternightak.world.gen;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** 小片浅层粘土：只替换冻结土壤下紧接的永久冻土，生成阶段执行。 */
public final class PolarClayFeature extends Feature<NoneFeatureConfiguration> {
    // 每区块3次候选，每次75%通过；调高会增加出现频率，候选重叠不会扩大搜索范围。
    private static final int ATTEMPTS = 3;
    private static final float CHANCE = 0.75F;
    // 单团最大3×3、厚1～2格；限制向下检查12格，防止生成到深层或遍历整根地柱。
    private static final int SEARCH_DEPTH = 12;
    public PolarClayFeature() { super(NoneFeatureConfiguration.CODEC); }
    public static void register() { PolarSnowFeature.FEATURES.register("shallow_clay", PolarClayFeature::new); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        var level = context.level();
        var random = context.random();
        boolean placed = false;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            if (random.nextFloat() >= CHANCE) continue;
            // 中心限制在本区块1..14，半径1，整个团块不跨区块。
            BlockPos center = context.origin().offset(1 + random.nextInt(14), 0, 1 + random.nextInt(14));
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                // 随机省去部分角落，形成小片不规则轮廓；中心及十字方向保留。
                if (Math.abs(x) + Math.abs(z) == 2 && random.nextBoolean()) continue;
                BlockPos top = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, center.offset(x, 0, z));
                if (!level.getBiome(top).is(PolarWorldgen.ICE_PLAIN)) continue;
                for (int depth = 1; depth <= SEARCH_DEPTH; depth++) {
                    BlockPos pos = top.below(depth);
                    if (!level.getBlockState(pos).is(ModBlocks.PERMAFROST.get())
                            || !level.getBlockState(pos.above()).is(ModBlocks.FROZEN_SOIL.get())) continue;
                    int thickness = 1 + random.nextInt(2);
                    for (int y = 0; y < thickness; y++) {
                        BlockPos target = pos.below(y);
                        if (level.getBlockState(target).is(ModBlocks.PERMAFROST.get())) {
                            level.setBlock(target, Blocks.CLAY.defaultBlockState(), 2);
                            placed = true;
                        }
                    }
                    break;
                }
            }
        }
        return placed;
    }
}
