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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
public final class PolarAdventureEvents {
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (var level : event.getServer().getAllLevels()) {
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
                    String message = directions[i] + "营地：" + (pos == null ? "正在选址" : pos.toShortString());
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
