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

/** 胸部皮毛斗篷：本类只管耐久、外观和减速；保暖值在cold_sweat/item/insulator/fur_wrap.json中。 */
public class FurWrapItem extends ArmorItem implements DyeableLeatherItem {
    // MULTIPLY_TOTAL下-0.10表示速度乘0.9；越负越慢，0不减速。
    public static final double SPEED_PENALTY = -0.10;
    // 耐久80调高更耐用；防御/韧性/附魔值/击退抗性为0，定位是保暖衣物。
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
    // 固定UUID避免反复穿脱时叠加同一个减速属性；只在胸部槽生效。
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
        return "winternightak:textures/models/armor/fur_wrap_layer_1" + ("overlay".equals(type) ? "_overlay" : "") + ".png";
    }
}
