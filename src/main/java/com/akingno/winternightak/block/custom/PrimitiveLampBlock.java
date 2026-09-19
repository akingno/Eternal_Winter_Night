package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.entity.ModBlockEntities;
import com.akingno.winternightak.block.entity.PrimitiveLampBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;
import javax.annotation.Nullable;

public class PrimitiveLampBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(5, 0, 5, 11, 6, 11);

    public PrimitiveLampBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT, WATERLOGGED); }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if (!SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluid)) return false;
        if (!level.isClientSide()) level.setBlock(pos, level.getBlockState(pos).setValue(LIT, false), 3);
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PrimitiveLampBlockEntity lamp)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        int fuel = PrimitiveLampBlockEntity.fuelValue(held);
        if (fuel > 0) {
            if (!level.isClientSide) {
                if (lamp.addFuel(fuel)) {
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    player.displayClientMessage(Component.translatable("message.winternightak.lamp_fuel", (lamp.getFuelTicks() + 19) / 20), true);
                } else player.displayClientMessage(Component.translatable("message.winternightak.lamp_full"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                if (state.getValue(WATERLOGGED)) player.displayClientMessage(Component.translatable("message.winternightak.lamp_wet"), true);
                else if (lamp.getFuelTicks() == 0) player.displayClientMessage(Component.translatable("message.winternightak.lamp_empty"), true);
                else if (!state.getValue(LIT)) {
                    level.setBlock(pos, state.setValue(LIT, true), 3);
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
                    held.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty()) {
            if (!level.isClientSide) {
                if (player.isShiftKeyDown() && state.getValue(LIT)) {
                    level.setBlock(pos, state.setValue(LIT, false), 3);
                    level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
                }
                player.displayClientMessage(Component.translatable("message.winternightak.lamp_fuel", (lamp.getFuelTicks() + 19) / 20), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            level.addParticle(ParticleTypes.SMALL_FLAME, pos.getX() + 0.5, pos.getY() + 0.40, pos.getZ() + 0.5, 0, 0, 0);
            if (random.nextInt(4) == 0) level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.45, pos.getZ() + 0.5, 0, 0.01, 0);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PrimitiveLampBlockEntity(pos, state); }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PRIMITIVE_LAMP.get(), PrimitiveLampBlockEntity::serverTick);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.winternightak.primitive_lamp"));
    }
}
