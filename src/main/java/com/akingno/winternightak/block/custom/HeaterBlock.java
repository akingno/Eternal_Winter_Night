package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.block.entity.HeaterSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** 无燃料、无库存，使用原版计划刻；只在加载区块内定期检查土地。 */
public final class HeaterBlock extends AdjacentRedstoneBlock {
    public HeaterBlock(Properties properties) { super(properties); }
    @Override public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        if (!level.isClientSide) {
            // 坐标散列将首轮检查分散到20个tick，避免同时放置的设备集中扫描。
            level.scheduleTick(pos, this, 1 + Math.floorMod(pos.hashCode(), HeaterSettings.CHECK_TICKS));
        }
    }
    @Override public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean on = powered(level, pos);
        if (state.getValue(BlockStateProperties.LIT) != on)
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, on), 3);
        if (on) for (BlockPos offset : SoilHeat.OFFSETS) {
            BlockPos target = pos.offset(offset);
            if (level.hasChunkAt(target) && level.getBlockState(target).is(ModBlocks.FROZEN_SOIL.get())
                    && !level.canSeeSky(target.above()))
                level.setBlockAndUpdate(target, ModBlocks.THAWED_SOIL.get().defaultBlockState());
        }
        level.scheduleTick(pos, this, HeaterSettings.CHECK_TICKS);
    }
}
