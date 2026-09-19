package com.akingno.winternightak.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class BlizzardData extends SavedData {
    private long nextCheck = -1;
    private long endTime;
    public static BlizzardData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlizzardData::load, BlizzardData::new, "winternightak_blizzard");
    }
    private static BlizzardData load(CompoundTag tag) {
        var data = new BlizzardData();
        data.nextCheck = tag.getLong("NextCheck");
        data.endTime = tag.getLong("EndTime");
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLong("NextCheck", nextCheck);
        tag.putLong("EndTime", endTime);
        return tag;
    }
    public boolean active(ServerLevel level) { return endTime > level.getGameTime(); }
    public long remaining(ServerLevel level) { return Math.max(0, endTime - level.getGameTime()); }
    public void start(ServerLevel level, int ticks) { endTime = level.getGameTime() + ticks; setDirty(); }
    public void stop() { endTime = 0; setDirty(); }
    public void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (nextCheck < 0) {
            nextCheck = now + PolarAdventureSettings.BLIZZARD_CHECK_TICKS;
            setDirty();
        } else if (now >= nextCheck) {
            nextCheck = now + PolarAdventureSettings.BLIZZARD_CHECK_TICKS;
            if (!active(level) && level.random.nextDouble() < PolarAdventureSettings.BLIZZARD_CHANCE)
                start(level, PolarAdventureSettings.BLIZZARD_DURATION_TICKS);
            setDirty();
        }
    }
}
