package com.akingno.winternightak.block.entity;

/** 火把初始燃料和供暖参数；范围以格计，热量以Cold Sweat MC单位计。 */
public final class TorchSettings {
    // 容量=一根木棍+一份油脂的时长；新火把初始为空，村庄生成时填满此容量。
    public static final int FUEL_TICKS = FuelSettings.STICK_FUEL_TICKS + FuelSettings.FAT_FUEL_TICKS;
    // 0.45比油灯0.35略暖；调高更暖，但仍受三设备共享上限限制。
    public static final double HEAT = 0.45;
    // 供暖半径3格，调高更远；手持时不计温，不使用此参数。
    public static final double HEAT_RANGE = 3;
    private TorchSettings() {}
}
