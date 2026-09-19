package com.akingno.winternightak.block.entity;

import com.akingno.winternightak.block.custom.PrimitiveLampBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PrimitiveLampBlockEntity extends BlockEntity {
    private int fuelTicks;

    public PrimitiveLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMITIVE_LAMP.get(), pos, state);
    }

    public static int fuelValue(ItemStack stack) {
        return FuelSettings.fuelTicks(stack);
    }

    public int getFuelTicks() { return fuelTicks; }

    public boolean addFuel(int ticks) {
        if (level == null || level.isClientSide || ticks <= 0 || ticks > LampSettings.MAX_FUEL_TICKS - fuelTicks) return false;
        fuelTicks += ticks;
        sync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrimitiveLampBlockEntity lamp) {
        if (!state.getValue(PrimitiveLampBlock.LIT)) return;
        if (state.getValue(PrimitiveLampBlock.WATERLOGGED) || lamp.fuelTicks <= 0) {
            level.setBlock(pos, state.setValue(PrimitiveLampBlock.LIT, false), 3);
            return;
        }
        --lamp.fuelTicks;
        lamp.setChanged();
        if (lamp.fuelTicks == 0) {
            level.setBlock(pos, state.setValue(PrimitiveLampBlock.LIT, false), 3);
            lamp.sync();
        }
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelTicks = Mth.clamp(tag.getInt("FuelTicks"), 0, LampSettings.MAX_FUEL_TICKS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FuelTicks", fuelTicks);
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
