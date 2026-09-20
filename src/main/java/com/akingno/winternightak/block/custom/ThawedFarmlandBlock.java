package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** 继承原版水分与作物支持；把干燥、踩踏和覆盖后的泥土回退纠正为解冻土壤。 */
public final class ThawedFarmlandBlock extends FarmBlock {
    public ThawedFarmlandBlock(Properties properties) { super(properties); }
    // 原版支持判断只认 FARMLAND 的注册实例；自定义耕地需要明确支持作物。
    @Override public boolean canSustainPlant(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
            net.minecraft.core.Direction facing, net.minecraftforge.common.IPlantable plant) {
        return facing == net.minecraft.core.Direction.UP
                && net.minecraftforge.common.PlantType.CROP.equals(plant.getPlantType(level, pos.above()));
    }
    @Override public boolean isFertile(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return state.getValue(MOISTURE) > 0;
    }
    private void restoreSoil(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockState(pos).is(Blocks.DIRT)) SoilHeat.replace(level, pos, ModBlocks.THAWED_SOIL.get());
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = super.getStateForPlacement(context);
        return state != null && state.is(Blocks.DIRT) ? ModBlocks.THAWED_SOIL.get().defaultBlockState() : state;
    }
    @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (SoilHeat.freezeIfNeeded(level,pos)) return;
        super.randomTick(state,level,pos,random);
        restoreSoil(level,pos);
    }
    @Override public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state,level,pos,random);
        restoreSoil(level,pos);
    }
    @Override public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
        super.fallOn(level,state,pos,entity,distance);
        restoreSoil(level,pos);
    }
}
