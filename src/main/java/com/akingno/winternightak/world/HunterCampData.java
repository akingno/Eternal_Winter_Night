package com.akingno.winternightak.world;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.block.entity.CampfireSettings;
import com.akingno.winternightak.block.entity.PolarCampfireBlockEntity;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;

/** 出生点四周的小营地：先定远处区块，再按已生成地表放置，海上允许硬冰平台。 */
public class HunterCampData extends SavedData {
    // 版本2取消海洋/坡度筛选；旧档仅重置尚未生成营地的失败次数，绝不重建已存在的营地。
    private static final int PLACEMENT_VERSION = 2;
    public static final String HUNTER_TAG = "WinterNightHunter";
    private BlockPos origin;
    private final BlockPos[] camps = new BlockPos[4];
    private final int[] attempts = new int[4];
    private int nextSlot;
    // 最多一个候选营地等待地形准备，不能在服务器tick内阻塞等待区块。
    private java.util.concurrent.CompletableFuture<Void> pendingTerrain;
    private BlockPos pendingBase;
    private int pendingSlot;
    private long pendingSince;

    public static HunterCampData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(HunterCampData::load, HunterCampData::new, "winternightak_hunter_camps");
    }
    private static HunterCampData load(CompoundTag tag) {
        var data = new HunterCampData();
        if (tag.contains("Origin")) data.origin = BlockPos.of(tag.getLong("Origin"));
        data.nextSlot = Math.floorMod(tag.getInt("NextSlot"), data.camps.length);
        for (int i = 0; i < 4; i++) {
            if (tag.contains("Camp" + i)) data.camps[i] = BlockPos.of(tag.getLong("Camp" + i));
            data.attempts[i] = tag.getInt("Attempt" + i);
            if (tag.getInt("PlacementVersion") < PLACEMENT_VERSION && data.camps[i] == null) data.attempts[i] = 0;
        }
        if (tag.getInt("PlacementVersion") < PLACEMENT_VERSION) data.setDirty();
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putInt("PlacementVersion", PLACEMENT_VERSION);
        if (origin != null) tag.putLong("Origin", origin.asLong());
        tag.putInt("NextSlot", nextSlot);
        for (int i = 0; i < 4; i++) {
            if (camps[i] != null) tag.putLong("Camp" + i, camps[i].asLong());
            tag.putInt("Attempt" + i, attempts[i]);
        }
        return tag;
    }
    public BlockPos camp(int index) { return camps[index]; }
    public boolean skipped(int index) { return camps[index] == null && attempts[index] >= PolarAdventureSettings.CAMP_ATTEMPTS; }

    public void tick(ServerLevel level) {
        if (pendingTerrain != null) {
            if (!pendingTerrain.isDone()) {
                // 地形准备超过30秒就跳过该方向，不继续追加同方向的加载请求。
                if (System.nanoTime() - pendingSince > 30_000_000_000L) {
                    attempts[pendingSlot] = PolarAdventureSettings.CAMP_ATTEMPTS;
                    pendingTerrain = null;
                    setDirty();
                }
                return;
            }
            var completed = pendingTerrain;
            pendingTerrain = null;
            if (!completed.isCompletedExceptionally()) finish(level, pendingBase, pendingSlot);
            return;
        }
        if (origin == null) { origin = level.getSharedSpawnPos().immutable(); setDirty(); }
        // 四个方向轮流放置；少量重试只处理建筑遮挡或区块准备失败，不再寻找特定群系。
        int slot = nextSlot;
        int checked = 0;
        while (checked < camps.length && (camps[slot] != null || skipped(slot))) {
            slot = (slot + 1) % camps.length;
            checked++;
        }
        if (checked == camps.length) return;
        nextSlot = (slot + 1) % camps.length;

        int attempt = attempts[slot]++;
        setDirty();
        var random = RandomSource.create(level.getSeed() ^ (slot * 918273645L) ^ (attempt * 192837465L));
        // 固定在该方向的象限内尝试；失败到上限即跳过，不再无限扩大范围。
        double halfAngle = 44;
        double angle = Math.toRadians(slot * 90 + (random.nextDouble() * 2 - 1) * halfAngle);
        double outerDistance = PolarAdventureSettings.CAMP_MAX_DISTANCE;
        double distance = PolarAdventureSettings.CAMP_MIN_DISTANCE
                + random.nextDouble() * (outerDistance - PolarAdventureSettings.CAMP_MIN_DISTANCE);
        int x = origin.getX() + (int) Math.round(Math.sin(angle) * distance);
        int z = origin.getZ() - (int) Math.round(Math.cos(angle) * distance);
        // 营地对齐目标区块中心，9×9占地完全位于一个区块；相对初始距离最多偏移约11格。
        // 原版奖励箱也在选定区块内找地表，但这里只放一个固定小平台，不随机遍历整片区域。
        var targetChunk = new net.minecraft.world.level.ChunkPos(new BlockPos(x, 0, z));
        x = targetChunk.getMiddleBlockX();
        z = targetChunk.getMiddleBlockZ();
        // 此处Y只是占位值；区块完成后直接读取现成高度图，不计算候选噪声或筛选海洋。
        BlockPos base = new BlockPos(x, PolarWorldgen.SEA_LEVEL, z);
        // 对齐后只需一个目标完整区块；下列循环保留占地检查，防止以后扩大模型却漏加载。
        var futures = new java.util.ArrayList<java.util.concurrent.CompletableFuture<?>>();
        var cache = level.getChunkSource();
        for (int cx = (x - 4) >> 4; cx <= (x + 4) >> 4; cx++) {
            for (int cz = (z - 4) >> 4; cz <= (z + 4) >> 4; cz++) {
                if (cache.getChunkNow(cx, cz) != null) continue;
                final int chunkX = cx, chunkZ = cz;
                // 原版getChunkFuture从主线程调用会同步等待，必须走其后台调度分支。
                futures.add(java.util.concurrent.CompletableFuture.supplyAsync(() -> cache.getChunkFuture(chunkX, chunkZ,
                                net.minecraft.world.level.chunk.ChunkStatus.FULL, true), net.minecraft.Util.backgroundExecutor())
                        .thenCompose(future -> future));
            }
        }
        if (futures.isEmpty()) { finish(level, base, slot); return; }
        pendingBase = base;
        pendingSlot = slot;
        pendingSince = System.nanoTime();
        pendingTerrain = java.util.concurrent.CompletableFuture.allOf(futures.toArray(java.util.concurrent.CompletableFuture[]::new));
    }

    private void finish(ServerLevel level, BlockPos base, int slot) {
        // 准备失败或已卸载时放弃本次，禁止下面的方块读取再触发同步加载。
        for (int cx = (base.getX() - 4) >> 4; cx <= (base.getX() + 4) >> 4; cx++)
            for (int cz = (base.getZ() - 4) >> 4; cz <= (base.getZ() + 4) >> 4; cz++)
                if (level.getChunkSource().getChunkNow(cx, cz) == null) return;
        // WORLD_SURFACE包含水面，因此海上营地落在海平面，不会沉到海床。
        // 取小屋占地最高地表，让平台跨过小起伏；读取的是已加载区块高度图，不生成或采样远处地形。
        int topY = level.getMinBuildHeight();
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            if (Math.abs(dx) + Math.abs(dz) > 6) continue;
            int px = base.getX() + dx, pz = base.getZ() + dz;
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, px, pz) - 1;
            if (level.getBlockState(new BlockPos(px, y, pz)).is(Blocks.SNOW)) y--;
            topY = Math.max(topY, y);
        }
        base = new BlockPos(base.getX(), topY, base.getZ());
        // 顶棚高4格；越过建造高度上限就跳过，避免写出世界边界。
        if (topY < level.getMinBuildHeight() + 2 || topY + 4 >= level.getMaxBuildHeight()) return;
        // 唯一的场地保护：不覆盖玩家建筑或其他结构，失败才消耗下一次有限重试。
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-4, -2, -4), base.offset(4, 4, 4))) {
            if (!replaceable(level.getBlockState(pos))) return;
        }
        build(level, base);
        camps[slot] = base.above();
        setDirty();
    }

    private static boolean replaceable(BlockState state) {
        return state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.WATER)
                || state.is(ModBlocks.HARD_ICE.get()) || state.is(ModBlocks.FROZEN_SOIL.get())
                || state.is(ModBlocks.PERMAFROST.get());
    }

    private static void build(ServerLevel level, BlockPos base) {
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            if (Math.abs(dx) + Math.abs(dz) > 6) continue;
            BlockPos floor = base.offset(dx, 0, dz);
            // 海上只垫3格厚平台；陆地起伏较低处延长支撑到该列地表，避免平台悬空。
            // WORLD_SURFACE包含水，绝不会为了支撑平台向深海海床填满整根冰柱。
            int bottom = Math.min(base.getY() - 2, level.getHeight(Heightmap.Types.WORLD_SURFACE, floor.getX(), floor.getZ()) - 1);
            for (int y = base.getY(); y >= bottom; y--) {
                BlockPos support = new BlockPos(floor.getX(), y, floor.getZ());
                var state = level.getBlockState(support);
                if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.SNOW))
                    level.setBlock(support, ModBlocks.HARD_ICE.get().defaultBlockState(), 3);
            }
            for (int dy = 1; dy <= 4; dy++) {
                BlockPos pos = floor.above(dy);
                if (level.getBlockState(pos).is(Blocks.SNOW)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            level.setBlock(floor.above(), Blocks.SNOW.defaultBlockState(), 3);
        }
        BlockPos fire = base.above();
        level.setBlock(fire, ModBlocks.CAMPFIRE.get().defaultBlockState().setValue(CampfireBlock.LIT, true), 3);
        if (level.getBlockEntity(fire) instanceof PolarCampfireBlockEntity be) be.addFuel(CampfireSettings.MAX_FUEL_TICKS);
        BlockPos barrel = base.offset(-2, 1, -2);
        level.setBlock(barrel, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.SOUTH), 3);
        for (int dx : new int[]{-3, 1}) for (int dz : new int[]{-3, -1}) for (int dy = 1; dy <= 3; dy++)
            level.setBlock(base.offset(dx, dy, dz), Blocks.SPRUCE_FENCE.defaultBlockState(), 3);
        for (int dx = -3; dx <= 1; dx++) for (int dz = -3; dz <= -1; dz++) {
            level.setBlock(base.offset(dx, 4, dz), ModBlocks.ICE_SLAB.get().defaultBlockState(), 3);
        }

        var hunter = EntityType.VILLAGER.create(level);
        if (hunter == null) throw new IllegalStateException("Unable to create hunter villager");
        hunter.moveTo(base.getX() - 0.5, base.getY() + 1, base.getZ() - 1.5, 0, 0);
        hunter.finalizeSpawn(level, level.getCurrentDifficultyAt(barrel), MobSpawnType.STRUCTURE, null, null);
        hunter.setVillagerData(hunter.getVillagerData().setType(VillagerType.SNOW).setProfession(VillagerProfession.FISHERMAN));
        hunter.getBrain().setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(level.dimension(), barrel));
        hunter.getPersistentData().putBoolean(HUNTER_TAG, true);
        hunter.setPersistenceRequired();
        // This step builds camps. Trade progression is connected when villages/compasses are implemented.
        hunter.setOffers(new MerchantOffers());
        level.addFreshEntity(hunter);
    }
}
