package com.akingno.winternightak.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** 火把只保存剩余燃烧tick；掉落表将FuelTicks写入物品BlockEntityTag，重新放置时恢复余量。 */
public class PolarTorchBlockEntity extends BlockEntity {
    private int fuelTicks;

    /** 整份添料，容量复用原火把时长；只有服务端能修改，满容量不扣物品。 */
    public boolean addFuel(int ticks) {
        if (level == null || level.isClientSide || ticks <= 0 || ticks > TorchSettings.FUEL_TICKS - fuelTicks) return false;
        fuelTicks += ticks;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        return true;
    }

    public PolarTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLAR_TORCH.get(), pos, state);
    }

    public int getFuelTicks() { return fuelTicks; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PolarTorchBlockEntity torch) {
        if (!state.getValue(BlockStateProperties.LIT)) return;
        if (torch.fuelTicks > 0) {
            --torch.fuelTicks;
            torch.setChanged();
        }
        if (torch.fuelTicks == 0) level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
    }

    // 无FuelTicks的新物品为空火把；旧物品/村庄显式保存的余料仍保留。
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        fuelTicks = Mth.clamp(tag.getInt("FuelTicks"), 0, TorchSettings.FUEL_TICKS);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FuelTicks", fuelTicks);
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
