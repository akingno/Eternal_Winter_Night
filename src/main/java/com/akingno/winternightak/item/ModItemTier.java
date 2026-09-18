package com.akingno.winternightak.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum ModItemTier implements Tier {
    KATANA(2, 400, 6.0F, 2.0F, 14,
            () -> Ingredient.of(Items.IRON_INGOT) // 3. fromItems 变成了 of
    );

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    ModItemTier(int level, int uses, float speed, float damage, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() { // 旧: getMaxUses
        return uses;
    }

    @Override
    public float getSpeed() { // 旧: getEfficiency
        return speed;
    }

    @Override
    public float getAttackDamageBonus() { // 旧: getAttackDamage
        return damage;
    }

    @Override
    public int getLevel() { // 旧: getHarvestLevel
        return level;
    }

    @Override
    public int getEnchantmentValue() { // 旧: getEnchantability
        return enchantmentValue;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() { // 旧: getRepairMaterial
        return repairIngredient.get(); // 直接调用 Supplier 的 get()
    }
}
