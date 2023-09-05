package com.sammy.lodestone.handlers;

import com.sammy.lodestone.config.ClientConfig;
import com.sammy.lodestone.systems.screenshake.ScreenshakeInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
public class ScreenshakeHandler {
	private static final PerlinNoiseSampler sampler = new PerlinNoiseSampler(Random.create());
	public static final ArrayList<ScreenshakeInstance> INSTANCES = new ArrayList<>();
	public static float intensity;
	public static float yawOffset;
	public static float pitchOffset;

	public static void cameraTick(Camera camera, Random random) {
		if (intensity >= 0.1) {
			yawOffset = randomizeOffset(random);
			pitchOffset = randomizeOffset(random);
			camera.setRotation(camera.getYaw() + yawOffset, camera.getPitch() + pitchOffset);
		}
	}

	public static void clientTick(Camera camera, Random random) {
		double sum = Math.min(INSTANCES.stream().mapToDouble(i1 -> i1.updateIntensity(camera, random)).sum(), ClientConfig.SCREENSHAKE_INTENSITY);

		intensity = (float) Math.pow(sum, 3);
		INSTANCES.removeIf(i -> i.progress >= i.duration);
	}

	public static void addScreenshake(ScreenshakeInstance instance) {
		INSTANCES.add(instance);
	}

	public static float randomizeOffset(Random random) {
		return MathHelper.nextFloat(random, -intensity * 2, intensity * 2);
	}
}
