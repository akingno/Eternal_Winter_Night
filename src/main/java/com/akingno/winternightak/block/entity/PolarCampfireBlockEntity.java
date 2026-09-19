package com.akingno.winternightak.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Reuse vanilla food storage, cooking, synchronization and rendering. */
public class PolarCampfireBlockEntity extends CampfireBlockEntity {
    private int fuelTicks;

    public PolarCampfireBlockEntity(BlockPos pos, BlockState state) { super(pos, state); }

    // Vanilla's constructor fixes its type; all saving, ticking and rendering use this getter.
    @Override
    public BlockEntityType<?> getType() { return ModBlockEntities.CAMPFIRE.get(); }

    public int getFuelTicks() { return fuelTicks; }

    public static int fuelValue(ItemStack stack) {
        return FuelSettings.fuelTicks(stack);
    }

    public boolean addFuel(int ticks) {
        if (level == null || level.isClientSide || ticks <= 0 || ticks > CampfireSettings.MAX_FUEL_TICKS - fuelTicks) return false;
        fuelTicks += ticks;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PolarCampfireBlockEntity fire) {
        if (!state.getValue(CampfireBlock.LIT)) {
            CampfireBlockEntity.cooldownTick(level, pos, state, fire);
            return;
        }
        if (fire.fuelTicks > 0 && !state.getValue(CampfireBlock.WATERLOGGED)) {
            CampfireBlockEntity.cookTick(level, pos, state, fire);
            --fire.fuelTicks;
            fire.setChanged();
        }
        if (fire.fuelTicks == 0 || state.getValue(CampfireBlock.WATERLOGGED)) {
            level.setBlock(pos, state.setValue(CampfireBlock.LIT, false), 3);
            CampfireBlock.dowse(null, level, pos, state);
        }
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        fuelTicks = Mth.clamp(tag.getInt("FuelTicks"), 0, CampfireSettings.MAX_FUEL_TICKS);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FuelTicks", fuelTicks);
    }
    @Override public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        tag.putInt("FuelTicks", fuelTicks);
        return tag;
    }
}
