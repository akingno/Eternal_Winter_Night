package com.akingno.winternightak.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import java.util.Optional;

/** 原版式延迟拼接雪村：起点群系由Structure检查，生成布局时只追加去树和道路材质处理。 */
public final class PolarVillageStructure extends Structure {
    public static final Codec<PolarVillageStructure> CODEC = simpleCodec(PolarVillageStructure::new);

    public PolarVillageStructure(StructureSettings settings) { super(settings); }

    @Override public StructureType<?> type() { return PolarVillages.TYPE.get(); }

    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int x = context.chunkPos().getMinBlockX();
        int z = context.chunkPos().getMinBlockZ();
        var pool = context.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL)
                .getHolderOrThrow(PolarVillages.START_POOL);
        // 起始Y为0，再投影到真实地表；深度、半径及扩展选项与原版雪村一致。
        // meeting point只是入口；它的拼接连接会继续选取原版道路和房屋，不需要手动列出所有房屋。
        var candidate = JigsawPlacement.addPieces(context, pool, Optional.empty(), PolarVillageSettings.SIZE,
                new BlockPos(x, 0, z), PolarVillageSettings.USE_EXPANSION_HACK,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG), PolarVillageSettings.MAX_RADIUS);
        // 与原版一样只返回延迟任务。父类findValidGenerationPoint负责检查起点是否属于ice_plain。
        // 绝不能在这里调用getPiecesBuilder：定位预检查不需要展开全部道路、房屋或扫描边界。
        return candidate.map(stub -> new GenerationStub(stub.position(), builder ->
                customizePieces(context, stub.getPiecesBuilder(), builder)));
    }

    /** 只有原版真正要求结构布局时执行一次；没有住宅数量门槛、布局重试或越界过滤。 */
    private static void customizePieces(GenerationContext context, StructurePiecesBuilder raw, StructurePiecesBuilder builder) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, context.registryAccess());
        for (var piece : raw.build().pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) {
                builder.addPiece(piece);
                continue;
            }
            // 通过正式序列化字段识别模板，不依赖调试字符串，也不修改原版模板池。
            var data = StructurePoolElement.CODEC.encodeStart(ops, poolPiece.getElement()).result().orElseThrow().getAsJsonObject();
            if (data.has("feature") && data.get("feature").isJsonPrimitive()
                    && data.get("feature").getAsString().equals("minecraft:spruce")) continue;
            if (data.has("location")) {
                // 保留房屋结构、实体、原有处理器和地形投影，仅追加极地土径处理。
                data.addProperty("element_type", "winternightak:village_template");
                var element = StructurePoolElement.CODEC.parse(ops, data).result().orElseThrow();
                var replacement = new PoolElementStructurePiece(context.structureTemplateManager(), element,
                        poolPiece.getPosition(), poolPiece.getGroundLevelDelta(), poolPiece.getRotation(), poolPiece.getBoundingBox());
                poolPiece.getJunctions().forEach(replacement::addJunction);
                builder.addPiece(replacement);
            } else builder.addPiece(piece);
        }
    }
}
