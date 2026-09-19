package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** 硬冰用普通方块实现，避免原版冰融化/破坏变水；只允许冰镐挖掘，爆炸掉落另由掉落表控制。 */
public class HardIceBlock extends Block {
    public HardIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        // 同类硬冰之间不画内部面，降低20格厚透明冰盖的渲染开销。
        return adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!player.getMainHandItem().is(ModItems.ICE_PICK.get())) {
            // 挖掘进度为0使其他工具完全无法挖开；这与爆炸破坏是两套逻辑。
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return player.getMainHandItem().is(ModItems.ICE_PICK.get());
    }
}
