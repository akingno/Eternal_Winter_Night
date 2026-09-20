package com.akingno.winternightak.entity;

import com.akingno.winternightak.item.ModItems;
import com.akingno.winternightak.item.custom.StoneJavelinItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;

/** 复用原版箭的运动、碰撞、落地回收和保存；命中逻辑参考三叉戟，取消其专属附魔能力。 */
public final class StoneJavelinEntity extends AbstractArrow {
    private ItemStack carriedStack = new ItemStack(ModItems.STONE_JAVELIN.get());
    private boolean dealtDamage;
    public StoneJavelinEntity(EntityType<? extends StoneJavelinEntity> type, Level level) { super(type, level); }
    public StoneJavelinEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.STONE_JAVELIN.get(), owner, level);
        carriedStack = stack.copy();
    }
    @Override public void tick() {
        // 沿用三叉戟：插地超过4 tick后不再命中路过生物，防止一支反复伤害。
        if (inGroundTime > 4) dealtDamage = true;
        super.tick();
    }
    @Override protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return dealtDamage ? null : super.findHitEntity(start, end);
    }
    @Override protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity(), owner = getOwner();
        dealtDamage = true;
        float damage = StoneJavelinItem.THROW_DAMAGE;
        if (target instanceof LivingEntity living) damage += EnchantmentHelper.getDamageBonus(carriedStack, living.getMobType());
        if (target.hurt(damageSources().trident(this, owner == null ? this : owner), damage)) {
            if (target.getType() == EntityType.ENDERMAN) return;
            if (target instanceof LivingEntity living) {
                if (owner instanceof LivingEntity attacker) {
                    EnchantmentHelper.doPostHurtEffects(living, attacker);
                    EnchantmentHelper.doPostDamageEffects(attacker, living);
                }
                doPostHurtEffects(living);
            }
        }
        // 原版三叉戟命中后的轻微回弹，使投射物落地，之后可以走近回收。
        setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        playSound(SoundEvents.TRIDENT_HIT, 1, 1);
    }
    @Override protected ItemStack getPickupItem() { return carriedStack.copy(); }
    @Override protected SoundEvent getDefaultHitGroundSoundEvent() { return SoundEvents.TRIDENT_HIT_GROUND; }
    // 原版三叉戟水中速度保留率，值越小水下阻力越大。
    @Override protected float getWaterInertia() { return 0.99F; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("JavelinItem", carriedStack.save(new CompoundTag()));
        tag.putBoolean("DealtDamage", dealtDamage);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("JavelinItem", 10)) carriedStack = ItemStack.of(tag.getCompound("JavelinItem"));
        dealtDamage = tag.getBoolean("DealtDamage");
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
