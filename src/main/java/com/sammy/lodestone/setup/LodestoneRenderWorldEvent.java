package com.sammy.lodestone.setup;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class LodestoneRenderWorldEvent {


	public static final Event<AfterParticles> AFTER_PARTICLES = EventFactory.createArrayBacked(AfterParticles.class, callbacks -> (mat, proj) -> {
		for (final AfterParticles callback : callbacks) {
			callback.afterParticles(mat, proj);
		}
	});

	public static final Event<AfterWeather> AFTER_WEATHER = EventFactory.createArrayBacked(AfterWeather.class, callbacks -> (mat, proj, worldRenderer) -> {
		for (final AfterWeather callback : callbacks) {
			callback.afterWeather(mat, proj, worldRenderer);
		}
	});

	@FunctionalInterface
	public interface AfterParticles {
		void afterParticles(MatrixStack matrices, Matrix4f projectionMatrix);
	}

	@FunctionalInterface
	public interface AfterWeather {
		void afterWeather(MatrixStack matrices, Matrix4f projectionMatrix, WorldRenderer worldRenderer);
	}
}
