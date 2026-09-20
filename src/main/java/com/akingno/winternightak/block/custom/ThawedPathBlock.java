package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** 土径被覆盖后仍回到解冻土壤；露天或失去供热时随机刻冻回冻结土壤。 */
public final class ThawedPathBlock extends DirtPathBlock {
    public ThawedPathBlock(Properties properties) { super(properties); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().canSurvive(context.getLevel(),context.getClickedPos())
                ? defaultBlockState() : ModBlocks.THAWED_SOIL.get().defaultBlockState();
    }
    @Override public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level,pos)) SoilHeat.replace(level,pos,ModBlocks.THAWED_SOIL.get());
    }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SoilHeat.freezeIfNeeded(level,pos);
    }
}
