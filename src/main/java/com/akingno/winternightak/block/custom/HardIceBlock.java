package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Temporary single-stage mining; deliberately does not inherit vanilla ice behavior. */
public class HardIceBlock extends Block {
    public HardIceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        // A twenty-block-thick transparent sheet should only render its exposed faces.
        return adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!player.getMainHandItem().is(ModItems.ICE_PICK.get())) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return player.getMainHandItem().is(ModItems.ICE_PICK.get());
    }
}
