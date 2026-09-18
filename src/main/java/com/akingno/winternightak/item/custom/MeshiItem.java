package com.akingno.winternightak.item.custom;

import com.akingno.winternightak.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MeshiItem extends Item {
    private static final int EAT_DURATION = 32;

    public MeshiItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * Called when the player finishes using this Item.
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        // 调用父类方法应用饱食度恢复
        ItemStack resultStack = super.finishUsingItem(stack, level, entityLiving);

        if (!level.isClientSide && entityLiving instanceof Player) {
            // 在服务器端清除效果并处理成竹碗
            if (entityLiving instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack); // 触发进度
                serverPlayer.awardStat(Stats.ITEM_USED.get(this));          // 统计使用次数
            }

            if (!((Player) entityLiving).getAbilities().instabuild) {
                stack.shrink(1);  // 减少物品堆叠数
            }
        }

        // 返回 BAMBOO_BUCKET 物品
        return stack.isEmpty() ? new ItemStack(ModItems.BAMBOO_BUCKET.get()) : resultStack;
    }


    /**
     * 设置物品使用持续时间
     */
    @Override
    public int getUseDuration(ItemStack stack) {
        return EAT_DURATION;
    }

    /**
     * 使用动画
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    /**
     * 右键点击使用物品
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
