package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.block.entity.ModBlockEntities;
import com.akingno.winternightak.block.entity.PolarTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/** 原版墙上火把负责朝向、支撑和粒子位置；有限燃料及交互与地面火把共用。 */
public final class PolarWallTorchBlock extends WallTorchBlock implements EntityBlock {
    public PolarWallTorchBlock(Properties properties) {
        super(properties, ParticleTypes.FLAME);
        registerDefaultState(defaultBlockState().setValue(PolarTorchBlock.LIT, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PolarTorchBlock.LIT);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PolarTorchBlockEntity(pos, state);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == ModBlockEntities.POLAR_TORCH.get()
                ? (world, pos, blockState, entity) -> PolarTorchBlockEntity.serverTick(world, pos, blockState, (PolarTorchBlockEntity) entity) : null;
    }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(PolarTorchBlock.LIT)) super.animateTick(state, level, pos, random);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ModBlocks.POLAR_TORCH.get().use(state, level, pos, player, hand, hit);
    }
}
