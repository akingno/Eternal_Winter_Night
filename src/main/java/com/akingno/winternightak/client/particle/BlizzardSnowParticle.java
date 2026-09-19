package com.akingno.winternightak.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class BlizzardSnowParticle extends TextureSheetParticle {
    private final boolean star;
    private boolean diagonal;
    protected BlizzardSnowParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z);
        pickSprite(sprites);
        star = random.nextBoolean();
        xd = dx; yd = dy; zd = dz;
        gravity = 0;
        friction = 1;
        quadSize = 0.08F + random.nextFloat() * 0.06F;
        lifetime = 30 + random.nextInt(20);
        hasPhysics = true;
    }
    // Read individual flakes from vanilla's 64x256 weather sheet without copying textures.
    // The plus and diagonal cross share a 5x5 footprint; overlaying them makes an eight-ray flake.
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
