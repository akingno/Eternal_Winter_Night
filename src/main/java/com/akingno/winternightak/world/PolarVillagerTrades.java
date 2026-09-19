package com.akingno.winternightak.world;
import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.item.ModItems;
import com.akingno.winternightak.item.custom.VillageCompassItem;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 清空职业交易表；交互时迁移旧村民和营地预设空交易的渔夫。 */
@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
public final class PolarVillagerTrades {
    // 基础价格20皮毛，调高更贵。
    private static final int FUR_COST = 20;
    // 工具匠出售冰镐需40皮毛，武器匠出售标枪需15硬冰；调高分别提高对应价格。
    private static final int PICK_FUR_COST = 40;
    private static final int JAVELIN_ICE_COST = 15;
    // 每次补货出售12次；调高减少补货等待，仍沿用原版工作站补货。
    private static final int MAX_USES = 12;
    // 失败后等待1200tick（20TPS下一分钟）再允许交互重试，避免反复搜索。
    private static final long RETRY_TICKS = 1200L;
    private static final String VERSION_KEY = "WinterNightTradesV1";
    private static final String RETRY_KEY = "WinterNightCompassRetry";
    @SubscribeEvent public static void tables(VillagerTradesEvent event) {
        event.getTrades().values().forEach(java.util.List::clear);
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)
                || !(villager.level() instanceof ServerLevel level) || villager.isBaby()) return;
        var profession = villager.getVillagerData().getProfession();
        String key = net.minecraftforge.registries.ForgeRegistries.VILLAGER_PROFESSIONS.getKey(profession).toString();
        // 只升级这两个职业的迁移版本，旧渔夫的指南针目标和库存不重置。
        if (profession == VillagerProfession.TOOLSMITH || profession == VillagerProfession.WEAPONSMITH) key += ":v2";
        var data = villager.getPersistentData();
        // 职业未变且已迁移时不覆盖，保存购买次数、补货状态和目标坐标。
        if (key.equals(data.getString(VERSION_KEY))
                && ((profession != VillagerProfession.FISHERMAN && profession != VillagerProfession.TOOLSMITH
                && profession != VillagerProfession.WEAPONSMITH) || !villager.getOffers().isEmpty())) return;
        villager.setOffers(new MerchantOffers());
        if (profession == VillagerProfession.TOOLSMITH || profession == VillagerProfession.WEAPONSMITH) {
            var offers = new MerchantOffers();
            boolean toolsmith = profession == VillagerProfession.TOOLSMITH;
            var cost = toolsmith ? new ItemStack(ItemInit.GOAT_FUR.get(), PICK_FUR_COST)
                    : new ItemStack(com.akingno.winternightak.block.ModBlocks.HARD_ICE.get(), JAVELIN_ICE_COST);
            // 每次产出1件，经验0、价格倍率0，沿用原版补货而不追加高级交易。
            offers.add(new MerchantOffer(cost, new ItemStack(toolsmith ? ModItems.ICE_PICK.get()
                    : ModItems.STONE_JAVELIN.get()), MAX_USES, 0, 0.0F));
            villager.setOffers(offers);
            data.putString(VERSION_KEY, key);
            return;
        }
        if (profession != VillagerProfession.FISHERMAN) {
            data.putString(VERSION_KEY, key);
            return;
        }
        if (level.getGameTime() < data.getLong(RETRY_KEY)) return;
        data.putLong(RETRY_KEY, level.getGameTime() + RETRY_TICKS);
        var pos = PolarVillageLocator.find(level, villager.blockPosition());
        if (pos == null) {
            event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "附近未找到雪村，暂时无法出售村庄指南针；稍后可再次询问"), false);
            return;
        }
        var compass = new ItemStack(ModItems.VILLAGE_COMPASS.get());
        VillageCompassItem.bind(compass, level, pos);
        var offers = new MerchantOffers();
        // 经验0不解锁后续职业等级；价格倍率0不因需求或声望调整基础价格。
        offers.add(new MerchantOffer(new ItemStack(ItemInit.GOAT_FUR.get(), FUR_COST), compass, MAX_USES, 0, 0.0F));
        villager.setOffers(offers);
        data.putString(VERSION_KEY, key);
    }
    private PolarVillagerTrades() {}
}
