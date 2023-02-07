package com.sammy.lodestone.systems.particle.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sammy.lodestone.config.ClientConfig;
import com.sammy.lodestone.handlers.RenderHandler;
import com.sammy.lodestone.helpers.RenderHelper;
import com.sammy.lodestone.setup.LodestoneRenderLayers;
import com.sammy.lodestone.systems.particle.data.ColorParticleData;
import com.sammy.lodestone.systems.particle.data.GenericParticleData;
import com.sammy.lodestone.systems.particle.data.SpinParticleData;
import com.sammy.lodestone.systems.particle.SimpleParticleEffect;
import net.fabricmc.fabric.impl.client.particle.FabricSpriteProviderImpl;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.function.Consumer;

import static com.sammy.lodestone.systems.particle.SimpleParticleEffect.ParticleSpritePicker.*;

public class GenericParticle extends SpriteBillboardParticle {
	private final ParticleTextureSheet renderType;
	protected final FabricSpriteProviderImpl spriteSet;
	protected final SimpleParticleEffect.ParticleSpritePicker spritePicker;
	protected final SimpleParticleEffect.ParticleDiscardFunctionType discardFunctionType;
	protected final ColorParticleData colorData;
	protected final GenericParticleData transparencyData;
	protected final GenericParticleData scaleData;
	protected final SpinParticleData spinData;
	protected final Consumer<GenericParticle> actor;

	private boolean reachedPositiveAlpha;
	private boolean reachedPositiveScale;

	float[] hsv1 = new float[3], hsv2 = new float[3];

	public GenericParticle(ClientWorld world, WorldParticleEffect options, FabricSpriteProviderImpl spriteSet, double x, double y, double z, double xd, double yd, double zd) {
		super(world, x, y, z);
		this.renderType = options.textureSheet == null ? ParticleTextureSheet.PARTICLE_SHEET_OPAQUE : options.textureSheet;
		this.spriteSet = spriteSet;

		this.spritePicker = options.spritePicker;
		this.discardFunctionType = options.discardFunctionType;
		this.colorData = options.colorData;
		this.transparencyData = options.transparencyData;
		this.scaleData = options.scaleData;
		this.spinData = options.spinData;
		this.actor = options.actor;
		this.angle = options.spinData.spinOffset + options.spinData.startingValue;
		this.velocityX = xd;
		this.velocityY = yd;
		this.velocityZ = zd;
		this.setMaxAge(options.lifetime);
		this.gravityStrength = options.gravity;
		this.collidesWithWorld = !options.noClip;
		this.velocityMultiplier = 1;
		Color.RGBtoHSB((int) (255 * Math.min(1.0f, colorData.r1)), (int) (255 * Math.min(1.0f, colorData.g1)), (int) (255 * Math.min(1.0f, colorData.b1)), hsv1);
		Color.RGBtoHSB((int) (255 * Math.min(1.0f, colorData.r2)), (int) (255 * Math.min(1.0f, colorData.g2)), (int) (255 * Math.min(1.0f, colorData.b2)), hsv2);

		if (spriteSet != null) {
			if (getSpritePicker().equals(RANDOM_SPRITE)) {
				setSprite(spriteSet);
			}
			if (getSpritePicker().equals(FIRST_INDEX) || getSpritePicker().equals(WITH_AGE)) {
				setSprite(0);
			}
			if (getSpritePicker().equals(LAST_INDEX)) {
				setSprite(spriteSet.getSprites().size() - 1);
			}
		}
		updateTraits();
	}

	@Override
	protected int getBrightness(float pPartialTick) {
		return RenderHelper.FULL_BRIGHT;
	}

	@Override
	public void tick() {
		updateTraits();
		if (spriteSet != null) {
			if (getSpritePicker().equals(WITH_AGE)) {
				setSpriteForAge(spriteSet);
			}
		}
		super.tick();
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		VertexConsumer consumer = vertexConsumer;
		if (ClientConfig.DELAYED_PARTICLE_RENDERING) {
			if (getType().equals(LodestoneWorldParticleTextureSheet.ADDITIVE)) {
				consumer = RenderHandler.DELAYED_RENDER.getBuffer(LodestoneRenderLayers.ADDITIVE_PARTICLE);
			}
			if (getType().equals(LodestoneWorldParticleTextureSheet.TRANSPARENT)) {
				consumer = RenderHandler.DELAYED_RENDER.getBuffer(LodestoneRenderLayers.TRANSPARENT_PARTICLE);
			}
		}
		super.buildGeometry(consumer, camera, tickDelta);
	}

	@Override
	public ParticleTextureSheet getType() {
		return renderType;
	}

	public SimpleParticleEffect.ParticleSpritePicker getSpritePicker() {
		return spritePicker;
	}

	public void setSprite(int spriteIndex) {
		if (spriteIndex < spriteSet.getSprites().size() && spriteIndex >= 0) {
			setSprite(spriteSet.getSprites().get(spriteIndex));
		}
	}

	public void pickColor(float colorCoeff) {
		float h = MathHelper.lerpAngleDegrees(colorCoeff, 360f * hsv1[0], 360f * hsv2[0]) / 360f;
		float s = MathHelper.lerp(colorCoeff, hsv1[1], hsv2[1]);
		float v = MathHelper.lerp(colorCoeff, hsv1[2], hsv2[2]);
		int packed = Color.HSBtoRGB(h, s, v);
		float r = ColorUtil.ARGB32.getRed(packed) / 255.0f;
		float g = ColorUtil.ARGB32.getGreen(packed) / 255.0f;
		float b = ColorUtil.ARGB32.getBlue(packed) / 255.0f;
		setColor(r, g, b);
	}

	protected void updateTraits() {
		boolean shouldAttemptRemoval = discardFunctionType == SimpleParticleEffect.ParticleDiscardFunctionType.INVISIBLE;
		if (discardFunctionType == SimpleParticleEffect.ParticleDiscardFunctionType.ENDING_CURVE_INVISIBLE) {

			if (scaleData.getProgress(age, maxAge) > 0.5f || transparencyData.getProgress(age, maxAge) > 0.5f) {
				shouldAttemptRemoval = true;
			}
		}
		if (shouldAttemptRemoval) {
			if ((reachedPositiveAlpha && colorAlpha <= 0) || (reachedPositiveScale && scale <= 0)) {
				markDead();
				return;
			}
		}

		if (!reachedPositiveAlpha && colorAlpha > 0) {
			reachedPositiveAlpha = true;
		}
		if (!reachedPositiveScale && scale > 0) {
			reachedPositiveScale = true;
		}
		pickColor(colorData.colorCurveEasing.ease(colorData.getProgress(age, maxAge), 0, 1, 1));

		scale = scaleData.getValue(age, maxAge);
		colorAlpha = transparencyData.getValue(age, maxAge);
		prevAngle = angle;
		angle += spinData.getValue(age, maxAge);

		if (actor != null) {
			actor.accept(this);
		}
	}

	public Vec3d getVelocity() {
		return new Vec3d(velocityX, velocityY, velocityZ);
	}

	public void setVelocity(Vec3d speed) {
		setVelocity(speed.x, speed.y, speed.z);
	}
}
