package com.akingno.winternightak.item.custom;

import com.akingno.winternightak.block.ModBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;

public class IcePickItem extends PickaxeItem {
    public IcePickItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(ModBlocks.HARD_ICE.get()) ? getTier().getSpeed() : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(ModBlocks.HARD_ICE.get()) || super.isCorrectToolForDrops(state);
    }
}
