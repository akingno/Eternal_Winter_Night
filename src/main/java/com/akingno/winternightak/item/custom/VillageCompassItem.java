package com.akingno.winternightak.item.custom;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** 复用磁石NBT和指针；普通Item不会检查磁石或被右键磁石重新绑定。 */
public final class VillageCompassItem extends Item {
    // 一格一个，避免不同目的地混在同一堆。
    public VillageCompassItem() { super(new Properties().stacksTo(1)); }
    public static void bind(ItemStack stack, Level level, BlockPos pos) {
        var tag = stack.getOrCreateTag();
        tag.put(CompassItem.TAG_LODESTONE_POS, NbtUtils.writeBlockPos(pos));
        tag.putString(CompassItem.TAG_LODESTONE_DIMENSION, level.dimension().location().toString());
        // 目标不要求磁石；NBT随物品保存，携带时不搜索结构。
        tag.putBoolean(CompassItem.TAG_LODESTONE_TRACKED, false);
    }
    @Override public void appendHoverText(ItemStack stack, Level level,
            java.util.List<net.minecraft.network.chat.Component> lines, TooltipFlag flag) {
        var target = stack.hasTag() ? CompassItem.getLodestonePosition(stack.getTag()) : null;
        // 坐标仅存于NBT供指针使用；交易预览与背包提示都不泄露目标位置。
        if (target == null) lines.add(net.minecraft.network.chat.Component.literal(
                "未绑定村庄（从渔夫购买可获得已绑定指南针）"));
    }
}
