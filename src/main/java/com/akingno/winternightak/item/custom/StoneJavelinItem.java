package com.akingno.winternightak.item.custom;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeMod;
import java.util.UUID;

/** 第一版为近战标枪，沿用石剑冷却、耐久和伤害，通过Forge同步攻击距离属性。 */
public final class StoneJavelinItem extends SwordItem {
    private static final UUID REACH_ID = UUID.fromString("2bd0cbb3-1098-450f-96a9-e5a8f01d8094");
    // 增加1格实体攻击距离，生存基础3格变4格；不增加方块交互距离。
    private static final double EXTRA_REACH = 1.0;
    public StoneJavelinItem() {
        // 石剑附加伤害3、攻速修饰-2.4：玩家面板5伤害、1.6次/秒，石级耐久131。
        super(Tiers.STONE, 3, -2.4F, new Properties());
    }
    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        var base = super.getDefaultAttributeModifiers(slot);
        if (slot != EquipmentSlot.MAINHAND) return base;
        return ImmutableMultimap.<Attribute, AttributeModifier>builder().putAll(base)
                .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_ID, "Javelin reach",
                        EXTRA_REACH, AttributeModifier.Operation.ADDITION)).build();
    }
}
