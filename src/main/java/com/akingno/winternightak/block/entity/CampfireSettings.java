package com.akingno.winternightak.block.entity;

/** 营火平衡参数。时间使用tick（正常20tick/秒）；热量为Cold Sweat MC单位，不是摄氏度。 */
public final class CampfireSettings {
    // 72000tick=60分钟容量，调高可一次加入更多燃料；单份时长仍读FuelSettings。
    public static final int MAX_FUEL_TICKS = 72000;
    // 单个营火的近距离增温，调高更暖；距离、墙体和共享上限仍然生效。
    public static final double HEAT = 0.9;
    // 三种设备合计增温最多1.7；调高允许堆叠更暖，不是最终环境温度上限。
    public static final double MAX_HEAT = 1.7;
    // 供暖范围（格）；调高更远，实际仍受Cold Sweat全局方块扫描范围约束。
    public static final double HEAT_RANGE = 8;
    private CampfireSettings() {}
}
