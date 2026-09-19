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

/** Four small spawn-area camps, generated incrementally rather than scanning a world. */
public class HunterCampData extends SavedData {
    public static final String HUNTER_TAG = "WinterNightHunter";
    private BlockPos origin;
    private final BlockPos[] camps = new BlockPos[4];
    private final int[] attempts = new int[4];

    public static HunterCampData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(HunterCampData::load, HunterCampData::new, "winternightak_hunter_camps");
    }
    private static HunterCampData load(CompoundTag tag) {
        var data = new HunterCampData();
        if (tag.contains("Origin")) data.origin = BlockPos.of(tag.getLong("Origin"));
        for (int i = 0; i < 4; i++) {
            if (tag.contains("Camp" + i)) data.camps[i] = BlockPos.of(tag.getLong("Camp" + i));
            data.attempts[i] = tag.getInt("Attempt" + i);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        if (origin != null) tag.putLong("Origin", origin.asLong());
        for (int i = 0; i < 4; i++) {
            if (camps[i] != null) tag.putLong("Camp" + i, camps[i].asLong());
            tag.putInt("Attempt" + i, attempts[i]);
        }
        return tag;
    }
    public BlockPos camp(int index) { return camps[index]; }

    public void tick(ServerLevel level) {
        if (origin == null) { origin = level.getSharedSpawnPos().immutable(); setDirty(); }
        int slot = 0;
        while (slot < 4 && camps[slot] != null) slot++;
        if (slot == 4) return;

        int attempt = attempts[slot]++;
        setDirty();
        var random = RandomSource.create(level.getSeed() ^ (slot * 918273645L) ^ (attempt * 192837465L));
        double angle = Math.toRadians(slot * 90 + random.nextDouble() * 40 - 20);
        double distance = PolarAdventureSettings.CAMP_MIN_DISTANCE
                + random.nextDouble() * (PolarAdventureSettings.CAMP_MAX_DISTANCE - PolarAdventureSettings.CAMP_MIN_DISTANCE);
        int x = origin.getX() + (int) Math.round(Math.sin(angle) * distance);
        int z = origin.getZ() - (int) Math.round(Math.cos(angle) * distance);
        var generator = level.getChunkSource().getGenerator();
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        // Runtime placement validation only: five ground heights, no area-wide biome sampling.
        for (int[] offset : new int[][]{{0, 0}, {-4, -4}, {4, -4}, {-4, 4}, {4, 4}}) {
            int height = generator.getBaseHeight(x + offset[0], z + offset[1], Heightmap.Types.OCEAN_FLOOR_WG,
                    level, level.getChunkSource().randomState());
            min = Math.min(min, height); max = Math.max(max, height);
        }
        boolean iceFloe = max < 63 && attempt >= PolarAdventureSettings.CAMP_ATTEMPTS;
        if (!iceFloe && (min < 63 || max - min > 2)) return;
        BlockPos base = new BlockPos(x, iceFloe ? 62 : max - 1, z);
        // Reject ocean anywhere under the camp, including biome borders and fallback sites.
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            if (Math.abs(dx) + Math.abs(dz) > 6) continue;
            if (level.getBiome(base.offset(dx, 1, dz)).is(PolarWorldgen.OCEAN)) return;
        }
        // Do not replace a player's buildings when adding camps to an existing save.
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
            for (int depth = 0; depth < 3; depth++) {
                BlockPos support = floor.below(depth);
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
