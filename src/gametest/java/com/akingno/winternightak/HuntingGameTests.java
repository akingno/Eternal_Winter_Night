package com.akingno.winternightak;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.item.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(WinterNight.MOD_ID)
@PrefixGameTestTemplate(false)
public class HuntingGameTests {
    @GameTest(template = "empty")
    public static void huntingDropsAndColdSweatLoad(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("cold_sweat"), "Cold Sweat must load alongside Winter Night");
        helper.assertTrue(!ModList.get().isLoaded("geckolib"), "GeckoLib should no longer be required");
        for (EntityType<? extends LivingEntity> type : List.of(EntityType.RABBIT, EntityType.FOX, EntityType.SQUID, EntityType.SALMON)) {
            LivingEntity animal = type.create(helper.getLevel());
            helper.assertTrue(animal != null, "Animal creation failed");
            // Burning animals must still produce raw food during the early phase.
            animal.setSecondsOnFire(30);
            LootParams params = new LootParams.Builder(helper.getLevel())
                    .withParameter(LootContextParams.THIS_ENTITY, animal)
                    .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, helper.getLevel().damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            var table = helper.getLevel().getServer().getLootData().getLootTable(type.getDefaultLootTable());
            for (int sample = 0; sample < 20; sample++) {
                var drops = table.getRandomItems(params);
                helper.assertTrue(drops.stream().anyMatch(s -> s.is(ModItems.RAW_GAME_MEAT.get())), type + " missing raw meat");
                helper.assertTrue(drops.stream().anyMatch(s -> s.is(ModItems.ANIMAL_FAT.get())), type + " missing fat");
                helper.assertTrue(drops.stream().anyMatch(s -> s.is(Items.LEATHER)), type + " missing leather");
                helper.assertTrue(drops.stream().noneMatch(s -> s.is(Items.RABBIT) || s.is(Items.COOKED_RABBIT)
                        || s.is(Items.SALMON) || s.is(Items.COOKED_SALMON) || s.is(Items.RABBIT_HIDE)), "Vanilla meat/hide duplicated");
                if (type == EntityType.RABBIT || type == EntityType.FOX) {
                    helper.assertTrue(drops.stream().anyMatch(s -> s.is(com.momosoftworks.coldsweat.core.init.ItemInit.GOAT_FUR.get())), "Missing Cold Sweat fur");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hardIceExplosionDrops(GameTestHelper helper) {
        var state = ModBlocks.HARD_ICE.get().defaultBlockState();
        // Explosions have no mining tool; also exercise radius-based explosion loot contexts.
        var params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withParameter(LootContextParams.EXPLOSION_RADIUS, 4.0F);
        for (int sample = 0; sample < 20; sample++) {
            var drops = state.getDrops(params);
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(ModItems.ICE_BRICK.get())
                    && drops.get(0).getCount() == 1, "Destroyed hard ice must drop one brick");
        }
        helper.succeed();
    }
}
