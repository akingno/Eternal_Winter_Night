package com.akingno.winternightak.world;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.item.ModItems;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import com.momosoftworks.coldsweat.core.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Set;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
/** 极地世界生物白名单与生成条件；这里控制种类和地面条件，刷新权重在PolarWorldgen中调整。 */
public final class PolarSpawning {
    private static final Set<EntityType<?>> ALLOWED = Set.of(EntityType.RABBIT, EntityType.FOX,
            EntityType.WOLF, EntityType.SQUID, EntityType.SALMON, EntityType.COD, EntityType.VILLAGER);

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !event.getLevel().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) return;
        if (event.getEntity() instanceof Rabbit rabbit) rabbit.setVariant(Rabbit.Variant.WHITE);
        if (event.getEntity() instanceof Fox fox) fox.setVariant(Fox.Type.SNOW);
        // 仅首次生成处理叼物，读档不重复随机；0.10调高更常叼物，nextBoolean保持肉/皮毛各一半。
        if (!event.loadedFromDisk() && event.getEntity() instanceof Fox fox) {
            fox.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (fox.getRandom().nextFloat() < 0.10F) {
                fox.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(fox.getRandom().nextBoolean()
                        ? ModItems.RAW_GAME_MEAT.get() : ItemInit.GOAT_FUR.get()));
            }
        }
        // 只拦截白名单之外的原版Mob，避免无意删除其他模组实体；同样涵盖刷怪蛋和结构带入的猫/铁傀儡。
        if (event.getEntity() instanceof Mob mob && !ALLOWED.contains(mob.getType())
                && "minecraft".equals(ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).getNamespace()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void position(MobSpawnEvent.PositionCheck event) {
        if (!event.getLevel().getLevel().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        var mob = event.getEntity();
        var type = mob.getType();
        if (type == EntityType.RABBIT || type == EntityType.FOX || type == EntityType.WOLF)
            event.setResult(mob.checkSpawnObstruction(event.getLevel()) ? Event.Result.ALLOW : Event.Result.DENY);
    }

    @SubscribeEvent
    public static void placement(MobSpawnEvent.SpawnPlacementCheck event) {
        var level = event.getLevel();
        if (!level.getLevel().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL && event.getSpawnType() != MobSpawnType.CHUNK_GENERATION) return;
        var type = event.getEntityType();
        if (type != EntityType.RABBIT && type != EntityType.FOX && type != EntityType.WOLF) return;
        BlockPos pos = event.getPos();
        var at = level.getBlockState(pos);
        var ground = level.getBlockState(pos.below());
        if (ground.is(Blocks.SNOW)) ground = level.getBlockState(pos.below(2));
        boolean island = ground.is(ModBlocks.FROZEN_SOIL.get());
        boolean cap = ground.is(ModBlocks.HARD_ICE.get());
        boolean clear = (at.isAir() || at.is(Blocks.SNOW)) && level.getBlockState(pos.above()).isAir();
        // 用极地地面替代原版草方块/光照限制；仍检查身体空间。狼只允许冻结土壤，兔狐允许硬冰。
        event.setResult(clear && (island || cap && type != EntityType.WOLF) ? Event.Result.ALLOW : Event.Result.DENY);
    }
}
