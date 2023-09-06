package com.sammy.lodestone.handlers.screenparticle;


import com.mojang.datafixers.util.Pair;
import com.sammy.lodestone.config.ClientConfig;
import com.sammy.lodestone.systems.particle.screen.*;
import com.sammy.lodestone.systems.particle.screen.base.ScreenParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ScreenParticleHandler {
	/**
	 * Earliest Screen Particles are rendered before nearly every piece of user interface.
	 */
	public static final ScreenParticleHolder EARLIEST_PARTICLES = new ScreenParticleHolder();

	/**
	 * Early Screen Particles are rendered after other UI elements, but before things like tooltips or other overlays.
	 */
	public static final ScreenParticleHolder EARLY_PARTICLES = new ScreenParticleHolder();

	/**
	 * Late Screen Particles are rendered after everything else.
	 */
	public static final ScreenParticleHolder LATE_PARTICLES = new ScreenParticleHolder();

	/**
	 * Item Stack Bound Particles are rendered just after an item stack in the inventory. They are ticked the same as other particles.
	 */
	public static final Map<ScreenParticleItemStackKey, ScreenParticleHolder> ITEM_PARTICLES = new HashMap<>();
	public static final Map<ScreenParticleItemStackRetrievalKey, ItemStack> ITEM_STACK_CACHE = new HashMap<>();
	public static final Collection<ScreenParticleItemStackRetrievalKey> ACTIVELY_ACCESSED_KEYS = new ArrayList<>();

	public static ScreenParticleHolder cachedItemParticles = null;
	public static int currentItemX, currentItemY;

	public static final Tessellator TESSELATOR = new Tessellator();
	public static boolean canSpawnParticles;

	public static boolean renderingHotbar;


	public static void tickParticles() {
		if (!ClientConfig.ENABLE_SCREEN_PARTICLES) {
			return;
		}
		EARLIEST_PARTICLES.tick();
		EARLY_PARTICLES.tick();
		LATE_PARTICLES.tick();

		ITEM_PARTICLES.values().forEach(ScreenParticleHolder::tick);
		ITEM_PARTICLES.values().removeIf(ScreenParticleHolder::isEmpty);

		ITEM_STACK_CACHE.keySet().removeIf(k -> !ACTIVELY_ACCESSED_KEYS.contains(k));
		ACTIVELY_ACCESSED_KEYS.clear();
		canSpawnParticles = true;
	}

	public static void renderItemStackEarly(ItemStack stack) {
		if (!ClientConfig.ENABLE_SCREEN_PARTICLES) {
			return;
		}
		MinecraftClient minecraft = MinecraftClient.getInstance();
		if (minecraft.world != null && minecraft.player != null) {
			if (minecraft.isPaused()) {
				return;
			}
			if (!stack.isEmpty()) {
				ParticleEmitterHandler.ItemParticleSupplier emitter = ParticleEmitterHandler.EMITTERS.get(stack.getItem());
				if (emitter != null) {
					renderParticles(spawnAndPullParticles(minecraft.world, emitter, stack, false));
					cachedItemParticles = spawnAndPullParticles(minecraft.world, emitter, stack, true);
				}
			}
		}
	}

	public static ScreenParticleHolder spawnAndPullParticles(ClientWorld level, ParticleEmitterHandler.ItemParticleSupplier emitter, ItemStack stack, boolean isRenderedAfterItem) {
		ScreenParticleItemStackRetrievalKey cacheKey = new ScreenParticleItemStackRetrievalKey(renderingHotbar, isRenderedAfterItem, currentItemX, currentItemY);
		ScreenParticleHolder target = ITEM_PARTICLES.computeIfAbsent(new ScreenParticleItemStackKey(renderingHotbar, isRenderedAfterItem, stack), s -> new ScreenParticleHolder());
		pullFromParticleVault(cacheKey, stack, target, isRenderedAfterItem);
		if (canSpawnParticles) {
			if (isRenderedAfterItem) {
				emitter.spawnLateParticles(target, level, MinecraftClient.getInstance().renderTickCounter.tickDelta, stack, currentItemX, currentItemY);
			} else {
				emitter.spawnEarlyParticles(target, level, MinecraftClient.getInstance().renderTickCounter.tickDelta, stack, currentItemX, currentItemY);
			}
		}
		ACTIVELY_ACCESSED_KEYS.add(cacheKey);
		return target;
	}

	public static void pullFromParticleVault(ScreenParticleItemStackRetrievalKey cacheKey, ItemStack currentStack, ScreenParticleHolder target, boolean isRenderedAfterItem) {
		if (ITEM_STACK_CACHE.containsKey(cacheKey)) {
			ItemStack oldStack = ITEM_STACK_CACHE.get(cacheKey);
			if (oldStack != currentStack && oldStack.getItem().equals(currentStack.getItem())) {
				ScreenParticleItemStackKey oldKey = new ScreenParticleItemStackKey(renderingHotbar, isRenderedAfterItem, oldStack);
				ScreenParticleHolder oldParticles = ITEM_PARTICLES.get(oldKey);
				if (oldParticles != null) {
					target.addFrom(oldParticles);
				}
				ITEM_STACK_CACHE.remove(cacheKey);
				ITEM_PARTICLES.remove(oldKey);
			}
		}
		ITEM_STACK_CACHE.put(cacheKey, currentStack);
	}

	public static void renderItemStackLate() {
		if (cachedItemParticles != null) {
			renderParticles(cachedItemParticles);
			cachedItemParticles = null;
		}
	}

	public static void renderParticles() {
        if (!ClientConfig.ENABLE_SCREEN_PARTICLES) {
            return;
        }
        Screen screen = MinecraftClient.getInstance().currentScreen;

        if (screen == null || screen instanceof ChatScreen || screen instanceof GameModeSelectionScreen) {
            renderEarliestParticles();
        }
        renderLateParticles();
        canSpawnParticles = false;
    }

	public static void renderEarliestParticles() {
		renderParticles(EARLIEST_PARTICLES);
	}

	public static void renderEarlyParticles() {
		renderParticles(EARLY_PARTICLES);
	}

	public static void renderLateParticles() {
		renderParticles(LATE_PARTICLES);
	}

	private static void renderParticles(ScreenParticleHolder screenParticleTarget) {
		if (!ClientConfig.ENABLE_SCREEN_PARTICLES) {
			return;
		}
		screenParticleTarget.particles.forEach((renderType, particles) -> {
			renderType.begin(TESSELATOR.getBuffer(), MinecraftClient.getInstance().getTextureManager());
			for (ScreenParticle next : particles) {
				next.render(TESSELATOR.getBuffer());
			}
			renderType.draw(TESSELATOR);
		});
	}

	public static void clearParticles() {
		clearParticles(EARLIEST_PARTICLES);
		clearParticles(EARLY_PARTICLES);
		clearParticles(LATE_PARTICLES);
		ITEM_PARTICLES.values().forEach(ScreenParticleHandler::clearParticles);
	}

	public static void clearParticles(ScreenParticleHolder screenParticleTarget) {
		screenParticleTarget.particles.values().forEach(ArrayList::clear);
	}

	@SuppressWarnings("unchecked")
	public static <T extends ScreenParticleEffect> ScreenParticle addParticle(ScreenParticleHolder screenParticleTarget, T options, double x, double y, double xMotion, double yMotion) {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		ScreenParticleType<T> type = (ScreenParticleType<T>) options.type;
		ScreenParticle particle = type.factory.createParticle(minecraft.world, options, x, y, xMotion, yMotion);
		ArrayList<ScreenParticle> list = screenParticleTarget.particles.computeIfAbsent(options.renderType, (a) -> new ArrayList<>());
		list.add(particle);
		return particle;
	}
}
