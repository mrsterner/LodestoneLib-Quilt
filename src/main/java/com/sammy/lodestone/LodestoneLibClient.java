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
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import static com.sammy.lodestone.LodestoneLib.MODID;

public class LodestoneLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		MidnightConfig.init(MODID, ClientConfig.class);

		LodestoneRenderLayerRegistry.yea();
		RenderHandler.init();
		ParticleEmitterHandler.registerParticleEmitters();

		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
			MatrixStack matrixStack = context.matrixStack();
			Vec3d cameraPos = context.camera().getPos();
			matrixStack.push();
			matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
			RenderHandler.MATRIX4F = new Matrix4f(RenderSystem.getModelViewMatrix());
			matrixStack.pop();
		});

		WorldRenderEvents.LAST.register(context -> {

			MatrixStack poseStack = context.matrixStack();
			var levelRenderer = context.worldRenderer();
			Vec3d cameraPos = context.camera().getPos();
			poseStack.push();
			poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);


			Matrix4f last = RenderSystem.getModelViewMatrix();
			Matrix4f copy = new Matrix4f(last);
			if (levelRenderer.transparencyShader != null) {
				MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
			}
			RenderHandler.beginBufferedRendering(poseStack);
			RenderHandler.renderBufferedParticles(true);
			if (RenderHandler.MATRIX4F != null) {
				RenderSystem.getModelViewMatrix().set(RenderHandler.MATRIX4F);
			}
			RenderHandler.renderBufferedBatches(true);
			RenderHandler.renderBufferedBatches(false);
			RenderSystem.getModelViewMatrix().set(copy);
			RenderHandler.renderBufferedParticles(false);

			RenderHandler.endBufferedRendering(poseStack);
			if (levelRenderer.transparencyShader != null) {
				levelRenderer.getCloudsFramebuffer().beginWrite(false);
			}

			poseStack.pop();
		});

		ClientPlayNetworking.registerGlobalReceiver(ScreenshakePacket.ID, (client, handler, buf, responseSender) -> new ScreenshakePacket(buf).apply(client.getNetworkHandler()));
		ClientPlayNetworking.registerGlobalReceiver(PositionedScreenshakePacket.ID, (client, handler, buf, responseSender) -> PositionedScreenshakePacket.fromBuf(buf).apply(client.getNetworkHandler()));
		ClientPlayNetworking.registerGlobalReceiver(SyncWorldEventPacket.ID, SyncWorldEventPacket::handle);
	}
}
