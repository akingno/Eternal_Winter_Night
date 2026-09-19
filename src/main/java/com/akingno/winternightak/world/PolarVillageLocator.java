package com.akingno.winternightak.world;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.world.gen.PolarVillages;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** 将本模组雪村定位拆成逐tick任务，不让原版同步locate长期占住服务器主线程。 */
@Mod.EventBusSubscriber(modid = WinterNight.MOD_ID)
public final class PolarVillageLocator {
    // 最多检查中心外12圈候选区域；间距16区块时覆盖约3千格。调高更远，但耗时和读盘更多。
    private static final int SEARCH_RINGS = 12;
    // 每tick最多筛8个便宜的候选；最多只保留1个区块加载请求，不会一下排队生成大量区块。
    private static final int CANDIDATES_PER_TICK = 8;
    // 60秒后停止搜索并反馈失败，避免无结果时一直运行；调高允许等更慢的磁盘或更远的搜索。
    private static final long TIMEOUT_NANOS = 60_000_000_000L;
    // 每台服务器只允许一个定位任务，避免多人重复请求叠加负载；关服时清空。
    private static final Map<MinecraftServer, Search> SEARCHES = new HashMap<>();

    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        // 同名literal优先于原版通用结构参数，仅替换这个结构ID的执行方式；其他locate保持原样。
        event.getDispatcher().register(Commands.literal("locate").requires(source -> source.hasPermission(2))
                .then(Commands.literal("structure").then(Commands.literal("winternightak:snowy_village")
                        .executes(context -> start(context.getSource())))));
    }

    private static int start(CommandSourceStack source) {
        if (!source.getLevel().dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) {
            source.sendFailure(Component.literal("此雪村搜索只用于极地世界"));
            return 0;
        }
        if (SEARCHES.containsKey(source.getServer())) {
            source.sendFailure(Component.literal("已有村庄搜索正在运行，请等待结果或60秒超时"));
            return 0;
        }
        var registry = source.getLevel().registryAccess();
        var set = registry.registryOrThrow(Registries.STRUCTURE_SET).getOrThrow(PolarVillages.SET);
        if (!(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            source.sendFailure(Component.literal("村庄放置配置不是随机分布，无法使用分批搜索"));
            return 0;
        }
        SEARCHES.put(source.getServer(), new Search(source, placement,
                registry.registryOrThrow(Registries.STRUCTURE).getOrThrow(PolarVillages.VILLAGE)));
        source.sendSuccess(() -> Component.literal("开始分批搜索雪村（约3千格范围，最多60秒）；期间可以继续操作和保存"), false);
        return 1;
    }

    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var search = SEARCHES.get(event.getServer());
        if (search == null) return;
        try {
            if (search.tick()) SEARCHES.remove(event.getServer());
        } catch (RuntimeException error) {
            SEARCHES.remove(event.getServer());
            LogUtils.getLogger().error("Polar village search failed", error);
            search.source.sendFailure(Component.literal("村庄搜索失败，详细原因已记录到日志"));
        }
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { SEARCHES.remove(event.getServer()); }

    private static final class Search {
        final CommandSourceStack source;
        final RandomSpreadStructurePlacement placement;
        final Structure village;
        final List<ChunkPos> regions = new ArrayList<>();
        final long started = System.nanoTime();
        int next;
        CompletableFuture<ChunkAccess> pending;

        Search(CommandSourceStack source, RandomSpreadStructurePlacement placement, Structure village) {
            this.source = source;
            this.placement = placement;
            this.village = village;
            var center = new ChunkPos(net.minecraft.core.BlockPos.containing(source.getPosition()));
            int rx = Math.floorDiv(center.x, placement.spacing());
            int rz = Math.floorDiv(center.z, placement.spacing());
            // 从近圈向外搜；返回第一座有效村庄，不承诺严格的欧氏距离最近。
            for (int ring = 0; ring <= SEARCH_RINGS; ring++)
                for (int dx = -ring; dx <= ring; dx++) for (int dz = -ring; dz <= ring; dz++)
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) regions.add(new ChunkPos(rx + dx, rz + dz));
        }

        boolean tick() {
            if (System.nanoTime() - started >= TIMEOUT_NANOS) {
                source.sendFailure(Component.literal("村庄搜索已达到时间上限，可以换个位置再搜索"));
                return true;
            }
            if (pending != null) {
                // 只取已经完成的结果，主线程绝不join或等待未完成的区块任务。
                if (!pending.isDone()) return false;
                var chunk = pending.getNow(null);
                pending = null;
                var start = chunk == null ? null : chunk.getStartForStructure(village);
                if (start != null && start.isValid()) {
                    var pos = start.getBoundingBox().getCenter();
                    source.sendSuccess(() -> Component.literal("找到雪村：" + pos.toShortString()), false);
                    return true;
                }
            }
            var level = source.getLevel();
            var cache = level.getChunkSource();
            for (int checked = 0; checked < CANDIDATES_PER_TICK && next < regions.size(); checked++) {
                var region = regions.get(next++);
                var pos = placement.getPotentialStructureChunk(level.getSeed(), region.x * placement.spacing(), region.z * placement.spacing());
                if (!placement.isStructureChunk(cache.getGeneratorState(), pos.x, pos.z)) continue;
                // 先做廉价的群系筛选，海洋和冰盖不请求区块生成。
                if (!cache.getGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(pos.getMinBlockX()),
                        QuartPos.fromBlock(PolarWorldgen.SEA_LEVEL), QuartPos.fromBlock(pos.getMinBlockZ()),
                        cache.randomState().sampler()).is(PolarWorldgen.ICE_PLAIN)) continue;
                // getChunkFuture在主线程调用仍会managedBlock；从后台调用其官方异步分支，
                // 实际世界操作仍由原版安排在线程正确的生成流程中。只到STRUCTURE_STARTS，不生成地表和生物。
                pending = CompletableFuture.supplyAsync(() -> cache.getChunkFuture(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS, true),
                        Util.backgroundExecutor()).thenCompose(future -> future).thenApply(result -> result.left().orElse(null));
                return false;
            }
            if (next == regions.size()) {
                source.sendFailure(Component.literal("本次范围内未找到雪村，请换个位置再搜索"));
                return true;
            }
            return false;
        }
    }
}
