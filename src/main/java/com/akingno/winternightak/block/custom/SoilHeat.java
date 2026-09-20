package com.akingno.winternightak.block.custom;

import com.akingno.winternightak.block.ModBlocks;
import com.akingno.winternightak.block.entity.HeaterSettings;
import com.akingno.winternightak.world.gen.PolarWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import java.util.*;

/** 加热器与土地共用球形范围；不保存永久热源缓存，拆掉设备后不会遗留供热。 */
public final class SoilHeat {
    public static final List<BlockPos> OFFSETS;
    static {
        var offsets = new ArrayList<BlockPos>();
        int r = HeaterSettings.SOIL_RADIUS;
        for (int x=-r; x<=r; x++) for (int y=-r; y<=r; y++) for (int z=-r; z<=r; z++)
            if (x*x+y*y+z*z <= r*r) offsets.add(new BlockPos(x,y,z));
        OFFSETS = List.copyOf(offsets);
    }
    public static boolean freezeIfNeeded(Level level, BlockPos pos) {
        if (!level.dimensionTypeRegistration().is(PolarWorldgen.DIMENSION_TYPE)) return false;
        if (!level.canSeeSky(pos.above())) {
            boolean unknown = false;
            for (BlockPos offset : OFFSETS) {
                BlockPos heater = pos.offset(offset);
                // 边界未加载时延后判定，避免把未加载的邻区块误当作没有热源。
                if (!level.hasChunkAt(heater)) { unknown = true; continue; }
                if (level.getBlockState(heater).is(ModBlocks.HEATER.get()) && AdjacentRedstoneBlock.powered(level, heater)) return false;
            }
            if (unknown) return false;
        }
        replace(level, pos, ModBlocks.FROZEN_SOIL.get());
        return true;
    }
    public static void replace(Level level, BlockPos pos, Block block) {
        var state = Block.pushEntitiesUp(level.getBlockState(pos), block.defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, state);
    }
    private SoilHeat() {}
}
