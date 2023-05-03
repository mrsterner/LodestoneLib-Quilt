package com.sammy.lodestone.systems.particle.screen.base;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

public abstract class BillboardScreenParticle extends ScreenParticle {
	protected float quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
	protected BillboardScreenParticle(World pLevel, double pX, double pY) {
		super(pLevel, pX, pY);
	}

	protected BillboardScreenParticle(World pLevel, double pX, double pY, double pXSpeed, double pYSpeed) {
		super(pLevel, pX, pY, pXSpeed, pYSpeed);
	}

    @Override
    public void render(BufferBuilder bufferBuilder) {
		float partialTicks = MinecraftClient.getInstance().getTickDelta();
		float size = getQuadSize(partialTicks) * 10;
		float u0 = getMinU();
		float u1 = getMaxU();
		float v0 = getMinV();
		float v1 = getMaxV();
		float roll = MathHelper.lerp(partialTicks, this.prevAngle, this.angle);
		Vec3f[] vectors = new Vec3f[]{new Vec3f(-1.0F, -1.0F, 0.0F), new Vec3f(-1.0F, 1.0F, 0.0F), new Vec3f(1.0F, 1.0F, 0.0F), new Vec3f(1.0F, -1.0F, 0.0F)};
		Quaternion rotation = Vec3f.POSITIVE_Z.getDegreesQuaternion(roll);
		for (int i = 0; i < 4; ++i) {
			Vec3f vector3f = vectors[i];
			vector3f.rotate(rotation);
			vector3f.scale(size);
			vector3f.add((float) x, (float) y, 0);
		}
		float quadZ = getQuadZPosition();
		bufferBuilder.vertex(vectors[0].getX(), vectors[0].getY(), quadZ).uv(u1, v1).color(this.red, this.green, this.blue, this.alpha).next();
		bufferBuilder.vertex(vectors[1].getX(), vectors[1].getY(), quadZ).uv(u1, v0).color(this.red, this.green, this.blue, this.alpha).next();
		bufferBuilder.vertex(vectors[2].getX(), vectors[2].getY(), quadZ).uv(u0, v0).color(this.red, this.green, this.blue, this.alpha).next();
		bufferBuilder.vertex(vectors[3].getX(), vectors[3].getY(), quadZ).uv(u0, v1).color(this.red, this.green, this.blue, this.alpha).next();
    }

    public float getQuadSize(float tickDelta) {
        return this.quadSize;
    }

	public float getQuadZPosition() {
		return 390;
	}

    protected abstract float getMinU();

    protected abstract float getMaxU();

    protected abstract float getMinV();

    protected abstract float getMaxV();
}
