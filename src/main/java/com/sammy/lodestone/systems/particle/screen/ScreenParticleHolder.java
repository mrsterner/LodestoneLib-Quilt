package com.sammy.lodestone.systems.particle.screen;

import com.sammy.lodestone.systems.particle.screen.base.ScreenParticle;

import java.util.*;

public class ScreenParticleHolder {

	public final Map<LodestoneScreenParticleTextureSheet, ArrayList<ScreenParticle>> particles = new HashMap<>();

	public ScreenParticleHolder() {
	}

	public void tick() {
		particles.forEach((pair, particles) -> {
			Iterator<ScreenParticle> iterator = particles.iterator();
			while (iterator.hasNext()) {
				ScreenParticle particle = iterator.next();
				particle.tick();
				if (!particle.isAlive()) {
					iterator.remove();
				}
			}
		});
	}

	public void addFrom(ScreenParticleHolder otherHolder) {
		particles.putAll(otherHolder.particles);
	}

	public boolean isEmpty() {
		return particles.values().stream().allMatch(ArrayList::isEmpty);
	}
}
