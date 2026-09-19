package com.akingno.winternightak.world;

public final class PolarAdventureSettings {
    // 48000tick为两个游戏日；调低更频繁抽签，调高间隔更长，不受视觉永夜影响。
    public static final int BLIZZARD_CHECK_TICKS = 48000;
    // 每次检查20%触发，调高更容易下暴雪，不是每tick的概率。
    public static final double BLIZZARD_CHANCE = 0.20;
    // 正常20tick/秒，3600为3分钟；调高单次暴雪更长。
    public static final int BLIZZARD_DURATION_TICKS = 3600;
    // 雾的远端距离（格）；调低视野更差，调高能看得更远。
    public static final float BLIZZARD_FOG_END = 20;
    // Cold Sweat MC温度单位；调高暴露在暴雪中的额外降温更强。
    public static final double BLIZZARD_COOLING = 0.25;
    // 营地距出生点的内外半径（格）；调高整体更远，增大两者差值会扩大可选范围。
    public static final int CAMP_MIN_DISTANCE = 350;
    public static final int CAMP_MAX_DISTANCE = 650;
    // 每方向最多3次，主要避让已有建筑/处理加载失败；不再因海洋或坡度反复搜索。调高重试机会更多。
    public static final int CAMP_ATTEMPTS = 3;
    private PolarAdventureSettings() {}
}
