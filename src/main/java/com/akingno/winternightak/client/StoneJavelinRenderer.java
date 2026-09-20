package com.akingno.winternightak.client;

import com.akingno.winternightak.WinterNight;
import com.akingno.winternightak.entity.StoneJavelinEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** 使用原版三叉戟几何模型，本地独立贴图方便以后替换。 */
public final class StoneJavelinRenderer extends EntityRenderer<StoneJavelinEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WinterNight.MOD_ID, "textures/entity/stone_javelin.png");
    private final TridentModel model;
    public StoneJavelinRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new TridentModel(context.bakeLayer(ModelLayers.TRIDENT));
    }
    @Override public void render(StoneJavelinEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        // 原版三叉戟模型轴向修正：偏航-90度、俯仰+90度，让尖端沿飞行方向。
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90));
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(StoneJavelinEntity entity) { return TEXTURE; }
}
