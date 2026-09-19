package com.akingno.winternightak.world.gen;

/** 雪村参数集中在此。改动后需要重新运行runData更新生成间距等数据，旧区块不会补生成。 */
public final class PolarVillageSettings {
    // 候选位置通过概率，不是每座岛的概率。调高村庄更多，调低更稀少；只要求起点群系为冰原。
    public static final float FREQUENCY = 0.20F;
    // 单位为区块，每区块16格：每16×16区块区域选一个候选位置。调小更密，调大更稀。
    public static final int SPACING = 16;
    // 候选区块之间的排布间隔参数，须小于SPACING；调高更均匀、相邻候选更难靠近。
    public static final int SEPARATION = 6;
    // 随机盐只改变同一种子下候选位置，不控制多少；不要为了调密度修改它。
    public static final int SALT = 19384721;
    // 原版雪村拼接深度6，不是房屋数量；调低容易只展开较少道路/建筑，调高布局和生成开销增大。
    public static final int SIZE = 6;
    // 中心到拼接边界的最大距离（格）；调高可展开更远，调低更紧凑但可能少房屋。
    public static final int MAX_RADIUS = 80;
    // 原版雪村开启此项，为拼接时的内部连接留出扩展空间；关闭可能改变组件容纳结果，不是密度参数。
    public static final boolean USE_EXPANSION_HACK = true;
    private PolarVillageSettings() {}
}
