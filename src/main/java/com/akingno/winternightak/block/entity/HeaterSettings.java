package com.akingno.winternightak.block.entity;

/** 初版加热器平衡参数，热量为Cold Sweat单位，不是摄氏度。 */
public final class HeaterSettings {
    // 半径4格的球共257个方块；调高范围会按体积增加检查量。
    public static final int SOIL_RADIUS = 4;
    // 每20tick一次，即正常TPS下1秒；调大更省计算，解冻反应更慢。
    public static final int CHECK_TICKS = 20;
    // 单台近距离增温高于营火；调高更暖。
    public static final double HEAT = 1.8;
    public static final double HEAT_RANGE = 8;
    // 四种热源共用2.5总上限；调高允许更多叠加，并取代旧的营火账户上限。
    public static final double MAX_HEAT = 2.5;
    private HeaterSettings() {}
}
