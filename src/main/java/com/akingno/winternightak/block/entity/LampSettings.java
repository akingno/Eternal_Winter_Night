package com.akingno.winternightak.block.entity;

/** Initial balance values: ticks are 1/20 second while the lamp's chunk is ticking. */
public final class LampSettings {
    public static final int MAX_FUEL_TICKS = 36000;
    public static final int LIGHT_LEVEL = 9;
    public static final double HEAT = 0.35;
    public static final double HEAT_RANGE = 3;

    private LampSettings() {}
}
