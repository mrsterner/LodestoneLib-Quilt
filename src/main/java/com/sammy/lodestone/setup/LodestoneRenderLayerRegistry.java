package com.sammy.lodestone.setup;

import com.mojang.datafixers.util.Pair;
import com.sammy.lodestone.handlers.RenderHandler;
import com.sammy.lodestone.systems.rendering.RenderPhases;
import com.sammy.lodestone.systems.rendering.renderlayer.ShaderUniformHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.function.Function;

import static com.sammy.lodestone.LodestoneLib.MODID;
import static com.sammy.lodestone.handlers.RenderHandler.LARGER_BUFFER_SOURCES;


public class LodestoneRenderLayerRegistry extends RenderPhase {
	public LodestoneRenderLayerRegistry(String string, Runnable runnable, Runnable runnable2) {
		super(string, runnable, runnable2);
	}

	public static void yea() {}
	/**
	 * Stores many copies of render types, a copy is a new instance of a render type with the same properties.
	 * It's useful when we want to apply different uniform changes with each separate use of our render type.
	 * Use the {@link #copyAndStore(int, RenderLayer)} {@link #copy(RenderLayer)} methods to create copies.
	 */

	public static final HashMap<Pair<Integer, RenderLayer>, RenderLayer> COPIES = new HashMap<>();

	public static final Function<RenderLayerData, RenderLayer> GENERIC = (data) -> createGenericRenderLayer(data.name, data.format, data.mode, data.shader, data.transparency, data.texture);


