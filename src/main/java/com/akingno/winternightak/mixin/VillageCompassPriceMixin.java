package com.akingno.winternightak.mixin;
import com.akingno.winternightak.item.ModItems;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 该交易固定20皮毛；原版村庄英雄折扣独立于价格倍率，因此只对此商品清除特殊折扣。 */
@Mixin(Villager.class)
public abstract class VillageCompassPriceMixin {
    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void winterNight$fixedCompassPrice(Player player, CallbackInfo ci) {
        for (var offer : ((Villager)(Object)this).getOffers()) {
            if (offer.getResult().is(ModItems.VILLAGE_COMPASS.get()) || offer.getResult().is(ModItems.ICE_PICK.get())
                    || offer.getResult().is(ModItems.STONE_JAVELIN.get()) || offer.getResult().is(ModItems.METAL_PART.get())
                    || offer.getResult().is(Items.REDSTONE)) offer.resetSpecialPriceDiff();
        }
    }
}
