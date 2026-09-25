package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.block.entity.HeaterSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** 无燃料、无库存，使用原版计划刻；只在加载区块内定期检查土地。 */
public final class HeaterBlock extends AdjacentRedstoneBlock {
    // 与原版熔炉相同，仅允许东、南、西、北，玩家俯仰视角不影响朝向。
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public HeaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // 保留相邻红石块控制的 LIT 状态。
        builder.add(FACING);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 继承放置时的供热状态，正面朝向放置者；从上方放置也只读取水平方向。
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        // 结构模板旋转、镜像时同步调整正面，沿用原版熔炉的处理。
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
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
