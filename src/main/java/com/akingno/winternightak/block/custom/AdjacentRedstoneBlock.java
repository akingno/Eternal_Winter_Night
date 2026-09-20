package com.akingno.winternightak.block.custom;

import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** 仿原版红石灯的邻居更新；只认六面相邻红石块，不读取红石信号。 */
public class AdjacentRedstoneBlock extends Block {
    public AdjacentRedstoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, false));
    }
    public static boolean powered(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (level.hasChunkAt(neighbor) && level.getBlockState(neighbor).is(Blocks.REDSTONE_BLOCK)) return true;
        }
        return false;
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.LIT, powered(context.getLevel(), context.getClickedPos()));
    }
    @Override public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        if (!level.isClientSide) {
            boolean on = powered(level, pos);
            if (state.getValue(BlockStateProperties.LIT) != on)
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, on), 3);
        }
    }
}
