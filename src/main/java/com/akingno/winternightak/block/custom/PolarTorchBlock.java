package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.entity.ModBlockEntities;
import com.akingno.winternightak.block.entity.PolarTorchBlockEntity;
import com.akingno.winternightak.block.entity.TorchSettings;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
import javax.annotation.Nullable;

/** Floor torch; stored item fuel does not tick and has no carried-temperature modifier. */
/** 地面火把：不添加手持计时或手持供暖；放置默认熄灭，有余料才允许打火石点燃。 */
public class PolarTorchBlock extends TorchBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public PolarTorchBlock(Properties properties) {
        super(properties, ParticleTypes.FLAME);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PolarTorchBlockEntity(pos, state); }

    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == ModBlockEntities.POLAR_TORCH.get()
                ? (world, pos, blockState, entity) -> PolarTorchBlockEntity.serverTick(world, pos, blockState, (PolarTorchBlockEntity) entity) : null;
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) super.animateTick(state, level, pos, random);
    }

    // 打火石点燃剩余燃料；0燃料不可再点燃，潜行空手只熄灭，不增加燃料。
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PolarTorchBlockEntity torch)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.FLINT_AND_STEEL)) {
            if (!level.isClientSide) {
                if (torch.getFuelTicks() <= 0) player.displayClientMessage(Component.translatable("message.winternightak.torch_spent"), true);
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
                if (player.isShiftKeyDown()) level.setBlock(pos, state.setValue(LIT, false), 3);
                player.displayClientMessage(Component.translatable("message.winternightak.torch_fuel", (torch.getFuelTicks() + 19) / 20), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    // 物品提示读取BlockEntityTag余料；(fuel+19)/20将不足1秒的余量也显示为1秒。
    @Override public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        var data = stack.getTagElement("BlockEntityTag");
        int fuel = data != null && data.contains("FuelTicks") ? data.getInt("FuelTicks") : TorchSettings.FUEL_TICKS;
        tooltip.add(Component.translatable("message.winternightak.torch_fuel", (fuel + 19) / 20));
        tooltip.add(Component.translatable("tooltip.winternightak.polar_torch"));
    }
}
