package com.akingno.winternightak.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** 从原版天气贴图截取十字雪花，随机叠加斜十字得到米字形；仅用于客户端显示。 */
public class BlizzardSnowParticle extends TextureSheetParticle {
    private final boolean star;
    private boolean diagonal;
    protected BlizzardSnowParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z);
        pickSprite(sprites);
        star = random.nextBoolean();
        xd = dx; yd = dy; zd = dz;
        // 重力0表示不再额外加速下落，friction=1保持给定风速不衰减。
        gravity = 0;
        friction = 1;
        // 粒子尺寸0.08～0.14，调高更大；寿命30～49tick约1.5～2.45秒，调高屏幕内粒子更多。
        quadSize = 0.08F + random.nextFloat() * 0.06F;
        lifetime = 30 + random.nextInt(20);
        hasPhysics = true;
    }
    // Read individual flakes from vanilla's 64x256 weather sheet without copying textures.
    // The plus and diagonal cross share a 5x5 footprint; overlaying them makes an eight-ray flake.
    // 下列坐标是原版64×256贴图内的5×5雪花裁切，不是平衡参数；除4/16换算到图集0～16坐标。
    @Override protected float getU0() { return sprite.getU((diagonal ? 54 : 22) / 4.0); }
    @Override protected float getU1() { return sprite.getU((diagonal ? 59 : 27) / 4.0); }
    @Override protected float getV0() { return sprite.getV((diagonal ? 14 : 18) / 16.0); }
    @Override protected float getV1() { return sprite.getV((diagonal ? 19 : 23) / 16.0); }
    @Override public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        super.render(buffer, camera, partialTick);
        if (star) {
            diagonal = true;
            super.render(buffer, camera, partialTick);
            diagonal = false;
        }
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_OPAQUE; }
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new BlizzardSnowParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
