package com.sammy.lodestone;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sammy.lodestone.config.ClientConfig;
import com.sammy.lodestone.handlers.screenparticle.ParticleEmitterHandler;
import com.sammy.lodestone.handlers.RenderHandler;
import com.sammy.lodestone.network.SyncWorldEventPacket;
import com.sammy.lodestone.network.screenshake.PositionedScreenshakePacket;
import com.sammy.lodestone.network.screenshake.ScreenshakePacket;
import com.sammy.lodestone.setup.LodestoneRenderLayerRegistry;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import static com.sammy.lodestone.LodestoneLib.MODID;

public class LodestoneLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MidnightConfig.init(MODID, ClientConfig.class);

		LodestoneRenderLayerRegistry.yea();
		RenderHandler.init();
		ParticleEmitterHandler.registerParticleEmitters();

		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			Camera camera = minecraft.gameRenderer.getCamera();
			Vec3d cameraPos = camera.getPos();
			MatrixStack poseStack = context.matrixStack();
			poseStack.push();
			poseStack.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());

			RenderHandler.MATRIX4F = new Matrix4f(RenderSystem.getModelViewMatrix());

			poseStack.pop();
		});
		WorldRenderEvents.END.register(this::renderLast);


		ClientPlayNetworking.registerGlobalReceiver(ScreenshakePacket.ID, (client, handler, buf, responseSender) -> new ScreenshakePacket(buf).apply(client.getNetworkHandler()));
		ClientPlayNetworking.registerGlobalReceiver(PositionedScreenshakePacket.ID, (client, handler, buf, responseSender) -> PositionedScreenshakePacket.fromBuf(buf).apply(client.getNetworkHandler()));
		ClientPlayNetworking.registerGlobalReceiver(SyncWorldEventPacket.ID, SyncWorldEventPacket::handle);
	}

	private void renderLast(WorldRenderContext worldRenderContext) {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		Camera camera = minecraft.gameRenderer.getCamera();
		Vec3d cameraPos = camera.getPos();
		MatrixStack poseStack = worldRenderContext.matrixStack();
        WorldRenderer levelRenderer = worldRenderContext.worldRenderer();
		poseStack.push();
		poseStack.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());

		Matrix4f last = new Matrix4f(RenderSystem.getModelViewMatrix());
		if (levelRenderer.transparencyPostProcessor != null) {
			MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
		}
		RenderHandler.beginBufferedRendering(poseStack);
		RenderHandler.renderBufferedParticles(true);
		if (RenderHandler.MATRIX4F != null) {
			RenderSystem.getModelViewMatrix().set(RenderHandler.MATRIX4F);
		}
		RenderHandler.renderBufferedBatches(true);
		RenderHandler.renderBufferedBatches(false);
		RenderSystem.getModelViewMatrix().set(last);
		RenderHandler.renderBufferedParticles(false);

		RenderHandler.endBufferedRendering(poseStack);
		if (levelRenderer.transparencyPostProcessor != null) {
			levelRenderer.getCloudsFramebuffer().beginWrite(false);
		}

		poseStack.pop();
	}
}
