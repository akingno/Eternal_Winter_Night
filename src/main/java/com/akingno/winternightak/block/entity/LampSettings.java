package com.akingno.winternightak.block.entity;

/** 油灯容量、亮度和供暖参数；区块正常运行时每秒20tick，卸载期间不扣燃料。 */
public final class LampSettings {
    // 最多储存36000tick=30分钟燃料；调高容量更大，不改变单份燃料时长。
    public static final int MAX_FUEL_TICKS = 36000;
    // 原版亮度0～15，调高照得更亮；不直接改变Cold Sweat供暖。
    public static final int LIGHT_LEVEL = 9;
    // 单盏近距离增温，单位为Cold Sweat MC温度而非摄氏度；调高更暖。
    public static final double HEAT = 0.35;
    // 衰减到0的距离（格）；调高覆盖更远，必须大于共享衰减公式的0.5格。
    public static final double HEAT_RANGE = 3;

    private LampSettings() {}
}
