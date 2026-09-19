package com.akingno.winternightak.block.entity;

import com.akingno.winternightak.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 所有简易燃烧设备共用的燃料表，避免油灯、火把和营火分别维护相同数值。 */
public final class FuelSettings {
    // 木棍2400tick=120秒；调高一份木棍烧更久，新火把也引用这项。
    public static final int STICK_FUEL_TICKS = 2400;
    // 小麦时长统一供油灯与营火读取；调高更耐烧，正常20tick为1秒。
    public static final int WHEAT_FUEL_TICKS = 2400;
    // 油脂1800tick=90秒；调高一份油脂烧更久，避免各设备分别维护数值。
    public static final int FAT_FUEL_TICKS = 1800;

    // 统一查表入口；返回0代表不接受此物品，不是无限燃料。
    public static int fuelTicks(ItemStack stack) {
        if (stack.is(Items.STICK)) return STICK_FUEL_TICKS;
        if (stack.is(Items.WHEAT)) return WHEAT_FUEL_TICKS;
        if (stack.is(ModItems.ANIMAL_FAT.get())) return FAT_FUEL_TICKS;
        return 0;
    }
    private FuelSettings() {}
}
