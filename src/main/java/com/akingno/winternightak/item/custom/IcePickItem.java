package com.akingno.winternightak.item.custom;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

/** 冰镐为硬冰提供正确工具与挖掘速度；其他方块继续走原版镐逻辑，本阶段没有制作配方。 */
public class IcePickItem extends PickaxeItem {
    public IcePickItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // 硬冰直接使用工具等级的挖掘速度；其他方块仍由原版PickaxeItem决定。
        return state.is(ModBlocks.HARD_ICE.get()) ? getTier().getSpeed() : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        // 让硬冰掉落判定认可冰镐；是否完全禁止其他工具挖掘由HardIceBlock另行控制。
        return state.is(ModBlocks.HARD_ICE.get()) || super.isCorrectToolForDrops(state);
    }
}
