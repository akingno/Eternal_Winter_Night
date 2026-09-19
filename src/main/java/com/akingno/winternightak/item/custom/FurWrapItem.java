package com.akingno.winternightak.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.UUID;

public class FurWrapItem extends ArmorItem {
    public static final double SPEED_PENALTY = -0.10;
    private static final ArmorMaterial MATERIAL = new ArmorMaterial() {
        public int getDurabilityForType(Type type) { return 80; }
        public int getDefenseForType(Type type) { return 0; }
        public int getEnchantmentValue() { return 0; }
        public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_LEATHER; }
        public Ingredient getRepairIngredient() { return Ingredient.of(ItemInit.GOAT_FUR.get()); }
        public String getName() { return "winternightak:fur_wrap"; }
        public float getToughness() { return 0; }
        public float getKnockbackResistance() { return 0; }
    };
    private static final Multimap<Attribute, AttributeModifier> MODIFIERS = ImmutableMultimap.of(
            Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("969b128b-36cb-4126-bbc1-d49fcdb57d11"),
                    "Fur wrap movement penalty", SPEED_PENALTY, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public FurWrapItem() { super(MATERIAL, Type.CHESTPLATE, new Item.Properties()); }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? MODIFIERS : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "minecraft:textures/models/armor/diamond_layer_1.png";
    }
}
