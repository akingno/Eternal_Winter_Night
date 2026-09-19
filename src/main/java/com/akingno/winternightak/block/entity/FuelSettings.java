package com.akingno.winternightak.block.entity;

import com.akingno.winternightak.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared burn times for all primitive fuel-burning devices. */
public final class FuelSettings {
    public static final int STICK_FUEL_TICKS = 2400;
    public static final int WHEAT_FUEL_TICKS = 2400;
    public static final int FAT_FUEL_TICKS = 1800;

    public static int fuelTicks(ItemStack stack) {
        if (stack.is(Items.STICK)) return STICK_FUEL_TICKS;
        if (stack.is(Items.WHEAT)) return WHEAT_FUEL_TICKS;
        if (stack.is(ModItems.ANIMAL_FAT.get())) return FAT_FUEL_TICKS;
        return 0;
    }
    private FuelSettings() {}
}
