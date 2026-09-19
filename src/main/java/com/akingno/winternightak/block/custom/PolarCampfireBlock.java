package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PolarCampfireBlock extends CampfireBlock {
    public PolarCampfireBlock(Properties properties) {
        super(true, 1, properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(LIT, false);
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PolarCampfireBlockEntity(pos, state); }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return state.getValue(LIT)
                ? createTickerHelper(type, ModBlockEntities.CAMPFIRE.get(), CampfireBlockEntity::particleTick) : null;
        return createTickerHelper(type, ModBlockEntities.CAMPFIRE.get(), PolarCampfireBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PolarCampfireBlockEntity fire)) return InteractionResult.PASS;
        var held = player.getItemInHand(hand);
        int fuel = PolarCampfireBlockEntity.fuelValue(held);
        if (fuel > 0) {
            if (!level.isClientSide) {
                if (fire.addFuel(fuel)) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    player.displayClientMessage(Component.translatable("message.winternightak.campfire_fuel", (fire.getFuelTicks() + 19) / 20), true);
                } else player.displayClientMessage(Component.translatable("message.winternightak.campfire_full"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                if (state.getValue(WATERLOGGED)) player.displayClientMessage(Component.translatable("message.winternightak.campfire_wet"), true);
                else if (fire.getFuelTicks() == 0) player.displayClientMessage(Component.translatable("message.winternightak.campfire_empty"), true);
                else if (!state.getValue(LIT)) {
                    level.setBlock(pos, state.setValue(LIT, true), 3);
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
                    held.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.FIRE_CHARGE)) return InteractionResult.sidedSuccess(level.isClientSide);
        if (held.isEmpty()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.winternightak.campfire_fuel", (fire.getFuelTicks() + 19) / 20), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    // Ignition must use flint and steel, not burning projectiles.
    @Override public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {}
}
