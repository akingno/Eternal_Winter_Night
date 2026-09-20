package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

/** 通过Forge原版工具钩子支持锄地、铲路，耐久与音效由工具负责。 */
public final class ThawedSoilBlock extends Block {
    public ThawedSoilBlock(Properties properties) { super(properties); }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SoilHeat.freezeIfNeeded(level, pos);
    }
    @Override public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
        if (context.getClickedFace() != Direction.DOWN && context.getLevel().isEmptyBlock(context.getClickedPos().above())) {
            if (action == ToolActions.HOE_TILL) return ModBlocks.THAWED_FARMLAND.get().defaultBlockState();
            if (action == ToolActions.SHOVEL_FLATTEN) return ModBlocks.THAWED_PATH.get().defaultBlockState();
        }
        return super.getToolModifiedState(state, context, action, simulate);
    }
}
