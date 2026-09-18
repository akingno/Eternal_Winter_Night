package com.akingno.winternightak.block.custom;


import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class FutonBlock extends BedBlock {
    public FutonBlock(DyeColor p_i48442_1_, Properties p_i48442_2_) {
        super(p_i48442_1_, p_i48442_2_);
    }

    private static final VoxelShape FUTON_SHAPE = Block.box(0, 0, 0, 16, 2, 16);


    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return FUTON_SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
