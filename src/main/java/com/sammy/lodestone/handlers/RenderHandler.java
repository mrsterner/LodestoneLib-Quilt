package com.sammy.lodestone.handlers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sammy.lodestone.helpers.RenderHelper;
import com.sammy.lodestone.systems.rendering.shader.ExtendedShader;
import com.sammy.lodestone.systems.rendering.renderlayer.ShaderUniformHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static com.sammy.lodestone.systems.rendering.RenderPhases.NORMAL_TRANSPARENCY;

public class RenderHandler {
	public static HashMap<RenderLayer, BufferBuilder> BUFFERS = new HashMap<>();
	public static HashMap<RenderLayer, BufferBuilder> PARTICLE_BUFFERS = new HashMap<>();
	public static boolean LARGER_BUFFER_SOURCES = FabricLoader.getInstance().isModLoaded("sodium");

	public static HashMap<RenderLayer, ShaderUniformHandler> UNIFORM_HANDLERS = new HashMap<>();
	public static VertexConsumerProvider.Immediate DELAYED_RENDER;
	public static VertexConsumerProvider.Immediate DELAYED_PARTICLE_RENDER;

	public static Matrix4f MATRIX4F;

	public static float FOG_NEAR;
	public static float FOG_FAR;
	public static FogShape FOG_SHAPE;
	public static float FOG_RED, FOG_GREEN, FOG_BLUE;

	public static void init() {
		int size = LARGER_BUFFER_SOURCES ? 262144 : 256;
		DELAYED_RENDER = VertexConsumerProvider.immediate(BUFFERS, new BufferBuilder(size));
		DELAYED_PARTICLE_RENDER = VertexConsumerProvider.immediate(PARTICLE_BUFFERS, new BufferBuilder(size));
	}
	public static void cacheFogData(float near, float far, FogShape shape) {
		FOG_NEAR = near;
		FOG_FAR = far;
		FOG_SHAPE = shape;
	}

	public static void cacheFogData(float r, float g, float b) {
		FOG_RED = r;
		FOG_GREEN = g;
		FOG_BLUE = b;
	}

	public static void beginBufferedRendering(MatrixStack matrixStack) {
		matrixStack.push();
		LightmapTextureManager lightTexture = MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager();
		lightTexture.enable();
		RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);

		float fogRed = RenderSystem.getShaderFogColor()[0];
		float fogGreen = RenderSystem.getShaderFogColor()[1];
		float fogBlue = RenderSystem.getShaderFogColor()[2];
		float shaderFogStart = RenderSystem.getShaderFogStart();
		float shaderFogEnd = RenderSystem.getShaderFogEnd();
		FogShape shaderFogShape = RenderSystem.getShaderFogShape();

		RenderSystem.setShaderFogStart(FOG_NEAR);
		RenderSystem.setShaderFogEnd(FOG_FAR);
		RenderSystem.setShaderFogShape(FOG_SHAPE);
		RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE);

		FOG_RED = fogRed;
		FOG_GREEN = fogGreen;
		FOG_BLUE = fogBlue;

		FOG_NEAR = shaderFogStart;
		FOG_FAR = shaderFogEnd;
		FOG_SHAPE = shaderFogShape;
	}

	public static void renderBufferedParticles(boolean transparentOnly) {
		renderBufferedBatches(DELAYED_PARTICLE_RENDER, PARTICLE_BUFFERS, transparentOnly);
	}

	public static void renderBufferedBatches(boolean transparentOnly) {
		renderBufferedBatches(DELAYED_RENDER, BUFFERS, transparentOnly);
	}

	private static void renderBufferedBatches(VertexConsumerProvider.Immediate bufferSource, HashMap<RenderLayer, BufferBuilder> buffer, boolean transparentOnly) {
		Collection<RenderLayer> transparentRenderTypes = new ArrayList<>();
		for (RenderLayer renderType : buffer.keySet()) {
			RenderPhase.Transparency transparency = RenderHelper.getTransparencyShard(renderType);
			if (transparency.equals(NORMAL_TRANSPARENCY)) {
				transparentRenderTypes.add(renderType);
			}
		}
		if (transparentOnly) {
			draw(bufferSource, transparentRenderTypes);
		}
		else {
			Collection<RenderLayer> nonTransparentRenderTypes = new ArrayList<>(buffer.keySet());
			nonTransparentRenderTypes.removeIf(transparentRenderTypes::contains);
			draw(bufferSource, nonTransparentRenderTypes);
		}
	}

	public static void endBufferedRendering(MatrixStack poseStack) {
		LightmapTextureManager lightTexture = MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager();
		RenderSystem.setShaderFogStart(FOG_NEAR);
		RenderSystem.setShaderFogEnd(FOG_FAR);
		RenderSystem.setShaderFogShape(FOG_SHAPE);
		RenderSystem.setShaderFogColor(FOG_RED, FOG_GREEN, FOG_BLUE);

		poseStack.pop();
		lightTexture.disable();
		RenderSystem.disableCull();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(true);
	}

	public static void draw(VertexConsumerProvider.Immediate source, Collection<RenderLayer> buffers) {
		for (RenderLayer type : buffers) {
			ShaderProgram instance = RenderHelper.getShader(type);
			if (UNIFORM_HANDLERS.containsKey(type)) {
				ShaderUniformHandler handler = UNIFORM_HANDLERS.get(type);
				handler.updateShaderData(instance);
			}
			source.draw(type);
			if (instance instanceof ExtendedShader extendedShaderInstance) {
				extendedShaderInstance.setUniformDefaults();
			}
		}
		source.draw();
	}

	public static void addRenderLayer(RenderLayer type) {
		int size = LARGER_BUFFER_SOURCES ? 262144 : type.getExpectedBufferSize();
		HashMap<RenderLayer, BufferBuilder> buffers = BUFFERS;
		if (type.name.contains("particle")) {
			buffers = PARTICLE_BUFFERS;
		}
		buffers.put(type, new BufferBuilder(size));
	}
}
