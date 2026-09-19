package com.akingno.winternightak.world;
import com.akingno.winternightak.world.gen.PolarVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;

/** 与原版LocateCommand相同的搜索接口；不再注册命令覆盖原版locate。 */
public final class PolarVillageLocator {
    // 原版locate的100圈上限，不是100格；调大可能搜索更远，也增加同步等待。
    private static final int SEARCH_RADIUS = 100;
    public static BlockPos find(ServerLevel level, BlockPos origin) {
        var village = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(PolarVillages.VILLAGE);
        // false表示已发现村庄也能成为目标，与locate一致。
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(
                level, HolderSet.direct(village), origin, SEARCH_RADIUS, false);
        return found == null ? null : found.getFirst();
    }
    private PolarVillageLocator() {}
}
