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

/** 油灯服务端燃料账户。每个燃烧tick扣1；未加载时不扣除，破坏掉落不继承余料。 */
public class PrimitiveLampBlockEntity extends BlockEntity {
    private int fuelTicks;

    public PrimitiveLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMITIVE_LAMP.get(), pos, state);
    }

    public static int fuelValue(ItemStack stack) {
        return FuelSettings.fuelTicks(stack);
    }

    public int getFuelTicks() { return fuelTicks; }

    // 仅服务端允许添料；必须能完整容纳一份，失败不消耗玩家物品。
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

    // 只在关键状态变化时发送更新，日常扣料只标记存档，避免每tick同步燃料。
    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Override
    // 读档余料限制在0～容量之间；0为空灯，旧版本超额数据不会越界。
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
