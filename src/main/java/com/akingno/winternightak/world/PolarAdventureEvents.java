package com.akingno.winternightak.world;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.compat.BlizzardTemperature;
import com.akingno.winternightak.network.PolarNetwork;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
/** 服务端入口：每秒维护暴雪、供暖与营地，登录/重生时同步状态，并注册管理员命令。 */
public final class PolarAdventureEvents {
    // 存在Forge约定的玩家持久区中，死亡重建Player实体时仍会保留，避免反复赠送打火石。
    private static final String STARTER_FLINT_KEY = "WinterNightStarterFlintGiven";

    @SubscribeEvent public static void serverStarted(ServerStartedEvent event) {
        // keepInventory是原版服务器级游戏规则；极地预设只含一个主世界，因此启用后死亡不掉落物品和经验。
        // 仅当服务器实际加载了极地维度时修改，避免把本Mod装进普通世界后无条件改变规则。
        boolean hasPolarLevel = false;
        for (var level : event.getServer().getAllLevels()) {
            if (level.dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) {
                hasPolarLevel = true;
                break;
            }
        }
        if (hasPolarLevel && !event.getServer().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))
            event.getServer().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, event.getServer());
    }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (var level : event.getServer().getAllLevels()) {
            // 每20tick（1秒）执行一次维护，避免每tick重复遍历玩家与进行营地选址。
            if (level.getGameTime() % 20 != 0 || !level.dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) continue;
            var storm = BlizzardData.get(level);
            storm.tick(level);
            boolean active = storm.active(level);
            for (var player : level.players()) {
                // Once per second also covers joins, respawns and changes of dimension.
                PolarNetwork.sync(player, active);
                if (active) Temperature.replaceOrAddModifier(player, new BlizzardTemperature().expires(40).tickRate(20), Temperature.Trait.WORLD, Matcher.SAME_CLASS);
                else Temperature.removeModifiers(player, Temperature.Trait.WORLD, BlizzardTemperature.class);
            }
            if (!level.players().isEmpty()) HunterCampData.get(level).tick(level);
        }
    }
    private static void sync(ServerPlayer player) {
        var level = player.serverLevel();
        boolean active = level.dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE) && BlizzardData.get(level).active(level);
        PolarNetwork.sync(player, active);
        if (!active) Temperature.removeModifiers(player, Temperature.Trait.WORLD, BlizzardTemperature.class);
        if (level.dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) giveStarterFlint(player);
    }

    /** 第一次进入极地世界时赠送一把满耐久打火石；背包满时掉在玩家脚下，标记仍只写一次。 */
    private static void giveStarterFlint(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
        if (persistent.getBoolean(STARTER_FLINT_KEY)) return;
        ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
        if (!player.getInventory().add(flint)) player.drop(flint, false);
        persistent.putBoolean(STARTER_FLINT_KEY, true);
        player.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, persistent);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) { if (event.getEntity() instanceof ServerPlayer player) sync(player); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { if (event.getEntity() instanceof ServerPlayer player) sync(player); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) { if (event.getEntity() instanceof ServerPlayer player) sync(player); }

    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("winternight").requires(source -> source.hasPermission(2))
            .then(Commands.literal("blizzard")
                .then(Commands.literal("start").executes(context -> start(context.getSource(), PolarAdventureSettings.BLIZZARD_DURATION_TICKS))
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 3600)).executes(context -> start(context.getSource(), IntegerArgumentType.getInteger(context, "seconds") * 20))))
                .then(Commands.literal("stop").executes(context -> {
                    var level = polar(context.getSource()); if (level == null) return 0;
                    BlizzardData.get(level).stop();
                    level.players().forEach(PolarAdventureEvents::sync);
                    context.getSource().sendSuccess(() -> Component.literal("暴雪已停止"), false);
                    return 1;
                }))
                .then(Commands.literal("status").executes(context -> {
                    var level = polar(context.getSource()); if (level == null) return 0;
                    context.getSource().sendSuccess(() -> Component.literal("暴雪剩余：" + BlizzardData.get(level).remaining(level) / 20 + " 秒"), false);
                    return 1;
                })))
            .then(Commands.literal("camps").executes(context -> {
                var level = polar(context.getSource()); if (level == null) return 0;
                var data = HunterCampData.get(level);
                String[] directions = {"北", "东", "南", "西"};
                for (int i = 0; i < 4; i++) {
                    var pos = data.camp(i);
                    String message = directions[i] + "营地：" + (pos == null
                            ? (data.skipped(i) ? "已跳过（有限次数内未找到合适位置）" : "正在选址") : pos.toShortString());
                    context.getSource().sendSuccess(() -> Component.literal(message), false);
                }
                return 1;
            })));
    }
    private static int start(CommandSourceStack source, int ticks) {
        var level = polar(source); if (level == null) return 0;
        BlizzardData.get(level).start(level, ticks);
        level.players().forEach(PolarAdventureEvents::sync);
        source.sendSuccess(() -> Component.literal("暴雪已开始，持续 " + ticks / 20 + " 秒"), false);
        return 1;
    }
    private static ServerLevel polar(CommandSourceStack source) {
        if (source.getLevel().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) return source.getLevel();
        source.sendFailure(Component.literal("此命令只用于极地世界"));
        return null;
    }
}