	public static final RenderLayer ADDITIVE_PARTICLE = createGenericRenderLayer(MODID, "additive_particle", VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.PARTICLE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
	public static final RenderLayer ADDITIVE_BLOCK = createGenericRenderLayer(MODID, "block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.ADDITIVE_TEXTURE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
	public static final RenderLayer ADDITIVE_SOLID = createGenericRenderLayer(MODID, "additive_solid", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, RenderPhase.POSITION_COLOR_LIGHTMAP_PROGRAM, RenderPhases.ADDITIVE_TRANSPARENCY);

	public static final RenderLayer TRANSPARENT_PARTICLE = createGenericRenderLayer(MODID, "transparent_particle", VertexFormats.POSITION_TEXTURE_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.PARTICLE.phase, RenderPhases.NORMAL_TRANSPARENCY, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
	public static final RenderLayer TRANSPARENT_BLOCK = createGenericRenderLayer(MODID, "transparent_block", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, RenderPhase.POSITION_COLOR_LIGHTMAP_PROGRAM, RenderPhases.NORMAL_TRANSPARENCY, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
	public static final RenderLayer TRANSPARENT_SOLID = createGenericRenderLayer(MODID, "transparent_solid", VertexFormats.POSITION_COLOR_LIGHT, VertexFormat.DrawMode.QUADS, RenderPhase.POSITION_COLOR_LIGHTMAP_PROGRAM, RenderPhases.NORMAL_TRANSPARENCY);


	public static final RenderLayer LUMITRANSPARENT_PARTICLE = copyWithUniformChanges(TRANSPARENT_PARTICLE, ShaderUniformHandler.LUMITRANSPARENT);
	public static final RenderLayer LUMITRANSPARENT_BLOCK = copyWithUniformChanges(TRANSPARENT_BLOCK, ShaderUniformHandler.LUMITRANSPARENT);
	public static final RenderLayer LUMITRANSPARENT_SOLID = copyWithUniformChanges(TRANSPARENT_SOLID, ShaderUniformHandler.LUMITRANSPARENT);

	/**
	 * Render Functions. You can create Render Types by statically applying these to your texture. Alternatively, use {@link #GENERIC} if none of the presets suit your needs.
	 */

	public static final RenderLayerProvider TEXTURE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "texture", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, RenderPhase.POSITION_COLOR_LIGHTMAP_PROGRAM, RenderPhases.NO_TRANSPARENCY, texture));

	public static final RenderLayerProvider TRANSPARENT_TEXTURE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "transparent_texture", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, RenderPhase.POSITION_COLOR_TEXTURE_LIGHTMAP_PROGRAM, RenderPhases.NORMAL_TRANSPARENCY, texture));
	public static final RenderLayerProvider TRANSPARENT_TEXTURE_TRIANGLE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "transparent_texture_triangle", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.TRIANGLE_TEXTURE.phase, RenderPhases.NORMAL_TRANSPARENCY, texture));

	public static final RenderLayerProvider ADDITIVE_TEXTURE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "additive_texture", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.ADDITIVE_TEXTURE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, texture));
	public static final RenderLayerProvider ADDITIVE_TEXTURE_TRIANGLE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "additive_texture_triangle", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.TRIANGLE_TEXTURE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, texture));

	public static final RenderLayerProvider RADIAL_NOISE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "radial_noise", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.RADIAL_NOISE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, texture));
	public static final RenderLayerProvider RADIAL_SCATTER_NOISE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "radial_scatter_noise", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.RADIAL_SCATTER_NOISE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, texture));
	public static final RenderLayerProvider SCROLLING_TEXTURE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "scrolling_texture", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.SCROLLING_TEXTURE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, texture));
	public static final RenderLayerProvider SCROLLING_TEXTURE_TRIANGLE = new RenderLayerProvider((texture) -> createGenericRenderLayer(texture.getNamespace(), "scrolling_texture_triangle", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, LodestoneShaderRegistry.SCROLLING_TRIANGLE_TEXTURE.phase, RenderPhases.ADDITIVE_TRANSPARENCY, texture));

	/**
	 * Creates a custom render type with a texture.
	 */
	public static RenderLayer createGenericRenderLayer(String modId, String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.ShaderProgram shader, RenderPhase.Transparency transparency, Identifier texture) {
		return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, new RenderPhase.Texture(texture, false, false));
	}
	/**
	 * Creates a custom render type with an empty texture state.
	 */
	public static RenderLayer createGenericRenderLayer(String modId, String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.ShaderProgram shader, RenderPhase.Transparency transparency, RenderPhase.TextureBase texture) {
		return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, texture);
	}

	/**
	 * Creates a custom render type with an empty texture.
	 */
	public static RenderLayer createGenericRenderLayer(String modId, String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.ShaderProgram shader, RenderPhase.Transparency transparency) {
		return createGenericRenderLayer(modId + ":" + name, format, mode, shader, transparency, RenderPhase.NO_TEXTURE);
	}

	/**
	 * Creates a custom render type and creates a buffer builder for it.
	 */
	public static RenderLayer createGenericRenderLayer(String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.ShaderProgram shader, RenderPhase.Transparency transparency, RenderPhase.TextureBase texture) {
		RenderLayer type = RenderLayer.of(
				name, format, mode, FabricLoader.getInstance().isModLoaded("sodium") ? 262144 : 256, false, false, RenderLayer.MultiPhaseParameters.builder()
						.program(shader)
						.transparency(transparency)
						.texture(texture)
						.cull(new RenderPhase.Cull(true))
						.build(true)
		);
		RenderHandler.addRenderLayer(type);
		return type;
	}

	/**
	 * Creates a custom render type and creates a buffer builder for it.
	 */
	public static RenderLayer createGenericRenderLayer(String name, VertexFormat format, VertexFormat.DrawMode mode, RenderLayer.MultiPhaseParameters.Builder builder) {
		RenderLayer type = RenderLayer.of(name, format, mode, LARGER_BUFFER_SOURCES ? 262144 : 256, false, false, builder.build(true));
		RenderHandler.addRenderLayer(type);
		return type;
	}

	public static RenderLayer copyWithUniformChanges(RenderLayer type, ShaderUniformHandler handler) {
		return applyUniformChanges(copy(type), handler);
	}

	/**
	 * Queues shader uniform changes for a render type. When we end batches in {@link RenderHandler}}, we do so one render type at a time.
	 * Prior to ending a batch, we run {@link ShaderUniformHandler#updateShaderData(net.minecraft.client.gl.ShaderProgram)} (ShaderProgram)} (ShaderInstance)} if one is present for a given render type.
	 */
	public static RenderLayer applyUniformChanges(RenderLayer type, ShaderUniformHandler handler) {
		RenderHandler.UNIFORM_HANDLERS.put(type, handler);
		return type;
	}

	/**
	 * Creates a copy of a render type.
	 */
	public static RenderLayer copy(RenderLayer type) {
		return GENERIC.apply(new RenderLayerData((RenderLayer.MultiPhase) type));
	}

	public static RenderLayer copyAndStore(int index, RenderLayer type) {
		return COPIES.computeIfAbsent(Pair.of(index, type), (p) -> GENERIC.apply(new RenderLayerData((RenderLayer.MultiPhase) type)));
	}

	/**
	 * Stores all relevant data from a RenderLayer.
	 */
	public static class RenderLayerData {
		public final String name;
		public final VertexFormat format;
		public final VertexFormat.DrawMode mode;
		public final RenderPhase.ShaderProgram shader;
		public RenderPhase.Transparency transparency = RenderPhases.ADDITIVE_TRANSPARENCY;
		public final RenderPhase.TextureBase texture;

		public RenderLayerData(String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.ShaderProgram shader, RenderPhase.TextureBase texture) {
			this.name = name;
			this.format = format;
			this.mode = mode;
			this.shader = shader;
			this.texture = texture;
		}

		public RenderLayerData(String name, VertexFormat format, VertexFormat.DrawMode mode, RenderPhase.ShaderProgram shader, RenderPhase.Transparency transparency, RenderPhase.TextureBase texture) {
			this(name, format, mode, shader, texture);
			this.transparency = transparency;
		}

		public RenderLayerData(RenderLayer.MultiPhase type) {
			this(type.name, type.getVertexFormat(), type.getDrawMode(), type.phases.program, type.phases.transparency, type.phases.texture);
		}


	}

	public static class RenderLayerProvider {
		private final Function<Identifier, RenderLayer> function;
		private final Function<Identifier, RenderLayer> memorizedFunction;

		public RenderLayerProvider(Function<Identifier, RenderLayer> function) {
			this.function = function;
			this.memorizedFunction = Util.memoize(function);
		}

		public RenderLayer apply(Identifier texture) {
			return function.apply(texture);
		}

		public RenderLayer applyAndCache(Identifier texture) {
			return this.memorizedFunction.apply(texture);
		}
	}
}
