package com.akingno.winternightak.block.custom;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;



public class BookBlock extends MyBaseHorizonBlock{
    public BookBlock(Properties properties) {
        super(properties);
    }


    private static final VoxelShape SHAPE_N = Block.box(5, 0, 3, 11, 1, 12);
    private static final VoxelShape SHAPE_E =Block.box(3.5, 0, 4.5, 12.5, 1, 10.5);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)){
            case NORTH:
                return SHAPE_N;
            case SOUTH:
                return SHAPE_N;
            case EAST:
                return SHAPE_E;
            case WEST:
                return SHAPE_E;
            default:
                return SHAPE_N;
        }
    }
}
