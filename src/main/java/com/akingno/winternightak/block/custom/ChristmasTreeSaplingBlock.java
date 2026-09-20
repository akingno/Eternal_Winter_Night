package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.world.gen.ChristmasTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/** 原版随机刻、光照>=9、两阶段生长及骨粉概率；额外允许种在解冻土壤。 */
public final class ChristmasTreeSaplingBlock extends SaplingBlock {
    public ChristmasTreeSaplingBlock(Properties properties) {
        super(new AbstractTreeGrower() {
            @Override protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean flowers) {
                return ChristmasTreeFeature.CONFIGURED;
            }
        }, properties);
    }
    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.THAWED_SOIL.get()) || super.mayPlaceOn(state, level, pos);
    }
    @Override public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        // 最大叶冠半径3格，先检查已加载边界，避免原版grower的花朵搜索触发区块加载。
        if (level.isAreaLoaded(pos, 3)) super.advanceTree(level, pos, state, random);
    }
}
