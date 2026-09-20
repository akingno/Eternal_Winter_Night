package com.akingno.winternightak.item.custom;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeMod;
import java.util.UUID;

/** 石剑级近战与耐久，增加原版三叉戟式蓄力投掷；投射物独立保存原物品。 */
public final class StoneJavelinItem extends SwordItem {
    private static final UUID REACH_ID = UUID.fromString("2bd0cbb3-1098-450f-96a9-e5a8f01d8094");
    // 增加1格实体攻击距离，生存基础3格变4格；不增加方块交互距离。
    private static final double EXTRA_REACH = 1.0;
    // 10 tick = 半秒，沿用三叉戟最短蓄力；调高需要按住更久。
    public static final int CHARGE_TICKS = 10;
    // 基础投掷伤害7，高于近战5，低于原版三叉戟投掷8；调高更强。
    public static final float THROW_DAMAGE = 7.0F;
    // 与原版三叉戟相同的初速；调高飞得更快、更远。
    private static final float THROW_SPEED = 2.5F;
    public StoneJavelinItem() {
        // 石剑附加伤害3、攻速修饰-2.4：玩家面板5伤害、1.6次/秒，石级耐久131。
        super(Tiers.STONE, 3, -2.4F, new Properties());
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPEAR; }
    // 沿用原版最长使用时间，并不需要蓄力这么久。
    @Override public int getUseDuration(ItemStack stack) { return 72000; }
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 保留最后一点耐久，避免投出已损坏的空物品。
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1)
            return net.minecraft.world.InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return net.minecraft.world.InteractionResultHolder.consume(stack);
    }
    @Override public void releaseUsing(ItemStack stack, net.minecraft.world.level.Level level,
            net.minecraft.world.entity.LivingEntity user, int remaining) {
        if (level.isClientSide || !(user instanceof net.minecraft.world.entity.player.Player player)
                || getUseDuration(stack) - remaining < CHARGE_TICKS
                || stack.getDamageValue() >= stack.getMaxDamage() - 1) return;
        // 发射成功后才扣耐久、移除手中物品；创造模式投射物不可供生存玩家回收。
        ItemStack thrownStack = stack.copy();
        thrownStack.setCount(1);
        if (!player.getAbilities().instabuild) thrownStack.setDamageValue(stack.getDamageValue() + 1);
        var projectile = new com.akingno.winternightak.entity.StoneJavelinEntity(level, player, thrownStack);
        // 0为额外俯仰，1为原版散布；增大散布会降低准确性。
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, THROW_SPEED, 1);
        if (player.getAbilities().instabuild)
            projectile.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY;
        if (!level.addFreshEntity(projectile)) return;
        if (!player.getAbilities().instabuild) player.getInventory().removeItem(stack);
        level.playSound(null, projectile, net.minecraft.sounds.SoundEvents.TRIDENT_THROW,
                net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
        player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));
    }
    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        var base = super.getDefaultAttributeModifiers(slot);
        if (slot != EquipmentSlot.MAINHAND) return base;
        return ImmutableMultimap.<Attribute, AttributeModifier>builder().putAll(base)
                .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_ID, "Javelin reach",
                        EXTRA_REACH, AttributeModifier.Operation.ADDITION)).build();
    }
}
