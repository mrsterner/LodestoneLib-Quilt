package com.sammy.lodestone.mixin;

import com.sammy.lodestone.handlers.PostProcessHandler;
import com.sammy.lodestone.handlers.WorldEventHandler;
import com.sammy.lodestone.setup.LodestoneRenderWorldEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 1))
	public void lodestone$injectionBeforeTransparencyChainProcess(CallbackInfo ci) {
		PostProcessHandler.copyDepthBuffer();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 6))
	private void lodestone$afterSky(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci){
		Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
		matrices.push();
		matrices.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
		WorldEventHandler.ClientOnly.renderWorldEvents(matrices, tickDelta);
		matrices.pop();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 16))
	private void lodestone$afterParticles(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci){
		LodestoneRenderWorldEvent.AFTER_PARTICLES.invoker().afterParticles(matrices, projectionMatrix);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", ordinal = 19))
	private void lodestone$afterParticles2(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci){
		LodestoneRenderWorldEvent.AFTER_PARTICLES.invoker().afterParticles(matrices, projectionMatrix);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", ordinal = 0))
	private void lodestone$afterWeather(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci){
		LodestoneRenderWorldEvent.AFTER_WEATHER.invoker().afterWeather(matrices, projectionMatrix, (WorldRenderer) (Object) this);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", ordinal = 1))
	private void lodestone$afterWeather2(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci){
		LodestoneRenderWorldEvent.AFTER_WEATHER.invoker().afterWeather(matrices, projectionMatrix, (WorldRenderer) (Object)this);
	}
}
