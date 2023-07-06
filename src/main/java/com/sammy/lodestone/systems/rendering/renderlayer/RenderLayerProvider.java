package com.sammy.lodestone.systems.rendering.renderlayer;

import com.sammy.lodestone.setup.LodestoneRenderLayerRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class RenderLayerProvider {
	private final Function<Identifier, RenderLayer> function;
	private final Function<Identifier, RenderLayer> memorizedFunction;

	public RenderLayerProvider(Function<Identifier, RenderLayer> function) {
		this.function = function;
		this.memorizedFunction = Util.memoize(function);
	}

	public RenderLayer apply(Identifier texture) {
		return function.apply(texture);
	}

	public RenderLayer apply(Identifier texture, ShaderUniformHandler uniformHandler) {
		return LodestoneRenderLayerRegistry.applyUniformChanges(function.apply(texture), uniformHandler);
	}

	public RenderLayer applyAndCache(Identifier texture) {
		return this.memorizedFunction.apply(texture);
	}
}
