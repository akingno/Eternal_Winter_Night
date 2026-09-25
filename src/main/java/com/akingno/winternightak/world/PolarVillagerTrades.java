package com.akingno.winternightak.world;
import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.item.ModItems;
import com.akingno.winternightak.item.custom.VillageCompassItem;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    // 32份生动物肉换1把弓；调高提高狩猎成本，补货次数复用MAX_USES。
    private static final int BOW_MEAT_COST = 32;
    // 10根云杉原木换1块铁板；调高原木数会减慢可再生铁的获取速度。
    private static final int METAL_LOG_COST = 10;
    // 1铁锭买1苗，每次补货最多4次；调高价格或调低次数可减慢木材再生。
    private static final int TREE_IRON_COST = 1;
    private static final int TREE_MAX_USES = 4;
    // 牧师红石交易同时消耗4份油脂和8根云杉原木；分别调高会增加狩猎或伐木压力。
    private static final int REDSTONE_FAT_COST = 4;
    private static final int REDSTONE_LOG_COST = 8;
    // 一次交易产出1份红石；调高会让第一批机器更容易制作。
    private static final int REDSTONE_RESULT = 1;
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
        // 工具匠升到v3以给旧村民追加铁板；牧师首次加入Phase 3交易。旧渔夫指南针目标不重置。
        if (profession == VillagerProfession.TOOLSMITH) key += ":v5";
        else if (profession == VillagerProfession.WEAPONSMITH) key += ":v3";
        else if (profession == VillagerProfession.CLERIC) key += ":v3";
        else if (profession == VillagerProfession.LEATHERWORKER) key += ":v1";
        var data = villager.getPersistentData();
        // 旧武器匠只追加弓，不重置标枪交易的已用次数和补货状态。
        if (profession == VillagerProfession.WEAPONSMITH && data.getString(VERSION_KEY).equals("minecraft:weaponsmith:v2")
                && !villager.getOffers().isEmpty()) {
            if (villager.getOffers().stream().noneMatch(offer -> offer.getResult().is(Items.BOW)))
                villager.getOffers().add(bowOffer());
            data.putString(VERSION_KEY, key);
            return;
        }
        // 渔夫单独按商品补齐，保留指南针坐标、原交易次数；定位失败也能购买标枪。
        if (profession == VillagerProfession.FISHERMAN) {
            var offers = villager.getOffers();
            offers.removeIf(offer -> !offer.getResult().is(ModItems.VILLAGE_COMPASS.get())
                    && !offer.getResult().is(ModItems.STONE_JAVELIN.get()));
            if (offers.stream().noneMatch(offer -> offer.getResult().is(ModItems.STONE_JAVELIN.get()))) {
                // 30份生动物肉换1支，每次补货12次；增加30提高狩猎成本。
                offers.add(new MerchantOffer(new ItemStack(ModItems.RAW_GAME_MEAT.get(), 30),
                        new ItemStack(ModItems.STONE_JAVELIN.get()), MAX_USES, 0, 0.0F));
            }
            if (offers.stream().noneMatch(offer -> offer.getResult().is(ModItems.VILLAGE_COMPASS.get()))
                    && level.getGameTime() >= data.getLong(RETRY_KEY)) {
                data.putLong(RETRY_KEY, level.getGameTime() + RETRY_TICKS);
                var target = PolarVillageLocator.find(level, villager.blockPosition());
                if (target != null) {
                    var compass = new ItemStack(ModItems.VILLAGE_COMPASS.get());
                    VillageCompassItem.bind(compass, level, target);
                    offers.add(new MerchantOffer(new ItemStack(ItemInit.GOAT_FUR.get(), FUR_COST), compass, MAX_USES, 0, 0.0F));
                }
            }
            data.putString(VERSION_KEY, key);
            return;
        }
        boolean hasCustomOffers = profession == VillagerProfession.FISHERMAN
                || profession == VillagerProfession.TOOLSMITH
                || profession == VillagerProfession.WEAPONSMITH
                || profession == VillagerProfession.CLERIC
                || profession == VillagerProfession.LEATHERWORKER;
        // 职业未变且已迁移时不覆盖，保存购买次数、补货状态和目标坐标。
        if (key.equals(data.getString(VERSION_KEY))
                && (!hasCustomOffers || !villager.getOffers().isEmpty())) return;
        villager.setOffers(new MerchantOffers());
        if (profession == VillagerProfession.TOOLSMITH || profession == VillagerProfession.WEAPONSMITH) {
            var offers = new MerchantOffers();
            boolean toolsmith = profession == VillagerProfession.TOOLSMITH;
            var cost = toolsmith ? new ItemStack(ItemInit.GOAT_FUR.get(), PICK_FUR_COST)
                    : new ItemStack(com.akingno.winternightak.block.ModBlocks.HARD_ICE.get(), JAVELIN_ICE_COST);
            // 每次产出1件，经验0、价格倍率0，沿用原版补货而不追加高级交易。
            offers.add(new MerchantOffer(cost, new ItemStack(toolsmith ? ModItems.ICE_PICK.get()
                    : ModItems.STONE_JAVELIN.get()), MAX_USES, 0, 0.0F));
            if (toolsmith) {
                // 第二项交易形成主动伐木→文明交易→铁板的可再生金属路线。
                offers.add(new MerchantOffer(new ItemStack(Items.SPRUCE_LOG, METAL_LOG_COST),
                        new ItemStack(ModItems.METAL_PART.get()), MAX_USES, 0, 0.0F));
                offers.add(treeOffer());
            }
            else offers.add(bowOffer());
            villager.setOffers(offers);
            data.putString(VERSION_KEY, key);
            return;
        }
        if (profession == VillagerProfession.CLERIC || profession == VillagerProfession.LEATHERWORKER) {
            var offers = new MerchantOffers();
            // 两种职业使用完全相同的双输入红石交易；不恢复红石矿或怪物资源链。
            offers.add(redstoneOffer());
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
    private static MerchantOffer bowOffer() {
        return new MerchantOffer(new ItemStack(ModItems.RAW_GAME_MEAT.get(), BOW_MEAT_COST),
                new ItemStack(Items.BOW), MAX_USES, 0, 0.0F);
    }
    private static MerchantOffer redstoneOffer() {
        return new MerchantOffer(new ItemStack(ModItems.ANIMAL_FAT.get(), REDSTONE_FAT_COST),
                new ItemStack(Items.SPRUCE_LOG, REDSTONE_LOG_COST),
                new ItemStack(Items.REDSTONE, REDSTONE_RESULT), MAX_USES, 0, 0.0F);
    }
    private static MerchantOffer treeOffer() {
        return new MerchantOffer(new ItemStack(Items.IRON_INGOT, TREE_IRON_COST),
                new ItemStack(com.akingno.winternightak.block.ModBlocks.CHRISTMAS_TREE_SAPLING.get()), TREE_MAX_USES, 0, 0.0F);
    }
}
