package com.akingno.winternightak.block.entity;

public final class TorchSettings {
    // One stick and one fat per newly crafted torch; reuse the shared fuel values.
    public static final int FUEL_TICKS = FuelSettings.STICK_FUEL_TICKS + FuelSettings.FAT_FUEL_TICKS;
    public static final double HEAT = 0.45;
    public static final double HEAT_RANGE = 3;
    private TorchSettings() {}
}
