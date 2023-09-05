package com.sammy.lodestone.mixin;

import com.sammy.lodestone.handlers.screenparticle.ScreenParticleHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
final class HandledScreenMixin {
	@Inject(at = @At("RETURN"), method = "render")
	private void lodestone$beforeTooltipParticle(DrawContext graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		ScreenParticleHandler.renderEarlyParticles();
	}
}
