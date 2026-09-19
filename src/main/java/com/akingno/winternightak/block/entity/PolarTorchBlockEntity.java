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
    private int fuelTicks = TorchSettings.FUEL_TICKS;

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

    // 新物品无FuelTicks时使用初始时长；明确保存的0必须保留，防止捡起燃尽火把刷满燃料。
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        fuelTicks = tag.contains("FuelTicks") ? Mth.clamp(tag.getInt("FuelTicks"), 0, TorchSettings.FUEL_TICKS) : TorchSettings.FUEL_TICKS;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FuelTicks", fuelTicks);
    }

    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
