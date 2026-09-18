package com.akingno.winternightak.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class SnowBlockGravityMixin {
    @Inject(method = "onPlace", at = @At("HEAD"))
    private void winterNight$schedulePlacement(BlockState state, Level level, BlockPos pos,
                                               BlockState oldState, boolean moving, CallbackInfo ci) {
        if (state.is(Blocks.SNOW_BLOCK) && !level.isClientSide) level.scheduleTick(pos, Blocks.SNOW_BLOCK, 2);
    }

    @Inject(method = "updateShape", at = @At("HEAD"))
    private void winterNight$scheduleNeighbor(BlockState state, Direction direction, BlockState neighbor,
                                              LevelAccessor level, BlockPos pos, BlockPos neighborPos,
                                              CallbackInfoReturnable<BlockState> cir) {
        if (state.is(Blocks.SNOW_BLOCK)) level.scheduleTick(pos, Blocks.SNOW_BLOCK, 2);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void winterNight$fall(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state.is(Blocks.SNOW_BLOCK) && pos.getY() >= level.getMinBuildHeight()
                && FallingBlock.isFree(level.getBlockState(pos.below())))
            FallingBlockEntity.fall(level, pos, state);
    }
}
